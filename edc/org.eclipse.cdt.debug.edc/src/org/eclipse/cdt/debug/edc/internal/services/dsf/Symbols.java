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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Modules.ModuleDMC;
import org.eclipse.cdt.debug.edc.internal.symbols.IEDCSymbolReader;
import org.eclipse.cdt.debug.edc.internal.symbols.IFunctionScope;
import org.eclipse.cdt.debug.edc.internal.symbols.ILineEntry;
import org.eclipse.cdt.debug.edc.internal.symbols.IModuleLineEntryProvider;
import org.eclipse.cdt.debug.edc.internal.symbols.IModuleScope;
import org.eclipse.cdt.debug.edc.internal.symbols.IScope;
import org.eclipse.cdt.debug.edc.internal.symbols.dwarf.EDCSymbolReader;
import org.eclipse.cdt.debug.edc.internal.symbols.files.DebugInfoProviderFactory;
import org.eclipse.cdt.debug.edc.internal.symbols.files.ExecutableSymbolicsReaderFactory;
import org.eclipse.cdt.debug.edc.internal.symbols.files.IDebugInfoProvider;
import org.eclipse.cdt.debug.edc.internal.symbols.files.IExecutableSymbolicsReader;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.ISymbols;
import org.eclipse.cdt.dsf.debug.service.IModules.ISymbolDMContext;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.model.ISourceLocator;

public class Symbols extends AbstractEDCService implements ISymbols {

	/** TEMPORARY system property (value "true", default "true") for selecting the new on-demand DWARF reader */
	public static final String DWARF_USE_NEW_READER = "dwarf.use_new_reader";
	/** TEMPORARY system property (value "true", default "false") for selecting the old DWARF reader */
	public static final String DWARF_USE_OLD_READER = "dwarf.use_old_reader";
	
	private static Map<IPath, IEDCSymbolReader> readerCache = new HashMap<IPath, IEDCSymbolReader>();
	private ISourceLocator sourceLocator;
	
	public ISourceLocator getSourceLocator() {
		return sourceLocator;
	}

	public void setSourceLocator(ISourceLocator sourceLocator) {
		this.sourceLocator = sourceLocator;
	}

	public Symbols(DsfSession session) {
		super(session, new String[] { ISymbols.class.getName(), Symbols.class.getName() });
	}

	public void getSymbols(ISymbolDMContext symCtx, DataRequestMonitor<Iterable<ISymbolObjectDMContext>> rm) {
		// TODO Auto-generated method stub

	}

	public void getModelData(IDMContext dmc, DataRequestMonitor<?> rm) {
		// TODO Auto-generated method stub

	}

	/**
	 * Get the function at the given runtime address
	 * 
	 * @param context
	 *            the context
	 * @param runtimeAddress
	 *            the runtime address
	 * @return the function containing the given address, or null if none found
	 */
	public IFunctionScope getFunctionAtAddress(ISymbolDMContext context, IAddress runtimeAddress) {
		Modules modulesService = getServicesTracker().getService(Modules.class);
		ModuleDMC module = modulesService.getModuleByAddress(context, runtimeAddress);
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

	/**
	 * Get the line entry at the given runtime address
	 * 
	 * @param context
	 *            the context
	 * @param runtimeAddress
	 *            the runtime address
	 * @return the line entry for the given address, or null if none found
	 */
	public ILineEntry getLineEntryForAddress(ISymbolDMContext context, IAddress runtimeAddress) {
		Modules modulesService = getServicesTracker().getService(Modules.class);
		ModuleDMC module = modulesService.getModuleByAddress(context, runtimeAddress);
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

	/**
	 * <p>
	 * Get source line entries with code that are between the given start and
	 * end startAddress.
	 * <p>
	 * This method is created mainly for supporting disassembly service.
	 * 
	 * @param context
	 * @param start
	 *            start runtime address
	 * @param end
	 *            end runtime address (exclusive).
	 * @return list of source line entries which may or may not be in the same
	 *         source file (note that even one compile unit may have code from
	 *         different source files). It's empty if the start address has no
	 *         source line.
	 */
	public List<ILineEntry> getLineEntriesForAddressRange(ISymbolDMContext context, IAddress start, IAddress end) {
		List<ILineEntry> lineEntries = new ArrayList<ILineEntry>();

		Modules modulesService = getServicesTracker().getService(Modules.class);
		ModuleDMC module = modulesService.getModuleByAddress(context, start);
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

	public static IEDCSymbolReader getSymbolReader(IPath modulePath) {

		IEDCSymbolReader reader = readerCache.get(modulePath);

		if (reader != null) {
			if (reader.getSymbolFile() != null
					&& reader.getSymbolFile().toFile().exists()
					&& reader.getSymbolFile().toFile().lastModified() == reader.getModificationDate()) {
				return reader;
			}

			// it's been deleted or modified. remove it from the cache
			readerCache.remove(reader);
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
			readerCache.put(modulePath, reader);
		}
		
		return reader;
	}

	@Override
	public void shutdown(RequestMonitor rm) {
		// Shutdown all readers when this service is shutdown. This is overkill
		// and will cause problems when more then one debug sessions is running,
		// but is an temporary measure until we do some more work on the
		// readers.
		shutdown();

		super.shutdown(rm);
	}

	/**
	 * This is exposed only for testing.
	 */
	public static void shutdown() {
		Collection<IEDCSymbolReader> readers = readerCache.values();
		for (IEDCSymbolReader reader : readers) {
			reader.shutDown();
		}
		readerCache.clear();
	}
}
