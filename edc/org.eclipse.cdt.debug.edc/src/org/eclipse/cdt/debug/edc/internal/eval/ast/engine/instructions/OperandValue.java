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

package org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions;

import java.math.BigInteger;
import java.text.MessageFormat;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.EDCTrace;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.ASTEvalMessages;
import org.eclipse.cdt.debug.edc.internal.symbols.IAggregate;
import org.eclipse.cdt.debug.edc.internal.symbols.IBasicType;
import org.eclipse.cdt.debug.edc.internal.symbols.ICPPBasicType;
import org.eclipse.cdt.debug.edc.internal.symbols.IEnumeration;
import org.eclipse.cdt.debug.edc.internal.symbols.IPointerType;
import org.eclipse.cdt.debug.edc.internal.symbols.IQualifierType;
import org.eclipse.cdt.debug.edc.internal.symbols.IReferenceType;
import org.eclipse.cdt.debug.edc.internal.symbols.TypedefType;
import org.eclipse.cdt.debug.edc.symbols.IEnumerator;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.debug.edc.symbols.IVariableLocation;
import org.eclipse.cdt.debug.edc.symbols.TypeUtils;
import org.eclipse.cdt.debug.edc.symbols.VariableLocationFactory;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.core.runtime.CoreException;

/**
 * This is the basic unit of work when evaluating expressions.  It carries
 * a type and a number.  Subclasses like {@link VariableWithValue} may hold
 * additional information, like a specific variable and a location.
 */
public class OperandValue {

	protected IVariableLocation valueLocation;

	protected Number value;
	protected String stringValue;
	protected IType type;
	protected boolean isBitField;


	public OperandValue(IType type) {
		this(type, false);
	}

	public OperandValue(IType type, boolean isBitField) {
		this.type = type;
		this.isBitField = isBitField;
	}

	// operand order switched so we don't accidentally invoke isBitField variant with Boolean
	public OperandValue(Number value, IType type) {
		this.type = type;
		this.value = value;
	}
	
	public OperandValue(String string, IType type) {
		this(type, false);
		this.stringValue = string;
	}

	/**
	 * @return the type
	 */
	public IType getValueType() {
		return type;
	}

	public IVariableLocation getValueLocation() {
		return valueLocation;
	}

	public void setValueLocation(IVariableLocation valueLocation) {
		this.valueLocation = valueLocation;
	}

	public Number getValueLocationAddress() throws CoreException {
		IVariableLocation location = getValueLocation();
		if (location == null)
			throw EDCDebugger.newCoreException(ASTEvalMessages.OperandValue_VariableNoAddress);
		return getLocationAddress(location);
	}

	public Number getValueByType(IType varType, IVariableLocation location)
			throws CoreException {
		Number result;
		
		if (varType != null) {
			if (varType instanceof ICPPBasicType)
				result = getBasicTypeValue(varType, location);
			else if (varType instanceof IPointerType)
				result = getBasicTypeValue(varType, location);
			else if (varType instanceof IReferenceType) {
				result = getBasicTypeValue(varType, location);
				
				// use result as the location of the referenced variable's value
				if (location != null && location.getContext() != null && location.getServicesTracker() != null) {
					IDMContext context = location.getContext();
					DsfServicesTracker servicesTracker = location.getServicesTracker();
					IType pointedTo = TypeUtils.getStrippedType(varType).getType();
					if (pointedTo instanceof ICPPBasicType || pointedTo instanceof IPointerType ||
						pointedTo instanceof IEnumeration) {
						IVariableLocation newLocation = VariableLocationFactory.createMemoryVariableLocation(servicesTracker, context, result);
						setValueLocation(newLocation);
						result = getBasicTypeValue(pointedTo, newLocation);
					}
				}
			} else if (varType instanceof IAggregate)
				result = getAggregateTypeValue((IAggregate) varType, location);
			else if (varType instanceof IQualifierType || varType instanceof TypedefType)
				result = getValueByType(varType.getType(), location);
			else if (varType instanceof IEnumeration)
				result = getBasicTypeValue(varType, location);
			else {
				assert false;
				throw EDCDebugger.newCoreException(ASTEvalMessages.VariableWithValue_UnhandledType + varType.getName());
			}
		} else {
			throw EDCDebugger.newCoreException(ASTEvalMessages.VariableWithValue_VariableHasNoType);
		}
		return result;
	}

	public Number getValue() throws CoreException {
		return value;
	}

	public void setValue(Number value) {
		this.value = value;
	}

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}
	
	public void setAddressValue(IVariableLocation location) throws CoreException {
		setValue(getLocationAddress(location));
	}

	protected Number getLocationAddress(IVariableLocation location) throws CoreException {
		IAddress addr = location.getAddress();
		if (addr == null)
			throw EDCDebugger.newCoreException(
					MessageFormat.format(ASTEvalMessages.OperandValue_CannotGetAddress, location.getLocationName()));
		return addr.getValue();
	}

	private Number getAggregateTypeValue(IAggregate varType, IVariableLocation location)
		throws CoreException {
		// assumes that an array, class, struct, or union is always located in
		// memory, not in a register
		return getLocationAddress(location);
	}

	private Number getBasicTypeValue(IType varType, IVariableLocation location)
		throws CoreException {
		if (EDCTrace.VARIABLE_VALUE_TRACE_ON) { EDCTrace.traceEntry(new Object[] { varType, location }); }
		Number result = null;
		try {
			result = doGetBasicTypeValue(varType, location);
			return result;
		} finally {
			if (EDCTrace.VARIABLE_VALUE_TRACE_ON) { EDCTrace.traceExit(result); }
		}
	}

	private Number doGetBasicTypeValue(IType varType, IVariableLocation location) throws CoreException {
			
		Number result = null;
		int varSize = varType.getByteSize();
	
		// get characteristics of the type
		int basicType = IBasicType.t_unspecified;
		boolean isSigned = false;
		boolean isShort = false;
		boolean isLong = false;
		boolean isLongLong = false;
		boolean isComplex = false;
	
		if (varType instanceof IBasicType) {
			IBasicType type = (IBasicType) varType;
			basicType = type.getBaseType();
			
			if (basicType == ICPPBasicType.t_void)
				throw EDCDebugger.newCoreException(ASTEvalMessages.OperandValue_CannotReadVoid);
			if (basicType == ICPPBasicType.t_unspecified)
				throw EDCDebugger.newCoreException(ASTEvalMessages.OperandValue_CannotReadUnspecifiedType);
			
			isSigned = type.isSigned();
			isShort = type.isShort();
			isLong = type.isLong();
			
			if (varType instanceof ICPPBasicType) {
				ICPPBasicType cppType = (ICPPBasicType) varType;
				isLongLong = cppType.isLongLong();
				isComplex  = cppType.isComplex();
			}
		} else if (varType instanceof IPointerType) {
			// treat pointer as an unsigned int
			basicType = IBasicType.t_int;
		} else if (varType instanceof IEnumerator){
			// treat enumerator as a signed int
			basicType = IBasicType.t_int;
			isSigned = true;
		} else {
			// treat unknown type as an unsigned int
			basicType = IBasicType.t_int;
			isSigned = false;
		}
	
		// if variable's size is 0, use its default size
		if (varSize == 0) {
			varSize = getDefaultSize(varType, basicType, isShort, isLong, isLongLong, isComplex);
		}
	
		// all other locations
		IVariableLocation varLocation = location;
		BigInteger varValue = null;
		
		if (varLocation != null) {
			varValue = varLocation.readValue(varSize);
		} else {
			throw EDCDebugger.newCoreException(ASTEvalMessages.VariableWithValue_UnknownLocation);
		}
		
		switch (basicType) {
		case IBasicType.t_float:
		case IBasicType.t_double:
			if (varSize == 4) {
				result = Float.intBitsToFloat(varValue.intValue());
			} else if (varSize == 8) {
				result = Double.longBitsToDouble(varValue.longValue());
			} else {
				// TODO: support 12-byte long double read from register
				throw EDCDebugger.newCoreException(ASTEvalMessages.VariableWithValue_NoTwelveByteLongDouble);
			}
			break;
	
		case ICPPBasicType.t_bool:
		case ICPPBasicType.t_wchar_t:
		case IBasicType.t_char:
		case IBasicType.t_int:
		case IBasicType.t_void:
			if (isSigned) {
				// as needed, mask the value and sign-extend
				if (varSize == 4) {
					result = new Integer(varValue.intValue());
				} else if (varSize == 2) {
					int intResult = varValue.intValue() & 0xffff;
					if ((intResult & 0x00008000) != 0)
						intResult |= 0xffff0000;
					result = new Integer(intResult);
				} else if (varSize == 1) {
					int intResult = varValue.intValue() & 0xff;
					if ((intResult & 0x00000080) != 0)
						intResult |= 0xffffff00;
					result = new Integer(intResult);
				} else {
					// assume an 8-byte long is the default
					result = new Long(varValue.longValue());
				}
			} else {
				if (varSize == 4) {
					result = new Long(varValue.longValue() & 0xffffffffL);  // keep it unsigned
				} else if (varSize == 2) {
					result = new Integer(varValue.intValue() & 0xffff);
				} else if (varSize == 1) {
					result = new Integer(varValue.intValue() & 0xff);
				} else {
					// assume an 8-byte long is the default
					result = new Long(varValue.longValue());
				}
			}
			break;
	
		case IBasicType.t_unspecified:
		default:
			assert false;
			throw EDCDebugger.newCoreException(ASTEvalMessages.OperandValue_UnhandledType);
		}
		return result;
	}

	private int getDefaultSize(IType varType, int basicType, boolean isShort,
			boolean isLong, boolean isLongLong, boolean isComplex) {
		int varSize = 0;

		// debug info should never claim something is zero size any more
		/*
		Object retrievedSize = null;
		ITargetEnvironment targetEnvironmentService = servicesTracker.getService(ITargetEnvironment.class);
		if (targetEnvironmentService != null) {
			Map<Integer, Integer> sizes = targetEnvironmentService.getBasicTypeSizes();
			
			switch(basicType) {
		case IBasicType.t_char:
			retrievedSize = sizes.get(TypeUtils.BASIC_TYPE_CHAR);
			break;
		case IBasicType.t_int:
			if (isShort) {
				retrievedSize = sizes.get(TypeUtils.BASIC_TYPE_SHORT);
			} else if (isLongLong) {
				retrievedSize = sizes.get(TypeUtils.BASIC_TYPE_LONG_LONG);
			} else if (isLong) {
				retrievedSize = sizes.get(TypeUtils.BASIC_TYPE_LONG);
			} else {
				retrievedSize = sizes.get(TypeUtils.BASIC_TYPE_INT);
			}
			break;
		case IBasicType.t_float:
			if (isComplex) {
				retrievedSize = sizes.get(TypeUtils.BASIC_TYPE_FLOAT_COMPLEX);
			} else {
				retrievedSize = sizes.get(TypeUtils.BASIC_TYPE_FLOAT);
			}
			break;
		case IBasicType.t_double:
			if (isComplex) {
				if (isLong) {
					retrievedSize = sizes.get(TypeUtils.BASIC_TYPE_LONG_DOUBLE_COMPLEX);
				} else {
					retrievedSize = sizes.get(TypeUtils.BASIC_TYPE_DOUBLE_COMPLEX);
				}
			} else {
				if (isLong) {
					retrievedSize = sizes.get(TypeUtils.BASIC_TYPE_LONG_DOUBLE);
				} else {
					retrievedSize = sizes.get(TypeUtils.BASIC_TYPE_DOUBLE);
				}
			}
			break;
		case ICPPBasicType.t_bool:
			retrievedSize = sizes.get(TypeUtils.BASIC_TYPE_BOOL);
			break;
		case ICPPBasicType.t_wchar_t:
			retrievedSize = sizes.get(TypeUtils.BASIC_TYPE_WCHAR_T);
			break;
		default:
			retrievedSize = 1;
			}
		}
		if (retrievedSize != null)
			varSize = (Integer) retrievedSize;
		*/
		
		varSize = 1;
		return varSize;
	}

	/**
	 * Get the operand value as a long
	 * @return long
	 * @throws CoreException 
	 */
	public long getLongValue() throws CoreException {
		return getValue().longValue();
	}

	/**
	 * @return
	 */
	public boolean isFloating() {
		return value instanceof Float || value instanceof Double;
	}


	public boolean isBitField() {
		return isBitField;
	}
	
	/**
	 * Get the value as a BigInteger 
	 * @return value or 0 if not a number
	 */
	public BigInteger getBigIntValue() throws CoreException {
		BigInteger bigIntVal;
		if (getValue() == null)
			return BigInteger.ZERO;
		if (value instanceof BigInteger)
			bigIntVal = (BigInteger) value;
		else
			bigIntVal = BigInteger.valueOf(value.longValue());
		return bigIntVal;
	}
	
	public OperandValue copyWithType(IType otherType) {
		OperandValue value = new OperandValue(otherType);
		value.value = this.value;
		value.stringValue = this.stringValue;
		value.valueLocation = this.valueLocation;
		return value;
	}

}