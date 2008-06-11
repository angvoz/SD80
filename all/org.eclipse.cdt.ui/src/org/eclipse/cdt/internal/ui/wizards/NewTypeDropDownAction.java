/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.PlatformUI;

public class NewTypeDropDownAction extends AbstractWizardDropDownAction {

	public NewTypeDropDownAction() {
	    super();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, ICHelpContextIds.OPEN_CLASS_WIZARD_ACTION);
	}

	@Override
	protected IAction[] getWizardActions() {
		return CWizardRegistry.getTypeWizardActions();
	}
}
