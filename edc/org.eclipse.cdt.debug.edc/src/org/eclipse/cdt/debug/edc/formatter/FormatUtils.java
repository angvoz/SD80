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

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.IArrayDimensionType;
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
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMData;
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
	
	/** The Constant CLASS. */
	private final static String CLASS = "class " ; //$NON-NLS-1$
	
	/** The Constant STRUCT. */
	private final static String STRUCT = "struct "; //$NON-NLS-1$

	/**
	 * Check type by name.
	 *
	 * @param type the type
	 * @param baseName the base name
	 * @return true, if successful
	 */
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
	
	/**
	 * Check name.
	 *
	 * @param typeName the type name
	 * @param baseName the base name
	 * @return true, if successful
	 */
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
		if (baseName.indexOf('<') != -1) //$NON-NLS-1$
			if (baseName.matches(name + "<.*>$")) //$NON-NLS-1$
				return true;

		// check classes and structs it derives from
		for (IInheritance inheritance : composite.getInheritances()) {
			if (checkClassOrInheritanceByName(inheritance.getType(), name))
				return true;
		}
		
		return false;
	}

	
	/**
	 * Creates the sub expression.
	 *
	 * @param variable the variable
	 * @param name the name
	 * @param subExpressionStr the sub expression str
	 * @return the IExpressionDMContext for the sub expression.
	 */
	public static IExpressionDMContext createSubExpression(IExpressionDMContext variable, String name, String subExpressionStr) {
		IEDCExpression parentExpr = (IEDCExpression) variable;
		IExpressions expressions = parentExpr.getExpressionsService();
		if (expressions == null)
			return null;
		String expressionStr = parentExpr.getExpression() + subExpressionStr;
		IEDCExpression subExpression = (IEDCExpression) expressions.createExpression(parentExpr, expressionStr);
		subExpression.setName(name);
		return subExpression;
	}
	
	/**
	 * Gets the formatted string.
	 *
	 * @param variable the variable
	 * @param address the address
	 * @param length the length
	 * @param charSize the char size
	 * @return the formatted string
	 * @throws CoreException the core exception
	 */
	public static String getFormattedString(IExpressionDMContext variable, IAddress address, int length, int charSize)
	 		throws CoreException {
		IEDCExpression expression = (IEDCExpression) variable;
		StackFrameDMC frame = (StackFrameDMC) expression.getFrame();
		IEDCMemory memory = frame.getEDCServicesTracker().getService(Memory.class);
		
		StringBuilder sb = new StringBuilder();
		ArrayList<MemoryByte> buffer = new ArrayList<MemoryByte>();
		IStatus status = memory.getMemory(frame.getExecutionDMC(), address, buffer, length * charSize, 1);
		if (status.isOK()) {
			for (int i = 0; i < length * charSize; i++) {
				// make sure each byte is okay
				if (!buffer.get(i).isReadable())
					throw EDCDebugger.newCoreException(
							MessageFormat.format(EDCFormatterMessages.FormatUtils_CannotReadMemory,
										address.add(i).getValue().toString(16)));

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
	
	/**
	 * Gets the formatted null term string.
	 *
	 * @param variable the variable
	 * @param address the address
	 * @param charSize the char size
	 * @param maximumLength the maximum length
	 * @return the formatted null term string
	 * @throws CoreException the core exception
	 */
	public static String getFormattedNullTermString(IExpressionDMContext variable, 
			IAddress address, int charSize,
			int maximumLength) throws CoreException {
		IEDCExpression expression = (IEDCExpression) variable;
		StackFrameDMC frame = (StackFrameDMC) expression.getFrame();
		IEDCMemory memory = frame.getEDCServicesTracker().getService(Memory.class);
		
		StringBuilder sb = new StringBuilder();
		ArrayList<MemoryByte> buffer = new ArrayList<MemoryByte>(64);// typical size of cache block
		if (maximumLength == 0)
			maximumLength = 16384;	// somewhat arbitrary; if the user really wants more, the value can always be set higher
		OUTER:while (maximumLength > 0) {
			int amount = Math.min(maximumLength, 64);// typical size of cache block
			IStatus status = memory.getMemory(frame.getExecutionDMC(), address, buffer, amount, charSize);
			if (status.isOK()) {
				// make sure each byte is okay
				for (int i = 0; i < buffer.size() && maximumLength > 0; ++i, --maximumLength) {
					if (!buffer.get(i).isReadable())
					{
						if (i == 0)	// partial memory read success
							throw EDCDebugger.newCoreException(
									MessageFormat.format(EDCFormatterMessages.FormatUtils_CannotReadMemory,
											address.add(i).getValue().toString(16)));
						maximumLength = 0;
						break OUTER;
					}
					char c = (char) buffer.get(i).getValue();
					if (charSize > 1) {
						char c2 = (char) (buffer.get(++i).getValue() << 8);
						c |= c2;
					}
					if (c == '\0')
						break OUTER;
					sb.append(c);
					address = address.add(charSize);
				}
			} else if (amount > 1) {
				maximumLength = Math.min(maximumLength, 64) / 2;
			} else {
				// Error in reading memory, bail out.  If we got more than one character,
				// use ellipsis, else fail.
				if (sb.length() == 0)
					throw EDCDebugger.newCoreException(
							MessageFormat.format(EDCFormatterMessages.FormatUtils_CannotReadMemory,
									address.getValue().toString(16)));
				maximumLength = 0;
				break;
			}
			buffer.clear();
		}
		if (maximumLength <= 0)
			sb.append("..."); //$NON-NLS-1$
		
		return sb.toString();
	}

	/**
	 * Find in collection by name.
	 *
	 * @param collection the collection
	 * @param name the name
	 * @return the i expression dm context
	 */
	public static IExpressionDMContext findInCollectionByName(Collection<IExpressionDMContext> collection, String name) {
		for (IExpressionDMContext context : collection) {
			if (((IEDCExpression) context).getName().equals(name))
				return context;
		}
		
		return null;
	}

	/**
	 * Gets the all child expressions.
	 *
	 * @param variable the variable
	 * @return the all child expressions
	 */
	public static List<IExpressionDMContext> getAllChildExpressions(IExpressionDMContext variable) {
		
		IEDCExpression variableDMC = (IEDCExpression) variable;
		Expressions expressions = (Expressions) variableDMC.getExpressionsService();
		if (expressions == null)
			return Collections.emptyList();
		
		List<IExpressionDMContext> kids = Arrays.<IExpressionDMContext>asList(
				expressions.getLogicalSubExpressions(variableDMC));
		return kids;
	}

	/**
	 * Gets the field accessor.
	 *
	 * @param type the type
	 * @return the field accessor
	 */
	public static String getFieldAccessor(IType type) {
		if (type instanceof IPointerType)
			return "->"; //$NON-NLS-1$
		return "."; //$NON-NLS-1$
	}
	
	/**
	 * Gets the member value.
	 *
	 * @param variable the variable
	 * @param type the type
	 * @param memberName the member name
	 * @return the member value
	 */
	public static String getMemberValue(IExpressionDMContext variable, IType type, String memberName) {
		return getMemberValue(variable, type, memberName, IExpressions.NATURAL_FORMAT);
	}

	/**
	 * Gets the member value.
	 *
	 * @param variable the variable
	 * @param type the type
	 * @param memberName the member name
	 * @param format the format
	 * @return the member value
	 */
	public static String getMemberValue(IExpressionDMContext variable, IType type, String memberName, String format) {
		IExpressions expressions = ((IEDCExpression)variable).getExpressionsService();
		if (expressions == null)
			return ""; //$NON-NLS-1$
		IEDCExpression expression = 
			(IEDCExpression) expressions.createExpression(variable, variable.getExpression()
					+ FormatUtils.getFieldAccessor(type) + memberName);
		FormattedValueDMContext fvc = expressions.getFormattedValueContext(expression, format);
		return expression.getFormattedValue(fvc).getFormattedValue();
	}

	/**
	 * Gets the variable value.
	 *
	 * @param variable the variable
	 * @return the variable value
	 * @since 2.0
	 */
	public static String getVariableValue(IExpressionDMContext variable) {
		return getVariableValue(variable, IExpressions.NATURAL_FORMAT);
	}

	/**
	 * Gets the variable value.
	 *
	 * @param variable the variable
	 * @param format the format
	 * @return the variable value
	 * @since 2.0
	 */
	public static String getVariableValue(IExpressionDMContext variable, String format) {
		IExpressions expressions = ((IEDCExpression)variable).getExpressionsService();
		FormattedValueDMContext fvc = 
			expressions.getFormattedValueContext(variable, format);
		FormattedValueDMData formattedValue = ((IEDCExpression) variable).getFormattedValue(fvc);
		return formattedValue.getFormattedValue();
	}
	
	/**
	 * Gets the unqualified type remove pointers.
	 *
	 * @param type the type
	 * @return the unqualified type remove pointers
	 */
	public static IType getUnqualifiedTypeRemovePointers(IType type) {
		IType unqualifiedType = TypeUtils.getStrippedType(type);
		while (unqualifiedType instanceof IPointerType)
			unqualifiedType = TypeUtils.getStrippedType(unqualifiedType.getType());
		return unqualifiedType;
	}

	/**
	 * Gets the custom value converter.
	 *
	 * @param variable the variable
	 * @return the custom value converter
	 */
	public static IVariableValueConverter getCustomValueConverter(IExpressionDMContext variable) {
		IEDCExpression variableDMC = (IEDCExpression) variable;
		variableDMC.evaluateExpression();
		IType type = TypeUtils.getUnRefStrippedType(variableDMC.getEvaluatedType());
		if (type instanceof IArrayDimensionType)
			type = ((IArrayDimensionType)type).getArrayType();
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

	/**
	 * Gets the template type name.
	 *
	 * @param typeName the type name
	 * @param type the type
	 * @return the template type name
	 * @since 2.0
	 */
	public static String getTemplateTypeName(String typeName, IType type) {
		// TODO Fix this when type gives template information Bug 11443
		
		ICompositeType composite = (ICompositeType) TypeUtils.getBaseType(type);
		String baseName = composite.getBaseName();
		
		Matcher m = Pattern.compile(typeName + "<(.+)>").matcher(baseName);
		if (m.matches())
			return m.group(1);

		// check classes and structs it derives from
		for (IInheritance inheritance : composite.getInheritances()) {
			String templateTypeName = getTemplateTypeName(typeName, inheritance.getType());
			if (templateTypeName != null)
				return templateTypeName;
		}
		
		return null;
	}
	
	/**
	 * Gets the formatted value.
	 *
	 * @param variable the variable
	 * @return the formatted value
	 * @throws CoreException the core exception
	 * @since 2.0
	 */
	public static String getFormattedValue(IExpressionDMContext variable) throws CoreException {
		IVariableValueConverter valueConverter = getCustomValueConverter(variable);
		if (valueConverter != null) {
			return valueConverter.getValue(variable);
		}
		else
			return getVariableValue(variable);
	}

	/**
	 * Gets the max number of children.
	 *
	 * @return the max number of children
	 * @since 2.0
	 */
	public static int getMaxNumberOfChildren() {
		return 200; // this seems like a good default
	}
	
	/**
	 * Evaluates the expression and throws a CoreException if there is an evaluation error.
	 *
	 * @param expression the expression
	 * @throws CoreException the core exception
	 * @since 2.0
	 */
	public static void evaluateExpression(IEDCExpression expression) throws CoreException {
		expression.evaluateExpression();
		IStatus status = expression.getEvaluationError();
		if ((status != null && !status.isOK()) || expression.getEvaluatedValue() == null) {
			Throwable t = status != null ? status.getException() : null;
			throw EDCDebugger.newDebugException("Error evaluating expression: " + expression.getExpression(), t);
		}
		
	}
}
