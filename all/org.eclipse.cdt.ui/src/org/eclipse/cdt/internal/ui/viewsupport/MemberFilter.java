/*******************************************************************************
 * Copyright (c) 2001 Rational Software Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     Rational Software - initial implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.viewsupport;

import org.eclipse.cdt.core.model.IDeclaration;
import org.eclipse.cdt.core.model.IField;
import org.eclipse.cdt.core.model.IMember;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;


/**
 * Filter for the methods viewer.
 * Changing a filter property does not trigger a refiltering of the viewer
 */

public class MemberFilter extends ViewerFilter{

	public static final int FILTER_NONPUBLIC= 1;
	public static final int FILTER_STATIC= 2;
	public static final int FILTER_FIELDS= 4;
	
	private int fFilterProperties;

	/**
	 * Modifies filter and add a property to filter for
	 */
	public final void addFilter(int filter) {
		fFilterProperties |= filter;
	}
	/**
	 * Modifies filter and remove a property to filter for
	 */	
	public final void removeFilter(int filter) {
		fFilterProperties &= (-1 ^ filter);
	}
	/**
	 * Tests if a property is filtered
	 */		
	public final boolean hasFilter(int filter) {
		return (fFilterProperties & filter) != 0;
	}
	
	/*
	 * @see ViewerFilter@isFilterProperty
	 */
	public boolean isFilterProperty(Object element, Object property) {
		return false;
	}
	/*
	 * @see ViewerFilter@select
	 */		
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if(element instanceof IDeclaration){
			IDeclaration declaration = (IDeclaration) element;
			if (hasFilter(FILTER_STATIC) && (declaration.isStatic()) ) {
				return false;
			}
			if (element instanceof IMember) {
				IMember member= (IMember)element;
				if (hasFilter(FILTER_NONPUBLIC) && (member.getVisibility() != IMember.V_PUBLIC)) {
					return false;
				}
				
				if (hasFilter(FILTER_FIELDS) && element instanceof IField) {
					return false;
				}					
			}
		}
		return true;
	}
/*	
	private boolean isMemberInInterface(IMember member) throws JavaModelException {
		IType parent= member.getDeclaringType();
		return parent != null && parent.isInterface();
	}
	
	private boolean isFieldInInterface(IMember member) throws JavaModelException {
		return (member.getElementType() == IJavaElement.FIELD) && member.getDeclaringType().isInterface();
	}	
	
	private boolean isTopLevelType(IMember member) throws JavaModelException {
		IType parent= member.getDeclaringType();
		return parent == null;
	}		
*/
}
