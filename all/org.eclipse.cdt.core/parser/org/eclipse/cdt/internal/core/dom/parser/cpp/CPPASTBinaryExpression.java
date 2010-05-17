/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Mike Kucera (IBM)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;


public class CPPASTBinaryExpression extends ASTNode implements ICPPASTBinaryExpression, IASTAmbiguityParent {
	private int op;
    private IASTExpression operand1;
    private IASTInitializerClause operand2;
    private IType type;
    private ICPPFunction overload= UNINITIALIZED_FUNCTION;
    private IASTImplicitName[] implicitNames = null;

    public CPPASTBinaryExpression() {
	}

	public CPPASTBinaryExpression(int op, IASTExpression operand1, IASTInitializerClause operand2) {
		this.op = op;
		setOperand1(operand1);
		setInitOperand2(operand2);
	}

	public CPPASTBinaryExpression copy() {
		CPPASTBinaryExpression copy = new CPPASTBinaryExpression();
		copy.op = op;
		copy.setOperand1(operand1 == null ? null : operand1.copy());
		copy.setInitOperand2(operand2 == null ? null : operand2.copy());
		copy.setOffsetAndLength(this);
		return copy;
	}
	
	public int getOperator() {
        return op;
    }

    public IASTExpression getOperand1() {
        return operand1;
    }

    public IASTInitializerClause getInitOperand2() {
    	return operand2;
    }

    public IASTExpression getOperand2() {
    	if (operand2 instanceof IASTExpression)
    		return (IASTExpression) operand2;
    	return null;
    }

    public void setOperator(int op) {
        assertNotFrozen();
        this.op = op;
    }

    public void setOperand1(IASTExpression expression) {
        assertNotFrozen();
        operand1 = expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(OPERAND_ONE);
		}
    }

    public void setInitOperand2(IASTInitializerClause operand) {
        assertNotFrozen();
        operand2 = operand;
        if (operand != null) {
        	operand.setParent(this);
        	operand.setPropertyInParent(OPERAND_TWO);
		}
    }

    public void setOperand2(IASTExpression expression) {
    	setInitOperand2(expression);
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
				operatorName.setBinding(overload);
				operatorName.setOperator(true);
				operatorName.computeOperatorOffsets(operand1, true);
				implicitNames = new IASTImplicitName[] { operatorName };
			}
		}

		return implicitNames;
	}

    @Override
	public boolean accept(ASTVisitor action) {
    	if (operand1 instanceof IASTBinaryExpression || operand2 instanceof IASTBinaryExpression) {
    		return acceptWithoutRecursion(this, action);
    	}
    	
		if (action.shouldVisitExpressions) {
			switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP:  return true;
	            default: break;
	        }
		}

		if (operand1 != null && !operand1.accept(action))
			return false;

		if (action.shouldVisitImplicitNames) {
			for (IASTImplicitName name : getImplicitNames()) {
				if (!name.accept(action))
					return false;
			}
		}

		if (operand2 != null && !operand2.accept(action))
			return false;

		if (action.shouldVisitExpressions && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;

		return true;
    }

    private static class N {
		final IASTBinaryExpression fExpression;
		int fState;
		N fNext;

		N(IASTBinaryExpression expr) {
			fExpression = expr;
		}
	}

	public static boolean acceptWithoutRecursion(IASTBinaryExpression bexpr, ASTVisitor action) {
		N stack= new N(bexpr);
		while (stack != null) {
			IASTBinaryExpression expr= stack.fExpression;
			if (stack.fState == 0) {
				if (action.shouldVisitExpressions) {
					switch (action.visit(expr)) {
					case ASTVisitor.PROCESS_ABORT :
						return false;
					case ASTVisitor.PROCESS_SKIP:
						stack= stack.fNext;
						continue;
					}
				}
				stack.fState= 1;
				IASTExpression op1 = expr.getOperand1();
				if (op1 instanceof IASTBinaryExpression) {
					N n= new N((IASTBinaryExpression) op1);
					n.fNext= stack;
					stack= n;
					continue;
				}
				if (op1 != null && !op1.accept(action))
					return false;
			}
			if (stack.fState == 1) {
				if (action.shouldVisitImplicitNames) {
					for (IASTImplicitName name : ((IASTImplicitNameOwner) expr).getImplicitNames()) {
		        		if (!name.accept(action))
		        			return false;
		        	}
		        }
				stack.fState= 2;

				IASTExpression op2 = expr.getOperand2();
				if (op2 instanceof IASTBinaryExpression) {
					N n= new N((IASTBinaryExpression) op2);
					n.fNext= stack;
					stack= n;
					continue;
				}
				if (op2 != null && !op2.accept(action))
					return false;
			}
			
			if (action.shouldVisitExpressions && action.leave(expr) == ASTVisitor.PROCESS_ABORT)
				return false;
		
			stack= stack.fNext;
		}
		
		return true;
	}

	public void replace(IASTNode child, IASTNode other) {
		if (child == operand1) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			operand1 = (IASTExpression) other;
		}
		if (child == operand2) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			operand2 = (IASTInitializerClause) other;
		}
	}

    public IType getExpressionType() {
    	if (type == null) {
    		type= createExpressionType();
    	}
    	return type;
    }

    public ICPPFunction getOverload() {
    	if (overload != UNINITIALIZED_FUNCTION)
    		return overload;
    	
    	return overload = CPPSemantics.findOverloadedOperator(this);
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
		case op_assign:
		case op_binaryAndAssign:
		case op_binaryOrAssign:
		case op_binaryXorAssign:
		case op_divideAssign:
		case op_minusAssign:
		case op_moduloAssign:
		case op_multiplyAssign:
		case op_plusAssign:
		case op_shiftLeftAssign:
		case op_shiftRightAssign:
			return true;
		}
		return false;
	}

	private IType createExpressionType() {
		// Check for overloaded operator.
		ICPPFunction o= getOverload();
		if (o != null) {
			try {
				return o.getType().getReturnType();
			} catch (DOMException e) {
				e.getProblem();
			}
		}
		
        final int op = getOperator();
		IType type1 = SemanticUtil.getUltimateTypeUptoPointers(getOperand1().getExpressionType());
		if (type1 instanceof IProblemBinding) {
			return type1;
		}
		
		IType type2 = SemanticUtil.getUltimateTypeUptoPointers(getOperand2().getExpressionType());
		if (type2 instanceof IProblemBinding) {
			return type2;
		}
		
    	IType type= CPPArithmeticConversion.convertCppOperandTypes(op, type1, type2);
    	if (type != null) {
    		return type;
    	}

        switch (op) {
        case IASTBinaryExpression.op_lessEqual:
        case IASTBinaryExpression.op_lessThan:
        case IASTBinaryExpression.op_greaterEqual:
        case IASTBinaryExpression.op_greaterThan:
        case IASTBinaryExpression.op_logicalAnd:
        case IASTBinaryExpression.op_logicalOr:
        case IASTBinaryExpression.op_equals:
        case IASTBinaryExpression.op_notequals:
        	return new CPPBasicType(Kind.eBoolean, 0, this);

        case IASTBinaryExpression.op_plus:
        	if (type1 instanceof IArrayType) {
        		return arrayTypeToPointerType((IArrayType) type1);
        	} else if (type2 instanceof IPointerType) {
        		return type2;
        	} else if (type2 instanceof IArrayType) {
        		return arrayTypeToPointerType((IArrayType) type2);
        	}
        	break;

        case IASTBinaryExpression.op_minus:
        	if (type2 instanceof IPointerType || type2 instanceof IArrayType) {
        		if (type1 instanceof IPointerType || type1 instanceof IArrayType) {
        			return CPPVisitor.getPointerDiffType(this);
        		}
        		return type1;
        	}
        	break;

        case ICPPASTBinaryExpression.op_pmarrow:
        case ICPPASTBinaryExpression.op_pmdot:
        	if (type2 instanceof ICPPPointerToMemberType) {
        		return ((ICPPPointerToMemberType) type2).getType();
        	}
        	return new ProblemBinding(this, IProblemBinding.SEMANTIC_INVALID_TYPE, getRawSignature().toCharArray());
        }
		return type1;
	}

	private IType arrayTypeToPointerType(IArrayType type) {
		return new CPPPointerType(type.getType());
	}
}
