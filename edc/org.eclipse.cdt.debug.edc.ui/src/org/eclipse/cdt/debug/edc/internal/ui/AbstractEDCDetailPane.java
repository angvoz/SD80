/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.ui;

import org.eclipse.cdt.dsf.debug.service.IFormattedValues.IFormattedDataDMContext;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.SyncVariableDataAccess;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.internal.ui.views.variables.details.AbstractDetailPane;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.IUndoManagerExtension;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.console.actions.TextViewerAction;
import org.eclipse.ui.operations.OperationHistoryActionHandler;
import org.eclipse.ui.operations.RedoActionHandler;
import org.eclipse.ui.operations.UndoActionHandler;
import org.eclipse.ui.texteditor.IAbstractTextEditorHelpContextIds;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.osgi.service.prefs.BackingStoreException;

@SuppressWarnings("restriction")
public abstract class AbstractEDCDetailPane extends AbstractDetailPane implements IPropertyChangeListener {

	public class SetValueAction  extends Action {
		
		@Override
		public void run() {
			Point selectedRange = viewer.getSelectedRange();
			String value = null;
			if (selectedRange.y == 0) {
				value = viewer.getDocument().get();
			} else {
				try {
					value = viewer.getDocument().get(selectedRange.x, selectedRange.y);
				} catch (BadLocationException e) {
					EDCDebugUI.logError(null, e);
				}
			}
			
			setValue((IStructuredSelection) currentSelection, value);
		}
		
		private void setValue(IStructuredSelection selection, String value) {
			Job setValueJob = createSetValueJob(selection, value);
			if (setValueJob != null)
				setValueJob.schedule();
		}

		@Override
		public boolean isEnabled() {
			return isEditingEnabled(currentSelection);
		}
		
		@Override
		public String getActionDefinitionId() {
			return IWorkbenchCommandConstants.FILE_SAVE;
		}
	}

	public class WordWrapAction extends Action {

		public WordWrapAction() {
			super(ActionMessages.DetailPaneWordWrapAction_0, IAction.AS_CHECK_BOX);
			setEnabled(true);
			
			boolean prefSetting = getWordWrapPreference();
			viewer.getTextWidget().setWordWrap(prefSetting);
			setChecked(prefSetting);
		}
		
		public void run() {
			viewer.getTextWidget().setWordWrap(isChecked());
			EDCDebugUI.getDefault().getPreferenceStore().setValue(WORD_WRAP_ACTION, isChecked());
			try {
				new InstanceScope().getNode(EDCDebugUI.PLUGIN_ID).flush();
			} catch (BackingStoreException e) {
				EDCDebugUI.logError(null, e);
			}
		}
	}
	
	protected static final String COPY_ACTION = ActionFactory.COPY.getId();
	protected static final String CUT_ACTION = ActionFactory.CUT.getId();
	protected static final String PASTE_ACTION = ActionFactory.PASTE.getId();
	protected static final String SELECT_ALL_ACTION = IDebugView.SELECT_ALL_ACTION;
	protected static final String WORD_WRAP_ACTION = IDebugPreferenceConstants.PREF_DETAIL_PANE_WORD_WRAP;
	protected static final String SET_VALUE_ACTION = "setValue"; //$NON-NLS-1$
	
	protected SourceViewer viewer;
	protected Document document;
	protected IStructuredSelection currentSelection = StructuredSelection.EMPTY;
	private Job displayDetailJob;

	/**
	 * Return the Job for displaying the detail for a selection
	 * @param selection IStructuredSelection
	 * @return Job
	 */
	protected abstract Job createDisplayDetailJob(IStructuredSelection selection);
	
	/**
	 * Return whether the selection represents values that can be modified
	 * @param selection IStructuredSelection
	 * @return boolean
	 */
	protected abstract boolean canSetValue(IStructuredSelection selection);
	
	/**
	 * Return the Job for setting the value for a selection
	 * @param selection IStructuredSelection
	 * @param newValue String
	 * @return Job
	 */
	protected abstract Job createSetValueJob(IStructuredSelection selection, String newValue);

	public Control createControl(Composite parent) {
	    Control control = createViewer(parent);
	    
		if (isInView()){
			hookViewer();
			createContextMenu();
			createActions();
			EDCDebugUI.getDefault().getPreferenceStore().addPropertyChangeListener(this);
			JFaceResources.getFontRegistry().addListener(this);
		}
		
		return control;  
	}

	protected Control createViewer(Composite parent) {
		viewer = new SourceViewer(parent, null, SWT.V_SCROLL | SWT.H_SCROLL);
	    viewer.setDocument(getDocument());
	    viewer.getTextWidget().setFont(JFaceResources.getFont(IDebugUIConstants.PREF_DETAIL_PANE_FONT));
	    viewer.getTextWidget().setWordWrap(getWordWrapPreference());
	    viewer.setEditable(false);
		viewer.unconfigure();
		viewer.configure(new SourceViewerConfiguration());
	    
	    Control control = viewer.getControl();
	    GridData gd = new GridData(GridData.FILL_BOTH);
	    control.setLayoutData(gd);
		return control;
	}

	private IDocument getDocument() {
		if (document == null)
			document = new Document();
		return document;
	}

	public void display(IStructuredSelection selection) {
		this.currentSelection = selection;
		if (selection == null || selection.isEmpty()){
			clearSourceViewer();
			return;
		}
		
		if (isInView()) {
			viewer.setEditable(isEditingEnabled(selection));
		}
		
	    synchronized (this) {
	    	if (displayDetailJob != null) {
	    		displayDetailJob.cancel();
	    	}
			displayDetailJob = createDisplayDetailJob(selection);
			if (displayDetailJob != null)
				displayDetailJob.schedule();
	    }
		
	}
	
	public void redisplay() {
		display(currentSelection);
	}

	private void clearSourceViewer() {
		if (displayDetailJob != null) {
			displayDetailJob.cancel();
		}
		document.set(""); //$NON-NLS-1$
	}

	public boolean setFocus() {
		if (viewer != null){
			viewer.getTextWidget().setFocus();
			return true;
		}
		return false;
	}

	protected void hookViewer() {
		// Add a document listener so actions get updated when the document changes
		getDocument().addDocumentListener(new IDocumentListener() {
			public void documentAboutToBeChanged(DocumentEvent event) {}
			public void documentChanged(DocumentEvent event) {
				updateSelectionDependentActions();
			}
		});
		
		// Add the selection listener so selection dependent actions get updated.
		viewer.getSelectionProvider().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateSelectionDependentActions();
			}
		});
		
		// Add a focus listener to update actions when details area gains focus
		viewer.getControl().addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				setGlobalAction(IDebugView.SELECT_ALL_ACTION, getAction(SELECT_ALL_ACTION));
				setGlobalAction(IDebugView.COPY_ACTION, getAction(COPY_ACTION));
				setGlobalAction(IDebugView.CUT_ACTION, getAction(CUT_ACTION));
				setGlobalAction(IDebugView.PASTE_ACTION, getAction(PASTE_ACTION));
				IAction action = getAction(SET_VALUE_ACTION);
				setGlobalAction(action.getActionDefinitionId(), action);
				getViewSite().getActionBars().updateActionBars();
			}
			
			public void focusLost(FocusEvent e) {
				setGlobalAction(IDebugView.SELECT_ALL_ACTION, null);
				setGlobalAction(IDebugView.COPY_ACTION, null);
				setGlobalAction(IDebugView.CUT_ACTION, null);
				setGlobalAction(IDebugView.PASTE_ACTION, null);
				setGlobalAction(getAction(SET_VALUE_ACTION).getActionDefinitionId(), null);
				getViewSite().getActionBars().updateActionBars();
				
			}
		});
	}

	protected void createContextMenu() {
		MenuManager menuMgr= new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				fillContextMenu(mgr);
			}
		});
		Menu menu= menuMgr.createContextMenu(viewer.getTextWidget());
		viewer.getTextWidget().setMenu(menu);
	
		getViewSite().registerContextMenu(IDebugUIConstants.VARIABLE_VIEW_DETAIL_ID, menuMgr, viewer.getSelectionProvider());
	
	}

	protected void fillContextMenu(IMenuManager menu) {
		menu.add(new Separator(IDebugUIConstants.VARIABLE_GROUP));
		menu.add(getAction(COPY_ACTION));
		menu.add(getAction(CUT_ACTION));
		menu.add(getAction(PASTE_ACTION));
		menu.add(getAction(SELECT_ALL_ACTION));
		menu.add(new Separator());
		menu.add(getAction(WORD_WRAP_ACTION));
	}

	protected void createActions() {
		TextViewerAction textAction = new TextViewerAction(viewer, ITextOperationTarget.SELECT_ALL);
		textAction.setText("Select &All");
		textAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_SELECT_ALL);
		setAction(SELECT_ALL_ACTION, textAction);
		
		textAction = new TextViewerAction(viewer, ITextOperationTarget.COPY);
		textAction.setText("&Copy");
		textAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_COPY);
		setAction(COPY_ACTION, textAction);
		
		textAction = new TextViewerAction(viewer, ITextOperationTarget.CUT);
		textAction.setText("&Cut");
		textAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_CUT);
		setAction(CUT_ACTION, textAction);
		
		textAction = new TextViewerAction(viewer, ITextOperationTarget.PASTE);
		textAction.setText("&Paste");
		textAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_PASTE);
		setAction(PASTE_ACTION, textAction);
		
		setSelectionDependantAction(COPY_ACTION);
		setSelectionDependantAction(CUT_ACTION);
		updateSelectionDependentActions();
		
		IAction action = new WordWrapAction();
		setAction(WORD_WRAP_ACTION, action);
		
		action = new SetValueAction();
		setAction(SET_VALUE_ACTION, action);
		
		createUndoRedoActions();
	}

	public void propertyChange(PropertyChangeEvent event) {
		String propertyName= event.getProperty();
		if (propertyName.equals(IDebugUIConstants.PREF_DETAIL_PANE_FONT)) {
			viewer.getTextWidget().setFont(JFaceResources.getFont(IDebugUIConstants.PREF_DETAIL_PANE_FONT));
		} else if (propertyName.equals(WORD_WRAP_ACTION)) {
			viewer.getTextWidget().setWordWrap(getWordWrapPreference());
			getAction(WORD_WRAP_ACTION).setChecked(getWordWrapPreference());
		}
		
	}

	private boolean getWordWrapPreference() {
		return EDCDebugUI.getDefault().getPreferenceStore().getBoolean(WORD_WRAP_ACTION);
	}

	protected boolean isEditingEnabled(IStructuredSelection selection) {
		return canSetValue(selection);
	}

	public void dispose() {
		if (displayDetailJob != null) 
			displayDetailJob.cancel();
		if (viewer != null && viewer.getControl() != null) viewer.getControl().dispose();
		
		if (isInView()){
			EDCDebugUI.getDefault().getPreferenceStore().removePropertyChangeListener(this);
			JFaceResources.getFontRegistry().removeListener(this);
			
			disposeUndoRedoAction(ITextEditorActionConstants.UNDO);
			disposeUndoRedoAction(ITextEditorActionConstants.REDO);
		}
		
		super.dispose();
	}

	private void createUndoRedoActions() {
		disposeUndoRedoAction(ITextEditorActionConstants.UNDO);
		disposeUndoRedoAction(ITextEditorActionConstants.REDO);
		IUndoContext undoContext = getUndoContext();
		if (undoContext != null) {
			// Use actions provided by global undo/re-do
			
			// Create the undo action
			OperationHistoryActionHandler undoAction= new UndoActionHandler(getViewSite(), undoContext);
			PlatformUI.getWorkbench().getHelpSystem().setHelp(undoAction, IAbstractTextEditorHelpContextIds.UNDO_ACTION);
			undoAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_UNDO);
			setAction(ITextEditorActionConstants.UNDO, undoAction);
			setGlobalAction(ITextEditorActionConstants.UNDO, undoAction);
			
			// Create the re-do action.
			OperationHistoryActionHandler redoAction= new RedoActionHandler(getViewSite(), undoContext);
			PlatformUI.getWorkbench().getHelpSystem().setHelp(redoAction, IAbstractTextEditorHelpContextIds.REDO_ACTION);
			redoAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_REDO);
			setAction(ITextEditorActionConstants.REDO, redoAction);
			setGlobalAction(ITextEditorActionConstants.REDO, redoAction);
			
			getViewSite().getActionBars().updateActionBars();
		}
	}

	private IUndoContext getUndoContext() {
		IUndoManager undoManager = viewer.getUndoManager();
		if (undoManager instanceof IUndoManagerExtension)
			return ((IUndoManagerExtension)undoManager).getUndoContext();
		return null;
	}

	private void disposeUndoRedoAction(String actionId) {
		OperationHistoryActionHandler action = (OperationHistoryActionHandler) getAction(actionId);
		if (action != null) {
			action.dispose();
			setAction(actionId, null);
		}
	}

	protected static SyncVariableDataAccess createSyncVariableDataAccess(IFormattedDataDMContext context) {
		DsfSession session = DsfSession.getSession(context.getSessionId());
		return new SyncVariableDataAccess(session);
	}
}