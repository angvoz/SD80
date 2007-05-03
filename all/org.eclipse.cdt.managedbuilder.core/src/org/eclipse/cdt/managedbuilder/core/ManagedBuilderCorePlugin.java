/*******************************************************************************
 * Copyright (c) 2003, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.eclipse.cdt.build.internal.core.scannerconfig.CfgDiscoveredPathManager;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.BuildStateManager;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.DbgUtil;
import org.eclipse.cdt.managedbuilder.internal.core.BuilderFactory;
import org.eclipse.cdt.managedbuilder.internal.core.GeneratedMakefileBuilder;
import org.eclipse.cdt.managedbuilder.internal.dataprovider.ProjectConverter;
import org.eclipse.cdt.managedbuilder.internal.scannerconfig.ManagedBuildCPathEntryContainer;
import org.eclipse.cdt.managedbuilder.internal.scannerconfig.ManagedBuildPathEntryContainerInitializer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;


public class ManagedBuilderCorePlugin extends Plugin {
	private static final String PLUGIN_ID = "org.eclipse.cdt.managedbuilder.core"; //$NON-NLS-1$
	// The shared instance
	private static ManagedBuilderCorePlugin plugin;
	// The attribute name for the makefile generator
	public static final String MAKEGEN_ID ="makefileGenerator"; //$NON-NLS-1$
	public static final String COMMANDLINEGEN_ID = "commandlineGenerator"; //$NON-NLS-1$
	// The unique id for all managed make projects 
	public static final String MANAGED_MAKE_PROJECT_ID = ManagedBuilderCorePlugin.getUniqueIdentifier() + ".managedMake"; //$NON-NLS-1$
	//  NOTE: The code below is for tracking resource renaming and deleting.  This is needed to keep
	//  ResourceConfiguration elements up to date.  It may also be needed by AdditionalInput
	//  elements
	
	private static ResourceChangeHandler2 listener;

//	private DiscoveredPathManager fDiscoveryPathManager;

	/**
	 * @param descriptor
	 */
	public ManagedBuilderCorePlugin() {
		super();
		plugin = this;
	}

	public static String getUniqueIdentifier() {
		if (getDefault() == null) {
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plugin id defined in plugin.xml
			return PLUGIN_ID;
		}
		return getDefault().getBundle().getSymbolicName();
	}
	
	/**
	 * Returns the shared instance.
	 */
	public static ManagedBuilderCorePlugin getDefault() {
		return plugin;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		// Turn on logging for plugin when debugging
		super.start(context);
		configurePluginDebugOptions();
		
 		
		//	  NOTE: The code below is for tracking resource renaming and deleting.  This is needed to keep
		//      ResourceConfiguration elements up to date.  It may also be needed by AdditionalInput
		//      elements
		
//		IJobManager jobManager = Platform.getJobManager();
//		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		listener = new ResourceChangeHandler2();

		ResourcesPlugin.getWorkspace().addResourceChangeListener(
				listener, 
				IResourceChangeEvent.POST_CHANGE 
				| IResourceChangeEvent.PRE_DELETE
				| IResourceChangeEvent.PRE_CLOSE
				/*| IResourceChangeEvent.POST_BUILD*/);
		
		BuildStateManager.getInstance().startup();
/*		try {
			jobManager.beginRule(root, null);

			startResourceChangeHandling();
		} catch (Throwable e) {
			//either an IllegalArgumentException is thrown by the jobManager.beginRule
			//or core exception is thrown by the startResourceChangeHandling()
			//in any case, schedule a job with the root rule
			//that will perform the resource change handling initialization
*/			
		//The startResourceChangeHandling() might result in throwing an error
		//see bug# 132001
		//Always schedule a job
//			Job rcJob = new Job(ManagedMakeMessages.getResourceString("ManagedBuilderCorePlugin.resourceChangeHandlingInitializationJob")){ 	//$NON-NLS-1$
//				protected IStatus run(IProgressMonitor monitor) {
//					try{
//						startResourceChangeHandling();
//					} catch (CoreException e){
//						CCorePlugin.log(e);
//						return e.getStatus();
//					}
//					return new Status(
//							IStatus.OK,
//							ManagedBuilderCorePlugin.getUniqueIdentifier(),
//							IStatus.OK,
//							new String(),
//							null);
//				}
//			};
//			
//			rcJob.setRule(root);
//			rcJob.setPriority(Job.INTERACTIVE);
//			rcJob.setSystem(true);
//			rcJob.schedule();
/*
		} finally {
			jobManager.endRule(root);
		}
*/
	}
	
	/*
	 * This method adds a save participant and resource change listener
	 * Throws CoreException if the methods fails to add a save participant.
	 * The resource change listener in not added in this case either.
	 */
//	private void startResourceChangeHandling() throws CoreException{
//		// Set up a listener for resource change events
//		listener = new ResourceChangeHandler();
//		ISavedState lastState =
//			ResourcesPlugin.getWorkspace().addSaveParticipant(ManagedBuilderCorePlugin.this, listener);
//
//		ResourcesPlugin.getWorkspace().addResourceChangeListener(
//				listener, 
//				IResourceChangeEvent.POST_CHANGE 
//				| IResourceChangeEvent.PRE_DELETE
//				| IResourceChangeEvent.PRE_CLOSE
//				/*| IResourceChangeEvent.POST_BUILD*/);
//
//		if (lastState != null) {
//			lastState.processResourceChangeEvents(listener);
//		}
//	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		BuildStateManager.getInstance().shutdown();

		CfgDiscoveredPathManager.stop();
//		if (fDiscoveryPathManager != null) {
//			fDiscoveryPathManager.shutdown();
//			fDiscoveryPathManager = null;
//		}


		
		//	  NOTE: The code below is for tracking resource renaming and deleting.  This is needed to keep
		//      ResourceConfiguration elements up to date.  It may also be needed by AdditionalInput
		//      elements
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(listener);
		IProject projects[] = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for(int i = 0; i < projects.length; i++){
			listener.sendClose(projects[i]);
		}
		listener = null;
		super.stop(context);
	}
	
	private static final String PATH_ENTRY = ManagedBuilderCorePlugin.getUniqueIdentifier() + "/debug/pathEntry"; //$NON-NLS-1$
	private static final String PATH_ENTRY_INIT = ManagedBuilderCorePlugin.getUniqueIdentifier() + "/debug/pathEntryInit"; //$NON-NLS-1$
	private static final String BUILDER = ManagedBuilderCorePlugin.getUniqueIdentifier() + "/debug/builder"; //$NON-NLS-1$
	private static final String BUILD_MODEL = ManagedBuilderCorePlugin.getUniqueIdentifier() + "/debug/buildModel"; //$NON-NLS-1$

	public static void log(IStatus status) {
		ResourcesPlugin.getPlugin().getLog().log(status);
	}

	public static void log(Throwable e) {
		if (e instanceof InvocationTargetException)
			e = ((InvocationTargetException) e).getTargetException();
		IStatus status = null;
		if (e instanceof CoreException)
			status = ((CoreException) e).getStatus();
		else
			status = new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.OK, e.getMessage(), e);
		log(status);
	}

	/**
	 * 
	 */
	private void configurePluginDebugOptions() {
		if (isDebugging()) {
			String pathInit = Platform.getDebugOption(PATH_ENTRY_INIT);
			if (pathInit != null) {
				ManagedBuildPathEntryContainerInitializer.VERBOSE = pathInit.equalsIgnoreCase("true") ; //$NON-NLS-1$
			}
			String pathCalc = Platform.getDebugOption(PATH_ENTRY);
			if (pathCalc != null) {
				ManagedBuildCPathEntryContainer.VERBOSE = pathCalc.equalsIgnoreCase("true") ; //$NON-NLS-1$
			}
			String builder = Platform.getDebugOption(BUILDER);
			if (builder != null) {
				GeneratedMakefileBuilder.VERBOSE = builder.equalsIgnoreCase("true") ; //$NON-NLS-1$
			}
			String buildModel = Platform.getDebugOption(BUILD_MODEL);
			if(buildModel != null){
				DbgUtil.DEBUG = buildModel.equalsIgnoreCase("true"); //$NON-NLS-1$
			}
		}
	}
	
	public static IBuilder[] createBuilders(IProject project, Map args){
		return BuilderFactory.createBuilders(project, args);
	}
	
	public static IBuilder createCustomBuilder(IConfiguration cfg, String builderId) throws CoreException{
		return BuilderFactory.createCustomBuilder(cfg, builderId);
	}
	
	public static IBuilder createCustomBuilder(IConfiguration cfg, IBuilder base){
		return BuilderFactory.createCustomBuilder(cfg, base);
	}
	
	public static IBuilder createBuilderForEclipseBuilder(IConfiguration cfg, String eclipseBuilderID) throws CoreException {
		return BuilderFactory.createBuilderForEclipseBuilder(cfg, eclipseBuilderID);
	}
	
	public boolean isOldStyleMakeProject(IProject project){
		return ProjectConverter.isOldStyleMakeProject(project);
	}

	public void convertOldStdMakeToNewStyle(IProject project, IProgressMonitor monitor) throws CoreException{
		ProjectConverter.convertOldStdMakeToNewStyle(project, monitor);
	}

	
//	public IDiscoveredPathManager getDiscoveryManager() {
//		if ( fDiscoveryPathManager == null) {
//			fDiscoveryPathManager = new DiscoveredPathManager();
//			fDiscoveryPathManager.startup();
//		}
//		return fDiscoveryPathManager;
//	}
}
