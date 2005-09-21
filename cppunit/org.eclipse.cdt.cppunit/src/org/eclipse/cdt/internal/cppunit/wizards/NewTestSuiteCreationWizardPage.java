/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.internal.cppunit.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.cppunit.util.CppUnitLog;
import org.eclipse.cdt.internal.cppunit.util.CppUnitStatus;
import org.eclipse.cdt.internal.cppunit.util.SWTUtil;
import org.eclipse.cdt.internal.cppunit.util.Separator;
import org.eclipse.cdt.internal.cppunit.util.StatusUtil;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * Wizard page to select the test classes to include
 * in the test suite.
 */
public class NewTestSuiteCreationWizardPage extends WizardPage {

	private final static String PAGE_NAME= "NewTestSuiteCreationWizardPage"; //$NON-NLS-1$
	protected final static String PROJECT= PAGE_NAME + ".project"; //$NON-NLS-1$
	private final static String CLASSES_IN_SUITE= PAGE_NAME + ".classesinsuite"; //$NON-NLS-1$
	private final static String SUITE_NAME= PAGE_NAME + ".suitename"; //$NON-NLS-1$
	protected final static String TEST_FILE_SUFFIX= "Test"; //$NON-NLS-1$

		// C Project Containing
	private Label fProjectLabel;
	private Text fProjectText;
	private Button fProjectButton;
	private ICProject fProjectToTest;

	// Table of selected Test Classes
	private CheckboxTableViewer fClassesInSuiteTable;	
	private Button fSelectAllButton;
	private Button fDeselectAllButton;
	private Label fSelectedClassesLabel;

	// Suite Class
	private Label fSuiteNameLabel;
	private Text fSuiteNameText;
	private String fSuiteNameTextInitialValue;
	
	// CppUnit Location
	private CppUnitLocationGroup fLocations;
	
	protected IStatus fProjectStatus;
	protected IStatus fSuiteNameStatus;
	protected IStatus fClassesInSuiteStatus;
	protected IStatus fCppUnitIncludeStatus;
	protected IStatus fCppUnitLibStatus;

	private ICProject [] currentProjects;

	
	public NewTestSuiteCreationWizardPage() {
		super(PAGE_NAME);

		fSuiteNameStatus= new CppUnitStatus();
		fSuiteNameTextInitialValue= ""; //$NON-NLS-1$
		setTitle(WizardMessages.getString("NewTestSuiteWizPage.title")); //$NON-NLS-1$
		setDescription(WizardMessages.getString("NewTestSuiteWizPage.description")); //$NON-NLS-1$
		
//		String[] buttonNames= new String[] {
//			"public static void main(Strin&g[] args)", //$NON-NLS-1$
//			/* Add testrunner statement to main Method */
//			WizardMessages.getString("NewTestSWizPage.methodStub.testRunner"), //$NON-NLS-1$
//		};
		
		fClassesInSuiteStatus= new CppUnitStatus();
		fProjectStatus=new CppUnitStatus();
		fCppUnitIncludeStatus=new CppUnitStatus();
		fCppUnitLibStatus=new CppUnitStatus();

		fLocations=new CppUnitLocationGroup(this);	
		try {
			currentProjects=CoreModel.getDefault().getCModel().getCProjects();
		} catch(CModelException e) {
			CppUnitLog.error("Error getting C Projects from the workspace",e);
			currentProjects=new ICProject[0];
		}
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		
		Composite composite= new Composite(parent, SWT.NONE);
		int nColumns= 4;
		
		GridLayout layout= new GridLayout();
		layout.numColumns= nColumns;		
		composite.setLayout(layout);

		createProjectControls(composite, nColumns);	
		createSeparator(composite, nColumns);
		createSuiteNameControl(composite, nColumns);
		setTypeName("AllTests", true); //$NON-NLS-1$
		createSeparator(composite, nColumns);
		createClassesInSuiteControl(composite, nColumns);
		createSeparator(composite, nColumns);
		fLocations.createCppUnitLocation(composite,nColumns);

		setControl(composite);
		restoreWidgetValues();	
//		WorkbenchHelp.setHelp(composite, IJUnitHelpContextIds.NEW_TESTSUITE_WIZARD_PAGE);			
	}
	protected void createSeparator(Composite composite, int nColumns) {
		(new Separator(SWT.SEPARATOR | SWT.HORIZONTAL)).doFillIntoGrid(composite, nColumns, convertHeightInCharsToPixels(1));		
	}
	protected void createProjectControls(Composite composite, int nColumns) {
		fProjectLabel= new Label(composite, SWT.LEFT | SWT.WRAP);
		fProjectLabel.setFont(composite.getFont());

		fProjectLabel.setText(WizardMessages.getString("NewTestSuiteWizPage.project.label")); //$NON-NLS-1$
		GridData gd= new GridData();
		gd.horizontalSpan= 1;
		fProjectLabel.setLayoutData(gd);

		fProjectText= new Text(composite, SWT.SINGLE | SWT.BORDER);
		fProjectText.setEnabled(true);
		fProjectText.setFont(composite.getFont());
		if(fProjectToTest!=null)
		{
			fProjectText.setText(fProjectToTest.getElementName());
		}
		fProjectText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				handleFieldChanged(PROJECT);
			}
		});
		gd= new GridData();
		gd.horizontalAlignment= GridData.FILL;
		gd.grabExcessHorizontalSpace= true;
		gd.horizontalSpan= nColumns - 2;
		fProjectText.setLayoutData(gd);
		
		fProjectButton= new Button(composite, SWT.PUSH);
		fProjectButton.setText(WizardMessages.getString("NewTestSuiteWizPage.project.browse")); //$NON-NLS-1$
		fProjectButton.setEnabled(true);
		fProjectButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				ICProject p=chooseCProject();
				if(p!=null)
				{
					fProjectToTest=p;
					fProjectText.setText(fProjectToTest.getElementName());
				}
			}
			public void widgetSelected(SelectionEvent e) {
				ICProject p=chooseCProject();
				if(p!=null)
				{
					fProjectToTest=p;
					fProjectText.setText(fProjectToTest.getElementName());
				}
			}
		});	
		gd= new GridData();
		gd.horizontalAlignment= GridData.FILL;
		gd.grabExcessHorizontalSpace= false;
		gd.horizontalSpan= 1;
		gd.heightHint = SWTUtil.getButtonHeigthHint(fProjectButton);
		gd.widthHint = SWTUtil.getButtonWidthHint(fProjectButton);		
		fProjectButton.setLayoutData(gd);
	}
	protected ICProject chooseCProject()
	{
		ICProject [] projects;
		try  {
			projects=CoreModel.getDefault().getCModel().getCProjects();
		} catch(CModelException e) {
			CppUnitLog.error("Error getting C Projects from the workspace",e);
			projects=new ICProject[0];
		}

		ILabelProvider labelProvider = new CElementLabelProvider();
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setTitle("Project Selection");
		dialog.setMessage("Choose a &project");
		dialog.setElements(projects);

		ICProject cProject = getCProject();
		if (cProject != null) {
			dialog.setInitialSelections(new Object[] { cProject });
		}
		if (dialog.open() == ElementListSelectionDialog.OK) {
			return (ICProject) dialog.getFirstResult();
		}
		return null;
	}
	protected ICProject getCProject() {
		return(fProjectToTest);
	}
	protected IStatus projectChanged()
	{
		CppUnitStatus status= new CppUnitStatus();
		String projectName=fProjectText.getText().trim();
		if (projectName.length() == 0) {
			status.setError(WizardMessages.getString("NewTestSuiteWizPage.error.project.name_empty")); //$NON-NLS-1$
			return status;
		}
		fProjectToTest=null;
		for(int i=0;i<currentProjects.length;i++)
		{
			if(currentProjects[i].getElementName().equals(projectName))
			{
				fProjectToTest=currentProjects[i];
			}
		}
		if(fProjectToTest==null)
		{
			status.setError(WizardMessages.getString("NewTestSuiteWizPage.error.project.project_dnot_exist")); //$NON-NLS-1$
			return status;
		}
//		if (!project.isOpen()) {
//			status.setError(WizardMessages.getString("NewTestSuiteWizPage.error.project.project_closed")); //$NON-NLS-1$
//			return status;
//		}
		return status;		
	}

	/**
	 * Should be called from the wizard with the initial selection.
	 */
	public void init(IStructuredSelection selection)
	{
		fLocations.init(selection);
		Object o=null;
		if(!selection.isEmpty())
		{
			o=selection.getFirstElement();
		}
		if(o==null) return;
		// obj may be a IFile, IFolder an IProject or anything else not treated
		if(o instanceof IProject)
		{
			fProjectToTest=CoreModel.getDefault().create((IProject)o);
		}
		if(o instanceof ICProject)
		{
			fProjectToTest=(ICProject)o;
		}
		if(o instanceof IFolder)
		{
			ICContainer d=CoreModel.getDefault().create((IFolder)o);
			if(d!=null)
			fProjectToTest=d.getCProject();
		}
		if(o instanceof ICContainer)
		{
			ICContainer d=(ICContainer)o;
			if(d!=null)
			fProjectToTest=d.getCProject();
		}
		if(o instanceof IFile)
		{
			ICElement f=CoreModel.getDefault().create((IFile)o);
			if((f!=null)&&(f instanceof ITranslationUnit))
			{
				fProjectToTest=f.getCProject();
			}
		}
		if(o instanceof ITranslationUnit)
		{
			ITranslationUnit f=(ITranslationUnit)o;
			fProjectToTest=f.getCProject();
		}
		fLocations.init(selection);
		doStatusUpdate();
	}
	
	/**
	 * @see NewContainerWizardPage#handleFieldChanged
	 */
	protected void handleFieldChanged(String fieldName)
	{
		if(fieldName.equals(CppUnitLocationGroup.CPPUNIT_LIB_LOCATION))
		{
			fCppUnitLibStatus=fLocations.cppUnitLibLocationChanged();
		}
		else if(fieldName.equals(CppUnitLocationGroup.CPPUNIT_INCLUDE_LOCATION))
		{
			fCppUnitIncludeStatus=fLocations.cppUnitIncludeLocationChanged();
		}
		else if(fieldName.equals(PROJECT))
		{
			fProjectStatus=projectChanged();
			updateClassesInSuiteTable();
		}
		else if (fieldName.equals(CLASSES_IN_SUITE))
		{
			fClassesInSuiteStatus= classesInSuiteChanged();
			fSuiteNameStatus= testSuiteChanged(); //must check this one too
			updateSelectedClassesLabel();
		}
		else if (fieldName.equals(SUITE_NAME))
		{
			fSuiteNameStatus= testSuiteChanged();
		}
		doStatusUpdate();
	}

	protected void updateStatus(IStatus status)
	{
		setPageComplete(!status.matches(IStatus.ERROR));
		StatusUtil.applyToStatusLine(this, status);
	}
	// ------ validation --------
	private void doStatusUpdate() {
		// status of all used components
		IStatus[] status = new IStatus[] {
			fProjectStatus,
			fSuiteNameStatus,
			fClassesInSuiteStatus,
			fCppUnitIncludeStatus,
			fCppUnitLibStatus
		};

		// the mode severe status will be displayed and the ok button enabled/disabled.
		IStatus s=StatusUtil.getMostSevere(status);
		updateStatus(s);
	}

	/**
	 * @see DialogPage#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			setFocus();		
			updateClassesInSuiteTable();
			handleAllFieldsChanged();
		}
	}

	private void handleAllFieldsChanged() {
		handleFieldChanged(PROJECT);
		handleFieldChanged(SUITE_NAME);
		handleFieldChanged(CLASSES_IN_SUITE);
	}

	protected void updateClassesInSuiteTable() {
		if (fClassesInSuiteTable != null)
		{
			if(fProjectToTest!=null)
			{
				fClassesInSuiteTable.setInput(fProjectToTest);
				fClassesInSuiteTable.setAllChecked(true);
				updateSelectedClassesLabel();	
			}
		}
	}
	
	protected void createClassesInSuiteControl(Composite parent, int nColumns)
	{
		if (fClassesInSuiteTable == null)
		{
			Label label = new Label(parent, SWT.LEFT);
			label.setText(WizardMessages.getString("NewTestSuiteWizPage.classes_in_suite.label")); //$NON-NLS-1$
			GridData gd= new GridData();
			gd.horizontalAlignment = GridData.FILL;
			gd.horizontalSpan= nColumns;
			label.setLayoutData(gd);

			fClassesInSuiteTable= CheckboxTableViewer.newCheckList(parent, SWT.BORDER);
			gd= new GridData(GridData.FILL_BOTH);
			gd.heightHint= 80;
			gd.horizontalSpan= nColumns-1;

			fClassesInSuiteTable.getTable().setLayoutData(gd);
			fClassesInSuiteTable.setContentProvider(new ClassesInSuitContentProvider());
			fClassesInSuiteTable.setLabelProvider(new CElementLabelProvider());
			fClassesInSuiteTable.addCheckStateListener(new ICheckStateListener() {
				public void checkStateChanged(CheckStateChangedEvent event) {
					handleFieldChanged(CLASSES_IN_SUITE);
				}
			});

			Composite buttonContainer= new Composite(parent, SWT.NONE);
			gd= new GridData(GridData.FILL_VERTICAL);
			buttonContainer.setLayoutData(gd);
			GridLayout buttonLayout= new GridLayout();
			buttonLayout.marginWidth= 0;
			buttonLayout.marginHeight= 0;
			buttonContainer.setLayout(buttonLayout);
	
			fSelectAllButton= new Button(buttonContainer, SWT.PUSH);
			fSelectAllButton.setText(WizardMessages.getString("NewTestSuiteWizPage.selectAll")); //$NON-NLS-1$
			GridData bgd= new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
			bgd.heightHint = SWTUtil.getButtonHeigthHint(fSelectAllButton);
			bgd.widthHint = SWTUtil.getButtonWidthHint(fSelectAllButton);
			fSelectAllButton.setLayoutData(bgd);
			fSelectAllButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					fClassesInSuiteTable.setAllChecked(true);
					handleFieldChanged(CLASSES_IN_SUITE);
				}
			});
	
			fDeselectAllButton= new Button(buttonContainer, SWT.PUSH);
			fDeselectAllButton.setText(WizardMessages.getString("NewTestSuiteWizPage.deselectAll")); //$NON-NLS-1$
			bgd= new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
			bgd.heightHint = SWTUtil.getButtonHeigthHint(fDeselectAllButton);
			bgd.widthHint = SWTUtil.getButtonWidthHint(fDeselectAllButton);
			fDeselectAllButton.setLayoutData(bgd);
			fDeselectAllButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					fClassesInSuiteTable.setAllChecked(false);
					handleFieldChanged(CLASSES_IN_SUITE);
				}
			});

			// No of selected classes label
			fSelectedClassesLabel= new Label(parent, SWT.LEFT | SWT.WRAP);
			fSelectedClassesLabel.setFont(parent.getFont());
			updateSelectedClassesLabel();
			gd = new GridData();
			gd.horizontalSpan = 2;
			fSelectedClassesLabel.setLayoutData(gd);
		}
	}

	static class ClassesInSuitContentProvider implements IStructuredContentProvider {
			
		public Object[] getElements(Object parent) {
			if(parent instanceof ICProject) return getTestClassList((ICProject)parent);
			else return new Object[0];
		}
		ITranslationUnit [] getTestClassList(ICElement r) {
			Vector v=new Vector();
			try {
				if(r instanceof IParent) {
					IParent p=(IParent)r;
					ICElement [] elements=p.getChildren();
					for(int i=0;i<elements.length;i++) {
						if(elements[i] instanceof IParent) {
							ITranslationUnit [] x=getTestClassList(elements[i]);
							for(int j=0;j<x.length;j++) {
								v.add(x[j]);
							}
						}
						if(elements[i] instanceof ITranslationUnit) {
							IPath path=elements[i].getPath().removeFileExtension();
							String lastSegment=path.lastSegment();
	ITranslationUnit unit=(ITranslationUnit)elements[i];
//TODO : Check if inherits from TestFixture would be the properTest !!!
							if(lastSegment.endsWith(TEST_FILE_SUFFIX)) {
								v.add(elements[i]);
							}
						}
					}
				}
			} catch(CModelException e) {
				CppUnitLog.error("",e);
			}
			return (ITranslationUnit [])v.toArray(new ITranslationUnit[0]);
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	/**
	 * Runnable for replacing an existing suite() method.
	 */
	public IRunnableWithProgress getRunnable() {
		return new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
			{
					if (monitor == null) {
						monitor= new NullProgressMonitor();
					}
			}
		};
	}
	
//	protected void updateExistingClass(IProgressMonitor monitor) throws CoreException, InterruptedException {
//		try {
//			IPackageFragment pack= getPackageFragment();
//			ICompilationUnit cu= pack.getCompilationUnit(getTypeName() + ".java"); //$NON-NLS-1$
//			
//			if (!cu.exists()) {
//				createType(monitor);
//				fUpdatedExistingClassButton= false;
//				return;
//			}
//			
//			if (! UpdateTestSuite.checkValidateEditStatus(cu, getShell()))
//				return;
//
//			IType suiteType= cu.getType(getTypeName());
//			monitor.beginTask(WizardMessages.getString("NewTestSuiteWizPage.createType.beginTask"), 10); //$NON-NLS-1$
//			IMethod suiteMethod= suiteType.getMethod("suite", new String[] {}); //$NON-NLS-1$
//			monitor.worked(1);
//			
//			String lineDelimiter= JUnitStubUtility.getLineDelimiterUsed(cu);
//			if (suiteMethod.exists()) {
//				ISourceRange range= suiteMethod.getSourceRange();
//				if (range != null) {
//					IBuffer buf= cu.getBuffer();
//					String originalContent= buf.getText(range.getOffset(), range.getLength());
//					StringBuffer source= new StringBuffer(originalContent);
//					//using JDK 1.4
//					//int start= source.toString().indexOf(START_MARKER) --> int start= source.indexOf(START_MARKER);
//					int start= source.toString().indexOf(START_MARKER);
//					if (start > -1) {
//						//using JDK 1.4
//						//int end= source.toString().indexOf(END_MARKER, start) --> int end= source.indexOf(END_MARKER, start)
//						int end= source.toString().indexOf(END_MARKER, start);
//						if (end > -1) {
//							monitor.subTask(WizardMessages.getString("NewTestSuiteWizPage.createType.updating.suite_method")); //$NON-NLS-1$
//							monitor.worked(1);
//							end += END_MARKER.length();
//							source.replace(start, end, getUpdatableString());
//							buf.replace(range.getOffset(), range.getLength(), source.toString());
//							cu.reconcile();  
//							originalContent= buf.getText(0, buf.getLength());
//							monitor.worked(1);
//							String formattedContent=
//								JUnitStubUtility.codeFormat(originalContent, 0, lineDelimiter);
//							buf.replace(0, buf.getLength(), formattedContent);
//							monitor.worked(1);
//							cu.save(new SubProgressMonitor(monitor, 1), false);
//						} else {
//							cannotUpdateSuiteError();
//						}
//					} else {
//						cannotUpdateSuiteError();
//					}
//				} else {
//					MessageDialog.openError(getShell(), WizardMessages.getString("NewTestSuiteWizPage.createType.updateErrorDialog.title"), WizardMessages.getString("NewTestSuiteWizPage.createType.updateErrorDialog.message")); //$NON-NLS-1$ //$NON-NLS-2$
//				}
//			} else {
//				suiteType.createMethod(getSuiteMethodString(), null, true, monitor);
//				ISourceRange range= cu.getSourceRange();
//				IBuffer buf= cu.getBuffer();
//				String originalContent= buf.getText(range.getOffset(), range.getLength());
//				monitor.worked(2);
//				String formattedContent=
//					JUnitStubUtility.codeFormat(originalContent, 0, lineDelimiter);
//				buf.replace(range.getOffset(), range.getLength(), formattedContent);
//				monitor.worked(1);
//				cu.save(new SubProgressMonitor(monitor, 1), false);
//			}
//			monitor.done();
//			fUpdatedExistingClassButton= true;
//		} catch (JavaModelException e) {
//			String title= WizardMessages.getString("NewTestSuiteWizPage.error_tile"); //$NON-NLS-1$
//			String message= WizardMessages.getString("NewTestSuiteWizPage.error_message"); //$NON-NLS-1$
//			ExceptionHandler.handle(e, getShell(), title, message);
//		}
//	}

	/**
	 * Returns true iff an existing suite() method has been replaced.
	 */
	public boolean hasUpdatedExistingClass() {
//		return fUpdatedExistingClassButton;
		return true;
	}
	
	private IStatus classesInSuiteChanged() {
		CppUnitStatus status= new CppUnitStatus();
		if (fClassesInSuiteTable.getCheckedElements().length <= 0)
			status.setWarning(WizardMessages.getString("NewTestSuiteWizPage.classes_in_suite.error.no_testclasses_selected")); //$NON-NLS-1$
		return status;
	}
	
	private void updateSelectedClassesLabel() {
		int noOfClassesChecked= fClassesInSuiteTable.getCheckedElements().length;
		String key= (noOfClassesChecked==1) ? "NewTestClassWizPage.treeCaption.classSelected" : "NewTestClassWizPage.treeCaption.classesSelected"; //$NON-NLS-1$ //$NON-NLS-2$
		fSelectedClassesLabel.setText(WizardMessages.getFormattedString(key, new Integer(noOfClassesChecked)));
	}

	protected void createSuiteNameControl(Composite composite, int nColumns) {
		fSuiteNameLabel= new Label(composite, SWT.LEFT | SWT.WRAP);
		fSuiteNameLabel.setFont(composite.getFont());
		fSuiteNameLabel.setText(WizardMessages.getString("NewTestSuiteWizPage.suiteName.text")); //$NON-NLS-1$
		GridData gd= new GridData();
		gd.horizontalSpan= 1;
		fSuiteNameLabel.setLayoutData(gd);

		fSuiteNameText= new Text(composite, SWT.SINGLE | SWT.BORDER);
		// moved up due to 1GEUNW2
		fSuiteNameText.setEnabled(true);
		fSuiteNameText.setFont(composite.getFont());
		fSuiteNameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				handleFieldChanged(SUITE_NAME);
			}
		});
		gd= new GridData();
		gd.horizontalAlignment= GridData.FILL;
		gd.grabExcessHorizontalSpace= true;
		gd.horizontalSpan= nColumns - 2;
		fSuiteNameText.setLayoutData(gd);
		
		Label space= new Label(composite, SWT.LEFT);
		space.setText(" "); //$NON-NLS-1$
		gd= new GridData();
		gd.horizontalSpan= 1;
		space.setLayoutData(gd);		
	}
	
	/**
	 * Gets the type name.
	 */
	public String getTypeName() {
		return (fSuiteNameText==null)?fSuiteNameTextInitialValue:fSuiteNameText.getText();
	}
	
	/**
	 * Sets the type name.
	 * @param canBeModified Selects if the type name can be changed by the user
	 */	
	public void setTypeName(String name, boolean canBeModified) {
		if (fSuiteNameText == null) {
			fSuiteNameTextInitialValue= name;
		} else {
			fSuiteNameText.setText(name);
			fSuiteNameText.setEnabled(canBeModified);
		}
	}	

	/**
	 * Called when the type name has changed.
	 * The method validates the type name and returns the status of the validation.
	 * Can be extended to add more validation
	 */
	protected IStatus testSuiteChanged()
	{
		CppUnitStatus status= new CppUnitStatus();
		String typeName= getTypeName();
		// must not be empty
		if (typeName.length() == 0)
		{
			status.setError(WizardMessages.getString("NewTestSuiteWizPage.typeName.error.name_empty")); //$NON-NLS-1$
			return status;
		}
		int indexOfTest=typeName.lastIndexOf("Tests");
		if(indexOfTest==-1)
		{
			status.setError(WizardMessages.getString("NewTestSuiteWizPage.typeName.error.dnot_contain_test")); //$NON-NLS-1$
			return status;
		}
		if(typeName.endsWith("Tests")==false)
		{
			status.setError(WizardMessages.getString("NewTestSuiteWizPage.typeName.error.dnot_contain_test")); //$NON-NLS-1$
			return status;
		}
		int index=typeName.lastIndexOf(".");
		if (index != -1)
		{
			status.setError(WizardMessages.getString("NewTestSuiteWizPage.typeName.error.name_contains_dot")); //$NON-NLS-1$
			return status;
		}
//		if (index == -1)
//		{
//			status.setError(WizardMessages.getString("NewTestSuiteWizPage.typeName.error.name_extension_empty")); //$NON-NLS-1$
//			return status;
//		}
//		String s=typeName.substring(index+1);
//		String [] srcExt=CoreModel.getDefault().getSourceExtensions();
//		boolean found=false;
//		for(int i=0;i<srcExt.length;i++)
//		{
//			if(srcExt[i].equals(s))
//			{
//				found=true;
//			}
//		}
//		if(found==false)
//		{
//			status.setError(WizardMessages.getString("NewTestSuiteWizPage.typeName.error.name_extension_incoherent")); //$NON-NLS-1$
//			return status;
//		}
		return status;
	}
	/**
	 * Sets the focus.
	 */		
	protected void setFocus() {
		fSuiteNameText.setFocus();
	}

	/**
	 * Sets the classes in <code>elements</code> as checked.
	 */	
	public void setCheckedElements(Object[] elements) {
		fClassesInSuiteTable.setCheckedElements(elements);
	}
	
	/**
	 *	Use the dialog store to restore widget values to the values that they held
	 *	last time this wizard was used to completion
	 */
	private void restoreWidgetValues() {
		IDialogSettings settings= getDialogSettings();
		if (settings != null) {
//			boolean generateMain= settings.getBoolean(STORE_GENERATE_MAIN);
//			fMethodStubsButtons.setSelection(0, generateMain);
//			fMethodStubsButtons.setEnabled(1, generateMain);
//			fMethodStubsButtons.setSelection(1,settings.getBoolean(STORE_USE_TESTRUNNER));
//			//The next 2 lines are necessary. Otherwise, if fMethodsStubsButtons is disabled, and USE_TESTRUNNER gets enabled,
//			//then the checkbox for USE_TESTRUNNER will be the only enabled component of fMethodsStubsButton
//			fMethodStubsButtons.setEnabled(!fMethodStubsButtons.isEnabled());
//			fMethodStubsButtons.setEnabled(!fMethodStubsButtons.isEnabled());
//			try {
//				fMethodStubsButtons.setComboSelection(settings.getInt(STORE_TESTRUNNER_TYPE));
//			} catch(NumberFormatException e) {}
		}		
	}	

	/**
	 * 	Since Finish was pressed, write widget values to the dialog store so that they
	 *	will persist into the next invocation of this wizard page
	 */
	void saveWidgetValues() {
		IDialogSettings settings= getDialogSettings();
		if (settings != null) {
//			settings.put(STORE_GENERATE_MAIN, fMethodStubsButtons.isSelected(0));
//			settings.put(STORE_USE_TESTRUNNER, fMethodStubsButtons.isSelected(1));
//			settings.put(STORE_TESTRUNNER_TYPE, fMethodStubsButtons.getComboSelection());
		}
	}	
	protected ITranslationUnit [] getCheckedTestClasses()
	{
		Object [] tab=fClassesInSuiteTable.getCheckedElements();
		ITranslationUnit [] testClasses=new ITranslationUnit[tab.length];
		for(int i=0;i<tab.length;i++)
		{
			if(tab[i] instanceof ITranslationUnit)
			{			
				testClasses[i]=(ITranslationUnit)tab[i];
			}
		}
		return testClasses;
	}
	public String getCppUnitIncludeLocation()
	{
		return(fLocations.getCppUnitIncludeLocation());
	}
	public String getCppUnitLibLocation()
	{
		return(fLocations.getCppUnitLibLocation());
	}

}
