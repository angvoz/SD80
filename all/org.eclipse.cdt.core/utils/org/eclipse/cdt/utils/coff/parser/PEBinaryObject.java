/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.coff.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.cdt.utils.BinaryObjectAdapter;
import org.eclipse.cdt.utils.Symbol;
import org.eclipse.cdt.utils.coff.Coff;
import org.eclipse.cdt.utils.coff.PE;
import org.eclipse.core.runtime.IPath;

/**
 */
public class PEBinaryObject extends BinaryObjectAdapter {

	BinaryObjectInfo info;
	ISymbol[] symbols;

	public PEBinaryObject(IBinaryParser parser, IPath path) {
		super(parser, path);
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryFile#getType()
	 */
	public int getType() {
		return IBinaryFile.OBJECT;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#getSymbols()
	 */
	public ISymbol[] getSymbols() {
		if (hasChanged() || symbols == null) {
			try {
				loadAll();
			} catch (IOException e) {
				symbols = NO_SYMBOLS;
			}
		}
		return symbols;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.utils.BinaryObjectAdapter#getBinaryObjectInfo()
	 */
	protected BinaryObjectInfo getBinaryObjectInfo() {
		if (hasChanged() || info == null) {
			try {
				loadInfo();
			} catch (IOException e) {
				info = new BinaryObjectInfo();
			}
		}
		return info;
	}

	protected PE getPE() throws IOException {
		return new PE(getPath().toOSString());
	}

	protected void loadAll() throws IOException {
		PE pe = null;
		try {
			pe = getPE();
			loadInfo(pe);
			loadSymbols(pe);
		} finally {
			if (pe != null) {
				pe.dispose();
			}
		}
	}

	protected void loadInfo() throws IOException {
		PE pe = null;
		try {
			pe = getPE();
			loadInfo(pe);
		} finally {
			if (pe != null) {
				pe.dispose();
			}
		}
	}

	protected void loadInfo(PE pe) throws IOException {
		info = new BinaryObjectInfo();
		PE.Attribute attribute = getPE().getAttribute();
		info.isLittleEndian = attribute.isLittleEndian();
		info.hasDebug = attribute.hasDebug();
		info.cpu = attribute.getCPU();
	}

	protected void loadSymbols(PE pe) throws IOException {
		ArrayList list = new ArrayList();

		Coff.Symbol[] peSyms = pe.getSymbols();
		byte[] table = pe.getStringTable();
		addSymbols(peSyms, table, list);

		symbols = (ISymbol[]) list.toArray(NO_SYMBOLS);
		Arrays.sort(symbols);
		list.clear();
	}

	protected void addSymbols(Coff.Symbol[] peSyms, byte[] table, List list) {
		for (int i = 0; i < peSyms.length; i++) {
			if (peSyms[i].isFunction() || peSyms[i].isPointer() || peSyms[i].isArray()) {
				String name = peSyms[i].getName(table);
				if (name == null || name.trim().length() == 0 || !Character.isJavaIdentifierStart(name.charAt(0))) {
					continue;
				}
				int type = peSyms[i].isFunction() ? ISymbol.FUNCTION : ISymbol.VARIABLE;
				list.add(new Symbol(this, name, type, peSyms[i].n_value, 1));
			}
		}
	}
}