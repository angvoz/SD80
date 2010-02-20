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
package org.eclipse.cdt.debug.edc.formatter;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.InvalidExpression;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Expressions.ExpressionDMC;
import org.eclipse.cdt.debug.edc.internal.symbols.IType;
import org.eclipse.cdt.debug.edc.internal.symbols.TypeUtils;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.core.runtime.CoreException;

/**
 * Base class for string formatters.
 */
public abstract class AbstractStringFormatter implements IVariableFormatProvider {

	/**
	 * Converter handles char* and wchar_t* strings terminated by a null character.
	 */
	public class DefaultNullTerminatedStringConverter extends AbstractVariableConverter {
		public DefaultNullTerminatedStringConverter(IType type, boolean forDetails) {
			super(type, forDetails);
		}


		@Override
		protected String getDetailsValue(IExpressionDMContext variable) throws CoreException {
			return getValueString(variable);
		}

		@Override
		protected String getSummaryValue(IExpressionDMContext variable) throws CoreException {
			return getValueString(variable);
		}

		private String getValueString(IExpressionDMContext variable) throws CoreException {
			ExpressionDMC expressionDMC = (ExpressionDMC) variable;
			expressionDMC.evaluateExpression();
			IType baseType = TypeUtils.getBaseType(expressionDMC.getEvaluatedType());
			int size = baseType.getByteSize();
			
			Object value = expressionDMC.getEvaluatedValue();
			if (value == null)
				return null;
			if (value instanceof InvalidExpression)
				return ((InvalidExpression) value).getMessage();

			IAddress address = FormatUtils.getPointerValue(value);
			if (address == null) {
				return value.toString();		// dunno
			}
			
			// null pointer
			if (address.isZero())
				return "0";
			
			String formattedString = 
				FormatUtils.getFormattedNullTermString(variable, address, size, getMaximumLength());
			return quoteString(formattedString);
		}

		private String quoteString(String str) {
			StringBuilder sb = new StringBuilder("\"");
			sb.append(str);
			sb.append('\"');
			return sb.toString();
		}
	}

	/**
	 * Override to change the maximum string length to format.
	 * @return size in characters
	 */
	protected int getMaximumLength() {
		return 256;
	}
	
	public ITypeContentProvider getTypeContentProvider(IType type) {
		return null;
	}
}
