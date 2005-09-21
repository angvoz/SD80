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

package org.eclipse.cdt.internal.cppunit.ui;

import java.net.MalformedURLException;
import java.text.NumberFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.internal.cppunit.runner.ITestRunListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.eclipse.ui.part.ViewPart;

/**
 * A ViewPart that shows the results of a test run.
 */
public class TestRunnerViewPart extends ViewPart implements ITestRunListener, IPropertyChangeListener {

	public static final String NAME= "org.eclipse.cdt.cppunit.ResultView"; //$NON-NLS-1$
 	/**
 	 * Number of executed tests during a test run
 	 */
	protected int fExecutedTests;
	/**
	 * Number of errors during this test run
	 */
	protected int fErrors;
	/**
	 * Number of failures during this test run
	 */
	protected int fFailures;
	/**
	 * Number of tests run
	 */
	private int fTestCount;
	/**
	 * Map storing TestInfos for each executed test keyed by
	 * the test name.
	 */
	private Map fTestInfos = new HashMap();
	/**
	 * The first failure of a test run. Used to reveal the
	 * first failed tests at the end of a run.
	 */
	private TestRunInfo fFirstFailure;

	private ProgressBar fProgressBar;
	private ProgressImages fProgressImages;
	private Image fViewImage;
	private CounterPanel fCounterPanel;
	private boolean fShowOnErrorOnly= false;

	/** 
	 * The view that shows the stack trace of a failure
	 */
	private FailureTraceView fFailureView;
	/** 
	 * The collection of ITestRunViews
	 */
	private Vector fTestRunViews = new Vector();
	/**
	 * The currently active run view
	 */
	private ITestRunView fActiveRunView;
	/**
	 * Is the UI disposed
	 */
	private boolean fIsDisposed= false;
	/**
	 * The launched project
	 */
	private ICProject fTestProject;
	/**
	 * The launcher that has started the test
	 */
	private String fLaunchMode;
	private ILaunch fLastLaunch= null;
	/**
	 * The client side of the remote test runner
	 */
	private RemoteTestRunnerClient fTestRunnerClient;

	final Image fStackViewIcon= TestRunnerViewPart.createImage("cview16/stackframe.gif");//$NON-NLS-1$
	final Image fTestRunOKIcon= TestRunnerViewPart.createImage("cview16/cppunitsucc.gif"); //$NON-NLS-1$
	final Image fTestRunFailIcon= TestRunnerViewPart.createImage("cview16/cppuniterr.gif"); //$NON-NLS-1$
	final Image fTestRunOKDirtyIcon= TestRunnerViewPart.createImage("cview16/cppunitsuccq.gif"); //$NON-NLS-1$
	final Image fTestRunFailDirtyIcon= TestRunnerViewPart.createImage("cview16/cppuniterrq.gif"); //$NON-NLS-1$
	
	Image fOriginalViewImage= null;
	IElementChangedListener fDirtyListener= null;
	
//	private class StopAction extends Action{
//		public StopAction() {
//			setText(CppUnitMessages.getString("TestRunnerViewPart.stopaction.text"));//$NON-NLS-1$
//			setToolTipText(CppUnitMessages.getString("TestRunnerViewPart.stopaction.tooltip"));//$NON-NLS-1$
//			setDisabledImageDescriptor(CppUnitPlugin.getImageDescriptor("dlcl16/stop.gif")); //$NON-NLS-1$
//			setHoverImageDescriptor(CppUnitPlugin.getImageDescriptor("clcl16/stop.gif")); //$NON-NLS-1$
//			setImageDescriptor(CppUnitPlugin.getImageDescriptor("elcl16/stop.gif")); //$NON-NLS-1$
//		}
//
//		public void run() {
//			stopTest();
//		}
//	}

//	private class RerunAction extends Action{
//		public RerunAction() {
//			setText(CppUnitMessages.getString("TestRunnerViewPart.rerunaction.label")); //$NON-NLS-1$
//			setToolTipText(CppUnitMessages.getString("TestRunnerViewPart.rerunaction.tooltip")); //$NON-NLS-1$
//			setDisabledImageDescriptor(CppUnitPlugin.getImageDescriptor("dlcl16/relaunch.gif")); //$NON-NLS-1$
//			setHoverImageDescriptor(CppUnitPlugin.getImageDescriptor("clcl16/relaunch.gif")); //$NON-NLS-1$
//			setImageDescriptor(CppUnitPlugin.getImageDescriptor("elcl16/relaunch.gif")); //$NON-NLS-1$
//		}
//		
//		public void run(){
//			rerunTestRun();
//		}
//	}
	
	/**
	 * Listen for for modifications to C elements
	 */
	private class DirtyListener implements IElementChangedListener {
		public void elementChanged(ElementChangedEvent event) {
			processDelta(event.getDelta());				
		}
		
		private boolean processDelta(ICElementDelta delta)
		{
			// Do not think. CodeHasChanged !!
			codeHasChanged();
			return false;
//			int kind= delta.getKind();
//			int details= delta.getFlags();
//			int type= delta.getElement().getElementType();
//			
//			switch (type) {
//				// Consider containers for class files.
////				case ICElement.JAVA_MODEL:
//				case ICElement.C_PROJECT:
////				case ICElement.PACKAGE_FRAGMENT_ROOT:
////				case ICElement.PACKAGE_FRAGMENT:
//					// If we did some different than changing a child we flush the the undo / redo stack.
//					if (kind != ICElementDelta.CHANGED || details != ICElementDelta.F_CHILDREN) {
//						codeHasChanged();
//						return false;
//					}
//					break;
////				case ICElement.COMPILATION_UNIT:
////					ICompilationUnit unit= (ICompilationUnit)delta.getElement();
////					// If we change a working copy we do nothing
////					if (unit.isWorkingCopy()) {
////						// Don't examine children of a working copy but keep processing siblings.
////						return true;
////					} else {
////						codeHasChanged();
////						return false;
////					}
////				case IJavaElement.CLASS_FILE:
////					// Don't examine children of a class file but keep on examining siblings.
////					return true;
//				default:
//					codeHasChanged();
//					return false;	
//			}
//				
////			ICElementDelta[] affectedChildren= delta.getAffectedChildren();
////			if (affectedChildren == null)
////				return true;
////	
////			for (int i= 0; i < affectedChildren.length; i++) {
////				if (!processDelta(affectedChildren[i]))
////					return false;
////			}
//			return true;			
		}
	}
	
	/**
	 * Stops the currently running test and shuts down the RemoteTestRunner
	 */
	public void stopTest() {
		if (fTestRunnerClient != null)
			fTestRunnerClient.stopTest();
	}

	/**
	 * Stops the currently running test and shuts down the RemoteTestRunner
	 */
//	public void rerunTestRun() {
//		if (fLastLaunch != null && fLastLaunch.getLaunchConfiguration() != null) {
//			try {
//				DebugUITools.saveAndBuildBeforeLaunch();
//				fLastLaunch.getLaunchConfiguration().launch(fLastLaunch.getLaunchMode(), null);		
//			} catch (CoreException e) {
//				ErrorDialog.openError(getSite().getShell(), 
//					CppUnitMessages.getString("TestRunnerViewPart.error.cannotrerun"), e.getMessage(), e.getStatus() //$NON-NLS-1$
//				);
//			}
//		}
//	}

	/*
	 * @see ITestRunListener#testRunStarted(testCount)
	 */
	public void testRunStarted(final int testCount){
		reset(testCount);
		fShowOnErrorOnly= CppUnitPreferencePage.getShowOnErrorOnly();
		fExecutedTests++;
	}

	/*
	 * @see ITestRunListener#testRunEnded
	 */
	public void testRunEnded(long elapsedTime){
		fExecutedTests--;
		String msg= CppUnitMessages.getFormattedString("TestRunnerViewPart.message.finish", elapsedTimeAsString(elapsedTime)); //$NON-NLS-1$
		postInfo(msg);
		postAsyncRunnable(new Runnable() {				
			public void run() {
				if(isDisposed()) 
					return;	
				if (fFirstFailure != null) {
					fActiveRunView.setSelectedTest(fFirstFailure.fTestName);
					handleTestSelected(fFirstFailure.fTestName);
				}
				updateViewIcon();
				if (fDirtyListener == null) {
					fDirtyListener= new DirtyListener();
					CoreModel.getDefault().addElementChangedListener(fDirtyListener);
				}
			}
		});	
	}

	private void updateViewIcon() {
		if (fErrors+fFailures > 0) 
			fViewImage= fTestRunFailIcon;
		else 
			fViewImage= fTestRunOKIcon;
		firePropertyChange(IWorkbenchPart.PROP_TITLE);	
	}

	private String elapsedTimeAsString(long runTime) {
		return NumberFormat.getInstance().format((double)runTime/1000);
	}

	/*
	 * @see ITestRunListener#testRunStopped
	 */
	public void testRunStopped(final long elapsedTime) {
		String msg= CppUnitMessages.getFormattedString("TestRunnerViewPart.message.stopped", elapsedTimeAsString(elapsedTime)); //$NON-NLS-1$
		postInfo(msg);
		postAsyncRunnable(new Runnable() {				
			public void run() {
				if(isDisposed()) 
					return;	
				resetViewIcon();
			}
		});	

	}

	private void resetViewIcon() {
		fViewImage= fOriginalViewImage;
		firePropertyChange(IWorkbenchPart.PROP_TITLE);
	}

	/*
	 * @see ITestRunListener#testRunTerminated
	 */
	public void testRunTerminated() {
		String msg= CppUnitMessages.getString("TestRunnerViewPart.message.terminated"); //$NON-NLS-1$
		showMessage(msg);
	}

	private void showMessage(String msg) {
		showInformation(msg);
		postError(msg);
	}

	/*
	 * @see ITestRunListener#testStarted
	 */
	public void testStarted(String testName) {
		// reveal the part when the first test starts
		if (!fShowOnErrorOnly && fExecutedTests == 1) 
			postShowTestResultsView();
			
		postInfo(CppUnitMessages.getFormattedString("TestRunnerViewPart.message.started", testName)); //$NON-NLS-1$
		TestRunInfo testInfo= getTestInfo(testName);
		if (testInfo == null) 
			fTestInfos.put(HierarchyRunView.filterFirstNumbers(testName), new TestRunInfo(testName));
	}

	/*
	 * @see ITestRunListener#testEnded
	 */
	public void testEnded(String testName){
		postEndTest(testName);
		fExecutedTests++;
	}

	/*
	 * @see ITestRunListener#testFailed
	 */
	public void testFailed(int status, String testName, String trace){
		TestRunInfo testInfo= getTestInfo(testName);
		if (testInfo == null) {
			testInfo= new TestRunInfo(testName);
			fTestInfos.put(HierarchyRunView.filterFirstNumbers(testName), testInfo);
		}
		testInfo.fTrace= trace;
		testInfo.fStatus= status;
		if (status == ITestRunListener.STATUS_ERROR)
			fErrors++;
		else
			fFailures++;
		if (fFirstFailure == null)
			fFirstFailure= testInfo;
		// show the view on the first error only
		if (fShowOnErrorOnly && (fErrors + fFailures == 1)) 
			postShowTestResultsView();
	}

	/*
	 * @see ITestRunListener#testReran
	 */
	public void testReran(String className, String testName, int status, String trace) {
		if (status == ITestRunListener.STATUS_ERROR) {
			String msg= CppUnitMessages.getFormattedString("TestRunnerViewPart.message.error", new String[]{testName, className}); //$NON-NLS-1$
			postError(msg); 
		} else if (status == ITestRunListener.STATUS_FAILURE) {
			String msg= CppUnitMessages.getFormattedString("TestRunnerViewPart.message.failure", new String[]{testName, className}); //$NON-NLS-1$
			postError(msg);
		} else {
			String msg= CppUnitMessages.getFormattedString("TestRunnerViewPart.message.success", new String[]{testName, className}); //$NON-NLS-1$
			postInfo(msg);
		}
		String test= testName+"("+className+")"; //$NON-NLS-1$ //$NON-NLS-2$
		TestRunInfo info= getTestInfo(test);
		updateTest(info, status);
		if (info.fTrace == null || !info.fTrace.equals(trace)) {
			info.fTrace= trace;
			showFailure(info.fTrace);
		}
	}

	private void updateTest(TestRunInfo info, final int status) {
		if (status == info.fStatus)
			return;
		if (info.fStatus == ITestRunListener.STATUS_OK) {
			if (status == ITestRunListener.STATUS_FAILURE) 
				fFailures++;
			else if (status == ITestRunListener.STATUS_ERROR)
				fErrors++;
		} else if (info.fStatus == ITestRunListener.STATUS_ERROR) {
			if (status == ITestRunListener.STATUS_OK) 
				fErrors--;
			else if (status == ITestRunListener.STATUS_FAILURE) {
				fErrors--;
				fFailures++;
			}
		} else if (info.fStatus == ITestRunListener.STATUS_FAILURE) {
			if (status == ITestRunListener.STATUS_OK) 
				fFailures--;
			else if (status == ITestRunListener.STATUS_ERROR) {
				fFailures--;
				fErrors++;
			}
		}			
		info.fStatus= status;	
		final TestRunInfo finalInfo= info;
		postAsyncRunnable(new Runnable() {
			public void run() {
				refreshCounters();
				for (Enumeration e= fTestRunViews.elements(); e.hasMoreElements();) {
					ITestRunView v= (ITestRunView) e.nextElement();
					v.testStatusChanged(finalInfo);
				}
			}
		});
		
	}

	/*
	 * @see ITestRunListener#testTreeEntry
	 */
	public void testTreeEntry(final String treeEntry){
		postSyncRunnable(new Runnable() {
			public void run() {
				if(isDisposed()) 
					return;
				for (Enumeration e= fTestRunViews.elements(); e.hasMoreElements();) {
					ITestRunView v= (ITestRunView) e.nextElement();
					v.newTreeEntry(treeEntry);
				}
			}
		});	
	}

//	public void startTestRunListening(ICElement type, int port, ILaunch launch) {
	public void startTestRunListening(ICElement program, int port, ILaunch launch) {
		fTestProject= program.getCProject();
		fLaunchMode= launch.getLaunchMode();
		aboutToLaunch();
		
		if (fTestRunnerClient != null) {
			stopTest();
		}
		fTestRunnerClient= new RemoteTestRunnerClient();
		fTestRunnerClient.startListening(this, port);
		fLastLaunch= launch;
		String title= CppUnitMessages.getFormattedString("TestRunnerViewPart.title",program); //$NON-NLS-1$
		setPartName(title);
		setTitleToolTip(program.getElementName());
			
	}

	private void aboutToLaunch() {
		String msg= CppUnitMessages.getString("TestRunnerViewPart.message.launching"); //$NON-NLS-1$
		showInformation(msg);
		postInfo(msg);
		fViewImage= fOriginalViewImage;
		firePropertyChange(IWorkbenchPart.PROP_TITLE);
	}

//	public void rerunTest(String className, String testName) {
//		if (fTestRunnerClient != null && fTestRunnerClient.isRunning() && ILaunchManager.DEBUG_MODE.equals(fLaunchMode))
//			fTestRunnerClient.rerunTest(className, testName);
//		else {
//			MessageDialog.openInformation(getSite().getShell(), 
//				CppUnitMessages.getString("TestRunnerViewPart.cannotrerun.title"),  //$NON-NLS-1$
//				CppUnitMessages.getString("TestRunnerViewPart.cannotrerurn.message") //$NON-NLS-1$
// 			); 
//		}
//	}

	public synchronized void dispose(){
		fIsDisposed= true;
		stopTest();
		if (fProgressImages != null)
			fProgressImages.dispose();
		CppUnitPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
		fTestRunOKIcon.dispose();
		fTestRunFailIcon.dispose();
		fStackViewIcon.dispose();
		fTestRunOKDirtyIcon.dispose();
		fTestRunFailDirtyIcon.dispose();
	}

	private void start(final int total) {
		resetProgressBar(total);
		fCounterPanel.setTotal(total);
		fCounterPanel.setRunValue(0);	
	}

	private void resetProgressBar(final int total) {
		fProgressBar.setMinimum(0);
		fProgressBar.setSelection(0);
		fProgressBar.setForeground(getDisplay().getSystemColor(SWT.COLOR_GREEN));
		fProgressBar.setMaximum(total);
	}

	private void postSyncRunnable(Runnable r) {
		if (!isDisposed())
			getDisplay().syncExec(r);
	}

	private void postAsyncRunnable(Runnable r) {
		// Martin fixed Calls asyncExec instead of the originial syncExec !! 
		if (!isDisposed())
			getDisplay().asyncExec(r);
	}

	private void aboutToStart() {
		postSyncRunnable(new Runnable() {
			public void run() {
				if (!isDisposed()) {
					for (Enumeration e= fTestRunViews.elements(); e.hasMoreElements();) {
						ITestRunView v= (ITestRunView) e.nextElement();
						v.aboutToStart();
					}
				}
			}
		});
	}

	private void postEndTest(final String testName) {
		postSyncRunnable(new Runnable() {
			public void run() {
				if(isDisposed()) 
					return;
				handleEndTest();
				for (Enumeration e= fTestRunViews.elements(); e.hasMoreElements();) {
					ITestRunView v= (ITestRunView) e.nextElement();
					v.endTest(testName);
				}
			}
		});	
	}

	private void handleEndTest() {
		refreshCounters();
		updateProgressColor(fFailures+fErrors);
		fProgressBar.setSelection(fProgressBar.getSelection() + 1);
		if (fShowOnErrorOnly) {
			Image progress= fProgressImages.getImage(fExecutedTests, fTestCount, fErrors, fFailures);
			if (progress != fViewImage) {
				fViewImage= progress;
				firePropertyChange(IWorkbenchPart.PROP_TITLE);
			}
		}
	}

	private void updateProgressColor(int failures) {
		if (failures > 0)
			fProgressBar.setForeground(getDisplay().getSystemColor(SWT.COLOR_RED));
		else 
			fProgressBar.setForeground(getDisplay().getSystemColor(SWT.COLOR_GREEN));
	}

	private void refreshCounters() {
		fCounterPanel.setErrorValue(fErrors);
		fCounterPanel.setFailureValue(fFailures);
		fCounterPanel.setRunValue(fExecutedTests);
		updateProgressColor(fErrors + fFailures);
	}

	protected void postShowTestResultsView() {
		postAsyncRunnable(new Runnable() {
			public void run() {
				if (isDisposed()) 
					return;
				showTestResultsView();
			}
		});
	}

	public void showTestResultsView() {
		IWorkbenchWindow window= getSite().getWorkbenchWindow();
		IWorkbenchPage page= window.getActivePage();
		TestRunnerViewPart testRunner= null;
		
		if (page != null) {
			try { // show the result view
				testRunner= (TestRunnerViewPart)page.findView(TestRunnerViewPart.NAME);
				if(testRunner == null) {
					IWorkbenchPart activePart= page.getActivePart();
					testRunner= (TestRunnerViewPart)page.showView(TestRunnerViewPart.NAME);
					//restore focus stolen by the creation of the console
					page.activate(activePart);
				} else {
					page.bringToTop(testRunner);
				}
			} catch (PartInitException pie) {
				CppUnitPlugin.log(pie);
			}
		}
	}

	protected void postInfo(final String message) {
		postAsyncRunnable(new Runnable() {
			public void run() {
				if (isDisposed()) 
					return;
				getStatusLine().setErrorMessage(null);
				getStatusLine().setMessage(message);
			}
		});
	}

	protected void postError(final String message) {
		postAsyncRunnable(new Runnable() {
			public void run() {
				if (isDisposed()) 
					return;
				getStatusLine().setMessage(null);
				getStatusLine().setErrorMessage(message);
			}
		});
	}

	protected void showInformation(final String info){
		postSyncRunnable(new Runnable() {
			public void run() {
				if (!isDisposed())
					fFailureView.setInformation(info);
			}
		});
	}

	private CTabFolder createTestRunViews(Composite parent) {
		CTabFolder tabFolder= new CTabFolder(parent, SWT.TOP);
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL));

		ITestRunView failureRunView= new FailureRunView(tabFolder, this); 
		ITestRunView testHierarchyRunView= new HierarchyRunView(tabFolder, this);
		
		fTestRunViews.addElement(failureRunView);
		fTestRunViews.addElement(testHierarchyRunView);
		
		tabFolder.setSelection(0);				
		fActiveRunView= (ITestRunView)fTestRunViews.firstElement();		
				
		tabFolder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				testViewChanged(event);
			}
		});
		return tabFolder;
	}

	private void testViewChanged(SelectionEvent event) {
		for (Enumeration e= fTestRunViews.elements(); e.hasMoreElements();) {
			ITestRunView v= (ITestRunView) e.nextElement();
			if (((CTabFolder) event.widget).getSelection().getText() == v.getName()){
				v.setSelectedTest(fActiveRunView.getTestName());
				fActiveRunView= v;
				fActiveRunView.activate();
			}
		}
	}

	private SashForm createSashForm(Composite parent) {
		SashForm sashForm= new SashForm(parent, SWT.VERTICAL);		
		ViewForm top= new ViewForm(sashForm, SWT.NONE);
		CTabFolder tabFolder= createTestRunViews(top);
		tabFolder.setLayoutData(new TabFolderLayout());
		top.setContent(tabFolder);
		
		ViewForm bottom= new ViewForm(sashForm, SWT.NONE);
		ToolBar failureToolBar= new ToolBar(bottom, SWT.FLAT | SWT.WRAP);
		bottom.setTopCenter(failureToolBar);
		
		fFailureView= new FailureTraceView(bottom, this);
		bottom.setContent(fFailureView.getComposite()); 
		CLabel label= new CLabel(bottom, SWT.NONE);
		label.setText(CppUnitMessages.getString("TestRunnerViewPart.label.failure")); //$NON-NLS-1$
		label.setImage(fStackViewIcon);
		bottom.setTopLeft(label);

		// fill the failure trace viewer toolbar
//		ToolBarManager failureToolBarmanager= new ToolBarManager(failureToolBar);
//		failureToolBarmanager.add(new EnableStackFilterAction(fFailureView));			
//		failureToolBarmanager.update(true);
		
		sashForm.setWeights(new int[]{50, 50});
		return sashForm;
	}

	private void reset(final int testCount) {
		postAsyncRunnable(new Runnable() {
			public void run() {
				if (isDisposed()) 
					return;
				fCounterPanel.reset();
				fFailureView.clear();
				clearStatus();
				start(testCount);
			}
		});
		fExecutedTests= 0;
		fFailures= 0;
		fErrors= 0;
		fTestCount= testCount;
		aboutToStart();
		fTestInfos.clear();
		fFirstFailure= null;
	}

	private void clearStatus() {
		getStatusLine().setMessage(null);
		getStatusLine().setErrorMessage(null);
	}

    public void setFocus() {
    	if (fActiveRunView != null)
    		fActiveRunView.setFocus();
    }

    public void createPartControl(Composite parent) {		
		GridLayout gridLayout= new GridLayout();
		gridLayout.marginWidth= 0;
		parent.setLayout(gridLayout);

		IActionBars actionBars= getViewSite().getActionBars();
		IToolBarManager toolBar= actionBars.getToolBarManager();
//		toolBar.add(new StopAction());
//		toolBar.add(new RerunAction());

		actionBars.updateActionBars();
		
		Composite counterPanel= createProgressCountPanel(parent);
		counterPanel.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		SashForm sashForm= createSashForm(parent);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		actionBars.setGlobalActionHandler(
				ActionFactory.COPY.getId(),
//				IWorkbenchActionConstants.COPY,
			new CopyTraceAction(fFailureView));

		CppUnitPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
		fOriginalViewImage= getTitleImage();
		fProgressImages= new ProgressImages();
	}

	private IStatusLineManager getStatusLine() {
		// we want to show messages globally hence we
		// have to go throgh the active part
		IViewSite site= getViewSite();
		IWorkbenchPage page= site.getPage();
		IWorkbenchPart activePart= page.getActivePart();
	
		if (activePart instanceof IViewPart) {
			IViewPart activeViewPart= (IViewPart)activePart;
			IViewSite activeViewSite= activeViewPart.getViewSite();
			return activeViewSite.getActionBars().getStatusLineManager();
		}
		
		if (activePart instanceof IEditorPart) {
			IEditorPart activeEditorPart= (IEditorPart)activePart;
			IEditorActionBarContributor contributor= activeEditorPart.getEditorSite().getActionBarContributor();
			if (contributor instanceof EditorActionBarContributor) 
				return ((EditorActionBarContributor) contributor).getActionBars().getStatusLineManager();
		}
		// no active part
		return getViewSite().getActionBars().getStatusLineManager();
	}

	private Composite createProgressCountPanel(Composite parent) {
		Composite composite= new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		fProgressBar = new ProgressBar(composite, SWT.HORIZONTAL);
		fProgressBar.setLayoutData(
			new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		fCounterPanel = new CounterPanel(composite);
		fCounterPanel.setLayoutData(
			new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		return composite;
	}

	public TestRunInfo getTestInfo(String testName) {
		return (TestRunInfo) fTestInfos.get(HierarchyRunView.filterFirstNumbers(testName));
	}

	public void handleTestSelected(String testName) {
		TestRunInfo testInfo= getTestInfo(testName);

		if (testInfo == null) {
			showFailure(""); //$NON-NLS-1$
		} else {
			showFailure(testInfo.fTrace);
		}
	}

	private void showFailure(final String failure) {
		postSyncRunnable(new Runnable() {
			public void run() {
				if (!isDisposed())
					fFailureView.showFailure(failure);
			}
		});		
	}

	public ICProject getLaunchedProject() {
		return fTestProject;
	}

	protected static Image createImage(String path) {
		try {
			ImageDescriptor id= ImageDescriptor.createFromURL(CppUnitPlugin.makeIconFileURL(path));
			return id.createImage();
		} catch (MalformedURLException e) {
			// fall through
		}  
		return null;
	}

	private boolean isDisposed() {
		return fIsDisposed || fCounterPanel.isDisposed();
	}

	private Display getDisplay() {
		return getViewSite().getShell().getDisplay();
	}
	/**
	 * @see IWorkbenchPart#getTitleImage()
	 */
	public Image getTitleImage() {
		if (fOriginalViewImage == null)
			fOriginalViewImage= super.getTitleImage();
			
		if (fViewImage == null)
			return super.getTitleImage();
		return fViewImage;
	}

	public void propertyChange(PropertyChangeEvent event) {
		if (isDisposed())
			return;

		if (ICppUnitPreferencesConstants.SHOW_ON_ERROR_ONLY.equals(event.getProperty())) {
//			if (!CppUnitPreferencePage.getShowOnErrorOnly()) {
				fViewImage= fOriginalViewImage;
				firePropertyChange(IWorkbenchPart.PROP_TITLE);
//			}
		}
	}

	void codeHasChanged() {
		postAsyncRunnable(new Runnable() {
			public void run() {
				if (isDisposed())
					return;
				if (fDirtyListener != null) {
					CoreModel.getDefault().removeElementChangedListener(fDirtyListener);
					fDirtyListener= null;
				}
				if (fViewImage == fTestRunOKIcon) 
					fViewImage= fTestRunOKDirtyIcon;
				else if (fViewImage == fTestRunFailIcon)
					fViewImage= fTestRunFailDirtyIcon;
				firePropertyChange(IWorkbenchPart.PROP_TITLE);
			}
		});
	}
	
	boolean isCreated() {
		return fCounterPanel != null;
	}
}
