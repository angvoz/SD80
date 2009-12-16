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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Modules.ModuleDMC;
import org.eclipse.cdt.debug.edc.internal.symbols.ICompileUnitScope;
import org.eclipse.cdt.debug.edc.internal.symbols.IEDCSymbolReader;
import org.eclipse.cdt.debug.edc.internal.symbols.IFunctionScope;
import org.eclipse.cdt.debug.edc.internal.symbols.ILineEntry;
import org.eclipse.cdt.debug.edc.internal.symbols.IScope;
import org.eclipse.cdt.debug.edc.internal.symbols.dwarf.EDCDwarfReader;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.ISymbols;
import org.eclipse.cdt.dsf.debug.service.IModules.ISymbolDMContext;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.model.ISourceLocator;

public class Symbols extends AbstractEDCService implements ISymbols {

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
				IScope scope = reader.getScopeAtAddress(module.toLinkAddress(runtimeAddress));
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
				ICompileUnitScope scope = reader.getCompileUnitForAddress(linkAddress);
				if (scope != null) {
					return scope.getLineEntryAtAddress(linkAddress);
				}
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
			ICompileUnitScope scope = reader.getCompileUnitForAddress(linkStartAddress);
			if (scope != null) {
				if (linkEndAddress.compareTo(reader.getHighAddress()) > 0) {
					// end address is out of the module sections.
					// we'll keep getting source lines until we reach an address
					// point where no source line is available.
					linkEndAddress = reader.getHighAddress();
				}

				ILineEntry entry = scope.getLineEntryAtAddress(linkStartAddress);
				while (entry != null && entry.getLowAddress().compareTo(linkEndAddress) < 0) {
					lineEntries.add(entry);
					entry = scope.getLineEntryAtAddress(entry.getHighAddress());
				}
			}
		}

		return lineEntries;
	}

	public static IEDCSymbolReader getSymbolReader(IPath modulePath) {

		IEDCSymbolReader reader = readerCache.get(modulePath);

		if (reader != null) {
			if (reader.getSymbolFile().toFile().exists()
					&& reader.getSymbolFile().toFile().lastModified() == reader.getModificationDate())
				return reader;

			// it's been deleted or modified. remove it from the cache
			readerCache.remove(reader);
		}

		try {
			// if this throws then it's not PE or ELF
			reader = new EDCDwarfReader(modulePath);
		} catch (IOException e) {
			EDCDebugger.getMessageLogger().logError(null, e);
		}

		if (reader == null || ! reader.hasRecognizedDebugInformation()) {
			// TODO try other symbol readers here as they get added
		}

		if (reader != null)
			readerCache.put(modulePath, reader);
		
		return reader;
	}

	@Override
	public void shutdown(RequestMonitor rm) {
		// Shutdown all readers when this service is shutdown. This is overkill
		// and will cause problems when more then one debug sessions is running,
		// but is an temporary measure until we do some more work on the
		// readers.
		Collection<IEDCSymbolReader> readers = readerCache.values();
		for (IEDCSymbolReader reader : readers) {
			reader.shutDown();
		}

		super.shutdown(rm);
	}

}
