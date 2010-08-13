/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.ui.views;

import java.util.Comparator;

import org.eclipse.swt.SWT;

public class SystemDMComparator implements Comparator<SystemDMContainer> {

	private String sortProperty;
	private int sortDirection;

	public SystemDMComparator(String sortProperty, int sortDirection) {
		this.setSortProperty(sortProperty);
		this.setSortDirection(sortDirection);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public int compare(SystemDMContainer arg0, SystemDMContainer arg1) {
		Object a = arg0.getProperties().get(getSortProperty());
		Object b = arg1.getProperties().get(getSortProperty());
		
		if (a instanceof String && b instanceof String)
		{
			a = ((String)a).toLowerCase();
			b = ((String)b).toLowerCase();
		}
		
		int result = 0;
		
		if (a instanceof Comparable<?> && b instanceof Comparable<?>)
			result = ((Comparable)a).compareTo((Comparable)b);
		
		if (getSortDirection() == SWT.UP)
			result = -result;
		
		return result;
	}

	public void setSortProperty(String sortProperty) {
		this.sortProperty = sortProperty;
	}

	public String getSortProperty() {
		return sortProperty;
	}

	public void setSortDirection(int sortDirection) {
		this.sortDirection = sortDirection;
	}

	public int getSortDirection() {
		return sortDirection;
	}

}
