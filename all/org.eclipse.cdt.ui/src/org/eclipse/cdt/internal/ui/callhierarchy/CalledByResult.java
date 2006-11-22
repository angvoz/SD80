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

package org.eclipse.cdt.internal.ui.callhierarchy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.ICElement;

public class CalledByResult {
	private Map fElementToReferences= new HashMap();

	public ICElement[] getElements() {
		Set elements = fElementToReferences.keySet();
		return (ICElement[]) elements.toArray(new ICElement[elements.size()]);
	}
	
	public IIndexName[] getReferences(ICElement calledElement) {
		List references= (List) fElementToReferences.get(calledElement);
		return (IIndexName[]) references.toArray(new IIndexName[references.size()]);
	}

	public void add(ICElement elem, IIndexName ref) {
		List list= (List) fElementToReferences.get(elem);
		if (list == null) {
			list= new ArrayList();
			fElementToReferences.put(elem, list);
		}
		list.add(ref);
	}
}
