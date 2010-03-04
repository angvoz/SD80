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

import org.eclipse.cdt.debug.edc.symbols.IUnmangler;

/**
 * This exception is thrown by {@link IUnmangler#unmangle(String)}
 * when decoding partially or fully fails.  A portion of the
 * unmangled content may be retrieved.
 */
public class UnmanglingException extends Exception {

	private static final long serialVersionUID = 3469819721317573378L;
	private final String partialUnmangling;

	public UnmanglingException(String message, String partialUnmangling) {
		super(message);
		this.partialUnmangling = partialUnmangling;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Throwable#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + ": got " + partialUnmangling;
	}
	
	public String getPartialUnmangling() {
		return partialUnmangling;
	}
}
