package org.eclipse.cdt.ui;

/***********************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * IBM Corp. - Rational Software 
***********************************************************************/

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.browser.AllTypesCache;
import org.eclipse.cdt.core.browser.IWorkingCopyProvider;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.model.IBufferFactory;
import org.eclipse.cdt.internal.corext.refactoring.base.Refactoring;
import org.eclipse.cdt.internal.ui.CElementAdapterFactory;
import org.eclipse.cdt.internal.ui.ICStatusConstants;
import org.eclipse.cdt.internal.ui.IContextMenuConstants;
import org.eclipse.cdt.internal.ui.ResourceAdapterFactory;
import org.eclipse.cdt.internal.ui.buildconsole.BuildConsoleManager;
import org.eclipse.cdt.internal.ui.cview.CView;
import org.eclipse.cdt.internal.ui.editor.CDocumentProvider;
import org.eclipse.cdt.internal.ui.editor.CustomBufferFactory;
import org.eclipse.cdt.internal.ui.editor.SharedTextColors;
import org.eclipse.cdt.internal.ui.editor.WorkingCopyManager;
import org.eclipse.cdt.internal.ui.editor.asm.AsmTextTools;
import org.eclipse.cdt.internal.ui.preferences.BuildConsolePreferencePage;
import org.eclipse.cdt.internal.ui.preferences.CEditorPreferencePage;
import org.eclipse.cdt.internal.ui.preferences.CPluginPreferencePage;
import org.eclipse.cdt.internal.ui.preferences.WorkInProgressPreferencePage;
import org.eclipse.cdt.internal.ui.text.CTextTools;
import org.eclipse.cdt.internal.ui.util.ImageDescriptorRegistry;
import org.eclipse.cdt.internal.ui.util.ProblemMarkerManager;
import org.eclipse.cdt.internal.ui.util.Util;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;

public class CUIPlugin extends AbstractUIPlugin {

	private ISharedTextColors fSharedTextColors;

	public static final String PLUGIN_ID = "org.eclipse.cdt.ui"; //$NON-NLS-1$
	public static final String PLUGIN_CORE_ID = "org.eclipse.cdt.core"; //$NON-NLS-1$
	public static final String EDITOR_ID = PLUGIN_ID + ".editor.CEditor"; //$NON-NLS-1$
	public static final String CVIEW_ID = PLUGIN_ID + ".CView"; //$NON-NLS-1$
	public static final String C_PROBLEMMARKER = PLUGIN_CORE_ID + ".problem"; //$NON-NLS-1$

	public static final String C_PROJECT_WIZARD_ID = PLUGIN_ID + ".wizards.StdCWizard"; //$NON-NLS-1$
	public static final String CPP_PROJECT_WIZARD_ID = PLUGIN_ID + ".wizards.StdCCWizard"; //$NON-NLS-1$

	public static final String FILE_WIZARD_ID = "org.eclipse.ui.wizards.new.file"; //$NON-NLS-1$
	public static final String FOLDER_WIZARD_ID = "org.eclipse.ui.wizards.new.folder"; //$NON-NLS-1$
	public static final String CLASS_WIZARD_ID = "org.eclipse.cdt.ui.wizards.NewClassWizard"; //$NON-NLS-1$
	public static final String SEARCH_ACTION_SET_ID = PLUGIN_ID + ".SearchActionSet"; //$NON-NLS-1$
	public static final String FOLDER_ACTION_SET_ID = PLUGIN_ID + ".CFolderActionSet"; //$NON-NLS-1$
	public static final String BUILDER_ID = PLUGIN_CORE_ID + ".cbuilder"; //$NON-NLS-1$

	private static CUIPlugin fgCPlugin;
	private static ResourceBundle fgResourceBundle;
	private ImageDescriptorRegistry fImageDescriptorRegistry;
	
	static String SEPARATOR = System.getProperty("file.separator"); //$NON-NLS-1$

	private static final String CONTENTASSIST = CUIPlugin.PLUGIN_ID + "/debug/contentassist" ; //$NON-NLS-1$

	
	/**
	 * The id of the C perspective
	 * (value <code>"org.eclipse.cdt.ui.CPerspective"</code>).
	 */	
	public static final String ID_CPERSPECTIVE = PLUGIN_ID + ".CPerspective"; //$NON-NLS-1$

	/**
	 * The id of the C hierarchy perspective
	 * (value <code>"org.eclipse.cdt.ui.CHierarchyPerspective"</code>).
	 */	
	public static final String ID_CHIERARCHY_PERSPECTIVE = PLUGIN_ID + ".CHierarchyPerspective"; //$NON-NLS-1$

	/**
	 * The id of the C Browsing Perspective
	 * (value <code>"org.eclipse.cdt.ui.CBrowsingPerspective"</code>).
	 * 
	 * @since 2.0
	 */
	public static final String ID_CBROWSING_PERSPECTIVE = PLUGIN_ID + ".CBrowsingPerspective"; //$NON-NLS-1$

	/**
	 * The view part id of the C Browsing Projects view
	 * (value <code>"org.eclipse.cdt.ui.ProjectsView"</code>).
	 * 
	 * @since 2.0
	 */
	public static String ID_PROJECTS_VIEW = PLUGIN_ID + ".ProjectsView"; //$NON-NLS-1$

	/**
	 * The view part id of the C Browsing Namespaces view
	 * (value <code>"org.eclipse.cdt.ui.NamespacesView"</code>).
	 * 
	 * @since 2.0
	 */
	public static String ID_NAMESPACES_VIEW = PLUGIN_ID + ".NamespacesView"; //$NON-NLS-1$

	/**
	 * The view part id of the C Browsing Types view
	 * (value <code>"org.eclipse.cdt.ui.TypesView"</code>).
	 * 
	 * @since 2.0
	 */
	public static String ID_TYPES_VIEW = PLUGIN_ID + ".TypesView"; //$NON-NLS-1$

	/**
	 * The view part id of the C Browsing Members view
	 * (value <code>"org.eclipse.cdt.ui.MembersView"</code>).
	 * 
	 * @since 2.0
	 */
	public static String ID_MEMBERS_VIEW = PLUGIN_ID + ".MembersView"; //$NON-NLS-1$

	
	
	// -------- static methods --------

	static {
		try {
			fgResourceBundle = ResourceBundle.getBundle("org.eclipse.cdt.internal.ui.CPluginResources"); //$NON-NLS-1$
		}
		catch (MissingResourceException x) {
			fgResourceBundle = null;
		}
	}

	public synchronized IBufferFactory getBufferFactory() {
		if (fBufferFactory == null)
			fBufferFactory= new CustomBufferFactory();
		return fBufferFactory;
	}
	
	public static IWorkingCopy[] getSharedWorkingCopies() {
		return CCorePlugin.getSharedWorkingCopies(getDefault().getBufferFactory());
	}
	
	public static String getResourceString(String key) {
		try {
			return fgResourceBundle.getString(key);
		}
		catch (MissingResourceException e) {
			return "!" + key + "!"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		catch (NullPointerException e) {
			return "#" + key + "#"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}
	
	public static String getFormattedString(String key, String arg) {
		return MessageFormat.format(getResourceString(key), new String[] { arg });
	}

	public static String getFormattedString(String key, String[] args) {
		return MessageFormat.format(getResourceString(key), args);
	}

	public static ResourceBundle getResourceBundle() {
		return fgResourceBundle;
	}
	
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}

	public static IWorkbenchPage getActivePage() {
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		if (window != null) {
			return window.getActivePage();
		}
		return null;
	}

	public static Shell getActiveWorkbenchShell() {
		 IWorkbenchWindow window= getActiveWorkbenchWindow();
		 if (window != null) {
		 	return window.getShell();
		 }
		 return null;
	}

	public static CUIPlugin getDefault() {
		return fgCPlugin;
	}

	public void log(Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, "Error", e)); //$NON-NLS-1$
	}

	public void log(IStatus status) {
		getLog().log(status);
	}
	
	public void logErrorMessage(String message) {
		log(new Status(IStatus.ERROR, getPluginId(), ICStatusConstants.INTERNAL_ERROR, message, null));
	}
	

	/**
	* Utility method with conventions
	*/
	public static void errorDialog(Shell shell, String title, String message, IStatus s) {
		getDefault().log(s);
		// if the 'message' resource string and the IStatus' message are the same,
		// don't show both in the dialog
		if (s != null && message.equals(s.getMessage())) {
			message = null;
		}
		ErrorDialog.openError(shell, title, message, s);
	}

	/**
	* Utility method with conventions
	*/
	public static void errorDialog(Shell shell, String title, String message, Throwable t) {
		getDefault().log(t);
		IStatus status;
		if (t instanceof CoreException) {
			status = ((CoreException) t).getStatus();
			// if the 'message' resource string and the IStatus' message are the same,
			// don't show both in the dialog
			if (status != null && message.equals(status.getMessage())) {
				message = null;
			}
		} else {
			status = new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, -1, "Internal Error: ", t); //$NON-NLS-1$	
		}
		ErrorDialog.openError(shell, title, message, status);
	}

	// ------ CUIPlugin

	private CoreModel fCoreModel;
	private CDocumentProvider fDocumentProvider;
	private IBufferFactory fBufferFactory;
	private WorkingCopyManager fWorkingCopyManager;
	private CTextTools fTextTools;
	private AsmTextTools fAsmTextTools;
	private ProblemMarkerManager fProblemMarkerManager;
	private BuildConsoleManager fBuildConsoleManager;
	private ResourceAdapterFactory fResourceAdapterFactory;
	private CElementAdapterFactory fCElementAdapterFactory;


	public CUIPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		fgCPlugin = this;
		fDocumentProvider = null;
		fTextTools = null;
	}
		
	/**
	 * Returns the used document provider
	 */
	public synchronized CDocumentProvider getDocumentProvider() {
		if (fDocumentProvider == null) {
			fDocumentProvider = new CDocumentProvider();
		}
		return fDocumentProvider;
	}

	/**
	 * Returns the working copy manager
	 * @return IWorkingCopyManager
	 */
	public synchronized IWorkingCopyManager getWorkingCopyManager() {
		if (fWorkingCopyManager == null) {
			CDocumentProvider provider = getDocumentProvider();
			fWorkingCopyManager = new WorkingCopyManager(provider);
		}
		return fWorkingCopyManager;
	}

	/**
	 * Returns the shared text tools
	 */
	public CTextTools getTextTools() {
		if (fTextTools == null)
			fTextTools = new CTextTools(getPreferenceStore(), CCorePlugin.getDefault().getPluginPreferences());
		return fTextTools;
	}

	/**
	 * Returns the shared assembly text tools
	 */
	public AsmTextTools getAsmTextTools() {
		if (fAsmTextTools == null)
			fAsmTextTools = new AsmTextTools(getPreferenceStore(), CCorePlugin.getDefault().getPluginPreferences());
		return fAsmTextTools;
	}

	public IBuildConsoleManager getConsoleManager() {
		if ( fBuildConsoleManager == null ) {
			fBuildConsoleManager = new BuildConsoleManager();
			fBuildConsoleManager.startup();
		}
		return fBuildConsoleManager;
	}

	/**
	 * @see Plugin#shutdown
	 */
	public void shutdown() throws CoreException {

		AllTypesCache.terminate();
		
		if (fTextTools != null) {
			fTextTools.dispose();
		}
		if (fImageDescriptorRegistry != null)
			fImageDescriptorRegistry.dispose();
		if ( fBuildConsoleManager != null ) {
			fBuildConsoleManager.shutdown();
			fBuildConsoleManager = null;
		}

		unregisterAdapters();

		if (fWorkingCopyManager != null) {
			fWorkingCopyManager.shutdown();
			fWorkingCopyManager= null;
		}
                
		if (fDocumentProvider != null) {
			fDocumentProvider.shutdown();
			fDocumentProvider= null;
		}
		Refactoring.getUndoManager().shutdown();		

		// Do this last.
		super.shutdown();
	}

	private void runUI(Runnable run) {
		Display display;
		display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
			display.asyncExec(run);
		}
		else {
			run.run();
		}
	}

	/**
	 * @see Plugin#startup
	 */
	public void startup() throws CoreException {
		super.startup();
		//Set debug tracing options
		CUIPlugin.getDefault().configurePluginDebugOptions();
		
		registerAdapters();
		AllTypesCache.initialize(new IWorkingCopyProvider() {
			public IWorkingCopy[] getWorkingCopies() {
				return CUIPlugin.getSharedWorkingCopies();
			}
		});
	}

	/**
	 * @see AbstractUIPlugin#initializeDefaultPreferences
	 */
	protected void initializeDefaultPreferences(final IPreferenceStore store) {
		super.initializeDefaultPreferences(store);
		MarkerAnnotationPreferences.initializeDefaultValues(store);	
        PreferenceConstants.initializeDefaultValues(store);
        EditorsUI.useAnnotationsPreferencePage(store);
		runUI(new Runnable() {
			public void run() {
				CPluginPreferencePage.initDefaults(store);
				CEditorPreferencePage.initDefaults(store);
				CView.initDefaults(store);
				BuildConsolePreferencePage.initDefaults(store);
				WorkInProgressPreferencePage.initDefaults(store);
			}
		});
	}

	public CoreModel getCoreModel() {
		return fCoreModel;
	}

	public static String getPluginId() {
		return PLUGIN_ID;
	}

	public static ImageDescriptorRegistry getImageDescriptorRegistry() {
		return getDefault().internalGetImageDescriptorRegistry();
	}

	private ImageDescriptorRegistry internalGetImageDescriptorRegistry() {
		if (fImageDescriptorRegistry == null)
			fImageDescriptorRegistry = new ImageDescriptorRegistry();
		return fImageDescriptorRegistry;
	}

	/**
	 * Returns the problem marker manager
	 */
	public ProblemMarkerManager getProblemMarkerManager() {
		if (fProblemMarkerManager == null)
			fProblemMarkerManager = new ProblemMarkerManager();
		return fProblemMarkerManager;
	}

	protected void registerAdapters() {
		fResourceAdapterFactory = new ResourceAdapterFactory();
		fCElementAdapterFactory = new CElementAdapterFactory();

		IAdapterManager manager = Platform.getAdapterManager();
		manager.registerAdapters(fResourceAdapterFactory, IResource.class);
		manager.registerAdapters(fCElementAdapterFactory, ICElement.class);
	}
        
	private void unregisterAdapters() {
		IAdapterManager manager = Platform.getAdapterManager();
		manager.unregisterAdapters(fResourceAdapterFactory);
		manager.unregisterAdapters(fCElementAdapterFactory);
	}

	public ISharedTextColors getSharedTextColors() {
		if (fSharedTextColors == null)
			fSharedTextColors= new SharedTextColors();
		return fSharedTextColors;
	}
	
	public void configurePluginDebugOptions(){
		
		if(CUIPlugin.getDefault().isDebugging()){
			String option = Platform.getDebugOption(CONTENTASSIST);
			if(option != null) Util.VERBOSE_CONTENTASSIST = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
		}
	}
	/**
	 * Returns an array of all editors that have an unsaved content. If the identical content is 
	 * presented in more than one editor, only one of those editor parts is part of the result.
	 * 
	 * @return an array of all dirty editor parts.
	 */
	
	public static IEditorPart[] getDirtyEditors() {
		Set inputs= new HashSet();
		List result= new ArrayList(0);
		IWorkbench workbench= getDefault().getWorkbench();
		IWorkbenchWindow[] windows= workbench.getWorkbenchWindows();
		for (int i= 0; i < windows.length; i++) {
			IWorkbenchPage[] pages= windows[i].getPages();
			for (int x= 0; x < pages.length; x++) {
				IEditorPart[] editors= pages[x].getDirtyEditors();
				for (int z= 0; z < editors.length; z++) {
					IEditorPart ep= editors[z];
					IEditorInput input= ep.getEditorInput();
					if (!inputs.contains(input)) {
						inputs.add(input);
						result.add(ep);
					}
				}
			}
		}
		return (IEditorPart[])result.toArray(new IEditorPart[result.size()]);
	}
	/**
	 * Returns an array of all instanciated editors. 
	 */
	public static IEditorPart[] getInstanciatedEditors() {
		List result= new ArrayList(0);
		IWorkbench workbench= getDefault().getWorkbench();
		IWorkbenchWindow[] windows= workbench.getWorkbenchWindows();
		for (int windowIndex= 0; windowIndex < windows.length; windowIndex++) {
			IWorkbenchPage[] pages= windows[windowIndex].getPages();
			for (int pageIndex= 0; pageIndex < pages.length; pageIndex++) {
				IEditorReference[] references= pages[pageIndex].getEditorReferences();
				for (int refIndex= 0; refIndex < references.length; refIndex++) {
					IEditorPart editor= references[refIndex].getEditor(false);
					if (editor != null)
						result.add(editor);
				}
			}
		}
		return (IEditorPart[])result.toArray(new IEditorPart[result.size()]);
	}

	/**
	 * Returns the standard display to be used. The method first checks, if
	 * the thread calling this method has an associated display. If so, this
	 * display is returned. Otherwise the method returns the default display.
	 */
	public static Display getStandardDisplay() {
		Display display= Display.getCurrent();
		if (display == null) {
			display= Display.getDefault();
		}
		return display;		
	}	

	/**
	 * Creates the CUIplugin standard groups in a context menu.
	 */
	public static void createStandardGroups(IMenuManager menu) {
		if (!menu.isEmpty())
			return;
			
		menu.add(new Separator(IContextMenuConstants.GROUP_NEW));
		menu.add(new GroupMarker(IContextMenuConstants.GROUP_GOTO));
		menu.add(new Separator(IContextMenuConstants.GROUP_OPEN));
		menu.add(new GroupMarker(IContextMenuConstants.GROUP_SHOW));
		menu.add(new Separator(IContextMenuConstants.GROUP_REORGANIZE));
		menu.add(new Separator(IContextMenuConstants.GROUP_GENERATE));
		menu.add(new Separator(IContextMenuConstants.GROUP_SEARCH));
		menu.add(new Separator(IContextMenuConstants.GROUP_BUILD));
		menu.add(new Separator(IContextMenuConstants.GROUP_ADDITIONS));
		menu.add(new Separator(IContextMenuConstants.GROUP_VIEWER_SETUP));
		menu.add(new Separator(IContextMenuConstants.GROUP_PROPERTIES));
	}

	
}
