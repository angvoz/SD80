/*******************************************************************************
 * Copyright (c) 2002, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.ui.text.c.hover;

import org.eclipse.jface.text.ITextHover;
import org.eclipse.ui.IEditorPart;

/**
 * Interface to be implemented by contributors to extension point
 * "org.eclipse.cdt.ui.textHovers". Provides a hover popup which appears on top
 * of an editor with relevant display information. If the text hover does not
 * provide information no hover popup is shown.
 * <p>
 * Clients may implement this interface.
 * </p>
 */
public interface ICEditorTextHover extends ITextHover {

	/**
	 * Sets the editor on which the hover is shown.
	 * 
	 * @param editor the editor on which the hover popup should be shown
	 */
	void setEditor(IEditorPart editor);

}
