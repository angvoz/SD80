/*******************************************************************************
 * Copyright (c) 2007, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bryan Wilkinson (QNX) - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.swt.graphics.Image;

import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.Directives;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.cdt.ui.text.contentassist.ICompletionProposalComputer;

import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;

public class KeywordCompletionProposalComputer extends ParsingBasedProposalComputer implements ICompletionProposalComputer {

	@Override
	protected List<CCompletionProposal> computeCompletionProposals(
			CContentAssistInvocationContext context,
			IASTCompletionNode completionNode, String prefix)
			throws CoreException {

		if (prefix.length() == 0) {
			try {
				prefix= context.computeIdentifierPrefix().toString();
			} catch (BadLocationException exc) {
				CUIPlugin.log(exc);
			}
		}
		final int prefixLength = prefix.length();
		// No prefix, no completions
        if (prefixLength == 0 || context.isContextInformationStyle())
            return Collections.emptyList();

        // keywords are matched case-sensitive
		final int relevance = RelevanceConstants.CASE_MATCH_RELEVANCE + RelevanceConstants.KEYWORD_TYPE_RELEVANCE;

		List<CCompletionProposal> proposals = new ArrayList<CCompletionProposal>();

		if (inPreprocessorDirective(context)) {
			// TODO split this into a separate proposal computer?
			boolean needDirectiveKeyword= inPreprocessorKeyword(context);
			String[] keywords= preprocessorKeywords;

			// add matching preprocessor keyword proposals
			ImageDescriptor imagedesc = CElementImageProvider.getKeywordImageDescriptor();
			Image image = imagedesc != null ? CUIPlugin.getImageDescriptorRegistry().get(imagedesc) : null;
			for (int i = 0; i < keywords.length; ++i) {
				String repString= keywords[i];
				if (repString.startsWith(prefix) && keywords[i].length() > prefixLength) {
					int repLength = prefixLength;
					int repOffset = context.getInvocationOffset() - repLength;
					if (prefix.charAt(0) == '#') {
						// strip leading '#' from replacement
						repLength--;
						repOffset++;
						repString= repString.substring(1);
					} else if (needDirectiveKeyword) {
						continue;
					}
					proposals.add(new CCompletionProposal(repString, repOffset,
							repLength, image, keywords[i], relevance, context.getViewer()));
				}
			}	        
		} else {
	        if (!isValidContext(completionNode))
	            return Collections.emptyList();
	        
	        ITranslationUnit tu = context.getTranslationUnit();
	        
	        String[] keywords = cppkeywords; // default to C++
	        if (tu != null && tu.isCLanguage())
	            keywords = ckeywords;
	        
			// add matching keyword proposals
	        ImageDescriptor imagedesc = CElementImageProvider.getKeywordImageDescriptor();
	        Image image = imagedesc != null ? CUIPlugin.getImageDescriptorRegistry().get(imagedesc) : null;
	        for (int i = 0; i < keywords.length; ++i) {
	            if (keywords[i].startsWith(prefix) && keywords[i].length() > prefixLength) {
	                int repLength = prefixLength;
	                int repOffset = context.getInvocationOffset() - repLength;
	                proposals.add(new CCompletionProposal(keywords[i], repOffset,
							repLength, image, keywords[i], relevance, context.getViewer()));
	            }
	        }
		}
        
        return proposals;
	}

	/**
	 * Checks whether the given invocation context looks valid for template completion.
	 * 
	 * @param context  the content assist invocation context
	 * @return <code>false</code> if the given invocation context looks like a field reference
	 */
	private boolean isValidContext(IASTCompletionNode completionNode) {
		IASTName[] names = completionNode.getNames();
		for (int i = 0; i < names.length; ++i) {
			IASTName name = names[i];
			
			// ignore if not connected
			if (name.getTranslationUnit() == null)
				continue;
		
			// ignore if this is a member access
			if (name.getParent() instanceof IASTFieldReference)
				continue;
			
			return true;
		}
		
		return false;
	}

	/**
	 * Test whether the invocation offset is inside or before the preprocessor directive keyword.
	 * 
	 * @param context  the invocation context
	 * @return <code>true</code> if the invocation offset is inside or before the directive keyword 
	 */
	private boolean inPreprocessorKeyword(CContentAssistInvocationContext context) {
		IDocument doc = context.getDocument();
		int offset = context.getInvocationOffset();
		
		try {
			final ITypedRegion partition= TextUtilities.getPartition(doc, ICPartitions.C_PARTITIONING, offset, true);
			if (ICPartitions.C_PREPROCESSOR.equals(partition.getType())) {
				String ppPrefix= doc.get(partition.getOffset(), offset - partition.getOffset());
				if (ppPrefix.matches("\\s*#\\s*\\w*")) { //$NON-NLS-1$
					// we are inside the directive keyword
					return true;
				}
			}
			
		} catch (BadLocationException exc) {
		}
		return false;
	}

	/**
	 * Check if the invocation offset is inside a preprocessor directive.
	 * 
	 * @param context  the content asist invocation context
	 * @return <code>true</code> if invocation offset is inside a preprocessor directive
	 */
	private boolean inPreprocessorDirective(CContentAssistInvocationContext context) {
		IDocument doc = context.getDocument();
		int offset = context.getInvocationOffset();
		
		try {
			final ITypedRegion partition= TextUtilities.getPartition(doc, ICPartitions.C_PARTITIONING, offset, true);
			if (ICPartitions.C_PREPROCESSOR.equals(partition.getType())) {
				return true;
			}
			
		} catch (BadLocationException exc) {
		}
		return false;
	}

    // These are the keywords we complete
    // We only do the ones that are >= 5 characters long
    private static String [] ckeywords = {
        Keywords.BREAK,
        Keywords.CONST,
        Keywords.CONTINUE,
        Keywords.DEFAULT,
        Keywords.DOUBLE,
        Keywords.EXTERN,
        Keywords.FLOAT,
        Keywords.INLINE,
        Keywords.REGISTER,
        Keywords.RESTRICT,
        Keywords.RETURN,
        Keywords.SHORT,
        Keywords.SIGNED,
        Keywords.SIZEOF,
        Keywords.STATIC,
        Keywords.STRUCT,
        Keywords.SWITCH,
        Keywords.TYPEDEF,
        Keywords.UNION,
        Keywords.UNSIGNED,
        Keywords.VOLATILE,
        Keywords.WHILE,
        Keywords._BOOL,
        Keywords._COMPLEX,
        Keywords._IMAGINARY
    };

    private static String [] cppkeywords = {
        Keywords.BREAK,
        Keywords.CATCH,
        Keywords.CLASS,
        Keywords.CONST,
        Keywords.CONST_CAST,
        Keywords.CONTINUE,
        Keywords.DEFAULT,
        Keywords.DELETE,
        Keywords.DOUBLE,
        Keywords.DYNAMIC_CAST,
        Keywords.EXPLICIT,
        Keywords.EXPORT,
        Keywords.EXTERN,
        Keywords.FALSE,
        Keywords.FLOAT,
        Keywords.FRIEND,
        Keywords.INLINE,
        Keywords.MUTABLE,
        Keywords.NAMESPACE,
        Keywords.OPERATOR,
        Keywords.PRIVATE,
        Keywords.PROTECTED,
        Keywords.PUBLIC,
        Keywords.REGISTER,
        Keywords.REINTERPRET_CAST,
        Keywords.RETURN,
        Keywords.SHORT,
        Keywords.SIGNED,
        Keywords.SIZEOF,
        Keywords.STATIC,
        Keywords.STATIC_CAST,
        Keywords.STRUCT,
        Keywords.SWITCH,
        Keywords.TEMPLATE,
        Keywords.THROW,
        Keywords.TYPEDEF,
        Keywords.TYPEID,
        Keywords.TYPENAME,
        Keywords.UNION,
        Keywords.UNSIGNED,
        Keywords.USING,
        Keywords.VIRTUAL,
        Keywords.VOLATILE,
        Keywords.WCHAR_T,
        Keywords.WHILE
    };

    private static String [] preprocessorKeywords = {
        Directives.POUND_DEFINE + ' ',
        Directives.POUND_ELIF + ' ',
        Directives.POUND_ELSE,
        Directives.POUND_ENDIF,
        Directives.POUND_ERROR + ' ',
        Directives.POUND_IF + ' ',
        Directives.POUND_IFDEF + ' ',
        Directives.POUND_IFNDEF + ' ',
        Directives.POUND_INCLUDE + ' ',
        Directives.POUND_PRAGMA + ' ',
        Directives.POUND_UNDEF + ' ',
        "defined" //$NON-NLS-1$
    };
}
