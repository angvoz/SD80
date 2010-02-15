/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Freescale Semiconductor - refactoring
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.disassembly.dsf;

import java.math.BigInteger;

import org.eclipse.jface.text.BadLocationException;

/**
 * DSF Disassembly view backends (CDI and DSF) need this limited access to the
 * editor/view Document.
 */
public interface IDisassemblyDocument {

	void addInvalidAddressRange(AddressRangePosition p);

	AddressRangePosition insertLabel(AddressRangePosition pos,
			BigInteger address, String label, boolean showLabels)
			throws BadLocationException;

	AddressRangePosition insertDisassemblyLine(AddressRangePosition p,
			BigInteger address, int intValue, String opCode, String string,
			String compilationPath, int lineNumber) throws BadLocationException;
}
