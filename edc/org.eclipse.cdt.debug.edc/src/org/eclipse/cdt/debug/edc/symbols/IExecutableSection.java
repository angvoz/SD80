/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.symbols;

import org.eclipse.cdt.debug.edc.IStreamBuffer;

/**
 * This abstracts access to a particular section in an executable
 */
public interface IExecutableSection {
	/**
	 * Get the name of the section.
	 */
	String getName();
	
	/**
	 * Get the buffer for the section.  This may be thrown away and reloaded on demand.
	 * The buffer has the correct endianness already set.
	 * @return buffer, or <code>null</code> if failed to load
	 */
	IStreamBuffer getBuffer();

	/**
	 * Free the buffer allocated for this section.
	 */
	void dispose();
}
