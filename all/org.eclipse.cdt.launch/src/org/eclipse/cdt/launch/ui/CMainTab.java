package org.eclipse.cdt.launch.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.ICDebugConfiguration;
import org.eclipse.cdt.internal.ui.CElementImageProvider;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.launch.internal.ui.LaunchImages;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.cdt.ui.CElementImageDescriptor;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.dialogs.TwoPaneElementSelector;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * A launch configuration tab that displays and edits project and
 * main type name launch configuration attributes.
 * <p>
 * This class may be instantiated. This class is not intended to be subclassed.
 * </p>
 * @since 2.0
 */

public class CMainTab extends CLaunchConfigurationTab {

	// Project UI widgets
	protected Label fProjLabel;
	protected Text fProjText;
	protected Button fProjButton;

	// Main class UI widgets
	protected Label fProgLabel;
	protected Text fProgText;
	protected Button fSearchButton;

	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private String filterPlatform = EMPTY_STRING;

	/**
	 * @see ILaunchConfigurationTab#createControl(Composite)
	 */
	public void createControl(Composite parent) {

		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);

		WorkbenchHelp.setHelp(getControl(), ICDTLaunchHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_MAIN_TAB);

		GridLayout topLayout = new GridLayout();
		comp.setLayout(topLayout);


		createVerticalSpacer(comp, 1);

		Composite projComp = new Composite(comp, SWT.NONE);
		GridLayout projLayout = new GridLayout();
		projLayout.numColumns = 2;
		projLayout.marginHeight = 0;
		projLayout.marginWidth = 0;
		projComp.setLayout(projLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		projComp.setLayoutData(gd);

		fProjLabel = new Label(projComp, SWT.NONE);
		fProjLabel.setText(LaunchUIPlugin.getResourceString("CMainTab.&ProjectColon")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 2;
		fProjLabel.setLayoutData(gd);

		fProjText = new Text(projComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fProjText.setLayoutData(gd);
		fProjText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});

		fProjButton = createPushButton(projComp, LaunchUIPlugin.getResourceString("Launch.common.B&rowse..."), null); //$NON-NLS-1$
		fProjButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleProjectButtonSelected();
				updateLaunchConfigurationDialog();
			}
		});

		createVerticalSpacer(comp, 1);

		Composite mainComp = new Composite(comp, SWT.NONE);
		GridLayout mainLayout = new GridLayout();
		mainLayout.numColumns = 3;
		mainLayout.marginHeight = 0;
		mainLayout.marginWidth = 0;
		mainComp.setLayout(mainLayout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		mainComp.setLayoutData(gd);
		fProgLabel = new Label(mainComp, SWT.NONE);
		fProgLabel.setText(LaunchUIPlugin.getResourceString("CMainTab.C/C++_Application")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 3;
		fProgLabel.setLayoutData(gd);
		fProgText = new Text(mainComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fProgText.setLayoutData(gd);
		fProgText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});

		fSearchButton = createPushButton(mainComp, LaunchUIPlugin.getResourceString("CMainTab.Search..."), null); //$NON-NLS-1$
		fSearchButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleSearchButtonSelected();
				updateLaunchConfigurationDialog();
			}
		});

		Button fBrowseForBinaryButton;
		fBrowseForBinaryButton = createPushButton(mainComp, LaunchUIPlugin.getResourceString("Launch.common.B&rowse..."), null); //$NON-NLS-1$
		fBrowseForBinaryButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleBinaryBrowseButtonSelected();
				updateLaunchConfigurationDialog();
			}
		});

		LaunchUIPlugin.setDialogShell(parent.getShell());
	}

	/**
	 * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration config) {
		filterPlatform = getPlatform(config);
		updateProjectFromConfig(config);
		updateProgramFromConfig(config);

	}

	protected void updateProjectFromConfig(ILaunchConfiguration config) {
		String projectName = EMPTY_STRING;
		try {
			projectName = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING);
		} catch (CoreException ce) {
			LaunchUIPlugin.log(ce);
		}
		fProjText.setText(projectName);
	}

	protected void updateProgramFromConfig(ILaunchConfiguration config) {
		String programName = EMPTY_STRING;
		try {
			programName = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, EMPTY_STRING);
		} catch (CoreException ce) {
			LaunchUIPlugin.log(ce);
		}
		fProgText.setText(programName);
	}

	/**
	 * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, fProjText.getText());
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, fProgText.getText());
	}

	/**
	 * Show a dialog that lists all main types
	 */
	protected void handleSearchButtonSelected() {

		if (getCProject() == null) {
			MessageDialog.openInformation(
				getShell(),
				LaunchUIPlugin.getResourceString("CMainTab.Project_required"), //$NON-NLS-1$
				LaunchUIPlugin.getResourceString("CMainTab.Enter_project_before_searching_for_program")); //$NON-NLS-1$
			return;
		}

		ILabelProvider programLabelProvider = new CElementLabelProvider() {
			CElementImageProvider imageProvider = new CElementImageProvider();
			
			public String getText(Object element) {
				if (element instanceof IBinary) {
					IBinary bin = (IBinary)element;
					StringBuffer name = new StringBuffer();
					name.append(bin.getPath().lastSegment());
					return name.toString();
				}
				return super.getText(element);
			}
			
			public Image getImage(Object element) {
				if(!(element instanceof ICElement)) {
					return super.getImage(element);
				}
				ICElement celement = (ICElement)element;
				
				if(celement.getElementType() == ICElement.C_BINARY) {
					IBinary belement = (IBinary)celement;
					if(belement.isExecutable()) {
						Image image = super.getImage(element);
						Point size= new Point(image.getBounds().width, image.getBounds().height);
						return CUIPlugin.getImageDescriptorRegistry().get(new CElementImageDescriptor(CPluginImages.DESC_OBJS_CEXEC, 0, size));
					}
				}
								
				return super.getImage(element);
			}
		};

		ILabelProvider qualifierLabelProvider = new CElementLabelProvider() {
			public String getText(Object element) {
				if (element instanceof IBinary) {
					IBinary bin = (IBinary)element;
					StringBuffer name = new StringBuffer();
					name.append(bin.getCPU() + (bin.isLittleEndian() ? "le" : "be")); //$NON-NLS-1$ //$NON-NLS-2$
					name.append(" - "); //$NON-NLS-1$
					name.append(bin.getPath().toString());
					return name.toString();
				}
				return super.getText(element);
			}
		};
		
		TwoPaneElementSelector dialog = new TwoPaneElementSelector(getShell(), programLabelProvider, qualifierLabelProvider);
		dialog.setElements(getBinaryFiles(getCProject()));
		dialog.setMessage(LaunchUIPlugin.getResourceString("CMainTab.Choose_program_to_run")); //$NON-NLS-1$
		dialog.setTitle(LaunchUIPlugin.getResourceString("CMainTab.Program_Selection")); //$NON-NLS-1$
		dialog.setUpperListLabel(LaunchUIPlugin.getResourceString("Launch.common.BinariesColon")); //$NON-NLS-1$
		dialog.setLowerListLabel(LaunchUIPlugin.getResourceString("Launch.common.QualifierColon")); //$NON-NLS-1$
		dialog.setMultipleSelection(false);
		//dialog.set
		if (dialog.open() == ElementListSelectionDialog.OK) {
			IBinary binary = (IBinary) dialog.getFirstResult();
			fProgText.setText(binary.getResource().getProjectRelativePath().toString());
		}

		
	}

	/**
	 * Show a dialog that lets the user select a project.  This in turn provides
	 * context for the main type, allowing the user to key a main type name, or
	 * constraining the search for main types to the specified project.
	 */
	protected void handleBinaryBrowseButtonSelected() {
		final ICProject cproject = getCProject();
		if(cproject == null) {
			MessageDialog.openInformation(
				getShell(),
				LaunchUIPlugin.getResourceString("CMainTab.Project_required"), //$NON-NLS-1$
				LaunchUIPlugin.getResourceString("CMainTab.Enter_project_before_browsing_for_program")); //$NON-NLS-1$
			return;
		}

		ElementTreeSelectionDialog dialog;
		WorkbenchLabelProvider labelProvider = new WorkbenchLabelProvider();
		WorkbenchContentProvider contentProvider = new WorkbenchContentProvider();
		dialog = new ElementTreeSelectionDialog(getShell(), labelProvider, contentProvider);
		dialog.setTitle(LaunchUIPlugin.getResourceString("CMainTab.Program_selection")); //$NON-NLS-1$
		dialog.setMessage(LaunchUIPlugin.getFormattedResourceString("CMainTab.Choose_program_to_run_from_NAME", cproject.getResource().getName())); //$NON-NLS-1$
		dialog.setBlockOnOpen(true);
		dialog.setAllowMultiple(false);
		dialog.setInput(cproject.getResource());
		dialog.setValidator(new ISelectionStatusValidator() {
			public IStatus validate(Object [] selection) {
				if(selection.length == 0 || !(selection[0] instanceof IFile)) {
					return new Status(IStatus.ERROR, LaunchUIPlugin.getUniqueIdentifier(), 1, LaunchUIPlugin.getResourceString("CMainTab.Selection_must_be_file"), null); //$NON-NLS-1$
				} else {
					try {
						ICElement celement = cproject.findElement(((IFile)selection[0]).getProjectRelativePath());
						if(celement == null ||
						   (celement.getElementType() != ICElement.C_BINARY && celement.getElementType() != ICElement.C_ARCHIVE)) {
							return new Status(IStatus.ERROR, LaunchUIPlugin.getUniqueIdentifier(), 1, LaunchUIPlugin.getResourceString("CMainTab.Selection_must_be_binary_file"), null); //$NON-NLS-1$
					   }
					
						return new Status(IStatus.OK, LaunchUIPlugin.getUniqueIdentifier(), IStatus.OK, celement.getResource().getName(), null);
					} catch(Exception ex) {
						return new Status(IStatus.ERROR, LaunchUIPlugin.PLUGIN_ID, 1, LaunchUIPlugin.getResourceString("CMainTab.Selection_must_be_binary_file"), null); //$NON-NLS-1$
					}
				}
			}
		});
		
		if(dialog.open() == ElementTreeSelectionDialog.CANCEL) {
			return;
		}

		Object [] results = dialog.getResult();
		
		try {
			fProgText.setText(((IResource)results[0]).getProjectRelativePath().toString());
		} catch(Exception ex) {
			/* Make sure it is a file */
		}
		
	}


	/**
	 * Iterate through and suck up all of the executable files that
	 * we can find.
	 */
	protected IBinary[] getBinaryFiles(final ICProject cproject) {
		final Display display;
		if ( getShell() == null ) {
			display = LaunchUIPlugin.getShell().getDisplay();
		} else {
			display = getShell().getDisplay();
		}
		final Object[] ret = new Object[1];
		BusyIndicator.showWhile(display, new Runnable() {
			public void run() {
				try {
					ret[0] = cproject.getBinaryContainer().getBinaries();
				} catch (CModelException e) {
					LaunchUIPlugin.errorDialog("Launch UI internal error", e); //$NON-NLS-1$
				}
			}
		});

		return (IBinary[])ret[0];
	}

	/**
	 * Show a dialog that lets the user select a project.  This in turn provides
	 * context for the main type, allowing the user to key a main type name, or
	 * constraining the search for main types to the specified project.
	 */
	protected void handleProjectButtonSelected() {
		ICProject project = chooseCProject();
		if (project == null) {
			return;
		}

		String projectName = project.getElementName();
		fProjText.setText(projectName);
	}

	/**
	 * Realize a C Project selection dialog and return the first selected project,
	 * or null if there was none.
	 */
	protected ICProject chooseCProject() {
		try {
			ICProject[] projects = getCProjects();

			ILabelProvider labelProvider = new CElementLabelProvider();
			ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
			dialog.setTitle(LaunchUIPlugin.getResourceString("CMainTab.Project_Selection")); //$NON-NLS-1$
			dialog.setMessage(LaunchUIPlugin.getResourceString("CMainTab.Choose_project_to_constrain_search_for_program")); //$NON-NLS-1$
			dialog.setElements(projects);

			ICProject cProject = getCProject();
			if (cProject != null) {
				dialog.setInitialSelections(new Object[] { cProject });
			}
			if (dialog.open() == ElementListSelectionDialog.OK) {
				return (ICProject) dialog.getFirstResult();
			}
		} catch (CModelException e) {
			LaunchUIPlugin.errorDialog("Launch UI internal error", e); //$NON-NLS-1$			
		}
		return null;
	}

	/**
	 * Return an array a ICProject whose platform match that of the runtime env.
	 **/

	protected ICProject[] getCProjects() throws CModelException {
		ICProject cproject[] = CoreModel.getDefault().getCModel().getCProjects();
		ArrayList list = new ArrayList(cproject.length);
		boolean isNative = filterPlatform.equals(Platform.getOS());

		for (int i = 0; i < cproject.length; i++) {
			ICDescriptor cdesciptor = null;
			try {
				cdesciptor = CCorePlugin.getDefault().getCProjectDescription((IProject) cproject[i].getResource());
				String projectPlatform = cdesciptor.getPlatform();
				if (filterPlatform.equals("*") //$NON-NLS-1$
					|| projectPlatform.equals("*") //$NON-NLS-1$
					|| (isNative && cdesciptor.getPlatform().equalsIgnoreCase(ICDebugConfiguration.PLATFORM_NATIVE))
					|| filterPlatform.equalsIgnoreCase(cdesciptor.getPlatform()) == true) {
					list.add(cproject[i]);
				}
			} catch (CoreException e) {
				list.add(cproject[i]); 
			}
		}
		return (ICProject[]) list.toArray(new ICProject[list.size()]);
	}
	/**
	 * Return the ICProject corresponding to the project name in the project name
	 * text field, or null if the text does not match a project name.
	 */
	protected ICProject getCProject() {
		String projectName = fProjText.getText().trim();
		if (projectName.length() < 1) {
			return null;
		}
		return CoreModel.getDefault().getCModel().getCProject(projectName);
	}

	/**
	 * @see ILaunchConfigurationTab#isValid(ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration config) {

		setErrorMessage(null);
		setMessage(null);

		String name = fProjText.getText().trim();
		if (name.length() == 0) {
			setErrorMessage(LaunchUIPlugin.getResourceString("CMainTab.Project_not_specified")); //$NON-NLS-1$
			return false;
		}
		if (!ResourcesPlugin.getWorkspace().getRoot().getProject(name).exists()) {
			setErrorMessage(LaunchUIPlugin.getResourceString("Launch.common.Project_does_not_exist")); //$NON-NLS-1$
			return false;
		}
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);

		name = fProgText.getText().trim();
		if (name.length() == 0) {
			setErrorMessage(LaunchUIPlugin.getResourceString("CMainTab.Program_not_specified")); //$NON-NLS-1$
			return false;
		}
		if (name.equals(".") || name.equals("..")) { //$NON-NLS-1$ //$NON-NLS-2$
			setErrorMessage(LaunchUIPlugin.getResourceString("CMainTab.Program_does_not_exist")); //$NON-NLS-1$
			return false;
		}
		if (!project.isOpen()) {
			setErrorMessage(LaunchUIPlugin.getResourceString("CMainTab.Project_must_be_opened")); //$NON-NLS-1$
			return false;
		}
		if (!project.getFile(name).exists()) {
			setErrorMessage(LaunchUIPlugin.getResourceString("CMainTab.Program_does_not_exist")); //$NON-NLS-1$
			return false;
		}
		return true;
	}

	/**
	 * @see ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		// We set empty attributes for project & program so that when one config is
		// compared to another, the existence of empty attributes doesn't cause an
		// incorrect result (the performApply() method can result in empty values
		// for these attributes being set on a config if there is nothing in the
		// corresponding text boxes)
		// plus getContext will use this to base context from if set.
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING);
		ICElement cElement = null;
		cElement = getContext(config, getPlatform(config));
		if (cElement != null) {
			initializeCProject(cElement, config);
			initializeProgramName(cElement, config);
		}
	}

	/**
	 * Set the program name attributes on the working copy based on the ICElement
	 */
	protected void initializeProgramName(ICElement cElement, ILaunchConfigurationWorkingCopy config) {
		IBinary binary = null;
		if (cElement instanceof ICProject) {
			IBinary[] bins = getBinaryFiles((ICProject) cElement);
			if (bins.length == 1) {
				binary = bins[0];
			}
		}

		if (binary != null) {
			String path;
			path = binary.getResource().getProjectRelativePath().toOSString();
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, path);
			String name = binary.getElementName();
			int index = name.lastIndexOf('.');
			if (index > 0) {
				name = name.substring(index + 1);
			}
			name = getLaunchConfigurationDialog().generateName(name);
			config.rename(name);
		} else {
			String name = getLaunchConfigurationDialog().generateName(cElement.getCProject().getElementName());
			config.rename(name);
		}
	}
	/**
	 * @see ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return LaunchUIPlugin.getResourceString("CMainTab.Main"); //$NON-NLS-1$
	}

	/**
	 * @see ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return LaunchImages.get(LaunchImages.IMG_VIEW_MAIN_TAB);
	}

	/**
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#updateLaunchConfigurationDialog()
	 */
	protected void updateLaunchConfigurationDialog() {
		super.updateLaunchConfigurationDialog();
	}

}
