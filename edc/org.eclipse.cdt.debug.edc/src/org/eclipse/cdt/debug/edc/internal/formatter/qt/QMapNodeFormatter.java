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

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.cdt.debug.edc.formatter.AbstractVariableConverter;
import org.eclipse.cdt.debug.edc.formatter.FormatUtils;
import org.eclipse.cdt.debug.edc.formatter.ITypeContentProvider;
import org.eclipse.cdt.debug.edc.formatter.IVariableFormatProvider;
import org.eclipse.cdt.debug.edc.formatter.IVariableValueConverter;
import org.eclipse.cdt.debug.edc.services.IEDCExpression;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.core.runtime.CoreException;

public class QMapNodeFormatter implements IVariableFormatProvider {

	private static final String TYPE_NAME = "QMapNode"; //$NON-NLS-1$

	public static class FormatProvider extends AbstractVariableConverter {
		private static final int KEY_CHILD_INDEX = 0;
		private static final int VALUE_CHILD_INDEX = 1;
		private static final String DETAIL_FMT = "{0} : {1}";

		public FormatProvider(IType type, boolean forDetails) {
			super(type, forDetails);
		}

		@Override
		protected String getDetailsValue(IExpressionDMContext variable) throws CoreException {
			return getSummaryValue(variable);
		}

		@Override
		protected String getSummaryValue(IExpressionDMContext variable) throws CoreException {
			List<IExpressionDMContext> children = FormatUtils.getAllChildExpressions(variable);
			IEDCExpression keyExpression = (IEDCExpression) children.get(KEY_CHILD_INDEX);
			String key = FormatUtils.getFormattedValue(keyExpression);
			IEDCExpression valueExpression = (IEDCExpression) children.get(VALUE_CHILD_INDEX);
			String value = FormatUtils.getFormattedValue(valueExpression);
			return MessageFormat.format(DETAIL_FMT, key, value);
		}
	}

	public ITypeContentProvider getTypeContentProvider(IType type) {
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
