/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.utils.macho.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.utils.Addr2line;
import org.eclipse.cdt.utils.CPPFilt;
import org.eclipse.cdt.utils.Symbol;
import org.eclipse.cdt.utils.macho.AR;
import org.eclipse.cdt.utils.macho.MachO;
import org.eclipse.cdt.utils.macho.MachOHelper;
import org.eclipse.core.runtime.IPath;

/**
 * ARMember
 */
public class ARMember extends MachOBinaryObject {
	AR.ARHeader header;

	public ARMember(IBinaryParser parser, IPath p, AR.ARHeader h) throws IOException {
		super(parser, p);
		header = h;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryFile#getContents()
	 */
	public InputStream getContents() {
		InputStream stream = null;
		if (path != null && header != null) {
			try {
				stream = new ByteArrayInputStream(header.getObjectData());
			} catch (IOException e) {
			}
		}
		if (stream == null) {
			stream = super.getContents();
		}
		return stream;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#getName()
	 */
	public String getName() {
		if (header != null) {
			return header.getObjectName();
		}
		return ""; //$NON-NLS-1$
	}

	protected MachOHelper getMachOHelper() throws IOException {
		if (header != null) {
			return new MachOHelper(header.getMachO());
		}
		throw new IOException(CCorePlugin.getResourceString("Util.exception.noFileAssociation")); //$NON-NLS-1$
	}

	protected void addSymbols(MachO.Symbol[] array, int type, Addr2line addr2line, CPPFilt cppfilt, List list) {
		for (int i = 0; i < array.length; i++) {
			Symbol sym = new Symbol(this);
			sym.type = type;
			sym.name = array[i].toString();
			sym.addr = array[i].n_value;
			list.add(sym);
		}
	}

}
