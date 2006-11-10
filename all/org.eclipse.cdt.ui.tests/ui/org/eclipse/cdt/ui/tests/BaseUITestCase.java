/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.ui.tests;

import java.io.IOException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Display;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.ui.testplugin.CTestPlugin;

public class BaseUITestCase extends BaseTestCase {
	private boolean fExpectFailure= false;
	private int fBugnumber= 0;
	
	public BaseUITestCase() {
		super();
	}
	
	public BaseUITestCase(String name) {
		super(name);
	}

	/**
	 * Reads a section in comments form the source of the given class. Fully 
	 * equivalent to <code>readTaggedComment(getClass(), tag)</code>
	 * @since 4.0
	 */
    protected String readTaggedComment(final String tag) throws IOException {
    	return TestSourceReader.readTaggedComment(CTestPlugin.getDefault().getBundle(), "ui", getClass(), tag);
    }
    
    protected IFile createFile(IContainer container, String fileName, String contents) throws Exception {
    	return TestSourceReader.createFile(container, new Path(fileName), contents);
    }
    
    protected IASTTranslationUnit createIndexBasedAST(IIndex index, ICProject project, IFile file) throws CModelException, CoreException {
    	return TestSourceReader.createIndexBasedAST(index, project, file);
    }

	protected void waitForIndexer(IIndex index, IFile file, int maxmillis) throws Exception {
		long endTime= System.currentTimeMillis() + maxmillis;
		do {
			index.acquireReadLock();
			try {
				IIndexFile pfile= index.getFile(file.getLocation());
				if (pfile != null && pfile.getTimestamp() >= file.getLocalTimeStamp()) {
					return;
				}
			}
			finally {
				index.releaseReadLock();
			}
			
			Thread.sleep(50);
		} while (System.currentTimeMillis() < endTime);
		throw new Exception("Indexer did not complete in time!");
	}
	
	protected void runEventQueue(int time) {
		long endTime= System.currentTimeMillis()+time;
		do {
			while (Display.getCurrent().readAndDispatch());
		}
		while(System.currentTimeMillis() < endTime);
	}
}
