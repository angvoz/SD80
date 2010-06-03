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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.formatter.FormatExtensionManager;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Expressions;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Memory;
import org.eclipse.cdt.debug.edc.internal.symbols.ICompositeType;
import org.eclipse.cdt.debug.edc.internal.symbols.IInheritance;
import org.eclipse.cdt.debug.edc.internal.symbols.IPointerType;
import org.eclipse.cdt.debug.edc.internal.symbols.ITypedef;
import org.eclipse.cdt.debug.edc.services.IEDCExpression;
import org.eclipse.cdt.debug.edc.services.IEDCMemory;
import org.eclipse.cdt.debug.edc.services.Stack.StackFrameDMC;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.debug.edc.symbols.TypeUtils;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.cdt.utils.Addr32;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.model.MemoryByte;

/**
 * Utilities for generating formatters
 * 
 * Use of non-api IType in this class is provisional. IType will later move to a public package.
 */
public class FormatUtils {
	
	private final static String CLASS = "class " ; //$NON-NLS-1$
	private final static String STRUCT = "struct "; //$NON-NLS-1$

	public static boolean checkTypeByName(IType type, String baseName) {
		if (type == null)
			return false;
		// we want to preserve typedefs to determine whether this is a type we support with a formatter
		IType baseType = TypeUtils.getBaseTypePreservingTypedef(type);
		
		// check for someone making a typedef of what we're looking for
		while (baseType != null && baseType instanceof ITypedef) {
			if (baseType.getName().equals(baseName))
				return true;
			baseType = TypeUtils.getBaseTypePreservingTypedef(baseType.getType());
		}

		if (baseType == null)
			return false;
		
		return checkName(baseType.getName(), baseName);
	}
	
	public static boolean checkName(String typeName, String baseName) {
		String checkName = typeName;
		if (typeName.startsWith(CLASS))
			checkName = typeName.substring(CLASS.length()).trim();
		else if (typeName.startsWith(STRUCT))
			checkName = typeName.substring(STRUCT.length()).trim();
		return checkName.equals(baseName);
	}
	
	
	/**
	 * Check if the name of a class/struct, or one of the classes/structs it
	 * derives from, matches a given name.
	 * 
	 * @param type type of class/struct
	 * @param name type name to match against
	 * @return true if class/struct or inherited class/struct matches name,
	 * or null if no match
	 */
	public static boolean checkClassOrInheritanceByName(IType type, String name) {
		// strip off typedefs and type qualifiers, to look for classes and structs
		type = TypeUtils.getBaseType(type);
		
		if (!(type instanceof ICompositeType))
			return false;
		
		ICompositeType composite = (ICompositeType)type;
		
		String baseName = composite.getBaseName();
		
		if (baseName.equals(name))
			return true;
		
		// if base name ends with a template size (e.g., "<15>"),
		// match ignoring the value in the braces
		if (baseName.contains("<")) //$NON-NLS-1$
			if (baseName.matches(name + "<.*>$")) //$NON-NLS-1$
				return true;

		// check classes and structs it derives from
		for (IInheritance inheritance : composite.getInheritances()) {
			if (checkClassOrInheritanceByName(inheritance.getType(), name))
				return true;
		}
		
		return false;
	}

	
	public static IExpressionDMContext createSubExpression(IExpressionDMContext variable, String name, String subExpressionStr) {
		IEDCExpression parentExpr = (IEDCExpression) variable;
		IExpressions expressions = parentExpr.getServiceTracker().getService(IExpressions.class);
		if (expressions == null)
			return null;
		String expressionStr = parentExpr.getExpression() + subExpressionStr;
		IEDCExpression subExpression = (IEDCExpression) expressions.createExpression(parentExpr, expressionStr);
		subExpression.setName(name);
		return subExpression;
	}
	
	public static String getFormattedString(IExpressionDMContext variable, IAddress address, int length, int charSize) {
		IEDCExpression expression = (IEDCExpression) variable;
		StackFrameDMC frame = (StackFrameDMC) expression.getFrame();
		IEDCMemory memory = frame.getDsfServicesTracker().getService(Memory.class);
		
		StringBuilder sb = new StringBuilder();
		ArrayList<MemoryByte> buffer = new ArrayList<MemoryByte>();
		IStatus status = memory.getMemory(frame.getExecutionDMC(), address, buffer, length * charSize, 1);
		if (status.isOK()) {
			for (int i = 0; i < length * charSize; i++) {
				char c = (char) (buffer.get(i).getValue() & 0xff);
				if (charSize > 1) {
					char c2 = (char) (buffer.get(++i).getValue() << 8);
					c |= c2;
				}
				sb.append(c);
			}
		}
		return sb.toString();
	}
	
	public static String getFormattedNullTermString(IExpressionDMContext variable, 
			IAddress address, int charSize,
			int maximumLength) throws CoreException {
		IEDCExpression expression = (IEDCExpression) variable;
		StackFrameDMC frame = (StackFrameDMC) expression.getFrame();
		IEDCMemory memory = frame.getDsfServicesTracker().getService(Memory.class);
		
		StringBuilder sb = new StringBuilder();
		while (maximumLength-- > 0) {
			ArrayList<MemoryByte> buffer = new ArrayList<MemoryByte>();
			IStatus status = memory.getMemory(frame.getExecutionDMC(), address, buffer, charSize, 1);
			if (status.isOK()) {
				char c = (char) buffer.get(0).getValue();
				if (charSize > 1) {
					char c2 = (char) (buffer.get(1).getValue() << 8);
					c |= c2;
				}
				if (c == '\0')
					break;
				sb.append(c);
				address = address.add(charSize);
			}
			else {
				// Error in reading memory, bail out.  If we got more than one character,
				// use ellipsis, else fail.
				if (sb.length() == 0)
					throw EDCDebugger.newCoreException(EDCFormatterMessages.FormatUtils_CannotReadMemory + address.getValue().toString(16));
				maximumLength = 0;
				break;
			}
		}
		if (maximumLength <= 0)
			sb.append("..."); //$NON-NLS-1$
		
		return sb.toString();
	}

	public static IExpressionDMContext findInCollectionByName(Collection<IExpressionDMContext> collection, String name) {
		for (IExpressionDMContext context : collection) {
			if (((IEDCExpression) context).getName().equals(name))
				return context;
		}
		
		return null;
	}

	public static List<IExpressionDMContext> getAllChildExpressions(IExpressionDMContext variable) {
		
		IEDCExpression variableDMC = (IEDCExpression) variable;
		Expressions expressions = variableDMC.getServiceTracker().getService(Expressions.class);
		if (expressions == null)
			return Collections.emptyList();
		
		List<IExpressionDMContext> kids = Arrays.<IExpressionDMContext>asList(
				expressions.getLogicalSubExpressions(variableDMC));
		return kids;
	}

	public static String getFieldAccessor(IType type) {
		if (type instanceof IPointerType)
			return "->"; //$NON-NLS-1$
		return "."; //$NON-NLS-1$
	}
	
	public static String getMemberValue(IExpressionDMContext variable, IType type, String memberName) {
		return getMemberValue(variable, type, memberName, IExpressions.NATURAL_FORMAT);
	}

	public static String getMemberValue(IExpressionDMContext variable, IType type, String memberName, String format) {
		IExpressions expressions = ((IEDCExpression)variable).getServiceTracker().getService(IExpressions.class);
		if (expressions == null)
			return ""; //$NON-NLS-1$
		IEDCExpression expression = 
			(IEDCExpression) expressions.createExpression(variable, variable.getExpression()
					+ FormatUtils.getFieldAccessor(type) + memberName);
		FormattedValueDMContext fvc = expressions.getFormattedValueContext(expression, format);
		return expression.getFormattedValue(fvc).getFormattedValue();
	}

	public static IType getUnqualifiedTypeRemovePointers(IType type) {
		IType unqualifiedType = TypeUtils.getStrippedType(type);
		while (unqualifiedType instanceof IPointerType)
			unqualifiedType = TypeUtils.getStrippedType(unqualifiedType.getType());
		return unqualifiedType;
	}

	public static IVariableValueConverter getCustomValueConverter(IExpressionDMContext variable) {
		IEDCExpression variableDMC = (IEDCExpression) variable;
		variableDMC.evaluateExpression();
		IType type = TypeUtils.getStrippedType(variableDMC.getEvaluatedType());
		return FormatExtensionManager.instance().getVariableValueConverter(type);
	}
	
	/**
	 * Get an address from an expression representing a pointer.
	 * @param value the evaluated value of an IEDCExpression
	 * @return the pointer address or <code>null</code>
	 */
	public static IAddress getPointerValue(Number value) {
		IAddress address = null;
		
		if (value instanceof BigInteger) {
			address = new Addr64((BigInteger) value);
		} else {
			address = new Addr32(value.longValue());
		}
		return address;
	}
}
