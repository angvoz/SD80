/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.internal.core.makefile;

import org.eclipse.cdt.make.core.makefile.IPosixRule;

/**
 * .POSIX
 * The appliation shall ensure that this special target is specified without
 * prerequisites or commands.
 */
public class PosixRule extends SpecialRule implements IPosixRule {

	public PosixRule(Directive parent) {
		super(parent, new Target(".POSIX"), new String[0], new Command[0]); //$NON-NLS-1$
	}
}
