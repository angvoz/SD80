/*
 * (c) Copyright IBM Corp. 2003.
 * Created on 27 févr. 03
 * All Rights Reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 *
 * Contributors:
 * IBM Software Group - Rational Software
 */

package org.eclipse.cdt.internal.cppunit.util;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.cdt.internal.cppunit.ui.CppUnitPlugin;
import org.eclipse.jface.util.Assert;

/**
 * An implemention of IStatus. 
 * TO DO: Why is it duplicated, it should leverage the Status base class???
 */
public class CppUnitStatus implements IStatus {
	private String fStatusMessage;
	private int fSeverity;
	
	/**
	 * Creates a status set to OK (no message)
	 */
	public CppUnitStatus() {
		this(OK, null);
	}

	/**
	 * Creates a status .
	 * @param severity The status severity: ERROR, WARNING, INFO and OK.
	 * @param message The message of the status. Applies only for ERROR,
	 * WARNING and INFO.
	 */	
	public CppUnitStatus(int severity, String message) {
		fStatusMessage= message;
		fSeverity= severity;
	}		
	
	/**
	 *  Returns if the status' severity is OK.
	 */
	public boolean isOK() {
		return fSeverity == IStatus.OK;
	}

	/**
	 *  Returns if the status' severity is WARNING.
	 */	
	public boolean isWarning() {
		return fSeverity == IStatus.WARNING;
	}

	/**
	 *  Returns if the status' severity is INFO.
	 */	
	public boolean isInfo() {
		return fSeverity == IStatus.INFO;
	}	

	/**
	 *  Returns if the status' severity is ERROR.
	 */	
	public boolean isError() {
		return fSeverity == IStatus.ERROR;
	}
	
	/**
	 * @see IStatus#getMessage
	 */
	public String getMessage() {
		return fStatusMessage;
	}
	
	/**
	 * Sets the status to ERROR.
	 * @param The error message (can be empty, but not null)
	 */	
	public void setError(String errorMessage) {
		Assert.isNotNull(errorMessage);
		fStatusMessage= errorMessage;
		fSeverity= IStatus.ERROR;
	}

	/**
	 * Sets the status to WARNING.
	 * @param The warning message (can be empty, but not null)
	 */		
	public void setWarning(String warningMessage) {
		Assert.isNotNull(warningMessage);
		fStatusMessage= warningMessage;
		fSeverity= IStatus.WARNING;
	}

	/**
	 * Sets the status to INFO.
	 * @param The info message (can be empty, but not null)
	 */		
	public void setInfo(String infoMessage) {
		Assert.isNotNull(infoMessage);
		fStatusMessage= infoMessage;
		fSeverity= IStatus.INFO;
	}	

	/**
	 * Sets the status to OK.
	 */		
	public void setOK() {
		fStatusMessage= null;
		fSeverity= IStatus.OK;
	}
	
	/*
	 * @see IStatus#matches(int)
	 */
	public boolean matches(int severityMask) {
		return (fSeverity & severityMask) != 0;
	}

	/**
	 * Returns always <code>false</code>.
	 * @see IStatus#isMultiStatus()
	 */
	public boolean isMultiStatus() {
		return false;
	}

	/*
	 * @see IStatus#getSeverity()
	 */
	public int getSeverity() {
		return fSeverity;
	}

	/*
	 * @see IStatus#getPlugin()
	 */
	public String getPlugin() {
		return CppUnitPlugin.PLUGIN_ID;
	}

	/**
	 * Returns always <code>null</code>.
	 * @see IStatus#getException()
	 */
	public Throwable getException() {
		return null;
	}

	/**
	 * Returns always the error severity.
	 * @see IStatus#getCode()
	 */
	public int getCode() {
		return fSeverity;
	}

	/**
	 * Returns always <code>null</code>.
	 * @see IStatus#getChildren()
	 */
	public IStatus[] getChildren() {
		return new IStatus[0];
	}	

}
