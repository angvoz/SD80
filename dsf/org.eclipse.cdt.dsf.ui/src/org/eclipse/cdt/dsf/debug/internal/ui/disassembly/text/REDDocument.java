/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.text;

import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.ITextStore;

/**
 * Standard Document implementation with REDTextStore (splice texts)
 * as text storage.
 */
public class REDDocument extends AbstractDocument {

	public REDDocument() {
		setTextStore(new REDTextStore());
		setLineTracker(new DefaultLineTracker());
		completeInitialization();
	}

	@Override
	protected void finalize() {
		dispose();
	}

	/**
	 * Free text store (delete scratchfiles).
	 */
	public void dispose() {
		ITextStore store = getStore();
		if (store instanceof REDTextStore) {
			((REDTextStore)store).dispose();
			setTextStore(new StringTextStore());
			getTracker().set(""); //$NON-NLS-1$
		}
	}

}
