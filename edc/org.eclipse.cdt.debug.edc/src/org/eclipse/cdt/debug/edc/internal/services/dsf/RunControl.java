/*******************************************************************************
 * Copyright (c) 2009, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.services.dsf;

import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.IAddressExpressionEvaluator;
import org.eclipse.cdt.debug.edc.IJumpToAddress;
import org.eclipse.cdt.debug.edc.JumpToAddress;
import org.eclipse.cdt.debug.edc.disassembler.IDisassembledInstruction;
import org.eclipse.cdt.debug.edc.disassembler.IDisassembler;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.IEDCTraceOptions;
import org.eclipse.cdt.debug.edc.internal.formatter.FormatExtensionManager;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Breakpoints.BreakpointDMData;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Modules.ModuleDMC;
import org.eclipse.cdt.debug.edc.internal.snapshot.Album;
import org.eclipse.cdt.debug.edc.internal.snapshot.SnapshotUtils;
import org.eclipse.cdt.debug.edc.services.AbstractEDCService;
import org.eclipse.cdt.debug.edc.services.DMContext;
import org.eclipse.cdt.debug.edc.services.IDSFServiceUsingTCF;
import org.eclipse.cdt.debug.edc.services.IEDCDMContext;
import org.eclipse.cdt.debug.edc.services.IEDCExecutionDMC;
import org.eclipse.cdt.debug.edc.services.IEDCModuleDMContext;
import org.eclipse.cdt.debug.edc.services.IEDCModules;
import org.eclipse.cdt.debug.edc.services.IEDCSymbols;
import org.eclipse.cdt.debug.edc.services.Registers;
import org.eclipse.cdt.debug.edc.services.Registers.RegisterGroupDMC;
import org.eclipse.cdt.debug.edc.services.Stack;
import org.eclipse.cdt.debug.edc.services.Stack.StackFrameDMC;
import org.eclipse.cdt.debug.edc.services.Stack.VariableDMC;
import org.eclipse.cdt.debug.edc.snapshot.IAlbum;
import org.eclipse.cdt.debug.edc.snapshot.ISnapshotContributor;
import org.eclipse.cdt.debug.edc.symbols.IEDCSymbolReader;
import org.eclipse.cdt.debug.edc.symbols.IFunctionScope;
import org.eclipse.cdt.debug.edc.symbols.ILineEntry;
import org.eclipse.cdt.debug.edc.symbols.IModuleLineEntryProvider;
import org.eclipse.cdt.debug.edc.symbols.IScope;
import org.eclipse.cdt.debug.edc.tcf.extension.ProtocolConstants;
import org.eclipse.cdt.debug.edc.tcf.extension.ProtocolConstants.IModuleProperty;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMEvent;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.ICachingService;
import org.eclipse.cdt.dsf.debug.service.IDisassembly.IDisassemblyDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.dsf.debug.service.IModules.IModuleDMContext;
import org.eclipse.cdt.dsf.debug.service.IModules.ISymbolDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterGroupDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl2;
import org.eclipse.cdt.dsf.debug.service.ISourceLookup.ISourceLookupDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IVariableDMContext;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IRunControl.DoneCommand;
import org.eclipse.tm.tcf.services.IRunControl.RunControlContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class RunControl extends AbstractEDCService implements IRunControl2, ICachingService, ISnapshotContributor,
		IDSFServiceUsingTCF {

	public static final String EXECUTION_CONTEXT = "execution_context";
	public static final String EXECUTION_CONTEXT_REGISTERS = "execution_context_registers";
	public static final String EXECUTION_CONTEXT_MODULES = "execution_context_modules";
	public static final String EXECUTION_CONTEXT_FRAMES = "execution_context_frames";
	/**
	 * Context property names.
	 */
	public static final String 
			PROP_PARENT_ID = "ParentID", 
			PROP_IS_CONTAINER = "IsContainer",
			PROP_HAS_STATE = "HasState", 
			PROP_CAN_RESUME = "CanResume", 
			PROP_CAN_COUNT = "CanCount",
			PROP_CAN_SUSPEND = "CanSuspend", 
			PROP_CAN_TERMINATE = "CanTerminate", 
			PROP_IS_SUSPENDED = "State",
			PROP_MESSAGE = "Message", 
			PROP_SUSPEND_PC = "SuspendPC";

	// Whether module is being loaded (if true) or unloaded (if false)

	public static class SuspendedEvent extends AbstractDMEvent<IExecutionDMContext> implements ISuspendedDMEvent {

		private final StateChangeReason reason;
		private final Map<String, Object> params;

		public SuspendedEvent(IExecutionDMContext dmc, StateChangeReason reason, Map<String, Object> params) {
			super(dmc);
			EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.RUN_CONTROL_TRACE,
					new Object[] { dmc, reason, params });
			this.reason = reason;
			this.params = params;
		}

		public StateChangeReason getReason() {
			return reason;
		}

		public Map<String, Object> getParams() {
			return params;
		}
	}

	public static class ResumedEvent extends AbstractDMEvent<IExecutionDMContext> implements IResumedDMEvent {

		public ResumedEvent(IExecutionDMContext dmc) {
			super(dmc);
		}

		public StateChangeReason getReason() {
			return StateChangeReason.USER_REQUEST;
		}
	}

	private static StateChangeReason toDsfStateChangeReason(String tcfReason) {
		if (tcfReason == null)
			return StateChangeReason.UNKNOWN;
		if (tcfReason.equals(org.eclipse.tm.tcf.services.IRunControl.REASON_USER_REQUEST))
			return StateChangeReason.USER_REQUEST;
		if (tcfReason.equals(org.eclipse.tm.tcf.services.IRunControl.REASON_STEP))
			return StateChangeReason.STEP;
		if (tcfReason.equals(org.eclipse.tm.tcf.services.IRunControl.REASON_BREAKPOINT))
			return StateChangeReason.BREAKPOINT;
		if (tcfReason.equals(org.eclipse.tm.tcf.services.IRunControl.REASON_EXCEPTION))
			return StateChangeReason.EXCEPTION;
		if (tcfReason.equals(org.eclipse.tm.tcf.services.IRunControl.REASON_CONTAINER))
			return StateChangeReason.CONTAINER;
		if (tcfReason.equals(org.eclipse.tm.tcf.services.IRunControl.REASON_WATCHPOINT))
			return StateChangeReason.WATCHPOINT;
		if (tcfReason.equals(org.eclipse.tm.tcf.services.IRunControl.REASON_SIGNAL))
			return StateChangeReason.SIGNAL;
		if (tcfReason.equals(org.eclipse.tm.tcf.services.IRunControl.REASON_SHAREDLIB))
			return StateChangeReason.SHAREDLIB;
		if (tcfReason.equals(org.eclipse.tm.tcf.services.IRunControl.REASON_ERROR))
			return StateChangeReason.ERROR;
		return StateChangeReason.UNKNOWN;
	}

	@Immutable
	private static class ExecutionData implements IExecutionDMData2 {
		private final StateChangeReason reason;
		private final String details;

		ExecutionData(StateChangeReason reason, String details) {
			this.reason = reason;
			this.details = details;
		}

		public StateChangeReason getStateChangeReason() {
			return reason;
		}

		public String getDetails() {
			return details;
		}
	}

	public abstract class ExecutionDMC extends DMContext implements IExecutionDMContext,
			ISnapshotContributor, IEDCExecutionDMC {

		private final List<ExecutionDMC> children = Collections.synchronizedList(new ArrayList<ExecutionDMC>());
		private StateChangeReason stateChangeReason = StateChangeReason.UNKNOWN;
		private String stateChangeDetails = null;
		private final RunControlContext tcfContext;
		private final ExecutionDMC parentExecutionDMC;
		private String latestPC = null;
		private RequestMonitor steppingRM = null;
		private boolean isStepping = false;
		private int countOfScheduledNotifications = 0 ;

		public ExecutionDMC(ExecutionDMC parent, Map<String, Object> props, RunControlContext tcfContext) {
			super(RunControl.this, parent == null ? new IDMContext[0] : new IDMContext[] { parent }, props);
			EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.RUN_CONTROL_TRACE,
					new Object[] { parent, properties });
			this.parentExecutionDMC = parent;
			this.tcfContext = tcfContext;
			if (props != null) {
				dmcsByID.put(getID(), this);
			}
			if (parent != null)
				parent.addChild(this);
			EDCDebugger.getDefault().getTrace().traceExit(IEDCTraceOptions.RUN_CONTROL_TRACE);
		}

		private void addChild(ExecutionDMC executionDMC) {
			synchronized (children) {
				children.add(executionDMC);
			}
		}

		private void removeChild(IEDCExecutionDMC executionDMC) {
			synchronized (children) {
				children.remove(executionDMC);
			}
		}

		public ExecutionDMC[] getChildren() {
			synchronized (children) {
				return children.toArray(new ExecutionDMC[children.size()]);
			}
		}

		public abstract ExecutionDMC contextAdded(Map<String, Object> properties, RunControlContext tcfContext);

		public abstract boolean canDetach();
		
		public void loadSnapshot(Element element) throws Exception {
			EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.RUN_CONTROL_TRACE, element);
			NodeList ecElements = element.getElementsByTagName(EXECUTION_CONTEXT);
			int numcontexts = ecElements.getLength();
			for (int i = 0; i < numcontexts; i++) {
				Element contextElement = (Element) ecElements.item(i);
				if (contextElement.getParentNode().equals(element)) {
					try {
						Element propElement = (Element) contextElement.getElementsByTagName(SnapshotUtils.PROPERTIES)
								.item(0);
						HashMap<String, Object> properties = new HashMap<String, Object>();
						SnapshotUtils.initializeFromXML(propElement, properties);
						ExecutionDMC exeDMC = contextAdded(properties, null);
						exeDMC.loadSnapshot(contextElement);
					} catch (CoreException e) {
						EDCDebugger.getMessageLogger().logError(null, e);
					}
				}

			}
			EDCDebugger.getDefault().getTrace().traceExit(IEDCTraceOptions.RUN_CONTROL_TRACE);
		}

		public Element takeShapshot(IAlbum album, Document document, IProgressMonitor monitor) {
			Element contextElement = document.createElement(EXECUTION_CONTEXT);
			contextElement.setAttribute(PROP_ID, this.getID());

			Element propsElement = SnapshotUtils.makeXMLFromProperties(document, getProperties());
			contextElement.appendChild(propsElement);

			ExecutionDMC[] dmcs = getChildren();

			for (ExecutionDMC executionDMC : dmcs) {
				Element dmcElement = executionDMC.takeShapshot(album, document, monitor);
				contextElement.appendChild(dmcElement);
			}

			return contextElement;
		}

		public boolean isSuspended() {
			synchronized (properties) {
				Boolean suspended = (Boolean) properties.get(PROP_IS_SUSPENDED);
				if (suspended != null)
					return suspended;
			}
			return false;
		}

		public StateChangeReason getStateChangeReason() {
			return stateChangeReason;
		}

		public String getStateChangeDetails() {
			return stateChangeDetails;
		}

		public void setIsSuspended(boolean isSuspended) {
			synchronized (properties) {
				properties.put(PROP_IS_SUSPENDED, isSuspended);
			}
			if (getParent() != null)
				getParent().childIsSuspended(isSuspended);
		}

		private void childIsSuspended(boolean isSuspended) {
			if (isSuspended) {
				setIsSuspended(true);
			} else {
				boolean anySuspended = false;
				for (ExecutionDMC childDMC : getChildren()) {
					if (childDMC.isSuspended()) {
						anySuspended = true;
						break;
					}
				}
				if (!anySuspended)
					setIsSuspended(false);
			}
		}

		protected void contextException(String msg) {
	        assert getExecutor().isInExecutorThread();

	        setIsSuspended(true);
			synchronized (properties) {
				properties.put(PROP_MESSAGE, msg);
			}
			stateChangeReason = StateChangeReason.EXCEPTION;
			getSession().dispatchEvent(
					new SuspendedEvent(this, StateChangeReason.EXCEPTION, new HashMap<String, Object>()),
					RunControl.this.getProperties());
		}

		protected void contextSuspended(String pc, String reason, final Map<String, Object> params) {
	        assert getExecutor().isInExecutorThread();

	        EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.RUN_CONTROL_TRACE,
					new Object[] { pc, reason, params });
			if (pc != null) {
				// the PC from TCF agent is decimal string.
				// convert it to hex string.
				pc = Long.toHexString(Long.parseLong(pc));
			}

			latestPC = pc;

			setIsSuspended(true);
			synchronized (properties) {
				properties.put(PROP_MESSAGE, reason);
				properties.put(PROP_SUSPEND_PC, pc);
			}
			stateChangeReason = toDsfStateChangeReason(reason);

			if (stateChangeReason == StateChangeReason.SHAREDLIB) {
				handleModuleEvent(this, params);
			} else {

				stateChangeDetails = (String) params.get(ProtocolConstants.PROP_SUSPEND_DETAIL);
				
				// Show the context is foreground one, if possible.
				//
				Boolean isForeground = (Boolean)params.get(ProtocolConstants.PROP_IS_FOREGROUND);
				if (isForeground == null)
					isForeground = false;
				stateChangeDetails += isForeground ? " [foreground]" : "";
				
				final ExecutionDMC dmc = this;

				final DataRequestMonitor<Boolean> preprocessDrm = new DataRequestMonitor<Boolean>(getExecutor(), null) {
					@Override
					protected void handleCompleted() {
						boolean honorSuspend = getData();
						
						if (honorSuspend) { // do suspend

							// Only after completion of those preprocessing do 
							// we fire the event.
							getSession().dispatchEvent(new SuspendedEvent(dmc, stateChangeReason, params),
									RunControl.this.getProperties());

							// All the following must be done in DSF dispatch
							// thread to ensure data integrity.

							// Mark done of the single step RM, if any pending.
							if (steppingRM != null) {
								steppingRM.done();
								steppingRM = null;
							}

							// Mark any stepping as done.
							setStepping(false);

							// Remove temporary breakpoints set by stepping.
							// Note we don't want to do this on a sharedLibrary
							// event as otherwise
							// stepping will be screwed up by that event.
							//
							Breakpoints bpService = getServicesTracker().getService(Breakpoints.class);
							bpService.removeAllTempBreakpoints(new RequestMonitor(getExecutor(), null));
							
						} else { 
							// ignore suspend if, say, breakpoint condition is not met.
							RunControl.this.resume(dmc, new RequestMonitor(getExecutor(), null));
						}
					}
				};
				
				preprocessOnSuspend(dmc, latestPC, preprocessDrm);
			}
			EDCDebugger.getDefault().getTrace().traceExit(IEDCTraceOptions.RUN_CONTROL_TRACE);
		}

		/**
		 * handle module load event and unload event. A module is an executable file
		 * or a library (e.g. DLL or shared lib).
		 * 
		 * @param dmc
		 * @param moduleProperties
		 */
		private void handleModuleEvent(final IEDCExecutionDMC dmc, final Map<String, Object> moduleProperties) {
			// The following needs be done in DSF dispatch thread.
			getSession().getExecutor().execute(new Runnable() {
				public void run() {
					// based on properties, either load or unload the module
					boolean loaded = true;
					Object loadedValue = moduleProperties.get(IModuleProperty.PROP_MODULE_LOADED);
					if (loadedValue != null) {
						if (loadedValue instanceof Boolean)
							loaded = (Boolean) loadedValue;
					}

					if (loaded)
						handleModuleLoadedEvent(dmc, moduleProperties);
					else
						handleModuleUnloadedEvent(dmc, moduleProperties);
				}
			});
		}
		
		public Boolean canTerminate() {
			EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.RUN_CONTROL_TRACE);
			Boolean result = false;
			synchronized (properties) {
				try {
					result = (Boolean) properties.get(PROP_CAN_TERMINATE);
				} catch (Exception e) {
					EDCDebugger.getDefault().getTrace().trace(IEDCTraceOptions.RUN_CONTROL_TRACE,
							"Error in canTerminate", e);
				}
			}
			EDCDebugger.getDefault().getTrace().traceExit(IEDCTraceOptions.RUN_CONTROL_TRACE, result);
			return result;
		}

		/**
		 * Resume the context.
		 * 
		 * @param rm
		 *            this is marked done as long as the resume command
		 *            succeeds.
		 */
		public boolean supportsStepMode(StepType type) {
			EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.RUN_CONTROL_TRACE, this);

			int mode = 0;
			switch (type) {
			case STEP_OVER:
				mode = org.eclipse.tm.tcf.services.IRunControl.RM_STEP_OVER_RANGE;
				break;
			case STEP_INTO:
				mode = org.eclipse.tm.tcf.services.IRunControl.RM_STEP_INTO_RANGE;
				break;
			case STEP_RETURN:
				mode = org.eclipse.tm.tcf.services.IRunControl.RM_STEP_OUT;
				break;
			case INSTRUCTION_STEP_OVER:
				mode = org.eclipse.tm.tcf.services.IRunControl.RM_STEP_OVER;
				break;
			case INSTRUCTION_STEP_INTO:
				mode = org.eclipse.tm.tcf.services.IRunControl.RM_STEP_INTO;
				break;
			}

			return tcfContext.canResume(mode);
		}

		/**
		 * Resume the context.
		 * 
		 * @param rm
		 *            this is marked done as long as the resume command
		 *            succeeds.
		 */
		public void resume(final RequestMonitor rm) {
			EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.RUN_CONTROL_TRACE, this);

			flushCache(this);

			Protocol.invokeLater(new Runnable() {
				public void run() {
					tcfContext.resume(org.eclipse.tm.tcf.services.IRunControl.RM_RESUME, 0, new DoneCommand() {

						public void doneCommand(IToken token, final Exception error) {
							getExecutor().execute(new Runnable() {
								public void run() {
									if (error == null) {
										contextResumed(false);

										EDCDebugger.getDefault().getTrace().trace(IEDCTraceOptions.RUN_CONTROL_TRACE,
												"Resume command succeeded.");
									} else {
										EDCDebugger.getDefault().getTrace().trace(IEDCTraceOptions.RUN_CONTROL_TRACE,
												"Resume command failed.");
										rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, REQUEST_FAILED,
												"Resume failed.", null));
									}
									rm.done();
								}
							});
						}
					});
				}
			});
			EDCDebugger.getDefault().getTrace().traceExit(IEDCTraceOptions.RUN_CONTROL_TRACE);
		}

		/**
		 * Resume the context but the request monitor is only marked done when
		 * the context is suspended. (vs. regular resume()). <br>
		 * Note this method does not wait for suspended-event.
		 * 
		 * @param rm
		 */
		protected void resumeForStepping(final RequestMonitor rm) {
			EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.RUN_CONTROL_TRACE, this);

			setStepping(true);

			flushCache(this);

			Protocol.invokeLater(new Runnable() {
				public void run() {
					tcfContext.resume(org.eclipse.tm.tcf.services.IRunControl.RM_RESUME, 0, new DoneCommand() {

						public void doneCommand(IToken token, final Exception error) {
							// do this in DSF executor thread.
							getExecutor().execute(new Runnable() {
								public void run() {
									handleTCFResumeDoneForStepping("ResumeForStepping", error, rm);
								}
							});
						}
					});
				}
			});
		
			EDCDebugger.getDefault().getTrace().traceExit(IEDCTraceOptions.RUN_CONTROL_TRACE);
		}

		private void handleTCFResumeDoneForStepping(String command, Exception tcfError, RequestMonitor rm) {
			assert getExecutor().isInExecutorThread();
			
			String msg = command;
			if (tcfError == null) {
				msg += " succeeded.";
				EDCDebugger.getDefault().getTrace().trace(IEDCTraceOptions.RUN_CONTROL_TRACE, msg);
				contextResumed(false);

				// we'll mark it as done when we get next
				// suspend event.
				assert steppingRM == null;
				steppingRM = rm;
			} else {
				msg += " failed.";
				EDCDebugger.getDefault().getTrace().trace(IEDCTraceOptions.RUN_CONTROL_TRACE, msg);

				setStepping(false);
				rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, REQUEST_FAILED, msg, tcfError));
				rm.done();
			}
		}

		public void suspend(final RequestMonitor requestMonitor) {
			EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.RUN_CONTROL_TRACE, this);
			Protocol.invokeLater(new Runnable() {
				public void run() {
					tcfContext.suspend(new DoneCommand() {

						public void doneCommand(IToken token, Exception error) {
							EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.RUN_CONTROL_TRACE, this);
							requestMonitor.done();
							EDCDebugger.getDefault().getTrace().traceExit(IEDCTraceOptions.RUN_CONTROL_TRACE);
						}
					});
				}
			});
			EDCDebugger.getDefault().getTrace().traceExit(IEDCTraceOptions.RUN_CONTROL_TRACE);
		}

		public void terminate(final RequestMonitor requestMonitor) {
			EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.RUN_CONTROL_TRACE, this);
			if (tcfContext != null) {
				Protocol.invokeLater(new Runnable() {
					public void run() {
						tcfContext.terminate(new DoneCommand() {

							public void doneCommand(IToken token, Exception error) {
								EDCDebugger.getDefault().getTrace()
										.traceEntry(IEDCTraceOptions.RUN_CONTROL_TRACE, this);
								if (error != null) {
									requestMonitor.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, 
											"terminate() failed.", error));
								}
								
								requestMonitor.done();
								EDCDebugger.getDefault().getTrace().traceExit(IEDCTraceOptions.RUN_CONTROL_TRACE);
							}
						});
					}
				});
			} else {	
				// Snapshots, for e.g., don't have a TCF RunControlContext, so just remove all the contexts recursively
				detachAllContexts();
			}
			EDCDebugger.getDefault().getTrace().traceExit(IEDCTraceOptions.RUN_CONTROL_TRACE);
		}

		protected ExecutionDMC getParent() {
			return parentExecutionDMC;
		}

		/**
		 * get latest PC register value of the context.
		 * 
		 * @return hex string of the PC value.
		 */
		public String getPC() {
			return latestPC;
		}
		
		/**
		 * Change cached PC value.
		 * This is only supposed to be used for move-to-line & resume-from-line commands.
		 *  
		 * @param pc
		 */
		private void setPC(String pc) {
			latestPC = pc;
		}

		/**
		 * Detach debugger from this context and all its children and grand-children.
		 * This is to purge the context from debugger UI and internal storage.
		 */
		public void detachFromDebugger(){
			EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.RUN_CONTROL_TRACE);

			for (ExecutionDMC e : getChildren())
				// recursively forget children first
				e.detachFromDebugger();

			ExecutionDMC parent = getParent();
			if (parent != null)
				parent.removeChild(this);
			
			getSession().dispatchEvent(new ExitedEvent(this), RunControl.this.getProperties());
			
			if (getRootDMC().getChildren().length == 0) 
				// no more contexts under debug, fire exitedEvent for the rootDMC which
				// will trigger shutdown of the debug session.
				// See EDCLaunch.eventDispatched(IExitedDMEvent e).
				getSession().dispatchEvent(new ExitedEvent(getRootDMC()), RunControl.this.getProperties());
			
			EDCDebugger.getDefault().getTrace().traceExit(IEDCTraceOptions.RUN_CONTROL_TRACE);
		}

		/**
		 * Recursively marks all execution contexts as resumed
		 * @param dmc
		 */
		public void resumeAll(){
			contextResumed(true);			
			for (ExecutionDMC e : getChildren()){
				e.resumeAll();
			}
		}

		protected void contextResumed(boolean fireResumeEventNow) {
	        assert getExecutor().isInExecutorThread();

	        if (children.size() > 0) {
	        	// If it has kids (e.g. a process has threads), only need
	        	// to mark the kids as resumed.
		        for (ExecutionDMC e : children){
					e.contextResumed(fireResumeEventNow);
				}
		        return;
	        }
	        
			EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.RUN_CONTROL_TRACE,
					new Object[] { this, fireResumeEventNow });
			
			setIsSuspended(false);
			
			if (fireResumeEventNow)
				getSession().dispatchEvent(new ResumedEvent(this), RunControl.this.getProperties());
			else
				scheduleResumeEvent();

			EDCDebugger.getDefault().getTrace().traceExit(IEDCTraceOptions.RUN_CONTROL_TRACE);
		}

		/** 
		 * Schedule a task to run after some time which will
		 * notify platform that the context is running.
		 */
		private void scheduleResumeEvent() {
			countOfScheduledNotifications++;

			final IExecutionDMContext dmc = this;
			
			Runnable notifyPlatformTask = new Runnable() {
				public void run() {
					/*
					 * Notify platform the context is running.
					 * 
					 * But don't do that if another such task is scheduled
					 * (namely current stepping is done within the 2 seconds and
					 * another stepping/resume is underway).
					 */
					countOfScheduledNotifications--;
					if (countOfScheduledNotifications == 0 && !isSuspended())
						getSession().dispatchEvent(new ResumedEvent(dmc), RunControl.this.getProperties());
				}};
			
			getExecutor().schedule(notifyPlatformTask, 2000, TimeUnit.MILLISECONDS);
		}

		/**
		 * Execute a single instruction. Note the "rm" is marked done() only
		 * when we get the suspend event, not when we successfully send the
		 * command to TCF agent.
		 * 
		 * @param rm
		 */
		protected void singleStep(final boolean stepInto, final RequestMonitor rm) {
			EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.RUN_CONTROL_TRACE, this.getName());

			setStepping(true);

			flushCache(this);

			Protocol.invokeLater(new Runnable() {
				public void run() {
					int mode = stepInto ? org.eclipse.tm.tcf.services.IRunControl.RM_STEP_INTO
							: org.eclipse.tm.tcf.services.IRunControl.RM_STEP_OVER;
					tcfContext.resume(mode, 1, new DoneCommand() {
						public void doneCommand(IToken token, final Exception error) {
							// do this in DSF executor thread.
							getExecutor().execute(new Runnable() {
								public void run() {
									handleTCFResumeDoneForStepping("SingleStep", error, rm);
								}
							});
						}
					});
				}
			});
			EDCDebugger.getDefault().getTrace().traceExit(IEDCTraceOptions.RUN_CONTROL_TRACE);
		}

		/**
		 * Step out of the current function. Note the "rm" is marked done() only
		 * when we get the suspend event, not when we successfully send the
		 * command to TCF agent.
		 * 
		 * @param rm
		 */
		protected void stepOut(final RequestMonitor rm) {
			EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.RUN_CONTROL_TRACE, this.getName());

			setStepping(true);

			flushCache(this);

			Protocol.invokeLater(new Runnable() {
				public void run() {
					tcfContext.resume(org.eclipse.tm.tcf.services.IRunControl.RM_STEP_OUT, 0, new DoneCommand() {

						public void doneCommand(IToken token, final Exception error) {
							// do this in DSF executor thread.
							getExecutor().execute(new Runnable() {
								public void run() {
									handleTCFResumeDoneForStepping("StepOut", error, rm);
								}
							});
						}
					});
				}
			});
			EDCDebugger.getDefault().getTrace().traceExit(IEDCTraceOptions.RUN_CONTROL_TRACE);
		}

		protected void stepRange(final boolean stepInto, final IAddress rangeStart, final IAddress rangeEnd,
				final RequestMonitor rm) {
			EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.RUN_CONTROL_TRACE, this.getName());

			setStepping(true);

			flushCache(this);

			Protocol.invokeLater(new Runnable() {
				public void run() {
					int mode = stepInto ? org.eclipse.tm.tcf.services.IRunControl.RM_STEP_INTO_RANGE
							: org.eclipse.tm.tcf.services.IRunControl.RM_STEP_OVER_RANGE;
					Map<String, Object> params = new HashMap<String, Object>();
					params.put("RANGE_START", rangeStart.getValue());
					params.put("RANGE_END", rangeEnd.getValue());

					tcfContext.resume(mode, 0, params, new DoneCommand() {

						public void doneCommand(IToken token, final Exception error) {
							// do this in DSF executor thread.
							getExecutor().execute(new Runnable() {
								public void run() {
									handleTCFResumeDoneForStepping("StepRange", error, rm);
								}
							});
						}
					});
				}
			});
			EDCDebugger.getDefault().getTrace().traceExit(IEDCTraceOptions.RUN_CONTROL_TRACE);
		}

		/**
		 * set whether debugger is stepping in the context.
		 * 
		 * @param isStepping
		 */
		public void setStepping(boolean isStepping) {
			this.isStepping = isStepping;
		}

		/**
		 * @return whether debugger is stepping the context.
		 */
		public boolean isStepping() {
			return isStepping;
		}

	}

	public class ProcessExecutionDMC extends ExecutionDMC implements IContainerDMContext, IProcessDMContext,
			ISymbolDMContext, IBreakpointsTargetDMContext, IDisassemblyDMContext {

		public ProcessExecutionDMC(ExecutionDMC parent, Map<String, Object> properties, RunControlContext tcfContext) {
			super(parent, properties, tcfContext);
		}

		@Override
		public ExecutionDMC contextAdded(Map<String, Object> properties, RunControlContext tcfContext) {
			EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.RUN_CONTROL_TRACE, properties);
			ThreadExecutionDMC newDMC = new ThreadExecutionDMC(this, properties, tcfContext);
			getSession().dispatchEvent(new StartedEvent(newDMC), RunControl.this.getProperties());
			EDCDebugger.getDefault().getTrace().traceExit(IEDCTraceOptions.RUN_CONTROL_TRACE, newDMC);
			return newDMC;
		}

		public ISymbolDMContext getSymbolDMContext() {
			return this;
		}

		@Override
		public void loadSnapshot(Element element) throws Exception {
			// load modules first, since this loads a stack which must consult modules and symbolics
			Modules modulesService = getServicesTracker().getService(Modules.class);
			modulesService.loadModulesForContext(this, element);
			super.loadSnapshot(element);
		}

		@Override
		public Element takeShapshot(IAlbum album, Document document, IProgressMonitor monitor) {
			Element contextElement = super.takeShapshot(album, document, monitor);
			Element modulesElement = document.createElement(EXECUTION_CONTEXT_MODULES);
			Modules modulesService = getServicesTracker().getService(Modules.class);

			IModuleDMContext[] modules = modulesService.getModulesForContext(this.getID());
			for (IModuleDMContext moduleContext : modules) {
				ModuleDMC moduleDMC = (ModuleDMC) moduleContext;
				modulesElement.appendChild(moduleDMC.takeShapshot(album, document, monitor));
			}

			contextElement.appendChild(modulesElement);
			return contextElement;
		}

		@Override
		public boolean canDetach() {
			// can detach from a process.
			return true;
		}

	}

	public class ThreadExecutionDMC extends ExecutionDMC implements IThreadDMContext, IDisassemblyDMContext {

		public ThreadExecutionDMC(ExecutionDMC parent, Map<String, Object> properties, RunControlContext tcfContext) {
			super(parent, properties, tcfContext);
			EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.RUN_CONTROL_TRACE,
					new Object[] { parent, properties });
			;
			EDCDebugger.getDefault().getTrace().traceExit(IEDCTraceOptions.RUN_CONTROL_TRACE);
		}

		public ISymbolDMContext getSymbolDMContext() {
			return DMContexts.getAncestorOfType(this, ISymbolDMContext.class);
		}

		@Override
		public void loadSnapshot(Element element) throws Exception {
			super.loadSnapshot(element);
			Registers regService = getServicesTracker().getService(Registers.class);
			regService.loadGroupsForContext(this, element);

			Stack stackService = getServicesTracker().getService(Stack.class);
			NodeList frameElements = element.getElementsByTagName(EXECUTION_CONTEXT_FRAMES);
			for (int i = 0; i < frameElements.getLength(); i++) {
				Element frameElement = (Element) frameElements.item(i);
				stackService.loadFramesForContext(this, frameElement);
			}
			
			getSession().dispatchEvent(
					new SuspendedEvent(this, StateChangeReason.EXCEPTION, new HashMap<String, Object>()),
					RunControl.this.getProperties());

		}

		@Override
		public Element takeShapshot(IAlbum album, Document document, IProgressMonitor monitor) {
			Element contextElement = super.takeShapshot(album, document, monitor);
			Element registersElement = document.createElement(EXECUTION_CONTEXT_REGISTERS);
			Registers regService = getServicesTracker().getService(Registers.class);

			IRegisterGroupDMContext[] regGroups = regService.getGroupsForContext(this);
			for (IRegisterGroupDMContext registerGroupDMContext : regGroups) {
				RegisterGroupDMC regDMC = (RegisterGroupDMC) registerGroupDMContext;
				registersElement.appendChild(regDMC.takeShapshot(album, document, monitor));
			}

			contextElement.appendChild(registersElement);

			Element framesElement = document.createElement(EXECUTION_CONTEXT_FRAMES);
			Stack stackService = getServicesTracker().getService(Stack.class);
			Expressions expressionsService = getServicesTracker().getService(Expressions.class);

			IFrameDMContext[] frames = stackService.getFramesForDMC(this, 0, IStack.ALL_FRAMES);
			for (IFrameDMContext frameDMContext : frames) {
				StackFrameDMC frameDMC = (StackFrameDMC) frameDMContext;
				
				// Get the local variables for each frame
				IVariableDMContext[] variables = frameDMC.getLocals();
				for (IVariableDMContext iVariableDMContext : variables) {
					VariableDMC varDMC = (VariableDMC) iVariableDMContext;
					IExpressionDMContext expression = expressionsService.createExpression(frameDMContext, varDMC.getName());
					boolean wasEnabled = FormatExtensionManager.instance().isEnabled();
					FormatExtensionManager.instance().setEnabled(true);
					expressionsService.loadExpressionValues(expression, Album.getVariableCaptureDepth());
					FormatExtensionManager.instance().setEnabled(wasEnabled);
				}
				
				framesElement.appendChild(frameDMC.takeShapshot(album, document, monitor));
			}

			contextElement.appendChild(framesElement);

			return contextElement;
		}

		@Override
		public ExecutionDMC contextAdded(Map<String, Object> properties, RunControlContext tcfContext) {
			assert (false);
			return null;
		}

		@Override
		public boolean canDetach() {
			// Cannot detach from a thread.
			return false;
		}

	}

	/**
	 * Context representing a program running on a bare device without OS, which
	 * can also be the boot-up "process" of an OS.
	 * <p>
	 * It's like a thread context as it has its registers and stack frames, but
	 * also like a process as it has modules associated with it. Currently we
	 * set it as an IProcessDMContext so that it appears as a ContainerVMNode in
	 * debug view. See LaunchVMProvider for more. Also it's treated like a
	 * process in
	 * {@link Processes#getProcessesBeingDebugged(IDMContext, DataRequestMonitor)}
	 */
	public class BareDeviceExecutionDMC extends ThreadExecutionDMC 
				implements IProcessDMContext, ISymbolDMContext, IBreakpointsTargetDMContext {

		public BareDeviceExecutionDMC(ExecutionDMC parent,
				Map<String, Object> properties, RunControlContext tcfContext) {
			super(parent, properties, tcfContext);
			assert !(Boolean)properties.get(PROP_IS_CONTAINER);
		}

		@Override
		public boolean canDetach() {
			return true;
		}
		
	}
	
	public class RootExecutionDMC extends ExecutionDMC implements ISourceLookupDMContext {

		public RootExecutionDMC(Map<String, Object> props) {
			super(null, props, null);
		}

		@Override
		public ExecutionDMC contextAdded(Map<String, Object> properties, RunControlContext tcfContext) {
			Boolean isContainer = (Boolean)(properties.get(PROP_IS_CONTAINER));
			ExecutionDMC newDMC;
			// If the new context being added under root is a container context,
			// we treat it as a Process, otherwise a bare device program context.
			//
			if (isContainer == null || Boolean.TRUE.equals(isContainer))
				newDMC = new ProcessExecutionDMC(this, properties, tcfContext);
			else
				newDMC = new BareDeviceExecutionDMC(this, properties, tcfContext);
			
			getSession().dispatchEvent(new StartedEvent(newDMC), RunControl.this.getProperties());
			return newDMC;
		}

		public ISymbolDMContext getSymbolDMContext() {
			return null;
		}

		@Override
		public boolean canDetach() {
			return false;
		}
	}

	private static final String EXECUTION_CONTEXTS = "execution_contexts";

	private org.eclipse.tm.tcf.services.IRunControl tcfRunService;
	private RootExecutionDMC rootExecutionDMC;
	private final Map<String, ExecutionDMC> dmcsByID = new HashMap<String, ExecutionDMC>();
	
	public RunControl(DsfSession session) {
		super(session, new String[] { 
				IRunControl.class.getName(), 
				IRunControl2.class.getName(), 
				RunControl.class.getName(),
				ISnapshotContributor.class.getName() });
		initializeRootExecutionDMC();
	}

	private void initializeRootExecutionDMC() {
		HashMap<String, Object> props = new HashMap<String, Object>();
		props.put(IEDCDMContext.PROP_ID, "root");
		rootExecutionDMC = new RootExecutionDMC(props);
	}

	public void canResume(IExecutionDMContext context, DataRequestMonitor<Boolean> rm) {
		rm.setData(((ExecutionDMC) context).isSuspended() ? Boolean.TRUE : Boolean.FALSE);
		rm.done();
	}

	public void canStep(IExecutionDMContext context, StepType stepType, DataRequestMonitor<Boolean> rm) {
		rm.setData(((ExecutionDMC) context).isSuspended() ? Boolean.TRUE : Boolean.FALSE);
		rm.done();
	}

	public void canSuspend(IExecutionDMContext context, DataRequestMonitor<Boolean> rm) {
		rm.setData(((ExecutionDMC) context).isSuspended() ? Boolean.FALSE : Boolean.TRUE);
		rm.done();
	}

	public void getExecutionContexts(IContainerDMContext c, DataRequestMonitor<IExecutionDMContext[]> rm) {
		if (c instanceof ProcessExecutionDMC) {
			ProcessExecutionDMC edmc = (ProcessExecutionDMC) c;
			IEDCExecutionDMC[] threads = edmc.getChildren();
			IExecutionDMContext[] threadArray = new IExecutionDMContext[threads.length];
			System.arraycopy(threads, 0, threadArray, 0, threads.length);
			rm.setData(threadArray);
		}
		rm.done();
	}

	public void getExecutionData(IExecutionDMContext dmc, DataRequestMonitor<IExecutionDMData> rm) {
		if (dmc instanceof ExecutionDMC) {
			ExecutionDMC exedmc = (ExecutionDMC) dmc;
			rm.setData(new ExecutionData(exedmc.isSuspended() ? exedmc.getStateChangeReason()
					: StateChangeReason.UNKNOWN, exedmc.getStateChangeDetails()));
		} else
			rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, INVALID_HANDLE,
					"Given context: " + dmc + " is not a recognized execution context.", null)); //$NON-NLS-1$ //$NON-NLS-2$
		rm.done();
	}

	public boolean isStepping(IExecutionDMContext context) {
		if (context instanceof ExecutionDMC) {
			ExecutionDMC exedmc = (ExecutionDMC) context;
			return exedmc.isStepping();
		}
		return false;
	}

	public boolean isSuspended(IExecutionDMContext context) {
		if (context instanceof ExecutionDMC) {
			ExecutionDMC exedmc = (ExecutionDMC) context;
			return exedmc.isSuspended();
		}
		return false;
	}

	/**
	 * Preprocessing for suspend event. This is done before we broadcast the
	 * suspend event across the debugger. Here's what's done in the
	 * preprocessing by default: <br>
	 * 1. Adjust PC after control hits a software breakpoint where the PC
	 * points at the byte right after the breakpoint instruction. This is to
	 * move PC back to the address of the breakpoint instruction.<br>
	 * 2. If we stops at a breakpoint, evaluate condition of the breakpoint
	 * and determine if we should ignore the suspend event and resume or
	 * should honor the suspend event and sent it up the ladder.
	 * <p>
	 * Subclass can override this method to add their own special preprocessing,
	 * while calling super implementation to carry out the default common.
	 * <p>
	 * This must be called in DSF executor thread.
	 * 
	 * @param pc
	 *            program pointer value from the event, in the format of
	 *            big-endian hex string. Can be null.
	 * @param drm
	 *            DataRequestMonitor whose result indicates whether to honor
	 *            the suspend.
	 */
	protected void preprocessOnSuspend(ExecutionDMC dmc, String pc,
			DataRequestMonitor<Boolean> drm) {
		
		assert getExecutor().isInExecutorThread();
		
		Breakpoints bpService = getServicesTracker().getService(Breakpoints.class);
		Registers regService = getServicesTracker().getService(Registers.class);
		String pcString;

		if (pc == null) {
			// read PC register
			pcString = regService.getRegisterValue(dmc, getTargetEnvironmentService().getPCRegisterID());
		} else
			pcString = pc;

		dmc.setPC(pcString);

		// This check is to speed up handling of suspend due to
		// other reasons such as "step".
		// The TCF agents should always report the
		// "stateChangeReason" as BREAKPOINT when a breakpoint
		// is hit.

		if (dmc.getStateChangeReason() != StateChangeReason.BREAKPOINT) {
			drm.setData(true);
			drm.done();
			return;
		}

		if (!bpService.usesTCFBreakpointService()) {
			// generic software breakpoint is used.
			// We need to move PC back to the breakpoint
			// instruction.
			long pcValue;

			pcValue = Long.valueOf(pcString, 16);
			pcValue -= getTargetEnvironmentService()
					.getBreakpointInstruction(dmc, new Addr64(pcString, 16)).length;
			pcString = Long.toHexString(pcValue);

			// Stopped but not due to breakpoint set by debugger.
			// For instance, some Windows DLL has "int 3"
			// instructions in it.
			// 
			if (bpService.findBreakpoint(new Addr64(pcString, 16)) != null) {
				// Now adjust PC register.
				regService.writeRegister(dmc, getTargetEnvironmentService().getPCRegisterID(), pcString);
				dmc.setPC(pcString);
			}
		}

		// check if a conditional breakpoint (must be a user bp) is
		// hit
		//
		BreakpointDMData bp = bpService.findUserBreakpoint(new Addr64(pcString, 16));
		if (bp != null) {
			// evaluate the condition
			bpService.evaluateBreakpointCondition(dmc, bp, drm);
		} else {
			drm.setData(true);
			drm.done();
		}
	}

	public void resume(IExecutionDMContext context, final RequestMonitor rm) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.RUN_CONTROL_TRACE,
				MessageFormat.format("resume context {0}", context));

		if (!(context instanceof ExecutionDMC)) {
			rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, INVALID_HANDLE, MessageFormat.format(
					"The context [{0}] is not a recognized execution context.", context), null));
			rm.done();
		}

		final ExecutionDMC dmc = (ExecutionDMC) context;

		final Breakpoints bpService = getServicesTracker().getService(Breakpoints.class);
		if (bpService.usesTCFBreakpointService()) {
			dmc.resume(rm);
			EDCDebugger.getDefault().getTrace().traceExit(IEDCTraceOptions.RUN_CONTROL_TRACE,
					MessageFormat.format("resume() done on context {0}", dmc));
		} else {
			prepareToRun(dmc, new DataRequestMonitor<Boolean>(getExecutor(), rm) {

				@Override
				protected void handleSuccess() {
					dmc.resume(rm);
					EDCDebugger.getDefault().getTrace().traceExit(IEDCTraceOptions.RUN_CONTROL_TRACE,
							MessageFormat.format("resume() done on context {0}", dmc));
				}
			});
		}
	}

	/**
	 * Prepare for resuming or stepping by <br>
	 * - executing current instruction if PC is at a breakpoint.
	 * 
	 * @param dmc
	 *            - the execution context, usually a thread.
	 * @param drm
	 *            - data request monitor which will contain boolean value on
	 *            done indicating whether an instruction is executed during the
	 *            preparation.
	 */
	private void prepareToRun(final ExecutionDMC dmc, final DataRequestMonitor<Boolean> drm) {
		// If there is breakpoint at current PC, remove it => Single step =>
		// Restore it.

		String latestPC = dmc.getPC();

		if (latestPC != null) {
			final Breakpoints bpService = getServicesTracker().getService(Breakpoints.class);
			final BreakpointDMData bp = bpService.findUserBreakpoint(new Addr64(latestPC, 16));
			if (bp != null) {
				bpService.disableBreakpoint(bp, new RequestMonitor(getExecutor(), drm) {

					@Override
					protected void handleSuccess() {
						// Now step over the instruction
						//
						dmc.singleStep(true, new RequestMonitor(getExecutor(), drm) {
							@Override
							protected void handleSuccess() {
								// At this point the single instruction
								// execution should be done
								// and the context being suspended.
								//
								drm.setData(true); // indicates an instruction
								// is executed

								// Now restore the breakpoint.
								bpService.enableBreakpoint(bp, drm);
							}
						});
					}
				});
			} else { // no breakpoint at PC
				drm.setData(false);
				drm.done();
			}
		} else {
			drm.setData(false);
			drm.done();
		}
	}

	// This is a coarse timer on stepping for internal use.
	// When needed, turn it on and watch output in console.
	//
	private static long steppingStartTime = 0;
	public static boolean timeStepping() {
		return false;
	}
	
	public static long getSteppingStartTime() {
		return steppingStartTime;
	}
	
	public void step(IExecutionDMContext context, StepType stepType, final RequestMonitor rm) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.RUN_CONTROL_TRACE,
				MessageFormat.format("{0} context {1}", stepType, context));

		if (!(context instanceof ExecutionDMC)) {
			rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, INVALID_HANDLE, MessageFormat.format(
					"The context [{0}] is not a recognized execution context.", context), null));
			rm.done();
		}

		if (timeStepping())
			steppingStartTime = System.currentTimeMillis();
		
		final ExecutionDMC dmc = (ExecutionDMC) context;

		dmc.setStepping(true);

		IAddress pcAddress = null;

		if (dmc.getPC() == null) { // PC is even unknown, can only do
			// one-instruction step.
			stepType = StepType.INSTRUCTION_STEP_INTO;
		} else
			pcAddress = new Addr64(dmc.getPC(), 16);

		// For step-out (step-return), no difference between source level or
		// instruction level.
		//
		if (stepType == StepType.STEP_RETURN)
			stepType = StepType.INSTRUCTION_STEP_RETURN;

		// Source level stepping request.
		// 
		if (stepType == StepType.STEP_OVER || stepType == StepType.STEP_INTO) {
			IEDCModules moduleService = getServicesTracker().getService(Modules.class);

			ISymbolDMContext symCtx = DMContexts.getAncestorOfType(context, ISymbolDMContext.class);

			IEDCModuleDMContext module = moduleService.getModuleByAddress(symCtx, pcAddress);

			// Check if there is source info for PC address.
			//
			if (module != null) {
				IEDCSymbolReader reader = module.getSymbolReader();
				if (reader != null) {
					IAddress linkAddress = module.toLinkAddress(pcAddress);
					IModuleLineEntryProvider lineEntryProvider = reader.getModuleScope().getModuleLineEntryProvider();
					ILineEntry line = lineEntryProvider.getLineEntryAtAddress(linkAddress);
					if (line != null) {
						// get runtime addresses of the line boundaries.
						IAddress endAddr = module.toRuntimeAddress(line.getHighAddress());

						// get the next source line entry that has a line #
						// greater
						// than the current line # (and in the same file),
						// but is
						// not outside of the function address range
						// if found, the start addr of that entry is our end
						// address, otherwise use the existing end address
						//   Note: Only do this if Step Over, if Step Into we use
						//   the endAddr we already have, so if are stepping into inline
						//   functions, this will work
						if (stepType == StepType.STEP_OVER) {
							ILineEntry nextLine = lineEntryProvider.getNextLineEntry(line);
							if (nextLine != null) {
								endAddr = module.toRuntimeAddress(nextLine.getLowAddress());
							}
						}

						/*
						 * It's possible that PC is larger than startAddr
						 * (e.g. user does a few instruction level stepping
						 * then switch to source level stepping; or when we
						 * just step out a function). We just parse and
						 * stepping instructions within [pcAddr, endAddr)
						 * instead of all those within [startAddr, endAddr).
						 * One possible problem with the solution is when
						 * control jumps from within [pcAddress, endAddr) to
						 * somewhere within [startAddr, pcAddress), the
						 * stepping would stop at somewhere within
						 * [startAddr, pcAddress) instead of outside of the
						 * [startAddr, endAddr). But that case is rare (e.g.
						 * a source line contains a bunch of statements) and
						 * that "problem" is not unacceptable as user could
						 * just keep stepping or set a breakpoint and run.
						 * 
						 * We can overcome the problem but that would incur
						 * much more complexity in the stepping code and
						 * brings down the stepping speed.
						 * ........................ 08/30/2009
						 */
						stepAddressRange(dmc, stepType == StepType.STEP_INTO, pcAddress, endAddr, rm);

						EDCDebugger.getDefault().getTrace().traceExit(IEDCTraceOptions.RUN_CONTROL_TRACE,
								"source level stepping.");
						return;
					}
				}
			}

			// No source found, fall back to instruction level step.
			if (stepType == StepType.STEP_INTO)
				stepType = StepType.INSTRUCTION_STEP_INTO;
			else
				stepType = StepType.INSTRUCTION_STEP_OVER;
		}

		// instruction level step
		// 
		if (stepType == StepType.INSTRUCTION_STEP_OVER)
			stepOverOneInstruction(dmc, pcAddress, rm);
		else if (stepType == StepType.INSTRUCTION_STEP_INTO)
			stepIntoOneInstruction(dmc, rm);
		else if (stepType == StepType.INSTRUCTION_STEP_RETURN)
			stepOut(dmc, pcAddress, rm);

		EDCDebugger.getDefault().getTrace().traceExit(IEDCTraceOptions.RUN_CONTROL_TRACE);
	}

	private void stepOut(final ExecutionDMC dmc, IAddress pcAddress, final RequestMonitor rm) {

		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.RUN_CONTROL_TRACE,
				"Step out from address " + pcAddress.toHexAddressString());

		if (dmc.supportsStepMode(StepType.STEP_RETURN)) {
			dmc.stepOut(rm);
			return;
		}

		Stack stackService = getServicesTracker().getService(Stack.class);
		IFrameDMContext[] frames = stackService.getFramesForDMC(dmc, 0, 1);
		if (frames.length <= 1) {
			rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, REQUEST_FAILED,
					"Cannot step out as no caller frame is available.", null));
			rm.done();
			return;
		}

		if (handleSteppingOutOfInLineFunctions(dmc, frames, rm))
			return;

		final IAddress stepToAddress = ((StackFrameDMC) frames[1]).getIPAddress();
		
		
		boolean keepgoing = true;
		if (!keepgoing) {
			rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, REQUEST_FAILED,
					"Cannot step out as no caller frame is available.", null));
			rm.done();
			return;
		}
		
		final Breakpoints bpService = getServicesTracker().getService(Breakpoints.class);

		prepareToRun(dmc, new DataRequestMonitor<Boolean>(getExecutor(), rm) {
			@Override
			protected void handleSuccess() {

				boolean goon = true;

				if (getData() == true) {
					// one instruction has been executed
					IAddress newPC = new Addr64(dmc.getPC(), 16);

					// And we already stepped out (that instruction is return
					// instruction).
					//
					if (newPC.equals(stepToAddress)) {
						goon = false;
					}
				}

				if (goon) {
					bpService.setTempBreakpoint(dmc, stepToAddress, new RequestMonitor(getExecutor(), rm) {
						@Override
						protected void handleSuccess() {
							dmc.resumeForStepping(rm);
						}
					});
				} else
					rm.done();
			}
		});
	}

	/**
	 * handle module load event. A module is an executable file
	 * or a library (e.g. DLL or shared lib).
	 * Allow subclass to override for special handling if needed.
	 * This must be called in DSF dispatch thread.
	 * 
	 * @param dmc
	 * @param moduleProperties
	 */
	protected void handleModuleLoadedEvent(IEDCExecutionDMC dmc, Map<String, Object> moduleProperties) {
		ISymbolDMContext symbolContext = dmc.getSymbolDMContext();

		if (symbolContext != null) {
			Modules modulesService = getServicesTracker().getService(Modules.class);
			modulesService.moduleLoaded(symbolContext, dmc, moduleProperties);
		}
	}
		
	/**
	 * handle module unload event. A module is an executable file
	 * or a library (e.g. DLL or shared lib).
	 * Allow subclass to override for special handling if needed.
	 * This must be called in DSF dispatch thread.
	 * 
	 * @param dmc
	 * @param moduleProperties
	 */
	protected void handleModuleUnloadedEvent(IEDCExecutionDMC dmc, Map<String, Object> moduleProperties) {
		ISymbolDMContext symbolContext = dmc.getSymbolDMContext();

		if (symbolContext != null) {
			Modules modulesService = getServicesTracker().getService(Modules.class);
			modulesService.moduleUnloaded(symbolContext, dmc, moduleProperties);
		}
	}
		
	private boolean handleSteppingOutOfInLineFunctions(final ExecutionDMC dmc, IFrameDMContext[] frames, final RequestMonitor rm) {
		assert frames.length > 1 && frames[0] instanceof StackFrameDMC;
		// Check to see if we are in an inlined function
		StackFrameDMC currentFrame = ((StackFrameDMC) frames[0]);
		IEDCSymbols symbolsService = getServicesTracker().getService(Symbols.class);
		IFunctionScope functionScope = symbolsService
		.getFunctionAtAddress(dmc.getSymbolDMContext(), currentFrame.getIPAddress());
		
		if (functionScope != null)
		{
			IScope parentScope = functionScope.getParent();		
			if (parentScope instanceof IFunctionScope && currentFrame.getModule() != null)
			{
				if (!currentFrame.getModule().toRuntimeAddress(functionScope.getLowAddress()).equals(currentFrame.getIPAddress()))
				{
					stepAddressRange(dmc, false, currentFrame.getIPAddress(), 
									 currentFrame.getModule().toRuntimeAddress(functionScope.getHighAddress()),
									 new RequestMonitor(getExecutor(), rm){

						@Override
						protected void handleSuccess() {
							step(dmc, StepType.STEP_OVER, new RequestMonitor(getExecutor(), new RequestMonitor(getExecutor(), rm)));
						}});
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * check if the instruction at PC is a subroutine call. If yes, set a
	 * breakpoint after it and resume; otherwise just execute one instruction.
	 * 
	 * @param dmc
	 * @param pcAddress
	 * @param rm
	 */
	private void stepOverOneInstruction(final ExecutionDMC dmc, final IAddress pcAddress, final RequestMonitor rm) {

		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.RUN_CONTROL_TRACE,
				"address " + pcAddress.toHexAddressString());

		if (dmc.supportsStepMode(StepType.INSTRUCTION_STEP_OVER)) {
			dmc.singleStep(false, rm);
			return;
		}

		final IDisassembler disassembler = getTargetEnvironmentService().getDisassembler();
		if (disassembler == null) {
			rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, REQUEST_FAILED,
					"No disassembler is available yet.", null));
			rm.done();
			return;
		}

		Memory memoryService = getServicesTracker().getService(Memory.class);
		IMemoryDMContext mem_dmc = DMContexts.getAncestorOfType(dmc, IMemoryDMContext.class);

		// We need to get the instruction at the PC. We have to
		// retrieve memory bytes for longest instruction.
		int maxInstLength = getTargetEnvironmentService().getLongestInstructionLength();

		// Note this memory read will give us memory bytes with
		// debugger breakpoints removed, which is just what we want.
		memoryService.getMemory(mem_dmc, pcAddress, 0, 1, maxInstLength, new DataRequestMonitor<MemoryByte[]>(
				getExecutor(), rm) {
			@Override
			protected void handleSuccess() {
				MemoryByte[] data = getData();
				final byte[] bytes = new byte[data.length];
				for (int i = 0; i < data.length; i++)
					bytes[i] = data[i].getValue();

				ByteBuffer codeBuf = ByteBuffer.wrap(bytes);

				IDisassembledInstruction inst;

				Map<String, Object> options = new HashMap<String, Object>();
				try {
					inst = disassembler.disassembleOneInstruction(pcAddress, codeBuf, options);
				} catch (CoreException e) {
					rm.setStatus(e.getStatus());
					rm.done();
					return;
				}

				final boolean isSubroutineCall = inst.getJumpToAddress() != null
						&& inst.getJumpToAddress().isSubroutineAddress();
				final IAddress nextInstructionAddress = pcAddress.add(inst.getSize());

				stepIntoOneInstruction(dmc, new RequestMonitor(getExecutor(), rm) {
					@Override
					protected void handleSuccess() {
						if (!isSubroutineCall)
							rm.done();
						else {
							// If current instruction is subroutine call, set a
							// temp
							// breakpoint at next instruction and resume ...
							//
							Breakpoints bpService = getServicesTracker().getService(Breakpoints.class);
							bpService.setTempBreakpoint(dmc, nextInstructionAddress, new RequestMonitor(getExecutor(),
									rm) {
								@Override
								protected void handleSuccess() {
									dmc.resumeForStepping(rm);
								}
							});
						}
					}
				});
			}
		});
	}

	/**
	 * Step into or over an address range. Note the startAddr is also the PC
	 * value.
	 * 
	 * @param dmc
	 * @param stepIn
	 *            - whether to step-in.
	 * @param startAddr
	 *            - also the PC register value.
	 * @param endAddr
	 * @param rm
	 *            - marked done after the stepping is over and context is
	 *            suspended again.
	 */
	private void stepAddressRange(final ExecutionDMC dmc, final boolean stepIn, final IAddress startAddr,
			final IAddress endAddr, final RequestMonitor rm) {

		EDCDebugger.getDefault().getTrace().traceEntry(
				IEDCTraceOptions.RUN_CONTROL_TRACE,
				MessageFormat.format("address range [{0},{1})", startAddr.toHexAddressString(), endAddr
						.toHexAddressString()));

		if (dmc.supportsStepMode(stepIn ? StepType.STEP_INTO : StepType.STEP_OVER)) {
			dmc.stepRange(stepIn, startAddr, endAddr, rm);
			return;
		}

		final IDisassembler disassembler = getTargetEnvironmentService().getDisassembler();
		if (disassembler == null) {
			rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, REQUEST_FAILED,
					"No disassembler is available yet.", null));
			rm.done();
			return;
		}

		final Memory memoryService = getServicesTracker().getService(Memory.class);
		IMemoryDMContext mem_dmc = DMContexts.getAncestorOfType(dmc, IMemoryDMContext.class);

		int memSize = startAddr.distanceTo(endAddr).intValue();

		final IAddress pcAddress = startAddr;

		// Note this memory read will give us memory bytes with
		// debugger breakpoints removed, which is just what we want.
		memoryService.getMemory(mem_dmc, startAddr, 0, 1, memSize, new DataRequestMonitor<MemoryByte[]>(getExecutor(),
				rm) {
			@Override
			protected void handleSuccess() {
				MemoryByte[] data = getData();
				final byte[] bytes = new byte[data.length];
				for (int i = 0; i < data.length; i++)
					bytes[i] = data[i].getValue();

				ByteBuffer codeBuf = ByteBuffer.wrap(bytes);

				List<IDisassembledInstruction> instList;

				Map<String, Object> options = new HashMap<String, Object>();
				try {
					instList = disassembler.disassembleInstructions(startAddr, endAddr, codeBuf, options);
				} catch (CoreException e) {
					rm.setStatus(e.getStatus());
					rm.done();
					return;
				}

				// Now collect all possible stop points
				//
				final List<IAddress> stopPoints = new ArrayList<IAddress>();
				final List<IAddress> runToAndCheckPoints = new ArrayList<IAddress>();

				for (IDisassembledInstruction inst : instList) {
					final IAddress instAddr = inst.getAddress();

					IJumpToAddress jta = inst.getJumpToAddress();
					if (jta == null)
						continue;

					// the instruction is a control-change instruction
					//
					if (!jta.isImmediate()) {

						if (inst.getAddress().equals(pcAddress)) {
							// Control is already at the instruction, evaluate
							// it.
							//
							String expr = (String) jta.getValue();
							if (expr.equals(JumpToAddress.EXPRESSION_RETURN_FAR)
									|| expr.equals(JumpToAddress.EXPRESSION_RETURN_NEAR)
									|| expr.equals(JumpToAddress.EXPRESSION_LR)) {
								// The current instruction is return instruction. Just execute it
								// to step-out and we are done with the stepping. This way we avoid
								// looking for return address from caller stack frame which may not
								// even available.
								// Is it possible that the destination address of the step-out
								// is still within the [startAddr, endAddr)range ? In theory
								// yes, but in practice it means one source line has several
								// function bodies in it, who would do that?
								//
								stepIntoOneInstruction(dmc, rm);
								return;
							} else { // others
								// evaluate the address expression

								if (!jta.isSubroutineAddress() || stepIn)
								{
									IAddressExpressionEvaluator evaluator = getTargetEnvironmentService()
									.getAddressExpressionEvaluator();
									if (evaluator == null) {
										rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, REQUEST_FAILED,
												"No evaluator for address expression yet.", null));
										rm.done();
										return;
									}

									Registers regService = getServicesTracker().getService(Registers.class);

									IAddress addr;
									try {
										addr = evaluator.evaluate(dmc, expr, regService, memoryService);
									} catch (CoreException e) {
										rm.setStatus(e.getStatus());
										rm.done();
										return;
									}
									stopPoints.add(addr);

								}
							}
						} else {
							// we must run to this instruction first
							//
							/*
							 * What if control would skip (jump-over) this
							 * instruction within the [startAddr, endAddr) range
							 * ? So we should go on collecting stop points from
							 * the remaining instructions in the range and then
							 * do our two-phase stepping (see below)
							 */
							runToAndCheckPoints.add(instAddr);
						}
					} else { // "jta" is immediate address.

						IAddress jumpAddress = (IAddress) jta.getValue();

						if (jta.isSoleDestination()) {
							if (jta.isSubroutineAddress()) {
								// is subroutine call
								if (stepIn) {
									stopPoints.add(jumpAddress);
									// no need to check remaining instructions
									// !! Wrong. Control may jump over (skip)this instruction
									// within the [startAddr, endAddr) range, so we still need
									// to parse instructions after this instruction.
									// break;
								} else {
									// step over the call instruction. Just stop
									// at next instruction.
									// nothing to do.
								}
							} else {
								// Unconditional jump instruction
								// ignore jump within the address range
								if (!(startAddr.compareTo(jumpAddress) <= 0 && jumpAddress.compareTo(endAddr) < 0)) {
									stopPoints.add(jumpAddress);
								}
							}
						} else {
							// conditional jump
							// ignore jump within the address range
							if (!(startAddr.compareTo(jumpAddress) <= 0 && jumpAddress.compareTo(endAddr) < 0))
							{
								stopPoints.add(jumpAddress);
							}
						}
					}
				} // end of parsing instructions

				// need a temp breakpoint at the "endAddr".
				stopPoints.add(endAddr);

				if (runToAndCheckPoints.size() > 0) {
					// Now do our two-phase stepping.
					//

					if (runToAndCheckPoints.size() > 1) {
						/*
						 * Wow, there are two control-change instructions in the
						 * range that requires run-to-check (let's call them RTC
						 * point). In theory the stepping might fail (not stop
						 * as desired) in such case: When we try to run to the
						 * first RTC, the control may skip the first RTC and run
						 * to second RTC (note we don't know the stop points of
						 * the second RTC yet) and run out of the range and be
						 * gone with the wind...
						 * 
						 * There is no way we can solve the problem. Good thing
						 * is, in practice is the case even possible ?
						 */
						// Log (and show it, get rid of the "show" part after
						// tons of test) warning here.
						EDCDebugger.getMessageLogger().log(
								new Status(IStatus.WARNING, EDCDebugger.PLUGIN_ID,
										MessageFormat.format(
												"More than one run-to-check points in the address range [{0},{1}). Stepping might fail.",
												startAddr.toHexAddressString(), endAddr.toHexAddressString())));
					}

					// ------------ Phase 1: run to the first RTC.
					//
					// recursive call
					stepAddressRange(dmc, stepIn, startAddr, runToAndCheckPoints.get(0), new RequestMonitor(
							getExecutor(), rm) {
						@Override
						protected void handleSuccess() {
							IAddress newPC = new Addr64(dmc.getPC(), 16);

							boolean doneWithStepping = false;
							for (IAddress addr : stopPoints)
								if (newPC.equals(addr)) {
									doneWithStepping = true; // done with the
									// stepping
									break;
								}

							Breakpoints bpService = getServicesTracker().getService(Breakpoints.class);
							if (bpService.findUserBreakpoint(newPC) != null) { // hit
								// a
								// user
								// breakpoint
								doneWithStepping = true;
							}

							if (!doneWithStepping)
								// -------- Phase 2: run to the "endAddr".
								//
								stepAddressRange(dmc, stepIn, newPC, endAddr, rm); // Recursive
							// call
							else
								rm.done();
						}
					});
				} else { // no RTC points, set temp breakpoints at stopPoints
					// and run...

					// Make sure we step over breakpoint at PC (if any)
					//
					prepareToRun(dmc, new DataRequestMonitor<Boolean>(getExecutor(), rm) {
						@Override
						protected void handleSuccess() {

							boolean goon = true;

							Breakpoints bpService = getServicesTracker().getService(Breakpoints.class);

							if (getData() == true) {
								// one instruction has been executed
								IAddress newPC = new Addr64(dmc.getPC(), 16);

								if (bpService.findUserBreakpoint(newPC) != null) {
									// hit a user breakpoint. Stepping finishes.
									goon = false;
								} else {
									// Check if we finish the stepping by
									// checking the newPC against
									// our stopPoints instead of checking if
									// newPC is outside of [startAddr, endAddr)
									// so that such case would not fail: step
									// over this address range:
									//
									// 0x10000 call ... // a user breakpoint is
									// set here
									// 0x10004 ...
									// 0x1000c ...
									// 
									//
									for (IAddress addr : stopPoints)
										if (newPC.equals(addr)) {
											goon = false;
											break;
										}
								}
							}

							if (goon) {
								// Now set temp breakpoints at our stop points.
								//
								CountingRequestMonitor setTempBpRM = new CountingRequestMonitor(getExecutor(), rm) {
									@Override
									protected void handleSuccess() {
										// we are done setting all temporary
										// breakpoints
										dmc.resumeForStepping(rm);
									}
								};

								setTempBpRM.setDoneCount(stopPoints.size());

								for (IAddress addr : stopPoints) {
									bpService.setTempBreakpoint(dmc, addr, setTempBpRM);
								}
							} else
								rm.done();
						}
					});
				}

			}
		});
	}

	/**
	 * step-into one instruction at current PC, namely execute only one
	 * instruction.
	 * 
	 * @param dmc
	 * @param rm
	 *            - this RequestMonitor is marked done when the execution
	 *            finishes and target suspends again.
	 */
	private void stepIntoOneInstruction(final ExecutionDMC dmc, final RequestMonitor rm) {

		// TODO what about protocols that supports stepping past breakpoints
		// like TRK?

		prepareToRun(dmc, new DataRequestMonitor<Boolean>(getExecutor(), rm) {
			@Override
			protected void handleSuccess() {
				if (getData() == true /* already executed one instruction */)
					// The "step" is over
					rm.done();
				else {
					dmc.singleStep(true, rm);
				}
			}
		});
	}

	public void suspend(IExecutionDMContext context, RequestMonitor requestMonitor) {
		if (context instanceof ExecutionDMC) {
			((ExecutionDMC) context).suspend(requestMonitor);
		} else {
			requestMonitor.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, INVALID_HANDLE, MessageFormat
					.format("The context [{0}] is not a recognized execution context.", context), null));
			requestMonitor.done();
		}
	}

	public void getModelData(IDMContext dmc, DataRequestMonitor<?> rm) {
		rm.done();
	}

	public void flushCache(IDMContext context) {
		if (isSnapshot())
			return;
		// Flush the Registers cache immediately
		// For instance the readPCRegister() may get wrong PC value when an
		// asynchronous suspend event comes too quick after resume.
		Registers regService = getServicesTracker().getService(Registers.class);
		regService.flushCache(context);
	}

	@Override
	public void shutdown(RequestMonitor monitor) {
		if (tcfRunService != null) {
			Protocol.invokeLater(new Runnable() {
				public void run() {
					tcfRunService.removeListener(runListener);
				}
			});
		}
		unregister();
		super.shutdown(monitor);
	}

	public RootExecutionDMC getRootDMC() {
		return rootExecutionDMC;
	}

	public static class StartedEvent extends AbstractDMEvent<IExecutionDMContext> implements IStartedDMEvent {

		public StartedEvent(IExecutionDMContext context) {
			super(context);
		}
	}

	public static class ExitedEvent extends AbstractDMEvent<IExecutionDMContext> implements IExitedDMEvent {

		public ExitedEvent(IExecutionDMContext context) {
			super(context);
		}

	}

	/*
	 * NOTE: 
	 * Methods in this listener are invoked in TCF dispatch thread.
	 * When they call into DSF services/objects, make sure it's done in 
	 * DSF executor thread so as to avoid possible race condition.
	 */
	private final org.eclipse.tm.tcf.services.IRunControl.RunControlListener runListener = new org.eclipse.tm.tcf.services.IRunControl.RunControlListener() {

		public void containerResumed(String[] context_ids) {
		}

		public void containerSuspended(String context, String pc, String reason, Map<String, Object> params,
				String[] suspended_ids) {
		}

		public void contextAdded(final RunControlContext[] contexts) {
			getExecutor().execute(new Runnable() {
				public void run() {
					for (RunControlContext ctx : contexts) {
						ExecutionDMC dmc = rootExecutionDMC;
						String parentID = ctx.getParentID();
						if (parentID != null)
							dmc = dmcsByID.get(parentID);
						if (dmc != null) {
							dmc.contextAdded(ctx.getProperties(), ctx);
						}
					}
				}
			});
		}

		public void contextChanged(RunControlContext[] contexts) {
		}

		public void contextException(final String context, final String msg) {
			getExecutor().execute(new Runnable() {
				public void run() {
					ExecutionDMC dmc = getContext(context);
					if (dmc != null)
						dmc.contextException(msg);
				}
			});
		}

		public void contextRemoved(final String[] context_ids) {
			getExecutor().execute(new Runnable() {
				public void run() {
					for (String contextID : context_ids) {
						ExecutionDMC dmc = getContext(contextID);
						assert dmc != null;
						if (dmc != null)
							dmc.detachFromDebugger();
					}
				}
			});
		}

		public void contextResumed(final String context) {
			getExecutor().execute(new Runnable() {
				public void run() {
					ExecutionDMC dmc = getContext(context);
					if (dmc != null)
						dmc.contextResumed(false);
				}
			});
		}

		public void contextSuspended(final String context, final String pc, final String reason,
				final Map<String, Object> params) {
			getExecutor().execute(new Runnable() {
				public void run() {
					ExecutionDMC dmc = getContext(context);
					if (dmc != null)
						dmc.contextSuspended(pc, reason, params);
					else {
						EDCDebugger.getMessageLogger().logError(
							MessageFormat.format("Unkown context [{0}] is reported in suspended event. Make sure TCF agent has reported contextAdded event first.", context), 
							null);
					}
				}
			});
		}
	};

	public Element takeShapshot(IAlbum album, Document document, IProgressMonitor monitor) {
		Element contextsElement = document.createElement(EXECUTION_CONTEXTS);

		ExecutionDMC[] dmcs = rootExecutionDMC.getChildren();

		for (ExecutionDMC executionDMC : dmcs) {
			Element dmcElement = executionDMC.takeShapshot(album, document, monitor);
			contextsElement.appendChild(dmcElement);
		}
		return contextsElement;
	}

	public ExecutionDMC getContext(String contextID) {
		return dmcsByID.get(contextID);
	}

	public void loadSnapshot(Element snapshotRoot) throws Exception {
		NodeList ecElements = snapshotRoot.getElementsByTagName(EXECUTION_CONTEXTS);
		rootExecutionDMC.resumeAll();
		initializeRootExecutionDMC();
		rootExecutionDMC.loadSnapshot((Element) ecElements.item(0));
	}

	public void tcfServiceReady(IService service) {
		if (service instanceof org.eclipse.tm.tcf.services.IRunControl) {
			tcfRunService = (org.eclipse.tm.tcf.services.IRunControl) service;
			Protocol.invokeLater(new Runnable() {
				public void run() {
					tcfRunService.addListener(runListener);
				}
			});
		} else
			assert false;
	}

	/**
	 * Stop debugging all execution contexts. This does not kill/terminate
	 * the actual process or thread.
	 * See: {@link #terminateAllContexts(RequestMonitor)}
	 */
	private void detachAllContexts(){
		getRootDMC().detachFromDebugger();
	}

	/**
	 * Terminate all contexts so as to terminate the debug session.
	 * 
	 * @param rm can be null.
	 */
	public void terminateAllContexts(final RequestMonitor rm){

		CountingRequestMonitor crm = new CountingRequestMonitor(getExecutor(), rm) {
			@Override
			protected void handleError() {
				// failed to terminate at least one process, usually
				// because connection to target is lost, or some processes
				// cannot be killed (e.g. OS does not permit that).
				// Just untarget the contexts.
				detachAllContexts();
		
				if (rm != null)
					rm.done();
			}
			
		};
		
		// It's assumed 
		// 1. First level of children under rootDMC are processes.
		// 2. Killing them would kill all contexts (processes and threads) being debugged.
		//
		ExecutionDMC[] processes = getRootDMC().getChildren();
		crm.setDoneCount(processes.length);
		
		for (ExecutionDMC e : processes) {
			e.terminate(crm);
		}
	}

	public void canRunToLine(IExecutionDMContext context, String sourceFile,
			int lineNumber, final DataRequestMonitor<Boolean> rm) {
		// I tried to have better filtering as shown in commented code. But that 
		// just make the command fail to be enabled as desired, not sure about the 
		// exact cause yet, but one problem (from the upper framework) I've seen is 
		// this API is not called whenever user selects a line in source editor (or
		// disassembly view) and bring up context menu.
		// Hence we blindly answer yes. The behavior is in par with DSF-GDB.
		// ................. 03/11/10  
		rm.setData(true);
		rm.done();
		
//		// Return true if we can find address(es) for the line in the context.
//		//
//		getLineAddress(context, sourceFile, lineNumber, new DataRequestMonitor<List<IAddress>>(getExecutor(), rm){
//			@Override
//			protected void handleCompleted() {
//				if (! isSuccess())
//					rm.setData(false);
//				else {
//					rm.setData(getData().size() > 0);
//				}
//				rm.done();
//			}});
	}

	public void runToLine(final IExecutionDMContext context, String sourceFile,
			int lineNumber, boolean skipBreakpoints, final RequestMonitor rm) {
		
		getLineAddress(context, sourceFile, lineNumber, new DataRequestMonitor<List<IAddress>>(getExecutor(), rm){
			@Override
			protected void handleCompleted() {
				if (! isSuccess()) {
					rm.setStatus(getStatus());
					rm.done();
				}
				else {
					runToAddresses(context, getData(), rm);
				}
			}});
	}

	private void runToAddresses(IExecutionDMContext context,
			final List<IAddress> addrs, final RequestMonitor rm) {
		// 1. Single step over breakpoint, if PC is at a breakpoint.
		// 2. Set temp breakpoint at the addresses.
		// 3. Resume the context.
		//
		final ExecutionDMC dmc = (ExecutionDMC)context;
		assert dmc != null;
		
		prepareToRun(dmc, new DataRequestMonitor<Boolean>(getExecutor(), rm){

			@Override
			protected void handleCompleted() {
				if (! isSuccess()) {
					rm.setStatus(getStatus());
					rm.done();
					return;
				}
				
				CountingRequestMonitor settingBP_crm = new CountingRequestMonitor(getExecutor(), rm) {
					@Override
					protected void handleCompleted() {
						if (! isSuccess()) {
							// as long as we fail to set on temp breakpoint, we bail out.
							rm.setStatus(getStatus());
							rm.done();
						}
						else {
							// all temp breakpoints are successfully set.
							// Now resume the context.
							dmc.resume(rm);
						}
					}};
				
				settingBP_crm.setDoneCount(addrs.size());
				
				Breakpoints bpService = getServicesTracker().getService(Breakpoints.class);
				
				for (IAddress a : addrs)
					bpService.setTempBreakpoint(dmc, a, settingBP_crm);
			}}
		);
	}

	public void canRunToAddress(IExecutionDMContext context, IAddress address,
			DataRequestMonitor<Boolean> rm) {
		// See comment in canRunToLine() for more.
		rm.setData(true);
		rm.done();

//		// If the address is not in any module of the run context, return false. 
//		Modules moduleService = getServicesTracker().getService(Modules.class);
//
//		ISymbolDMContext symCtx = DMContexts.getAncestorOfType(context, ISymbolDMContext.class);
//
//		ModuleDMC m = moduleService.getModuleByAddress(symCtx, address);
//		rm.setData(m == null);
//		rm.done();
	}

	public void runToAddress(IExecutionDMContext context, IAddress address,
			boolean skipBreakpoints, RequestMonitor rm) {
		List<IAddress> addrs = new ArrayList<IAddress>(1);
		addrs.add(address);
		runToAddresses(context, addrs, rm);
	}

	public void canMoveToLine(IExecutionDMContext context, String sourceFile,
			int lineNumber, boolean resume, final DataRequestMonitor<Boolean> rm) {
		// See comment in canRunToLine() for more.
		rm.setData(true);
		rm.done();
		
		// Return true if we can find one and only one address for the line in the context.
		//
//		getLineAddress(context, sourceFile, lineNumber, new DataRequestMonitor<List<IAddress>>(getExecutor(), rm){
//			@Override
//			protected void handleCompleted() {
//				if (! isSuccess())
//					rm.setData(false);
//				else {
//					rm.setData(getData().size() == 1);
//				}
//				rm.done();
//			}});
	}

	public void moveToLine(final IExecutionDMContext context, String sourceFile,
			int lineNumber, final boolean resume, final RequestMonitor rm) {
		getLineAddress(context, sourceFile, lineNumber, new DataRequestMonitor<List<IAddress>>(getExecutor(), rm){
			@Override
			protected void handleCompleted() {
				if (! isSuccess()) {
					rm.setStatus(getStatus());
					rm.done();
				}
				else {
					List<IAddress> addrs = getData();
					// No, canMoveToLine() does not do sanity check now.
					// We just move to the first address we found, which may or
					// may not be the address user wants. Is it better we return
					// error if "addrs.size() > 1" ? .......03/28/10
					// assert addrs.size() == 1;	// ensured by canMoveToLine().
					moveToAddress(context, addrs.get(0), resume, rm);
				}
			}});
	}

	public void canMoveToAddress(IExecutionDMContext context, IAddress address,
			boolean resume, DataRequestMonitor<Boolean> rm) {
		// Allow moving to any address.
		rm.setData(true);
		rm.done();
	}

	public void moveToAddress(IExecutionDMContext context, IAddress address,
			boolean resume, RequestMonitor rm) {

		Registers regService = getServicesTracker().getService(Registers.class);
		
		assert(context instanceof ExecutionDMC);
		ExecutionDMC dmc = (ExecutionDMC)context;
		
		String newPC = address.toString(16);
		
		if (! newPC.equals(dmc.getPC())) {
			// Hmm, this interface should report status.
			regService.writeRegister(dmc, getTargetEnvironmentService().getPCRegisterID(), newPC);

			// udpate cached PC.
			dmc.setPC(newPC);
		}
		
		if (resume)
			resume(context, rm);
		else {
			// fire a suspendEvent so that PC arrow can be updated in UI.
			getSession().dispatchEvent(
					new SuspendedEvent(context, StateChangeReason.USER_REQUEST, new HashMap<String, Object>()),
					RunControl.this.getProperties());
			
			rm.done();
		}
	}

	/**
	 * Get runtime addresses mapped to given source line in given run context.
	 *  
	 * @param context
	 * @param sourceFile
	 * @param lineNumber
	 * @param drm holds an empty list if no address found, or the run context is not suspended.
	 */
	private void getLineAddress(IExecutionDMContext context,
			String sourceFile, int lineNumber, DataRequestMonitor<List<IAddress>> drm) {
		List<IAddress> addrs = new ArrayList<IAddress>(1);
		
		ExecutionDMC dmc = (ExecutionDMC) context;
		if (dmc == null || ! dmc.isSuspended()) {
			drm.setData(addrs);
			drm.done();
			return;
		}
		
		Modules moduleService = getServicesTracker().getService(Modules.class);

		moduleService.getLineAddress(dmc, sourceFile, lineNumber, drm);
	}

	/**
	 * Check if this context is non-container. Only non-container context
	 * (thread and bare device context) can have register, stack frames, etc.
	 * 
	 * @param dmc
	 * @return
	 */
	static public boolean isNonContainer(IDMContext dmc) {
		return ! (dmc instanceof IContainerDMContext);
	}
}
