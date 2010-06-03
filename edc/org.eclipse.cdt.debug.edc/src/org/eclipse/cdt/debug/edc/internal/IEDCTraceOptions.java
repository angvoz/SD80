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
package org.eclipse.cdt.debug.edc.internal;

/**
 * The Interface IEDCTraceOptions contains constants used to control tracing
 * options in EDC.
 * 
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IEDCTraceOptions {

	public static final String DEBUG_TRACE = "/debug";
	public static final String RUN_CONTROL_TRACE = "/debug/runControl";
	public static final String STACK_TRACE = "/debug/stack";
	public static final String EXPRESSION_PARSE_TRACE = "/debug/expressionParse";
	public static final String SYMBOL_READER_TRACE = "/debug/symbolReader";
	public static final String SYMBOL_READER_VERBOSE_TRACE = "/debug/symbolReader/verbose";
	public static final String VARIABLE_VALUE_TRACE = "/debug/variableValue";
	public static final String BREAKPOINTS_TRACE = "/debug/breakpoints";
	public static final String MEMORY_TRACE = "/debug/memory";
}
