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

package org.eclipse.cdt.internal.core.index;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * Interface used by the indexer to write to the index. 
 *
 * @since 4.0
 */
public interface IWritableIndex extends IIndex {

	/**
	 * Creates a file object for the given location or returns an existing one.
	 */
	IIndexFragmentFile addFile(IPath fileLocation) throws CoreException;

	/**
	 * Adds content to the given file.
	 */
	void setFileContent(IIndexFragmentFile sourceFile, 
			IASTPreprocessorIncludeStatement[] includes, 
			IASTPreprocessorMacroDefinition[] macros, IASTName[] names) throws CoreException;

	/**
	 * Clears the entire index.
	 */
	void clear() throws CoreException;

	/**
	 * Clears the given file in the index.
	 */
	void clearFile(IIndexFragmentFile file) throws CoreException;

	/**
	 * Acquires a write lock, while giving up a certain amount of read locks.
	 */
	void acquireWriteLock(int giveupReadLockCount) throws InterruptedException;

	/**
	 * Releases a write lock, reestablishing a certain amount of read locks.
	 */
	void releaseWriteLock(int establishReadLockCount);
}
