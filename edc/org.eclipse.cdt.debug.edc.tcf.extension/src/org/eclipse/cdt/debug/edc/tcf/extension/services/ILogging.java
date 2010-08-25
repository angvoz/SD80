/**
* Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies).
* All rights reserved.
* This component and the accompanying materials are made available
* under the terms of the License "Eclipse Public License v1.0"
* which accompanies this distribution, and is available
* at the URL "http://www.eclipse.org/legal/epl-v10.html".
*
* Initial Contributors:
* Nokia Corporation - initial contribution.
*
* Contributors:
*
* Description: 
*
*/


package org.eclipse.cdt.debug.edc.tcf.extension.services;

import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IToken;

/**
 * Interface to allow agents to send log messages to the client
 */
public interface ILogging extends IService {

	/**
	 * Name of the service
	 */
	String NAME = "Logging";

	/**
     * Clients implement LogListener interface to receive log messages.
     */
    interface LogListener {
    	
    	/**
    	 * Callback to receive a log message.
    	 * @param msg String
    	 */
    	void write(String msg);
    	
    	/**
    	 * Callback to receive a log message with an implicit end line.
    	 * @param msg String
    	 */
    	void writeln(String msg);

		/**
		 * Callback to receive a log message with an implicit end line.
		 * @param int severity - level @see IStatus
		 * @param summary String - short description for main part of dialog
		 * @param details String - detailed description for "Details>>" button
		 * @since 2.0
		 */
		void dialog(int severity, String summary, String details);
    }

    /**
     * Clients add a listener to log messages by id.
     * @param id String
     * @param listener LogListener
     * @param done DoneAddListener
     * @return IToken
     */
    IToken addListener(String id, LogListener listener, DoneAddListener done);

    /**
     * Callback interface for addListener command.
     */
    interface DoneAddListener {
        void doneAddListener(IToken token, Exception error);
    }

    /**
     * Clients remove a listener to log messages by id.
     * @param id String
     * @param listener LogListener
     * @param done DoneSubscribe
     * @return IToken
     */
    IToken removeListener(String id, LogListener listener, DoneRemoveListener done);

    /**
     * Call back interface for removeListener command.
     */
    interface DoneRemoveListener {
        void doneRemoveListener(IToken token, Exception error);
    }

}
