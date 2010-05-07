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

import org.eclipse.cdt.debug.edc.IStreamBuffer;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.symbols.IExecutableSection;

/**
 * 
 */
public class ExecutableSection implements IExecutableSection {

	private final String name;
	private final ISectionMapper executableSectionMapper;
	private final SectionInfo section;
	private IStreamBuffer buffer;
	private boolean deadSection;

	/**
	 * @param section
	 * @param name
	 * @param elfExecutableSymbolicsReader
	 */
	public ExecutableSection(ISectionMapper executableSectionMapper,
			String name,
			SectionInfo section) {
		this.executableSectionMapper = executableSectionMapper;
		this.name = name;
		this.section = section;
		this.deadSection = false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name + " @ " + section + (deadSection ? " <<BROKEN>>": ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.exe.IExecutableSection#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.exe.IExecutableSection#getBuffer()
	 */
	public IStreamBuffer getBuffer() {
		if (buffer == null && !deadSection) {
			try {
				buffer = executableSectionMapper.getSectionBuffer(section);
			} catch (IOException e) {
				deadSection = true;
				EDCDebugger.getMessageLogger().logError("Failed to read section " + name, e);
			}  
		}
		return buffer;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.exe.IExecutableSection#dispose()
	 */
	public void dispose() {
		executableSectionMapper.releaseSectionBuffer(section);
		buffer = null;
	}
}
