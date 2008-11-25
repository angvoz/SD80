/*******************************************************************************
 * Copyright (c) 2000, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.osgi.util.NLS;

public final class CEditorMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.ui.editor.CEditorMessages";//$NON-NLS-1$

	private CEditorMessages() {
		// Do not instantiate
	}

	public static String AddIncludeOnSelection_description;
	public static String AddIncludeOnSelection_error_message1;
	public static String AddIncludeOnSelection_error_message3;
	public static String AddIncludeOnSelection_error_message4;
	public static String AddIncludeOnSelection_label;
	public static String AddIncludeOnSelection_tooltip;
	public static String AddIncludesOperation_description;
	public static String ShowInCView_description;
	public static String ShowInCView_label;
	public static String ShowInCView_tooltip;
	public static String OpenDeclarations_description;
	public static String OpenDeclarationsAction_dialog_title;
	public static String OpenDeclarationsAction_selectMessage;
	public static String OpenDeclarations_dialog_title;
	public static String OpenDeclarations_label;
	public static String OpenDeclarations_tooltip;
	public static String DefaultCEditorTextHover_html_name;
	public static String DefaultCEditorTextHover_html_prototype;
	public static String DefaultCEditorTextHover_html_description;
	public static String DefaultCEditorTextHover_html_includes;
	public static String CEditor_menu_folding;
	public static String EditorUtility_concatModifierStrings;
	public static String GotoMatchingBracket_label;
	public static String GotoMatchingBracket_error_invalidSelection;
	public static String GotoMatchingBracket_error_noMatchingBracket;
	public static String GotoMatchingBracket_error_bracketOutsideSelectedElement;
	public static String Scalability_message;
	public static String Scalability_info;
	public static String Scalability_reappear;
	public static String Scalability_outlineDisabled;
	public static String ToggleComment_error_title;
	public static String ToggleComment_error_message;
	public static String InactiveCodeHighlighting_job;
	public static String Reconciling_job;
	public static String SemanticHighlighting_job;
	public static String SemanticHighlighting_field;
	public static String SemanticHighlighting_staticField;
	public static String SemanticHighlighting_staticConstField;
	public static String SemanticHighlighting_methodDeclaration;
	public static String SemanticHighlighting_staticMethodInvocation;
	public static String SemanticHighlighting_localVariableDeclaration;
	public static String SemanticHighlighting_localVariable;
	public static String SemanticHighlighting_globalVariable;
	public static String SemanticHighlighting_parameterVariable;
	public static String SemanticHighlighting_method;
	public static String SemanticHighlighting_classes;
	public static String SemanticHighlighting_enums;
	public static String SemanticHighlighting_enumerator;
	public static String SemanticHighlighting_templateArguments;
	public static String SemanticHighlighting_templateParameter;
	public static String SemanticHighlighting_functionDeclaration;
	public static String SemanticHighlighting_function;
	public static String SemanticHighlighting_macroSubstitution;
	public static String SemanticHighlighting_macroDefintion;
	public static String SemanticHighlighting_typeDef;
	public static String SemanticHighlighting_namespace;
	public static String SemanticHighlighting_label;
	public static String SemanticHighlighting_problem;
	public static String SemanticHighlighting_externalSDK;
	public static String CEditor_markOccurrences_job_name;

	static {
		NLS.initializeMessages(BUNDLE_NAME, CEditorMessages.class);
	}
}