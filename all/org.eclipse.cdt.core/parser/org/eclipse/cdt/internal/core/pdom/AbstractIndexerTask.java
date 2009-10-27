/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit.IDependencyTree;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit.IDependencyTree.IASTInclusionNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDirective;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.AbstractLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.AbstractCodeReaderFactory;
import org.eclipse.cdt.internal.core.dom.IIncludeFileResolutionHeuristics;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFile;
import org.eclipse.cdt.internal.core.index.IWritableIndex;
import org.eclipse.cdt.internal.core.index.IndexBasedCodeReaderFactory;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

/**
 * Task for the actual indexing. Various indexers need to implement the abstract methods.
 * @since 5.0
 */
public abstract class AbstractIndexerTask extends PDOMWriter {
	protected static enum UnusedHeaderStrategy {
		skip, useDefaultLanguage, useAlternateLanguage, useBoth
	}

	private static final int MAX_ERRORS = 500;
	
	private static class FileKey {
		final URI fUri;
		final int fLinkageID;
		
		public FileKey(int linkageID, URI uri) {
			fUri= uri;
			fLinkageID= linkageID;
		}
		@Override
		public int hashCode() {
			return fUri.hashCode() * 31 + fLinkageID;
		}
		@Override
		public boolean equals(Object obj) {
			FileKey other = (FileKey) obj;
			return fLinkageID == other.fLinkageID && fUri.equals(other.fUri);
		}
	}

	public static class FileContent {
		private IIndexFile fIndexFile= null;
		private boolean fRequestUpdate= false;
		private boolean fRequestIsCounted= true;
		private boolean fIsUpdated= false;
		private Object[] fPreprocessingDirectives;
		private ICPPUsingDirective[] fDirectives;

		public Object[] getPreprocessingDirectives() throws CoreException {
			if (fPreprocessingDirectives == null) {
				if (fIndexFile == null)
					return new Object[0];
				setPreprocessorDirectives(fIndexFile.getIncludes(), fIndexFile.getMacros());
			}
			return fPreprocessingDirectives;
		}
		
		public ICPPUsingDirective[] getUsingDirectives() throws CoreException {
			if (fDirectives == null) {
				if (fIndexFile == null)
					return ICPPUsingDirective.EMPTY_ARRAY;
				setUsingDirectives(fIndexFile.getUsingDirectives());
			}
			return fDirectives;
		}

		public void setPreprocessorDirectives(IIndexInclude[] includes, IIndexMacro[] macros) throws CoreException {
			fPreprocessingDirectives= merge(includes, macros);
		}

		public void setUsingDirectives(ICPPUsingDirective[] usingDirectives) {
			fDirectives= usingDirectives;
		}

		public void clearCaches() {
			fPreprocessingDirectives= null;
			fDirectives= null;
		}
		
		public static Object[] merge(IIndexInclude[] includes, IIndexMacro[] macros) throws CoreException {
			Object[] merged= new Object[includes.length+macros.length];
			int i=0;
			int m=0;
			int ioffset= getOffset(includes, i);
			int moffset= getOffset(macros, m);
			for (int k = 0; k < merged.length; k++) {
				if (ioffset <= moffset) {
					merged[k]= includes[i];
					ioffset= getOffset(includes, ++i);
				} else {
					merged[k]= macros[m];
					moffset= getOffset(macros, ++m);
				}
			}
			return merged;
		}

		private static int getOffset(IIndexMacro[] macros, int m) throws CoreException {
			if (m < macros.length) {
				return macros[m].getFileLocation().getNodeOffset();
			}
			return Integer.MAX_VALUE;
		}

		private static int getOffset(IIndexInclude[] includes, int i) throws CoreException {
			if (i < includes.length) {
				return includes[i].getNameOffset();
			}
			return Integer.MAX_VALUE;
		}
	}
	
	protected enum MessageKind {parsingFileTask, errorWhileParsing, tooManyIndexProblems}
	
	private int fUpdateFlags= IIndexManager.UPDATE_ALL;
	private UnusedHeaderStrategy fIndexHeadersWithoutContext= UnusedHeaderStrategy.useDefaultLanguage;
	private boolean fIndexFilesWithoutConfiguration= true;
	private HashMap<FileKey, FileContent> fFileInfos= new HashMap<FileKey, FileContent>();

	private Object[] fFilesToUpdate;
	private List<Object> fFilesToRemove = new ArrayList<Object>();
	private List<String> fFilesUpFront= new ArrayList<String>();
	private int fASTOptions;
	private int fForceNumberFiles= 0;
	
	protected IWritableIndex fIndex;
	private ITodoTaskUpdater fTodoTaskUpdater;
	private final boolean fIsFastIndexer;
	private AbstractCodeReaderFactory fCodeReaderFactory;

	public AbstractIndexerTask(Object[] filesToUpdate, Object[] filesToRemove, IndexerInputAdapter resolver, boolean fastIndexer) {
		super(resolver);
		fIsFastIndexer= fastIndexer;
		fFilesToUpdate= filesToUpdate;
		fFilesToRemove.addAll(Arrays.asList(filesToRemove));
		updateRequestedFiles(fFilesToUpdate.length + fFilesToRemove.size());
	}
	
	public final void setIndexHeadersWithoutContext(UnusedHeaderStrategy mode) {
		fIndexHeadersWithoutContext= mode;
	}
	public final void setIndexFilesWithoutBuildConfiguration(boolean val) {
		fIndexFilesWithoutConfiguration= val;
	}
	public UnusedHeaderStrategy getIndexHeadersWithoutContext() {
		return fIndexHeadersWithoutContext;
	}
	public boolean indexFilesWithoutConfiguration() {
		return fIndexFilesWithoutConfiguration;
	}
	public final void setUpdateFlags(int flags) {
		fUpdateFlags= flags;
	}
	public final void setParseUpFront(String[] astFilePaths) {
		fFilesUpFront.addAll(Arrays.asList(astFilePaths));
	}
	public final void setForceFirstFiles(int number) {
		fForceNumberFiles= number;
	}

	protected abstract IWritableIndex createIndex();
	protected abstract IIncludeFileResolutionHeuristics createIncludeHeuristics();
	protected abstract AbstractCodeReaderFactory createReaderFactory();
	protected abstract AbstractLanguage[] getLanguages(String fileName);

	protected ITodoTaskUpdater createTodoTaskUpdater() {
		return null;
	}
	
	protected IScannerInfo createDefaultScannerConfig(int linkageID) {
		return new ScannerInfo();
	}
	
	protected String getASTPathForParsingUpFront() {
		return "______"; //$NON-NLS-1$
	}

	private final IASTTranslationUnit createAST(String code, AbstractLanguage lang, IScannerInfo scanInfo,
			int options, IProgressMonitor monitor) throws CoreException {
		String dummyName= getASTPathForParsingUpFront();
		if (dummyName != null) {
			IIndexFileLocation dummyLoc= fResolver.resolveASTPath(dummyName);
			setIndexed(lang.getLinkageID(), dummyLoc);
			CodeReader codeReader= new CodeReader(dummyName, code.toCharArray());
			return createAST(lang, codeReader, scanInfo, options, monitor);
		}
		return null;
	}
	

	private final IASTTranslationUnit createAST(Object tu, AbstractLanguage language, IScannerInfo scanInfo, int options, IProgressMonitor pm)
			throws CoreException {
		final CodeReader codeReader= fResolver.getCodeReader(tu);
		if (codeReader == null) {
			return null;
		}
		if (fResolver.isSourceUnit(tu)) {
			options |= ILanguage.OPTION_IS_SOURCE_UNIT;
		}
		return createAST(language, codeReader, scanInfo, options, pm);
	}

	private final IASTTranslationUnit createAST(AbstractLanguage language, CodeReader codeReader,
			IScannerInfo scanInfo, int options, IProgressMonitor pm) throws CoreException {
		if (fCodeReaderFactory == null) {
			if (fIsFastIndexer) {
				fCodeReaderFactory= new IndexBasedCodeReaderFactory(fIndex, createIncludeHeuristics(),
						fResolver, language.getLinkageID(), createReaderFactory(), this);
			} else {
				fCodeReaderFactory= createReaderFactory();
			}
		} else if (fIsFastIndexer) {
			((IndexBasedCodeReaderFactory) fCodeReaderFactory).setLinkage(language.getLinkageID());
		}
		
		try {
			IASTTranslationUnit ast= language.getASTTranslationUnit(codeReader, scanInfo, fCodeReaderFactory,
					fIndex, options, getLogService());
			if (pm.isCanceled()) {
				return null;
			}
			return ast;
		} finally {
			if (fIsFastIndexer) {
				((IndexBasedCodeReaderFactory) fCodeReaderFactory).cleanupAfterTranslationUnit();
			}
		}
	}

	protected IParserLogService getLogService() {
		return ParserUtil.getParserLogService();
	}

	public final void runTask(IProgressMonitor monitor) throws InterruptedException {
		if (!fIndexFilesWithoutConfiguration) {
			fIndexHeadersWithoutContext= UnusedHeaderStrategy.skip;
		}
		
		fIndex= createIndex();
		if (fIndex == null) {
			return;
		}
		fTodoTaskUpdater= createTodoTaskUpdater();
		
		fASTOptions= ILanguage.OPTION_NO_IMAGE_LOCATIONS
				| ILanguage.OPTION_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS;
		if (getSkipReferences() == SKIP_ALL_REFERENCES) {
			fASTOptions |= ILanguage.OPTION_SKIP_FUNCTION_BODIES;
		}

		fIndex.resetCacheCounters();
		fIndex.acquireReadLock();

		try {
			try {
				// split into sources and headers, remove excluded sources.
				final HashMap<Integer, List<Object>> files= new HashMap<Integer, List<Object>>();
				final ArrayList<IIndexFragmentFile> ifilesToRemove= new ArrayList<IIndexFragmentFile>();
				extractFiles(files, ifilesToRemove, monitor);

				// remove files from index
				removeFilesInIndex(fFilesToRemove, ifilesToRemove, monitor);

				parseFilesUpFront(monitor);
				for (int linkageID : getLinkagesToParse()) {
					parseLinkage(linkageID, files, monitor);
				}
			} finally {
				fIndex.flush();
			}
		} catch (CoreException e) {
			logException(e);
		} finally {
			fIndex.releaseReadLock();
		}
	}

	private void extractFiles(Map<Integer, List<Object>> files, List<IIndexFragmentFile> iFilesToRemove,
			IProgressMonitor monitor) throws CoreException {
		final boolean forceAll= (fUpdateFlags & IIndexManager.UPDATE_ALL) != 0;
		final boolean checkTimestamps= (fUpdateFlags & IIndexManager.UPDATE_CHECK_TIMESTAMPS) != 0;
		final boolean checkConfig= (fUpdateFlags & IIndexManager.UPDATE_CHECK_CONFIGURATION) != 0;

		int count= 0;
		int forceFirst= fForceNumberFiles;
		for (final Object tu : fFilesToUpdate) {
			if (monitor.isCanceled())
				return;

			final boolean force= forceAll || --forceFirst >= 0;
			final IIndexFileLocation ifl= fResolver.resolveFile(tu);
			if (ifl == null)
				continue;
			
			final boolean isSourceUnit= fResolver.isSourceUnit(tu);
			final boolean isExcludedSource= isSourceUnit && !fIndexFilesWithoutConfiguration && !fResolver.isFileBuildConfigured(tu);
			final IIndexFragmentFile[] indexFiles= fIndex.getWritableFiles(ifl);
			
			if ((isSourceUnit && !isExcludedSource) || fIndexHeadersWithoutContext != UnusedHeaderStrategy.skip) {
				// headers or sources required with a specific linkage
				AbstractLanguage[] langs= fResolver.getLanguages(tu, fIndexHeadersWithoutContext==UnusedHeaderStrategy.useBoth);
				for (AbstractLanguage lang : langs) {
					int linkageID = lang.getLinkageID();
					IIndexFragmentFile ifile= getFile(linkageID, indexFiles);
					if (ifile == null || !ifile.hasContent()) {
						store(tu, linkageID, isSourceUnit, files);
						requestUpdate(linkageID, ifl, null);
						count++;
					} else {
						takeFile(ifile, indexFiles);
						boolean update= false;
						if (checkConfig) {
							update= isSourceUnit ? isSourceUnitConfigChange(tu, ifile) : isHeaderConfigChange(tu, ifile);
						}
						update= update || force || (checkTimestamps && fResolver.getLastModified(ifl) != ifile.getTimestamp());
						if (update) {
							requestUpdate(linkageID, ifl, ifile);
							store(tu, linkageID, isSourceUnit, files);
							count++;
						}
					}
				}
			}
			
			// handle other files present in index
			for (IIndexFragmentFile ifile : indexFiles) {
				if (ifile != null && ifile.hasContent()) {
					IIndexInclude ctx= ifile.getParsedInContext();
					if (ctx == null) {
						iFilesToRemove.add(ifile);
						count++;
					} else {
						boolean update= false;
						if (checkConfig && ifile.getParsedInContext() != null) {
							update= isHeaderConfigChange(tu, ifile);
						}
						update= update || force || (checkTimestamps && fResolver.getLastModified(ifl) != ifile.getTimestamp());
						if (update) {
							final int linkageID = ifile.getLinkageID();
							requestUpdate(linkageID, ifl, ifile);
							store(tu, linkageID, false, files);
							count++;
						}
					}
				}
			}
		}
		updateRequestedFiles(count-fFilesToUpdate.length);
		fFilesToUpdate= null;
	}
	
	private void requestUpdate(int linkageID, IIndexFileLocation ifl, IIndexFragmentFile ifile) {
		FileKey key= new FileKey(linkageID, ifl.getURI());
		FileContent info= fFileInfos.get(key);
		if (info == null) {
			info= createFileInfo(key, null);
		}
		info.fIndexFile= ifile;
		info.fRequestUpdate= true;
	}
	
	private void setIndexed(int linkageID, IIndexFileLocation ifl) {
		FileKey key= new FileKey(linkageID, ifl.getURI());
		FileContent info= fFileInfos.get(key);
		if (info == null) {
			info= createFileInfo(key, null);
		}
		info.fIsUpdated= true;
		info.clearCaches();
	}

	private FileContent createFileInfo(FileKey key, IIndexFile ifile) {
		FileContent info = new FileContent();
		fFileInfos.put(key, info);
		info.fIndexFile= ifile;
		return info;
	}

	private FileContent getFileInfo(int linkageID, IIndexFileLocation ifl) {
		FileKey key= new FileKey(linkageID, ifl.getURI());
		return fFileInfos.get(key);
	}

	private boolean isSourceUnitConfigChange(Object tu, IIndexFragmentFile ifile) {
		return false;
	}

	private boolean isHeaderConfigChange(Object tu, IIndexFragmentFile ifile) {
		return false;
	}
	
	private IIndexFragmentFile getFile(int linkageID, IIndexFragmentFile[] indexFiles) throws CoreException {
		for (IIndexFragmentFile ifile : indexFiles) {
			if (ifile != null && ifile.getLinkageID() == linkageID) {
				return ifile;
			}
		}
		return null;
	}

	private void takeFile(IIndexFragmentFile ifile, IIndexFragmentFile[] indexFiles) {
		for (int i = 0; i < indexFiles.length; i++) {
			if (indexFiles[i] == ifile) {
				indexFiles[i]= null;
				return;
			}
		}
	}

	private void store(Object tu, int linkageID, boolean isSourceUnit, Map<Integer, List<Object>> files) {
		Integer key = getFileListKey(linkageID, isSourceUnit);
		List<Object> list= files.get(key);
		if (list == null) {
			list= new LinkedList<Object>();
			files.put(key, list);
		}
		list.add(tu);
	}

	private Integer getFileListKey(int linkageID, boolean isSourceUnit) {
		Integer key= new Integer(linkageID*2 + (isSourceUnit ? 0 : 1));
		return key;
	}

	private void removeFilesInIndex(List<Object> filesToRemove, List<IIndexFragmentFile> ifilesToRemove,
			IProgressMonitor monitor) throws InterruptedException, CoreException {
		if (!filesToRemove.isEmpty() || !ifilesToRemove.isEmpty()) {
			fIndex.acquireWriteLock(1);
			try {
				for (Object tu : fFilesToRemove) {
					if (monitor.isCanceled()) {
						return;
					}
					IIndexFileLocation ifl= fResolver.resolveFile(tu);
					if (ifl == null)
						continue;
					IIndexFragmentFile[] ifiles= fIndex.getWritableFiles(ifl);
					for (IIndexFragmentFile ifile : ifiles) {
						fIndex.clearFile(ifile, null);
					}
					updateRequestedFiles(-1);
				}
				for (IIndexFragmentFile ifile : ifilesToRemove) {
					if (monitor.isCanceled()) {
						return;
					}
					fIndex.clearFile(ifile, null);
					updateRequestedFiles(-1);
				}
			} finally {
				fIndex.releaseWriteLock(1);
			}
		}
		fFilesToRemove.clear();
	}
	
	private void parseFilesUpFront(IProgressMonitor monitor) throws CoreException {
		for (String upfront : fFilesUpFront) {
			if (monitor.isCanceled()) {
				return;
			}
			String filePath = upfront;
			filePath= filePath.trim();
			if (filePath.length() == 0) {
				continue;
			}
			final IPath path= new Path(filePath);
			final String fileName = path.lastSegment();
			try {
				if (fShowActivity) {
					trace("Indexer: parsing " + filePath + " up front");  //$NON-NLS-1$ //$NON-NLS-2$
				}
				monitor.subTask(getMessage(MessageKind.parsingFileTask,
						fileName, path.removeLastSegments(1).toString()));
				
				AbstractLanguage[] langs= getLanguages(fileName);
				for (AbstractLanguage lang : langs) {
					int linkageID= lang.getLinkageID();
					String code= "#include \"" + filePath + "\"\n";  //$NON-NLS-1$ //$NON-NLS-2$
					
					IScannerInfo scanInfo= createDefaultScannerConfig(linkageID);
					if (scanInfo != null) {
						long start= System.currentTimeMillis();
						IASTTranslationUnit ast= createAST(code, lang, scanInfo, fASTOptions, monitor);
						fStatistics.fParsingTime += System.currentTimeMillis()-start;
						
						if (ast != null) {
							writeToIndex(linkageID, ast, computeHashCode(scanInfo), monitor);
							updateFileCount(0, 0, 1);
						}
					}
				}
			} catch (Exception e) {
				swallowError(path, e);
			}
		}
		fFilesUpFront.clear();
	}
	
	private void parseLinkage(int linkageID, Map<Integer, List<Object>> fileListMap, IProgressMonitor monitor)
			throws CoreException, InterruptedException {
		// sources
		List<Object> files= fileListMap.get(getFileListKey(linkageID, true));
		if (files != null) {
			for (Object tu : files) {
				if (monitor.isCanceled())
					return;
				final IIndexFileLocation ifl = fResolver.resolveFile(tu);
				if (ifl == null)
					continue;
				final FileContent info= getFileInfo(linkageID, ifl);
				if (info != null && info.fRequestUpdate && !info.fIsUpdated) {
					info.fRequestIsCounted= false;
					final IScannerInfo scannerInfo= fResolver.getBuildConfiguration(linkageID, tu);
					parseFile(tu, linkageID, ifl, scannerInfo, monitor);
					if (info.fIsUpdated) {
						updateFileCount(1, 0, 0);	// a source file was parsed
					}
				}
			}
			files.clear();
		}
		
		// headers with context
		HashMap<IIndexFragmentFile, Object> contextMap= new HashMap<IIndexFragmentFile, Object>();
		files= fileListMap.get(getFileListKey(linkageID, false));
		if (files != null) {
			for (Iterator<Object> iter = files.iterator(); iter.hasNext();) {
				if (monitor.isCanceled())
					return;
				final Object header= iter.next();
				final IIndexFileLocation ifl = fResolver.resolveFile(header);
				final FileContent info= getFileInfo(linkageID, ifl);
				if (info != null && info.fRequestUpdate && !info.fIsUpdated) {
					if (info.fIndexFile != null && fIndex.isWritableFile(info.fIndexFile)) {
						Object tu= findContext((IIndexFragmentFile) info.fIndexFile, contextMap);
						if (tu != null) {
							final IScannerInfo scannerInfo= fResolver.getBuildConfiguration(linkageID, tu);
							info.fRequestIsCounted= false;
							parseFile(tu, linkageID, fResolver.resolveFile(tu), scannerInfo, monitor);
							if (info.fIsUpdated) {
								updateFileCount(0, 0, 1);	// a header was parsed in context
								iter.remove();
							}
						}
					}
				} else {
					// file was already parsed.
					iter.remove();
				}
			}

			// headers without context
			contextMap= null;
			for (Iterator<Object> iter = files.iterator(); iter.hasNext();) {
				if (monitor.isCanceled())
					return;
				final Object header= iter.next();
				final IIndexFileLocation ifl = fResolver.resolveFile(header);
				final FileContent info= getFileInfo(linkageID, ifl);
				if (info != null && info.fRequestUpdate && !info.fIsUpdated) {
					info.fRequestIsCounted= false;
					final IScannerInfo scannerInfo= fResolver.getBuildConfiguration(linkageID, header);
					parseFile(header, linkageID, ifl, scannerInfo, monitor);
					if (info.fIsUpdated) {
						updateFileCount(0, 1, 1);	// a header was parsed without context
						iter.remove();
					}
				}
			}
		}
	}

	private static Object NO_CONTEXT= new Object();
	private Object findContext(IIndexFragmentFile ifile, HashMap<IIndexFragmentFile, Object> contextMap) {
		Object cachedContext= contextMap.get(ifile);
		if (cachedContext != null) {
			return cachedContext == NO_CONTEXT ? null : cachedContext;
		}
		try {
			Object context= fResolver.getInputFile(ifile.getLocation());
			if (context != null && fResolver.isSourceUnit(context)) {
				contextMap.put(ifile, context);
				return context;
			}

			contextMap.put(ifile, NO_CONTEXT); // prevent recursion
			final IIndexInclude contextInclude= ifile.getParsedInContext();
			if (contextInclude != null) {
				final IIndexFragmentFile contextIFile= (IIndexFragmentFile) contextInclude.getIncludedBy();
				context= findContext(contextIFile, contextMap);
				if (context != null) {
					contextMap.put(ifile, context);
					return context;
				}
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	private void parseFile(Object tu, int linkageID, IIndexFileLocation ifl, IScannerInfo scanInfo,
			IProgressMonitor pm) throws CoreException, InterruptedException {
		IPath path= getPathForLabel(ifl);
		AbstractLanguage[] langs= fResolver.getLanguages(tu, fIndexHeadersWithoutContext==UnusedHeaderStrategy.useBoth);
		AbstractLanguage lang= null;
		for (AbstractLanguage lang2 : langs) {
			if (lang2.getLinkageID() == linkageID) {
				lang= lang2;
				break;
			}
		}
		if (lang==null) {
			return;
		}
		
		Throwable th= null;
		try {
			if (fShowActivity) {
				trace("Indexer: parsing " + path.toOSString()); //$NON-NLS-1$
			}
			pm.subTask(getMessage(MessageKind.parsingFileTask,
					path.lastSegment(), path.removeLastSegments(1).toString()));
			long start= System.currentTimeMillis();
			IASTTranslationUnit ast= createAST(tu, lang, scanInfo, fASTOptions, pm);
			fStatistics.fParsingTime += System.currentTimeMillis()-start;
			if (ast != null) {
				writeToIndex(linkageID, ast, computeHashCode(scanInfo), pm);
			}
		} catch (CoreException e) {
			th= e;
		} catch (RuntimeException e) {
			th= e;
		} catch (PDOMNotImplementedError e) {
			th= e;
		} catch (StackOverflowError e) {
			th= e;
		} catch (Error e) {
			try {
				swallowError(path, e);
			} catch (Throwable ignore) {
			}
			throw e;
		}
		if (th != null) {
			swallowError(path, th);
		}
	}
	
	private void writeToIndex(final int linkageID, IASTTranslationUnit ast, int configHash,
			IProgressMonitor pm) throws CoreException, InterruptedException {
		HashSet<IIndexFileLocation> enteredFiles= new HashSet<IIndexFileLocation>();
		ArrayList<IIndexFileLocation> orderedIFLs= new ArrayList<IIndexFileLocation>();
		
		final IIndexFileLocation topIfl = fResolver.resolveASTPath(ast.getFilePath());
		enteredFiles.add(topIfl);
		IDependencyTree tree= ast.getDependencyTree();
		IASTInclusionNode[] inclusions= tree.getInclusions();
		for (IASTInclusionNode inclusion : inclusions) {
			collectOrderedIFLs(linkageID, inclusion, enteredFiles, orderedIFLs);
		}
		
		FileContent info= getFileInfo(linkageID, topIfl);
		if (info != null && info.fRequestUpdate && !info.fIsUpdated) {
			orderedIFLs.add(topIfl);
		}
		
		IIndexFileLocation[] ifls= orderedIFLs.toArray(new IIndexFileLocation[orderedIFLs.size()]);
		try {
			addSymbols(ast, ifls, fIndex, 1, false, configHash, fTodoTaskUpdater, pm);
		} finally {
			// mark as updated in any case, to avoid parsing files that caused an exception to be thrown.
			for (IIndexFileLocation ifl : ifls) {
				info= getFileInfo(linkageID, ifl);
				Assert.isNotNull(info);
				info.fIsUpdated= true;
			}
		}
	}

	private void collectOrderedIFLs(final int linkageID, IASTInclusionNode inclusion,
			HashSet<IIndexFileLocation> enteredFiles, ArrayList<IIndexFileLocation> orderedIFLs) throws CoreException {
		final IASTPreprocessorIncludeStatement id= inclusion.getIncludeDirective();
		if (id.isActive() && id.isResolved()) {
			final IIndexFileLocation ifl= fResolver.resolveASTPath(id.getPath());
			final boolean isFirstEntry= enteredFiles.add(ifl);
			IASTInclusionNode[] nested= inclusion.getNestedInclusions();
			for (IASTInclusionNode element : nested) {
				collectOrderedIFLs(linkageID, element, enteredFiles, orderedIFLs);
			}
			if (isFirstEntry && needToUpdateHeader(linkageID, ifl)) {
				orderedIFLs.add(ifl);
			}
		}
	}

	public final boolean needToUpdateHeader(int linkageID, IIndexFileLocation ifl) throws CoreException {
		FileContent info= getFileInfo(linkageID, ifl);
		if (info == null) {
			IIndexFile ifile= null;
			if (fResolver.canBePartOfSDK(ifl)) {
				ifile= fIndex.getFile(linkageID, ifl);
			} else {
				IIndexFragmentFile fragFile= fIndex.getWritableFile(linkageID, ifl);
				if (fragFile != null && fragFile.hasContent()) {
					ifile= fragFile;
				}
			}
			info= createFileInfo(new FileKey(linkageID, ifl.getURI()), ifile);
			if (ifile == null) {
				info.fRequestIsCounted= false;
				info.fRequestUpdate= true;
			}
		}
		final boolean needUpdate= !info.fIsUpdated && info.fRequestUpdate;
		if (needUpdate && info.fRequestIsCounted) {
			updateFileCount(0, 1, 0);	// total headers will be counted when written to db
			info.fRequestIsCounted= false;
		}
		return needUpdate;
	}

	private IPath getPathForLabel(IIndexFileLocation ifl) {
		String fullPath= ifl.getFullPath();
		if (fullPath != null) {
			return new Path(fullPath);
		}
		IPath path= IndexLocationFactory.getAbsolutePath(ifl);
		if (path != null) {
			return path;
		}
		URI uri= ifl.getURI();
		return new Path(uri.getPath());
	}

	private void swallowError(IPath file, Throwable e) throws CoreException {
		IStatus s;
		/*
		 * If the thrown CoreException is for a STATUS_PDOM_TOO_LARGE, we don't want to
		 * swallow this one.
		 */
		if (e instanceof CoreException) {
			s=((CoreException)e).getStatus();
			if (s != null && s.getCode() == CCorePlugin.STATUS_PDOM_TOO_LARGE) {
				if (CCorePlugin.PLUGIN_ID.equals(s.getPlugin()))
					throw (CoreException) e;
			}
		}
		if (e instanceof CoreException) {
			s= ((CoreException) e).getStatus();
			Throwable exception = s.getException();
			if (exception instanceof OutOfMemoryError || exception instanceof StackOverflowError) {
				// mask errors in order to avoid dialog from platform
				e= new InvocationTargetException(exception);
				exception= null;
			}
			if (exception == null) {
				s= new Status(s.getSeverity(), s.getPlugin(), s.getCode(), s.getMessage(), e);
			}
		} else {
			s= createStatus(getMessage(MessageKind.errorWhileParsing, file), e);
		}
		logError(s);
		if (++fStatistics.fErrorCount > MAX_ERRORS) {
			throw new CoreException(createStatus(getMessage(MessageKind.tooManyIndexProblems)));
		}
	}

	/**
	 * @param s
	 */
	protected void logError(IStatus s) {
		CCorePlugin.log(s);
	}
	
	protected void logException(Throwable e) {
		CCorePlugin.log(e);
	}

	private static int computeHashCode(IScannerInfo scannerInfo) {
		int result= 0;
		Map<String, String> macros= scannerInfo.getDefinedSymbols();
		if (macros != null) {
			for (Entry<String, String> entry : macros.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				result= addToHashcode(result, key);
				if (value != null && value.length() > 0) {
					result= addToHashcode(result, value);
				}
			}
		}
		String[] a= scannerInfo.getIncludePaths();
		if (a != null) {
			for (String element : a) {
				result= addToHashcode(result, element);

			}
		}
		if (scannerInfo instanceof IExtendedScannerInfo) {
			IExtendedScannerInfo esi= (IExtendedScannerInfo) scannerInfo;
			a= esi.getIncludeFiles();
			if (a != null) {
				for (String element : a) {
					result= addToHashcode(result, element);

				}
			}			
			a= esi.getLocalIncludePath();
			if (a != null) {
				for (String element : a) {
					result= addToHashcode(result, element);

				}
			}		
			a= esi.getMacroFiles();
			if (a != null) {
				for (String element : a) {
					result= addToHashcode(result, element);

				}
			}		
		}
		return result;
	}

	private static int addToHashcode(int result, String key) {
		return result*31 + key.hashCode();
	}

	public final FileContent getFileContent(int linkageID, IIndexFileLocation ifl) throws CoreException {
		if (!needToUpdateHeader(linkageID, ifl)) {
			FileContent info= getFileInfo(linkageID, ifl);
			Assert.isNotNull(info);
			if (info.fIndexFile == null) {
				info.fIndexFile= fIndex.getFile(linkageID, ifl);
				if (info.fIndexFile == null) {
					return null;
				}
			}
			return info;
		}
		return null;
	}
	
	protected String getMessage(MessageKind kind, Object... arguments) {
		switch (kind) {
		case parsingFileTask:
			return NLS.bind(Messages.AbstractIndexerTask_parsingFileTask, arguments);
		case errorWhileParsing:
			return NLS.bind(Messages.AbstractIndexerTask_errorWhileParsing, arguments);
		case tooManyIndexProblems:
			return Messages.AbstractIndexerTask_tooManyIndexProblems;
		}
		return null;
	}
	
	
	/**
	 * @return array of linkage IDs that should be parsed
	 */
	protected int[] getLinkagesToParse() {
		return PDOMManager.IDS_FOR_LINKAGES_TO_INDEX;
	}
}