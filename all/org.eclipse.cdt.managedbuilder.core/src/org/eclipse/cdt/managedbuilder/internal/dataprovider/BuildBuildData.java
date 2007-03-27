/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.dataprovider;

import org.eclipse.cdt.core.envvar.IEnvironmentContributor;
import org.eclipse.cdt.core.settings.model.ICOutputEntry;
import org.eclipse.cdt.core.settings.model.extension.CBuildData;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.internal.core.Builder;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;

public class BuildBuildData extends CBuildData {
	private Builder fBuilder;
	private Configuration fCfg;
//	private BuildEnvironmentContributor fEnvContibutor;
	public BuildBuildData(IBuilder builder){
		fBuilder = (Builder)builder;
		fCfg = (Configuration)fBuilder.getParent().getParent();
	}

	public IPath getBuilderCWD() {
		return new Path(fBuilder.getBuildPath());//ManagedBuildManager.getBuildLocation(fCfg, fBuilder);
	}
	
	private IPath createAbsolutePathFromWorkspacePath(IPath path){
		IStringVariableManager mngr = VariablesPlugin.getDefault().getStringVariableManager();
		String locationString = mngr.generateVariableExpression("workspace_loc", path.toString()); //$NON-NLS-1$
		return new Path(locationString);
	}

	public String[] getErrorParserIDs() {
		return fCfg.getErrorParserList();
	}

	public ICOutputEntry[] getOutputDirectories() {
		return fBuilder.getOutputEntries();
	}

	public void setBuilderCWD(IPath path) {
		fBuilder.setBuildPath(path.toString());
	}

	public void setErrorParserIDs(String[] ids) {
		fCfg.setErrorParserList(ids);
	}

	public void setOutputDirectories(ICOutputEntry[] entries) {
		fBuilder.setOutputEntries(entries);
	}

	public String getId() {
		return fBuilder.getId();
	}

	public String getName() {
		return fBuilder.getName();
	}

	public boolean isValid() {
		return fBuilder != null;
	}

	public void setName(String name) {
		//TODO
	}

	public IEnvironmentContributor getBuildEnvironmentContributor() {
//		if(fEnvContibutor == null)
//			fEnvContibutor = new BuildEnvironmentContributor(this);
//		return fEnvContibutor;
		return new BuildEnvironmentContributor(this);
	}
	
	public IBuilder getBuilder(){
		return fBuilder;
	}

}
