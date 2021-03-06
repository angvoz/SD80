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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.EDCTrace;
import org.eclipse.cdt.debug.edc.internal.launch.CSourceLookup;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl.ExecutionDMC;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Symbols;
import org.eclipse.cdt.debug.edc.internal.snapshot.SnapshotUtils;
import org.eclipse.cdt.debug.edc.internal.symbols.MemoryVariableLocation;
import org.eclipse.cdt.debug.edc.internal.symbols.dwarf.EDCSymbolReader;
import org.eclipse.cdt.debug.edc.internal.symbols.files.UnmanglerEABI;
import org.eclipse.cdt.debug.edc.internal.symbols.files.UnmanglingException;
import org.eclipse.cdt.debug.edc.snapshot.IAlbum;
import org.eclipse.cdt.debug.edc.snapshot.ISnapshotContributor;
import org.eclipse.cdt.debug.edc.symbols.ICompileUnitScope;
import org.eclipse.cdt.debug.edc.symbols.IDebugInfoProvider;
import org.eclipse.cdt.debug.edc.symbols.IEDCSymbolReader;
import org.eclipse.cdt.debug.edc.symbols.IEnumerator;
import org.eclipse.cdt.debug.edc.symbols.IFunctionScope;
import org.eclipse.cdt.debug.edc.symbols.ILineEntry;
import org.eclipse.cdt.debug.edc.symbols.IModuleLineEntryProvider;
import org.eclipse.cdt.debug.edc.symbols.IModuleScope;
import org.eclipse.cdt.debug.edc.symbols.IScope;
import org.eclipse.cdt.debug.edc.symbols.IUnmangler;
import org.eclipse.cdt.debug.edc.symbols.IVariable;
import org.eclipse.cdt.debug.edc.symbols.TypeEngine;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLookupDirector;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.ICachingService;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.service.IDsfService;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class Stack extends AbstractEDCService implements IStack, ICachingService {

	public static final String STACK_FRAME = "stack_frame";
	
	public Boolean showAllVariablesEnabled = null;

	private final Map<String, List<StackFrameDMC>> stackFrames = Collections
			.synchronizedMap(new HashMap<String, List<StackFrameDMC>>());
	private final Map<String, Boolean> allFramesCached = Collections
	.synchronizedMap(new HashMap<String, Boolean>());

	
	
	public static class StackFrameData implements IFrameDMData {

		public final IAddress address;
		public final int level;
		public final String function;
		public final String module;
		private final String file;
		private final int lineNumber;

		StackFrameData(StackFrameDMC dmc) {
			level = dmc.getLevel();
			address = dmc.getInstructionPtrAddress();
			module = dmc.getModuleName();
			file = dmc.getSourceFile(); // "" instead of null if no file.
			lineNumber = dmc.getLineNumber();
			function = dmc.getFunctionName();
		}

		public IAddress getAddress() {
			return address;
		}

		public String getFunction() {
			return function;
		}

		public int getLevel() {
			return level;
		}

		// DSF requires non-null return value.
		public String getFile() {
			return file;
		}

		public int getLine() {
			return lineNumber;
		}

		public int getColumn() {
			return 0;
		}

		public String getModule() {
			return module;
		}

		@Override
		public boolean equals(Object other) {
			return
				this == other
				|| (other != null && other instanceof StackFrameData
					&& getAddress().equals(((StackFrameData)other).getAddress())
					&& getFunction().equals(((StackFrameData)other).getFunction())
					&& getLevel() == ((StackFrameData)other).getLevel()
					&& getFile().equals(((StackFrameData)other).getFile())
					&& getLine() == ((StackFrameData)other).getLine()
					&& getColumn() == ((StackFrameData)other).getColumn()
					&& getModule().equals(((StackFrameData)other).getModule()));
		}
	}

    /**
     * Variable or enumerator context.  This interface provides a wrapper
     * for treating variables and enumerators the same when needed.
     **/
    public interface IVariableEnumeratorContext {}

    /**
     * Enumerator context.
     **/
    public interface IEnumeratorDMContext {}

    public static final class CurrentFrameRegisters implements IFrameRegisters {
    	private final Registers registers;
		private final IEDCExecutionDMC executionDMC;
    	
    	public CurrentFrameRegisters(IEDCExecutionDMC executionDMC, Registers registers) {
    		this.executionDMC = executionDMC;
			this.registers = registers;
    	}
    	
    	public BigInteger getRegister(int regnum, int bytes) throws CoreException {
    		String value = registers.getRegisterValue(executionDMC, regnum);
    		if (value == null || value.equals(Registers.REGISTER_VALUE_ERROR))
    			throw EDCDebugger.newCoreException("failed to read register");
    		return new BigInteger(value, 16);
    	}

		public void writeRegister(int regnum, int bytes, BigInteger value) throws CoreException {
			String id = registers.getRegisterNameFromCommonID(regnum);
			if (id != null) {
				// if value is negative, using value.toString(16) directly gives values such as '-af'
				registers.writeRegister(executionDMC, id, Long.toHexString(value.longValue()));
			} else
				throw EDCDebugger.newCoreException(MessageFormat.format("could not find register number {0}", regnum));
		}
    }
    
    /**
	 * Frame registers read from preserved registers on the stack frame.
	 */
	public static class PreservedFrameRegisters implements IFrameRegisters {
		private final Map<Integer, BigInteger> preservedRegisters;
		private final EDCServicesTracker dsfServicesTracker;
		private final StackFrameDMC context;

		/**
		 * @param preservedRegisters map of register number to the address
		 * where the register is saved
		 * @since 2.0
		 */
		public PreservedFrameRegisters(EDCServicesTracker dsfServicesTracker,
				StackFrameDMC context,
				Map<Integer, BigInteger> preservedRegisters) {
			this.dsfServicesTracker = dsfServicesTracker;
			this.context = context;
			this.preservedRegisters = preservedRegisters;
		}

		public BigInteger getRegister(int regnum, int bytes) throws CoreException {
			BigInteger addrVal = preservedRegisters.get(regnum);
			if (addrVal != null) {
				MemoryVariableLocation location = new MemoryVariableLocation(
						dsfServicesTracker, context, 
						addrVal, true);
				return location.readValue(bytes);
			}
			throw EDCDebugger.newCoreException("cannot read $R" + regnum + " from frame");
		}

		public void writeRegister(int regnum, int bytes, BigInteger value) throws CoreException {
			BigInteger addrVal = preservedRegisters.get(regnum);
			if (addrVal != null) {
				MemoryVariableLocation location = new MemoryVariableLocation(
						dsfServicesTracker, context, 
						addrVal, true);
				location.writeValue(bytes, value);
			}
		}
	}

	/**
	 * Frame registers which always throws an exception.
	 */
	public static class AlwaysFailingFrameRegisters implements IFrameRegisters {
		private final CoreException e;

		public AlwaysFailingFrameRegisters(CoreException e) {
			this.e = e;
		}

		public BigInteger getRegister(int regnum, int bytes) throws CoreException {
			throw e;
		}

		public void writeRegister(int regnum, int bytes, BigInteger value)
				throws CoreException {
			throw e;
		}
	}

	public class StackFrameDMC extends DMContext implements IFrameDMContext, Comparable<StackFrameDMC>,
			ISnapshotContributor {

		/** 
		 * Stack frame level.  Zero is used for the first frame, where the PC is.
		 */
		public static final String LEVEL_INDEX = "Level";
		/**
		 * If set and True, tells that this frame is the topmost that we can fetch.
		 */
		public static final String ROOT_FRAME = "root_frame";
		public static final String BASE_ADDR = "Base_address";
		/**
		 * @since 2.0 - previously "IP_ADDR"
		 */
		public static final String INSTRUCTION_PTR_ADDR = "Instruction_address";
		public static final String MODULE_NAME = "module_name";
		public static final String SOURCE_FILE = "source_file";
		public static final String FUNCTION_NAME = "function_name";
		public static final String LINE_NUMBER = "line_number";
		/** 
		 * For LEVEL_INDEX == 0, if set and True, this tells us that this frame
		 * is not "authentic" yet, e.g., that the frame still represents the caller's
		 * state.  This means we cannot trust the parameters and locals,
		 * and must resolve variables from other frames differently.
		 */
		public static final String IN_PROLOGUE = "in_prologue"; // Boolean
		/**
		 * Provides a Map<Integer, BigInteger> instance which can yield addresses of
		 * registers pushed into the stack frame if debug info does not provide it.
		 */
		public static final String PRESERVED_REGISTERS = "preserved_registers";
		private static final String FRAME_PROPERTY_CACHE = "_frame_properties";
		/**
		 * @since 2.0 The id of the owning execution dmc
		 */
		public static final String EXECUTION_DMC_ID = "execution_dmc_id";

		private final EDCServicesTracker dsfServicesTracker = Stack.this.getEDCServicesTracker();
		private final IEDCExecutionDMC executionDMC;
		private final int level;
		private IAddress baseAddress;
		private IAddress instructionPtrAddress;

		private String moduleName = "";
		private String sourceFile = "";
		private String functionName = "";
		private int lineNumber;
		private IScope variableScope = null;
		private List<VariableDMC> locals;
		private List<EnumeratorDMC> enumerators;
		private final Map<String, VariableDMC> localsByName = Collections
				.synchronizedMap(new HashMap<String, VariableDMC>());
		private final Map<String, EnumeratorDMC> enumeratorsByName = Collections
				.synchronizedMap(new HashMap<String, EnumeratorDMC>());
		private final Map<String, IVariable> thisPtrs = Collections
				.synchronizedMap(new LinkedHashMap<String, IVariable>());
		private IFunctionScope functionScope;
		private IFrameRegisters frameRegisters;
		public StackFrameDMC calledFrame;
		private TypeEngine typeEngine;
		private IEDCModuleDMContext module;

		// additional items may be null but are usually set early and used repeatedly
		private IAddress instrPtrLinkAddr = null;
		private IEDCSymbolReader reader = null;
		private IModuleLineEntryProvider provider = null;
		private IDebugInfoProvider debugInfoProvider = null;
		private IPath symbolFile = null;

		/**
		 * @since 2.0
		 */
		@SuppressWarnings("unchecked")
		public StackFrameDMC(final IEDCExecutionDMC executionDMC, EdcStackFrame edcFrame) {
			super(Stack.this, new IDMContext[] { executionDMC }, createFrameID(executionDMC, edcFrame), edcFrame.props);
			
			Map<String, Object> frameProperties = edcFrame.props;
			
			this.executionDMC = executionDMC;
			frameProperties.put(EXECUTION_DMC_ID, executionDMC.getID());

			this.level = (Integer) frameProperties.get(LEVEL_INDEX);
			this.moduleName = (String) frameProperties.get(MODULE_NAME);
			this.baseAddress = address(frameProperties.get(BASE_ADDR));
			this.instructionPtrAddress = address(frameProperties.get(INSTRUCTION_PTR_ADDR));

			// compute the source location
			IEDCSymbols symbolsService = getService(Symbols.class);
			functionScope = symbolsService.getFunctionAtAddress(executionDMC.getSymbolDMContext(),
																instructionPtrAddress);

			boolean usingCachedProperties = false;
			IEDCModules modules = dsfServicesTracker.getService(IEDCModules.class);
			Map<IAddress, Map<String, Object>> cachedFrameProperties
			  = new HashMap<IAddress, Map<String, Object>>();
			if (modules != null) {
				module = modules.getModuleByAddress(executionDMC.getSymbolDMContext(), instructionPtrAddress);
				if (module != null) {
					instrPtrLinkAddr = module.toLinkAddress(instructionPtrAddress);
					reader = module.getSymbolReader();
					if (reader != null) {
						symbolFile = this.reader.getSymbolFile();
						if (symbolFile != null) {
							// Check the persistent cache
							String cacheKey = reader.getSymbolFile().toOSString() + FRAME_PROPERTY_CACHE;
							Map<IAddress, Map<String, Object>> cachedData
							  = EDCDebugger.getDefault().getCache().getCachedData(cacheKey, Map.class,
									  											  reader.getModificationDate());
							if (cachedData != null) {
								cachedFrameProperties = cachedData;
								Map<String, Object> cachedProperties
								  = cachedFrameProperties.get(instrPtrLinkAddr);
								if (cachedProperties != null) {
									if (cachedProperties.containsKey(SOURCE_FILE))
										frameProperties.put(SOURCE_FILE, cachedProperties.get(SOURCE_FILE));

									boolean cachedPropertiesHasFunctionName = false;
 									if (cachedProperties.containsKey(FUNCTION_NAME)) {
										Object fnObj = cachedProperties.get(FUNCTION_NAME);
										if (fnObj != null 
											&& fnObj instanceof String
											&& ((String)fnObj).length() != 0) {
											frameProperties.put(FUNCTION_NAME, fnObj);
											cachedPropertiesHasFunctionName = true;
									}	}

									if (!cachedPropertiesHasFunctionName) {
										setFunctionName(executionDMC, frameProperties, symbolsService);
										cachedProperties.put(FUNCTION_NAME, functionName);
									}

									if (cachedProperties.containsKey(LINE_NUMBER))
										frameProperties.put(LINE_NUMBER, cachedProperties.get(LINE_NUMBER));
									usingCachedProperties = true;								
			}	}	}	}	}	}	// null-checks on cachedProperties <= cachedData <= symbolFile

			if (frameProperties.containsKey(SOURCE_FILE)) {
				sourceFile   = (String) frameProperties.get(SOURCE_FILE);
				functionName = (String) frameProperties.get(FUNCTION_NAME);
				lineNumber   = (Integer) frameProperties.get(LINE_NUMBER);
			} else if (frameProperties.containsKey(FUNCTION_NAME)) {
				functionName = (String) frameProperties.get(FUNCTION_NAME);
			} else if (!usingCachedProperties) {
				ILineEntry line
				  = symbolsService.getLineEntryForAddress(executionDMC.getSymbolDMContext(),
														  instructionPtrAddress);
				if (line != null)
					setSourceProperties(frameProperties, line);

				setFunctionName(executionDMC, frameProperties, symbolsService);
			}
			properties.putAll(frameProperties);

			if (symbolFile != null) {
				String cacheKey = symbolFile.toOSString() + FRAME_PROPERTY_CACHE;
				cachedFrameProperties.put(this.instrPtrLinkAddr, frameProperties);
				EDCDebugger.getDefault().getCache().putCachedData(cacheKey,
																  (Serializable)cachedFrameProperties,
																  this.reader.getModificationDate());
			}

			if (reader instanceof EDCSymbolReader)
				debugInfoProvider = ((EDCSymbolReader)reader).getDebugInfoProvider();
			typeEngine = new TypeEngine(getTargetEnvironmentService(), debugInfoProvider);
		}

		private void setFunctionName(final IEDCExecutionDMC executionDMC,
				Map<String, Object> frameProperties, IEDCSymbols symbolsService) {
			if (functionScope != null) {
				// ignore inlined functions
				IFunctionScope containerScope = functionScope;
				while (containerScope.getParent() instanceof IFunctionScope) {
					containerScope = (IFunctionScope) containerScope.getParent();
				}
				functionName = unmangle(containerScope.getName());
				adjustFunctionSourceInfo(containerScope, frameProperties);
			} else {
				functionName
				  = unmangle(symbolsService.getSymbolNameAtAddress(executionDMC.getSymbolDMContext(),
						  										   instructionPtrAddress));
			}

			frameProperties.put(FUNCTION_NAME, functionName);
		}

		/**
		 * Modify the name to refer to the inline function within the parent function.
		 * <p>
		 * However, ignore the inline function name if the pointer is on the first
		 * line of the inline function and the "previous" line is
		 * <br> (a) in the parent function; or
		 * <br> (b) not in the original inline (meaning it was part of a prior inline); or 
		 * <br> (c) is nested in another inline
		 * @param container the ultimate function containing the inline(s)
		 * @param frameProperties so source-file and line-number can also be adjusted
		 */
		private void adjustFunctionSourceInfo(IFunctionScope container,
				Map<String, Object> frameProperties) {
			if (functionScope.equals(container)) {
				ILineEntry funcFirstEntry = this.getLineEntryInFunction(functionScope);
				if (funcFirstEntry != null
						&& !instrPtrLinkAddr.equals(funcFirstEntry.getLowAddress())) {
					// this case covers the compiler having inline LNT entries
					// whose bounds are outside the DWARF function scope boundaries
					// for the inlines
					setSourceProperties(frameProperties, funcFirstEntry);
				}
				return;		// i.e. never fall through to "inline" re-naming below
			}

			ILineEntry containerEntry = this.getLineEntryInFunction(container);
			if (containerEntry != null && isInlineShouldBeHidden(containerEntry)) {
				setSourceProperties(frameProperties, containerEntry);
				return;
			}

			this.functionName
			  = unmangle(functionScope.getName()) + " inlined in " + this.functionName;
		}
		
		/**
		 * Attempt to determine if the frame's instruction pointer is
		 * <br>(a) at the first instruction of an inlined function; and
		 * <br>(b) coincidentally at the first instruction of the line
		 * entry corresponding to the line that caused the inline to
		 * be generated.<p>
		 * @param entry if null, will be calculated based on established
		 * 			frame instruction pointer and function scope; can be passed
		 * 			in if caller needs line entry for other usage
		 * @return true if it can be determined that the instruction pointer is
		 * 			the first instruction of an inline function and coincidentally the
		 * 			first instruction of the line entry for which the inline was generated
		 * @since 2.0
		 */
		public boolean isInlineShouldBeHidden(ILineEntry entry) {
			if (functionScope == null
					|| !(functionScope.getParent() instanceof IFunctionScope)
					|| !instrPtrLinkAddr.equals(functionScope.getLowAddress()))
				return false;

			if (entry == null) {
				entry = getLineEntryInFunction(functionScope);
				if (entry == null)
					return false;
			}

			if (instrPtrLinkAddr.equals(entry.getLowAddress())) {
				ILineEntry prevEntry = getPreviousLineEntry(entry, true);
				if (prevEntry != null) {
					ILineEntry testEntry = getNextLineEntry(prevEntry, true);
					if (entry.equals(testEntry)) {
						return true;
					}
					return false;
				}
				return true;
			}
			return false;
		}

		/**
		 * Private utility function to call the module's reader's provider's interfaces
		 * @see org.eclipse.cdt.debug.edc.symbols.ILineEntryProvider#getLineEntryInFunction
		 * @see IModuleScope#getModuleLineEntryProvider
		 */
		private ILineEntry getLineEntryInFunction(IFunctionScope func) {
			return getModuleLineEntryProvider().getLineEntryInFunction(instrPtrLinkAddr, func);
		}

		/**
		 * Private utility function to call the module's reader's provider's interfaces
		 * @see IModuleScope#getModuleLineEntryProvider
		 * @return {@link IModuleLineEntryProvider} never <code>null</code>
		 */
		private IModuleLineEntryProvider getModuleLineEntryProvider() {
			if (provider == null && reader != null) {
				IModuleScope moduleScope = reader.getModuleScope();
				if (moduleScope != null)
					provider = moduleScope.getModuleLineEntryProvider();			
			}
			return provider;
		}

		/**
		 * Private utility function to call the module's reader's provider's interfaces
		 * @see org.eclipse.cdt.debug.edc.symbols.ILineEntryProvider#getNextLineEntry
		 * @see IModuleScope#getModuleLineEntryProvider
		 */
		private ILineEntry getNextLineEntry(ILineEntry entry, boolean collapseInlineFunctions) {
			return getModuleLineEntryProvider().getNextLineEntry(entry, collapseInlineFunctions);
		}					

		/**
		 * Private utility function to call the module's reader's provider's interfaces
		 * @see org.eclipse.cdt.debug.edc.symbols.ILineEntryProvider#getPreviousLineEntry
		 * @see IModuleScope#getModuleLineEntryProvider
		 */
		private ILineEntry getPreviousLineEntry(ILineEntry entry, boolean collapseInlineFunctions) {
			return getModuleLineEntryProvider().getPreviousLineEntry(entry, collapseInlineFunctions);
		}					

		private void setSourceProperties(Map<String, Object> frameProperties,
				ILineEntry entry) {
			frameProperties.put(SOURCE_FILE, (sourceFile = entry.getFilePath().toOSString()));
			frameProperties.put(LINE_NUMBER, (lineNumber = entry.getLineNumber()));
		}

		private String unmangle(String name) {
			if (name == null)
				return null;
			
			// unmangle the name
			IUnmangler unmangler = null;
			if (reader instanceof EDCSymbolReader) {
				unmangler = ((EDCSymbolReader) reader).getUnmangler();
			}
			if (unmangler == null) {
				unmangler = new UnmanglerEABI();
			}
			
			if (!unmangler.isMangled(name))
				return name;
			
			try {
				return unmangler.unmangleWithoutArgs(name);
			} catch (UnmanglingException e) {
				return name;
			}
		}

		private IAddress address(Object obj) {
			if (obj instanceof Integer)
				return new Addr64(obj.toString());
			if (obj instanceof Long)
				return new Addr64(obj.toString());
			if (obj instanceof String) // the string should be hex string
				return new Addr64((String) obj, 16);
			return null;
		}

		private void setInstructionPtrAddress(IAddress ipAddrPtr) {
			this.instructionPtrAddress = ipAddrPtr;
			if (module != null)
				this.instrPtrLinkAddr = module.toLinkAddress(instructionPtrAddress);
		}

		public IFunctionScope getFunctionScope() {
			return functionScope;
		}

		public String getModuleName() {
			return moduleName;
		}

		/**
		 * Get source file name if any for the frame. 
		 * @return valid file name or "" otherwise.
		 */
		public String getSourceFile() {
			return sourceFile;
		}

		public String getFunctionName() {
			return functionName;
		}

		public int getLineNumber() {
			return lineNumber;
		}

		public IEDCExecutionDMC getExecutionDMC() {
			return executionDMC;
		}

		public IAddress getBaseAddress() {
			return baseAddress;
		}

		/**
		 * @since 2.0
		 */
		public IAddress getInstructionPtrAddress() {
			return instructionPtrAddress;
		}

		public int getLevel() {
			return level;
		}

		/**
		 * @since 2.0
		 */
		public EDCServicesTracker getEDCServicesTracker() {
			return Stack.this.getEDCServicesTracker();
		}

		public int compareTo(StackFrameDMC f) {
			if (level < f.level)
				return -1;
			if (level > f.level)
				return +1;
			return 0;
		}

		
		@Override
		public String toString() {
			return "StackFrameDMC [baseAddress=" + baseAddress.toHexAddressString() + ", ipAddress="
					+ instructionPtrAddress.toHexAddressString() + ", sourceFile=" + sourceFile
					+ ", functionName=" + functionName + ", lineNumber="
					+ lineNumber + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((baseAddress == null) ? 0 : baseAddress.hashCode());
			result = prime * result
					+ ((executionDMC == null) ? 0 : executionDMC.hashCode());
			result = prime * result + level;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;

			StackFrameDMC other = (StackFrameDMC) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;

			if (baseAddress == null) {
				if (other.baseAddress != null)
					return false;
			} else if (!baseAddress.equals(other.baseAddress))
				return false;

			if (executionDMC == null) {
				if (other.executionDMC != null)
					return false;
			} else if (!executionDMC.equals(other.executionDMC))
				return false;

			if (level != other.level)
				return false;
			return true;
		}

		/**
		 * Finds a source file using the source lookup director.
		 * 
		 * @param sourceFile the raw source file location, usually from the symbol data
		 * 
		 * @return location of the source file
		 */
		private String findSourceFile(String sourceFile) {
			String result = "";
			CSourceLookup lookup = getService(CSourceLookup.class);
			RunControl runControl = getService(RunControl.class);
			CSourceLookupDirector[] directors = lookup.getSourceLookupDirectors(runControl.getRootDMC());

			for (CSourceLookupDirector cSourceLookupDirector : directors) {
				try {
					Object[] elements = cSourceLookupDirector.findSourceElements(sourceFile);
					if (elements != null && elements.length > 0)
					{
						Object element = elements[0];
						if (element instanceof File) {
							try {
								result = (((File) element).getCanonicalPath());
							} catch (IOException e) {
								EDCDebugger.getMessageLogger().logError(null, e);
							}
						} else if (element instanceof IFile) {
							result = (((IFile) element).getLocation().toOSString());
						} else if (element instanceof IStorage) {
							result = (((IStorage) element).getFullPath().toOSString());
						} else if (element instanceof ITranslationUnit) {
							result =(((ITranslationUnit) element).getLocation().toOSString());
						}
						break;
					}			
				} catch (CoreException e1) {
					EDCDebugger.getMessageLogger().logError(sourceFile, e1);
				}
			}
			return result;
		}

		/**
		 * @since 2.0
		 */
		public Element takeSnapshot(IAlbum album, Document document, IProgressMonitor monitor) {
			Element contextElement = document.createElement(STACK_FRAME);
			contextElement.setAttribute(PROP_ID, this.getID());

			Element propsElement = SnapshotUtils.makeXMLFromProperties(document, getProperties());
			contextElement.appendChild(propsElement);
			// Locate the actual source file to be included in the album.
			if (sourceFile.length() > 0) // No source file for this frame (just module/address)
				album.addFile(new Path(findSourceFile(sourceFile)));
			return contextElement;
		}

		@SuppressWarnings("unchecked")
		public void loadSnapshot(Element element) {
			// fix up registers to use integers again
			Map<String, String> preservedRegisters = (Map<String, String>) properties.get(PRESERVED_REGISTERS);
			if (preservedRegisters != null) {
				Map<Integer, BigInteger> newPreservedRegisters = new HashMap<Integer, BigInteger>();
				for (Map.Entry<String, String> entry : preservedRegisters.entrySet()) {
					newPreservedRegisters.put(Integer.valueOf(entry.getKey().toString()), new BigInteger(entry.getValue().toString()));
				}
				properties.put(PRESERVED_REGISTERS, newPreservedRegisters);
			}
		}

		public IVariableDMContext[] getLocals() {
			return getLocals(/* boolean useCachedVariables => */ true);
		}

		private IVariableDMContext[] getLocals(boolean useCachedVariables) {
			// may need to refresh the locals list because "Show All Variables"
			// toggle has changed
		    if (showAllVariablesEnabled == null) {
				IEclipsePreferences scope = InstanceScope.INSTANCE.getNode(EDCDebugger.PLUGIN_ID);
		    	showAllVariablesEnabled = scope.getBoolean(IEDCSymbols.SHOW_ALL_VARIABLES_ENABLED, false);
		    }

			boolean enabled = showAllVariablesEnabled.booleanValue();
			if (locals != null) {
				IEclipsePreferences scope = InstanceScope.INSTANCE.getNode(EDCDebugger.PLUGIN_ID);
				enabled = scope.getBoolean(IEDCSymbols.SHOW_ALL_VARIABLES_ENABLED, showAllVariablesEnabled);
			}

			if (locals == null || !useCachedVariables || enabled != showAllVariablesEnabled) {
				showAllVariablesEnabled = enabled;
				locals = new ArrayList<VariableDMC>();
				localsByName.clear();
				thisPtrs.clear();
				IEDCSymbols symbolsService = getService(IEDCSymbols.class);
				IFunctionScope scope = symbolsService
						.getFunctionAtAddress(executionDMC.getSymbolDMContext(), instructionPtrAddress);
				if (scope != null) {
					this.variableScope = scope;
				}
				
				while (scope != null && instrPtrLinkAddr != null) {
					Collection<IVariable> scopedVariables = scope.getScopedVariables(instrPtrLinkAddr);
					for (IVariable variable : scopedVariables) {
						VariableDMC var = new VariableDMC(Stack.this, this, variable);
						String name = variable.getName();
						// because of inlined functions, debugger information may indicate that
						// more than one "this" pointer is live at one time
						if (name != null && name.equals("this")) {
							thisPtrs.put(variable.getScope().getName(), variable);
						} else {
							// now that we've screened out compiler generated "this" variables,
							// get rid of other compiler generated variables
							// TODO: Allow user to choose whether to show compiler generated variables
							if (var.getVariable().isDeclared()) {
								VariableDMC haveLocal = localsByName.get(name);
								if (haveLocal != null) {
									localsByName.remove(name);
									locals.remove(haveLocal);
								}
								locals.add(var);
								localsByName.put(name, var);
							}
						}
					}

					// if requesting to show all variables, add file-scope globals too
					// (this isn't nearly sufficient since globals can show up
					// in a header while all code is in the source file)
					IScope parentScope = null;
					if (showAllVariablesEnabled)
						parentScope = scope.getParent();
					while (parentScope != null) {
						if (parentScope instanceof ICompileUnitScope) {
							ICompileUnitScope cuScope = ((ICompileUnitScope) parentScope);

							List<ICompileUnitScope> cuScopes = null;
							if (this.debugInfoProvider != null) {
								cuScopes = debugInfoProvider.getCompileUnitsForFile(cuScope.getFilePath());
							} else {
								cuScopes = new ArrayList<ICompileUnitScope>(1);
								cuScopes.add(cuScope);
							}

							// add the globals of all compile unit scopes for the source file
							String cuFile = ((ICompileUnitScope) parentScope).getFilePath().toOSString();
							for (ICompileUnitScope nextCuScope : cuScopes) {
								Collection<IVariable> globals = nextCuScope.getVariables();
								if (globals != null) {
									for (IVariable variable : globals) {
										IPath varFile = variable.getDefiningFile();
										if (varFile != null && !varFile.toOSString().equalsIgnoreCase(cuFile))
											continue;

										VariableDMC var = new VariableDMC(Stack.this, this, variable);
										String name = var.getName();
										VariableDMC haveLocal = localsByName.get(name);
										if (haveLocal != null) {
											localsByName.remove(name);
											locals.remove(haveLocal);
										}
										locals.add(var);
										localsByName.put(name, var);
									}
								}
							}
						}
						parentScope = parentScope.getParent();
					}
					
					if (!(scope.getParent() instanceof IFunctionScope))
						break;
					scope = (IFunctionScope) scope.getParent();
				}
			}
			
			// start with "this" pointers, if any
			VariableDMC[] localsArray = new VariableDMC[(thisPtrs.isEmpty() ? 0 : 1) + locals.size()];
			int i = 0;
			if (!thisPtrs.isEmpty())
				localsArray[i++] = new VariableDMC(Stack.this, this, getOuterThis());
			// TODO For now, turn off ability to see multiple this pointers
			// of the form "this$ScopeName"
//			for (IVariable variable : thisPtrs.values()) {
//				VariableDMC var = new VariableDMC(Stack.this, this, variable);
//				var.setName("this$" + variable.getScope().getName());
//				localsArray[i++] = var;
//			}
			for (VariableDMC var : locals)
				localsArray[i++] = var;
			return localsArray;
		}
		
		/**
		 * From a list of "this" pointers in scope, return the one from the outermost scope
		 * @return this pointer from the outermost scope
		 */
		private IVariable getOuterThis() {
			if (thisPtrs.isEmpty())
				return null;
			
			if (thisPtrs.size() == 1)
				return thisPtrs.values().iterator().next();

			IVariable outer = null;
			for (IVariable variable : thisPtrs.values()) {
				if (outer == null)
					outer = variable;
				else {
					IScope outerScope    = outer.getScope();
					IScope variableScope = variable.getScope();
					if (   variableScope.getLowAddress().compareTo(outerScope.getLowAddress()) < 0
						|| variableScope.getHighAddress().compareTo(outerScope.getHighAddress()) > 0)
						outer = variable;
				}
			}
			return outer;
		}


		/**
		 * Find a variable or enumerator by name
		 * 
		 * @param name required name of the variable or enumerator
		 * @param qualifiedName optional fully qualified name of the variable or enumerator
		 * @param localsOnly whether to restrict search to local variables and enumerators only
		 * @return variable or enumerator, if found; otherwise, null
		 * @since 2.0
		 */
		public IVariableEnumeratorContext findVariableOrEnumeratorByName(String name, String qualifiedName, boolean localsOnly) {
			if (name == null)
				return null;

			if (locals == null)
				getLocals();

			// quickly check for a local variable or enumerator
			IVariableEnumeratorContext variableOrEnumerator;
			
			if (qualifiedName != null) {
				variableOrEnumerator = localsByName.get(qualifiedName);
				if (variableOrEnumerator != null)
					return variableOrEnumerator;
			}

			variableOrEnumerator = localsByName.get(name);
			if (variableOrEnumerator != null)
				return variableOrEnumerator;

			if (enumerators == null)
				getEnumerators();
			
			if (qualifiedName != null) {
				variableOrEnumerator = enumeratorsByName.get(qualifiedName);
				if (variableOrEnumerator != null)
					return variableOrEnumerator;
			}
			
			variableOrEnumerator = enumeratorsByName.get(name);
			if (variableOrEnumerator != null)
				return variableOrEnumerator;
			
			if (name.equals("this")) {
				if (thisPtrs.isEmpty())
					return null;
				return new VariableDMC(Stack.this, this, getOuterThis());
			}

			// TODO For now, turn off ability to see multiple this pointers
			// of the form "this$ScopeName"
//			if (name.startsWith("this$")) {
//				// return the one with the right scope
//				if (thisPtrs.isEmpty())
//					return null;
//				IVariable variable = thisPtrs.get(name.substring("this$".length()));
//				if (variable == null)
//					return null;
//				return new VariableDMC(Stack.this, this, variable);
//			}

			if (localsOnly || this.getVariableScope() == null)
				return null;

			// if there is no local variable or enumerator with this name, not very
			// efficiently check enclosing scopes for a variable or enumerator
			IScope variableScope = this.getVariableScope().getParent();

			// to find file scope variables, we may need to check several compile units
			// associated with one file
			ArrayList<IScope> scopes = new ArrayList<IScope>();

			while (variableOrEnumerator == null && variableScope != null) {
				// At the module level, match against globals across the entire symbol
				// file, even for big symbol files.
				if (variableScope instanceof IModuleScope) {
					Collection<IVariable> variables = ((IModuleScope)variableScope).getVariablesByName(qualifiedName != null ? qualifiedName : name, true);
					if (variables.size() > 0) {
						// list may contain non-global variables, so return the first global
						for (Object varObject : variables) {
							if (varObject instanceof IVariable) {
								IVariable variable = (IVariable)varObject;
								if (variable.getScope() instanceof IModuleScope) {
									variableOrEnumerator = new VariableDMC(Stack.this, this, variable);
									break;
								}
							}
						}
					}
					// module scope has no matching global variables
					break;
				}

				scopes.clear();

				if (variableScope instanceof ICompileUnitScope) {
					// there may be several compile units for a file

					// find the module scope parent of the compile unit
					IScope parent = variableScope.getParent();
					while (parent != null && !(parent instanceof IModuleScope))
						parent = parent.getParent();

					// find all compile units for the file
					if (parent != null) {
						IPath currentFile = ((ICompileUnitScope)variableScope).getFilePath();
						if (currentFile != null)
							for (ICompileUnitScope cu : ((IModuleScope)parent).getCompileUnitsForFile(currentFile))
								scopes.add(cu);
					}
				}

				if (scopes.isEmpty())
					scopes.add(variableScope);

				for (IScope scope : scopes) {
					for (IVariable scopeVariable : scope.getVariables()) {
						String scopeVariableName = scopeVariable.getName();
						if (qualifiedName != null && scopeVariableName.equals(qualifiedName)) {
							variableOrEnumerator = new VariableDMC(Stack.this, this, scopeVariable);
							break;
						}

						if (scopeVariableName.equals(name)) {
							variableOrEnumerator = new VariableDMC(Stack.this, this, scopeVariable);
							break;
						}
					}

					if (variableOrEnumerator == null && scope instanceof IFunctionScope) {
						IFunctionScope functionScope = (IFunctionScope)scope;
						for (IVariable scopeVariable : functionScope.getParameters()) {
							String scopeVariableName = scopeVariable.getName();
							if (qualifiedName != null && scopeVariableName.equals(qualifiedName)) {
								variableOrEnumerator = new VariableDMC(Stack.this, this, scopeVariable);
								break;
							}

							if (scopeVariableName.equals(name)) {
								variableOrEnumerator = new VariableDMC(Stack.this, this, scopeVariable);
								break;
							}
						}
					}

					if (variableOrEnumerator == null) {
						for (IEnumerator scopeEnumerator : scope.getEnumerators()) {
							String scopeEnumeratorName = scopeEnumerator.getName();
							if (qualifiedName != null && scopeEnumeratorName.equals(qualifiedName)) {
								variableOrEnumerator = new EnumeratorDMC(this, scopeEnumerator);
								break;
							}

							if (scopeEnumeratorName.equals(name)) {
								variableOrEnumerator = new EnumeratorDMC(this, scopeEnumerator);
								break;
							}
						}
					}
				}

				variableScope = variableScope.getParent();
			}
			
			return variableOrEnumerator;
		}
		
		public IScope getVariableScope() {
			return variableScope;
		}

		public EnumeratorDMC[] getEnumerators() {
			if (enumerators == null) {
				enumerators = new ArrayList<EnumeratorDMC>();
				if (getServicesTracker() != null) {
					IEDCSymbols symbolsService = getService(Symbols.class);
					if (executionDMC != null && symbolsService != null) {
						IFunctionScope scope = symbolsService.getFunctionAtAddress(executionDMC.getSymbolDMContext(),
								instructionPtrAddress);
						while (scope != null) {
							Collection<IEnumerator> localEnumerators = scope.getEnumerators();
							for (IEnumerator enumerator : localEnumerators) {
								EnumeratorDMC enumeratorDMC = new EnumeratorDMC(this, enumerator);
								enumerators.add(enumeratorDMC);
								enumeratorsByName.put(enumerator.getName(), enumeratorDMC);
							}
							if (!(scope.getParent() instanceof IFunctionScope))
								break;
							scope = (IFunctionScope) scope.getParent();
						}
					}
				}
			}
			return enumerators.toArray(new EnumeratorDMC[enumerators.size()]);
		}

		public EnumeratorDMC findEnumeratorbyName(String name) {
			if (enumerators == null)
				getEnumerators();
			return enumeratorsByName.get(name);
		}
		
		/**
		 * Get the view onto registers for this stack frame.  For the top stack frame, this
		 * forwards to the {@link Registers} service.  Otherwise, this information
		 * is synthesized from unwind information in the debug information.  
		 * @return {@link IFrameRegisters}, never <code>null</code>
		 */
		@SuppressWarnings("unchecked")
		public IFrameRegisters getFrameRegisters() {
			if (frameRegisters == null) {
				if (level == 0) {
					// for top of stack, the registers service does the work
					final Registers registers = getEDCServicesTracker().getService(Registers.class);
					frameRegisters = new CurrentFrameRegisters(executionDMC, registers);
				} else {
					// see if symbolics can provide unwinding support
					if (module != null) {
						Symbols symbolsService = getService(Symbols.class);
						IFrameRegisterProvider frameRegisterProvider = symbolsService.getFrameRegisterProvider(
								executionDMC.getSymbolDMContext(), instructionPtrAddress);
						if (frameRegisterProvider != null) {
							try {
								frameRegisters = frameRegisterProvider.getFrameRegisters(
										getSession(), getEDCServicesTracker(), this);
							} catch (CoreException e) {
								// debug info failure; we should report this 
								frameRegisters = new AlwaysFailingFrameRegisters(e);
							}
						}
					}
					
					if (frameRegisters == null) {
						// no information from symbolics; see if the stack unwinder found anything
						final Map<Integer, BigInteger> preservedRegisters = (Map<Integer,BigInteger>) properties.get(
								PRESERVED_REGISTERS);
						if (preservedRegisters != null) {
							frameRegisters = new PreservedFrameRegisters(dsfServicesTracker, StackFrameDMC.this, preservedRegisters);
						}
					}
					
					if (frameRegisters == null) {
						frameRegisters = new AlwaysFailingFrameRegisters(
								EDCDebugger.newCoreException("cannot read variables in this frame"));
					}
				}
			}
			return frameRegisters;
		}

		/**
		 * Get the frame this one has called.
		 * @return StackFrameDMC or <code>null</code> for top of stack
		 */
		public StackFrameDMC getCalledFrame() throws CoreException {
			return calledFrame;
		}

		/**
		 * Get a type engine (which holds cached information about types for use by expressions)
		 * @return TypeEngine instance
		 */
		public TypeEngine getTypeEngine() {
			return typeEngine;
		}
		
		public IEDCModuleDMContext getModule() {
			return module;
		}

		private Stack getOuterType() {
			return Stack.this;
		}
		
	}

	public class VariableData implements IVariableDMData {

		private final String name;

		public VariableData(VariableDMC variableDMC) {
			name = variableDMC.getName();
		}

		public String getName() {
			return name;
		}

		public String getValue() {
			return "0";
		}

	}

	public class VariableDMC extends DMContext implements IVariableDMContext, IVariableEnumeratorContext {

		public static final String PROP_LOCATION = "Location";
		private final IVariable variable;

		public VariableDMC(IDsfService service, StackFrameDMC frame, IVariable variable) {
			super(Stack.this, new IDMContext[] { frame }, variable.getName(), variable.getName());
			this.variable = variable;
		}

		public IVariable getVariable() {
			return variable;
		}
	}

	public class EnumeratorDMC extends DMContext implements IEnumeratorDMContext, IVariableEnumeratorContext {

		private final IEnumerator enumerator;

		public EnumeratorDMC(StackFrameDMC frame, IEnumerator enumerator) {
			super(Stack.this, new IDMContext[] { frame }, enumerator.getName(), enumerator.getName());
			this.enumerator = enumerator;
		}

		public IEnumerator getEnumerator() {
			return enumerator;
		}
	}

	/**
	 * @param classNames
	 *            the type names the service will be registered under. See
	 *            AbstractDsfService#register for details. We tack on base DSF's
	 *            IStack and this class to the list if not provided.
	 */
	public Stack(DsfSession session, String[] classNames) {
		super(session, 
				massageClassNames(classNames, 
						new String[] { IStack.class.getName(), Stack.class.getName() }));
	}

	/**
	 * @since 2.0
	 */
	public static String createFrameID(IEDCExecutionDMC executionDMC, EdcStackFrame edcFrame) {
		int level = (Integer) edcFrame.props.get(StackFrameDMC.LEVEL_INDEX);
		String parentID = executionDMC.getID();
		return parentID + ".frame[" + level + "]";
	}

	@Override
	protected void doInitialize(RequestMonitor requestMonitor) {
		super.doInitialize(requestMonitor);
		getSession().addServiceEventListener(this, null);
	}

	public void getArguments(IFrameDMContext frameCtx, DataRequestMonitor<IVariableDMContext[]> rm) {
		// never called by DSF. it expects arguments to be lumped in with
		// locals.
		rm.done();
	}

	public void getFrameData(IFrameDMContext frameDmc, DataRequestMonitor<IFrameDMData> rm) {
		if (EDCTrace.STACK_TRACE_ON) { EDCTrace.getTrace().traceEntry(null, EDCTrace.fixArg(frameDmc)); }
		rm.setData(new StackFrameData((StackFrameDMC) frameDmc));
		if (EDCTrace.STACK_TRACE_ON) { EDCTrace.getTrace().traceExit(null, EDCTrace.fixArg(rm.getData())); }
		rm.done();
	}

	public void getFrames(final IDMContext execContext, final DataRequestMonitor<IFrameDMContext[]> rm) {
		if (EDCTrace.STACK_TRACE_ON) { EDCTrace.getTrace().traceEntry(null, EDCTrace.fixArg(execContext)); }

		final ExecutionDMC execDmc = DMContexts.getAncestorOfType(execContext, ExecutionDMC.class);
		if (execDmc != null)
		{
			if (!execDmc.isSuspended())
			{
				rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, INVALID_STATE, "Context is running: " + execDmc, null)); //$NON-NLS-1$
				rm.done();
				return;
			}
			
			asyncExec(new Runnable() {
				public void run() {
					try {
						rm.setData(getFramesForDMC((ExecutionDMC) execContext, 0, ALL_FRAMES));
						if (rm.getData().length == 0)
							rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, INVALID_STATE, "No stack frame available for: " + execDmc, null)); //$NON-NLS-1$
					} catch (CoreException e) {
						Status s = new Status(IStatus.ERROR, EDCDebugger.getUniqueIdentifier(), null, e);
						EDCDebugger.getMessageLogger().log(s);
						rm.setStatus(s);
					}
					if (EDCTrace.STACK_TRACE_ON) { EDCTrace.getTrace().traceExit(null, EDCTrace.fixArg(rm.getData())); }
					rm.done();
				}
				
			}, rm);

		}
		else {
			rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, INVALID_HANDLE, "Invalid context", null)); //$NON-NLS-1$
			rm.done();
		}
	}

	public void getLocals(final IFrameDMContext frameCtx, final DataRequestMonitor<IVariableDMContext[]> rm) {
		asyncExec(new Runnable() {
			public void run() {
				final StackFrameDMC frameContext = (StackFrameDMC) frameCtx;
				IAddress contextIPAddress = frameContext.getInstructionPtrAddress();
				boolean useVariableCache = false;
				// the frame context passed in may be "stale".  it may prove equal to the current frame,
				// but if the instruction ptr address is different, then the locals won't be collected properly
				try {
					IFrameDMContext[] iFrames = getFramesForDMC(frameContext.getExecutionDMC(), 0, ALL_FRAMES);
					for (IFrameDMContext iFrameDMC : iFrames) {
						if (frameCtx == iFrameDMC) {
							useVariableCache = true;
							break;
						}
						if (frameContext.equals(iFrameDMC)) {
							StackFrameDMC frameDMC = (StackFrameDMC)iFrameDMC;
							IAddress stackFrameIPAddr = frameDMC.getInstructionPtrAddress(); 
							if (contextIPAddress.equals(stackFrameIPAddr)) {
								useVariableCache = true;
							} else {
								frameContext.setInstructionPtrAddress(stackFrameIPAddr);
							}
							break;
						}
					}

					rm.setData(frameContext.getLocals(useVariableCache));
				} catch (CoreException e) {
					EDCDebugger.getMessageLogger().log(e.getStatus());
					rm.setStatus(e.getStatus());
				}
				rm.done();
			}
		}, rm);
	}

	public void getStackDepth(IDMContext dmc, final int maxDepth, final DataRequestMonitor<Integer> rm) {
		if (EDCTrace.STACK_TRACE_ON) { EDCTrace.getTrace().traceEntry(null, EDCTrace.fixArgs(new Object[] { dmc, maxDepth })); }
		
		final ExecutionDMC execDmc = DMContexts.getAncestorOfType(dmc, ExecutionDMC.class);
		if (execDmc != null)
		{
			if (!execDmc.isSuspended())
			{
				rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, INVALID_STATE, "Context is running: " + execDmc, null)); //$NON-NLS-1$
				rm.done();
				return;
			}

			asyncExec(new Runnable() {
				public void run() {
					int startFrame = 0;
					int endFrame = ALL_FRAMES;	
					if (maxDepth > 0)
						endFrame = maxDepth - 1;
					try {
						rm.setData(getFramesForDMC(execDmc, startFrame, endFrame).length);
						if (rm.getData() == 0)
							rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, INVALID_STATE, "No stack frame available for: " + execDmc, null)); //$NON-NLS-1$
						if (EDCTrace.STACK_TRACE_ON) { EDCTrace.getTrace().traceExit(null, rm.getData()); }
					} catch (CoreException e) {
						Status s = new Status(IStatus.ERROR, EDCDebugger.getUniqueIdentifier(), null, e);
						EDCDebugger.getMessageLogger().log(s);
						rm.setStatus(s);
					}
					rm.done();
				}
			}, rm);
		}
		else {
			rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, INVALID_HANDLE, "Invalid context", null)); //$NON-NLS-1$
			rm.done();
		}
	}

	public void getTopFrame(final IDMContext execContext, final DataRequestMonitor<IFrameDMContext> rm) {
		if (EDCTrace.STACK_TRACE_ON) { EDCTrace.getTrace().traceEntry(null, EDCTrace.fixArg(execContext)); }

		asyncExec(new Runnable() {
			public void run() {
				try {
					IFrameDMContext[] frames = getFramesForDMC((ExecutionDMC) execContext, 0, 0);
					if (frames.length == 0) {
						rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, INVALID_STATE,
								"No top stack frame available", null)); //$NON-NLS-1$
						rm.done();
						return;
					}
					rm.setData(frames[0]);
				} catch (CoreException e) {
					Status s = new Status(IStatus.ERROR, EDCDebugger.getUniqueIdentifier(), null, e);
					EDCDebugger.getMessageLogger().log(s);
					rm.setStatus(s);
				}
				if (EDCTrace.STACK_TRACE_ON) { EDCTrace.getTrace().traceExit(null, EDCTrace.fixArg(rm.getData())); }
				rm.done();
			}
		}, rm);

	}

	public void getVariableData(IVariableDMContext variableDmc, DataRequestMonitor<IVariableDMData> rm) {
		rm.setData(new VariableData((VariableDMC) variableDmc));
		rm.done();
	}

	@SuppressWarnings("unchecked")
	public void getModelData(IDMContext dmc, DataRequestMonitor<?> rm) {
		if (dmc instanceof IFrameDMContext) {
			getFrameData((IFrameDMContext) dmc, (DataRequestMonitor<IFrameDMData>) rm);
		} else if (dmc instanceof IVariableDMContext) {
			getVariableData((IVariableDMContext) dmc, (DataRequestMonitor<IVariableDMData>) rm);
		} else
			rm.done();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IStack#getFrames(org.eclipse.cdt.dsf.datamodel.IDMContext, int, int, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	public void getFrames(final IDMContext execContext, final int startIndex, final int endIndex, final DataRequestMonitor<IFrameDMContext[]> rm) {
		if (EDCTrace.STACK_TRACE_ON) { EDCTrace.getTrace().traceEntry(null, EDCTrace.fixArgs(new Object[] { execContext, startIndex, endIndex })); }
		final ExecutionDMC execDmc = DMContexts.getAncestorOfType(execContext, ExecutionDMC.class);
		if (execDmc != null)
		{
			if (!execDmc.isSuspended())
			{
				rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, INVALID_STATE, "Context is running: " + execDmc, null)); //$NON-NLS-1$
				rm.done();
				return;
			}

			asyncExec(new Runnable() {
				public void run() {
					try {
						rm.setData(getFramesForDMC((ExecutionDMC) execContext, startIndex, endIndex));
						if (rm.getData().length == 0)
							rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, INVALID_STATE, "No stack frame available for: " + execContext, null)); //$NON-NLS-1$
					} catch (CoreException e) {
						Status s = new Status(IStatus.ERROR, EDCDebugger.getUniqueIdentifier(), null, e);
						EDCDebugger.getMessageLogger().log(s);
						rm.setStatus(s);
					}
					if (EDCTrace.STACK_TRACE_ON) { EDCTrace.getTrace().traceExit(null, EDCTrace.fixArgs(rm.getData())); }
					rm.done();
				}
				
			}, rm);

		}
		else {
			rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, INVALID_HANDLE, "Invalid context", null)); //$NON-NLS-1$
			rm.done();
		}
		if (EDCTrace.STACK_TRACE_ON) { EDCTrace.getTrace().traceExit(null, EDCTrace.fixArgs(rm.getData())); }
	}

	public IFrameDMContext[] getFramesForDMC(IEDCExecutionDMC context, int startIndex, int endIndex) throws CoreException {
		if (EDCTrace.STACK_TRACE_ON) { EDCTrace.getTrace().traceEntry(null, EDCTrace.fixArgs(new Object[] { context, startIndex, endIndex })); }

		if (!context.isSuspended() || 
			! RunControl.isNonContainer(context))	// no frames for container context. 
		{
			return new IFrameDMContext[0];
		}

		boolean needsUpdate = false;
		synchronized (stackFrames) {
			List<StackFrameDMC> frames = stackFrames.get(context.getID());
			// Need to update the frames if there is no cached list for this
			// context or if the cached list does not include all of the
			// requested frames.
			if (frames == null) {
				// nothing in the cache so need to update
				needsUpdate = true;
			} else if (allFramesCached.get(context.getID())) {
				// all frames are cached
				needsUpdate = false;
			} else if (endIndex == ALL_FRAMES) {
				// some but not all frames cached
				needsUpdate = true;
			} else {
				// some but not all requested frames cached
				needsUpdate = (frames.get(0).getLevel() > startIndex || 
						frames.get(frames.size() - 1).getLevel() < endIndex);
			}

			if (needsUpdate)
				updateFrames(context, startIndex, endIndex);

			frames = stackFrames.get(context.getID());
			// endIndex is inclusive and may be negative to fetch all frames
			if (endIndex >= 0) {
				if (startIndex < frames.size() && startIndex <= endIndex) {
					frames = frames.subList(startIndex, Math.min(endIndex + 1, frames.size()));
				} else {
					frames = Collections.emptyList();
				}
			}
			IFrameDMContext[] result = frames.toArray(new IFrameDMContext[frames.size()]);
			if (EDCTrace.STACK_TRACE_ON) { EDCTrace.getTrace().traceExit(null, EDCTrace.fixArgs(result)); }
			return result;
		}
	}

	private void updateFrames(IEDCExecutionDMC context, int startIndex, int endIndex) throws CoreException {
		ArrayList<StackFrameDMC> frames = new ArrayList<StackFrameDMC>();
		List<EdcStackFrame> edcFrames = computeStackFrames(context, startIndex, endIndex);
		StackFrameDMC previous = null;
		for (EdcStackFrame edcFrame : edcFrames) {
			StackFrameDMC frame = new StackFrameDMC(context, edcFrame);
			if (previous != null) {
				frame.calledFrame = previous;
				// note: don't store "callerFrame" since this is missing if only a partial stack was fetched
			}
			frames.add(frame);
			previous = frame;
		}
		
		stackFrames.put(context.getID(), frames);
		
		// all frames are cached if we request all frames, or if the returned number of frames was less than
		// the requested max number of frames.  e.g. if we ask for 10 and they return 9, it's because there
		// are only 9 frames.  so we have calculated all of them.
		allFramesCached.put(context.getID(), startIndex == 0 && ((endIndex == ALL_FRAMES) || (frames.size() <= endIndex)));
	}

	/**
	 * A stack frame described as one or more of the following properties, plus
	 * any additional custom ones.
	 * 
	 * <ul>
	 * <li>{@link StackFrameDMC#LEVEL_INDEX}
	 * <li>{@link StackFrameDMC#ROOT_FRAME}
	 * <li>{@link StackFrameDMC#BASE_ADDR}
	 * <li>{@link StackFrameDMC#INSTRUCTION_PTR_ADDR}
	 * <li>{@link StackFrameDMC#MODULE_NAME}
	 * <li>{@link StackFrameDMC#SOURCE_FILE}
	 * <li>{@link StackFrameDMC#FUNCTION_NAME}
	 * <li>{@link StackFrameDMC#LINE_NUMBER}
	 * <li>{@link StackFrameDMC#IN_PROLOGUE}
	 * <li>{@link StackFrameDMC#PRESERVED_REGISTERS}
	 * </ul>
	 * 
	 * @since 2.0
	 */
	public class EdcStackFrame {
		public EdcStackFrame(Map<String, Object> props) { 
			this.props = props; 
		}
		public Map<String, Object> props;
	}
	
	protected abstract List<EdcStackFrame> computeStackFrames(IEDCExecutionDMC context, int startIndex, int endIndex) throws CoreException;

	public void loadFramesForContext(IEDCExecutionDMC exeDmc, Element allFrames) throws Exception {
		flushCache(null);
		List<StackFrameDMC> frames = Collections.synchronizedList(new ArrayList<StackFrameDMC>());

		NodeList frameElements = allFrames.getElementsByTagName(STACK_FRAME);

		int numFrames = frameElements.getLength();
		StackFrameDMC previousFrameDMC = null;
		
		for (int i = 0; i < numFrames; i++) {
			Element groupElement = (Element) frameElements.item(i);
			Element propElement = (Element) groupElement.getElementsByTagName(SnapshotUtils.PROPERTIES).item(0);
			HashMap<String, Object> properties = new HashMap<String, Object>();
			SnapshotUtils.initializeFromXML(propElement, properties);

			// ensure that stack level numbering is canonical: 
			// we expect level==0 to be the top, but it used to be 1
			properties.put(StackFrameDMC.LEVEL_INDEX, i);
			
			StackFrameDMC frameDMC = new StackFrameDMC(exeDmc, new EdcStackFrame(properties));
			frameDMC.loadSnapshot(groupElement);
			if (previousFrameDMC != null) {
				frameDMC.calledFrame = previousFrameDMC;
			}
			frames.add(frameDMC);

			previousFrameDMC = frameDMC;
		}
		stackFrames.put(exeDmc.getID(), frames);
		allFramesCached.put(exeDmc.getID(), true);
	}

	public void flushCache(IDMContext context) {
		if (isSnapshot())
			return;
		if (context != null && context instanceof IEDCDMContext) {
			String contextID = ((IEDCDMContext) context).getID();
			stackFrames.remove(contextID);
			allFramesCached.remove(contextID);
		} else {
			stackFrames.clear();
			allFramesCached.clear();
		}
	}

	@DsfServiceEventHandler
	public void eventDispatched(ISuspendedDMEvent e) {
		flushCache(e.getDMContext());
	}

	@DsfServiceEventHandler
	public void eventDispatched(IResumedDMEvent e) {
		flushCache(e.getDMContext());
	}

}
