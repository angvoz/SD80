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
import java.util.Map.Entry;

import org.eclipse.cdt.debug.edc.formatter.AbstractCompositeFormatProvider;
import org.eclipse.cdt.debug.edc.formatter.FormatUtils;
import org.eclipse.cdt.debug.edc.formatter.ITypeContentProvider;
import org.eclipse.cdt.debug.edc.formatter.IVariableFormatProvider;
import org.eclipse.cdt.debug.edc.formatter.IVariableValueConverter;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.services.IEDCExpression;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.core.runtime.CoreException;

public class QLinkedListFormatter implements IVariableFormatProvider {
	
	private static final String TYPE_NAME = "QLinkedList"; //$NON-NLS-1$

	public static class FormatProvider extends AbstractCompositeFormatProvider {
		private static final String SIZE_NAME = "size"; //$NON-NLS-1$
		private static final String SIZE_PATH = "$unnamed$1.d->size"; //$NON-NLS-1$
		private static final String HEAD_NAME = "head"; //$NON-NLS-1$
		private static final String HEAD_PATH = "$unnamed$1.e"; //$NON-NLS-1$
		private static final int SIZE_CHILD_INDEX = 0;
		private static final int HEAD_CHILD_INDEX = 1;
		private static final int NODE_NEXT_CHILD_INDEX = 0;
		private static final int NODE_ELEMENT_CHILD_INDEX = 2;
		
		private static final String NODE_CAST_FMT = 
			"reinterpret_cast<QLinkedListNode<{0}>*>({1})"; //$NON-NLS-1$
		
		private static final String DETAIL_SIZE_FMT = "size={0}"; //$NON-NLS-1$
		private static final String DETAIL_FMT = "size={0} {1}"; //$NON-NLS-1$
		private static final int STOP_LENGTH = 300;

		private static Map<String, String> nameToFieldPathMap;
		static {
			nameToFieldPathMap = new LinkedHashMap<String, String>();
			nameToFieldPathMap.put(SIZE_NAME, SIZE_PATH);
			nameToFieldPathMap.put(HEAD_NAME, HEAD_PATH);
		}
		
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
		public int getChildCount(IExpressionDMContext variable) throws CoreException {
			List<IExpressionDMContext> childContexts = super.getChildren(variable);
			IEDCExpression sizeChild = (IEDCExpression) childContexts.get(SIZE_CHILD_INDEX);
			FormatUtils.evaluateExpression(sizeChild);
			
			int size = sizeChild.getEvaluatedValue().intValue();
			if (size < 0 || size > 0x1000000) // sanity
				throw EDCDebugger.newCoreException("Uninitialized");
			return size;
		}
		
		@Override
		protected List<IExpressionDMContext> getChildren(IExpressionDMContext variable) throws CoreException {
			List<IExpressionDMContext> children = new ArrayList<IExpressions.IExpressionDMContext>();
			List<IExpressionDMContext> childContexts = super.getChildren(variable);
			IEDCExpression sizeChild = (IEDCExpression) childContexts.get(SIZE_CHILD_INDEX);
			FormatUtils.evaluateExpression(sizeChild);
			children.add(sizeChild);
			
			int size = sizeChild.getEvaluatedValue().intValue();
			if (size < 0 || size > 0x1000000) // sanity
				throw EDCDebugger.newCoreException("Uninitialized");
			if (size > 0) {
				IExpressions expressions = sizeChild.getServiceTracker().getService(IExpressions.class);
				children.addAll(getElementsFromListHead(expressions, (IEDCExpression) childContexts.get(HEAD_CHILD_INDEX)));
			}
			
			return children;
		}

		private List<IEDCExpression> getElementsFromListHead(IExpressions expressions, IEDCExpression listHead) throws CoreException {
			FormatUtils.evaluateExpression(listHead);
			int listHeadValue = listHead.getEvaluatedValue().intValue();
			IFrameDMContext frame = ((IEDCExpression) listHead).getFrame();
			String templateTypeName = FormatUtils.getTemplateTypeName(TYPE_NAME, type);
			List<IEDCExpression> elements = new ArrayList<IEDCExpression>();
			IEDCExpression nextNode = getNextNode(expressions, frame, templateTypeName, listHead);
			while (nextNode.getEvaluatedValue().intValue() != listHeadValue) {
				List<IExpressionDMContext> nodeChildren = FormatUtils.getAllChildExpressions(nextNode);
				IEDCExpression elementChild = (IEDCExpression) nodeChildren.get(NODE_ELEMENT_CHILD_INDEX);
				// make a copy so they have unique ids
				IEDCExpression copy = (IEDCExpression) expressions.createExpression(frame, elementChild.getExpression());
				copy.setName("" + (elements.size() + 1)); //$NON-NLS-1$
				elements.add(copy);
				nextNode = getNextNode(expressions, frame, templateTypeName, nextNode);
				if (elements.size() >= FormatUtils.getMaxNumberOfChildren()) {
					IEDCExpression moreExpression = (IEDCExpression) expressions.createExpression(frame, "0"); //$NON-NLS-1$
					moreExpression.setName("more...");
					elements.add(moreExpression);
					break;
				}
			}
			return elements;
		}

		private IEDCExpression getNextNode(IExpressions expressions, IFrameDMContext frame, String templateTypeName, IExpressionDMContext node) throws CoreException {
			FormatUtils.evaluateExpression((IEDCExpression) node);
			List<IExpressionDMContext> nodeFields = FormatUtils.getAllChildExpressions(node);
			IEDCExpression nextNode = (IEDCExpression) nodeFields.get(NODE_NEXT_CHILD_INDEX);
			// make a copy based on value of the pointer to avoid ever longer expressions n->n->n->...
			FormatUtils.evaluateExpression(nextNode);
			String expression = MessageFormat.format(NODE_CAST_FMT, templateTypeName,
						"0x" + Integer.toHexString(nextNode.getEvaluatedValue().intValue())); //$NON-NLS-1$
			nextNode = (IEDCExpression) expressions.createExpression(frame, expression);
			FormatUtils.evaluateExpression(nextNode);
			return nextNode;
		}
		
		@Override
		public String getValue(IExpressionDMContext variable) throws CoreException {
			int count = getChildCount(variable);
			return MessageFormat.format(DETAIL_SIZE_FMT, count);
		}

		protected String getFullStringValue(IExpressionDMContext variable) throws CoreException {
			List<IExpressionDMContext> children = getChildren(variable);
			IEDCExpression sizeExp = (IEDCExpression) children.get(SIZE_CHILD_INDEX);
			StringBuilder sb = new StringBuilder("["); //$NON-NLS-1$
			for (int i = SIZE_CHILD_INDEX + 1; i < children.size(); i++) {
				IEDCExpression elementChild = (IEDCExpression) children.get(i);
				if (i > SIZE_CHILD_INDEX + 1)
					sb.append(", "); //$NON-NLS-1$
				String elementString = FormatUtils.getFormattedValue(elementChild);
				if (elementString != null) {
					sb.append('{');
					sb.append(elementString);
					sb.append('}');
				}
				if (sb.length() > STOP_LENGTH) {
					if (!children.get(children.size() - 1).equals(elementChild))
						sb.append(", ..."); //$NON-NLS-1$
					break;
				}
				
			}
			return MessageFormat.format(DETAIL_FMT, FormatUtils.getVariableValue(sizeExp), sb.append(']').toString());
		}
	}

	public ITypeContentProvider getTypeContentProvider(IType type) {
		if (FormatUtils.checkClassOrInheritanceByName(type, TYPE_NAME)	) {
			return new FormatProvider(type, false);
		}
		return null;
	}

	public IVariableValueConverter getVariableValueConverter(IType type) {
		if (FormatUtils.checkClassOrInheritanceByName(type, TYPE_NAME)	) {
			return new FormatProvider(type, false);
		}
		return null;
	}

	public IVariableValueConverter getDetailValueConverter(IType type) {
		if (FormatUtils.checkClassOrInheritanceByName(type, TYPE_NAME)	) {
			return new FormatProvider(type, true);
		}
		return null;
	}

}
