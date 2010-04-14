/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.provisional.core.model.cfg;

/**
 * Node that represent empty operator with label, such as case branch or label
 */
public interface ILabeledNode extends IConnectorNode {
	public static String THEN = "then"; //$NON-NLS-1$
	public static String ELSE = "else"; //$NON-NLS-1$
	public static String DEFAULT = "default"; //$NON-NLS-1$

	String getLabel();
}
