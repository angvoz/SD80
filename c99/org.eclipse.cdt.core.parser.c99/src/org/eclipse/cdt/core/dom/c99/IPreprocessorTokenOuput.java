/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.c99;

import lpg.lpgjavaruntime.IToken;


/**
 * Collects the output of the preprocessor.
 *
 * The preprocessor injects tokens into the parser via this interface.
 */
public interface IPreprocessorTokenOuput {
	
	void addToken(IToken token);
	
}
