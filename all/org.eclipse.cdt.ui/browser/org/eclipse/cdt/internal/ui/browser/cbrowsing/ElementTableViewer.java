/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
/*
 * Created on Sep 1, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.cdt.internal.ui.browser.cbrowsing;

import org.eclipse.cdt.internal.ui.util.ProblemTableViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

/**
 * @author CWiebe
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ElementTableViewer extends ProblemTableViewer {

    /**
     * @param parent
     */
    public ElementTableViewer(Composite parent) {
        super(parent);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param parent
     * @param style
     */
    public ElementTableViewer(Composite parent, int style) {
        super(parent, style);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param table
     */
    public ElementTableViewer(Table table) {
        super(table);
        // TODO Auto-generated constructor stub
    }
    
	protected void handleInvalidSelection(ISelection invalidSelection,
			ISelection newSelection) {
		updateSelection(newSelection);
		SelectionChangedEvent event = new SelectionChangedEvent(this,
				newSelection);
		firePostSelectionChanged(event);
	}
    
}
