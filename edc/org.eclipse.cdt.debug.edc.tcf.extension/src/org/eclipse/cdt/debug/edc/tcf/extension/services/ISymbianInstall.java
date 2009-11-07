/*******************************************************************************
 * Copyright (c) 2009 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.edc.tcf.extension.services;

import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IToken;

/**
 * The Symbian install service can be used to install executables on Symbian
 * devices
 */
public interface ISymbianInstall extends IService {

	/**
	 * Name of the service
	 */
	String NAME = "SymbianInstall";

	interface DoneInstall {

		/**
		 * Called when install operation is done
		 * 
		 * @param token
		 *            IToken
		 * @param error
		 *            Exception
		 */
		void doneInstall(IToken token, Exception error);
	}

	/**
	 * Install the file at targetFilePath using the install drive without
	 * showing UI on the device.
	 * 
	 * @param targetFilePath
	 *            String
	 * @param installDrive
	 *            char
	 * @param done
	 *            DoneInstall
	 * @return IToken
	 */
	IToken install(String targetFilePath, char installDrive, DoneInstall done);

	/**
	 * Install the file at targetFilePath using the UI on the device to specify
	 * the drive.
	 * 
	 * @param targetFilePath
	 *            String
	 * @param done
	 *            DoneInstall
	 * @return IToken
	 */
	IToken installWithUI(String targetFilePath, DoneInstall done);

}
