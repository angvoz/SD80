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
package org.eclipse.cdt.internal.core.settings.model;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public abstract class CExternalSettingContainerFactory {
	
	public abstract CExternalSettingsContainer createContainer(
			String id,
			IProject project,
			ICConfigurationDescription cfgDes) throws CoreException;

	public void addListener(ICExternalSettingsListener listener){
	}
	
	public void removeListener(ICExternalSettingsListener listener){
	}

	public void startup(){
	}
	
	public void shutdown(){
	}
}
