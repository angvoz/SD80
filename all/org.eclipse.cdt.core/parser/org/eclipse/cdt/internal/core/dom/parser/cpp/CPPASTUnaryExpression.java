/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying masterials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *    Sergey Prigogin (Google)
 *    Mike Kucera (IBM)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.REF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

/**
 * Unary expression in c++
 */
public class CPPASTUnaryExpression extends ASTNode implements ICPPASTUnaryExpression, IASTAmbiguityParent {
    private int op;
    private IASTExpression operand;

    private ICPPFunction overload = UNINITIALIZED_FUNCTION;
    private IASTImplicitName[] implicitNames = null;

    public CPPASTUnaryExpression() {
	}

	public CPPASTUnaryExpression(int operator, IASTExpression operand) {
		op = operator;
		setOperand(operand);
	}

	public CPPASTUnaryExpression copy() {
		CPPASTUnaryExpression copy = new CPPASTUnaryExpression(op, operand == null ? null : operand.copy());
		copy.setOffsetAndLength(this);
		return copy;
	}
	
	public int getOperator() {
        return op;
    }

    public void setOperator(int operator) {
        assertNotFrozen();
        op = operator;
    }

    public IASTExpression getOperand() {
        return operand;
    }

    public void setOperand(IASTExpression expression) {
        assertNotFrozen();
        operand = expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(OPERAND);
		}
    }

    public boolean isPostfixOperator() {
    	return op == op_postFixDecr || op == op_postFixIncr;
    }

    /**
     * @see org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner#getImplicitNames()
     */
    public IASTImplicitName[] getImplicitNames() {
		if (implicitNames == null) {
			ICPPFunction overload = getOverload();
			if (overload == null) {
				implicitNames = IASTImplicitName.EMPTY_NAME_ARRAY;
			} else {
				CPPASTImplicitName operatorName = new CPPASTImplicitName(overload.getNameCharArray(), this);
				operatorName.setOperator(true);
				operatorName.setBinding(overload);
				operatorName.computeOperatorOffsets(operand, isPostfixOperator());
				implicitNames = new IASTImplicitName[] { operatorName };
			}
		}
		
		return implicitNames;
	}
	
    @Override
	public boolean accept(ASTVisitor action) {
        if (action.shouldVisitExpressions) {
		    switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP:  return true;
	            default: break;
	        }
		}
 
        final boolean isPostfix = isPostfixOperator();

        if (!isPostfix && action.shouldVisitImplicitNames) {
        	for (IASTImplicitName name : getImplicitNames()) {
        		if (!name.accept(action))
        			return false;
        	}
        }

        if (operand != null && !operand.accept(action))
        	return false;

        if (isPostfix && action.shouldVisitImplicitNames) {
        	for (IASTImplicitName name : getImplicitNames()) {
        		if (!name.accept(action))
        			return false;
        	}
        }

        if (action.shouldVisitExpressions) {
		    switch (action.leave(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP:  return true;
	            default: break;
	        }
		}
        return true;
    }

    public void replace(IASTNode child, IASTNode other) {
        if (child == operand) {
            other.setPropertyInParent(child.getPropertyInParent());
            other.setParent(child.getParent());
            operand  = (IASTExpression) other;
        }
    }

    public ICPPFunction getOverload() {
    	if (overload != UNINITIALIZED_FUNCTION)
    		return overload;
    	
    	overload = CPPSemantics.findOverloadedOperator(this);
    	if (overload != null && op == op_amper && computePointerToMemberType() instanceof CPPPointerToMemberType)
    		overload = null;
    	
    	return overload;
    }

    private IType computePointerToMemberType() {
    	IASTNode child= operand;
		boolean inParenthesis= false;
		while (child instanceof IASTUnaryExpression && ((IASTUnaryExpression) child).getOperator() == IASTUnaryExpression.op_bracketedPrimary) {
			child= ((IASTUnaryExpression) child).getOperand();
			inParenthesis= true;
		}
		if (child instanceof IASTIdExpression) {
			IASTName name= ((IASTIdExpression) child).getName();
			IBinding b= name.resolveBinding();
			if (b instanceof ICPPMember) {
				ICPPMember member= (ICPPMember) b;
				try {
					if (name instanceof ICPPASTQualifiedName) {
						if (!member.isStatic()) { // so if the member is static it will fall through
							if (!inParenthesis) {
								return new CPPPointerToMemberType(member.getType(), member.getClassOwner(), false, false);
							} else if (member instanceof IFunction) {
								return new ProblemBinding(operand, IProblemBinding.SEMANTIC_INVALID_TYPE, operand.getRawSignature().toCharArray());
							}
						}
					}
				} catch (DOMException e) {
					return e.getProblem();
				}
			}
		}
		return null;
    }

    public IType getExpressionType() {
    	final int op= getOperator();
		switch (op) {
		case op_sizeof:
		case op_sizeofParameterPack:
			return CPPVisitor.get_SIZE_T(this);
		case op_typeid:
			return CPPVisitor.get_type_info(this);
		}
		
		final IASTExpression operand = getOperand();

		if (op == op_amper) {  // check for pointer to member
			IType ptm = computePointerToMemberType();
			if (ptm != null)
				return ptm;

			IType operator = findOperatorReturnType();
			if (operator != null)
				return operator;

			IType type= operand.getExpressionType();
			type = SemanticUtil.getNestedType(type, TDEF | REF);
			return new CPPPointerType(type);
		}

		if (op == op_star) {
			IType type= operand.getExpressionType();
			type = SemanticUtil.getNestedType(type, TDEF | REF | CVTYPE);
	    	if (type instanceof IProblemBinding) {
	    		return type;
	    	}
		    IType operator = findOperatorReturnType();
			if (operator != null) {
				return operator;
			} else if (type instanceof IPointerType || type instanceof IArrayType) {
				return ((ITypeContainer) type).getType();
			} else if (type instanceof ICPPUnknownType) {
				return CPPUnknownClass.createUnnamedInstance();
			}
			return new ProblemBinding(this, IProblemBinding.SEMANTIC_INVALID_TYPE, this.getRawSignature().toCharArray());
		}

		IType origType= operand.getExpressionType();
		IType type = SemanticUtil.getUltimateTypeUptoPointers(origType);
		IType operator = findOperatorReturnType();
		if (operator != null) {
			return operator;
		}
		
		switch (op) {
		case op_not:
			return new CPPBasicType(Kind.eBoolean, 0);
		case op_minus:
		case op_plus:
		case op_tilde:
			IType t= CPPArithmeticConversion.promoteCppType(type);
			if (t != null) {
				return t;
			}
			break;
		}

		if (origType instanceof CPPBasicType) {
			((CPPBasicType) origType).setFromExpression(this);
		}
		return origType;
    }

	public boolean isLValue() {
		ICPPFunction op = getOverload();
		if (op != null) {
			try {
				return CPPVisitor.isLValueReference(op.getType().getReturnType());
			} catch (DOMException e) {
			}
		}

		switch (getOperator()) {
		case op_bracketedPrimary:
			return getOperand().isLValue();
		case op_star:
		case op_prefixDecr:
		case op_prefixIncr:
			return true;
		default:
			return false;
		}
	}

    private IType findOperatorReturnType() {
    	ICPPFunction operatorFunction = getOverload();
    	if (operatorFunction != null) {
    		try {
				return operatorFunction.getType().getReturnType();
			} catch (DOMException e) {
				return e.getProblem();
			}
    	}
    	return null;
    }
}
