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
package org.eclipse.cdt.debug.edc.internal.services.dsf;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.debug.edc.MemoryUtils;
import org.eclipse.cdt.debug.edc.formatter.ITypeContentProvider;
import org.eclipse.cdt.debug.edc.formatter.IVariableValueConverter;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.EDCTrace;
import org.eclipse.cdt.debug.edc.internal.NumberFormatUtils;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.ASTEvaluationEngine;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.IArrayDimensionType;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.InstructionSequence;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.Interpreter;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.OperandValue;
import org.eclipse.cdt.debug.edc.internal.formatter.FormatExtensionManager;
import org.eclipse.cdt.debug.edc.internal.symbols.IAggregate;
import org.eclipse.cdt.debug.edc.internal.symbols.IArrayType;
import org.eclipse.cdt.debug.edc.internal.symbols.ICPPBasicType;
import org.eclipse.cdt.debug.edc.internal.symbols.ICompositeType;
import org.eclipse.cdt.debug.edc.internal.symbols.IEnumeration;
import org.eclipse.cdt.debug.edc.internal.symbols.IField;
import org.eclipse.cdt.debug.edc.internal.symbols.IInheritance;
import org.eclipse.cdt.debug.edc.internal.symbols.IPointerType;
import org.eclipse.cdt.debug.edc.internal.symbols.IReferenceType;
import org.eclipse.cdt.debug.edc.internal.symbols.ISubroutineType;
import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.cdt.debug.edc.services.AbstractEDCService;
import org.eclipse.cdt.debug.edc.services.DMContext;
import org.eclipse.cdt.debug.edc.services.IEDCDMContext;
import org.eclipse.cdt.debug.edc.services.IEDCExpression;
import org.eclipse.cdt.debug.edc.services.IEDCExpressions;
import org.eclipse.cdt.debug.edc.services.Stack.StackFrameDMC;
import org.eclipse.cdt.debug.edc.symbols.IEnumerator;
import org.eclipse.cdt.debug.edc.symbols.IInvalidVariableLocation;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.debug.edc.symbols.IVariableLocation;
import org.eclipse.cdt.debug.edc.symbols.TypeUtils;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.AbstractDMEvent;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions2;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IModules.ISymbolDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public class Expressions extends AbstractEDCService implements IEDCExpressions {

	public abstract class BaseEDCExpressionDMC extends DMContext implements IEDCExpression {
		protected String expression;
		private InstructionSequence parsedExpression;
		private final ASTEvaluationEngine engine;
		private final StackFrameDMC frame;
		protected Number value;
		protected IStatus valueError;
		private IVariableLocation valueLocation;
		private IType valueType;
		private boolean hasChildren = false;
		private String valueString;

		public BaseEDCExpressionDMC(IDMContext parent, String expression, String name) {
			super(Expressions.this, new IDMContext[] { parent }, name, ((IEDCDMContext)parent).getID() + "." + name); //$NON-NLS-1$
			this.expression = expression;
			this.frame = DMContexts.getAncestorOfType(parent, StackFrameDMC.class);
			engine = new ASTEvaluationEngine(getEDCServicesTracker(), frame, frame.getTypeEngine());
		}
		
		public BaseEDCExpressionDMC(IDMContext parent, String expression) {
			this(parent, expression, expression);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.services.DMContext#toString()
		 */
		@Override
		public String toString() {
			return getExpression();
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
			return expression;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.internal.services.dsf.IEDCExpression#evaluateExpression()
		 */
		public synchronized void evaluateExpression() {
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

			valueLocation = variableValue.getValueLocation();
			valueType = variableValue.getValueType();
			try {
				value = variableValue.getValue();
				valueString = variableValue.getStringValue();
			} catch (CoreException e1) {
				value = null;
				valueError = e1.getStatus();
				return;
			}

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
			if (EDCTrace.VARIABLE_VALUE_TRACE_ON) { EDCTrace.getTrace().traceEntry(null, EDCTrace.fixArg(dmc)); }
			evaluateExpression();
			String result = ""; //$NON-NLS-1$

			if (valueError != null) {
				result = valueError.getMessage();
			} else if (value != null) {
				result = value.toString();
				
				IType unqualifiedType = TypeUtils.getUnRefStrippedType(valueType);
				
				String temp = null;
				String formatID = dmc.getFormatID();
				
				// the non-natural formats have expected representations in other
				// parts of DSF, so be strict about what we return
				if (formatID.equals(IFormattedValues.HEX_FORMAT)) {
					temp = NumberFormatUtils.toHexString(value);
				} else if (formatID.equals(IFormattedValues.OCTAL_FORMAT)) {
					temp = NumberFormatUtils.toOctalString(value);
				} else if (formatID.equals(IFormattedValues.BINARY_FORMAT)) {
					temp = NumberFormatUtils.asBinary(value);
				} else if (formatID.equals(IFormattedValues.NATURAL_FORMAT)) {
					// convert non-integer types to original representation
					if (unqualifiedType instanceof ICPPBasicType) {
						ICPPBasicType basicType = (ICPPBasicType) unqualifiedType;
						switch (basicType.getBaseType()) {
						case ICPPBasicType.t_char:
							temp = NumberFormatUtils.toCharString(value, valueType);
							break;
						case ICPPBasicType.t_wchar_t:
							temp = NumberFormatUtils.toCharString(value, valueType);
							break;
						case ICPPBasicType.t_bool:
							temp = Boolean.toString(value.longValue() != 0);
							break;
						default:
							// account for other debug formats
							if (basicType.getName().equals("wchar_t")) { //$NON-NLS-1$
								temp = NumberFormatUtils.toCharString(value, valueType);
							}
							break;
						}
					} else if (unqualifiedType instanceof IAggregate || unqualifiedType instanceof IPointerType) {
						// show addresses for aggregates and pointers as hex in natural format
						temp = NumberFormatUtils.toHexString(value);
					} 
					
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
							if (temp == null)
								temp = result;
							
							temp = enumerator.getName() + " [" + temp + "]"; //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
				}
				if (temp != null)
					result = temp; 
				
				// otherwise, leave value as is
				
				
			}
			if (EDCTrace.VARIABLE_VALUE_TRACE_ON) { EDCTrace.getTrace().traceExit(null, EDCTrace.fixArg(result)); }
			return new FormattedValueDMData(result);
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
		public Expressions getExpressionsService() {
			return Expressions.this;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.services.IEDCExpression#getExecutor()
		 */
		public Executor getExecutor() {
			return getSession().getExecutor();
		}
	
	}

	/** A basic expression.  */
	private class ExpressionDMC extends BaseEDCExpressionDMC {

		public ExpressionDMC(IDMContext parent, String expression) {
			super(parent, expression);
		}
		
		
		public ExpressionDMC(IDMContext parent, String expression, String name) {
			super(parent, expression, name);
		}
		
		/**
		 * There is no casting on a vanilla expression.
		 * @return <code>null</code>
		 */
		public CastInfo getCastInfo() {
			return null;
		}

		
	}

	/** A casted or array-displayed expression.  */
	private class CastedExpressionDMC extends BaseEDCExpressionDMC implements ICastedExpressionDMContext {

		private final CastInfo castInfo;
		/** if non-null, interpret result as this type rather than the raw expression's type */
		private IType castType = null;
		private IStatus castError;

		public CastedExpressionDMC(IEDCExpression exprDMC, String expression, String name, CastInfo castInfo) {
			super(exprDMC, name);
			this.castInfo = castInfo;
			
			String castType = castInfo.getTypeString();
			
			String castExpression = expression;
			
			// If changing type, assume it's reinterpret_cast<>. 
			// Once we support RTTI, this should be dynamic_cast<> when casting
			// class pointers to class pointers.
			if (castType != null) {
				if (castInfo.getArrayCount() > 0) {
					castType += "[]"; //$NON-NLS-1$
					// Force non-pointer expressions to be pointers.
					exprDMC.evaluateExpression();
					IType exprType = TypeUtils.getStrippedType(exprDMC.getEvaluatedType());
					if (exprType != null) {
						if (!(exprType instanceof IPointerType || exprType instanceof IArrayType)) {
							expression = "&" + expression; //$NON-NLS-1$
						}
					}
				}
				castExpression = "reinterpret_cast<" + castType +">(" + expression + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			} else if (castInfo.getArrayCount() > 0) {
				// For arrays, be sure the OperatorSubscript accepts the base type.
				// Force non-pointer expressions to be pointers.
				exprDMC.evaluateExpression();
				IType exprType = TypeUtils.getStrippedType(exprDMC.getEvaluatedType());
				if (exprType != null) {
					if (!(exprType instanceof IPointerType || exprType instanceof IArrayType)) {
						// cast to pointer if not already one (cast to array is not valid C/C++ but we support it)
						castExpression = "("  + exprDMC.getTypeName() + "[])&" + expression; //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			}
			this.expression = castExpression;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.internal.services.dsf.IEDCExpression#evaluateExpression()
		 */
		public void evaluateExpression() {
			if (castError != null) {
				return;
			}
			
			super.evaluateExpression();
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.internal.services.dsf.IEDCExpression#getEvaluatedType()
		 */
		public IType getEvaluatedType() {
			if (castType != null)
				return castType;
			return super.getEvaluatedType();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.dsf.debug.service.IExpressions2.ICastedExpressionDMContext#getCastInfo()
		 */
		public CastInfo getCastInfo() {
			return castInfo;
		}
	}

	public class ExpressionData implements IExpressionDMData {

		private final IEDCExpression dmc;
		private String typeName = "";

		public ExpressionData(IEDCExpression dmc) {
			this.dmc = dmc;
			if (dmc != null)
				this.typeName = dmc.getTypeName();
		}

		public BasicType getBasicType() {
			BasicType basicType = BasicType.unknown;
			if (dmc == null)
				return basicType;
			
			IType type = dmc.getEvaluatedType();
			type = TypeUtils.getStrippedType(type);
			if (type instanceof IArrayType) {
				basicType = BasicType.array;
			}
			else if (type instanceof IBasicType) {
				basicType = BasicType.basic;
			}
			else if (type instanceof ICompositeType) {
				basicType = BasicType.composite;
			}
			else if (type instanceof IEnumeration) {
				basicType = BasicType.enumeration;
			}
			else if (type instanceof IPointerType) {
				basicType = BasicType.pointer;
			}
			else if (type instanceof ISubroutineType) {
				basicType = BasicType.function;
			}
			return basicType;
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
			return typeName;
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
			if (exprContext instanceof IEDCExpression)
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
		super(session, new String[] { IExpressions.class.getName(), Expressions.class.getName(), IExpressions2.class.getName() });
	}
	
	public boolean canWriteExpression(IEDCExpression expressionDMC) {
		EDCLaunch launch = EDCLaunch.getLaunchForSession(getSession().getId());
		if (launch.isSnapshotLaunch())
			return false;
		IVariableValueConverter converter = getCustomValueConverter(expressionDMC);
		if (converter != null)
			return converter.canEditValue();
		
		return !isComposite(expressionDMC);
	}

	public void canWriteExpression(IExpressionDMContext exprContext, DataRequestMonitor<Boolean> rm) {
		IEDCExpression expressionDMC = (IEDCExpression) exprContext;
		rm.setData(canWriteExpression(expressionDMC));
		rm.done();
	}

	private boolean isComposite(IEDCExpression expressionDMC) {
		IType exprType = TypeUtils.getStrippedType(expressionDMC.getEvaluatedType());
		return exprType instanceof ICompositeType;
	}

	public IExpressionDMContext createExpression(IDMContext context, String expression) {
		StackFrameDMC frameDmc = DMContexts.getAncestorOfType(context, StackFrameDMC.class);

		if (frameDmc != null) {
			return new ExpressionDMC(frameDmc, expression);
		}
		return new InvalidContextExpressionDMC(getSession().getId(), expression, context);
	}
	
	class CastInfoCachedData  {

		private CastInfo info;

		private IType type;
		private IStatus error;
		private StackFrameDMC frameDmc;

		public CastInfoCachedData(ExpressionDMC exprDMC, CastInfo info) {
			this.info = info;
			this.frameDmc = DMContexts.getAncestorOfType(exprDMC, StackFrameDMC.class);
		}
		
		public String getTypeString() {
			return info.getTypeString();
		}
		
		public int getArrayStartIndex() {
			return info.getArrayStartIndex();
		}
		
		public int getArrayCount() {
			return info.getArrayCount();
		}
		
		/**
		 * Get the compiled type
		 * @return the type
		 */
		public IType getType() {
			if (info.getTypeString() == null)
				return null;
			
			if (type == null && error == null) {
				if (frameDmc != null) {
					ASTEvaluationEngine engine = new ASTEvaluationEngine(getEDCServicesTracker(), frameDmc, frameDmc.getTypeEngine());
					try {
						IASTTypeId typeId = engine.getCompiledType(info.getTypeString());
						type = engine.getTypeEngine().getTypeForTypeId(typeId);
					} catch (CoreException e) {
						error = e.getStatus();
					}
				} else {
					error = EDCDebugger.dsfRequestFailedStatus(EDCServicesMessages.Expressions_CannotCastOutsideFrame, null); 
				}
			}
			return type;
		}
		
		/**
		 * @return the error
		 */
		public IStatus getError() {
			if (type == null && error == null) {
				getType();
			}
			return error;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IExpressions2#createCastedExpression(org.eclipse.cdt.dsf.datamodel.IDMContext, java.lang.String, org.eclipse.cdt.dsf.debug.service.IExpressions2.ICastedExpressionDMContext)
	 */
	public ICastedExpressionDMContext createCastedExpression(IExpressionDMContext exprDMC,
			CastInfo castInfo) {
		
		// then apply the casting stuff
		if (exprDMC instanceof IEDCExpression) {
			CastedExpressionDMC castedDMC = new CastedExpressionDMC((IEDCExpression) exprDMC, 
					exprDMC.getExpression(), ((IEDCExpression) exprDMC).getName(), castInfo);
			return castedDMC;
		} else {
			assert false;
			return null;
		}
	}

	/*
	public void createCastedExpression(IDMContext context, String expression, 
			ICastedExpressionDMContext castDMC, IArrayCastedExpressionDMContext arrayCastDMC,
			DataRequestMonitor<IExpressionDMContext> rm) {
		
		// create an ordinary expression...
		IExpressionDMContext exprDMC = createExpression(context, expression);
		
		// then apply the casting stuff
		if (exprDMC instanceof ExpressionDMC 
				&& (castDMC == null || castDMC instanceof CastedExpressionDMContext)
				&& (arrayCastDMC == null || arrayCastDMC instanceof ArrayCastedExpressionDMContext)) {
			ExpressionDMC expressionDMC = ((ExpressionDMC) exprDMC);
			if (castDMC != null)
				expressionDMC.setCastToType((CastedExpressionDMContext) castDMC);
			if (arrayCastDMC != null)
				expressionDMC.setArrayCast((ArrayCastedExpressionDMContext) arrayCastDMC);
			rm.setData(expressionDMC);
			rm.done();
		} else {
			assert false;
			rm.setStatus(EDCDebugger.dsfRequestFailedStatus("unexpected cast information", null));
			rm.done();
		}
	}
	*/
	
	public void getBaseExpressions(IExpressionDMContext exprContext, DataRequestMonitor<IExpressionDMContext[]> rm) {
		rm.setData(new IEDCExpression[0]);
		rm.done();
	}

	public void getExpressionAddressData(final IExpressionDMContext exprContext, final DataRequestMonitor<IExpressionDMAddress> rm) {
		asyncExec(new Runnable() {
			public void run() {
				if (exprContext instanceof IEDCExpression)
					rm.setData(new ExpressionDMAddress(exprContext));
				else
					rm.setData(new ExpressionDMAddress(null));
				rm.done();
			}
		}, rm);
	}

	public void getExpressionData(final IExpressionDMContext exprContext, final DataRequestMonitor<IExpressionDMData> rm) {
		asyncExec(new Runnable() {
			public void run() {
				if (exprContext instanceof IEDCExpression)
					rm.setData(new ExpressionData((IEDCExpression) exprContext));
				else
					rm.setData(new ExpressionData(null));
				rm.done();
			}
		}, rm);
	}

	public void getSubExpressionCount(final IExpressionDMContext exprContext, final DataRequestMonitor<Integer> rm) {
		asyncExec(new Runnable() {
			public void run() {
				// handle array casts
				CastInfo cast = null;
				if (exprContext instanceof IEDCExpression && (cast = ((IEDCExpression) exprContext).getCastInfo()) != null) { 
					if (cast.getArrayCount() > 0) {
						if (((IEDCExpression)exprContext).getEvaluationError() != null) {
							rm.setData(0);
							rm.done();
							return;
						}
						rm.setData(cast.getArrayCount());
						rm.done();
						return;
					}
				}

				if (!(exprContext instanceof IEDCExpression)) {
					rm.setData(0);
					rm.done();
					return;
				}

				IEDCExpression expr = (IEDCExpression) exprContext;

				// if expression has no evaluated value, then it has not yet been evaluated
				if (expr.getEvaluatedValue() == null && expr.getEvaluatedValueString() != null) {
					expr.evaluateExpression();
				}

				IType exprType = TypeUtils.getStrippedType(expr.getEvaluatedType());
				
				// to expand it, it must either be a pointer, a reference to an aggregate,
				// or an aggregate
				boolean pointerType = exprType instanceof IPointerType;
				boolean referenceType = exprType instanceof IReferenceType;
				IType pointedTo = null;
				if (referenceType)
					pointedTo = TypeUtils.getStrippedType(((IReferenceType) exprType).getType());
				
				if (!(exprType instanceof IAggregate) && !pointerType &&
					!(referenceType && (pointedTo instanceof IAggregate || pointedTo instanceof IPointerType))) {
					rm.setData(0);
					rm.done();
					return;
				}
				
				ITypeContentProvider customProvider = 
					FormatExtensionManager.instance().getTypeContentProvider(exprType);
				if (customProvider != null) {
					try {
						rm.setData(customProvider.getChildCount(expr));
						rm.done();
						return;
					} catch (Throwable e) {
					}
				}

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
		}, rm);
	}

	public void getSubExpressions(final IExpressionDMContext exprContext, final DataRequestMonitor<IExpressionDMContext[]> rm) {
		asyncExec(new Runnable() {
			public void run() {
				if (!(exprContext instanceof IEDCExpression) || ((IEDCExpression) exprContext).getFrame() == null) {
					rm.setData(new IEDCExpression[0]);
					rm.done();
					return;
				}

				IEDCExpression expr = (IEDCExpression) exprContext;

				// if expression has no evaluated value, then it has not yet been evaluated
				if (expr.getEvaluatedValue() == null && expr.getEvaluatedValueString() != null) {
					expr.evaluateExpression();
				}

				StackFrameDMC frame = (StackFrameDMC) expr.getFrame();
				IType exprType = TypeUtils.getStrippedType(expr.getEvaluatedType());

				// if casted to an array, convert thusly
		    	CastInfo castInfo = expr.getCastInfo();
		    	if (castInfo != null && castInfo.getArrayCount() > 0) {
		    		try {
		    			exprType = frame.getTypeEngine().convertToArrayType(exprType, castInfo.getArrayCount());
		    		} catch (CoreException e) {
		    			rm.setStatus(e.getStatus());
		    			rm.done();
		    			return;
		    		}
		    	}
		    	
				
				// to expand it, it must either be a pointer, a reference to an aggregate,
				// or an aggregate
				boolean pointerType = exprType instanceof IPointerType;
				boolean referenceType = exprType instanceof IReferenceType;
				IType pointedTo = null;
				if (referenceType) {
					pointedTo = TypeUtils.getStrippedType(((IReferenceType) exprType).getType());
					exprType = pointedTo;
				}
				
				if (!(exprType instanceof IAggregate) && !pointerType &&
					!(referenceType && (pointedTo instanceof IAggregate || pointedTo instanceof IPointerType))) {
					rm.setData(new IEDCExpression[0]);
					rm.done();
					return;
				}
				
				ITypeContentProvider customProvider = 
					FormatExtensionManager.instance().getTypeContentProvider(exprType);
				if (customProvider != null) {
					getSubExpressions(expr, frame, exprType, customProvider, rm);
				}
				else
					getSubExpressions(expr, rm);
			}
		}, rm);
	}

	public void getSubExpressions(final IExpressionDMContext exprContext, final int startIndex_, final int length_,
			final DataRequestMonitor<IExpressionDMContext[]> rm) {
		asyncExec(new Runnable() {
			public void run() {
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
		}, rm);
	}

	private void getSubExpressions(final IEDCExpression expr, final StackFrameDMC frame, 
			final IType exprType, final ITypeContentProvider customProvider,
			final DataRequestMonitor<IExpressionDMContext[]> rm) {

		List<IExpressionDMContext> children = new ArrayList<IExpressionDMContext>();
		Iterator<IExpressionDMContext> childIterator;
		try {
			childIterator = customProvider.getChildIterator(expr);
			while (childIterator.hasNext() && !rm.isCanceled()) {
				children.add(childIterator.next());
			}
			rm.setData(children.toArray(new IExpressionDMContext[children.size()]));
			rm.done();
		} catch (CoreException e) {
			// Checked exception. But we don't want to pass the error up as it
			// would make the variable (say, a structure) not expandable on UI. 
			// Just resort to the normal formatting.  
			getSubExpressions(expr, rm);
		} catch (Throwable e) {
			// unexpected error. log it.
			EDCDebugger.getMessageLogger().logError(
					EDCServicesMessages.Expressions_ErrorInVariableFormatter + customProvider.getClass().getName(), e);
			
			// default to normal formatting
			getSubExpressions(expr, rm);
		}
	}

	private void getSubExpressions(final IEDCExpression expr, 
			final DataRequestMonitor<IExpressionDMContext[]> rm) {
		rm.setData(getLogicalSubExpressions(expr));
		rm.done();
	}
	
	/**
	 * Get the logical subexpressions for the given expression context.  We want
	 * to skip unnecessary nodes, e.g., a pointer to a composite, and directly
	 * show the object contents.
	 * @param expr the expression from which to start
	 * @return array of children
	 */
	public IEDCExpression[] getLogicalSubExpressions(IEDCExpression expr) {

		IType exprType = TypeUtils.getUnRefStrippedType(expr.getEvaluatedType());
		
		// cast to array?
		CastInfo castInfo = expr.getCastInfo();
		if (castInfo != null && castInfo.getArrayCount() > 0) {
			
			String exprName = expr.getExpression();
			
			// in case of casts, need to resolve that before dereferencing, so be safe
			if (exprName.contains("(")) //$NON-NLS-1$
				exprName = '(' + exprName + ')';

			long lowerBound = castInfo.getArrayStartIndex();
			long count = castInfo.getArrayCount();
			
			List<IEDCExpression> arrayChildren = new ArrayList<IEDCExpression>();
			for (int i = 0; i < count; i++) {
				String arrayElement = "[" + (i + lowerBound) + "]"; //$NON-NLS-1$ //$NON-NLS-2$
				IEDCExpression newExpr = new ExpressionDMC(expr.getFrame(), (exprName + arrayElement),
						expr.getName() + arrayElement);
				IEDCExpression exprChild = newExpr; //$NON-NLS-1$ //$NON-NLS-2$
				if (exprChild != null) {
					arrayChildren.add(exprChild);
				}
			}

			return arrayChildren.toArray(new IEDCExpression[arrayChildren.size()]);
		} 
		
		if (exprType instanceof IPointerType) {
			// automatically dereference a pointer
			String exprName = expr.getExpression();
			IType typePointedTo = TypeUtils.getStrippedType(exprType.getType());

			// Try to resolve opaque pointer. 
			// Note this may take some time depending on symbol file size. 
			// ........05/19/11
			//
			if (TypeUtils.isOpaqueType(typePointedTo)) {
				final Symbols symService = getService(Symbols.class);
				assert symService != null;
				
				final ISymbolDMContext symCtx = DMContexts.getAncestorOfType(expr, ISymbolDMContext.class);
				if (symCtx != null) {
					final ICompositeType[] resolved = {null};
					final IType original = typePointedTo;
					Job j = new Job("Resolving opaque type" + original.getName()) {
						@Override
						protected IStatus run(IProgressMonitor monitor) {
							monitor.beginTask("Resolving opaque type: " + original.getName(), IProgressMonitor.UNKNOWN);
							resolved[0] = symService.resolveOpaqueType(symCtx, (ICompositeType)original);
							monitor.done();
							return Status.OK_STATUS;
						}};
						
					j.schedule();
					try {
						j.join();
					} catch (InterruptedException e) {
						// ignore
					}
					
					if (resolved[0] != null) {
						typePointedTo = resolved[0];
						
						// Make the pointer type points to the resolved type
						// so that we won't need to resolve the opaque type again
						// and again.
						exprType.setType(resolved[0]);
					}
				}
			}
			
			// If expression name already starts with "&" (e.g. "&struct"), indirect it first
			boolean indirected = false;
			
			IEDCExpression exprChild;
			
			if (exprName.startsWith("&")) { //$NON-NLS-1$
				exprName = exprName.substring(1);
				IEDCExpression newExpr = new ExpressionDMC(expr.getFrame(), exprName);
				exprChild = newExpr;
				indirected = true;
			} 
			else {
				// avoid dereferencing void pointer
				if (typePointedTo instanceof ICPPBasicType 
						&& ((ICPPBasicType) typePointedTo).getBaseType() == ICPPBasicType.t_void) {
					return new IEDCExpression[0];
				}
				
				// do not dereference null either
				if (expr.getEvaluatedValue() != null && expr.getEvaluatedValue().intValue() == 0)
					return new IEDCExpression[0];
				IEDCExpression newExpr = new ExpressionDMC(expr.getFrame(), ("*" + exprName), "*" + expr.getName()); //$NON-NLS-1$ //$NON-NLS-2$
					
				// a pointer type has one child
				exprChild = newExpr; //$NON-NLS-1$
			}
			
			return doGetLogicalSubExpressions(exprChild, typePointedTo, indirected);
		} 
		else if (exprType instanceof IReferenceType) {
			// and bypass a reference
			
			IType typePointedTo = TypeUtils.getStrippedType(exprType.getType());
			return doGetLogicalSubExpressions(expr, typePointedTo, false);
		}else {
			// normal aggregate, just do it
			return doGetLogicalSubExpressions(expr, exprType, false);
		}
		
	}

	/**
	 * Get the logical subexpressions for the given expression context and string 
	 * @param expr the expression from which to start
	 * @param exprType the type in which to consider the expression
	 * @param indirected if true, the expression was already indirected, as opposed to what the expression says
	 * @return
	 */
	private IEDCExpression[] doGetLogicalSubExpressions(IEDCExpression expr, IType exprType, boolean indirected) {
		ArrayList<IEDCExpression> exprList = new ArrayList<IEDCExpression>();
		IEDCExpression exprChild;

		String expression = expr.getExpression();
		
		// in case of casts, need to resolve that before dereferencing, so be safe
		if (expression.contains("(")) //$NON-NLS-1$
			expression = '(' + expression + ')';

		/*
		// cast to array?
		CastInfo castInfo = expr.getCastInfo();
		if (castInfo != null && castInfo.getArrayCount() > 0) {
			long lowerBound = castInfo.getArrayStartIndex();
			long count = castInfo.getArrayCount();
			for (int i = 0; i < count; i++) {
				exprChild = createDerivedExpression(expr, exprName + "[" + (i + lowerBound) + "]"); //$NON-NLS-1$ //$NON-NLS-2$
				if (exprChild != null) {
					exprList.add(exprChild);
				}
			}
		
		} 
		else*/ if (exprType instanceof ICompositeType) {
			// an artifact of following a pointer to a structure is that the
			// name starts with '*'
			if (expression.startsWith("*")) { //$NON-NLS-1$
				if (expression.startsWith("**"))
					expression = "(" + expression.substring(1) + ")->"; //$NON-NLS-1$
				else
					expression = expression.substring(1) + "->"; //$NON-NLS-1$
			} else {
				expression = expression + '.'; //$NON-NLS-1$
			}

			// for each field, evaluate an expression, then shorten the name
			ICompositeType compositeType = (ICompositeType) exprType;

			for (IField field : compositeType.getFields()) {
				String fieldName = field.getName();
				if (fieldName.length() == 0) {
					// This makes an invalid expression
					// The debug info provider should have filtered out or renamed such fields
					assert false;
					continue;
				}
				exprChild = new ExpressionDMC(expr.getFrame(), expression + fieldName, fieldName);
				if (exprChild != null) {
					exprList.add(exprChild);
				}
			}

			for (IInheritance inherited : compositeType.getInheritances()) {
				String inheritedName = inherited.getName();
				if (inheritedName.length() == 0) {
					// This makes an invalid expression
					// The debug info provider should have filtered out or renamed such fields
					assert false;	// couldn't this be the case for an anonymous member, like a union?
				} else if (!inheritedName.contains("<")) {
					exprChild = new ExpressionDMC(expr.getFrame(), expression + inheritedName, inheritedName);
					if (exprChild != null) {
						exprList.add(exprChild);
					}
				} else {
					IType inheritedType = inherited.getType(); 
					if (inheritedType instanceof ICompositeType) {
						for (IField field : ((ICompositeType)inheritedType).getFields()) {
							String fieldName = field.getName();
							if (fieldName.length() == 0) {
								// This makes an invalid expression
								// The debug info provider should have filtered out or renamed such fields
								assert false;
								continue;
							}
							exprChild = new ExpressionDMC(expr.getFrame(), expression + fieldName, fieldName);
							if (exprChild != null) {
								exprList.add(exprChild);
							}
						}
					}
				}
			}
			
		} 
		else if (exprType instanceof IArrayType) {
			IArrayType arrayType = (IArrayType) exprType;

			if (arrayType.getBoundsCount() > 0) {
				long lowerBound = expr.getCastInfo() != null && expr.getCastInfo().getArrayCount() > 0 
					? expr.getCastInfo().getArrayStartIndex() : 0;
				long upperBound = arrayType.getBound(0).getBoundCount();
				if (upperBound == 0)
					upperBound = 1;
				for (int i = 0; i < upperBound; i++) {
					String arrayElementName = "[" + (i + lowerBound) + "]"; //$NON-NLS-1$ //$NON-NLS-2$
					IEDCExpression newExpr = new ExpressionDMC(expr.getFrame(), expression + arrayElementName,
							expr.getName() + arrayElementName); 
					exprChild = newExpr;
					if (exprChild != null) {
						exprList.add(exprChild);
					}
				}
			}
		} 
		else if (exprType instanceof IArrayDimensionType) {
			IArrayDimensionType arrayDimensionType = (IArrayDimensionType) exprType;
			IArrayType arrayType = arrayDimensionType.getArrayType();

			if (arrayType.getBoundsCount() > arrayDimensionType.getDimensionCount()) {
				long lowerBound = expr.getCastInfo() != null && expr.getCastInfo().getArrayCount() > 0 
				? expr.getCastInfo().getArrayStartIndex() : 0;
				long upperBound = arrayType.getBound(arrayDimensionType.getDimensionCount()).getBoundCount();
				for (int i = 0; i < upperBound; i++) {
					String arrayElement = "[" + (i + lowerBound) + "]"; //$NON-NLS-1$ //$NON-NLS-2$
					IEDCExpression newExpr = new ExpressionDMC(expr.getFrame(), expression + arrayElement,
							expr.getName() + arrayElement); 
					exprChild = newExpr;
					if (exprChild != null) {
						exprList.add(exprChild);
					}
				}
			}
		} 
		else {
			// nothing interesting
			exprList.add(expr);
		}

		return exprList.toArray(new IEDCExpression[exprList.size()]);
	}

	@Immutable
    private static class ExpressionChangedDMEvent extends AbstractDMEvent<IExpressionDMContext> implements IExpressionChangedDMEvent {
        ExpressionChangedDMEvent(IExpressionDMContext expression) {
            super(expression);
        }
    }

	public void writeExpression(final IExpressionDMContext exprContext, final String expressionValue, final String formatId, final RequestMonitor rm) {

		asyncExec(new Runnable() {
			public void run() {
				IEDCExpression expressionDMC = (IEDCExpression) exprContext;
				if (isComposite(expressionDMC)) {
					rm.setStatus(EDCDebugger.dsfRequestFailedStatus(EDCServicesMessages.Expressions_CannotModifyCompositeValue, null));
					rm.done();
					return;
				}

				IType exprType = TypeUtils.getStrippedType(expressionDMC.getEvaluatedType());

				// first try to get value by format as BigInteger
				Number number = NumberFormatUtils.parseIntegerByFormat(expressionValue, formatId);
		        if (number == null) {
		       		IEDCExpression temp = (IEDCExpression) createExpression(expressionDMC.getFrame(), expressionValue);
		       		temp.evaluateExpression();
					number = temp.getEvaluatedValue();

		       		if (number == null) {
		       			rm.setStatus(EDCDebugger.dsfRequestFailedStatus(EDCServicesMessages.Expressions_CannotParseExpression, null));
		       			rm.done();
		       			return;
		       		}
		        }
		        
		        BigInteger value = null;
				try {
					value = MemoryUtils.convertValueToMemory(exprType, number);
				} catch (CoreException e) {
		   			rm.setStatus(e.getStatus());
		   			rm.done();
		   			return;
				}
		        
		        IVariableLocation variableLocation = expressionDMC.getValueLocation();
		        if (variableLocation == null) {
		        	rm.setStatus(EDCDebugger.dsfRequestFailedStatus(EDCServicesMessages.Expressions_ExpressionNoLocation, null));
		   			rm.done();
		   			return;
		        }
		        	
		    	try {
		    		variableLocation.writeValue(exprType.getByteSize(), value);
		    		getSession().dispatchEvent(new ExpressionChangedDMEvent(exprContext), getProperties());
				} catch (CoreException e) {
					rm.setStatus(e.getStatus());
				}
		        
				rm.done();
			}
		}, rm);

	}

	public void getAvailableFormats(IFormattedDataDMContext formattedDataContext, DataRequestMonitor<String[]> rm) {
		rm.setData(new String[] { IFormattedValues.NATURAL_FORMAT, IFormattedValues.DECIMAL_FORMAT, 
				IFormattedValues.HEX_FORMAT, IFormattedValues.OCTAL_FORMAT, IFormattedValues.BINARY_FORMAT });
		rm.done();
	}

	public void getFormattedExpressionValue(final FormattedValueDMContext formattedDataContext,
			final DataRequestMonitor<FormattedValueDMData> rm) {
		asyncExec(new Runnable() {
			public void run() {
				try {
					rm.setData(getFormattedExpressionValue(formattedDataContext));
					rm.done();
				} catch (CoreException ce) {
					rm.setStatus(ce.getStatus());
					rm.done();
					return;
				}
			}
		}, rm);
	}

	public String getExpressionValueString(IExpressionDMContext expression, String format) throws CoreException {
		FormattedValueDMContext formattedDataContext = getFormattedValueContext(expression, format);
		FormattedValueDMData formattedValue = getFormattedExpressionValue(formattedDataContext);
		
		return formattedValue != null ? formattedValue.getFormattedValue() : "";
	}

	public FormattedValueDMData getFormattedExpressionValue(FormattedValueDMContext formattedDataContext) throws CoreException {
		IDMContext idmContext = formattedDataContext.getParents()[0];
		FormattedValueDMData formattedValue = null;
		IEDCExpression exprDMC = null;

		if (idmContext instanceof IEDCExpression) {
			exprDMC = (IEDCExpression) formattedDataContext.getParents()[0];

			exprDMC.evaluateExpression();
			
			if (exprDMC != null && exprDMC.getEvaluationError() != null) {
				throw new CoreException(exprDMC.getEvaluationError());
			}
			
			formattedValue = exprDMC.getFormattedValue(formattedDataContext); // must call this to get type
			
			if (formattedDataContext.getFormatID().equals(IFormattedValues.NATURAL_FORMAT))
			{
				IVariableValueConverter customConverter = getCustomValueConverter(exprDMC);
				if (customConverter != null) {
					FormattedValueDMData customFormattedValue = null;
					try {
						customFormattedValue = new FormattedValueDMData(customConverter.getValue(exprDMC));
						formattedValue = customFormattedValue;
					}
					catch (Throwable t) {
						// CoreExeception will just propagate out, so this is for
						// other unexpected errors, usually bug in the formatter. Log it 
						// so that user will be able to see and report the bug. 
						// Meanwhile, default to normal formatting so that user won't see 
						// such error in Variable UI.
						EDCDebugger.getMessageLogger().logError(
								EDCServicesMessages.Expressions_ErrorInVariableFormatter + customConverter.getClass().getName(), t);
					}
				}
			}
		} else
			formattedValue = new FormattedValueDMData(""); //$NON-NLS-1$
		
		return formattedValue;
	}

	private IVariableValueConverter getCustomValueConverter(IEDCExpression exprDMC) {
		IType exprType = TypeUtils.getUnRefStrippedType(exprDMC.getEvaluatedType());
		return FormatExtensionManager.instance().getVariableValueConverter(exprType);
	}

	public FormattedValueDMContext getFormattedValueContext(IFormattedDataDMContext formattedDataContext,
			String formatId) {
		return new FormattedValueDMContext(this, formattedDataContext, formatId);
	}

	public void getModelData(IDMContext context, DataRequestMonitor<?> rm) {
	}

	public String getExpressionValue(IExpressionDMContext expression)
	{
		final StringBuffer holder = new StringBuffer();
		FormattedValueDMContext formattedValueContext = getFormattedValueContext(expression, IFormattedValues.NATURAL_FORMAT);					
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
