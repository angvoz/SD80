/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.core.pdom.messages"; //$NON-NLS-1$
	public static String Checksums_taskComputeChecksums;
	public static String PDOMManager_ClosePDOMJob;
	public static String PDOMManager_creationOfIndexInterrupted;
	public static String PDOMManager_ExistingFileCollides;
	public static String PDOMManager_indexMonitorDetail;
	public static String PDOMManager_notifyJob_label;
	public static String PDOMManager_notifyTask_message;
	public static String PDOMManager_StartJob_name;
	public static String PDOMWriter_errorResolvingName;
	public static String PDOMWriter_errorWhileParsing;
	public static String TeamPDOMExportOperation_errorCreateArchive;
	public static String TeamPDOMExportOperation_errorCreatingTempFile;
	public static String TeamPDOMExportOperation_errorWriteTempFile;
	public static String TeamPDOMExportOperation_taskExportIndex;
	public static String AbstractIndexerTask_parsingFileTask;
	public static String AbstractIndexerTask_errorWhileParsing;
	public static String AbstractIndexerTask_tooManyIndexProblems;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
