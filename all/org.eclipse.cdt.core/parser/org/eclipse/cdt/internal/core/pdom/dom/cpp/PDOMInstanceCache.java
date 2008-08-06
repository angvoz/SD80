/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.util.HashMap;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.internal.core.index.IndexCPPSignatureUtil;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.NamedNodeCollector;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNamedNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

public class PDOMInstanceCache {
	
	public static PDOMInstanceCache getCache(PDOMBinding binding) {
		final PDOM pdom= binding.getPDOM();
		final int record= binding.getRecord();
		final Integer key = record+PDOMCPPLinkage.CACHE_INSTANCES;
		Object cache= pdom.getCachedResult(key);
		if (cache instanceof PDOMInstanceCache) {
			return (PDOMInstanceCache) cache;
		}
		
		PDOMInstanceCache newCache= new PDOMInstanceCache();
		try {
			newCache.populate(binding);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		
		newCache= (PDOMInstanceCache) pdom.putCachedResult(key, newCache, false);
		return newCache;
	}
	
	private final HashMap<String, ICPPTemplateInstance> fMap;

	public PDOMInstanceCache() {
		fMap= new HashMap<String, ICPPTemplateInstance>();
	}
	
	synchronized public final void addInstance(IType[] arguments, ICPPTemplateInstance instance) {
		try {
			String key= IndexCPPSignatureUtil.getTemplateArgString(arguments, true);
			fMap.put(key, instance);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		} catch (DOMException e) {
		}
	}

	synchronized public final ICPPTemplateInstance getInstance(IType[] arguments) {		
		try {
			String key= IndexCPPSignatureUtil.getTemplateArgString(arguments, true);
			return fMap.get(key);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		} catch (DOMException e) {
		}
		return null;
	}
	
	private void populate(PDOMBinding binding) throws CoreException {
		PDOMNode parent= binding.getParentNode();
		if (parent == null) {
			parent= binding.getLinkage();
		}
		NamedNodeCollector nn= new NamedNodeCollector(binding.getLinkage(), binding.getNameCharArray());
		parent.accept(nn);
		PDOMNamedNode[] nodes= nn.getNodes();
		for (PDOMNamedNode node : nodes) {
			if (node instanceof ICPPTemplateInstance) {
				ICPPTemplateInstance inst= (ICPPTemplateInstance) node;
				if (binding.equals(inst.getTemplateDefinition())) {
					IType[] args= inst.getArguments();
					addInstance(args, inst);
				}
			}
		}
	}

	synchronized public ICPPTemplateInstance[] getAllInstances() {
		return fMap.values().toArray(new ICPPTemplateInstance[fMap.size()]);
	}
}
