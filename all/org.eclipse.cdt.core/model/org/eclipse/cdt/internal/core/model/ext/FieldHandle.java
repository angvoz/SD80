/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core.model.ext;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;

public class FieldHandle extends CElementHandle implements org.eclipse.cdt.core.model.IField {

	private ASTAccessVisibility fVisibility;
	private boolean fIsStatic;

	public FieldHandle(ICElement parent, IField field) {
		super(parent, ICElement.C_FIELD, field.getName());
		fVisibility= getVisibility(field);
		try {
			fIsStatic= field.isStatic();
		} catch (DOMException e) {
			CCorePlugin.log(e);
			fIsStatic= false;
		}
	}

	public ASTAccessVisibility getVisibility() throws CModelException {
		return fVisibility;
	}

	public boolean isStatic() throws CModelException {
		return fIsStatic;
	}
}
