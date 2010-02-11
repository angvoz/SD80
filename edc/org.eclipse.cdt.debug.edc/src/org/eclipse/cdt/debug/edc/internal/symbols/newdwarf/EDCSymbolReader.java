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
package org.eclipse.cdt.debug.edc.internal.symbols.newdwarf;

import java.nio.ByteOrder;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.internal.symbols.IEDCSymbolReader;
import org.eclipse.cdt.debug.edc.internal.symbols.IFunctionScope;
import org.eclipse.cdt.debug.edc.internal.symbols.IModuleScope;
import org.eclipse.cdt.debug.edc.internal.symbols.ISection;
import org.eclipse.cdt.debug.edc.internal.symbols.ISymbol;
import org.eclipse.cdt.debug.edc.internal.symbols.IVariable;
import org.eclipse.cdt.debug.edc.internal.symbols.files.IDebugInfoProvider;
import org.eclipse.cdt.debug.edc.internal.symbols.files.IExecutableSection;
import org.eclipse.cdt.debug.edc.internal.symbols.files.IExecutableSymbolicsReader;
import org.eclipse.core.runtime.IPath;

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

	public Collection<ISymbol> getSymbols() {
		return exeSymReader.getSymbols();
	}

	public ISymbol getSymbolAtAddress(IAddress linkAddress) {
		return exeSymReader.getSymbolAtAddress(linkAddress);
	}

	public IPath getSymbolFile() {
		return exeSymReader.getSymbolFile();
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

	public Collection<IFunctionScope> getFunctionsByName(String name) {
		if (debugInfoProvider != null)
			return debugInfoProvider.getFunctionsByName(name);
		else
			return Collections.emptyList();
	}

	public Collection<IVariable> getVariablesByName(String name) {
		if (debugInfoProvider != null)
			return debugInfoProvider.getVariablesByName(name);
		else
			return Collections.emptyList();
	}

	public String[] getSourceFiles() {
		if (debugInfoProvider != null)
			return debugInfoProvider.getSourceFiles();
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
