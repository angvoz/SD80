/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.ui.text;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IProject;

/**
 * Invocation context for the ICCompletionContributor
 */
public interface ICCompletionInvocationContext {
	
	/**
	 * @return the project
	 */
	IProject getProject();

	/**
	 * @return ITranslationUnit or null
	 */
	ITranslationUnit getTranslationUnit();

}
