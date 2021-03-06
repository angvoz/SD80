/*
 * (c) Copyright IBM Corp. 2003.
 * Created on 27 f�vr. 03
 * All Rights Reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 *
 * Contributors:
 * IBM Software Group - Rational Software
 */

package org.eclipse.cdt.internal.cppunit.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * Dialog field describing a separator.
 */
//public class Separator extends DialogField {
public class Separator
{
	
	private Label fSeparator;
	private int fStyle;
	
	public Separator() {
		this(SWT.NONE);
	}	
	
	/**
	 * @param style of the separator. See <code>Label</code> for possible
	 * styles.
	 */
	public Separator(int style) {
		super();
		fStyle= style;
	}
			
	// ------- layout helpers

	/**
	 * Creates the separator and fills it in a MGridLayout.
	 * @param height The heigth of the separator
	 */		
	public Control[] doFillIntoGrid(Composite parent, int nColumns, int height) {
//		assertEnoughColumns(nColumns);
		
		Control separator= getSeparator(parent);
		separator.setLayoutData(gridDataForSeperator(nColumns, height));
		
		return new Control[] { separator };
	}

	/*
	 * @see DialogField#doFillIntoGrid
	 */	
	public Control[] doFillIntoGrid(Composite parent, int nColumns) {
		return doFillIntoGrid(parent, nColumns, 4);
	}

	/*
	 * @see DialogField#getNumberOfControls
	 */	
	public int getNumberOfControls() {
		return 1;	
	}
	
	protected static GridData gridDataForSeperator(int span, int height) {
		GridData gd= new GridData();
		gd.horizontalAlignment= GridData.FILL;
		gd.verticalAlignment= GridData.BEGINNING;
		gd.heightHint= height;		
		gd.horizontalSpan= span;
		return gd;
	}
	
	// ------- ui creation	

	/**
	 * Creates or returns the created separator.
	 * @param parent The parent composite or <code>null</code> if the widget has
	 * already been created.
	 */	
	public Control getSeparator(Composite parent) {
		if (fSeparator == null) {
			fSeparator= new Label(parent, fStyle);
		}	
		return fSeparator;
	}

}
