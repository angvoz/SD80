/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.model;

import org.eclipse.core.runtime.IPath;

/**
 * 
 */
public interface IPathEntryContainerExtension extends IPathEntryContainer {

	/**
	 * Returns the set of entries associated with the resource
	 * and empty array if none.
	 * 
	 * @param path Workspace relative path.
	 * @param typeMask type of path entries:
	 * <li><code>IPathEntry.CDT_INCLUDE</code></li>
	 * <li><code>IPathEntry.CDT_INCLUDE_FILE</code></li>
	 * <li><code>IPathEntry.CDT_MACRO_FILE</code></li>
	 * <li><code>IPathEntry.CDT_MACRO</code></li>
	 * @return IPathEntry[] - the entries or empty set if none
	 * @see IPathEntry
	 */
	IPathEntry[] getPathEntries(IPath path, int typesMask);

	/**
	 * Returns whether there are any path entries for the resource.
	 * 
	 * @param path Workspace relative path.
	 * @return
	 */
	boolean isEmpty(IPath path);
}
