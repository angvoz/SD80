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
package org.eclipse.cdt.make.core.makefile.gnu;

import org.eclipse.cdt.make.core.makefile.IDirective;

/**
 */
public interface IConditional extends IDirective {

	String getConditional();

	String getArg1();

	String getArg2();

	boolean isIfdef();

	boolean isIfndef();

	boolean isIfeq();

	boolean isIfneq();

	boolean isElse();

}