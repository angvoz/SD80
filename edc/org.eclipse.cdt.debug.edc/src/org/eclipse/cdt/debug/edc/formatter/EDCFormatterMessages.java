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
package org.eclipse.cdt.debug.edc.formatter;

import org.eclipse.osgi.util.NLS;

public class EDCFormatterMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.debug.edc.formatter.EDCFormatterMessages"; //$NON-NLS-1$
	public static String FormatUtils_CannotReadMemory;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, EDCFormatterMessages.class);
	}

	private EDCFormatterMessages() {
	}
}
