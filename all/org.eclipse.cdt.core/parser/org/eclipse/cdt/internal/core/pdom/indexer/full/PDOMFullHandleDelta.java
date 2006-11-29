/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.indexer.full;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Doug Schaefer
 *
 */
class PDOMFullHandleDelta extends PDOMFullIndexerJob {

	private List changed = new ArrayList();
	private List removed = new ArrayList();

	public PDOMFullHandleDelta(PDOMFullIndexer indexer, ICElementDelta delta) throws CoreException {
		super(indexer);
		processDelta(delta, changed, changed, removed);
		fTotalSourcesEstimate= changed.size() + removed.size();
	}

	public void run(IProgressMonitor monitor) {
		long start = System.currentTimeMillis();
		try {
			setupIndexAndReaderFactory();
			
			// separate headers
			List headers= new ArrayList();
			List sources= changed;
			for (Iterator iter = changed.iterator(); iter.hasNext();) {
				ITranslationUnit tu = (ITranslationUnit) iter.next();
				if (!tu.isSourceUnit()) {
					headers.add(tu);
					iter.remove();
				}
			}

			registerTUsInReaderFactory(sources);
			registerTUsInReaderFactory(headers);
					
			Iterator i= removed.iterator();
			while (i.hasNext()) {
				if (monitor.isCanceled())
					return;
				ITranslationUnit tu = (ITranslationUnit)i.next();
				removeTU(index, tu);
				if (tu.isSourceUnit()) {
					fCompletedSources++;
				}
				else {
					fTotalSourcesEstimate--;
					fCompletedHeaders++;
				}
			}

			parseTUs(sources, headers, monitor);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		} catch (InterruptedException e) {
		}
		traceEnd(start);
	}
}
