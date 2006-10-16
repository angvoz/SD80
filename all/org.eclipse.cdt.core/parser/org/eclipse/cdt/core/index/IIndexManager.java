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

package org.eclipse.cdt.core.index;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Starting point for working with the index. The manager can be obtained via
 * {@link CCorePlugin#getIndexManager()}.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the CDT team.
 * </p>
 * 
 * @since 4.0
 */
public interface IIndexManager {
	public final static int ADD_DEPENDENCIES = 0x1;
	public final static int ADD_DEPENDENT    = 0x2;
	
	public final static int FOREVER= -1;
	/**
	 * Returns the index for the given project.
	 * @param project the project to get the index for
	 * @return an index for the project
	 * @throws CoreException
	 */
	IIndex getIndex(ICProject project) throws CoreException;

	/**
	 * Returns the index for the given projects.
	 * @param projects the projects to get the index for
	 * @return an index for the projects
	 * @throws CoreException
	 */
	IIndex getIndex(ICProject[] projects) throws CoreException;

	/**
	 * Returns the index for the given project. You can specify to add dependencies or dependent projects.
	 * @param project the project to get the index for
	 * @param options <code>0</code> or a combination of {@link #ADD_DEPENDENCIES} and {@link #ADD_DEPENDENT}.
	 * @return an index for the project
	 * @throws CoreException
	 */
	IIndex getIndex(ICProject project, int options) throws CoreException;

	/**
	 * Returns the index for the given projects. You can specify to add dependencies or dependent projects.
	 * @param projects the projects to get the index for
	 * @param options <code>0</code> or a combination of {@link #ADD_DEPENDENCIES} and {@link #ADD_DEPENDENT}.
	 * @return an index for the projects
	 * @throws CoreException
	 */
	IIndex getIndex(ICProject[] projects, int options) throws CoreException;

	/**
	 * Registers a listener that will be notified whenever the indexer go idle.
	 * @param listener the listener to register.
	 */
	void addIndexChangeListener(IIndexChangeListener listener);

	/**
	 * Removes a previously registered index change listener.
	 * @param listener the listener to unregister.
	 */
	void removeIndexChangeListener(IIndexChangeListener listener);
	
	/**
	 * Registers a listener that will be notified whenever the indexer changes its state.
	 * @param listener the listener to register.
	 */
	void addIndexerStateListener(IIndexerStateListener listener);

	/**
	 * Removes a previously registered indexer state listener.
	 * @param listener the listener to unregister.
	 */
	void removeIndexerStateListener(IIndexerStateListener listener);
	
	/**
	 * Joins the indexer and reports progress.
	 * @param waitMaxMillis time limit in millis after which the method returns with <code>false</code>,
	 * or {@link #FOREVER}.
	 * @param monitor a monitor to report progress.
	 * @return <code>true</code>, if the indexer went idle in the given time.
	 */
	boolean joinIndexer(int waitMaxMillis, IProgressMonitor monitor);
}
