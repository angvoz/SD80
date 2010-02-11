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
package org.eclipse.cdt.debug.edc.internal.symbols.files;

import java.io.IOException;

import org.eclipse.cdt.debug.edc.internal.IStreamBuffer;
import org.eclipse.core.runtime.IPath;

/**
 * Handle mapping sections of an executable into memory and caching the content.
 * This may hold files open and consume heap and system memory.
 */
public interface ISectionMapper {
	/**
	 * Get the associated file on which content is mapped.
	 */
	IPath getMappedFile();
	
	/**
	 * Get the contents of a section.  The buffer may be cached or may be fetched on-demand,
	 * so there is no (memory) limit on its size.
	 * The buffer has the appropriate endianness for the file.
	 * @param section a backend-specific object representing a section 
	 * @return an {@link IStreamBuffer} for the contents
	 * @throws IOException if contents could not be (re-)read.
	 */
	IStreamBuffer getSectionBuffer(SectionInfo section) throws IOException;

	/**
	 * Explicitly release a section's buffer.  References to the previously-returned
	 * ByteBuffer may become invalid.
	 */
	void releaseSectionBuffer(SectionInfo section);
	
	/**
	 * Free any cached content and close any open files.
	 */
	void dispose();
}
