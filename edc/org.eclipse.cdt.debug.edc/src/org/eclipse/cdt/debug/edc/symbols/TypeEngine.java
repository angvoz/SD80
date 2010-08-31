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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.ASTSignatureUtil;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTProblemTypeId;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.PushLongOrBigInteger;
import org.eclipse.cdt.debug.edc.internal.symbols.ArrayBoundType;
import org.eclipse.cdt.debug.edc.internal.symbols.ArrayType;
import org.eclipse.cdt.debug.edc.internal.symbols.CPPBasicType;
import org.eclipse.cdt.debug.edc.internal.symbols.IArrayBoundType;
import org.eclipse.cdt.debug.edc.internal.symbols.IArrayType;
import org.eclipse.cdt.debug.edc.internal.symbols.ICPPBasicType;
import org.eclipse.cdt.debug.edc.internal.symbols.ICompositeType;
import org.eclipse.cdt.debug.edc.internal.symbols.IPointerType;
import org.eclipse.cdt.debug.edc.internal.symbols.PointerType;
import org.eclipse.cdt.debug.edc.services.ITargetEnvironment;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.core.runtime.CoreException;

/**
 * This class manages the {@link IType} instances relevant to a given target.
 * @noextend This class is not intended to be subclassed by clients.
 */
@SuppressWarnings("deprecation")
public class TypeEngine {
	private Map<Integer, Integer> typeSizeMap;
	private Map<Object, IType> typeMap = new HashMap<Object, IType>();
	private int addressSize;
	private Map<IType, String> typeNameMap = new HashMap<IType, String>();
	private final IDebugInfoProvider debugInfoProvider;
	/** map of type signature to IType or CoreException */
	private Map<String, Object> typeIdToTypeMap = new HashMap<String, Object>();
	private Map<String, IArrayType> typeToArrayTypeMap = new HashMap<String, IArrayType>();
	private boolean charIsSigned;

	public TypeEngine(DsfServicesTracker tracker, IDebugInfoProvider debugInfoProvider) {
		this.debugInfoProvider = debugInfoProvider;
		ITargetEnvironment targetEnvironment = tracker.getService(ITargetEnvironment.class);
		if (targetEnvironment != null) {
			typeSizeMap = targetEnvironment.getBasicTypeSizes();
			addressSize = targetEnvironment.getPointerSize();
			charIsSigned = targetEnvironment.isCharSigned();
		} else {
			typeSizeMap = Collections.emptyMap();
			addressSize = 4;
			charIsSigned = true;
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
			typeCode = name + ":" + typeCode; //$NON-NLS-1$
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
			name = "bool"; break; //$NON-NLS-1$
		case ICPPBasicType.t_wchar_t:
			name = "wchar_t"; break; //$NON-NLS-1$
		case ICPPBasicType.t_char:
			name = "char"; break; //$NON-NLS-1$
		case ICPPBasicType.t_int:
			if ((flags & ICPPBasicType.IS_SHORT) != 0)
				name = "short"; //$NON-NLS-1$
			else if ((flags & ICPPBasicType.IS_LONG) != 0)
				name = "long"; //$NON-NLS-1$
			else if ((flags & ICPPBasicType.IS_LONG_LONG) != 0)
				name = "long long"; //$NON-NLS-1$
			else
				name = "int";  //$NON-NLS-1$
			break;
		case ICPPBasicType.t_float:
			name = "float"; break; //$NON-NLS-1$
		case ICPPBasicType.t_double:
			if ((flags & ICPPBasicType.IS_LONG) != 0)
				name = "long double"; //$NON-NLS-1$
			else
				name = "double";  //$NON-NLS-1$
			break;
		case ICPPBasicType.t_unspecified:
			name = "<<unknown>>"; //$NON-NLS-1$
			break;
		case ICPPBasicType.t_void:
			name = "void"; //$NON-NLS-1$
			break;
		default:
			assert(false);
			name = ""; //$NON-NLS-1$
			break;
		}
		
		if ((flags & ICPPBasicType.IS_SIGNED) != 0)
			name = "signed " + name; //$NON-NLS-1$
		else if ((flags & ICPPBasicType.IS_UNSIGNED) != 0)
			name = "unsigned " + name; //$NON-NLS-1$
		if ((flags & ICPPBasicType.IS_COMPLEX) != 0)
			name = "complex " + name; //$NON-NLS-1$
		if ((flags & ICPPBasicType.IS_IMAGINARY) != 0)
			name = "imaginary " + name; //$NON-NLS-1$
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
		IArrayType array = new ArrayType(charType.getName() + "[" + length + "]", null, length, null); //$NON-NLS-1$ //$NON-NLS-2$
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
		return getBasicType("ptrsize_t", ICPPBasicType.t_int, 0, size); //$NON-NLS-1$
	}

	private int getPointerSize() {
		return addressSize;
	}

	/**
	 * Get the byte size of a type
	 * @param basicType (TypeUtils#BASIC_TYPE_xxx)
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
			typeName = TypeUtils.getFullTypeName(valueType);
			typeNameMap.put(valueType, typeName);
		}
		return typeName;
	}

	/**
	 * Convert an AST type ID into an EDC IType.
	 * @param typeId
	 * @return IType
	 * @throws CoreException if the IType cannot be created
	 */
	public IType getTypeForTypeId(IASTTypeId typeId) throws CoreException {
		if (typeId == null)
			throw EDCDebugger.newCoreException(SymbolsMessages.TypeEngine_NoTypeToCast);

		if (typeId instanceof IASTProblemTypeId)
			throw EDCDebugger.newCoreException(((IASTProblemTypeId) typeId).getProblem().getMessage());
		
		String typeSignature = ASTSignatureUtil.getSignature(typeId);

		Object obj = typeIdToTypeMap.get(typeSignature);
		if (obj instanceof CoreException)
			throw (CoreException) obj;
		
		obj = null; //HACK
		IType type = null;
		if (!(obj instanceof IType)) {
			try {
				type = createTypeForTypeId(typeId, typeSignature, type);
				typeIdToTypeMap.put(typeSignature, type);
			} catch (CoreException e) {
				typeIdToTypeMap.put(typeSignature, e);
				throw e;
			}
		} else {
			type = (IType) obj;
		}
		
		return type;
	}

	/**
	 * Create an IType mapping to IASTTypeId
	 * @param typeId
	 * @param typeSignature
	 * @param type
	 * @return new IType
	 * @throws CoreException
	 */
	private IType createTypeForTypeId(IASTTypeId typeId, String typeSignature, IType type) throws CoreException {
		IASTDeclSpecifier declSpec = typeId.getDeclSpecifier();
		if (declSpec instanceof IASTSimpleDeclSpecifier) {
			type = getTypeForDeclSpecifier((IASTSimpleDeclSpecifier) declSpec);
		} else if (declSpec instanceof IASTNamedTypeSpecifier || declSpec instanceof IASTElaboratedTypeSpecifier) {
			String typeName;
			
			int elaboration = -1;
			
			if (declSpec instanceof IASTNamedTypeSpecifier) {
				typeName = ((IASTNamedTypeSpecifier) declSpec).getName().toString();
			} else /*if (declSpec instanceof IASTElaboratedTypeSpecifier)*/ {
				// note: ignore the elaboration (class/struct/etc) since compilers are
				// inconsistent with how they do this, and furthermore, only one name
				// should be visible at a time, usually, anyway
				elaboration = ((IASTElaboratedTypeSpecifier) declSpec).getKind();
				typeName = ((IASTElaboratedTypeSpecifier) declSpec).getName().toString();
			}
			
			if (debugInfoProvider != null) {
				Collection<IType> types;
				IType aMatch = null;
				types = debugInfoProvider.getTypesByName(typeName);
				
				// try to find one matching struct/class/etc 
				for (IType aType : types) {
					aMatch = aType;
					if (elaboration < 0 ||
							(type instanceof ICompositeType && ((ICompositeType) type).getKey() == elaboration)) { 
						type = aType;
						break;
					}
				}
				
				// if no match, just take a matching name
				if (type == null && aMatch != null) {
					type = aMatch;
				}
				
				// fall through to check type != null 
			}
		} else if (declSpec instanceof IASTDeclSpecifier) {
		}
		
		if (type == null) {
			throw EDCDebugger.newCoreException(SymbolsMessages.TypeEngine_CannotResolveType + typeSignature);
		}
			
		if (typeId.getAbstractDeclarator() instanceof ICPPASTDeclarator) {
			ICPPASTDeclarator declarator = (ICPPASTDeclarator) typeId.getAbstractDeclarator();
			for (@SuppressWarnings("unused") IASTPointerOperator pointer : declarator.getPointerOperators()) {
				IType ptr = new PointerType(type.getName()+"*", type.getScope(), getPointerSize(), null); //$NON-NLS-1$
				ptr.setType(type);
				type = ptr;
			}
			
			if (declarator instanceof ICPPASTArrayDeclarator) {
				ICPPASTArrayDeclarator arrayDeclarator = (ICPPASTArrayDeclarator) declarator;
				IArrayType arrayType = new ArrayType(type.getName()+"[]", type.getScope(), 0, null); //$NON-NLS-1$
				for (IASTArrayModifier arrayMod : arrayDeclarator.getArrayModifiers()) {
					long elementCount = 1;
					if (arrayMod.getConstantExpression() != null) {
						elementCount = getConstantValue(arrayMod.getConstantExpression());
					}
					IArrayBoundType bound = new ArrayBoundType(arrayType.getScope(), elementCount);
					arrayType.addBound(bound);
				}
				arrayType.setType(type);
				type = arrayType;
			}
		}
		return type;
	}

	private long getConstantValue(IASTExpression constantExpression) throws CoreException {
		if (constantExpression instanceof IASTLiteralExpression) {
			if (((IASTLiteralExpression) constantExpression).getKind() == IASTLiteralExpression.lk_integer_constant) {
				// HACK, use more generic utilities
				PushLongOrBigInteger pusher = new PushLongOrBigInteger(constantExpression.toString());
				return pusher.getLong();
			}
		}
		throw EDCDebugger.newCoreException(SymbolsMessages.TypeEngine_ExpectedIntegerConstant + 
				ASTSignatureUtil.getExpressionString(constantExpression));
	}

	private IType getTypeForDeclSpecifier(IASTSimpleDeclSpecifier simpleDeclSpec) throws CoreException {
		int baseType = 0;
		int basicType = 0;
		switch (simpleDeclSpec.getType()) {
		case IASTSimpleDeclSpecifier.t_bool:
			baseType = ICPPBasicType.t_bool;
			basicType = TypeUtils.BASIC_TYPE_BOOL;
			break;
		case IASTSimpleDeclSpecifier.t_char:
			baseType = ICPPBasicType.t_char; 
			basicType = TypeUtils.BASIC_TYPE_CHAR;
			break;
		case IASTSimpleDeclSpecifier.t_wchar_t:
			baseType = ICPPBasicType.t_wchar_t; 
			basicType = TypeUtils.BASIC_TYPE_WCHAR_T;
			break;
		case IASTSimpleDeclSpecifier.t_double:
			baseType = ICPPBasicType.t_double; 
			basicType = TypeUtils.BASIC_TYPE_DOUBLE;
			break;
		case IASTSimpleDeclSpecifier.t_float:
			baseType = ICPPBasicType.t_float; 
			basicType = TypeUtils.BASIC_TYPE_FLOAT;
			break;
		case IASTSimpleDeclSpecifier.t_int:
			baseType = ICPPBasicType.t_int;
			basicType = TypeUtils.BASIC_TYPE_INT;
			break;
		case IASTSimpleDeclSpecifier.t_void:
			baseType = ICPPBasicType.t_void; 
			break;
		case IASTSimpleDeclSpecifier.t_typeof:
			// we'd need to parse the subexpression then get its type
		case IASTSimpleDeclSpecifier.t_decltype:
			// not sure about this one
			throw EDCDebugger.newCoreException(SymbolsMessages.TypeEngine_NoDecltypeSupport);	
		case IASTSimpleDeclSpecifier.t_unspecified:
			baseType = ICPPBasicType.t_int;
			break;
		default:
			throw EDCDebugger.newCoreException(SymbolsMessages.TypeEngine_UnhandledType + simpleDeclSpec);	
		}
		
		int flags = 0;
		if (simpleDeclSpec.isComplex()) flags |= ICPPBasicType.IS_COMPLEX;
		if (simpleDeclSpec.isImaginary()) flags |= ICPPBasicType.IS_IMAGINARY;
		if (simpleDeclSpec.isLong()) {
			flags |= ICPPBasicType.IS_LONG;
			if (basicType == 0)
				basicType = TypeUtils.BASIC_TYPE_LONG;
		}
		if (simpleDeclSpec.isLongLong()) {
			flags |= ICPPBasicType.IS_LONG_LONG;
			if (basicType == 0)
				basicType = TypeUtils.BASIC_TYPE_LONG_LONG;
		}
		if (simpleDeclSpec.isShort()) {
			flags |= ICPPBasicType.IS_SHORT;
			if (basicType == 0)
				basicType = TypeUtils.BASIC_TYPE_SHORT;
		}
		if (simpleDeclSpec.isUnsigned()) flags |= ICPPBasicType.IS_UNSIGNED;
		if (simpleDeclSpec.isSigned()) flags |= ICPPBasicType.IS_SIGNED;
		
		if (!simpleDeclSpec.isUnsigned() && !simpleDeclSpec.isSigned()) {
			if (baseType == ICPPBasicType.t_char)
				flags |= charIsSigned ? ICPPBasicType.IS_SIGNED : ICPPBasicType.IS_UNSIGNED;
			else if (baseType == ICPPBasicType.t_int)
				flags |= ICPPBasicType.IS_SIGNED;
		}
		
		int size = getTypeSize(basicType);
		return getBasicType(baseType, flags, size); 
	}
	
	/**
	 * Convert a given type to an array type
	 * @param exprType
	 * @return array type
	 * @throws CoreException if not a sensible conversion
	 */
	public IType convertToArrayType(IType type, int count) throws CoreException {
		String typeSig = getTypeName(type);
		
		IArrayType arrayType = typeToArrayTypeMap.get(typeSig);
		if (arrayType == null) {
			type = TypeUtils.getStrippedType(type);
			
			IType baseType = type;
			
			if (type instanceof IPointerType || type instanceof IArrayType) {
				baseType = type.getType();
			}
	
			if (baseType == null)
				throw EDCDebugger.newCoreException(SymbolsMessages.TypeEngine_CannotResolveBaseType + typeSig);
			
			arrayType = new ArrayType(baseType.getName()+"[]", baseType.getScope(),  //$NON-NLS-1$
					baseType.getByteSize() * count, null);
			IArrayBoundType bound = new ArrayBoundType(arrayType.getScope(), count);
			arrayType.addBound(bound);
			arrayType.setType(baseType);
			
			typeToArrayTypeMap.put(typeSig, arrayType);
		}
		
		return arrayType;
	}


}
