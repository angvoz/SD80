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
package org.eclipse.cdt.debug.edc.internal.ui;

import java.util.Collections;
import java.util.Set;

import org.eclipse.cdt.debug.edc.services.IEDCExpression;
import org.eclipse.debug.ui.IDetailPane;
import org.eclipse.debug.ui.IDetailPaneFactory;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * A factory for EDC specific detail panes
 */
public class EDCDetailPaneFactory implements IDetailPaneFactory {

	public IDetailPane createDetailPane(String paneID) {
        return new CustomFormatDetailPane();
	}

	public String getDefaultDetailPane(IStructuredSelection selection) {
		if (hasCustomProvider(selection))
			return CustomFormatDetailPane.ID;
		return null;
	}

	public String getDetailPaneDescription(String paneID) {
        if (paneID.equals(CustomFormatDetailPane.ID)){
            return CustomFormatDetailPane.DESCRIPTION;
        }
        return null;
	}

	public String getDetailPaneName(String paneID) {
        if (paneID.equals(CustomFormatDetailPane.ID)){
            return CustomFormatDetailPane.NAME;
        }
        return null;
	}
	
	public Set<String> getDetailPaneTypes(IStructuredSelection selection) {
		if (hasCustomProvider(selection))
			return Collections.singleton(CustomFormatDetailPane.ID);
		return Collections.emptySet();
	}

	private static boolean hasCustomProvider(IStructuredSelection selection) {
		if (selection.size() != 1)
			return false;
		
		IEDCExpression expressionDMC = 
			CustomFormatDetailPane.getExpressionFromSelectedElement(selection.getFirstElement());
		if (expressionDMC != null) {
			return CustomFormatDetailPane.getCustomConverter(expressionDMC) != null;
		}
		return false;
	}
	
	
}
