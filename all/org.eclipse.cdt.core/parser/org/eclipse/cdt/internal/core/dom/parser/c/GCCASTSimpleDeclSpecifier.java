/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Emanuel Graf IFS - Bugfix for #198257
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.gnu.c.IGCCASTSimpleDeclSpecifier;

/**
 * @deprecated Replaced by {@link CASTSimpleDeclSpecifier}.
 */
@Deprecated
public class GCCASTSimpleDeclSpecifier extends CASTSimpleDeclSpecifier implements IGCCASTSimpleDeclSpecifier {

	public GCCASTSimpleDeclSpecifier() {
	}
	
	public GCCASTSimpleDeclSpecifier(IASTExpression typeofExpression) {
		setTypeofExpression(typeofExpression);
	}

	@Override
	public GCCASTSimpleDeclSpecifier copy() {
		GCCASTSimpleDeclSpecifier copy = new GCCASTSimpleDeclSpecifier();
		copySimpleDeclSpec(copy);
		return copy;
	}

	public void setTypeofExpression(IASTExpression expr) {
		setDeclTypeExpression(expr);
	}

	public IASTExpression getTypeofExpression() {
		return getDeclTypeExpression();
	}
}
