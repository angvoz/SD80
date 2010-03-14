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

package org.eclipse.cdt.debug.edc.symbols;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.IArrayDimensionType;
import org.eclipse.cdt.debug.edc.internal.symbols.ArrayBoundType;
import org.eclipse.cdt.debug.edc.internal.symbols.ArrayType;
import org.eclipse.cdt.debug.edc.internal.symbols.CPPBasicType;
import org.eclipse.cdt.debug.edc.internal.symbols.IArrayBoundType;
import org.eclipse.cdt.debug.edc.internal.symbols.IArrayType;
import org.eclipse.cdt.debug.edc.internal.symbols.ICPPBasicType;
import org.eclipse.cdt.debug.edc.internal.symbols.ICompositeType;
import org.eclipse.cdt.debug.edc.internal.symbols.IPointerType;
import org.eclipse.cdt.debug.edc.internal.symbols.IQualifierType;
import org.eclipse.cdt.debug.edc.internal.symbols.IReferenceType;
import org.eclipse.cdt.debug.edc.internal.symbols.ISubroutineType;
import org.eclipse.cdt.debug.edc.internal.symbols.ITypedef;
import org.eclipse.cdt.debug.edc.services.ITargetEnvironment;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;

/**
 * This class manages the {@link IType} instances relevant to a given target.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class TypeEngine {
	private Map<Integer, Integer> typeSizeMap;
	private Map<Object, IType> typeMap = new HashMap<Object, IType>();
	private int addressSize;
	private Map<IType, String> typeNameMap = new HashMap<IType, String>();

	public TypeEngine(DsfServicesTracker tracker) {
		//this.tracker = tracker;
		ITargetEnvironment targetEnvironment = tracker.getService(ITargetEnvironment.class);
		if (targetEnvironment != null) {
			typeSizeMap = targetEnvironment.getBasicTypeSizes();
			addressSize = targetEnvironment.getPointerSize();
		} else {
			typeSizeMap = Collections.emptyMap();
			addressSize = 4;
		}
	}
		
	/**
	 * Get the target's basic type for an integer of the given size 
	 * @param size
	 * @param isSigned
	 * @return type or <code>null</code> if no match
	 */
	public IType getIntegerTypeOfSize(int size, boolean isSigned) {
		int basicType;
		int flags;
		
		if (isSigned && typeSizeMap.get(TypeUtils.BASIC_TYPE_CHAR) == size) {
			basicType = ICPPBasicType.t_char;
			flags = ICPPBasicType.IS_SIGNED;
		} else if (!isSigned && typeSizeMap.get(TypeUtils.BASIC_TYPE_CHAR) == size) {
			basicType = ICPPBasicType.t_char;
			flags = ICPPBasicType.IS_UNSIGNED;
		} else if (isSigned && typeSizeMap.get(TypeUtils.BASIC_TYPE_SHORT) == size) {
			basicType = ICPPBasicType.t_int;
			flags = ICPPBasicType.IS_SHORT + ICPPBasicType.IS_SIGNED;
		} else if (!isSigned && typeSizeMap.get(TypeUtils.BASIC_TYPE_SHORT) == size) {
			basicType = ICPPBasicType.t_int;
			flags = ICPPBasicType.IS_SHORT + ICPPBasicType.IS_UNSIGNED;
		} else if (isSigned && typeSizeMap.get(TypeUtils.BASIC_TYPE_INT) == size) {
			basicType = ICPPBasicType.t_int;
			flags = ICPPBasicType.IS_SIGNED;
		} else if (!isSigned && typeSizeMap.get(TypeUtils.BASIC_TYPE_INT) == size) {
			basicType = ICPPBasicType.t_int;
			flags = ICPPBasicType.IS_UNSIGNED;
		} else if (isSigned && typeSizeMap.get(TypeUtils.BASIC_TYPE_LONG) == size) {
			basicType = ICPPBasicType.t_int;
			flags = ICPPBasicType.IS_LONG + ICPPBasicType.IS_SIGNED;
		} else if (!isSigned && typeSizeMap.get(TypeUtils.BASIC_TYPE_LONG) == size) {
			basicType = ICPPBasicType.t_int;
			flags = ICPPBasicType.IS_LONG + ICPPBasicType.IS_UNSIGNED;
		} else if (isSigned && typeSizeMap.get(TypeUtils.BASIC_TYPE_LONG_LONG) == size) {
			basicType = ICPPBasicType.t_int;
			flags = ICPPBasicType.IS_LONG_LONG + ICPPBasicType.IS_SIGNED;
		} else if (!isSigned && typeSizeMap.get(TypeUtils.BASIC_TYPE_LONG_LONG) == size) {
			basicType = ICPPBasicType.t_int;
			flags = ICPPBasicType.IS_LONG_LONG + ICPPBasicType.IS_UNSIGNED;
		} else {
			return null;
		}
		
		return getBasicType(basicType, flags, size);
	}

	/**
	 * Get the target's basic type for an integer of the given kind
	 * @param typeUtilsType from TypeUtils#BASIC_TYPE
	 * @param isSigned
	 * @return type or <code>null</code> if no match
	 */
	public IType getIntegerTypeFor(int typeUtilsBasicType, boolean isSigned) {
		return getIntegerTypeOfSize(typeSizeMap.get(typeUtilsBasicType), isSigned);
	}
	
	/**
	 * Get a cached ICPPBasicType instance
	 * @param basicType
	 * @param flags
	 * @param size
	 * @return IType
	 */
	public IType getBasicType(int basicType, int flags, int size) {
		return getBasicType(null, basicType, flags, size);
	}

	/**
	 * Get a cached ICPPBasicType instance, using a custom name.
	 * @param name
	 * @param basicType
	 * @param flags
	 * @param size
	 * @return IType
	 */
	public IType getBasicType(String name, int basicType, int flags, int size) {
		Object typeCode = (flags << 16) + (basicType << 8) + size;
		if (name != null)
			typeCode = name + ":" + typeCode;
		IType type = typeMap.get(typeCode);
		if (type == null) {
			if (name == null)
				name = getBasicTypeName(basicType, flags);
			type = new CPPBasicType(name, basicType, flags, size);
			typeMap.put(typeCode, type);
		}
		return type;
	}

	/**
	 * @param basicType
	 * @param flags
	 * @return
	 */
	private String getBasicTypeName(int basicType, int flags) {
		String name;
		switch (basicType) {
		case ICPPBasicType.t_bool:
			name = "bool"; break;
		case ICPPBasicType.t_wchar_t:
			name = "wchar_t"; break;
		case ICPPBasicType.t_char:
			name = "char"; break;
		case ICPPBasicType.t_int:
			if ((flags & ICPPBasicType.IS_SHORT) != 0)
				name = "short";
			else if ((flags & ICPPBasicType.IS_LONG) != 0)
				name = "long";
			else if ((flags & ICPPBasicType.IS_LONG_LONG) != 0)
				name = "long long";
			else
				name = "int"; 
			break;
		case ICPPBasicType.t_float:
			name = "float"; break;
		case ICPPBasicType.t_double:
			if ((flags & ICPPBasicType.IS_LONG) != 0)
				name = "long double";
			else
				name = "double"; 
			break;
		case ICPPBasicType.t_unspecified:
			name = "<<unknown>>";
			break;
		case ICPPBasicType.t_void:
			name = "void";
			break;
		default:
			assert(false);
			name = "";
			break;
		}
		
		if ((flags & ICPPBasicType.IS_SIGNED) != 0)
			name = "signed " + name;
		else if ((flags & ICPPBasicType.IS_UNSIGNED) != 0)
			name = "unsigned " + name;
		if ((flags & ICPPBasicType.IS_COMPLEX) != 0)
			name = "complex " + name;
		if ((flags & ICPPBasicType.IS_IMAGINARY) != 0)
			name = "imaginary " + name;
		return name;
	}

	/**
	 * Get the target's basic type for a float of the given size 
	 * @param size
	 * @param isSigned
	 * @return type or <code>null</code> if no match
	 */
	public IType getFloatTypeOfSize(int size) {
		if (typeSizeMap.get(TypeUtils.BASIC_TYPE_FLOAT) == size) {
			return getBasicType(ICPPBasicType.t_float, 0, size);
		}
		if (typeSizeMap.get(TypeUtils.BASIC_TYPE_DOUBLE) == size) {
			return getBasicType(ICPPBasicType.t_double, 0, size);
		}
		if (typeSizeMap.get(TypeUtils.BASIC_TYPE_LONG_DOUBLE) == size) {
			return getBasicType(ICPPBasicType.t_double, ICPPBasicType.IS_LONG, size);
		}
		return null;
	}

	/**
	 * Get the basic type for a character of a given size.
	 * @param size
	 * @return IType
	 */
	public IType getCharacterType(int size) {
		if (typeSizeMap.get(TypeUtils.BASIC_TYPE_CHAR) == size) {
			return getBasicType(ICPPBasicType.t_char, 0, size);
		}
		return getBasicType(ICPPBasicType.t_wchar_t, 0, size);
	}

	/**
	 * Get the basic type for a character of a given size.
	 * @param size
	 * @return IType
	 */
	public IType getWideCharacterType(int size) {
		return getBasicType(ICPPBasicType.t_wchar_t, 0, size);
	}

	public IType getBooleanType(int size) {
		return getBasicType(ICPPBasicType.t_bool, 0, size);
	}

	public IType getCharArrayType(IType charType, int length) {
		IArrayBoundType bounds = new ArrayBoundType(null, length);
		IArrayType array = new ArrayType(charType.getName() + "[" + length + "]", null, length, null);
		array.addBound(bounds);
		array.setType(charType);
		return array;
	}

	/**
	 * Get the integral type the same size as a pointer. 
	 * @return IType or <code>null</code>
	 */
	public IType getPointerSizeType() {
		int size = getPointerSize();
		return getBasicType("ptrsize_t", ICPPBasicType.t_int, 0, size);
	}

	private int getPointerSize() {
		return addressSize;
	}

	/**
	 * Get the byte size of a type
	 * @param basicType
	 * @return type, or 0 if unknown
	 */
	public int getTypeSize(int basicType) {
		Integer size = typeSizeMap.get(basicType);
		return size != null ? size.intValue() : 0;
	}

	/**
	 * @param valueType
	 * @return
	 */
	public String getTypeName(IType valueType) {
		if (valueType == null)
			return ""; //$NON-NLS-1$
		
		String typeName = typeNameMap.get(valueType);
		if (typeName == null) {
			typeName = recursiveGetType(valueType);
			typeNameMap.put(valueType, typeName);
		}
		return typeName;
	}


	private String recursiveGetType(IType type) {
		if (type == null)
			return ""; //$NON-NLS-1$
		if (type instanceof IReferenceType)
			return recursiveGetType(((IReferenceType) type).getType()) + " &"; //$NON-NLS-1$
		if (type instanceof IPointerType)
			return recursiveGetType(((IPointerType) type).getType()) + " *"; //$NON-NLS-1$
		if (type instanceof IArrayType) {
			IArrayType arrayType = (IArrayType) type;
			String returnType = recursiveGetType(arrayType.getType());

			IArrayBoundType[] bounds = arrayType.getBounds();
			for (IArrayBoundType bound : bounds) {
				returnType += "[" + bound.getBoundCount() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			return returnType;
		}
		if (type instanceof IArrayDimensionType) {
			IArrayDimensionType arrayDimensionType = (IArrayDimensionType) type;
			IArrayType arrayType = arrayDimensionType.getArrayType();
			String returnType = recursiveGetType(arrayType.getType());

			IArrayBoundType[] bounds = arrayType.getBounds();
			for (int i = arrayDimensionType.getDimensionCount(); i < arrayType.getBoundsCount(); i++) {
				returnType += "[" + bounds[i].getBoundCount() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			return returnType;
		}
		if (type instanceof ITypedef)
			return ((ITypedef) type).getName();
		if (type instanceof ICompositeType)
			return ((ICompositeType) type).getName();
		if (type instanceof IQualifierType)
			return ((IQualifierType) type).getName()
					+ " " + recursiveGetType(((IQualifierType) type).getType()); //$NON-NLS-1$
		if (type instanceof ISubroutineType) {
			// TODO: real stuff once we parse parameters
			// TODO: the '*' for a function pointer (e.g. in a vtable) is in the wrong place
			return recursiveGetType(((ISubroutineType) type).getType()) + "(...)"; //$NON-NLS-1$
		}
		return type.getName() + recursiveGetType(type.getType());
	}



}
