/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems) - Adapted for CDT
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTImageLocation;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionTryBlockDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.model.ASTCache;

import org.eclipse.cdt.internal.ui.editor.SemanticHighlightingManager.HighlightedPosition;
import org.eclipse.cdt.internal.ui.editor.SemanticHighlightingManager.HighlightingStyle;
import org.eclipse.cdt.internal.ui.text.ICReconcilingListener;


/**
 * Semantic highlighting reconciler - Background thread implementation.
 * Cloned from JDT.
 * 
 * @since 4.0
 */
public class SemanticHighlightingReconciler implements ICReconcilingListener {

	/**
	 * AST visitor to test whether a node is a leaf node.
	 */
	public static final class LeafNodeTester extends CPPASTVisitor {
		{
			shouldVisitNames= true;
			shouldVisitDeclarations= true;
			shouldVisitInitializers= true;
			shouldVisitParameterDeclarations= true;
			shouldVisitDeclarators= true;
			shouldVisitDeclSpecifiers= true;
			shouldVisitExpressions= true;
			shouldVisitStatements= true;
			shouldVisitTypeIds= true;
			shouldVisitEnumerators= true;
			shouldVisitTranslationUnit= false;
			shouldVisitProblems= true;
			shouldVisitComments= false;
			shouldVisitBaseSpecifiers= true;
			shouldVisitNamespaces= true;
			shouldVisitTemplateParameters= true;
		}
		private int fVisits;

		private int processNode(IASTNode node) {
			if (++fVisits > 1) 
				return PROCESS_ABORT;
			return PROCESS_CONTINUE;
		}
		public int visit(ICPPASTBaseSpecifier specifier) {
			return processNode(specifier);
		}
		public int visit(ICPPASTNamespaceDefinition namespace) {
			return processNode(namespace);
		}
		public int visit(ICPPASTTemplateParameter parameter) {
			return processNode(parameter);
		}
		public int visit(IASTDeclaration declaration) {
			return processNode(declaration);
		}
		public int visit(IASTDeclarator declarator) {
			return processNode(declarator);
		}
		public int visit(IASTDeclSpecifier declSpec) {
			return processNode(declSpec);
		}
		public int visit(IASTEnumerator enumerator) {
			return processNode(enumerator);
		}
		public int visit(IASTExpression expression) {
			return processNode(expression);
		}
		public int visit(IASTInitializer initializer) {
			return processNode(initializer);
		}
		public int visit(IASTName name) {
			return processNode(name);
		}
		public int visit(IASTParameterDeclaration parameterDeclaration) {
			return processNode(parameterDeclaration);
		}
		public int visit(IASTProblem problem) {
			return processNode(problem);
		}
		public int visit(IASTStatement statement) {
			return processNode(statement);
		}
		public int visit(IASTTranslationUnit tu) {
			return processNode(tu);
		}
		public int visit(IASTTypeId typeId) {
			return processNode(typeId);
		}
		public boolean isLeafNode(IASTNode node) {
			fVisits= 0;
			node.accept(this);
			return fVisits <= 1;
		}
	}

	/**
	 * Collects positions from the AST.
	 */
	private class PositionCollector extends CPPASTVisitor {

		{
			shouldVisitTranslationUnit= true;
			shouldVisitNames= true;
			shouldVisitDeclarations= true;
			shouldVisitExpressions= true;
			shouldVisitStatements= true;
			shouldVisitDeclSpecifiers= true;
			shouldVisitDeclarators= true;
			shouldVisitNamespaces= true;
		}
		private boolean shouldVisitCatchHandlers= true;
		
		/** The semantic token */
		private SemanticToken fToken= new SemanticToken();
		private String fFilePath;
		private int fMinLocation;
		private final LeafNodeTester fgLeafNodeTester= new LeafNodeTester();
		
		/**
		 * @param filePath
		 */
		public PositionCollector(String filePath) {
			fFilePath= filePath;
			fMinLocation= -1;
		}

		/*
		 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTTranslationUnit)
		 */
		public int visit(IASTTranslationUnit tu) {
			// visit macro definitions
			IASTPreprocessorMacroDefinition[] macroDefs= tu.getMacroDefinitions();
			for (int i= 0; i < macroDefs.length; i++) {
				IASTPreprocessorMacroDefinition macroDef= macroDefs[i];
				if (fFilePath.equals(macroDef.getContainingFilename())) {
					visitNode(macroDef.getName());
				}
			}
			// TODO visit macro expansions
//			IASTName[] macroExps= tu.getMacroExpansions();
//			for (int i= 0; i < macroExps.length; i++) {
//				IASTName macroExp= macroExps[i];
//				if (fFilePath.equals(macroExp.getContainingFilename())) {
//					visitMacroExpansion(macroExp);
//				}
//			}
			return super.visit(tu);
		}

		/*
		 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTDeclaration)
		 */
		public int visit(IASTDeclaration declaration) {
			if (!fFilePath.equals(declaration.getContainingFilename())) {
				return PROCESS_SKIP;
			}
			if (checkForMacro(declaration)) {
				return PROCESS_SKIP;
			}
			return PROCESS_CONTINUE;
		}

		/*
		 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#leave(org.eclipse.cdt.core.dom.ast.IASTDeclaration)
		 */
		public int leave(IASTDeclaration declaration) {
			if (!shouldVisitCatchHandlers && declaration instanceof IASTFunctionDefinition) {
				shouldVisitCatchHandlers= true;
				IASTFunctionDefinition functionDef= (IASTFunctionDefinition) declaration;
				ICPPASTFunctionTryBlockDeclarator declarator= (ICPPASTFunctionTryBlockDeclarator) functionDef.getDeclarator();
				ICPPASTCatchHandler[] catchHandlers= declarator.getCatchHandlers();
				for (int i = 0; i < catchHandlers.length; i++) {
					catchHandlers[i].accept(this);
				}
			}
			return PROCESS_CONTINUE;
		}
		
		/*
		 * @see org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor#visit(org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition)
		 */
		public int visit(ICPPASTNamespaceDefinition namespace) {
			if (!fFilePath.equals(namespace.getContainingFilename())) {
				return PROCESS_SKIP;
			}
			if (checkForMacro(namespace)) {
				return PROCESS_SKIP;
			}
			return PROCESS_CONTINUE;
		}

		/*
		 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier)
		 */
		public int visit(IASTDeclSpecifier declSpec) {
			if (checkForMacro(declSpec)) {
				return PROCESS_SKIP;
			}
			return PROCESS_CONTINUE;
		}

		/*
		 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTDeclarator)
		 */
		public int visit(IASTDeclarator declarator) {
			if (checkForMacro(declarator)) {
				return PROCESS_SKIP;
			}
			if (declarator instanceof ICPPASTFunctionTryBlockDeclarator) {
				shouldVisitCatchHandlers= false;
			}
			return PROCESS_CONTINUE;
		}

		/*
		 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTExpression)
		 */
		public int visit(IASTExpression expression) {
			if (checkForMacro(expression)) {
				return PROCESS_SKIP;
			}
			return PROCESS_CONTINUE;
		}

		/*
		 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTStatement)
		 */
		public int visit(IASTStatement statement) {
			if (!shouldVisitCatchHandlers && statement instanceof ICPPASTCatchHandler) {
				return PROCESS_SKIP;
			}
			if (checkForMacro(statement)) {
				return PROCESS_SKIP;
			}
			return PROCESS_CONTINUE;
		}

		/*
		 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTName)
		 */
		public int visit(IASTName name) {
			if (checkForMacro(name)) {
				return PROCESS_SKIP;
			}
			if (visitNode(name)) {
				return PROCESS_SKIP;
			}
			return PROCESS_CONTINUE;
		}
		
		private boolean checkForMacro(IASTNode node) {
			boolean isLeafNode= isLeafNode(node);
			IASTNodeLocation[] nodeLocations= node.getNodeLocations();
			for (int i= 0; i < nodeLocations.length; i++) {
				IASTNodeLocation nodeLocation= nodeLocations[i];
				if (nodeLocation instanceof IASTMacroExpansion) {
					IASTNodeLocation useLocation= nodeLocation.asFileLocation();
					if (useLocation != null) {
						final int useOffset = useLocation.getNodeOffset();
						if (useOffset > fMinLocation) {
							fMinLocation= useOffset;
							IASTPreprocessorMacroDefinition macroDef= ((IASTMacroExpansion)nodeLocation).getMacroDefinition();
							final int macroLength;
							IASTNodeLocation defLocation= macroDef.getName().getFileLocation();
							if (defLocation != null) {
								macroLength= defLocation.getNodeLength();
							} else {
								macroLength= macroDef.getName().toCharArray().length;
							}
							IASTNode macroNode= node.getTranslationUnit().selectNodeForLocation(fFilePath, useOffset, macroLength);
							if (macroNode != null && visitMacro(macroNode, macroLength)) {
								fMinLocation= useOffset + macroLength;
							}
						}
					}
				}
				if (!isLeafNode) {
					break;
				}
			}
			return false;
		}

		private boolean isLeafNode(IASTNode node) {
			return fgLeafNodeTester.isLeafNode(node);
		}

		private boolean visitMacro(IASTNode node, int macroLength) {
			fToken.update(node);
			for (int i= 0, n= fJobSemanticHighlightings.length; i < n; ++i) {
				SemanticHighlighting semanticHighlighting= fJobSemanticHighlightings[i];
				if (fJobHighlightings[i].isEnabled() && semanticHighlighting.consumes(fToken)) {
					if (node instanceof IASTName) {
						addNameLocation((IASTName)node, fJobHighlightings[i]);
					} else {
						addMacroLocation(node.getFileLocation(), macroLength, fJobHighlightings[i]);
					}
					break;
				}
			}
			fToken.clear();
			// always consume this node
			return true;
		}

		private boolean visitNode(IASTNode node) {
			boolean consumed= false;
			fToken.update(node);
			for (int i= 0, n= fJobSemanticHighlightings.length; i < n; ++i) {
				SemanticHighlighting semanticHighlighting= fJobSemanticHighlightings[i];
				if (fJobHighlightings[i].isEnabled() && semanticHighlighting.consumes(fToken)) {
					if (node instanceof IASTName) {
						addNameLocation((IASTName)node, fJobHighlightings[i]);
					} else {
						addNodeLocation(node.getFileLocation(), fJobHighlightings[i]);
					}
					consumed= true;
					break;
				}
			}
			fToken.clear();
			return consumed;
		}

		/**
		 * Add the a location range for the given name.
		 * 
		 * @param name  The name
		 * @param highlighting The highlighting
		 */
		private void addNameLocation(IASTName name, HighlightingStyle highlightingStyle) {
			IASTImageLocation imageLocation= name.getImageLocation();
			if (imageLocation == null) {
				addNodeLocation(name.getFileLocation(), highlightingStyle);
			} else {
				int offset= imageLocation.getNodeOffset();
				if (offset >= fMinLocation) {
					int length= imageLocation.getNodeLength();
					if (offset > -1 && length > 0) {
						addPosition(offset, length, highlightingStyle);
					}
				}
			}
		}

		/**
		 * Add the a location range for the given highlighting.
		 * 
		 * @param nodeLocation  The node location
		 * @param highlighting The highlighting
		 */
		private void addNodeLocation(IASTNodeLocation nodeLocation, HighlightingStyle highlighting) {
			if (nodeLocation == null) {
				return;
			}
			int offset= nodeLocation.getNodeOffset();
			if (offset >= fMinLocation) {
				int length= nodeLocation.getNodeLength();
				if (offset > -1 && length > 0) {
					addPosition(offset, length, highlighting);
				}
			}
		}

		/**
		 * Add the a location range for the given macro highlighting.
		 * 
		 * @param macroUseLocaton The location of the macro occurrence
		 * @param macroLength  the length of the macro name
		 * @param highlighting The highlighting
		 */
		private void addMacroLocation(IASTNodeLocation macroUseLocation, int macroLength, HighlightingStyle highlighting) {
			if (macroUseLocation == null) {
				return;
			}
			int offset= macroUseLocation.getNodeOffset();
			int length= macroLength;
			if (offset > -1 && length > 0) {
				addPosition(offset, length, highlighting);
			}
		}

		/**
		 * Add a position with the given range and highlighting iff it does not exist already.
		 * 
		 * @param offset The range offset
		 * @param length The range length
		 * @param highlighting The highlighting
		 */
		private void addPosition(int offset, int length, HighlightingStyle highlighting) {
			boolean isExisting= false;
			// TODO: use binary search
			for (int i= 0, n= fRemovedPositions.size(); i < n; i++) {
				HighlightedPosition position= (HighlightedPosition) fRemovedPositions.get(i);
				if (position == null)
					continue;
				if (position.isEqual(offset, length, highlighting)) {
					isExisting= true;
					fRemovedPositions.set(i, null);
					fNOfRemovedPositions--;
					break;
				}
			}

			if (!isExisting) {
				Position position= fJobPresenter.createHighlightedPosition(offset, length, highlighting);
				fAddedPositions.add(position);
			}
		}

	}

	/** The C editor this semantic highlighting reconciler is installed on */
	private CEditor fEditor;
	/** The semantic highlighting presenter */
	private SemanticHighlightingPresenter fPresenter;
	/** Semantic highlightings */
	private SemanticHighlighting[] fSemanticHighlightings;
	/** Highlightings */
	private HighlightingStyle[] fHighlightings;

	/** Background job's added highlighted positions */
	private List fAddedPositions= new ArrayList();
	/** Background job's removed highlighted positions */
	private List fRemovedPositions= new ArrayList();
	/** Number of removed positions */
	private int fNOfRemovedPositions;

	/** Background job */
	private Job fJob;
	/** Background job lock */
	private final Object fJobLock= new Object();
	/** Reconcile operation lock. */
	private final Object fReconcileLock= new Object();
	/**
	 * <code>true</code> if any thread is executing
	 * <code>reconcile</code>, <code>false</code> otherwise.
	 */
	private boolean fIsReconciling= false;

	/** The semantic highlighting presenter - cache for background thread, only valid during {@link #reconciled(IASTTranslationUnit, boolean, IProgressMonitor)} */
	private SemanticHighlightingPresenter fJobPresenter;
	/** Semantic highlightings - cache for background thread, only valid during {@link #reconciled(IASTTranslationUnit, boolean, IProgressMonitor)} */
	private SemanticHighlighting[] fJobSemanticHighlightings;
	/** Highlightings - cache for background thread, only valid during {@link #reconciled(IASTTranslationUnit, boolean, IProgressMonitor)} */
	private HighlightingStyle[] fJobHighlightings;

	/*
	 * @see org.eclipse.cdt.internal.ui.text.java.ICReconcilingListener#aboutToBeReconciled()
	 */
	public void aboutToBeReconciled() {
		// Do nothing
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.ICReconcilingListener#reconciled(IASTTranslationUnit, boolean, IProgressMonitor)
	 */
	public void reconciled(IASTTranslationUnit ast, boolean force, IProgressMonitor progressMonitor) {
		// ensure at most one thread can be reconciling at any time
		synchronized (fReconcileLock) {
			if (fIsReconciling)
				return;
			else
				fIsReconciling= true;
		}
		fJobPresenter= fPresenter;
		fJobSemanticHighlightings= fSemanticHighlightings;
		fJobHighlightings= fHighlightings;
		
		try {
			if (fJobPresenter == null || fJobSemanticHighlightings == null || fJobHighlightings == null)
				return;
			
			fJobPresenter.setCanceled(progressMonitor != null && progressMonitor.isCanceled());
			
			if (ast == null || fJobPresenter.isCanceled())
				return;
			
			PositionCollector collector= new PositionCollector(ast.getFilePath());

			startReconcilingPositions();
			
			if (!fJobPresenter.isCanceled())
				reconcilePositions(ast, collector);
			
			TextPresentation textPresentation= null;
			if (!fJobPresenter.isCanceled())
				textPresentation= fJobPresenter.createPresentation(fAddedPositions, fRemovedPositions);
			
			if (!fJobPresenter.isCanceled())
				updatePresentation(textPresentation, fAddedPositions, fRemovedPositions);
			
			stopReconcilingPositions();
		} finally {
			fJobPresenter= null;
			fJobSemanticHighlightings= null;
			fJobHighlightings= null;
			synchronized (fReconcileLock) {
				fIsReconciling= false;
			}
		}
	}

	/**
	 * Start reconciling positions.
	 */
	private void startReconcilingPositions() {
		fJobPresenter.addAllPositions(fRemovedPositions);
		fNOfRemovedPositions= fRemovedPositions.size();
	}

	/**
	 * Reconcile positions based on the AST.
	 *
	 * @param ast  the AST
	 * @param visitor  the AST visitor
	 */
	private void reconcilePositions(IASTTranslationUnit ast, PositionCollector visitor) {
		ast.accept(visitor);
		List oldPositions= fRemovedPositions;
		List newPositions= new ArrayList(fNOfRemovedPositions);
		for (int i= 0, n= oldPositions.size(); i < n; i ++) {
			Object current= oldPositions.get(i);
			if (current != null)
				newPositions.add(current);
		}
		fRemovedPositions= newPositions;
		// positions need to be sorted by ascending offset
		Collections.sort(fAddedPositions, new Comparator() {
			public int compare(Object o1, Object o2) {
				final Position p1= (Position)o1;
				final Position p2= (Position)o2;
				return p1.getOffset() - p2.getOffset();
			}});
	}

	/**
	 * Update the presentation.
	 *
	 * @param textPresentation the text presentation
	 * @param addedPositions the added positions
	 * @param removedPositions the removed positions
	 */
	private void updatePresentation(TextPresentation textPresentation, List addedPositions, List removedPositions) {
		Runnable runnable= fJobPresenter.createUpdateRunnable(textPresentation, addedPositions, removedPositions);
		if (runnable == null)
			return;

		CEditor editor= fEditor;
		if (editor == null)
			return;

		IWorkbenchPartSite site= editor.getSite();
		if (site == null)
			return;

		Shell shell= site.getShell();
		if (shell == null || shell.isDisposed())
			return;

		Display display= shell.getDisplay();
		if (display == null || display.isDisposed())
			return;

		display.asyncExec(runnable);
	}

	/**
	 * Stop reconciling positions.
	 */
	private void stopReconcilingPositions() {
		fRemovedPositions.clear();
		fNOfRemovedPositions= 0;
		fAddedPositions.clear();
	}

	/**
	 * Install this reconciler on the given editor, presenter and highlightings.
	 * @param editor the editor
	 * @param sourceViewer the source viewer
	 * @param presenter the semantic highlighting presenter
	 * @param semanticHighlightings the semantic highlightings
	 * @param highlightings the highlightings
	 */
	public void install(CEditor editor, ISourceViewer sourceViewer, SemanticHighlightingPresenter presenter, SemanticHighlighting[] semanticHighlightings, HighlightingStyle[] highlightings) {
		fPresenter= presenter;
		fSemanticHighlightings= semanticHighlightings;
		fHighlightings= highlightings;

		fEditor= editor;

		if (fEditor != null) {
			fEditor.addReconcileListener(this);
		}
	}

	/**
	 * Uninstall this reconciler from the editor
	 */
	public void uninstall() {
		if (fPresenter != null)
			fPresenter.setCanceled(true);

		if (fEditor != null) {
			fEditor.removeReconcileListener(this);
			fEditor= null;
		}

		fSemanticHighlightings= null;
		fHighlightings= null;
		fPresenter= null;
	}

	/**
	 * Schedule a background job for retrieving the AST and reconciling the Semantic Highlighting model.
	 */
	private void scheduleJob() {
		final ICElement element= fEditor.getInputCElement();

		synchronized (fJobLock) {
			final Job oldJob= fJob;
			if (fJob != null) {
				fJob.cancel();
				fJob= null;
			}
			
			if (element != null) {
				fJob= new Job(CEditorMessages.getString("SemanticHighlighting_job")) { //$NON-NLS-1$
					protected IStatus run(final IProgressMonitor monitor) {
						if (oldJob != null) {
							try {
								oldJob.join();
							} catch (InterruptedException e) {
								CUIPlugin.getDefault().log(e);
								return Status.CANCEL_STATUS;
							}
						}
						if (monitor.isCanceled())
							return Status.CANCEL_STATUS;
						
						final Job me= this;
						ASTProvider astProvider= CUIPlugin.getDefault().getASTProvider();
						IStatus status= astProvider.runOnAST(element, ASTProvider.WAIT_YES, monitor, new ASTCache.ASTRunnable() {
							public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) {
								reconciled(ast, true, monitor);
								synchronized (fJobLock) {
									// allow the job to be gc'ed
									if (fJob == me)
										fJob= null;
								}
								return Status.OK_STATUS;
							}
						});
						return status;
					}
				};
//				fJob.setSystem(true);
				fJob.setPriority(Job.DECORATE);
				fJob.schedule();
			}
		}
	}

	/**
	 * Refreshes the highlighting.
	 */
	public void refresh() {
		scheduleJob();
	}
}
