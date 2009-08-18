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
package org.eclipse.cdt.internal.ui.refactoring;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.osgi.util.NLS;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTProblemExpression;
import org.eclipse.cdt.core.dom.ast.IASTProblemStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblemTypeId;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.refactoring.utils.SelectionHelper;

/**
 * The baseclass for all other refactorings, provides some common implementations for
 * condition checking, change generating, selection handling and translation unit loading.
 *
 */
public abstract class CRefactoring extends Refactoring {

	private static final int AST_STYLE = ITranslationUnit.AST_CONFIGURE_USING_SOURCE_CONTEXT | ITranslationUnit.AST_SKIP_INDEXED_HEADERS;

	protected String name = Messages.Refactoring_name; 
	protected IFile file;
	protected Region region;
	protected RefactoringStatus initStatus;
	protected IASTTranslationUnit unit;
	private IIndex fIndex;

	public CRefactoring(IFile file, ISelection selection, ICElement element) {
		if (element instanceof ISourceReference) {
			ISourceReference sourceRef= (ISourceReference) element;
			ITranslationUnit tu= sourceRef.getTranslationUnit();
			IResource res= tu.getResource();
			if (res instanceof IFile) 
				this.file= (IFile) res;
		
			try {
				final ISourceRange sourceRange = sourceRef.getSourceRange();
				this.region = new Region(sourceRange.getIdStartPos(), sourceRange.getIdLength());
			} catch (CModelException e) {
				CUIPlugin.log(e);
			}
		}
		else {
			this.file = file;
			this.region = SelectionHelper.getRegion(selection);
		}

		this.initStatus=new RefactoringStatus();
		if (this.file == null || region == null) {
			initStatus.addFatalError(Messages.Refactoring_SelectionNotValid);  
		}
	}

	private class ProblemFinder extends ASTVisitor{
		
		private boolean problemFound = false;
		private final RefactoringStatus status;
		
		public ProblemFinder(RefactoringStatus status){
			this.status = status;
		}
		
		{
			shouldVisitProblems = true;
			shouldVisitDeclarations = true;
			shouldVisitExpressions = true;
			shouldVisitStatements = true;
			shouldVisitTypeIds = true;
		}

		@Override
		public int visit(IASTProblem problem) {
			addWarningToState();
			return ASTVisitor.PROCESS_CONTINUE;
		}
		
		@Override
		public int visit(IASTDeclaration declaration) {
			if (declaration instanceof IASTProblemDeclaration) {
				addWarningToState();
			}
			return ASTVisitor.PROCESS_CONTINUE;
		}
		
		@Override
		public int visit(IASTExpression expression) {
			if (expression instanceof IASTProblemExpression) {
				addWarningToState();
			}
			return ASTVisitor.PROCESS_CONTINUE;
		}

		@Override
		public int visit(IASTStatement statement) {
			if (statement instanceof IASTProblemStatement) {
				addWarningToState();
			}
			return ASTVisitor.PROCESS_CONTINUE;
		}

		@Override
		public int visit(IASTTypeId typeId) {
			if (typeId instanceof IASTProblemTypeId) {
				addWarningToState();
			}
			return ASTVisitor.PROCESS_CONTINUE;
		}

		public boolean hasProblem() {
			return problemFound;
		}
		
		private void addWarningToState() {
			if(!problemFound){
				status.addWarning(Messages.Refactoring_CompileErrorInTU); 
				problemFound = true;
			}
		}
		
	}
	
	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		RefactoringStatus status = new RefactoringStatus();
		return status;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		SubMonitor sm = SubMonitor.convert(pm, 10);
		sm.subTask(Messages.Refactoring_PM_LoadTU); 
		if(isProgressMonitorCanceld(sm, initStatus)) {
			return initStatus;
		}
		if(!loadTranslationUnit(initStatus, sm.newChild(8))){
			initStatus.addError(Messages.Refactoring_CantLoadTU);  
			return initStatus;
		}
		if(isProgressMonitorCanceld(sm, initStatus)) {
			return initStatus;
		}
		sm.subTask(Messages.Refactoring_PM_CheckTU); 
		translationUnitHasProblem();
		if(translationUnitIsAmbiguous()) {
			initStatus.addError(Messages.Refactoring_Ambiguity); 
		}
		sm.worked(2);
		sm.subTask(Messages.Refactoring_PM_InitRef); 
		sm.done();
		return initStatus;
	}

	protected boolean isProgressMonitorCanceld(IProgressMonitor sm,
			RefactoringStatus initStatus2) {
		if(sm.isCanceled()) {
			initStatus2.addFatalError(Messages.Refactoring_CanceledByUser); 
			return true;
		}
		return false;
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		ModificationCollector collector = new ModificationCollector();
		collectModifications(pm, collector);
		return collector.createFinalChange();
	}
	
	abstract protected void collectModifications(IProgressMonitor pm, ModificationCollector collector)
		throws CoreException, OperationCanceledException;

	@Override
	public String getName() {
		return name;
	}

	protected boolean loadTranslationUnit(RefactoringStatus status,
			IProgressMonitor mon) {
		SubMonitor subMonitor = SubMonitor.convert(mon, 10);
		if (file != null) {
			try {
				subMonitor.subTask(Messages.Refactoring_PM_ParseTU);
				unit = loadTranslationUnit(file);
				if(unit == null) {
					subMonitor.done();
					return false;
				}
				subMonitor.worked(2);
				if(isProgressMonitorCanceld(subMonitor, initStatus)) {
					return true;
				}
				subMonitor.subTask(Messages.Refactoring_PM_MergeComments); 

				subMonitor.worked(8);
			} catch (CoreException e) {
				status.addFatalError(e.getMessage()); 
				subMonitor.done();
				return false;
			}

		} else {
			status.addFatalError(Messages.NO_FILE); 
			subMonitor.done();
			return false;
		}
		subMonitor.done();
		return true;
	}

	protected IASTTranslationUnit loadTranslationUnit(IFile file) throws CoreException {
		ITranslationUnit tu = (ITranslationUnit) CCorePlugin.getDefault().getCoreModel().create(file);
		if(tu == null){
			initStatus.addFatalError(NLS.bind(Messages.CRefactoring_FileNotFound, file.getName()));
			return null;
		}
		return tu.getAST(fIndex, AST_STYLE);
	}

	protected boolean translationUnitHasProblem() {
		ProblemFinder pf = new ProblemFinder(initStatus);
		unit.accept(pf);		
		return pf.hasProblem();
	}
	
	protected boolean translationUnitIsAmbiguous() {
		// ambiguities are resolved before the tu is passed to the refactoring.
		return false;
	}
	
	public void lockIndex() throws CoreException, InterruptedException {
		if (fIndex == null) {
			ICProject[] projects= CoreModel.getDefault().getCModel().getCProjects();
			fIndex= CCorePlugin.getIndexManager().getIndex(projects);
		}
		fIndex.acquireReadLock();
	}
	
	public void unlockIndex() {
		if (fIndex != null) {
			fIndex.releaseReadLock();
		}
		fIndex= null;
	}

	public IIndex getIndex() {
		return fIndex;
	}
	
	protected ArrayList<IASTName> findAllMarkedNames() {
		final ArrayList<IASTName> namesVector = new ArrayList<IASTName>();

		unit.accept(new CPPASTVisitor() {

			{
				shouldVisitNames = true;
			}

			@Override
			public int visit(IASTName name) {
				if (SelectionHelper.isInSameFileSelection(region, name, file)) {
					if (!(name instanceof ICPPASTQualifiedName)) {
						namesVector.add(name);
					}
				}
				return super.visit(name);
			}
		});
		return namesVector;
	}
}
