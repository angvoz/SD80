/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalUnknownScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;

/**
 * Helper class for performing the base class lookup. First a directed graph without loops is computed
 * to represent the base class hierarchy up to those bases for which the lookup finds matches. Next, from
 * these leaves we search for virtual bases that are hidden. With this information the matches are extracted
 * from the graph.
 */
class BaseClassLookup {
	public static void lookupInBaseClasses(LookupData data, ICPPClassScope classScope, IIndexFileSet fileSet) {
		if (classScope == null)
			return;
		
		final ICPPClassType classType= classScope.getClassType();
		if (classType == null) 
			return;
		
		final HashMap<IScope, BaseClassLookup> infoMap = new HashMap<IScope, BaseClassLookup>();
		BaseClassLookup rootInfo= lookupInBaseClass(data, null, false, classType, fileSet, infoMap, 0);
		if (data.contentAssist) {
			rootInfo.collectResultForContentAssist(data);
		} else {
			hideVirtualBases(rootInfo, infoMap);
			IBinding[] result= rootInfo.collectResult(data, true, null);
			verifyResult(data, result);
		}
	}


	private final ICPPClassType fClassType;
	private IBinding[] fBindings;
	private List<BaseClassLookup> fChildren= Collections.emptyList();
	private BitSet fVirtual;
	private boolean fHiddenAsVirtualBase= false;
	private boolean fPropagationDone= false;
	private boolean fCollected;
	private boolean fCollectedAsRegularBase;

	private BaseClassLookup(ICPPClassType type) {
		fClassType= type;
	}
	ICPPClassType getClassType() {
		return fClassType;
	}

	IBinding[] getResult() {
		return fBindings;
	}
	boolean containsVirtualBase() {
		return (fVirtual != null && fVirtual.nextSetBit(0) >= 0);
	}
	boolean hasMatches() {
		return fBindings != null && fBindings.length > 0 && fBindings[0] != null;
	}

	public void addBase(boolean virtual, BaseClassLookup baseInfo) {
		if (virtual && fHiddenAsVirtualBase)
			return;
		
		if (fChildren.isEmpty()) {
			fChildren= new ArrayList<BaseClassLookup>();
			fVirtual= new BitSet();
		}
		fVirtual.set(fChildren.size(), virtual);
		fChildren.add(baseInfo);
	}

	public void setResult(IBinding[] bindings) {
		fBindings= bindings;
	}

	public void setHiddenAsVirtualBase() {
		fHiddenAsVirtualBase= true;
	}
	public void propagateHiddenAsVirtual() {
		if (fPropagationDone)
			return;
		fPropagationDone= true;
		for (int i= 0; i < fChildren.size(); i++) {
			BaseClassLookup child = fChildren.get(i);
			if (fVirtual.get(i)) {
				child.setHiddenAsVirtualBase();
			}
			child.propagateHiddenAsVirtual();
		}
	}
	
	public boolean containsNonStaticMember() {
		for (IBinding binding : fBindings) {
			if (binding == null)
				return false;
			if (binding instanceof ICPPMember) {
				try {
					if (!((ICPPMember) binding).isStatic()) 
						return true;
				} catch (DOMException e) {
					// treat as non-static
				}
			}
		}
		return false;
	}
	
	static BaseClassLookup lookupInBaseClass(LookupData data, ICPPClassScope baseClassScope, boolean isVirtual,
			ICPPClassType root, IIndexFileSet fileSet, HashMap<IScope, BaseClassLookup> infoMap, int depth) {
		if (depth++ > CPPSemantics.MAX_INHERITANCE_DEPTH)
			return null;
	
		if (baseClassScope != null) {
			BaseClassLookup info= infoMap.get(baseClassScope);
			if (info != null) {
				// avoid loops
				if (info.getResult() == null) {
				    data.problem = new ProblemBinding(null, IProblemBinding.SEMANTIC_CIRCULAR_INHERITANCE,
				    		root.getNameCharArray());
				    return null;
				}
				return info;
			}
		}
	
		// this is the first time to handle the class
		BaseClassLookup result;
		IBinding[] matches= IBinding.EMPTY_BINDING_ARRAY;
		if (baseClassScope == null) {
			result= new BaseClassLookup(root);
			try {
				infoMap.put(root.getCompositeScope(), result);
			} catch (DOMException e) {
				// ignore
			}
		} else {
			result= new BaseClassLookup(baseClassScope.getClassType());
			infoMap.put(baseClassScope, result);
			try {
				IBinding[] members= CPPSemantics.getBindingsFromScope(baseClassScope, fileSet, data);
				if (data.typesOnly) {
					CPPSemantics.removeObjects(members);
				}
				if (members != null && members.length > 0 && members[0] != null) {
					if (data.prefixLookup) {
						matches= members;
					} else {
						result.setResult(members);
						return result;
					}
				}
			} catch (DOMException e) {
				// continue the lookup
			}
		}
		
		// There is no result in the baseClass itself or we do content assist, we have to examine its
		// base-classes
		ICPPClassType baseClass= result.getClassType();
		if (baseClass != null) { 
			ICPPBase[] grandBases= null;
			try {
				grandBases= baseClass.getBases();
			} catch (DOMException e) {
				// assume that there are no bases
			}
			if (grandBases != null && grandBases.length > 0) {
				HashSet<IBinding> grandBaseBindings= null;
				BitSet selectedBases= null;
				if (grandBases.length > 1) {
					grandBaseBindings= new HashSet<IBinding>();

					// if we have reachable bases, then ignore the others
					selectedBases = selectPreferredBases(data, grandBases);
				}
				for (int i = 0; i < grandBases.length; i++) {
					ICPPBase grandBase = grandBases[i];
					if (grandBase instanceof IProblemBinding)
						continue;
					if (selectedBases != null && !selectedBases.get(i))
						continue;
	
					try {
						IBinding grandBaseBinding = grandBase.getBaseClass();
						if (!(grandBaseBinding instanceof ICPPClassType)) {
							// 14.6.2.3 scope is not examined 
							if (grandBaseBinding instanceof ICPPUnknownBinding) {
								if (data.skippedScope == null)
									data.skippedScope= root;
							}
							continue;
						}
	
						final ICPPClassType grandBaseClass = (ICPPClassType) grandBaseBinding;
						if (grandBaseBindings != null && !grandBaseBindings.add(grandBaseClass))
							continue;
	
						final IScope grandBaseScope= grandBaseClass.getCompositeScope();
						if (grandBaseScope == null || grandBaseScope instanceof ICPPInternalUnknownScope) {
							// 14.6.2.3 scope is not examined 
							if (data.skippedScope == null)
								data.skippedScope= root;
							continue;
						}
						if (!(grandBaseScope instanceof ICPPClassScope))
							continue;
						
						BaseClassLookup baseInfo= lookupInBaseClass(data, (ICPPClassScope) grandBaseScope,
								grandBase.isVirtual(), root, fileSet, infoMap, depth);
						if (baseInfo != null)
							result.addBase(grandBase.isVirtual(), baseInfo);
					} catch (DOMException e) {
						// move on to next base
					}
				}
			}
		}
		result.setResult(matches);
		return result;	
	}

	private static BitSet selectPreferredBases(LookupData data, ICPPBase[] grandBases) {
		if (data.contentAssist) 
			return null;

		BitSet selectedBases;
		selectedBases= new BitSet(grandBases.length);
		IName baseName= null;
		for (int i = 0; i < grandBases.length; i++) {
			ICPPBase nbase = grandBases[i];
			if (nbase instanceof IProblemBinding) 
				continue;

			final IName nbaseName = nbase.getBaseClassSpecifierName();
			int cmp= baseName == null ? -1 : CPPSemantics.compareByRelevance(data, baseName, nbaseName);
			if (cmp <= 0) {
				if (cmp < 0) {
					selectedBases.clear();
					baseName= nbaseName;
				}
				selectedBases.set(i);
			}
		}
		return selectedBases;
	}

	static void hideVirtualBases(BaseClassLookup rootInfo, HashMap<IScope, BaseClassLookup> infoMap) {
		boolean containsVirtualBase= false;
		final BaseClassLookup[] allInfos = infoMap.values().toArray(new BaseClassLookup[infoMap.size()]);
		for (BaseClassLookup info : allInfos) {
			if (info.containsVirtualBase()) {
				containsVirtualBase= true;
				break;
			}
		}
		if (containsVirtualBase) {
			for (BaseClassLookup info : allInfos) {
				if (info.hasMatches()) {
					info.hideVirtualBases(infoMap, 0);
				}
			}
		}
	}

	void hideVirtualBases(HashMap<IScope, BaseClassLookup> infoMap, int depth) {
		if (depth++ > CPPSemantics.MAX_INHERITANCE_DEPTH)
			return;
		
		if (fClassType != null) { 
			ICPPBase[] bases= null;
			try {
				bases= fClassType.getBases();
			} catch (DOMException e) {
				// assume that there are no bases
			}
			if (bases != null && bases.length > 0) {
				for (ICPPBase base : bases) {
					if (base instanceof IProblemBinding)
						continue;
	
					try {
						IBinding baseBinding = base.getBaseClass();
						if (!(baseBinding instanceof ICPPClassType)) {
							continue;
						}
	
						final ICPPClassType baseClass = (ICPPClassType) baseBinding;
						final IScope baseScope= baseClass.getCompositeScope();
						if (!(baseScope instanceof ICPPClassScope))
							continue;
						
						BaseClassLookup baseInfo= infoMap.get(baseScope);
						if (baseInfo != null) {
							if (base.isVirtual()) {
								baseInfo.setHiddenAsVirtualBase();
							}
							baseInfo.propagateHiddenAsVirtual();
						} else {
							// mark to catch recursions
							baseInfo= new BaseClassLookup(baseClass);
							infoMap.put(baseScope, baseInfo);
							baseInfo.hideVirtualBases(infoMap, depth);
						}
					} catch (DOMException e) {
						// move on to next base
					}
				}
			}
		}
	}
	public void collectResultForContentAssist(LookupData data) {
		if (fCollected)
			return;
		fCollected= true;
		
		data.foundItems = CPPSemantics.mergePrefixResults((CharArrayObjectMap) data.foundItems, fBindings, true);
		for (int i= 0; i < fChildren.size(); i++) {
			BaseClassLookup child = fChildren.get(i);
			child.collectResultForContentAssist(data);
		}
	}

	public IBinding[] collectResult(LookupData data, boolean asVirtualBase, IBinding[] result) {
		if (asVirtualBase) {
			if (fHiddenAsVirtualBase)
				return result;
		} else {
			if (fCollectedAsRegularBase && data.problem == null && containsNonStaticMember()) {
				data.problem= new ProblemBinding(data.astName, IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP);
			}
			fCollectedAsRegularBase= true;
		}

		if (fCollected)
			return result;
		fCollected= true;
		
		result= (IBinding[]) ArrayUtil.addAll(IBinding.class, result, fBindings);
		for (int i= 0; i < fChildren.size(); i++) {
			BaseClassLookup child = fChildren.get(i);
			result= child.collectResult(data, fVirtual.get(i), result);
		}
		return result;
	}
	
	static void verifyResult(LookupData data, IBinding[] bindings) {
		bindings= (IBinding[]) ArrayUtil.trim(IBinding.class, bindings);
		if (bindings.length == 0)
			return;
		
		if (data.problem != null) {
			data.problem.setCandidateBindings(bindings);
		} else {
			ICPPClassType uniqueOwner= null;
			for (IBinding b : bindings) {
				if (!(b instanceof IType)) {
					try {
						IBinding owner= b.getOwner();
						if (owner instanceof ICPPClassType) {
							final ICPPClassType classOwner = (ICPPClassType) owner;
							if (uniqueOwner == null) {
								uniqueOwner= classOwner;
							} else if (!uniqueOwner.isSameType(classOwner)) {
								data.problem= new ProblemBinding(data.astName,
										IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, bindings);
								return;
							}
						}
					} catch (DOMException e) {
						// ignore
					}
				}
			}
		}

		data.foundItems = ArrayUtil.addAll(Object.class, (Object[]) data.foundItems, bindings);
	}
}