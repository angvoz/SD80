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
package org.eclipse.cdt.debug.edc.internal.ui;

import java.text.MessageFormat;

import org.eclipse.cdt.debug.edc.formatter.IVariableValueConverter;
import org.eclipse.cdt.debug.edc.internal.formatter.FormatExtensionManager;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Expressions.ExpressionDMC;
import org.eclipse.cdt.debug.edc.internal.symbols.IType;
import org.eclipse.cdt.debug.edc.internal.symbols.TypeUtils;
import org.eclipse.cdt.debug.edc.ui.EDCDebugUI;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
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
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.console.actions.TextViewerAction;

@SuppressWarnings("restriction")
public class CustomFormatDetailPane extends AbstractDetailPane implements IPropertyChangeListener {
	
	public static final String ID = "CustomFormatDetailPane";
	public static final String DESCRIPTION = "A detail pane to display custom formats";
	public static final String NAME = "Custom Format Details";
	
	protected static final String COPY_ACTION = ActionFactory.COPY.getId() + ".SourceDetailPane"; //$NON-NLS-1$
	protected static final String SELECT_ALL_ACTION = IDebugView.SELECT_ALL_ACTION + ".SourceDetailPane"; //$NON-NLS-1$
	protected static final String WORD_WRAP_ACTION = IDebugPreferenceConstants.PREF_DETAIL_PANE_WORD_WRAP;


	public class WordWrapAction extends Action {

		public WordWrapAction() {
			super(ActionMessages.DetailPaneWordWrapAction_0, IAction.AS_CHECK_BOX);
			setEnabled(true);
			
			boolean prefSetting = DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(WORD_WRAP_ACTION);
			viewer.getTextWidget().setWordWrap(prefSetting);
			setChecked(prefSetting);
		}
		
		@SuppressWarnings("deprecation")
		public void run() {
			viewer.getTextWidget().setWordWrap(isChecked());
			DebugUIPlugin.getDefault().getPreferenceStore().setValue(WORD_WRAP_ACTION, isChecked());
			DebugUIPlugin.getDefault().savePluginPreferences();
		}
	}

	public class DetailJob extends Job {
		private class GetCustomValueQuery extends Query<String> {
			@Override
			protected void execute(final DataRequestMonitor<String> rm) {
				try {
					rm.setData(customConverter.getValue(expressionDMC));
				} catch (Throwable t) {
					rm.setStatus(new Status(IStatus.ERROR, EDCDebugUI.PLUGIN_ID, t.getLocalizedMessage()));
				}
				finally {
					rm.done();
				}
			}
		}
		
		private ExpressionDMC expressionDMC;
		private IVariableValueConverter customConverter;

		public DetailJob(IStructuredSelection selection) {
			super("compute variable details");
			setExpressionDMC(getExpressionFromSelectedElement(selection.getFirstElement()));
			setCustomConverter(getCustomConverter(expressionDMC));
		}

		private void setCustomConverter(IVariableValueConverter customConverter) {
			this.customConverter = customConverter;
		}

		private void setExpressionDMC(ExpressionDMC expressionDMC) {
			this.expressionDMC = expressionDMC;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			GetCustomValueQuery customValueQuery = new GetCustomValueQuery();
			expressionDMC.getService().getExecutor().execute(customValueQuery);
			String text;
			try {
				text = customValueQuery.get();
			} catch (final Throwable e) {
				text = MessageFormat.format("Could not get custom value due to: {0}", e.getMessage());
			}
			final String _text = text;
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					document.set(_text);
				}
			});
			return Status.OK_STATUS;
		}

		@Override
		protected void canceling() {
			super.canceling();
			synchronized (this) {
				notifyAll();
			}
		}
	}

	private TextViewer viewer;
	private Document document;
	private DetailJob detailJob;
	
	public Control createControl(Composite parent) {
        Control control = createViewer(parent);
        
		if (isInView()){
			createViewSpecificComponents();
			createActions();
			DebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
			JFaceResources.getFontRegistry().addListener(this);
		}
		
		return control;  
	}

	private Control createViewer(Composite parent) {
		viewer = new TextViewer(parent, SWT.V_SCROLL | SWT.H_SCROLL);
        viewer.setDocument(getDocument());
        viewer.getTextWidget().setFont(JFaceResources.getFont(IDebugUIConstants.PREF_DETAIL_PANE_FONT));
        viewer.getTextWidget().setWordWrap(DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(WORD_WRAP_ACTION));
        viewer.setEditable(false);
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
		
		if (selection == null){
			clearSourceViewer();
			return;
		}
				
		if (selection.isEmpty()){
			clearSourceViewer();
			return;
		}
		
        synchronized (this) {
        	if (detailJob != null) {
        		detailJob.cancel();
        	}
			detailJob = new DetailJob(selection);
			detailJob.schedule();
        }
		
	}

	private void clearSourceViewer() {
		if (detailJob != null) {
			detailJob.cancel();
		}
		document.set(""); //$NON-NLS-1$
	}

	public String getDescription() {
		return DESCRIPTION;
	}

	public String getID() {
		return ID;
	}

	public String getName() {
		return NAME;
	}

	public boolean setFocus() {
		if (viewer != null){
			viewer.getTextWidget().setFocus();
			return true;
		}
		return false;
	}
	
	private void createViewSpecificComponents() {
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
				getViewSite().getActionBars().updateActionBars();
			}
			
			public void focusLost(FocusEvent e) {
				setGlobalAction(IDebugView.SELECT_ALL_ACTION, null);
				setGlobalAction(IDebugView.COPY_ACTION, null);
				getViewSite().getActionBars().updateActionBars();
				
			}
		});

		createContextMenu(viewer.getTextWidget());
	}

	protected void createContextMenu(Control menuControl) {
		MenuManager menuMgr= new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				fillContextMenu(mgr);
			}
		});
		Menu menu= menuMgr.createContextMenu(menuControl);
		menuControl.setMenu(menu);

		getViewSite().registerContextMenu(IDebugUIConstants.VARIABLE_VIEW_DETAIL_ID, menuMgr, viewer.getSelectionProvider());

	}

	protected void fillContextMenu(IMenuManager menu) {
		menu.add(new Separator(IDebugUIConstants.VARIABLE_GROUP));
		menu.add(getAction(COPY_ACTION));
		menu.add(getAction(SELECT_ALL_ACTION));
		menu.add(new Separator());
		menu.add(getAction(WORD_WRAP_ACTION));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void createActions() {
		TextViewerAction textAction = new TextViewerAction(viewer, ITextOperationTarget.SELECT_ALL);
		textAction.setText("Select &All");
		textAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_SELECT_ALL);
		setAction(SELECT_ALL_ACTION, textAction);
		
		textAction = new TextViewerAction(viewer, ITextOperationTarget.COPY);
		textAction.setText("&Copy");
		textAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_COPY);
		setAction(COPY_ACTION, textAction);
		
		setSelectionDependantAction(COPY_ACTION);
		updateSelectionDependentActions();
		
		IAction action = new WordWrapAction();
		setAction(WORD_WRAP_ACTION, action);
	}

	public void propertyChange(PropertyChangeEvent event) {
		String propertyName= event.getProperty();
		if (propertyName.equals(IDebugUIConstants.PREF_DETAIL_PANE_FONT)) {
			viewer.getTextWidget().setFont(JFaceResources.getFont(IDebugUIConstants.PREF_DETAIL_PANE_FONT));
		} else if (propertyName.equals(WORD_WRAP_ACTION)) {
			viewer.getTextWidget().setWordWrap(DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(WORD_WRAP_ACTION));
			getAction(WORD_WRAP_ACTION).setChecked(DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(WORD_WRAP_ACTION));
		}
		
	}

	public void dispose(){
		if (detailJob != null) detailJob.cancel();
		if (viewer != null && viewer.getControl() != null) viewer.getControl().dispose();
		
		if (isInView()){
			DebugUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
			JFaceResources.getFontRegistry().removeListener(this);
		}
		
		super.dispose();
	}
	
	public static ExpressionDMC getExpressionFromSelectedElement(Object element) {
		if (element instanceof IAdaptable) {
			IExpressionDMContext expression = 
				(IExpressionDMContext) ((IAdaptable) element).getAdapter(IExpressionDMContext.class);
			if (expression instanceof ExpressionDMC) {
				return (ExpressionDMC) expression;
			}
		}
		return null;
	}

	public static IVariableValueConverter getCustomConverter(ExpressionDMC expressionDMC) {
		IType exprType = TypeUtils.getStrippedType(expressionDMC.getEvaluatedType());
		IVariableValueConverter customConverter = 
			FormatExtensionManager.instance().getDetailValueConverter(exprType);
		return customConverter;
	}

}