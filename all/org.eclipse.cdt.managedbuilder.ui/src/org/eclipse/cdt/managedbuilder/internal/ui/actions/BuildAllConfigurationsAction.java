/*******************************************************************************
 * Copyright (c) 2010, 2010 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev (Quoin Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.ui.actions;

import java.util.ArrayList;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.ide.actions.BuildUtilities;

/**
 * Action which builds all configurations of the selected projects
 */
public class BuildAllConfigurationsAction implements IObjectActionDelegate {
	private ArrayList<IProject> projects = null;

	public void selectionChanged(IAction action, ISelection selection) {
		projects = CleanAndBuildAction.getSelectedCdtProjects(selection);
		action.setEnabled(projects.size() > 0);
	}

	public void run(IAction action) {
		for (IProject project : projects) {
			ICProjectDescription prjd = CoreModel.getDefault().getProjectDescription(project, false);
			if (prjd != null) {
				ICConfigurationDescription[] cfgds = prjd.getConfigurations();
				if (cfgds != null && cfgds.length > 0) {
					// save all dirty editors
					BuildUtilities.saveEditors(null);
					
					Job buildFilesJob = new BuildConfigurationsJob(cfgds, 0, IncrementalProjectBuilder.INCREMENTAL_BUILD);
					buildFilesJob.schedule();
				}
			}

		}
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}
}
