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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.internal.symbols.dwarf.EDCSymbolReader;
import org.eclipse.cdt.debug.edc.internal.symbols.files.DebugInfoProviderFactory;
import org.eclipse.cdt.debug.edc.internal.symbols.files.ExecutableSymbolicsReaderFactory;
import org.eclipse.cdt.debug.edc.services.AbstractEDCService;
import org.eclipse.cdt.debug.edc.services.IEDCModuleDMContext;
import org.eclipse.cdt.debug.edc.services.IEDCModules;
import org.eclipse.cdt.debug.edc.services.IEDCSymbols;
import org.eclipse.cdt.debug.edc.services.IFrameRegisterProvider;
import org.eclipse.cdt.debug.edc.services.IFrameRegisters;
import org.eclipse.cdt.debug.edc.services.Stack.StackFrameDMC;
import org.eclipse.cdt.debug.edc.symbols.IDebugInfoProvider;
import org.eclipse.cdt.debug.edc.symbols.IEDCSymbolReader;
import org.eclipse.cdt.debug.edc.symbols.IExecutableSymbolicsReader;
import org.eclipse.cdt.debug.edc.symbols.IFunctionScope;
import org.eclipse.cdt.debug.edc.symbols.ILineEntry;
import org.eclipse.cdt.debug.edc.symbols.IModuleLineEntryProvider;
import org.eclipse.cdt.debug.edc.symbols.IModuleScope;
import org.eclipse.cdt.debug.edc.symbols.IScope;
import org.eclipse.cdt.debug.edc.symbols.ISymbol;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IModules.ISymbolDMContext;
import org.eclipse.cdt.dsf.debug.service.ISymbols;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.model.ISourceLocator;

public class Symbols extends AbstractEDCService implements ISymbols, IEDCSymbols {
	private static Map<IPath, WeakReference<IEDCSymbolReader>> readerCache = new HashMap<IPath, WeakReference<IEDCSymbolReader>>();
	private ISourceLocator sourceLocator;
	
	public ISourceLocator getSourceLocator() {
		return sourceLocator;
	}

	public void setSourceLocator(ISourceLocator sourceLocator) {
		this.sourceLocator = sourceLocator;
	}

	public Symbols(DsfSession session) {
		super(session, new String[] { IEDCSymbols.class.getName(), ISymbols.class.getName(), Symbols.class.getName() });
	}

	public void getSymbols(ISymbolDMContext symCtx, DataRequestMonitor<Iterable<ISymbolObjectDMContext>> rm) {
		// TODO Auto-generated method stub

	}

	public void getModelData(IDMContext dmc, DataRequestMonitor<?> rm) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.services.dsf.IEDCSymbols#getFunctionAtAddress(org.eclipse.cdt.dsf.debug.service.IModules.ISymbolDMContext, org.eclipse.cdt.core.IAddress)
	 */
	public IFunctionScope getFunctionAtAddress(ISymbolDMContext context, IAddress runtimeAddress) {
		IEDCModules modulesService = getServicesTracker().getService(Modules.class);
		IEDCModuleDMContext module = modulesService.getModuleByAddress(context, runtimeAddress);
		if (module != null) {
			IEDCSymbolReader reader = module.getSymbolReader();
			if (reader != null) {
				IScope scope = reader.getModuleScope().getScopeAtAddress(module.toLinkAddress(runtimeAddress));
				while (scope != null && !(scope instanceof IFunctionScope)) {
					scope = scope.getParent();
				}
				return (IFunctionScope) scope;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.services.dsf.IEDCSymbols#getFunctionAtAddress(org.eclipse.cdt.dsf.debug.service.IModules.ISymbolDMContext, org.eclipse.cdt.core.IAddress)
	 */
	public String getSymbolNameAtAddress(ISymbolDMContext context, IAddress runtimeAddress) {
		IEDCModules modulesService = getServicesTracker().getService(Modules.class);
		IEDCModuleDMContext module = modulesService.getModuleByAddress(context, runtimeAddress);
		if (module != null) {
			IEDCSymbolReader reader = module.getSymbolReader();
			if (reader != null) {
				ISymbol symbol = reader.getSymbolAtAddress(module.toLinkAddress(runtimeAddress));
				if (symbol != null)
					return symbol.getName();
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.services.dsf.IEDCSymbols#getLineEntryForAddress(org.eclipse.cdt.dsf.debug.service.IModules.ISymbolDMContext, org.eclipse.cdt.core.IAddress)
	 */
	public ILineEntry getLineEntryForAddress(ISymbolDMContext context, IAddress runtimeAddress) {
		IEDCModules modulesService = getServicesTracker().getService(Modules.class);
		IEDCModuleDMContext module = modulesService.getModuleByAddress(context, runtimeAddress);
		if (module != null) {
			IEDCSymbolReader reader = module.getSymbolReader();
			if (reader != null) {
				IAddress linkAddress = module.toLinkAddress(runtimeAddress);
				IModuleLineEntryProvider lineEntryProvider = reader.getModuleScope().getModuleLineEntryProvider();
				return lineEntryProvider.getLineEntryAtAddress(linkAddress);
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.services.dsf.IEDCSymbols#getLineEntriesForAddressRange(org.eclipse.cdt.dsf.debug.service.IModules.ISymbolDMContext, org.eclipse.cdt.core.IAddress, org.eclipse.cdt.core.IAddress)
	 */
	public List<ILineEntry> getLineEntriesForAddressRange(ISymbolDMContext context, IAddress start, IAddress end) {
		List<ILineEntry> lineEntries = new ArrayList<ILineEntry>();

		IEDCModules modulesService = getServicesTracker().getService(Modules.class);
		IEDCModuleDMContext module = modulesService.getModuleByAddress(context, start);
		if (module == null)
			return lineEntries;

		IAddress linkStartAddress = module.toLinkAddress(start);
		IAddress linkEndAddress = module.toLinkAddress(end);

		IEDCSymbolReader reader = module.getSymbolReader();
		if (reader != null) {
			if (linkStartAddress == null)
				linkStartAddress = module.getSymbolReader().getModuleScope().getLowAddress();
			if (linkEndAddress == null)
				linkEndAddress = module.getSymbolReader().getModuleScope().getHighAddress();

			IModuleScope moduleScope = reader.getModuleScope();
			
			if (linkEndAddress.compareTo(moduleScope.getHighAddress()) > 0) {
				// end address is out of the module sections.
				// we'll keep getting source lines until we reach an address
				// point where no source line is available.
				linkEndAddress = moduleScope.getHighAddress();
			}

			IModuleLineEntryProvider lineEntryProvider = moduleScope.getModuleLineEntryProvider();

			ILineEntry entry = lineEntryProvider.getLineEntryAtAddress(linkStartAddress);
			while (entry != null && entry.getLowAddress().compareTo(linkEndAddress) < 0) {
				lineEntries.add(entry);
				// FIXME: this shouldn't happen
				if (entry.getLowAddress().compareTo(entry.getHighAddress()) >= 0)
					entry = lineEntryProvider.getLineEntryAtAddress(entry.getHighAddress().add(1));
				else
					entry = lineEntryProvider.getLineEntryAtAddress(entry.getHighAddress());
			}
		}

		return lineEntries;
	}
	
	/**
	 * Get an accessor for registers in stack frames other than the current one.
	 * <p>
	 * Note: this is not meaningful by itselfis typically used from {@link StackFrameDMC#getFrameRegisters()}.
	 * @param context
	 * @param runtimeAddress
	 * @return {@link IFrameRegisters} or <code>null</code>
	 */
	public IFrameRegisterProvider getFrameRegisterProvider(ISymbolDMContext context, IAddress runtimeAddress) {
		Modules modulesService = getServicesTracker().getService(Modules.class);
		IEDCModuleDMContext module = modulesService.getModuleByAddress(context, runtimeAddress);
		if (module != null) {
			IEDCSymbolReader reader = module.getSymbolReader();
			if (reader != null) {
				IFrameRegisterProvider frameRegisterProvider = reader.getModuleScope().getFrameRegisterProvider();
				return frameRegisterProvider;
			}
		}
		return null;
	}

	public static IEDCSymbolReader getSymbolReader(IPath modulePath) {

		IEDCSymbolReader reader = null;
		WeakReference<IEDCSymbolReader> cacheEntry = readerCache.get(modulePath);
		
		if (cacheEntry != null)
			reader = cacheEntry.get();

		if (reader != null) {
			if (reader.getSymbolFile() != null
					&& reader.getSymbolFile().toFile().exists()
					&& reader.getSymbolFile().toFile().lastModified() == reader.getModificationDate()) {
				return reader;
			}

			// it's been deleted or modified. remove it from the cache
			readerCache.remove(modulePath);
		}

		IExecutableSymbolicsReader exeReader = ExecutableSymbolicsReaderFactory.createFor(modulePath);
		if (exeReader != null) {
			IDebugInfoProvider debugProvider = DebugInfoProviderFactory.createFor(modulePath, exeReader); // may be null
			if (debugProvider != null) {
				// if debug info came from a different file, the "executable" may be that symbol file too 
				if (!exeReader.getSymbolFile().equals(debugProvider.getExecutableSymbolicsReader().getSymbolFile())) {
					exeReader.dispose();
					exeReader = debugProvider.getExecutableSymbolicsReader();
				}
			}
			reader = new EDCSymbolReader(exeReader, debugProvider);
		}

		if (reader != null) {
			readerCache.put(modulePath, new WeakReference<IEDCSymbolReader>(reader));
		}
		
		return reader;
	}

	@Override
	public void shutdown(RequestMonitor rm) {
		super.shutdown(rm);
	}

	/**
	 * This is exposed only for testing.
	 */
	public static void releaseReaderCache() {
		Collection<WeakReference<IEDCSymbolReader>> readers = readerCache.values();
		for (WeakReference<IEDCSymbolReader> readerRef : readers) {
			IEDCSymbolReader reader = readerRef.get();
			if (reader != null)
				reader.shutDown();
		}
		readerCache.clear();
	}

	/**
	 * A wrapper method that calls into symbol reader to get runtime address(es)
	 * for a given function name. 
	 * Currently this method use symbol table instead of debug info (e.g. dwarf2)
	 * to get function address. See reason in the implementation.
	 * 
	 * @param module where the runtime address is.
	 * @param functionName
	 * @return list of runtime addresses.
	 */
	public List<IAddress> getFunctionAddress(IEDCModuleDMContext module, String functionName) {
		
		List<IAddress> ret = new ArrayList<IAddress>(2);
		
		IEDCSymbolReader symReader = module.getSymbolReader();
		if (symReader == null)
			return ret;
		
		// Remove "()" if any
		int parenIndex = functionName.indexOf('(');
		if (parenIndex >= 0)
			functionName = functionName.substring(0, parenIndex);

		// Supposedly we could call this to get what we need, but this may cause full parse of 
		// debug info and lead to heap overflow for a large symbol file (over one giga bytes of 
		// memory required in parsing a 180M symbol file) and chokes the debugger.
		// So before a good solution is available, we resort to symbol table of the executable.
		// ................04/02/10
//			Collection<IFunctionScope> functions = symReader.getModuleScope().getFunctionsByName(function);
//			for (IFunctionScope f : functions) {
//				IAddress breakAddr = f.getLowAddress();
//		        ...

		// assume it's the human-readable name first
		Collection<ISymbol> symbols = symReader.findUnmangledSymbols(functionName);
		if (symbols.isEmpty()) {
			// else look for a raw symbol
			symbols = symReader.findSymbols(functionName);
		}
		
		for (ISymbol symbol : symbols) {
			// don't consider zero sized symbol.
			if (symbol.getSize() ==  0) 
				continue;

			IAddress addr = symbol.getAddress();
			// convert from link to runtime address
			addr = module.toRuntimeAddress(addr);
			
			ret.add(addr);
		}

		return ret;
	}
	
}
