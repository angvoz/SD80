/*******************************************************************************
 * Copyright (c) 2008, 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation          
 *     Nokia - create and use backend service. 
 *     IBM Corporation 
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.launching;

import java.util.List;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLookupDirector;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.datamodel.DataModelInitializedEvent;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.ISourceLookup.ISourceLookupDMContext;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.actions.IConnect;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.cdt.dsf.gdb.service.SessionType;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.CSourceLookup;
import org.eclipse.cdt.dsf.mi.service.IMIProcesses;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointsManager;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLISource;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIEnvironmentCD;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIFileExecAndSymbols;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetArgs;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetAutoSolib;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetNonStop;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetSolibSearchPath;
import org.eclipse.cdt.dsf.mi.service.command.commands.MITargetSelect;
import org.eclipse.cdt.dsf.mi.service.command.commands.MITargetSelectCore;
import org.eclipse.cdt.dsf.mi.service.command.commands.RawCommand;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IStatusHandler;

public class FinalLaunchSequence extends Sequence {

    Step[] fSteps = new Step[] {
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
            fTracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), fLaunch.getSession().getId());
            requestMonitor.done();
        }
        @Override
        public void rollBack(RequestMonitor requestMonitor) {
            if (fTracker != null) fTracker.dispose();
            fTracker = null;
            requestMonitor.done();
        }},

        /*
         * Fetch the GDBBackend service for later use
         */
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
            fGDBBackend = fTracker.getService(IGDBBackend.class);
            if (fGDBBackend == null) {
        		requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot obtain GDBBackend service", null)); //$NON-NLS-1$
            }

            requestMonitor.done();
        }},
        /*
         * Fetch the control service for later use
         */
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
            fCommandControl = fTracker.getService(IGDBControl.class);
            if (fCommandControl == null) {
        		requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot obtain control service", null)); //$NON-NLS-1$
            }

            requestMonitor.done();
        }},
        /*
         * Fetch the process service for later use
         */
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
            fProcService = fTracker.getService(IMIProcesses.class);
            if (fProcService == null) {
        		requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot obtain process service", null)); //$NON-NLS-1$
            }

            requestMonitor.done();
        }},
    	/*
    	 * Source the gdbinit file specified in the launch
    	 */
        new Step() { @Override
        public void execute(final RequestMonitor requestMonitor) {
        	try {
        		final String gdbinitFile = fGDBBackend.getGDBInitFile();
        		
        		if (gdbinitFile != null && gdbinitFile.length() > 0) {
        			fCommandControl.queueCommand(
        					new CLISource(fCommandControl.getContext(), gdbinitFile), 
        					new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor) {
        						@Override
        						protected void handleCompleted() {
        							// If the gdbinitFile is the default, then it may not exist and we
        							// should not consider this an error.
        							// If it is not the default, then the user must have specified it and
        							// we want to warn the user if we can't find it.
        							if (!gdbinitFile.equals(IGDBLaunchConfigurationConstants.DEBUGGER_GDB_INIT_DEFAULT )) {
        								requestMonitor.setStatus(getStatus());
        							}
        							requestMonitor.done();
        						}
        					});
        		} else {
        			requestMonitor.done();
        		}
        	} catch (CoreException e) {
        		requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot get gdbinit option", e)); //$NON-NLS-1$
        		requestMonitor.done();
        	}
        }},
    	/*
    	 * Specify the executable file to be debugged and read the symbol table.
    	 */
        new Step() { @Override
        public void execute(final RequestMonitor requestMonitor) {
    		boolean noFileCommand = IGDBLaunchConfigurationConstants.DEBUGGER_USE_SOLIB_SYMBOLS_FOR_APP_DEFAULT;
    		try {
    			noFileCommand = fLaunch.getLaunchConfiguration().getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_USE_SOLIB_SYMBOLS_FOR_APP,
    			                                                              IGDBLaunchConfigurationConstants.DEBUGGER_USE_SOLIB_SYMBOLS_FOR_APP_DEFAULT);
    		} catch (CoreException e) {
    			requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot read use solib symbols for app options", e)); //$NON-NLS-1$
    			requestMonitor.done();
    			return;
    		}

        	final IPath execPath = fGDBBackend.getProgramPath();
        	if (!noFileCommand && execPath != null && !execPath.isEmpty()) {
        		fCommandControl.queueCommand(
       				new MIFileExecAndSymbols(fCommandControl.getContext(), 
       						                 execPath.toPortableString()), 
       				new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor));
        	} else {
        		requestMonitor.done();
        	}
        }},        
    	/*
    	 * Specify the arguments to the executable file
    	 */
        new Step() { @Override
        public void execute(final RequestMonitor requestMonitor) {
    		try {
    			String args = fGDBBackend.getProgramArguments();
    			
        		if (args != null) {
        			fCommandControl.queueCommand(
        					new MIGDBSetArgs(fCommandControl.getContext(), args), 
        					new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor));
        		} else {
        			requestMonitor.done();
        		}
    		} catch (CoreException e) {
    			requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot get inferior arguments", e)); //$NON-NLS-1$
    			requestMonitor.done();
    		}    		
        }},
    	/*
    	 * Specify GDB's working directory
    	 */
        new Step() { @Override
        public void execute(final RequestMonitor requestMonitor) {
   			IPath dir = null;
       		try {
       			dir = fGDBBackend.getGDBWorkingDirectory();
       		} catch (CoreException e) {
       			requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot get working directory", e)); //$NON-NLS-1$
       			requestMonitor.done();
       			return;
      		}

        	if (dir != null) {
        		fCommandControl.queueCommand(
        				new MIEnvironmentCD(fCommandControl.getContext(), dir.toPortableString()), 
        				new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor));
        	} else {
        		requestMonitor.done();
        	}
        }},
    	/*
    	 * Enable non-stop mode if necessary
    	 */
        new Step() { @Override
        public void execute(final RequestMonitor requestMonitor) {
        	boolean isNonStop = false;
    		try {
    			isNonStop = fLaunch.getLaunchConfiguration().getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_NON_STOP,
                        IGDBLaunchConfigurationConstants.DEBUGGER_NON_STOP_DEFAULT);
        	} catch (CoreException e) {    		
        	}

        	// GDBs that don't support non-stop don't allow you to set it to false.
        	// We really should set it to false when GDB supports it though.
        	// Something to fix later.
        	if (isNonStop) {
        		// The raw commands should not be necessary in the official GDB release
        		fCommandControl.queueCommand(
       				new RawCommand(fCommandControl.getContext(), "set target-async on"), //$NON-NLS-1$
       				new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor) {
       					@Override
       					protected void handleSuccess() {
       						fCommandControl.queueCommand(
   								new RawCommand(fCommandControl.getContext(), "set pagination off"),  //$NON-NLS-1$ 
   								new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor) {
   									@Override
   									protected void handleSuccess() {
   										fCommandControl.queueCommand(
											new MIGDBSetNonStop(fCommandControl.getContext(), true), 
											new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor));
      									}
      								});
       					}
					});
        	} else {
        		requestMonitor.done();
        	}
        }},
        /*
         * Tell GDB to automatically load or not the shared library symbols
         */
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
    		try {
    			boolean autolib = fLaunch.getLaunchConfiguration().getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_AUTO_SOLIB,
    					                                                        IGDBLaunchConfigurationConstants.DEBUGGER_AUTO_SOLIB_DEFAULT);
                fCommandControl.queueCommand(
                	new MIGDBSetAutoSolib(fCommandControl.getContext(), autolib), 
                	new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor));
    		} catch (CoreException e) {
    			requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot set shared library option", e)); //$NON-NLS-1$
    			requestMonitor.done();
    		}
        }},
        /*
         * Set the shared library paths
         */
        new Step() { @Override
        public void execute(final RequestMonitor requestMonitor) {
      		try {
    			List<String> p = fGDBBackend.getSharedLibraryPaths();
      		    
   				if (p.size() > 0) {
   					String[] paths = p.toArray(new String[p.size()]);
   	                fCommandControl.queueCommand(
   	                	new MIGDBSetSolibSearchPath(fCommandControl.getContext(), paths), 
   	                	new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor) {
   	                		@Override
   	                		protected void handleSuccess() {
// Sysroot is not available in GDB6.6 and will make the launch fail in that case.
// Let's remove it for now
   	                			requestMonitor.done();
//   	                			// If we are able to set the solib-search-path,
//   	                			// we should disable the sysroot variable, as indicated
//   	                			// in the GDB documentation.  This is to avoid the sysroot
//   	                			// variable finding libraries that were not meant to be found.
//   	        	                fCommandControl.queueCommand(
//   	        	   	                	new MIGDBSetSysroot(fCommandControl.getContext()), 
//   	        	   	                	new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor));
   	                		};
   	                	});
   				} else {
   	                requestMonitor.done();
   				}
    		} catch (CoreException e) {
                requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot set share library paths", e)); //$NON-NLS-1$
                requestMonitor.done();
    		}
    	}},
    	/*
    	 * Setup the source paths
    	 */
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
            CSourceLookup sourceLookup = fTracker.getService(CSourceLookup.class);
            CSourceLookupDirector locator = (CSourceLookupDirector)fLaunch.getSourceLocator();
    		ISourceLookupDMContext sourceLookupDmc = (ISourceLookupDMContext)fCommandControl.getContext();

            sourceLookup.setSourceLookupPath(sourceLookupDmc, locator.getSourceContainers(), requestMonitor);
        }},
    	/*
    	 * Specify the core file to be debugged if we are launching such a debug session.
    	 */
        new Step() {
        	// Need a job because prompter.handleStatus will block
        	class PromptForCoreJob extends Job {
        		DataRequestMonitor<String> fRequestMonitor;

        		public PromptForCoreJob(String name, DataRequestMonitor<String> rm) {
        			super(name);
        			fRequestMonitor = rm;
        		}

        		@Override
        		protected IStatus run(IProgressMonitor monitor) {
        			final IStatus promptStatus = new Status(IStatus.INFO, "org.eclipse.debug.ui", 200/*STATUS_HANDLER_PROMPT*/, "", null); //$NON-NLS-1$//$NON-NLS-2$
        			final IStatus filePrompt = new Status(IStatus.INFO, "org.eclipse.cdt.dsf.gdb.ui", 1001, "", null); //$NON-NLS-1$//$NON-NLS-2$
        			// consult a status handler
        			final IStatusHandler prompter = DebugPlugin.getDefault().getStatusHandler(promptStatus);

        			final Status NO_CORE_STATUS = new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1,
        					LaunchMessages.getString("LocalCDILaunchDelegate.6"), //$NON-NLS-1$
        					null);

        			if (prompter == null) {
        				fRequestMonitor.setStatus(NO_CORE_STATUS);
        				fRequestMonitor.done();
        				return Status.OK_STATUS;
        			} 				

        			try {
        				Object result = prompter.handleStatus(filePrompt, null);
        				if (result instanceof String) {
        					fRequestMonitor.setData((String)result);
        				} else {
        					fRequestMonitor.setStatus(NO_CORE_STATUS);
        				}
        			} catch (CoreException e) {
        				fRequestMonitor.setStatus(NO_CORE_STATUS);
        			}
        			fRequestMonitor.done();

        			return Status.OK_STATUS;
        		}
        	};
        	@Override
        	public void execute(final RequestMonitor requestMonitor) {
        		if (fSessionType == SessionType.CORE) {
        			try {
        				String coreFile = fLaunch.getLaunchConfiguration().getAttribute(ICDTLaunchConfigurationConstants.ATTR_COREFILE_PATH, ""); //$NON-NLS-1$

        				if (coreFile.length() == 0) {
        					new PromptForCoreJob(
        							"Prompt for core file",  //$NON-NLS-1$
        							new DataRequestMonitor<String>(getExecutor(), requestMonitor) {
        								@Override
        								protected void handleSuccess() {
        									String newCoreFile = getData();
        									if (newCoreFile == null || newCoreFile.length()== 0) {
        										requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot get core file path", null)); //$NON-NLS-1$
        										requestMonitor.done();
        									} else {
        			        					fCommandControl.queueCommand(
        			        							new MITargetSelectCore(fCommandControl.getContext(), newCoreFile), 
        			        							new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor));
        									}
        								}
        							}).schedule();
        				} else {
        					fCommandControl.queueCommand(
        							new MITargetSelectCore(fCommandControl.getContext(), coreFile), 
        							new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor));
        				}
        			} catch (CoreException e) {
        				requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot get core file path", e)); //$NON-NLS-1$
        				requestMonitor.done();
        			}
        		} else {
        			requestMonitor.done();
        		}
        	}
        },
        /* 
         * If remote debugging, connect to target.
         */
        new Step() {
        	private boolean fTcpConnection;
            private String fRemoteTcpHost;
            private String fRemoteTcpPort;
            private String fSerialDevice;
            
            private boolean checkConnectionType(RequestMonitor requestMonitor) {
                try {
                	fTcpConnection = fLaunch.getLaunchConfiguration().getAttribute(
                                    IGDBLaunchConfigurationConstants.ATTR_REMOTE_TCP,
                                    false);
                } catch (CoreException e) {
                    requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot retrieve connection mode", e)); //$NON-NLS-1$
                    requestMonitor.done();
                    return false;
                }
                return true;
            }
            
            private boolean getSerialDevice(RequestMonitor requestMonitor) {
                try {
                    fSerialDevice = fLaunch.getLaunchConfiguration().getAttribute(
                                    			IGDBLaunchConfigurationConstants.ATTR_DEV, "invalid"); //$NON-NLS-1$
                } catch (CoreException e) {
                    requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot retrieve serial device", e)); //$NON-NLS-1$
                    requestMonitor.done();
                    return false;
                }
                return true;
            }
            
            private boolean getTcpHost(RequestMonitor requestMonitor) {
                try {
                    fRemoteTcpHost = fLaunch.getLaunchConfiguration().getAttribute(
                    							IGDBLaunchConfigurationConstants.ATTR_HOST, "invalid"); //$NON-NLS-1$
                } catch (CoreException e) {
                    requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot retrieve remote TCP host", e)); //$NON-NLS-1$
                    requestMonitor.done();
                    return false;
                }
                return true;
            }

            private boolean getTcpPort(RequestMonitor requestMonitor) {
                try {
                    fRemoteTcpPort = fLaunch.getLaunchConfiguration().getAttribute(
                                    			IGDBLaunchConfigurationConstants.ATTR_PORT, "invalid"); //$NON-NLS-1$
                } catch (CoreException e) {
                    requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot retrieve remote TCP port", e)); //$NON-NLS-1$
                    requestMonitor.done();
                    return false;
                }
                return true;
            }
            
            @Override
            public void execute(final RequestMonitor requestMonitor) {
               	if (fSessionType == SessionType.REMOTE) {
               		if (!checkConnectionType(requestMonitor)) return;
               		
               		if (fTcpConnection) {
                   		if (!getTcpHost(requestMonitor)) return;
                        if (!getTcpPort(requestMonitor)) return;
                    
                        fCommandControl.queueCommand(
                        		new MITargetSelect(fCommandControl.getContext(), 
                        				           fRemoteTcpHost, fRemoteTcpPort, fAttach), 
                        	    new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor));
               		} else {
               			if (!getSerialDevice(requestMonitor)) return;
                    
                        fCommandControl.queueCommand(
                        		new MITargetSelect(fCommandControl.getContext(), 
                        				           fSerialDevice, fAttach), 
                        	    new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor));
               		}
            	} else {
            		requestMonitor.done();
            	}
            }
        },
        /*
        * If attach session, perform the attach. 
        */
       new Step() {
        	@Override
        	public void execute(final RequestMonitor requestMonitor) {
        		// A local attach can figure out the binary from the attach
        		// command.  This allows the user not to specify the binary
        		// in the launch.  But for breakpoints to work, we must do the
        		// attach before we set the breakpoints, i.e., here.
        		// On the other hand, for a remote attach, we need to specify
        		// the binary anyway, or use the solib command.  In both cases,
        		// breakpoints can be set before we attach.  Therefore, we don't
        		// force an attach here, but wait for the user to decide to connect
        		// using the connect action.
        		if (fAttach && fSessionType != SessionType.REMOTE) {
        			// If we are attaching, get the process id.
        			int pid = -1;
        			try {
        				// have we already been given the pid (maybe from a JUnit test launch or something)
        				pid = fLaunch.getLaunchConfiguration().getAttribute(ICDTLaunchConfigurationConstants.ATTR_ATTACH_PROCESS_ID, -1);
        			} catch (CoreException e) { 
        				// do nothing and fall to below
        			}

        			if (pid != -1) {
        				fProcService.attachDebuggerToProcess(
        						fProcService.createProcessContext(fCommandControl.getContext(), Integer.toString(pid)),
        						new DataRequestMonitor<IDMContext>(getExecutor(), requestMonitor));
        			} else {
        				IConnect connectCommand = (IConnect)fLaunch.getSession().getModelAdapter(IConnect.class);
        				if (connectCommand != null) {
       						connectCommand.connect(requestMonitor);
        				} else {
    						requestMonitor.done();
        				}
        			}
        		} else {
        			requestMonitor.done();
        		}
        	}
        },
        /* 
         * Start tracking the breakpoints once we know we are connected to the target (necessary for remote debugging) 
         */
        new Step() { @Override
        public void execute(final RequestMonitor requestMonitor) {
           	if (fSessionType != SessionType.CORE) {
           		MIBreakpointsManager bpmService = fTracker.getService(MIBreakpointsManager.class);
           		IBreakpointsTargetDMContext breakpointDmc = (IBreakpointsTargetDMContext)fCommandControl.getContext();

           		bpmService.startTrackingBreakpoints(breakpointDmc, requestMonitor);
           	} else {
           		requestMonitor.done();
           	}
        }},
        /*
         * Start the program.
         */
        new Step() {
            @Override
            public void execute(final RequestMonitor requestMonitor) {
               	if (fSessionType != SessionType.CORE) {
               		fCommandControl.start(fLaunch, requestMonitor);
               	} else {
               		requestMonitor.done();
               	}
            }
        },
        /*
         * Indicate that the Data Model has been filled.  This will trigger the Debug view to expand.
         */
        new Step() {
            @Override
            public void execute(final RequestMonitor requestMonitor) {
            	fLaunch.getSession().dispatchEvent(new DataModelInitializedEvent(fCommandControl.getContext()),
            			                           fCommandControl.getProperties());
            	requestMonitor.done();
            }
        },
        /*
         * Cleanup
         */
        new Step() {
            @Override
            public void execute(final RequestMonitor requestMonitor) {
            	fTracker.dispose();
                fTracker = null;
                requestMonitor.done();
            }
        },
    };

    GdbLaunch fLaunch;
    SessionType fSessionType;
    boolean fAttach;

    private IGDBControl fCommandControl;
    private IGDBBackend	fGDBBackend;
    private IMIProcesses fProcService;

    DsfServicesTracker fTracker;
        
    public FinalLaunchSequence(DsfExecutor executor, GdbLaunch launch, SessionType sessionType, boolean attach, IProgressMonitor pm) {
        super(executor, pm, LaunchMessages.getString("FinalLaunchSequence.0"), LaunchMessages.getString("FinalLaunchSequence.1"));     //$NON-NLS-1$ //$NON-NLS-2$
        fLaunch = launch;
        fSessionType = sessionType;
        fAttach = attach;
    }

    @Override
    public Step[] getSteps() {
        return fSteps;
    }
}

