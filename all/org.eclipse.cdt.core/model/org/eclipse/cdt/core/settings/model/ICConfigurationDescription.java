/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import java.util.Map;

import org.eclipse.cdt.core.cdtvariables.ICdtVariablesContributor;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationDataProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.QualifiedName;

/**
 * this is the element representing configuration and thus this is the root element
 * for configuration-specific settings   
 *
 */
public interface ICConfigurationDescription extends ICSettingContainer, ICSettingObject, ICSettingsStorage{
	/**
	 * Returns whether or not this is an active configuration.
	 * Active configuratiuon is the one that is built by default.
	 * This configuration is returned by the {@link ICProjectDescription#getActiveConfiguration()} call
	 * 
	 * @return boolean
	 */
	boolean isActive();

	/**
	 * returns the human-readable configuration description
	 */
	String getDescription();
	
	/**
	 * sets the configuration description
	 * 
	 * @param des
	 * @throws WriteAccessException when the configuration description is read-only
	 * see {@link CoreModel#getProjectDescription(org.eclipse.core.resources.IProject, boolean)}
	 */
	void setDescription(String des) throws WriteAccessException;
	
	/**
	 * returns the project description this configuration belongs to
	 */
	ICProjectDescription getProjectDescription();
	
	/**
	 * returns the "root" folder description
	 * The root folder description is the default one used for the project root folder
	 * The root folder description can not be null
	 */
	ICFolderDescription getRootFolderDescription();
	
	/**
	 * returns the complete set of folder descriptions defined for this configuration
	 * The folder description is the settings holder for the specified folder
	 * @see ICFolderDescription
	 */
	ICFolderDescription[] getFolderDescriptions();

	/**
	 * returns the complete set of file descriptions defined for this configuration
	 * The file description is the settings holder for the specified file
	 * @see ICFileDescription
	 */
	ICFileDescription[] getFileDescriptions();

	/**
	 * returns the complete set of file and folder descriptions (resource descriptions) defined for this configuration
	 * The resource description is the settings holder for the specified resource
	 * @see ICResourceDescription
	 * @see ICFileDescription
	 * @see ICFolderDescription
	 */
	ICResourceDescription[] getResourceDescriptions();

	/**
	 * Returns the resource description for the given path
	 * 
	 * @param path - project-relative workspace resource path
	 * @param exactPath - when true the resource description of the given path is searched and if not found null is returned,
	 * if false, the resource description applicable for the given path is returned,
	 * i.e. if the configuration contains resource descriptions of the following paths "" and "a/"
	 * getResourceDescription(new Path("a/b"), true) returns null
	 * getResourceDescription(new Path("a/b"), false) returns the "a" folder description
	 * @return {@link ICResourceDescription} that is either a {@link ICFolderDescription} or an {@link ICFileDescription}
	 */
	ICResourceDescription getResourceDescription(IPath path, boolean exactPath);

	/**
	 * removes the given resource description from the configuration
	 * 
	 * @param des
	 * @throws CoreException
	 * @throws WriteAccessException when the configuration description is read-only
	 * see {@link CoreModel#getProjectDescription(org.eclipse.core.resources.IProject, boolean)}
	 */
	void removeResourceDescription(ICResourceDescription des) throws CoreException, WriteAccessException;
	
	/**
	 * creates a new file description for the specified path
	 * @param path project-relative file workspace path
	 * @param base resource description from which settings will be coppied/inheritted 
	 * @throws CoreException
	 * @throws WriteAccessException when the configuration description is read-only
	 * see {@link CoreModel#getProjectDescription(org.eclipse.core.resources.IProject, boolean)}
	 */
	ICFileDescription createFileDescription(IPath path, ICResourceDescription base) throws CoreException, WriteAccessException;
	
	/**
	 * creates a new folder description for the specified path
	 * @param path project-relative folder workspace path
	 * @param base resource description from which settings will be coppied/inheritted 
	 * @throws CoreException
	 * @throws WriteAccessException when the configuration description is read-only
	 * see {@link CoreModel#getProjectDescription(org.eclipse.core.resources.IProject, boolean)}
	 */
	ICFolderDescription createFolderDescription(IPath path, ICFolderDescription base) throws CoreException, WriteAccessException;
	
	/**
	 * returns the ID of the build system used with this configuration
	 * i.e. the id of extension contributing to the org.eclipse.cdt.core.CConfigurationDataProvider extension point
	 * used with this configuration
	 *  
	 * @return String
	 */
	String getBuildSystemId();
	
	/**
	 * This method should be used by the build system only for gettings
	 * the build-system contributed CConfigurationData
	 * @see CConfigurationDataProvider and the org.eclipse.cdt.core.CConfigurationDataProvider extension point
	 */
	CConfigurationData getConfigurationData();
	
	/**
	 * sets this cinfiguration as active
	 * this call is equivalent to {@link ICProjectDescription#setActiveConfiguration(ICConfigurationDescription)}
	 * Active configuratiuon is the one that is built by default.
	 * This configuration is returned by the {@link ICProjectDescription#getActiveConfiguration()} call
	 * 
	 * @throws WriteAccessException when the configuration description is read-only,
	 * see {@link CoreModel#getProjectDescription(org.eclipse.core.resources.IProject, boolean)}
	 */
	void setActive() throws WriteAccessException;
	
	/**
	 * This method should be used by the build system only for updating
	 * the build-system contributed CConfigurationData
	 * 
	 * @throws WriteAccessException when the configuration description is read-only,
	 * see {@link CoreModel#getProjectDescription(org.eclipse.core.resources.IProject, boolean)}
	 * 
	 * @see CConfigurationDataProvider and the extension point org.eclipse.cdt.core.CConfigurationDataProvider
	 */
	void setConfigurationData(String buildSystemId, CConfigurationData data) throws WriteAccessException;
	
	/**
	 * returns whether or not the configuration description was modified
	 */
	boolean isModified();
	
	/**
	 * returns the target platform settings for this configuration
	 * @see ICTargetPlatformSetting
	 */
	ICTargetPlatformSetting getTargetPlatformSetting();
	
	/**
	 * returns the source entries for this configuration
	 * @see ICSourceEntry
	 */
	ICSourceEntry[] getSourceEntries();
	
	ICSourceEntry[] getResolvedSourceEntries();

	/**
	 * sets the source entries for this configuration
	 * 
	 * @param entries
	 * 
	 * @throws WriteAccessException when the configuration description is read-only,
	 * see {@link CoreModel#getProjectDescription(org.eclipse.core.resources.IProject, boolean)} 
	 */
	void setSourceEntries(ICSourceEntry[] entries) throws CoreException, WriteAccessException;

	/**
	 * returns the reference information for this configuration, i.e. the information on the projects/configurations
	 * this configuration references
	 * the map contains the project_name<->configuration_id associations
	 * if the current configuration does not reference any other configurations,
	 * empty map is returned
	 */
	Map<String, String> getReferenceInfo();

	/**
	 * sets the reference information for this configuration, i.e. the information on the projects/configurations
	 * this configuration references
	 * the map should contain the project_name<->configuration_id associations
	 * 
	 * @param refs
	 * 
	 * @throws WriteAccessException when the configuration description is read-only
	 * see {@link CoreModel#getProjectDescription(org.eclipse.core.resources.IProject, boolean)}
	 */
	void setReferenceInfo(Map<String, String> refs) throws WriteAccessException;
	
	/**
	 * returns an array of settings exported by this configuration
	 * in case some configurations refer (depend on) this configuration
	 * exported settings of this configuration get applied to those configurations
	 * @see ICExternalSetting
	 * @see #getReferenceInfo()
	 * @see #setReferenceInfo(Map)
	 */
	ICExternalSetting[] getExternalSettings();
	
	/**
	 * creates/adds external setting to this configuration 
	 * in case some configurations refer (depend on) this configuration
	 * exported settings of this configuration get applied to those configurations
	 * @see ICExternalSetting
	 * @see #getReferenceInfo()
	 * @see #setReferenceInfo(Map)
	 * @see #getExternalSettings() 
	 * 
	 * @param languageIDs
	 * @param contentTypeIds
	 * @param extensions
	 * @param entries
	 * @throws WriteAccessException when the configuration description is read-only
	 * see {@link CoreModel#getProjectDescription(org.eclipse.core.resources.IProject, boolean)}
	 */
	ICExternalSetting createExternalSetting(String languageIDs[],
			String contentTypeIds[],
			String extensions[],
			ICSettingEntry entries[]) throws WriteAccessException;

	/**
	 * removes external setting from this configuration 
	 * in case some configurations refer (depend on) this configuration
	 * exported settings of this configuration get applied to those configurations
	 * @see ICExternalSetting
	 * @see #getReferenceInfo()
	 * @see #setReferenceInfo(Map)
	 * @see #getExternalSettings() 
	 * 
	 * @param setting
	 * @throws WriteAccessException when the configuration description is read-only
	 * see {@link CoreModel#getProjectDescription(org.eclipse.core.resources.IProject, boolean)}
	 */
	void removeExternalSetting(ICExternalSetting setting) throws WriteAccessException;

	/**
	 * removes all external settings from this configuration 
	 * in case some configurations refer (depend on) this configuration
	 * exported settings of this configuration get applied to those configurations
	 * @see ICExternalSetting
	 * @see #getReferenceInfo()
	 * @see #setReferenceInfo(Map)
	 * @see #getExternalSettings() 
	 * 
	 * @throws WriteAccessException when the configuration description is read-only
	 * see {@link CoreModel#getProjectDescription(org.eclipse.core.resources.IProject, boolean)}
	 */
	void removeExternalSettings() throws WriteAccessException;
	
	/**
	 * returns the build setting for this configuration
	 * @see ICBuildSetting
	 */
	ICBuildSetting getBuildSetting();
	
	/**
	 * returns the CDT variable contributor that represent information on the
	 * CDT variables (Build Macros) contributed/used with this configuration 
	 * 
	 * @see ICdtVariablesContributor
	 */
	ICdtVariablesContributor getBuildVariablesContributor();
	
	/**
	 * the get/setSettionsProperty methods allow to associate the session properties mechanism on the configuration level
	 * session properties are not persisted and are not restored on the next eclipse session
	 * the scope of configuration session properties is the current configuration description,
	 * i.e. modifications to the properties are not applied until the setProjectDescription call
	 */
	Object getSessionProperty(QualifiedName name);

	/**
	 * the get/setSettionsProperty methods allow to associate the session properties mechanism on the configuration level
	 * session properties are not persisted and are not restored on the next eclipse session
	 * the scope of configuration session properties is the current configuration description,
	 * i.e. modifications to the properties are not applied until the setProjectDescription call
	 * 
	 * @param name
	 * @param value
	 */
	void setSessionProperty(QualifiedName name, Object value);
	
	/**
	 * sets the name for this configuration
	 * 
	 * @param name
	 * @throws WriteAccessException when the configuration description is read-only
	 * see {@link CoreModel#getProjectDescription(org.eclipse.core.resources.IProject, boolean)}
	 */
	void setName(String name) throws WriteAccessException;
	
	ICConfigExtensionReference[] get(String extensionPointID);
	
	ICConfigExtensionReference create(String extensionPoint, String extension) throws CoreException;
	
	void remove(ICConfigExtensionReference ext) throws CoreException;
	
	void remove(String extensionPoint) throws CoreException;
	
	boolean isPreferenceConfiguration();
	
	/**
	 * convenience method to return a language setting for the file
	 * with the specified project-relative path
	 * 
	 * @param path - file project relative path
	 * @return ICLanguageSetting or null if not found
	 */
	ICLanguageSetting getLanguageSettingForFile(IPath path, boolean ignoreExludeStatus);
	
	/**
	 * sets the external setting providers to be used for the configuration
	 * 
	 * @param ids the ids of externalSettinsProvider extensions
	 */
	void setExternalSettingsProviderIds(String ids[]);

	/**
	 * returns the ids of external setting providers used for the configuration
	 * 
	 * @return the ids of externalSettinsProvider extensions
	 */
	String[] getExternalSettingsProviderIds();
	
	/**
	 * tells the configuration to update the given providers
	 * In case the specified ids contain provider ids not associated with the configuration,
	 * those ids will be ignored and will NOT be added to the configuration settings
	 * 
	 * @param ids the ids of externalSettinsProvider extensions
	 * 
	 * @see ICProjectDescriptionManager#updateExternalSettingsProviders(String[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	void updateExternalSettingsProviders(String[] ids) throws WriteAccessException;
	
	CConfigurationStatus getConfigurationStatus();
}
