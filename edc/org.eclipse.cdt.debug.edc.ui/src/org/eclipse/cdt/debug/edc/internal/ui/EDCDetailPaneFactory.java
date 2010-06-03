/*******************************************************************************
 * Copyright (c) 2009, 2010 Nokia and others.
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
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.ui.IDetailPane;
import org.eclipse.debug.ui.IDetailPaneFactory;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * A factory for EDC specific detail panes
 */
public class EDCDetailPaneFactory implements IDetailPaneFactory {

	public IDetailPane createDetailPane(String paneID) {
		if (paneID.equals(CustomFormatDetailPane.ID)) {
			return new CustomFormatDetailPane();
		} else if (paneID.equals(EDCVariableDetailPane.ID)) {
			return new EDCVariableDetailPane();
		}
		return null;
	}

	public String getDefaultDetailPane(IStructuredSelection selection) {
		if (!selection.isEmpty()) {
			IEDCExpression expression = getExpressionFromSelectedElement(selection.getFirstElement());
			if (hasCustomProvider(expression))
				return CustomFormatDetailPane.ID;
			else if (expression != null)
				return EDCVariableDetailPane.ID;
		}
		return null;
	}

	public String getDetailPaneDescription(String paneID) {
		if (paneID.equals(CustomFormatDetailPane.ID)) {
			return CustomFormatDetailPane.DESCRIPTION;
		} else if (paneID.equals(EDCVariableDetailPane.ID)) {
			return EDCVariableDetailPane.DESCRIPTION;
		}
		return null;
	}

	public String getDetailPaneName(String paneID) {
		if (paneID.equals(CustomFormatDetailPane.ID)) {
			return CustomFormatDetailPane.NAME;
		} else if (paneID.equals(EDCVariableDetailPane.ID)) {
			return EDCVariableDetailPane.NAME;
		}
		return null;
	}

	public Set<String> getDetailPaneTypes(IStructuredSelection selection) {
		if (!selection.isEmpty()) {
			IEDCExpression expression = getExpressionFromSelectedElement(selection.getFirstElement());
			if (hasCustomProvider(expression))
				return Collections.singleton(CustomFormatDetailPane.ID);
			else if (expression != null)
				return Collections.singleton(EDCVariableDetailPane.ID);
		}
		return Collections.emptySet();
	}

	private static boolean hasCustomProvider(IEDCExpression expression) {
		if (expression != null) {
			return CustomFormatDetailPane.getCustomConverter(expression) != null;
		}
		return false;
	}
	
	public static IEDCExpression getExpressionFromSelectedElement(Object element) {
		if (element instanceof IAdaptable) {
			IExpressionDMContext expression = 
				(IExpressionDMContext) ((IAdaptable) element).getAdapter(IExpressionDMContext.class);
			if (expression instanceof IEDCExpression) {
				return (IEDCExpression) expression;
			}
		}
		return null;
	}
}
