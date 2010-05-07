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
package org.eclipse.cdt.debug.edc.services;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.IEDCTraceOptions;
import org.eclipse.cdt.debug.edc.internal.launch.CSourceLookup;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Modules;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Modules.ModuleDMC;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl.ExecutionDMC;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Symbols;
import org.eclipse.cdt.debug.edc.internal.snapshot.SnapshotUtils;
import org.eclipse.cdt.debug.edc.internal.symbols.MemoryVariableLocation;
import org.eclipse.cdt.debug.edc.internal.symbols.dwarf.EDCSymbolReader;
import org.eclipse.cdt.debug.edc.snapshot.IAlbum;
import org.eclipse.cdt.debug.edc.snapshot.ISnapshotContributor;
import org.eclipse.cdt.debug.edc.symbols.ICompileUnitScope;
import org.eclipse.cdt.debug.edc.symbols.IDebugInfoProvider;
import org.eclipse.cdt.debug.edc.symbols.IEDCSymbolReader;
import org.eclipse.cdt.debug.edc.symbols.IEnumerator;
import org.eclipse.cdt.debug.edc.symbols.IFunctionScope;
import org.eclipse.cdt.debug.edc.symbols.ILineEntry;
import org.eclipse.cdt.debug.edc.symbols.IModuleScope;
import org.eclipse.cdt.debug.edc.symbols.IScope;
import org.eclipse.cdt.debug.edc.symbols.IVariable;
import org.eclipse.cdt.debug.edc.symbols.TypeEngine;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLookupDirector;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.ICachingService;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.service.IDsfService;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
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
			address = dmc.getIPAddress();
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
			if (id != null)
				registers.writeRegister(executionDMC, id, value.toString(16));
			else
				throw EDCDebugger.newCoreException(MessageFormat.format("could not find register number {0}", regnum));
		}
    }
    
    /**
	 * Frame registers read from preserved registers on the stack frame.
	 */
	public static class PreservedFrameRegisters implements IFrameRegisters {
		private final Map<Integer, BigInteger> preservedRegisters;
		private final DsfServicesTracker dsfServicesTracker;
		private final StackFrameDMC context;

		/**
		 * @param preservedRegisters map of register number to the address
		 * where the register is saved
		 */
		public PreservedFrameRegisters(DsfServicesTracker dsfServicesTracker,
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
		public static final String IP_ADDR = "Instruction_address";
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

		private final DsfServicesTracker dsfServicesTracker = getServicesTracker();
		private final IEDCExecutionDMC executionDMC;
		private final int level;
		private IAddress baseAddress;
		private IAddress ipAddress;

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
		private IFunctionScope functionScope;
		private IFrameRegisters frameRegisters;
		public StackFrameDMC calledFrame;
		private TypeEngine typeEngine;
		private IEDCModuleDMContext module;

		public StackFrameDMC(final IEDCExecutionDMC executionDMC, Map<String, Object> frameProperties) {
			super(Stack.this, new IDMContext[] { executionDMC }, createFrameID(executionDMC, frameProperties), frameProperties);
			this.executionDMC = executionDMC;
			this.level = (Integer) frameProperties.get(LEVEL_INDEX);
			this.moduleName = (String) frameProperties.get(MODULE_NAME);

			Object base = frameProperties.get(BASE_ADDR);
			if (base instanceof Integer)
				this.baseAddress = new Addr64(base.toString());
			if (base instanceof Long)
				this.baseAddress = new Addr64(base.toString());
			if (base instanceof String) // the string should be hex string
				this.baseAddress = new Addr64((String) base, 16);

			Object ipAddr = frameProperties.get(IP_ADDR);
			if (ipAddr instanceof Integer)
				this.ipAddress = new Addr64(ipAddr.toString());
			if (ipAddr instanceof Long)
				this.ipAddress = new Addr64(ipAddr.toString());
			if (ipAddr instanceof String)
				this.ipAddress = new Addr64((String) ipAddr, 16);

			if (frameProperties.containsKey(SOURCE_FILE)) {
				this.sourceFile = (String) frameProperties.get(SOURCE_FILE);
				this.functionName = (String) frameProperties.get(FUNCTION_NAME);
				this.lineNumber = (Integer) frameProperties.get(LINE_NUMBER);
			} else {
				// compute the source location
				IEDCSymbols symbolsService = getServicesTracker().getService(Symbols.class);
				ILineEntry line = symbolsService.getLineEntryForAddress(executionDMC.getSymbolDMContext(), ipAddress);
				if (line != null) {
					sourceFile = line.getFilePath().toOSString();
					frameProperties.put(SOURCE_FILE, sourceFile);
					lineNumber = line.getLineNumber();
					frameProperties.put(LINE_NUMBER, lineNumber);
				}

				functionScope = symbolsService
						.getFunctionAtAddress(executionDMC.getSymbolDMContext(), ipAddress);
				if (functionScope != null) {
					// ignore inlined functions
					while (functionScope.getParent() instanceof IFunctionScope) {
						functionScope = (IFunctionScope) functionScope.getParent();
					}
					functionName = functionScope.getName();
					frameProperties.put(FUNCTION_NAME, functionName);
				}
			}
			properties.putAll(frameProperties);
			
			// get the type engine
			IDebugInfoProvider debugInfoProvider = null;
			IEDCModules modules = dsfServicesTracker.getService(IEDCModules.class);
			if (modules != null) {
				module = modules.getModuleByAddress(executionDMC.getSymbolDMContext(), ipAddress);
				if (module != null) {
					IEDCSymbolReader symbolReader = module.getSymbolReader();
					if (symbolReader instanceof EDCSymbolReader) {
						debugInfoProvider = ((EDCSymbolReader) symbolReader).getDebugInfoProvider();
					}
				}
			}
			typeEngine = new TypeEngine(dsfServicesTracker, debugInfoProvider);
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

		public IAddress getIPAddress() {
			return ipAddress;
		}

		public int getLevel() {
			return level;
		}

		public DsfServicesTracker getDsfServicesTracker() {
			return dsfServicesTracker;
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
			return "StackFrameDMC [baseAddress=" + baseAddress
					+ ", sourceFile=" + sourceFile + ", functionName="
					+ functionName + ", lineNumber=" + lineNumber + "]";
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
			CSourceLookup lookup = getServicesTracker().getService(CSourceLookup.class);
			RunControl runControl = getServicesTracker().getService(RunControl.class);
			CSourceLookupDirector director = lookup.getSourceLookupDirector(runControl.getRootDMC());
			try {
				Object[] elements = director.findSourceElements(sourceFile);
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
				}
			} catch (CoreException e1) {
				EDCDebugger.getMessageLogger().logError(sourceFile, e1);
			}
			return result;
		}

		public Element takeShapshot(IAlbum album, Document document, IProgressMonitor monitor) {
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
			// may need to refresh the locals list because "Show All Variables"
			// toggle has changed
		    if (showAllVariablesEnabled == null) {
				IEclipsePreferences scope = new InstanceScope().getNode(EDCDebugger.PLUGIN_ID);
		    	showAllVariablesEnabled = scope.getBoolean(IEDCSymbols.SHOW_ALL_VARIABLES_ENABLED, false);
		    }

			Boolean enabled = showAllVariablesEnabled;
			if (locals != null) {
				IEclipsePreferences scope = new InstanceScope().getNode(EDCDebugger.PLUGIN_ID);
				enabled = scope.getBoolean(IEDCSymbols.SHOW_ALL_VARIABLES_ENABLED, showAllVariablesEnabled);
			}

			if (locals == null || (locals != null && enabled != showAllVariablesEnabled)) {
				showAllVariablesEnabled = enabled;
				locals = new ArrayList<VariableDMC>();
				IEDCSymbols symbolsService = getServicesTracker().getService(Symbols.class);
				IFunctionScope scope = symbolsService
						.getFunctionAtAddress(executionDMC.getSymbolDMContext(), ipAddress);
				if (scope != null) {
					this.variableScope = scope;
				}
				
				// TODO: we fetch ModuleDMC a whole lot; it could be saved in a StackFrameDMC
				IEDCModules modulesService = getServicesTracker().getService(Modules.class);
				IEDCModuleDMContext module = modulesService.getModuleByAddress(executionDMC.getSymbolDMContext(), ipAddress);
				
				IAddress linkAddress = null;
				if (module != null) {
					linkAddress = module.toLinkAddress(ipAddress);
				}

				while (scope != null) {
					Collection<IVariable> scopedVariables = scope.getScopedVariables(linkAddress);
					for (IVariable variable : scopedVariables) {
						VariableDMC var = new VariableDMC(Stack.this, this, variable);
						locals.add(var);
						localsByName.put(var.getName(), var);
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

							// there may be multiple compile unit scopes for the same source file,
							// so look for a debug info provider to find multiples
							IDebugInfoProvider debugInfoProvider = null;
							IEDCSymbolReader symbolReader = module.getSymbolReader();
							if (symbolReader instanceof EDCSymbolReader) {
								debugInfoProvider = ((EDCSymbolReader) symbolReader).getDebugInfoProvider();
							}

							List<ICompileUnitScope> cuScopes = null;
							if (debugInfoProvider != null) {
								cuScopes = debugInfoProvider.getCompileUnitsForFile(cuScope.getFilePath());
							} else {
								cuScopes = new ArrayList<ICompileUnitScope>(1);
								cuScopes.add(cuScope);
							}

							// add the globals of all compile unit scopes for the source file
							for (ICompileUnitScope nextCuScope : cuScopes) {
								Collection<IVariable> globals = nextCuScope.getVariables();
								if (globals != null) {
									for (IVariable variable : globals) {
										VariableDMC var = new VariableDMC(Stack.this, this, variable);
										locals.add(var);
										localsByName.put(var.getName(), var);
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
			return locals.toArray(new VariableDMC[locals.size()]);
		}

		/**
		 * Find a variable or enumerator by name
		 * 
		 * @param name name of the variable or enumerator
		 * @param localsOnly whether to restrict search to local variables and enumerators only
		 * @return variable or enumerator, if found; otherwise, null
		 */
		public IVariableEnumeratorContext findVariableOrEnumeratorByName(String name, boolean localsOnly) {
			if (locals == null)
				getLocals();

			// quickly check for a local variable or enumerator
			IVariableEnumeratorContext variableOrEnumerator;

			variableOrEnumerator = localsByName.get(name);
			if (variableOrEnumerator != null)
				return variableOrEnumerator;

			if (enumerators == null)
				getEnumerators();
			
			variableOrEnumerator = enumeratorsByName.get(name);
			if (variableOrEnumerator != null)
				return variableOrEnumerator;

			if (localsOnly || this.getVariableScope() == null)
				return null;

			// if there is no local variable or enumerator with this name, not very
			// efficiently check enclosing scopes for a variable or enumerator
			IScope variableScope = this.getVariableScope().getParent();

			while (variableOrEnumerator == null && variableScope != null) {
				// At the module level, match against globals across the entire symbol
				// file, even for big symbol files.
				if (variableScope instanceof IModuleScope) {
					Collection<IVariable> variables = ((IModuleScope)variableScope).getVariablesByName(name, true);
					if (variables.size() > 0) {
						Object[] variableArray = variables.toArray();
						if (variableArray[0] instanceof IVariable)
							variableOrEnumerator = new VariableDMC(Stack.this, this, (IVariable)variableArray[0]);
					}
					// module scope has no parent with variables
					break;
				}

				for (IVariable scopeVariable : variableScope.getVariables()) {
					if (scopeVariable.getName().equals(name)) {
						variableOrEnumerator = new VariableDMC(Stack.this, this, scopeVariable);
						break;
					}
				}

				if (variableOrEnumerator == null && variableScope instanceof IFunctionScope) {
					IFunctionScope functionScope = (IFunctionScope)variableScope;
					for (IVariable scopeVariable : functionScope.getParameters()) {
						if (scopeVariable.getName().equals(name)) {
							variableOrEnumerator = new VariableDMC(Stack.this, this, scopeVariable);
							break;
						}
					}
				}

				if (variableOrEnumerator == null) {
					for (IEnumerator scopeEnumerator : variableScope.getEnumerators()) {
						if (scopeEnumerator.getName().equals(name)) {
							variableOrEnumerator = new EnumeratorDMC(this, scopeEnumerator);
							break;
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
					IEDCSymbols symbolsService = getServicesTracker().getService(Symbols.class);
					if (executionDMC != null && symbolsService != null) {
						IFunctionScope scope = symbolsService.getFunctionAtAddress(executionDMC.getSymbolDMContext(),
								ipAddress);
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
					final Registers registers = getDsfServicesTracker().getService(Registers.class);
					frameRegisters = new CurrentFrameRegisters(executionDMC, registers);
				} else {
					// see if symbolics can provide unwinding support
					Modules modulesService = getServicesTracker().getService(Modules.class);
					ModuleDMC module = modulesService.getModuleByAddress(executionDMC.getSymbolDMContext(), ipAddress);
					if (module != null) {
						Symbols symbolsService = getServicesTracker().getService(Symbols.class);
						IFrameRegisterProvider frameRegisterProvider = symbolsService.getFrameRegisterProvider(
								executionDMC.getSymbolDMContext(), ipAddress);
						if (frameRegisterProvider != null) {
							try {
								frameRegisters = frameRegisterProvider.getFrameRegisters(
										getSession(), getServicesTracker(), this);
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

	public Stack(DsfSession session, String[] classNames) {
		super(session, classNames);
	}

	public static String createFrameID(IEDCExecutionDMC executionDMC, Map<String, Object> frameProperties) {
		int level = (Integer) frameProperties.get(StackFrameDMC.LEVEL_INDEX);
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
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.STACK_TRACE, frameDmc);
		rm.setData(new StackFrameData((StackFrameDMC) frameDmc));
		EDCDebugger.getDefault().getTrace().traceExit(IEDCTraceOptions.STACK_TRACE, rm.getData());
		rm.done();
	}

	public void getFrames(IDMContext execContext, DataRequestMonitor<IFrameDMContext[]> rm) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.STACK_TRACE, execContext);
		rm.setData(new IFrameDMContext[0]);
		if (execContext instanceof IEDCExecutionDMC) {
			rm.setData(getFramesForDMC((ExecutionDMC) execContext, 0, ALL_FRAMES));
		}
		EDCDebugger.getDefault().getTrace().traceExit(IEDCTraceOptions.STACK_TRACE, rm.getData());
		rm.done();
	}

	public void getLocals(IFrameDMContext frameCtx, DataRequestMonitor<IVariableDMContext[]> rm) {
		StackFrameDMC frameContext = (StackFrameDMC) frameCtx;
		rm.setData(frameContext.getLocals());
		rm.done();
	}

	public void getStackDepth(IDMContext dmc, int maxDepth, DataRequestMonitor<Integer> rm) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.STACK_TRACE, new Object[] { dmc, maxDepth });
		rm.setData(getFramesForDMC((ExecutionDMC) dmc, 0, ALL_FRAMES).length);
		EDCDebugger.getDefault().getTrace().traceExit(IEDCTraceOptions.STACK_TRACE, rm.getData());
		rm.done();
	}

	public void getTopFrame(IDMContext execContext, DataRequestMonitor<IFrameDMContext> rm) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.STACK_TRACE, execContext);
		IFrameDMContext[] frames = getFramesForDMC((ExecutionDMC) execContext, 0, 0);

		if (frames.length == 0) {
			rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, INVALID_STATE,
					"No top stack frame available", null)); //$NON-NLS-1$
			rm.done();
			return;
		}
		rm.setData(frames[0]);
		EDCDebugger.getDefault().getTrace().traceExit(IEDCTraceOptions.STACK_TRACE, rm.getData());
		rm.done();
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
	public void getFrames(IDMContext execContext, int startIndex, int endIndex, DataRequestMonitor<IFrameDMContext[]> rm) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.STACK_TRACE,
				new Object[] { execContext, startIndex, endIndex });
		if (execContext instanceof IEDCExecutionDMC) {
			rm.setData(getFramesForDMC((ExecutionDMC) execContext, startIndex, endIndex));
		}
		EDCDebugger.getDefault().getTrace().traceExit(IEDCTraceOptions.STACK_TRACE, rm.getData());
		rm.done();
	}

	public IFrameDMContext[] getFramesForDMC(IEDCExecutionDMC context, int startIndex, int endIndex) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.STACK_TRACE,
				new Object[] { context, startIndex, endIndex });

		if (!context.isSuspended()) {
			return new IFrameDMContext[0];
		}

		boolean needsUpdate = false;
		synchronized (stackFrames) {
			List<StackFrameDMC> frames = stackFrames.get(context.getID());
			// Need to update the frames if there is no cached list for this
			// context or if the cached list does not include all of the
			// requested frames.
			needsUpdate = frames == null ||
			(frames.get(0).getLevel() > startIndex || 
				frames.get(frames.size() - 1).getLevel() < endIndex) ||
				(endIndex == ALL_FRAMES && !allFramesCached.get(context.getID()));
		}
		if (needsUpdate)
			updateFrames(context, startIndex, endIndex);
		synchronized (stackFrames) {
			List<StackFrameDMC> frames = stackFrames.get(context.getID());
			// endIndex is inclusive and may be negative to fetch all frames
			if (endIndex >= 0) {
				if (startIndex < frames.size() && startIndex <= endIndex) {
					frames = frames.subList(startIndex, Math.min(endIndex + 1, frames.size()));
				} else {
					frames = Collections.emptyList();
				}
			}
			IFrameDMContext[] result = frames.toArray(new IFrameDMContext[frames.size()]);
			EDCDebugger.getDefault().getTrace().traceExit(IEDCTraceOptions.STACK_TRACE, result);
			return result;
		}
	}

	private void updateFrames(IEDCExecutionDMC context, int startIndex, int endIndex) {
		ArrayList<StackFrameDMC> frames = new ArrayList<StackFrameDMC>();
		List<Map<String, Object>> frameProperties = computeStackFrames(context, startIndex, endIndex);
		StackFrameDMC previous = null;
		for (Map<String, Object> props : frameProperties) {
			StackFrameDMC frame = new StackFrameDMC(context, props);
			if (previous != null) {
				frame.calledFrame = previous;
				// note: don't store "callerFrame" since this is missing if only a partial stack was fetched
			}
			frames.add(frame);
			previous = frame;
		}
		stackFrames.put(context.getID(), frames);
		allFramesCached.put(context.getID(), startIndex == 0 && endIndex == ALL_FRAMES);
	}

	protected abstract List<Map<String, Object>> computeStackFrames(IEDCExecutionDMC context, int startIndex, int endIndex);

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
			
			StackFrameDMC frameDMC = new StackFrameDMC(exeDmc, properties);
			frameDMC.loadSnapshot(groupElement);
			if (previousFrameDMC != null) {
				frameDMC.calledFrame = previousFrameDMC;
			}
			frames.add(frameDMC);

			previousFrameDMC = frameDMC;
		}
		stackFrames.put(exeDmc.getID(), frames);

	}

	public void flushCache(IDMContext context) {
		if (isSnapshot())
			return;
		if (context != null && context instanceof IEDCDMContext) {
			stackFrames.remove(((IEDCDMContext) context).getID());
		} else {
			stackFrames.clear();
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
