package org.eclipse.cdt.internal.core.model;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
***********************************************************************/

import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;

public class MethodInfo extends FunctionInfo {

	boolean isAbstract = false;
	boolean isInline = false;
	boolean isVirtual = false;
	boolean isFriend = false;
	boolean isConst = false;
	ASTAccessVisibility visibility = null;
		
	MethodInfo(CElement element) {
		super(element);
		visibility = ASTAccessVisibility.PRIVATE;
	}
	
	public boolean isAbstract(){
		return isAbstract;
	}

	public void setAbstract(boolean isAbstract){
		this.isAbstract = isAbstract;
	}

	public boolean isInline(){
		return isInline;
	}

	public void setInline(boolean isInline){
		this.isInline = isInline;
	}

	public boolean isVirtual(){
		return isVirtual;
	}

	public void setVirtual(boolean isVirtual){
		this.isVirtual = isVirtual;
	}

	public boolean isFriend(){
		return isFriend;
	}

	public void setFriend(boolean isFriend){
		this.isFriend = isFriend;
	}

	public boolean isConst(){
		return isConst;
	}

	public void setConst(boolean isConst){
		this.isConst = isConst;
	}
	
	/**
	 * Returns the visibility.
	 * @return int
	 */
	public ASTAccessVisibility getVisibility() {
		return visibility;
	}

	/**
	 * Sets the visibility.
	 * @param visibility The visibility to set
	 */
	public void setVisibility(ASTAccessVisibility visibility) {
		this.visibility = visibility;
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.SourceManipulationInfo#hasSameContentsAs(org.eclipse.cdt.internal.core.model.SourceManipulationInfo)
	 */
	public boolean hasSameContentsAs(SourceManipulationInfo otherInfo) {
		return (super.hasSameContentsAs(otherInfo)
		&&  (isConst == ((MethodInfo)otherInfo).isConst())
		&&  (isAbstract == ((MethodInfo)otherInfo).isAbstract())
		&& 	(isInline == ((MethodInfo)otherInfo).isInline())
		&& 	(isVirtual == ((MethodInfo)otherInfo).isVirtual())
		&& 	(isFriend == ((MethodInfo)otherInfo).isFriend())
		&& 	(visibility == ((MethodInfo)otherInfo).getVisibility())
		);
	}

}
