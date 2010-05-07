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

import java.io.File;
import java.io.Serializable;
import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.executables.Executable;
import org.eclipse.cdt.debug.core.executables.ExecutablesManager;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.PathUtils;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl.ExecutionDMC;
import org.eclipse.cdt.debug.edc.internal.snapshot.SnapshotUtils;
import org.eclipse.cdt.debug.edc.internal.symbols.ISection;
import org.eclipse.cdt.debug.edc.internal.symbols.Section;
import org.eclipse.cdt.debug.edc.internal.symbols.files.ExecutableSymbolicsReaderFactory;
import org.eclipse.cdt.debug.edc.services.AbstractEDCService;
import org.eclipse.cdt.debug.edc.services.DMContext;
import org.eclipse.cdt.debug.edc.services.IEDCDMContext;
import org.eclipse.cdt.debug.edc.services.IEDCExecutionDMC;
import org.eclipse.cdt.debug.edc.services.IEDCModuleDMContext;
import org.eclipse.cdt.debug.edc.services.IEDCModules;
import org.eclipse.cdt.debug.edc.snapshot.IAlbum;
import org.eclipse.cdt.debug.edc.snapshot.ISnapshotContributor;
import org.eclipse.cdt.debug.edc.symbols.IEDCSymbolReader;
import org.eclipse.cdt.debug.edc.tcf.extension.ProtocolConstants.IModuleProperty;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLookupDirector;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMEvent;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IModules;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Modules extends AbstractEDCService implements IModules, IEDCModules {

	public static final String MODULE = "module";
	public static final String SECTION = "section";
	private static final String ADDRESS_RANGE_CACHE = "_address_range";

	/**
	 * Modules that are loaded for each ISymbolDMContext (process).
	 */
	private final Map<String, List<ModuleDMC>> modules = Collections
			.synchronizedMap(new HashMap<String, List<ModuleDMC>>());

	private ISourceLocator sourceLocator;
	private static int nextModuleID = 100;

	public static class EDCAddressRange implements AddressRange, Serializable {
		
		private static final long serialVersionUID = -6475152211053407789L;
		private IAddress startAddr, endAddr;

		public EDCAddressRange(IAddress start, IAddress end) {
			startAddr = start;
			endAddr = end;
		}

		public IAddress getEndAddress() {
			return endAddr;
		}

		public void setEndAddress(IAddress address) {
			endAddr = address;
		}

		public IAddress getStartAddress() {
			return startAddr;
		}

		public void setStartAddress(IAddress address) {
			startAddr = address;
		}

		@Override
		public String toString() {
			return MessageFormat.format("[{0},{1})", startAddr.toHexAddressString(), endAddr.toHexAddressString());
		}
	}

	public class ModuleDMC extends DMContext implements IEDCModuleDMContext, ISnapshotContributor,
	// This means we'll install existing breakpoints
			// for each newly loaded module
			IBreakpointsTargetDMContext,
			// This means calcAddressInfo() also applies to single module
			// in addition to a process.
			ISymbolDMContext  {
		private final ISymbolDMContext symbolContext;

		private final IPath hostFilePath;
		private IEDCSymbolReader symReader;
		private final List<ISection> runtimeSections = new ArrayList<ISection>();

		public ModuleDMC(ISymbolDMContext symbolContext, Map<String, Object> props) {
			super(Modules.this, symbolContext == null ? new IDMContext[0] : new IDMContext[] { symbolContext }, Integer
					.toString(getNextModuleID()), props);
			this.symbolContext = symbolContext;

			String filename = "";
			if (props.containsKey(IModuleProperty.PROP_FILE))
				filename = (String) props.get(IModuleProperty.PROP_FILE);

			hostFilePath = locateModuleFileOnHost(filename);
		}

		public ISymbolDMContext getSymbolContext() {
			return symbolContext;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.services.IEDCModuleDMContext#getSymbolReader()
		 */
		public IEDCSymbolReader getSymbolReader() {
			return symReader;
		}

		public void loadSnapshot(Element element) throws Exception {
			NodeList sectionElements = element.getElementsByTagName(SECTION);

			int numSections = sectionElements.getLength();
			for (int i = 0; i < numSections; i++) {
				Element sectionElement = (Element) sectionElements.item(i);
				Element propElement = (Element) sectionElement.getElementsByTagName(SnapshotUtils.PROPERTIES).item(0);
				HashMap<String, Object> properties = new HashMap<String, Object>();
				SnapshotUtils.initializeFromXML(propElement, properties);

				IAddress linkAddress = new Addr64(sectionElement.getAttribute(ISection.PROPERTY_LINK_ADDRESS));
				int sectionID = Integer.parseInt(sectionElement.getAttribute(ISection.PROPERTY_ID));
				long size = Long.parseLong(sectionElement.getAttribute(ISection.PROPERTY_SIZE));

				Section section = new Section(sectionID, size, linkAddress, properties);
				section.relocate(new Addr64(sectionElement.getAttribute(ISection.PROPERTY_RUNTIME_ADDRESS)));
				runtimeSections.add(section);
			}

			initializeSymbolReader();
		}

		public Element takeShapshot(IAlbum album, Document document, IProgressMonitor monitor) {
			Element contextElement = document.createElement(MODULE);
			contextElement.setAttribute(PROP_ID, this.getID());
			Element propsElement = SnapshotUtils.makeXMLFromProperties(document, getProperties());
			contextElement.appendChild(propsElement);

			for (ISection s : runtimeSections) {
				Element sectionElement = document.createElement(SECTION);
				sectionElement.setAttribute(ISection.PROPERTY_ID, Integer.toString(s.getId()));
				sectionElement.setAttribute(ISection.PROPERTY_SIZE, Long.toString(s.getSize()));
				sectionElement.setAttribute(ISection.PROPERTY_LINK_ADDRESS, s.getLinkAddress().toHexAddressString());
				sectionElement.setAttribute(ISection.PROPERTY_RUNTIME_ADDRESS, s.getRuntimeAddress()
						.toHexAddressString());
				propsElement = SnapshotUtils.makeXMLFromProperties(document, s.getProperties());
				sectionElement.appendChild(propsElement);
				contextElement.appendChild(sectionElement);
			}

			if (!hostFilePath.isEmpty()) {
				album.addFile(hostFilePath);
				IPath possibleSymFile = ExecutableSymbolicsReaderFactory.findSymbolicsFile(hostFilePath);
				if (possibleSymFile != null) {
					album.addFile(possibleSymFile);
				}
			}

			return contextElement;
		}

		/**
		 * Relocate sections of the module. This should be called when the
		 * module is loaded.<br>
		 * <br>
		 * The relocation handling is target environment dependent.
		 * Implementation here has been tested for debug applications on
		 * Windows, Linux and Symbian. <br>
		 * 
		 * @param props
		 *            - runtime section properties from OS or from loader.
		 */
		public void relocateSections(Map<String, Object> props) {

			initializeSymbolReader();

			if (symReader != null) {
				runtimeSections.addAll(symReader.getSections());
			}

			if (props.containsKey(IModuleProperty.PROP_IMAGE_BASE_ADDRESS)) {
				// Windows module (PE file)
				//

				Object base = props.get(IModuleProperty.PROP_IMAGE_BASE_ADDRESS);
				IAddress imageBaseAddr = null;
				if (base != null) {
					if (base instanceof Integer)
						imageBaseAddr = new Addr64(base.toString());
					else if (base instanceof Long)
						imageBaseAddr = new Addr64(base.toString());
					else if (base instanceof String) // the string should be hex
						// string
						imageBaseAddr = new Addr64((String) base, 16);
					else
						EDCDebugger.getMessageLogger().logError(
								MessageFormat.format("Module property PROP_ADDRESS has invalid format {0}.", base
										.getClass()), null);
				}

				Number size = 0;
				if (props.containsKey(IModuleProperty.PROP_CODE_SIZE))
					size = (Number) props.get(IModuleProperty.PROP_CODE_SIZE);

				if (symReader != null) {
					// relocate
					//
					IAddress linkBase = symReader.getBaseLinkAddress();
					if (linkBase != null && !linkBase.equals(imageBaseAddr)) {
						BigInteger offset = linkBase.distanceTo(imageBaseAddr);
						for (ISection s : runtimeSections) {
							IAddress runtimeB = s.getLinkAddress().add(offset);
							s.relocate(runtimeB);
						}
					}
				} else { // fill in fake section data
					Map<String, Object> pp = new HashMap<String, Object>();
					pp.put(ISection.PROPERTY_NAME, ISection.NAME_TEXT);
					runtimeSections.add(new Section(0, size.longValue(), imageBaseAddr, pp));
				}
			} else if (props.containsKey(IModuleProperty.PROP_CODE_ADDRESS)) {
				// platforms other than Windows
				//
				Number codeAddr = null, dataAddr = null, bssAddr = null;
				Number codeSize = null, dataSize = null, bssSize = null;

				try {
					codeAddr = (Number) props.get(IModuleProperty.PROP_CODE_ADDRESS);
					dataAddr = (Number) props.get(IModuleProperty.PROP_DATA_ADDRESS);
					bssAddr = (Number) props.get(IModuleProperty.PROP_BSS_ADDRESS);
					codeSize = (Number) props.get(IModuleProperty.PROP_CODE_SIZE);
					dataSize = (Number) props.get(IModuleProperty.PROP_DATA_SIZE);
					bssSize = (Number) props.get(IModuleProperty.PROP_BSS_SIZE);
				} catch (ClassCastException e) {
					EDCDebugger.getMessageLogger().logError("Module property value has invalid format.", null);
				}

				if (symReader != null) {
					// Relocate.
					for (ISection s : runtimeSections) {
						if (s.getProperties().get(ISection.PROPERTY_NAME).equals(ISection.NAME_TEXT)
								&& codeAddr != null)
							s.relocate(new Addr64(codeAddr.toString()));
						else if (s.getProperties().get(ISection.PROPERTY_NAME).equals(ISection.NAME_DATA)
								&& dataAddr != null)
							s.relocate(new Addr64(dataAddr.toString()));
						else if (s.getProperties().get(ISection.PROPERTY_NAME).equals(ISection.NAME_BSS)
								&& bssAddr != null)
							s.relocate(new Addr64(bssAddr.toString()));
					}
				} else {
					// binary file not available.
					// fill in our fake sections. If no section size available,
					// don't bother.
					//
					Map<String, Object> pp = new HashMap<String, Object>();

					if (codeAddr != null && codeSize != null) {
						pp.put(ISection.PROPERTY_NAME, ISection.NAME_TEXT);
						runtimeSections.add(new Section(0, codeSize.intValue(), new Addr64(codeAddr.toString()), pp));
					}
					if (dataAddr != null && dataSize != null) {
						pp.clear();
						pp.put(ISection.PROPERTY_NAME, ISection.NAME_DATA);
						runtimeSections.add(new Section(0, dataSize.intValue(), new Addr64(dataAddr.toString()), pp));
					}
					if (bssAddr != null && bssSize != null) {
						pp.clear();
						pp.put(ISection.PROPERTY_NAME, ISection.NAME_BSS);
						runtimeSections.add(new Section(0, bssSize.intValue(), new Addr64(bssAddr.toString()), pp));
					}
				}
			} else {
				// No runtime address info available from target environment.
				// The runtime sections will just be the link-time sections.
				// 
				// This works well for the case where no relocation is needed
				// such as running the main executable (not DLLs nor shared
				// libs)
				// on Windows and Linux.
				// 
				// However, this may also indicate an error that the debug agent
				// (or even the target OS or loader) is not doing its job of
				// telling us the runtime address info.
			}
		}

		private void initializeSymbolReader() {
			if (hostFilePath.toFile().exists()) {
				symReader = Symbols.getSymbolReader(hostFilePath);
				if (symReader == null)
					EDCDebugger.getMessageLogger().log(IStatus.WARNING,
							MessageFormat.format("''{0}'' has no recognized file format.",
									hostFilePath), null);
				else if (! symReader.hasRecognizedDebugInformation()) {
					// Log as INFO, not ERROR.
					EDCDebugger.getMessageLogger().log(IStatus.INFO,
							MessageFormat.format("''{0}'' has no recognized symbolics.",
									hostFilePath), null);
				}
			} else {
				// Binary file not on host. Do we want to prompt user for one ?
			}
		}

		/**
		 * Check if a given runtime address falls in this module
		 * 
		 * @param absoluteAddr
		 *            - absolute runtime address.
		 * @return
		 */
		public boolean containsAddress(IAddress runtimeAddress) {
			for (ISection s : runtimeSections) {
				long offset = s.getRuntimeAddress().distanceTo(runtimeAddress).longValue();
				if (offset >= 0 && offset < s.getSize())
					return true;
			}

			return false;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.services.IEDCModuleDMContext#toLinkAddress(org.eclipse.cdt.core.IAddress)
		 */
		public IAddress toLinkAddress(IAddress runtimeAddress) {
			IAddress ret = null;

			for (ISection s : runtimeSections) {
				long offset = s.getRuntimeAddress().distanceTo(runtimeAddress).longValue();
				if (offset >= 0 && offset < s.getSize()) {
					return s.getLinkAddress().add(offset);
				}
			}

			return ret;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.internal.services.dsf.IEDCModuleDMContext#toRuntimeAddress(org.eclipse.cdt.core.IAddress)
		 */
		public IAddress toRuntimeAddress(IAddress linkAddress) {
			IAddress ret = null;

			for (ISection s : runtimeSections) {
				long offset = s.getLinkAddress().distanceTo(linkAddress).longValue();
				if (offset >= 0 && offset < s.getSize()) {
					return s.getRuntimeAddress().add(offset);
				}
			}

			return ret;
		}

		/**
		 * Get file name (without path) of the module.
		 * 
		 * @return
		 */
		public String getFile() {
			return hostFilePath.lastSegment();
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("\nModuleDMC [");
			if (hostFilePath != null) {
				builder.append("file=");
				builder.append(hostFilePath.lastSegment());
				builder.append(", ");
			}
			for (ISection s : runtimeSections) {
				builder.append("\n");
				builder.append(s);
			}
			return builder.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((hostFilePath == null) ? 0 : hostFilePath.hashCode());
			result = prime * result + ((symbolContext == null) ? 0 : symbolContext.hashCode());
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
			ModuleDMC other = (ModuleDMC) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (hostFilePath == null) {
				if (other.hostFilePath != null)
					return false;
			} else if (!hostFilePath.equals(other.hostFilePath))
				return false;
			if (symbolContext == null) {
				if (other.symbolContext != null)
					return false;
			} else if (!symbolContext.equals(other.symbolContext))
				return false;
			return true;
		}

		private IEDCModules getOuterType() {
			return Modules.this;
		}
	}

	static class ModuleDMData implements IModuleDMData {

		private final Map<String, Object> properties;

		public ModuleDMData(ModuleDMC dmc) {
			properties = dmc.getProperties();
		}

		public String getFile() {
			return (String) properties.get(IModuleProperty.PROP_FILE);
		}

		public String getName() {
			return (String) properties.get(IEDCDMContext.PROP_NAME);
		}

		public long getTimeStamp() {
			return 0;
			// return (String) properties.get(IModuleProperty.PROP_TIME);
		}

		public String getBaseAddress() {
			// return hex string representation.
			//
			Object baseAddress = properties.get(IModuleProperty.PROP_IMAGE_BASE_ADDRESS);
			if (baseAddress == null)
				baseAddress = properties.get(IModuleProperty.PROP_CODE_ADDRESS);

			if (baseAddress != null)
				return baseAddress.toString();
			else
				return "";
		}

		public String getToAddress() {
			// TODO this should return the end address, e.g. base + size
			return getBaseAddress();
		}

		public boolean isSymbolsLoaded() {
			return false;
		}

		public long getSize() {
			Number moduleSize = (Number) properties.get(IModuleProperty.PROP_CODE_SIZE);
			if (moduleSize != null)
				return moduleSize.longValue();
			else
				return 0;
		}

	}

	public static class ModuleLoadedEvent extends AbstractDMEvent<ISymbolDMContext> implements ModuleLoadedDMEvent {

		private final ModuleDMC module;
		private final IExecutionDMContext executionDMC;

		public ModuleLoadedEvent(ISymbolDMContext symbolContext, IExecutionDMContext executionDMC, ModuleDMC module) {
			super(symbolContext);
			this.module = module;
			this.executionDMC = executionDMC;
		}

		public IExecutionDMContext getExecutionDMC() {
			return executionDMC;
		}

		public IModuleDMContext getLoadedModuleContext() {
			return module;
		}

	}

	public static class ModuleUnloadedEvent extends AbstractDMEvent<ISymbolDMContext> implements ModuleUnloadedDMEvent {

		private final ModuleDMC module;
		private final ExecutionDMC executionDMC;

		public ModuleUnloadedEvent(ISymbolDMContext symbolContext, ExecutionDMC executionDMC, ModuleDMC module) {
			super(symbolContext);
			this.module = module;
			this.executionDMC = executionDMC;
		}

		public ExecutionDMC getExecutionDMC() {
			return executionDMC;
		}

		public IModuleDMContext getUnloadedModuleContext() {
			return module;
		}

	}

	public Modules(DsfSession session) {
		super(session, new String[] { IModules.class.getName(), IEDCModules.class.getName(), Modules.class.getName() });
	}

	public void setSourceLocator(ISourceLocator sourceLocator) {
		this.sourceLocator = sourceLocator;
	}

	public ISourceLocator getSourceLocator() {
		return sourceLocator;
	}

	private void addModule(ModuleDMC module) {
		ISymbolDMContext symContext = module.getSymbolContext();
		if (symContext instanceof IEDCDMContext) {
			String symContextID = ((IEDCDMContext) symContext).getID();
			synchronized (modules) {
				List<ModuleDMC> moduleList = modules.get(symContextID);
				if (moduleList == null) {
					moduleList = Collections.synchronizedList(new ArrayList<ModuleDMC>());
					modules.put(symContextID, moduleList);
				}
				moduleList.add(module);
			}
		}
	}

	private void removeModule(ModuleDMC module) {
		ISymbolDMContext symContext = module.getSymbolContext();
		if (symContext instanceof IEDCDMContext) {
			String symContextID = ((IEDCDMContext) symContext).getID();
			synchronized (modules) {
				List<ModuleDMC> moduleList = modules.get(symContextID);
				if (moduleList != null) {
					// other module attributes may not be passed during removal,
					// so remove the module with the same name
					for (ModuleDMC next : moduleList) {
						if (next.getFile().equals(module.getFile())) {
							moduleList.remove(next);
							break;
						}
					}
				}
			}
		}

	}

	/*
	 * The result AddressRange[] will contain absolute runtime addresses. And
	 * the "symCtx" can be a process or a module.
	 */
	@SuppressWarnings("unchecked")
	public void calcAddressInfo(ISymbolDMContext symCtx, String file, int line, int col,
			DataRequestMonitor<AddressRange[]> rm) {
		IModuleDMContext[] moduleList = null;

		if (symCtx instanceof IEDCExecutionDMC) {
			String symContextID = ((IEDCDMContext) symCtx).getID();
			moduleList = getModulesForContext(symContextID);
		} else if (symCtx instanceof IModuleDMContext) {
			moduleList = new IModuleDMContext[1];
			moduleList[0] = (IModuleDMContext) symCtx;
		} else {
			// should not happen
			assert false : "Unknown ISymbolDMContext class.";
			rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, REQUEST_FAILED, MessageFormat.format(
					"Unknown class implementing ISymbolDMContext : {0}", symCtx.getClass().getName()), null));
			rm.done();
			return;
		}

		List<EDCAddressRange> addrRanges = new ArrayList<EDCAddressRange>(1);
		
		for (IModuleDMContext module : moduleList) {
			ModuleDMC mdmc = (ModuleDMC) module;
			IEDCSymbolReader reader = mdmc.getSymbolReader();

			if (reader != null) {

				Collection<AddressRange> linkAddressRanges = null;
				Map<String, Collection<AddressRange>> cachedRanges = new HashMap<String, Collection<AddressRange>>();
				// Check the persistent cache
				String cacheKey = reader.getSymbolFile().toOSString() + ADDRESS_RANGE_CACHE;
				Object cachedData = EDCDebugger.getDefault().getCache().getCachedData(cacheKey, reader.getModificationDate());
				if (cachedData != null && cachedData instanceof Map<?,?>)
				{
					cachedRanges = (Map<String, Collection<AddressRange>>) cachedData;
					linkAddressRanges = cachedRanges.get(file + line);
				}
				
				if (linkAddressRanges == null)
				{
					linkAddressRanges = LineEntryMapper.getAddressRangesAtSource(  
						reader.getModuleScope().getModuleLineEntryProvider(),
						PathUtils.createPath(file),
						line);
					
					cachedRanges.put(file + line, linkAddressRanges);
					EDCDebugger.getDefault().getCache().putCachedData(cacheKey, (Serializable) cachedRanges, reader.getModificationDate());				
				}
								
				// map addresses
				for (AddressRange linkAddressRange : linkAddressRanges) {
					EDCAddressRange addrRange = new EDCAddressRange(
							mdmc.toRuntimeAddress(linkAddressRange.getStartAddress()),
							mdmc.toRuntimeAddress(linkAddressRange.getEndAddress()));
					addrRanges.add(addrRange);
				}
			}
		}

		if (addrRanges.size() > 0) {
			AddressRange[] ar = addrRanges.toArray(new AddressRange[addrRanges.size()]);
			rm.setData(ar);
		} else {
			/*
			 * we try to set the breakpoint for every module since we don't know
			 * which one the file is in. we report this error though if the file
			 * isn't in the module, and let the caller handle the error.
			 */
			rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, REQUEST_FAILED, MessageFormat.format(
					"Fail to find address for source line {0}: line# {1}", file, line), null));
		}

		rm.done();
	}

	public void calcLineInfo(ISymbolDMContext symCtx, IAddress address, DataRequestMonitor<LineInfo[]> rm) {
		// TODO Auto-generated method stub

	}

	/**
	 * Get runtime addresses mapped to given source line in given run context.
	 *  
	 * @param context
	 * @param sourceFile
	 * @param lineNumber
	 * @param drm If no address found, holds an empty list.
	 */
	public void getLineAddress(IExecutionDMContext context,
			String sourceFile, int lineNumber, final DataRequestMonitor<List<IAddress>> drm) {
		final List<IAddress> addrs = new ArrayList<IAddress>(1);
		
		final ExecutionDMC dmc = (ExecutionDMC) context;
		if (dmc == null) {
			drm.setData(addrs);
			drm.done();
			return;
		}
		
		ISymbolDMContext symCtx = DMContexts.getAncestorOfType(context, ISymbolDMContext.class);

		calcAddressInfo(symCtx, sourceFile, lineNumber, 0, 
				new DataRequestMonitor<AddressRange[]>(getExecutor(), drm) {

			@Override
			protected void handleCompleted() {
				if (! isSuccess()) {
					drm.setStatus(getStatus());
					drm.done();
					return;
				}

				AddressRange[] addr_ranges = getData();

				for (AddressRange range : addr_ranges) {
					IAddress a = range.getStartAddress();  // this is runtime address
					addrs.add(a);
				}

				drm.setData(addrs);
				drm.done();
			}
		});
	}
		
	public void getModuleData(IModuleDMContext dmc, DataRequestMonitor<IModuleDMData> rm) {
		rm.setData(new ModuleDMData((ModuleDMC) dmc));
		rm.done();
	}

	public void getModules(ISymbolDMContext symCtx, DataRequestMonitor<IModuleDMContext[]> rm) {
		String symContextID = ((IEDCDMContext) symCtx).getID();
		IModuleDMContext[] moduleList = getModulesForContext(symContextID);
		rm.setData(moduleList);
		rm.done();
	}

	public IModuleDMContext[] getModulesForContext(String symContextID) {
		synchronized (modules) {
			List<ModuleDMC> moduleList = modules.get(symContextID);
			if (moduleList == null)
				return new IModuleDMContext[0];
			else
				return moduleList.toArray(new IModuleDMContext[moduleList.size()]);
		}
	}

	private int getNextModuleID() {
		return nextModuleID++;
	}

	public void moduleLoaded(ISymbolDMContext symbolContext, ExecutionDMC executionDMC, Map<String, Object> moduleProps) {
		ModuleDMC module = new ModuleDMC(symbolContext, moduleProps);
		module.relocateSections(moduleProps);
		addModule(module);
		getSession().dispatchEvent(new ModuleLoadedEvent(symbolContext, executionDMC, module),
				Modules.this.getProperties());
	}

	public void moduleUnloaded(ISymbolDMContext symbolContext, ExecutionDMC executionDMC,
			Map<String, Object> moduleProps) {
		Object fileName = moduleProps.get(IEDCDMContext.PROP_NAME);
		ModuleDMC module = getModuleByName(symbolContext, fileName);
		if (module == null) {
			EDCDebugger.getMessageLogger().logError("Unexpected unload of module: " + fileName, null);
			return;
		}
		Object requireResumeValue = moduleProps.get("RequireResume");
		if (requireResumeValue != null && requireResumeValue instanceof Boolean)
			module.setProperty("RequireResume", requireResumeValue);
		removeModule(module);
		getSession().dispatchEvent(new ModuleUnloadedEvent(symbolContext, executionDMC, module),
				Modules.this.getProperties());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.services.dsf.IEDCModules#getModuleByAddress(org.eclipse.cdt.dsf.debug.service.IModules.ISymbolDMContext, org.eclipse.cdt.core.IAddress)
	 */
	public ModuleDMC getModuleByAddress(ISymbolDMContext symCtx, IAddress instructionAddress) {
		ModuleDMC bestMatch = null;
		synchronized (modules) {
			List<ModuleDMC> moduleList = modules.get(((IEDCDMContext) symCtx).getID());
			if (moduleList != null) {
				for (ModuleDMC moduleDMC : moduleList) {
					if (moduleDMC.containsAddress(instructionAddress)) {
						bestMatch = moduleDMC;
						break;
					}
				}

				if (bestMatch == null) {
					// TODO: add a bogus wrap-all module ?
				}
			}
		}
		return bestMatch;
	}

	/**
	 * Find the host file that corresponds to a given module file whose name
	 * comes from target platform.
	 * 
	 * @param originalPath
	 *            path or filename from target platform.
	 * @return the path to an existing file on host, null otherwise.
	 */
	public IPath locateModuleFileOnHost(String originalPath) {
		if (originalPath == null || originalPath.length() == 0)
			return Path.EMPTY;

		// Canonicalize path for the host OS, in hopes of finding a match directly on the host,
		// and for searching sources and executables below.
		//
		IPath path = PathUtils.findExistingPathIfCaseSensitive(PathUtils.createPath(originalPath));
		if (path.toFile().exists())
			return path;

		// Try source locator first, using the host-correct path.
		//
		Object sourceElement = null;
		ISourceLocator locator = getSourceLocator();
		if (locator != null) {
			if (locator instanceof ICSourceLocator || locator instanceof CSourceLookupDirector) {
				if (locator instanceof ICSourceLocator)
					sourceElement = ((ICSourceLocator) locator).findSourceElement(path.toOSString());
				else
					sourceElement = ((CSourceLookupDirector) locator).getSourceElement(path.toOSString());
			}
			if (sourceElement != null) {
				if (sourceElement instanceof LocalFileStorage) {
					return new Path(((LocalFileStorage) sourceElement).getFile().getAbsolutePath());
				}
			}
		}

		// Now looking for the file in executable view.
		//
		// Between the SDK and target, the exact directory and file capitalization may differ.
		//
		// Inject required initial slash so we can confidently use String#endsWith() without
		// matching, e.g. "/path/to/program.exe" with "ram.exe".
		//
		String slashAndLowerFileName = File.separator + path.lastSegment().toLowerCase();
		String absoluteLowerPath = path.makeAbsolute().toOSString().toLowerCase();
		
		Collection<Executable> executables = ExecutablesManager.getExecutablesManager().getExecutables(false);
		for (Executable e : executables) {
			String p = e.getPath().makeAbsolute().toOSString().toLowerCase();
			if (p.endsWith(absoluteLowerPath) || // stricter match first
				p.endsWith(slashAndLowerFileName)) // then only check by name
			{
				return e.getPath();
			}
		}

		return path;
	}

	public void loadModulesForContext(ISymbolDMContext context, Element element) throws Exception {

		List<ModuleDMC> contextModules = Collections.synchronizedList(new ArrayList<ModuleDMC>());

		NodeList moduleElements = element.getElementsByTagName(MODULE);

		int numModules = moduleElements.getLength();
		for (int i = 0; i < numModules; i++) {
			Element moduleElement = (Element) moduleElements.item(i);
			Element propElement = (Element) moduleElement.getElementsByTagName(SnapshotUtils.PROPERTIES).item(0);
			HashMap<String, Object> properties = new HashMap<String, Object>();
			SnapshotUtils.initializeFromXML(propElement, properties);

			ModuleDMC module = new ModuleDMC(context, properties);
			module.loadSnapshot(moduleElement);
			contextModules.add(module);

		}
		modules.put(((IEDCDMContext) context).getID(), contextModules);

	}
	
	/**
	 * get module with given file name
	 * 
	 * @param symCtx
	 * @param fileName
	 *            executable name for module
	 * @return null if not found.
	 */
	public ModuleDMC getModuleByName(ISymbolDMContext symCtx, Object fileName) {
		ModuleDMC module = null;
		synchronized (modules) {
			List<ModuleDMC> moduleList = modules.get(((IEDCDMContext) symCtx).getID());
			if (moduleList != null) {
				for (ModuleDMC moduleDMC : moduleList) {
					if ((moduleDMC.getName().compareToIgnoreCase((String) fileName)) == 0 ) {
						module = moduleDMC;
						break;
					}
				}
			}
		}
		return module;
	}

}
