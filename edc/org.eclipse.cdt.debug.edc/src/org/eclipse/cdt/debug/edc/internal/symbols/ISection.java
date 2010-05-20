/*******************************************************************************
 * Copyright (c) 2009 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.symbols;

import java.util.Map;

import org.eclipse.cdt.core.IAddress;

/**
 * Interface representing a section in memory. It is segment in Elf file and a
 * section in PE file.
 */
public interface ISection {

	/**
	 * Commonly known properties of a section
	 */
	static final String PROPERTY_ID = "id"; //$NON-NLS-1$
	static final String PROPERTY_SIZE = "size"; //$NON-NLS-1$
	static final String PROPERTY_LINK_ADDRESS = "link_address"; //$NON-NLS-1$
	/** Canonical section name: one of NAME_TEXT, NAME_DATA, NAME_RODATA, or NAME_BSS */
	static final String PROPERTY_NAME = "name"; //$NON-NLS-1$
	
	/* TODO: not used
	static final String PROPERTY_READABLE = "readable";
	static final String PROPERTY_WRITABLE = "writable";
	static final String PROPERTY_EXECUTABLE = "executable";
	 */
	
	static final String NAME_TEXT = ".text"; //$NON-NLS-1$
	static final String NAME_DATA = ".data"; //$NON-NLS-1$
	static final String NAME_RODATA = ".rodata"; // read only data //$NON-NLS-1$
	static final String NAME_BSS = ".bss"; // uninitialized data //$NON-NLS-1$

	/**
	 * Get the section id
	 * 
	 * @return the section id
	 */
	int getId();

	/**
	 * Get the section size
	 * 
	 * @return the section size
	 */
	long getSize();

	/**
	 * Get the base link address of the section
	 * 
	 * @return the base link address
	 */
	IAddress getLinkAddress();

	/**
	 * Get the properties of the section
	 * 
	 * @return the section properties
	 */
	Map<String, Object> getProperties();

}
