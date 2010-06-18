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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.debug.edc.formatter.AbstractCompositeFormatProvider;
import org.eclipse.cdt.debug.edc.formatter.FormatUtils;
import org.eclipse.cdt.debug.edc.formatter.ITypeContentProvider;
import org.eclipse.cdt.debug.edc.formatter.IVariableFormatProvider;
import org.eclipse.cdt.debug.edc.formatter.IVariableValueConverter;
import org.eclipse.cdt.debug.edc.services.IEDCExpression;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions2;
import org.eclipse.cdt.dsf.debug.service.IExpressions2.CastInfo;
import org.eclipse.core.runtime.CoreException;

public class QVariantFormatter implements IVariableFormatProvider {

	private static final String TYPE_NAME = "QVariant"; //$NON-NLS-1$
	

	public static class FormatProvider extends AbstractCompositeFormatProvider {

		private static final String TYPE_NAME = "type"; //$NON-NLS-1$
		private static final String TYPE_PATH = "d.type"; //$NON-NLS-1$
		private static final String DATA_NAME = "data"; //$NON-NLS-1$
		private static final String DATA_PATH = "d.data"; //$NON-NLS-1$
		private static final String ISSHARED_NAME = "is_shared"; //$NON-NLS-1$
		private static final String ISSHARED_PATH = "d.is_shared"; //$NON-NLS-1$
		private static final String SHARED_DATA_NAME = "shared"; //$NON-NLS-1$
		private static final String SHARED_DATA_PATH = "d.data.shared->ptr"; //$NON-NLS-1$
		private static final int TYPE_CHILD_INDEX = 0;
		private static final int DATA_CHILD_INDEX = 1;
		private static final int ISSHARED_CHILD_INDEX = 2;
		private static final int SHARED_DATA_CHILD_INDEX = 3;
		
		private static Map<String, String> nameToFieldPathMap;
		private static Map<Integer, String> typeValueToNameMap;
		
		static {
			nameToFieldPathMap = new LinkedHashMap<String, String>();
			nameToFieldPathMap.put(TYPE_NAME, TYPE_PATH);
			nameToFieldPathMap.put(DATA_NAME, DATA_PATH);
			nameToFieldPathMap.put(ISSHARED_NAME, ISSHARED_PATH);
			nameToFieldPathMap.put(SHARED_DATA_NAME, SHARED_DATA_PATH);

			typeValueToNameMap = new HashMap<Integer, String>();
			typeValueToNameMap.put(1, "bool"); //$NON-NLS-1$
			typeValueToNameMap.put(2, "int"); //$NON-NLS-1$
			typeValueToNameMap.put(3, "uint"); //$NON-NLS-1$
			typeValueToNameMap.put(4, "qlonglong"); //$NON-NLS-1$
			typeValueToNameMap.put(5, "qulonglong"); //$NON-NLS-1$
			typeValueToNameMap.put(6, "double"); //$NON-NLS-1$
			typeValueToNameMap.put(7, "QChar"); //$NON-NLS-1$
			typeValueToNameMap.put(8, "QVariantMap"); //$NON-NLS-1$
			typeValueToNameMap.put(9, "QVariantList"); //$NON-NLS-1$
			typeValueToNameMap.put(10, "QString"); //$NON-NLS-1$
			typeValueToNameMap.put(11, "QStringList"); //$NON-NLS-1$
			typeValueToNameMap.put(12, "QByteArray"); //$NON-NLS-1$
			typeValueToNameMap.put(13, "QBitArray"); //$NON-NLS-1$
			typeValueToNameMap.put(14, "QDate"); //$NON-NLS-1$
			typeValueToNameMap.put(15, "QTime"); //$NON-NLS-1$
			typeValueToNameMap.put(16, "QDateTime"); //$NON-NLS-1$
			typeValueToNameMap.put(17, "QUrl"); //$NON-NLS-1$
			typeValueToNameMap.put(18, "QLocale"); //$NON-NLS-1$
			typeValueToNameMap.put(19, "QRect"); //$NON-NLS-1$
			typeValueToNameMap.put(20, "QRectF"); //$NON-NLS-1$
			typeValueToNameMap.put(21, "QSize"); //$NON-NLS-1$
			typeValueToNameMap.put(22, "QSizeF"); //$NON-NLS-1$
			typeValueToNameMap.put(23, "QLine"); //$NON-NLS-1$
			typeValueToNameMap.put(24, "QLineF"); //$NON-NLS-1$
			typeValueToNameMap.put(25, "QPoint"); //$NON-NLS-1$
			typeValueToNameMap.put(26, "QPointF"); //$NON-NLS-1$
			typeValueToNameMap.put(27, "QRegExp"); //$NON-NLS-1$
			typeValueToNameMap.put(28, "QVariantHash"); //$NON-NLS-1$
			typeValueToNameMap.put(135, "float"); //$NON-NLS-1$
			typeValueToNameMap.put(136, "QObject*"); //$NON-NLS-1$
		}
		
		private boolean isShared;

		public FormatProvider(IType type, boolean forDetails) {
			super(type, forDetails, getNameToFieldPaths());
		}

		private static List<NameToFieldPath> getNameToFieldPaths() {
			List<NameToFieldPath> nameToFieldPaths = new ArrayList<NameToFieldPath>();
			for (Entry<String, String> entry : nameToFieldPathMap.entrySet()) {
				nameToFieldPaths.add(new NameToFieldPath(entry.getKey(), entry.getValue()));
			}
			return nameToFieldPaths;
		}
		
		@Override
		protected List<IExpressionDMContext> getChildren(IExpressionDMContext variable) throws CoreException {
			List<IExpressionDMContext> children = new ArrayList<IExpressionDMContext>();
			List<IExpressionDMContext> childContexts = super.getChildren(variable);
			IEDCExpression typeChild = (IEDCExpression) childContexts.get(TYPE_CHILD_INDEX);
			typeChild.evaluateExpression();
			children.add(typeChild);
			IEDCExpression isSharedChild = (IEDCExpression) childContexts.get(ISSHARED_CHILD_INDEX);
			isSharedChild.evaluateExpression();
			Number isSharedValue = isSharedChild.getEvaluatedValue();
			isShared = isSharedValue.intValue() != 0;
			IEDCExpression dataChild = (IEDCExpression) childContexts.get(isShared ? SHARED_DATA_CHILD_INDEX : DATA_CHILD_INDEX);
			Number typeValue = typeChild.getEvaluatedValue();
			String typeString = typeValueToNameMap.get(typeValue.intValue());
			if (isShared)
				typeString += '*';
			if (typeString != null) {
				IExpressions2 expressions2 = dataChild.getServiceTracker().getService(IExpressions2.class);
				CastInfo castInfo = new CastInfo(typeString);
				dataChild = (IEDCExpression) expressions2.createCastedExpression(dataChild, castInfo);
			}
			dataChild.evaluateExpression();
			children.add(dataChild);
			return children;
		}
		
		@Override
		public String getValue(IExpressionDMContext variable) throws CoreException {
			List<IExpressionDMContext> children = getChildren(variable);
			IEDCExpression dataChild = (IEDCExpression) children.get(DATA_CHILD_INDEX);
			return FormatUtils.getFormattedValue(dataChild);
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
