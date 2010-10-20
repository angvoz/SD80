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
package org.eclipse.cdt.debug.edc.services;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.debug.edc.MemoryUtils;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.NumberFormatUtils;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl.ExecutionDMC;
import org.eclipse.cdt.debug.edc.internal.snapshot.SnapshotUtils;
import org.eclipse.cdt.debug.edc.services.Stack.StackFrameDMC;
import org.eclipse.cdt.debug.edc.snapshot.IAlbum;
import org.eclipse.cdt.debug.edc.snapshot.ISnapshotContributor;
import org.eclipse.cdt.debug.edc.tcf.extension.services.ISimpleRegisters;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.ICachingService;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IRegisters;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExitedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IRegisters.RegistersContext;
import org.eclipse.tm.tcf.util.TCFTask;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class Registers extends AbstractEDCService implements IRegisters, ICachingService, IDSFServiceUsingTCF {

	/**
	 * Cache register groups per context.
	 * Keyed on context ID.
	 */
	private Map<String, List<RegisterGroupDMC>> registerGroupsPerContext = 
		Collections.synchronizedMap(new HashMap<String, List<RegisterGroupDMC>>());

	private ISimpleRegisters 	tcfSimpleRegistersService = null;
	protected org.eclipse.tm.tcf.services.IRegisters		tcfRegistersService = null;
	
	/**
	 * Register value cache per execution context.
	 * Keyed on context ID.
	 */
	private Map<String, Map<String, String>> registerValueCache = 
		Collections.synchronizedMap(new HashMap<String, Map<String, String>>());

	/**
	 * A hex string indicating error in register read.
	 * See where this is used for more.
	 */
	protected static final String REGISTER_VALUE_ERROR = "badbadba";
	
	public static final String PROP_EXECUTION_CONTEXT_ID = "Context_ID";

	private static final String REGISTER = "register";

	public class RegisterGroupDMC extends DMContext implements IRegisterGroupDMContext, ISnapshotContributor {

		private static final String REGISTER_GROUP = "register_group";

		private List<RegisterDMC> registers = Collections.synchronizedList(new ArrayList<RegisterDMC>());

		private final IEDCExecutionDMC exeContext;

		public RegisterGroupDMC(Registers service, IEDCExecutionDMC contDmc, String groupName, String groupDescription,
				String groupID) {
			super(service, new IDMContext[] { contDmc }, groupName, groupID);
			exeContext = contDmc;
			properties.put(PROP_DESCRIPTION, groupDescription);
			properties.put(PROP_EXECUTION_CONTEXT_ID, contDmc.getID());
		}

		public RegisterGroupDMC(Registers service, IEDCExecutionDMC executionDmc,
								Map<String, Object> props) {
			super(service, new IDMContext[] { executionDmc }, props);
			exeContext = (IEDCExecutionDMC) executionDmc;
			properties.put(PROP_EXECUTION_CONTEXT_ID, exeContext.getID());
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

		public Element takeShapshot(IAlbum album, Document document, IProgressMonitor monitor) {
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

		public IEDCExecutionDMC getExecutionDMC() {
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

		private org.eclipse.tm.tcf.services.IRegisters.RegistersContext tcfContext = null;
		
		public RegisterDMC(IEDCExecutionDMC exeDMC, String name, String description, String id) {
			super(Registers.this, new IDMContext[] { exeDMC }, name, id);
			properties.put(PROP_EXECUTION_CONTEXT_ID, exeDMC.getID());
		}

		public RegisterDMC(RegisterGroupDMC registerGroupDmc, IEDCExecutionDMC exeDMC,
							Map<String, Object> properties) {
			super(Registers.this, new IDMContext[] { exeDMC }, properties);
			this.properties.put(PROP_EXECUTION_CONTEXT_ID, exeDMC.getID());
		}

		/**
		 * Construct based on underlying context from TCF IRegisters service.
		 * 
		 * @param registerGroupDMC
		 * @param exeDMC
		 * @param tcfContext
		 */
		public RegisterDMC(RegisterGroupDMC registerGroupDMC, IEDCExecutionDMC exeDMC, RegistersContext tcfContext) {
			super(Registers.this, new IDMContext[] { registerGroupDMC }, tcfContext.getProperties());
			this.properties.put(PROP_EXECUTION_CONTEXT_ID, exeDMC.getID());
			
			this.tcfContext = tcfContext;
		}

		/**
		 * get underlying TCF context.
		 * @return may be null.
		 */
		public RegistersContext getTCFContext() {
			return tcfContext;
		}
		
		@Override
		public String toString() {
			return baseToString() + ".register[" + getName() + "]";} //$NON-NLS-1$ //$NON-NLS-2$

		public Element takeShapshot(IAlbum album, Document document, IProgressMonitor monitor) {
			Element registerElement = document.createElement(REGISTER);
			registerElement.setAttribute(PROP_ID, this.getID());
			registerElement.setAttribute(PROP_VALUE, getRegisterValue(this));
			Element propsElement = SnapshotUtils.makeXMLFromProperties(document, getProperties());
			registerElement.appendChild(propsElement);
			return registerElement;
		}

		public void loadSnapshot(Element element) throws Exception {
			String registerValue = element.getAttribute(PROP_VALUE);
			String contextID = (String) getProperties().get(PROP_EXECUTION_CONTEXT_ID);

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

	class RegisterData implements IRegisterDMData {

		private final HashMap<String, Object> properties = new HashMap<String, Object>();

		public RegisterData(Map<String, Object> properties) {
			this.properties.putAll(properties);
		}

		public boolean isReadable() {
            Boolean n = (Boolean)properties.get(org.eclipse.tm.tcf.services.IRegisters.PROP_READBLE);
            if (n == null) 
            	return true;
            return n.booleanValue();
		}

		public boolean isReadOnce() {
            Boolean n = (Boolean)properties.get(org.eclipse.tm.tcf.services.IRegisters.PROP_READ_ONCE);
            if (n == null) 
            	return false;
            return n.booleanValue();
		}

		public boolean isWriteable() {
            Boolean n = (Boolean)properties.get(org.eclipse.tm.tcf.services.IRegisters.PROP_WRITEABLE);
            if (n == null) 
            	return true;
            return n.booleanValue();
		}

		public boolean isWriteOnce() {
            Boolean n = (Boolean)properties.get(org.eclipse.tm.tcf.services.IRegisters.PROP_WRITE_ONCE);
            if (n == null) 
            	return false;
            return n.booleanValue();
		}

		public boolean hasSideEffects() {
            Boolean n = (Boolean)properties.get(org.eclipse.tm.tcf.services.IRegisters.PROP_SIDE_EFFECTS);
            if (n == null) 
            	return false;
            return n.booleanValue();
		}

		public boolean isVolatile() {
            Boolean n = (Boolean)properties.get(org.eclipse.tm.tcf.services.IRegisters.PROP_VOLATILE);
            if (n == null) 
            	return false;
            return n.booleanValue();
		}

		public boolean isFloat() {
            Boolean n = (Boolean)properties.get(org.eclipse.tm.tcf.services.IRegisters.PROP_FLOAT);
            if (n == null) 
            	return false;
            return n.booleanValue();
		}

		public String getName() {
			return (String) properties.get(IEDCDMContext.PROP_NAME);
		}

		public String getDescription() {
			return (String) properties.get(IEDCDMContext.PROP_DESCRIPTION);
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

	/**
	 * @param classNames
	 *            the type names the service will be registered under. See
	 *            AbstractDsfService#register for details. We tack on base DSF's
	 *            IRegisters and this class to the list if missing.
	 */
	public Registers(DsfSession session, String[] classNames) {
		super(session, 
				massageClassNames(classNames,
						new String[] {IRegisters.class.getName(), Registers.class.getName()}));
	}

	/**
	 * Find register DMC by register name. <br>
	 * 
	 * It's required the register name be known/recognizable to TCF agent,
	 * meaning host debugger still cannot be totally target neutral on register
	 * access. TCF IRegisters service allows us to access common registers such
	 * as PC, LP and SP in a target-independent way (using Role property). But
	 * debugger need to access other registers (e.g. R0, R1, CPSR on ARM) for
	 * stack crawl and variable evaluation.
	 * 
	 * @param exeDMC
	 * @param name
	 * @return
	 */
	private RegisterDMC findRegisterDMCByName(IEDCExecutionDMC exeDMC, String name) {
		assert RunControl.isNonContainer(exeDMC);
		
		// this will create the reg groups for the exeDMC if not yet. 
		IRegisterGroupDMContext[] regGroups = getGroupsForContext(exeDMC);
		
		for (IRegisterGroupDMContext g : regGroups) {
			// Note the getRegisters() will create registerDMCs for the group if not yet. 
			for (RegisterDMC reg : ((RegisterGroupDMC)g).getRegisters()) {
				String n = (String)reg.getProperties().get(org.eclipse.tm.tcf.services.IRegisters.PROP_NAME);
				if (name.equals(n))
					return reg;
			}
		}
		
		return null;
	}

	@Override
	protected void doInitialize(RequestMonitor requestMonitor) {
		super.doInitialize(requestMonitor);
		getSession().addServiceEventListener(this, null);
	}

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
				this.description = (String) dmc.getProperty(IEDCDMContext.PROP_DESCRIPTION);
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

	public IRegisterGroupDMContext[] getGroupsForContext(IEDCExecutionDMC exeContext) {
		String contextID = exeContext.getID();
		List<RegisterGroupDMC> groupsForContext = registerGroupsPerContext.get(contextID);
		if (groupsForContext == null) {
			groupsForContext = createGroupsForContext(exeContext);
			synchronized (registerGroupsPerContext) {
				registerGroupsPerContext.put(contextID, groupsForContext);
			}
		}
		return groupsForContext.toArray(new IRegisterGroupDMContext[groupsForContext.size()]);
	}

	public void getRegisterGroups(IDMContext ctx, DataRequestMonitor<IRegisterGroupDMContext[]> rm) {
		IEDCExecutionDMC execDmc = DMContexts.getAncestorOfType(ctx, IEDCExecutionDMC.class);
		if (execDmc != null && RunControl.isNonContainer(execDmc)) {
			rm.setData(getGroupsForContext(execDmc));
			rm.done();
			return;
		}

		StackFrameDMC frameDmc = DMContexts.getAncestorOfType(ctx, StackFrameDMC.class);
		if (frameDmc != null) {
			rm.setData(getGroupsForContext(frameDmc.getExecutionDMC()));
			rm.done();
			return;
		}
		
		rm.setData(new IRegisterGroupDMContext[0]);
		rm.done();
	}

	public void getRegisters(IDMContext ctx, DataRequestMonitor<IRegisterDMContext[]> rm) {
		RegisterGroupDMC groupContext = DMContexts.getAncestorOfType(ctx, RegisterGroupDMC.class);
		IEDCExecutionDMC executionContext = DMContexts.getAncestorOfType(ctx, IEDCExecutionDMC.class);

		RegisterDMC[] allRegisters;
		if (groupContext != null && executionContext != null) {
			allRegisters = groupContext.getRegisters();
		}
		else {
			allRegisters = new RegisterDMC[0];
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
	public void writeRegister(IEDCExecutionDMC context, String regID, String regValue) {
		RegisterDMC regDMC;
		
		regDMC = findRegisterDMCByName(context, regID);
		assert regDMC != null;
		
		writeRegister(regDMC, regValue, IFormattedValues.HEX_FORMAT,
				new RequestMonitor(getExecutor(), null));
	}

	public void writeRegister(IRegisterDMContext regCtx, String regValue, String formatID, final RequestMonitor rm) {
		assert (regCtx instanceof RegisterDMC);
		
		final RegisterDMC regDMC = (RegisterDMC) regCtx;
		IExecutionDMContext exeDMC = DMContexts.getAncestorOfType(regDMC, IExecutionDMContext.class);
		if (exeDMC == null || !(exeDMC instanceof IEDCDMContext)) {
			Status s = new Status(IStatus.ERROR, EDCDebugger.getUniqueIdentifier(), "No valid executionDMC for the register.");
			EDCDebugger.getMessageLogger().log(s);
			rm.setStatus(s);
			rm.done();
			return;
		}

		final String exeDMCID = ((IEDCDMContext) exeDMC).getID();
		final String regDMCID = regDMC.getID();
		
		// Put the incoming value into hex
		if (formatID.equals(IFormattedValues.OCTAL_FORMAT) || formatID.equals(IFormattedValues.BINARY_FORMAT) ||
				formatID.equals(IFormattedValues.DECIMAL_FORMAT))
		{
			BigInteger bigRegValue = NumberFormatUtils.parseIntegerByFormat(regValue, formatID);
			regValue = bigRegValue.toString(16);
		}

		// Update cached register values
		Map<String, String> exeDMCRegisters = registerValueCache.get(exeDMCID);
		if (exeDMCRegisters != null) {
			exeDMCRegisters.put(regDMC.getID(), regValue);
		}

		if (tcfRegistersService != null) {	// TCF IRegisters service available
			final RegistersContext tcfReg = regDMC.getTCFContext();
			byte[] bv = null;
			try {
				bv = MemoryUtils.convertHexStringToByteArray(regValue, tcfReg.getSize(), 2);
			} catch (NumberFormatException e) {
				Status s = new Status(IStatus.ERROR, EDCDebugger.getUniqueIdentifier(), "Error writing register.", e);
				EDCDebugger.getMessageLogger().log(s);
				rm.setStatus(s);
				rm.done();
				return;
			}
			
			final byte[] byteVal = bv;
			
			TCFTask<Object> tcfTask = new TCFTask<Object>() {
				public void run() {
					tcfReg.set(byteVal, new org.eclipse.tm.tcf.services.IRegisters.DoneSet() {
						public void doneSet(IToken token, Exception error) {
							if (error == null) {
								generateRegisterChangedEvent(regDMC);
							} else {
								rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, INTERNAL_ERROR,
										"Error writing register.", error));
							}
							done(null);
						}
					});
				}
			};
	
			try {
				tcfTask.get(15, TimeUnit.SECONDS);
			} catch (Throwable e) {
				rm.setStatus(EDCDebugger.dsfRequestFailedStatus(null, e));
			} finally {
				if (!rm.isSuccess())
					EDCDebugger.getMessageLogger().log(rm.getStatus());
				rm.done();
			}
		}
		else {
			// Ensure connection to service agent
			if (tcfSimpleRegistersService == null) {
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
					final String[] registerIDs = new String[] { regDMCID };
					tcfSimpleRegistersService.set(exeDMCID, registerIDs, registerValues, new ISimpleRegisters.DoneSet() {
						public void doneSet(IToken token, Exception error, String[] values) {
							if (error == null) {
								generateRegisterChangedEvent(regDMC);
							} else {
								rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, INTERNAL_ERROR,
										"Error writing register.", error));
							}
							done(null);
						}
					});
				}
			};
	
			try {
				tcfTask.get(15, TimeUnit.SECONDS);
			} catch (Throwable e) {
				rm.setStatus(EDCDebugger.dsfRequestFailedStatus(null, e));
			} finally {
				rm.done();
			}
		}
	}

	public void getAvailableFormats(IFormattedDataDMContext dmc, DataRequestMonitor<String[]> rm) {
        rm.setData(new String[] { HEX_FORMAT, DECIMAL_FORMAT, OCTAL_FORMAT, BINARY_FORMAT, NATURAL_FORMAT });
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
	public String getRegisterValue(IExecutionDMContext context, String id) {
		RegisterDMC regDMC;
		
		regDMC = findRegisterDMCByName((IEDCExecutionDMC) context, id);
		assert regDMC != null;
		
		return getRegisterValue(regDMC);
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
				EDCDebugger.getMessageLogger().log(getStatus());
				regValue.append(REGISTER_VALUE_ERROR);
			}
		});

		return regValue.toString();
	}

	public void getRegisterValue(RegisterDMC registerDMC, final DataRequestMonitor<String> rm) {

		IExecutionDMContext exeDMC = DMContexts.getAncestorOfType(registerDMC, IExecutionDMContext.class);
		if (exeDMC == null || !(exeDMC instanceof IEDCDMContext)) {
			rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.getUniqueIdentifier(), "No valid executionDMC for the register."));
			rm.done();
			return;
		}

		final String exeDMCID = ((IEDCDMContext) exeDMC).getID();
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
	
		if (tcfRegistersService != null) {	// TCF IRegisters service available
			final RegistersContext tcfReg = registerDMC.getTCFContext();
			
            if (tcfReg == null) {
                    rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.getUniqueIdentifier(), "RegisterDMC " + registerDMC.getID() + " has no underlying TCF register context."));
                    rm.done();
                    return;
            }

            TCFTask<byte[]> tcfTask = new TCFTask<byte[]>() {
		
				public void run() {
					tcfReg.get(new org.eclipse.tm.tcf.services.IRegisters.DoneGet() {
		
						public void doneGet(IToken token, Exception error, byte[] value) {
		
							if (error != null) {
								rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, REQUEST_FAILED, "Error reading register.", error));
							}
							else {
								String strVal = MemoryUtils.convertByteArrayToHexString(value);
								synchronized (registerValueCache) {
									Map<String, String> exeDMCRegisters = registerValueCache.get(exeDMCID);
									if (exeDMCRegisters == null) {
										exeDMCRegisters = new HashMap<String, String>();
										registerValueCache.put(exeDMCID, exeDMCRegisters);
									}
									exeDMCRegisters.put(registerDMCID, strVal);
								}
		
								rm.setData(strVal);
							}
		
							done(value);
						}
					});
				}
			};
			
			try {
				tcfTask.get(15, TimeUnit.SECONDS);	// ignore the return
			} catch (Throwable e) {
				rm.setStatus(EDCDebugger.dsfRequestFailedStatus("Exception reading register " + registerDMC.getName(), e));
			} finally {
				rm.done();
			}
		}
		else {
			if (tcfSimpleRegistersService == null) {
				rm.setData(REGISTER_VALUE_ERROR);
				rm.done();
				return;
			}
		
			TCFTask<String[]> tcfTask = new TCFTask<String[]>() {
		
				public void run() {
					final String[] registerIDs = new String[] { registerDMCID };
					tcfSimpleRegistersService.get(exeDMCID, registerIDs, new ISimpleRegisters.DoneGet() {
		
						public void doneGet(IToken token, Exception error, String[] values) {
		
							if (error != null) {
								rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, REQUEST_FAILED,
										"Error reading register.", error));
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
		
							done(values);
						}
					});
				}
			};
			
			try {
				tcfTask.get(15, TimeUnit.SECONDS);	// ignore the return
			} catch (Throwable e) {
				rm.setStatus(EDCDebugger.dsfRequestFailedStatus("Failed to read register " + registerDMC.getName(), e));
			} finally {
				rm.done();
			}
		}
	}

	private void generateRegisterChangedEvent(IRegisterDMContext dmc) {
		getSession().dispatchEvent(new RegisterChangedDMEvent(dmc), getProperties());
	}

	private void getRegisterDataValue(RegisterDMC registerDMC, final String formatID,
			final DataRequestMonitor<FormattedValueDMData> rm) {

		getRegisterValue(registerDMC, new DataRequestMonitor<String>(getExecutor(), rm) {

			@Override
			protected void handleSuccess() {
				String registerValueAsHexString = getData();
				String formattedValue = registerValueAsHexString;
				BigInteger bigIntValue = new BigInteger(registerValueAsHexString, 16);
				
				if (formatID.equals(IFormattedValues.OCTAL_FORMAT))
					formattedValue = NumberFormatUtils.toOctalString(bigIntValue);
				if (formatID.equals(IFormattedValues.BINARY_FORMAT))
					formattedValue = NumberFormatUtils.asBinary(bigIntValue);
				if (formatID.equals(IFormattedValues.DECIMAL_FORMAT))
					formattedValue = bigIntValue.toString();
				
				rm.setData(new FormattedValueDMData(formattedValue));
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

	public void flushCache(IDMContext context) {
		if (isSnapshot())
			return;
		// Why flush this static info ?
		// registerGroupsPerThread.clear();
		
		registerValueCache.clear();
	}

	public void loadGroupsForContext(IEDCExecutionDMC executionDmc, Element element) throws Exception {
		// Can't call flushCache here because it does nothing for snapshot
		// services.
		registerGroupsPerContext.clear();
		registerValueCache.clear();

		NodeList registerGroups = element.getElementsByTagName(RegisterGroupDMC.REGISTER_GROUP);

		List<RegisterGroupDMC> regGroups = Collections.synchronizedList(new ArrayList<RegisterGroupDMC>());

		int numGroups = registerGroups.getLength();
		for (int i = 0; i < numGroups; i++) {
			Element groupElement = (Element) registerGroups.item(i);
			Element propElement = (Element) groupElement.getElementsByTagName(SnapshotUtils.PROPERTIES).item(0);
			HashMap<String, Object> properties = new HashMap<String, Object>();
			SnapshotUtils.initializeFromXML(propElement, properties);

			RegisterGroupDMC regdmc = new RegisterGroupDMC(this, executionDmc, properties);
			regdmc.loadSnapshot(groupElement);
			regGroups.add(regdmc);
		}
		registerGroupsPerContext.put(((IEDCDMContext) executionDmc).getID(), regGroups);
	}

	public void tcfServiceReady(IService service) {
		if (service instanceof ISimpleRegisters)
			tcfSimpleRegistersService = (ISimpleRegisters) service;
		else
			tcfRegistersService = (org.eclipse.tm.tcf.services.IRegisters)service;
	}

	public String getRegisterValue(IEDCExecutionDMC executionDMC, int id) {
		String name = getRegisterNameFromCommonID(id);
		if (name != null) {
			return getRegisterValue(executionDMC, name);
		}
		return null;
	}

	/** 
	 * Get TCF child registers contexts for the given parent.
	 * If parent is a thread, the registers contexts are register groups.
	 * If parent is a register group, the contexts returned are registers.
	 *   
	 * @param parentID thread ID or register group ID.
	 * @return
	 */
	protected List<RegistersContext>	getTCFRegistersContexts(final String parentID) {
		List<RegistersContext> tcfRegContexts = new ArrayList<RegistersContext>();
		
		TCFTask<String[]> getChildIDTask = new TCFTask<String[]>() {
			public void run() {
				tcfRegistersService.getChildren(parentID, new org.eclipse.tm.tcf.services.IRegisters.DoneGetChildren() {

					public void doneGetChildren(IToken token, Exception error, String[] contextIds) {
						if (error == null)
							done(contextIds);
						else
							error(error);
					}});
			}
		};
		
		String[] childIDs;
		try {
			childIDs = getChildIDTask.get(15, TimeUnit.SECONDS);
		} catch (Throwable e) {
			EDCDebugger.getMessageLogger().logError("Fail to get TCF context for: " + parentID, e);
			return tcfRegContexts;
		}
		
		for (String gid: childIDs) {
			final String id = gid;
			TCFTask<RegistersContext> getGroupContextTask = new TCFTask<RegistersContext>() {
				public void run() {
					tcfRegistersService.getContext(id, new org.eclipse.tm.tcf.services.IRegisters.DoneGetContext(){
						public void doneGetContext(IToken token, Exception error, RegistersContext context) {
							if (error == null)
								done(context);
							else
								error(error);
						}});
				}
			};
		
			RegistersContext rgc = null;
			try {
				rgc = getGroupContextTask.get(15, TimeUnit.SECONDS);
			} catch (Throwable e) {
				EDCDebugger.getMessageLogger().logError("Fail to get TCF context with ID: " + gid, e);
			}
			
			if (rgc != null)
				tcfRegContexts.add(rgc);
		}

		return tcfRegContexts;
	}
	
	@DsfServiceEventHandler
	public void eventDispatched(ISuspendedDMEvent e) {
		flushCache(null);
	}

	@DsfServiceEventHandler
	public void eventDispatched(IResumedDMEvent e) {
		flushCache(null);
	}

	/**
	 * When a context (e.g. a thread) is killed/detached, we should forget
	 * cached register info & values for it so that we can properly access
	 * registers when we re-attach to it.
	 * 
	 * @since 2.0
	 */
	@DsfServiceEventHandler
	public void eventDispatched(IExitedDMEvent e) {
		IExecutionDMContext cxt = e.getDMContext();
		if (cxt != null && cxt instanceof IEDCDMContext) {
			String cxtID = ((IEDCDMContext)cxt).getID();
			// It does not hurt if the context is not in the caches.
			registerGroupsPerContext.remove(cxtID);
			registerValueCache.remove(cxtID);
		}
	}

	protected List<RegisterDMC> createRegistersForGroup(RegisterGroupDMC registerGroupDMC) {
		ArrayList<RegisterDMC> registers = new ArrayList<RegisterDMC>();
	
		if (tcfRegistersService != null) {
			List<RegistersContext> tcfRegs = getTCFRegistersContexts(registerGroupDMC.getID());
			
			for (RegistersContext rg: tcfRegs) {
				registers.add(new RegisterDMC(registerGroupDMC, registerGroupDMC.getExecutionDMC(), rg));
			}
		}
		
		return registers;
	}
	
	protected List<RegisterGroupDMC> createGroupsForContext(IEDCExecutionDMC ctx) {

		List<RegisterGroupDMC> groups = Collections.synchronizedList(new ArrayList<RegisterGroupDMC>());

		if (RunControl.isNonContainer(ctx)) {
			if (tcfRegistersService != null) {
				List<RegistersContext> tcfRegGroups = getTCFRegistersContexts(ctx.getID());
				
				for (RegistersContext rg: tcfRegGroups) {
					groups.add(new RegisterGroupDMC(this, ctx, rg.getProperties()));
				}
			}
		}

		return groups;
	}

	/**
	 * Given a common general purpose register id (e.g. from symbolics), get the
	 * corresponding register name.
	 * 
	 * @param id
	 *            the common general purpose register id (0-31)
	 * @return the corresponding register name, or null of n/a
	 */
	public abstract String getRegisterNameFromCommonID(int id);
}
