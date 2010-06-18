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
package org.eclipse.cdt.debug.edc.internal.formatter.qt;

import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.debug.edc.formatter.AbstractCompositeFormatProvider;
import org.eclipse.cdt.debug.edc.formatter.FormatUtils;
import org.eclipse.cdt.debug.edc.formatter.ITypeContentProvider;
import org.eclipse.cdt.debug.edc.formatter.IVariableFormatProvider;
import org.eclipse.cdt.debug.edc.formatter.IVariableValueConverter;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.core.runtime.CoreException;

public class QByteArrayFormatter implements IVariableFormatProvider {

	private static final String TYPE_NAME = "QByteArray"; //$NON-NLS-1$

	public static class FormatProvider extends AbstractCompositeFormatProvider {

		private static final String DATA_NAME = "data"; //$NON-NLS-1$
		private static final String DATA_PATH = "d->data"; //$NON-NLS-1$

		public FormatProvider(IType type, boolean forDetails) {
			super(type, forDetails, getNameToFieldPaths());
		}

		private static List<NameToFieldPath> getNameToFieldPaths() {
			return Collections.singletonList(new NameToFieldPath(DATA_NAME, DATA_PATH));
		}
		
		@Override
		protected List<IExpressionDMContext> getChildren(IExpressionDMContext variable) throws CoreException {
			IExpressionDMContext dataChild = FormatUtils.findInCollectionByName(super.getChildren(variable), DATA_NAME);
			return Collections.singletonList(dataChild);
		}
		
		@Override
		public String getValue(IExpressionDMContext variable) throws CoreException {
			List<IExpressionDMContext> children = getChildren(variable);
			IExpressionDMContext dataChild = children.get(0);
			IVariableValueConverter valueConverter = FormatUtils.getCustomValueConverter(dataChild);
			if (valueConverter != null)
				return valueConverter.getValue(dataChild);
			return "";
		}
	}

	public ITypeContentProvider getTypeContentProvider(IType type) {
		// leave children unformatted
		return null;
	}

	public IVariableValueConverter getVariableValueConverter(IType type) {
		if (FormatUtils.checkClassOrInheritanceByName(type, TYPE_NAME)) {
			return new FormatProvider(type, false);
		}
		return null;
	}

	public IVariableValueConverter getDetailValueConverter(IType type) {
		if (FormatUtils.checkClassOrInheritanceByName(type, TYPE_NAME)) {
			return new FormatProvider(type, true);
		}
		return null;
	}
}
