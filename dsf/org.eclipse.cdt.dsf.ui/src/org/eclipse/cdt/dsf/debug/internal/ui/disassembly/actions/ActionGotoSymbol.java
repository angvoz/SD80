/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.actions;

import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.DisassemblyMessages;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.IDisassemblyPart;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.internal.ui.text.CWordFinder;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.widgets.Shell;

@SuppressWarnings("restriction")
public final class ActionGotoSymbol extends AbstractDisassemblyAction {
	public ActionGotoSymbol(IDisassemblyPart disassemblyPart) {
		super(disassemblyPart);
		setText(DisassemblyMessages.Disassembly_action_GotoSymbol_label);
	}
	@Override
	public void run() {
		ITextViewer viewer = getDisassemblyPart().getTextViewer();
		IDocument document= viewer.getDocument();
		IRegion wordRegion = CWordFinder.findWord(document, viewer.getSelectedRange().x);
		String defaultValue = null;
		if (wordRegion != null) {
			try {
				defaultValue = document.get(wordRegion.getOffset(), wordRegion.getLength());
			} catch (BadLocationException e) {
				// safely ignored
			}
		}
		if (defaultValue == null) {
			defaultValue = DsfUIPlugin.getDefault().getDialogSettings().get("gotoSymbol"); //$NON-NLS-1$
			if (defaultValue == null) {
				defaultValue = ""; //$NON-NLS-1$
			}
		}
		String dlgTitle = DisassemblyMessages.Disassembly_GotoSymbolDialog_title;
		String dlgLabel = DisassemblyMessages.Disassembly_GotoSymbolDialog_label;
		final Shell shell= getDisassemblyPart().getSite().getShell();
		InputDialog dlg = new InputDialog(shell, dlgTitle, dlgLabel, defaultValue, null);
		if (dlg.open() == IDialogConstants.OK_ID) {
			String value = dlg.getValue();
			DsfUIPlugin.getDefault().getDialogSettings().put("gotoSymbol", value); //$NON-NLS-1$
			getDisassemblyPart().gotoSymbol(value);
		}
	}
}
