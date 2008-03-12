/*******************************************************************************
 * Copyright (c) 2007 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractconstant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.text.edits.TextEditGroup;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansionLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTInitializerExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNamespaceDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPMethod;

import org.eclipse.cdt.internal.ui.refactoring.AddDeclarationNodeToClassChange;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.MethodContext;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.NameNVisibilityInformation;
import org.eclipse.cdt.internal.ui.refactoring.utils.TranslationUnitHelper;

public class ExtractConstantRefactoring extends CRefactoring {
	
	private IASTLiteralExpression target = null;
	private final Vector<IASTExpression> literalsToReplace = new Vector<IASTExpression>();
	private final NameNVisibilityInformation info;
	
	public ExtractConstantRefactoring(IFile file, ISelection selection, NameNVisibilityInformation info){
		this(file, selection, info, false);
	}
	
	public ExtractConstantRefactoring(IFile file, ISelection selection, NameNVisibilityInformation info, boolean runHeadless){
		super(file,selection, runHeadless);
		this.info = info;
		name = Messages.ExtractConstantRefactoring_ExtractConst; 
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		SubMonitor sm = SubMonitor.convert(pm, 10);
		super.checkInitialConditions(sm.newChild(6));

		Collection<IASTLiteralExpression> literalExpressionVector = findAllLiterals();
		if(literalExpressionVector.isEmpty()){
			initStatus.addFatalError(Messages.ExtractConstantRefactoring_LiteralMustBeSelected); 
			return initStatus;
		}
		sm.worked(1);

		if(isProgressMonitorCanceld(sm, initStatus)) return initStatus;
		
		ITextSelection textSelection = null;
		if (selection instanceof ITextSelection) {
			textSelection = (ITextSelection) selection;
		} else {
			initStatus.addFatalError(Messages.ExtractConstantRefactoring_LiteralMustBeSelected); 
			return initStatus;
		}
		sm.worked(1);

		if(isProgressMonitorCanceld(sm, initStatus)) return initStatus;
		
		//Feststellen das nur einer Markiert ist.
		boolean oneMarked = isOneMarked(literalExpressionVector, textSelection);
		if(!oneMarked){ 
			//No or more than one marked
			if(target == null){
				//No Selection found;
				initStatus.addFatalError(Messages.ExtractConstantRefactoring_NoLiteralSelected); 
			} else {
				//To many selection found
				initStatus.addFatalError(Messages.ExtractConstantRefactoring_TooManyLiteralSelected); 
			}
			return initStatus;
		}
		sm.worked(1);

		if(isProgressMonitorCanceld(sm, initStatus)) return initStatus;
				
		// Alle Knoten zum ersetzen finden
		findAllNodesForReplacement(literalExpressionVector);
		
		info.addNamesToUsedNames(findAllDeclaredNames());
		sm.done();
		return initStatus;
	}

	private Vector<String> findAllDeclaredNames() {
		Vector<String>names = new Vector<String>();
		IASTFunctionDefinition funcDef = getFunctionDefinition();
		ICPPASTCompositeTypeSpecifier comTypeSpec = getCompositeTypeSpecifier(funcDef);
		if(comTypeSpec != null) {
			for(IASTDeclaration dec : comTypeSpec.getMembers()) {
				if (dec instanceof IASTSimpleDeclaration) {
					IASTSimpleDeclaration simpDec = (IASTSimpleDeclaration) dec;
					for(IASTDeclarator decor : simpDec.getDeclarators()) {
						names.add(decor.getName().getRawSignature());
					}
				}
			}
		}
		return names;
	}

	private ICPPASTCompositeTypeSpecifier getCompositeTypeSpecifier(IASTFunctionDefinition funcDef) {
		if(funcDef != null) {
			IBinding binding = funcDef.getDeclarator().getName().resolveBinding();
			if (binding instanceof CPPMethod) {
								
				CPPMethod methode = (CPPMethod) binding;
				IASTNode decl = methode.getDeclarations()[0];
								
				IASTNode spec = decl.getParent().getParent();
				if (spec instanceof ICPPASTCompositeTypeSpecifier) {
					ICPPASTCompositeTypeSpecifier compTypeSpec = (ICPPASTCompositeTypeSpecifier) spec;
					return compTypeSpec;
				}
			}
		}
		return null;
	}

	private IASTFunctionDefinition getFunctionDefinition() {
		IASTNode node = target;
		while((node = node.getParent()) != null){
			if (node instanceof IASTFunctionDefinition) {
				IASTFunctionDefinition funcDef = (IASTFunctionDefinition) node;
				return funcDef;
			}
		}
		return null;
	}

	private void findAllNodesForReplacement(Collection<IASTLiteralExpression> literalExpressionVector) {
		
		
		if (target.getParent() instanceof IASTUnaryExpression) {
			IASTUnaryExpression unary = (IASTUnaryExpression) target.getParent();
			for (IASTLiteralExpression expression : literalExpressionVector) {
				if( target.getKind() == expression.getKind()
						&& target.toString().equals( expression.toString() ) 
						&& expression.getParent() instanceof IASTUnaryExpression
						&& unary.getOperator() == ((IASTUnaryExpression)expression.getParent()).getOperator()) {
					literalsToReplace.add( ((IASTUnaryExpression)expression.getParent()) );
				}	
			}
		} else {
			for (IASTLiteralExpression expression : literalExpressionVector) {
				if( target.getKind() == expression.getKind()
						&& target.toString().equals( expression.toString() ) ) {
					literalsToReplace.add( expression );
				}	
			}
		}
	}

	private boolean isOneMarked(Collection<IASTLiteralExpression> literalExpressionVector, ITextSelection textSelection) {
		boolean oneMarked = false;
		for (IASTLiteralExpression expression : literalExpressionVector) {
			boolean isInSameFileSelection = isInSameFileSelection(textSelection, expression);
			if(isInSameFileSelection){
				if(target == null) {
					target = expression;
					oneMarked = true;
				} else {
					oneMarked = false;
				}
			}
		}
		return oneMarked;
	}

	private Collection<IASTLiteralExpression> findAllLiterals() {
		final Collection<IASTLiteralExpression> result = new ArrayList<IASTLiteralExpression>();		
		
		unit.accept(new CPPASTVisitor(){
			{
				shouldVisitExpressions = true;
			}
			@Override
			public int visit(IASTExpression expression) {
				if (expression instanceof IASTLiteralExpression) {
					if(!(expression.getNodeLocations().length == 1
							&& expression.getNodeLocations()[0] instanceof IASTMacroExpansionLocation)){
						IASTLiteralExpression literal = (IASTLiteralExpression) expression;
						result.add(literal);
					}
				}
				return super.visit(expression);
			}
			
		});
		
		return result;
	}

	@Override
	protected void collectModifications(IProgressMonitor pm, ModificationCollector collector)
		throws CoreException, OperationCanceledException {
		
		MethodContext context = findContext(target);
		Collection<IASTExpression> locLiteralsToReplace = new ArrayList<IASTExpression>();

		if(context.getType() == MethodContext.ContextType.METHOD){
			
			for (IASTExpression expression : literalsToReplace) {
				MethodContext exprContext = findContext(expression);
				if(exprContext.getType() == MethodContext.ContextType.METHOD){
					if( MethodContext.isSameClass(exprContext.getMethodQName(), context.getMethodQName())){
						locLiteralsToReplace.add(expression);
					}
				}
			}
			
		} else {
			
			for (IASTExpression expression : literalsToReplace) {
				IPath path = new Path(expression.getContainingFilename());
				IFile expressionFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
				//expressionFile may be null if the file is NOT in the workspace
				if( expressionFile != null && expressionFile.equals(file) ){
					locLiteralsToReplace.add(expression);
				}
			}
			
		}
				
		//Create all Changes for literals
		String constName = info.getName();
		createLiteralToConstantChanges(constName, locLiteralsToReplace, collector);
		
		if(context.getType() == MethodContext.ContextType.METHOD) {
			ICPPASTCompositeTypeSpecifier classDefinition = (ICPPASTCompositeTypeSpecifier) context.getMethodDeclaration().getParent();
			AddDeclarationNodeToClassChange.createChange(classDefinition, info.getVisibility(), getConstNodesClass(constName), true, collector);
		} else {
			IASTDeclaration nodes = getConstNodesGlobal(constName);
			ASTRewrite rewriter = collector.rewriterForTranslationUnit(unit);
			rewriter.insertBefore(unit, TranslationUnitHelper.getFirstNode(unit), nodes, new TextEditGroup(Messages.ExtractConstantRefactoring_CreateConstant));
		}
	}

	private void createLiteralToConstantChanges(String constName, Iterable<? extends IASTExpression> literals, ModificationCollector collector) {
		
		for (IASTExpression each : literals) {
			ASTRewrite rewrite = collector.rewriterForTranslationUnit(each.getTranslationUnit());
			CPPASTIdExpression idExpression = new CPPASTIdExpression(new CPPASTName(constName.toCharArray()));
			rewrite.replace(each, idExpression, new TextEditGroup(Messages.ExtractConstantRefactoring_ReplaceLiteral));			
		}
	}

	private IASTSimpleDeclaration getConstNodes(String newName){
		
		ICPPASTSimpleDeclSpecifier declSpec = new CPPASTSimpleDeclSpecifier();	
		declSpec.setConst(true);
		
		switch(target.getKind()){
		case IASTLiteralExpression.lk_char_constant:
			declSpec.setType(IASTSimpleDeclSpecifier.t_char);
			break;
		case IASTLiteralExpression.lk_float_constant:
			declSpec.setType(IASTSimpleDeclSpecifier.t_float);
			break;
		case IASTLiteralExpression.lk_integer_constant:
			declSpec.setType(IASTSimpleDeclSpecifier.t_int);
			break;
		case IASTLiteralExpression.lk_string_literal:
			declSpec.setType(ICPPASTSimpleDeclSpecifier.t_wchar_t);
			break;
		case ICPPASTLiteralExpression.lk_false: 
			//Like lk_true a boolean type
		case ICPPASTLiteralExpression.lk_true:
			declSpec.setType(ICPPASTSimpleDeclSpecifier.t_bool);
			break;
		case ICPPASTLiteralExpression.lk_this:
			break;
		}

		IASTSimpleDeclaration simple = new CPPASTSimpleDeclaration();
		simple.setDeclSpecifier(declSpec);
		
		IASTDeclarator decl = new CPPASTDeclarator();
		IASTName name = new CPPASTName(newName.toCharArray());
		decl.setName(name);
		IASTInitializerExpression init = new CPPASTInitializerExpression(); 
		if (target.getParent() instanceof IASTUnaryExpression) {
			IASTUnaryExpression unary = (IASTUnaryExpression) target.getParent();
			init.setExpression(unary);
		} else {
			CPPASTLiteralExpression expression = new CPPASTLiteralExpression(target.getKind(), target.toString());
			init.setExpression(expression);
		}
		decl.setInitializer(init);
		simple.addDeclarator(decl);
		
		return simple;
	}
	
	private IASTDeclaration getConstNodesGlobal(String newName){
		IASTSimpleDeclaration simple = getConstNodes(newName);

		if(unit.getParserLanguage().isCPP()){
			ICPPASTNamespaceDefinition namespace = new CPPASTNamespaceDefinition();
			namespace.setName(new CPPASTName());
			namespace.addDeclaration(simple);
			return namespace;
		}
		
		simple.getDeclSpecifier().setStorageClass(IASTDeclSpecifier.sc_static);
		return simple;
	}
	
	private IASTDeclaration getConstNodesClass(String newName){
		IASTSimpleDeclaration simple = getConstNodes(newName);
		simple.getDeclSpecifier().setStorageClass(IASTDeclSpecifier.sc_static);
		return simple;
	}
	
}
