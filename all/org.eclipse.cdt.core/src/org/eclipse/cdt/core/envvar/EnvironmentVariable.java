/*******************************************************************************
 * Copyright (c) 2005, 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.envvar;

import org.eclipse.cdt.internal.core.envvar.EnvironmentVariableManager;



/**
 * a trivial implementation of the IBuildEnvironmentVariable
 * 
 * @since 3.0
 */
public class EnvironmentVariable implements IEnvironmentVariable, Cloneable {
	protected String fName;
	protected String fValue;
	protected String fDelimiter;
	protected int fOperation;
	
	public EnvironmentVariable(String name, String value, int op, String delimiter){
		fName = name;
		fOperation = op;
		fValue = value;
		fDelimiter = delimiter;
	}
	
	protected EnvironmentVariable(){
		
	}
	
	public EnvironmentVariable(String name){
		this(name,null,ENVVAR_REPLACE,null);
	}
	
	public EnvironmentVariable(String name, String value){
		this(name,value,ENVVAR_REPLACE,null);	
	}

	public EnvironmentVariable(String name, String value, String delimiter){
		this(name,value,ENVVAR_REPLACE,delimiter);	
	}
	
	public EnvironmentVariable(IEnvironmentVariable var){
		this(var.getName(),var.getValue(),var.getOperation(),var.getDelimiter());	
	}

	public String getName(){
		return fName;
	}

	public String getValue(){
		return fValue;
	}

	public int getOperation(){
		return fOperation;
	}

	public String getDelimiter(){
		if (fDelimiter == null)
			return EnvironmentVariableManager.getDefault().getDefaultDelimiter();
		else
			return fDelimiter;
	}
	
	@Override
	public Object clone(){
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
		}
		return null;
	}
}
