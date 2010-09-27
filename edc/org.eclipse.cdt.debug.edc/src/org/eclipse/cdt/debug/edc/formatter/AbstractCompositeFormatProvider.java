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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.debug.edc.services.IEDCExpression;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.core.runtime.CoreException;

/**
 * An abstract class for formatting composite types
 */
public abstract class AbstractCompositeFormatProvider extends AbstractVariableConverter implements ITypeContentProvider {

	protected static class NameToFieldPath {
		private String name;
		private String fieldPath;
		
		public NameToFieldPath(String name, String fieldPath) {
			this.name = name;
			this.fieldPath = fieldPath;
		}

		public String getName() {
			return name;
		}

		public String getFieldPath() {
			return fieldPath;
		}
	}
	
	private final NameToFieldPath[] nameToFieldPaths;
	
	public AbstractCompositeFormatProvider(IType type, boolean forDetails, List<NameToFieldPath> nameToFieldPaths) {
		super(type, forDetails);
		this.nameToFieldPaths = nameToFieldPaths.toArray(new NameToFieldPath[nameToFieldPaths.size()]);
	}

	public Iterator<IExpressionDMContext> getChildIterator(IExpressionDMContext variable) throws CoreException {
		List<IExpressionDMContext> childExpressions = getChildren(variable);
		return childExpressions.iterator();
	}
	
	/**
	 * @since 2.0
	 */
	public int getChildCount(IExpressionDMContext variable) throws CoreException {
		List<IExpressionDMContext> childExpressions = getChildren(variable);
		return childExpressions.size();
	}

	protected List<IExpressionDMContext> getChildren(IExpressionDMContext variable) throws CoreException {
		List<IExpressionDMContext> childExpressions = new ArrayList<IExpressionDMContext>();
		for (NameToFieldPath nameToFieldPath : nameToFieldPaths) {
			IExpressionDMContext createSubExpression = 
				FormatUtils.createSubExpression(variable, nameToFieldPath.getName(), 
						FormatUtils.getFieldAccessor(type) + nameToFieldPath.getFieldPath());
			if (createSubExpression != null) {
				childExpressions.add(createSubExpression);
			}
		}
		// now add all unmapped children
		List<IExpressionDMContext> allChildren = FormatUtils.getAllChildExpressions(variable);
		for (IExpressionDMContext child : allChildren) {
			String name = ((IEDCExpression) child).getName();
			if (!hasFieldPath(name))
				childExpressions.add(child);
		}
		return childExpressions;
	}

	private boolean hasFieldPath(String fieldPath) {
		for (NameToFieldPath nameToFieldPath : nameToFieldPaths) {
			if (nameToFieldPath.getFieldPath().equals(fieldPath))
				return true;
		}
		return false;
	}

	protected String getDetailsValue(IExpressionDMContext variable) throws CoreException {
		return getValueString(variable);
	}

	protected String getSummaryValue(IExpressionDMContext variable) throws CoreException {
		return getValueString(variable);
	}

	protected String getValueString(IExpressionDMContext variable) throws CoreException {
		IExpressions expressions = ((IEDCExpression) variable).getServiceTracker().getService(IExpressions.class);
		if (expressions == null)
			return ""; //$NON-NLS-1$
		
		StringBuilder sb = new StringBuilder();
		List<IExpressionDMContext> children = getChildren(variable);
		int i = 0;
		for (IExpressionDMContext child : children) {
			IEDCExpression childExpression = (IEDCExpression) child;
			if (i < nameToFieldPaths.length)
				sb.append(nameToFieldPaths[i].getName());
			else
				sb.append(childExpression.getName());
			sb.append("="); //$NON-NLS-1$
			sb.append(FormatUtils.getVariableValue(childExpression));
			sb.append(" "); //$NON-NLS-1$
			i++;
		}
		return sb.toString();
	}
	
}
