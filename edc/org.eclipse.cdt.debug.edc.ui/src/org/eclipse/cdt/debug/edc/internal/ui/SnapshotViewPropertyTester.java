/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.ui;

import org.eclipse.cdt.debug.edc.internal.snapshot.Album;
import org.eclipse.cdt.debug.edc.internal.snapshot.Snapshot;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeNode;

/**
 * A property tester used to programmatically test the type of the selection in
 * the Snapshots view to see if it's an Album or a Snapshot. This can't be done
 * declaratively via 'instanceof' because the elements of the view are TreeNode
 * objects.
 * 
 * This property tester is referenced by using a 'test' element in the command
 * enablement expression with a property of
 * "org.eclipse.cdt.debug.edc.ui.isAlbumOrSnapshot".
 */
public class SnapshotViewPropertyTester extends PropertyTester {

	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (property.equals("isAlbumOrSnapshot")) {
			IStructuredSelection selection = (IStructuredSelection)receiver;
			Object selectionElement = selection.getFirstElement();
			if (selectionElement instanceof TreeNode) {
				TreeNode treeNode = (TreeNode)selectionElement;
				Object element = treeNode.getValue();
				return (element instanceof Album) || (element instanceof Snapshot);
			}
		}
		else {
			assert false : "unexpected property; check for a typo in the enablement expression";
		}
		return false;
	}

}
