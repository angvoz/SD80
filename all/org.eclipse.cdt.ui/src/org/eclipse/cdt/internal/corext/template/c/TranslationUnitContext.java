package org.eclipse.cdt.internal.corext.template.c;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;

/**
 * A compilation unit context.
 */
public abstract class TranslationUnitContext extends DocumentTemplateContext {

	/** The compilation unit, may be <code>null</code>. */
	private final ITranslationUnit fTranslationUnit;

	/**
	 * Creates a compilation unit context.
	 * 
	 * @param type   the context type.
	 * @param string the document string.
	 * @param completionPosition the completion position within the document.
	 * @param compilationUnit the compilation unit (may be <code>null</code>).
	 */
	protected TranslationUnitContext(TemplateContextType type, IDocument document, int completionOffset,
			int completionLength, ITranslationUnit translationUnit)
	{
		super(type, document, completionOffset, completionLength);
		fTranslationUnit= translationUnit;
	}
	
	/**
	 * Returns the compilation unit if one is associated with this context, <code>null</code> otherwise.
	 */
	public final ITranslationUnit getTranslationUnit() {
		return fTranslationUnit;
	}

	/**
	 * Returns the enclosing element of a particular element type, <code>null</code>
	 * if no enclosing element of that type exists.
	 */
	public ICElement findEnclosingElement(int elementType) {
		if (fTranslationUnit == null)
			return null;

		try {
			ICElement element= fTranslationUnit.getElementAtOffset(getStart());
			while (element != null && element.getElementType() != elementType)
				element= element.getParent();
			
			return element;

		} catch (CModelException e) {
			return null;
		}
	}

}


