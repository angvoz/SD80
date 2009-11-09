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

package org.eclipse.cdt.debug.edc.agent.gdbserver;

/**
 * UNIX signals.
 * 
 * @see <a href="http://www.tech-faq.com/unix-signals.shtml">What are Unix
 *      signals?</a>
 */
public class UnixSignal {
	static String sNameStringPairs[] = { "0", "<invalid>", "SIGHUP", "Hangup", "SIGINT", "Interrupt", "SIGQUIT",
			"Quit", "SIGILL", "Illegal instruction", "SIGTRAP", "Trace/breakpoint trap", "SIGABRT", "Abort", "SIGEMT",
			"Emulation instruction executed", "SIGFPE", "Floating-point exception", "SIGKILL", "Kill", "SIGBUS",
			"Bus error", "SIGSEGV", "Segmentation violation", "SIGSYS", "Bad arguements to system call", "SIGPIPE",
			"Broken pipe", "SIGALRM", "Alarm clock", "SIGTERM", "Software termination", "SIGURG",
			"Urgent condition on I/O channel", "SIGSTOP", "Stop signal not from terminal", "SIGTSTP",
			"Stop signal from terminal (user)", "SIGCONT", "Continued", "SIGCHLD", "Child stop or exit", "SIGTTIN",
			"Read on terminal by background process", "SIGTTOU", "Write to terminal by background process", "SIGIO",
			"I/O possible on a descriptor", "SIGXCPU", "CPU time limit exceeded", "SIGXFSZ",
			"File size limit exceeded", "SIGVTALRM", "Virtual timer expired", "SIGPROF", "Profiling timer expired",
			"SIGWINCH", "Window size changed",

	};

	static public String getSignalName(int signal) {
		if (signal >= 0 && signal < sNameStringPairs.length / 2)
			return sNameStringPairs[signal * 2];
		else
			return "Signal #" + signal;
	}

	static public String getSignalString(int signal) {
		if (signal >= 0 && signal < sNameStringPairs.length / 2)
			return sNameStringPairs[signal * 2 + 1];
		else
			return "<unknown>";
	}
}
