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
package org.eclipse.cdt.debug.edc.formatter;

import java.util.List;

import org.eclipse.cdt.debug.edc.internal.symbols.ICompositeType;
import org.eclipse.cdt.debug.edc.services.IEDCExpression;
import org.eclipse.cdt.debug.edc.symbols.IMemoryVariableLocation;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.debug.edc.symbols.IVariableLocation;
import org.eclipse.cdt.debug.edc.symbols.TypeUtils;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.core.runtime.CoreException;

public class DefaultCompositeFormatter implements IVariableFormatProvider {

	public class DefaultCompositeConverter extends AbstractVariableConverter {
		
		private static final int MAX_DEPTH = 1; // stop recursing at this depth
		private static final int STOP_LENGTH = 300; // stop adding items when string is this length

		public DefaultCompositeConverter(IType type, boolean forDetails) {
			super(type, forDetails);
		}

		@Override
		protected String getSummaryValue(IExpressionDMContext variable) throws CoreException {
			StringBuilder sb = new StringBuilder();
			addVariableFields(null, sb, variable, 0);
			if (sb.length() == 0) {
				// if debug information does not include child information,
				// return the already evaluated value
				if (variable instanceof IEDCExpression) {
					IEDCExpression variableEDMC = (IEDCExpression)variable;
					return variableEDMC.getEvaluatedValueString();
				}
			}
			return sb.toString();
		}

		private boolean hasNullLocation(IExpressionDMContext variable) throws CoreException {
			if (variable instanceof IEDCExpression) {
				FormatUtils.evaluateExpression((IEDCExpression) variable);
				IVariableLocation loc = ((IEDCExpression) variable).getEvaluatedLocation();
				if (loc instanceof IMemoryVariableLocation) {
					if (((IMemoryVariableLocation) loc).getAddress().isZero()) {
						return true;
					}
				}
			}
			return false;
		}

		private void addVariableFields(String prefix, StringBuilder sb, IExpressionDMContext variable, int curDepth) throws CoreException {
			// if at null, don't try
			if (hasNullLocation(variable))
				return;
			
			if (prefix == null)
				prefix = ""; //$NON-NLS-1$
			List<IExpressionDMContext> childContexts = FormatUtils.getAllChildExpressions(variable);
			for (IExpressionDMContext child : childContexts) {
				IEDCExpression childExpression = (IEDCExpression) child;
				
				// if any child is at null, likely the struct is at null or crosses null, and is bad news
				if (hasNullLocation(childExpression)) {
					continue;
				}
					
				IVariableValueConverter customConverter = 
					FormatUtils.getCustomValueConverter(child);
				if (customConverter != null &&
						!(customConverter instanceof DefaultCompositeConverter)) {
					sb.append(prefix);
					sb.append(childExpression.getName());
					sb.append("="); //$NON-NLS-1$
					// for default array converter strings, don't surround in extra brackets
					boolean isDefaultArrayConverter = 
						customConverter instanceof DefaultArrayFormatter.DefaultArrayConverter;
					if (!isDefaultArrayConverter)
						sb.append("{");
					if (customConverter instanceof AbstractVariableConverter)
						((AbstractVariableConverter) customConverter).setCurValueLength(getCurValueLength() + sb.length());
					sb.append(customConverter.getValue(child));
					if (!isDefaultArrayConverter)
						sb.append("}");
					sb.append(" ");
				}
				else {
					IType evaluatedType = childExpression.getEvaluatedType();
					IType unqualifiedType = FormatUtils.getUnqualifiedTypeRemovePointers(evaluatedType);
					if (unqualifiedType instanceof ICompositeType) {
						unqualifiedType = TypeUtils.getStrippedType(evaluatedType);
						StringBuilder childPrefixSB = new StringBuilder(prefix);
						String name = childExpression.getName();
						if (name.startsWith("*")) {
							childPrefixSB.append('(');
							childPrefixSB.append(name);
							childPrefixSB.append(')');
						} else
							childPrefixSB.append(name);
						childPrefixSB.append(FormatUtils.getFieldAccessor(unqualifiedType));
						if (curDepth < MAX_DEPTH)
							addVariableFields(childPrefixSB.toString(), sb, child, ++curDepth);
						else {
							addSimpleChild(prefix, sb, childExpression);
						}
					}
					else {
						addSimpleChild(prefix, sb, childExpression);
					}
				}
				
				if (getCurValueLength() + sb.length() > STOP_LENGTH) {
					if (!childContexts.get(childContexts.size() - 1).equals(child))
						sb.append("... ");
					break;
				}
			}
		}

		private void addSimpleChild(String prefix, StringBuilder sb, IEDCExpression childExpression) {
			IExpressions expressions = childExpression.getServiceTracker().getService(IExpressions.class);
			if (expressions == null)
				return;
			
			sb.append(prefix);
			sb.append(childExpression.getName());
			sb.append("="); //$NON-NLS-1$
			sb.append(FormatUtils.getVariableValue(childExpression));
			sb.append(" "); //$NON-NLS-1$
		}

		@Override
		protected String getDetailsValue(IExpressionDMContext variable) throws CoreException {
			return getSummaryValue(variable);
		}
	}
	
	public static boolean handlesType(IType type) {
		IType unqualifiedType = FormatUtils.getUnqualifiedTypeRemovePointers(type);
		return unqualifiedType instanceof ICompositeType;
	}

	public ITypeContentProvider getTypeContentProvider(IType type) {
		return null;
	}
	
	public IVariableValueConverter getDetailValueConverter(IType type) {
		if (handlesType(type))
			return new DefaultCompositeConverter(type, true);
		
		return null;
	}

	public IVariableValueConverter getVariableValueConverter(IType type) {
		if (handlesType(type))
			return new DefaultCompositeConverter(type, false);
		
		return null;
	}

}
