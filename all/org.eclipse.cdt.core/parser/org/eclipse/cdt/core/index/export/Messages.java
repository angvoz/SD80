/*******************************************************************************
 * Copyright (c) 2007, 2009 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.core.index.export;

import org.eclipse.osgi.util.NLS;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.core.index.export.messages"; //$NON-NLS-1$
	public static String CLIUtil_OptionParametersMismatch;
	public static String ExternalContentPEM_LocationToIndexNonExistent;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
