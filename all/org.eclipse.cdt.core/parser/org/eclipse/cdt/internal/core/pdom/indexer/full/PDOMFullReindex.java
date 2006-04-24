/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.indexer.full;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementVisitor;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMFullReindex extends PDOMFullIndexerJob {

	public PDOMFullReindex(PDOM pdom) {
		super(pdom);
	}

	protected IStatus run(final IProgressMonitor monitor) {
		try {
			long start = System.currentTimeMillis();
			
			// First clear out the PDOM
			pdom.clear();
			
			// Get a count of all the elements that we'll be visiting for the monitor
			final int[] count = { 0 };
			pdom.getProject().accept(new ICElementVisitor() {
				public boolean visit(ICElement element) throws CoreException {
					switch (element.getElementType()) {
					case ICElement.C_UNIT:
						++count[0];
						return false;
					case ICElement.C_CCONTAINER:
					case ICElement.C_PROJECT:
						return true;
					}
					return false;
				}
			});
			
			monitor.beginTask("Indexing", count[0]);
			
			// First index all the source files (i.e. not headers)
			pdom.getProject().accept(new ICElementVisitor() {
				public boolean visit(ICElement element) throws CoreException {
					switch (element.getElementType()) {
					case ICElement.C_UNIT:
						ITranslationUnit tu = (ITranslationUnit)element;
						if (tu.isSourceUnit()) {
							monitor.subTask(tu.getElementName());
							try {
								addTU(tu);
							} catch (InterruptedException e) {
								throw new CoreException(Status.CANCEL_STATUS);
							}
							monitor.worked(1);
						}
						return false;
					case ICElement.C_CCONTAINER:
					case ICElement.C_PROJECT:
						return true;
					}
					return false;
				}
			});
			
			// Now add in the header files but only if they aren't already indexed
			pdom.getProject().accept(new ICElementVisitor() {
				public boolean visit(ICElement element) throws CoreException {
					switch (element.getElementType()) {
					case ICElement.C_UNIT:
						ITranslationUnit tu = (ITranslationUnit)element;
						if (tu.isHeaderUnit()) {
							IFile rfile = (IFile)tu.getUnderlyingResource();
							String filename = rfile.getLocation().toOSString();
							if (pdom.getFile(filename) == null) {
								monitor.subTask(tu.getElementName());
								try {
									addTU(tu);
								} catch (InterruptedException e) {
									throw new CoreException(Status.CANCEL_STATUS);
								}
							}
							monitor.worked(1);
						}
						return false;
					case ICElement.C_CCONTAINER:
					case ICElement.C_PROJECT:
						return true;
					}
					return false;
				}
			});

			String showTimings = Platform.getDebugOption(CCorePlugin.PLUGIN_ID
					+ "/debug/pdomtimings"); //$NON-NLS-1$
			if (showTimings != null && showTimings.equalsIgnoreCase("true")) //$NON-NLS-1$
				System.out.println("PDOM Full Reindex Time: " + (System.currentTimeMillis() - start)); //$NON-NLS-1$

			return Status.OK_STATUS;
		} catch (CoreException e) {
			return e.getStatus();
		}
	}

}
