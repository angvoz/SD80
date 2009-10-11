/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rational Software - initial implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import java.util.LinkedList;

import org.eclipse.cdt.core.dom.ast.c.ICArrayType;
import org.eclipse.cdt.core.dom.ast.c.ICBasicType;
import org.eclipse.cdt.core.dom.ast.c.ICPointerType;
import org.eclipse.cdt.core.dom.ast.c.ICQualifierType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPBasicType;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPPointerType;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPQualifierType;
import org.eclipse.cdt.core.parser.GCCKeywords;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTTypeId;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.cdt.internal.core.dom.parser.c.ICInternalBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTypeId;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

/**
 * This is a utility class to help convert AST elements to Strings corresponding to the
 * AST element's type.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class ASTTypeUtil {
	private static final String COMMA_SPACE = ", "; //$NON-NLS-1$
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private static final String SPACE = " "; //$NON-NLS-1$
	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	private static final int DEAULT_ITYPE_SIZE = 2;

	/**
	 * Returns a string representation for the parameters of the given function type. The 
	 * representation contains the comma-separated list of the normalized parameter type
	 * representations wrapped in parenthesis. 
	 */
	public static String getParameterTypeString(IFunctionType type) {
		StringBuilder result = new StringBuilder();
		String[] parms = getParameterTypeStringArray(type);
		
		result.append(Keywords.cpLPAREN);
		for (int i = 0; i < parms.length; i++) {
			if (parms[i] != null) {
				result.append(parms[i]);
				if (i < parms.length - 1)
					result.append(COMMA_SPACE);
			}
		}
		result.append(Keywords.cpRPAREN);
		return result.toString();
	}

	/**
	 * @return Whether the function matching the given function binding takes
	 *         parameters or not.
	 * @throws DOMException
	 * 
	 * @since 5.1
	 */
	public static boolean functionTakesParameters(IFunction function)
			throws DOMException {
		IParameter[] parameters = function.getParameters();

		if (parameters.length == 0) {
			return false;
		} else if (parameters.length == 1) {
			IType ultimateType = SemanticUtil.getNestedType(parameters[0].getType(), TDEF);

			if (ultimateType instanceof IBasicType) {
				if (((IBasicType) ultimateType).getType() == IBasicType.t_void) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Returns a string representation for the type array. The representation is
	 * a comma-separated list of the normalized string representations of the 
	 * provided types.
	 * @see #getTypeListString(IType[], boolean)
	 */
	public static String getTypeListString(IType[] types) {
		return getTypeListString(types, true);
	}

	/**
	 * Returns a String representation of the type array as a
	 * comma-separated list.
	 * @param types
	 * @return representation of the type array as a comma-separated list 
	 * @since 5.1
	 */
	public static String getTypeListString(IType[] types, boolean normalize) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < types.length; i++) {
			if (types[i] != null) {
				result.append(getType(types[i], normalize));
				if (i < types.length - 1)
					result.append(COMMA_SPACE);
			}
		}
		return result.toString();
	}
	
	/**
	 * Returns a comma-separated list of the string representations of the arguments, enclosed
	 * in angle brackets. 
	 * Optionally normalization is performed:
	 * <br> template parameter names are represented by their parameter position,
	 * <br> further normalization may be performed in future versions.
	 * @param normalize indicates whether normalization shall be performed
	 * @since 5.1
	 */
	public static String getArgumentListString(ICPPTemplateArgument[] args, boolean normalize) {
		StringBuilder result= new StringBuilder();
		boolean first= true;
		result.append('<');
		for (ICPPTemplateArgument arg : args) {
			if (!first) {
				result.append(',');
			}
			first= false;
			result.append(getArgumentString(arg, normalize));
		}
		result.append('>');
		return result.toString();
	}

	/**
	 * Returns a string representation for an template argument. Optionally 
	 * normalization is performed:
	 * <br> template parameter names are represented by their parameter position,
	 * <br> further normalization may be performed in future versions.
	 * @param normalize indicates whether normalization shall be performed
	 * @since 5.1
	 */
	public static String getArgumentString(ICPPTemplateArgument arg, boolean normalize) {
		IValue val= arg.getNonTypeValue();
		if (val != null)
			return new String(val.getSignature());

		return getType(arg.getTypeValue(), normalize);
	}

	/**
	 * Returns an array of normalized string representations for the parameter types of the
	 * given function type.
	 * @see #getType(IType, boolean)
	 */
	public static String[] getParameterTypeStringArray(IFunctionType type) {
		IType[] parms = null;
		try {
			parms = type.getParameterTypes();
		} catch (DOMException e) {
			return EMPTY_STRING_ARRAY;
		}
		
		String[] result = new String[parms.length];
		
		for (int i = 0; i < parms.length; i++) {
			if (parms[i] != null) {
				result[i] = getType(parms[i]);
			}
		}
		
		return result;
	}
	
	private static String getTypeString(IType type, boolean normalize) {
		StringBuilder result = new StringBuilder();
		boolean needSpace = false;
		
		if (type instanceof IArrayType) {
			result.append(Keywords.cpLBRACKET);
			if (type instanceof ICArrayType) {
				try {
					if (((ICArrayType) type).isConst()) {
						result.append(Keywords.CONST); needSpace = true;
					}
					if (((ICArrayType) type).isRestrict()) {
						if (needSpace) {
							result.append(SPACE); needSpace = false;
						}
						result.append(Keywords.RESTRICT); needSpace = true;
					}
					if (((ICArrayType) type).isStatic()) {
						if (needSpace) {
							result.append(SPACE); needSpace = false;
						}
						result.append(Keywords.STATIC); needSpace = true;
					}
					if (((ICArrayType) type).isVolatile()) {
						if (needSpace) {
							result.append(SPACE); needSpace = false;
						}
						result.append(Keywords.VOLATILE);
					}
				} catch (DOMException e) {
				}
			}
			result.append(Keywords.cpRBRACKET);
		} else if (type instanceof IBasicType) {
			IBasicType basicType= (IBasicType) type;
			try {
				if (basicType.isSigned()) {
					// 3.9.1.2: signed integer types
					if (!normalize || basicType.getType() == IBasicType.t_char) {
						result.append(Keywords.SIGNED); needSpace = true;
					}
				} else if (basicType.isUnsigned()) {
					if (needSpace) {
						result.append(SPACE); needSpace = false;
					}
					result.append(Keywords.UNSIGNED); needSpace = true;
				}
				if (basicType.isLong()) {
					if (needSpace) {
						result.append(SPACE); needSpace = false;
					}
					result.append(Keywords.LONG); needSpace = true;
				} else if (basicType.isShort()) {
					if (needSpace) {
						result.append(SPACE); needSpace = false;
					}
					result.append(Keywords.SHORT); needSpace = true;
				}
			} catch (DOMException e) {
			}
			
			if (type instanceof IGPPBasicType) {
				try {
					if (((IGPPBasicType) type).isLongLong()) {
						if (needSpace) {
							result.append(SPACE); needSpace = false;
						}
						result.append(Keywords.LONG_LONG); needSpace = true;
					}
					if (((IGPPBasicType) type).isComplex()) {
						if (needSpace) {
							result.append(SPACE); needSpace = false;
						}
						result.append(Keywords.c_COMPLEX); needSpace = true;
					}
					if (((IGPPBasicType) type).isImaginary()) {
						if (needSpace) {
							result.append(SPACE); needSpace = false;
						}
						result.append(Keywords.c_IMAGINARY); needSpace = true;
					}

					switch (((IGPPBasicType) type).getType()) {
						case IGPPBasicType.t_typeof:
							result.append(GCCKeywords.TYPEOF);
							break;						
					}
				} catch (DOMException e) {
				}
			} else if (type instanceof ICPPBasicType) {
				try {
					switch (((ICPPBasicType) type).getType()) {
						case ICPPBasicType.t_bool:
							result.append(Keywords.BOOL);
							break;
						case ICPPBasicType.t_wchar_t:
							result.append(Keywords.WCHAR_T);
							break;
					}
				} catch (DOMException e) {
				}
			} else if (type instanceof ICBasicType) {
				try {
					if (((ICBasicType) type).isComplex()) {
						if (needSpace) {
							result.append(SPACE); needSpace = false;
						}
						result.append(Keywords.c_COMPLEX); needSpace = true;
					}
					if (((ICBasicType) type).isImaginary()) {
						if (needSpace) {
							result.append(SPACE); needSpace = false;
						}
						result.append(Keywords.c_IMAGINARY); needSpace = true;
					}
					
					switch (((ICBasicType) type).getType()) {
						case ICBasicType.t_Bool:
							result.append(Keywords.c_BOOL);
							break;
					}
				} catch (DOMException e) {
				}
			}
			
			try {
				switch (basicType.getType()) {
					case IBasicType.t_char:
						if (needSpace) result.append(SPACE);
						result.append(Keywords.CHAR);
						break;
					case IBasicType.t_double:
						if (needSpace) result.append(SPACE);
						result.append(Keywords.DOUBLE);
						break;
					case IBasicType.t_float:
						if (needSpace) result.append(SPACE);
						result.append(Keywords.FLOAT);
						break;
					case IBasicType.t_int:
						if (needSpace) result.append(SPACE);
						result.append(Keywords.INT);
						break;
					case IBasicType.t_void:
						if (needSpace) result.append(SPACE);
						result.append(Keywords.VOID);
						break;
				}
			} catch (DOMException e) {
			}
		} else if (type instanceof ICPPTemplateParameter) {
			final ICPPTemplateParameter tpar = (ICPPTemplateParameter) type;
			if (normalize) {
				result.append('#');
				result.append(Integer.toString(tpar.getParameterID(), 16));
			} else {
				result.append(tpar.getName());
			}
		} else if (type instanceof ICompositeType) {
//			101114 fix, do not display class, and for consistency don't display struct/union as well
			if (type instanceof ICPPClassType) {
				try {
					String qn = CPPVisitor.renderQualifiedName(getQualifiedNameForAnonymous((ICPPClassType) type, normalize));
					result.append(qn);
				} catch (DOMException e) {
					result.append(getNameForAnonymous((ICompositeType) type));
				}
			} else {
				result.append(getNameForAnonymous((ICompositeType) type));
			}
			if (type instanceof ICPPTemplateInstance) {
				ICPPTemplateInstance inst = (ICPPTemplateInstance) type;
				result.append(getArgumentListString(inst.getTemplateArguments(), normalize));
			}
		} else if (type instanceof ICPPReferenceType) {
			result.append(Keywords.cpAMPER);
		} else if (type instanceof IEnumeration) {
			result.append(Keywords.ENUM);
			result.append(SPACE);
			result.append(getNameForAnonymous((IEnumeration) type));
		} else if (type instanceof IFunctionType) {
			try {
				String temp = getType(((IFunctionType) type).getReturnType(), normalize);
				if (temp != null && !temp.equals(EMPTY_STRING)) {
					result.append(temp); needSpace = true;
				}
				if (needSpace) {
					result.append(SPACE); needSpace = false;
				}
				temp = getParameterTypeString((IFunctionType) type);
				if (temp != null && !temp.equals(EMPTY_STRING)) {
					result.append(temp); needSpace = false;
				}
				if (type instanceof ICPPFunctionType) {
					ICPPFunctionType ft= (ICPPFunctionType) type;
					needSpace= appendCVQ(result, needSpace, ft.isConst(), ft.isVolatile());
				}
			} catch (DOMException e) {
			}
		} else if (type instanceof IPointerType) {
			if (type instanceof ICPPPointerToMemberType) {
				result.append(getTypeString(((ICPPPointerToMemberType) type).getMemberOfClass(), normalize));
				result.append(Keywords.cpCOLONCOLON);
			}
			result.append(Keywords.cpSTAR); needSpace = true;
			
			if (type instanceof IGPPPointerType) {
				if (((IGPPPointerType) type).isRestrict()) {
					if (needSpace) {
						result.append(SPACE); needSpace = false;
					}
					result.append(Keywords.RESTRICT); needSpace = true;
				}
			} else if (type instanceof ICPointerType) {
				if (((ICPointerType) type).isRestrict()) {
					if (needSpace) {
						result.append(SPACE); needSpace = false;
					}
					result.append(Keywords.RESTRICT); needSpace = true;
				}
			}
			
			IPointerType pt= (IPointerType) type;
			needSpace= appendCVQ(result, needSpace, pt.isConst(), pt.isVolatile());
		} else if (type instanceof IQualifierType) {
			if (type instanceof ICQualifierType) {
				if (((ICQualifierType) type).isRestrict()) {
					result.append(Keywords.RESTRICT); needSpace = true;
				}
			} else if (type instanceof IGPPQualifierType) {
				if (((IGPPQualifierType) type).isRestrict()) {
					result.append(Keywords.RESTRICT); needSpace = true;
				}
			}
			
			IQualifierType qt= (IQualifierType) type;
			needSpace= appendCVQ(result, needSpace, qt.isConst(), qt.isVolatile());
		} else if (type instanceof ITypedef) {
			result.append(((ITypedef) type).getNameCharArray());
		}
		
		return result.toString();
	}

	private static boolean appendCVQ(StringBuilder target, boolean needSpace, final boolean isConst,
			final boolean isVolatile) {
		if (isConst) {
			if (needSpace) {
				target.append(SPACE); needSpace = false;
			}
			target.append(Keywords.CONST); needSpace = true;
		}
		if (isVolatile) {
			if (needSpace) {
				target.append(SPACE); needSpace = false;
			}
			target.append(Keywords.VOLATILE); needSpace = true;
		}
		return needSpace;
	}

	/**
	 * Returns the normalized string representation of the type. 
	 * @see #getType(IType, boolean)
	 */
	public static String getType(IType type) {
		return getType(type, true);
	}
		
	/**
	 * Returns a string representation of a type.  
	 * Optionally the representation is normalized:
	 * <br> typedefs are resolved
	 * <br> template parameter names are represented by their parameter position
	 * <br> further normalization may be performed in the future.
	 * @param type a type to compute the string representation for.
	 * @param normalize whether or not normalization should be performed.
	 * @return the type representation of the IType
	 */
	public static String getType(IType type, boolean normalize) {
		StringBuilder result = new StringBuilder();
		IType[] types = new IType[DEAULT_ITYPE_SIZE];
		
		// push all of the types onto the stack
		int i = 0;
		IQualifierType cvq= null;
		while (type != null && ++i < 100) {
			if (!normalize) {
			    types = (IType[]) ArrayUtil.append(IType.class, types, type);
				if (type instanceof ITypedef) {
					type= null;	// stop here
				}
			} else {
				if (type instanceof ITypedef) {
					// skip it
				} else {
					if (cvq != null) {
						if (type instanceof IQualifierType || type instanceof IPointerType) {
							type= SemanticUtil.addQualifiers(type, cvq.isConst(), cvq.isVolatile());
							cvq= null;
						} else {
							types = (IType[]) ArrayUtil.append(IType.class, types, cvq);							
						}
					} 
					if (type instanceof IQualifierType) {
						cvq= (IQualifierType) type;
					} else {
						types = (IType[]) ArrayUtil.append(IType.class, types, type);
					} 
				}
			}
			if (type instanceof ITypeContainer) {
				try {
					type = ((ITypeContainer) type).getType();
				} catch (DOMException e) {
					type= null;
				}
			} else {
				type= null;
			}
		}	 
		
		// pop all of the types off of the stack, and build the string representation while doing so
		for (int j = types.length - 1; j >= 0; j--) {
			if (types[j] != null && result.length() > 0)
				result.append(SPACE); // only add a space if this is not the first type being added

			if (types[j] != null) {
                if (j > 0 && types[j - 1] instanceof IQualifierType) {
                    result.append(getTypeString(types[j - 1], normalize));
                    result.append(SPACE);
                    result.append(getTypeString(types[j], normalize));
                    --j;
                } else {
                    result.append(getTypeString(types[j], normalize));
                }
            }
		}

		return result.toString();
	}

	/**
	 * For testing purposes, only.
	 * Returns the normalized string representation of the type defined by the given declarator.
	 *  
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static String getType(IASTDeclarator decltor) {
		// get the most nested declarator
		while (decltor.getNestedDeclarator() != null)
			decltor = decltor.getNestedDeclarator();
		
		IBinding binding = decltor.getName().resolveBinding();
		IType type = null;
		
		try {
			if (binding instanceof IEnumerator) {
				type = ((IEnumerator)binding).getType();
			} else if (binding instanceof IFunction) {
				type = ((IFunction)binding).getType();
			} else if (binding instanceof ITypedef) {
				type = ((ITypedef)binding).getType();
			} else if (binding instanceof IVariable) {
				type = ((IVariable)binding).getType();
			}
		} catch (DOMException e) {
			return EMPTY_STRING;
		}
		
		if (type != null) {
			return getType(type);
		}
		
		return EMPTY_STRING;
	}

	/**
	 * For testing purposes, only.
	 * Return's the String representation of a node's type (if available).  
	 * 
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static String getNodeType(IASTNode node) {
		try {
			if (node instanceof IASTDeclarator)
				return getType((IASTDeclarator) node);
			if (node instanceof IASTName && ((IASTName) node).resolveBinding() instanceof IVariable)
				return getType(((IVariable)((IASTName) node).resolveBinding()).getType());
			if (node instanceof IASTName && ((IASTName) node).resolveBinding() instanceof IFunction)
				return getType(((IFunction)((IASTName) node).resolveBinding()).getType());
			if (node instanceof IASTName && ((IASTName) node).resolveBinding() instanceof IType)
				return getType((IType)((IASTName) node).resolveBinding());
			if (node instanceof IASTTypeId)
				return getType((IASTTypeId) node);
		} catch (DOMException e) {
			return EMPTY_STRING;
		}
		
		return EMPTY_STRING;
	}
	
	/**
	 * Returns the type representation of the IASTTypeId as a String.
	 * 
	 * @param typeId
	 * @return the type representation of the IASTTypeId as a String
	 */
	public static String getType(IASTTypeId typeId) {
		if (typeId instanceof CASTTypeId)
			return createCType(typeId.getAbstractDeclarator());
		else if (typeId instanceof CPPASTTypeId)
			return createCPPType(typeId.getAbstractDeclarator());
		
		return EMPTY_STRING;
	}
	
	private static String createCType(IASTDeclarator declarator) {
		IType type = CVisitor.createType(declarator);
		return getType(type);
	}
	
	private static String createCPPType(IASTDeclarator declarator) {
		IType type = CPPVisitor.createType(declarator);
		return getType(type);
	}
	
	/**
	 * This can be used to invoke the IType's isConst() if it has an isConst() method.
     * This returns the result of that invoked isConst() method.
     * It is a convenience function so that the structure of IType does not need
	 * to be known to determine if the IType is const or not.
     *
     * Note:  false is returned if no isConst() method is found
     * 
	 * @param type
	 */
	public static boolean isConst(IType type) {
		if (type instanceof IQualifierType) {
			return ((IQualifierType) type).isConst();
		} else if (type instanceof ITypeContainer) {
			try {
				return isConst(((ITypeContainer) type).getType());
			} catch (DOMException e) {
				return false;
			}
		} else if (type instanceof IArrayType) {
			try {
				return isConst(((IArrayType) type).getType());
			} catch (DOMException e) {
				return false;
			}
		} else if (type instanceof ICPPReferenceType) {
			try {
				return isConst(((ICPPReferenceType) type).getType());
			} catch (DOMException e) {
				return false;
			}
		} else if (type instanceof IFunctionType) {
			try {
				return isConst(((IFunctionType) type).getReturnType());
			} catch (DOMException e) {
				return false;
			}
		} else if (type instanceof IPointerType) {
			try {
				return isConst(((IPointerType) type).getType());
			} catch (DOMException e) {
				return false;
			}
		} else if (type instanceof ITypedef) {
			try {
				return isConst(((ITypedef) type).getType());
			} catch (DOMException e) {
				return false;
			}
		} else {
			return false;
		}
	}

	private static String[] getQualifiedNameForAnonymous(ICPPBinding binding, boolean normalize) throws DOMException {
		LinkedList<String> result= new LinkedList<String>();
		result.addFirst(getNameForAnonymous(binding));
		
		IBinding owner= binding.getOwner();
		while (owner instanceof ICPPNamespace || owner instanceof IType) {
			char[] name= owner.getNameCharArray();
			if (name == null || name.length == 0) {
				if (!(binding instanceof ICPPNamespace)) {
					char[] altname= createNameForAnonymous(binding);
					if (altname != null) {
						result.addFirst(new String(altname));
					}
				}
			} else {
				if (normalize && owner instanceof IType) {
					result.addFirst(getType((IType) owner, normalize));
				} else {
					result.addFirst(new String(name));
				}
			}
			if (owner instanceof ICPPTemplateParameter)
				break;
			
			owner= owner.getOwner();
		}
	    return result.toArray(new String[result.size()]);
	}
	
	private static String getNameForAnonymous(IBinding binding) {
		char[] name= binding.getNameCharArray();
		if (name == null || name.length == 0) {
			char[] altname= createNameForAnonymous(binding);
			if (altname != null) {
				return new String(altname);
			}
		}
		return new String(name);
	}

	public static char[] createNameForAnonymous(IBinding binding) {
		IASTNode node= null;
		if (binding instanceof ICInternalBinding) {
			node= ((ICInternalBinding) binding).getPhysicalNode();
		} else if (binding instanceof ICPPInternalBinding) {
			node= ((ICPPInternalBinding) binding).getDefinition();
		}
		if (node != null) {
			IASTFileLocation loc= node.getFileLocation();
			if (loc == null) {
				node= node.getParent();
				if (node != null) {
					loc= node.getFileLocation();
				}
			}
			if (loc != null) {
				char[] fname= loc.getFileName().toCharArray();
				int fnamestart= findFileNameStart(fname);
				StringBuilder buf= new StringBuilder();
				buf.append('{');
				buf.append(fname, fnamestart, fname.length-fnamestart);
				buf.append(':');
				buf.append(loc.getNodeOffset());
				buf.append('}');
				return buf.toString().toCharArray();
			}
		}
		return null;
	}

	private static int findFileNameStart(char[] fname) {
		for (int i= fname.length - 2; i >= 0; i--) {
			switch (fname[i]) {
			case '/':
			case '\\':
				return i+1;
			}
		}
		return 0;
	}
}
