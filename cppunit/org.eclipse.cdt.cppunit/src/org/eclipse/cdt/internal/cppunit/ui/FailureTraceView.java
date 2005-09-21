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
import java.io.StringReader;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * A view that shows a stack trace of a failed test.
 */
class FailureTraceView implements IMenuListener {
	private static final String FRAME_PREFIX= "at ";
	private Table fTable;
	private TestRunnerViewPart fTestRunner;
	private String fInputTrace;
	
	private final Image fStackIcon= TestRunnerViewPart.createImage("obj16/stkfrm_obj.gif"); //$NON-NLS-1$
	private final Image fExceptionIcon= TestRunnerViewPart.createImage("obj16/exc_catch.gif"); //$NON-NLS-1$

	public FailureTraceView(Composite parent, TestRunnerViewPart testRunner) {
		fTable= new Table(parent, SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
		fTestRunner= testRunner;
		
		fTable.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e){
				handleDoubleClick(e);
			}
		});
		
		initMenu();
		
		parent.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				disposeIcons();
			}
		});
	}
	
	void handleDoubleClick(MouseEvent e) {
		if(fTable.getSelection().length != 0) {
			Action a= createOpenEditorAction(getSelectedText());
			if (a != null)
				a.run();
		}
	}
	
	private void initMenu() {
		MenuManager menuMgr= new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(this);
		Menu menu= menuMgr.createContextMenu(fTable);
		fTable.setMenu(menu);		
	}
	
	public void menuAboutToShow(IMenuManager manager) {
		if (fTable.getSelectionCount() > 0) {
			Action a= createOpenEditorAction(getSelectedText());
			if (a != null)
				manager.add(a);
		}
		manager.add(new CopyTraceAction(FailureTraceView.this));
	}

	public String getTrace() {
		return fInputTrace;
	}
	
	private String getSelectedText() {
		return fTable.getSelection()[0].getText();
	}				

	private Action createOpenEditorAction(String traceLine)
	{
		if(traceLine.startsWith("File "))
		{
			String fileName=traceLine.substring("File ".length(),traceLine.indexOf(':'));
			String lineNumber=traceLine.substring(traceLine.indexOf(':')+1,traceLine.length());
			int line=Integer.valueOf(lineNumber).intValue();
			if(fileName.equals("Unknown")) return null;
			return new OpenEditorAtLineAction(fTestRunner, fileName, line);
		}
		return null;
	}
	
	void disposeIcons(){
		if (fExceptionIcon != null && !fExceptionIcon.isDisposed()) 
			fExceptionIcon.dispose();
		if (fStackIcon != null && !fStackIcon.isDisposed()) 
			fStackIcon.dispose();
	}
	
	/**
	 * Returns the composite used to present the trace
	 */
	public Composite getComposite(){
		return fTable;
	}
	
	/**
	 * Refresh the table from the the trace.
	 */
	public void refresh() {
		updateTable(fInputTrace);
	}
	
	/**
	 * Shows a TestFailure
	 */
	public void showFailure(String trace) {	
		if (fInputTrace == trace)
			return;
		fInputTrace= trace;
		updateTable(trace);
	}

	private void updateTable(String trace) {
		if(trace == null || trace.trim().equals("")) { //$NON-NLS-1$
			clear();
			return;
		}
		trace= trace.trim();
		fTable.setRedraw(false);
		fTable.removeAll();
		fillTable(filterStack(trace));
		fTable.setRedraw(true);
	}

	protected void fillTable(String trace) {
		StringReader stringReader= new StringReader(trace);
		BufferedReader bufferedReader= new BufferedReader(stringReader);
		String line;

		try {	
			// first line contains the thrown exception
			line= bufferedReader.readLine();
			if (line == null)
				return;
				
			TableItem tableItem= new TableItem(fTable, SWT.NONE);
			String itemLabel= line.replace('\t', ' ');
			tableItem.setText(itemLabel);
			tableItem.setImage(fExceptionIcon);
			
			// the stack frames of the trace
			while ((line= bufferedReader.readLine()) != null) {
				itemLabel= line.replace('\t', ' ');
				tableItem= new TableItem(fTable, SWT.NONE);
				// heuristic for detecting a stack frame - works for JDK
				if ((itemLabel.indexOf(" at ") >= 0)) { //$NON-NLS-1$
					tableItem.setImage(fStackIcon);
				}
				tableItem.setText(itemLabel);
			}
		} catch (IOException e) {
			TableItem tableItem= new TableItem(fTable, SWT.NONE);
			tableItem.setText(trace);
		}			
	}
	
	/**
	 * Shows other information than a stack trace.
	 */
	public void setInformation(String text) {
		clear();
		TableItem tableItem= new TableItem(fTable, SWT.NONE);
		tableItem.setText(text);
	}
	
	/**
	 * Clears the non-stack trace info
	 */
	public void clear() {
		fTable.removeAll();
		fInputTrace= null;
	}
	
	private String filterStack(String stackTrace) {	
//		if (!CppUnitPreferencePage.getFilterStack() || stackTrace == null) 
//			return stackTrace;
//			
//		StringWriter stringWriter= new StringWriter();
//		PrintWriter printWriter= new PrintWriter(stringWriter);
//		StringReader stringReader= new StringReader(stackTrace);
//		BufferedReader bufferedReader= new BufferedReader(stringReader);	
//			
//		String line;
//		String[] patterns= CppUnitPreferencePage.getFilterPatterns();
//		try {	
//			while ((line= bufferedReader.readLine()) != null) {
//				if (!filterLine(patterns, line))
//					printWriter.println(line);
//			}
//		} catch (IOException e) {
//			return stackTrace; // return the stack unfiltered
//		}
//		return stringWriter.toString();
		return stackTrace;
	}
	
	private boolean filterLine(String[] patterns, String line) {
		String pattern;
		int len;
		for (int i= (patterns.length - 1); i >= 0; --i) {
			pattern= patterns[i];
			len= pattern.length() - 1;
			if (pattern.charAt(len) == '*') {
				//strip trailing * from a package filter
				pattern= pattern.substring(0, len);
			} else if (Character.isUpperCase(pattern.charAt(0))) {
				//class in the default package
				pattern= FRAME_PREFIX + pattern + '.';
			} else {
				//class names start w/ an uppercase letter after the .
				final int lastDotIndex= pattern.lastIndexOf('.');
				if ((lastDotIndex != -1) && (lastDotIndex != len) && Character.isUpperCase(pattern.charAt(lastDotIndex + 1)))
					pattern += '.'; //append . to a class filter
			}

			if (line.indexOf(pattern) > 0)
				return true;
		}		
		return false;
	}
}
