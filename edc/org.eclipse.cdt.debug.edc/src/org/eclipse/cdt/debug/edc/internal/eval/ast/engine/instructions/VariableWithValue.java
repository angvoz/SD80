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
package org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.IEDCTraceOptions;
import org.eclipse.cdt.debug.edc.internal.MemoryUtils;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.ASTEvalMessages;
import org.eclipse.cdt.debug.edc.internal.services.dsf.ITargetEnvironment;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Memory;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Modules;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Registers;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Modules.ModuleDMC;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Stack.StackFrameDMC;
import org.eclipse.cdt.debug.edc.internal.symbols.IAggregate;
import org.eclipse.cdt.debug.edc.internal.symbols.IBasicType;
import org.eclipse.cdt.debug.edc.internal.symbols.ICPPBasicType;
import org.eclipse.cdt.debug.edc.internal.symbols.IEnumeration;
import org.eclipse.cdt.debug.edc.internal.symbols.IEnumerator;
import org.eclipse.cdt.debug.edc.internal.symbols.IInvalidVariableLocation;
import org.eclipse.cdt.debug.edc.internal.symbols.ILocationProvider;
import org.eclipse.cdt.debug.edc.internal.symbols.IMemoryVariableLocation;
import org.eclipse.cdt.debug.edc.internal.symbols.IPointerType;
import org.eclipse.cdt.debug.edc.internal.symbols.IQualifierType;
import org.eclipse.cdt.debug.edc.internal.symbols.IReferenceType;
import org.eclipse.cdt.debug.edc.internal.symbols.IRegisterVariableLocation;
import org.eclipse.cdt.debug.edc.internal.symbols.IType;
import org.eclipse.cdt.debug.edc.internal.symbols.IVariable;
import org.eclipse.cdt.debug.edc.internal.symbols.IVariableLocation;
import org.eclipse.cdt.debug.edc.internal.symbols.TypeUtils;
import org.eclipse.cdt.debug.edc.internal.symbols.TypedefType;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IModules.ISymbolDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.model.MemoryByte;

public class VariableWithValue {

	private final StackFrameDMC frame;
	private final IVariable variable;
	private Object valueLocation;
	private Object value;
	private final DsfServicesTracker servicesTracker;
	private final boolean isBitField;

	public VariableWithValue(DsfServicesTracker servicesTracker, StackFrameDMC frame, IVariable variable) {
		this.frame = frame;
		this.variable = variable;
		this.servicesTracker = servicesTracker;
		this.isBitField = false;
	}

	public VariableWithValue(DsfServicesTracker servicesTracker, StackFrameDMC frame, IVariable variable,
			boolean isBitField) {
		this.frame = frame;
		this.variable = variable;
		this.servicesTracker = servicesTracker;
		this.isBitField = isBitField;
	}

	public IVariable getVariable() {
		return variable;
	}

	public DsfServicesTracker getServicesTracker() {
		return servicesTracker;
	}

	public StackFrameDMC getFrame() {
		return frame;
	}

	public boolean isBitField() {
		return isBitField;
	}

	public Object getValueLocation() {
		if (valueLocation == null) {
			valueLocation = new Object();
			Modules modules = servicesTracker.getService(Modules.class);
			ISymbolDMContext symContext = DMContexts.getAncestorOfType(frame, ISymbolDMContext.class);
			ILocationProvider provider = variable.getLocationProvider();
			IAddress pcValue = frame.getIPAddress();
			ModuleDMC module = modules.getModuleByAddress(symContext, pcValue);
			IVariableLocation location = provider.getLocation(servicesTracker, frame, module.toLinkAddress(pcValue));
			if (location instanceof IMemoryVariableLocation) {
				IMemoryVariableLocation memoryLocation = (IMemoryVariableLocation) location;
				if (memoryLocation.isRuntimeAddress()) {
					valueLocation = memoryLocation.getAddress();
				} else {
					valueLocation = module.toRuntimeAddress(memoryLocation.getAddress());
				}
			} else {
				// either in a register or not live at the given address
				valueLocation = location;
			}
		}
		return valueLocation;
	}

	// intended to be used for internal expression evaluation
	public void setValueLocation(Object valueLocation) {
		this.valueLocation = valueLocation;
	}

	public Object getValueByType(IType varType, Object location) {
		Object result = new Object();
		
		if (varType != null) {
			if (varType instanceof ICPPBasicType)
				result = getBasicTypeValue(varType, location);
			else if (varType instanceof IPointerType)
				result = getBasicTypeValue(varType, location);
			else if (varType instanceof IReferenceType) {
				result = getBasicTypeValue(varType, location);
				IType pointedTo = TypeUtils.getStrippedType(varType).getType();
				if (pointedTo instanceof ICPPBasicType || pointedTo instanceof IPointerType ||
					pointedTo instanceof IEnumeration) {
					// use result as the new location
					Object newLocation = result;
					if (newLocation instanceof BigInteger) {
						newLocation = new Addr64((BigInteger) newLocation);
					} else if (result instanceof Long) {
						newLocation = new Addr64(((Long) newLocation).toString());
					}
					setValueLocation(newLocation);
					result = getBasicTypeValue(pointedTo, newLocation);
				}
			} else if (varType instanceof IAggregate)
				result = getAggregateTypeValue((IAggregate) varType, location);
			else if (varType instanceof IQualifierType || varType instanceof TypedefType)
				result = getValueByType(varType.getType(), location);
			else if (varType instanceof IEnumeration)
				result = getBasicTypeValue(varType, location);
			else
				assert false;
		}
		return result;
	}

	public Object getValue() {
		if (value == null) {
			value = new Object();
			Object location = getValueLocation();
			if (location instanceof IAddress || location instanceof IRegisterVariableLocation) {
				IType varType = variable.getType();
				if (varType != null) {
					value = getValueByType(varType, location);
				} else
					assert false;
			} else if (!(location instanceof IInvalidVariableLocation)) {
				assert false;
			}
		}
		return value;
	}

	// used for internal expression variable value objects
	public void setValue(Object value) {
		this.value = value;
	}

	private Object getAggregateTypeValue(IAggregate varType, Object location) {
		// assumes that an array, class, struct, or union is always located in
		// memory, not in a register
		if (location instanceof IAddress)
			return ((IAddress) location).getValue();
		else
			return new BigInteger("0"); //$NON-NLS-1$
	}

	private Object getBasicTypeValue(IType varType, Object location) {
		return getBasicTypeValue(varType, location, this.servicesTracker, this.frame);
	}

	private Object getBasicTypeValue(IType varType, Object location, DsfServicesTracker servicesTracker,
			StackFrameDMC frame) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.VARIABLE_VALUE_TRACE,
				new Object[] { varType, location });

		Object result = new Object();
		int varSize = varType.getByteSize();
		boolean isOK = true;
		
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
			isSigned = type.isSigned();
			isShort = type.isShort();
			isLong = type.isLong();
			
			if (varType instanceof ICPPBasicType) {
				ICPPBasicType cppType = (ICPPBasicType) varType;
				isLongLong = (cppType.getQualifierBits() & ICPPBasicType.IS_LONG_LONG) != 0;
				isComplex  = (cppType.getQualifierBits() & ICPPBasicType.IS_COMPLEX) != 0;
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

		// for variables in memory
		Memory memoryService = null;
		ArrayList<MemoryByte> memBuffer = null;
		IStatus memGetStatus = null;

		// for variables in registers
		boolean inRegister = location instanceof IRegisterVariableLocation;
		Registers registerService = null;
		IRegisterVariableLocation registerLocation = null;
		BigInteger registerValue = new BigInteger("0");

		if (location instanceof IAddress) {
			memoryService = servicesTracker.getService(Memory.class);
			memBuffer = new ArrayList<MemoryByte>();
			memGetStatus = memoryService.getMemory(frame.getExecutionDMC(), (IAddress) location, memBuffer, varSize, 1);
			isOK = memGetStatus.isOK();
			if (!isOK) {
				result = new InvalidExpression(ASTEvalMessages.VariableWithValue_ErrorReadingMemory +
						((IAddress) location).toHexAddressString());
			}
		} else if (inRegister) {
			registerService = servicesTracker.getService(Registers.class);
			registerLocation = (IRegisterVariableLocation) location;
			String registerValueString = registerService.getRegisterValue(frame.getExecutionDMC(), registerLocation
					.getRegisterID());
			isOK = registerValueString != null; 
			if (!isOK) {
				result = new InvalidExpression(ASTEvalMessages.VariableWithValue_InvalidRegisterID +
						registerLocation.getRegisterID());
			} else {
				registerValue = new BigInteger(registerValueString, 16);
			}
		} else {
			isOK = false;
			result = new InvalidExpression(ASTEvalMessages.VariableWithValue_UnknownLocation);
		}
		
		if (!isOK) {
			// bad memory, register, or other location
			EDCDebugger.getDefault().getTrace().traceExit(IEDCTraceOptions.VARIABLE_VALUE_TRACE, result);
			return result;
		}

		switch (basicType) {
		case IBasicType.t_float:
		case IBasicType.t_double:
			if (inRegister) {
				if (varSize == 4) {
					result = Float.intBitsToFloat(registerValue.intValue());
				} else if (varSize == 8) {
					result = Double.longBitsToDouble(registerValue.longValue());
				} else {
					// TODO: support 12-byte long double read from register
					result = new Double(0);
				}
			} else {
				if (varSize == 4) {
					result = Float.intBitsToFloat(MemoryUtils.convertByteArrayToInt(memBuffer.subList(0, 4)
							.toArray(new MemoryByte[4]), MemoryUtils.LITTLE_ENDIAN));
				} else if (varSize == 8) {
					result = Double.longBitsToDouble(MemoryUtils.convertByteArrayToLong(memBuffer.subList(0, 8)
							.toArray(new MemoryByte[8]), MemoryUtils.LITTLE_ENDIAN));
				} else {
					// TODO: support 12-byte long double read from memory
					result = new Double(0);
				}
			}
			break;

		case ICPPBasicType.t_bool:
		case ICPPBasicType.t_wchar_t:
		case IBasicType.t_char:
		case IBasicType.t_int:
		case IBasicType.t_void:
			if (inRegister) {
				if (isSigned) {
					// as needed, mask the value and sign-extend
					if (varSize == 4) {
						result = new Integer(registerValue.intValue());
					} else if (varSize == 2) {
						int intResult = registerValue.intValue() & 0xffff;
						if ((intResult & 0x00008000) != 0)
							intResult |= 0xffff0000;
						result = new Integer(intResult);
					} else if (varSize == 1) {
						int intResult = registerValue.intValue() & 0xff;
						if ((intResult & 0x00000080) != 0)
							intResult |= 0xffffff00;
						result = new Integer(intResult);
					} else {
						// assume an 8-byte long is the default
						result = new Long(registerValue.longValue());
					}
				} else {
					if (varSize == 4) {
						result = new Integer(registerValue.intValue());
					} else if (varSize == 2) {
						result = new Integer(registerValue.intValue() & 0xffff);
					} else if (varSize == 1) {
						result = new Integer(registerValue.intValue() & 0xff);
					} else {
						// assume an 8-byte long is the default
						result = new Long(registerValue.longValue());
					}
				}
			} else {
				if (isSigned) {
					if (varSize == 4) {
						result = new Integer(MemoryUtils.convertByteArrayToInt(memBuffer.subList(0, 4).toArray(
								new MemoryByte[4]), MemoryUtils.LITTLE_ENDIAN));
					} else if (varSize == 2) {
						result = new Integer(MemoryUtils.convertByteArrayToShort(memBuffer.subList(0, 2).toArray(
								new MemoryByte[2]), MemoryUtils.LITTLE_ENDIAN));
					} else if (varSize == 1) {
						result = new Integer(MemoryUtils.convertByteArrayToInt(memBuffer.subList(0, 1).toArray(
								new MemoryByte[1]), MemoryUtils.LITTLE_ENDIAN));
					} else if (varSize == 8) {
						result = new Long(MemoryUtils.convertByteArrayToLong(memBuffer.subList(0, 8).toArray(
								new MemoryByte[8]), MemoryUtils.LITTLE_ENDIAN));
					}
				} else {
					if (varSize < 8) {
						result = MemoryUtils.convertByteArrayToUnsignedLong(memBuffer.subList(0, varSize).toArray(
								new MemoryByte[varSize]), MemoryUtils.LITTLE_ENDIAN);
					} else {
						byte[] bytes = new byte[varSize];
						int i = 0;
						for (; i < varSize && i <= memBuffer.size(); i++)
							bytes[i] = memBuffer.get(i).getValue();
						for (; i < varSize; i++)
							bytes[i] = 0;

						result = MemoryUtils.convertByteArrayToUnsignedBigInt(bytes, MemoryUtils.LITTLE_ENDIAN,
								varSize);
					}
				}
			}
			break;

		case IBasicType.t_unspecified:
		default:
			assert false;
			break;
		}

		EDCDebugger.getDefault().getTrace().traceExit(IEDCTraceOptions.VARIABLE_VALUE_TRACE, result);
		return result;
	}
	
	private int getDefaultSize(IType varType, int basicType, boolean isShort, boolean isLong,
			boolean isLongLong, boolean isComplex) {
		int varSize = 0;
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
		
		return varSize;
	}

}
