/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QnX Software System
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.template.c;

import org.eclipse.osgi.util.NLS;

public final class TemplateMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.corext.template.c.TemplateMessages";//$NON-NLS-1$

	private TemplateMessages() {
		// Do not instantiate
	}

	public static String CContextType_variable_description_file;
	public static String CContextType_variable_description_enclosing_method;
	public static String CContextType_variable_description_enclosing_project;
	public static String CContextType_variable_description_enclosing_method_arguments;
	public static String CContextType_variable_description_return_type;
	public static String CContextType_variable_description_todo;
	
	public static String CodeTemplateContextType_variable_description_todo;
	public static String CodeTemplateContextType_variable_description_typedeclaration;
	public static String CodeTemplateContextType_variable_description_fieldname;
	public static String CodeTemplateContextType_variable_description_fieldtype;
	public static String CodeTemplateContextType_variable_description_typecomment;
	public static String CodeTemplateContextType_variable_description_enclosingtype;
	public static String CodeTemplateContextType_variable_description_typename;
	public static String CodeTemplateContextType_variable_description_include_guard_symbol;
	public static String CodeTemplateContextType_variable_description_enclosingmethod;
	public static String CodeTemplateContextType_variable_description_bodystatement;
	public static String CodeTemplateContextType_variable_description_returntype;
	public static String CodeTemplateContextType_variable_description_filecomment;
	public static String CodeTemplateContextType_validate_invalidcomment;
	public static String CodeTemplateContextType_csource_name;
	public static String CodeTemplateContextType_cheader_name;
	public static String CodeTemplateContextType_cppsource_name;
	public static String CodeTemplateContextType_cppheader_name;
	public static String CodeTemplateContextType_asmsource_name;
	
	public static String FileTemplateContextType__variable_description_eclipse;
	public static String FileTemplateContextType_validate_unknownvariable;
	public static String FileTemplateContextType_validate_missingvariable;
	public static String FileTemplateContextType_variable_description_date;
	public static String FileTemplateContextType_variable_description_filename;
	public static String FileTemplateContextType_variable_description_filebase;
	public static String FileTemplateContextType_variable_description_fileloc;
	public static String FileTemplateContextType_variable_description_filepath;
	public static String FileTemplateContextType_variable_description_projectname;

	static {
		NLS.initializeMessages(BUNDLE_NAME, TemplateMessages.class);
	}
}