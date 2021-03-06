/*******************************************************************************
 * Copyright (c) 2009, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 * Freescale Semiconductor - Refactoring and improvements
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal;

import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.debug.DebugTrace;

/**
 * Tracing of EDC code based on standard tracing mechanism in eclipse;
 * <br>see
 * <a href=http://wiki.eclipse.org/FAQ_How_do_I_use_the_platform_debug_tracing_facility%3F>
 * How do I use the Platform debug tracing facility?</a>
 */
public class EDCTrace {

	// The various tracing options. DEBUG_TRACE is a master shut-on/off valve
	public static final String DEBUG_TRACE = "/debug";
	public static final String RUN_CONTROL_TRACE = "/debug/runControl";
	public static final String STACK_TRACE = "/debug/stack";
	public static final String EXPRESSION_PARSE_TRACE = "/debug/expressionParse";
	public static final String SYMBOL_READER_TRACE = "/debug/symbolReader";
	public static final String SYMBOL_READER_VERBOSE_TRACE = "/debug/symbolReader/verbose";
	public static final String VARIABLE_VALUE_TRACE = "/debug/variableValue";
	public static final String BREAKPOINTS_TRACE = "/debug/breakpoints";
	public static final String MEMORY_TRACE = "/debug/memory";
	public static final String ACPM_TRACE = "/debug/acpm";
	public static final String PERSISTENT_CACHE_TRACE = "/debug/persistentCache";

	// In order to minimize trace overhead when tracing is off, we check these
	// "globals". They are set at plugin initialization time. Note that they do
	// not preclude dynamic toggling of the trace options. Toggling would
	// require dedicated GUI in any case. We would just have to have a pref
	// change listener that toggles the values of these fields (note that they
	// are not 'final').
	public static boolean DEBUG_TRACE_ON;
	public static boolean RUN_CONTROL_TRACE_ON;
	public static boolean STACK_TRACE_ON;
	public static boolean EXPRESSION_PARSE_TRACE_ON;
	public static boolean SYMBOL_READER_TRACE_ON;
	public static boolean SYMBOL_READER_VERBOSE_TRACE_ON;
	public static boolean VARIABLE_VALUE_TRACE_ON;
	public static boolean BREAKPOINTS_TRACE_ON;
	public static boolean MEMORY_TRACE_ON;
	public static boolean ACPM_TRACE_ON;
	public static boolean PERSISTENT_CACHE_TRACE_ON;

	/**
	 * Returns whether the specific tracing option is on. The answer is based on
	 * the real-time state of options as managed by the platform, whereas our
	 * XXXXX_ON static fields provide the answer based on the state of the
	 * options at plugin initialization time. Since we currently provide the
	 * user no way to toggle the options after launching Eclipse, use of this
	 * method is a heavy and unnecessary alternative to just checking the static
	 * field--thus the private visibility.
	 */
	private static boolean isOn(String option) {
		return "true".equals(Platform.getDebugOption(EDCDebugger.PLUGIN_ID + option)); 
	}
	
	/**
	 * Sets up static booleans at plugin startup time for efficient trace checks.
	 */
	public static void init() {
		if ("true".equals(Platform.getDebugOption(EDCDebugger.PLUGIN_ID + "/debug"))) {  //$NON-NLS-1$//$NON-NLS-2$
			DEBUG_TRACE_ON = true;
			RUN_CONTROL_TRACE_ON = isOn(EDCTrace.RUN_CONTROL_TRACE);
			STACK_TRACE_ON = isOn(EDCTrace.STACK_TRACE);
			EXPRESSION_PARSE_TRACE_ON = isOn(EDCTrace.EXPRESSION_PARSE_TRACE);
			SYMBOL_READER_TRACE_ON = isOn(EDCTrace.SYMBOL_READER_TRACE);
			SYMBOL_READER_VERBOSE_TRACE_ON = SYMBOL_READER_TRACE_ON && isOn(EDCTrace.SYMBOL_READER_VERBOSE_TRACE);
			VARIABLE_VALUE_TRACE_ON = isOn(EDCTrace.VARIABLE_VALUE_TRACE);
			BREAKPOINTS_TRACE_ON = isOn(EDCTrace.BREAKPOINTS_TRACE);
			MEMORY_TRACE_ON = isOn(EDCTrace.MEMORY_TRACE);
			ACPM_TRACE_ON = isOn(EDCTrace.ACPM_TRACE);
			PERSISTENT_CACHE_TRACE_ON = isOn(EDCTrace.PERSISTENT_CACHE_TRACE);
		}
	}

	static class NullDebugTrace implements DebugTrace {
		public void trace(String option, String message) {}
		public void trace(String option, String message, Throwable error) {}
		public void traceDumpStack(String option) {}
		public void traceEntry(String option) {}
		public void traceEntry(String option, Object methodArgument) {}
		public void traceEntry(String option, Object[] methodArguments) {}
		public void traceExit(String option) {}
		public void traceExit(String option, Object result) {}
	};

	private static DebugTrace sTrace;

	public static String fixArg(Object argument) {
		if (argument == null || sTrace instanceof NullDebugTrace)
			return null;
		return argument.toString().replaceAll("\\{", "[").replaceAll("\\}", "]");
	}

	public static String[] fixArgs(Object[] arguments) {
		if (arguments == null || sTrace instanceof NullDebugTrace)
			return null;
		String[] args = new String[arguments.length];
		for (int i = 0; i < arguments.length; i++) {
			args[i] = fixArg(arguments[i]);
		}
		return args;
	}

	public static DebugTrace getTrace() {
		if (sTrace == null) {
			EDCDebugger activator = EDCDebugger.getDefault();
			if (activator != null) {
				sTrace = activator.getTrace();
			}
			else
				sTrace = new NullDebugTrace();
		}
		return sTrace;
	}
}
