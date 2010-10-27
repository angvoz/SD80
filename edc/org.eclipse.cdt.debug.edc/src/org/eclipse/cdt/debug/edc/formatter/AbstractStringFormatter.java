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
import org.eclipse.cdt.debug.edc.internal.symbols.IArrayType;
import org.eclipse.cdt.debug.edc.services.IEDCExpression;
import org.eclipse.cdt.debug.edc.symbols.IMemoryVariableLocation;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.debug.edc.symbols.IVariableLocation;
import org.eclipse.cdt.debug.edc.symbols.TypeUtils;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

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
			IEDCExpression expressionDMC = (IEDCExpression) variable;
			expressionDMC.evaluateExpression();
			IType baseType = TypeUtils.getBaseType(expressionDMC.getEvaluatedType());
			int size = baseType.getByteSize();
			
			if (expressionDMC.getEvaluationError() != null)
				return expressionDMC.getEvaluationError().getMessage();
			
			// pointer living at null is not valid (e.g. inside a struct pointing to null)
			IVariableLocation exprLoc = expressionDMC.getEvaluatedLocation();
			if (exprLoc instanceof IMemoryVariableLocation && ((IMemoryVariableLocation) exprLoc).getAddress().isZero())
				return "0";

			int maximumLength = getMaximumLength();
			// limit to express char[] size if given
			if (expressionDMC.getEvaluatedType() instanceof IArrayType) {
				long boundLength = ((IArrayType)expressionDMC.getEvaluatedType()).getBound(0).getBoundCount();
				if (boundLength > 0) {
					maximumLength = (int) Math.min(boundLength, maximumLength);
				}
			}
			
			Number value = expressionDMC.getEvaluatedValue();
			if (value == null) {
				if (expressionDMC.getEvaluatedValueString() != null) {
					// already a string
					// TODO: proper formatting
					return '"' + expressionDMC.getEvaluatedValueString() + '"';
				}
				return null;
			}

			IAddress address = FormatUtils.getPointerValue(value);
			if (address == null) {
				return value.toString();		// dunno
			}
			
			// null pointer
			if (address.isZero())
				return "0";
			
			String formattedString = 
				FormatUtils.getFormattedNullTermString(variable, address, size, maximumLength);
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
		IEclipsePreferences scope = new InstanceScope().getNode("org.eclipse.debug.ui");
		return scope.getInt("org.eclipse.debug.ui.max_detail_length", 256);
	}
	
	public ITypeContentProvider getTypeContentProvider(IType type) {
		return null;
	}
}
