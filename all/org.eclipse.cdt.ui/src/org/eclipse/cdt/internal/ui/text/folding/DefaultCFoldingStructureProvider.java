/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Markus Schorn (Wind River Systems)
 *     Elazar Leibovich (IDF) - Code folding of compound statements (bug 174597)
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.folding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.projection.IProjectionListener;
import org.eclipse.jface.text.source.projection.IProjectionPosition;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElseStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorEndifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfdefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfndefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.cdt.ui.text.folding.ICFoldingStructureProvider;

import org.eclipse.cdt.internal.core.model.ASTCache;

import org.eclipse.cdt.internal.ui.editor.ASTProvider;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.text.DocumentCharacterIterator;
import org.eclipse.cdt.internal.ui.text.ICReconcilingListener;

/**
 * Default implementation of a {@link ICFoldingStructureProvider}.
 * <p>
 * Derived from JDT counterpart.
 * </p>
 */
public class DefaultCFoldingStructureProvider implements ICFoldingStructureProvider {

	/**
	 * Listen to cursor position changes.
	 */
	private final class SelectionListener implements ISelectionChangedListener {
		public void selectionChanged(SelectionChangedEvent event) {
			ISelection s= event.getSelection();
			if (s instanceof ITextSelection) {
				ITextSelection selection= (ITextSelection)event.getSelection();
				fCursorPosition= selection.getOffset();
			}
		}
	}

	/**
	 * Reconcile annotation positions from preprocessor branches.
	 */
	private class PreprocessorBranchesReconciler implements ICReconcilingListener {
		volatile boolean fReconciling;

		/*
		 * @see org.eclipse.cdt.internal.ui.text.ICReconcilingListener#aboutToBeReconciled()
		 */
		public void aboutToBeReconciled() {
			
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.text.ICReconcilingListener#reconciled(IASTTranslationUnit, boolean, IProgressMonitor)
		 */
		public void reconciled(IASTTranslationUnit ast, boolean force, IProgressMonitor progressMonitor) {
			if (fInput == null || fReconciling) {
				return;
			}
			if (fPreprocessorBranchFoldingEnabled && ast == null) {
				return;
			}
			fReconciling= true;
			try {
				FoldingStructureComputationContext ctx= createContext(fInitialReconcilePending);
				fInitialReconcilePending= false;
				if (ctx != null) {
					ctx.fAST= ast;
					update(ctx);
				}
			} finally {
				fReconciling= false;
			}
		}

	}


	/**
	 * A context that contains the information needed to compute the folding structure of an
	 * {@link ITranslationUnit}. Computed folding regions are collected via
	 * {@linkplain #addProjectionRange(DefaultCFoldingStructureProvider.CProjectionAnnotation, Position) addProjectionRange}.
	 */
	protected final class FoldingStructureComputationContext {
		private final ProjectionAnnotationModel fModel;
		private final IDocument fDocument;
		private final boolean fAllowCollapsing;

		private ISourceReference fFirstType;
		private boolean fHasHeaderComment;
		private LinkedHashMap<CProjectionAnnotation,Position> fMap= new LinkedHashMap<CProjectionAnnotation,Position>();
		private IASTTranslationUnit fAST;

		FoldingStructureComputationContext(IDocument document, ProjectionAnnotationModel model, boolean allowCollapsing) {
			Assert.isNotNull(document);
			Assert.isNotNull(model);
			fDocument= document;
			fModel= model;
			fAllowCollapsing= allowCollapsing;
		}
		
		void setFirstType(ISourceReference reference) {
			if (hasFirstType())
				throw new IllegalStateException();
			fFirstType= reference;
		}
		
		boolean hasFirstType() {
			return fFirstType != null;
		}
		
		ISourceReference getFirstType() {
			return fFirstType;
		}

		boolean hasHeaderComment() {
			return fHasHeaderComment;
		}

		void setHasHeaderComment() {
			fHasHeaderComment= true;
		}
		
		/**
		 * Returns <code>true</code> if newly created folding regions may be collapsed,
		 * <code>false</code> if not. This is usually <code>false</code> when updating the
		 * folding structure while typing; it may be <code>true</code> when computing or restoring
		 * the initial folding structure.
		 * 
		 * @return <code>true</code> if newly created folding regions may be collapsed,
		 *         <code>false</code> if not
		 */
		public boolean allowCollapsing() {
			return fAllowCollapsing;
		}

		/**
		 * Returns the document which contains the code being folded.
		 * 
		 * @return the document which contains the code being folded
		 */
		IDocument getDocument() {
			return fDocument;
		}

		ProjectionAnnotationModel getModel() {
			return fModel;
		}
		
		/**
		 * Adds a projection (folding) region to this context. The created annotation / position
		 * pair will be added to the {@link ProjectionAnnotationModel} of the
		 * {@link ProjectionViewer} of the editor.
		 * 
		 * @param annotation the annotation to add
		 * @param position the corresponding position
		 */
		public void addProjectionRange(CProjectionAnnotation annotation, Position position) {
			fMap.put(annotation, position);
		}

		/**
		 * Returns <code>true</code> if header comments should be collapsed.
		 * 
		 * @return <code>true</code> if header comments should be collapsed
		 */
		public boolean collapseHeaderComments() {
			return fAllowCollapsing && fCollapseHeaderComments;
		}

		/**
		 * Returns <code>true</code> if comments should be collapsed.
		 * 
		 * @return <code>true</code> if comments should be collapsed
		 */
		public boolean collapseComments() {
			return fAllowCollapsing && fCollapseComments;
		}

		/**
		 * Returns <code>true</code> if functions should be collapsed.
		 * 
		 * @return <code>true</code> if functions should be collapsed
		 */
		public boolean collapseFunctions() {
			return fAllowCollapsing && fCollapseFunctions;
		}

		/**
		 * Returns <code>true</code> if macros should be collapsed.
		 * 
		 * @return <code>true</code> if macros should be collapsed
		 */
		public boolean collapseMacros() {
			return fAllowCollapsing && fCollapseMacros;
		}

		/**
		 * Returns <code>true</code> if methods should be collapsed.
		 * 
		 * @return <code>true</code> if methods should be collapsed
		 */
		public boolean collapseMethods() {
			return fAllowCollapsing && fCollapseMethods;
		}

		/**
		 * Returns <code>true</code> if structures should be collapsed.
		 * 
		 * @return <code>true</code> if structures should be collapsed
		 */
		public boolean collapseStructures() {
			return fAllowCollapsing && fCollapseStructures;
		}

		/**
		 * Returns <code>true</code> if inactive code should be collapsed.
		 * 
		 * @return <code>true</code> if inactive code should be collapsed
		 */
		public boolean collapseInactiveCode() {
			return fAllowCollapsing && fCollapseInactiveCode;
		}

		/**
		 * @return the current AST or <code>null</code>
		 */
		public IASTTranslationUnit getAST() {
			return fAST;
		}
	}
	

	private static class CProjectionAnnotation extends ProjectionAnnotation {

		public final static int COMMENT= 1;
		public final static int BRANCH= 2;
		
		private Object fKey;
		private int fCategory;
		
		public CProjectionAnnotation(boolean isCollapsed, Object key, boolean isComment) {
			this(isCollapsed, key, isComment ? COMMENT : 0);
		}
		
		public CProjectionAnnotation(boolean isCollapsed, Object key, int category) {
			super(isCollapsed);
			fKey= key;
			fCategory= category;
		}
		
		public Object getElement() {
			return fKey;
		}
		
		public void setElement(Object element) {
			fKey= element;
		}
		
		public int getCategory() {
			return fCategory;
		}

		public void setCategory(int category) {
			fCategory = category;
		}

		public boolean isComment() {
			return fCategory == COMMENT;
		}
		
		public void setIsComment(boolean isComment) {
			fCategory= isComment ? COMMENT : 0;
		}
		/*
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return "CProjectionAnnotation:\n" + //$NON-NLS-1$
					"\tkey: \t"+ fKey + "\n" + //$NON-NLS-1$ //$NON-NLS-2$
					"\tcollapsed: \t" + isCollapsed() + "\n" + //$NON-NLS-1$ //$NON-NLS-2$
					"\tcomment: \t" + isComment() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	

	private static final class Tuple {
		CProjectionAnnotation annotation;
		Position position;
		Tuple(CProjectionAnnotation annotation, Position position) {
			this.annotation= annotation;
			this.position= position;
		}
	}

	
	/**
	 * Projection position that will return two foldable regions: one folding away
	 * the region from after the '/*' to the beginning of the content, the other
	 * from after the first content line until after the comment.
	 */
	private static final class CommentPosition extends Position implements IProjectionPosition {
		CommentPosition(int offset, int length) {
			super(offset, length);
		}

		/*
		 * @see org.eclipse.jface.text.source.projection.IProjectionPosition#computeFoldingRegions(org.eclipse.jface.text.IDocument)
		 */
		public IRegion[] computeProjectionRegions(IDocument document) throws BadLocationException {
			DocumentCharacterIterator sequence= new DocumentCharacterIterator(document, offset, offset + length);
			int prefixEnd= 0;
			int contentStart= findFirstContent(sequence, prefixEnd);

			int firstLine= document.getLineOfOffset(offset + prefixEnd);
			int captionLine= document.getLineOfOffset(offset + contentStart);
			int lastLine= document.getLineOfOffset(offset + length);

			Assert.isTrue(firstLine <= captionLine, "first folded line is greater than the caption line"); //$NON-NLS-1$
			Assert.isTrue(captionLine <= lastLine, "caption line is greater than the last folded line"); //$NON-NLS-1$

			IRegion preRegion;
			if (firstLine < captionLine) {
				int preOffset= document.getLineOffset(firstLine);
				IRegion preEndLineInfo= document.getLineInformation(captionLine);
				int preEnd= preEndLineInfo.getOffset();
				preRegion= new Region(preOffset, preEnd - preOffset);
			} else {
				preRegion= null;
			}

			if (captionLine < lastLine) {
				int postOffset= document.getLineOffset(captionLine + 1);
				IRegion postRegion= new Region(postOffset, offset + length - postOffset);

				if (preRegion == null)
					return new IRegion[] { postRegion };

				return new IRegion[] { preRegion, postRegion };
			}

			if (preRegion != null)
				return new IRegion[] { preRegion };

			return null;
		}

		/**
		 * Finds the offset of the first identifier part within <code>content</code>.
		 * Returns 0 if none is found.
		 *
		 * @param content the content to search
		 * @return the first index of a unicode identifier part, or zero if none can
		 *         be found
		 */
		private int findFirstContent(final CharSequence content, int prefixEnd) {
			int lenght= content.length();
			for (int i= prefixEnd; i < lenght; i++) {
				if (Character.isUnicodeIdentifierPart(content.charAt(i)))
					return i;
			}
			return 0;
		}

		/*
		 * @see org.eclipse.jface.text.source.projection.IProjectionPosition#computeCaptionOffset(org.eclipse.jface.text.IDocument)
		 */
		public int computeCaptionOffset(IDocument document) {
//			return 0;
			DocumentCharacterIterator sequence= new DocumentCharacterIterator(document, offset, offset + length);
			return findFirstContent(sequence, 0);
		}
	}

	/**
	 * Projection position that will return two foldable regions: one folding away
	 * the lines before the one containing the simple name of the C element, one
	 * folding away any lines after the caption.
	 */
	private static final class CElementPosition extends Position implements IProjectionPosition {

		private ICElement fElement;

		public CElementPosition(int offset, int length, ICElement element) {
			super(offset, length);
			Assert.isNotNull(element);
			fElement= element;
		}
		
		public void setElement(ICElement member) {
			Assert.isNotNull(member);
			fElement= member;
		}
		
		/*
		 * @see org.eclipse.jface.text.source.projection.IProjectionPosition#computeFoldingRegions(org.eclipse.jface.text.IDocument)
		 */
		public IRegion[] computeProjectionRegions(IDocument document) throws BadLocationException {
			int nameStart= offset;
			try {
				/* The member's name range may not be correct. However,
				 * reconciling would trigger another element delta which would
				 * lead to reentrant situations. Therefore, we optimistically
				 * assume that the name range is correct, but double check the
				 * received lines below. */
				if (fElement instanceof ISourceReference) {
					ISourceRange sourceRange= ((ISourceReference) fElement).getSourceRange();
					if (sourceRange != null)
						nameStart= sourceRange.getIdStartPos();
				}
			} catch (CModelException e) {
				// ignore and use default
			}

			int firstLine= document.getLineOfOffset(offset);
			int captionLine= document.getLineOfOffset(nameStart);
			int lastLine= document.getLineOfOffset(offset + length);

			/* see comment above - adjust the caption line to be inside the
			 * entire folded region, and rely on later element deltas to correct
			 * the name range. */
			if (captionLine < firstLine)
				captionLine= firstLine;
			if (captionLine > lastLine)
				captionLine= lastLine;

			IRegion preRegion;
			if (firstLine < captionLine) {
				int preOffset= document.getLineOffset(firstLine);
				IRegion preEndLineInfo= document.getLineInformation(captionLine);
				int preEnd= preEndLineInfo.getOffset();
				preRegion= new Region(preOffset, preEnd - preOffset);
			} else {
				preRegion= null;
			}

			if (captionLine < lastLine) {
				int postOffset= document.getLineOffset(captionLine + 1);
				IRegion postRegion= new Region(postOffset, offset + length - postOffset);

				if (preRegion == null)
					return new IRegion[] { postRegion };

				return new IRegion[] { preRegion, postRegion };
			}

			if (preRegion != null)
				return new IRegion[] { preRegion };

			return null;
		}

		/*
		 * @see org.eclipse.jface.text.source.projection.IProjectionPosition#computeCaptionOffset(org.eclipse.jface.text.IDocument)
		 */
		public int computeCaptionOffset(IDocument document) throws BadLocationException {
			int nameStart= offset;
			try {
				// need a reconcile here?
				if (fElement instanceof ISourceReference) {
					ISourceRange sourceRange= ((ISourceReference) fElement).getSourceRange();
					if (sourceRange != null)
						nameStart= sourceRange.getIdStartPos();
				}
			} catch (CModelException e) {
				// ignore and use default
			}

			return nameStart - offset;
		}

	}
	
	/**
	 * Internal projection listener.
	 */
	private final class ProjectionListener implements IProjectionListener {
		private ProjectionViewer fViewer;

		/**
		 * Registers the listener with the viewer.
		 * 
		 * @param viewer the viewer to register a listener with
		 */
		public ProjectionListener(ProjectionViewer viewer) {
			Assert.isLegal(viewer != null);
			fViewer= viewer;
			fViewer.addProjectionListener(this);
		}
		
		/**
		 * Disposes of this listener and removes the projection listener from the viewer.
		 */
		public void dispose() {
			if (fViewer != null) {
				fViewer.removeProjectionListener(this);
				fViewer= null;
			}
		}
		
		/*
		 * @see org.eclipse.jface.text.source.projection.IProjectionListener#projectionEnabled()
		 */
		public void projectionEnabled() {
			handleProjectionEnabled();
		}

		/*
		 * @see org.eclipse.jface.text.source.projection.IProjectionListener#projectionDisabled()
		 */
		public void projectionDisabled() {
			handleProjectionDisabled();
		}
	}

	/**
	 * Implementation of <code>IRegion</code> that can be reused
	 * by setting the offset and the length.
	 */
	private static class ModifiableRegion extends Position implements IRegion {
		ModifiableRegion() {
			super();
		}
		ModifiableRegion(int offset, int length) {
			super(offset, length);
		}
	}

	/**
	 * Representation of a preprocessor code branch.
	 */
	private static class Branch extends ModifiableRegion {

		private boolean fTaken;
		public boolean fInclusive;

		/**
		 * @param offset
		 * @param taken
		 */
		Branch(int offset, boolean taken) {
			super(offset, 0);
			fTaken= taken;
		}

		/**
		 * @param offset
		 * @param length
		 * @param taken
		 */
		Branch(int offset, int length, boolean taken) {
			super(offset, length);
			fTaken= taken;
		}

		public void setEndOffset(int endOffset) {
			setLength(endOffset - getOffset());
		}

		public boolean taken() {
			return fTaken;
		}

		public void setInclusive(boolean inclusive) {
			fInclusive= inclusive;
		}
	}

	private final static boolean DEBUG= "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.cdt.ui/debug/folding"));  //$NON-NLS-1$//$NON-NLS-2$;

	private ITextEditor fEditor;
	private ProjectionListener fProjectionListener;
	protected ICElement fInput;
	private IElementChangedListener fElementListener;
	
	private boolean fCollapseHeaderComments= true;
	private boolean fCollapseComments= false;
	private boolean fCollapseMacros= false;
	private boolean fCollapseFunctions= true;
	private boolean fCollapseStructures= true;
	private boolean fCollapseMethods= false;
	private boolean fCollapseInactiveCode= true;
	
	private int fMinCommentLines= 1;
	private boolean fPreprocessorBranchFoldingEnabled= true;
	private boolean fStatementsFoldingEnabled= false;
	private boolean fCommentFoldingEnabled= true;

	private ICReconcilingListener fReconilingListener;
	boolean fInitialReconcilePending= true;

	private int fCursorPosition;

	private SelectionListener fSelectionListener;


	/**
	 * Creates a new folding provider. It must be
	 * {@link #install(ITextEditor, ProjectionViewer) installed} on an editor/viewer pair before it
	 * can be used, and {@link #uninstall() uninstalled} when not used any longer.
	 * <p>
	 * The projection state may be reset by calling {@link #initialize()}.
	 * </p>
	 */
	public DefaultCFoldingStructureProvider() {
	}
	
	/*
	 * @see org.eclipse.cdt.ui.text.folding.ICFoldingStructureProvider#install(org.eclipse.ui.texteditor.ITextEditor, org.eclipse.jface.text.source.projection.ProjectionViewer)
	 */
	public void install(ITextEditor editor, ProjectionViewer viewer) {
		Assert.isLegal(editor != null);
		Assert.isLegal(viewer != null);

		internalUninstall();
		
		if (editor instanceof CEditor) {
			fEditor= editor;
			fProjectionListener= new ProjectionListener(viewer);
		}
	}

	/*
	 * @see org.eclipse.cdt.ui.text.folding.ICFoldingStructureProvider#uninstall()
	 */
	public void uninstall() {
		internalUninstall();
	}
	
	/**
	 * Internal implementation of {@link #uninstall()}.
	 */
	private void internalUninstall() {
		if (isInstalled()) {
			handleProjectionDisabled();
			fProjectionListener.dispose();
			fProjectionListener= null;
			fEditor= null;
		}
	}

	/**
	 * Returns <code>true</code> if the provider is installed, <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if the provider is installed, <code>false</code> otherwise
	 */
	protected final boolean isInstalled() {
		return fEditor != null;
	}
		
	/**
	 * Called whenever projection is enabled, for example when the viewer issues a
	 * {@link IProjectionListener#projectionEnabled() projectionEnabled} message. When the provider
	 * is already enabled when this method is called, it is first
	 * {@link #handleProjectionDisabled() disabled}.
	 * <p>
	 * Subclasses may extend.
	 * </p>
	 */
	protected void handleProjectionEnabled() {
		if (DEBUG) System.out.println("DefaultCFoldingStructureProvider.handleProjectionEnabled()"); //$NON-NLS-1$
		// projectionEnabled messages are not always paired with projectionDisabled
		// i.e. multiple enabled messages may be sent out.
		// we have to make sure that we disable first when getting an enable
		// message.
		handleProjectionDisabled();

		if (fEditor instanceof CEditor) {
			fInitialReconcilePending= true;
			initialize();
			fReconilingListener= new PreprocessorBranchesReconciler();
			((CEditor)fEditor).addReconcileListener(fReconilingListener);
			fSelectionListener= new SelectionListener();
			fEditor.getSelectionProvider().addSelectionChangedListener(fSelectionListener);
		}
	}

	/**
	 * Called whenever projection is disabled, for example when the provider is
	 * {@link #uninstall() uninstalled}, when the viewer issues a
	 * {@link IProjectionListener#projectionDisabled() projectionDisabled} message and before
	 * {@link #handleProjectionEnabled() enabling} the provider. Implementations must be prepared to
	 * handle multiple calls to this method even if the provider is already disabled.
	 * <p>
	 * Subclasses may extend.
	 * </p>
	 */
	protected void handleProjectionDisabled() {
		if (fElementListener != null) {
			CoreModel.getDefault().removeElementChangedListener(fElementListener);
			fElementListener= null;
		}
		if (fReconilingListener != null) {
			((CEditor)fEditor).removeReconcileListener(fReconilingListener);
			fReconilingListener= null;
		}
		if (fSelectionListener != null) {
			fEditor.getSelectionProvider().removeSelectionChangedListener(fSelectionListener);
			fSelectionListener= null;
		}
	}

	/*
	 * @see org.eclipse.cdt.ui.text.folding.ICFoldingStructureProvider#initialize()
	 */
	public final void initialize() {
		if (DEBUG) System.out.println("DefaultCFoldingStructureProvider.initialize()"); //$NON-NLS-1$
		update(createInitialContext());
	}

	private FoldingStructureComputationContext createInitialContext() {
		initializePreferences();
		fInput= getInputElement();
		if (fInput == null)
			return null;
		
		return createContext(true);
	}

	private FoldingStructureComputationContext createContext(boolean allowCollapse) {
		if (!isInstalled())
			return null;
		ProjectionAnnotationModel model= getModel();
		if (model == null)
			return null;
		IDocument doc= getDocument();
		if (doc == null)
			return null;
		
		return new FoldingStructureComputationContext(doc, model, allowCollapse);
	}
	
	private ICElement getInputElement() {
		if (fEditor instanceof CEditor) {
			return ((CEditor)fEditor).getInputCElement();
		}
		return null;
	}

	private void initializePreferences() {
		IPreferenceStore store= CUIPlugin.getDefault().getPreferenceStore();
		fCollapseFunctions= store.getBoolean(PreferenceConstants.EDITOR_FOLDING_FUNCTIONS);
		fCollapseStructures= store.getBoolean(PreferenceConstants.EDITOR_FOLDING_STRUCTURES);
		fCollapseMacros= store.getBoolean(PreferenceConstants.EDITOR_FOLDING_MACROS);
		fCollapseMethods= store.getBoolean(PreferenceConstants.EDITOR_FOLDING_METHODS);
		fCollapseHeaderComments= store.getBoolean(PreferenceConstants.EDITOR_FOLDING_HEADERS);
		fCollapseComments= store.getBoolean(PreferenceConstants.EDITOR_FOLDING_COMMENTS);
		fCollapseInactiveCode= store.getBoolean(PreferenceConstants.EDITOR_FOLDING_INACTIVE_CODE);
		fPreprocessorBranchFoldingEnabled= store.getBoolean(PreferenceConstants.EDITOR_FOLDING_PREPROCESSOR_BRANCHES_ENABLED);
		fStatementsFoldingEnabled= store.getBoolean(PreferenceConstants.EDITOR_FOLDING_STATEMENTS);
		fCommentFoldingEnabled = true;
	}

	private void update(FoldingStructureComputationContext ctx) {
	    if (ctx == null)
			return;

		if (!fInitialReconcilePending && fSelectionListener != null) {
			fEditor.getSelectionProvider().removeSelectionChangedListener(fSelectionListener);
			fSelectionListener= null;
		}

		Map<CProjectionAnnotation,Position> additions= new HashMap<CProjectionAnnotation,Position>();
		List<CProjectionAnnotation> deletions= new ArrayList<CProjectionAnnotation>();
		List<CProjectionAnnotation> updates= new ArrayList<CProjectionAnnotation>();
		
		computeFoldingStructure(ctx);
		Map updated= ctx.fMap;
		Map previous= computeCurrentStructure(ctx);

		Iterator e= updated.keySet().iterator();
		while (e.hasNext()) {
			CProjectionAnnotation newAnnotation= (CProjectionAnnotation) e.next();
			Object key= newAnnotation.getElement();
			Position newPosition= (Position) updated.get(newAnnotation);

			List annotations= (List) previous.get(key);
			if (annotations == null) {
				if (DEBUG) System.out.println("DefaultCFoldingStructureProvider.update() new annotation " + newAnnotation); //$NON-NLS-1$

				additions.put(newAnnotation, newPosition);

			} else {
				Iterator x= annotations.iterator();
				boolean matched= false;
				while (x.hasNext()) {
					Tuple tuple= (Tuple) x.next();
					CProjectionAnnotation existingAnnotation= tuple.annotation;
					Position existingPosition= tuple.position;
					if (newAnnotation.isComment() == existingAnnotation.isComment()) {
						if (existingPosition != null && (!newPosition.equals(existingPosition) || ctx.allowCollapsing() && existingAnnotation.isCollapsed() != newAnnotation.isCollapsed())) {
							existingPosition.setOffset(newPosition.getOffset());
							existingPosition.setLength(newPosition.getLength());
							if (ctx.allowCollapsing() && existingAnnotation.isCollapsed() != newAnnotation.isCollapsed())
								if (DEBUG) System.out.println("DefaultCFoldingStructureProvider.update() change annotation " + newAnnotation); //$NON-NLS-1$
								if (newAnnotation.isCollapsed())
									existingAnnotation.markCollapsed();
								else
									existingAnnotation.markExpanded();
							updates.add(existingAnnotation);
						}
						matched= true;
						x.remove();
						break;
					}
				}
				if (!matched) {
					if (DEBUG) System.out.println("DefaultCFoldingStructureProvider.update() new annotation " + newAnnotation); //$NON-NLS-1$

					additions.put(newAnnotation, newPosition);
				}
				if (annotations.isEmpty())
					previous.remove(key);
			}
		}

		e= previous.values().iterator();
		while (e.hasNext()) {
			List list= (List) e.next();
			int size= list.size();
			for (int i= 0; i < size; i++) {
				CProjectionAnnotation annotation= ((Tuple) list.get(i)).annotation;
				if (DEBUG) System.out.println("DefaultCFoldingStructureProvider.update() deleted annotation " + annotation); //$NON-NLS-1$
				deletions.add(annotation);
			}
		}

		match(deletions, additions, updates, ctx);

		Annotation[] removals= new Annotation[deletions.size()];
		deletions.toArray(removals);
		Annotation[] changes= new Annotation[updates.size()];
		updates.toArray(changes);
		if (DEBUG) System.out.println("DefaultCFoldingStructureProvider.update() "+removals.length+" deleted, "+additions.size()+" added, "+changes.length+" changed"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ctx.getModel().modifyAnnotations(removals, additions, changes);
    }

	/**
	 * Matches deleted annotations to changed or added ones. A deleted
	 * annotation/position tuple that has a matching addition / change
	 * is updated and marked as changed. The matching tuple is not added
	 * (for additions) or marked as deletion instead (for changes). The
	 * result is that more annotations are changed and fewer get
	 * deleted/re-added.
	 */
	private void match(List<CProjectionAnnotation> deletions, Map additions,
			List<CProjectionAnnotation> changes, FoldingStructureComputationContext ctx) {
		if (deletions.isEmpty() || (additions.isEmpty() && changes.isEmpty()))
			return;

		List<CProjectionAnnotation> newDeletions= new ArrayList<CProjectionAnnotation>();
		List<CProjectionAnnotation> newChanges= new ArrayList<CProjectionAnnotation>();

		Iterator deletionIterator= deletions.iterator();
		while (deletionIterator.hasNext()) {
			CProjectionAnnotation deleted= (CProjectionAnnotation) deletionIterator.next();
			Position deletedPosition= ctx.getModel().getPosition(deleted);
			if (deletedPosition == null)
				continue;
			
			Tuple deletedTuple= new Tuple(deleted, deletedPosition);

			Tuple match= findMatch(deletedTuple, changes, null, ctx);
			boolean addToDeletions= true; 
			if (match == null) {
				match= findMatch(deletedTuple, additions.keySet(), additions, ctx);
				addToDeletions= false;
			}
			
			if (match != null) {
				Object element= match.annotation.getElement();
				deleted.setElement(element);
				deletedPosition.setLength(match.position.getLength());
				if (deletedPosition instanceof CElementPosition && element instanceof ICElement) {
					CElementPosition cep= (CElementPosition) deletedPosition;
					cep.setElement((ICElement) element);
				}

				deletionIterator.remove();
				if (DEBUG) System.out.println("DefaultCFoldingStructureProvider.update() changed annotation " + deleted); //$NON-NLS-1$
				newChanges.add(deleted);

				if (addToDeletions) {
					if (DEBUG) System.out.println("DefaultCFoldingStructureProvider.update() deleted annotation " + match.annotation); //$NON-NLS-1$
					newDeletions.add(match.annotation);
				}
			}
		}

		deletions.addAll(newDeletions);
		changes.addAll(newChanges);
	}

	/**
	 * Finds a match for <code>tuple</code> in a collection of
	 * annotations. The positions for the
	 * <code>CProjectionAnnotation</code> instances in
	 * <code>annotations</code> can be found in the passed
	 * <code>positionMap</code> or <code>fCachedModel</code> if
	 * <code>positionMap</code> is <code>null</code>.
	 * <p>
	 * A tuple is said to match another if their annotations have the
	 * same comment flag and their position offsets are equal.
	 * </p>
	 * <p>
	 * If a match is found, the annotation gets removed from
	 * <code>annotations</code>.
	 * </p>
	 * 
	 * @param tuple the tuple for which we want to find a match
	 * @param annotations collection of
	 *        <code>CProjectionAnnotation</code>
	 * @param positionMap a <code>Map&lt;Annotation, Position&gt;</code>
	 *        or <code>null</code>
	 * @return a matching tuple or <code>null</code> for no match
	 */
	private Tuple findMatch(Tuple tuple, Collection annotations, Map positionMap, FoldingStructureComputationContext ctx) {
		Iterator it= annotations.iterator();
		while (it.hasNext()) {
			CProjectionAnnotation annotation= (CProjectionAnnotation) it.next();
			if (tuple.annotation.isComment() == annotation.isComment()) {
				Position position= positionMap == null ? ctx.getModel().getPosition(annotation) : (Position) positionMap.get(annotation);
				if (position == null)
					continue;

				if (tuple.position.getOffset() == position.getOffset()) {
					it.remove();
					return new Tuple(annotation, position);
				}
			}
		}
		
		return null;
	}

	private Map computeCurrentStructure(FoldingStructureComputationContext ctx) {
		boolean includeBranches= fPreprocessorBranchFoldingEnabled && ctx.fAST != null;
		Map<Object, List<Tuple>> map= new HashMap<Object, List<Tuple>>();
		ProjectionAnnotationModel model= ctx.getModel();
		Iterator e= model.getAnnotationIterator();
		while (e.hasNext()) {
			Object annotation= e.next();
			if (annotation instanceof CProjectionAnnotation) {
				CProjectionAnnotation cAnnotation= (CProjectionAnnotation) annotation;
				if (!includeBranches && cAnnotation.getCategory() == CProjectionAnnotation.BRANCH) {
					continue;
				}
				Position position= model.getPosition(cAnnotation);
				Assert.isNotNull(position);
				List<Tuple> list= map.get(cAnnotation.getElement());
				if (list == null) {
					list= new ArrayList<Tuple>(2);
					map.put(cAnnotation.getElement(), list);
				}
				list.add(new Tuple(cAnnotation, position));
			}
		}

		Comparator<Tuple> comparator= new Comparator<Tuple>() {
			public int compare(Tuple t1, Tuple t2) {
				return t1.position.getOffset() - t2.position.getOffset();
			}
		};
		for(List<Tuple> list : map.values()) {
			Collections.sort(list, comparator);
		}
		return map;
	}
	

	private void computeFoldingStructure(final FoldingStructureComputationContext ctx) {
		if (fCommentFoldingEnabled) {
			// compute comment positions from partitioning
			try {
				IDocument doc= ctx.getDocument();
				ITypedRegion[] partitions = TextUtilities.computePartitioning(doc, ICPartitions.C_PARTITIONING, 0, doc.getLength(), false);
				computeFoldingStructure(partitions, ctx);
			} catch (BadLocationException e) {
				// ignore
			}
		}
		if (fPreprocessorBranchFoldingEnabled || fStatementsFoldingEnabled) {
			IASTTranslationUnit ast= ctx.getAST();
			if (ast == null) {
				final ASTProvider astProvider= CUIPlugin.getDefault().getASTProvider();
				IStatus status= astProvider.runOnAST(getInputElement(), ASTProvider.WAIT_ACTIVE_ONLY, null, new ASTCache.ASTRunnable() {
					public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) {
						if (ast != null) {
							ctx.fAST= ast;
							fInitialReconcilePending= false;
							computeFoldingStructure(ast, ctx);
						}
						return Status.OK_STATUS;
					}
				});
				if (status.matches(IStatus.ERROR)) {
					CUIPlugin.getDefault().log(status);
				}
			} else {
				computeFoldingStructure(ast, ctx);
			}
		}
		if (!fInitialReconcilePending || isConsistent(fInput)) {
			IParent parent= (IParent) fInput;
			try {
				computeFoldingStructure(parent.getChildren(), ctx);
			} catch (CModelException x) {
			}
		}
	}
	
	static boolean isConsistent(ICElement element) {
		if (element instanceof ITranslationUnit) {
			try {
				return ((ITranslationUnit)element).isConsistent();
			} catch (CModelException exc) {
			}
		}
		return false;
	}

	private static class ModifiableRegionExtra extends ModifiableRegion {
		/*
		 * A modifiable region with extra information about the region it holds.
		 * It tells us whether or not to include the last line of the region
		 */
		public boolean inclusive;
		public String function;
		public int level;
	}

	/**
	 * Computes folding structure for preprocessor branches for the given AST.
	 * 
	 * @param ast
	 * @param ctx
	 */
	private void computeStatementFoldingStructure(IASTTranslationUnit ast, FoldingStructureComputationContext ctx) {
		final Stack<ModifiableRegionExtra> iral= new Stack<ModifiableRegionExtra>();
		ast.accept(new ASTVisitor() {
			{
				shouldVisitStatements = true;
				shouldVisitDeclarations = true;
			}
			int fLevel= 0;
			String fFunction= ""; //$NON-NLS-1$
			public int visit(IASTStatement statement) {
				++fLevel;
				// if it's not part of the displayed - file, we don't need it
				if (!statement.isPartOfTranslationUnitFile())
					return PROCESS_SKIP;// we neither need its descendants
				try {
					ModifiableRegionExtra mr;
					IASTFileLocation fl;
					if (statement instanceof IASTIfStatement) {
						IASTIfStatement ifstmt = (IASTIfStatement) statement;
						fl = ifstmt.getFileLocation();
						if (fl==null) return PROCESS_CONTINUE;
						int ifOffset= fl.getNodeOffset();
						IASTStatement tmp;
						mr = new ModifiableRegionExtra();
						mr.function= fFunction;
						mr.level= fLevel;
						tmp = ifstmt.getThenClause();
						if (tmp==null) return PROCESS_CONTINUE;
						fl = tmp.getFileLocation();
						mr.setLength(fl.getNodeOffset() + fl.getNodeLength() - ifOffset);
						mr.setOffset(ifOffset);
						mr.inclusive = false;
						tmp = ifstmt.getElseClause();
						if (tmp==null) {
							mr.inclusive = true;
							iral.push(mr);
							return PROCESS_CONTINUE;
						}
						iral.push(mr);
						mr = new ModifiableRegionExtra();
						mr.function= fFunction;
						mr.level= fLevel;
						fl = tmp.getFileLocation();
						mr.setLength(fl.getNodeLength());
						mr.setOffset(fl.getNodeOffset());
						mr.inclusive = true;
						iral.push(mr);
						return PROCESS_CONTINUE;
					}
					mr = new ModifiableRegionExtra();
					mr.function= fFunction;
					mr.level= fLevel;
					mr.inclusive = true;
					if (statement instanceof IASTDoStatement)
						mr.inclusive = false;
					if (statement instanceof IASTSwitchStatement) {
						IASTStatement switchstmt = ((IASTSwitchStatement)statement).getBody();
						if (switchstmt instanceof IASTCompoundStatement) {
							IASTStatement[] stmts = ((IASTCompoundStatement)switchstmt).getStatements();
							boolean pushedMR = false;
							for (int i = 0; i < stmts.length; i++) {
								IASTStatement tmpstmt = stmts[i];
								ModifiableRegionExtra tmpmr;
								if (!(tmpstmt instanceof IASTCaseStatement || tmpstmt instanceof IASTDefaultStatement)) {
									if (!pushedMR) return PROCESS_SKIP;
									IASTFileLocation tmpfl = tmpstmt.getFileLocation();
									tmpmr = iral.peek();
									tmpmr.setLength(tmpfl.getNodeLength()+tmpfl.getNodeOffset()-tmpmr.getOffset());
									if (tmpstmt instanceof IASTBreakStatement) pushedMR = false;
									continue;
								}
								IASTFileLocation tmpfl;
								tmpmr = new ModifiableRegionExtra();
								tmpmr.function= fFunction;
								tmpmr.level= fLevel+1;
								tmpmr.inclusive = true;
								if (tmpstmt instanceof IASTCaseStatement) {
									IASTCaseStatement casestmt = (IASTCaseStatement) tmpstmt;
									tmpfl = casestmt.getExpression().getFileLocation();
									tmpmr.setOffset(tmpfl.getNodeOffset());
									tmpmr.setLength(tmpfl.getNodeLength());
								} else if (tmpstmt instanceof IASTDefaultStatement) {
									IASTDefaultStatement defstmt = (IASTDefaultStatement) tmpstmt;
									tmpfl = defstmt.getFileLocation();
									tmpmr.setOffset(tmpfl.getNodeOffset()+tmpfl.getNodeLength());
									tmpmr.setLength(0);
								}
								iral.push(tmpmr);
								pushedMR = true;
							}
						}
					}
					if (statement instanceof IASTForStatement ||
							statement instanceof IASTWhileStatement ||
							statement instanceof IASTDoStatement ||
							statement instanceof IASTSwitchStatement) {
						fl = statement.getFileLocation();
						mr.setLength(fl.getNodeLength());
						mr.setOffset(fl.getNodeOffset());
						iral.push(mr);
					}
					return PROCESS_CONTINUE;
				} catch (Exception e) {
					CUIPlugin.getDefault().log(e);
					return PROCESS_ABORT;
				}
			}
			public int leave(IASTStatement statement) {
				--fLevel;
				return PROCESS_CONTINUE;
			}
			public int visit(IASTDeclaration declaration) {
				if (!declaration.isPartOfTranslationUnitFile())
					return PROCESS_SKIP;// we neither need its descendants
				if (declaration instanceof IASTFunctionDefinition) {
					final IASTFunctionDeclarator declarator = ((IASTFunctionDefinition)declaration).getDeclarator();
					if (declarator != null) {
						fFunction= new String(declarator.getName().toCharArray());
					}
				}
				return PROCESS_CONTINUE;
			}
			public int leave(IASTDeclaration declaration) {
				fFunction= ""; //$NON-NLS-1$
				return PROCESS_CONTINUE;
			}
		});
		while (!iral.empty()) {
			ModifiableRegionExtra mr = iral.pop();
			IRegion aligned = alignRegion(mr, ctx,mr.inclusive);
			if (aligned != null) {
				Position alignedPos= new Position(aligned.getOffset(), aligned.getLength());
				ctx.addProjectionRange(new CProjectionAnnotation(false, mr.function + mr.level + computeKey(mr, ctx), false), alignedPos);
			}
		}
	}

	/**
	 * Compute folding structure of things related to the AST tree. Currently it
	 * computes the folding structure for: preprocessor branches for the given
	 * AST. Also, it computes statements folding (if/else do/while for and
	 * switch)
	 * 
	 * @param ast
	 * @param ctx
	 */
	private void computeFoldingStructure(IASTTranslationUnit ast, FoldingStructureComputationContext ctx) {
		if (ast == null) {
			return;
		}
		String fileName = ast.getFilePath();
		if (fileName == null) {
			return;
		}

		if (fStatementsFoldingEnabled)
			computeStatementFoldingStructure(ast, ctx);

		if (fPreprocessorBranchFoldingEnabled)
			computePreprocessorFoldingStructure(ast, ctx, fileName);
	}

	/**
	 * Computes folding structure for preprocessor branches for the given AST.
	 * 
	 * @param ast
	 * @param ctx
	 * @param fileName
	 */
	private void computePreprocessorFoldingStructure(IASTTranslationUnit ast,
			FoldingStructureComputationContext ctx, String fileName) {
		List<Branch> branches = new ArrayList<Branch>();
		Stack<Branch> branchStack = new Stack<Branch>();

		IASTPreprocessorStatement[] preprocStmts = ast.getAllPreprocessorStatements();

		for (int i = 0; i < preprocStmts.length; i++) {
			IASTPreprocessorStatement statement = preprocStmts[i];
			if (!statement.isPartOfTranslationUnitFile()) {
				// preprocessor directive is from a different file
				continue;
			}
			IASTNodeLocation stmtLocation= statement.getFileLocation();
			if (stmtLocation == null) {
				continue;
			}
			if (statement instanceof IASTPreprocessorIfStatement) {
				IASTPreprocessorIfStatement ifStmt = (IASTPreprocessorIfStatement)statement;
				branchStack.push(new Branch(stmtLocation.getNodeOffset(), ifStmt.taken()));
			} else if (statement instanceof IASTPreprocessorIfdefStatement) {
				IASTPreprocessorIfdefStatement ifdefStmt = (IASTPreprocessorIfdefStatement)statement;
				branchStack.push(new Branch(stmtLocation.getNodeOffset(), ifdefStmt.taken()));
			} else if (statement instanceof IASTPreprocessorIfndefStatement) {
				IASTPreprocessorIfndefStatement ifndefStmt = (IASTPreprocessorIfndefStatement)statement;
				branchStack.push(new Branch(stmtLocation.getNodeOffset(), ifndefStmt.taken()));
			} else if (statement instanceof IASTPreprocessorElseStatement) {
				if (branchStack.isEmpty()) {
					// #else without #if
					continue;
				}
				Branch branch= branchStack.pop();
				IASTPreprocessorElseStatement elseStmt = (IASTPreprocessorElseStatement)statement;
				branchStack.push(new Branch(stmtLocation.getNodeOffset(), elseStmt.taken()));
				branch.setEndOffset(stmtLocation.getNodeOffset());
				branches.add(branch);
			} else if (statement instanceof IASTPreprocessorElifStatement) {
				if (branchStack.isEmpty()) {
					// #elif without #if
					continue;
				}
				Branch branch= branchStack.pop();
				IASTPreprocessorElifStatement elifStmt = (IASTPreprocessorElifStatement) statement;
				branchStack.push(new Branch(stmtLocation.getNodeOffset(), elifStmt.taken()));
				branch.setEndOffset(stmtLocation.getNodeOffset());
				branches.add(branch);
			} else if (statement instanceof IASTPreprocessorEndifStatement) {
				if (branchStack.isEmpty()) {
					// #endif without #if
					continue;
				}
				Branch branch= branchStack.pop();
				branch.setEndOffset(stmtLocation.getNodeOffset() + stmtLocation.getNodeLength());
				branch.setInclusive(true);
				branches.add(branch);
			}
		}
		for (Iterator iter = branches.iterator(); iter.hasNext(); ) {
			Branch branch= (Branch) iter.next();
			IRegion aligned = alignRegion(branch, ctx, branch.fInclusive);
			if (aligned != null) {
				Position alignedPos= new Position(aligned.getOffset(), aligned.getLength());
				final boolean collapse= !branch.taken() && ctx.collapseInactiveCode() && !alignedPos.includes(fCursorPosition);
				ctx.addProjectionRange(new CProjectionAnnotation(collapse, computeKey(branch, ctx), false), alignedPos);
			}
		}
	}

	/**
	 * Compute a key for recognizing an annotation based on the given position.
	 * 
	 * @param pos
	 * @param ctx
	 * @return a key to recognise an annotation position
	 */
	private Object computeKey(Position pos, FoldingStructureComputationContext ctx) {
		try {
			return ctx.getDocument().get(pos.offset, Math.min(16, pos.length));
		} catch (BadLocationException exc) {
			return exc;
		}
	}

	/**
	 * Compute folding structure based on partioning information.
	 * 
	 * @param partitions  array of document partitions
	 * @param ctx  the folding structure context
	 * @throws BadLocationException 
	 */
	private void computeFoldingStructure(ITypedRegion[] partitions, FoldingStructureComputationContext ctx) throws BadLocationException {
		boolean collapse = ctx.collapseComments();
		IDocument doc= ctx.getDocument();
		int startLine = -1;
		int endLine = -1;
		List<Tuple> comments= new ArrayList<Tuple>();
		ModifiableRegion commentRange = new ModifiableRegion();
		for (int i = 0; i < partitions.length; i++) {
			ITypedRegion partition = partitions[i];
			boolean singleLine= false;
			if (ICPartitions.C_MULTI_LINE_COMMENT.equals(partition.getType())
				|| ICPartitions.C_MULTI_LINE_DOC_COMMENT.equals(partition.getType())) {
				Position position= createCommentPosition(alignRegion(partition, ctx, true));
				if (position != null) {
					if (startLine >= 0 && endLine - startLine >= fMinCommentLines) {
						Position projection = createCommentPosition(alignRegion(commentRange, ctx, true));
						if (projection != null) {
							comments.add(new Tuple(new CProjectionAnnotation(collapse, doc.get(projection.offset, Math.min(16, projection.length)), true), projection));
						}
						startLine= -1;
					}
					comments.add(new Tuple(new CProjectionAnnotation(collapse, doc.get(position.offset, Math.min(16, position.length)), true), position));
				} else {
					singleLine= true;
				}
			} else {
				singleLine= ICPartitions.C_SINGLE_LINE_COMMENT.equals(partition.getType());
			}
			if (singleLine) {
				// if comment starts at column 0 and spans only one line
				// and is adjacent to a previous line comment, add it
				// to the commentRange
				int lineNr = doc.getLineOfOffset(partition.getOffset());
				IRegion lineRegion = doc.getLineInformation(lineNr);
				boolean isLineStart = partition.getOffset() == lineRegion.getOffset();
				if (!isLineStart) {
					continue;
				}
				if (!singleLine) {
					singleLine = lineRegion.getOffset() + lineRegion.getLength() >= partition.getOffset() + partition.getLength();
					if (!singleLine) {
						continue;
					}
				}
				if (startLine < 0 || lineNr - endLine > 1) {
					if (startLine >= 0 && endLine - startLine >= fMinCommentLines) {
						Position projection = createCommentPosition(alignRegion(commentRange, ctx, true));
						if (projection != null) {
							comments.add(new Tuple(new CProjectionAnnotation(collapse, doc.get(projection.offset, Math.min(16, projection.length)), true), projection));
						}
					}
					startLine = lineNr;
					endLine = lineNr;
					commentRange.offset = lineRegion.getOffset();
					commentRange.length = lineRegion.getLength();
				} else {
					endLine = lineNr;
					int delta = lineRegion.getOffset() + lineRegion.getLength() - commentRange.offset - commentRange.length;
					commentRange.length += delta;
				}
			}
		}
		if (startLine >= 0 && endLine - startLine >= fMinCommentLines) {
			Position projection = createCommentPosition(alignRegion(commentRange, ctx, true));
			if (projection != null) {
				comments.add(new Tuple(new CProjectionAnnotation(collapse, doc.get(projection.offset, Math.min(16, projection.length)), true), projection));
			}
		}
		if (!comments.isEmpty()) {
			// first comment starting before line 10 is considered the header comment
			Iterator iter = comments.iterator();
			Tuple tuple = (Tuple) iter.next();
			int lineNr = doc.getLineOfOffset(tuple.position.getOffset());
			if (lineNr < 10) {
				if (ctx.collapseHeaderComments()) {
					tuple.annotation.markCollapsed();
				} else {
					tuple.annotation.markExpanded();
				}
			}
			ctx.addProjectionRange(tuple.annotation, tuple.position);
			while (iter.hasNext()) {
				tuple = (Tuple) iter.next();
				ctx.addProjectionRange(tuple.annotation, tuple.position);
			}
		}
	}

	private void computeFoldingStructure(ICElement[] elements, FoldingStructureComputationContext ctx) throws CModelException {
		for (int i= 0; i < elements.length; i++) {
			ICElement element= elements[i];

			computeFoldingStructure(element, ctx);

			if (element instanceof IParent) {
				IParent parent= (IParent) element;
				computeFoldingStructure(parent.getChildren(), ctx);
			}
		}
	}

	/**
	 * Computes the folding structure for a given {@link ICElement C element}. Computed
	 * projection annotations are
	 * {@link DefaultCFoldingStructureProvider.FoldingStructureComputationContext#addProjectionRange(DefaultCFoldingStructureProvider.CProjectionAnnotation, Position) added}
	 * to the computation context.
	 * <p>
	 * Subclasses may extend or replace. The default implementation creates projection annotations
	 * for the following elements:
	 * <ul>
	 * <li>structs, unions, classes</li>
	 * <li>functions</li>
	 * <li>methods</li>
	 * <li>multiline macro definitions</li>
	 * </ul>
	 * </p>
	 * 
	 * @param element the C element to compute the folding structure for
	 * @param ctx the computation context
	 */
	protected void computeFoldingStructure(ICElement element, FoldingStructureComputationContext ctx) {

		boolean collapse= false;
		switch (element.getElementType()) {

		case ICElement.C_STRUCT:
		case ICElement.C_CLASS:
		case ICElement.C_UNION:
		case ICElement.C_ENUMERATION:
		case ICElement.C_TEMPLATE_STRUCT:
		case ICElement.C_TEMPLATE_CLASS:
		case ICElement.C_TEMPLATE_UNION:
			collapse= ctx.collapseStructures();
			break;
		case ICElement.C_MACRO:
			collapse= ctx.collapseMacros();
			break;
		case ICElement.C_FUNCTION:
		case ICElement.C_TEMPLATE_FUNCTION:
			collapse= ctx.collapseFunctions();
			break;
		case ICElement.C_METHOD:
		case ICElement.C_TEMPLATE_METHOD:
			collapse= ctx.collapseMethods();
			break;
		default:
			return;
		}

		IRegion[] regions= computeProjectionRanges((ISourceReference) element, ctx);
		if (regions.length > 0) {
			IRegion normalized= alignRegion(regions[regions.length - 1], ctx, true);
			if (normalized != null) {
				Position position= createElementPosition(normalized, element);
				if (position != null) {
					collapse= collapse && !position.includes(fCursorPosition);
					ctx.addProjectionRange(new CProjectionAnnotation(collapse, element, false), position);
				}
			}
		}
	}

	/**
	 * Computes the projection ranges for a given <code>ISourceReference</code>. More than one
	 * range or none at all may be returned. If there are no foldable regions, an empty array is
	 * returned.
	 * <p>
	 * The last region in the returned array (if not empty) describes the region for the C
	 * element that implements the source reference. Any preceding regions describe comments
	 * of that element.
	 * </p>
	 * 
	 * @param reference a C element that is a source reference
	 * @param ctx the folding context
	 * @return the regions to be folded
	 */
	protected final IRegion[] computeProjectionRanges(ISourceReference reference, FoldingStructureComputationContext ctx) {
		try {
			ISourceRange range= reference.getSourceRange();
			return new IRegion[] {
				new Region(range.getStartPos(), range.getLength())
			};
		} catch (CModelException e) {
		}

		return new IRegion[0];
	}

	/**
	 * Creates a comment folding position from an
	 * {@link #alignRegion(IRegion, DefaultCFoldingStructureProvider.FoldingStructureComputationContext, boolean) aligned}
	 * region.
	 * 
	 * @param aligned an aligned region
	 * @return a folding position corresponding to <code>aligned</code>
	 */
	protected final Position createCommentPosition(IRegion aligned) {
		if (aligned == null) {
			return null;
		}
		return new CommentPosition(aligned.getOffset(), aligned.getLength());
	}

	/**
	 * Creates a folding position that remembers its element from an
	 * {@link #alignRegion(IRegion, DefaultCFoldingStructureProvider.FoldingStructureComputationContext, boolean) aligned}
	 * region.
	 * 
	 * @param aligned an aligned region
	 * @param element the element to remember
	 * @return a folding position corresponding to <code>aligned</code>
	 */
	protected final Position createElementPosition(IRegion aligned, ICElement element) {
		return new CElementPosition(aligned.getOffset(), aligned.getLength(), element);
	}

	/**
	 * Aligns <code>region</code> to start and end at a line offset. The region's start is
	 * decreased to the next line offset, and the end offset increased to the next line start or the
	 * end of the document. <code>null</code> is returned if <code>region</code> is
	 * <code>null</code> itself or does not comprise at least one line delimiter, as a single line
	 * cannot be folded.
	 * 
	 * @param region the region to align, may be <code>null</code>
	 * @param ctx the folding context
	 * @return a region equal or greater than <code>region</code> that is aligned with line
	 *         offsets, <code>null</code> if the region is too small to be foldable (e.g. covers
	 *         only one line)
	 */
	protected final IRegion alignRegion(IRegion region, FoldingStructureComputationContext ctx) {
		return alignRegion(region, ctx, true);
	}

	/**
	 * Aligns <code>region</code> to start and end at a line offset. The region's start is
	 * decreased to the next line offset, and the end offset increased to the next line start or the
	 * end of the document. <code>null</code> is returned if <code>region</code> is
	 * <code>null</code> itself or does not comprise at least one line delimiter, as a single line
	 * cannot be folded.
	 * 
	 * @param region the region to align, may be <code>null</code>
	 * @param ctx the folding context
	 * @param inclusive include line of end offset
	 * @return a region equal or greater than <code>region</code> that is aligned with line
	 *         offsets, <code>null</code> if the region is too small to be foldable (e.g. covers
	 *         only one line)
	 */
	protected final IRegion alignRegion(IRegion region, FoldingStructureComputationContext ctx, boolean inclusive) {
		if (region == null)
			return null;
		
		IDocument document= ctx.getDocument();
		
		try {
			
			int start= document.getLineOfOffset(region.getOffset());
			int end= document.getLineOfOffset(region.getOffset() + region.getLength());
			if (start >= end)
				return null;
			
			int offset= document.getLineOffset(start);
			int endOffset;
			if (inclusive) {
				if (document.getNumberOfLines() > end + 1)
					endOffset= document.getLineOffset(end + 1);
				else
					endOffset= document.getLineOffset(end) + document.getLineLength(end);
			} else {
				endOffset= document.getLineOffset(end);
			}
			return new Region(offset, endOffset - offset);
			
		} catch (BadLocationException x) {
			// concurrent modification
			return null;
		}
	}
	
	private ProjectionAnnotationModel getModel() {
		return (ProjectionAnnotationModel) fEditor.getAdapter(ProjectionAnnotationModel.class);
	}

	private IDocument getDocument() {
		IDocumentProvider provider= fEditor.getDocumentProvider();
		return provider.getDocument(fEditor.getEditorInput());
	}

}
