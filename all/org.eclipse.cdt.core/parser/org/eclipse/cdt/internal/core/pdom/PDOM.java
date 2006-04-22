/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.dom.IPDOMResolver;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.IPDOMWriter;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.internal.core.pdom.db.BTree;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMLinkageFactory;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile.Comparator;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile.Finder;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.QualifiedName;

/**
 * The PDOM Database.
 * 
 * @author Doug Schaefer
 */
public class PDOM extends PlatformObject
		implements IPDOM, IPDOMResolver, IPDOMWriter {

	private final ICProject project;
	private IPDOMIndexer indexer;
	
	private final IPath dbPath;
	private Database db;
	
	public static final int VERSION = 2;
	// 0 - the beginning of it all
	// 1 - first change to kick off upgrades
	// 2 - added file inclusions

	public static final int LINKAGES = Database.DATA_AREA;
	public static final int FILE_INDEX = Database.DATA_AREA + 4;
	
	private BTree fileIndex;

	private static final QualifiedName dbNameProperty
		= new QualifiedName(CCorePlugin.PLUGIN_ID, "dbName"); //$NON-NLS-1$

	public PDOM(ICProject project, IPDOMIndexer indexer) throws CoreException {
		this.project = project;
		this.indexer = indexer;
		indexer.setPDOM(this);
		
		// Load up the database
		IProject rproject = project.getProject();
		String dbName = rproject.getPersistentProperty(dbNameProperty);
		if (dbName == null) {
			dbName = project.getElementName() + "_"
					+ System.currentTimeMillis() + ".pdom";
			rproject.setPersistentProperty(dbNameProperty, dbName);
		}
		dbPath = CCorePlugin.getDefault().getStateLocation().append(dbName);
		db = new Database(dbPath.toOSString());
		
		// Check the version and force a rebuild if needed.
		// TODO Conversion might be a nicer story down the road
		if (db.getVersion() != VERSION) {
			indexer.reindex();
		}
	}

	public Object getAdapter(Class adapter) {
		if (adapter == IPDOM.class)
			return this;
		else if (adapter == IPDOMResolver.class)
			return this;
		else if (adapter == IPDOMWriter.class)
			return this;
		else if (adapter == PDOM.class)
			// TODO this use is deprecated (or bad at least)
			return this;
		else
			return super.getAdapter(adapter);
	}
	
	public ICProject getProject() {
		return project;
	}
	
	public IPDOMIndexer getIndexer() {
		return indexer;
	}
	
	public void setIndexer(IPDOMIndexer indexer) throws CoreException {
		// Force a reindex if indexer changes 
		boolean reindex = indexer != null && this.indexer != indexer;
		this.indexer = indexer;
		indexer.setPDOM(this);
		
		if (reindex)
			indexer.reindex();
	}
	
	public void accept(IPDOMVisitor visitor) throws CoreException {
		for (PDOMLinkage linkage = getFirstLinkage(); linkage != null; linkage = linkage.getNextLinkage())
			linkage.accept(visitor);
	}
	
	public static interface IListener {
		public void handleChange(PDOM pdom);
	}
	
	private List listeners;
	
	public void addListener(IListener listener) {
		if (listeners == null)
			listeners = new LinkedList();
		listeners.add(listener);
	}
	
	public void removeListener(IListener listener) {
		if (listeners == null)
			return;
		listeners.remove(listener);
	}
	
	public void fireChange() {
		if (listeners == null)
			return;
		Iterator i = listeners.iterator();
		while (i.hasNext())
			((IListener)i.next()).handleChange(this);
	}

	public Database getDB() throws CoreException {
		return db;
	}

	public BTree getFileIndex() throws CoreException {
		if (fileIndex == null)
			fileIndex = new BTree(getDB(), FILE_INDEX);
		return fileIndex;
	}

	public PDOMFile getFile(String filename) throws CoreException {
		Finder finder = new Finder(db, filename);
		getFileIndex().accept(finder);
		int record = finder.getRecord();
		return record != 0 ? new PDOMFile(this, record) : null;
	}
	
	public PDOMFile addFile(String filename) throws CoreException {
		PDOMFile file = getFile(filename);
		if (file == null) {
			file = new PDOMFile(this, filename);
			getFileIndex().insert(file.getRecord(), new Comparator(db));
		}
		return file;		
	}
	
	public void addSymbols(ILanguage language, IASTTranslationUnit ast) throws CoreException {
		final PDOMLinkage linkage = getLinkage(language);
		if (linkage == null)
			return;
		
		// Add in the includes
		IASTPreprocessorIncludeStatement[] includes = ast.getIncludeDirectives();
		for (int i = 0; i < includes.length; ++i) {
			IASTPreprocessorIncludeStatement include = includes[i];
			
			String sourcePath = include.getFileLocation().getFileName();
			PDOMFile sourceFile = addFile(sourcePath);
			String destPath = include.getPath();
			PDOMFile destFile = addFile(destPath);
			sourceFile.addIncludeTo(destFile);
		}
	
		// Add in the names
		ast.accept(new ASTVisitor() {
			{
				shouldVisitNames = true;
				shouldVisitDeclarations = true;
			}

			public int visit(IASTName name) {
				try {
					linkage.addName(name);
					return PROCESS_CONTINUE;
				} catch (CoreException e) {
					CCorePlugin.log(e);
					return PROCESS_ABORT;
				}
			};
		});;
	
		// Tell the world
		fireChange();
	}
	
	public void removeSymbols(ITranslationUnit tu) throws CoreException {
		String filename = ((IFile)tu.getResource()).getLocation().toOSString();
		PDOMFile file = getFile(filename);
		if (file == null)
			return;
		file.clear();
	}
	
	public void clear() throws CoreException {
		Database db = getDB();
		db.clear();
		db.setVersion(VERSION);
		fileIndex = null;
	}

	public boolean isEmpty() throws CoreException {
		return getFirstLinkage() == null;
	}

	public ICodeReaderFactory getCodeReaderFactory() {
		return new PDOMCodeReaderFactory(this);
	}

	public ICodeReaderFactory getCodeReaderFactory(IWorkingCopy root) {
		return new PDOMCodeReaderFactory(this, root);
	}

	public IASTName[] getDeclarations(IBinding binding) {
		try {
			if (binding instanceof PDOMBinding) {
				List names = new ArrayList();
				for (PDOMName name = ((PDOMBinding)binding).getFirstDeclaration(); name != null; name = name.getNextInBinding())
					names.add(name);
				// Add in definitions, too
				for (PDOMName name = ((PDOMBinding)binding).getFirstDefinition(); name != null; name = name.getNextInBinding())
					names.add(name);
				return (IASTName[])names.toArray(new IASTName[names.size()]);
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return new IASTName[0];
	}

	public IASTName[] getDefinitions(IBinding binding) {
		try {
			if (binding instanceof PDOMBinding) {
				List names = new ArrayList();
				for (PDOMName name = ((PDOMBinding)binding).getFirstDefinition(); name != null; name = name.getNextInBinding())
					names.add(name);
				return (IASTName[])names.toArray(new IASTName[names.size()]);
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return new IASTName[0];
	}
	
	public IBinding resolveBinding(IASTName name) {
		try {
			ILanguage language = name.getTranslationUnit().getLanguage();
			return getLinkage(language).resolveBinding(name);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		
		return null;
	}

	public IBinding[] findBindings(String pattern) throws CoreException {
		List bindings = new ArrayList();
		PDOMLinkage linkage = getFirstLinkage();
		while (linkage != null) {
			linkage.findBindings(pattern, bindings);
			linkage = linkage.getNextLinkage();
		}
		return (IBinding[])bindings.toArray(new IBinding[bindings.size()]);
	}
	
	public PDOMLinkage getLinkage(ILanguage language) throws CoreException {
		IPDOMLinkageFactory factory = (IPDOMLinkageFactory)language.getAdapter(IPDOMLinkageFactory.class);
		String id = language.getId();
		int linkrec = db.getInt(LINKAGES);
		while (linkrec != 0) {
			if (id.equals(PDOMLinkage.getId(this, linkrec)))
				return factory.getLinkage(this, linkrec);
			else
				linkrec = PDOMLinkage.getNextLinkageRecord(this, linkrec);
		}
		
		return factory.createLinkage(this);
	}

	public PDOMLinkage getLinkage(int record) throws CoreException {
		if (record == 0)
			return null;
		
		String id = PDOMLinkage.getId(this, record);
		ILanguage language = LanguageManager.getInstance().getLanguage(id);
		return getLinkage(language);
	}
	
	public PDOMLinkage getFirstLinkage() throws CoreException {
		return getLinkage(db.getInt(LINKAGES));
	}
	
	public void insertLinkage(PDOMLinkage linkage) throws CoreException {
		linkage.setNext(db.getInt(LINKAGES));
		db.putInt(LINKAGES, linkage.getRecord());
	}
	
	public PDOMBinding getBinding(int record) throws CoreException {
		if (record == 0)
			return null;
		else
			return PDOMLinkage.getLinkage(this, record).getBinding(record);
	}

	// Read-write lock rules. Readers don't conflict with other readers,
	// Writers conflict with readers, and everyone conflicts with writers.
	private Object mutex = new Object();
	private int lockCount;
	private int waitingReaders;
	
	public void acquireReadLock() throws InterruptedException {
		synchronized (mutex) {
			++waitingReaders;
			while (lockCount < 0)
				mutex.wait();
			--waitingReaders;
			++lockCount;
		}
	}
	
	public void releaseReadLock() {
		synchronized (mutex) {
			if (lockCount > 0)
				--lockCount;
			mutex.notifyAll();
		}
	}
	
	public void acquireWriteLock() throws InterruptedException {
		synchronized (mutex) {
			// Let the readers go first
			while (lockCount != 0 || waitingReaders > 0)
				mutex.wait();
			--lockCount;
		}
	}
	
	public void releaseWriteLock() {
		synchronized (mutex) {
			if (lockCount < 0)
				++lockCount;
			mutex.notifyAll();
		}
	}
	
}
