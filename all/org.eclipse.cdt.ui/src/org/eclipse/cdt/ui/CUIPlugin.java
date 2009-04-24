/*******************************************************************************
 * Copyright (c) 2003, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     IBM Corp. - Rational Software
 *     Markus Schorn (Wind River Systems)
 *     Anton Leherbauer (Wind River Systems)
 *     Jeff Johnston (Red Hat Inc.)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.ConfigurationElementSorter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import com.ibm.icu.text.MessageFormat;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.model.IWorkingCopyProvider;

import org.eclipse.cdt.internal.core.dom.rewrite.ASTRewriteAnalyzer;
import org.eclipse.cdt.internal.core.model.IBufferFactory;
import org.eclipse.cdt.internal.corext.template.c.CContextType;
import org.eclipse.cdt.internal.corext.template.c.CodeTemplateContextType;
import org.eclipse.cdt.internal.corext.template.c.CommentContextType;
import org.eclipse.cdt.internal.corext.template.c.DocCommentContextType;
import org.eclipse.cdt.internal.corext.template.c.FileTemplateContextType;

import org.eclipse.cdt.internal.ui.CElementAdapterFactory;
import org.eclipse.cdt.internal.ui.ICStatusConstants;
import org.eclipse.cdt.internal.ui.IContextMenuConstants;
import org.eclipse.cdt.internal.ui.ResourceAdapterFactory;
import org.eclipse.cdt.internal.ui.buildconsole.BuildConsoleManager;
import org.eclipse.cdt.internal.ui.editor.ASTProvider;
import org.eclipse.cdt.internal.ui.editor.CDocumentProvider;
import org.eclipse.cdt.internal.ui.editor.CustomBufferFactory;
import org.eclipse.cdt.internal.ui.editor.WorkingCopyManager;
import org.eclipse.cdt.internal.ui.editor.asm.AsmTextTools;
import org.eclipse.cdt.internal.ui.refactoring.CTextFileChangeFactory;
import org.eclipse.cdt.internal.ui.text.CTextTools;
import org.eclipse.cdt.internal.ui.text.PreferencesAdapter;
import org.eclipse.cdt.internal.ui.text.c.hover.CEditorTextHoverDescriptor;
import org.eclipse.cdt.internal.ui.text.doctools.DocCommentOwnerManager;
import org.eclipse.cdt.internal.ui.text.doctools.EditorReopener;
import org.eclipse.cdt.internal.ui.text.folding.CFoldingStructureProviderRegistry;
import org.eclipse.cdt.internal.ui.util.ImageDescriptorRegistry;
import org.eclipse.cdt.internal.ui.util.ProblemMarkerManager;
import org.eclipse.cdt.internal.ui.util.Util;
import org.eclipse.cdt.internal.ui.viewsupport.CDTContextActivator;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class CUIPlugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.eclipse.cdt.ui"; //$NON-NLS-1$
	public static final String PLUGIN_CORE_ID = "org.eclipse.cdt.core"; //$NON-NLS-1$
	public static final String EDITOR_ID = PLUGIN_ID + ".editor.CEditor"; //$NON-NLS-1$
	public static final String CVIEW_ID = PLUGIN_ID + ".CView"; //$NON-NLS-1$
	public static final String C_PROBLEMMARKER = PLUGIN_CORE_ID + ".problem"; //$NON-NLS-1$

	public static final String ID_COMMENT_OWNER= PLUGIN_ID+".DocCommentOwner"; //$NON-NLS-1$
	
    public static final String ID_INCLUDE_BROWSER= PLUGIN_ID + ".includeBrowser"; //$NON-NLS-1$
    public static final String ID_CALL_HIERARCHY= PLUGIN_ID + ".callHierarchy"; //$NON-NLS-1$
	public static final String ID_TYPE_HIERARCHY = PLUGIN_ID + ".typeHierarchy"; //$NON-NLS-1$
    
	public static final String C_PROJECT_WIZARD_ID = PLUGIN_ID + ".wizards.StdCWizard"; //$NON-NLS-1$
	public static final String CPP_PROJECT_WIZARD_ID = PLUGIN_ID + ".wizards.StdCCWizard"; //$NON-NLS-1$

	public final static String CWIZARD_CATEGORY_ID = "org.eclipse.cdt.ui.newCWizards"; //$NON-NLS-1$
	public final static String CCWIZARD_CATEGORY_ID = "org.eclipse.cdt.ui.newCCWizards"; //$NON-NLS-1$
	
	public static final String SEARCH_ACTION_SET_ID = PLUGIN_ID + ".SearchActionSet"; //$NON-NLS-1$
	public static final String BUILDER_ID = PLUGIN_CORE_ID + ".cbuilder"; //$NON-NLS-1$

	private static CUIPlugin fgCPlugin;
	private static ResourceBundle fgResourceBundle;

	private static final String CONTENTASSIST = CUIPlugin.PLUGIN_ID + "/debug/contentassist" ; //$NON-NLS-1$

	/**
	 * The id of the C perspective
	 * (value <code>"org.eclipse.cdt.ui.CPerspective"</code>).
	 */	
	public static final String ID_CPERSPECTIVE = PLUGIN_ID + ".CPerspective"; //$NON-NLS-1$

	/**
	 * The id of the C hierarchy perspective
	 * (value <code>"org.eclipse.cdt.ui.CHierarchyPerspective"</code>).
	 * 
	 * @deprecated This perspective no longer exists.
	 * @noreference This field is not intended to be referenced by clients.
	 */	
	@Deprecated
	public static final String ID_CHIERARCHY_PERSPECTIVE = PLUGIN_ID + ".CHierarchyPerspective"; //$NON-NLS-1$

	/**
	 * The id of the C Browsing Perspective
	 * (value <code>"org.eclipse.cdt.ui.CBrowsingPerspective"</code>).
	 * 
	 * @since 2.0
	 * @deprecated This perspective no longer exists.
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public static final String ID_CBROWSING_PERSPECTIVE = PLUGIN_ID + ".CBrowsingPerspective"; //$NON-NLS-1$

	/**
	 * The view part id of the C Browsing Projects view
	 * (value <code>"org.eclipse.cdt.ui.ProjectsView"</code>).
	 * 
	 * @since 2.0
	 * @deprecated This view no longer exists.
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public static String ID_PROJECTS_VIEW = PLUGIN_ID + ".ProjectsView"; //$NON-NLS-1$

	/**
	 * The view part id of the C Browsing Namespaces view
	 * (value <code>"org.eclipse.cdt.ui.NamespacesView"</code>).
	 * 
	 * @since 2.0
	 * @deprecated This view no longer exists.
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public static String ID_NAMESPACES_VIEW = PLUGIN_ID + ".NamespacesView"; //$NON-NLS-1$

	/**
	 * The view part id of the C Browsing Types view
	 * (value <code>"org.eclipse.cdt.ui.TypesView"</code>).
	 * 
	 * @since 2.0
	 * @deprecated This view no longer exists.
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public static String ID_TYPES_VIEW = PLUGIN_ID + ".TypesView"; //$NON-NLS-1$

	/**
	 * The view part id of the C Browsing Members view
	 * (value <code>"org.eclipse.cdt.ui.MembersView"</code>).
	 * 
	 * @since 2.0
	 * @deprecated This view no longer exists.
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public static String ID_MEMBERS_VIEW = PLUGIN_ID + ".MembersView"; //$NON-NLS-1$

	/**
	 * The key to store customized templates. 
	 * @since 3.0
	 */
	private static final String CUSTOM_TEMPLATES_KEY= "org.eclipse.cdt.ui.text.templates.custom"; //$NON-NLS-1$

	/**
	 * The id of the C Element Creation action set
	 * (value <code>"org.eclipse.cdt.ui.CElementCreationActionSet"</code>).
	 * 
	 * @since 2.0
	 */
	public static final String ID_CELEMENT_CREATION_ACTION_SET= "org.eclipse.cdt.ui.CElementCreationActionSet"; //$NON-NLS-1$
	
	/**
	 * The id of the scope used by all the CDT views
	 * (value <code>"org.eclipse.cdt.ui.scope"</code>).
	 * @since 4.0
	 */
	public static final String CVIEWS_SCOPE = "org.eclipse.cdt.ui.cViewScope"; //$NON-NLS-1$
	
	/**
	 * The key to store customized code templates. 
	 * @since 5.0
	 */
	private static final String CODE_TEMPLATES_KEY= "org.eclipse.cdt.ui.text.custom_code_templates"; //$NON-NLS-1$

	// -------- static methods --------

	static {
		try {
			fgResourceBundle = ResourceBundle.getBundle("org.eclipse.cdt.internal.ui.CPluginResources"); //$NON-NLS-1$
		}
		catch (MissingResourceException x) {
			fgResourceBundle = null;
		}
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
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
		return MessageFormat.format(getResourceString(key), new Object[] {arg});
	}

	@SuppressWarnings("cast") // java.text.MessageFormat would require the cast
	public static String getFormattedString(String key, String[] args) {
		return MessageFormat.format(getResourceString(key), (Object[]) args);
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

	public static void log(Throwable e) {
		log("Error", e); //$NON-NLS-1$
	}

	public static void log(String message, Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, message, e));
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	
	public void logErrorMessage(String message) {
		log(new Status(IStatus.ERROR, getPluginId(), ICStatusConstants.INTERNAL_ERROR, message, null));
	}
	

	/**
	* Utility method with conventions
	* @param logError TODO
	*/
	public static void errorDialog(Shell shell, String title, String message, IStatus s, boolean logError) {
		if (logError)
		    log(s);
		
		// if the 'message' resource string and the IStatus' message are the same,
		// don't show both in the dialog
		if (s != null && message.equals(s.getMessage())) {
			message = null;
		}
		ErrorDialog.openError(shell, title, message, s);
	}

	/**
	* Utility method with conventions
	* @param logError TODO
	*/
	public static void errorDialog(Shell shell, String title, String message, Throwable t, boolean logError) {
		if (logError)
		    log(t);	
		
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

	private ImageDescriptorRegistry fImageDescriptorRegistry;
	private CEditorTextHoverDescriptor[] fCEditorTextHoverDescriptors;

	/**
	 * The extension point registry for the <code>org.eclipse.cdt.ui.foldingStructureProviders</code>
	 * extension point.
	 */
	private CFoldingStructureProviderRegistry fFoldingStructureProviderRegistry;

	/**
	 * The combined preference store.
	 * @since 3.0
	 */
	private IPreferenceStore fCombinedPreferenceStore;

	private CoreModel fCoreModel;
	private CDocumentProvider fDocumentProvider;
	private IBufferFactory fBufferFactory;
	private WorkingCopyManager fWorkingCopyManager;
	private CTextTools fTextTools;
	private AsmTextTools fAsmTextTools;
	private ProblemMarkerManager fProblemMarkerManager;
	private Map<String, BuildConsoleManager> fBuildConsoleManagers;
	private ResourceAdapterFactory fResourceAdapterFactory;
	private CElementAdapterFactory fCElementAdapterFactory;

	/** 
	 * The template context type registry for the C editor. 
	 * @since 3.0
	 */
	private ContributionContextTypeRegistry fContextTypeRegistry;

	/**
	 * The template store for the C editor. 
	 * @since 3.0
	 */
	private TemplateStore fTemplateStore;

	/**
	 * The AST provider.
	 * @since 4.0
	 */
	private ASTProvider fASTProvider;

	/** 
	 * The code template context type registry for the C editor. 
	 * @since 5.0
	 */
	private ContextTypeRegistry fCodeTemplateContextTypeRegistry;
	
	/**
	 * The code template store for the C editor. 
	 * @since 5.0
	 */
	private TemplateStore fCodeTemplateStore;

	public CUIPlugin() {
		fgCPlugin = this;
		fDocumentProvider = null;
		fTextTools = null;		
		fBuildConsoleManagers = new HashMap<String, BuildConsoleManager>();
	}
		
	/**
	 * Returns the used document provider.
	 * 
	 * @noreference This method is not intended to be referenced by clients.
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
	 * Returns the shared C/C++ text tools.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public CTextTools getTextTools() {
		if (fTextTools == null)
			fTextTools = new CTextTools();
		return fTextTools;
	}

	/**
	 * Returns the shared assembly text tools.
	 * @deprecated Use methods provided by {@link CDTUITools} instead.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public AsmTextTools getAsmTextTools() {
		if (fAsmTextTools == null)
			fAsmTextTools = new AsmTextTools();
		return fAsmTextTools;
	}

	/**
	 * Return the default console manager.
	 * @return IBuildConsoleManager
	 */
	public IBuildConsoleManager getConsoleManager() {
		return getConsoleManager(getResourceString("BuildConsole.name"), BuildConsoleManager.DEFAULT_CONTEXT_MENU_ID); //$NON-NLS-1$
	}

	/**
	 * Return a console manager specified by id.
	 * @param name console name
	 * @param id console id
	 * @return IBuildConsoleManager
	 */	
	public IBuildConsoleManager getConsoleManager(String name, String id) {
		BuildConsoleManager manager = fBuildConsoleManagers.get(id);
		if (manager == null ) {
			manager = new BuildConsoleManager();
			fBuildConsoleManagers.put(id, manager);
			manager.startup(name, id);
		}
		return manager;
	}

	/*
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);

		//Set debug tracing options
		configurePluginDebugOptions();
		
		registerAdapters();
		IWorkingCopyProvider workingCopyProvider = new IWorkingCopyProvider() {
			public IWorkingCopy[] getWorkingCopies() {
				return CUIPlugin.getSharedWorkingCopies();
			}
		};
		CCorePlugin.getDefault().getDOM().setWorkingCopyProvider(workingCopyProvider);
		
		// init ast provider
		getASTProvider();
		CDTContextActivator.getInstance().install();
		
		DocCommentOwnerManager.getInstance().addListener(new EditorReopener());
		ASTRewriteAnalyzer.setCTextFileChangeFactory(new CTextFileChangeFactory());
		
		// start make-ui plugin, such that it can check for project conversions.
		Job job= new Job(Messages.CUIPlugin_jobStartMakeUI) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				Bundle bundle= Platform.getBundle("org.eclipse.cdt.make.ui"); //$NON-NLS-1$
				try {
					if (bundle != null) {
						switch (bundle.getState()) {
						case Bundle.RESOLVED:
						case Bundle.STARTING:  // because make.ui uses lazy activation, we need to start it.
							bundle.start(Bundle.START_TRANSIENT);
							break;
						}
					}
				} catch (BundleException e) {
					return new Status(IStatus.WARNING, PLUGIN_ID, e.getMessage(), e);
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		CDTContextActivator.getInstance().uninstall();
		if (fASTProvider != null) {
			fASTProvider.dispose();
			fASTProvider= null;
		}
		if (fTextTools != null) {
			fTextTools.dispose();
			fTextTools= null;
		}
		if (fAsmTextTools != null) {
			fAsmTextTools.dispose();
			fAsmTextTools= null;
		}
		if (fImageDescriptorRegistry != null) {
			fImageDescriptorRegistry.dispose();
			fImageDescriptorRegistry= null;
		}
		if (fBuildConsoleManagers != null ) {
			Object[] bcm = fBuildConsoleManagers.values().toArray();
			for (int i = 0; i < bcm.length; ++i) {
				BuildConsoleManager m = (BuildConsoleManager)bcm[i];
				if (m != null)
					m.shutdown();
			}
			fBuildConsoleManagers.clear();
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

		// Do this last.
		super.stop(context);
	}

	public CoreModel getCoreModel() {
		return fCoreModel;
	}

	public static String getPluginId() {
		return PLUGIN_ID;
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static ImageDescriptorRegistry getImageDescriptorRegistry() {
		return getDefault().internalGetImageDescriptorRegistry();
	}

	private ImageDescriptorRegistry internalGetImageDescriptorRegistry() {
		if (fImageDescriptorRegistry == null)
			fImageDescriptorRegistry = new ImageDescriptorRegistry();
		return fImageDescriptorRegistry;
	}

	/**
	 * Returns the problem marker manager.
	 * 
	 * @noreference This method is not intended to be referenced by clients.
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

	/** 
	 * @deprecated Use {@link EditorsUI#getSharedTextColors()} instead.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public ISharedTextColors getSharedTextColors() {
		return EditorsUI.getSharedTextColors();
	}
	
	public void configurePluginDebugOptions(){
		if(isDebugging()){
			String option = Platform.getDebugOption(CONTENTASSIST);
			if(option != null) Util.VERBOSE_CONTENTASSIST = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
		}
	}

	/**
	 * Returns a combined preference store, this store is read-only.
	 * 
	 * @return the combined preference store
	 * 
	 * @since 3.0
	 */
	public IPreferenceStore getCombinedPreferenceStore() {
		if (fCombinedPreferenceStore == null) {
			IPreferenceStore generalTextStore= EditorsUI.getPreferenceStore(); 
			fCombinedPreferenceStore= new ChainedPreferenceStore(new IPreferenceStore[] { getPreferenceStore(), new PreferencesAdapter(CCorePlugin.getDefault().getPluginPreferences()), generalTextStore });
		}
		return fCombinedPreferenceStore;
	}
	

	/**
	 * Returns an array of all editors that have an unsaved content. If the identical content is 
	 * presented in more than one editor, only one of those editor parts is part of the result.
	 * 
	 * @return an array of all dirty editor parts.
	 */	
	public static IEditorPart[] getDirtyEditors() {
		Set<IEditorInput> inputs= new HashSet<IEditorInput>();
		List<IEditorPart> result= new ArrayList<IEditorPart>(0);
		IWorkbench workbench= getDefault().getWorkbench();
		IWorkbenchWindow[] windows= workbench.getWorkbenchWindows();
		for (IWorkbenchWindow window : windows) {
			IWorkbenchPage[] pages= window.getPages();
			for (IWorkbenchPage page : pages) {
				IEditorPart[] editors= page.getDirtyEditors();
				for (IEditorPart ep : editors) {
					IEditorInput input= ep.getEditorInput();
					if (!inputs.contains(input)) {
						inputs.add(input);
						result.add(ep);
					}
				}
			}
		}
		return result.toArray(new IEditorPart[result.size()]);
	}
	/**
	 * Returns an array of all instanciated editors. 
	 */
	public static IEditorPart[] getInstanciatedEditors() {
		List<IEditorPart> result= new ArrayList<IEditorPart>(0);
		IWorkbench workbench= getDefault().getWorkbench();
		IWorkbenchWindow[] windows= workbench.getWorkbenchWindows();
		for (IWorkbenchWindow window : windows) {
			IWorkbenchPage[] pages= window.getPages();
			for (IWorkbenchPage page : pages) {
				IEditorReference[] references= page.getEditorReferences();
				for (IEditorReference reference : references) {
					IEditorPart editor= reference.getEditor(false);
					if (editor != null)
						result.add(editor);
				}
			}
		}
		return result.toArray(new IEditorPart[result.size()]);
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

	/**
	 * Returns all C editor text hovers contributed to the workbench.
	 * 
	 * @return an array of CEditorTextHoverDescriptor
	 * 
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public CEditorTextHoverDescriptor[] getCEditorTextHoverDescriptors() {
		if (fCEditorTextHoverDescriptors == null) {
			fCEditorTextHoverDescriptors= CEditorTextHoverDescriptor.getContributedHovers();
			ConfigurationElementSorter sorter= new ConfigurationElementSorter() {
				/**
				 * {@inheritDoc}
				 */
				@Override
				public IConfigurationElement getConfigurationElement(Object object) {
					return ((CEditorTextHoverDescriptor)object).getConfigurationElement();
				}
			};
			sorter.sort(fCEditorTextHoverDescriptors);
		
			// The Problem hover has to be the first and the Annotation hover has to be the last one in the CDT UI's hover list
			int length= fCEditorTextHoverDescriptors.length;
			int first= -1;
			int last= length - 1;
			int problemHoverIndex= -1;
			int annotationHoverIndex= -1;
			for (int i= 0; i < length; i++) {
				if (!fCEditorTextHoverDescriptors[i].getId().startsWith(PLUGIN_ID)) {
					if (problemHoverIndex == -1 || annotationHoverIndex == -1) {
						continue;
					}
					last= i - 1;
					break;
				}
				if (first == -1)
					first= i;
				
				if (fCEditorTextHoverDescriptors[i].getId().equals("org.eclipse.cdt.ui.AnnotationHover")) { //$NON-NLS-1$
					annotationHoverIndex= i;
					continue;
				}
				if (fCEditorTextHoverDescriptors[i].getId().equals("org.eclipse.cdt.ui.ProblemHover")) { //$NON-NLS-1$
					problemHoverIndex= i;
					continue;
				}
			}
	
			CEditorTextHoverDescriptor hoverDescriptor= null;
			
			if (first > -1 && problemHoverIndex > -1 && problemHoverIndex > first) {
				// move problem hover to beginning
				hoverDescriptor= fCEditorTextHoverDescriptors[problemHoverIndex];
				System.arraycopy(fCEditorTextHoverDescriptors, first, fCEditorTextHoverDescriptors, first+1, problemHoverIndex - first);
				fCEditorTextHoverDescriptors[first]= hoverDescriptor;

				// update annotation hover index if needed
				if (annotationHoverIndex >= first && annotationHoverIndex < problemHoverIndex)
					annotationHoverIndex++;
			}
			
			if (annotationHoverIndex > -1 && annotationHoverIndex < last) {
				// move annotation hover to end
				hoverDescriptor= fCEditorTextHoverDescriptors[annotationHoverIndex];
				System.arraycopy(fCEditorTextHoverDescriptors, annotationHoverIndex+1, fCEditorTextHoverDescriptors, annotationHoverIndex, last - annotationHoverIndex);
				fCEditorTextHoverDescriptors[last]= hoverDescriptor;
			}

			// Move Best Match hover to front
			for (int i= 0; i < length; i++) {
				if (PreferenceConstants.ID_BESTMATCH_HOVER.equals(fCEditorTextHoverDescriptors[i].getId())) {
					if (i > 0) {
						// move to top
						CEditorTextHoverDescriptor bestMatchHover= fCEditorTextHoverDescriptors[i];
						System.arraycopy(fCEditorTextHoverDescriptors, 0, fCEditorTextHoverDescriptors, 1, i);
						fCEditorTextHoverDescriptors[0]= bestMatchHover;
					}
					break;
				}
				
			}
		}
		return fCEditorTextHoverDescriptors;
	} 

	/**
	 * Resets the C editor text hovers contributed to the workbench.
	 * <p>
	 * This will force a rebuild of the descriptors the next time
	 * a client asks for them.
	 * </p>
	 * 
	 */
	public void resetCEditorTextHoverDescriptors() {
		fCEditorTextHoverDescriptors= null;
	}

	/**
	 * Returns the registry of the extensions to the <code>org.eclipse.cdt.ui.foldingStructureProviders</code>
	 * extension point.
	 * 
	 * @return the registry of contributed <code>ICFoldingStructureProvider</code>
	 * 
	 * @noreference This method is not intended to be referenced by clients.
	 * @since 3.0
	 */
	public synchronized CFoldingStructureProviderRegistry getFoldingStructureProviderRegistry() {
		if (fFoldingStructureProviderRegistry == null)
			fFoldingStructureProviderRegistry= new CFoldingStructureProviderRegistry();
		return fFoldingStructureProviderRegistry;
	}

	/**
	 * Returns the template context type registry for the C plugin.
	 * 
	 * @return the template context type registry for the C plugin
	 * @since 3.0
	 */
	public ContextTypeRegistry getTemplateContextRegistry() {
		if (fContextTypeRegistry == null) {
			fContextTypeRegistry= new ContributionContextTypeRegistry(EDITOR_ID);
			fContextTypeRegistry.addContextType(CContextType.ID);
			fContextTypeRegistry.addContextType(CommentContextType.ID);
			fContextTypeRegistry.addContextType(DocCommentContextType.ID);
		}
		return fContextTypeRegistry;
	}

	/**
	 * Returns the template store for the C editor templates.
	 * 
	 * @return the template store for the C editor templates
	 * @since 3.0
	 */
	public TemplateStore getTemplateStore() {
		if (fTemplateStore == null) {
			fTemplateStore = new ContributionTemplateStore(getTemplateContextRegistry(), getPreferenceStore(), CUSTOM_TEMPLATES_KEY);
			try {
				fTemplateStore.load();
			} catch (IOException e) {
				log(e);
			}
		}
		return fTemplateStore;
	}

	/**
	 * Returns the template context type registry for the code generation
	 * templates.
	 * 
	 * @return the template context type registry for the code generation
	 *         templates
	 * @since 5.0
	 */
	public ContextTypeRegistry getCodeTemplateContextRegistry() {
		if (fCodeTemplateContextTypeRegistry == null) {
			fCodeTemplateContextTypeRegistry= new ContributionContextTypeRegistry();
			
			CodeTemplateContextType.registerContextTypes(fCodeTemplateContextTypeRegistry);
			FileTemplateContextType.registerContextTypes(fCodeTemplateContextTypeRegistry);
		}

		return fCodeTemplateContextTypeRegistry;
	}
	
	/**
	 * Returns the template store for the code generation templates.
	 * 
	 * @return the template store for the code generation templates
	 * @since 5.0
	 */
	public TemplateStore getCodeTemplateStore() {
		if (fCodeTemplateStore == null) {
			IPreferenceStore store= getPreferenceStore();
			fCodeTemplateStore= new ContributionTemplateStore(getCodeTemplateContextRegistry(), store, CODE_TEMPLATES_KEY);

			try {
				fCodeTemplateStore.load();
			} catch (IOException e) {
				log(e);
			}
			
			fCodeTemplateStore.startListeningForPreferenceChanges();
		}
		
		return fCodeTemplateStore;
	}
	
	/**
	 * Returns the AST provider.
	 * 
	 * @return the AST provider
	 * 
	 * @noreference This method is not intended to be referenced by clients.
	 * @since 4.0
	 */
	public synchronized ASTProvider getASTProvider() {
		if (fASTProvider == null)
			fASTProvider= new ASTProvider();
		
		return fASTProvider;
	}
		
	/**
	 * Answers the <code>Shell</code> associated with the active workbench, or 
	 * one of the windows associated with the workbench.
	 */
	public Shell getShell() {
		if (getActiveWorkbenchShell() != null) {
			return getActiveWorkbenchShell();
		}
		IWorkbenchWindow[] windows = getDefault().getWorkbench().getWorkbenchWindows();
		return windows[0].getShell();
	}
	
}
