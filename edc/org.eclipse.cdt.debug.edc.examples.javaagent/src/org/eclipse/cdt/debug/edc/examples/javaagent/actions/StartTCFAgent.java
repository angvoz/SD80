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
package org.eclipse.cdt.debug.edc.examples.javaagent.actions;

import java.io.IOException;

import org.eclipse.cdt.debug.edc.examples.javaagent.TCFAgentInJava;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.jface.dialogs.MessageDialog;

/**
 * The action to start/stop our TCF agent in the plugin.
 * 
 * The action proxy will be created by the workbench and shown in the UI. When
 * the user tries to use the action, this delegate will be created and execution
 * will be delegated to it.
 * 
 * @see IWorkbenchWindowActionDelegate
 */
public class StartTCFAgent implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;

	/**
	 * The constructor.
	 */
	public StartTCFAgent() {
	}

	/**
	 * The action has been activated. The argument of the method represents the
	 * 'real' action sitting in the workbench UI.
	 * 
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {

		String msg;

		if (!TCFAgentInJava.getInstance().isRunning()) {
			try {
				TCFAgentInJava.getInstance().start("Agent_InPlugin");

				msg = "The agent is started. Please run any agent test.";

				action.setText("Stop TCF Agent");
				action.setToolTipText("Stop TCF Agent");
			} catch (IOException e) {
				msg = "Fail to start the agent. IOException: " + e.getMessage();
			}
		} else {
			try {
				TCFAgentInJava.getInstance().stop();

				msg = "The agent is stoped.";

				action.setText("Start TCF Agent");
				action.setToolTipText("Start TCF Agent");
			} catch (IOException e) {
				msg = "Fail to stop the agent. IOException: " + e.getMessage();
			}
		}

		MessageDialog.openInformation(window.getShell(), "TCF Agent In Java Plugin", msg);
	}

	/**
	 * Selection in the workbench has been changed. We can change the state of
	 * the 'real' action here if we want, but this can only happen after the
	 * delegate has been created.
	 * 
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system resources we previously
	 * allocated.
	 * 
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to be able to provide parent shell
	 * for the message dialog.
	 * 
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}