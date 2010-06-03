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
package org.eclipse.cdt.debug.edc.agent.gdbserver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class provides support for converting back and forth from lists of
 * arguments to quoted command-line strings for shells.
 * <p>
 * This assumes Unix semantics.
 * <p>
 * (Originally from mica project)
 *
 */
public class CommandLineArguments {
	private static final boolean IS_WIN32 = File.separatorChar == '\\';
	
	/** Construct with a command line (with spaces and quoting) */
	public static List<String> createFromCommandLine(String cmdLine) {
		List<String> arguments = new ArrayList<String>();
		StringBuilder curArg = new StringBuilder();
		char quoting = 0;
		char[] cmdLineChars = cmdLine.toCharArray();
		for (int idx = 0; idx < cmdLineChars.length; ) {
			char ch = cmdLineChars[idx++];
			if (ch == '\\' && idx < cmdLineChars.length) {
				ch = cmdLineChars[idx++];
			} else {
				if (quoting != 0) {
					if (ch == quoting) {
						quoting = 0;
						continue;
					} else {
						curArg.append(ch);
						continue;
					}
				} 
				if (ch == '\'' || ch == '\"') {
					quoting = ch;
					continue;
				}
				if (ch == ' ') {
					if (curArg.length() > 0) {
						arguments.add(curArg.toString());
					}
					curArg.setLength(0);
					continue;
				}
			}
			curArg.append(ch);
		}
		if (curArg.length() > 0) {
			arguments.add(curArg.toString());
		}
		return arguments;
	}
	
	/** Convert argument list to a command line with spaces between quoted arguments. */
	public static String toCommandLine(List<String> arguments) {
		return toString(arguments, " ", true);
	}

	/** Convert argument list to a command line with the given program as the first argument. */
	public static String toCommandLine(String program, List<String> arguments) {
		List<String> copy = new ArrayList<String>(arguments);
		copy.add(0, program);
		return toCommandLine(copy);
	}
	
	/** Convert to a string with the space separator and quoting behavior. **/
	public static String toString(List<String> arguments) {
		return toString(arguments, " ", true);
	}

	/** Convert to a string with the given separator and optional quoting behavior. **/
	public static String toString(List<String> arguments, String separator, boolean quote) {
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		for (String arg : arguments) {
			if (first)
				first = false;
			else
				builder.append(separator);

			// don't handle general shell metacharacters; they may actually be part of the command!
			if (quote && (arg.contains("\"") || arg.contains(separator)) 
					&& (IS_WIN32 || (!arg.matches("'.*'") && !arg.matches("`.*`")))) {
				builder.append('"');
				// this takes a regex, so we're really just taking \ -> \\ here
				arg = arg.replaceAll("\\\\", "\\\\\\\\");
				// again, regex multiplication: really replacing " with \"
				builder.append(arg.replaceAll("\"", "\\\\\""));
				builder.append('"');
			} else {
				builder.append(arg);
			}
		}
		return builder.toString();
	}

	/** Add all the given arguments to the end of the arguments list. */
	public static List<String> append(List<String> arguments, String[] args) {
		for (String argument : args)
			arguments.add(argument);
		return arguments;
	}

	/** Escape a string for a quoting context */
	public static String escape(String arg) {
		return escape(arg, true);
	}

	/** Escape a string for a quoting context */
	public static String escape(String arg, boolean singleQuoteToo) {
		arg = arg.replaceAll("\\\\", "\\\\\\\\"); 
		arg = arg.replaceAll("\"", "\\\\\"");
		if (singleQuoteToo)
			arg = arg.replaceAll("'", "\\\\'");
		if (!IS_WIN32) {
			arg = arg.replaceAll("\\$", "\\\\\\\\\\\\\\$");
			arg = arg.replaceAll("\\%", "\\\\%");
		}
		return arg;
	}

	/**
	 * Convert the given command line for invocation as a script, by
	 * inserting /bin/sh -c in front and packaging all the existing arguments
	 * into one quoted and escaped string.
	 * @param shellName name of shell
	 * @param cmdLine script name and arguments
	 * @return new command line launching a shell
	 */
	public static List<String> wrapScriptCommandLineForUnixShell(String shellName, List<String> cmdLine) {
		String command = CommandLineArguments.toCommandLine(cmdLine);
		List<String> scriptCmdLine = new ArrayList<String>();
		scriptCmdLine.add(0, shellName);
		scriptCmdLine.add(1, "-c");
		scriptCmdLine.add(command);
		return scriptCmdLine;
	}

	/**
	 * Wrap a command launch in a standard shell command line (presumed to be on
	 * Unix).
	 * @param shellName the name of the shell, e.g., "/bin/sh"
	 * @param commandLineList command name and arguments
	 * @param environment map of variable to value, or <code>null</code>
	 * @param workingDirectory directory in which to launch, or <code>null</code>
	 * @return String which can executed in a shell 
	 */
	public static String wrapStandardUnixShellCommandLine(
			String shellName,
			List<String> commandLineList,
			Map<String, String> environment,
			File workingDirectory) {
		String commandLine = toCommandLine(commandLineList);
		
		if (environment != null) {
			StringBuilder envCmdLine = new StringBuilder();
			
			// Defines must be defined on the same line as the following command like this:
			//
			//		FOO=bar BAR=baz BAZ=foo ./command
			//
			// or else they will not be visible (and we don't want to waste space exporting them
			// nor start a new shell).
			
			for (Map.Entry<String, String> entry : environment.entrySet()) {
				envCmdLine.append(entry.getKey());
				envCmdLine.append('=');
				envCmdLine.append("\"" + escape(entry.getValue(), false) +  "\"");
				envCmdLine.append(' ');
			}
			
			commandLine = envCmdLine + commandLine;
		}
	
		if (workingDirectory != null) {
			commandLine = "cd \"" + workingDirectory.getAbsolutePath() + "\" && " + commandLine;
		}
		
		commandLine = shellName + " -c \"" + escape(commandLine, false) + "\""; 
		return commandLine;
	}

	
}