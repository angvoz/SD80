/*
 * (c) Copyright IBM Corp. 2003.
 * Created on 27 févr. 03
 * All Rights Reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 *
 * Contributors:
 * IBM Software Group - Rational Software
 */

package org.eclipse.cdt.internal.cppunit.wizards;

import java.util.HashMap;
import java.util.Vector;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.cppunit.util.CppUnitLog;
import org.eclipse.cdt.internal.cppunit.util.CppUnitStatus;
import org.eclipse.cdt.internal.cppunit.util.SWTUtil;
import org.eclipse.cdt.internal.cppunit.util.Separator;
import org.eclipse.cdt.internal.cppunit.util.StatusUtil;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
/**
 * The first page of the TestCase creation wizard. 
 */
public class NewTestCaseCreationWizardPage extends WizardPage {

	protected final static String PAGE_NAME= "NewTestCaseCreationWizardPage"; //$NON-NLS-1$
	protected final static String PROJECT= PAGE_NAME + ".project"; //$NON-NLS-1$
	protected final static String CLASS_TO_TEST= PAGE_NAME + ".classtotest"; //$NON-NLS-1$
	protected final static String TEST_CLASS= PAGE_NAME + ".testclass"; //$NON-NLS-1$
	protected final static String TEST_FILE_SUFFIX= "Test"; //$NON-NLS-1$
	protected final static String TEST_METHOD_SUFFIX= "test"; //$NON-NLS-1$

	private NewTestCaseCreationWizardPage2 fPage2;

	private ITranslationUnit fClassToTest;
	ICProject fProjectToTest;

	// C Project Containing
	private Label fProjectLabel;
	Text fProjectText;
	private Button fProjectButton;

	// Class To Test
	private Label fClassToTestLabel;
	private Text fClassToTestText;
	private Button fClassToTestButton;
	
	// Test Class
	private Label fTestClassLabel;
	private Text fTestClassText;

	// CppUnit Locations
	private CppUnitLocationGroup fLocations;

	// Status on these information
	protected IStatus fProjectStatus;
	protected IStatus fClassToTestStatus;
	protected IStatus fTestClassStatus;
	protected IStatus fCppUnitIncludeStatus;
	protected IStatus fCppUnitLibStatus;

	private boolean fFirstTime;  
	private boolean fPageVisible;

	private ICProject [] currentProjects;

	public NewTestCaseCreationWizardPage() {
		super(PAGE_NAME);
		fFirstTime= true;
		
		setTitle(WizardMessages.getString("NewTestClassWizPage.title")); //$NON-NLS-1$
		setDescription(WizardMessages.getString("NewTestClassWizPage.description")); //$NON-NLS-1$

		fProjectStatus= new CppUnitStatus();
		fClassToTestStatus= new CppUnitStatus();
		fTestClassStatus= new CppUnitStatus();
		fCppUnitIncludeStatus=new CppUnitStatus();
		fCppUnitLibStatus=new CppUnitStatus();
		fLocations=new CppUnitLocationGroup(this);
		try {
			currentProjects=CoreModel.getDefault().getCModel().getCProjects();
		} catch(CModelException e) {
			CppUnitLog.error("",e); //$NON-NLS-1$
			currentProjects=new ICProject[0];
		}
	}

	// -------- Initialization ---------

	/**
	 * Should be called from the wizard with the initial selection and the 2nd page of the wizard..
	 */
	public void init(IStructuredSelection selection, NewTestCaseCreationWizardPage2 page2)
	{
		fPage2= page2;
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
			fProjectToTest=CoreModel.getDefault().create((IProject) o);
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
			ICElement f=CoreModel.getDefault().create((IFile) o);
			if((f!=null)&&(f instanceof ITranslationUnit))
			{
				fClassToTest=(ITranslationUnit)f;
				fProjectToTest=f.getCProject();
			}
		}
		if(o instanceof ITranslationUnit)
		{
			ITranslationUnit f=(ITranslationUnit)o;
			fClassToTest=f;
			fProjectToTest=f.getCProject();
		}
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
		else if (fieldName.equals(CLASS_TO_TEST))
		{
			fClassToTestStatus=classToTestChanged();
			updateDefaultName();
		}
		else if (fieldName.equals(TEST_CLASS))
		{
			fTestClassStatus= testClassChanged();
		}
		else if (fieldName.equals(PROJECT))
		{
			fProjectStatus=projectChanged();
		}
		doStatusUpdate();
	}

	// ------ validation --------
	protected void updateStatus(IStatus status)
	{
		setPageComplete(!status.matches(IStatus.ERROR));
		if (fPageVisible)
		{
			StatusUtil.applyToStatusLine(this, status);
		}
	}
	private void doStatusUpdate()
	{
		// status of all used components
		IStatus[] status= new IStatus[]
		{
			fProjectStatus,
			fClassToTestStatus,
			fTestClassStatus,
			fCppUnitIncludeStatus,
			fCppUnitLibStatus
		};

		// the mode severe status will be displayed and the ok button enabled/disabled.
		IStatus s=StatusUtil.getMostSevere(status);
		updateStatus(s);
		if(s.getSeverity()!=IStatus.OK)
		{
			if(s.equals(fProjectStatus)) setFocus(fProjectText);
			if(s.equals(fClassToTestStatus)) setFocus(fClassToTestText);
			if(s.equals(fTestClassStatus)) setFocus(fTestClassText);
			if(s.equals(fCppUnitIncludeStatus)) setFocus(fLocations.getIncludeTextWidget());
			if(s.equals(fCppUnitLibStatus)) setFocus(fLocations.getLibTextWidget());
		}
	}
	
	protected void updateDefaultName()
	{
		Path path=new Path(fClassToTestText.getText().trim());
		String ext=path.getFileExtension();
		if(ext==null) ext="cpp"; //$NON-NLS-1$
		String fName=path.removeFileExtension().toString()+TEST_FILE_SUFFIX+"."+ext; //$NON-NLS-1$
		fTestClassText.setText(fName);
	}
	
	/*
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
		createClassToTestControls(composite, nColumns);
		createSeparator(composite, nColumns);
		createTestClassControls(composite, nColumns);
		createSeparator(composite, nColumns);
		fLocations.createCppUnitLocation(composite,nColumns);
		setControl(composite);
			
		//set default and focus
		restoreWidgetValues();
	}

	protected void createSeparator(Composite composite, int nColumns) {
		(new Separator(SWT.SEPARATOR | SWT.HORIZONTAL)).doFillIntoGrid(composite, nColumns, convertHeightInCharsToPixels(1));		
	}

	void classToTestButtonPressed() {
		ITranslationUnit t=chooseClassToTest();
		if(t!=null)
		{
			fClassToTest=t;
			fClassToTestText.setText(t.getPath().makeRelative().removeFirstSegments(1).toString());
		}
	}
	private ITranslationUnit chooseClassToTest() {
		ICProject p=getCProject();
		if(p==null) return null;
		try {
			ICElement elements[]=p.getChildren();
			Vector names=new Vector();
			for (int i=0;i<elements.length;i++) {
				if(elements[i] instanceof ITranslationUnit || elements[i] instanceof ICContainer) {
					names.add(elements[i]);
				}
			}
			ILabelProvider labelProvider = new CElementLabelProvider();
			TusAndDirsProvider tusAndDirsProvider=new TusAndDirsProvider(names.toArray());
			ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), labelProvider, tusAndDirsProvider);
			dialog.setTitle("Source File Selection"); //$NON-NLS-1$
			dialog.setMessage("Choose a &file"); //$NON-NLS-1$
			dialog.setInput(names.toArray());
			if (dialog.open() == Window.OK) {
				Object o=dialog.getFirstResult();
				// As it could be a Folder...
				if(o instanceof ITranslationUnit) return (ITranslationUnit)o;
			}
		} catch(CModelException e) {
			CppUnitLog.error("",e); //$NON-NLS-1$
		}
		return null;
	}

	private static class 	TusAndDirsProvider implements ITreeContentProvider
	{
		private Object[] dads;
		private final Object[] fEmpty= new Object[0];

		public TusAndDirsProvider(Object[] types)
		{
			dads= types;
		}

		private Object [] getTusAndDirs(ICContainer f)
		{
			try {
				Vector v=new Vector();
				ICElement [] e=f.getChildren();
				for(int i=0;i<e.length;i++) {
					if(e[i] instanceof ICContainer || e[i] instanceof ITranslationUnit) {
						v.add(e[i]);
					}
				}
				return(v.toArray());
			} catch(CModelException e) {
				CppUnitLog.error("",e); //$NON-NLS-1$
			}
			return new Object[0];
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
		 * @see IContentProvider#dispose()
		 */
		public void dispose() {
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
		 * @see IContentProvider#inputChanged(Viewer, Object, Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		/*
		 * @see ITreeContentProvider#getChildren(Object)
		 */
		public Object[] getChildren(Object parentElement)
		{
			if (parentElement instanceof ICContainer)
			{
				return(getTusAndDirs((ICContainer)parentElement));
			}
			return fEmpty;
		}

	}
	protected IStatus classToTestChanged() {
		String classToTestName=fClassToTestText.getText().trim();
		CppUnitStatus status= new CppUnitStatus();
		
		fClassToTest=null;
		if (classToTestName.length() == 0) {
			status.setError(WizardMessages.getString("NewTestClassWizPage.error.class_to_test.name_empty")); //$NON-NLS-1$
			return status;
		}
		ICProject p=getCProject();
		if(p==null)
		{
			status.setError(WizardMessages.getString("NewTestClassWizPage.error.class_to_test.project_dnot_exist")); //$NON-NLS-1$
			return status;
		}

		Path path=new Path(classToTestName);
		try
		{
				ICElement e=getCProject().findElement(path);
				if(e instanceof ITranslationUnit)
				{
					fClassToTest=(ITranslationUnit)e;
				}
				else
				{
					status.setError(WizardMessages.getString("NewTestClassWizPage.error.class_to_test.class_to_test_dnot_exist")); //$NON-NLS-1$);
					return status;
				}
		}
		catch (CModelException e)
		{
				status.setError(WizardMessages.getString("NewTestClassWizPage.error.class_to_test.class_to_test_dnot_exist")); //$NON-NLS-1$);
				return status;
		}
		return status;
	}
	/**
	 * Called when the type name has changed.
	 * The method validates the type name and returns the status of the validation.
	 * Can be extended to add more validation
	 */
	protected IStatus testClassChanged() {
		CppUnitStatus status= new CppUnitStatus();
        if( fClassToTestText.getText().equals( "")) //$NON-NLS-1$
        {
            status.setError(WizardMessages.getString("NewTestClassWizPage.error.testcase.name_extension_incoherent")); //$NON-NLS-1$
            return status;
        }
            
        IContentType c = CCorePlugin.getContentType(fProjectToTest.getProject(), fClassToTestText.getText().trim());
        if( !CCorePlugin.CONTENT_TYPE_CXXSOURCE.equals(c.getId()))
		{
			status.setError(WizardMessages.getString("NewTestClassWizPage.error.testcase.name_extension_incoherent")); //$NON-NLS-1$
			return status;
		}
		return status;
	}
	protected IStatus projectChanged() {
		CppUnitStatus status= new CppUnitStatus();
		String projectName=fProjectText.getText().trim();
		if (projectName.length() == 0) {
			status.setError(WizardMessages.getString("NewTestClassWizPage.error.project.name_empty")); //$NON-NLS-1$
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
			status.setError(WizardMessages.getString("NewTestClassWizPage.error.project.project_dnot_exist")); //$NON-NLS-1$
			return status;
		}
//		if (!project.isOpen()) {
//			status.setError(WizardMessages.getString("NewTestClassWizPage.error.project.project_closed")); //$NON-NLS-1$
//			return status;
//		}
		return status;		
	}

	/**
	 * @see DialogPage#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		fPageVisible=visible;
		if (visible && fFirstTime) {
			handleFieldChanged(CLASS_TO_TEST);
			handleFieldChanged(TEST_CLASS);
			handleFieldChanged(PROJECT); //creates error message when wizard is opened if TestCase already exists
//			fFirstTime= false;
			fFirstTime= true;
		}
//		if (visible) setFocus();
	}

	public void createTaskMarkers() throws CoreException
	{
		if (fPage2.getCreateTasksButtonSelection())
		{
			// The test driver file path
			IPath t= getTestFile();
			ICElement elem=getCProject().findElement(t);
			if(elem instanceof ITranslationUnit)
			{
				IResource res=elem.getResource();
				ITranslationUnit tu=(ITranslationUnit)elem;
				ICElement [] kids=tu.getChildren();
				for(int i=0;i<kids.length;i++)
				{
					if((kids[i] instanceof IStructure)&&
						kids[i].getElementName().endsWith(TEST_FILE_SUFFIX))
					{
						ICElement [] methods=((IStructure)kids[i]).getChildren();
						for(int j=0;j<methods.length;j++)
						{
							if((methods[j] instanceof IFunctionDeclaration)&&
								methods[j].getElementName().startsWith(TEST_METHOD_SUFFIX))
							{
								IMarker marker= res.createMarker("org.eclipse.cdt.cppunit.cppunit_task"); //$NON-NLS-1$
								HashMap attributes= new HashMap(10);
								attributes.put(IMarker.PRIORITY, new Integer(IMarker.PRIORITY_NORMAL));
								attributes.put(IMarker.MESSAGE, WizardMessages.getFormattedString("NewTestClassWizPage.marker.message",methods[j].getElementName())); //$NON-NLS-1$
								ISourceRange markerRange= ((IFunctionDeclaration)methods[j]).getSourceRange();
								attributes.put(IMarker.CHAR_START, new Integer(markerRange.getIdStartPos()));
								attributes.put(IMarker.CHAR_END, new Integer(markerRange.getIdStartPos()+markerRange.getIdLength()));
								marker.setAttributes(attributes);
							}
						}
					}
				}
			}
		}
	}
	protected void createClassToTestControls(Composite composite, int nColumns)
	{
		fClassToTestLabel= new Label(composite, SWT.LEFT | SWT.WRAP);
		fClassToTestLabel.setFont(composite.getFont());

		fClassToTestLabel.setText(WizardMessages.getString("NewTestClassWizPage.class_to_test.label")); //$NON-NLS-1$
		GridData gd= new GridData();
		gd.horizontalSpan= 1;
		fClassToTestLabel.setLayoutData(gd);

		fClassToTestText= new Text(composite, SWT.SINGLE | SWT.BORDER);
		fClassToTestText.setEnabled(true);
		fClassToTestText.setFont(composite.getFont());
		if(fClassToTest!=null)
		{
			fClassToTestText.setText(fClassToTest.getPath().makeRelative().removeFirstSegments(1).toString());
		}
		fClassToTestText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				handleFieldChanged(CLASS_TO_TEST);
			}
		});
		gd= new GridData();
		gd.horizontalAlignment= GridData.FILL;
		gd.grabExcessHorizontalSpace= true;
		gd.horizontalSpan= nColumns - 2;
		fClassToTestText.setLayoutData(gd);
		
		fClassToTestButton= new Button(composite, SWT.PUSH);
		fClassToTestButton.setText(WizardMessages.getString("NewTestClassWizPage.class_to_test.browse")); //$NON-NLS-1$
		fClassToTestButton.setEnabled(true);
		fClassToTestButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				classToTestButtonPressed();
			}
			public void widgetSelected(SelectionEvent e) {
				classToTestButtonPressed();
			}
		});	
		gd= new GridData();
		gd.horizontalAlignment= GridData.FILL;
		gd.grabExcessHorizontalSpace= false;
		gd.horizontalSpan= 1;
		gd.heightHint = SWTUtil.getButtonHeigthHint(fClassToTestButton);
		gd.widthHint = SWTUtil.getButtonWidthHint(fClassToTestButton);		
		fClassToTestButton.setLayoutData(gd);

	}
	protected void createProjectControls(Composite composite, int nColumns) {
		fProjectLabel= new Label(composite, SWT.LEFT | SWT.WRAP);
		fProjectLabel.setFont(composite.getFont());

		fProjectLabel.setText(WizardMessages.getString("NewTestClassWizPage.project.label")); //$NON-NLS-1$
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
		fProjectButton.setText(WizardMessages.getString("NewTestClassWizPage.project.browse")); //$NON-NLS-1$
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
	protected void createTestClassControls(Composite composite, int nColumns) {
		fTestClassLabel= new Label(composite, SWT.LEFT | SWT.WRAP);
		fTestClassLabel.setFont(composite.getFont());
		fTestClassLabel.setText(WizardMessages.getString("NewTestClassWizPage.testcase.label")); //$NON-NLS-1$
		GridData gd= new GridData();
		gd.horizontalSpan= 1;
		fTestClassLabel.setLayoutData(gd);

		fTestClassText= new Text(composite, SWT.SINGLE | SWT.BORDER);
		fTestClassText.setEnabled(true);
		fTestClassText.setFont(composite.getFont());
		fTestClassText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				handleFieldChanged(TEST_CLASS);
			}
		});
		gd= new GridData();
		gd.horizontalAlignment= GridData.FILL;
		gd.grabExcessHorizontalSpace= true;
		gd.horizontalSpan= nColumns - 2;
		fTestClassText.setLayoutData(gd);
		
		Label space= new Label(composite, SWT.LEFT);
		space.setText(" "); //$NON-NLS-1$
		gd= new GridData();
		gd.horizontalSpan= 1;
		space.setLayoutData(gd);
	}
	/**
	 * @see IWizardPage#canFlipToNextPage
	 */
	public boolean canFlipToNextPage() {
		return isPageComplete() && getNextPage() != null && isNextPageValid();
	}

	protected boolean isNextPageValid() {
		return !fTestClassText.getText().equals("") && //$NON-NLS-1$
				!fTestClassText.getText().equals("") && //$NON-NLS-1$
				!fProjectText.getText().equals(""); //$NON-NLS-1$
	}

	/**
	 * Sets the focus on the type name.
	 */		
//	protected void setFocus() {
//		fProjectText.setFocus();
//		fProjectText.setSelection(fProjectText.getText().length(), fProjectText.getText().length());
//	}
	protected void setFocus(Text t)
	{
		t.setFocus();
		t.setSelection(t.getText().length(),t.getText().length());
	}
	/**
	 *	Use the dialog store to restore widget values to the values that they held
	 *	last time this wizard was used to completion
	 */
	private void restoreWidgetValues() {	
	}	

	/**
	 * 	Since Finish was pressed, write widget values to the dialog store so that they
	 *	will persist into the next invocation of this wizard page
	 */
	void saveWidgetValues() {
	}

	protected ICProject chooseCProject() {
		ICProject [] projects=null;
		try {
			 projects=CoreModel.getDefault().getCModel().getCProjects();
		} catch(CModelException e) {
			CppUnitLog.error("",e); //$NON-NLS-1$
			projects=new ICProject[0];
		}

		ILabelProvider labelProvider = new CElementLabelProvider();
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setTitle("Project Selection"); //$NON-NLS-1$
		dialog.setMessage("Choose a &project"); //$NON-NLS-1$
		dialog.setElements(projects);

		ICProject cProject = getCProject();
		if (cProject != null) {
			dialog.setInitialSelections(new Object[] { cProject });
		}
		if (dialog.open() == Window.OK) {
			return (ICProject) dialog.getFirstResult();
		}
		return null;
	}
	/**
	 * Return the ICProject corresponding to the project name in the project name
	 * text field, or null if the text does not match a project name.
	 */
	protected ICProject getCProject() {
		return(fProjectToTest);
	}
	public ITranslationUnit getFileToTest()
	{
		return(fClassToTest);
	}
	public IPath getTestFile()
	{
		Path p=new Path(fTestClassText.getText().trim());
		return(p);
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
