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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.model.ICastToArray;
import org.eclipse.cdt.debug.edc.EDCDebugger;
import org.eclipse.cdt.debug.edc.formatter.ITypeContentProvider;
import org.eclipse.cdt.debug.edc.formatter.IVariableValueConverter;
import org.eclipse.cdt.debug.edc.internal.IEDCTraceOptions;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.ASTEvaluationEngine;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.IArrayDimensionType;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.InstructionSequence;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.Interpreter;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.OperandValue;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.VariableWithValue;
import org.eclipse.cdt.debug.edc.internal.formatter.FormatExtensionManager;
import org.eclipse.cdt.debug.edc.internal.symbols.CPPBasicType;
import org.eclipse.cdt.debug.edc.internal.symbols.IAggregate;
import org.eclipse.cdt.debug.edc.internal.symbols.IArrayType;
import org.eclipse.cdt.debug.edc.internal.symbols.ICPPBasicType;
import org.eclipse.cdt.debug.edc.internal.symbols.ICompositeType;
import org.eclipse.cdt.debug.edc.internal.symbols.IEnumeration;
import org.eclipse.cdt.debug.edc.internal.symbols.IField;
import org.eclipse.cdt.debug.edc.internal.symbols.IInheritance;
import org.eclipse.cdt.debug.edc.internal.symbols.IPointerType;
import org.eclipse.cdt.debug.edc.internal.symbols.IReferenceType;
import org.eclipse.cdt.debug.edc.internal.symbols.PointerType;
import org.eclipse.cdt.debug.edc.internal.symbols.dwarf.EDCSymbolReader;
import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.cdt.debug.edc.services.AbstractEDCService;
import org.eclipse.cdt.debug.edc.services.DMContext;
import org.eclipse.cdt.debug.edc.services.IEDCExpression;
import org.eclipse.cdt.debug.edc.services.IEDCModuleDMContext;
import org.eclipse.cdt.debug.edc.services.IEDCModules;
import org.eclipse.cdt.debug.edc.services.Stack.StackFrameDMC;
import org.eclipse.cdt.debug.edc.symbols.IDebugInfoProvider;
import org.eclipse.cdt.debug.edc.symbols.IEDCSymbolReader;
import org.eclipse.cdt.debug.edc.symbols.IEnumerator;
import org.eclipse.cdt.debug.edc.symbols.IInvalidVariableLocation;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.debug.edc.symbols.IVariableLocation;
import org.eclipse.cdt.debug.edc.symbols.TypeEngine;
import org.eclipse.cdt.debug.edc.symbols.TypeUtils;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
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
import org.eclipse.debug.core.DebugException;

public class Expressions extends AbstractEDCService implements IExpressions {

	private static int nextExpressionID = 100;

	private Map<String, ICPPBasicType> basicTypes;

	public class ExpressionDMC extends DMContext implements IEDCExpression, ICastToArray {

		private static final String HEX_PREFIX = "0x"; //$NON-NLS-1$
		private static final String OCTAL_PREFIX = "0"; //$NON-NLS-1$
		private static final String BINARY_PREFIX = "0b"; //$NON-NLS-1$
		private static final String SINGLE_QUOTE = "'"; //$NON-NLS-1$
		private static final String DECIMAL_SUFFIX = " (Decimal)"; //$NON-NLS-1$
		private InstructionSequence parsedExpression;
		private final ASTEvaluationEngine engine;
		private final StackFrameDMC frame;
		private Number value;
		private IStatus valueError;
		private IVariableLocation valueLocation;
		private IType valueType;
		private boolean hasChildren = false;
		private String valueString;
		private IType valueTypeCastedTo = null;
		private IType valueTypeOriginal = null;

		public ExpressionDMC(StackFrameDMC frame, String name) {
			super(Expressions.this, new IDMContext[] { frame }, name, nextExpressionID++ + name);
			engine = new ASTEvaluationEngine(getServicesTracker(), frame, frame.getTypeEngine());
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
			if (value != null || valueError != null)
				return;

			String expression = getExpression();

			if (parsedExpression == null) {
				try {
					parsedExpression = engine.getCompiledExpression(expression);
				} catch (CoreException e) {
					value = null;
					valueError = e.getStatus();
					valueLocation = null;
					valueType = null;
					return;
				}
			}

			if (parsedExpression.getInstructions().length == 0) {
				value = null;
				valueError = new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID,
						EDCServicesMessages.Expressions_SyntaxError);
				valueLocation = null;
				valueType = null;
				return;
			}

			Interpreter interpreter;
			try {
				interpreter = engine.evaluateCompiledExpression(parsedExpression);
			} catch (CoreException e) {
				value = null;
				valueError = e.getStatus();
				valueLocation = null;
				valueType = null;
				return;
			}
			
			OperandValue variableValue = interpreter.getResult();
			if (variableValue == null) {
				value = null;
				valueError = null;
				valueLocation = null;
				valueType = null;
				return;
			}
			
			// if we're casting to type, make sure a VariableWithValue returned now has the new type
			if (valueTypeCastedTo != null && variableValue instanceof VariableWithValue)
			{
				((VariableWithValue)variableValue).setType(valueTypeCastedTo);
			}

			try {
				value = variableValue.getValue();
				valueString = variableValue.getStringValue();
			} catch (CoreException e1) {
				value = null;
				valueError = e1.getStatus();
				valueLocation = null;
				valueType = null;
				return;
			}
			valueLocation = variableValue.getValueLocation();
			valueType = variableValue.getValueType();

			// for a structured type or array, return the location and note
			// that it has children
			if (valueType instanceof IAggregate && valueLocation != null) {
				// TODO
				try {
					value = variableValue.getValueLocationAddress();
				} catch (CoreException e) {
					value = null;
					valueError = e.getStatus();
				}
				if (!(value instanceof IInvalidVariableLocation))
					hasChildren = true;
			} else {
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
				value = null;
				valueError = new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID,
						((IInvalidVariableLocation) valueLocation).getMessage());
				valueLocation = null; //$NON-NLS-1$
				return;
			}

		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.internal.services.dsf.IEDCExpression#getFormattedValue(org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext)
		 */
		public FormattedValueDMData getFormattedValue(FormattedValueDMContext dmc) {
			EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.VARIABLE_VALUE_TRACE, dmc);
			evaluateExpression();
			String result = ""; //$NON-NLS-1$

			if (valueError != null) {
				result = valueError.getMessage();
			} else if (value != null) {
				result = value.toString();
				
				IType unqualifiedType = TypeUtils.getStrippedType(valueType);
				
				String temp = null;
				String formatID = dmc.getFormatID();
				
				if (formatID.equals(IFormattedValues.HEX_FORMAT)) {
					temp = toHexString(value);
				} else if (formatID.equals(IFormattedValues.OCTAL_FORMAT)) {
					temp = toOctalString(value);
				} else if (formatID.equals(IFormattedValues.BINARY_FORMAT)) {
					temp = asBinary(value);
				} else if (formatID.equals(IFormattedValues.NATURAL_FORMAT)) {
					// convert non-integer types to original representation
					if (unqualifiedType instanceof ICPPBasicType) {
						ICPPBasicType basicType = (ICPPBasicType) unqualifiedType;
						switch (basicType.getBaseType()) {
						case ICPPBasicType.t_char:
							temp = toCharString(value);
							break;
						case ICPPBasicType.t_wchar_t:
							temp = toCharString(value);
							break;
						case ICPPBasicType.t_bool:
							temp = Boolean.toString(value.longValue() != 0);
							break;
						}
					} else if (valueType instanceof IAggregate || valueType instanceof IPointerType) {
						// show addresses for aggregates and pointers as hex in natural format
						temp = toHexString(value);
					} 
				}
				if (temp != null)
					result = temp; 
				
				// otherwise, leave value as is
				
				// TODO: add type suffix if the value cannot fit in
				// the ordinary range of the base type.
				// E.g., for an unsigned int, 0xFFFFFFFF should usually be 0xFFFFFFFFU,
				// and for a long double, 1.E1000 should be 1.E1000L.
				/*
				// apply required integer and float suffixes
				IType unqualifiedType = TypeUtils.getStrippedType(valueType);
				if (unqualifiedType instanceof ICPPBasicType) {
					ICPPBasicType basicType = (ICPPBasicType) unqualifiedType;
					
					if (basicType.getBaseType() == ICPPBasicType.t_float) {
						//result += "F"; // no
					} else if (basicType.getBaseType() == ICPPBasicType.t_double) {
						if (basicType.isLong() AND actual value does not fit in a double)
							result += "L";
					} else if (basicType.getBaseType() == ICPPBasicType.t_int) {
						if (basicType.isUnsigned() AND actual value does not fit in a signed int)
							result += "U";
						if (basicType.isLongLong() AND actual value does not fit in a signed int)
							result += "LL";
						else if (basicType.isLong() AND actual value does not fit in a signed int)
							result += "L";
					}
				}
				 */
				
				// for an enumerator, return the name, if any
				if (unqualifiedType instanceof IEnumeration) {
					long enumeratorValue = value.longValue();

					IEnumerator enumerator = ((IEnumeration) unqualifiedType).getEnumeratorByValue(enumeratorValue);
					if (enumerator != null) {
						result = enumerator.getName() + " [" + result + "]"; //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
				
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
		
			String prefix = ""; //$NON-NLS-1$
			if (valueType instanceof ICPPBasicType && ((ICPPBasicType) valueType).getBaseType() == ICPPBasicType.t_wchar_t)
				prefix = "L"; //$NON-NLS-1$
			return prefix + asStringQuoted(Character.toString((char) value.shortValue()));
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
		public IVariableLocation getValueLocation() {
			evaluateExpression();
			return getEvaluatedLocation();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.services.IEDCExpression#getEvaluationError()
		 */
		public IStatus getEvaluationError() {
			return valueError;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.internal.services.dsf.IEDCExpression#getEvaluatedValue()
		 */
		public Number getEvaluatedValue() {
			return value;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.services.IEDCExpression#getEvaluatedValueString()
		 */
		public String getEvaluatedValueString() {
			if (valueError != null)
				return valueError.getMessage();
			
			if (valueString != null)
				return valueString;
			
			valueString = value != null ? value.toString() : ""; //$NON-NLS-1$
			return valueString;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.services.IEDCExpression#setEvaluatedValueString(java.lang.String)
		 */
		public void setEvaluatedValueString(String string) {
			this.valueString = string;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.internal.services.dsf.IEDCExpression#setEvaluatedValue(java.lang.Object)
		 */
		public void setEvaluatedValue(Number value) {
			this.value = value;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.internal.services.dsf.IEDCExpression#getEvaluatedLocation()
		 */
		public IVariableLocation getEvaluatedLocation() {
			return valueLocation;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.internal.services.dsf.IEDCExpression#getEvaluatedType()
		 */
		public IType getEvaluatedType() {
			return valueType;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.internal.services.dsf.IEDCExpression#getTypeName()
		 */
		public String getTypeName() {
			evaluateExpression();
			if (valueType == null)
				if (valueError != null)
					return ""; //$NON-NLS-1$
				else
					return ASTEvaluationEngine.UNKNOWN_TYPE;
			return engine.getTypeEngine().getTypeName(valueType);
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

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.cdt.debug.core.model.ICastToType#canCast()
		 */
		public boolean canCast() {
			return valueType instanceof IType && valueError == null;
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.cdt.debug.core.model.ICastToType#getCurrentType()
		 */
		public String getCurrentType() {
			if (valueType instanceof IType)
				return ((IType)valueType).getName();
			return ""; //$NON-NLS-1$
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.cdt.debug.core.model.ICastToType#cast(java.lang.String)
		 */
		public void cast(String type) throws DebugException {
			if (valueError != null)
				throw new DebugException(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, EDCServicesMessages.Expressions_NoCurrentValue));

			type = type.trim();
			StringBuilder sb = new StringBuilder(type.length());
			
			// replace multiple spaces with single spaces
			char previous = '\0';
			for (int i = 0; i < type.length(); i++) {
				char next = type.charAt(i);
				if (next == ' ' && previous == ' ')
					continue;
				sb.append(next);
				previous = next;
			}

			// count the '*' indirections at the end, and strip them
			int indirections = 0;
			
			int length = sb.length() - 1;
			for ( ; length >= 0; length--) {
				if (sb.charAt(length) == '*') {
					indirections++;
				} else if (sb.charAt(length) != ' ') {
					break;
				}
			}
			sb.setLength(length + 1);

			if (sb.length() == 0)
				throw new DebugException(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, EDCServicesMessages.Expressions_InvalidType + type));

			String canonicalType = sb.toString();

			// create the linked type structure for indirections
			IType indirectionType = null;
			IType innermostIndirectionType =  null;
			IType lastIndirectionType = null;
			for (int i = indirections; i > 0; i--) {
				innermostIndirectionType = new PointerType("", null, 4, null); //$NON-NLS-1$
				if (indirectionType == null)
					indirectionType = innermostIndirectionType;
				else
					lastIndirectionType.setType(innermostIndirectionType);
				lastIndirectionType = innermostIndirectionType;
			}
			
			// ask the debug info provider to match the type
			Query<IDebugInfoProvider> runnableIDIP = new Query<IDebugInfoProvider>() {
				@Override
				protected void execute(DataRequestMonitor<IDebugInfoProvider> rm) {

					IDebugInfoProvider debugInfoProvider = null;
	
					IEDCModules modules = getServicesTracker().getService(IEDCModules.class);
					if (modules != null) {
						IEDCModuleDMContext module = modules.getModuleByAddress(frame.getExecutionDMC().getSymbolDMContext(),
														frame.getIPAddress());
						if (module != null) {
							IEDCSymbolReader symbolReader = module.getSymbolReader();
							if (symbolReader instanceof EDCSymbolReader) {
								debugInfoProvider = ((EDCSymbolReader) symbolReader).getDebugInfoProvider();
							}
						}
					}					
					rm.setData(debugInfoProvider);
					rm.done();
				}
			};

			getExecutor().execute(runnableIDIP);

			IDebugInfoProvider debugInfoProvider = null;
			
			try {
				debugInfoProvider = runnableIDIP.get();
			} catch (InterruptedException e) {
			} catch (ExecutionException e) {
			}

//			IEDCSymbolReader symbolReader = Symbols.getSymbolReader(new Path(frame.getSourceFile()));
//			if (symbolReader instanceof EDCSymbolReader) {
//				debugInfoProvider = ((EDCSymbolReader) symbolReader).getDebugInfoProvider();
//			}

			IType typeCastedTo = null;
			
			if (debugInfoProvider != null) {
				Collection<IType> list = debugInfoProvider.getTypesByName(type);
				if (!list.isEmpty())
					typeCastedTo = (IType)list.toArray()[0];
			}

			// if we haven't found the type, try the C++/C base types
			if (typeCastedTo == null)
				typeCastedTo = tryCastToBaseType(engine.getTypeEngine(), canonicalType); 
			
			if (typeCastedTo == null)
				throw new DebugException(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, EDCServicesMessages.Expressions_UnknownType + type));
			
			if (indirectionType != null) {
				innermostIndirectionType.setType(typeCastedTo);
				typeCastedTo = indirectionType;
			}

			valueTypeCastedTo = typeCastedTo;

			if (valueTypeOriginal == null)
				valueTypeOriginal = valueType;

			value = null; // allow expression to be re-evaluated with new type
			final ExpressionDMC expression = this;
			
			Query<ExpressionDMC> runnable = new Query<ExpressionDMC>() {
				@Override
				protected void execute(DataRequestMonitor<ExpressionDMC> rm) {
					evaluateExpression();
					rm.setData(expression);
					rm.done();
				}
			};
			
			getExecutor().execute(runnable);
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.cdt.debug.core.model.ICastToType#restoreOriginal()
		 */
		public void restoreOriginal() throws DebugException {
			valueType = valueTypeOriginal;
			valueTypeCastedTo = null;
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.cdt.debug.core.model.ICastToType#isCasted()
		 */
		public boolean isCasted() {
			return valueTypeCastedTo != null;
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.cdt.debug.core.model.ICastToArray#canCastToArray()
		 */
		public boolean canCastToArray() {
			return TypeUtils.getStrippedType(valueType) instanceof IPointerType && valueError == null;
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.cdt.debug.core.model.ICastToArray#castToArray(int, int)
		 */
		public void castToArray(int startIndex, int length)
				throws DebugException {
			// TODO Auto-generated method stub
			
		}
		
		private IType tryCastToBaseType(TypeEngine typeEngine, String type) {
			IType castedToType = null;
			
			// if the type name is one of the many allowed alternative versions of a C/C++,
			// synthesize a base type
			if (type.equals("int") || type.equals("signed")|| type.equals("signed int")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				castedToType = new CPPBasicType("int", ICPPBasicType.t_int, ICPPBasicType.IS_SIGNED, //$NON-NLS-1$
						typeEngine.getTypeSize(TypeUtils.BASIC_TYPE_INT));
			} else if (type.equals("long") || type.equals("long int") || //$NON-NLS-1$ //$NON-NLS-2$
						type.equals("signed long") || type.equals("signed long int")) { //$NON-NLS-1$ //$NON-NLS-2$
				castedToType = new CPPBasicType("long", ICPPBasicType.t_int, ICPPBasicType.IS_LONG | ICPPBasicType.IS_SIGNED, //$NON-NLS-1$
						typeEngine.getTypeSize(TypeUtils.BASIC_TYPE_LONG));
			} else if (type.equals("short") || type.equals("short int") || //$NON-NLS-1$ //$NON-NLS-2$
						type.equals("signed short") || type.equals("signed short int")) { //$NON-NLS-1$ //$NON-NLS-2$
				castedToType = new CPPBasicType("short", ICPPBasicType.t_int, ICPPBasicType.IS_SHORT | ICPPBasicType.IS_SIGNED, //$NON-NLS-1$
						typeEngine.getTypeSize(TypeUtils.BASIC_TYPE_SHORT));
			} else if (type.equals("unsigned") || type.equals("unsigned int")) { //$NON-NLS-1$ //$NON-NLS-2$
				castedToType = new CPPBasicType("unsigned int", ICPPBasicType.t_int, ICPPBasicType.IS_UNSIGNED, //$NON-NLS-1$
						typeEngine.getTypeSize(TypeUtils.BASIC_TYPE_INT_UNSIGNED));
			} else if (type.equals("unsigned long") || type.equals("unsigned long int")) { //$NON-NLS-1$ //$NON-NLS-2$
				castedToType = new CPPBasicType("unsigned long", ICPPBasicType.t_int, ICPPBasicType.IS_LONG | ICPPBasicType.IS_UNSIGNED, //$NON-NLS-1$
						typeEngine.getTypeSize(TypeUtils.BASIC_TYPE_LONG_UNSIGNED));
			} else if (type.equals("unsigned short") || type.equals("unsigned short int")) { //$NON-NLS-1$ //$NON-NLS-2$
				castedToType = new CPPBasicType("unsigned short", ICPPBasicType.t_int, ICPPBasicType.IS_SHORT | ICPPBasicType.IS_UNSIGNED, //$NON-NLS-1$
						typeEngine.getTypeSize(TypeUtils.BASIC_TYPE_SHORT_UNSIGNED));
			} else if (type.equals("char")) { //$NON-NLS-1$
				castedToType = new CPPBasicType(type, ICPPBasicType.t_char, 0,
						typeEngine.getTypeSize(TypeUtils.BASIC_TYPE_CHAR));
			} else if (type.equals("unsigned char")) { //$NON-NLS-1$
				castedToType = new CPPBasicType(type, ICPPBasicType.t_char, ICPPBasicType.IS_UNSIGNED,
						typeEngine.getTypeSize(TypeUtils.BASIC_TYPE_CHAR_UNSIGNED));
			} else if (type.equals("signed char")) { //$NON-NLS-1$
				castedToType = new CPPBasicType(type, ICPPBasicType.t_char, ICPPBasicType.IS_SIGNED,
						typeEngine.getTypeSize(TypeUtils.BASIC_TYPE_CHAR_SIGNED));
			} else if (type.equals("bool")) { //$NON-NLS-1$
				castedToType = new CPPBasicType(type, ICPPBasicType.t_bool, 0,
						typeEngine.getTypeSize(TypeUtils.BASIC_TYPE_BOOL));
			} else if (type.equals("_Bool")) { //$NON-NLS-1$
				castedToType = new CPPBasicType(type, ICPPBasicType.t_bool, 0,
						typeEngine.getTypeSize(TypeUtils.BASIC_TYPE_BOOL));
			} else if (type.equals("float")) { //$NON-NLS-1$
				castedToType = new CPPBasicType(type, ICPPBasicType.t_float, 0,
						typeEngine.getTypeSize(TypeUtils.BASIC_TYPE_FLOAT));
			} else if (type.equals("double")) { //$NON-NLS-1$
				castedToType = new CPPBasicType(type, ICPPBasicType.t_float, ICPPBasicType.IS_LONG,
						typeEngine.getTypeSize(TypeUtils.BASIC_TYPE_DOUBLE));
//			} else if (type.equals("long double")) { //$NON-NLS-1$
//				// TODO support long double
			} else if (type.equals("wchar_t")) { //$NON-NLS-1$
				castedToType = new CPPBasicType(type, ICPPBasicType.t_wchar_t, ICPPBasicType.IS_LONG,
						typeEngine.getTypeSize(TypeUtils.BASIC_TYPE_WCHAR_T));
			} else if (type.equals("long long") || type.equals("long long int") || //$NON-NLS-1$ //$NON-NLS-2$
					type.equals("signed long long") || type.equals("signed long long int")) { //$NON-NLS-1$ //$NON-NLS-2$
				castedToType = new CPPBasicType("long long", ICPPBasicType.t_int, ICPPBasicType.IS_LONG_LONG | ICPPBasicType.IS_SIGNED, //$NON-NLS-1$
						typeEngine.getTypeSize(TypeUtils.BASIC_TYPE_LONG_LONG));
			} else if (type.equals("unsigned long long") || type.equals("unsigned long long int")) { //$NON-NLS-1$ //$NON-NLS-2$
				castedToType = new CPPBasicType("unsigned long long", ICPPBasicType.t_int, ICPPBasicType.IS_LONG_LONG | ICPPBasicType.IS_UNSIGNED, //$NON-NLS-1$
						typeEngine.getTypeSize(TypeUtils.BASIC_TYPE_LONG_LONG_UNSIGNED));
			}
			
			return castedToType;
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

		private final IVariableLocation valueLocation;

		public ExpressionDMAddress(IExpressionDMContext exprContext) {
			if (exprContext instanceof ExpressionDMC)
				valueLocation = ((IEDCExpression) exprContext).getValueLocation();
			else
				valueLocation = null;
		}

		public IAddress getAddress() {
			if (valueLocation != null) {
				IAddress address = valueLocation.getAddress();
				if (address != null)
					return address;
			}
			return new Addr64(BigInteger.ZERO);
		}

		public int getSize() {
			return 4;
		}

		public String getLocation() {
			if (valueLocation instanceof IInvalidVariableLocation) {
				return ((IInvalidVariableLocation)valueLocation).getMessage();
			}
			if (valueLocation == null)
				return ""; //$NON-NLS-1$
			return valueLocation.getLocationName();
		}

	}

	public Expressions(DsfSession session) {
		super(session, new String[] { IExpressions.class.getName(), Expressions.class.getName() });
	}
	
	public boolean canWriteExpression(ExpressionDMC expressionDMC) {
		EDCLaunch launch = EDCLaunch.getLaunchForSession(getSession().getId());
		if (launch.isSnapshotLaunch())
			return false;
		IVariableValueConverter converter = getCustomValueConverter(expressionDMC);
		if (converter != null)
			return converter.canEditValue();
		
		return !isComposite(expressionDMC);
	}

	public void canWriteExpression(IExpressionDMContext exprContext, DataRequestMonitor<Boolean> rm) {
		ExpressionDMC expressionDMC = (ExpressionDMC) exprContext;
		rm.setData(canWriteExpression(expressionDMC));
		rm.done();
	}

	private boolean isComposite(ExpressionDMC expressionDMC) {
		IType exprType = TypeUtils.getStrippedType(expressionDMC.getEvaluatedType());
		return exprType instanceof ICompositeType;
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

	public void getSubExpressionCount(IExpressionDMContext exprContext, final DataRequestMonitor<Integer> rm) {
		// TODO: maybe cache these subexpressions; they are just requested again in #getSubExpressions()
		getSubExpressions(exprContext, new DataRequestMonitor<IExpressions.IExpressionDMContext[]>(
				getExecutor(), rm) {
			/* (non-Javadoc)
			 * @see org.eclipse.cdt.dsf.concurrent.RequestMonitor#handleSuccess()
			 */
			@Override
			protected void handleSuccess() {
				rm.setData(getData().length);
				rm.done();
			}
		});
	}

	public void getSubExpressions(IExpressionDMContext exprContext, DataRequestMonitor<IExpressionDMContext[]> rm) {
		if (!(exprContext instanceof ExpressionDMC) || ((IEDCExpression) exprContext).getFrame() == null) {
			rm.setData(new ExpressionDMC[0]);
			rm.done();
			return;
		}

		ExpressionDMC expr = (ExpressionDMC) exprContext;

		// if expression has no evaluated value, then it has not yet been evaluated
		if (expr.getEvaluatedValue() == null && expr.getEvaluatedValueString() != null) {
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
				if (field.getName().length() == 0) {
					// This makes an invalid expression
					// The debug info provider should have filtered out or renamed such fields
					assert false;
					continue;
				}
				exprChild = new ExpressionDMC(frame, exprName + field.getName());
				if (exprChild != null) {
					exprList.add(exprChild);
				}
			}
			
			IInheritance[] inheritedFrom = compositeType.getInheritances();
			for (IInheritance inherited : inheritedFrom) {
				if (inherited.getName().length() == 0) {
					// This makes an invalid expression
					// The debug info provider should have filtered out or renamed such fields
					assert false;
					continue;
				}
				exprChild = new ExpressionDMC(frame, exprName + inherited.getName());
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

		// If expression name already starts with "&" (e.g. "&struct"), indirect it first
		boolean indirected = false;
		if ((exprType instanceof IPointerType) && exprName.startsWith("&")) { //$NON-NLS-1$
			exprName = exprName.substring(1);
			exprChild = new ExpressionDMC(frame, exprName);
			indirected = true;
		}

		// a pointer type has one child
		IType typePointedTo = TypeUtils.getStrippedType(exprType.getType());

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
			if (exprType instanceof IReferenceType || indirected)
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

	public void getSubExpressions(IExpressionDMContext exprContext, final int startIndex_, final int length_,
			final DataRequestMonitor<IExpressionDMContext[]> rm) {
		getSubExpressions(exprContext, new DataRequestMonitor<IExpressionDMContext[]>(getExecutor(), rm) {
			@Override
			protected void handleSuccess() {
				IExpressionDMContext[] allExprs = getData();
				if (startIndex_ == 0 && length_ >= allExprs.length) {
					rm.setData(allExprs);
					rm.done();
				} else {
					int startIndex = startIndex_, length = length_;
					if (startIndex > allExprs.length) {
						startIndex = allExprs.length;
						length = 0;
					} else if (startIndex + length > allExprs.length) {
						length = allExprs.length - startIndex;
						if (length < 0)
							length = 0;
					}
						
					IExpressionDMContext[] result = new IExpressionDMContext[length];
					System.arraycopy(allExprs, startIndex, result, 0, length);
					rm.setData(result);
					rm.done();
				}
			}
		});
	}

	public void writeExpression(IExpressionDMContext exprContext, String expressionValue, String formatId, RequestMonitor rm) {
		ExpressionDMC expressionDMC = (ExpressionDMC) exprContext;
		if (isComposite(expressionDMC)) {
			rm.setStatus(EDCDebugger.dsfRequestFailedStatus(EDCServicesMessages.Expressions_CannotModifyCompositeValue, null));
			rm.done();
			return;
		}
		// first try to get value by format as BigInteger
		BigInteger value = parseIntegerByFormat(expressionValue, formatId);
        if (value == null) {
       		// TODO parse as expression
        	rm.setStatus(EDCDebugger.dsfRequestFailedStatus(EDCServicesMessages.Expressions_CannotParseExpression, null));
			rm.done();
			return;
        }
        
        IVariableLocation variableLocation = expressionDMC.getValueLocation();
		IType exprType = TypeUtils.getStrippedType(expressionDMC.getEvaluatedType());
    	try {
    		variableLocation.writeValue(exprType.getByteSize(), value);
		} catch (CoreException e) {
			rm.setStatus(e.getStatus());
		}
        
		rm.done();
	}

	private BigInteger parseIntegerByFormat(String expressionValue, String formatId) {
		int radix = 10;
		if (HEX_FORMAT.equals(formatId)) {
			if (expressionValue.startsWith(ExpressionDMC.HEX_PREFIX)) 
				expressionValue = expressionValue.substring(ExpressionDMC.HEX_PREFIX.length());
			radix = 16;
		} else if (OCTAL_FORMAT.equals(formatId)) {
			if (expressionValue.startsWith(ExpressionDMC.OCTAL_PREFIX)) 
				expressionValue = expressionValue.substring(ExpressionDMC.OCTAL_PREFIX.length()); 
			radix = 8;
		} else if (BINARY_FORMAT.equals(formatId)) {
			if (expressionValue.startsWith(ExpressionDMC.BINARY_PREFIX)) 
				expressionValue = expressionValue.substring(ExpressionDMC.BINARY_PREFIX.length()); 
			radix = 2;
		} else if (NATURAL_FORMAT.equals(formatId)) {
			if (expressionValue.startsWith(ExpressionDMC.BINARY_PREFIX)) {
				expressionValue = expressionValue.substring(ExpressionDMC.BINARY_PREFIX.length());
				radix = 2;
			} else if (expressionValue.startsWith(ExpressionDMC.OCTAL_PREFIX)) { 
				expressionValue = expressionValue.substring(ExpressionDMC.OCTAL_PREFIX.length());
				radix = 8;
			} else if (expressionValue.startsWith(ExpressionDMC.HEX_PREFIX)) { 
				expressionValue = expressionValue.substring(ExpressionDMC.HEX_PREFIX.length());
				radix = 16;
			} 
			// else, decimal
		}
        try {
        	return new BigInteger(expressionValue, radix);
        } catch (NumberFormatException e) {
        	// just return null
        }
        
        return null;
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

			if (exprDMC != null && exprDMC.getEvaluationError() != null) {
				rm.setStatus(exprDMC.getEvaluationError());
				rm.done();
				return;
			}
			
			formattedValue = exprDMC.getFormattedValue(formattedDataContext); // must call this to get type
			IVariableValueConverter customConverter = getCustomValueConverter(exprDMC);
			if (customConverter != null) {
				FormattedValueDMData customFormattedValue = null;
				try {
					customFormattedValue = new FormattedValueDMData(customConverter.getValue(exprDMC));
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
							EDCServicesMessages.Expressions_ErrorInVariableFormatter + customConverter.getClass().getName(), t);
				}
			}
		} else
			formattedValue = new FormattedValueDMData(""); //$NON-NLS-1$

		rm.setData(formattedValue);
		rm.done();
	}

	private IVariableValueConverter getCustomValueConverter(ExpressionDMC exprDMC) {
		IType exprType = TypeUtils.getStrippedType(exprDMC.getEvaluatedType());
		return FormatExtensionManager.instance().getVariableValueConverter(exprType);
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
