/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.cview;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.jface.viewers.IElementComparer;

public class CViewElementComparer implements IElementComparer {

	public boolean equals(Object o1, Object o2) {
		if (o1 == o2)	// this handles also the case that both are null
			return true;
		if (o1 == null)  
			return false; // o2 != null if we reach this point 
		if (o1.equals(o2))
			return true;

		// Assume they are CElements
		ICElement c1= (o1 instanceof ICElement) ? (ICElement)o1 : null;
		ICElement c2= (o2 instanceof ICElement) ? (ICElement)o2 : null;
		if (c1 == null || c2 == null)
			return false;

		if (c1 instanceof ITranslationUnit) {
			ITranslationUnit t1 = (ITranslationUnit)o1;
			if (t1.isWorkingCopy()) {
				c1 = ((IWorkingCopy)t1).getOriginalElement();
			}
		}
		if (c2 instanceof ITranslationUnit) {
			ITranslationUnit t2 = (ITranslationUnit)o2;
			if (t2.isWorkingCopy()) {
				c2 = ((IWorkingCopy)t2).getOriginalElement(); 
			}
		}
		if (c1 == null || c2 == null) {
			return false;
		}
		return c1.equals(c2);
	}

	public int hashCode(Object o1) {
		ICElement c1= (o1 instanceof ICElement) ? (ICElement)o1 : null;
		if (c1 == null)
			return o1.hashCode();
		if (c1 instanceof ITranslationUnit) {
			ITranslationUnit t1= (ITranslationUnit)c1;
			if (t1.isWorkingCopy()) {
				c1= ((IWorkingCopy)t1).getOriginalElement();
			}
		}
		if (c1 == null) {
			return o1.hashCode();
		}
		return c1.hashCode();
	}
}
