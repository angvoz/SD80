/*******************************************************************************
 * Copyright (c) 2009 Broadcom Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import org.eclipse.osgi.util.NLS;

public class HeadlessBuildMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.managedbuilder.internal.core.PluginResources"; //$NON-NLS-1$
	public static String HeadlessBuilder_16;
	public static String HeadlessBuilder_already_exists_in_workspace;
	public static String HeadlessBuilder_build_failed;
	public static String HeadlessBuilder_building_all;
	public static String HeadlessBuilder_cant_be_found;
	public static String HeadlessBuilder_clean_failed;
	public static String HeadlessBuilder_cleaning_all_projects;
	public static String HeadlessBuilder_Error;
	public static String HeadlessBuilder_invalid_argument;
	public static String HeadlessBuilder_is_not_accessible;
	public static String HeadlessBuilder_is_not_valid_in_workspace;
	public static String HeadlessBuilder_no_arguments;
	public static String HeadlessBuilder_project;
	public static String HeadlessBuilder_unknown_argument;
	public static String HeadlessBuilder_URI;
	public static String HeadlessBuilder_usage;
	public static String HeadlessBuilder_usage_build;
	public static String HeadlessBuilder_usage_clean_build;
	public static String HeadlessBuilder_usage_import;
	public static String HeadlessBuilder_Workspace;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, HeadlessBuildMessages.class);
	}

	private HeadlessBuildMessages() {
	}
}
