/*******************************************************************************
 * Copyright (c) 2008, 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.rewrite.changegenerator;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.parser.tests.rewrite.TestHelper;
import org.eclipse.cdt.core.tests.BaseTestFramework;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;
import org.eclipse.cdt.internal.core.dom.rewrite.changegenerator.ChangeGenerator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;

public abstract class ChangeGeneratorTest extends BaseTestFramework {
	protected String source;
	protected String expectedSource;

	public ChangeGeneratorTest() {
		super();
	}

	@Override
	protected void setUp() throws Exception {
		CCorePlugin.getIndexManager().joinIndexer(IIndexManager.FOREVER, new NullProgressMonitor());
		super.setUp();
	}

	@Override
	public void runTest() throws Exception{
		final ASTModificationStore modStore = new ASTModificationStore();
		final ChangeGenerator changegenartor = new ChangeGenerator(modStore);
		IFile testFile = importFile("source.h", source); //$NON-NLS-1$
		
		ASTVisitor visitor = createModificator(modStore);
		
		CCorePlugin.getIndexManager().reindex(cproject);

		ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());

		boolean joined = CCorePlugin.getIndexManager().joinIndexer(20000, new NullProgressMonitor());
		assertTrue("The indexing operation of the test CProject has not finished jet. This should not happen...", joined);
		
		IASTTranslationUnit unit = CoreModelUtil.findTranslationUnit(testFile).getAST();
		unit.accept(visitor);
		
		changegenartor.generateChange(unit);
		Document doc = new Document(source);
		for (Change curChange : ((CompositeChange)changegenartor.getChange()).getChildren()){
			if (curChange instanceof TextFileChange) {
				TextFileChange textChange = (TextFileChange) curChange;
				textChange.getEdit().apply(doc);
			}
		}
		assertEquals(TestHelper.unifyNewLines(expectedSource), TestHelper.unifyNewLines(doc.get()));
	}

	protected abstract ASTVisitor createModificator(ASTModificationStore modStore);

	public ChangeGeneratorTest(String name) {
		super(name);
	}

	@Override
	protected void tearDown() throws Exception {
		System.gc();
		fileManager.closeAllFiles();
		super.tearDown();
	}
}
