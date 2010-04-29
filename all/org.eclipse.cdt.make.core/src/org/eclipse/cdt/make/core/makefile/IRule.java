/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.core.makefile;

/**
 * There are several kinds of rules: Inference rules, target rules
 * Some make provides special rules for example:
 * .DEFAULT, .IGNORE etc ...
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IRule extends IParent {
	/**
	 * @return Array of command for the rule.
	 */
	ICommand[] getCommands();

	/**
	 * @return The rule target name.
	 * 
	 */
	ITarget getTarget();

}
