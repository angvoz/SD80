/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;




/**
 * Interface of an object participating in reconciling.
 * @deprecated in favour of {@link org.eclipse.cdt.internal.ui.text.ICReconcilingListener}
 */
public interface IReconcilingParticipant {
	
	/**
	 * Called after reconciling has been finished.
	 */
	void reconciled(boolean SomethingHasChanged);
}
