/*******************************************************************************
 * Copyright (c) 2006, 2011 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson 		  - Modified for additional features in DSF Reference implementation
 *     Nokia - create and use backend service.
 *     Vladimir Prus (CodeSourcery) - Support for -data-read-memory-bytes (bug 322658)      
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service.command;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.datamodel.AbstractDMEvent;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControl;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.cdt.dsf.gdb.service.SessionType;
import org.eclipse.cdt.dsf.mi.service.IMIBackend;
import org.eclipse.cdt.dsf.mi.service.IMIBackend.BackendStateChangedEvent;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.MIProcesses.ContainerExitedDMEvent;
import org.eclipse.cdt.dsf.mi.service.command.AbstractCLIProcess;
import org.eclipse.cdt.dsf.mi.service.command.AbstractMIControl;
import org.eclipse.cdt.dsf.mi.service.command.CLIEventProcessor;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.MIControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.MIInferiorProcess;
import org.eclipse.cdt.dsf.mi.service.command.MIInferiorProcess.State;
import org.eclipse.cdt.dsf.mi.service.command.MIRunControlEventProcessor;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.osgi.framework.BundleContext;

/**
 * GDB Debugger control implementation.  This implementation extends the 
 * base MI control implementation to provide the GDB-specific debugger 
 * features.  This includes:<br>
 * - CLI console support,<br>
 * - inferior process status tracking.<br>
 */
public class GDBControl extends AbstractMIControl implements IGDBControl {

    /**
     * Event indicating that the back end process has started.
     */
    private static class GDBControlInitializedDMEvent extends AbstractDMEvent<ICommandControlDMContext> 
        implements ICommandControlInitializedDMEvent
    {
        public GDBControlInitializedDMEvent(ICommandControlDMContext context) {
            super(context);
        }
    }
    
    /**
     * Event indicating that the CommandControl (back end process) has terminated.
     */
    private static class GDBControlShutdownDMEvent extends AbstractDMEvent<ICommandControlDMContext> 
        implements ICommandControlShutdownDMEvent
    {
        public GDBControlShutdownDMEvent(ICommandControlDMContext context) {
            super(context);
        }
    }

    private GDBControlDMContext fControlDmc;

    private IGDBBackend fMIBackend;
    
    private boolean fConnected;
    
    private MIRunControlEventProcessor fMIEventProcessor;
    private CLIEventProcessor fCLICommandProcessor;
    private AbstractCLIProcess fCLIProcess;
    private MIInferiorProcess fInferiorProcess;
    
    private PTY fPty;

    /**
     * @since 3.0
     */
    public GDBControl(DsfSession session, ILaunchConfiguration config, CommandFactory factory) {
    	super(session, false, factory);
    }

    @Override
    protected BundleContext getBundleContext() {
        return GdbPlugin.getBundleContext();
    }
    
    @Override
    public void initialize(final RequestMonitor requestMonitor) {
        super.initialize( new RequestMonitor(getExecutor(), requestMonitor) {
            @Override
            protected void handleSuccess() {
                doInitialize(requestMonitor);
            }
        });
    }

    public void doInitialize(final RequestMonitor requestMonitor) {

        fMIBackend = getServicesTracker().getService(IGDBBackend.class);
    	
        // getId uses the MIBackend service, which is why we must wait until we
        // have it, before we can create this context.
        fControlDmc = new GDBControlDMContext(getSession().getId(), getId()); 

        final Sequence.Step[] initializeSteps = new Sequence.Step[] {
                new CommandMonitoringStep(InitializationShutdownStep.Direction.INITIALIZING),
                new CommandProcessorsStep(InitializationShutdownStep.Direction.INITIALIZING),
                new RegisterStep(InitializationShutdownStep.Direction.INITIALIZING),
            };

        Sequence startupSequence = new Sequence(getExecutor(), requestMonitor) {
            @Override public Step[] getSteps() { return initializeSteps; }
        };
        getExecutor().execute(startupSequence);
    }

    @Override
    public void shutdown(final RequestMonitor requestMonitor) {
        final Sequence.Step[] shutdownSteps = new Sequence.Step[] {
                new RegisterStep(InitializationShutdownStep.Direction.SHUTTING_DOWN),
                new CommandProcessorsStep(InitializationShutdownStep.Direction.SHUTTING_DOWN),
                new CommandMonitoringStep(InitializationShutdownStep.Direction.SHUTTING_DOWN),
            };
        Sequence shutdownSequence = 
        	new Sequence(getExecutor(), 
        				 new RequestMonitor(getExecutor(), requestMonitor) {
        					@Override
        					protected void handleCompleted() {
        						GDBControl.super.shutdown(requestMonitor);
        					}
        				}) {
            @Override public Step[] getSteps() { return shutdownSteps; }
        };
        getExecutor().execute(shutdownSequence);
        
    }        

    public String getId() {
        return fMIBackend.getId();
    }
    
    @Override
    public MIControlDMContext getControlDMContext() {
        return fControlDmc;
    }
    
    public ICommandControlDMContext getContext() {
        return fControlDmc;
    }
    
    public void terminate(final RequestMonitor rm) {
       // To fix bug 234467:
       // Interrupt GDB in case the inferior is running.
       // That way, the inferior will also be killed when we exit GDB.
       //
       if (fInferiorProcess.getState() == State.RUNNING) {
           fMIBackend.interrupt();
       }
       
        // Schedule a runnable to be executed 2 seconds from now.
        // If we don't get a response to the quit command, this 
        // runnable will kill the task.
        final Future<?> forceQuitTask = getExecutor().schedule(
            new DsfRunnable() {
                public void run() {
                    fMIBackend.destroy();
                    rm.done();
                }
                
                @Override
                protected boolean isExecutionRequired() {
                    return false;
                }
            }, 
            2, TimeUnit.SECONDS);
        
        queueCommand(
       		getCommandFactory().createMIGDBExit(fControlDmc),
            new DataRequestMonitor<MIInfo>(getExecutor(), rm) { 
                @Override
                public void handleCompleted() {
                    if (isSuccess()) {
                        // Cancel the time out runnable (if it hasn't run yet).
                        forceQuitTask.cancel(false);
                        rm.done();
                    }
                    // else: the forceQuitTask has or will handle it.
                    // It is good to wait for the forceQuitTask to trigger
                    // to leave enough time for the interrupt() to complete.
                }
            }
        );
    }
    
    /*
     * This method does the necessary work to setup the input/output streams for the
     * inferior process, by either preparing the PTY to be used, to simply leaving
     * the PTY null, which indicates that the input/output streams of the CLI should
     * be used instead; this decision is based on the type of session.
     */
    public void initInferiorInputOutput(final RequestMonitor requestMonitor) {
    	if (fMIBackend.getSessionType() == SessionType.REMOTE || fMIBackend.getIsAttachSession()) {
    		// These types do not use a PTY
    		fPty = null;
    		requestMonitor.done();
    	} else {
    		// These types always use a PTY
    		try {
    			fPty = new PTY();

    			// Tell GDB to use this PTY
    			queueCommand(
    					getCommandFactory().createMIInferiorTTYSet(fControlDmc, fPty.getSlaveName()), 
    					new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor) {
    						@Override
    						protected void handleFailure() {
    							// We were not able to tell GDB to use the PTY
    							// so we won't use it at all.
    			    			fPty = null;
    			        		requestMonitor.done();
    						}
    					});
    		} catch (IOException e) {
    			fPty = null;
        		requestMonitor.done();
    		}
    	}
    }

    /*
     * This method creates a new inferior process object based on the current Pty or output stream.
     */
    public void createInferiorProcess() {
    	if (fPty == null) {
    		fInferiorProcess = new GDBInferiorProcess(GDBControl.this, fMIBackend, fMIBackend.getMIOutputStream());
    	} else {
    		fInferiorProcess = new GDBInferiorProcess(GDBControl.this, fMIBackend, fPty);
    	}
    	
    	// Create the CLI event processor each time this method is called
    	// to reset the internal thread id count
    	// Bug 313372
    	if (fCLICommandProcessor != null) {
    		fCLICommandProcessor.dispose();
    	}
    	fCLICommandProcessor = new CLIEventProcessor(GDBControl.this, fControlDmc);
    }
    
    public boolean isConnected() {
        return fInferiorProcess.getState() != MIInferiorProcess.State.TERMINATED && 
        			(!fMIBackend.getIsAttachSession() || fConnected);
    }

    public void setConnected(boolean connected) {
        fConnected = connected;
    }

    public AbstractCLIProcess getCLIProcess() { 
        return fCLIProcess; 
    }

    public MIInferiorProcess getInferiorProcess() {
        return fInferiorProcess;
    }
    
	/**
	 * @since 2.0
	 */
	public void setTracingStream(OutputStream tracingStream) {
		setMITracingStream(tracingStream);
	}
	
	/** @since 3.0 */
	public void setEnvironment(Properties props, boolean clear, final RequestMonitor rm) {
		int count = 0;
		CountingRequestMonitor countingRm = new CountingRequestMonitor(getExecutor(), rm);

		// First clear the environment if requested.
		if (clear) {
			count++;
			queueCommand(
					getCommandFactory().createCLIUnsetEnv(getContext()),
					new DataRequestMonitor<MIInfo>(getExecutor(), countingRm));	
		}
		
		// Now set the new variables
		for (Entry<Object,Object> property : props.entrySet()) {
			count++;
			String name = (String)property.getKey();
			String value = (String)property.getValue();
			queueCommand(
					getCommandFactory().createMIGDBSetEnv(getContext(), name, value),
					new DataRequestMonitor<MIInfo>(getExecutor(), countingRm));	
		}
		countingRm.setDoneCount(count);
	}
	 
    @DsfServiceEventHandler 
    public void eventDispatched(ICommandControlShutdownDMEvent e) {
        // Handle our "GDB Exited" event and stop processing commands.
        stopCommandProcessing();
    }
    
    @DsfServiceEventHandler 
    public void eventDispatched(BackendStateChangedEvent e) {
        if (e.getState() == IMIBackend.State.TERMINATED && e.getBackendId().equals(fMIBackend.getId())) {
            // Handle "GDB Exited" event, just relay to following event.
            getSession().dispatchEvent(new GDBControlShutdownDMEvent(fControlDmc), getProperties());
        }
    }
    
    /** @since 2.0 */
    @DsfServiceEventHandler 
    public void eventDispatched(ContainerExitedDMEvent e) {
    	if (Platform.getPreferencesService().getBoolean("org.eclipse.cdt.dsf.gdb.ui",  //$NON-NLS-1$
    													IGdbDebugPreferenceConstants.PREF_AUTO_TERMINATE_GDB,
    													true, null)) {
    		// If the inferior finishes, let's terminate GDB
    		terminate(new RequestMonitor(ImmediateExecutor.getInstance(), null));
    	}
    }
    
    public static class InitializationShutdownStep extends Sequence.Step {
        public enum Direction { INITIALIZING, SHUTTING_DOWN }
        
        private Direction fDirection;
        public InitializationShutdownStep(Direction direction) { fDirection = direction; }
        
        @Override
        final public void execute(RequestMonitor requestMonitor) {
            if (fDirection == Direction.INITIALIZING) {
                initialize(requestMonitor);
            } else {
                shutdown(requestMonitor);
            }
        }
        
        @Override
        final public void rollBack(RequestMonitor requestMonitor) {
            if (fDirection == Direction.INITIALIZING) {
                shutdown(requestMonitor);
            } else {
                super.rollBack(requestMonitor);
            }
        }
        
        protected void initialize(RequestMonitor requestMonitor) {
            requestMonitor.done();
        }
        protected void shutdown(RequestMonitor requestMonitor) {
            requestMonitor.done();
        }
    }
    
    protected class CommandMonitoringStep extends InitializationShutdownStep {
        CommandMonitoringStep(Direction direction) { super(direction); }

        @Override
        protected void initialize(final RequestMonitor requestMonitor) {
            startCommandProcessing(fMIBackend.getMIInputStream(), fMIBackend.getMIOutputStream());
            requestMonitor.done();
        }

        @Override
        protected void shutdown(RequestMonitor requestMonitor) {
            stopCommandProcessing();
            requestMonitor.done();
        }
    }
    
    protected class CommandProcessorsStep extends InitializationShutdownStep {
        CommandProcessorsStep(Direction direction) { super(direction); }

        @Override
        public void initialize(final RequestMonitor requestMonitor) {
            try {
                fCLIProcess = new GDBBackendCLIProcess(GDBControl.this, fMIBackend);
            }
            catch(IOException e) {
                requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED, "Failed to create CLI Process", e)); //$NON-NLS-1$
                requestMonitor.done();
                return;
            }

            createInferiorProcess();
            
            fMIEventProcessor = new MIRunControlEventProcessor(GDBControl.this, fControlDmc);

            requestMonitor.done();
        }
        
        @Override
        protected void shutdown(RequestMonitor requestMonitor) {
            fCLICommandProcessor.dispose();
            fMIEventProcessor.dispose();
            fCLIProcess.dispose();
            fInferiorProcess.dispose();

            requestMonitor.done();
        }
    }
    
    protected class RegisterStep extends InitializationShutdownStep {
        RegisterStep(Direction direction) { super(direction); }
        @Override
        public void initialize(final RequestMonitor requestMonitor) {
            getSession().addServiceEventListener(GDBControl.this, null);
            register(
                new String[]{ ICommandControl.class.getName(), 
                              ICommandControlService.class.getName(), 
                              IMICommandControl.class.getName(),
                              AbstractMIControl.class.getName(),
                              IGDBControl.class.getName() }, 
                new Hashtable<String,String>());
            getSession().dispatchEvent(new GDBControlInitializedDMEvent(fControlDmc), getProperties());
            requestMonitor.done();
        }

        @Override
        protected void shutdown(RequestMonitor requestMonitor) {
            unregister();
            getSession().removeServiceEventListener(GDBControl.this);
            requestMonitor.done();
        }
    }

    /**
     * GDBControl is only used for GDB earlier that 7.0. Although -list-features
     * is available in 6.8, it does not report anything we care about, so
     * return empty list.
     */
	private final List<String> fFeatures = new ArrayList<String>();
	/** @since 4.0 */
	public List<String> getFeatures() {
		return fFeatures;
	}
	
	/**
	 * @since 4.0
	 */
	public void enablePrettyPrintingForMIVariableObjects(RequestMonitor rm) {
		rm.done();
	}

	/**
	 * @since 4.0
	 */
	public void setPrintPythonErrors(boolean enabled, RequestMonitor rm) {
		rm.done();
	}
}
