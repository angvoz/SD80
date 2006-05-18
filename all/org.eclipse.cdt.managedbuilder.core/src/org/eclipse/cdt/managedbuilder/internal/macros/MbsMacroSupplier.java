/*******************************************************************************
 * Copyright (c) 2005, 2006 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.macros;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOutputType;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacro;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroSupplier;
import org.eclipse.cdt.managedbuilder.macros.IFileContextBuildMacroValues;
import org.eclipse.cdt.managedbuilder.macros.IFileContextData;
import org.eclipse.cdt.managedbuilder.macros.IOptionContextData;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.osgi.framework.Bundle;

/**
 * This supplier is used to suply MBS-predefined macros
 * 
 * @since 3.0
 */
public class MbsMacroSupplier implements IBuildMacroSupplier {
	private static MbsMacroSupplier fInstance;
	public final static String DOT = ".";	//$NON-NLS-1$
	public final static String EMPTY_STRING = ""; //$NON-NLS-1$

	
	private static final String fFileMacros[] = new String[]{
		"InputFileName",	//$NON-NLS-1$
		"InputFileExt",	//$NON-NLS-1$
		"InputFileBaseName",	//$NON-NLS-1$
		"InputFileRelPath",	//$NON-NLS-1$
		"InputDirRelPath",	//$NON-NLS-1$
		"OutputFileName",	//$NON-NLS-1$
		"OutputFileExt",	//$NON-NLS-1$
		"OutputFileBaseName",	//$NON-NLS-1$
		"OutputFileRelPath",	//$NON-NLS-1$
		"OutputDirRelPath",	//$NON-NLS-1$
	};

	private static final String fOptionMacros[] = new String[]{
		"IncludeDefaults",	//$NON-NLS-1$
		"ParentVersion",	//$NON-NLS-1$
	};
	
	private static final String fToolMacros[] = new String[]{
		"ToolVersion",	//$NON-NLS-1$
	};

	private static final String fConfigurationMacros[] = new String[]{
		"ConfigName",	//$NON-NLS-1$
		"ConfigDescription",	//$NON-NLS-1$
		"BuildArtifactFileName",	//$NON-NLS-1$
		"BuildArtifactFileExt",	//$NON-NLS-1$
		"BuildArtifactFileBaseName",	//$NON-NLS-1$
		"BuildArtifactFilePrefix",	//$NON-NLS-1$
		"TargetOsList",	//$NON-NLS-1$
		"TargetArchList",	//$NON-NLS-1$
		"ToolChainVersion",	//$NON-NLS-1$
		"BuilderVersion",	//$NON-NLS-1$
	};

	private static final String fProjectMacros[] = new String[]{
		"ProjName",	//$NON-NLS-1$
		"ProjDirPath",	//$NON-NLS-1$
	};

	private static final String fWorkspaceMacros[] = new String[]{
		"WorkspaceDirPath",	//$NON-NLS-1$
		"DirectoryDelimiter",	//$NON-NLS-1$
		"PathDelimiter",	//$NON-NLS-1$
	};

	private static final String fCDTEclipseMacros[] = new String[]{
		"EclipseVersion",	//$NON-NLS-1$
		"CDTVersion",	//$NON-NLS-1$
		"MBSVersion",	//$NON-NLS-1$
		"HostOsName",	//$NON-NLS-1$
		"HostArchName",	//$NON-NLS-1$
		"OsType",	//$NON-NLS-1$
		"ArchType",	//$NON-NLS-1$
	};
	
	private class OptionData extends OptionContextData {
		private IBuildObject fOptionContainer;
		public OptionData(IOption option, IBuildObject parent) {
			this(option, parent, parent);
		}

		public OptionData(IOption option, IBuildObject parent,  IBuildObject optionContainer) {
			super(option, parent);
			fOptionContainer = optionContainer;
		}

		public IBuildObject getOptionContainer(){
			return fOptionContainer;
		}
	}

	public class FileContextMacro extends BuildMacro{
		private IFileContextData fContextData;
		private IConfiguration fConfiguration;
		private boolean fIsExplicit = true;
		private boolean fIsInitialized;
		private String fExplicitValue;
		private boolean fIsExplicitResolved;
		private FileContextMacro(String name, IFileContextData contextData){
			fName = name;
			fType = VALUE_TEXT;
			fContextData = contextData;
		}
		
		private void loadValue(){
			if(fIsInitialized)
				return;
			IBuilder builder = null;
			IOptionContextData optionContext = fContextData.getOptionContextData();
			if(optionContext != null){
				IBuildObject buildObject = optionContext.getParent();
				if(buildObject instanceof ITool){
					buildObject = ((ITool)buildObject).getParent();
				} else if(buildObject instanceof IConfiguration){
					buildObject = ((IConfiguration)buildObject).getToolChain();
				}
				if(buildObject instanceof IToolChain){
					IToolChain toolChain = (IToolChain)buildObject;
					builder = toolChain.getBuilder();
					fConfiguration = toolChain.getParent();
				} else if (buildObject instanceof IResourceConfiguration){
					fConfiguration = ((IResourceConfiguration)buildObject).getParent();
					if(fConfiguration != null){
						IToolChain toolChain = fConfiguration.getToolChain();
						if(toolChain != null)
							builder = toolChain.getBuilder();
					}
				}
			}
			
			if(builder != null){
				IFileContextBuildMacroValues values = builder.getFileContextBuildMacroValues();
				String value = values.getMacroValue(fName);
				if(value != null){
					fStringValue = value;
					fIsExplicit = false;
				}
			}
			
			if(fStringValue == null){
				fIsExplicit = true;
				fStringValue = getExplicitFileMacroValue(fName, fContextData.getInputFileLocation(), fContextData.getOutputFileLocation(), fConfiguration);
				fExplicitValue = fStringValue;
				fIsExplicitResolved = true;
			}
			
			fIsInitialized = true;
		}
		
		public String getExplicitMacroValue(){
			loadValue();
			if(!fIsExplicitResolved){
				fExplicitValue = getExplicitFileMacroValue(fName, fContextData.getInputFileLocation(), fContextData.getOutputFileLocation(), fConfiguration);
				fIsExplicitResolved = true;
			}
			return fExplicitValue;
		}
		
		public boolean isExplicit(){
			loadValue();
			return fIsExplicit;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacro#getStringValue()
		 */
		public String getStringValue(){
			loadValue();
			return fStringValue;
		}
	}
	
	private String getExplicitFileMacroValue(String name, IPath inputFileLocation, IPath outputFileLocation, IConfiguration cfg){
		String value = null;
		if("InputFileName".equals(name)){	//$NON-NLS-1$
			if(inputFileLocation != null && inputFileLocation.segmentCount() > 0)
				value = inputFileLocation.lastSegment();
		}else if("InputFileExt".equals(name)){	//$NON-NLS-1$
			if(inputFileLocation != null && inputFileLocation.segmentCount() > 0)
				value = getExtension(inputFileLocation.lastSegment());
		}else if("InputFileBaseName".equals(name)){	//$NON-NLS-1$
			if(inputFileLocation != null && inputFileLocation.segmentCount() > 0)
				value = getBaseName(inputFileLocation.lastSegment());
		}else if("InputFileRelPath".equals(name)){	//$NON-NLS-1$
			if(inputFileLocation != null && inputFileLocation.segmentCount() > 0){
				IPath workingDirectory = getBuilderCWD(cfg);
				if(workingDirectory != null){
					IPath filePath = ManagedBuildManager.calculateRelativePath(workingDirectory, inputFileLocation);
					if(filePath != null)
						value = filePath.toOSString();
				}
			}
		}
		else if("InputDirRelPath".equals(name)){	//$NON-NLS-1$
			if(inputFileLocation != null && inputFileLocation.segmentCount() > 0){
				IPath workingDirectory = getBuilderCWD(cfg);
				if(workingDirectory != null){
					IPath filePath = ManagedBuildManager.calculateRelativePath(workingDirectory, inputFileLocation.removeLastSegments(1).addTrailingSeparator());
					if(filePath != null)
						value = filePath.toOSString();
				}
			}
		}
		else if("OutputFileName".equals(name)){	//$NON-NLS-1$
			if(outputFileLocation != null && outputFileLocation.segmentCount() > 0)
				value = outputFileLocation.lastSegment();
		}else if("OutputFileExt".equals(name)){	//$NON-NLS-1$
			if(outputFileLocation != null && outputFileLocation.segmentCount() > 0)
				value = getExtension(outputFileLocation.lastSegment());
		}else if("OutputFileBaseName".equals(name)){	//$NON-NLS-1$
			if(outputFileLocation != null && outputFileLocation.segmentCount() > 0)
				value = getBaseName(outputFileLocation.lastSegment());
		}else if("OutputFileRelPath".equals(name)){	//$NON-NLS-1$
			if(outputFileLocation != null && outputFileLocation.segmentCount() > 0){
				IPath workingDirectory = getBuilderCWD(cfg);
				if(workingDirectory != null){
					IPath filePath = ManagedBuildManager.calculateRelativePath(workingDirectory, outputFileLocation);
					if(filePath != null)
						value = filePath.toOSString();
				}
			}
		}else if("OutputDirRelPath".equals(name)){	//$NON-NLS-1$
			if(outputFileLocation != null && outputFileLocation.segmentCount() > 0){
				IPath workingDirectory = getBuilderCWD(cfg);
				if(workingDirectory != null){
					IPath filePath = ManagedBuildManager.calculateRelativePath(workingDirectory, outputFileLocation.removeLastSegments(1).addTrailingSeparator());
					if(filePath != null)
						value = filePath.toOSString();
				}
			}
		}
		
		return value;
	}

	public String[] getMacroNames(int contextType){
		return getMacroNames(contextType,true);
	}

	private String[] getMacroNames(int contextType, boolean clone){
		String names[] = null;
		switch(contextType){
		case IBuildMacroProvider.CONTEXT_FILE:
			names = fFileMacros; 
			break;
		case IBuildMacroProvider.CONTEXT_OPTION:
			names = fOptionMacros; 
			break;
		case IBuildMacroProvider.CONTEXT_TOOL:
			names = fToolMacros; 
			break;
		case IBuildMacroProvider.CONTEXT_CONFIGURATION:
			names = fConfigurationMacros; 
			break;
		case IBuildMacroProvider.CONTEXT_PROJECT:
			names = fProjectMacros; 
			break;
		case IBuildMacroProvider.CONTEXT_WORKSPACE:
			names = fWorkspaceMacros; 
			break;
		case IBuildMacroProvider.CONTEXT_INSTALLATIONS:
			names = fCDTEclipseMacros; 
			break;
		case IBuildMacroProvider.CONTEXT_ECLIPSEENV:
			break;
		}
		if(names != null)
			return clone ? (String[])names.clone() : names;
		return null;
	}

	private MbsMacroSupplier(){
		
	}

	public static MbsMacroSupplier getInstance(){
		if(fInstance == null)
			fInstance = new MbsMacroSupplier();
		return fInstance;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroSupplier#getMacro(java.lang.String, int, java.lang.Object)
	 */
	public IBuildMacro getMacro(String macroName, int contextType,
			Object contextData) {
		IBuildMacro macro = null; 
		switch(contextType){
		case IBuildMacroProvider.CONTEXT_FILE:
			if(contextData instanceof IFileContextData){
				for(int i = 0; i < fFileMacros.length; i++){
					if(macroName.equals(fFileMacros[i])){
						macro = new FileContextMacro(macroName,(IFileContextData)contextData);
						break;
					}
				}
			}
			break;
		case IBuildMacroProvider.CONTEXT_OPTION:
			if(contextData instanceof IOptionContextData){
				macro = getMacro(macroName, (IOptionContextData)contextData);
			}
			break;
		case IBuildMacroProvider.CONTEXT_TOOL:
			if(contextData instanceof ITool){
				macro = getMacro(macroName, (ITool)contextData);
			}
			break;
		case IBuildMacroProvider.CONTEXT_CONFIGURATION:
			if(contextData instanceof IConfiguration){
				macro = getMacro(macroName, (IConfiguration)contextData);
			}
			break;
		case IBuildMacroProvider.CONTEXT_PROJECT:
			if(contextData instanceof IManagedProject){
				macro = getMacro(macroName, (IManagedProject)contextData);
			}
			break;
		case IBuildMacroProvider.CONTEXT_WORKSPACE:
			if(contextData instanceof IWorkspace){
				macro = getMacro(macroName, (IWorkspace)contextData);
			}
			break;
		case IBuildMacroProvider.CONTEXT_INSTALLATIONS:
			if(contextData == null){
				macro = getMacro(macroName);
			}
			break;
		case IBuildMacroProvider.CONTEXT_ECLIPSEENV:
			break;
		}
		
		return macro;
	}

	public IBuildMacro getMacro(String macroName, IOptionContextData optionContext){
		IBuildMacro macro = null;
		if("IncludeDefaults".equals(macroName)){	//$NON-NLS-1$
			if(!canHandle(optionContext))
				optionContext = null;
			macro = new OptionMacro(macroName,optionContext);
		} else if("ParentVersion".equals(macroName)){
			IHoldsOptions holder = OptionContextData.getHolder(optionContext);
			if(holder != null && holder.getVersion() != null)
				macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,holder.getVersion().toString());
		}
		return macro;
	}
	
	public IBuildMacro getMacro(String macroName, ITool tool){
		IBuildMacro macro = null;
		if("ToolVersion".equals(macroName) && tool.getVersion() != null){	//$NON-NLS-1$
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,tool.getVersion().toString());
		}
		return macro;
	}

	public IBuildMacro getMacro(String macroName, IConfiguration cfg){
		IBuildMacro macro = null;
		if("ConfigName".equals(macroName)){	//$NON-NLS-1$
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,cfg.getName());
		}
		else if("ConfigDescription".equals(macroName)){	//$NON-NLS-1$
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,cfg.getDescription());
		}
		else if("BuildArtifactFileName".equals(macroName)){	//$NON-NLS-1$
			String name = cfg.getArtifactName();
			String ext = cfg.getArtifactExtension();
			if(ext != null && !EMPTY_STRING.equals(ext))
				name = name + DOT + ext;
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,name); 
		}
		else if("BuildArtifactFileExt".equals(macroName)){	//$NON-NLS-1$
			String ext = cfg.getArtifactExtension();
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,ext); 
		}
		else if("BuildArtifactFileBaseName".equals(macroName)){	//$NON-NLS-1$
			String name = cfg.getArtifactName();
			ITool targetTool = cfg.calculateTargetTool();
			if(targetTool != null){
				IOutputType pot = targetTool.getPrimaryOutputType();
				String prefix = pot.getOutputPrefix();
				

				// Resolve any macros in the outputPrefix
				// Note that we cannot use file macros because if we do a clean
				// we need to know the actual
				// name of the file to clean, and cannot use any builder
				// variables such as $@. Hence
				// we use the next best thing, i.e. configuration context.

				// figure out the configuration we're using
				IBuildObject toolParent = targetTool.getParent();
				IConfiguration config = null;
				// if the parent is a config then we're done
				if (toolParent instanceof IConfiguration)
					config = (IConfiguration) toolParent;
				else if (toolParent instanceof IToolChain) {
					// must be a toolchain
					config = (IConfiguration) ((IToolChain) toolParent)
							.getParent();
				}

				else if (toolParent instanceof IResourceConfiguration) {
					config = (IConfiguration) ((IResourceConfiguration) toolParent)
							.getParent();
				}

				else {
					// bad
					throw new AssertionError(
							"tool parent must be one of configuration, toolchain, or resource configuration");
				}

				if (config != null) {

					try {
						prefix = ManagedBuildManager
								.getBuildMacroProvider()
								.resolveValueToMakefileFormat(
										prefix,
										"", //$NON-NLS-1$
										" ", //$NON-NLS-1$
										IBuildMacroProvider.CONTEXT_CONFIGURATION,
										config);
					}

					catch (BuildMacroException e) {
					}

				}

				
				if(prefix != null && !EMPTY_STRING.equals(prefix))
					name = prefix + name;
			}
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,name); 
		}
		else if("BuildArtifactFilePrefix".equals(macroName)){	//$NON-NLS-1$
			ITool targetTool = cfg.calculateTargetTool();
			if(targetTool != null){
				IOutputType pot = targetTool.getPrimaryOutputType();
				String prefix = pot.getOutputPrefix();
				
				// Resolve any macros in the outputPrefix
				// Note that we cannot use file macros because if we do a clean
				// we need to know the actual
				// name of the file to clean, and cannot use any builder
				// variables such as $@. Hence
				// we use the next best thing, i.e. configuration context.

				// figure out the configuration we're using
				IBuildObject toolParent = targetTool.getParent();
				IConfiguration config = null;
				// if the parent is a config then we're done
				if (toolParent instanceof IConfiguration)
					config = (IConfiguration) toolParent;
				else if (toolParent instanceof IToolChain) {
					// must be a toolchain
					config = (IConfiguration) ((IToolChain) toolParent)
							.getParent();
				}

				else if (toolParent instanceof IResourceConfiguration) {
					config = (IConfiguration) ((IResourceConfiguration) toolParent)
							.getParent();
				}

				else {
					// bad
					throw new AssertionError(
							"tool parent must be one of configuration, toolchain, or resource configuration");
				}

				if (config != null) {

					try {
						prefix = ManagedBuildManager
								.getBuildMacroProvider()
								.resolveValueToMakefileFormat(
										prefix,
										"", //$NON-NLS-1$
										" ", //$NON-NLS-1$
										IBuildMacroProvider.CONTEXT_CONFIGURATION,
										config);
					}

					catch (BuildMacroException e) {
					}

				}
				
				if(prefix == null)
					prefix = EMPTY_STRING;
				macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,prefix);
			}
		}
		else if("TargetOsList".equals(macroName)){	//$NON-NLS-1$
			IToolChain toolChain = cfg.getToolChain();
			String osList[] = toolChain.getOSList();
			if(osList == null)
				osList = new String[0];
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT_LIST,osList);
		}
		else if("TargetArchList".equals(macroName)){	//$NON-NLS-1$
			IToolChain toolChain = cfg.getToolChain();
			String archList[] = toolChain.getArchList();
			if(archList == null)
				archList = new String[0];
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT_LIST,archList);
			
		}
		else if("ToolChainVersion".equals(macroName)){	//$NON-NLS-1$
			if(cfg.getToolChain().getVersion() != null)
				macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,cfg.getToolChain().getVersion().toString());
		}
		else if("BuilderVersion".equals(macroName)){	//$NON-NLS-1$
			PluginVersionIdentifier version = cfg.getToolChain().getBuilder().getVersion(); 
			if(version != null)
				macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,version.toString());
		}
		return macro;
	}
	
	private String getBaseName(String name){
		String value = null;
		int index = name.lastIndexOf('.');
		if(index == -1)
			value = name;
		else
			value = name.substring(0,index);
		return value;
	}
	
	private String getExtension(String name){
		String value = null;
		int index = name.lastIndexOf('.');
		if(index != -1)
			value = name.substring(index+1);
		return value;
	}
	
	public IBuildMacro getMacro(String macroName, IManagedProject mngProj){
		IBuildMacro macro = null;
		if("ProjName".equals(macroName)){	//$NON-NLS-1$
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,mngProj.getOwner().getName());
		}
		else if("ProjDirPath".equals(macroName)){	//$NON-NLS-1$
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_PATH_DIR,mngProj.getOwner().getLocation().toOSString());
		}
		return macro;
	}
	
	public IBuildMacro getMacro(String macroName, IWorkspace wsp){
		IBuildMacro macro = null;
		if("WorkspaceDirPath".equals(macroName)){	//$NON-NLS-1$
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_PATH_DIR,wsp.getRoot().getLocation().toOSString());
		} else if("DirectoryDelimiter".equals(macroName)){	//$NON-NLS-1$
			if(isWin32()){
				macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,"\\");	//$NON-NLS-1$
			} else {
				macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,"/");	//$NON-NLS-1$
			}
		} else if("PathDelimiter".equals(macroName)){	//$NON-NLS-1$
			if(isWin32()){
				macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,";");	//$NON-NLS-1$
			} else {
				macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,":");	//$NON-NLS-1$
			}
		}
		return macro;
	}
	
	private boolean isWin32(){
		String os = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
		if (os.startsWith("windows ")) //$NON-NLS-1$
			return true;
		return false;
	}

	public IBuildMacro getMacro(String macroName){
		IBuildMacro macro = null;
		if("EclipseVersion".equals(macroName)){	//$NON-NLS-1$
			Bundle bundle = Platform.getBundle("org.eclipse.platform");	//$NON-NLS-1$
			String version = bundle != null ? 
					(String)bundle.getHeaders().get(org.osgi.framework.Constants.BUNDLE_VERSION) :
						null;
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,version);
		}
		else if("CDTVersion".equals(macroName)){	//$NON-NLS-1$
			String version = (String)CCorePlugin.getDefault().getBundle().getHeaders().get(org.osgi.framework.Constants.BUNDLE_VERSION);
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,version);
		}
		else if("MBSVersion".equals(macroName)){	//$NON-NLS-1$
			String version = ManagedBuildManager.getBuildInfoVersion().toString();
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,version);
		}
		else if("HostOsName".equals(macroName)){	//$NON-NLS-1$
			String os = System.getProperty("os.name"); //$NON-NLS-1$
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,os);
		}
		else if("HostArchName".equals(macroName)){	//$NON-NLS-1$
			String arch = System.getProperty("os.arch"); //$NON-NLS-1$
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,arch);
		}
		else if("OsType".equals(macroName)){	//$NON-NLS-1$
			String os = Platform.getOS();
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,os);
		}
		else if("ArchType".equals(macroName)){	//$NON-NLS-1$
			String arch = Platform.getOSArch();
			macro = new BuildMacro(macroName,IBuildMacro.VALUE_TEXT,arch);
		}

		return macro;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroSupplier#getMacros(int, java.lang.Object)
	 */
	public IBuildMacro[] getMacros(int contextType, Object contextData) {
		String names[] = getMacroNames(contextType,false);
		
		if(names != null){
			IBuildMacro macros[] = new IBuildMacro[names.length];
			int num = 0;
			for(int i = 0; i < names.length; i++){
				IBuildMacro macro = getMacro(names[i],contextType,contextData);
				if(macro != null)
					macros[num++] = macro;
			}
			if(macros.length != num){
				IBuildMacro tmp[] = new IBuildMacro[num];
				if(num > 0)
					System.arraycopy(macros,0,tmp,0,num);
				macros = tmp;
			}
			return macros;
		}
		return null;
	}
	
	private IPath getBuilderCWD(IConfiguration cfg){
		IPath workingDirectory = null;
		IResource owner = cfg.getOwner();
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(owner);
			
		if(info != null){
			if(info.getDefaultConfiguration().equals(cfg)){
				IManagedBuilderMakefileGenerator generator = ManagedBuildManager.getBuildfileGenerator(info.getDefaultConfiguration());
				generator.initialize((IProject)owner,info,null);
				
				IPath topBuildDir = generator.getBuildWorkingDir();
				if(topBuildDir == null)
					topBuildDir = new Path(info.getConfigurationName());

				IPath projectLocation = owner.getLocation();
				workingDirectory = projectLocation.append(topBuildDir);
			}
		}
		return workingDirectory;
	}
	
	private IPath getOutputFilePath(IPath inputPath, IConfiguration cfg){
		ITool buildTools[] = null; 
		IResourceConfiguration rcCfg = cfg.getResourceConfiguration(inputPath.toString());
		if(rcCfg != null) {
			buildTools = rcCfg.getToolsToInvoke();
		}
		if (buildTools == null || buildTools.length == 0) {
			buildTools = cfg.getFilteredTools();
		}
		
		String name = null;
		IPath path = null;
		for(int i = 0; i < buildTools.length; i++){
			ITool tool = buildTools[i];
			IInputType inputType = tool.getInputType(inputPath.getFileExtension());
			if(inputType != null){
				IOutputType prymOutType = tool.getPrimaryOutputType();
				String names[] = prymOutType.getOutputNames();
				if(names != null && names.length > 0)
					name = names[0];
			}
		}
		if(name != null){
			IPath namePath = new Path(name);
			if(namePath.isAbsolute()){
				path = namePath;
			}
			else{
				IPath cwd = getBuilderCWD(cfg);
				if(cwd != null)
					path = cwd.append(namePath);
			}
		}
		return path;
		
	}
	
	/* (non-Javadoc)
	 * Returns the option that matches the option ID in this tool
	 */
	public IOption getOption(ITool tool, String optionId) {
		if (optionId == null) return null;
		
		//  Look for an option with this ID, or an option with a superclass with this id
		IOption[] options = tool.getOptions();
		for (int i = 0; i < options.length; i++) {
			IOption targetOption = options[i];
			IOption option = targetOption;
			do {
				if (optionId.equals(option.getId())) {
					return targetOption;
				}		
				option = option.getSuperClass();
			} while (option != null);
		}
		
		return null;
	}
	
	private class IncludeDefaultsSubstitutor implements IMacroSubstitutor {
		private IOptionContextData fOptionContextData;

		public IncludeDefaultsSubstitutor(IOptionContextData data){
			fOptionContextData = data;
		}

		public String resolveToString(String macroName) throws BuildMacroException {
			if(!"IncludeDefaults".equals(macroName)) 	//$NON-NLS-1$
				return MacroResolver.createMacroReference(macroName);
			IOptionContextData parent = getParent(fOptionContextData);
			if(parent == null)
				return EMPTY_STRING;
			IncludeDefaultsSubstitutor sub = new IncludeDefaultsSubstitutor(parent);
			IOption option = parent.getOption();
			String str = null;
			String strL[] = null;
			try{
				switch(option.getValueType()){
				case IOption.STRING :
					str = option.getStringValue();
					break;
				case IOption.STRING_LIST :
					strL = option.getStringListValue();
					break;
				case IOption.INCLUDE_PATH :
					strL = option.getIncludePaths();
					break;
				case IOption.PREPROCESSOR_SYMBOLS :
					strL = option.getDefinedSymbols();
					break;
				case IOption.LIBRARIES :
					strL = option.getLibraries();
					break;
				case IOption.OBJECTS :
					strL = option.getUserObjects();
					break;
				default :
					break;
				}
				
				if(str != null)
					return MacroResolver.resolveToString(str,sub);
				else if(strL != null){
					strL = MacroResolver.resolveStringListValues(strL,sub,true);
					return MacroResolver.convertStringListToString(strL," "); 	//$NON-NLS-1$
				}
			} catch (BuildException e){
				
			} catch (BuildMacroException e){
				
			}
			return null;
		}

		public String[] resolveToStringList(String macroName) throws BuildMacroException {
			if(!"IncludeDefaults".equals(macroName)) 	//$NON-NLS-1$
				return new String[]{MacroResolver.createMacroReference(macroName)};
			
			IOptionContextData parent = getParent(fOptionContextData);
			if(parent == null)
				return new String[]{EMPTY_STRING};
			IncludeDefaultsSubstitutor sub = new IncludeDefaultsSubstitutor(parent);
			IOption option = parent.getOption();
			String str = null;
			String strL[] = null;
			try{
				switch(option.getValueType()){
				case IOption.STRING :
					str = option.getStringValue();
					break;
				case IOption.STRING_LIST :
					strL = option.getStringListValue();
					break;
				case IOption.INCLUDE_PATH :
					strL = option.getIncludePaths();
					break;
				case IOption.PREPROCESSOR_SYMBOLS :
					strL = option.getDefinedSymbols();
					break;
				case IOption.LIBRARIES :
					strL = option.getLibraries();
					break;
				case IOption.OBJECTS :
					strL = option.getUserObjects();
					break;
				default :
					break;
				}
				
				if(str != null)
					return MacroResolver.resolveToStringList(str,sub);
				else if(strL != null)
					return MacroResolver.resolveStringListValues(strL,sub,true);
			} catch (BuildException e){
				
			} catch (BuildMacroException e){
				
			}
			return null;
		}
		
		public void setMacroContextInfo(int contextType, Object contextData) throws BuildMacroException {
		}

		public IMacroContextInfo getMacroContextInfo() {
			return null;
		}
	}

	public class OptionMacro extends BuildMacro{
		private IOptionContextData fOptionContextData;
		private IOptionContextData fParentOptionContextData;
//		private IOption fParentOption;
		private OptionMacro(String name, IOptionContextData optionContextData){
			fName = name;
			fOptionContextData = optionContextData;
			fParentOptionContextData = getParent(fOptionContextData);
			load();
		}
		
		private boolean load(){
			fStringValue = null;
			fStringListValue = null;
			fType = 0;
			if(fParentOptionContextData != null){
				IOption option = fParentOptionContextData.getOption();
				try{
					switch (option.getValueType()) {
					case IOption.BOOLEAN:
						break;
					case IOption.STRING:
						fType = IBuildMacro.VALUE_TEXT;
						fStringValue = option.getStringValue();
						break;
					case IOption.ENUMERATED:
						break;
					case IOption.STRING_LIST:
						fType = IBuildMacro.VALUE_TEXT_LIST;
						fStringListValue = option.getStringListValue();
						break;
					case IOption.INCLUDE_PATH:
						fType = IBuildMacro.VALUE_PATH_DIR_LIST;
						fStringListValue = option.getIncludePaths();
						break;
					case IOption.PREPROCESSOR_SYMBOLS:
						fType = IBuildMacro.VALUE_TEXT_LIST;
						fStringListValue = option.getDefinedSymbols();
						break;
					case IOption.LIBRARIES:
						fType = IBuildMacro.VALUE_PATH_FILE_LIST;
						fStringListValue = option.getLibraries();
						break;
					case IOption.OBJECTS:
						fType = IBuildMacro.VALUE_PATH_FILE_LIST;
						fStringListValue = option.getUserObjects();
						break;
					}
					if(fStringValue != null)
						fStringValue = MacroResolver.resolveToString(fStringValue,new IncludeDefaultsSubstitutor(fParentOptionContextData));
					else if(fStringListValue != null)
						fStringListValue = MacroResolver.resolveStringListValues(fStringListValue,new IncludeDefaultsSubstitutor(fParentOptionContextData), true);
				}catch(Exception e){
					fType = 0;
				}
			}
			
			boolean result = fType != 0;
			if(!result){
				fType = VALUE_TEXT;
				fStringListValue = null;
				fStringValue = null;
			}
			
			return result;
		}
	}

	private OptionData getParent(IOptionContextData optionContext){
		if(optionContext == null)
			return null;
		IOption option = optionContext.getOption();
		if(option == null)
			return null;
		IOption parentOption = null;
			
		IBuildObject parent = option.getParent();
		ITool tool = null;
		if (parent instanceof ITool) {
			tool = (ITool)parent;
		}
		IBuildObject bObj = (optionContext instanceof OptionData) ?
				((OptionData)optionContext).getOptionContainer() : optionContext.getParent();

		IResourceConfiguration rcCfg = null;
		ITool holderTool = null;
		if(bObj instanceof ITool){
			holderTool = (ITool)bObj;
			IBuildObject p = holderTool.getParent();
			if(p instanceof IResourceConfiguration)
				rcCfg = (IResourceConfiguration)p;
		} else if(bObj instanceof IResourceConfiguration)
			rcCfg = (IResourceConfiguration)bObj;

		IBuildObject parentObject = rcCfg == null ? bObj : rcCfg.getParent();

		if(rcCfg != null && rcCfg.getTool(tool.getId()) != null){
			tool = tool.getSuperClass();
			parentOption = tool.getOptionBySuperClassId(option.getSuperClass().getId());
		} else {
			parentOption = option.getSuperClass();
		}
		
		if(parentOption != null)
			return new OptionData(parentOption,bObj,parentObject);

		return null;
	}

	private boolean canHandle(IOptionContextData optionData){
		IOption option = optionData.getOption();
		if(option == null)
			return false;
		
		boolean can = false;
		try{
			switch (option.getValueType()) {
			case IOption.BOOLEAN:
				break;
			case IOption.STRING:
				can = true;
				break;
			case IOption.ENUMERATED:
				break;
			case IOption.STRING_LIST:
				can = true;
				break;
			case IOption.INCLUDE_PATH:
				can = true;
				break;
			case IOption.PREPROCESSOR_SYMBOLS:
				can = true;
				break;
			case IOption.LIBRARIES:
				can = true;
				break;
			case IOption.OBJECTS:
				can = true;
				break;
			}
		}catch(BuildException e){
			can = false;
		}
		return can;
	}
	
}
