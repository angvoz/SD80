/*******************************************************************************
 * Copyright (c) 2004, 2006 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedCProjectNature;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.internal.envvar.EnvironmentVariableProvider;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuildOptionBlock;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderHelpContextIds;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIMessages;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIPlugin;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.eclipse.ui.help.WorkbenchHelp;


public class ResourceBuildPropertyPage extends AbstractBuildPropertyPage implements
		IWorkbenchPropertyPage, IPreferencePageContainer, ICOptionContainer {
	/*
	 * String constants
	 */
	private static final String PREFIX = "ResourceBuildPropertyPage"; //$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label"; //$NON-NLS-1$
	private static final String NAME_LABEL = LABEL + ".NameText"; //$NON-NLS-1$
	private static final String CONFIG_LABEL = LABEL + ".Configuration"; //$NON-NLS-1$
	private static final String ALL_CONFS = PREFIX	+ ".selection.configuration.all"; //$NON-NLS-1$
	private static final String ACTIVE_RESOURCE_LABEL = LABEL + ".ActiveResource"; //$NON-NLS-1$
	private static final String RESOURCE_SETTINGS_LABEL = LABEL	+ ".ResourceSettings"; //$NON-NLS-1$
	private static final String TREE_LABEL = LABEL + ".ToolTree"; //$NON-NLS-1$
	private static final String OPTIONS_LABEL = LABEL + ".ToolOptions"; //$NON-NLS-1$
	private static final String NOTMBSFILE_LABEL = LABEL + ".NotMBSFile"; //$NON-NLS-1$
	private static final String EXCLUDE_CHECKBOX = LABEL + ".ExcludeCheckBox"; //$NON-NLS-1$
	private static final String TIP = PREFIX + ".tip"; //$NON-NLS-1$
	private static final String RESOURCE_PLAT_TIP = TIP + ".ResourcePlatform"; //$NON-NLS-1$
	private static final String CONF_TIP = TIP + ".config"; //$NON-NLS-1$
	private static final String EXCLUDE_TIP = TIP + ".excludecheck"; //$NON-NLS-1$
	private static final String MANAGE_TITLE = PREFIX + ".manage.title"; //$NON-NLS-1$
	private static final int[] DEFAULT_SASH_WEIGHTS = new int[] { 20, 30 };
	private static final String ID_SEPARATOR = "."; //$NON-NLS-1$
	private static final String MSG_UNSUPPORTED_PROJ = PREFIX + ".unsupported.proj"; //$NON-NLS-1$
	private static final String MSG_UNSUPPORTED_CONFIG = PREFIX + ".unsupported.config"; //$NON-NLS-1$
	private static final String MSG_CONFIG_NOTSELECTED = PREFIX + ".config.notselected"; //$NON-NLS-1$
	private static final String MSG_RC_NON_BUILD = PREFIX + ".rc.non.build"; //$NON-NLS-1$
	private static final String MSG_RC_GENERATED = PREFIX + ".rc.generated"; //$NON-NLS-1$
	
	private static final boolean DEFAULT_EXCLUDE_VALUE = false;
	/*
	 * Dialog widgets
	 */

	private Combo configSelector;

//	private Point lastShellSize;
	private Button excludedCheckBox;
	
	/*
	 * Bookeeping variables
	 */
	private boolean noContentOnPage = false;
	
	private IConfiguration[] configurations;
	private IConfiguration selectedConfiguration;
	private IConfiguration clonedConfiguration;
	private IResourceConfiguration clonedResourceConfig;
	private Point lastShellSize;
	protected ManagedBuildOptionBlock fOptionBlock;
	protected boolean displayedConfig = false;
	
	/**
	 * Default constructor
	 */
	public ResourceBuildPropertyPage() {
	//	super();
	}

	public void setContainer(IPreferencePageContainer preferencePageContainer) {
	    super.setContainer(preferencePageContainer);
	    if (fOptionBlock == null) {
	    	fOptionBlock = new ManagedBuildOptionBlock(this);
	    }
	}	
	
	protected Control createContents(Composite parent) {
		//	Create the container we return to the property page editor
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		GridLayout compositeLayout = new GridLayout();
		compositeLayout.numColumns = 1;
		compositeLayout.marginHeight = 0;
		compositeLayout.marginWidth = 0;
		composite.setLayout( compositeLayout );

		//  Check to see if we are dealing with a managed build project
		boolean openMBSProject;
		try {
		    openMBSProject = (getProject().hasNature(ManagedCProjectNature.MNG_NATURE_ID));
		} catch (CoreException e) {
		    openMBSProject = false;
		}
		
		if (openMBSProject) {
		    contentForMBSFile(composite);
		} else {
		    noContent(composite,ManagedBuilderUIMessages.getResourceString(NOTMBSFILE_LABEL));
		}
		
		return composite;
	}
	
	protected void contentForMBSFile(Composite composite) {
		GridData gd;

		//	Initialize the key data
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(getProject());
		String error = null;
		if (info.getVersion() == null) {
			error = ManagedBuilderUIMessages.getResourceString("ResourceBuildPropertyPage.error.version_low"); //$NON-NLS-1$
		} else {
			IFile file = (IFile)getElement();
			if(isGeneratedResource(file))
				error = ManagedBuilderUIMessages.getResourceString(MSG_RC_GENERATED);
			else if(file.isDerived())
				error = ManagedBuilderUIMessages.getResourceString(MSG_RC_NON_BUILD);
		}
		
		if(error != null){
			noContent(composite,error);
			return;
		}

		// Add a config selection area
		Group configGroup = ControlFactory.createGroup(composite, ManagedBuilderUIMessages.getResourceString(ACTIVE_RESOURCE_LABEL), 1);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.grabExcessHorizontalSpace = true;
		configGroup.setLayoutData(gd);
		// Use the form layout inside the group composite
		FormLayout form = new FormLayout();
		form.marginHeight = 5;
		form.marginWidth = 5;
		configGroup.setLayout(form);

		excludedCheckBox = ControlFactory.createCheckBox(configGroup, ManagedBuilderUIMessages.getResourceString(EXCLUDE_CHECKBOX));
		excludedCheckBox.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				handleIsExcluded();
			}
		});
		excludedCheckBox.setToolTipText(ManagedBuilderUIMessages.getResourceString(EXCLUDE_TIP));

		FormData fd = new FormData();
		fd.left = new FormAttachment(excludedCheckBox, 0, SWT.CENTER);
		excludedCheckBox.setLayoutData(fd);

		Label configLabel = ControlFactory.createLabel(configGroup, ManagedBuilderUIMessages.getResourceString(CONFIG_LABEL));
		configSelector = new Combo(configGroup, SWT.READ_ONLY | SWT.DROP_DOWN);
		configSelector.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				handleConfigSelection();
			}
		});
		configSelector.setToolTipText(ManagedBuilderUIMessages.getResourceString(CONF_TIP));

		// Now do the form layout for the widgets

		fd = new FormData();
		fd.top = new FormAttachment(excludedCheckBox, 15, SWT.DEFAULT);

		configLabel.setLayoutData(fd);

		fd = new FormData();
		fd.top = new FormAttachment(excludedCheckBox, 15, SWT.DEFAULT);
		fd.left = new FormAttachment(configLabel, 5, SWT.DEFAULT);
		fd.right = new FormAttachment(80, -20);
		configSelector.setLayoutData(fd);

		//	Create the Tools Settings, Build Settings, ... Tabbed pane
		Group tabGroup = ControlFactory.createGroup(composite, ManagedBuilderUIMessages.getResourceString(RESOURCE_SETTINGS_LABEL), 1);
		gd = new GridData(GridData.FILL_BOTH);
		tabGroup.setLayoutData(gd);
		//	Update the contents of the configuration widget
		populateConfigurations();
		fOptionBlock.createContents(tabGroup, getElement());

		WorkbenchHelp.setHelp(composite,ManagedBuilderHelpContextIds.MAN_PROJ_BUILD_PROP);
	}
	
	protected void noContent(Composite composite, String message) {
		Label label = new Label(composite, SWT.LEFT);
		label.setText(message);
		label.setFont(composite.getFont());

		noContentOnPage = true;
		noDefaultAndApplyButton();
	}

	private void handleIsExcluded() {

		// Check whether the check box is selected or not.
		boolean isSelected = excludedCheckBox.getSelection();
		setExcluded(isSelected);
	}

	/*
	 * (non-Javadoc) @return an array of names for the configurations defined
	 * for the chosen
	 */
	private String[] getConfigurationNames() {
		String[] names = new String[configurations.length];
		for (int index = 0; index < configurations.length; ++index) {
			names[index] = configurations[index].getName();
		}
		return names;
	}

	protected Point getLastShellSize() {
		if (lastShellSize == null) {
			Shell shell = getShell();
			if (shell != null)
				lastShellSize = shell.getSize();
		}
		return lastShellSize;
	}

	public IProject getProject() {
		Object element = getElement();
		if (element != null && element instanceof IFile) {
			IFile file = (IFile) element;
			return (IProject) file.getProject();
		}
		return null;
	}

	/*
	 * (non-Javadoc) @return
	 */
	public IConfiguration getSelectedConfiguration() {
		return selectedConfiguration;
	}

		/*
	 * Event Handlers
	 */
	private void handleConfigSelection() {
		// If there is nothing in config selection widget just bail
		if (configSelector.getItemCount() == 0)
			return;

		// Check if the user has selected the "all" configuration
		int selectionIndex = configSelector.getSelectionIndex();
		if (selectionIndex == -1)
			return;
		String configName = configSelector.getItem(selectionIndex);
		if (configName.equals(ManagedBuilderUIMessages
				.getResourceString(ALL_CONFS))) {
			// This is the all config
			return;
		} else {
			IConfiguration newConfig = configurations[selectionIndex];
			if (newConfig != selectedConfiguration) {
				// If the user has changed values, and is now switching configurations, prompt for saving
			    if (selectedConfiguration != null) {
			        if (fOptionBlock.isDirty()) {
						Shell shell = ManagedBuilderUIPlugin.getDefault().getShell();
						boolean shouldApply = MessageDialog.openQuestion(shell,
						        ManagedBuilderUIMessages.getResourceString("BuildPropertyPage.changes.save.title"), //$NON-NLS-1$
						        ManagedBuilderUIMessages.getFormattedString("BuildPropertyPage.changes.save.question",  //$NON-NLS-1$
						                new String[] {selectedConfiguration.getName(), newConfig.getName()}));
						if (shouldApply) {
						    if (performOk()) {
			        			fOptionBlock.setDirty(false);    
			        		} else {
						        MessageDialog.openWarning(shell,
								        ManagedBuilderUIMessages.getResourceString("BuildPropertyPage.changes.save.title"), //$NON-NLS-1$
								        ManagedBuilderUIMessages.getResourceString("BuildPropertyPage.changes.save.error")); //$NON-NLS-1$ 
						    }
						}
			        }
			    }
			    // Set the new selected configuration
				selectedConfiguration = newConfig;
				ManagedBuildManager.setSelectedConfiguration(getProject(), selectedConfiguration);
				clonedConfiguration = getClonedConfig(selectedConfiguration);
				//	Set the current Resource Configuration
				clonedResourceConfig = getCurrentResourceConfig(clonedConfiguration,true);
				
				fOptionBlock.updateValues();
				excludedCheckBox.setSelection(isExcluded());
			}
		}
		return;
	}
	

	/*
	 * This method updates the property page message
	 */
	private void doUpdateMessage(){
		if(selectedConfiguration != null){
			if(selectedConfiguration.isSupported()){
				setMessage(null,IMessageProvider.NONE);
			}
			else{
				IManagedProject mngProj = selectedConfiguration.getManagedProject();
				IProjectType projType = mngProj != null ? mngProj.getProjectType() : null;
				if(projType != null && !projType.isSupported())
					setMessage(ManagedBuilderUIMessages.getResourceString(MSG_UNSUPPORTED_PROJ),IMessageProvider.WARNING);
				else
					setMessage(ManagedBuilderUIMessages.getResourceString(MSG_UNSUPPORTED_CONFIG),IMessageProvider.WARNING);
			}
		} else {
			setMessage(ManagedBuilderUIMessages.getResourceString(MSG_CONFIG_NOTSELECTED),IMessageProvider.WARNING);
		}
		getContainer().updateMessage();
	}

	/*
	 * Returns true if the given resource is created by the buildfile generator
	 */
	public boolean isGeneratedResource(IFile file){
		IConfiguration cfg = getSelectedConfiguration();
		IProject project = getProject();
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		if(cfg == null)
			cfg = info.getDefaultConfiguration();

		if(cfg != null && info != null){
			IManagedBuilderMakefileGenerator makeGen = ManagedBuildManager.getBuildfileGenerator(cfg);
			makeGen.initialize(project,info,null);
			return makeGen.isGeneratedResource(file);
		}
		return false;
	}
	
	/*
	 * Returns true if the tool-chain used in the given configuration 
	 * can build the resource
	 *  
	 * @param file
	 * @param cfg
	 * @return
	 */
	public boolean isBuildResource(IFile file, IConfiguration cfg){
		IResourceConfiguration rcCfg = cfg.getResourceConfiguration(file.getFullPath().toString());
		if(rcCfg != null){
			ITool tools[] = rcCfg.getTools();
			if(tools == null || tools.length == 0)
				return false;
			for (int i = 0; i < tools.length; i++) {
				ITool tool = tools[i];
				if (!tool.getCustomBuildStep() || tool.isExtensionElement())
					return true;
			}
		} else {
			String ext = file.getFileExtension();
			ITool[] tools = cfg.getFilteredTools();
			for (int index = 0; index < tools.length; index++) {
				ITool tool = tools[index];
				if (tool.buildsFileType(ext))
					return true;
			}
		}
		return false;
	}
	
	/*
	 * Returns true if the given resource can be built by at list one configuration 
	 * defined for the resource project
	 */
	public boolean isBuildResource(IFile file){
		IProject project = file.getProject();
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		if(info != null){
			IManagedProject managedProject = info.getManagedProject();
			if(managedProject != null){
				IConfiguration cfgs[] = managedProject.getConfigurations();
				if(cfgs != null && cfgs.length > 0){
					for(int i = 0; i < cfgs.length; i++){
						if(isBuildResource(file,cfgs[i]))
							return true;
					}
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	
	
	protected void performDefaults() {
		fOptionBlock.performDefaults();
		excludedCheckBox.setSelection(getCurrentResourceConfigClone().isExcluded());
		super.performDefaults();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		
		boolean retStatus = true;
		
		//	If there is no content on the page, then there is nothing to do
		if (noContentOnPage) return true;

		//	If the user did not visit this page, then there is nothing to do.
		if (!displayedConfig) return true;

		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				if(containsDefaults()){
					removeCurrentResourceConfig();
					return;
				}
				
				fOptionBlock.performApply(monitor);
				getCurrentResourceConfig(true).setExclude(getCurrentResourceConfigClone().isExcluded());
			}
		};
		IRunnableWithProgress op = new WorkspaceModifyDelegatingOperation(runnable);
		try {
			new ProgressMonitorDialog(getShell()).run(false, true, op);
		} catch (InvocationTargetException e) {
			Throwable e1 = e.getTargetException();
			ManagedBuilderUIPlugin.errorDialog(getShell(), ManagedBuilderUIMessages.getResourceString("ManagedProjectPropertyPage.internalError"),e1.toString(), e1); //$NON-NLS-1$
			return false;
		} catch (InterruptedException e) {
			// cancelled
			return false;
		}

		// Write out the build model info
		ManagedBuildManager.setDefaultConfiguration(getProject(), getSelectedConfiguration());
		
		if (getCurrentResourceConfigClone().isDirty()) {
//			selectedConfiguration.setRebuildState(true);
			getCurrentResourceConfigClone().setDirty(false);
		}

		retStatus = ManagedBuildManager.saveBuildInfo(getProject(), false);
		
		IManagedBuildInfo bi = ManagedBuildManager.getBuildInfo(getProject());
		if (bi != null & bi instanceof ManagedBuildInfo) {
			((ManagedBuildInfo)bi).initializePathEntries();
		}
		
		EnvironmentVariableProvider.fUserSupplier.checkInexistentConfigurations(clonedConfiguration.getManagedProject());

		return retStatus;
	}
	
	public boolean containsDefaults(){
		//  Check for a non-default "excluded" value
		if(getCurrentResourceConfigClone().isExcluded() != DEFAULT_EXCLUDE_VALUE)
			return false;
		return fOptionBlock.containsDefaults();
	}
	
    public boolean performCancel() {
		//	If there is no content on the page, then there is nothing to do
		if (noContentOnPage) return true;

    	EnvironmentVariableProvider.fUserSupplier.checkInexistentConfigurations(clonedConfiguration.getManagedProject());

        return true;
    }


	private void populateConfigurations() {
		
		ManagedBuildManager.setSelectedConfiguration(getProject(), selectedConfiguration);
		// If the config select widget is not there yet, just stop
		if (configSelector == null)
			return;

		// Find the configurations defined for the platform
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(getProject());
		configurations = info.getManagedProject().getConfigurations();
		if (configurations.length == 0)
			return;

		// Clear and replace the contents of the selector widget
		configSelector.removeAll();
		configSelector.setItems(getConfigurationNames());

		// Make sure the active configuration is selected
		IConfiguration defaultConfig = info.getDefaultConfiguration();
		int index = configSelector.indexOf(defaultConfig.getName());
		configSelector.select(index == -1 ? 0 : index);
		handleConfigSelection();

	}

	
	/**
	 * @return Returns the currentResourceConfig.
	 */
	public IResourceConfiguration getCurrentResourceConfigClone() {
		return clonedResourceConfig;
	}
	
	public IResourceConfiguration getCurrentResourceConfig(boolean create) {
		return getCurrentResourceConfig(selectedConfiguration,create);
	}


	private IResourceConfiguration getCurrentResourceConfig(IConfiguration cfg, boolean create){
		IResourceConfiguration rcCfg = cfg.getResourceConfiguration(((IFile)getElement()).getFullPath().toString());
		if(rcCfg == null && create)
			rcCfg = cfg.createResourceConfiguration((IFile)getElement());
		return rcCfg;
	}
	
	public boolean removeCurrentResourceConfig(){
		IResourceConfiguration rcCfg = getCurrentResourceConfig(false);
		if(rcCfg != null){
			selectedConfiguration.removeResourceConfiguration(rcCfg);
			return true;
		}
		return false;
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferencePageContainer#updateButtons()
	 */
	public void updateButtons() {
	}
	/**
	 * @see org.eclipse.jface.preference.IPreferencePageContainer#updateMessage()
	 */
	public void updateMessage() {
	}
	/**
	 * @see org.eclipse.jface.preference.IPreferencePageContainer#updateTitle()
	 */
	public void updateTitle() {
	}
	
	public void updateContainer() {
		fOptionBlock.update();
		setValid(fOptionBlock.isValid());
		setErrorMessage(fOptionBlock.getErrorMessage());
	}

	public boolean isValid() {
		updateContainer();
		return super.isValid();
	}
	
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		fOptionBlock.setVisible(visible);
		if (visible) {
			fOptionBlock.updateValues();
			displayedConfig = true;
		}
	}

	public IPreferenceStore getPreferenceStore()
	{
		return fOptionBlock.getPreferenceStore();
	}

	/* (non-Javadoc)
	 * Return the IPreferenceStore of the Tool Settings block
	 */
	public BuildToolSettingsPreferenceStore getToolSettingsPreferenceStore()
	{
		return fOptionBlock.getToolSettingsPreferenceStore();
	}

	public Preferences getPreferences()
	{
		return null;
	}
	public void enableConfigSelection (boolean enable) {
		configSelector.setEnabled(enable);
	}
	/**
	 * @return Returns the isExcluded.
	 */
	public boolean isExcluded(){
		return getCurrentResourceConfigClone().isExcluded();
	}

	/**
	 * @param isExcluded The isExcluded to set.
	 */
	public void setExcluded(boolean isExcluded) {
		getCurrentResourceConfigClone().setExclude(isExcluded);
		fOptionBlock.updateValues();
	}
}
