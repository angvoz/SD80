/*******************************************************************************
 * Copyright (c) 2009 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.services.dsf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.edc.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl.ExecutionDMC;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl.ThreadExecutionDMC;
import org.eclipse.cdt.debug.edc.internal.snapshot.Album;
import org.eclipse.cdt.debug.edc.internal.snapshot.ISnapshotContributor;
import org.eclipse.cdt.debug.edc.internal.snapshot.SnapshotUtils;
import org.eclipse.cdt.debug.edc.tcf.extension.services.ISimpleRegisters;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.ICachingService;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IRegisters;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tm.tcf.protocol.IErrorReport;
import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.util.TCFTask;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class Registers extends AbstractEDCService implements IRegisters, ICachingService, IDSFServiceUsingTCF {

	private Map<String, List<RegisterGroupDMC>> groups = Collections
			.synchronizedMap(new HashMap<String, List<RegisterGroupDMC>>());
	private ISimpleRegisters tcfRegisterService;
	private Map<String, Map<String, String>> registerValueCache = Collections
			.synchronizedMap(new HashMap<String, Map<String, String>>());

	/**
	 * A hex string indicating error in register read.
	 * See where this is used for more.
	 */
	protected static final String REGISTER_VALUE_ERROR = "badbadba";
	
	public static final String PROP_CONTEXT_ID = "Context_ID";

	private static final String REGISTER = "register";

	public class RegisterGroupDMC extends DMContext implements IRegisterGroupDMContext, ISnapshotContributor {

		private static final String REGISTER_GROUP = "register_group";

		private List<RegisterDMC> registers = Collections.synchronizedList(new ArrayList<RegisterDMC>());

		private final ExecutionDMC exeContext;

		public RegisterGroupDMC(Registers service, ExecutionDMC contDmc, String groupName, String groupDescription,
				String groupID) {
			super(service, new IDMContext[] { contDmc }, groupName, groupID);
			exeContext = contDmc;
			properties.put(PROP_DESCRIPTION, groupDescription);
			properties.put(PROP_CONTEXT_ID, contDmc.getID());
		}

		public RegisterGroupDMC(Registers service, ThreadExecutionDMC threadExecutionDmc,
				HashMap<String, Object> properties) {
			super(service, new IDMContext[] { threadExecutionDmc }, properties);
			exeContext = threadExecutionDmc;
		}

		@Override
		public String toString() {
			return baseToString() + ".group[" + getName() + "]";} //$NON-NLS-1$ //$NON-NLS-2$

		public RegisterDMC[] getRegisters() {
			RegisterDMC[] result = new RegisterDMC[0];
			synchronized (registers) {
				if (registers.size() == 0) {
					registers = Registers.this.createRegistersForGroup(this);
				}
				result = registers.toArray(new RegisterDMC[registers.size()]);
			}
			return result;
		}

		public Element takeShapshot(Album album, Document document, IProgressMonitor monitor) {
			Element contextElement = document.createElement(REGISTER_GROUP);
			contextElement.setAttribute(PROP_ID, this.getID());

			Element propsElement = SnapshotUtils.makeXMLFromProperties(document, getProperties());
			contextElement.appendChild(propsElement);

			RegisterDMC[] allRegisters = getRegisters();
			for (RegisterDMC registerDMC : allRegisters) {
				Element dmcElement = registerDMC.takeShapshot(album, document, monitor);
				contextElement.appendChild(dmcElement);
			}

			return contextElement;
		}

		public ExecutionDMC getExecutionDMC() {
			return exeContext;
		}

		public void loadSnapshot(Element element) throws Exception {
			NodeList registerElement = element.getElementsByTagName(REGISTER);

			int numRegisters = registerElement.getLength();
			for (int i = 0; i < numRegisters; i++) {
				Element regElement = (Element) registerElement.item(i);
				Element propElement = (Element) regElement.getElementsByTagName(SnapshotUtils.PROPERTIES).item(0);
				HashMap<String, Object> properties = new HashMap<String, Object>();
				SnapshotUtils.initializeFromXML(propElement, properties);

				RegisterDMC regdmc = new RegisterDMC(this, exeContext, properties);
				regdmc.loadSnapshot(regElement);
				registers.add(regdmc);
			}

		}

	}

	public class RegisterDMC extends DMContext implements IRegisterDMContext, ISnapshotContributor {

		public RegisterDMC(ExecutionDMC exeDMC, String name, String description, String id) {
			super(Registers.this, new IDMContext[] { exeDMC }, name, id);
			properties.put(PROP_CONTEXT_ID, exeDMC.getID());
		}

		public RegisterDMC(RegisterGroupDMC registerGroupDmc, ExecutionDMC exeContext,
				HashMap<String, Object> properties) {
			super(Registers.this, new IDMContext[] { exeContext }, properties);
			properties.put(PROP_CONTEXT_ID, exeContext.getID());
		}

		@Override
		public String toString() {
			return baseToString() + ".register[" + getName() + "]";} //$NON-NLS-1$ //$NON-NLS-2$

		public Element takeShapshot(Album album, Document document, IProgressMonitor monitor) {
			Element registerElement = document.createElement(REGISTER);
			registerElement.setAttribute(PROP_ID, this.getID());
			registerElement.setAttribute(PROP_VALUE, getRegisterValue(this));
			Element propsElement = SnapshotUtils.makeXMLFromProperties(document, getProperties());
			registerElement.appendChild(propsElement);
			return registerElement;
		}

		public void loadSnapshot(Element element) throws Exception {
			String registerValue = element.getAttribute(PROP_VALUE);
			String contextID = (String) getProperties().get(PROP_CONTEXT_ID);

			synchronized (registerValueCache) {
				Map<String, String> exeDMCRegisters = registerValueCache.get(contextID);
				if (exeDMCRegisters == null) {
					exeDMCRegisters = new HashMap<String, String>();
					registerValueCache.put(contextID, exeDMCRegisters);
				}
				exeDMCRegisters.put(getID(), registerValue);
			}
		}
	}

	/*
	 * Event class to notify register value is changed
	 */
	public static class RegisterChangedDMEvent implements IRegisters.IRegisterChangedDMEvent {

		private final IRegisterDMContext fRegisterDMC;

		RegisterChangedDMEvent(IRegisterDMContext registerDMC) {
			fRegisterDMC = registerDMC;
		}

		public IRegisterDMContext getDMContext() {
			return fRegisterDMC;
		}
	}

	public Registers(DsfSession session, String[] classNames) {
		super(session, classNames);
	}

	@Override
	protected void doInitialize(RequestMonitor requestMonitor) {
		super.doInitialize(requestMonitor);
		getSession().addServiceEventListener(this, null);
	}

	abstract protected List<RegisterDMC> createRegistersForGroup(RegisterGroupDMC registerGroupDMC);

	/**
	 * Given a common general purpose register id (e.g. from symbolics), get the
	 * corresponding register name.
	 * 
	 * @param id
	 *            the common general purpose register id (0-31)
	 * @return the corresponding register name, or null of n/a
	 */
	abstract protected String getRegisterNameFromCommonID(int id);

	public void findBitField(IDMContext ctx, String name, DataRequestMonitor<IBitFieldDMContext> rm) {
		rm.done();
	}

	public void findRegister(IDMContext ctx, String name, DataRequestMonitor<IRegisterDMContext> rm) {
		rm.done();
	}

	public void findRegisterGroup(IDMContext ctx, String name, DataRequestMonitor<IRegisterGroupDMContext> rm) {
		rm.done();
	}

	public void getBitFieldData(IBitFieldDMContext dmc, DataRequestMonitor<IBitFieldDMData> rm) {
		rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, NOT_SUPPORTED,
				"Bit fields not yet supported", null)); //$NON-NLS-1$
		rm.done();
	}

	public void getBitFields(IDMContext ctx, DataRequestMonitor<IBitFieldDMContext[]> rm) {
		rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, NOT_SUPPORTED, "BitField not supported", null)); //$NON-NLS-1$
		rm.done();
	}

	public void getRegisterData(IRegisterDMContext dmc, DataRequestMonitor<IRegisterDMData> rm) {

		class RegisterData implements IRegisterDMData {

			private final HashMap<String, Object> properties = new HashMap<String, Object>();

			public RegisterData(Map<String, Object> properties) {
				this.properties.putAll(properties);
			}

			public boolean isReadable() {
				return true;
			}

			public boolean isReadOnce() {
				return false;
			}

			public boolean isWriteable() {
				return true;
			}

			public boolean isWriteOnce() {
				return false;
			}

			public boolean hasSideEffects() {
				return false;
			}

			public boolean isVolatile() {
				return true;
			}

			public boolean isFloat() {
				return false;
			}

			public String getName() {
				return (String) properties.get(DMContext.PROP_NAME);
			}

			public String getDescription() {
				return (String) properties.get(DMContext.PROP_DESCRIPTION);
			}

		}

		RegisterDMC regdmc = (RegisterDMC) dmc;
		rm.setData(new RegisterData(regdmc.getProperties()));
		rm.done();
	}

	public void getRegisterGroupData(IRegisterGroupDMContext dmc, DataRequestMonitor<IRegisterGroupDMData> rm) {

		class RegisterGroupData implements IRegisterGroupDMData {
			private final String name;
			private final String description;

			public RegisterGroupData(RegisterGroupDMC dmc) {
				this.name = dmc.getName();
				this.description = (String) dmc.getProperty(DMContext.PROP_DESCRIPTION);
			}

			public String getName() {
				return name;
			}

			public String getDescription() {
				return description;
			}
		}

		rm.setData(new RegisterGroupData((RegisterGroupDMC) dmc));

		rm.done();
	}

	public IRegisterGroupDMContext[] getGroupsForContext(ExecutionDMC exeContext) {
		String contextID = exeContext.getID();
		List<RegisterGroupDMC> groupsForContext = groups.get(contextID);
		if (groupsForContext == null) {
			groupsForContext = createGroupsForContext(exeContext);
			synchronized (groups) {
				groups.put(contextID, groupsForContext);
			}
		}
		return groupsForContext.toArray(new IRegisterGroupDMContext[groupsForContext.size()]);
	}

	public void getRegisterGroups(IDMContext ctx, DataRequestMonitor<IRegisterGroupDMContext[]> rm) {
		IDMContext[] parents = ctx.getParents();

		rm.setData(new IRegisterGroupDMContext[0]);
		for (IDMContext context : parents) {
			if (context instanceof ExecutionDMC) {
				rm.setData(getGroupsForContext((ExecutionDMC) context));
			}
		}

		rm.done();
	}

	abstract protected List<RegisterGroupDMC> createGroupsForContext(ExecutionDMC ctx);

	public void getRegisters(IDMContext ctx, DataRequestMonitor<IRegisterDMContext[]> rm) {

		IDMContext[] parents = ctx.getParents();

		RegisterGroupDMC groupContext = null;
		ExecutionDMC executionContext = null;

		for (IDMContext context : parents) {
			if (context instanceof RegisterGroupDMC) {
				groupContext = (RegisterGroupDMC) context;
			}
			if (context instanceof ExecutionDMC) {
				executionContext = (ExecutionDMC) context;
			}
		}

		RegisterDMC[] allRegisters = new RegisterDMC[0];
		if (groupContext != null && executionContext != null) {
			allRegisters = groupContext.getRegisters();
		}

		rm.setData(allRegisters);

		rm.done();
	}

	public void writeBitField(IBitFieldDMContext bitFieldCtx, String bitFieldValue, String formatId, RequestMonitor rm) {
		rm.done();
	}

	public void writeBitField(IBitFieldDMContext bitFieldCtx, IMnemonic mnemonic, RequestMonitor rm) {
		rm.done();
	}

	/**
	 * Write a register.
	 * 
	 * @param context
	 * @param regID
	 *            register name.
	 * @param regValue
	 *            big-endian hex string representation of the value to write.
	 */
	public void writeRegister(ExecutionDMC context, String regID, String regValue) {
		writeRegister(new RegisterDMC(context, regID, regID, regID), regValue, IFormattedValues.HEX_FORMAT,
				new RequestMonitor(getExecutor(), null));
	}

	public void writeRegister(IRegisterDMContext regCtx, String regValue, String formatId, final RequestMonitor rm) {
		if (regCtx instanceof RegisterDMC) {
			final RegisterDMC regDMC = (RegisterDMC) regCtx;
			IExecutionDMContext exeDMC = DMContexts.getAncestorOfType(regDMC, IExecutionDMContext.class);
			if (exeDMC != null && exeDMC instanceof DMContext) {
				final String exeDMCID = ((DMContext) exeDMC).getID();
				final String regDMCID = regDMC.getID();

				// Update cached register values
				Map<String, String> exeDMCRegisters = registerValueCache.get(exeDMCID);
				if (exeDMCRegisters != null) {
					exeDMCRegisters.put(regDMC.getID(), regValue);
				}

				// Ensure connection to service agent
				if (tcfRegisterService == null) {
					rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, REQUEST_FAILED,
							"Serivce agent not available", null)); //$NON-NLS-1$
					rm.done();
					return;
				}

				// Create task to communicate with agent and ask it to set
				// register value
				final String[] registerValues = new String[] { regValue };
				TCFTask<String[]> tcfTask = new TCFTask<String[]>() {
					public void run() {
						final TCFTask<String[]> task = this;
						final String[] registerIDs = new String[] { regDMCID };
						tcfRegisterService.set(exeDMCID, registerIDs, registerValues, new ISimpleRegisters.DoneSet() {
							public void doneSet(IToken token, Exception error, String[] values) {
								if (error == null) {
									generateRegisterChangedEvent(regDMC);
								} else {
									rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, INTERNAL_ERROR,
											"Error writing register", null));
								}
								task.done(null);
							}
						});
					}
				};

				try {
					tcfTask.getIO();
				} catch (IOException e) {
					EDCDebugger.getMessageLogger().logError(null, e);
				}
			}
		}

		rm.done();
	}

	public void getAvailableFormats(IFormattedDataDMContext dmc, DataRequestMonitor<String[]> rm) {
		rm.setData(new String[] { IFormattedValues.HEX_FORMAT });
		rm.done();
	}

	public void getFormattedExpressionValue(FormattedValueDMContext dmc, DataRequestMonitor<FormattedValueDMData> rm) {
		if (dmc.getParents().length == 1 && dmc.getParents()[0] instanceof RegisterDMC) {
			getRegisterDataValue((RegisterDMC) dmc.getParents()[0], dmc.getFormatID(), rm);
		} else {
			rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
			rm.done();
		}
	}

	/**
	 * Read register with given ID, usually a name that's recognizable by TCF agent.
	 * This API is not good in error handling.  
	 * 
	 * @param context
	 * @param id
	 * @return a hex string on success, and {@link #REGISTER_VALUE_ERROR} on error.
	 */
	public String getRegisterValue(ExecutionDMC context, String id) {
		return getRegisterValue(new RegisterDMC(context, id, id, id));
	}

	/**
	 * See {@link #getRegisterValue(ExecutionDMC, String)}.
	 * @param registerDMC
	 * @return
	 */
	public String getRegisterValue(RegisterDMC registerDMC) {
		final StringBuffer regValue = new StringBuffer();
		getRegisterValue(registerDMC, new DataRequestMonitor<String>(ImmediateExecutor.getInstance(), null) {

			@Override
			protected void handleSuccess() {
				regValue.append(getData());
			}

			@Override
			protected void handleError() {
				// We pass this hex string instead of "unknown"
				// so that callers won't run into exception.
				regValue.append(REGISTER_VALUE_ERROR);
			}
		});

		return regValue.toString();
	}

	public void getRegisterValue(RegisterDMC registerDMC, final DataRequestMonitor<String> rm) {

		IExecutionDMContext exeDMC = DMContexts.getAncestorOfType(registerDMC, IExecutionDMContext.class);
		if (exeDMC != null && exeDMC instanceof DMContext) {

			final String exeDMCID = ((DMContext) exeDMC).getID();
			final String registerDMCID = registerDMC.getID();

			synchronized (registerValueCache) {

				Map<String, String> exeDMCRegisters = registerValueCache.get(exeDMCID);
				if (exeDMCRegisters != null) {
					String cachedValue = exeDMCRegisters.get(registerDMC.getID());
					if (cachedValue != null) {
						rm.setData(cachedValue);
						rm.done();
						return;
					}
				}

			}

			if (tcfRegisterService == null) {
				rm.setData(REGISTER_VALUE_ERROR);
				rm.done();
				return;
			}

			// ??? This has to be synchronous at present, see caller of this 
			// method for reason. But it should be asynchronous.
			//   
			TCFTask<String[]> tcfTask = new TCFTask<String[]>() {

				public void run() {
					final TCFTask<String[]> task = this;
					final String[] registerIDs = new String[] { registerDMCID };
					tcfRegisterService.get(exeDMCID, registerIDs, new ISimpleRegisters.DoneGet() {

						public void doneGet(IToken token, Exception error, String[] values) {

							if (error != null) {
								String errMsg = error.getLocalizedMessage(); 
								if (error instanceof IErrorReport)
									errMsg = (String)((IErrorReport)error).getAttributes().get(IErrorReport.ERROR_FORMAT);
								
								rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, REQUEST_FAILED,
										errMsg, null));
							}
							else {
								assert values.length == 1;
								synchronized (registerValueCache) {
									Map<String, String> exeDMCRegisters = registerValueCache.get(exeDMCID);
									if (exeDMCRegisters == null) {
										exeDMCRegisters = new HashMap<String, String>();
										registerValueCache.put(exeDMCID, exeDMCRegisters);
									}
									exeDMCRegisters.put(registerDMCID, values[0]);
								}

								rm.setData(values[0]);
							}
							task.done(values);
						}
					});
				}
			};
			
			try {
				tcfTask.getIO();	// ignore the return
				rm.done();
			} catch (IOException e) {
				EDCDebugger.getMessageLogger().logError("IOExceptoin reading register " + registerDMC.getName(), e);
			}
		}

	}

	private void generateRegisterChangedEvent(IRegisterDMContext dmc) {
		getSession().dispatchEvent(new RegisterChangedDMEvent(dmc), getProperties());
	}

	private void getRegisterDataValue(RegisterDMC registerDMC, String formatID,
			final DataRequestMonitor<FormattedValueDMData> rm) {

		getRegisterValue(registerDMC, new DataRequestMonitor<String>(getExecutor(), rm) {

			@Override
			protected void handleSuccess() {
				rm.setData(new FormattedValueDMData(getData()));
				rm.done();
			}
		});
	}

	public FormattedValueDMContext getFormattedValueContext(IFormattedDataDMContext dmc, String formatId) {
		if (dmc instanceof RegisterDMC) {
			return new FormattedValueDMContext(Registers.this, dmc, formatId);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public void getModelData(IDMContext dmc, DataRequestMonitor<?> rm) {

		if (dmc instanceof RegisterGroupDMC)
			getRegisterGroupData((IRegisterGroupDMContext) dmc, (DataRequestMonitor<IRegisterGroupDMData>) rm);
		else if (dmc instanceof RegisterDMC)
			getRegisterData((IRegisterDMContext) dmc, (DataRequestMonitor<IRegisterDMData>) rm);
		else if (dmc instanceof FormattedValueDMContext)
			getFormattedExpressionValue((FormattedValueDMContext) dmc, (DataRequestMonitor<FormattedValueDMData>) rm);
		else
			rm.done();
	}

	public RegisterGroupDMC[] getAllRegisterGroups() {
		List<RegisterGroupDMC> allGroups = new ArrayList<RegisterGroupDMC>();
		synchronized (groups) {
			Collection<List<RegisterGroupDMC>> allValues = groups.values();
			for (List<RegisterGroupDMC> list : allValues) {
				allGroups.addAll(list);
			}
		}
		return allGroups.toArray(new RegisterGroupDMC[allGroups.size()]);
	}

	public void flushCache(IDMContext context) {
		if (isSnapshot())
			return;
		groups = Collections.synchronizedMap(new HashMap<String, List<RegisterGroupDMC>>());
		registerValueCache = Collections.synchronizedMap(new HashMap<String, Map<String, String>>());
	}

	public void loadGroupsForContext(ThreadExecutionDMC threadExecutionDmc, Element element) throws Exception {
		// Can't call flushCache here because it does nothing for snapshot
		// services.
		groups = Collections.synchronizedMap(new HashMap<String, List<RegisterGroupDMC>>());
		registerValueCache = Collections.synchronizedMap(new HashMap<String, Map<String, String>>());

		NodeList registerGroups = element.getElementsByTagName(RegisterGroupDMC.REGISTER_GROUP);

		List<RegisterGroupDMC> regGroups = Collections.synchronizedList(new ArrayList<RegisterGroupDMC>());

		int numGroups = registerGroups.getLength();
		for (int i = 0; i < numGroups; i++) {
			Element groupElement = (Element) registerGroups.item(i);
			Element propElement = (Element) groupElement.getElementsByTagName(SnapshotUtils.PROPERTIES).item(0);
			HashMap<String, Object> properties = new HashMap<String, Object>();
			SnapshotUtils.initializeFromXML(propElement, properties);

			RegisterGroupDMC regdmc = new RegisterGroupDMC(this, threadExecutionDmc, properties);
			regdmc.loadSnapshot(groupElement);
			regGroups.add(regdmc);
		}
		groups.put(threadExecutionDmc.getID(), regGroups);
	}

	public void tcfServiceReady(IService service) {
		tcfRegisterService = (ISimpleRegisters) service;
	}

	public String getRegisterValue(ExecutionDMC executionDMC, int id) {
		String name = getRegisterNameFromCommonID(id);
		if (name != null) {
			return getRegisterValue(executionDMC, name);
		}
		return null;
	}

	@DsfServiceEventHandler
	public void eventDispatched(ISuspendedDMEvent e) {
		flushCache(null);
	}

	@DsfServiceEventHandler
	public void eventDispatched(IResumedDMEvent e) {
		flushCache(null);
	}

}
