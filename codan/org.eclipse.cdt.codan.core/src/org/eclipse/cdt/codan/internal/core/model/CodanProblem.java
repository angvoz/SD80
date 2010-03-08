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
package org.eclipse.cdt.codan.internal.core.model;

import java.util.HashMap;

import org.eclipse.cdt.codan.core.model.CodanSeverity;
import org.eclipse.cdt.codan.core.model.IProblemCategory;
import org.eclipse.cdt.codan.core.model.IProblemParameterInfo;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;

public class CodanProblem implements IProblemWorkingCopy {
	private String id;
	private String name;
	private String message;
	private CodanSeverity severity = CodanSeverity.Warning;
	private boolean enabled = true;
	private HashMap<Object, Object> parameters = new HashMap<Object, Object>(0);
	private IProblemParameterInfo parameterInfo;
	private boolean frozen;

	public CodanSeverity getSeverity() {
		return severity;
	}

	public CodanProblem(String problemId, String name) {
		this.id = problemId;
		this.name = name;
		this.frozen = false;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public IProblemCategory getCategory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		return name;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setSeverity(CodanSeverity sev) {
		if (sev == null) throw new NullPointerException();
		this.severity = sev;
	}

	public void setEnabled(boolean checked) {
		this.enabled = checked;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public void setParameter(Object key, Object value) {
		parameters.put(key, value);
	}

	public void setParameterInfo(IProblemParameterInfo info) {
		parameterInfo = info;
	}

	public Object getParameter(Object key) {
		return parameters.get(key);
	};

	public IProblemParameterInfo getParameterInfo() {
		return parameterInfo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.model.IProblem#getMessagePattern()
	 */
	public String getMessagePattern() {
		return message;
	}

	protected void freeze() {
		frozen = true;
	}

	/**
	 * @param message
	 *            the message to set
	 */
	public void setMessagePattern(String message) {
		checkSet();
		this.message = message;
	}

	protected void checkSet() {
		if (frozen) throw new IllegalStateException("Object is unmodifieble"); //$NON-NLS-1$
	}
}
