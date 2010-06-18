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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.edc.formatter.AbstractCompositeFormatProvider;
import org.eclipse.cdt.debug.edc.formatter.FormatUtils;
import org.eclipse.cdt.debug.edc.formatter.ITypeContentProvider;
import org.eclipse.cdt.debug.edc.formatter.IVariableFormatProvider;
import org.eclipse.cdt.debug.edc.formatter.IVariableValueConverter;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.services.IEDCExpression;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions2;
import org.eclipse.cdt.dsf.debug.service.IExpressions2.CastInfo;
import org.eclipse.core.runtime.CoreException;

public class QListFormatter implements IVariableFormatProvider {

	private static final String TYPE_NAME = "QList"; //$NON-NLS-1$

	public static class FormatProvider extends AbstractCompositeFormatProvider {

		private static final String BEGIN_NAME = "begin"; //$NON-NLS-1$
		private static final String BEGIN_PATH = "$unnamed$1.d->begin"; //$NON-NLS-1$
		private static final String END_NAME = "end"; //$NON-NLS-1$
		private static final String END_PATH = "$unnamed$1.d->end"; //$NON-NLS-1$
		private static final String ARRAY_NAME = "array"; //$NON-NLS-1$
		private static final String ARRAY_PATH = "$unnamed$1.d->array"; //$NON-NLS-1$
		private static final int BEGIN_CHILD_INDEX = 0;
		private static final int END_CHILD_INDEX = 1;
		private static final int ARRAY_CHILD_INDEX = 2;
		private static final String DETAIL_FMT = "size={0} {1}"; //$NON-NLS-1$
		private static final String DETAIL_EMPTY = "size=0"; //$NON-NLS-1$

		private static Map<String, String> nameToFieldPathMap;
		
		static {
			nameToFieldPathMap = new LinkedHashMap<String, String>();
			nameToFieldPathMap.put(BEGIN_NAME, BEGIN_PATH);
			nameToFieldPathMap.put(END_NAME, END_PATH);
			nameToFieldPathMap.put(ARRAY_NAME, ARRAY_PATH);
		}
		
		private boolean isEmpty;

		public FormatProvider(IType type, boolean forDetails) {
			super(type, forDetails, getNameToFieldPaths());
		}

		private static List<NameToFieldPath> getNameToFieldPaths() {
			List<NameToFieldPath> nameToFieldPaths = new ArrayList<NameToFieldPath>();
			nameToFieldPaths.add(new NameToFieldPath(BEGIN_NAME, BEGIN_PATH));
			nameToFieldPaths.add(new NameToFieldPath(END_NAME, END_PATH));
			nameToFieldPaths.add(new NameToFieldPath(ARRAY_NAME, ARRAY_PATH));
			return nameToFieldPaths;
		}
		
		@Override
		protected List<IExpressionDMContext> getChildren(IExpressionDMContext variable) throws CoreException {
			List<IExpressionDMContext> children = new ArrayList<IExpressionDMContext>();
			for (IExpressionDMContext child : super.getChildren(variable)) {
				String name = ((IEDCExpression) child).getName();
				if (nameToFieldPathMap.containsKey(name))
					children.add(child);
			}
			// cast the array child to right type and size
			IEDCExpression beginChild = (IEDCExpression) children.get(BEGIN_CHILD_INDEX);
			beginChild.evaluateExpression();
			int begin = beginChild.getEvaluatedValue().intValue();
			IEDCExpression endChild = (IEDCExpression) children.get(END_CHILD_INDEX);
			endChild.evaluateExpression();
			int end = endChild.getEvaluatedValue().intValue();
			int size = end - begin;
			if (size < 0 || (size > 0x1000000)) // sanity
				throw EDCDebugger.newCoreException("Uninitialized");
			IEDCExpression arrayChild = (IEDCExpression) children.get(ARRAY_CHILD_INDEX);
			IExpressions2 expressions2 = arrayChild.getServiceTracker().getService(IExpressions2.class);
			int numItems = Math.min(size, FormatUtils.getMaxNumberOfChildren());
			if (numItems == 0) {
				children.remove(ARRAY_CHILD_INDEX);
				isEmpty = true;
			}
			else {
				CastInfo castInfo = new CastInfo(FormatUtils.getTemplateTypeName(TYPE_NAME, type), begin, numItems);
				IExpressionDMContext castedArrayChild = expressions2.createCastedExpression(arrayChild, castInfo );
				children.set(ARRAY_CHILD_INDEX, castedArrayChild);
			}
			return children;
		}
		
		@Override
		public String getValue(IExpressionDMContext variable) throws CoreException {
			List<IExpressionDMContext> children = getChildren(variable);
			if (isEmpty)
				return DETAIL_EMPTY;
			IExpressionDMContext arrayChild = children.get(ARRAY_CHILD_INDEX);
			IVariableValueConverter valueConverter = FormatUtils.getCustomValueConverter(arrayChild);
			if (valueConverter != null) {
				IEDCExpression beginChild = (IEDCExpression) children.get(BEGIN_CHILD_INDEX);
				int begin = beginChild.getEvaluatedValue().intValue();
				IEDCExpression endChild = (IEDCExpression) children.get(END_CHILD_INDEX);
				int end = endChild.getEvaluatedValue().intValue();
				return MessageFormat.format(DETAIL_FMT, end - begin, valueConverter.getValue(arrayChild));
			}
			return ""; //$NON-NLS-1$
		}
	}

	public ITypeContentProvider getTypeContentProvider(IType type) {
		if (FormatUtils.checkClassOrInheritanceByName(type, TYPE_NAME)) {
			return new FormatProvider(type, false);
		}
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
