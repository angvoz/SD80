/**********************************************************************
 * Created on Mar 25, 2003
 *
 * Copyright (c) 2002,2003 QNX Software Systems Ltd. and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.ISourceEntry;
import org.eclipse.core.runtime.IPath;

public class SourceEntry extends APathEntry implements ISourceEntry {

	public SourceEntry(IPath path, IPath[] exclusionPatterns) {
		super(ISourceEntry.CDT_SOURCE, path, null, exclusionPatterns, false);
	}

	public boolean equals (Object obj) {
		if (obj instanceof ISourceEntry) {
			ISourceEntry otherEntry = (ISourceEntry)obj;
			if (!super.equals(otherEntry)) {
				return false;
			}
			if (path == null) {
				if (otherEntry.getPath() != null) {
					return false;
				}
			} else {
				if (!path.toString().equals(otherEntry.getPath().toString())) {
					return false;
				}
			}
			return true;
		}
		return super.equals(obj);
	}

}
