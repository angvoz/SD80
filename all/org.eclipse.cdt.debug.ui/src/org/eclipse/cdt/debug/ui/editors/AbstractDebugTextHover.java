/*******************************************************************************
 * Copyright (c) 2000, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Nokia - Refactored from DebugTextHover to remove CDI dependency
 *     
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.editors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.debug.internal.ui.CDebugUIUtils;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.ui.text.c.hover.ICEditorTextHover;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;

/* The class LanguageOperators protects some language specific
 * operator information used by the DebugTextHover class.
 */

class LanguageOperators {
	public String getAssignmentOperator() {
		return "="; //$NON-NLS-1$
	}

	public String getGreaterThanEqualToOperator() {
		return ">="; //$NON-NLS-1$
	}

	public String getEqualToOperator() {
		return "=="; //$NON-NLS-1$
	}

	public String getNotEqualToOperator() {
		return "!="; //$NON-NLS-1$
	}

	public String getLessThenEqualToOperator() {
		return "<="; //$NON-NLS-1$
	}

	public String getValueChangeOperatorsRegex() {
		return "(\\+\\+)|(\\-\\-)|(\\+\\=)|" //$NON-NLS-1$
				+ "(\\-\\=)|(\\*\\=)|(/\\=)|(\\&\\=)" //$NON-NLS-1$
				+ "(\\%\\=)|(\\^\\=)|(\\|\\=)|(\\<\\<\\=)|(\\>\\>\\=)"; //$NON-NLS-1$
	}

	public String getEqualToOperatorsRegex() {
		return "\\=\\=|\\<\\=|\\>\\=|!\\="; //$NON-NLS-1$
	}

	public String getIdentifierRegex() {
		return "[_A-Za-z][_A-Za-z0-9]*"; //$NON-NLS-1$
	}
}

/**
 * The text hovering support for C/C++ debugger.
 * 
 * @since 6.1
 */

public abstract class AbstractDebugTextHover implements ICEditorTextHover, ITextHoverExtension,
		ISelectionListener, IPartListener {

	static final private int MAX_HOVER_INFO_SIZE = 100;

	private ISelection fSelection = null;

	private IEditorPart fEditor;

	protected abstract boolean canEvaluate();
	
	protected abstract String evaluateExpression(String expression);
	
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		if (canEvaluate()) {
			try {
				IDocument document = textViewer.getDocument();
				if (document == null)
					return null;
				String expression = document.get(hoverRegion.getOffset(),
						hoverRegion.getLength());
				if (expression == null)
					return null;
				expression = expression.trim();
				if (expression.length() == 0)
					return null;
				LanguageOperators operatorsObj = new LanguageOperators();

				Pattern pattern = Pattern.compile(operatorsObj
						.getValueChangeOperatorsRegex());
				Matcher matcher = pattern.matcher(expression);

				boolean match_found = matcher.find();
				// Get matching string
				// If the expression has some operators which can change the
				// value of a variable, that expresssion should not be
				// evaluated.
				if (match_found) {
					return null;
				}
				pattern = Pattern.compile(operatorsObj
						.getEqualToOperatorsRegex());
				String[] tokens = pattern.split(expression);
				for (int i = 0; i < tokens.length; i++) {
					//If the expression contains assignment operator that
					// can change the value of a variable, the expression
					// should not be evaluated.
					if (tokens[i].indexOf(operatorsObj
							.getAssignmentOperator()) != -1)
						return null;
				}
				//Supressing function calls from evaluation.
				String functionCallRegex = operatorsObj.getIdentifierRegex() + "\\s*\\("; //$NON-NLS-1$
				pattern = Pattern.compile(functionCallRegex);
				matcher = pattern.matcher(expression);
				match_found = matcher.find();
				if (match_found) {
					return null;
				}
				StringBuffer buffer = new StringBuffer();
				String result = evaluateExpression(expression);
				if (result == null)
					return null;
				try {
					appendVariable(buffer, makeHTMLSafe(expression), makeHTMLSafe(result.trim()));
				} catch (DebugException x) {
					CDebugUIPlugin.log(x);
				}
				if (buffer.length() > 0) {
					return buffer.toString();
				}
			} catch (BadLocationException x) {
				CDebugUIPlugin.log(x);
			}
		}
		return null;
	}

	public IRegion getHoverRegion(ITextViewer viewer, int offset) {
		/*
		 * Point selectedRange = viewer.getSelectedRange(); if ( selectedRange.x >= 0 && selectedRange.y > 0 && offset >= selectedRange.x && offset <=
		 * selectedRange.x + selectedRange.y ) return new Region( selectedRange.x, selectedRange.y );
		 */
		if (viewer != null)
			return CDebugUIUtils.findWord(viewer.getDocument(), offset);
		return null;
	}

	/**
	 * Append HTML for the given variable to the given buffer
	 */
	private static void appendVariable(StringBuffer buffer, String expression, String value) throws DebugException {
		if (value.length() > MAX_HOVER_INFO_SIZE)
			value = value.substring(0, MAX_HOVER_INFO_SIZE) + " ..."; //$NON-NLS-1$
		buffer.append("<p>"); //$NON-NLS-1$
		buffer.append("<pre>").append(expression).append("</pre>"); //$NON-NLS-1$ //$NON-NLS-2$
		buffer.append(" = "); //$NON-NLS-1$
		buffer.append("<b><pre>").append(value).append("</pre></b>"); //$NON-NLS-1$ //$NON-NLS-2$
		buffer.append("</p>"); //$NON-NLS-1$
	}

	public void setEditor(IEditorPart editor) {
		if (editor != null) {
			fEditor = editor;
			final IWorkbenchPage page = editor.getSite().getPage();
			page.addSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
			page.addPartListener(this);
			// initialize selection
			Runnable r = new Runnable() {

				public void run() {
					fSelection = page
							.getSelection(IDebugUIConstants.ID_DEBUG_VIEW);
				}
			};
			CDebugUIPlugin.getStandardDisplay().asyncExec(r);
		}
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		fSelection = selection;
	}

	public void partActivated(IWorkbenchPart part) {
	}

	public void partBroughtToTop(IWorkbenchPart part) {
	}

	public void partClosed(IWorkbenchPart part) {
		if (part.equals(fEditor)) {
			IWorkbenchPage page = fEditor.getSite().getPage();
			page.removeSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
			page.removePartListener(this);
			fSelection = null;
			fEditor = null;
		}
	}

	public void partDeactivated(IWorkbenchPart part) {
	}

	public void partOpened(IWorkbenchPart part) {
	}

	public IInformationControlCreator getHoverControlCreator() {
		return null;
	}

	protected ISelection getSelection() {
	    return fSelection;
	}

	protected IAdaptable getSelectionAdaptable() {
        if (fSelection instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection) fSelection;
            if (selection.size() == 1) {
                Object element = selection.getFirstElement();
                if (element instanceof IAdaptable) {
                    return (IAdaptable) element;
                }
            }
        }
        return null;
    }


	protected IEditorPart getEditor() {
	    return fEditor;
	}
	
	/**
	 * Replace any characters in the given String that would confuse an HTML parser with their escape sequences.
	 */
	private static String makeHTMLSafe(String string) {
		StringBuffer buffer = new StringBuffer(string.length());
		for (int i = 0; i != string.length(); i++) {
			char ch = string.charAt(i);
			switch (ch) {
			case '&':
				buffer.append("&amp;"); //$NON-NLS-1$
				break;
			case '<':
				buffer.append("&lt;"); //$NON-NLS-1$
				break;
			case '>':
				buffer.append("&gt;"); //$NON-NLS-1$
				break;
			default:
				buffer.append(ch);
				break;
			}
		}
		return buffer.toString();
	}
}
