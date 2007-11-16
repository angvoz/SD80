/*******************************************************************************
 * Copyright (c) 2005, 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.extension.CFileData;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IFileInfo;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.dataprovider.BuildFileData;
import org.eclipse.cdt.managedbuilder.internal.dataprovider.BuildLanguageData;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PluginVersionIdentifier;

public class ResourceConfiguration extends ResourceInfo implements IFileInfo {

	private static final String EMPTY_STRING = new String();

	//property name for holding the rebuild state
	private static final String REBUILD_STATE = "rebuildState";  //$NON-NLS-1$

	//  Parent and children
	private List toolList;
	private Map toolMap;
	//  Managed Build model attributes
	private Integer rcbsApplicability;
	private String toolsToInvoke;
	//  Miscellaneous
	private boolean isExtensionResourceConfig = false;
	private boolean resolved = true;
	
	/*
	 *  C O N S T R U C T O R S
	 */
	
	/**
	 * This constructor is called to create a resource configuration defined by an 
	 * extension point in a plugin manifest file, or returned by a dynamic element provider
	 * 
	 * @param parent  The IConfiguration parent of this resource configuration
	 * @param element The resource configuration definition from the manifest file 
	 *                or a dynamic element provider
	 * @param managedBuildRevision
	 */
	public ResourceConfiguration(IConfiguration parent, IManagedConfigElement element, String managedBuildRevision) {
		super(parent, element, true);
		isExtensionResourceConfig = true;
		
		// setup for resolving
		resolved = false;

		setManagedBuildRevision(managedBuildRevision);
		loadFromManifest(element);
		
		// Hook me up to the Managed Build Manager
		ManagedBuildManager.addExtensionResourceConfiguration(this);

		// Load the tool children
		IManagedConfigElement[] tools = element.getChildren(ITool.TOOL_ELEMENT_NAME);
		for (int n = 0; n < tools.length; ++n) {
			Tool toolChild = new Tool(this, tools[n], getManagedBuildRevision());
			getToolList().add(toolChild);
			getToolMap().put(toolChild.getId(), toolChild);
		}
		
		setDirty(false);
	}

	/**
	 * Create a <code>ResourceConfiguration</code> based on the specification stored in the 
	 * project file (.cdtbuild).
	 * 
	 * @param parent The <code>IConfiguration</code> the resource configuration will be added to. 
	 * @param element The XML element that contains the resource configuration settings.
	 * @param managedBuildRevision
	 */
	public ResourceConfiguration(IConfiguration parent, ICStorageElement element, String managedBuildRevision) {
		super(parent, element, true);
		isExtensionResourceConfig = false;
		setResourceData(new BuildFileData(this));
		
		setManagedBuildRevision(managedBuildRevision);
		// Initialize from the XML attributes
		loadFromProject(element);

		// Load children
		ICStorageElement configElements[] = element.getChildren();
		for (int i = 0; i < configElements.length; ++i) {
			ICStorageElement configElement = configElements[i];
			if (configElement.getName().equals(ITool.TOOL_ELEMENT_NAME)) {
				Tool tool = new Tool((IBuildObject)this, configElement, getManagedBuildRevision());
				addTool(tool);
			}
		}
		
		String rebuild = PropertyManager.getInstance().getProperty(this, REBUILD_STATE);
		if(rebuild == null || Boolean.valueOf(rebuild).booleanValue())
			setRebuildState(true);
		setDirty(false);
	}
	
	public ResourceConfiguration(FolderInfo folderInfo, ITool baseTool, String id, String resourceName, IPath path){
		super(folderInfo, path, id, resourceName);
//		setParentFolder(folderInfo);
//		setParentFolderId(folderInfo.getId());
		
		isExtensionResourceConfig = folderInfo.isExtensionElement();
		if(!isExtensionResourceConfig)
			setResourceData(new BuildFileData(this));
	
		if ( folderInfo.getParent() != null)
			setManagedBuildRevision(folderInfo.getParent().getManagedBuildRevision());
		
		setDirty(false);
		toolsToInvoke = EMPTY_STRING;
		rcbsApplicability = new Integer(KIND_DISABLE_RCBS_TOOL);
		
		
		//	Get file extension.
		String extString = path.getFileExtension();
		if(baseTool != null){
			if(baseTool.getParentResourceInfo() != folderInfo)
				baseTool = null;
		}
		// Add the resource specific tools to this resource.
		ITool tools[] = folderInfo.getFilteredTools();
		String subId = new String();
		for (int i = 0; i < tools.length; i++) {
			if( tools[i].buildsFileType(extString) ) {
				baseTool = tools[i];
				break;
			}
		}
		
		if(baseTool != null){
			subId = ManagedBuildManager.calculateChildId(baseTool.getId(), null);				
			createTool(baseTool, subId, baseTool.getName(), false);
			setRebuildState(true);
		}
	}

	/**
	 * Create a new resource configuration based on one already defined.
	 * 
	 * @param managedProject The <code>ManagedProject</code> the configuration will be added to. 
	 * @param parentConfig The <code>IConfiguration</code> to copy the settings from.
	 * @param id A unique ID for the new configuration.
	 */
	public ResourceConfiguration(IConfiguration cfg, ResourceConfiguration cloneConfig, String id, Map superClassIdMap, boolean cloneChildren) {
		super(cfg, cloneConfig, id);
		
		isExtensionResourceConfig = cfg.isExtensionElement();
		if(!cloneConfig.isExtensionResourceConfig)
			cloneChildren = true;

		if(!isExtensionResourceConfig)
			setResourceData(new BuildFileData(this));

		setManagedBuildRevision(cloneConfig.getManagedBuildRevision());
		
		//  Copy the remaining attributes
		if (cloneConfig.toolsToInvoke != null) {
			toolsToInvoke = new String(cloneConfig.toolsToInvoke);
		}
		if (cloneConfig.rcbsApplicability != null) {
			rcbsApplicability = new Integer(cloneConfig.rcbsApplicability.intValue());
		}
		
		boolean copyIds = cloneChildren && id.equals(cloneConfig.id);
		// Clone the resource configuration's tool children
		if (cloneConfig.toolList != null) {
			Iterator iter = cloneConfig.getToolList().listIterator();
			while (iter.hasNext()) {
				Tool toolChild = (Tool) iter.next();
				String subId = null;
				String subName;
				
				Map curIdMap = (Map)superClassIdMap.get(cloneConfig.getPath());
				ITool extTool = ManagedBuildManager.getExtensionTool(toolChild);
				if(curIdMap != null){
					if(extTool != null){
						subId = (String)curIdMap.get(extTool.getId());
					}
				}
				
				subName = toolChild.getName();
				
				if(subId == null){
					if (extTool != null) {
						subId = copyIds ? toolChild.getId() : ManagedBuildManager.calculateChildId(
									extTool.getId(),
									null);
	//					subName = toolChild.getSuperClass().getName();
					} else {
						subId = copyIds ? toolChild.getId() : ManagedBuildManager.calculateChildId(
									toolChild.getId(),
									null);
	//					subName = toolChild.getName();
					}
				}

				//  The superclass for the cloned tool is not the same as the one from the tool being cloned.
				//  The superclasses reside in different configurations. 
				ITool toolSuperClass = null;
				String superId = null;
				//  Search for the tool in this configuration that has the same grand-superClass as the 
				//  tool being cloned
				ITool otherSuperTool = toolChild.getSuperClass();
				if(otherSuperTool != null){
					if(otherSuperTool.isExtensionElement()){
						toolSuperClass = otherSuperTool;
					} else {
						IResourceInfo otherRcInfo = otherSuperTool.getParentResourceInfo();
						IResourceInfo thisRcInfo = cfg.getResourceInfo(otherRcInfo.getPath(), true);
						ITool otherExtTool = ManagedBuildManager.getExtensionTool(otherSuperTool);
						if(otherExtTool != null){
							if(thisRcInfo != null){
								ITool tools[] = thisRcInfo.getTools();
								for(int i = 0; i < tools.length; i++){
									ITool thisExtTool = ManagedBuildManager.getExtensionTool(tools[i]);
									if(otherExtTool.equals(thisExtTool)){
										toolSuperClass = tools[i];
										superId = toolSuperClass.getId();
										break;
									}
								}
							} else {
								superId = copyIds ? otherSuperTool.getId() : ManagedBuildManager.calculateChildId(otherExtTool.getId(), null);
								Map idMap = (Map)superClassIdMap.get(otherRcInfo.getPath());
								if(idMap == null){
									idMap = new HashMap();
									superClassIdMap.put(otherRcInfo.getPath(), idMap);
								}
								idMap.put(otherExtTool.getId(), superId);
							}
						}
					}
				}
//				IToolChain tCh = cloneConfig.getBaseToolChain();
//				if(tCh != null){
//					if(!tCh.isExtensionElement()){
//						IFolderInfo fo = tCh.getParentFolderInfo();
//						IPath path = fo.getPath();
//						IResourceInfo baseFo = cfg.getResourceInfo(path, false);
//						if(baseFo instanceof IFileInfo)
//							baseFo = cfg.getResourceInfo(path.removeLastSegments(1), false);
//						tCh = ((IFolderInfo)baseFo).getToolChain();
//						
//					}
//					ITool[] tools = tCh.getTools();
//					for (int i=0; i<tools.length; i++) {
//					    ITool configTool = tools[i];
//					    if (toolChild.getSuperClass() != null 
//					    		&& configTool.getSuperClass() == toolChild.getSuperClass().getSuperClass())
//					    {
//					        toolSuperClass = configTool;
//					        break;
//					    }
//					}
//				} else {
//					//TODO:
//				}

				Tool newTool = null;
				if(toolSuperClass != null)
					newTool = new Tool(this, toolSuperClass, subId, subName, toolChild);
				else 
					newTool = new Tool(this, superId, subId, subName, toolChild);

				if(newTool != null)
					addTool(newTool);
			}
		}
		
		if(copyIds){
			isDirty = cloneConfig.isDirty;
			needsRebuild = cloneConfig.needsRebuild;
		} else {
			setDirty(true);
			setRebuildState(true);
		}
	}

	public ResourceConfiguration(ResourceConfiguration baseInfo, IPath path, String id, String name) {
		super(baseInfo, path, id, name);
		
		isExtensionResourceConfig = false;
		setResourceData(new BuildFileData(this));

		setManagedBuildRevision(baseInfo.getManagedBuildRevision());
		
		//  Copy the remaining attributes
		toolsToInvoke = baseInfo.toolsToInvoke;
		
		rcbsApplicability = new Integer(KIND_DISABLE_RCBS_TOOL);
				
		// Clone the resource configuration's tool children
		if (baseInfo.toolList != null) {
			Iterator iter = baseInfo.getToolList().listIterator();
			while (iter.hasNext()) {
				Tool toolChild = (Tool) iter.next();
				ITool superTool = toolChild.getSuperClass();
				String baseId = superTool != null ? superTool.getId() : toolChild.getId();
				String subId = ManagedBuildManager.calculateChildId(baseId, null);
				String subName = toolChild.getName();

				Tool newTool = new Tool(this, superTool, subId, subName, toolChild);
				addTool(newTool);
			}
		}

		setDirty(true);
		setRebuildState(true);
	}

	/*
	 *  E L E M E N T   A T T R I B U T E   R E A D E R S   A N D   W R I T E R S
	 */
	
	/* (non-Javadoc)
	 * Loads the resource configuration information from the ManagedConfigElement 
	 * specified in the argument.
	 * 
	 * @param element Contains the resource configuration information 
	 */
	protected void loadFromManifest(IManagedConfigElement element) {
		ManagedBuildManager.putConfigElement(this, element);

		// toolsToInvoke
		toolsToInvoke = element.getAttribute(IResourceConfiguration.TOOLS_TO_INVOKE);

		// rcbsApplicability
		String rcbsApplicabilityStr = element.getAttribute(IResourceConfiguration.RCBS_APPLICABILITY);
		if (rcbsApplicabilityStr == null || rcbsApplicabilityStr.equals(DISABLE_RCBS_TOOL)) {
			rcbsApplicability = new Integer(KIND_DISABLE_RCBS_TOOL);
		} else if (rcbsApplicabilityStr.equals(APPLY_RCBS_TOOL_BEFORE)) {
			rcbsApplicability = new Integer(KIND_APPLY_RCBS_TOOL_BEFORE);
		} else if (rcbsApplicabilityStr.equals(APPLY_RCBS_TOOL_AFTER)) {
			rcbsApplicability = new Integer(KIND_APPLY_RCBS_TOOL_AFTER);
		} else if (rcbsApplicabilityStr.equals(APPLY_RCBS_TOOL_AS_OVERRIDE)) {
			rcbsApplicability = new Integer(KIND_APPLY_RCBS_TOOL_AS_OVERRIDE);
		}
	}
	
	/* (non-Javadoc)
	 * Initialize the resource configuration information from the XML element 
	 * specified in the argument
	 * 
	 * @param element An XML element containing the resource configuration information 
	 */
	protected void loadFromProject(ICStorageElement element) {
		// toolsToInvoke
		if (element.getAttribute(IResourceConfiguration.TOOLS_TO_INVOKE) != null) {
			toolsToInvoke = element.getAttribute(IResourceConfiguration.TOOLS_TO_INVOKE);
		}

		// rcbsApplicability
		if (element.getAttribute(IResourceConfiguration.RCBS_APPLICABILITY) != null) {
			String rcbsApplicabilityStr = element.getAttribute(IResourceConfiguration.RCBS_APPLICABILITY);
			if (rcbsApplicabilityStr == null || rcbsApplicabilityStr.equals(DISABLE_RCBS_TOOL)) {
				rcbsApplicability = new Integer(KIND_DISABLE_RCBS_TOOL);
			} else if (rcbsApplicabilityStr.equals(APPLY_RCBS_TOOL_BEFORE)) {
				rcbsApplicability = new Integer(KIND_APPLY_RCBS_TOOL_BEFORE);
			} else if (rcbsApplicabilityStr.equals(APPLY_RCBS_TOOL_AFTER)) {
				rcbsApplicability = new Integer(KIND_APPLY_RCBS_TOOL_AFTER);
			} else if (rcbsApplicabilityStr.equals(APPLY_RCBS_TOOL_AS_OVERRIDE)) {
				rcbsApplicability = new Integer(KIND_APPLY_RCBS_TOOL_AS_OVERRIDE);
			}
		}
	}

	/**
	 * Persist the resource configuration to the project file.
	 * 
	 * @param doc
	 * @param element
	 */
	public void serialize(ICStorageElement element) {
		super.serialize(element);
		if (toolsToInvoke != null) {
			element.setAttribute(IResourceConfiguration.TOOLS_TO_INVOKE, toolsToInvoke);
		}

		if (rcbsApplicability != null) {
			String str;
			switch (getRcbsApplicability()) {
				case KIND_APPLY_RCBS_TOOL_BEFORE:
					str = APPLY_RCBS_TOOL_BEFORE;
					break;
				case KIND_APPLY_RCBS_TOOL_AFTER:
					str = APPLY_RCBS_TOOL_AFTER;
					break;
				case KIND_APPLY_RCBS_TOOL_AS_OVERRIDE:
					str = APPLY_RCBS_TOOL_AS_OVERRIDE;
					break;
				case KIND_DISABLE_RCBS_TOOL:
					str = DISABLE_RCBS_TOOL;
					break;
				default:
					str = DISABLE_RCBS_TOOL; 
					break;
			}
			element.setAttribute(IResourceConfiguration.RCBS_APPLICABILITY, str);
		}
		
		// Serialize my children
		List toolElements = getToolList();
		Iterator iter = toolElements.listIterator();
		while (iter.hasNext()) {
			Tool tool = (Tool) iter.next();
			ICStorageElement toolElement = element.createChild(ITool.TOOL_ELEMENT_NAME);
			tool.serialize(toolElement);
		}
		
		// I am clean now
		setDirty(false);
	}

	/*
	 *  P A R E N T   A N D   C H I L D   H A N D L I N G
	 */

	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IResourceConfiguration#getTools()
	 */
	public ITool[] getTools() {
		Tool[] tools = new Tool[getToolList().size()];
		Iterator iter = getToolList().listIterator();
		int i = 0;
		while (iter.hasNext()) {
			Tool tool = (Tool)iter.next();
			tools[i++] = tool; 
		}
		return tools;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IResourceConfiguration#getTool(java.lang.String)
	 */
	public ITool getTool(String id) {
		Tool tool = (Tool)getToolMap().get(id);
		return (ITool)tool;
	}
	
	/* (non-Javadoc)
	 * Safe accessor for the list of tools.
	 * 
	 * @return List containing the tools
	 */
	private List getToolList() {
		if (toolList == null) {
			toolList = new ArrayList();
		}
		return toolList;
	}
	
	/* (non-Javadoc)
	 * Safe accessor for the map of tool ids to tools
	 * 
	 * @return
	 */
	private Map getToolMap() {
		if (toolMap == null) {
			toolMap = new HashMap();
		}
		return toolMap;
	}

	/* (non-Javadoc)
	 * Adds the Tool to the Tool list and map
	 * 
	 * @param Tool
	 */
	public void addTool(Tool tool) {
		getToolList().add(tool);
		getToolMap().put(tool.getId(), tool);
		setRebuildState(true);
	}

	/* (non-Javadoc)
	 * Removes the Tool from the Tool list and map
	 * 
	 * @param Tool
	 */
	public void removeTool(ITool tool) {
		getToolList().remove(tool);
		getToolMap().remove(tool);
		setRebuildState(true);
	}

	/*
	 *  M O D E L   A T T R I B U T E   A C C E S S O R S
	 */

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IResourceConfiguration#getResourcePath()
	 */
	public String getResourcePath() {
		IPath path = getParent().getOwner().getProject().getFullPath();
		path = path.append(getPath());
		return path.toString();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IResourceConfiguration#getRcbsApplicability()
	 */
	public int getRcbsApplicability() {
		/*
		 * rcbsApplicability is an integer constant that represents how the user wants to
		 * order the application of a resource custom build step tool.
		 * Defaults to disable rcbs tool.
		 * Choices are before, after, or override other tools, or disable rcbs tool.
		 */
		if (rcbsApplicability == null) {
			return KIND_DISABLE_RCBS_TOOL;
		}
		return rcbsApplicability.intValue();
		}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IResourceConfiguration#getToolsToInvoke()
	 */
	public ITool[] getToolsToInvoke() {
		/*
		 * toolsToInvoke is an ordered list of tool ids for the currently defined tools in
		 * the resource configuration.
		 * Defaults to all tools in the order found.
		 * Modified by the presence of an rcbs tool and the currently assigned applicability of that tool.
		 * The attribute is implemented as a String of a semicolon separated list of tool ids.
		 * An empty string implies treat as if no resource configuration, i.e., use project level tool.
		 * This getter routine returns an ITool[] to consumers (i.e., the makefile generator).
		 */
		String t_ToolsToInvoke = EMPTY_STRING;
		ITool[] resConfigTools;
		ITool[] tools;
		String rcbsToolId = EMPTY_STRING;
		int len;
		int j;
		int rcbsToolIdx=-1;
		resConfigTools = getTools();

		/*
		 * Evaluate the tools currently defined in the resource configuration.
		 * Update the current state of the toolsToInvoke attribute.
		 * Build and return an ITool[] for consumers.
		 */
		
		/*
		 * If no tools are currently defined, return a zero lengh array of ITool.
		 */
		if (resConfigTools.length == 0) {
			toolsToInvoke = EMPTY_STRING;
			tools = new ITool[0];
			return tools;
		}
		
		/*
		 * See if there is an rcbs tool defined.  There should only be one at most.
		 */
		for ( int i = 0; i < resConfigTools.length; i++ ){
			if (resConfigTools[i].getCustomBuildStep() && !resConfigTools[i].isExtensionElement()) {
				rcbsToolId = resConfigTools[i].getId();
				rcbsToolIdx = i;
				break;
			}
		}
		if (!rcbsToolId.equals(EMPTY_STRING)){
			/*
			 * Here if an rcbs tool is defined.
			 * Apply the tools according to the current rcbsApplicability setting.
			 */
			switch(rcbsApplicability.intValue()){
			case KIND_APPLY_RCBS_TOOL_AS_OVERRIDE:
				toolsToInvoke = rcbsToolId;
				tools = new ITool[1];
				tools[0] = resConfigTools[rcbsToolIdx];
				break;
			case KIND_APPLY_RCBS_TOOL_AFTER:
				j = 0;
				tools = new ITool[resConfigTools.length];
				for ( int i = 0; i < resConfigTools.length; i++ ){
					if (resConfigTools[i].getId() != rcbsToolId) {
						t_ToolsToInvoke += resConfigTools[i].getId() + ";";	//$NON-NLS-1$
						tools[j++] = resConfigTools[i];
					}
				}
				t_ToolsToInvoke += rcbsToolId;
				tools[j++] = resConfigTools[rcbsToolIdx];
				toolsToInvoke = t_ToolsToInvoke;
				break;
			case KIND_APPLY_RCBS_TOOL_BEFORE:
				j = 0;
				tools = new ITool[resConfigTools.length];
				t_ToolsToInvoke = rcbsToolId + ";";	//$NON-NLS-1$
				tools[j++] = resConfigTools[rcbsToolIdx];
				for ( int i = 0; i < resConfigTools.length; i++ ){
					if (resConfigTools[i].getId() != rcbsToolId) {
						t_ToolsToInvoke += resConfigTools[i].getId() + ";";	//$NON-NLS-1$
						tools[j++] = resConfigTools[i];
					}
				}
				len = t_ToolsToInvoke.length();
				t_ToolsToInvoke = t_ToolsToInvoke.substring(0,len-1);
				toolsToInvoke = t_ToolsToInvoke;
				break;
			case KIND_DISABLE_RCBS_TOOL:
				/*
				 * If the rcbs tool is the only tool and the user has disabled it,
				 * there are no tools to invoke in the resource configuration.
				 */
				if(resConfigTools.length == 1){
					tools = new ITool[0];
					toolsToInvoke = EMPTY_STRING;
					break;
				}
				j = 0;
				tools = new ITool[resConfigTools.length-1];
				for ( int i = 0; i < resConfigTools.length; i++ ){
					if (resConfigTools[i].getId() != rcbsToolId) {
						t_ToolsToInvoke += resConfigTools[i].getId() + ";";	//$NON-NLS-1$
						tools[j++] = resConfigTools[i];
					}
				}
				len = t_ToolsToInvoke.length();
				t_ToolsToInvoke = t_ToolsToInvoke.substring(0,len-1);
				toolsToInvoke = t_ToolsToInvoke;
				break;
			default:
				/*
				 * If we get an unexpected value, apply all tools in the order found.
				 */
				tools = new ITool[resConfigTools.length];
				for ( int i = 0; i < resConfigTools.length; i++ ){
					t_ToolsToInvoke += resConfigTools[i].getId() + ";";	//$NON-NLS-1$
					tools[i] = resConfigTools[i];
				}
				len = t_ToolsToInvoke.length();
				t_ToolsToInvoke = t_ToolsToInvoke.substring(0,len-1);
				toolsToInvoke = t_ToolsToInvoke;
				break;
			}
		}
		else {
			/*
			 * Here if no rcbs tool is defined, but there are other tools in the resource configuration.
			 * Specify all tools in the order found.
			 */
			tools = new ITool[resConfigTools.length];
			for ( int i = 0; i < resConfigTools.length; i++ ){
				t_ToolsToInvoke += resConfigTools[i].getId() + ";";	//$NON-NLS-1$
				tools[i] = resConfigTools[i];
			}
			len = t_ToolsToInvoke.length();
			t_ToolsToInvoke = t_ToolsToInvoke.substring(0,len-1);
			toolsToInvoke = t_ToolsToInvoke;
		}
		return tools;
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IResourceConfiguration#getRcbsApplicability()
	 */
	public void setRcbsApplicability(int newValue) {
		/*
		 * rcbsApplicability is an integer constant that represents how the user wants to
		 * order the application of a resource custom build step tool.
		 * Defaults to override all other tools.
		 * Choices are before, after, or override other tools, or disable rcbs tool.
		 */
		if (rcbsApplicability == null || !(rcbsApplicability.intValue() == newValue)) {
			rcbsApplicability = new Integer(newValue);
			setDirty(true);
			setRebuildState(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IResourceConfiguration#setResourcePath()
	 */
	public void setResourcePath(String path) {
		if( path == null)
			return;
		IPath p = new Path(path).removeFirstSegments(1);
		setPath(p);
	}

	/*
	 *  O B J E C T   S T A T E   M A I N T E N A N C E
	 */
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IResourceConfiguration#isExtensionElement()
	 */
	public boolean isExtensionResourceConfiguration() {
		return isExtensionResourceConfig;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IResourceConfiguration#isDirty()
	 */
	public boolean isDirty() {
		// This shouldn't be called for an extension tool-chain
 		if (isExtensionResourceConfig) return false;
		
		// If I need saving, just say yes
		if (super.isDirty())
			return true;
		
		// Otherwise see if any tools need saving
		Iterator iter = getToolList().listIterator();
		while (iter.hasNext()) {
			Tool toolChild = (Tool) iter.next();
			if (toolChild.isDirty()) return true;
		}
		
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IResourceConfiguration#setDirty(boolean)
	 */
	public void setDirty(boolean isDirty) {
 		if (isExtensionResourceConfig) return;
 		
 		super.setDirty(isDirty);

 		// Propagate "false" to the children
		if (!isDirty) {
			Iterator iter = getToolList().listIterator();
			while (iter.hasNext()) {
				Tool toolChild = (Tool) iter.next();
				toolChild.setDirty(false);
			}		    
		}
	}
	
	/* (non-Javadoc)
	 *  Resolve the element IDs to interface references
	 */
	public void resolveReferences() {
		if (!resolved) {
			resolved = true;

			//  Call resolveReferences on our children
			Iterator iter = getToolList().listIterator();
			while (iter.hasNext()) {
				Tool toolChild = (Tool) iter.next();
				toolChild.resolveReferences();
			}
		}
	}
	
	public ITool createTool(ITool superClass, String id, String name, boolean isExtensionElement) {
		Tool tool = new Tool(this, superClass, id, name, isExtensionElement);
		addTool(tool);
		setDirty(true);
		return (ITool)tool;
	}
	
	public void reset() {
		// We just need to remove all Options
		ITool[] tools = getTools();
		// Send out the event to notify the options that they are about to be removed
//		ManagedBuildManager.performValueHandlerEvent(this, IManagedOptionValueHandler.EVENT_CLOSE);
		// Remove the configurations		
		for (int i = 0; i < tools.length; i++) {
			ITool tool = tools[i];
			IOption[] opts = tool.getOptions();
			for (int j = 0; j < opts.length; j++) {
				tool.removeOption(opts[j]);
			}
		}
//		setExclude(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IResourceConfiguration#setToolCommand(org.eclipse.cdt.managedbuilder.core.ITool, java.lang.String)
	 */
	public void setToolCommand(ITool tool, String command) {
		// TODO:  Do we need to verify that the tool is part of the configuration?
			tool.setToolCommand(command);
	}
	
	private IBuildObject getHoldersParent(IOption option) {
		IHoldsOptions holder = option.getOptionHolder();
		if (holder instanceof ITool) {
			return ((ITool)holder).getParent();
		} else if (holder instanceof IToolChain) {
			return ((IToolChain)holder).getParent();
		}
		return null;
	}
	
	public IResource getOwner() {
		return getParent().getOwner();
	}
	
	/**
	 * @return Returns the version.
	 */
	public PluginVersionIdentifier getVersion() {
		if ( version == null) {
			if ( getParent() != null) {
				return getParent().getVersion();
			}
		}
		return version;
	}
	
	public void setVersion(PluginVersionIdentifier version) {
		// Do nothing
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.core.BuildObject#updateManagedBuildRevision(java.lang.String)
	 */
	public void updateManagedBuildRevision(String revision){
		super.updateManagedBuildRevision(revision);
		
		for(Iterator iter = getToolList().iterator(); iter.hasNext();){
			((Tool)iter.next()).updateManagedBuildRevision(revision);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IResourceConfiguration#needsRebuild()
	 */
	public boolean needsRebuild() {
		if(super.needsRebuild())
			return true;
		
		ITool tools[] = getToolsToInvoke();
		for(int i = 0; i < tools.length; i++){
			if(tools[i].needsRebuild())
				return true;
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IResourceConfiguration#setRebuildState(boolean)
	 */
	public void setRebuildState(boolean rebuild) {
		if(isExtensionResourceConfiguration() && rebuild)
			return;
		
		if(needsRebuild() != rebuild){
			super.setRebuildState(rebuild);
			saveRebuildState();
		}
		
		if(!rebuild){
			ITool tools[] = getToolsToInvoke();
			for(int i = 0; i < tools.length; i++){
				tools[i].setRebuildState(false);
			}
		}

	}
	
	private void saveRebuildState(){
		PropertyManager.getInstance().setProperty(this, REBUILD_STATE, Boolean.toString(needsRebuild()));
	}

	public final int getKind() {
		return ICSettingBase.SETTING_FILE;
	}
	
	public CFileData getFileData(){
		return (CFileData)getResourceData();
	}
	
	public CLanguageData[] getCLanguageDatas() {
		ITool tools[] = getTools/*ToInvoke*/();
		List list = new ArrayList();
		for(int i = 0; i < tools.length; i++){
			CLanguageData datas[] = tools[i].getCLanguageDatas();
			for(int j = 0; j < datas.length; j++){
				list.add(datas[j]);
			}
		}
		return (BuildLanguageData[])list.toArray(new BuildLanguageData[list.size()]);
	}

	public IToolChain getBaseToolChain() {
		ITool tools[] = getToolsToInvoke();
		ITool baseTool = null;
		for(int i = 0; i < tools.length; i++){
			ITool tool = tools[i];
			ITool superTool = tool.getSuperClass(); 
			if(superTool != null){
				baseTool = superTool;
				if(!superTool.isExtensionElement()){
					break;
				}
			}
		}
		
		IToolChain baseTc = null;
		if(baseTool != null){
			IBuildObject parent = baseTool.getParent();
			if(parent instanceof IToolChain){
				baseTc = (IToolChain)parent;
			} else if(parent instanceof ResourceConfiguration){
				baseTc = ((ResourceConfiguration)parent).getBaseToolChain();
			}
		}
		
		return baseTc;
	}

	public boolean isExtensionElement() {
		return isExtensionResourceConfig;
	}
	
	public boolean supportsBuild(boolean managed) {
		ITool tools[] = getToolsToInvoke();
		for(int i = 0; i < tools.length; i++){
			if(!tools[i].supportsBuild(managed))
				return false;
		}
		
		return true;
	}

	public Set contributeErrorParsers(Set set) {
		return contributeErrorParsers(getToolsToInvoke(), set);
	}

	public void resetErrorParsers() {
		resetErrorParsers(getToolsToInvoke());
	}

	void removeErrorParsers(Set set) {
		removeErrorParsers(getToolsToInvoke(), set);
	}

	void resolveProjectReferences(boolean onLoad){
		for(Iterator iter = getToolList().iterator(); iter.hasNext();){
			Tool tool = (Tool)iter.next();
			tool.resolveProjectReferences(onLoad);
		}
	}

	public boolean hasCustomSettings() {
		IResourceInfo parentRc = getParentResourceInfo();
		if(parentRc instanceof FolderInfo){
			IPath path = getPath();
			String ext = path.getFileExtension();
			if(ext == null)
				ext = ""; //$NON-NLS-1$
			ITool otherTool = ((FolderInfo)parentRc).getToolFromInputExtension(ext);
			if(otherTool == null)
				return true;
			
			ITool[] tti = getToolsToInvoke();
			if(tti.length != 1)
				return true;
			
			return ((Tool)tti[0]).hasCustomSettings((Tool)otherTool);
		}
		ITool[] tools = getTools();
		ITool[] otherTools = ((IFileInfo)parentRc).getTools();
		if(tools.length != otherTools.length)
			return true;
		
		for(int i = 0; i < tools.length; i++){
			Tool tool = (Tool)tools[i];
			Tool otherTool = (Tool)otherTools[i];
			if(tool.hasCustomSettings(otherTool))
				return true;
		}
		
		return false;
	}
	
	public void setTools(ITool[] tools){
		ToolListModificationInfo info = getToolListModificationInfo(tools);
		info.apply();
	}

	public boolean isFolderInfo() {
		return false;
	}

	void applyToolsInternal(ITool[] resultingTools,
			ToolListModificationInfo info) {
		List list = getToolList();
		Map map = getToolMap();
		
		list.clear();
		map.clear();
		
		list.addAll(Arrays.asList(resultingTools));
		for(int i = 0; i < resultingTools.length; i++){
			ITool tool = resultingTools[i];
			map.put(tool.getId(), tool);
		}
		
		setRebuildState(true);
	}
	
	public boolean isSupported(){
		IFolderInfo foInfo = getParentFolderInfo();
		if(foInfo == null){
			IConfiguration cfg = getParent();
			if(cfg != null) {
				foInfo = cfg.getRootFolderInfo();
			}
		}
		
		if(foInfo != null)
			return foInfo.isSupported();
		return false;
	}
}
