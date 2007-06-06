/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c99;

import java.io.PrintStream;

import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * A utility that prints an AST to the console, useful for debugging purposes.
 * 
 * @author Mike Kucera
 */
public class ASTPrinter {
	
	/**
	 * Prints the AST to the given PrintStream.
	 */
	public static void printAST(IASTTranslationUnit root, PrintStream stream) {
		PrintStream out = stream == null ? System.out : stream;
		if(root == null) {
			out.println("null"); //$NON-NLS-1$
			return;
		}

		PrintVisitor visitor = new PrintVisitor(out);
		
		IASTPreprocessorStatement[] preStats = root.getAllPreprocessorStatements();
		if(preStats != null) {
			for(int i = 0; i < preStats.length; i++) {
				print(out, 0, preStats[i]);
			}
		}

		root.accept(visitor);
		
		IASTProblem[] problems = root.getPreprocessorProblems();
		if(problems != null) {
			for(int i = 0; i < problems.length; i++) {
				print(out, 0, problems[i]);
			}
		}
		
		IASTComment[] comments = root.getComments();
		if(comments != null) {
			for(int i = 0; i < comments.length; i++) {
				print(out, 0, comments[i]);
			}
		}
	}
	
	
	/**
	 * Prints the AST to stdout.
	 */
	public static void printAST(IASTTranslationUnit root) {
		printAST(root, null);
	}
	
	
	public static void printProblems(IASTTranslationUnit root, PrintStream stream) {
		PrintStream out = stream == null ? System.out : stream;
		if(root == null) {
			out.println("null");//$NON-NLS-1$
			return;
		}
		
		ProblemVisitor visitor = new ProblemVisitor(out);
		root.accept(visitor);
		
		IASTProblem[] problems = root.getPreprocessorProblems();
		if(problems != null) {
			for(int i = 0; i < problems.length; i++) {
				print(out, 0, problems[i]);
			}
		}
	}
	
	public static void printProblems(IASTTranslationUnit root) {
		printProblems(root, System.out);
	}
	
	
	private static void print(PrintStream out, int indentLevel, IASTNode n) {
		ASTNode node = (ASTNode) n;
		for(int i = 0; i < indentLevel; i++)
			out.print("  "); //$NON-NLS-1$
		
		String classname = node.getClass().getName();
		
		out.print(classname);
		out.print(" (" + node.getOffset() + "," + node.getLength() + ") "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if(node instanceof IASTName) {
			out.print(" " + ((IASTName)node).toString()); //$NON-NLS-1$
		}
		if(node instanceof IASTPointer) {
			IASTPointer pointer = (IASTPointer) node;
			if(pointer.isConst())
				out.print(" const"); //$NON-NLS-1$
			if(pointer.isVolatile())
				out.print(" volatile"); //$NON-NLS-1$
		}
		if(node instanceof ICASTArrayModifier) {
			if(((ICASTArrayModifier)node).isRestrict()) {
				out.print(" restrict"); //$NON-NLS-1$
			}
		}
		out.println();
	}

	
	private static class ProblemVisitor extends CASTVisitor {
		private PrintStream out;
		
		ProblemVisitor(PrintStream out) {
			this.out = out;
			shouldVisitProblems = true;
		}


		public int visit(IASTProblem problem) {
			print(out, 0, problem);
			return PROCESS_CONTINUE;
		}
	}
	
	
	private static class PrintVisitor extends CASTVisitor {

		
		private PrintStream out;
		private int indentLevel = 0;
		
		PrintVisitor(PrintStream out) {
			this.out = out;
			shouldVisitDesignators = true;
			shouldVisitNames = true;
			shouldVisitDeclarations = true;
			shouldVisitInitializers = true;
			shouldVisitParameterDeclarations = true;
			shouldVisitDeclarators = true;
			shouldVisitDeclSpecifiers = true;
			shouldVisitExpressions = true;
			shouldVisitStatements = true;
			shouldVisitTypeIds = true;
			shouldVisitEnumerators = true;
			shouldVisitTranslationUnit = true;
			shouldVisitProblems = true;
		}
		
		private void print(IASTNode node) {
			ASTPrinter.print(out, indentLevel,  node);
		}
		
		

		public int visit(IASTComment comment) {
			print(comment);
			indentLevel++;
			return super.visit(comment);
		}

		public int visit(ICASTDesignator designator) {
			print(designator);
			indentLevel++;
			return super.visit(designator);
		}
		
		public int visit(IASTDeclaration declaration) {
			print(declaration);
			indentLevel++;
			return super.visit(declaration);
		}

		public int visit(IASTDeclarator declarator) {
			print(declarator);
			indentLevel++;
			IASTPointerOperator[] pointers = declarator.getPointerOperators();
			for(int i = 0; i < pointers.length; i++) {
				print(pointers[i]);
			}
			if(declarator instanceof IASTArrayDeclarator) {
				IASTArrayDeclarator decl = (IASTArrayDeclarator)declarator;
				org.eclipse.cdt.core.dom.ast.IASTArrayModifier[] modifiers = decl.getArrayModifiers();
				for(int i = 0; i < modifiers.length; i++) {
					print((IASTNode)modifiers[i]);
				}
			}
			return super.visit(declarator);
		}

		public int visit(IASTDeclSpecifier declSpec) {
			print(declSpec);
			indentLevel++;
			return super.visit(declSpec);
		}

		public int visit(IASTEnumerator enumerator) {
			print(enumerator);
			indentLevel++;
			return super.visit(enumerator);
		}

		public int visit(IASTExpression expression) {
			print(expression);
			indentLevel++;
			return super.visit(expression);
		}

		public int visit(IASTInitializer initializer) {
			print(initializer);
			indentLevel++;
			return super.visit(initializer);
		}

		public int visit(IASTName name) {
			print(name);
			//print("resolved to:" + name.resolveBinding());
			indentLevel++;
			return super.visit(name);
		}

		public int visit(IASTParameterDeclaration parameterDeclaration) {
			print(parameterDeclaration);
			indentLevel++;
			return super.visit(parameterDeclaration);
		}

		public int visit(IASTProblem problem) {
			print(problem);
			indentLevel++;
			return super.visit(problem);
		}

		public int visit(IASTStatement statement) {
			print(statement);
			indentLevel++;
			return super.visit(statement);
		}

		public int visit(IASTTranslationUnit tu) {
			print(tu);
			indentLevel++;
			return super.visit(tu);
		}

		public int visit(IASTTypeId typeId) {
			print(typeId);
			indentLevel++;
			return super.visit(typeId);
		}

		public int leave(IASTComment comment) {
			indentLevel--;
			return super.leave(comment);
		}
		
		public int leave(ICASTDesignator designator) {
			indentLevel--;
			return super.leave(designator);
		}

		public int leave(IASTDeclaration declaration) {
			indentLevel--;
			return super.leave(declaration);
		}

		public int leave(IASTDeclarator declarator) {
			indentLevel--;
			return super.leave(declarator);
		}

		public int leave(IASTDeclSpecifier declSpec) {
			indentLevel--;
			return super.leave(declSpec);
		}

		public int leave(IASTEnumerator enumerator) {
			indentLevel--;
			return super.leave(enumerator);
		}

		public int leave(IASTExpression expression) {
			indentLevel--;
			return super.leave(expression);
		}

		public int leave(IASTInitializer initializer) {
			indentLevel--;
			return super.leave(initializer);
		}

		public int leave(IASTName name) {
			indentLevel--;
			return super.leave(name);
		}

		public int leave(IASTParameterDeclaration parameterDeclaration) {
			indentLevel--;
			return super.leave(parameterDeclaration);
		}

		public int leave(IASTProblem problem) {
			indentLevel--;
			return super.leave(problem);
		}

		public int leave(IASTStatement statement) {
			indentLevel--;
			return super.leave(statement);
		}

		public int leave(IASTTranslationUnit tu) {
			indentLevel--;
			return super.leave(tu);
		}

		public int leave(IASTTypeId typeId) {
			indentLevel--;
			return super.leave(typeId);
		}
		
	};
	
	
		
}
