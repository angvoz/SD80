/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ffs.internal.ui.add;

import org.eclipse.ui.PlatformUI;

/**
 * Help context ids for the data transfer wizards.
 * <p>
 * This interface contains constants only; it is not intended to be implemented
 * or extended.
 * </p>
 * 
 */
public interface IDataTransferHelpContextIds {
    public static final String PREFIX = PlatformUI.PLUGIN_ID + "."; //$NON-NLS-1$

    // Wizard pages
    public static final String FILE_SYSTEM_EXPORT_WIZARD_PAGE = PREFIX
            + "file_system_export_wizard_page"; //$NON-NLS-1$

    public static final String FILE_SYSTEM_IMPORT_WIZARD_PAGE = PREFIX
            + "file_system_import_wizard_page"; //$NON-NLS-1$

    public static final String ZIP_FILE_EXPORT_WIZARD_PAGE = PREFIX
            + "zip_file_export_wizard_page"; //$NON-NLS-1$

    public static final String ZIP_FILE_IMPORT_WIZARD_PAGE = PREFIX
            + "zip_file_import_wizard_page"; //$NON-NLS-1$
}
