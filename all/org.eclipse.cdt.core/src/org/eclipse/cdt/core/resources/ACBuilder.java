/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.core.resources;


import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.osgi.util.NLS;

public abstract class ACBuilder extends IncrementalProjectBuilder implements IMarkerGenerator {

	private static final String PREF_BUILD_ALL_CONFIGS = "build.all.configs.enabled"; //$NON-NLS-1$
	private static final String PREF_BUILD_CONFIGS_RESOURCE_CHANGES = "build.proj.ref.configs.enabled"; //$NON-NLS-1$
	private static final Preferences prefs = CCorePlugin.getDefault().getPluginPreferences();

	/**
	 * Constructor for ACBuilder
	 */
	public ACBuilder() {
		super();
	}

	public void addMarker(IResource file, int lineNumber, String errorDesc, int severity, String errorVar) {
		ProblemMarkerInfo problemMarkerInfo = new ProblemMarkerInfo(file, lineNumber, errorDesc, severity, errorVar, null);
		addMarker(problemMarkerInfo);
	}

		/*
		 * callback from Output Parser
		 */
	public void addMarker(ProblemMarkerInfo problemMarkerInfo) {
		try {
			IResource markerResource = problemMarkerInfo.file ;
			if (markerResource==null)  {
				markerResource = getProject();
			}
			IMarker[] cur = markerResource.findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_ONE);
			/*
			 * Try to find matching markers and don't put in duplicates
			 */
			String externalLocation = null;
			if (problemMarkerInfo.externalPath != null) {
				externalLocation = problemMarkerInfo.externalPath.toOSString();
			}
			if ((cur != null) && (cur.length > 0)) {
				for (IMarker element : cur) {
					int line = ((Integer) element.getAttribute(IMarker.LINE_NUMBER)).intValue();
					int sev = ((Integer) element.getAttribute(IMarker.SEVERITY)).intValue();
					String mesg = (String) element.getAttribute(IMarker.MESSAGE);
					String extloc = (String) element.getAttribute(ICModelMarker.C_MODEL_MARKER_EXTERNAL_LOCATION);
					if (line == problemMarkerInfo.lineNumber && sev == mapMarkerSeverity(problemMarkerInfo.severity) && mesg.equals(problemMarkerInfo.description)) {
						if (extloc==externalLocation || (extloc!=null && extloc.equals(externalLocation))) {
							return;
						}
					}
				}
			}
			
			IMarker marker = markerResource.createMarker(ICModelMarker.C_MODEL_PROBLEM_MARKER);
			marker.setAttribute(IMarker.MESSAGE, problemMarkerInfo.description);
			marker.setAttribute(IMarker.SEVERITY, mapMarkerSeverity(problemMarkerInfo.severity));
			marker.setAttribute(IMarker.LINE_NUMBER, problemMarkerInfo.lineNumber);
			marker.setAttribute(IMarker.CHAR_START, -1);
			marker.setAttribute(IMarker.CHAR_END, -1);
			if (problemMarkerInfo.variableName != null) {
				marker.setAttribute(ICModelMarker.C_MODEL_MARKER_VARIABLE, problemMarkerInfo.variableName);
			}
			if (externalLocation != null) {
				marker.setAttribute(ICModelMarker.C_MODEL_MARKER_EXTERNAL_LOCATION, externalLocation);
				String locationText = NLS.bind(CCorePlugin.getResourceString("ACBuilder.ProblemsView.Location"), //$NON-NLS-1$
						problemMarkerInfo.lineNumber, externalLocation);
				marker.setAttribute(IMarker.LOCATION, locationText);
			}
		}
		catch (CoreException e) {
			CCorePlugin.log(e.getStatus());
		}

	}

	int mapMarkerSeverity(int severity) {
		switch (severity) {
			case SEVERITY_ERROR_BUILD :
			case SEVERITY_ERROR_RESOURCE :
				return IMarker.SEVERITY_ERROR;
			case SEVERITY_INFO :
				return IMarker.SEVERITY_INFO;
			case SEVERITY_WARNING :
				return IMarker.SEVERITY_WARNING;
		}
		return IMarker.SEVERITY_ERROR;
	}
	
	public static boolean needAllConfigBuild() {
		return prefs.getBoolean(PREF_BUILD_ALL_CONFIGS);
	}
	
	public static void setAllConfigBuild(boolean enable) {
		prefs.setValue(PREF_BUILD_ALL_CONFIGS, enable);		
	}
	
	/**
	 * Preference for building configurations only when there are resource changes within Eclipse or
	 * when there are changes in its references.
	 * @return true if configurations will be build when project resource changes within Eclipse 
	 *         false otherwise
	 * @since 5.1
	 */
	public static boolean buildConfigResourceChanges() {
		//bug 219337
		return prefs.getBoolean(PREF_BUILD_CONFIGS_RESOURCE_CHANGES);
	}
	
	/**
	 * Preference for building configurations only when there are resource changes within Eclipse or
	 * when there are changes in its references.
	 * @param enable
	 * @since 5.1
	 */
	public static void setBuildConfigResourceChanges(boolean enable) {
		prefs.setValue(PREF_BUILD_CONFIGS_RESOURCE_CHANGES, enable);		
	}
	
}
