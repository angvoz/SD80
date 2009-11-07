/*******************************************************************************
 * Copyright (c) 2009 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.launch;

/**
 * Class to hold process information (ID, name) for the ChooseProcessDialog
 */
public class ChooseProcessItem {
	String processID;
	String processName;

	public ChooseProcessItem(String id, String name) {
		this.processID = id;
		this.processName = name;
	}

	public String getProcessID() {
		return processID;
	}

	public String getProcessName() {
		return processName;
	}

	public void setProcessID(String processID) {
		this.processID = processID;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}
}
