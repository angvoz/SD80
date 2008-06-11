/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui.compare;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;


import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.IViewerCreator;


import org.eclipse.jface.viewers.Viewer;


/**
 * Required when creating a MakefileMergeViewer from the plugin.xml file.
 */
public class MakefileContentViewerCreator implements IViewerCreator {
	
	public Viewer createViewer(Composite parent, CompareConfiguration mp) {
		return new MakefileMergeViewer(parent, SWT.NULL, mp);
	}
}