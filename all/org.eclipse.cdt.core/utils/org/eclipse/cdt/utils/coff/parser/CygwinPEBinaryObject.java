/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others. All rights
 * reserved. This program and the accompanying materials are made available
 * under the terms of the Common Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - Initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.utils.coff.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.List;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.cdt.utils.Addr2line;
import org.eclipse.cdt.utils.Addr32;
import org.eclipse.cdt.utils.CPPFilt;
import org.eclipse.cdt.utils.CygPath;
import org.eclipse.cdt.utils.Objdump;
import org.eclipse.cdt.utils.coff.Coff;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/*
 * CygwinPEBinaryObject
 */
public class CygwinPEBinaryObject extends PEBinaryObject {

	private Addr2line addr2line;

	/**
	 * @param parser
	 * @param path
	 */
	public CygwinPEBinaryObject(CygwinPEParser parser, IPath path) {
		super(parser, path);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.utils.BinaryObjectAdapter#getAddr2line()
	 */
	protected Addr2line getAddr2line(boolean autodisposing) {
		if (!autodisposing) {
			CygwinPEParser parser = (CygwinPEParser) getBinaryParser();
			return parser.getAddr2line(getPath());
		}
		if (addr2line == null) {
			CygwinPEParser parser = (CygwinPEParser) getBinaryParser();
			addr2line = parser.getAddr2line(getPath());
			if (addr2line != null) {
				timestamp = System.currentTimeMillis();
				Runnable worker = new Runnable() {

					public void run() {
						long diff = System.currentTimeMillis() - timestamp;
						while (diff < 10000) {
							try {
								Thread.sleep(10000);
							} catch (InterruptedException e) {
								break;
							}
							diff = System.currentTimeMillis() - timestamp;
						}
						stopAddr2Line();
					}
				};
				new Thread(worker, "Addr2line Reaper").start(); //$NON-NLS-1$
			}
		} else {
			timestamp = System.currentTimeMillis();
		}
		return addr2line;
	}

	synchronized void stopAddr2Line() {
		if (addr2line != null) {
			addr2line.dispose();
		}
		addr2line = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.utils.BinaryObjectAdapter#getCPPFilt()
	 */
	protected CPPFilt getCPPFilt() {
		CygwinPEParser parser = (CygwinPEParser) getBinaryParser();
		return parser.getCPPFilt();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.utils.BinaryObjectAdapter#getObjdump()
	 */
	protected Objdump getObjdump() {
		CygwinPEParser parser = (CygwinPEParser) getBinaryParser();
		return parser.getObjdump(getPath());
	}

	/**
	 * @return
	 */
	protected CygPath getCygPath() {
		CygwinPEParser parser = (CygwinPEParser) getBinaryParser();
		return parser.getCygPath();
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryFile#getContents()
	 */
	public InputStream getContents() {
		InputStream stream = null;
		Objdump objdump = getObjdump();
		if (objdump != null) {
			try {
				byte[] contents = objdump.getOutput();
				stream = new ByteArrayInputStream(contents);
			} catch (IOException e) {
				// Nothing
			}
		}
		if (stream == null) {
			stream = super.getContents();
		}
		return stream;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.utils.coff.parser.PEBinaryObject#addSymbols(org.eclipse.cdt.utils.coff.Coff.Symbol[],
	 *      byte[], java.util.List)
	 */
	protected void addSymbols(Coff.Symbol[] peSyms, byte[] table, List list) {
		CPPFilt cppfilt = getCPPFilt();
		Addr2line addr2line = getAddr2line(false);
		CygPath cygpath = getCygPath();
		for (int i = 0; i < peSyms.length; i++) {
			if (peSyms[i].isFunction() || peSyms[i].isPointer() || peSyms[i].isArray()) {
				String name = peSyms[i].getName(table);
				if (name == null || name.trim().length() == 0 || !Character.isJavaIdentifierStart(name.charAt(0))) {
					continue;
				}
				int type = peSyms[i].isFunction() ? ISymbol.FUNCTION : ISymbol.VARIABLE;
				IAddress addr = new Addr32(peSyms[i].n_value);
				int size = 4;
				if (cppfilt != null) {
					try {
						name = cppfilt.getFunction(name);
					} catch (IOException e1) {
						cppfilt = null;
					}
				}
				if (addr2line != null) {
					try {
						String filename = addr2line.getFileName(addr);
						// Addr2line returns the funny "??" when it can not find
						// the file.
						if (filename != null && filename.equals("??")) { //$NON-NLS-1$
							filename = null;
						}

						if (filename != null) {
							if (cygpath != null) {
								filename = cygpath.getFileName(filename);
							}
						}
						IPath file = filename != null ? new Path(filename) : Path.EMPTY;
						int startLine = addr2line.getLineNumber(addr);
						int endLine = addr2line.getLineNumber(addr.add(BigInteger.valueOf(size - 1)));
						list.add(new CygwinSymbol(this, name, type, addr, size, file, startLine, endLine));
					} catch (IOException e) {
						addr2line = null;
						// the symbol still needs to be added
						list.add(new CygwinSymbol(this, name, type, addr, size));
					}
				} else {
					list.add(new CygwinSymbol(this, name, type, addr, size));
				}

			}
		}
		if (cppfilt != null) {
			cppfilt.dispose();
		}
		if (cygpath != null) {
			cygpath.dispose();
		}
		if (addr2line != null) {
			addr2line.dispose();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == Addr2line.class) {
			return getAddr2line(false);
		} else if (adapter == CPPFilt.class) {
			return getCPPFilt();
		} else if (adapter == CygPath.class) {
			return getCygPath();
		}
		return super.getAdapter(adapter);
	}
}