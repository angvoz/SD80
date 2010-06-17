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
package org.eclipse.cdt.debug.edc.formatter;

import java.util.List;

import org.eclipse.cdt.debug.edc.internal.symbols.IArrayType;
import org.eclipse.cdt.debug.edc.services.IEDCExpression;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.core.runtime.CoreException;

public class DefaultArrayFormatter implements IVariableFormatProvider {

	public class DefaultArrayConverter extends AbstractVariableConverter {

		private static final int STOP_LENGTH = 300; // stop adding items when string is this length

		public DefaultArrayConverter(IType type, boolean forDetails) {
			super(type, forDetails);
		}

		@Override
		protected String getSummaryValue(IExpressionDMContext variable) throws CoreException {
			IExpressions expressions = ((IEDCExpression) variable).getServiceTracker().getService(IExpressions.class);
			if (expressions == null)
				return ""; //$NON-NLS-1$
			StringBuilder sb = new StringBuilder("[");
			List<IExpressionDMContext> children = FormatUtils.getAllChildExpressions(variable);
			boolean skip = true;
			for (IExpressionDMContext child : children) {
				if (skip)
					skip = false;
				else
					sb.append(", ");
				String customString = getCustomValueString(child);
				if (customString != null) {
					sb.append("{");
					sb.append(customString);
					sb.append("}");
				}
				else {
					IEDCExpression childExpression = (IEDCExpression) child;
					FormattedValueDMContext fvc = 
						expressions.getFormattedValueContext(childExpression, IExpressions.NATURAL_FORMAT);
					FormattedValueDMData formattedValue = childExpression.getFormattedValue(fvc);
					sb.append(formattedValue.getFormattedValue());
				}
				if (sb.length() > STOP_LENGTH) {
					if (!children.get(children.size() - 1).equals(child))
						sb.append(", ...");
					break;
				}
			}
			return sb.append("]").toString();
		}

		private String getCustomValueString(IExpressionDMContext variable) throws CoreException {
			IVariableValueConverter converter = FormatUtils.getCustomValueConverter(variable);
			if (converter != null)
				return converter.getValue(variable);
			return null;
		}

		@Override
		protected String getDetailsValue(IExpressionDMContext variable) throws CoreException {
			return getSummaryValue(variable);
		}
		
	}

	public static boolean handlesType(IType type) {
		IType unqualifiedType = FormatUtils.getUnqualifiedTypeRemovePointers(type);
		return unqualifiedType instanceof IArrayType;
	}

	public ITypeContentProvider getTypeContentProvider(IType type) {
		return null;
	}
	
	public IVariableValueConverter getDetailValueConverter(IType type) {
		if (handlesType(type))
			return new DefaultArrayConverter(type, true);
		return null;
	}

	public IVariableValueConverter getVariableValueConverter(IType type) {
		if (handlesType(type))
			return new DefaultArrayConverter(type, false);
		return null;
	}

}
