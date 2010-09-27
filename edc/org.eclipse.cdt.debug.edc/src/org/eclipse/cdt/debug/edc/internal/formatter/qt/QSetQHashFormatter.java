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
import org.eclipse.cdt.dsf.debug.service.IExpressions2;
import org.eclipse.cdt.dsf.debug.service.IExpressions2.CastInfo;
import org.eclipse.core.runtime.CoreException;

public class QSetQHashFormatter implements IVariableFormatProvider {

	private static final String QSET_NAME = "QSet"; //$NON-NLS-1$
	private static final String QHASH_NAME = "QHash"; //$NON-NLS-1$

	public static class FormatProvider extends AbstractCompositeFormatProvider {
		private static final String SIZE_NAME = "size"; //$NON-NLS-1$
		private static final String SIZE_PATH = "$unnamed$1.d->size"; //$NON-NLS-1$
		private static final String NUM_BUCKETS_NAME = "numBuckets"; //$NON-NLS-1$
		private static final String NUM_BUCKETS_PATH = "$unnamed$1.d->numBuckets"; //$NON-NLS-1$
		private static final String SANITY_TEST_NAME = "fakeNext"; //$NON-NLS-1$
		private static final String SANITY_TEST_PATH = "$unnamed$1.d->fakeNext"; //$NON-NLS-1$
		private static final String BUCKETS_NAME = "buckets"; //$NON-NLS-1$
		private static final String BUCKETS_PATH = "$unnamed$1.d->buckets"; //$NON-NLS-1$
		private static final String QSET_PATH_PREFIX = "q_hash."; //$NON-NLS-1$
		private static final int SIZE_CHILD_INDEX = 0;
		private static final int NUM_BUCKETS_CHILD_INDEX = 1;
		private static final int BUCKETS_CHILD_INDEX = 2;
		private static final int SANITY_TEST_CHILD_INDEX = 3;
		private static final int NODE_NEXT_CHILD_INDEX = 0;
		private static final String HASH_NODE_CAST_FMT = 
			"reinterpret_cast<QHashNode<{0}>*>({1})"; //$NON-NLS-1$
		private static final String DUMMY_VALUE_TYPE_ADD = ", QHashDummyValue"; //$NON-NLS-1$
		private static final String DETAIL_SIZE_FMT = "size={0}"; //$NON-NLS-1$
		private static final String DETAIL_FMT = "size={0} {1}"; //$NON-NLS-1$
		private static final int STOP_LENGTH = 300;

		private static Map<String, String> nameToFieldPathMap;
		static {
			nameToFieldPathMap = new LinkedHashMap<String, String>();
			nameToFieldPathMap.put(SIZE_NAME, SIZE_PATH);
			nameToFieldPathMap.put(NUM_BUCKETS_NAME, NUM_BUCKETS_PATH);
			nameToFieldPathMap.put(BUCKETS_NAME, BUCKETS_PATH);
			nameToFieldPathMap.put(SANITY_TEST_NAME, SANITY_TEST_PATH);
		}
		
		private final boolean isQSet;

		public FormatProvider(IType type, boolean forDetails, boolean isQSet) {
			super(type, forDetails, getNameToFieldPaths(isQSet));
			this.isQSet = isQSet;
		}

		private static List<NameToFieldPath> getNameToFieldPaths(boolean isQSet) {
			List<NameToFieldPath> nameToFieldPaths = new ArrayList<NameToFieldPath>();
			for (Entry<String, String> entry : nameToFieldPathMap.entrySet()) {
				String value = entry.getValue();
				nameToFieldPaths.add(new NameToFieldPath(entry.getKey(), isQSet ? QSET_PATH_PREFIX + value : value));
			}
			return nameToFieldPaths;
		}
		
		@Override
		public int getChildCount(IExpressionDMContext variable)throws CoreException {
			List<IExpressionDMContext> children = new ArrayList<IExpressionDMContext>();
			for (IExpressionDMContext child : super.getChildren(variable)) {
				String name = ((IEDCExpression) child).getName();
				if (nameToFieldPathMap.containsKey(name))
					children.add(child);
			}
			// test sanity and cast the buckets child to numBuckets size and remove them as children
			IEDCExpression sanityTestChild = (IEDCExpression) children.remove(SANITY_TEST_CHILD_INDEX);
			FormatUtils.evaluateExpression(sanityTestChild);
			int shouldBeZero = sanityTestChild.getEvaluatedValue().intValue();
			if (shouldBeZero != 0)
				throw EDCDebugger.newCoreException("uninitialized");
			IEDCExpression sizeExp = (IEDCExpression) children.get(SIZE_CHILD_INDEX);
			FormatUtils.evaluateExpression(sizeExp);
			int size = sizeExp.getEvaluatedValue().intValue();
			if (size < 0 || size > 0x1000000) // sanity
				throw EDCDebugger.newCoreException("Uninitialized");
			return size;
		}
		
		@Override
		protected List<IExpressionDMContext> getChildren(IExpressionDMContext variable) throws CoreException {
			List<IExpressionDMContext> children = new ArrayList<IExpressionDMContext>();
			for (IExpressionDMContext child : super.getChildren(variable)) {
				String name = ((IEDCExpression) child).getName();
				if (nameToFieldPathMap.containsKey(name))
					children.add(child);
			}
			// test sanity and cast the buckets child to numBuckets size and remove them as children
			IEDCExpression sanityTestChild = (IEDCExpression) children.remove(SANITY_TEST_CHILD_INDEX);
			IEDCExpression bucketsChild = (IEDCExpression) children.remove(BUCKETS_CHILD_INDEX);
			IEDCExpression numBucketsChild = (IEDCExpression) children.remove(NUM_BUCKETS_CHILD_INDEX);
			FormatUtils.evaluateExpression(sanityTestChild);
			int shouldBeZero = sanityTestChild.getEvaluatedValue().intValue();
			if (shouldBeZero != 0)
				throw EDCDebugger.newCoreException("uninitialized");
			FormatUtils.evaluateExpression(numBucketsChild);
			int numBuckets = numBucketsChild.getEvaluatedValue().intValue();
			if (numBuckets > 0) {
				IExpressions2 expressions2 = bucketsChild.getServiceTracker().getService(IExpressions2.class);
				IExpressionDMContext castedBucketsChild = expressions2.createCastedExpression(bucketsChild, new CastInfo(0, numBuckets));
				// add in all actual children from bucket elements
				IExpressions expressions = bucketsChild.getServiceTracker().getService(IExpressions.class);
				List<IExpressionDMContext> bucketsChildExpressions = FormatUtils.getAllChildExpressions(castedBucketsChild);
				int nameIndex = 0;
				for (IExpressionDMContext bucketChild : bucketsChildExpressions) {
					List<IEDCExpression> hashNodes = getHashNodesFromBucket(expressions, bucketChild);
					for (IEDCExpression hashNode : hashNodes) {
						IEDCExpression subExpression = (IEDCExpression) expressions.createExpression(variable, hashNode.getExpression());
						subExpression.setName("" + nameIndex++); //$NON-NLS-1$
						children.add(subExpression);
					}
					if (nameIndex > FormatUtils.getMaxNumberOfChildren()) {
						IEDCExpression moreExpression = (IEDCExpression) expressions.createExpression(variable, "0"); //$NON-NLS-1$
						moreExpression.setName("more...");
						children.add(moreExpression);
						break;
					}
				}
			}
			return children;
		}

		private List<IEDCExpression> getHashNodesFromBucket(IExpressions expressions, IExpressionDMContext nodeChild) throws CoreException {
			List<IEDCExpression> hashNodes = new ArrayList<IEDCExpression>();
			IEDCExpression nextExp = getNextExpression(nodeChild);
			while (nextExp != null) {
				String templateTypeName = FormatUtils.getTemplateTypeName(isQSet ? QSET_NAME : QHASH_NAME, type);
				if (isQSet)
					templateTypeName += DUMMY_VALUE_TYPE_ADD;
				String expression = 
					MessageFormat.format(HASH_NODE_CAST_FMT, 
							templateTypeName,
							nextExp.getEvaluatedLocation().toString());
				IEDCExpression hashNodeChild = 
					(IEDCExpression) expressions.createExpression(((IEDCExpression) nodeChild).getFrame(), expression);
				FormatUtils.evaluateExpression(hashNodeChild);
				hashNodes.add(hashNodeChild);
				nextExp = getNextExpression(nextExp);
			}
			return hashNodes;
		}

		private IEDCExpression getNextExpression(IExpressionDMContext nodeChild) throws CoreException {
			FormatUtils.evaluateExpression((IEDCExpression) nodeChild);
			List<IExpressionDMContext> nodeFields = FormatUtils.getAllChildExpressions(nodeChild);
			IEDCExpression nextExp = (IEDCExpression) nodeFields.get(NODE_NEXT_CHILD_INDEX);
			FormatUtils.evaluateExpression(nextExp);
			Number nextValue = nextExp.getEvaluatedValue();
			if (nextValue.intValue() != 0)
				return nextExp;
			return null;
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
		boolean isQSet = FormatUtils.checkClassOrInheritanceByName(type, QSET_NAME);
		if (isQSet || FormatUtils.checkClassOrInheritanceByName(type, QHASH_NAME)	) {
			return new FormatProvider(type, false, isQSet);
		}
		return null;
	}

	public IVariableValueConverter getVariableValueConverter(IType type) {
		boolean isQSet = FormatUtils.checkClassOrInheritanceByName(type, QSET_NAME);
		if (isQSet || FormatUtils.checkClassOrInheritanceByName(type, QHASH_NAME)	) {
			return new FormatProvider(type, false, isQSet);
		}
		return null;
	}

	public IVariableValueConverter getDetailValueConverter(IType type) {
		boolean isQSet = FormatUtils.checkClassOrInheritanceByName(type, QSET_NAME);
		if (isQSet || FormatUtils.checkClassOrInheritanceByName(type, QHASH_NAME)	) {
			return new FormatProvider(type, true, isQSet);
		}
		return null;
	}

}
