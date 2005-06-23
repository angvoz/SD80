/*******************************************************************************
 * Copyright (c) 2002, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.core.filebuffers.IDocumentFactory;
import org.eclipse.jface.text.IDocument;

/**
 * CDocumentFactory
 */
public class CDocumentFactory implements IDocumentFactory {
	/**
	 * 
	 */
	public CDocumentFactory() {
	}

	/*
	 * @see org.eclipse.core.filebuffers.IDocumentFactory#createDocument()
	 */
	public IDocument createDocument() {
		return new PartiallySynchronizedDocument();
	}

}
