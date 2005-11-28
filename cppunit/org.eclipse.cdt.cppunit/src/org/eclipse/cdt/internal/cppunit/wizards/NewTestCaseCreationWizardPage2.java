/*
 * (c) Copyright IBM Corp. 2003.
 * Created on 27 f�vr. 03
 * All Rights Reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 *
 * Contributors:
 * IBM Software Group - Rational Software
 */

package org.eclipse.cdt.internal.cppunit.wizards;

import java.util.ArrayList;
import java.util.Vector;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IFunction;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.core.model.INamespace;
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.cppunit.util.CppUnitLog;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;

/**
 * Wizard page to select the methods from a class under test.
 */
public class NewTestCaseCreationWizardPage2 extends WizardPage
{

	private final static String PAGE_NAME= "NewTestCaseCreationWizardPage2"; //$NON-NLS-1$
	private final static String STORE_USE_TASKMARKER= PAGE_NAME + ".USE_TASKMARKER"; //$NON-NLS-1$
//	private final static String STORE_CREATE_FINAL_METHOD_STUBS= PAGE_NAME + ".CREATE_FINAL_METHOD_STUBS"; //$NON-NLS-1$
	public final static String PREFIX= "test"; //$NON-NLS-1$

	private NewTestCaseCreationWizardPage fFirstPage;	
	private IStructure fClassToTest;

	private Button fCreateTasksButton;
	private ContainerCheckedTreeViewer fMethodsTree;
	private Button fSelectAllButton;
	private Button fDeselectAllButton;
	private Label fSelectedMethodsLabel;
 
	/**
	 * Constructor for NewTestCaseCreationWizardPage2.
	 */
	protected NewTestCaseCreationWizardPage2(NewTestCaseCreationWizardPage firstPage) {
		super(PAGE_NAME);
		fFirstPage= firstPage;
		setTitle(WizardMessages.getString("NewTestClassWizPage2.title")); //$NON-NLS-1$
		setDescription(WizardMessages.getString("NewTestClassWizPage2.description")); //$NON-NLS-1$
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		container.setLayout(layout);

		createMethodsTreeControls(container);
		createSpacer(container);
		createFinalMethodStubsControls(container);
		createTasksControls(container);
		setControl(container);
		restoreWidgetValues();
	}

	protected void createFinalMethodStubsControls(Composite container) {
		GridLayout layout;
		GridData gd;
		Composite prefixContainer= new Composite(container, SWT.NONE);
		gd= new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.horizontalSpan = 2;
		prefixContainer.setLayoutData(gd);
		
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		prefixContainer.setLayout(layout);
		
	}
	
	protected void createTasksControls(Composite container) {
		GridLayout layout;
		GridData gd;
		Composite prefixContainer= new Composite(container, SWT.NONE);
		gd= new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.horizontalSpan = 2;
		prefixContainer.setLayoutData(gd);
		
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		prefixContainer.setLayout(layout);
		
		fCreateTasksButton= new Button(prefixContainer, SWT.CHECK | SWT.LEFT);
		fCreateTasksButton.setText(WizardMessages.getString("NewTestClassWizPage2.create_tasks.text")); //$NON-NLS-1$
		fCreateTasksButton.setEnabled(true);
		fCreateTasksButton.setSelection(true);
		gd= new GridData();
		gd.horizontalAlignment= GridData.FILL;
		gd.horizontalSpan= 2;
		fCreateTasksButton.setLayoutData(gd);
	}

	protected void createMethodsTreeControls(Composite container) {
		Label label= new Label(container, SWT.LEFT | SWT.WRAP);
		label.setFont(container.getFont());
		label.setText(WizardMessages.getString("NewTestClassWizPage2.methods_tree.label")); //$NON-NLS-1$
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);

		fMethodsTree= new ContainerCheckedTreeViewer(container, SWT.BORDER);
		gd= new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		gd.heightHint= 180;
		fMethodsTree.getTree().setLayoutData(gd);

//		fMethodsTree.setLabelProvider(new AppearanceAwareLabelProvider());
		fMethodsTree.setLabelProvider(new CElementLabelProvider());
		fMethodsTree.setAutoExpandLevel(2);			
		fMethodsTree.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				updateSelectedMethodsLabel();
			}	
		});
		Composite buttonContainer= new Composite(container, SWT.NONE);
		gd= new GridData(GridData.FILL_VERTICAL);
		buttonContainer.setLayoutData(gd);
		GridLayout buttonLayout= new GridLayout();
		buttonLayout.marginWidth= 0;
		buttonLayout.marginHeight= 0;
		buttonContainer.setLayout(buttonLayout);

		fSelectAllButton= new Button(buttonContainer, SWT.PUSH);
		fSelectAllButton.setText(WizardMessages.getString("NewTestClassWizPage2.selectAll")); //$NON-NLS-1$
		gd= new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		fSelectAllButton.setLayoutData(gd);
		fSelectAllButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fMethodsTree.setCheckedElements((Object[]) fMethodsTree.getInput());
				updateSelectedMethodsLabel();
			}
		});

		fDeselectAllButton= new Button(buttonContainer, SWT.PUSH);
		fDeselectAllButton.setText(WizardMessages.getString("NewTestClassWizPage2.deselectAll")); //$NON-NLS-1$
		gd= new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		fDeselectAllButton.setLayoutData(gd);
		fDeselectAllButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fMethodsTree.setCheckedElements(new Object[0]);
				updateSelectedMethodsLabel();
			}
		});

		/* No of selected methods label */
		fSelectedMethodsLabel= new Label(container, SWT.LEFT);
		fSelectedMethodsLabel.setFont(container.getFont());
		updateSelectedMethodsLabel();
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan= 1;
		fSelectedMethodsLabel.setLayoutData(gd);
		
		Label emptyLabel= new Label(container, SWT.LEFT);
		gd= new GridData();
		gd.horizontalSpan= 1;
		emptyLabel.setLayoutData(gd);
	}

	protected void createSpacer(Composite container) {
		Label spacer= new Label(container, SWT.NONE);
		GridData data= new GridData();
		data.horizontalSpan= 2;
		data.horizontalAlignment= GridData.FILL;
		data.verticalAlignment= GridData.BEGINNING;
		data.heightHint= 4;
		spacer.setLayoutData(data);
	}

	/**
	 * @see DialogPage#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		ITranslationUnit f=fFirstPage.getFileToTest();
		if (visible) {
			try {
				ArrayList types=new ArrayList();
				ICElement [] elements=f.getChildren();
				for(int i=0;i<elements.length;i++) {
					if(	(elements[i] instanceof IFunctionDeclaration)||
							(elements[i] instanceof INamespace)||
							(elements[i] instanceof IStructure)) {
						types.add(elements[i]);
					}
				}
				fMethodsTree.setContentProvider(new MethodsTreeContentProvider(types.toArray()));
				fMethodsTree.setInput(types.toArray());
				updateSelectedMethodsLabel();
				setFocus();
			} catch(CModelException e) {
				CppUnitLog.error("",e);
			}
		}
	}

	/**
	 * Returns all checked methods in the Methods tree.
	 */
	public IFunctionDeclaration[] getCheckedFunctions()
	{
		Object[] checkedObjects= fMethodsTree.getCheckedElements();
		Vector v=new Vector();
		for (int i = 0; i < checkedObjects.length; i++)
		{
			if(checkedObjects[i] instanceof IFunctionDeclaration)
			{
				v.add(checkedObjects[i]);
			}
		}
		IFunctionDeclaration [] ret=new IFunctionDeclaration[v.size()];
		for(int i=0;i<v.size();i++)
		{
			ret[i]=(IFunctionDeclaration)v.elementAt(i);
		}
		return ret;
	}
	
	private static class MethodsTreeContentProvider implements ITreeContentProvider
	{
		private Object[] dads;
		private final Object[] fEmpty= new Object[0];

		public MethodsTreeContentProvider(Object[] types)
		{
			dads= types;
		}
		/*
		 * @see ITreeContentProvider#getChildren(Object)
		 */
		public Object[] getChildren(Object parentElement)
		{
			if (parentElement instanceof IStructure)
			{
				return(getMethodsAndStructs((IStructure)parentElement));
			}
			if (parentElement instanceof INamespace)
			{
				return(getMethodsAndStructs((INamespace)parentElement));
			}
			return fEmpty;
		}

		public Object [] getMethodsAndStructs(INamespace s) {
			try {
				Vector result=new Vector();
				ICElement [] elements=s.getChildren();
				for(int i=0;i<elements.length;i++) {
				if ((elements[i] instanceof IStructure)||
					(elements[i] instanceof INamespace) ||
					(elements[i] instanceof IFunctionDeclaration)) {
						result.add(elements[i]);
					}
				}
				return result.toArray();
			} catch (CModelException e) {
				CppUnitLog.error("",e);
				return (new Object[0]);
			}
		}
		public Object [] getMethodsAndStructs(IStructure s) {
			try {
				Vector result=new Vector();
				ICElement [] elements=s.getChildren();
				for(int i=0;i<elements.length;i++) {
					if ((elements[i] instanceof IStructure)||
							(elements[i] instanceof IFunctionDeclaration)) {
						result.add(elements[i]);
					}
				}
				return result.toArray();
			} catch(CModelException e) {
				CppUnitLog.error("",e);
				return (new Object[0]);
			}
		}

		/*
		 * @see ITreeContentProvider#getParent(Object)
		 */
		public Object getParent(Object element) {
			if (element instanceof ICElement) 
				return ((ICElement)element).getParent();
			return null;
		}

		/*
		 * @see ITreeContentProvider#hasChildren(Object)
		 */
		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}

		/*
		 * @see IStructuredContentProvider#getElements(Object)
		 */
		public Object[] getElements(Object inputElement) {
			return dads;
		}

		/*
		 * @see IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/*
		 * @see IContentProvider#inputChanged(Viewer, Object, Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	/**
	 * Returns true if the checkbox for creating tasks is checked.
	 */
	public boolean getCreateTasksButtonSelection() {
		return fCreateTasksButton.getSelection();
	}

	/**
	 * Returns true if the checkbox for final method stubs is checked.
	 */
	private void updateSelectedMethodsLabel() {
		Object[] checked= fMethodsTree.getCheckedElements();
		int checkedMethodCount= 0;
		for (int i= 0; i < checked.length; i++) {
			if ((checked[i] instanceof IFunction)||(checked[i] instanceof IFunctionDeclaration))
				checkedMethodCount++;
		}
		String label= ""; //$NON-NLS-1$
		if (checkedMethodCount == 1)
			label= WizardMessages.getFormattedString("NewTestClassWizPage2.selected_methods.label_one", new Integer(checkedMethodCount)); //$NON-NLS-1$
		else
			label= WizardMessages.getFormattedString("NewTestClassWizPage2.selected_methods.label_many", new Integer(checkedMethodCount)); //$NON-NLS-1$
		fSelectedMethodsLabel.setText(label);
	}
	
	/**
	 * Sets the focus on the type name.
	 */		
	protected void setFocus() {
		fMethodsTree.getControl().setFocus();
	}
		
	/**
	 *	Use the dialog store to restore widget values to the values that they held
	 *	last time this wizard was used to completion
	 */
	private void restoreWidgetValues() {
		IDialogSettings settings= getDialogSettings();
		if (settings != null) {
			fCreateTasksButton.setSelection(settings.getBoolean(STORE_USE_TASKMARKER));
		}		
	}	

	/**
	 * 	Since Finish was pressed, write widget values to the dialog store so that they
	 *	will persist into the next invocation of this wizard page
	 */
	void saveWidgetValues() {
		IDialogSettings settings= getDialogSettings();
		if (settings != null) {
			settings.put(STORE_USE_TASKMARKER, fCreateTasksButton.getSelection());
		}
	}
}
