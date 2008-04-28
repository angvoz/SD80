/*******************************************************************************
 * Copyright (c) 2008 QNX Software Systems and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * QNX Software Systems - catchpoints - bug 226689
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.breakpoints;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;

public class CBreakpointUIContributionFactory {
	private static final String EXTENSION_POINT_NAME = "breakpointContribution";
	private static final String MAIN_ELEMENT = "breakpointLabels";

	private static CBreakpointUIContributionFactory instance;
	protected ArrayList<ICBreakpointsUIContribution> contributions;

	private CBreakpointUIContributionFactory() {
		contributions = new ArrayList<ICBreakpointsUIContribution>();
		loadSubtypeContributions();
	}

	/**
	 * 
	 * @param breakpoint
	 * @return non-null array of ICBreakpointsUIContribution 
	 * @throws CoreException 
	 * @throws CoreException if cannot get marker attributes from berakpoint
	 */

	public ICBreakpointsUIContribution[] getBreakpointUIContributions(IBreakpoint breakpoint) throws CoreException {
		String debugModelId = breakpoint.getModelIdentifier();
		IMarker bmarker = breakpoint.getMarker();
		Map attributes = bmarker.getAttributes();
		String markerType = bmarker.getType();
		return getBreakpointUIContributions(debugModelId, markerType, attributes);
	}

	public ICBreakpointsUIContribution[] getBreakpointUIContributions(String debugModelId, String markerType,
			Map attributes) {
		ArrayList<ICBreakpointsUIContribution> list = new ArrayList<ICBreakpointsUIContribution>();
		for (ICBreakpointsUIContribution con : contributions) {
			try {
				if (debugModelId == null || con.getDebugModelId() == null || debugModelId.equals(con.getDebugModelId())) {
					String contributedMarkerType = con.getMarkerType();
					if (isMarkerSubtypeOf(markerType, contributedMarkerType)) {
						if (attributes == null || con.isApplicable(attributes)) {
							list.add(con);
						}
					}
				}
			} catch (Exception e) {
				CDebugUIPlugin.log(e);
			}

		}
		return list.toArray(new ICBreakpointsUIContribution[list.size()]);
	}

	public boolean isMarkerSubtypeOf(String currentType, String type) throws CoreException {
		return getWorkspace().getMarkerManager().isSubtype(currentType, type);
	}

	private Workspace getWorkspace() {
		return (Workspace) CDebugUIPlugin.getWorkspace();
	}

	private void loadSubtypeContributions() {

		IExtensionPoint ep = Platform.getExtensionRegistry().getExtensionPoint(CDebugUIPlugin.getUniqueIdentifier(),
				EXTENSION_POINT_NAME);
		if (ep == null)
			return;
		IConfigurationElement[] elements = ep.getConfigurationElements();
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement configurationElement = elements[i];
			if (configurationElement.getName().equals(MAIN_ELEMENT)) {
				String modelId = configurationElement.getAttribute("debugModelId");
				String markerType = getRequired(configurationElement, "markerType");
				if (markerType == null)
					continue;
				IConfigurationElement[] children = configurationElement.getChildren("attribute");
				for (IConfigurationElement att : children) {

					DefaultCBreakpointUIContribution adapter = new DefaultCBreakpointUIContribution();
					adapter.setMarkerType(markerType);
					adapter.setDebugModelId(modelId);
					if (processAttribute(att, adapter) == false)
						continue;

				}

			}
		}
	}

	private boolean processAttribute(IConfigurationElement attrElement, DefaultCBreakpointUIContribution adapter) {
		String attrId = getRequired(attrElement, "name");
		String attrLabel = getRequired(attrElement, "label");
		String className = attrElement.getAttribute("fieldEditor");
		String type = attrElement.getAttribute("type");
		String svisible = attrElement.getAttribute("visible");

		if (attrId == null) {
			return false;
		}
		if (attrLabel == null) {
			return false;
		}
		if (type == null) {
			type = "string";
		}
		boolean visible = true;
		if (svisible != null && svisible.equalsIgnoreCase("false")) {
			visible = false;
		}
		adapter.setId(attrId);
		adapter.setLabel(attrLabel);
		adapter.setControlClass(className);
		adapter.setType(type);
		adapter.setVisible(visible);
		addContribution(adapter);

		IConfigurationElement[] children = attrElement.getChildren("value");
		for (IConfigurationElement value : children) {
			processValue(value, adapter);
		}
		return true;
	}

	private void processValue(IConfigurationElement valueElement, DefaultCBreakpointUIContribution adapter) {
		String valueId = getRequired(valueElement, "value");
		String valueLabel = getRequired(valueElement, "label");
		if (valueId == null)
			return;
		if (valueLabel == null)
			return;
		adapter.addValue(valueId, valueLabel);
		IConfigurationElement[] children = valueElement.getChildren("attribute");
		for (IConfigurationElement att : children) {
			DefaultCBreakpointUIContribution adapter2 = new DefaultCBreakpointUIContribution();
			// inherit values
			adapter2.setMarkerType(adapter.getMarkerType());
			adapter2.setDebugModelId(adapter.getDebugModelId());
			adapter2.addContionsAll(adapter.getConditions());
			// add value condition
			adapter2.addContionEquals(adapter.getId(), valueId);
			if (processAttribute(att, adapter2) == false)
				continue;
		}
	}

	public void addContribution(ICBreakpointsUIContribution contribution) {
		contributions.add(contribution);

	}

	public static CBreakpointUIContributionFactory getInstance() {
		if (instance == null) {
			instance = new CBreakpointUIContributionFactory();
		}
		return instance;
	}

	private static String getRequired(IConfigurationElement configurationElement, String name) {
		String elementValue = configurationElement.getAttribute(name);
		if (elementValue == null)
			CDebugUIPlugin.log(new Status(IStatus.ERROR, CDebugUIPlugin.getUniqueIdentifier(),
					DebugPlugin.INTERNAL_ERROR, "Extension "
							+ configurationElement.getDeclaringExtension().getUniqueIdentifier()
							+ " missing required attribute: " + name, null));
		return elementValue;
	}

}
