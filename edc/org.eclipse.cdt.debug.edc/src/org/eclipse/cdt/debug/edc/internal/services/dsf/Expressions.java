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
package org.eclipse.cdt.debug.edc.internal.services.dsf;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.EDCDebugger;
import org.eclipse.cdt.debug.edc.formatter.ITypeContentProvider;
import org.eclipse.cdt.debug.edc.formatter.IVariableValueConverter;
import org.eclipse.cdt.debug.edc.internal.IEDCTraceOptions;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.ASTEvaluationEngine;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.IArrayDimensionType;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.IInvalidExpression;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.InstructionSequence;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.Interpreter;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.InvalidExpression;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.VariableWithValue;
import org.eclipse.cdt.debug.edc.internal.formatter.FormatExtensionManager;
import org.eclipse.cdt.debug.edc.internal.symbols.IAggregate;
import org.eclipse.cdt.debug.edc.internal.symbols.IArrayBoundType;
import org.eclipse.cdt.debug.edc.internal.symbols.IArrayType;
import org.eclipse.cdt.debug.edc.internal.symbols.IBasicType;
import org.eclipse.cdt.debug.edc.internal.symbols.ICPPBasicType;
import org.eclipse.cdt.debug.edc.internal.symbols.ICompositeType;
import org.eclipse.cdt.debug.edc.internal.symbols.IEnumeration;
import org.eclipse.cdt.debug.edc.internal.symbols.IField;
import org.eclipse.cdt.debug.edc.internal.symbols.IInheritance;
import org.eclipse.cdt.debug.edc.internal.symbols.IInvalidVariableLocation;
import org.eclipse.cdt.debug.edc.internal.symbols.IPointerType;
import org.eclipse.cdt.debug.edc.internal.symbols.IQualifierType;
import org.eclipse.cdt.debug.edc.internal.symbols.IReferenceType;
import org.eclipse.cdt.debug.edc.internal.symbols.IRegisterVariableLocation;
import org.eclipse.cdt.debug.edc.internal.symbols.ISubroutineType;
import org.eclipse.cdt.debug.edc.internal.symbols.ITypedef;
import org.eclipse.cdt.debug.edc.services.AbstractEDCService;
import org.eclipse.cdt.debug.edc.services.DMContext;
import org.eclipse.cdt.debug.edc.services.IEDCExpression;
import org.eclipse.cdt.debug.edc.services.Registers;
import org.eclipse.cdt.debug.edc.services.Stack.StackFrameDMC;
import org.eclipse.cdt.debug.edc.symbols.IEnumerator;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.debug.edc.symbols.TypeUtils;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class Expressions extends AbstractEDCService implements IExpressions {

	private static int nextExpressionID = 100;

	private Map<String, ICPPBasicType> basicTypes;

	public class ExpressionDMC extends DMContext implements IEDCExpression {

		private static final String HEX_PREFIX = "0x"; //$NON-NLS-1$
		private static final String OCTAL_PREFIX = "0"; //$NON-NLS-1$
		private static final String BINARY_PREFIX = "0b"; //$NON-NLS-1$
		private static final String SINGLE_QUOTE = "'"; //$NON-NLS-1$
		private static final String DECIMAL_SUFFIX = " (Decimal)"; //$NON-NLS-1$
		private InstructionSequence parsedExpression;
		private final ASTEvaluationEngine engine = new ASTEvaluationEngine();
		private final StackFrameDMC frame;
		private Object value;
		private Object valueLocation;
		private Object valueType;
		private boolean hasChildren = false;

		public ExpressionDMC(StackFrameDMC frame, String name) {
			super(Expressions.this, new IDMContext[] { frame }, name, nextExpressionID++ + name);
			this.frame = frame;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.internal.services.dsf.IEDCExpression#getFrame()
		 */
		public IFrameDMContext getFrame() {
			return frame;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.internal.services.dsf.IEDCExpression#getExpression()
		 */
		public String getExpression() {
			Object expression = getProperty(IEDCExpression.EXPRESSION_PROP);
			if (expression == null)
				expression = getName();
			return (String) expression;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.internal.services.dsf.IEDCExpression#evaluateExpression()
		 */
		public void evaluateExpression() {
			if (value != null)
				return;

			String expression = getExpression();

			if (parsedExpression == null)
				parsedExpression = engine.getCompiledExpression(expression);

			if (parsedExpression.getInstructions().length == 0) {
				value = new InvalidExpression(EDCServicesMessages.Expressions_SyntaxError);
				valueLocation = ""; //$NON-NLS-1$
				valueType = ""; //$NON-NLS-1$
				return;
			}

			Interpreter interpreter = engine.evaluateCompiledExpression(parsedExpression, frame);
			value = interpreter.getResult();
			valueLocation = interpreter.getValueLocation();
			valueType = interpreter.getValueType();

			if (value instanceof VariableWithValue) {
				VariableWithValue variableValue = (VariableWithValue) value;

				// change register variable location to the register name
				if (valueLocation instanceof IRegisterVariableLocation) {
					IRegisterVariableLocation regVarLocation = (IRegisterVariableLocation) valueLocation;
					if (regVarLocation.getRegisterName() == null) {
						Registers registerservice = variableValue.getServicesTracker().getService(Registers.class);
						valueLocation = "$" + registerservice.getRegisterNameFromCommonID(regVarLocation.getRegisterID()); //$NON-NLS-1$
					} else
						valueLocation = "$" + regVarLocation.getRegisterName(); //$NON-NLS-1$
				}

				// for a structured type or array, return the location and note
				// that it has children
				if (valueType instanceof IAggregate) {
					value = variableValue.getValueLocation();
					if (!(value instanceof IInvalidVariableLocation))
						hasChildren = true;
				} else {
					value = variableValue.getValue();

					// for a reference to a plain type, use the location in the variable with value
					if (TypeUtils.getStrippedType(valueType) instanceof IReferenceType) {
						IType pointedTo = TypeUtils.getStrippedType(valueType).getType();
						if (pointedTo instanceof ICPPBasicType || pointedTo instanceof IPointerType ||
							pointedTo instanceof IEnumeration) {
							valueLocation = variableValue.getValueLocation();
						}
					}
				}

				// if the location evaluates to NotLive, the types and values do
				// not matter
				if (valueLocation instanceof IInvalidVariableLocation) {
					value = new InvalidExpression(((IInvalidVariableLocation) valueLocation).getMessage());
					valueLocation = ""; //$NON-NLS-1$
					return;
				}

				// for a structured type, array, or pointer return the value in
				// hex
				IType unqualifiedType = TypeUtils.getStrippedType(valueType);
				if (unqualifiedType instanceof IAggregate || unqualifiedType instanceof IPointerType) {
					if (value instanceof Addr64)
						value = HEX_PREFIX + ((Addr64) value).toString(16);
					else if (value instanceof Integer)
						value = HEX_PREFIX + Integer.toHexString((Integer) value);
					else if (value instanceof BigInteger)
						value = HEX_PREFIX + ((BigInteger) value).toString(16);
					else if (value instanceof Long)
						value = HEX_PREFIX + Long.toHexString((Long) value);
				} else if (unqualifiedType instanceof IEnumeration) {
					// for an enumerator, return the name, if any
					if ((value instanceof Integer) || (value instanceof Long) || (value instanceof BigInteger)) {
						long enumeratorValue = -1;
						if (value instanceof Integer)
							enumeratorValue = (Integer) value;
						else if (value instanceof Long)
							enumeratorValue = (Long) value;
						else if (value instanceof BigInteger)
							enumeratorValue = ((BigInteger) value).longValue();

						IEnumerator enumerator = ((IEnumeration) unqualifiedType).getEnumeratorByValue(enumeratorValue);

						if (enumerator != null)
							value = enumerator.getName() + " [" + enumeratorValue + "]"; //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.internal.services.dsf.IEDCExpression#getFormattedValue(org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext)
		 */
		public FormattedValueDMData getFormattedValue(FormattedValueDMContext dmc) {
			EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.VARIABLE_VALUE_TRACE, dmc);
			evaluateExpression();
			String result = ""; //$NON-NLS-1$

			if (value instanceof IInvalidExpression) {
				result = ((IInvalidExpression) value).getMessage();
			} else if (value != null) {
				result = value.toString();
			}

			if (value instanceof Number) {
				String temp = null;
				String formatID = dmc.getFormatID();
				if (formatID.equals(IFormattedValues.HEX_FORMAT)) {
					temp = toHexString((Number) value);
				} else if (formatID.equals(IFormattedValues.OCTAL_FORMAT)) {
					temp = toOctalString((Number) value);
				} else if (formatID.equals(IFormattedValues.BINARY_FORMAT)) {
					temp = asBinary((Number) value);
				} else if (formatID.equals(IFormattedValues.NATURAL_FORMAT)) {
					// for chars, do something special
					IType unqualifiedType = TypeUtils.getStrippedType(valueType);
					if (unqualifiedType instanceof ICPPBasicType
							&& ((ICPPBasicType) unqualifiedType).getBaseType() == IBasicType.t_char) {
						temp = toCharString((Number) value);
					}
				}
				if (temp != null)
					result = temp; 
				// otherwise, leave value as is
			}
			EDCDebugger.getDefault().getTrace().traceExit(IEDCTraceOptions.VARIABLE_VALUE_TRACE, result);
			return new FormattedValueDMData(result);
		}

		private String toHexString(Number number) {
			String str = null;
			if (number instanceof Integer)
				str = Integer.toHexString((Integer) number);
			else if (number instanceof Long)
				str = Long.toHexString((Long) number);
			else if (number instanceof BigInteger)
				str = ((BigInteger) number).toString(16);
			else if (number instanceof Float)
				str = Float.toHexString((Float) number);
			else if (number instanceof Double)
				str = Double.toHexString((Double) number);
			if (str != null && !str.startsWith(HEX_PREFIX))
				return HEX_PREFIX + str;
			return str;
		}

		private String toOctalString(Number number) {
			String str = null;
			if (number instanceof Integer)
				str = Integer.toOctalString((Integer) number);
			else if (number instanceof Long)
				str = Long.toOctalString((Long) number);
			else if (number instanceof BigInteger)
				str = ((BigInteger) number).toString(8);
			if (str != null && !str.startsWith(OCTAL_PREFIX))
				str = OCTAL_PREFIX + str;
			if (str == null && (number instanceof Float || number instanceof Double))
				str = number.toString() + DECIMAL_SUFFIX;
			return str;
		}

		private String asBinary(Number number) {
			String str = null;
			if (number instanceof Integer)
				str = Integer.toBinaryString((Integer) number);
			else if (number instanceof Long)
				str = Long.toBinaryString((Long) number);
			else if (number instanceof BigInteger)
				str = ((BigInteger) number).toString(2);
			if (str != null && !str.startsWith(BINARY_PREFIX))
				str = BINARY_PREFIX + str;
			if (str == null && (number instanceof Float || number instanceof Double))
				str = number.toString() + DECIMAL_SUFFIX;
			return str;
		}

		private String toCharString(Number number) {
			int intValue = number.intValue();
			switch ((char) intValue) {
				case 0:
					return asStringQuoted("\\0"); //$NON-NLS-1$
				case '\b':
					return asStringQuoted("\\b"); //$NON-NLS-1$
				case '\f':
					return asStringQuoted("\\f"); //$NON-NLS-1$
				case '\n':
					return asStringQuoted("\\n"); //$NON-NLS-1$
				case '\r':
					return asStringQuoted("\\r"); //$NON-NLS-1$
				case '\t':
					return asStringQuoted("\\t"); //$NON-NLS-1$
				case '\'':
					return asStringQuoted("\\'"); //$NON-NLS-1$
				case '\"':
					return asStringQuoted("\\\""); //$NON-NLS-1$
				case '\\':
					return asStringQuoted("\\\\"); //$NON-NLS-1$
				case 0xb:
					return asStringQuoted("\\v"); //$NON-NLS-1$
			}
		
			return asStringQuoted(Character.toString((char) ((Number) value).byteValue()));
		}

		private String asStringQuoted(String val) {
			StringBuilder sb = new StringBuilder(SINGLE_QUOTE);
			sb.append(val);
			sb.append(SINGLE_QUOTE);
			return sb.toString();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.internal.services.dsf.IEDCExpression#getValueLocation()
		 */
		public Object getValueLocation() {
			evaluateExpression();
			return getEvaluatedLocation();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.internal.services.dsf.IEDCExpression#getEvaluatedValue()
		 */
		public Object getEvaluatedValue() {
			return value;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.internal.services.dsf.IEDCExpression#setEvaluatedValue(java.lang.Object)
		 */
		public void setEvaluatedValue(Object value) {
			this.value = value;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.internal.services.dsf.IEDCExpression#getEvaluatedLocation()
		 */
		public Object getEvaluatedLocation() {
			if (valueLocation instanceof IAddress) {
				// don't print these as decimal or as ridiculously long numbers
				IAddress addr = (IAddress) valueLocation;
				if (addr.compareTo(Addr64.MAX) < 0)
					return HEX_PREFIX + Long.toHexString(addr.getValue().longValue());
				else
					return addr.toHexAddressString();
			}
			return valueLocation;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.internal.services.dsf.IEDCExpression#getEvaluatedType()
		 */
		public Object getEvaluatedType() {
			return valueType;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.internal.services.dsf.IEDCExpression#getTypeName()
		 */
		public String getTypeName() {
			evaluateExpression();
			if (valueType == null)
				return ASTEvaluationEngine.UNKNOWN_TYPE;
			return recursiveGetType(valueType);
		}

		private String recursiveGetType(Object typeValue) {
			// FIXME: move this into an IType method
			if (typeValue instanceof IReferenceType)
				return recursiveGetType(((IReferenceType) typeValue).getType()) + " &"; //$NON-NLS-1$
			if (typeValue instanceof IPointerType)
				return recursiveGetType(((IPointerType) typeValue).getType()) + " *"; //$NON-NLS-1$
			if (typeValue instanceof IArrayType) {
				IArrayType arrayType = (IArrayType) typeValue;
				String returnType = recursiveGetType(arrayType.getType());

				IArrayBoundType[] bounds = arrayType.getBounds();
				for (IArrayBoundType bound : bounds) {
					returnType += "[" + bound.getBoundCount() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
				}
				return returnType;
			}
			if (typeValue instanceof IArrayDimensionType) {
				IArrayDimensionType arrayDimensionType = (IArrayDimensionType) typeValue;
				IArrayType arrayType = arrayDimensionType.getArrayType();
				String returnType = recursiveGetType(arrayType.getType());

				IArrayBoundType[] bounds = arrayType.getBounds();
				for (int i = arrayDimensionType.getDimensionCount(); i < arrayType.getBoundsCount(); i++) {
					returnType += "[" + bounds[i].getBoundCount() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
				}
				return returnType;
			}
			if (typeValue instanceof ITypedef)
				return ((ITypedef) typeValue).getName();
			if (typeValue instanceof ICompositeType)
				return ((ICompositeType) typeValue).getName();
			if (typeValue instanceof IQualifierType)
				return ((IQualifierType) typeValue).getName()
						+ " " + recursiveGetType(((IQualifierType) typeValue).getType()); //$NON-NLS-1$
			if (typeValue instanceof ISubroutineType) {
				// TODO: real stuff once we parse parameters
				// TODO: the '*' for a function pointer (e.g. in a vtable) is in the wrong place
				return recursiveGetType(((ISubroutineType) typeValue).getType()) + "(...)"; //$NON-NLS-1$
			}
			if (typeValue instanceof IType)
				return ((IType) typeValue).getName() + recursiveGetType(((IType) typeValue).getType());
			if (typeValue == null)
				return ""; //$NON-NLS-1$
			return typeValue.toString();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.internal.services.dsf.IEDCExpression#hasChildren()
		 */
		public boolean hasChildren() {
			return this.hasChildren;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.internal.services.dsf.IEDCExpression#getService()
		 */
		public IExpressions getService() {
			return Expressions.this;
		}
	}

	public class ExpressionData implements IExpressionDMData {

		private final ExpressionDMC dmc;

		public ExpressionData(ExpressionDMC dmc) {
			this.dmc = dmc;
		}

		public BasicType getBasicType() {
			return null;
		}

		public String getEncoding() {
			return null;
		}

		public Map<String, Integer> getEnumerations() {
			return null;
		}

		public String getName() {
			if (dmc != null)
				return dmc.getName();
			else
				return ""; //$NON-NLS-1$
		}

		public IRegisterDMContext getRegister() {
			return null;
		}

		public String getTypeId() {
			return TYPEID_INTEGER;
		}

		public String getTypeName() {
			if (dmc != null)
				return dmc.getTypeName();
			else
				return ""; //$NON-NLS-1$
		}

	}

	protected static class InvalidContextExpressionDMC extends AbstractDMContext implements IExpressionDMContext {
		private final String expression;

		public InvalidContextExpressionDMC(String sessionId, String expr, IDMContext parent) {
			super(sessionId, new IDMContext[] { parent });
			expression = expr;
		}

		@Override
		public boolean equals(Object other) {
			return super.baseEquals(other) && expression == null ? ((InvalidContextExpressionDMC) other)
					.getExpression() == null : expression.equals(((InvalidContextExpressionDMC) other).getExpression());
		}

		@Override
		public int hashCode() {
			return expression == null ? super.baseHashCode() : super.baseHashCode() ^ expression.hashCode();
		}

		@Override
		public String toString() {
			return baseToString() + ".invalid_expr[" + expression + "]"; //$NON-NLS-1$ //$NON-NLS-2$
		}

		public String getExpression() {
			return expression;
		}
	}

	public class ExpressionDMAddress implements IExpressionDMLocation {

		private final Object valueLocation;

		public ExpressionDMAddress(IExpressionDMContext exprContext) {
			if (exprContext instanceof ExpressionDMC)
				valueLocation = ((IEDCExpression) exprContext).getValueLocation();
			else
				valueLocation = new Addr64("0"); //$NON-NLS-1$
		}

		public IAddress getAddress() {
			if (valueLocation instanceof IAddress)
				return (IAddress) valueLocation;
			return new Addr64("0"); //$NON-NLS-1$
		}

		public int getSize() {
			return 4;
		}

		public String getLocation() {
			if (valueLocation instanceof IInvalidVariableLocation) {
				return ((IInvalidVariableLocation)valueLocation).getMessage();
			}
			return valueLocation == null ? "" : valueLocation.toString(); //$NON-NLS-1$
		}

	}

	public Expressions(DsfSession session) {
		super(session, new String[] { IExpressions.class.getName(), Expressions.class.getName() });
	}

	public void canWriteExpression(IExpressionDMContext exprContext, DataRequestMonitor<Boolean> rm) {
		rm.setData(false);
		rm.done();
	}

	public IExpressionDMContext createExpression(IDMContext context, String expression) {
		StackFrameDMC frameDmc = DMContexts.getAncestorOfType(context, StackFrameDMC.class);

		if (basicTypes == null && getTargetEnvironmentService() != null)
			createBasicTypes();

		if (frameDmc != null) {
			return new ExpressionDMC(frameDmc, expression);
		}
		return new InvalidContextExpressionDMC(getSession().getId(), expression, context);
	}

	public void getBaseExpressions(IExpressionDMContext exprContext, DataRequestMonitor<IExpressionDMContext[]> rm) {
		rm.setData(new ExpressionDMC[0]);
		rm.done();
	}

	public void getExpressionAddressData(IExpressionDMContext exprContext, DataRequestMonitor<IExpressionDMAddress> rm) {
		if (exprContext instanceof ExpressionDMC)
			rm.setData(new ExpressionDMAddress(exprContext));
		else
			rm.setData(new ExpressionDMAddress(null));
		rm.done();
	}

	public void getExpressionData(IExpressionDMContext exprContext, DataRequestMonitor<IExpressionDMData> rm) {
		if (exprContext instanceof ExpressionDMC)
			rm.setData(new ExpressionData((ExpressionDMC) exprContext));
		else
			rm.setData(new ExpressionData(null));
		rm.done();
	}

	public void getSubExpressionCount(IExpressionDMContext exprContext, DataRequestMonitor<Integer> rm) {
		rm.setData(0);
		rm.done();
	}

	public void getSubExpressions(IExpressionDMContext exprContext, DataRequestMonitor<IExpressionDMContext[]> rm) {
		if (!(exprContext instanceof ExpressionDMC) || ((IEDCExpression) exprContext).getFrame() == null) {
			rm.setData(new ExpressionDMC[0]);
			rm.done();
			return;
		}

		ExpressionDMC expr = (ExpressionDMC) exprContext;

		// if expression has no evaluated value, then it has not yet been evaluated
		// NOTE: this should never happen
		if (expr.getEvaluatedValue() == null) {
			expr.evaluateExpression();
		}

		StackFrameDMC frame = (StackFrameDMC) expr.getFrame();
		IType exprType = TypeUtils.getStrippedType(expr.getEvaluatedType());

		// to expand it, it must either be a pointer, a reference to an aggregate,
		// or an aggregate
		boolean pointerType = exprType instanceof IPointerType;
		boolean referenceType = exprType instanceof IReferenceType;
		IType pointedTo = null;
		if (referenceType)
			pointedTo = TypeUtils.getStrippedType(((IReferenceType) exprType).getType());
		
		if (!(exprType instanceof IAggregate) && !pointerType &&
			!(referenceType && (pointedTo instanceof IAggregate))) {
			rm.setData(new ExpressionDMC[0]);
			rm.done();
			return;
		}
		
		ITypeContentProvider customProvider = 
			FormatExtensionManager.instance().getTypeContentProvider(exprType);
		if (customProvider != null) {
			try {
				getSubExpressions(expr, frame, customProvider, rm);
			}
			catch (CoreException e) {
				// Checked exception. But we don't want to pass the error up as it
				// would make the variable (say, a structure) not expandable on UI. 
				// Just resort to the normal formatting.  
				getSubExpressions(expr, frame, exprType, rm);
			} catch (Throwable e) {
				// unexpected error. log it.
				EDCDebugger.getMessageLogger().logError(
						EDCServicesMessages.Expressions_ErrorInVariableFormatter + customProvider.getClass().getName(), e);
				
				// default to normal formatting
				getSubExpressions(expr, frame, exprType, rm);
			}
		}
		else
			getSubExpressions(expr, frame, exprType, rm);
	}

	private void getSubExpressions(ExpressionDMC expr, StackFrameDMC frame, 
					IType exprType,	DataRequestMonitor<IExpressionDMContext[]> rm) {
		ExpressionDMC[] children = getsubExpressions(expr, frame, exprType);
		rm.setData(children);
		rm.done();
	}
	
	public ExpressionDMC[] getsubExpressions(ExpressionDMC expr, StackFrameDMC frame, IType exprType) {
		ArrayList<ExpressionDMC> exprList = new ArrayList<ExpressionDMC>();
		ExpressionDMC exprChild;

		// the expression string we need may be different from the name displayed in
		// the Variables view Name column or the Expressions view Expression column
		String exprName = expr.getExpression();

		exprType = TypeUtils.getStrippedType(exprType);

		// should be a pointer, structure, or array
		if (exprType instanceof IPointerType || exprType instanceof IReferenceType) {
			expandPointedTo(frame, exprType, exprName, exprList);
		} else if (exprType instanceof ICompositeType) {
			// an artifact of following a pointer to a structure is that the
			// name starts with '*'
			if (exprName.startsWith("*")) //$NON-NLS-1$
				exprName = exprName.substring(1) + "->"; //$NON-NLS-1$
			else
				exprName = exprName + "."; //$NON-NLS-1$

			// for each field, evaluate an expression, then shorten the name
			ICompositeType compositeType = (ICompositeType) exprType;

			IField[] fields = compositeType.getFields();

			for (IField field : fields) {
				exprChild = new ExpressionDMC(frame, exprName + (field).getName());
				if (exprChild != null) {
					exprList.add(exprChild);
				}
			}
			
			IInheritance[] inheritedFrom = compositeType.getInheritances();
			for (IInheritance inherited : inheritedFrom) {
				exprChild = new ExpressionDMC(frame, exprName + (inherited).getName());
				if (exprChild != null) {
					exprList.add(exprChild);
				}
			}
			
		} else if (exprType instanceof IArrayType) {
			IArrayType arrayType = (IArrayType) exprType;

			if (arrayType.getBoundsCount() > 0) {
				long upperBound = arrayType.getBound(0).getBoundCount();
				for (int i = 0; i < upperBound; i++) {
					exprChild = new ExpressionDMC(frame, exprName + "[" + i + "]"); //$NON-NLS-1$ //$NON-NLS-2$
					if (exprChild != null) {
						exprList.add(exprChild);
					}
				}
			}
		} else if (exprType instanceof IArrayDimensionType) {
			IArrayDimensionType arrayDimensionType = (IArrayDimensionType) exprType;
			IArrayType arrayType = arrayDimensionType.getArrayType();

			if (arrayType.getBoundsCount() > arrayDimensionType.getDimensionCount()) {
				long upperBound = arrayType.getBound(arrayDimensionType.getDimensionCount()).getBoundCount();
				for (int i = 0; i < upperBound; i++) {
					exprChild = new ExpressionDMC(frame, exprName + "[" + i + "]"); //$NON-NLS-1$ //$NON-NLS-2$
					if (exprChild != null) {
						exprList.add(exprChild);
					}
				}
			}
		}

		ExpressionDMC[] children = new ExpressionDMC[exprList.size()];
		for (int i = 0; i < exprList.size(); i++) {
			final ExpressionDMC child = exprList.get(i);
			children[i] = child;

			// if needed, shorten the displayed name or expression associated
			// with a child
			String childName = child.getExpression();
			if (childName.contains(".") || childName.contains("->")) { //$NON-NLS-1$ //$NON-NLS-2$
				child.setProperty(IEDCExpression.EXPRESSION_PROP, childName);
				if (childName.contains(".")) //$NON-NLS-1$
					childName = childName.substring(childName.lastIndexOf(".") + 1); //$NON-NLS-1$
				if (childName.contains("->")) //$NON-NLS-1$
					childName = childName.substring(childName.lastIndexOf("->") + 2); //$NON-NLS-1$
				child.setName(childName);
			}
		}
		return children;
	}

	private void getSubExpressions(ExpressionDMC expr, StackFrameDMC frame, 
		ITypeContentProvider customProvider, DataRequestMonitor<IExpressionDMContext[]> rm) throws CoreException {
		List<IExpressionDMContext> children = new ArrayList<IExpressionDMContext>();
		Iterator<IExpressionDMContext> childIterator = customProvider.getChildIterator(expr);
		while (childIterator.hasNext() && !rm.isCanceled()) {
			children.add(childIterator.next());
		}
		rm.setData(children.toArray(new IExpressionDMContext[children.size()]));
		rm.done();
	}

	private void expandPointedTo(StackFrameDMC frame, IType exprType, String exprName, ArrayList<ExpressionDMC> exprList) {
		ExpressionDMC exprChild;

		// a pointer type has one child
		IType typePointedTo = TypeUtils.getStrippedType(exprType.getType());

		// if expression name already starts with "&", just remove it
		if ((exprType instanceof IPointerType) && exprName.startsWith("&")) { //$NON-NLS-1$
			exprName = exprName.substring(1);
			exprChild = new ExpressionDMC(frame, exprName);
			return;
		}
		
		if (exprType instanceof IReferenceType)
			exprChild = new ExpressionDMC(frame, exprName); //$NON-NLS-1$
		else
			exprChild = new ExpressionDMC(frame, "*" + exprName); //$NON-NLS-1$

		// ALTERNATIVE EXPANSION: for struct s *p, show additional "struct s *"
		// line that then gets expanded to to a "struct s" line
		// if (exprChild != null) {
		// 		exprList.add(exprChild);
		// }

		if (typePointedTo instanceof ICPPBasicType || typePointedTo instanceof IPointerType
				|| typePointedTo instanceof IEnumeration) {
			// for types without members/elements
			if (exprChild != null && exprType instanceof IPointerType) {
				exprList.add(exprChild);
			}
		} else if (typePointedTo instanceof ICompositeType) {
			// for composites, go directly to showing the fields
			if (exprType instanceof IReferenceType)
				exprName = exprName + "."; //$NON-NLS-1$
			else
				exprName = exprName + "->"; //$NON-NLS-1$

			// for each field, evaluate an expression, then shorten the name
			ICompositeType compositeType = (ICompositeType) typePointedTo;

			IField[] fields = compositeType.getFields();

			for (IField field : fields) {
				exprChild = new ExpressionDMC(frame, exprName + (field).getName());
				if (exprChild != null) {
					exprList.add(exprChild);
				}
			}
		} else {
			// for arrays
			if (exprChild != null) {
				exprList.add(exprChild);
			}
		}
	}

	public void getSubExpressions(IExpressionDMContext exprContext, int startIndex, int length,
			DataRequestMonitor<IExpressionDMContext[]> rm) {
		rm.setData(new ExpressionDMC[0]);
		rm.done();
	}

	public void writeExpression(IExpressionDMContext exprContext, String expressionValue, String formatId,
			RequestMonitor rm) {
	}

	public void getAvailableFormats(IFormattedDataDMContext formattedDataContext, DataRequestMonitor<String[]> rm) {
		rm.setData(new String[] { IFormattedValues.BINARY_FORMAT, IFormattedValues.NATURAL_FORMAT,
				IFormattedValues.HEX_FORMAT, IFormattedValues.OCTAL_FORMAT, IFormattedValues.DECIMAL_FORMAT });
		rm.done();
	}

	public void getFormattedExpressionValue(FormattedValueDMContext formattedDataContext,
			DataRequestMonitor<FormattedValueDMData> rm) {
		IDMContext idmContext = formattedDataContext.getParents()[0];
		FormattedValueDMData formattedValue = null;
		ExpressionDMC exprDMC = null;

		if (idmContext instanceof ExpressionDMC) {
			exprDMC = (ExpressionDMC) formattedDataContext.getParents()[0];

			formattedValue = exprDMC.getFormattedValue(formattedDataContext); // must call this to get type
			IType exprType = TypeUtils.getStrippedType(exprDMC.getEvaluatedType());
			IVariableValueConverter customValue = 
				FormatExtensionManager.instance().getVariableValueConverter(exprType);
			if (customValue != null) {
				FormattedValueDMData customFormattedValue = null;
				try {
					customFormattedValue = new FormattedValueDMData(customValue.getValue(exprDMC));
					formattedValue = customFormattedValue;
				}
				catch (CoreException e) {
					// Checked exception like failure in reading memory.
					// Pass the error to the RM so that it would show up in UI. 
					rm.setStatus(e.getStatus());
					rm.done();
					return;
				}
				catch (Throwable t) {
					// Other unexpected errors, usually bug in the formatter. Log it 
					// so that user will be able to see and report the bug. 
					// Meanwhile default to normal formatting so that user won't see 
					// such error in Variable UI.
					EDCDebugger.getMessageLogger().logError(
							EDCServicesMessages.Expressions_ErrorInVariableFormatter + customValue.getClass().getName(), t);
				}
			}
		} else
			formattedValue = new FormattedValueDMData(""); //$NON-NLS-1$

		rm.setData(formattedValue);
		String formattedValueStr = formattedValue.getFormattedValue();

		if (exprDMC != null && exprDMC.getEvaluatedValue() instanceof IInvalidExpression)
			rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, formattedValueStr));
		rm.done();
	}

	public FormattedValueDMContext getFormattedValueContext(IFormattedDataDMContext formattedDataContext,
			String formatId) {
		return new FormattedValueDMContext(this, formattedDataContext, formatId);
	}

	public void getModelData(IDMContext context, DataRequestMonitor<?> rm) {
	}

	private void createBasicTypes() {
		// TODO: create basic types for standard C/C++ base types, for casting
		// to types
	}

	public String getExpressionValue(IExpressionDMContext expression)
	{
		return getExpressionValue(expression, IFormattedValues.NATURAL_FORMAT);
	}

	public String getExpressionValue(IExpressionDMContext expression, String format)
	{
		final StringBuffer holder = new StringBuffer();
		FormattedValueDMContext formattedValueContext = getFormattedValueContext(expression, format);					
		getFormattedExpressionValue(formattedValueContext, new DataRequestMonitor<FormattedValueDMData>(ImmediateExecutor.getInstance(), null) {
			@Override
			protected void handleSuccess() {
				holder.append(this.getData().getFormattedValue());
			}

			@Override
			protected void handleFailure() {
				// RequestMonitor would by default log any error if it's not explicitly 
				// handled. But we don't want to log those expected errors (checked exceptions)
				// in such case as creating snapshot. Hence this dummy handler...02/17/10
				
				// DO nothing.
			}
			
			
		});
		return holder.toString();
	}

	public void loadExpressionValues(IExpressionDMContext expression, int depth)
	{
		loadExpressionValues(expression, new Integer[] {depth});
	}

	private void loadExpressionValues(IExpressionDMContext expression, final Integer[] depth)
	{
		getExpressionValue(expression);
		if (depth[0] > 0)
		{
			getSubExpressions(expression, new DataRequestMonitor<IExpressions.IExpressionDMContext[]>(ImmediateExecutor.getInstance(), null) {

				@Override
				protected void handleSuccess() {
					depth[0] = depth[0] - 1;
					IExpressions.IExpressionDMContext[] subExpressions = getData();
					for (IExpressionDMContext iExpressionDMContext : subExpressions) {
						loadExpressionValues(iExpressionDMContext, depth);
					}
				}});
		}
	}
}
