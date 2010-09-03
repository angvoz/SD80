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
package org.eclipse.cdt.debug.edc.launch;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 2.0
 */
public interface IEDCLaunchConfigurationConstants {

	public static final String ATTR_ALBUM_FILE = "org.eclipse.cdt.debug.edc.internal.launch.snapshotAlbum"; //$NON-NLS-1$

	public static final String ATTR_USE_REMOTE_PEERS = "org.eclipse.cdt.debug.edc.useRemotePeers"; //$NON-NLS-1$

	/**
	 * @since 2.0
	 */
	public static final String ATTR_ATTACH_CONTEXT_ID = "org.eclipse.cdt.debug.edc.attachContextID"; //$NON-NLS-1$

	/**
	 * @since 2.0
	 */
	public static final String ATTR_ATTACH_CONTEXT_NAME = "org.eclipse.cdt.debug.edc.attachContextName"; //$NON-NLS-1$

	/**
	 * @since 2.0
	 */
	public static final String ATTR_IS_ONE_USE = "org.eclipse.cdt.debug.edc.isOneUse"; //$NON-NLS-1$

}
