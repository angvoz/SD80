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
package org.eclipse.cdt.debug.edc.internal.symbols.dwarf;

import java.nio.ByteOrder;
import java.util.Collection;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.symbols.ISection;
import org.eclipse.cdt.debug.edc.symbols.IDebugInfoProvider;
import org.eclipse.cdt.debug.edc.symbols.IEDCSymbolReader;
import org.eclipse.cdt.debug.edc.symbols.IExecutableSection;
import org.eclipse.cdt.debug.edc.symbols.IExecutableSymbolicsReader;
import org.eclipse.cdt.debug.edc.symbols.IModuleScope;
import org.eclipse.cdt.debug.edc.symbols.ISymbol;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * This class handles the high-level retrieval of symbolic information, using 
 * {@link IDebugInfoProvider} and {@link IExecutableSymbolicsReader}.
 */
public class EDCSymbolReader implements IEDCSymbolReader {

	private static final String[] NO_SOURCE_FILES = new String[0];

	private static final IModuleScope EMPTY_MODULE_SCOPE = new DwarfModuleScope(null);
	
	private IDebugInfoProvider debugInfoProvider;
	private IExecutableSymbolicsReader exeSymReader;

	public EDCSymbolReader(IExecutableSymbolicsReader exeSymReader, IDebugInfoProvider debugInfoProvider) {
		if (exeSymReader == null)
			throw new IllegalArgumentException();
		
		this.exeSymReader = exeSymReader;
		this.debugInfoProvider = debugInfoProvider;
		
		// we expect these two files to be the same
		if (debugInfoProvider != null)
			if (!debugInfoProvider.getSymbolFile().equals(exeSymReader.getSymbolFile()))
				throw new IllegalArgumentException();
	}

	public void shutDown() {
		if (exeSymReader != null) {
			exeSymReader.dispose();
			exeSymReader = null;
		}
		if (debugInfoProvider != null) {
			debugInfoProvider.dispose();
			debugInfoProvider = null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.exe.IExecutableSymbolicsReader#dispose()
	 */
	public void dispose() {
		exeSymReader.dispose();
	}
	
	public Collection<IExecutableSection> getExecutableSections() {
		return exeSymReader.getExecutableSections();
	}
	
	public IExecutableSection findExecutableSection(String sectionName) {
		return exeSymReader.findExecutableSection(sectionName);
	}
	
	public Collection<ISection> getSections() {
		return exeSymReader.getSections();
	}

	public IAddress getBaseLinkAddress() {
		return exeSymReader.getBaseLinkAddress();
	}

	public long getModificationDate() {
		return exeSymReader.getModificationDate();
	}
	
	public Collection<ISymbol> findSymbols(String name) {
		return exeSymReader.findSymbols(name);
	}

	public Collection<ISymbol> findUnmangledSymbols(String name) {
		return exeSymReader.findUnmangledSymbols(name);
	}
	
	public Collection<ISymbol> getSymbols() {
		return exeSymReader.getSymbols();
	}

	public ISymbol getSymbolAtAddress(IAddress linkAddress) {
		return exeSymReader.getSymbolAtAddress(linkAddress);
	}

	public IPath getSymbolFile() {
		return debugInfoProvider != null ? debugInfoProvider.getSymbolFile() :
			(exeSymReader != null ? exeSymReader.getSymbolFile() : null);
	}

	public ByteOrder getByteOrder() {
		return exeSymReader.getByteOrder();
	}
	
	public IModuleScope getModuleScope() {
		if (debugInfoProvider != null)
			return debugInfoProvider.getModuleScope();
		else
			return EMPTY_MODULE_SCOPE;
	}

	public String[] getSourceFiles() {
		if (debugInfoProvider != null)
		{
			final String[][] resultHolder = new String[1][];
			Job quickParseJob = new Job("Reading Debug Symbol Information: " + debugInfoProvider.getSymbolFile().toOSString()) {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					String[] sourceFiles = getSourceFiles(monitor);
					resultHolder[0] = sourceFiles;
					return Status.OK_STATUS;
				}
			};
			
			try {
				quickParseJob.schedule();
				quickParseJob.join();
			} catch (InterruptedException e) {
				EDCDebugger.getMessageLogger().logError(null, e);
			}
			return resultHolder[0];
		}
		else
			return NO_SOURCE_FILES;
	}

	public String[] getSourceFiles(IProgressMonitor monitor) {
		if (debugInfoProvider != null)
			return debugInfoProvider.getSourceFiles(monitor);
		else
			return NO_SOURCE_FILES;
	}

	public boolean hasRecognizedDebugInformation() {
		return debugInfoProvider != null;
	}
	
	public IDebugInfoProvider getDebugInfoProvider() {
		return debugInfoProvider;
	}
}
