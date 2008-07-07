/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.ASTAmbiguousNode.NameCollector;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.core.runtime.Assert;

/**
 * Handles the ambiguity between cast and function-call expressions: (type)(expr) versus (function)(expr);
 * It also handles the impact on the grouping of the sub-expressions.
 */
public abstract class ASTAmbiguousCastVsFunctionCallExpression extends ASTNode implements IASTAmbiguousExpression {

    private final IASTCastExpression fCastExpression;
    private final IASTFunctionCallExpression fFunctionCallExpression;
    
    /**
     * The operand of the cast expression must start with an expression in parenthesis (which could be read as the parameter
     * list of the function call).
     * The function-call must contain the name expression (corresponding to the type-id of the cast expression). The parameter
     * expression may be <code>null</code>, it will be computed from the cast expression.
     */
    public ASTAmbiguousCastVsFunctionCallExpression(IASTCastExpression castExpression, IASTFunctionCallExpression functionCall) {
    	fCastExpression= castExpression;
    	fFunctionCallExpression= functionCall;
    }
    
    public void addExpression(IASTExpression e) {
		Assert.isLegal(false);
    }
    
    public IType getExpressionType() {
    	return CPPVisitor.getExpressionType(getExpressions()[0]);
    }

	public IASTExpression[] getExpressions() {
		return new IASTExpression[] {fCastExpression, fFunctionCallExpression};
	}

	@Override
	public boolean accept(ASTVisitor visitor) {
		final IASTAmbiguityParent owner= (IASTAmbiguityParent) getParent();
		IASTNode nodeToReplace= this;

		// try cast-expression
		owner.replace(nodeToReplace, fCastExpression);
		nodeToReplace= fCastExpression;
		// resolve nested ambiguities
		fCastExpression.accept(visitor);

		// if the operand of the cast-expr is not suitable for a function call, we are done.
		final IASTUnaryExpression primaryWithParenthesis= findPrimaryExpressionInParenthesis(fCastExpression.getOperand());
		if (primaryWithParenthesis == null)
			return true;

		
		// find nested names
		final NameCollector nameCollector= new NameCollector();
		fCastExpression.getTypeId().accept(nameCollector);
		final IASTName[] names= nameCollector.getNames();

		// resolve names 
		boolean hasIssue= false;
		for (IASTName name : names) {
			try {
				IBinding b = name.resolveBinding();
				if (b instanceof IProblemBinding) {
					hasIssue= true;
					break;
				}
			} catch (Exception t) {
				hasIssue= true;
				break;
			}
		}
		if (!hasIssue) 
			return true;

		fFunctionCallExpression.setParameterExpression(primaryWithParenthesis.getOperand());
		setRange(fFunctionCallExpression, fCastExpression, primaryWithParenthesis);
		
		IASTExpression result= fFunctionCallExpression;
		IASTExpression postFix= fCastExpression.getOperand();
		if (postFix != primaryWithParenthesis) {
			result= postFix;
			while (true) {
				setStart(postFix, fFunctionCallExpression);
				if (postFix instanceof IASTArraySubscriptExpression) {
					final IASTArraySubscriptExpression ase = (IASTArraySubscriptExpression) postFix;
					postFix= ase.getArrayExpression();
					if (postFix == primaryWithParenthesis) {
						ase.setArrayExpression(fFunctionCallExpression);
						break;
					}
				} else if (postFix instanceof IASTFunctionCallExpression) {
					final IASTFunctionCallExpression fc = (IASTFunctionCallExpression) postFix;
					postFix= fc.getFunctionNameExpression();
					if (postFix == primaryWithParenthesis) {
						fc.setFunctionNameExpression(fFunctionCallExpression);
						break;
					}
				} else if (postFix instanceof IASTFieldReference) {
					final IASTFieldReference fr = (IASTFieldReference) postFix;
					postFix= fr.getFieldOwner();
					if (postFix == primaryWithParenthesis) {
						fr.setFieldOwner(fFunctionCallExpression);
						break;
					}
				} else {
					final IASTUnaryExpression ue = (IASTUnaryExpression) postFix;
					postFix= ue.getOperand();
					if (postFix == primaryWithParenthesis) {
						ue.setOperand(fFunctionCallExpression);
						break;
					}
				}
			}
		}
		
		owner.replace(nodeToReplace, result);
		// resolve ambiguities in the function-call expression
		fFunctionCallExpression.getFunctionNameExpression().accept(visitor);
		return true;
	}

	private IASTUnaryExpression findPrimaryExpressionInParenthesis(IASTExpression operand) {
		while (true) {
			if (operand instanceof IASTUnaryExpression) {
				final IASTUnaryExpression unary= (IASTUnaryExpression) operand;
				switch (unary.getOperator()) {
				case IASTUnaryExpression.op_bracketedPrimary:
					return unary;
				case IASTUnaryExpression.op_postFixDecr:
				case IASTUnaryExpression.op_postFixIncr:
					operand= unary.getOperand();
					break;
				default:
					return null;
				}
			}
			else if (operand instanceof IASTArraySubscriptExpression) {
				operand= ((IASTArraySubscriptExpression) operand).getArrayExpression();
			} else if (operand instanceof IASTFunctionCallExpression) {
				operand= ((IASTFunctionCallExpression) operand).getFunctionNameExpression();
			} else if (operand instanceof IASTFieldReference) {
				operand= ((IASTFieldReference) operand).getFieldOwner();
			} else {
				return null;
			}
		}
	}

	private void setStart(IASTNode node, IASTNode start) {
		final ASTNode target= (ASTNode) node;
		final int offset = ((ASTNode) start).getOffset();
		target.setOffsetAndLength(offset, target.getOffset() + target.getLength() - offset);
	}

	private void setRange(IASTNode node, IASTNode from, IASTNode to) {
		final int offset = ((ASTNode) from).getOffset();
		final ASTNode t= (ASTNode) to;
		((ASTNode) node).setOffsetAndLength(offset, t.getOffset()+t.getLength()-offset);
	}
}
