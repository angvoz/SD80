/*******************************************************************************
 * Copyright (c) 2001, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rational Software - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.util;

import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.dialogs.Dialog;

public class PixelConverter {
	
	private FontMetrics fFontMetrics;
	
	public PixelConverter(Control control) {
		GC gc = new GC(control);
		gc.setFont(control.getFont());
		fFontMetrics= gc.getFontMetrics();
		gc.dispose();
	}
	
	/**
	 * @see Dialog#convertHeightInCharsToPixels(FontMetrics, int)
	 */
	public int convertHeightInCharsToPixels(int chars) {
		return Dialog.convertHeightInCharsToPixels(fFontMetrics, chars);
	}

	/**
	 * @see Dialog#convertHorizontalDLUsToPixels(FontMetrics, int)
	 */
	public int convertHorizontalDLUsToPixels(int dlus) {
		return Dialog.convertHorizontalDLUsToPixels(fFontMetrics, dlus);
	}

	/**
	 * @see Dialog#convertVerticalDLUsToPixels(FontMetrics, int)
	 */
	public int convertVerticalDLUsToPixels(int dlus) {
		return Dialog.convertVerticalDLUsToPixels(fFontMetrics, dlus);
	}
	
	/**
	 * @see Dialog#convertWidthInCharsToPixels(FontMetrics, int)
	 */
	public int convertWidthInCharsToPixels(int chars) {
		return Dialog.convertWidthInCharsToPixels(fFontMetrics, chars);
	}	

}
