package org.eclipse.cdt.internal.ui.compare;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.cdt.internal.ui.CPlugin;
import org.eclipse.cdt.internal.ui.text.CSourceViewerConfiguration;
import org.eclipse.cdt.internal.ui.text.CTextTools;


import org.eclipse.swt.widgets.Composite;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.contentmergeviewer.ITokenComparator;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.compare.internal.TokenComparator;


import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.SourceViewer;

public class CMergeViewer extends TextMergeViewer {
	
	private static final String TITLE= "CMergeViewer.title";
		
		
	public CMergeViewer(Composite parent, int styles, CompareConfiguration mp) {
		super(parent, styles, mp);
	}
	
	public String getTitle() {
		return CPlugin.getResourceString(TITLE);
	}


	protected ITokenComparator createTokenComparator(String s) {
		return new TokenComparator(s);
	}
	
	protected IDocumentPartitioner getDocumentPartitioner() {
		return CPlugin.getDefault().getTextTools().createDocumentPartitioner();
	}
		
	protected void configureTextViewer(TextViewer textViewer) {
		if (textViewer instanceof SourceViewer) {
			CTextTools tools= CPlugin.getDefault().getTextTools();
			((SourceViewer)textViewer).configure(new CSourceViewerConfiguration(tools, null));
		}
	}
}
