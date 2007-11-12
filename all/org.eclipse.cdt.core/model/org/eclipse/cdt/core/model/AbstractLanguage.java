/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Anton Leherbauer (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.core.model;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PlatformObject;

/**
 * Models the differences between various languages.
 * @since 4.0
 */
public abstract class AbstractLanguage extends PlatformObject implements ILanguage {
	/**
	 * Option for {@link #getASTTranslationUnit(CodeReader, IScannerInfo, ICodeReaderFactory, IIndex, int, IParserLogService)}
	 * Instructs the parser to skip function and method bodies.
	 */
	public final static int OPTION_SKIP_FUNCTION_BODIES= 1;

	/**
	 * Option for {@link #getASTTranslationUnit(CodeReader, IScannerInfo, ICodeReaderFactory, IIndex, int, IParserLogService)}
	 * Instructs the parser to add comment nodes to the ast.
	 */
	public final static int OPTION_ADD_COMMENTS= 2;

	/**
	 * Option for {@link #getASTTranslationUnit(CodeReader, IScannerInfo, ICodeReaderFactory, IIndex, int, IParserLogService)}
	 * Performance optimization, instructs the parser not to create image-locations. 
	 * When using this option {@link IASTName#getImageLocation()} will always return <code>null</code>.
	 */
	public final static int OPTION_NO_IMAGE_LOCATIONS= 4;

	/** 
	 * @deprecated, throws an UnsupportedOperationException
	 */
	final public IASTTranslationUnit getASTTranslationUnit(ITranslationUnit file, int style) throws CoreException {
		throw new UnsupportedOperationException();
	}

	/** 
	 * @deprecated, throws an UnsupportedOperationException
	 */
	final public IASTTranslationUnit getASTTranslationUnit(ITranslationUnit file, ICodeReaderFactory codeReaderFactory,
			int style) throws CoreException {
		throw new UnsupportedOperationException();
	}

	/*
	 * @see org.eclipse.cdt.core.model.ILanguage#getName()
	 */
	public String getName() {
		ILanguageDescriptor languageDescriptor= LanguageManager.getInstance().getLanguageDescriptor(getId());
		if (languageDescriptor != null) {
			return languageDescriptor.getName();
		}
		return getId();
	}

	/**
	 * Construct an AST for the source code provided by <code>reader</code>.
	 * As an option you can supply 
	 * @param reader source code to be parsed.
	 * @param scanInfo provides include paths and defined symbols.
	 * @param fileCreator factory that provides CodeReaders for files included
	 *                    by the source code being parsed.
	 * @param index (optional) index to use to provide support for ambiguity
	 *              resolution.
	 * @param options A combination of 
	 * {@link #OPTION_SKIP_FUNCTION_BODIES} and {@link #OPTION_ADD_COMMENTS} or <code>0</code>.
	 * @param log logger
	 * @return an AST for the source code provided by reader.
	 * @throws CoreException
	 */
	public IASTTranslationUnit getASTTranslationUnit(CodeReader reader, IScannerInfo scanInfo, ICodeReaderFactory fileCreator, IIndex index, int options, IParserLogService log) 
			throws CoreException {
		// for backwards compatibility
		return getASTTranslationUnit(reader, scanInfo, fileCreator, index, log);
	}
}
