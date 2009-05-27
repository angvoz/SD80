/*******************************************************************************
 *  Copyright (c) 2004, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *  IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.dialogs;

/**
 * Reusing AbstractGNUBinaryParserPage.
 * New class is required for the algorithm in method performApply.
 * Must implement getRealBinaryParserPage method. 
 * 
 * @author vhirsl
 * @noextend This class is not intended to be subclassed by clients.
 */
public class GNUElfBinaryParserPage extends AbstractGNUBinaryParserPage {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.AbstractGNUBinaryParserPage#getRealBinaryParserPage()
	 */
	@Override
	protected AbstractGNUBinaryParserPage getRealBinaryParserPage() {
		return this;
	}

}
