/*******************************************************************************
 * Copyright (c) 2009, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.services.dsf;

import org.eclipse.osgi.util.NLS;

public class EDCServicesMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.debug.edc.internal.services.dsf.EDCServicesMessages"; //$NON-NLS-1$

	public static String Expressions_CannotCastOutsideFrame;

	public static String Expressions_CannotModifyCompositeValue;

	public static String Expressions_CannotParseExpression;

	public static String Expressions_ErrorInVariableFormatter;

	public static String Expressions_ExpressionNoLocation;

	public static String Expressions_SyntaxError;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, EDCServicesMessages.class);
	}

	private EDCServicesMessages() {
	}
}
