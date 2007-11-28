/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Andrew Ferguson (Symbian)
 *    Bryan Wilkinson (QNX)
 *******************************************************************************/ 

package org.eclipse.cdt.core.index;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.core.index.DeclaredBindingsFilter;
import org.eclipse.core.runtime.CoreException;

/**
 * Can be subclassed and used for queries in the index.
 * @since 4.0
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will
 * work or that it will remain the same. Please do not use this API without
 * consulting with the CDT team.
 * </p>
 */

abstract public class IndexFilter {
	public static final IndexFilter ALL = new IndexFilter() {};
	public static final IndexFilter ALL_DECLARED = getDeclaredBindingFilter(-1, false);
	public static final IndexFilter ALL_DECLARED_OR_IMPLICIT = getDeclaredBindingFilter(-1, true);
	public static final IndexFilter CPP_DECLARED_OR_IMPLICIT= getDeclaredBindingFilter(ILinkage.CPP_LINKAGE_ID, true);
	public static final IndexFilter C_DECLARED_OR_IMPLICIT= getDeclaredBindingFilter(ILinkage.C_LINKAGE_ID, true);

	/**
	 * Get an IndexFilter that filters out bindings from linkages other than that
	 * specified
	 * @param linkageID the id of the linkage whose bindings should be retained
	 * @return an IndexFilter instance
	 */
	public static IndexFilter getFilter(final int linkageID) {
		return new IndexFilter() {
			public boolean acceptLinkage(ILinkage linkage) {
				return linkageID == linkage.getLinkageID();
			}
		};
	}

	/**
	 * Get an IndexFilter that filters out bindings without declarations and those
	 * from linkages other than that specified. 
	 * @param linkageID the id of the linkage whose bindings should be retained, or -1 
	 * to accept all linkages.
	 * @return an IndexFilter instance
	 */
	public static IndexFilter getDeclaredBindingFilter(final int linkageID, boolean acceptImplicit) {
		return new DeclaredBindingsFilter(linkageID, acceptImplicit);
	}

	/**
	 * Returns whether or not to include objects of the given linkage in the query.
	 * @see IIndex#findBindings(java.util.regex.Pattern, boolean, IndexFilter, org.eclipse.core.runtime.IProgressMonitor)
	 * @param linkage a linkage to be tested
	 * @return whether to include objects of the given linkage in the query.
	 */
	public boolean acceptLinkage(ILinkage linkage) {
		return true;
	}
		
	/**
	 * Determines whether or not a binding is valid.
	 * 
	 * @param binding the binding being checked for validity
	 * @return whether or not the binding is valid
	 * @throws CoreException 
	 */
	public boolean acceptBinding(IBinding binding) throws CoreException {
		return true;
	}
}
