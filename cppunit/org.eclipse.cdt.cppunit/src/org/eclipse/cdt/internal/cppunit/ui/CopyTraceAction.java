/*
 * (c) Copyright IBM Corp. 2003.
 * Created on 27 févr. 03
 * All Rights Reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 *
 * Contributors:
 * IBM Software Group - Rational Software
 */

package org.eclipse.cdt.internal.cppunit.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;

import org.eclipse.jface.action.Action;

/**
 * Copies a test failure stack trace to the clipboard.
 */
public class CopyTraceAction extends Action {
	private FailureTraceView fView;
	
	/**
	 * Constructor for CopyTraceAction.
	 */
	public CopyTraceAction(FailureTraceView view) {
		super(CppUnitMessages.getString("CopyTrace.action.label"));  //$NON-NLS-1$
		fView= view;
	}

	/*
	 * @see IAction#run()
	 */
	public void run() {
		String trace= fView.getTrace();
		if (trace == null)
			trace= ""; //$NON-NLS-1$
		
		TextTransfer plainTextTransfer = TextTransfer.getInstance();
		Clipboard clipboard= new Clipboard(fView.getComposite().getDisplay());		
		clipboard.setContents(
			new String[]{ convertLineTerminators(trace) }, 
			new Transfer[]{ plainTextTransfer });
		clipboard.dispose();
	}

	private String convertLineTerminators(String in) {
		StringWriter stringWriter= new StringWriter();
		PrintWriter printWriter= new PrintWriter(stringWriter);
		StringReader stringReader= new StringReader(in);
		BufferedReader bufferedReader= new BufferedReader(stringReader);		
		String line;
		try {
			while ((line= bufferedReader.readLine()) != null) {
				printWriter.println(line);
			}
		} catch (IOException e) {
			return in; // return the trace unfiltered
		}
		return stringWriter.toString();
	}
}
