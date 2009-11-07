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
package org.eclipse.cdt.debug.edc.internal.ui.launch;

import org.eclipse.swt.widgets.Composite;

public class SnapshotTabComposite extends Composite {

	/**
	 * Create the composite
	 * 
	 * @param parent
	 * @param style
	 */
	public SnapshotTabComposite(Composite parent, int style) {
		super(parent, style);
		//
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
