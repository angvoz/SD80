/*******************************************************************************
 * Copyright (c) 2005, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Andrew Ferguson (Symbian)
 *    Anton Leherbauer (Wind River Systems)
 *    IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDirective;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.ICodeReaderCache;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.internal.core.dom.AbstractCodeReaderFactory;
import org.eclipse.cdt.internal.core.dom.IIncludeFileResolutionHeuristics;
import org.eclipse.cdt.internal.core.parser.InternalParserUtil;
import org.eclipse.cdt.internal.core.parser.scanner.IIndexBasedCodeReaderFactory;
import org.eclipse.cdt.internal.core.parser.scanner.IncludeFileContent;
import org.eclipse.cdt.internal.core.parser.scanner.IncludeFileContent.InclusionKind;
import org.eclipse.cdt.internal.core.pdom.ASTFilePathResolver;
import org.eclipse.cdt.internal.core.pdom.AbstractIndexerTask;
import org.eclipse.cdt.internal.core.pdom.AbstractIndexerTask.FileContent;
import org.eclipse.core.runtime.CoreException;

/**
 * Code reader factory, that fakes code readers for header files already stored in the 
 * index.
 */
public final class IndexBasedCodeReaderFactory extends AbstractCodeReaderFactory implements IIndexBasedCodeReaderFactory {
	private static final class NeedToParseException extends Exception {}
	private static final String GAP = "__gap__"; //$NON-NLS-1$

	private final IIndex fIndex;
	private int fLinkage;
	private Set<IIndexFileLocation> fIncludedFiles= new HashSet<IIndexFileLocation>();
	/** The fall-back code reader factory used in case a header file is not indexed */
	private final AbstractCodeReaderFactory fFallBackFactory;
	private final ASTFilePathResolver fPathResolver;
	private final AbstractIndexerTask fRelatedIndexerTask;
	private boolean fSupportFillGapFromContextToHeader= false;
	
	public IndexBasedCodeReaderFactory(IIndex index, IIncludeFileResolutionHeuristics heuristics,
			ASTFilePathResolver pathResolver, int linkage, AbstractCodeReaderFactory fallbackFactory) {
		this(index, heuristics, pathResolver, linkage, fallbackFactory, null);
	}

	public IndexBasedCodeReaderFactory(IIndex index, IIncludeFileResolutionHeuristics heuristics,
			ASTFilePathResolver pathResolver, int linkage,
			AbstractCodeReaderFactory fallbackFactory, AbstractIndexerTask relatedIndexerTask) {
		super(heuristics);
		fIndex= index;
		fFallBackFactory= fallbackFactory;
		fPathResolver= pathResolver;
		fRelatedIndexerTask= relatedIndexerTask;
		fLinkage= linkage;
	}

	public void setSupportFillGapFromContextToHeader(boolean val) {
		fSupportFillGapFromContextToHeader= val;
	}
	
	public void setLinkage(int linkageID) {
		fLinkage= linkageID;
	}

	public void cleanupAfterTranslationUnit() {
		fIncludedFiles.clear();
	}
	
	public int getUniqueIdentifier() {
		return 0;
	}

	public ICodeReaderCache getCodeReaderCache() {
		return null;
	}
	
	public CodeReader createCodeReaderForTranslationUnit(String path) {
		if (fFallBackFactory != null) {
			return fFallBackFactory.createCodeReaderForTranslationUnit(path);
		}
		return ParserUtil.createReader(path, null);
	}

	@Override
	public CodeReader createCodeReaderForInclusion(IIndexFileLocation ifl, String astPath) throws CoreException, IOException {
		if (fFallBackFactory != null) {
			return fFallBackFactory.createCodeReaderForInclusion(ifl, astPath);
		}
		return InternalParserUtil.createCodeReader(ifl, null);
	}
	
	@Deprecated
	public CodeReader createCodeReaderForInclusion(String path) {
		if (fFallBackFactory != null) {
			return fFallBackFactory.createCodeReaderForInclusion(path);
		}
		return ParserUtil.createReader(path, null);
	}

	public boolean getInclusionExists(String path) {
		return fPathResolver.doesIncludeFileExist(path); 
	}
	
	public void reportTranslationUnitFile(String path) {
		IIndexFileLocation ifl= fPathResolver.resolveASTPath(path);
		fIncludedFiles.add(ifl);
	}

	public boolean hasFileBeenIncludedInCurrentTranslationUnit(String path) {
		IIndexFileLocation ifl= fPathResolver.resolveASTPath(path);
		return fIncludedFiles.contains(ifl);
	}
	
	public IncludeFileContent getContentForInclusion(String path) {
		IIndexFileLocation ifl= fPathResolver.resolveIncludeFile(path);
		if (ifl == null) {
			return null;
		}
		path= fPathResolver.getASTPath(ifl);
		
		// include files once, only.
		if (!fIncludedFiles.add(ifl)) {
			return new IncludeFileContent(path, InclusionKind.SKIP_FILE);
		}
		
		try {
			IIndexFile file= fIndex.getFile(fLinkage, ifl);
			if (file != null) {
				try {
					List<IIndexFile> files= new ArrayList<IIndexFile>();
					List<IIndexMacro> macros= new ArrayList<IIndexMacro>();
					List<ICPPUsingDirective> directives= new ArrayList<ICPPUsingDirective>();
					Set<IIndexFileLocation> ifls= new HashSet<IIndexFileLocation>();
					collectFileContent(file, ifls, files, macros, directives, false);
					// add included files only, if no exception was thrown
					fIncludedFiles.addAll(ifls);
					return new IncludeFileContent(path, macros, directives, files);
				} catch (NeedToParseException e) {
				}
			}
		}
		catch (CoreException e) {
			CCorePlugin.log(e);
		}

		try {
			CodeReader codeReader= createCodeReaderForInclusion(ifl, path);
			if (codeReader != null) {
				IncludeFileContent ifc= new IncludeFileContent(codeReader);
				ifc.setIsSource(fPathResolver.isSource(path));
				return ifc;
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		} catch (IOException e) {
			CCorePlugin.log(e);
		}
		return null;
	}


	private void collectFileContent(IIndexFile file, Set<IIndexFileLocation> ifls, List<IIndexFile> files,
			List<IIndexMacro> macros, List<ICPPUsingDirective> usingDirectives, boolean checkIncluded)
			throws CoreException, NeedToParseException {
		IIndexFileLocation ifl= file.getLocation();
		if (!ifls.add(ifl) || (checkIncluded && fIncludedFiles.contains(ifl))) {
			return;
		}
		FileContent content;
		if (fRelatedIndexerTask != null) {
			content= fRelatedIndexerTask.getFileContent(fLinkage, ifl);
			if (content == null) {
				throw new NeedToParseException();
			}
		} else {
			content= new FileContent();
			content.setPreprocessorDirectives(file.getIncludes(), file.getMacros());
			content.setUsingDirectives(file.getUsingDirectives());
		}
		
		files.add(file);
		usingDirectives.addAll(Arrays.asList(content.getUsingDirectives()));
		Object[] dirs= content.getPreprocessingDirectives();
		for (Object d : dirs) {
			if (d instanceof IIndexMacro) {
				macros.add((IIndexMacro) d);
			} else if (d instanceof IIndexInclude) {
				IIndexFile includedFile= fIndex.resolveInclude((IIndexInclude) d);
				if (includedFile != null) {
					collectFileContent(includedFile, ifls, files, macros, usingDirectives, true);
				}
			}
		}
	}

	public IncludeFileContent getContentForContextToHeaderGap(String path) {
		if (!fSupportFillGapFromContextToHeader) {
			return null;
		}
		
		IIndexFileLocation ifl= fPathResolver.resolveASTPath(path);
		if (ifl == null) {
			return null;
		}
		
		try {
			IIndexFile targetFile= fIndex.getFile(fLinkage, ifl);
			if (targetFile == null) {
				return null;
			}
			
			IIndexFile contextFile= findContext(targetFile);
			if (contextFile == targetFile || contextFile == null) {
				return null;
			}
			
			HashSet<IIndexFile> filesIncluded= new HashSet<IIndexFile>();
			ArrayList<IIndexMacro> macros= new ArrayList<IIndexMacro>();
			ArrayList<ICPPUsingDirective> directives= new ArrayList<ICPPUsingDirective>();
			if (!collectFileContentForGap(contextFile, ifl, filesIncluded, macros, directives)) {
				return null;
			}

			// mark the files in the gap as included
			for (IIndexFile file : filesIncluded) {
				fIncludedFiles.add(file.getLocation());
			}
			return new IncludeFileContent(GAP, macros, directives, new ArrayList<IIndexFile>(filesIncluded));
		}
		catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	private IIndexFile findContext(IIndexFile file) throws CoreException {
		final HashSet<IIndexFile> ifiles= new HashSet<IIndexFile>();
		ifiles.add(file);
		IIndexInclude include= file.getParsedInContext();
		while (include != null) {
			final IIndexFile context= include.getIncludedBy();
			if (!ifiles.add(context)) {
				return file;
			}
			file= context;
			include= context.getParsedInContext();
		}
		return file;
	}

	private boolean collectFileContentForGap(IIndexFile from, IIndexFileLocation to,
			Set<IIndexFile> filesIncluded, List<IIndexMacro> macros,
			List<ICPPUsingDirective> directives) throws CoreException {

		final IIndexFileLocation ifl= from.getLocation();
		if (ifl.equals(to)) {
			return true;
		}

		if (fIncludedFiles.contains(ifl) || !filesIncluded.add(from)) {
			return false;
		}

		final IIndexInclude[] ids= from.getIncludes();
		final IIndexMacro[] ms= from.getMacros();
		final Object[] dirs= FileContent.merge(ids, ms);
		IIndexInclude success= null;
		for (Object d : dirs) {
			if (d instanceof IIndexMacro) {
				macros.add((IIndexMacro) d);
			} else if (d instanceof IIndexInclude) {
				IIndexFile includedFile= fIndex.resolveInclude((IIndexInclude) d);
				if (includedFile != null) {
					if (collectFileContentForGap(includedFile, to, filesIncluded, macros, directives)) {
						success= (IIndexInclude) d;
						break;
					}
				}
			}
		}

		final ICPPUsingDirective[] uds= from.getUsingDirectives();
		if (success == null) {
			directives.addAll(Arrays.asList(uds));
			return false;
		}
			
		final int offset= success.getNameOffset();
		for (ICPPUsingDirective ud : uds) {
			if (ud.getPointOfDeclaration() > offset)
				break;
			directives.add(ud);
		}
		return true;
	}
}
