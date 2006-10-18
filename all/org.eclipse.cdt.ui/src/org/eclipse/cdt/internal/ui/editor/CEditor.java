/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems)
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin, Google
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;


import java.text.CharacterIterator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Stack;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.AbstractInformationControlManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ILineTracker;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.ITextViewerExtension4;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.IWidgetTokenKeeper;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.formatter.FormattingContextProperties;
import org.eclipse.jface.text.formatter.IFormattingContext;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.IInformationProviderExtension;
import org.eclipse.jface.text.information.IInformationProviderExtension2;
import org.eclipse.jface.text.information.InformationPresenter;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.text.link.LinkedModeUI.ExitFlags;
import org.eclipse.jface.text.link.LinkedModeUI.IExitPolicy;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHoverExtension;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension3;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.search.ui.actions.TextSearchGroup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTargetList;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.ITextEditorDropTargetListener;
import org.eclipse.ui.texteditor.IUpdate;
import org.eclipse.ui.texteditor.ResourceAction;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.texteditor.TextEditorAction;
import org.eclipse.ui.texteditor.TextNavigationAction;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import com.ibm.icu.text.BreakIterator;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.core.IPositionConverter;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.refactoring.actions.CRefactoringActionGroup;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IWorkingCopyManager;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.actions.GenerateActionGroup;
import org.eclipse.cdt.ui.actions.OpenViewActionGroup;
import org.eclipse.cdt.ui.actions.ShowInCViewAction;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.cdt.ui.text.folding.ICFoldingStructureProvider;

import org.eclipse.cdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.cdt.internal.corext.util.SimplePositionTracker;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.IContextMenuConstants;
import org.eclipse.cdt.internal.ui.actions.AddBlockCommentAction;
import org.eclipse.cdt.internal.ui.actions.FoldingActionGroup;
import org.eclipse.cdt.internal.ui.actions.GoToNextPreviousMemberAction;
import org.eclipse.cdt.internal.ui.actions.IndentAction;
import org.eclipse.cdt.internal.ui.actions.JoinLinesAction;
import org.eclipse.cdt.internal.ui.actions.RemoveBlockCommentAction;
import org.eclipse.cdt.internal.ui.actions.SelectionConverter;
import org.eclipse.cdt.internal.ui.dnd.TextEditorDropAdapter;
import org.eclipse.cdt.internal.ui.dnd.TextViewerDragAdapter;
import org.eclipse.cdt.internal.ui.search.actions.OpenDeclarationsAction;
import org.eclipse.cdt.internal.ui.search.actions.OpenDefinitionAction;
import org.eclipse.cdt.internal.ui.search.actions.SelectionSearchGroup;
import org.eclipse.cdt.internal.ui.text.CHeuristicScanner;
import org.eclipse.cdt.internal.ui.text.CPairMatcher;
import org.eclipse.cdt.internal.ui.text.CSourceViewerConfiguration;
import org.eclipse.cdt.internal.ui.text.CTextTools;
import org.eclipse.cdt.internal.ui.text.CWordIterator;
import org.eclipse.cdt.internal.ui.text.DocumentCharacterIterator;
import org.eclipse.cdt.internal.ui.text.HTMLTextPresenter;
import org.eclipse.cdt.internal.ui.text.ICReconcilingListener;
import org.eclipse.cdt.internal.ui.text.Symbols;
import org.eclipse.cdt.internal.ui.text.c.hover.SourceViewerInformationControl;
import org.eclipse.cdt.internal.ui.text.comment.CommentFormattingContext;
import org.eclipse.cdt.internal.ui.text.contentassist.ContentAssistPreference;
import org.eclipse.cdt.internal.ui.util.CUIHelp;


/**
 * C specific text editor.
 */
public class CEditor extends TextEditor implements ISelectionChangedListener, IReconcilingParticipant, ICReconcilingListener {

	interface ITextConverter {
		void customizeDocumentCommand(IDocument document, DocumentCommand command);
	}

	class AdaptedSourceViewer extends CSourceViewer  {

		public AdaptedSourceViewer(Composite parent, IVerticalRuler verticalRuler, IOverviewRuler overviewRuler,
				                   boolean showAnnotationsOverview, int styles, IPreferenceStore store) {
			super(parent, verticalRuler, overviewRuler, showAnnotationsOverview, styles, store);
		}

		public IContentAssistant getContentAssistant() {
			return fContentAssistant;
		}

		/*
		 * @see ITextOperationTarget#doOperation(int)
		 */
		public void doOperation(int operation) {

			if (getTextWidget() == null)
				return;

			switch (operation) {
				case CONTENTASSIST_PROPOSALS:
//					long time= CODE_ASSIST_DEBUG ? System.currentTimeMillis() : 0;
					String msg= fContentAssistant.showPossibleCompletions();
//					if (CODE_ASSIST_DEBUG) {
//						long delta= System.currentTimeMillis() - time;
//						System.err.println("Code Assist (total): " + delta); //$NON-NLS-1$
//					}
					setStatusLineErrorMessage(msg);
					return;
				case QUICK_ASSIST:
					/*
					 * XXX: We can get rid of this once the SourceViewer has a way to update the status line
					 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=133787
					 */
					msg= fQuickAssistAssistant.showPossibleQuickAssists();
					setStatusLineErrorMessage(msg);
					return;
			}

			super.doOperation(operation);
		}

		public void updateIndentationPrefixes() {
			SourceViewerConfiguration configuration= getSourceViewerConfiguration();
			String[] types= configuration.getConfiguredContentTypes(this);
			for (int i= 0; i < types.length; i++) {
				String[] prefixes= configuration.getIndentPrefixes(this, types[i]);
				if (prefixes != null && prefixes.length > 0)
					setIndentPrefixes(prefixes, types[i]);
			}
		}

		/*
		 * @see IWidgetTokenOwner#requestWidgetToken(IWidgetTokenKeeper)
		 */
		public boolean requestWidgetToken(IWidgetTokenKeeper requester) {
			if (PlatformUI.getWorkbench().getHelpSystem().isContextHelpDisplayed())
				return false;
			return super.requestWidgetToken(requester);
		}

		/*
		 * @see IWidgetTokenOwnerExtension#requestWidgetToken(IWidgetTokenKeeper, int)
		 * @since 3.0
		 */
		public boolean requestWidgetToken(IWidgetTokenKeeper requester, int priority) {
			if (PlatformUI.getWorkbench().getHelpSystem().isContextHelpDisplayed())
				return false;
			return super.requestWidgetToken(requester, priority);
		}

		/*
		 * @see org.eclipse.jface.text.source.SourceViewer#createFormattingContext()
		 * @since 3.0
		 */
		public IFormattingContext createFormattingContext() {
			IFormattingContext context= new CommentFormattingContext();

			Map preferences;
			ICElement inputCElement= getInputCElement();
			ICProject cProject= inputCElement != null ? inputCElement.getCProject() : null;
			if (cProject == null)
				preferences= new HashMap(CCorePlugin.getOptions());
			else
				preferences= new HashMap(cProject.getOptions(true));

			context.setProperty(FormattingContextProperties.CONTEXT_PREFERENCES, preferences);

			return context;
		}
	}

	static class TabConverter implements ITextConverter {
		private int fTabRatio;
		private ILineTracker fLineTracker;
		
		public TabConverter() {
		}
		
		public void setNumberOfSpacesPerTab(int ratio) {
			fTabRatio = ratio;
		}
		
		public void setLineTracker(ILineTracker lineTracker) {
			fLineTracker = lineTracker;
		}
		
		private int insertTabString(StringBuffer buffer, int offsetInLine) {
			
			if (fTabRatio == 0)
				return 0;
				
			int remainder = offsetInLine % fTabRatio;
			remainder = fTabRatio - remainder;
			for (int i = 0; i < remainder; i++)
				buffer.append(' ');
			return remainder;
		}
		
		public void customizeDocumentCommand(IDocument document, DocumentCommand command) {
			String text = command.text;
			if (text == null)
				return;
				
			int index = text.indexOf('\t');
			if (index > -1) {
				StringBuffer buffer = new StringBuffer();
				
				fLineTracker.set(command.text);
				int lines = fLineTracker.getNumberOfLines();
				
				try {
					for (int i = 0; i < lines; i++) {
						int offset = fLineTracker.getLineOffset(i);
						int endOffset = offset + fLineTracker.getLineLength(i);
						String line = text.substring(offset, endOffset);
						
						int position = 0;
						if (i == 0) {
							IRegion firstLine = document.getLineInformationOfOffset(command.offset);
							position = command.offset - firstLine.getOffset();	
						}
						
						int length = line.length();
						for (int j = 0; j < length; j++) {
							char c = line.charAt(j);
							if (c == '\t') {
								int oldPosition = position;
								position += insertTabString(buffer, position);
								if (command.caretOffset > command.offset + oldPosition) {
									command.caretOffset += position - oldPosition - 1;
								}
							} else {
								buffer.append(c);
								++position;
							}
						}
					}
						
					command.text = buffer.toString();
				} catch (BadLocationException x) {
				}
			}
		}
	}

	private class ExitPolicy implements IExitPolicy {

		final char fExitCharacter;
		final char fEscapeCharacter;
		final Stack fStack;
		final int fSize;

		public ExitPolicy(char exitCharacter, char escapeCharacter, Stack stack) {
			fExitCharacter = exitCharacter;
			fEscapeCharacter = escapeCharacter;
			fStack = stack;
			fSize = fStack.size();
		}

		/*
		 * @see org.eclipse.jface.text.link.LinkedModeUI$IExitPolicy#doExit(org.eclipse.jface.text.link.LinkedModeModel, org.eclipse.swt.events.VerifyEvent, int, int)
		 */
		public ExitFlags doExit(LinkedModeModel model, VerifyEvent event, int offset, int length) {

			if (fSize == fStack.size() && !isMasked(offset)) {
				if (event.character == fExitCharacter) {
					BracketLevel level = (BracketLevel) fStack.peek();
					if (level.fFirstPosition.offset > offset || level.fSecondPosition.offset < offset)
						return null;
					if (level.fSecondPosition.offset == offset && length == 0)
						// don't enter the character if if its the closing peer
						return new ExitFlags(ILinkedModeListener.UPDATE_CARET, false);
				}
				// when entering an anonymous class between the parenthesis', we don't want
				// to jump after the closing parenthesis when return is pressed
				if (event.character == SWT.CR && offset > 0) {
					IDocument document = getSourceViewer().getDocument();
					try {
						if (document.getChar(offset - 1) == '{')
							return new ExitFlags(ILinkedModeListener.EXIT_ALL, true);
					} catch (BadLocationException e) {
					}
				}
			}
			return null;
		}

		private boolean isMasked(int offset) {
			IDocument document = getSourceViewer().getDocument();
			try {
				return fEscapeCharacter == document.getChar(offset - 1);
			} catch (BadLocationException e) {
			}
			return false;
		}
	}

	private static class BracketLevel {
		int fOffset;
		int fLength;
		LinkedModeUI fUI;
		Position fFirstPosition;
		Position fSecondPosition;
	}

	/**
	 * Position updater that takes any changes at the borders of a position to not belong to the position.
	 *
	 * @since 4.0
	 */
	private static class ExclusivePositionUpdater implements IPositionUpdater {

		/** The position category. */
		private final String fCategory;

		/**
		 * Creates a new updater for the given <code>category</code>.
		 *
		 * @param category the new category.
		 */
		public ExclusivePositionUpdater(String category) {
			fCategory = category;
		}

		/*
		 * @see org.eclipse.jface.text.IPositionUpdater#update(org.eclipse.jface.text.DocumentEvent)
		 */
		public void update(DocumentEvent event) {

			int eventOffset = event.getOffset();
			int eventOldLength = event.getLength();
			int eventNewLength = event.getText() == null ? 0 : event.getText().length();
			int deltaLength = eventNewLength - eventOldLength;

			try {
				Position[] positions = event.getDocument().getPositions(fCategory);

				for (int i = 0; i != positions.length; i++) {

					Position position = positions[i];

					if (position.isDeleted())
						continue;

					int offset = position.getOffset();
					int length = position.getLength();
					int end = offset + length;

					if (offset >= eventOffset + eventOldLength)
						// position comes
						// after change - shift
						position.setOffset(offset + deltaLength);
					else if (end <= eventOffset) {
						// position comes way before change -
						// leave alone
					} else if (offset <= eventOffset && end >= eventOffset + eventOldLength) {
						// event completely internal to the position - adjust length
						position.setLength(length + deltaLength);
					} else if (offset < eventOffset) {
						// event extends over end of position - adjust length
						int newEnd = eventOffset;
						position.setLength(newEnd - offset);
					} else if (end > eventOffset + eventOldLength) {
						// event extends from before position into it - adjust offset
						// and length
						// offset becomes end of event, length adjusted accordingly
						int newOffset = eventOffset + eventNewLength;
						position.setOffset(newOffset);
						position.setLength(end - newOffset);
					} else {
						// event consumes the position - delete it
						position.delete();
					}
				}
			} catch (BadPositionCategoryException e) {
				// ignore and return
			}
		}

		/**
		 * Returns the position category.
		 *
		 * @return the position category
		 */
		public String getCategory() {
			return fCategory;
		}
	}

	private class BracketInserter implements VerifyKeyListener, ILinkedModeListener {

		private boolean fCloseBrackets = true;
		private boolean fCloseStrings = true;
		private boolean fCloseAngularBrackets = true;
		private final String CATEGORY = toString();
		private IPositionUpdater fUpdater = new ExclusivePositionUpdater(CATEGORY);
		private Stack fBracketLevelStack = new Stack();

		public void setCloseBracketsEnabled(boolean enabled) {
			fCloseBrackets = enabled;
		}

		public void setCloseStringsEnabled(boolean enabled) {
			fCloseStrings = enabled;
		}

		public void setCloseAngularBracketsEnabled(boolean enabled) {
			fCloseAngularBrackets = enabled;
		}

		private boolean isAngularIntroducer(String identifier) {
			return identifier.length() > 0
				&& (Character.isUpperCase(identifier.charAt(0))
					|| identifier.equals("template") //$NON-NLS-1$
					|| identifier.equals("vector") //$NON-NLS-1$
					|| identifier.equals("list") //$NON-NLS-1$
					|| identifier.equals("slist") //$NON-NLS-1$
					|| identifier.equals("map") //$NON-NLS-1$
					|| identifier.equals("set") //$NON-NLS-1$
					|| identifier.equals("multimap") //$NON-NLS-1$
					|| identifier.equals("multiset") //$NON-NLS-1$
					|| identifier.equals("hash_map") //$NON-NLS-1$
					|| identifier.equals("hash_set") //$NON-NLS-1$
					|| identifier.equals("hash_multimap") //$NON-NLS-1$
					|| identifier.equals("hash_multiset") //$NON-NLS-1$
					|| identifier.equals("pair") //$NON-NLS-1$
					|| identifier.endsWith("_ptr") //$NON-NLS-1$
					|| identifier.endsWith("include")); //$NON-NLS-1$
		}

		/*
		 * @see org.eclipse.swt.custom.VerifyKeyListener#verifyKey(org.eclipse.swt.events.VerifyEvent)
		 */
		public void verifyKey(VerifyEvent event) {

			// early pruning to slow down normal typing as little as possible
			if (!event.doit || getInsertMode() != SMART_INSERT)
				return;
			switch (event.character) {
				case '(':
				case '<':
				case '[':
				case '\'':
				case '\"':
					break;
				default:
					return;
			}

			final ISourceViewer sourceViewer = getSourceViewer();
			IDocument document = sourceViewer.getDocument();

			final Point selection = sourceViewer.getSelectedRange();
			final int offset = selection.x;
			final int length = selection.y;

			try {
				IRegion startLine = document.getLineInformationOfOffset(offset);
				IRegion endLine = document.getLineInformationOfOffset(offset + length);

				CHeuristicScanner scanner = new CHeuristicScanner(document);
				int nextToken = scanner.nextToken(offset + length, endLine.getOffset() + endLine.getLength());
				String next = nextToken == Symbols.TokenEOF ? null : document.get(offset, scanner.getPosition() - offset).trim();
				int prevToken = scanner.previousToken(offset - 1, startLine.getOffset());
				int prevTokenOffset = scanner.getPosition() + 1;
				String previous = prevToken == Symbols.TokenEOF ? null : document.get(prevTokenOffset, offset - prevTokenOffset).trim();

				switch (event.character) {
					case '(':
						if (!fCloseBrackets
								|| nextToken == Symbols.TokenLPAREN
								|| nextToken == Symbols.TokenIDENT
								|| next != null && next.length() > 1)
							return;
						break;

					case '<':
						if (!(fCloseAngularBrackets && fCloseBrackets)
								|| nextToken == Symbols.TokenLESSTHAN
								|| 		   prevToken != Symbols.TokenLBRACE
										&& prevToken != Symbols.TokenRBRACE
										&& prevToken != Symbols.TokenSEMICOLON
										&& prevToken != Symbols.TokenSTATIC
										&& (prevToken != Symbols.TokenIDENT || !isAngularIntroducer(previous))
										&& prevToken != Symbols.TokenEOF)
							return;
						break;

					case '[':
						if (!fCloseBrackets
								|| nextToken == Symbols.TokenIDENT
								|| next != null && next.length() > 1)
							return;
						break;

					case '\'':
					case '"':
						if (!fCloseStrings
								|| nextToken == Symbols.TokenIDENT
								|| next != null && next.length() > 1
								|| (!("include".equals(previous) && event.character == '"') //$NON-NLS-1$
										&& (prevToken == Symbols.TokenIDENT
												|| previous != null && previous.length() > 1)))
							return;
						break;

					default:
						return;
				}

				ITypedRegion partition = TextUtilities.getPartition(document, ICPartitions.C_PARTITIONING, offset, true);
				if (!IDocument.DEFAULT_CONTENT_TYPE.equals(partition.getType()) 
						&& !ICPartitions.C_PREPROCESSOR.equals(partition.getType()))
					return;

				if (!validateEditorInputState())
					return;

				final char character = event.character;
				final char closingCharacter = getPeerCharacter(character);
				final StringBuffer buffer = new StringBuffer();
				buffer.append(character);
				buffer.append(closingCharacter);
				if (closingCharacter == '>' && nextToken != Symbols.TokenEOF
						&& document.getChar(offset + length) == '>') {
					// Insert a space to avoid two consequtive closing angular brackets. 
					buffer.append(' ');
				}

				document.replace(offset, length, buffer.toString());

				BracketLevel level = new BracketLevel();
				fBracketLevelStack.push(level);

				LinkedPositionGroup group = new LinkedPositionGroup();
				group.addPosition(new LinkedPosition(document, offset + 1, 0, LinkedPositionGroup.NO_STOP));

				LinkedModeModel model = new LinkedModeModel();
				model.addLinkingListener(this);
				model.addGroup(group);
				model.forceInstall();

				level.fOffset = offset;
				level.fLength = 2;

				// set up position tracking for our magic peers
				if (fBracketLevelStack.size() == 1) {
					document.addPositionCategory(CATEGORY);
					document.addPositionUpdater(fUpdater);
				}
				level.fFirstPosition = new Position(offset, 1);
				level.fSecondPosition = new Position(offset + 1, 1);
				document.addPosition(CATEGORY, level.fFirstPosition);
				document.addPosition(CATEGORY, level.fSecondPosition);

				level.fUI = new EditorLinkedModeUI(model, sourceViewer);
				level.fUI.setSimpleMode(true);
				level.fUI.setExitPolicy(new ExitPolicy(closingCharacter, getEscapeCharacter(closingCharacter), fBracketLevelStack));
				level.fUI.setExitPosition(sourceViewer, offset + 2, 0, Integer.MAX_VALUE);
				level.fUI.setCyclingMode(LinkedModeUI.CYCLE_NEVER);
				level.fUI.enter();

				IRegion newSelection = level.fUI.getSelectedRegion();
				sourceViewer.setSelectedRange(newSelection.getOffset(), newSelection.getLength());

				event.doit = false;

			} catch (BadLocationException e) {
				CUIPlugin.getDefault().log(e);
			} catch (BadPositionCategoryException e) {
				CUIPlugin.getDefault().log(e);
			}
		}

		/*
		 * @see org.eclipse.jface.text.link.ILinkedModeListener#left(org.eclipse.jface.text.link.LinkedModeModel, int)
		 */
		public void left(LinkedModeModel environment, int flags) {

			final BracketLevel level = (BracketLevel) fBracketLevelStack.pop();

			if (flags != ILinkedModeListener.EXTERNAL_MODIFICATION)
				return;

			// remove brackets
			final ISourceViewer sourceViewer = getSourceViewer();
			final IDocument document = sourceViewer.getDocument();
			if (document instanceof IDocumentExtension) {
				IDocumentExtension extension = (IDocumentExtension) document;
				extension.registerPostNotificationReplace(null, new IDocumentExtension.IReplace() {

					public void perform(IDocument d, IDocumentListener owner) {
						if ((level.fFirstPosition.isDeleted || level.fFirstPosition.length == 0)
								&& !level.fSecondPosition.isDeleted
								&& level.fSecondPosition.offset == level.fFirstPosition.offset)
						{
							try {
								document.replace(level.fSecondPosition.offset,
												 level.fSecondPosition.length,
												 null);
							} catch (BadLocationException e) {
								CUIPlugin.getDefault().log(e);
							}
						}

						if (fBracketLevelStack.size() == 0) {
							document.removePositionUpdater(fUpdater);
							try {
								document.removePositionCategory(CATEGORY);
							} catch (BadPositionCategoryException e) {
								CUIPlugin.getDefault().log(e);
							}
						}
					}
				});
			}
		}

		/*
		 * @see org.eclipse.jface.text.link.ILinkedModeListener#suspend(org.eclipse.jface.text.link.LinkedModeModel)
		 */
		public void suspend(LinkedModeModel environment) {
		}

		/*
		 * @see org.eclipse.jface.text.link.ILinkedModeListener#resume(org.eclipse.jface.text.link.LinkedModeModel, int)
		 */
		public void resume(LinkedModeModel environment, int flags) {
		}
	}

	/**
	 * Updates the C outline page selection and this editor's range indicator.
	 * 
	 * @since 3.0
	 */
	private class EditorSelectionChangedListener extends AbstractSelectionChangedListener {
		
		/**
		 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
		 */
		public void selectionChanged(SelectionChangedEvent event) {
			// XXX: see https://bugs.eclipse.org/bugs/show_bug.cgi?id=56161
			CEditor.this.selectionChanged();
		}
	}
	
 	/**
	 * Information provider used to present focusable information shells.
	 * 
	 * @since 3.1.1
	 */
	private static final class InformationProvider implements
			IInformationProvider, IInformationProviderExtension,
			IInformationProviderExtension2 {

		private IRegion fHoverRegion;
		private Object fHoverInfo;
		private IInformationControlCreator fControlCreator;

		InformationProvider(IRegion hoverRegion, Object hoverInfo,
				IInformationControlCreator controlCreator) {
			fHoverRegion = hoverRegion;
			fHoverInfo = hoverInfo;
			fControlCreator = controlCreator;
		}

		/*
		 * @see org.eclipse.jface.text.information.IInformationProvider#getSubject(org.eclipse.jface.text.ITextViewer,
		 *      int)
		 */
		public IRegion getSubject(ITextViewer textViewer, int invocationOffset) {
			return fHoverRegion;
		}

		/*
		 * @see org.eclipse.jface.text.information.IInformationProvider#getInformation(org.eclipse.jface.text.ITextViewer,
		 *      org.eclipse.jface.text.IRegion)
		 */
		public String getInformation(ITextViewer textViewer, IRegion subject) {
			return fHoverInfo.toString();
		}

		/*
		 * @see org.eclipse.jface.text.information.IInformationProviderExtension#getInformation2(org.eclipse.jface.text.ITextViewer,
		 *      org.eclipse.jface.text.IRegion)
		 */
		public Object getInformation2(ITextViewer textViewer, IRegion subject) {
			return fHoverInfo;
		}

		/*
		 * @see org.eclipse.jface.text.information.IInformationProviderExtension2#getInformationPresenterControlCreator()
		 */
		public IInformationControlCreator getInformationPresenterControlCreator() {
			return fControlCreator;
		}
	}

	/**
	 * This action behaves in two different ways: If there is no current text
	 * hover, the tooltip is displayed using information presenter. If there is
	 * a current text hover, it is converted into a information presenter in
	 * order to make it sticky.
	 * 
	 * @since 3.1.1
	 */
	class InformationDispatchAction extends TextEditorAction {

		/** The wrapped text operation action. */
		private final TextOperationAction fTextOperationAction;

		/**
		 * Creates a dispatch action.
		 * 
		 * @param resourceBundle
		 *            the resource bundle
		 * @param prefix
		 *            the prefix
		 * @param textOperationAction
		 *            the text operation action
		 */
		public InformationDispatchAction(ResourceBundle resourceBundle,
				String prefix, final TextOperationAction textOperationAction) {
			super(resourceBundle, prefix, CEditor.this);
			if (textOperationAction == null)
				throw new IllegalArgumentException();
			fTextOperationAction = textOperationAction;
		}

		/*
		 * @see org.eclipse.jface.action.IAction#run()
		 */
		public void run() {

			ISourceViewer sourceViewer = getSourceViewer();
			if (sourceViewer == null) {
				fTextOperationAction.run();
				return;
			}

			if (sourceViewer instanceof ITextViewerExtension4) {
				ITextViewerExtension4 extension4 = (ITextViewerExtension4) sourceViewer;
				if (extension4.moveFocusToWidgetToken())
					return;
			}

			if (sourceViewer instanceof ITextViewerExtension2) {
				// does a text hover exist?
				ITextHover textHover = ((ITextViewerExtension2) sourceViewer)
						.getCurrentTextHover();
				if (textHover != null
						&& makeTextHoverFocusable(sourceViewer, textHover))
					return;
			}

			if (sourceViewer instanceof ISourceViewerExtension3) {
				// does an annotation hover exist?
				IAnnotationHover annotationHover = ((ISourceViewerExtension3) sourceViewer)
						.getCurrentAnnotationHover();
				if (annotationHover != null
						&& makeAnnotationHoverFocusable(sourceViewer,
								annotationHover))
					return;
			}

			// otherwise, just display the tooltip
			// fTextOperationAction.run();
		}

		/**
		 * Tries to make a text hover focusable (or "sticky").
		 * 
		 * @param sourceViewer
		 *            the source viewer to display the hover over
		 * @param textHover
		 *            the hover to make focusable
		 * @return <code>true</code> if successful, <code>false</code>
		 *         otherwise
		 */
		private boolean makeTextHoverFocusable(ISourceViewer sourceViewer,
				ITextHover textHover) {
			Point hoverEventLocation = ((ITextViewerExtension2) sourceViewer)
					.getHoverEventLocation();
			int offset = computeOffsetAtLocation(sourceViewer,
					hoverEventLocation.x, hoverEventLocation.y);
			if (offset == -1)
				return false;

			try {
				IRegion hoverRegion = textHover.getHoverRegion(sourceViewer,
						offset);
				if (hoverRegion == null)
					return false;

				String hoverInfo = textHover.getHoverInfo(sourceViewer,
						hoverRegion);

				IInformationControlCreator controlCreator = null;
				if (textHover instanceof IInformationProviderExtension2)
					controlCreator = ((IInformationProviderExtension2) textHover)
							.getInformationPresenterControlCreator();

				IInformationProvider informationProvider = new InformationProvider(
						hoverRegion, hoverInfo, controlCreator);

				fInformationPresenter.setOffset(offset);
				fInformationPresenter
						.setAnchor(AbstractInformationControlManager.ANCHOR_BOTTOM);
				fInformationPresenter.setMargins(6, 6); // default values from
														// AbstractInformationControlManager
				String contentType = TextUtilities.getContentType(sourceViewer
						.getDocument(), ICPartitions.C_PARTITIONING, offset,
						true);
				fInformationPresenter.setInformationProvider(
						informationProvider, contentType);
				fInformationPresenter.showInformation();

				return true;

			} catch (BadLocationException e) {
				return false;
			}
		}

		/**
		 * Tries to make an annotation hover focusable (or "sticky").
		 * 
		 * @param sourceViewer
		 *            the source viewer to display the hover over
		 * @param annotationHover
		 *            the hover to make focusable
		 * @return <code>true</code> if successful, <code>false</code>
		 *         otherwise
		 */
		private boolean makeAnnotationHoverFocusable(
				ISourceViewer sourceViewer, IAnnotationHover annotationHover) {
			IVerticalRulerInfo info = getVerticalRuler();
			int line = info.getLineOfLastMouseButtonActivity();
			if (line == -1)
				return false;

			try {

				// compute the hover information
				Object hoverInfo;
				if (annotationHover instanceof IAnnotationHoverExtension) {
					IAnnotationHoverExtension extension = (IAnnotationHoverExtension) annotationHover;
					ILineRange hoverLineRange = extension.getHoverLineRange(
							sourceViewer, line);
					if (hoverLineRange == null)
						return false;
					final int maxVisibleLines = Integer.MAX_VALUE; // allow any
																	// number of
																	// lines
																	// being
																	// displayed,
																	// as we
																	// support
																	// scrolling
					hoverInfo = extension.getHoverInfo(sourceViewer,
							hoverLineRange, maxVisibleLines);
				} else {
					hoverInfo = annotationHover
							.getHoverInfo(sourceViewer, line);
				}

				// hover region: the beginning of the concerned line to place
				// the control right over the line
				IDocument document = sourceViewer.getDocument();
				int offset = document.getLineOffset(line);
				String contentType = TextUtilities.getContentType(document,
						ICPartitions.C_PARTITIONING, offset, true);

				IInformationControlCreator controlCreator = null;

				/*
				 * XXX: This is a hack to avoid API changes at the end of 3.2,
				 * and should be fixed for 3.3, see:
				 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=137967
				 */
				if ("org.eclipse.jface.text.source.projection.ProjectionAnnotationHover".equals(annotationHover.getClass().getName())) { //$NON-NLS-1$
					controlCreator = new IInformationControlCreator() {
						public IInformationControl createInformationControl(
								Shell shell) {
							int shellStyle = SWT.RESIZE | SWT.TOOL
									| getOrientation();
							int style = SWT.V_SCROLL | SWT.H_SCROLL;
							return new SourceViewerInformationControl(shell,
									shellStyle, style);
						}
					};

				} else {
					if (annotationHover instanceof IInformationProviderExtension2)
						controlCreator = ((IInformationProviderExtension2) annotationHover)
								.getInformationPresenterControlCreator();
					else if (annotationHover instanceof IAnnotationHoverExtension)
						controlCreator = ((IAnnotationHoverExtension) annotationHover)
								.getHoverControlCreator();
				}

				IInformationProvider informationProvider = new InformationProvider(
						new Region(offset, 0), hoverInfo, controlCreator);

				fInformationPresenter.setOffset(offset);
				fInformationPresenter
						.setAnchor(AbstractInformationControlManager.ANCHOR_RIGHT);
				fInformationPresenter.setMargins(4, 0); // AnnotationBarHoverManager
														// sets (5,0), minus
														// SourceViewer.GAP_SIZE_1
				fInformationPresenter.setInformationProvider(
						informationProvider, contentType);
				fInformationPresenter.showInformation();

				return true;

			} catch (BadLocationException e) {
				return false;
			}
		}

		// modified version from TextViewer
		private int computeOffsetAtLocation(ITextViewer textViewer, int x, int y) {

			StyledText styledText = textViewer.getTextWidget();
			IDocument document = textViewer.getDocument();

			if (document == null)
				return -1;

			try {
				int widgetOffset = styledText.getOffsetAtLocation(new Point(x,
						y));
				Point p = styledText.getLocationAtOffset(widgetOffset);
				if (p.x > x)
					widgetOffset--;

				if (textViewer instanceof ITextViewerExtension5) {
					ITextViewerExtension5 extension = (ITextViewerExtension5) textViewer;
					return extension.widgetOffset2ModelOffset(widgetOffset);
				} else {
					IRegion visibleRegion = textViewer.getVisibleRegion();
					return widgetOffset + visibleRegion.getOffset();
				}
			} catch (IllegalArgumentException e) {
				return -1;
			}
		}
	}
	
	/**
	 * Text navigation action to navigate to the next sub-word.
	 *
	 * @since 4.0
	 */
	protected abstract class NextSubWordAction extends TextNavigationAction {

		protected CWordIterator fIterator = new CWordIterator();

		/**
		 * Creates a new next sub-word action.
		 *
		 * @param code Action code for the default operation. Must be an action code from @see org.eclipse.swt.custom.ST.
		 */
		protected NextSubWordAction(int code) {
			super(getSourceViewer().getTextWidget(), code);
		}

		/*
		 * @see org.eclipse.jface.action.IAction#run()
		 */
		public void run() {
			// Check whether sub word navigation is enabled.
			final IPreferenceStore store = getPreferenceStore();
			if (!store.getBoolean(SUB_WORD_NAVIGATION)) {
				super.run();
				return;
			}

			final ISourceViewer viewer = getSourceViewer();
			final IDocument document = viewer.getDocument();
			fIterator.setText((CharacterIterator) new DocumentCharacterIterator(document));
			int position = widgetOffset2ModelOffset(viewer, viewer.getTextWidget().getCaretOffset());
			if (position == -1)
				return;

			int next = findNextPosition(position);
			if (next != BreakIterator.DONE) {
				setCaretPosition(next);
				getTextWidget().showSelection();
				fireSelectionChanged();
			}
		}

		/**
		 * Finds the next position after the given position.
		 *
		 * @param position the current position
		 * @return the next position
		 */
		protected int findNextPosition(int position) {
			ISourceViewer viewer = getSourceViewer();
			int widget = -1;
			while (position != BreakIterator.DONE && widget == -1) { // TODO: optimize
				position = fIterator.following(position);
				if (position != BreakIterator.DONE)
					widget = modelOffset2WidgetOffset(viewer, position);
			}
			return position;
		}

		/**
		 * Sets the caret position to the sub-word boundary given with <code>position</code>.
		 *
		 * @param position Position where the action should move the caret
		 */
		protected abstract void setCaretPosition(int position);
	}

	/**
	 * Text navigation action to navigate to the next sub-word.
	 *
	 * @since 4.0
	 */
	protected class NavigateNextSubWordAction extends NextSubWordAction {

		/**
		 * Creates a new navigate next sub-word action.
		 */
		public NavigateNextSubWordAction() {
			super(ST.WORD_NEXT);
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.CEditor.NextSubWordAction#setCaretPosition(int)
		 */
		protected void setCaretPosition(final int position) {
			getTextWidget().setCaretOffset(modelOffset2WidgetOffset(getSourceViewer(), position));
		}
	}

	/**
	 * Text operation action to delete the next sub-word.
	 *
	 * @since 4.0
	 */
	protected class DeleteNextSubWordAction extends NextSubWordAction implements IUpdate {

		/**
		 * Creates a new delete next sub-word action.
		 */
		public DeleteNextSubWordAction() {
			super(ST.DELETE_WORD_NEXT);
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.CEditor.NextSubWordAction#setCaretPosition(int)
		 */
		protected void setCaretPosition(final int position) {
			if (!validateEditorInputState())
				return;

			final ISourceViewer viewer = getSourceViewer();
			final int caret, length;
			Point selection = viewer.getSelectedRange();
			if (selection.y != 0) {
				caret = selection.x;
				length = selection.y;
			} else {
				caret = widgetOffset2ModelOffset(viewer, viewer.getTextWidget().getCaretOffset());
				length = position - caret;
			}

			try {
				viewer.getDocument().replace(caret, length, ""); //$NON-NLS-1$
			} catch (BadLocationException exception) {
				// Should not happen
			}
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.CEditor.NextSubWordAction#findNextPosition(int)
		 */
		protected int findNextPosition(int position) {
			return fIterator.following(position);
		}

		/*
		 * @see org.eclipse.ui.texteditor.IUpdate#update()
		 */
		public void update() {
			setEnabled(isEditorInputModifiable());
		}
	}

	/**
	 * Text operation action to select the next sub-word.
	 *
	 * @since 4.0
	 */
	protected class SelectNextSubWordAction extends NextSubWordAction {

		/**
		 * Creates a new select next sub-word action.
		 */
		public SelectNextSubWordAction() {
			super(ST.SELECT_WORD_NEXT);
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.CEditor.NextSubWordAction#setCaretPosition(int)
		 */
		protected void setCaretPosition(final int position) {
			final ISourceViewer viewer = getSourceViewer();

			final StyledText text = viewer.getTextWidget();
			if (text != null && !text.isDisposed()) {

				final Point selection = text.getSelection();
				final int caret = text.getCaretOffset();
				final int offset = modelOffset2WidgetOffset(viewer, position);

				if (caret == selection.x)
					text.setSelectionRange(selection.y, offset - selection.y);
				else
					text.setSelectionRange(selection.x, offset - selection.x);
			}
		}
	}

	/**
	 * Text navigation action to navigate to the previous sub-word.
	 *
	 * @since 4.0
	 */
	protected abstract class PreviousSubWordAction extends TextNavigationAction {

		protected CWordIterator fIterator = new CWordIterator();

		/**
		 * Creates a new previous sub-word action.
		 *
		 * @param code Action code for the default operation. Must be an action code from @see org.eclipse.swt.custom.ST.
		 */
		protected PreviousSubWordAction(final int code) {
			super(getSourceViewer().getTextWidget(), code);
		}

		/*
		 * @see org.eclipse.jface.action.IAction#run()
		 */
		public void run() {
			// Check whether sub word navigation is enabled.
			final IPreferenceStore store = getPreferenceStore();
			if (!store.getBoolean(SUB_WORD_NAVIGATION)) {
				super.run();
				return;
			}

			final ISourceViewer viewer = getSourceViewer();
			final IDocument document = viewer.getDocument();
			fIterator.setText((CharacterIterator) new DocumentCharacterIterator(document));
			int position = widgetOffset2ModelOffset(viewer, viewer.getTextWidget().getCaretOffset());
			if (position == -1)
				return;

			int previous = findPreviousPosition(position);
			if (previous != BreakIterator.DONE) {
				setCaretPosition(previous);
				getTextWidget().showSelection();
				fireSelectionChanged();
			}
		}

		/**
		 * Finds the previous position before the given position.
		 *
		 * @param position the current position
		 * @return the previous position
		 */
		protected int findPreviousPosition(int position) {
			ISourceViewer viewer = getSourceViewer();
			int widget = -1;
			while (position != BreakIterator.DONE && widget == -1) { // TODO: optimize
				position = fIterator.preceding(position);
				if (position != BreakIterator.DONE)
					widget = modelOffset2WidgetOffset(viewer, position);
			}
			return position;
		}

		/**
		 * Sets the caret position to the sub-word boundary given with <code>position</code>.
		 *
		 * @param position Position where the action should move the caret
		 */
		protected abstract void setCaretPosition(int position);
	}

	/**
	 * Text navigation action to navigate to the previous sub-word.
	 *
	 * @since 4.0
	 */
	protected class NavigatePreviousSubWordAction extends PreviousSubWordAction {

		/**
		 * Creates a new navigate previous sub-word action.
		 */
		public NavigatePreviousSubWordAction() {
			super(ST.WORD_PREVIOUS);
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.CEditor.PreviousSubWordAction#setCaretPosition(int)
		 */
		protected void setCaretPosition(final int position) {
			getTextWidget().setCaretOffset(modelOffset2WidgetOffset(getSourceViewer(), position));
		}
	}

	/**
	 * Text operation action to delete the previous sub-word.
	 *
	 * @since 4.0
	 */
	protected class DeletePreviousSubWordAction extends PreviousSubWordAction implements IUpdate {

		/**
		 * Creates a new delete previous sub-word action.
		 */
		public DeletePreviousSubWordAction() {
			super(ST.DELETE_WORD_PREVIOUS);
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.CEditor.PreviousSubWordAction#setCaretPosition(int)
		 */
		protected void setCaretPosition(int position) {
			if (!validateEditorInputState())
				return;

			final int length;
			final ISourceViewer viewer = getSourceViewer();
			Point selection = viewer.getSelectedRange();
			if (selection.y != 0) {
				position = selection.x;
				length = selection.y;
			} else {
				length = widgetOffset2ModelOffset(viewer, viewer.getTextWidget().getCaretOffset()) - position;
			}

			try {
				viewer.getDocument().replace(position, length, ""); //$NON-NLS-1$
			} catch (BadLocationException exception) {
				// Should not happen
			}
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.CEditor.PreviousSubWordAction#findPreviousPosition(int)
		 */
		protected int findPreviousPosition(int position) {
			return fIterator.preceding(position);
		}

		/*
		 * @see org.eclipse.ui.texteditor.IUpdate#update()
		 */
		public void update() {
			setEnabled(isEditorInputModifiable());
		}
	}

	/**
	 * Text operation action to select the previous sub-word.
	 *
	 * @since 4.0
	 */
	protected class SelectPreviousSubWordAction extends PreviousSubWordAction {

		/**
		 * Creates a new select previous sub-word action.
		 */
		public SelectPreviousSubWordAction() {
			super(ST.SELECT_WORD_PREVIOUS);
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.CEditor.PreviousSubWordAction#setCaretPosition(int)
		 */
		protected void setCaretPosition(final int position) {
			final ISourceViewer viewer = getSourceViewer();

			final StyledText text = viewer.getTextWidget();
			if (text != null && !text.isDisposed()) {

				final Point selection = text.getSelection();
				final int caret = text.getCaretOffset();
				final int offset = modelOffset2WidgetOffset(viewer, position);

				if (caret == selection.x)
					text.setSelectionRange(selection.y, offset - selection.y);
				else
					text.setSelectionRange(selection.x, offset - selection.x);
			}
		}
	}
	

	/**
	 * The information provider used to present focusable information
	 * shells.
	 */
	private InformationPresenter fInformationPresenter;
	
	/**
	 * The editor selection changed listener.
	 * 
	 * @since 3.0
	 */
	private EditorSelectionChangedListener fEditorSelectionChangedListener;

	/** The outline page */
	protected CContentOutlinePage fOutlinePage;
	
	/** Search actions **/
	private ActionGroup fSelectionSearchGroup;
	private ActionGroup fTextSearchGroup;
	private CRefactoringActionGroup fRefactoringActionGroup;
	private ActionGroup fOpenInViewGroup;

	/** Generate action group filling the "Source" submenu */
	private GenerateActionGroup fGenerateActionGroup;

    /** Action which shows selected element in CView. */
	private ShowInCViewAction fShowInCViewAction;
	
	/** Activity Listeners **/
	protected ISelectionChangedListener fStatusLineClearer;
    protected ISelectionChangedListener fSelectionUpdateListener;
	
	/** Pairs of brackets, used to match. */
    protected final static char[] BRACKETS = { '{', '}', '(', ')', '[', ']', '<', '>' };

	/** Matches the brackets. */
    protected CPairMatcher fBracketMatcher = new CPairMatcher(BRACKETS);

	/** The bracket inserter. */
	private BracketInserter fBracketInserter = new BracketInserter();

	/** The editor's tab converter */
	private TabConverter fTabConverter;

	/** Listener to annotation model changes that updates the error tick in the tab image */
	private CEditorErrorTickUpdater fCEditorErrorTickUpdater;

	/** Preference key for sub-word navigation, aka smart caret positioning */
	public final static String SUB_WORD_NAVIGATION = "subWordNavigation"; //$NON-NLS-1$
	/** Preference key for matching brackets */
	public final static String MATCHING_BRACKETS = "matchingBrackets"; //$NON-NLS-1$
	/** Preference key for matching brackets color */
	public final static String MATCHING_BRACKETS_COLOR = "matchingBracketsColor"; //$NON-NLS-1$
	/** Preference key for inactive code painter enablement */
	public static final String INACTIVE_CODE_ENABLE = "inactiveCodeEnable"; //$NON-NLS-1$
	/** Preference key for inactive code painter color */
	public static final String INACTIVE_CODE_COLOR = "inactiveCodeColor"; //$NON-NLS-1$
	/** Preference key for code formatter tab size */
	private final static String CODE_FORMATTER_TAB_SIZE = DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE;
	/** Preference key for inserting spaces rather than tabs */
	public final static String SPACES_FOR_TABS = DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR;
	/** Preference key for automatically closing strings */
	private final static String CLOSE_STRINGS = PreferenceConstants.EDITOR_CLOSE_STRINGS;
	/** Preference key for automatically closing brackets and parenthesis */
	private final static String CLOSE_BRACKETS = PreferenceConstants.EDITOR_CLOSE_BRACKETS;
	/** Preference key for automatically closing angular brackets */
	private final static String CLOSE_ANGULAR_BRACKETS = PreferenceConstants.EDITOR_CLOSE_ANGULAR_BRACKETS;

    /** Preference key for compiler task tags */
    private final static String TRANSLATION_TASK_TAGS = CCorePreferenceConstants.TRANSLATION_TASK_TAGS;

	/** 
	 * This editor's projection support 
	 */
	private ProjectionSupport fProjectionSupport;
	/** 
	 * This editor's projection model updater 
	 */
	private ICFoldingStructureProvider fProjectionModelUpdater;

	/**
	 * The action group for folding.
	 */
	private FoldingActionGroup fFoldingGroup;

	/**
	 * AST reconciling listeners.
	 * @since 4.0
	 */
	private ListenerList fReconcilingListeners= new ListenerList(ListenerList.IDENTITY);

	/**
	 * Semantic highlighting manager
	 * @since 4.0
	 */
	private SemanticHighlightingManager fSemanticManager;


	/**
	 * Default constructor.
	 */
	public CEditor() {
		super();
	}

	/**
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#initializeEditor()
	 */
	protected void initializeEditor() {
		IPreferenceStore store= CUIPlugin.getDefault().getCombinedPreferenceStore();
		setPreferenceStore(store);
		CTextTools textTools = CUIPlugin.getDefault().getTextTools();
		setSourceViewerConfiguration(new CSourceViewerConfiguration(textTools.getColorManager(), store, this, textTools.getDocumentPartitioning()));
		setDocumentProvider(CUIPlugin.getDefault().getDocumentProvider());
	
		setEditorContextMenuId("#CEditorContext"); //$NON-NLS-1$
		setRulerContextMenuId("#CEditorRulerContext"); //$NON-NLS-1$
		setOutlinerContextMenuId("#CEditorOutlinerContext"); //$NON-NLS-1$

		fCEditorErrorTickUpdater = new CEditorErrorTickUpdater(this);          
	}

	/**
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#doSetInput(org.eclipse.ui.IEditorInput)
	 */
	protected void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);
		setOutlinePageInput(fOutlinePage, input);

		if (fProjectionModelUpdater != null) {
			fProjectionModelUpdater.initialize();
		}
		if (fCEditorErrorTickUpdater != null) {
			fCEditorErrorTickUpdater.updateEditorImage(getInputCElement());
		}
	}

	/**
	 * Update the title image.
     * @param image Title image.
	 */
	public void updatedTitleImage(Image image) {
		setTitleImage(image);
	}

	/**
	 * Returns the C element wrapped by this editors input.
	 *
	 * @return the C element wrapped by this editors input.
	 * @since 3.0
	 */
	public ICElement getInputCElement () {
		return CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(getEditorInput());
	}

	/**
	 * Gets the current IFile input.
	 * This method will be remove after cdt-3.0.
	 * We can not guaranty that the input is an IFile, it may
	 * an external file.  Clients should test for <code>null<code> or use getInputCElement()
	 * @deprecated use <code>CEditor.getInputCElement()</code>.
     * @return IFile Input file or null if input is not and IFileEditorInput.
	 */
	public IFile getInputFile() {		
		IEditorInput editorInput = getEditorInput();
		if (editorInput != null) {
			if ((editorInput instanceof IFileEditorInput)) {
				return ((IFileEditorInput) editorInput).getFile();
			}
		}
		return null;
	}

	/**
     * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
	 */
    public boolean isSaveAsAllowed() {
		return true;
	}
	/**
	 * Gets the outline page of the c-editor.
     * @return Outline page.
	 */
	public CContentOutlinePage getOutlinePage() {
		if (fOutlinePage == null) {
			fOutlinePage = new CContentOutlinePage(this);
			fOutlinePage.addSelectionChangedListener(this);
		}
		setOutlinePageInput(fOutlinePage, getEditorInput());
		return fOutlinePage;
	}

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class required) {
		if (IContentOutlinePage.class.equals(required)) {
			return getOutlinePage();
		}
		if (required == IShowInTargetList.class) {
			return new IShowInTargetList() {
				public String[] getShowInTargetIds() {
					return new String[] { CUIPlugin.CVIEW_ID, IPageLayout.ID_OUTLINE, IPageLayout.ID_RES_NAV };
				}

			};
		}
		if (required == IShowInSource.class) {
			ICElement ce= null;
			try {
				ce= SelectionConverter.getElementAtOffset(this);
			} catch (CModelException ex) {
				ce= null;
			}
			if (ce != null) { 
				final ISelection selection= new StructuredSelection(ce);
				return new IShowInSource() {
					public ShowInContext getShowInContext() {
						return new ShowInContext(getEditorInput(), selection);
					}
				};
			}
		}
		if (ProjectionAnnotationModel.class.equals(required)) {
			if (fProjectionSupport != null) {
				Object adapter = fProjectionSupport.getAdapter(getSourceViewer(), required);
				if (adapter != null)
					return adapter;
			}
		}
		return super.getAdapter(required);
	}
	/**
	 * Handles a property change event describing a change
	 * of the editor's preference store and updates the preference
	 * related editor properties.
	 * 
	 * @param event the property change event
	 */
	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
		String property = event.getProperty();

		if (AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH.equals(property)) {
			/*
			 * Ignore tab setting since we rely on the formatter preferences.
			 * We do this outside the try-finally block to avoid that EDITOR_TAB_WIDTH
			 * is handled by the sub-class (AbstractDecoratedTextEditor).
			 */
			return;
		}

		try {
			AdaptedSourceViewer asv = (AdaptedSourceViewer) getSourceViewer();

			if (asv != null) {

				if (CLOSE_BRACKETS.equals(property)) {
					fBracketInserter.setCloseBracketsEnabled(getPreferenceStore().getBoolean(property));
					return;
				}

				if (CLOSE_ANGULAR_BRACKETS.equals(property)) {
					fBracketInserter.setCloseAngularBracketsEnabled(getPreferenceStore().getBoolean(property));
					return;
				}
				
				if (CLOSE_STRINGS.equals(property)) {
					fBracketInserter.setCloseStringsEnabled(getPreferenceStore().getBoolean(property));
					return;
				}

				if (SPACES_FOR_TABS.equals(property)) {
					if (isTabConversionEnabled())
						startTabConversion();
					else
						stopTabConversion();
					return;
				}
				
				if (PreferenceConstants.EDITOR_TEXT_HOVER_MODIFIERS.equals(property))
					updateHoverBehavior();

				((CSourceViewerConfiguration)getSourceViewerConfiguration()).handlePropertyChangeEvent(event);

				if (PreferenceConstants.EDITOR_SMART_TAB.equals(property)) {
					if (getPreferenceStore().getBoolean(property)) {
						setActionActivationCode("IndentOnTab", '\t', -1, SWT.NONE); //$NON-NLS-1$
					} else {
						removeActionActivationCode("IndentOnTab"); //$NON-NLS-1$
					}
				}

				if (CODE_FORMATTER_TAB_SIZE.equals(property)) {
					asv.updateIndentationPrefixes();
					if (fTabConverter != null)
						fTabConverter.setNumberOfSpacesPerTab(getTabSize());
				}

				// Not implemented ... for the future.
				if (TRANSLATION_TASK_TAGS.equals(event.getProperty())) {
					ISourceViewer sourceViewer = getSourceViewer();
					if (sourceViewer != null && affectsTextPresentation(event))
						sourceViewer.invalidateTextPresentation();
				}

				if (PreferenceConstants.EDITOR_FOLDING_PROVIDER.equals(property)) {
					if (fProjectionModelUpdater != null) {
						fProjectionModelUpdater.uninstall();
					}
					// either freshly enabled or provider changed
					fProjectionModelUpdater = CUIPlugin.getDefault().getFoldingStructureProviderRegistry().getCurrentFoldingProvider();
					if (fProjectionModelUpdater != null) {
						fProjectionModelUpdater.install(this, asv);
					}
					return;
				}

				if (DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE.equals(property)
						|| DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE.equals(property)
						|| DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR.equals(property)) {
					StyledText textWidget= asv.getTextWidget();
					int tabWidth= getSourceViewerConfiguration().getTabWidth(asv);
					if (textWidget.getTabs() != tabWidth)
						textWidget.setTabs(tabWidth);
					return;
				}

				if (SemanticHighlightings.affectsEnablement(getPreferenceStore(), event)) {
					if (isSemanticHighlightingEnabled()) {
						installSemanticHighlighting();
						fSemanticManager.refresh();
					} else {
						uninstallSemanticHighlighting();
					}
					return;
				}

				IContentAssistant c = asv.getContentAssistant();
				if (c instanceof ContentAssistant) {
					ContentAssistPreference.changeConfiguration((ContentAssistant) c, getPreferenceStore(), event);
				}
			}
		} finally {
			super.handlePreferenceStoreChanged(event);
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#initializeViewerColors(org.eclipse.jface.text.source.ISourceViewer)
	 */
	protected void initializeViewerColors(ISourceViewer viewer) {
		// is handled by CSourceViewer
	}

	/*
	 * Update the hovering behavior depending on the preferences.
	 */
	private void updateHoverBehavior() {
		SourceViewerConfiguration configuration= getSourceViewerConfiguration();
		String[] types= configuration.getConfiguredContentTypes(getSourceViewer());

		for (int i= 0; i < types.length; i++) {

			String t= types[i];

			ISourceViewer sourceViewer= getSourceViewer();
			if (sourceViewer instanceof ITextViewerExtension2) {
				// Remove existing hovers
				((ITextViewerExtension2)sourceViewer).removeTextHovers(t);

				int[] stateMasks= configuration.getConfiguredTextHoverStateMasks(getSourceViewer(), t);

				if (stateMasks != null) {
					for (int j= 0; j < stateMasks.length; j++)	{
						int stateMask= stateMasks[j];
						ITextHover textHover= configuration.getTextHover(sourceViewer, t, stateMask);
						((ITextViewerExtension2)sourceViewer).setTextHover(textHover, t, stateMask);
					}
				} else {
					ITextHover textHover= configuration.getTextHover(sourceViewer, t);
					((ITextViewerExtension2)sourceViewer).setTextHover(textHover, t, ITextViewerExtension2.DEFAULT_HOVER_STATE_MASK);
				}
			} else
				sourceViewer.setTextHover(configuration.getTextHover(sourceViewer, t), t);
		}
	}

	/**
	 * React to changed selection.
	 * 
	 * @since 3.0
	 */
	protected void selectionChanged() {
		if (getSelectionProvider() == null)
			return;
		updateStatusLine();
	}

	/**
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		ISelection sel = event.getSelection();
		if (sel instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) sel;
			Object obj = selection.getFirstElement();
			if (obj instanceof ISourceReference) {
				try {
					ISourceRange range = ((ISourceReference) obj).getSourceRange();
					if (range != null) {
						setSelection(range, !isActivePart());
					}
				} catch (CModelException e) {
                    // Selection change not applied.
				}
			}
		}
	}

	/**
     * Sets selection for C element. 
     * @param element Element to select.
	 */
    public void setSelection(ICElement element) {

		if (element == null || element instanceof ITranslationUnit) {
			/*
			 * If the element is an ITranslationUnit this unit is either the input
			 * of this editor or not being displayed. In both cases, nothing should
			 * happened.
			 */
			return;
		}
		if (element instanceof ISourceReference) {
			ISourceReference reference = (ISourceReference) element;
			// set hightlight range
			setSelection(reference, true);
		}
	}

    /**
     * Sets selection for source reference.
     * @param element Source reference to set.
     * @param moveCursor Should cursor be moved.
     */
    public void setSelection(ISourceReference element, boolean moveCursor) {
		if (element != null) {
			StyledText  textWidget = null;
			
			ISourceViewer sourceViewer = getSourceViewer();
			if (sourceViewer != null)
				textWidget = sourceViewer.getTextWidget();
			
			if (textWidget == null)
				return;

			try {
				setSelection(element.getSourceRange(), moveCursor);
			} catch (CModelException e) {
                // Selection not applied.
			}
		}
	}

	/**
	 * Sets the current editor selection to the source range. Optionally
	 * sets the current editor position.
	 *
	 * @param element the source range to be shown in the editor, can be null.
	 * @param moveCursor if true the editor is scrolled to show the range.
	 */
	public void setSelection(ISourceRange element, boolean moveCursor) {

		if (element == null) {
			return;
		}

		try {
			IRegion alternateRegion = null;
			int start = element.getStartPos();
			int length = element.getLength();

			// Sanity check sometimes the parser may throw wrong numbers.
			if (start < 0 || length < 0) {
				start = 0;
				length = 0;
			}

			// 0 length and start and non-zero start line says we know
			// the line for some reason, but not the offset.
			if (length == 0 && start == 0 && element.getStartLine() > 0) {
				// We have the information in term of lines, we can work it out.
				// Binary elements return the first executable statement so we have to substract -1
				start = getDocumentProvider().getDocument(getEditorInput()).getLineOffset(element.getStartLine() - 1);
				if (element.getEndLine() > 0) {
					length = getDocumentProvider().getDocument(getEditorInput()).getLineOffset(element.getEndLine()) - start;
				} else {
					length = start;
				}
				// create an alternate region for the keyword highlight.
				alternateRegion = getDocumentProvider().getDocument(getEditorInput()).getLineInformation(element.getStartLine() - 1);
				if (start == length || length < 0) {
					if (alternateRegion != null) {
						start = alternateRegion.getOffset();
						length = alternateRegion.getLength();
					}
				}
			}
			setHighlightRange(start, length, moveCursor);

			if (moveCursor) {
				start = element.getIdStartPos();
				length = element.getIdLength();
				if (start == 0 && length == 0 && alternateRegion != null) {
					start = alternateRegion.getOffset();
					length = alternateRegion.getLength();
				}
				if (start > -1 && getSourceViewer() != null) {
					getSourceViewer().revealRange(start, length);
					getSourceViewer().setSelectedRange(start, length);
				}
				updateStatusField(CTextEditorActionConstants.STATUS_CURSOR_POS);
			}
			return;
		} catch (IllegalArgumentException x) {
            // No information to the user
		} catch (BadLocationException e) {
            // No information to the user
		}

		if (moveCursor)
			resetHighlightRange();
	}

	/**
     * Checks is the editor active part. 
     * @return <code>true</code> if editor is the active part of the workbench.
	 */
    private boolean isActivePart() {
		IWorkbenchWindow window = getSite().getWorkbenchWindow();
		IPartService service = window.getPartService();
		return (this == service.getActivePart());
	}

    /**
     * @see org.eclipse.ui.IWorkbenchPart#dispose()
     */
    public void dispose() {

		ISourceViewer sourceViewer = getSourceViewer();
		if (sourceViewer instanceof ITextViewerExtension)
			((ITextViewerExtension) sourceViewer).removeVerifyKeyListener(fBracketInserter);

		if (fProjectionModelUpdater != null) {
			fProjectionModelUpdater.uninstall();
			fProjectionModelUpdater = null;
		}
		
		if (fProjectionSupport != null) {
			fProjectionSupport.dispose();
			fProjectionSupport = null;
		}

		if (fCEditorErrorTickUpdater != null) {
			fCEditorErrorTickUpdater.dispose();
			fCEditorErrorTickUpdater = null;
		}
		
        if (fSelectionUpdateListener != null) {
			getSelectionProvider().addSelectionChangedListener(fSelectionUpdateListener);
			fSelectionUpdateListener = null;
        }
        
       	if (fStatusLineClearer != null) {
			ISelectionProvider provider = getSelectionProvider();
       		provider.removeSelectionChangedListener(fStatusLineClearer);
			fStatusLineClearer = null;
		}
        
        if (fBracketMatcher != null) {
			fBracketMatcher.dispose();
			fBracketMatcher = null;
		}
		
		if (fOutlinePage != null) {
			fOutlinePage.dispose();
			fOutlinePage = null;
		}
		
		if (fShowInCViewAction != null) {
			fShowInCViewAction.dispose();
			fShowInCViewAction = null;
		}
		
		if (fSelectionSearchGroup != null) {
			fSelectionSearchGroup.dispose();
			fSelectionSearchGroup = null;
		}

		if (fTextSearchGroup != null) {
			fTextSearchGroup.dispose();
			fTextSearchGroup = null;
		}

		if (fRefactoringActionGroup != null) {
			fRefactoringActionGroup.dispose();
			fRefactoringActionGroup = null;
		}

		if (fOpenInViewGroup != null) {
			fOpenInViewGroup.dispose();
			fOpenInViewGroup = null;
		}

		if (fGenerateActionGroup != null) {
			fGenerateActionGroup.dispose();
			fGenerateActionGroup= null;
		}
		
		if (fEditorSelectionChangedListener != null)  {
			fEditorSelectionChangedListener.uninstall(getSelectionProvider());
			fEditorSelectionChangedListener = null;
		}

		stopTabConversion();
		
		super.dispose();
	}

	/**
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#canHandleMove(org.eclipse.ui.IEditorInput, org.eclipse.ui.IEditorInput)
	 */
	protected boolean canHandleMove(IEditorInput originalElement, IEditorInput movedElement) {
		String oldLanguage = ""; //$NON-NLS-1$
		if (originalElement instanceof IFileEditorInput) {
			IFile file = ((IFileEditorInput) originalElement).getFile();
			if (file != null) {
				IContentType type = CCorePlugin.getContentType(file.getProject(), file.getName());
				if (type != null) {
					oldLanguage = type.getId();
				}
				if (oldLanguage == null) {
					return false;
				}
			}
		}

		String newLanguage = ""; //$NON-NLS-1$
		if (movedElement instanceof IFileEditorInput) {
			IFile file = ((IFileEditorInput) movedElement).getFile();
			if (file != null) {
				IContentType type = CCorePlugin.getContentType(file.getProject(), file.getName());
				if (type != null) {
					newLanguage = type.getId();
				}
				if (newLanguage == null) {
					return false;
				}
			}
		}
		return oldLanguage.equals(newLanguage);
	}

	/**
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#createActions()
	 */
	protected void createActions() {
		super.createActions();

		fFoldingGroup = new FoldingActionGroup(this, getSourceViewer());

		// Sticky hover support
		ResourceAction resAction = new TextOperationAction(CEditorMessages.getResourceBundle(), "ShowToolTip.", this, ISourceViewer.INFORMATION, true); //$NON-NLS-1$
		ResourceAction resAction2 = new InformationDispatchAction(CEditorMessages.getResourceBundle(), "ShowToolTip.", (TextOperationAction) resAction); //$NON-NLS-1$
		resAction2.setActionDefinitionId(ICEditorActionDefinitionIds.SHOW_TOOLTIP);
		setAction("ShowToolTip", resAction2); //$NON-NLS-1$
		PlatformUI.getWorkbench().getHelpSystem().setHelp(resAction2, ICHelpContextIds.SHOW_TOOLTIP_ACTION);
		
		// Default text editing menu items
		IAction action= new GotoMatchingBracketAction(this);
		action.setActionDefinitionId(ICEditorActionDefinitionIds.GOTO_MATCHING_BRACKET);				
		setAction(GotoMatchingBracketAction.GOTO_MATCHING_BRACKET, action);
		
		action = new JoinLinesAction(CEditorMessages.getResourceBundle(), "JoinLines.", this); //$NON-NLS-1$
		action.setActionDefinitionId(ICEditorActionDefinitionIds.JOIN_LINES);
		setAction("Join Lines", action); //$NON-NLS-1$
		
		action = new ToggleCommentAction(CEditorMessages.getResourceBundle(), "ToggleComment.", this); //$NON-NLS-1$
		action.setActionDefinitionId(ICEditorActionDefinitionIds.TOGGLE_COMMENT);
		setAction("ToggleComment", action); //$NON-NLS-1$
		markAsStateDependentAction("ToggleComment", true); //$NON-NLS-1$
		configureToggleCommentAction();
		
		action = new AddBlockCommentAction(CEditorMessages.getResourceBundle(), "AddBlockComment.", this);  //$NON-NLS-1$
		action.setActionDefinitionId(ICEditorActionDefinitionIds.ADD_BLOCK_COMMENT);		
		setAction("AddBlockComment", action); //$NON-NLS-1$
		markAsStateDependentAction("AddBlockComment", true); //$NON-NLS-1$
		markAsSelectionDependentAction("AddBlockComment", true); //$NON-NLS-1$		
		//WorkbenchHelp.setHelp(action, ICHelpContextIds.ADD_BLOCK_COMMENT_ACTION);

		action = new RemoveBlockCommentAction(CEditorMessages.getResourceBundle(), "RemoveBlockComment.", this);  //$NON-NLS-1$
		action.setActionDefinitionId(ICEditorActionDefinitionIds.REMOVE_BLOCK_COMMENT);		
		setAction("RemoveBlockComment", action); //$NON-NLS-1$
		markAsStateDependentAction("RemoveBlockComment", true); //$NON-NLS-1$
		markAsSelectionDependentAction("RemoveBlockComment", true); //$NON-NLS-1$		
		//WorkbenchHelp.setHelp(action, ICHelpContextIds.REMOVE_BLOCK_COMMENT_ACTION);

		action = new IndentAction(CEditorMessages.getResourceBundle(), "Indent.", this, false); //$NON-NLS-1$
		action.setActionDefinitionId(ICEditorActionDefinitionIds.INDENT);
		setAction("Indent", action); //$NON-NLS-1$
		markAsStateDependentAction("Indent", true); //$NON-NLS-1$
		markAsSelectionDependentAction("Indent", true); //$NON-NLS-1$
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(action, ICHelpContextIds.INDENT_ACTION);

		action = new IndentAction(CEditorMessages.getResourceBundle(), "Indent.", this, true); //$NON-NLS-1$
		setAction("IndentOnTab", action); //$NON-NLS-1$
		markAsStateDependentAction("IndentOnTab", true); //$NON-NLS-1$
		markAsSelectionDependentAction("IndentOnTab", true); //$NON-NLS-1$
		
		if (getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_SMART_TAB)) {
			setActionActivationCode("IndentOnTab", '\t', -1, SWT.NONE); //$NON-NLS-1$
		}
		
		action = new TextOperationAction(CEditorMessages.getResourceBundle(), "Format.", this, ISourceViewer.FORMAT); //$NON-NLS-1$
		action.setActionDefinitionId(ICEditorActionDefinitionIds.FORMAT);
		setAction("Format", action); //$NON-NLS-1$
		markAsStateDependentAction("Format", true); //$NON-NLS-1$

		action = new ContentAssistAction(CEditorMessages.getResourceBundle(), "ContentAssistProposal.", this); //$NON-NLS-1$
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		setAction("ContentAssistProposal", action); //$NON-NLS-1$
		markAsStateDependentAction("ContentAssistProposal", true); //$NON-NLS-1$

		action = new TextOperationAction(CEditorMessages.getResourceBundle(), "ContentAssistTip.", this, ISourceViewer.CONTENTASSIST_CONTEXT_INFORMATION); //$NON-NLS-1$
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_CONTEXT_INFORMATION);
		setAction("ContentAssistTip", action); //$NON-NLS-1$

		action = new OpenDeclarationsAction(this);
		action.setActionDefinitionId(ICEditorActionDefinitionIds.OPEN_DECL);
		setAction("OpenDeclarations", action); //$NON-NLS-1$

        action = new OpenDefinitionAction(this);
        action.setActionDefinitionId(ICEditorActionDefinitionIds.OPEN_DEF);
        setAction("OpenDefinition", action); //$NON-NLS-1$
        
//		action = new OpenTypeHierarchyAction(this);
//		action.setActionDefinitionId(ICEditorActionDefinitionIds.OPEN_TYPE_HIERARCHY);
//		setAction("OpenTypeHierarchy", action); //$NON-NLS-1$

		fShowInCViewAction = new ShowInCViewAction(this);
		action = fShowInCViewAction;
		action.setActionDefinitionId(ICEditorActionDefinitionIds.OPEN_CVIEW);
		setAction("ShowInCView", action); //$NON-NLS-1$
        
        action = new TextOperationAction(CEditorMessages.getResourceBundle(), "OpenOutline.", this, CSourceViewer.SHOW_OUTLINE, true); //$NON-NLS-1$
        action.setActionDefinitionId(ICEditorActionDefinitionIds.OPEN_OUTLINE);
        setAction("OpenOutline", action); //$NON-NLS-1$*/
        
        action = new GoToNextPreviousMemberAction(CEditorMessages.getResourceBundle(), "GotoNextMember.", this, true); //$NON-NLS-1$
        action.setActionDefinitionId(ICEditorActionDefinitionIds.GOTO_NEXT_MEMBER);
        setAction(GoToNextPreviousMemberAction.PREVIOUS_MEMBER, action);

        action = new GoToNextPreviousMemberAction(CEditorMessages.getResourceBundle(), "GotoPreviousMember.", this, false); //$NON-NLS-1$
        action.setActionDefinitionId(ICEditorActionDefinitionIds.GOTO_PREVIOUS_MEMBER);
        setAction(GoToNextPreviousMemberAction.NEXT_MEMBER, action);

        //Assorted action groupings
		fSelectionSearchGroup = new SelectionSearchGroup(this);
		fTextSearchGroup= new TextSearchGroup(this);
		fRefactoringActionGroup= new CRefactoringActionGroup(this, ITextEditorActionConstants.GROUP_EDIT);
		fOpenInViewGroup= new OpenViewActionGroup(this);
		fGenerateActionGroup= new GenerateActionGroup(this, ITextEditorActionConstants.GROUP_EDIT);

		action = getAction(ITextEditorActionConstants.SHIFT_RIGHT);
		if (action != null) {
			action.setId(ITextEditorActionConstants.SHIFT_RIGHT);
			CPluginImages.setImageDescriptors(action, CPluginImages.T_LCL, CPluginImages.IMG_MENU_SHIFT_RIGHT);
		}
		action = getAction(ITextEditorActionConstants.SHIFT_LEFT);
		if (action != null) {
			action.setId(ITextEditorActionConstants.SHIFT_LEFT);
			CPluginImages.setImageDescriptors(action, CPluginImages.T_LCL, CPluginImages.IMG_MENU_SHIFT_LEFT);
		}
	}

	/**
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#editorContextMenuAboutToShow(org.eclipse.jface.action.IMenuManager)
	 */
	public void editorContextMenuAboutToShow(IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		// remove shift actions added by base class
		menu.remove(ITextEditorActionConstants.SHIFT_LEFT);
		menu.remove(ITextEditorActionConstants.SHIFT_RIGHT);

		menu.insertAfter(IContextMenuConstants.GROUP_OPEN, new GroupMarker(IContextMenuConstants.GROUP_SHOW));

		addAction(menu, IContextMenuConstants.GROUP_OPEN, "OpenDeclarations"); //$NON-NLS-1$
        addAction(menu, IContextMenuConstants.GROUP_OPEN, "OpenDefinition"); //$NON-NLS-1$
		addAction(menu, IContextMenuConstants.GROUP_OPEN, "OpenTypeHierarchy"); //$NON-NLS-1$

		addAction(menu, IContextMenuConstants.GROUP_OPEN, "OpenOutline"); //$NON-NLS-1$

		ActionContext context= new ActionContext(getSelectionProvider().getSelection());
		fGenerateActionGroup.setContext(context);
		fGenerateActionGroup.fillContextMenu(menu);
		fGenerateActionGroup.setContext(null);

		fSelectionSearchGroup.fillContextMenu(menu);
		fTextSearchGroup.fillContextMenu(menu);
		fRefactoringActionGroup.fillContextMenu(menu);
		fOpenInViewGroup.fillContextMenu(menu);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#rulerContextMenuAboutToShow(org.eclipse.jface.action.IMenuManager)
	 */
	protected void rulerContextMenuAboutToShow(IMenuManager menu) {
		super.rulerContextMenuAboutToShow(menu);
		IMenuManager foldingMenu= new MenuManager(CEditorMessages.getString("CEditor.menu.folding"), "projection"); //$NON-NLS-1$ //$NON-NLS-2$
		menu.appendToGroup(ITextEditorActionConstants.GROUP_RULERS, foldingMenu);

		IAction action= getAction("FoldingToggle"); //$NON-NLS-1$
		foldingMenu.add(action);
		action= getAction("FoldingExpandAll"); //$NON-NLS-1$
		foldingMenu.add(action);
		action= getAction("FoldingCollapseAll"); //$NON-NLS-1$
		foldingMenu.add(action);
		action= getAction("FoldingRestore"); //$NON-NLS-1$
		foldingMenu.add(action);
	}

	/**
     * Sets an input for the outline page.
	 * @param page Page to set the input.
	 * @param input Input to set.
	 */
	public static void setOutlinePageInput(CContentOutlinePage page, IEditorInput input) {
		if (page != null) {
			IWorkingCopyManager manager = CUIPlugin.getDefault().getWorkingCopyManager();
			page.setInput(manager.getWorkingCopy(input));
		}
	}

	/**
     * Determines is folding enabled.
	 * @return <code>true</code> if folding is enabled, <code>false</code> otherwise.
	 */
	boolean isFoldingEnabled() {
		return CUIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_FOLDING_ENABLED);
	}


	/**
	 * The <code>AbstractTextEditor</code> implementation of this 
	 * <code>IWorkbenchPart</code> method creates the vertical ruler and
	 * source viewer. Subclasses may extend.
	 * 
	 * We attach our own mouseDown listener on the menu bar, 
	 * and our own listener for cursor/key/selection events to update cursor position in
	 * status bar.

     * @param parent Parent composite of the control.
	 */
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
	
		// Sticky hover support
		IInformationControlCreator informationControlCreator = new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell shell) {
				boolean cutDown = false;
				int style = cutDown ? SWT.NONE : (SWT.V_SCROLL | SWT.H_SCROLL);
				return new DefaultInformationControl(shell, SWT.RESIZE
						| SWT.TOOL, style, new HTMLTextPresenter(cutDown));
			}
		};

		fInformationPresenter = new InformationPresenter(informationControlCreator);
		fInformationPresenter.setSizeConstraints(60, 10, true, true);
		fInformationPresenter.install(getSourceViewer());
		fInformationPresenter.setDocumentPartitioning(ICPartitions.C_PARTITIONING);
				
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, ICHelpContextIds.CEDITOR_VIEW);

		fEditorSelectionChangedListener = new EditorSelectionChangedListener();
		fEditorSelectionChangedListener.install(getSelectionProvider());
		
		if (isTabConversionEnabled())
			startTabConversion();

		if (isSemanticHighlightingEnabled())
			installSemanticHighlighting();
			
		IPreferenceStore preferenceStore = getPreferenceStore();
		boolean closeBrackets = preferenceStore.getBoolean(CLOSE_BRACKETS);
		boolean closeAngularBrackets = preferenceStore.getBoolean(CLOSE_ANGULAR_BRACKETS);
		boolean closeStrings = preferenceStore.getBoolean(CLOSE_STRINGS);

		fBracketInserter.setCloseBracketsEnabled(closeBrackets);
		fBracketInserter.setCloseStringsEnabled(closeStrings);
		fBracketInserter.setCloseAngularBracketsEnabled(closeAngularBrackets);

		ISourceViewer sourceViewer = getSourceViewer();
		if (sourceViewer instanceof ITextViewerExtension)
			((ITextViewerExtension) sourceViewer).prependVerifyKeyListener(fBracketInserter);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#initializeDragAndDrop(org.eclipse.jface.text.source.ISourceViewer)
	 */
	protected void initializeDragAndDrop(ISourceViewer viewer) {
		Control control = viewer.getTextWidget();
		int operations = DND.DROP_MOVE | DND.DROP_COPY;

		DropTarget dropTarget = new DropTarget(control, operations);
		ITextEditorDropTargetListener dropTargetListener = new TextEditorDropAdapter(viewer, this);
		dropTarget.setTransfer(dropTargetListener.getTransfers());
		dropTarget.addDropListener(dropTargetListener);

		DragSource dragSource = new DragSource(control, operations);
		Transfer[] dragTypes = new Transfer[] { TextTransfer.getInstance() };
		dragSource.setTransfer(dragTypes);
		DragSourceListener dragSourceListener = new TextViewerDragAdapter(viewer, this);
		dragSource.addDragListener(dragSourceListener);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#getSourceViewerDecorationSupport(org.eclipse.jface.text.source.ISourceViewer)
	 */
	protected SourceViewerDecorationSupport getSourceViewerDecorationSupport(
			ISourceViewer viewer) {
		if (fSourceViewerDecorationSupport == null) {
			fSourceViewerDecorationSupport= new CSourceViewerDecorationSupport(this, viewer, getOverviewRuler(), getAnnotationAccess(), getSharedColors());
			configureSourceViewerDecorationSupport(fSourceViewerDecorationSupport);
		}
		return fSourceViewerDecorationSupport;
	}
	
	/*
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#configureSourceViewerDecorationSupport(org.eclipse.ui.texteditor.SourceViewerDecorationSupport)
	 */
	protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {
		super.configureSourceViewerDecorationSupport(support);
		//Enhance the stock source viewer decorator with a bracket matcher
		support.setCharacterPairMatcher(fBracketMatcher);
		support.setMatchingCharacterPainterPreferenceKeys(MATCHING_BRACKETS, MATCHING_BRACKETS_COLOR);
		((CSourceViewerDecorationSupport)support).setInactiveCodePainterPreferenceKeys(INACTIVE_CODE_ENABLE, INACTIVE_CODE_COLOR);
	}
	
	/**
	 * Jumps to the matching bracket.
	 */
	public void gotoMatchingBracket() {
		
		ISourceViewer sourceViewer = getSourceViewer();
		IDocument document = sourceViewer.getDocument();
		if (document == null)
			return;
		
		IRegion selection = getSignedSelection(sourceViewer);

		int selectionLength = Math.abs(selection.getLength());
		if (selectionLength > 1) {
			setStatusLineErrorMessage(CEditorMessages.getString("GotoMatchingBracket.error.invalidSelection"));	//$NON-NLS-1$		
			sourceViewer.getTextWidget().getDisplay().beep();
			return;
		}

		// #26314
		int sourceCaretOffset = selection.getOffset() + selection.getLength();
		if (isSurroundedByBrackets(document, sourceCaretOffset))
			sourceCaretOffset -= selection.getLength();

		IRegion region = fBracketMatcher.match(document, sourceCaretOffset);
		if (region == null) {
			setStatusLineErrorMessage(CEditorMessages.getString("GotoMatchingBracket.error.noMatchingBracket"));	//$NON-NLS-1$		
			sourceViewer.getTextWidget().getDisplay().beep();
			return;		
		}
		
		int offset = region.getOffset();
		int length = region.getLength();
		
		if (length < 1)
			return;
			
		int anchor = fBracketMatcher.getAnchor();
		// http://dev.eclipse.org/bugs/show_bug.cgi?id=34195
		int targetOffset = (ICharacterPairMatcher.RIGHT == anchor) ? offset + 1: offset + length;
		
		boolean visible = false;
		if (sourceViewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension = (ITextViewerExtension5) sourceViewer;
			visible = (extension.modelOffset2WidgetOffset(targetOffset) > -1);
		} else {
			IRegion visibleRegion = sourceViewer.getVisibleRegion();
			// http://dev.eclipse.org/bugs/show_bug.cgi?id=34195
			visible = (targetOffset >= visibleRegion.getOffset() && targetOffset <= visibleRegion.getOffset() + visibleRegion.getLength());
		}
		
		if (!visible) {
			setStatusLineErrorMessage(CEditorMessages.getString("GotoMatchingBracket.error.bracketOutsideSelectedElement"));	//$NON-NLS-1$		
			sourceViewer.getTextWidget().getDisplay().beep();
			return;
		}
		
		if (selection.getLength() < 0)
			targetOffset -= selection.getLength();
			
		sourceViewer.setSelectedRange(targetOffset, selection.getLength());
		sourceViewer.revealRange(targetOffset, selection.getLength());
	}

	protected void updateStatusLine() {
		ITextSelection selection = (ITextSelection) getSelectionProvider().getSelection();
		Annotation annotation = getAnnotation(selection.getOffset(), selection.getLength());
		setStatusLineErrorMessage(null);
		setStatusLineMessage(null);
		if (annotation != null) {
			updateMarkerViews(annotation);
			if (annotation instanceof ICAnnotation && ((ICAnnotation) annotation).isProblem())
				setStatusLineMessage(annotation.getText());
		}
	}

	/**
	 * Returns the annotation overlapping with the given range or <code>null</code>.
	 * 
	 * @param offset the region offset
	 * @param length the region length
	 * @return the found annotation or <code>null</code>
	 * @since 3.0
	 */
	private Annotation getAnnotation(int offset, int length) {
		IAnnotationModel model = getDocumentProvider().getAnnotationModel(getEditorInput());
		Iterator e = new CAnnotationIterator(model, true, true);
		while (e.hasNext()) {
			Annotation a = (Annotation) e.next();
			if (!isNavigationTarget(a))
				continue;
				
			Position p = model.getPosition(a);
			if (p != null && p.overlapsWith(offset, length))
				return a;
		}
		
		return null;
	}

	/*
	 * Get the dektop's StatusLineManager
	 */
	protected IStatusLineManager getStatusLineManager() {
		IEditorActionBarContributor contributor = getEditorSite().getActionBarContributor();
		if (contributor instanceof EditorActionBarContributor) {
			return ((EditorActionBarContributor) contributor).getActionBars().getStatusLineManager();
		}
		return null;
	}
	
	/**
	 * Configures the toggle comment action
	 *
	 * @since 4.0.0
	 */
	private void configureToggleCommentAction() {
		IAction action = getAction("ToggleComment"); //$NON-NLS-1$
		if (action instanceof ToggleCommentAction) {
			ISourceViewer sourceViewer = getSourceViewer();
			SourceViewerConfiguration configuration = getSourceViewerConfiguration();
			((ToggleCommentAction)action).configure(sourceViewer, configuration);
		}
	}

	private void configureTabConverter() {
		if (fTabConverter != null) {
			IDocumentProvider provider = getDocumentProvider();
			if (provider instanceof CDocumentProvider) {
				CDocumentProvider prov = (CDocumentProvider) provider;
				fTabConverter.setLineTracker(prov.createLineTracker(getEditorInput()));
			} else {
				fTabConverter.setLineTracker(new DefaultLineTracker());
			}
		}
	}

	private int getTabSize() {
		ICElement element = getInputCElement();
		ICProject project = element == null ? null : element.getCProject();
		return CodeFormatterUtil.getTabWidth(project);
	}

	private void startTabConversion() {
		if (fTabConverter == null) {
			fTabConverter= new TabConverter();
			configureTabConverter();
			fTabConverter.setNumberOfSpacesPerTab(getTabSize());
			AdaptedSourceViewer asv= (AdaptedSourceViewer) getSourceViewer();
			asv.addTextConverter(fTabConverter);
			asv.updateIndentationPrefixes();
		}
	}

	private void stopTabConversion() {
		if (fTabConverter != null) {
			AdaptedSourceViewer asv= (AdaptedSourceViewer) getSourceViewer();
			asv.removeTextConverter(fTabConverter);
			asv.updateIndentationPrefixes();
			fTabConverter= null;
		}
	}

	private boolean isTabConversionEnabled() {
		ICElement element= getInputCElement();
		ICProject project= element == null ? null : element.getCProject();
		String option;
		if (project == null)
			option= CCorePlugin.getOption(SPACES_FOR_TABS);
		else
			option= project.getOption(SPACES_FOR_TABS, true);
		return CCorePlugin.SPACE.equals(option);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#createNavigationActions()
	 */
	protected void createNavigationActions() {
		super.createNavigationActions();

		final StyledText textWidget = getSourceViewer().getTextWidget();

		IAction action = new NavigatePreviousSubWordAction();
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.WORD_PREVIOUS);
		setAction(ITextEditorActionDefinitionIds.WORD_PREVIOUS, action);
		textWidget.setKeyBinding(SWT.CTRL | SWT.ARROW_LEFT, SWT.NULL);

		action = new NavigateNextSubWordAction();
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.WORD_NEXT);
		setAction(ITextEditorActionDefinitionIds.WORD_NEXT, action);
		textWidget.setKeyBinding(SWT.CTRL | SWT.ARROW_RIGHT, SWT.NULL);

		action = new SelectPreviousSubWordAction();
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.SELECT_WORD_PREVIOUS);
		setAction(ITextEditorActionDefinitionIds.SELECT_WORD_PREVIOUS, action);
		textWidget.setKeyBinding(SWT.CTRL | SWT.SHIFT | SWT.ARROW_LEFT, SWT.NULL);

		action = new SelectNextSubWordAction();
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.SELECT_WORD_NEXT);
		setAction(ITextEditorActionDefinitionIds.SELECT_WORD_NEXT, action);
		textWidget.setKeyBinding(SWT.CTRL | SWT.SHIFT | SWT.ARROW_RIGHT, SWT.NULL);
		
		action = new DeletePreviousSubWordAction();
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.DELETE_PREVIOUS_WORD);
		setAction(ITextEditorActionDefinitionIds.DELETE_PREVIOUS_WORD, action);
		textWidget.setKeyBinding(SWT.CTRL | SWT.BS, SWT.NULL);
		markAsStateDependentAction(ITextEditorActionDefinitionIds.DELETE_PREVIOUS_WORD, true);

		action = new DeleteNextSubWordAction();
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.DELETE_NEXT_WORD);
		setAction(ITextEditorActionDefinitionIds.DELETE_NEXT_WORD, action);
		textWidget.setKeyBinding(SWT.CTRL | SWT.DEL, SWT.NULL);
		markAsStateDependentAction(ITextEditorActionDefinitionIds.DELETE_NEXT_WORD, true);
	}

	public final ISourceViewer getViewer() {
		return getSourceViewer();
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#createSourceViewer(org.eclipse.swt.widgets.Composite, org.eclipse.jface.text.source.IVerticalRuler, int)
	 */
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		IPreferenceStore store= getPreferenceStore();
		ISourceViewer sourceViewer =
				new AdaptedSourceViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles, store);

		CUIHelp.setHelp(this, sourceViewer.getTextWidget(), ICHelpContextIds.CEDITOR_VIEW);

		CSourceViewer cSourceViewer= null;
		if (sourceViewer instanceof CSourceViewer) {
			cSourceViewer= (CSourceViewer) sourceViewer;
		}
		
		/*
		 * This is a performance optimization to reduce the computation of
		 * the text presentation triggered by {@link #setVisibleDocument(IDocument)}
		 */
		if (cSourceViewer != null && isFoldingEnabled() && (store == null || !store.getBoolean(PreferenceConstants.EDITOR_SHOW_SEGMENTS)))
			cSourceViewer.prepareDelayedProjection();
		
		ProjectionViewer projectionViewer = (ProjectionViewer) sourceViewer;
		
		fProjectionSupport = new ProjectionSupport(projectionViewer, getAnnotationAccess(), getSharedColors());
		fProjectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.error"); //$NON-NLS-1$
		fProjectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.warning"); //$NON-NLS-1$
		fProjectionSupport.addSummarizableAnnotationType("org.eclipse.search.results"); //$NON-NLS-1$
		fProjectionSupport.setHoverControlCreator(new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell shell) {
				return new SourceViewerInformationControl(shell, SWT.TOOL | SWT.NO_TRIM | getOrientation(), SWT.NONE);
			}
		});
		fProjectionSupport.install();
		
		fProjectionModelUpdater = CUIPlugin.getDefault().getFoldingStructureProviderRegistry().getCurrentFoldingProvider();
		if (fProjectionModelUpdater != null)
			fProjectionModelUpdater.install(this, projectionViewer);

		if (isFoldingEnabled())
			projectionViewer.doOperation(ProjectionViewer.TOGGLE);
		
		getSourceViewerDecorationSupport(sourceViewer);
		
		return sourceViewer;
	}

	/** Outliner context menu Id */
	protected String fOutlinerContextMenuId;

	/**
	 * Sets the outliner's context menu ID.
	 */
	protected void setOutlinerContextMenuId(String menuId) {
		fOutlinerContextMenuId = menuId;
	}
	
	/*
	 * @see org.eclipse.ui.editors.text.TextEditor#initializeKeyBindingScopes()
	 */
	protected void initializeKeyBindingScopes() {
		setKeyBindingScopes(new String [] { "org.eclipse.cdt.ui.cEditorScope" } ); //$NON-NLS-1$
	}

	/*
	 * @see AbstractTextEditor#affectsTextPresentation(PropertyChangeEvent)
	 */
	protected boolean affectsTextPresentation(PropertyChangeEvent event) {
		SourceViewerConfiguration configuration = getSourceViewerConfiguration();
		if (configuration instanceof CSourceViewerConfiguration) {
			return ((CSourceViewerConfiguration)configuration).affectsTextPresentation(event);
		}
		return false;
	}

	/**
	 * Returns the folding action group, or <code>null</code> if there is none.
	 * 
	 * @return the folding action group, or <code>null</code> if there is none
	 */
	protected FoldingActionGroup getFoldingActionGroup() {
		return fFoldingGroup;
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#performRevert()
	 */
	protected void performRevert() {
		ProjectionViewer projectionViewer = (ProjectionViewer) getSourceViewer();
		projectionViewer.setRedraw(false);
		try {
			
			boolean projectionMode = projectionViewer.isProjectionMode();
			if (projectionMode) {
				projectionViewer.disableProjection();				
				if (fProjectionModelUpdater != null)
					fProjectionModelUpdater.uninstall();
			}
			
			super.performRevert();
			
			if (projectionMode) {
				if (fProjectionModelUpdater != null)
					fProjectionModelUpdater.install(this, projectionViewer);	
				projectionViewer.enableProjection();
			}
			
		} finally {
			projectionViewer.setRedraw(true);
		}
	}

    /**
     * Sets the given message as error message to this editor's status line.
     * 
     * @param msg message to be set
     */
    protected void setStatusLineErrorMessage(String msg) {
    	IEditorStatusLine statusLine = (IEditorStatusLine) getAdapter(IEditorStatusLine.class);
    	if (statusLine != null)
    		statusLine.setMessage(true, msg, null);	
    }  

	/**
	 * Sets the given message as message to this editor's status line.
	 * 
	 * @param msg message to be set
	 * @since 3.0
	 */
	protected void setStatusLineMessage(String msg) {
		IEditorStatusLine statusLine = (IEditorStatusLine) getAdapter(IEditorStatusLine.class);
		if (statusLine != null)
			statusLine.setMessage(false, msg, null);	
	}

	/**
	 * Returns the signed current selection.
	 * The length will be negative if the resulting selection
	 * is right-to-left (RtoL).
	 * <p>
	 * The selection offset is model based.
	 * </p>
	 * 
	 * @param sourceViewer the source viewer
	 * @return a region denoting the current signed selection, for a resulting RtoL selections length is < 0 
	 */
	protected IRegion getSignedSelection(ISourceViewer sourceViewer) {
		StyledText text = sourceViewer.getTextWidget();
		Point selection = text.getSelectionRange();
		
		if (text.getCaretOffset() == selection.x) {
			selection.x = selection.x + selection.y;
			selection.y = -selection.y;
		}
		
		selection.x = widgetOffset2ModelOffset(sourceViewer, selection.x);
		
		return new Region(selection.x, selection.y);
	}
	
	private static boolean isBracket(char character) {
		for (int i = 0; i != BRACKETS.length; ++i) {
			if (character == BRACKETS[i])
				return true;
		}
		return false;
	}

	private static boolean isSurroundedByBrackets(IDocument document, int offset) {
		if (offset == 0 || offset == document.getLength())
			return false;

		try {
			return isBracket(document.getChar(offset - 1)) &&
					isBracket(document.getChar(offset));
		} catch (BadLocationException e) {
			return false;	
		}
	}
		
	private static char getEscapeCharacter(char character) {
		switch (character) {
			case '"':
			case '\'':
				return '\\';
			default:
				return 0;
		}
	}

	private static char getPeerCharacter(char character) {
		switch (character) {
			case '(':
				return ')';

			case ')':
				return '(';

			case '<':
				return '>';

			case '>':
				return '<';

			case '[':
				return ']';

			case ']':
				return '[';

			case '"':
				return character;

			case '\'':
				return character;

			default:
				throw new IllegalArgumentException();
		}
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.editor.IReconcilingParticipant#reconciled()
	 */
	public void reconciled(boolean somethingHasChanged) {
		if (getSourceViewer() == null || getSourceViewer().getTextWidget() == null) {
			return;
		}
		// this method must be called in a background thread
		assert getSourceViewer().getTextWidget().getDisplay().getThread() != Thread.currentThread();
		
		if (fReconcilingListeners.size() > 0) {
			// create AST and notify ICReconcilingListeners
			ICElement cElement= getInputCElement();
			if (cElement == null) {
				return;
			}
			
			aboutToBeReconciled();

			// track changes to the document while parsing
			IDocument doc= getDocumentProvider().getDocument(getEditorInput());
			SimplePositionTracker positionTracker= new SimplePositionTracker();
			positionTracker.startTracking(doc);
			
			ASTProvider astProvider= CUIPlugin.getDefault().getASTProvider();
			IIndex index;
			try {
				index = CCorePlugin.getIndexManager().getIndex(cElement.getCProject());
				index.acquireReadLock();
			} catch (CoreException e) {
				CUIPlugin.getDefault().log(e);
				return;
			} catch (InterruptedException e) {
				return;
			}
			try {
				IASTTranslationUnit ast= astProvider.createAST(cElement, index, null);
				reconciled(ast, positionTracker, null);
			} finally {
				index.releaseReadLock();
				positionTracker.stopTracking();
			}
		}
	}
	
	public CSourceViewer getCSourceViewer()  {
		ISourceViewer viewer = getSourceViewer();
		CSourceViewer cViewer = null ;
		if (viewer instanceof CSourceViewer) {
			cViewer = (CSourceViewer) viewer;
		}
		return cViewer ;
	}
	
	/*
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#collectContextMenuPreferencePages()
	 */
	protected String[] collectContextMenuPreferencePages() {
		// Add C/C++ Editor relevant pages
		String[] parentPrefPageIds = super.collectContextMenuPreferencePages();
		String[] prefPageIds = new String[parentPrefPageIds.length + 6];
		int nIds = 0;
		prefPageIds[nIds++] = "org.eclipse.cdt.ui.preferences.CEditorPreferencePage"; //$NON-NLS-1$
		prefPageIds[nIds++] = "org.eclipse.cdt.ui.preferences.CodeAssistPreferencePage"; //$NON-NLS-1$
		prefPageIds[nIds++] = "org.eclipse.cdt.ui.preferences.CodeColoringPreferencePage"; //$NON-NLS-1$
		prefPageIds[nIds++] = "org.eclipse.cdt.ui.preferences.TemplatePreferencePage"; //$NON-NLS-1$
		prefPageIds[nIds++] = "org.eclipse.cdt.ui.preferences.SmartTypingPreferencePage"; //$NON-NLS-1$
		prefPageIds[nIds++] = "org.eclipse.cdt.ui.preferences.CodeFormatterPreferencePage"; //$NON-NLS-1$
		System.arraycopy(parentPrefPageIds, 0, prefPageIds, nIds, parentPrefPageIds.length);
		return prefPageIds;
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.ICReconcilingListener#aboutToBeReconciled()
	 * @since 4.0
	 */
	public void aboutToBeReconciled() {

		// Notify AST provider
		CUIPlugin.getDefault().getASTProvider().aboutToBeReconciled(getInputCElement());

		// Notify listeners
		Object[] listeners = fReconcilingListeners.getListeners();
		for (int i = 0, length= listeners.length; i < length; ++i) {
			((ICReconcilingListener)listeners[i]).aboutToBeReconciled();
		}
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.ICReconcilingListener#reconciled(IASTTranslationUnit, IPositionConverter, IProgressMonitor)
	 * @since 4.0
	 */
	public void reconciled(IASTTranslationUnit ast, IPositionConverter positionTracker, IProgressMonitor progressMonitor) {

		CUIPlugin cuiPlugin= CUIPlugin.getDefault();
		if (cuiPlugin == null)
			return;
		
		// Always notify AST provider
		cuiPlugin.getASTProvider().reconciled(ast, positionTracker, getInputCElement(), progressMonitor);

		// Notify listeners
		Object[] listeners = fReconcilingListeners.getListeners();
		for (int i = 0, length= listeners.length; i < length; ++i) {
			((ICReconcilingListener)listeners[i]).reconciled(ast, positionTracker, progressMonitor);
		}
	
	}

	/**
	 * Adds the given listener.
	 * Has no effect if an identical listener was not already registered.
	 *
	 * @param listener	The reconcile listener to be added
	 * @since 4.0
	 */
	final public void addReconcileListener(ICReconcilingListener listener) {
		fReconcilingListeners.add(listener);
	}

	/**
	 * Removes the given listener.
	 * Has no effect if an identical listener was not already registered.
	 *
	 * @param listener	the reconcile listener to be removed
	 * @since 4.0
	 */
	final public void removeReconcileListener(ICReconcilingListener listener) {
		fReconcilingListeners.remove(listener);
	}

	/**
	 * @return <code>true</code> if Semantic Highlighting is enabled.
	 *
	 * @since 4.0
	 */
	private boolean isSemanticHighlightingEnabled() {
		return SemanticHighlightings.isEnabled(getPreferenceStore());
	}

	/**
	 * Install Semantic Highlighting.
	 *
	 * @since 4.0
	 */
	private void installSemanticHighlighting() {
		if (fSemanticManager == null) {
			fSemanticManager= new SemanticHighlightingManager();
			fSemanticManager.install(this, (CSourceViewer) getSourceViewer(), CUIPlugin.getDefault().getTextTools().getColorManager(), getPreferenceStore());
		}
	}

	/**
	 * Uninstall Semantic Highlighting.
	 *
	 * @since 4.0
	 */
	private void uninstallSemanticHighlighting() {
		if (fSemanticManager != null) {
			fSemanticManager.uninstall();
			fSemanticManager= null;
		}
	}

	/**
	 * Called whenever the editor is activated and allows for registering 
	 * action handlers.
	 */ 
	public void fillActionBars(IActionBars actionBars) {
		fOpenInViewGroup.fillActionBars(actionBars);
		fRefactoringActionGroup.fillActionBars(actionBars);
		fGenerateActionGroup.fillActionBars(actionBars);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#updateStateDependentActions()
	 */
	protected void updateStateDependentActions() {
		super.updateStateDependentActions();
		fGenerateActionGroup.editorStateChanged();
	}

	/**
	 * Resets the foldings structure according to the folding
	 * preferences.
	 * 
	 * @since 4.0
	 */
	public void resetProjection() {
		if (fProjectionModelUpdater != null) {
			fProjectionModelUpdater.initialize();
		}
	}
	
}
