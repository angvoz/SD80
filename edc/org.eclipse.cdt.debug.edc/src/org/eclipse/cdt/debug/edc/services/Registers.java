/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
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
import org.eclipse.cdt.debug.edc.internal.snapshot.SnapshotUtils;
import org.eclipse.cdt.debug.edc.services.Stack.StackFrameDMC;
import org.eclipse.cdt.debug.edc.snapshot.IAlbum;
import org.eclipse.cdt.debug.edc.snapshot.ISnapshotContributor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.ICachingService;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IRegisters;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExitedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IRegisters.RegistersContext;
import org.eclipse.tm.tcf.util.TCFTask;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The Registers service provides information about the target processor
 * registers.
 */
public abstract class Registers extends AbstractEDCService implements IRegisters, ICachingService, IDSFServiceUsingTCF {

	/**
	 * Cache register groups per context.
	 * Keyed on context ID.
	 */
	private Map<String, List<RegisterGroupDMC>> registerGroupsPerContext = 
		Collections.synchronizedMap(new HashMap<String, List<RegisterGroupDMC>>());

	/** The TCF registers service. */
	protected org.eclipse.tm.tcf.services.IRegisters		tcfRegistersService = null;
	
	/**
	 * Register value cache per execution context.
	 * Keyed on context ID.
	 */
	private Map<String, Map<String, BigInteger>> registerValueCache = 
		Collections.synchronizedMap(new HashMap<String, Map<String, BigInteger>>());

	/** Iimeout value in milliseconds when waiting for a response from the TCF service. */
	private long tcfTimeout;

	/**
	 * A hex string indicating error in register read.
	 * See where this is used for more.
	 */
	protected static final String REGISTER_VALUE_ERROR = "badbadba";
	
	public static final String PROP_EXECUTION_CONTEXT_ID = "Context_ID";

	private static final String REGISTER = "register";

	/**
	 * Represents a group of registers.
	 */
	public class RegisterGroupDMC extends DMContext implements IRegisterGroupDMContext, ISnapshotContributor {

		private static final String REGISTER_GROUP = "register_group";

		/** The registers in this group. */
		private List<RegisterDMC> registers = Collections.synchronizedList(new ArrayList<RegisterDMC>());

		/** The executable context. */
		private final IEDCExecutionDMC exeContext;

		/**
		 * Instantiates a new register group dmc.
		 *
		 * @param service the service
		 * @param executionDMC the execution context
		 * @param groupName the group name
		 * @param groupDescription the group description
		 * @param groupID the group id
		 */
		public RegisterGroupDMC(Registers service, IEDCExecutionDMC executionDMC, String groupName, String groupDescription,
				String groupID) {
			super(service, new IDMContext[] { executionDMC }, groupName, groupID);
			exeContext = executionDMC;
			properties.put(PROP_DESCRIPTION, groupDescription);
			properties.put(PROP_EXECUTION_CONTEXT_ID, executionDMC.getID());
		}

		/**
		 * Instantiates a new register group dmc.
		 *
		 * @param service the service
		 * @param executionDmc the execution dmc
		 * @param props the props
		 */
		public RegisterGroupDMC(Registers service, IEDCExecutionDMC executionDmc,
								Map<String, Object> props) {
			super(service, new IDMContext[] { executionDmc }, props);
			exeContext = executionDmc;
			properties.put(PROP_EXECUTION_CONTEXT_ID, exeContext.getID());
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.services.DMContext#toString()
		 */
		@Override
		public String toString() {
			return baseToString() + ".group[" + getName() + "]";} //$NON-NLS-1$ //$NON-NLS-2$

		/**
		 * Gets the registers for this group.
		 *
		 * @return array of register contexts for this group
		 * @throws CoreException the core exception
		 */
		public RegisterDMC[] getRegisters() throws CoreException {
			RegisterDMC[] result = new RegisterDMC[0];
			synchronized (registers) {
				if (registers.size() == 0) {
					registers = Registers.this.createRegistersForGroup(this);
				}
				result = registers.toArray(new RegisterDMC[registers.size()]);
			}
			return result;
		}

		/**
		 * Take a snapshot of this group of registers.
		 *
		 * @param album the snapshot album
		 * @param document the XML document
		 * @param monitor the progress monitor
		 * @return the XML element
		 * @throws Exception the exception if anything goes wrong
		 * @since 2.0
		 */
		public Element takeSnapshot(IAlbum album, Document document, IProgressMonitor monitor)throws Exception {
			Element contextElement = document.createElement(REGISTER_GROUP);
			contextElement.setAttribute(PROP_ID, this.getID());

			Element propsElement = SnapshotUtils.makeXMLFromProperties(document, getProperties());
			contextElement.appendChild(propsElement);

			RegisterDMC[] allRegisters = getRegisters();
			SubMonitor progress = SubMonitor.convert(monitor, allRegisters.length * 1000);
			progress.subTask("Registers");
			for (RegisterDMC registerDMC : allRegisters) {
				Element dmcElement = registerDMC.takeSnapshot(album, document, progress.newChild(1000));
				contextElement.appendChild(dmcElement);
			}
			return contextElement;
		}

		/**
		 * Gets the execution dmc.
		 *
		 * @return the execution dmc
		 */
		public IEDCExecutionDMC getExecutionDMC() {
			return exeContext;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.snapshot.ISnapshotContributor#loadSnapshot(org.w3c.dom.Element)
		 */
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

	/**
	 * Represents the context for a single register.
	 */
	public class RegisterDMC extends DMContext implements IRegisterDMContext, ISnapshotContributor {

		/** The context used by the TCF agent. */
		private org.eclipse.tm.tcf.services.IRegisters.RegistersContext tcfContext = null;
		
		/**
		 * Instantiates a new register dmc.
		 *
		 * @param executableDMC the executable context
		 * @param name the register name
		 * @param description the register description
		 * @param id the register id
		 */
		public RegisterDMC(IEDCExecutionDMC executableDMC, String name, String description, String id) {
			super(Registers.this, new IDMContext[] { executableDMC }, name, id);
			properties.put(PROP_EXECUTION_CONTEXT_ID, executableDMC.getID());
		}

		/**
		 * Instantiates a new register dmc.
		 *
		 * @param registerGroupDmc the register group dmc
		 * @param executableDMC the executable context
		 * @param properties the properties
		 */
		public RegisterDMC(RegisterGroupDMC registerGroupDmc, IEDCExecutionDMC executableDMC,
							Map<String, Object> properties) {
			super(Registers.this, new IDMContext[] { executableDMC }, properties);
			this.properties.put(PROP_EXECUTION_CONTEXT_ID, executableDMC.getID());
		}

		/**
		 * Construct based on underlying context from TCF IRegisters service.
		 *
		 * @param registerGroupDMC the register group dmc
		 * @param executableDMC the executable context
		 * @param tcfContext the tcf context
		 */
		public RegisterDMC(RegisterGroupDMC registerGroupDMC, IEDCExecutionDMC executableDMC, RegistersContext tcfContext) {
			super(Registers.this, new IDMContext[] { registerGroupDMC }, tcfContext.getProperties());
			this.properties.put(PROP_EXECUTION_CONTEXT_ID, executableDMC.getID());
			
			this.tcfContext = tcfContext;
		}

		/**
		 * Get the underlying TCF context.
		 * @return may be null.
		 */
		public RegistersContext getTCFContext() {
			return tcfContext;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.services.DMContext#toString()
		 */
		@Override
		public String toString() {
			return baseToString() + ".register[" + getName() + "]";} //$NON-NLS-1$ //$NON-NLS-2$

		/**
		 * Take a snapshot of this register.
		 *
		 * @param album the snapshot album
		 * @param document the XML document
		 * @param monitor the progress monitor
		 * @return the XML element
		 * @throws Exception the exception if anything goes wrong
		 * @since 2.0
		 */
		public Element takeSnapshot(IAlbum album, Document document, IProgressMonitor monitor) throws Exception {
			Element registerElement = document.createElement(REGISTER);
			registerElement.setAttribute(PROP_ID, this.getID());
			registerElement.setAttribute(PROP_VALUE, getRegisterValueAsHexString(this));
			Element propsElement = SnapshotUtils.makeXMLFromProperties(document, getProperties());
			registerElement.appendChild(propsElement);
			return registerElement;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.snapshot.ISnapshotContributor#loadSnapshot(org.w3c.dom.Element)
		 */
		public void loadSnapshot(Element element) throws Exception {
			String registerValue = element.getAttribute(PROP_VALUE);
			String contextID = (String) getProperties().get(PROP_EXECUTION_CONTEXT_ID);

			synchronized (registerValueCache) {
				Map<String, BigInteger> exeDMCRegisters = registerValueCache.get(contextID);
				if (exeDMCRegisters == null) {
					exeDMCRegisters = new HashMap<String, BigInteger>();
					registerValueCache.put(contextID, exeDMCRegisters);
				}
				exeDMCRegisters.put(getID(), new BigInteger(registerValue, 16));
			}
		}
	}

	class RegisterData implements IRegisterDMData {

		private final HashMap<String, Object> properties = new HashMap<String, Object>();

		public RegisterData(Map<String, Object> properties) {
			this.properties.putAll(properties);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMData#isReadable()
		 */
		public boolean isReadable() {
            Boolean n = (Boolean)properties.get(org.eclipse.tm.tcf.services.IRegisters.PROP_READBLE);
            if (n == null) 
            	return true;
            return n.booleanValue();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMData#isReadOnce()
		 */
		public boolean isReadOnce() {
            Boolean n = (Boolean)properties.get(org.eclipse.tm.tcf.services.IRegisters.PROP_READ_ONCE);
            if (n == null) 
            	return false;
            return n.booleanValue();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMData#isWriteable()
		 */
		public boolean isWriteable() {
            Boolean n = (Boolean)properties.get(org.eclipse.tm.tcf.services.IRegisters.PROP_WRITEABLE);
            if (n == null) 
            	return true;
            return n.booleanValue();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMData#isWriteOnce()
		 */
		public boolean isWriteOnce() {
            Boolean n = (Boolean)properties.get(org.eclipse.tm.tcf.services.IRegisters.PROP_WRITE_ONCE);
            if (n == null) 
            	return false;
            return n.booleanValue();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMData#hasSideEffects()
		 */
		public boolean hasSideEffects() {
            Boolean n = (Boolean)properties.get(org.eclipse.tm.tcf.services.IRegisters.PROP_SIDE_EFFECTS);
            if (n == null) 
            	return false;
            return n.booleanValue();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMData#isVolatile()
		 */
		public boolean isVolatile() {
            Boolean n = (Boolean)properties.get(org.eclipse.tm.tcf.services.IRegisters.PROP_VOLATILE);
            if (n == null) 
            	return false;
            return n.booleanValue();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMData#isFloat()
		 */
		public boolean isFloat() {
            Boolean n = (Boolean)properties.get(org.eclipse.tm.tcf.services.IRegisters.PROP_FLOAT);
            if (n == null) 
            	return false;
            return n.booleanValue();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMData#getName()
		 */
		public String getName() {
			return (String) properties.get(IEDCDMContext.PROP_NAME);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMData#getDescription()
		 */
		public String getDescription() {
			return (String) properties.get(IEDCDMContext.PROP_DESCRIPTION);
		}

	}

	/**
	 * Event class to notify register value is changed
	 */
	public static class RegisterChangedDMEvent implements IRegisters.IRegisterChangedDMEvent {

		/** The register dmc. */
		private final IRegisterDMContext fRegisterDMC;

		/**
		 * Instantiates a new register changed dm event.
		 *
		 * @param registerDMC the register dmc
		 */
		RegisterChangedDMEvent(IRegisterDMContext registerDMC) {
			fRegisterDMC = registerDMC;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.dsf.datamodel.IDMEvent#getDMContext()
		 */
		public IRegisterDMContext getDMContext() {
			return fRegisterDMC;
		}
	}

	/**
	 * Instantiates a new Registers service.
	 *
	 * @param session the session
	 * @param classNames the type names the service will be registered under. See
	 * AbstractDsfService#register for details. We tack on base DSF's
	 * IRegisters and this class to the list if missing.
	 */
	public Registers(DsfSession session, String[] classNames) {
		super(session, 
				massageClassNames(classNames,
						new String[] {IRegisters.class.getName(), Registers.class.getName()}));
		setTCFTimeout(15 * 1000); // Fifteen seconds
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
	 * @param exeDMC the exe dmc
	 * @param name the name
	 * @return the register dmc
	 * @throws CoreException the core exception
	 * @since 2.0
	 */
	public RegisterDMC findRegisterDMCByName(IEDCExecutionDMC exeDMC, String name) throws CoreException {
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.services.AbstractEDCService#doInitialize(org.eclipse.cdt.dsf.concurrent.RequestMonitor)
	 */
	@Override
	protected void doInitialize(RequestMonitor requestMonitor) {
		super.doInitialize(requestMonitor);
		getSession().addServiceEventListener(this, null);
	}

	/**
	 * Gets the groups for context.
	 *
	 * @param executableContext the executable context
	 * @return the groups for context
	 * @throws CoreException the core exception
	 */
	public IRegisterGroupDMContext[] getGroupsForContext(IEDCExecutionDMC executableContext) throws CoreException {
		String contextID = executableContext.getID();
		List<RegisterGroupDMC> groupsForContext = registerGroupsPerContext.get(contextID);
		if (groupsForContext == null) {
			groupsForContext = createGroupsForContext(executableContext);
			synchronized (registerGroupsPerContext) {
				registerGroupsPerContext.put(contextID, groupsForContext);
			}
		}
		return groupsForContext.toArray(new IRegisterGroupDMContext[groupsForContext.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IRegisters#writeBitField(org.eclipse.cdt.dsf.debug.service.IRegisters.IBitFieldDMContext, java.lang.String, java.lang.String, org.eclipse.cdt.dsf.concurrent.RequestMonitor)
	 */
	public void writeBitField(IBitFieldDMContext bitFieldCtx, String bitFieldValue, String formatId, RequestMonitor rm) {
		rm.done();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IRegisters#writeBitField(org.eclipse.cdt.dsf.debug.service.IRegisters.IBitFieldDMContext, org.eclipse.cdt.dsf.debug.service.IRegisters.IMnemonic, org.eclipse.cdt.dsf.concurrent.RequestMonitor)
	 */
	public void writeBitField(IBitFieldDMContext bitFieldCtx, IMnemonic mnemonic, RequestMonitor rm) {
		rm.done();
	}

	/**
	 * Writes a value to a register.
	 *
	 * @param context the context
	 * @param regID register name.
	 * @param regValue big-endian hex string representation of the value to write.
	 * @throws CoreException the core exception
	 */
	public void writeRegister(IEDCExecutionDMC context, String regID, String regValue) throws CoreException {
		RegisterDMC regDMC;
		
		regDMC = findRegisterDMCByName(context, regID);
		assert regDMC != null;
		
		writeRegister(regDMC, regValue, IFormattedValues.HEX_FORMAT,
				new RequestMonitor(getExecutor(), null));
	}

	/**
	 * Writes a value to a register
	 * @throws CoreException 
	 * @since 2.0
	 */
	public void writeRegister(IRegisterDMContext regCtx, String regValue, String formatID) throws CoreException {
		assert (regCtx instanceof RegisterDMC);

		final RegisterDMC regDMC = (RegisterDMC) regCtx;
		IExecutionDMContext exeDMC = DMContexts.getAncestorOfType(regDMC, IExecutionDMContext.class);
		if (exeDMC == null || !(exeDMC instanceof IEDCDMContext)) {
			throw new CoreException(new Status(IStatus.ERROR, EDCDebugger.getUniqueIdentifier(), "No valid executionDMC for the register."));
		}

		final String exeDMCID = ((IEDCDMContext) exeDMC).getID();
		
		// Put the incoming value into hex
		if (formatID.equals(IFormattedValues.OCTAL_FORMAT) || formatID.equals(IFormattedValues.BINARY_FORMAT) ||
				formatID.equals(IFormattedValues.DECIMAL_FORMAT))
		{
			BigInteger bigRegValue = NumberFormatUtils.parseIntegerByFormat(regValue, formatID);
			regValue = bigRegValue.toString(16);
		}

		// Update cached register values
		Map<String, BigInteger> exeDMCRegisters = registerValueCache.get(exeDMCID);
		if (exeDMCRegisters != null) {
			exeDMCRegisters.put(regDMC.getID(), new BigInteger(regValue, 16));
		}

		if (tcfRegistersService != null) {	// TCF IRegisters service available
			final RegistersContext tcfReg = regDMC.getTCFContext();
			byte[] bv = null;
			try {
				bv = MemoryUtils.convertHexStringToByteArray(regValue, tcfReg.getSize(), 2);
			} catch (NumberFormatException e) {
				throw new CoreException(new Status(IStatus.ERROR, EDCDebugger.getUniqueIdentifier(), "Error writing register."));
			}
			
			final byte[] byteVal = bv;
			
			TCFTask<Object> tcfTask = new TCFTask<Object>() {
				public void run() {
					tcfReg.set(byteVal, new org.eclipse.tm.tcf.services.IRegisters.DoneSet() {
						public void doneSet(IToken token, Exception error) {
							if (error == null) {
								generateRegisterChangedEvent(regDMC);
								done(null);
							} else {
								done(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, INTERNAL_ERROR,
										"Error writing register.", error));
							}
						}
					});
				}
			};

			try {
				Object result = tcfTask.get(getTCFTimeout(), TimeUnit.MILLISECONDS);
				if (result != null && result instanceof IStatus)
					throw new CoreException((IStatus) result);
			} catch (Exception e) {
				throw new CoreException(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, INTERNAL_ERROR,
						"Error writing register.", e));
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IFormattedValues#getAvailableFormats(org.eclipse.cdt.dsf.debug.service.IFormattedValues.IFormattedDataDMContext, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	public void getAvailableFormats(IFormattedDataDMContext dmc, DataRequestMonitor<String[]> rm) {
        rm.setData(new String[] { HEX_FORMAT, DECIMAL_FORMAT, OCTAL_FORMAT, BINARY_FORMAT, NATURAL_FORMAT });
		rm.done();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IFormattedValues#getFormattedExpressionValue(org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
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
	 *
	 * @param context the context
	 * @param id the id
	 * @return a hex string on success, and {@link #REGISTER_VALUE_ERROR} on error.
	 * @throws CoreException the core exception
	 */
	public String getRegisterValue(IExecutionDMContext context, String id) throws CoreException {
		RegisterDMC regDMC;
		
		regDMC = findRegisterDMCByName((IEDCExecutionDMC) context, id);
		assert regDMC != null;
		
		return getRegisterValueAsHexString(regDMC);
	}

	/**
	 * Gets the register value as hex string.
	 *
	 * @param registerDMC the register dmc
	 * @return the register value as hex string
	 * @throws CoreException the core exception
	 * @since 2.0
	 */
	public String getRegisterValueAsHexString(RegisterDMC registerDMC) throws CoreException {
		return getRegisterValue(registerDMC).toString(16);
	}
	
	/**
	 * Gets the register value as a big integer.
	 *
	 * @param registerDMC the register dmc
	 * @return the register value
	 * @throws CoreException the core exception
	 * @since 2.0
	 */
	public BigInteger getRegisterValue(RegisterDMC registerDMC) throws CoreException {

		IExecutionDMContext exeDMC = DMContexts.getAncestorOfType(registerDMC, IExecutionDMContext.class);
		if (exeDMC == null || !(exeDMC instanceof IEDCDMContext)) {
			throw new CoreException(new Status(IStatus.ERROR, EDCDebugger.getUniqueIdentifier(), "No valid executionDMC for the register."));
		}

		final String exeDMCID = ((IEDCDMContext) exeDMC).getID();
		final String registerDMCID = registerDMC.getID();

		synchronized (registerValueCache) {
	
			Map<String, BigInteger> exeDMCRegisters = registerValueCache.get(exeDMCID);
			if (exeDMCRegisters != null) {
				BigInteger cachedValue = exeDMCRegisters.get(registerDMC.getID());
				if (cachedValue != null) {
					return cachedValue;
				}
			}
		}
	
		if (tcfRegistersService != null) {	// TCF IRegisters service available
			final RegistersContext tcfReg = registerDMC.getTCFContext();
			
            if (tcfReg == null) {
    			throw new CoreException(new Status(IStatus.ERROR, EDCDebugger.getUniqueIdentifier(), "RegisterDMC " + registerDMC.getID() + " has no underlying TCF register context."));
            }

            TCFTask<byte[]> tcfTask = new TCFTask<byte[]>() {
		
				public void run() {
					tcfReg.get(new org.eclipse.tm.tcf.services.IRegisters.DoneGet() {
		
						public void doneGet(IToken token, Exception error, byte[] value) {
							done(value);
						}
					});
				}
			};
			
			try {
				byte[] value = tcfTask.get(getTCFTimeout(), TimeUnit.MILLISECONDS);	// ignore the return
				String strVal = MemoryUtils.convertByteArrayToHexString(value);
				BigInteger biValue = new BigInteger(strVal, 16);
				synchronized (registerValueCache) {
					Map<String, BigInteger> exeDMCRegisters = registerValueCache.get(exeDMCID);
					if (exeDMCRegisters == null) {
						exeDMCRegisters = new HashMap<String, BigInteger>();
						registerValueCache.put(exeDMCID, exeDMCRegisters);
					}
					exeDMCRegisters.put(registerDMCID, biValue);
				}
				return biValue;
			} catch (Throwable e) {
    			throw new CoreException(EDCDebugger.dsfRequestFailedStatus("Exception reading register " + registerDMC.getName(), e));
			}
		}
		throw new CoreException(new Status(IStatus.ERROR, EDCDebugger.getUniqueIdentifier(), "No data for register " + registerDMC.getName()));
	}

	/**
	 * Generate a register changed event.
	 *
	 * @param dmc the register dmc
	 */
	private void generateRegisterChangedEvent(IRegisterDMContext dmc) {
		getSession().dispatchEvent(new RegisterChangedDMEvent(dmc), getProperties());
	}

	/**
	 * Gets the register data value.
	 *
	 * @param registerDMC the register dmc
	 * @param formatID the format id
	 * @param rm the request monitor
	 * @return the register data value
	 */
	private void getRegisterDataValue(RegisterDMC registerDMC, final String formatID,
			final DataRequestMonitor<FormattedValueDMData> rm) {
		try {
			BigInteger bigIntValue = getRegisterValue(registerDMC);

			String formattedValue = bigIntValue.toString(16);

			if (formatID.equals(IFormattedValues.OCTAL_FORMAT))
				formattedValue = NumberFormatUtils.toOctalString(bigIntValue);
			if (formatID.equals(IFormattedValues.BINARY_FORMAT))
				formattedValue = NumberFormatUtils.asBinary(bigIntValue);
			if (formatID.equals(IFormattedValues.DECIMAL_FORMAT))
				formattedValue = bigIntValue.toString();
			
			rm.setData(new FormattedValueDMData(formattedValue));

		} catch (CoreException e) {
			Status s = new Status(IStatus.ERROR, EDCDebugger.getUniqueIdentifier(), "Error in getRegisterDataValue.", e);
			EDCDebugger.getMessageLogger().log(s);
			rm.setStatus(s);
		}
		rm.done();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IFormattedValues#getFormattedValueContext(org.eclipse.cdt.dsf.debug.service.IFormattedValues.IFormattedDataDMContext, java.lang.String)
	 */
	public FormattedValueDMContext getFormattedValueContext(IFormattedDataDMContext dmc, String formatId) {
		if (dmc instanceof RegisterDMC) {
			return new FormattedValueDMContext(Registers.this, dmc, formatId);
		}
		return null;
	}

	/**
	 * Gets the model data for a register.
	 *
	 * @param dmc the dmc
	 * @param rm the request monitor
	 * @return the model data
	 */
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.ICachingService#flushCache(org.eclipse.cdt.dsf.datamodel.IDMContext)
	 */
	public void flushCache(IDMContext context) {
		if (isSnapshot())
			return;
		// Why flush this static info ?
		// registerGroupsPerThread.clear();
		
		registerValueCache.clear();
	}

	/**
	 * Load register groups for an executable context.
	 *
	 * @param executionDmc the execution dmc
	 * @param element the element
	 * @throws Exception the exception
	 */
	public void loadGroupsForContext(IEDCExecutionDMC executionDmc, Element element) throws Exception {
		// Can't call flushCache here because it does nothing for snapshot
		// services.
		String cxtID = ((IEDCDMContext)executionDmc).getID();
		// It does not hurt if the context is not in the caches.
		registerGroupsPerContext.remove(cxtID);
		registerValueCache.remove(cxtID);

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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.services.IDSFServiceUsingTCF#tcfServiceReady(org.eclipse.tm.tcf.protocol.IService)
	 */
	public void tcfServiceReady(IService service) {
		tcfRegistersService = (org.eclipse.tm.tcf.services.IRegisters)service;
	}

	/**
	 * Gets the register value.
	 *
	 * @param executionDMC the execution dmc
	 * @param id the register id
	 * @return the register value
	 * @throws CoreException the core exception
	 */
	public String getRegisterValue(IEDCExecutionDMC executionDMC, int id) throws CoreException {
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
	 * @return the tCF registers contexts
	 * @throws CoreException the core exception
	 */
	protected List<RegistersContext>	getTCFRegistersContexts(final String parentID) throws CoreException {
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
			childIDs = getChildIDTask.get(getTCFTimeout(), TimeUnit.MILLISECONDS);
		} catch (Throwable e) {
			throw new CoreException(EDCDebugger.dsfRequestFailedStatus("Fail to get TCF context for: " + parentID, e));
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
				rgc = getGroupContextTask.get(getTCFTimeout(), TimeUnit.MILLISECONDS);
			} catch (Throwable e) {
				throw new CoreException(EDCDebugger.dsfRequestFailedStatus("Fail to get TCF context for: " + parentID, e));
			}
			
			if (rgc != null)
				tcfRegContexts.add(rgc);
		}

		return tcfRegContexts;
	}
	
	/**
	 * Handle a suspended event by flushing the cache.
	 *
	 * @param e the event
	 */
	@DsfServiceEventHandler
	public void eventDispatched(ISuspendedDMEvent e) {
		flushCache(null);
	}

	/**
	 * Handle a resumed event by flushing the cache.
	 *
	 * @param e the event
	 */
	@DsfServiceEventHandler
	public void eventDispatched(IResumedDMEvent e) {
		flushCache(null);
	}

	/**
	 * When a context (e.g. a thread) is killed/detached, we should forget
	 * cached register info & values for it so that we can properly access
	 * registers when we re-attach to it.
	 *
	 * @param e the event
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

	/**
	 * Creates the registers for group.
	 *
	 * @param registerGroupDMC the register group dmc
	 * @return the list
	 * @throws CoreException the core exception
	 */
	protected List<RegisterDMC> createRegistersForGroup(RegisterGroupDMC registerGroupDMC) throws CoreException {
		ArrayList<RegisterDMC> registers = new ArrayList<RegisterDMC>();
	
		if (tcfRegistersService != null) {
			List<RegistersContext> tcfRegs = getTCFRegistersContexts(registerGroupDMC.getID());
			
			for (RegistersContext rg: tcfRegs) {
				registers.add(new RegisterDMC(registerGroupDMC, registerGroupDMC.getExecutionDMC(), rg));
			}
		}
		
		return registers;
	}
	
	/**
	 * Creates the groups for context.
	 *
	 * @param ctx the ctx
	 * @return the list
	 * @throws CoreException the core exception
	 */
	protected List<RegisterGroupDMC> createGroupsForContext(IEDCExecutionDMC ctx) throws CoreException {

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
	
	/**
	 * Sets the TCF timeout.
	 *
	 * @param msecs the new TCF timeout
	 * @since 2.0
	 */
	public void setTCFTimeout(long msecs) {
		tcfTimeout = msecs;
	}

	/**
	 * Gets the TCF timeout.
	 *
	 * @return the TCF timeout
	 * @since 2.0
	 */
	public long getTCFTimeout() {
		return tcfTimeout;
	}

	// Implementation of org.eclipse.cdt.dsf.debug.service.IRegisters
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IRegisters#getRegisterGroups(org.eclipse.cdt.dsf.datamodel.IDMContext, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	public void getRegisterGroups(final IDMContext ctx, final DataRequestMonitor<IRegisterGroupDMContext[]> rm) {
		
		asyncExec(new Runnable() {
			
			public void run() {
				IEDCExecutionDMC execDmc = DMContexts.getAncestorOfType(ctx, IEDCExecutionDMC.class);
				if (execDmc != null && RunControl.isNonContainer(execDmc)) {
					try {
						rm.setData(getGroupsForContext(execDmc));
					} catch (CoreException e) {
						EDCDebugger.getMessageLogger().log(e.getStatus());
						rm.setStatus(e.getStatus());
					}
					rm.done();
					return;
				}

				StackFrameDMC frameDmc = DMContexts.getAncestorOfType(ctx, StackFrameDMC.class);
				if (frameDmc != null) {
					try {
						rm.setData(getGroupsForContext(frameDmc.getExecutionDMC()));
					} catch (CoreException e) {
						EDCDebugger.getMessageLogger().log(e.getStatus());
						rm.setStatus(e.getStatus());
					}
					rm.done();
					return;
				}
				
				rm.setData(new IRegisterGroupDMContext[0]);
				rm.done();
			}
		}, rm);
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IRegisters#getRegisters(org.eclipse.cdt.dsf.datamodel.IDMContext, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	public void getRegisters(final IDMContext ctx, final DataRequestMonitor<IRegisterDMContext[]> rm) {

		asyncExec(new Runnable() {
			
			public void run() {
				RegisterGroupDMC groupContext = DMContexts.getAncestorOfType(ctx, RegisterGroupDMC.class);
				IEDCExecutionDMC executionContext = DMContexts.getAncestorOfType(ctx, IEDCExecutionDMC.class);
				RegisterDMC[] allRegisters;
				try {
					if (groupContext != null && executionContext != null) {
						allRegisters = groupContext.getRegisters();
					}
					else {
						allRegisters = new RegisterDMC[0];
					}
					rm.setData(allRegisters);
				} catch (CoreException e) {
					EDCDebugger.getMessageLogger().log(e.getStatus());
					rm.setStatus(e.getStatus());
				}
				rm.done();
			}
		}, rm);
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IRegisters#getBitFields(org.eclipse.cdt.dsf.datamodel.IDMContext, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	public void getBitFields(IDMContext ctx, DataRequestMonitor<IBitFieldDMContext[]> rm) {
		rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, NOT_SUPPORTED, "BitField not supported", null)); //$NON-NLS-1$
		rm.done();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IRegisters#findRegisterGroup(org.eclipse.cdt.dsf.datamodel.IDMContext, java.lang.String, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	public void findRegisterGroup(IDMContext ctx, String name, DataRequestMonitor<IRegisterGroupDMContext> rm) {
		rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, NOT_SUPPORTED, "findRegisterGroup not supported", null)); //$NON-NLS-1$
		rm.done();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IRegisters#findRegister(org.eclipse.cdt.dsf.datamodel.IDMContext, java.lang.String, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	public void findRegister(IDMContext ctx, String name, DataRequestMonitor<IRegisterDMContext> rm) {
		rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, NOT_SUPPORTED, "findRegister not supported", null)); //$NON-NLS-1$
		rm.done();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IRegisters#findBitField(org.eclipse.cdt.dsf.datamodel.IDMContext, java.lang.String, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	public void findBitField(IDMContext ctx, String name, DataRequestMonitor<IBitFieldDMContext> rm) {
		rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, NOT_SUPPORTED, "findBitField not supported", null)); //$NON-NLS-1$
		rm.done();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IRegisters#getRegisterGroupData(org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterGroupDMContext, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IRegisters#getRegisterData(org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMContext, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	public void getRegisterData(IRegisterDMContext dmc, DataRequestMonitor<IRegisterDMData> rm) {
		RegisterDMC regdmc = (RegisterDMC) dmc;
		rm.setData(new RegisterData(regdmc.getProperties()));
		rm.done();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IRegisters#getBitFieldData(org.eclipse.cdt.dsf.debug.service.IRegisters.IBitFieldDMContext, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	public void getBitFieldData(IBitFieldDMContext dmc, DataRequestMonitor<IBitFieldDMData> rm) {
		rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, NOT_SUPPORTED,
				"Bit fields not yet supported", null)); //$NON-NLS-1$
		rm.done();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IRegisters#writeRegister(org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMContext, java.lang.String, java.lang.String, org.eclipse.cdt.dsf.concurrent.RequestMonitor)
	 */
	public void writeRegister(final IRegisterDMContext regCtx, final String regValue, final String formatID, final RequestMonitor rm) {

		asyncExec(new Runnable() {
			
			public void run() {
				try{
				writeRegister(regCtx, regValue, formatID);
				} catch (CoreException e) {
					EDCDebugger.getMessageLogger().log(e.getStatus());
					rm.setStatus(e.getStatus());
				}
				rm.done();
			}
		}, rm);
		
	}

}
