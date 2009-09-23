/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.core;

import org.eclipse.cdt.codan.core.model.CodanSeverity;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemProfile;
import org.eclipse.cdt.codan.internal.core.model.CodanProblem;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

/**
 * @author Alena
 * 
 */
public class CodanPreferencesLoader {
	private IProblemProfile baseModel;

	/**
	 * @param workspaceProfile
	 */
	public CodanPreferencesLoader(IProblemProfile profile) {
		setInput(profile);
	}

	/**
	 * 
	 */
	public CodanPreferencesLoader() {
	}

	public void setInput(Object model) {
		baseModel = (IProblemProfile) model;
	}

	/**
	 * @return
	 */
	public IProblem[] getProblems() {
		IProblem[] problems = baseModel.getProblems();
		return problems;
	}

	/**
	 * @param id
	 * @param s
	 */
	public void setProperty(String id, String s) {
		IProblem prob = baseModel.findProblem(id);
		if (!(prob instanceof CodanProblem))
			return;
		String sevs = s;
		boolean enabled = true;
		if (sevs.startsWith("-")) {
			sevs = sevs.substring(1);
			enabled = false;
		}
		((CodanProblem) prob).setEnabled(enabled);
		CodanSeverity sev;
		try {
			sev = CodanSeverity.valueOf(sevs);
		} catch (RuntimeException e) {
			sev = CodanSeverity.Warning;
		}
		((CodanProblem) prob).setSeverity(sev);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getInput().toString();
	}

	/**
	 * @return
	 */
	public IProblemProfile getInput() {
		return baseModel;
	}

	/**
	 * @param id
	 * @return
	 */
	public String getProperty(String id) {
		IProblem prob = baseModel.findProblem(id);
		if (!(prob instanceof CodanProblem))
			return null;
		String enabled = prob.isEnabled() ? "" : "-";
		String severity = prob.getSeverity().toString();
		String res = enabled + severity;
		return res;
	}

	/**
	 * @param storePreferences
	 */
	public void load(IEclipsePreferences storePreferences) {
		IProblem[] probs = getProblems();
		for (int i = 0; i < probs.length; i++) {
			String id = probs[i].getId();
			String s = storePreferences.get(id, null);
			if (s != null) {
				setProperty(id, s);
			}
		}
	}
}
