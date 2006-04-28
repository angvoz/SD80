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

package org.eclipse.cdt.internal.ui.indexview;

import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.cdt.internal.core.pdom.db.IString;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class LinkageCache {

	private final PDOM pdom;
	private final PDOMLinkage linkage;
	private int[] cache;
	
	private static class Counter implements IBTreeVisitor {
		int count;
		PDOM pdom;
		public Counter(PDOM pdom) {
			this.pdom = pdom;
		}
		public int compare(int record) throws CoreException {
			return 1;
		}
		public boolean visit(int record) throws CoreException {
			if (record != 0 && ! PDOMBinding.isOrphaned(pdom, record))
				++count;
			return true;
		}
	}

	private static class FillCache implements IBTreeVisitor {
		final PDOM pdom;
		final int[] cache;
		int index;
		public FillCache(PDOM pdom, int [] cache) {
			this.pdom = pdom;
			this.cache = cache;
		}
		public int compare(int record) throws CoreException {
			return 1;
		};
		public boolean visit(int record) throws CoreException {
			if (record == 0 || PDOMBinding.isOrphaned(pdom, record))
				return true;
			
			cache[index++] = record;
			return true;
		};
	}

	public LinkageCache(PDOM pdom, PDOMLinkage linkage) throws CoreException {
		this.pdom = pdom;
		this.linkage = linkage;
		
		Counter counter = new Counter(pdom);
		linkage.getIndex().accept(counter);
		cache = new int[counter.count];
		FillCache fillCache = new FillCache(pdom, cache);
		linkage.getIndex().accept(fillCache);
	}
	
	public int getCount() {
		return cache.length;
	}
	
	public PDOMBinding getItem(int index) throws CoreException {
		return pdom.getBinding(cache[index]);
	}
	
	public IString getName() throws CoreException {
		return linkage.getDBName();
	}
}
