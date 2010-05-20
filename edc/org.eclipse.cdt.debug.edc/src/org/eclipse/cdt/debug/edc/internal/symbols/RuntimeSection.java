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
package org.eclipse.cdt.debug.edc.internal.symbols;

import java.text.MessageFormat;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;

public class RuntimeSection implements IRuntimeSection {

	private ISection section;
	// the relocated address of the section at runtime
	private IAddress runtimeAddress;

	public RuntimeSection(ISection section) {
		this.section = section;
		// the section may not get relocted, so set the runtime
		// address to be the link address
		runtimeAddress = section.getLinkAddress();
	}

	public int getId() {
		return section.getId();
	}

	public long getSize() {
		return section.getSize();
	}

	public IAddress getLinkAddress() {
		return section.getLinkAddress();
	}

	public Map<String, Object> getProperties() {
		return section.getProperties();
	}

	public IAddress getRuntimeAddress() {
		return runtimeAddress;
	}

	public void relocate(IAddress runtimeAddress) {
		this.runtimeAddress = runtimeAddress;
	}

	@Override
	public String toString() {
		return MessageFormat.format("[sectionID={0}, link address={1}, runtime address={2}]", getId(), getLinkAddress().toHexAddressString(), //$NON-NLS-1$
				runtimeAddress.toHexAddressString());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RuntimeSection other = (RuntimeSection) obj;
		if (!getLinkAddress().equals(other.getLinkAddress()))
			return false;
		if (!getRuntimeAddress().equals(other.getRuntimeAddress()))
			return false;
		if (getId() != other.getId())
			return false;
		return true;
	}

}
