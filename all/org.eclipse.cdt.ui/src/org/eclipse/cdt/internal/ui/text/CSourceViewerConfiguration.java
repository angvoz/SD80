package org.eclipse.cdt.internal.ui.text;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.HashMap;
import java.util.Vector;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.CEditorTextHoverDispatcher;
import org.eclipse.cdt.internal.ui.text.contentassist.*;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.ICDTConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IAutoIndentStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.formatter.ContentFormatter;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.formatter.IFormattingStrategy;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.Reconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.DefaultPartitioner;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.ITextEditor;



/**
 * Configuration for an <code>SourceViewer</code> which shows C code.
 */
public class CSourceViewerConfiguration extends SourceViewerConfiguration {
	
	/** Key used to look up display tab width */
	public final static String PREFERENCE_TAB_WIDTH= "org.eclipse.cdt.editor.tab.width"; //$NON-NLS-1$
	/** Key used to look up code formatter tab size */
	private final static String CODE_FORMATTER_TAB_SIZE= "org.eclipse.cdt.formatter.tabulation.size"; //$NON-NLS-1$
	/** Key used to look up code formatter tab character */
	private final static String CODE_FORMATTER_TAB_CHAR= "org.eclipse.cdt.formatter.tabulation.char"; //$NON-NLS-1$

	private CTextTools fTextTools;
	private CEditor fEditor;
	
	/**
	 * Creates a new C source viewer configuration for viewers in the given editor using
	 * the given C tools collection.
	 *
	 * @param tools the C text tools collection to be used
	 * @param editor the editor in which the configured viewer will reside
	 */
	public CSourceViewerConfiguration(CTextTools tools, CEditor editor) {
		fTextTools= tools;
		fEditor= editor;
	}

	/**
	 * Returns the C multiline comment scanner for this configuration.
	 *
	 * @return the C multiline comment scanner
	 */
	protected RuleBasedScanner getMultilineCommentScanner() {
		return fTextTools.getMultilineCommentScanner();
	}
	
	/**
	 * Returns the C singleline comment scanner for this configuration.
	 *
	 * @return the C singleline comment scanner
	 */
	protected RuleBasedScanner getSinglelineCommentScanner() {
		return fTextTools.getSinglelineCommentScanner();
	}
	
	/**
	 * Returns the C string scanner for this configuration.
	 *
	 * @return the C string scanner
	 */
	protected RuleBasedScanner getStringScanner() {
		return fTextTools.getStringScanner();
	}	
	
	/**
	 * Returns the color manager for this configuration.
	 *
	 * @return the color manager
	 */
	protected IColorManager getColorManager() {
		return fTextTools.getColorManager();
	}
	
	/**
	 * Returns the editor in which the configured viewer(s) will reside.
	 *
	 * @return the enclosing editor
	 */
	protected ITextEditor getEditor() {
		return fEditor;
	}
	
	/**
	 * @see ISourceViewerConfiguration#getPresentationReconciler(ISourceViewer)
	 */
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {

		PresentationReconciler reconciler= new PresentationReconciler();

		RuleBasedScanner scanner;

		if(sourceViewer instanceof CEditor.AdaptedSourceViewer) {
			String language = ((CEditor.AdaptedSourceViewer)sourceViewer).getDisplayLanguage();
			if(language != null && language.equals(CEditor.LANGUAGE_CPP)) {
				scanner= fTextTools.getCppCodeScanner();
			} else {
				scanner= fTextTools.getCCodeScanner();
			}
		} else {
			scanner= fTextTools.getCCodeScanner();
		}

		DefaultDamagerRepairer dr= new DefaultDamagerRepairer(scanner);

		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		//TextAttribute attr = new TextAttribute(manager.getColor(ICColorConstants.C_DEFAULT));
		
		dr= new DefaultDamagerRepairer(getSinglelineCommentScanner());		
		reconciler.setDamager(dr, CPartitionScanner.C_SINGLE_LINE_COMMENT);
		reconciler.setRepairer(dr, CPartitionScanner.C_SINGLE_LINE_COMMENT);
		
		dr= new DefaultDamagerRepairer(getStringScanner());
		reconciler.setDamager(dr, CPartitionScanner.C_STRING);
		reconciler.setRepairer(dr, CPartitionScanner.C_STRING);
		
		dr= new DefaultDamagerRepairer(getMultilineCommentScanner());		
		reconciler.setDamager(dr, CPartitionScanner.C_MULTILINE_COMMENT);
		reconciler.setRepairer(dr, CPartitionScanner.C_MULTILINE_COMMENT);

		return reconciler;
	}


	/**
	 * @see SourceViewerConfiguration#getContentAssistant(ISourceViewer)
	 */
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		if(getEditor() == null) {
			return null;
		}

		ContentAssistant assistant = new ContentAssistant();
		
		IContentAssistProcessor processor = new CCompletionProcessor(getEditor());
		assistant.setContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);

		//Will this work as a replacement for the configuration lines below?
		ContentAssistPreference.configure(assistant, getPreferenceStore());
		
		assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);		
		assistant.setContextInformationPopupOrientation(ContentAssistant.CONTEXT_INFO_ABOVE);
		assistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));

		return assistant;
	}
	
	
	/**
	 * @see SourceViewerConfiguration#getReconciler(ISourceViewer)
	 */
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		if (fEditor != null && fEditor.isEditable()) {
			Reconciler reconciler= new Reconciler() {
				protected void initialProcess() {
					// prevent case where getDocument() returns null
					// and causes exception in initialProcess()
					IDocument doc = getDocument();
					if (doc != null)
						super.initialProcess();
				}
			};
			reconciler.setDelay(1000);
			reconciler.setIsIncrementalReconciler(false);
			reconciler.setReconcilingStrategy(new CReconcilingStrategy(fEditor), IDocument.DEFAULT_CONTENT_TYPE);
			return reconciler;
		}
		return null;
	}


	/**
	 * @see SourceViewerConfiguration#getAutoIndentStrategy(ISourceViewer, String)
	 */
	public IAutoIndentStrategy getAutoIndentStrategy(ISourceViewer sourceViewer, String contentType) {
		if(CPartitionScanner.C_MULTILINE_COMMENT.equals(contentType))
			return new CCommentAutoIndentStrategy();
		return new CAutoIndentStrategy();
	}


	/**
	 * @see SourceViewerConfiguration#getDoubleClickStrategy(ISourceViewer, String)
	 */
	public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer, String contentType) {
		return new CDoubleClickSelector();
	}

	/**
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getDefaultPrefixes(ISourceViewer, String)
	 */
	public String[] getDefaultPrefixes(ISourceViewer sourceViewer, String contentType) {
		return new String[] { "//", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see SourceViewerConfiguration#getDefaultPrefix(ISourceViewer, String)
	 */
	public String getDefaultPrefix(ISourceViewer sourceViewer, String contentType) {
		if(IDocument.DEFAULT_CONTENT_TYPE.equals(contentType))
			return "//"; //$NON-NLS-1$
		if(CPartitionScanner.C_SINGLE_LINE_COMMENT.equals(contentType))
			return "//"; //$NON-NLS-1$
		if(CPartitionScanner.C_MULTILINE_COMMENT.equals(contentType))
			return "//"; //$NON-NLS-1$
		return null;
	}


	/*
	 * @see SourceViewerConfiguration#getIndentPrefixes(ISourceViewer, String)
	 */
	public String[] getIndentPrefixes(ISourceViewer sourceViewer, String contentType) {

		Vector vector= new Vector();

		// prefix[0] is either '\t' or ' ' x tabWidth, depending on useSpaces
		int tabWidth= getPreferenceStore().getInt(PREFERENCE_TAB_WIDTH);
		boolean useSpaces= getPreferenceStore().getBoolean(CEditor.SPACES_FOR_TABS); //$NON-NLS-1$

		for (int i= 0; i <= tabWidth; i++) {
		    StringBuffer prefix= new StringBuffer();

			if (useSpaces) {
			    for (int j= 0; j + i < tabWidth; j++)
			    	prefix.append(' ');
		    	
				if (i != 0)
		    		prefix.append('\t');				
			} else {    
			    for (int j= 0; j < i; j++)
			    	prefix.append(' ');
		    	
				if (i != tabWidth)
		    		prefix.append('\t');
			}
			
			vector.add(prefix.toString());
		}

		vector.add(""); //$NON-NLS-1$
		
		return (String[]) vector.toArray(new String[vector.size()]);
	}


	/**
	 * @see SourceViewerConfiguration#getTabWidth(ISourceViewer)
	 */
	public int getTabWidth(ISourceViewer sourceViewer) {
		return getPreferenceStore().getInt(PREFERENCE_TAB_WIDTH);
	}


	/**
	 * @see SourceViewerConfiguration#getAnnotationHover(ISourceViewer)
	 */
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		return new CAnnotationHover();
	}


	/**
	 * @see SourceViewerConfiguration#getTextHover(ISourceViewer, String)
	 */
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		HashMap textHovers = new HashMap( 3 );
		IPluginRegistry pluginRegistry = Platform.getPluginRegistry();
		IExtensionPoint point = pluginRegistry.getExtensionPoint( CUIPlugin.getDefault().getDescriptor().getUniqueIdentifier(), 
																  ICDTConstants.EP_TEXT_HOVERS );
		if ( point != null ) 
		{
			IExtension[] extensions = point.getExtensions();
			for ( int i = 0; i < extensions.length; i++ ) 
			{
				IExtension currentExtension = extensions[i];
				IConfigurationElement[] configElements = currentExtension.getConfigurationElements();
				for ( int j = 0; j < configElements.length; j++ ) 
				{
					IConfigurationElement config = configElements[j];
					if ( config.getName().equals( ICDTConstants.TAG_TEXT_HOVER ) ) 
					{
						processTextHoverElement( textHovers, config );
					}
				}
			}
		}

		return new CEditorTextHoverDispatcher( fEditor, textHovers );
	}

	private void processTextHoverElement( HashMap textHovers, IConfigurationElement element ) {
		String perspId = element.getAttribute( ICDTConstants.ATT_PERSPECTIVE );
		ITextHover textHover = null;
		try {
			textHover = (ITextHover)element.createExecutableExtension( ICDTConstants.ATT_CLASS );
		} catch (CoreException e) {
		}
		if ( perspId != null ) {
			textHovers.put( perspId, textHover );
		}
	}

	/**
	 * @see SourceViewerConfiguration#getConfiguredContentTypes(ISourceViewer)
	 */
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] { 	IDocument.DEFAULT_CONTENT_TYPE, 
								CPartitionScanner.C_MULTILINE_COMMENT,
								CPartitionScanner.C_SINGLE_LINE_COMMENT,
								CPartitionScanner.C_STRING };
	}
	
	/**
	 * @see SourceViewerConfiguration#getContentFormatter(ISourceViewer)
	 */
	public IContentFormatter getContentFormatter(ISourceViewer sourceViewer) {
		String[] types= new String[] {
			DefaultPartitioner.CONTENT_TYPES_CATEGORY
		};
		
		ContentFormatter formatter= new ContentFormatter();
		IFormattingStrategy strategy= new CFormattingStrategy(sourceViewer);
		
		formatter.setFormattingStrategy(strategy, IDocument.DEFAULT_CONTENT_TYPE);
		formatter.enablePartitionAwareFormatting(false);		
		formatter.setPartitionManagingPositionCategories(types);
		
		return formatter;
	}
	
	protected IPreferenceStore getPreferenceStore() {
		return CUIPlugin.getDefault().getPreferenceStore();
	}
	
	/*
	 * @see SourceViewerConfiguration#getHoverControlCreator(ISourceViewer)
	 * @since 2.0
	 */
	public IInformationControlCreator getInformationControlCreator(ISourceViewer sourceViewer) {
		return getInformationControlCreator(sourceViewer, true);
	}
	

	public IInformationControlCreator getInformationControlCreator(ISourceViewer sourceViewer, final boolean cutDown) {
			return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				int style = cutDown ? SWT.NONE : (SWT.V_SCROLL | SWT.H_SCROLL);
				return new DefaultInformationControl(parent, style, new HTMLTextPresenter(cutDown));
				// return new HoverBrowserControl(parent);
			}
		};
	}

	public IInformationPresenter getInformationPresenter(ISourceViewer sourceViewer) {
		return super.getInformationPresenter(sourceViewer);
	}

}
