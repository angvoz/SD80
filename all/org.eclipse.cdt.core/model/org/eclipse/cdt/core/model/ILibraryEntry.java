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
package org.eclipse.cdt.core.model;

import org.eclipse.core.runtime.IPath;

public interface ILibraryEntry extends ICPathEntry {

	/**
	 * Returns the absolute path of the library
	 * @return String
	 */
	IPath getLibraryPath();

	/**
	 * Returns the path to the source archive or folder associated with this
	 * C path entry, or <code>null</code> if this C path entry has no
	 * source attachment.
	 * <p>
	 * Only library and variable C path entries may have source attachments.
	 * For library C path entries, the result path (if present) locates a source
	 * archive or folder. This archive or folder can be located in a project of the
	 * workspace or outside thr workspace. For variable c path entries, the
	 * result path (if present) has an analogous form and meaning as the
	 * variable path, namely the first segment is the name of a c path variable.
	 * </p>
	 *
	 * @return the path to the source archive or folder, or <code>null</code> if none
	 */
	IPath getSourceAttachmentPath();

	/**
	 * Returns the path within the source archive or folder where source
	 * are located. An empty path indicates that packages are located at
	 * the root of the source archive or folder. Returns a non-<code>null</code> value
	 * if and only if <code>getSourceAttachmentPath</code> returns
	 * a non-<code>null</code> value.
	 *
	 * @return the path within the source archive or folder, or <code>null</code> if
	 *    not applicable
	 */
	IPath getSourceAttachmentRootPath();
 
	/**
	 * Returns the path to map the source paths with to the source achive or folder
	 * An empty path indicates that the is a one-to-one mapping of source paths to the
	 * source achive or folder path. Returns a non-<code>null</code> value
	 * if and only if <code>getSourceAttachmentPath</code> returns
	 * a non-<code>null</code> value.
	 *
	 * @return the path mapping within the source archive or folder, or <code>null</code> if
	 *    not applicable
	 */
	IPath getSourceAttachmentPrefixMapping();

}
