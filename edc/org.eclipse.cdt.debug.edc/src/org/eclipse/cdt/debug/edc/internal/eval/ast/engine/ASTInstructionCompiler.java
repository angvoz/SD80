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
package org.eclipse.cdt.debug.edc.internal.eval.ast.engine;

import java.util.Stack;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCastExpression;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.IEDCTraceOptions;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.ArraySubscript;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.CompoundInstruction;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.EvaluateID;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.FieldReference;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.Instruction;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.InstructionSequence;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.NoOp;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.OperatorAddrOf;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.OperatorBinaryAnd;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.OperatorBinaryOr;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.OperatorBinaryXor;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.OperatorBitwiseNot;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.OperatorCast;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.OperatorCastValue;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.OperatorDivide;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.OperatorEquals;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.OperatorGreaterEqual;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.OperatorGreaterThan;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.OperatorIndirection;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.OperatorLessEqual;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.OperatorLessThan;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.OperatorLogicalAnd;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.OperatorLogicalNot;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.OperatorLogicalOr;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.OperatorMinus;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.OperatorModulo;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.OperatorMultiply;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.OperatorNotEquals;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.OperatorPlus;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.OperatorShiftLeft;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.OperatorShiftRight;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.OperatorUnaryMinus;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.OperatorUnaryPlus;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.PushBoolean;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.PushChar;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.PushDouble;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.PushFloat;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.PushLongOrBigInteger;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.PushString;
import org.eclipse.cdt.debug.edc.symbols.TypeEngine;
import org.eclipse.cdt.internal.core.dom.parser.ASTAmbiguousNode;

@SuppressWarnings("restriction")
public class ASTInstructionCompiler extends ASTVisitor {

	private final Stack<Instruction> stack;
	private final InstructionSequence instructions;
	private int counter;
	private String errorMessage;
	private boolean active = true;

	public ASTInstructionCompiler( String snippet) {
		super(true);
		stack = new Stack<Instruction>();
		instructions = new InstructionSequence(snippet);
	}

	private void push(Instruction i) {
		stack.push(i);
	}

	private Instruction pop() {
		return stack.pop();
	}

	/**
	 * Returns the instruction sequence generated by this AST instruction
	 * compiler
	 */
	public InstructionSequence getInstructions() {
		return instructions;
	}

	public boolean hasErrors() {
		return errorMessage != null;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	private boolean isActive() {
		return active;
	}

	private void storeInstruction() {
		Instruction instruction = pop();
		counter++;
		if (instruction instanceof CompoundInstruction) {
			((CompoundInstruction) instruction).setEnd(counter);
		}
		instructions.add(instruction);
	}

	@Override
	public int leave(IASTArrayModifier arrayModifier) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.EXPRESSION_PARSE_TRACE, arrayModifier);
		return super.leave(arrayModifier);
	}

	@SuppressWarnings("deprecation")	// we're simply wrapping a deprecated method
	@Override
	public int leave(IASTComment comment) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.EXPRESSION_PARSE_TRACE, comment);
		return super.leave(comment);
	}

	@Override
	public int leave(IASTDeclaration declaration) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.EXPRESSION_PARSE_TRACE, declaration);
		return super.leave(declaration);
	}

	@Override
	public int leave(IASTDeclarator declarator) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.EXPRESSION_PARSE_TRACE, declarator);
		return super.leave(declarator);
	}

	@Override
	public int leave(IASTDeclSpecifier declSpec) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.EXPRESSION_PARSE_TRACE, declSpec);
		return super.leave(declSpec);
	}

	@Override
	public int leave(IASTEnumerator enumerator) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.EXPRESSION_PARSE_TRACE, enumerator);
		return super.leave(enumerator);
	}

	@Override
	public int leave(IASTExpression expression) {
		if (!isActive() || hasErrors())
			return PROCESS_CONTINUE;
		storeInstruction();
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.EXPRESSION_PARSE_TRACE, expression);
		return super.leave(expression);
	}

	@Override
	public int leave(IASTInitializer initializer) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.EXPRESSION_PARSE_TRACE, initializer);
		return super.leave(initializer);
	}

	@Override
	public int leave(IASTName name) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.EXPRESSION_PARSE_TRACE, name);
		return super.leave(name);
	}

	@Override
	public int leave(IASTParameterDeclaration parameterDeclaration) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.EXPRESSION_PARSE_TRACE, parameterDeclaration);
		return super.leave(parameterDeclaration);
	}

	@Override
	public int leave(IASTPointerOperator ptrOperator) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.EXPRESSION_PARSE_TRACE, ptrOperator);
		return super.leave(ptrOperator);
	}

	@Override
	public int leave(IASTProblem problem) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.EXPRESSION_PARSE_TRACE, problem);
		return super.leave(problem);
	}

	@Override
	public int leave(IASTStatement statement) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.EXPRESSION_PARSE_TRACE, statement);
		return super.leave(statement);
	}

	@Override
	public int leave(IASTTranslationUnit tu) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.EXPRESSION_PARSE_TRACE, tu);
		return super.leave(tu);
	}

	@Override
	public int leave(IASTTypeId typeId) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.EXPRESSION_PARSE_TRACE, typeId);
		return super.leave(typeId);
	}

	@Override
	public int visit(IASTArrayModifier arrayModifier) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.EXPRESSION_PARSE_TRACE, arrayModifier);
		return super.visit(arrayModifier);
	}

	@SuppressWarnings("deprecation")	// we're simply wrapping a deprecated method
	@Override
	public int visit(IASTComment comment) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.EXPRESSION_PARSE_TRACE, comment);
		return super.visit(comment);
	}

	@Override
	public int visit(IASTDeclaration declaration) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.EXPRESSION_PARSE_TRACE, declaration);
		return super.visit(declaration);
	}

	@Override
	public int visit(IASTDeclarator declarator) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.EXPRESSION_PARSE_TRACE, declarator);
		return super.visit(declarator);
	}

	@Override
	public int visit(IASTDeclSpecifier declSpec) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.EXPRESSION_PARSE_TRACE, declSpec);
		return super.visit(declSpec);
	}

	@Override
	public int visit(IASTEnumerator enumerator) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.EXPRESSION_PARSE_TRACE, enumerator);
		return super.visit(enumerator);
	}

	@Override
	public int visit(IASTInitializer initializer) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.EXPRESSION_PARSE_TRACE, initializer);
		return super.visit(initializer);
	}

	@Override
	public int visit(IASTParameterDeclaration parameterDeclaration) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.EXPRESSION_PARSE_TRACE,
				"visit: " + parameterDeclaration.getClass().getSimpleName()); //$NON-NLS-1$
		return super.visit(parameterDeclaration);
	}

	@Override
	public int visit(IASTPointerOperator ptrOperator) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.EXPRESSION_PARSE_TRACE, ptrOperator);
		return super.visit(ptrOperator);
	}

	@Override
	public int visit(IASTStatement statement) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.EXPRESSION_PARSE_TRACE, statement);
		return super.visit(statement);
	}

	@Override
	public int visit(IASTTranslationUnit tu) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.EXPRESSION_PARSE_TRACE, tu);
		return super.visit(tu);
	}

	@Override
	public int visit(IASTTypeId typeId) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.EXPRESSION_PARSE_TRACE, typeId);
		return super.visit(typeId);
	}

	@Override
	public int visit(ASTAmbiguousNode astAmbiguousNode) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.EXPRESSION_PARSE_TRACE, astAmbiguousNode);
		return super.visit(astAmbiguousNode);
	}

	@Override
	public int visit(IASTName name) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.EXPRESSION_PARSE_TRACE, name);
		return super.visit(name);
	}

	@Override
	public int visit(IASTProblem problem) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.EXPRESSION_PARSE_TRACE, problem);
		return super.visit(problem);
	}

	@Override
	public int visit(IASTExpression expression) {
		EDCDebugger.getDefault().getTrace().traceEntry(IEDCTraceOptions.EXPRESSION_PARSE_TRACE, expression);

		if (expression instanceof IASTLiteralExpression) {
			visitLiteralExpression((IASTLiteralExpression) expression);
		} else if (expression instanceof IASTBinaryExpression) {
			visitBinaryExpression((IASTBinaryExpression) expression);
		} else if (expression instanceof IASTUnaryExpression) {
			visitUnaryExpression((IASTUnaryExpression) expression);
		} else if (expression instanceof IASTIdExpression) {
			visitIDExpression((IASTIdExpression) expression);
		} else if (expression instanceof IASTArraySubscriptExpression) {
			visitArraySubscriptExpression((IASTArraySubscriptExpression) expression);
		} else if (expression instanceof IASTFieldReference) {
			visitFieldReference((IASTFieldReference) expression);
		} else if (expression instanceof IASTCastExpression) {
			visitCastExpression((IASTCastExpression) expression);
		} else
			push(new NoOp(counter));

		return super.visit(expression);
	}

	private void visitIDExpression(IASTIdExpression expression) {
		push(new EvaluateID(expression));
	}

	private void visitArraySubscriptExpression(IASTArraySubscriptExpression expression) {
		push(new ArraySubscript(counter));
	}

	private void visitFieldReference(IASTFieldReference expression) {
		push(new FieldReference(expression, counter));
	}

	private void visitCastExpression(IASTCastExpression expression) {
		if (expression.getOperator() == ICPPASTCastExpression.op_reinterpret_cast) 
			push(new OperatorCastValue(counter, expression));
		else
			push(new OperatorCast(counter, expression));
	}

	private void visitLiteralExpression(IASTLiteralExpression expression) {
		int kind = expression.getKind();
		String value = new String(expression.getValue());
		switch (kind) {
		case IASTLiteralExpression.lk_integer_constant:
			try {
				push(new PushLongOrBigInteger(value));
			} catch (NumberFormatException nfe) {
				errorMessage = ASTEvalMessages.ASTInstructionCompiler_InvalidNumber;
			}
			break;
		case IASTLiteralExpression.lk_float_constant:
			// check for explicitly float constant
			try {
				if (value.toUpperCase().endsWith("F")) //$NON-NLS-1$
					push(new PushFloat(value));
				else
					push(new PushDouble(value));
			} catch (NumberFormatException nfe) {
				errorMessage = ASTEvalMessages.ASTInstructionCompiler_InvalidNumber;
			}
			break;
		case IASTLiteralExpression.lk_char_constant:
			try {
				push(new PushChar(value));
			} catch (NumberFormatException nfe) {
				errorMessage = ASTEvalMessages.ASTInstructionCompiler_InvalidNumber;
			}
			break;
		case IASTLiteralExpression.lk_string_literal:
			push(new PushString(value));
			break;
		case IASTLiteralExpression.lk_false:
			push(new PushBoolean(false));
			break;
		case IASTLiteralExpression.lk_true:
			push(new PushBoolean(true));
			break;
		case IASTLiteralExpression.lk_this:
			push(new EvaluateID("this")); //$NON-NLS-1$
			break;
		default:
			push(new NoOp(counter));
		}
	}

	private void visitBinaryExpression(IASTBinaryExpression expression) {
		int op = expression.getOperator();

		switch (op) {
		case IASTBinaryExpression.op_binaryAnd:
			push(new OperatorBinaryAnd(counter));
			break;

		case IASTBinaryExpression.op_binaryOr:
			push(new OperatorBinaryOr(counter));
			break;

		case IASTBinaryExpression.op_binaryXor:
			push(new OperatorBinaryXor(counter));
			break;

		case IASTBinaryExpression.op_plus:
			push(new OperatorPlus(counter));
			break;

		case IASTBinaryExpression.op_minus:
			push(new OperatorMinus(counter));
			break;

		case IASTBinaryExpression.op_multiply:
			push(new OperatorMultiply(counter));
			break;

		case IASTBinaryExpression.op_divide:
			push(new OperatorDivide(counter));
			break;

		case IASTBinaryExpression.op_modulo:
			push(new OperatorModulo(counter));
			break;

		case IASTBinaryExpression.op_shiftLeft:
			push(new OperatorShiftLeft(counter));
			break;

		case IASTBinaryExpression.op_shiftRight:
			push(new OperatorShiftRight(counter));
			break;

		case IASTBinaryExpression.op_equals:
			push(new OperatorEquals(counter));
			break;

		case IASTBinaryExpression.op_notequals:
			push(new OperatorNotEquals(counter));
			break;

		case IASTBinaryExpression.op_greaterEqual:
			push(new OperatorGreaterEqual(counter));
			break;

		case IASTBinaryExpression.op_greaterThan:
			push(new OperatorGreaterThan(counter));
			break;

		case IASTBinaryExpression.op_lessEqual:
			push(new OperatorLessEqual(counter));
			break;

		case IASTBinaryExpression.op_lessThan:
			push(new OperatorLessThan(counter));
			break;

		case IASTBinaryExpression.op_logicalAnd:
			push(new OperatorLogicalAnd(counter));
			break;

		case IASTBinaryExpression.op_logicalOr:
			push(new OperatorLogicalOr(counter));
			break;

		default:
			push(new NoOp(counter));
		}
	}

	private void visitUnaryExpression(IASTUnaryExpression expression) {
		int op = expression.getOperator();

		switch (op) {
		case IASTUnaryExpression.op_minus:
			push(new OperatorUnaryMinus(counter));
			break;

		case IASTUnaryExpression.op_not:
			push(new OperatorLogicalNot(counter));
			break;

		case IASTUnaryExpression.op_plus:
			push(new OperatorUnaryPlus(counter));
			break;

		case IASTUnaryExpression.op_tilde:
			push(new OperatorBitwiseNot(counter));
			break;

		case IASTUnaryExpression.op_star:
			push(new OperatorIndirection(counter));
			break;

		case IASTUnaryExpression.op_amper:
			push(new OperatorAddrOf(counter));
			break;

		default:
			push(new NoOp(counter));
		}
	}

	/**
	 * Fixup the instruction stream:
	 * 
	 * (1) Remove NoOps
	 * (2) Reduce (possibly internally generated) cast expressions to avoid 
	 * taking the address of a register or bitfield.
	 * @param typeEngine 
	 * 
	 */
	public void fixupInstructions(TypeEngine typeEngine) {
		instructions.removeNoOps();
		instructions.reduceCasts(typeEngine);
	}
}
