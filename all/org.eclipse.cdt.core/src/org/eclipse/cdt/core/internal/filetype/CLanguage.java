/**********************************************************************
 * Copyright (c) 2004 TimeSys Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * TimeSys Corporation - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.internal.filetype;

import org.eclipse.cdt.core.filetype.ICLanguage;

/**
 * Representation of a declared file type.
 */
public class CLanguage implements ICLanguage {

	private String	fId;
	private String	fName;

	public CLanguage(String id, String name) {
		fId		= id;
		fName	= name;
	}
	
	public String getId() {
		return fId;
	}

	public String getName() {
		return fName;
	}
}
