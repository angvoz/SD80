/*******************************************************************************
 * Copyright (c) 2000, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import java.util.LinkedList;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.jface.text.formatter.ContextBasedFormattingStrategy;
import org.eclipse.jface.text.formatter.FormattingContextProperties;
import org.eclipse.jface.text.formatter.IFormattingContext;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import org.eclipse.cdt.core.formatter.CodeFormatter;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.corext.util.CodeFormatterUtil;

/**
 * @author AChapiro
 */
public class CFormattingStrategy extends ContextBasedFormattingStrategy {
	

	/** Documents to be formatted by this strategy */
	private final LinkedList fDocuments= new LinkedList();
	/** Partitions to be formatted by this strategy */
	private final LinkedList fPartitions= new LinkedList();

	/**
	 * Creates a new java formatting strategy.
 	 */
	public CFormattingStrategy() {
		super();
	}

	/*
	 * @see org.eclipse.jface.text.formatter.ContextBasedFormattingStrategy#format()
	 */
	public void format() {
		super.format();
		
		final IDocument document= (IDocument)fDocuments.removeFirst();
		final TypedPosition partition= (TypedPosition)fPartitions.removeFirst();
		
		if (document != null && partition != null) {
			try {

				final TextEdit edit = CodeFormatterUtil.format(
						CodeFormatter.K_TRANSLATION_UNIT, document.get(),
						partition.getOffset(), partition.getLength(), 0,
						TextUtilities.getDefaultLineDelimiter(document),
						getPreferences());

				if (edit != null)
					edit.apply(document);

			} catch (MalformedTreeException exception) {
				CUIPlugin.log(exception);
			} catch (BadLocationException exception) {
				// Can only happen on concurrent document modification - log and
				// bail out
				CUIPlugin.log(exception);
			}
		}
 	}

	/*
	 * @see org.eclipse.jface.text.formatter.ContextBasedFormattingStrategy#formatterStarts(org.eclipse.jface.text.formatter.IFormattingContext)
	 */
	public void formatterStarts(final IFormattingContext context) {
		super.formatterStarts(context);
		
		fPartitions.addLast(context.getProperty(FormattingContextProperties.CONTEXT_PARTITION));
		fDocuments.addLast(context.getProperty(FormattingContextProperties.CONTEXT_MEDIUM));
	}

	/*
	 * @see org.eclipse.jface.text.formatter.ContextBasedFormattingStrategy#formatterStops()
	 */
	public void formatterStops() {
		super.formatterStops();

		fPartitions.clear();
		fDocuments.clear();
	}

}
