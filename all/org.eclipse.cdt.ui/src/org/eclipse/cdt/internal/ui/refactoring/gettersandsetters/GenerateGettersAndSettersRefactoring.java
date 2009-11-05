/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.gettersandsetters;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.model.ICElement;

import org.eclipse.cdt.internal.ui.refactoring.AddDeclarationNodeToClassChange;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.Container;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.utils.SelectionHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.VisibilityEnum;

/**
 * @author Thomas Corbat
 * 
 */
public class GenerateGettersAndSettersRefactoring extends CRefactoring {

	private final class CompositeTypeSpecFinder extends CPPASTVisitor {
		private final int start;
		private final Container<IASTCompositeTypeSpecifier> container;
		{
			shouldVisitDeclSpecifiers = true;
		}

		private CompositeTypeSpecFinder(int start, Container<IASTCompositeTypeSpecifier> container) {
			this.start = start;
			this.container = container;
		}

		@Override
		public int visit(IASTDeclSpecifier declSpec) {
			
			if (declSpec instanceof IASTCompositeTypeSpecifier) {
				IASTFileLocation loc = declSpec.getFileLocation();
				if(start > loc.getNodeOffset() && start < loc.getNodeOffset()+ loc.getNodeLength()) {
					container.setObject((IASTCompositeTypeSpecifier) declSpec);
					return ASTVisitor.PROCESS_ABORT;
				}
			}
			
			return super.visit(declSpec);
		}
	}

	private static final String MEMBER_DECLARATION = "MEMBER_DECLARATION"; //$NON-NLS-1$
	private final GetterAndSetterContext context = new GetterAndSetterContext();	
	
	public GenerateGettersAndSettersRefactoring(IFile file, ISelection selection, ICElement element) {
		super(file, selection, element);
	}
	
	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		SubMonitor sm = SubMonitor.convert(pm, 10);

		RefactoringStatus status = super.checkInitialConditions(sm.newChild(6));
		if(status.hasError()) {
			return status;
		}

		if(!initStatus.hasFatalError()) {

			initRefactoring(pm);		

			if(context.existingFields.size() == 0) {
				initStatus.addFatalError(Messages.GenerateGettersAndSettersRefactoring_NoFields);
			}
		}		
		return initStatus;
	}

	private void initRefactoring(IProgressMonitor pm) {
		loadTranslationUnit(initStatus, pm);
		context.setUnit(unit);
		context.selectedName = getSelectedName();
		IASTCompositeTypeSpecifier compositeTypeSpecifier = null;
		if(context.selectedName != null) {
			compositeTypeSpecifier = getCompositeTypeSpecifier(context.selectedName);
		}else {
			compositeTypeSpecifier = findCurrentCompositeTypeSpecifier();
		}
		if(compositeTypeSpecifier != null) {
			findDeclarations(compositeTypeSpecifier);
		}else {
			initStatus.addFatalError(Messages.GenerateGettersAndSettersRefactoring_NoCassDefFound);
		}
	}
	
	private IASTCompositeTypeSpecifier findCurrentCompositeTypeSpecifier() {
		final int start = region.getOffset();
		Container<IASTCompositeTypeSpecifier> container = new Container<IASTCompositeTypeSpecifier>();
		unit.accept(new CompositeTypeSpecFinder(start, container));
		return container.getObject();
	}

	private IASTCompositeTypeSpecifier getCompositeTypeSpecifier(IASTName selectedName) {
		IASTNode node = selectedName;
		while(node != null && !(node instanceof IASTCompositeTypeSpecifier)) {
			node = node.getParent();
		}
		return (IASTCompositeTypeSpecifier) node;
	}

	private IASTName getSelectedName() {
		ArrayList<IASTName> names = findAllMarkedNames();
		if (names.size() < 1) {
			return null;
		}
		return names.get(names.size()-1);
	}

	protected void findDeclarations(IASTCompositeTypeSpecifier compositeTypeSpecifier) {

		compositeTypeSpecifier.accept(new CPPASTVisitor() {

			{
				shouldVisitDeclarations = true;
			}

			@Override
			public int visit(IASTDeclaration declaration) {
				if (declaration instanceof IASTSimpleDeclaration) {
					IASTSimpleDeclaration fieldDeclaration = (IASTSimpleDeclaration) declaration;
					ASTNodeProperty props = fieldDeclaration.getPropertyInParent();
					if (props.getName().contains(MEMBER_DECLARATION)) {
						final IASTDeclarator[] declarators = fieldDeclaration.getDeclarators();
						if (declarators.length > 0) {
							if ((declarators[0] instanceof IASTFunctionDeclarator)) {
								context.existingFunctionDeclarations.add(fieldDeclaration);
							} else {
								if(SelectionHelper.isInSameFile(fieldDeclaration, file)){
									context.existingFields.add(fieldDeclaration);
								}
							}
						}
					}
				}
				if (declaration instanceof IASTFunctionDefinition) {
					IASTFunctionDefinition functionDefinition = (IASTFunctionDefinition) declaration;
					ASTNodeProperty props = functionDefinition.getPropertyInParent();
					if (props.getName().contains(MEMBER_DECLARATION)) {
						context.existingFunctionDefinitions.add(functionDefinition);
					}
				}
				return super.visit(declaration);
			}
		});
	}

	@Override
	protected void collectModifications(IProgressMonitor pm,ModificationCollector collector) throws CoreException, OperationCanceledException {
		ArrayList<IASTNode> getterAndSetters = new ArrayList<IASTNode>();
		for(GetterSetterInsertEditProvider currentProvider : context.selectedFunctions){
			getterAndSetters.add(currentProvider.getFunction());
		}		
		ICPPASTCompositeTypeSpecifier classDefinition = (ICPPASTCompositeTypeSpecifier) context.existingFields.get(context.existingFields.size()-1).getParent();

		AddDeclarationNodeToClassChange.createChange(classDefinition, VisibilityEnum.v_public, getterAndSetters, false, collector);
	}

	public GetterAndSetterContext getContext() {
		return context;
	}
	
	public Region getRegion() {
		return region;
	}	
}
