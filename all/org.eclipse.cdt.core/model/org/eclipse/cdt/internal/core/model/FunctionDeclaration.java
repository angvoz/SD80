package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IFunctionDeclaration;

public class FunctionDeclaration extends SourceManipulation implements IFunctionDeclaration {
	/**
	 * An empty list of Strings
	 */
	protected static final String[] fgEmptyList= new String[] {};
	protected String[] fParameterTypes;
	protected String returnType;
	
	public FunctionDeclaration(ICElement parent, String name) {
		super(parent, name, CElement.C_FUNCTION_DECLARATION);
		fParameterTypes= fgEmptyList;
	}

	public FunctionDeclaration(ICElement parent, String name, int type) {
		super(parent, name, type);
		fParameterTypes= fgEmptyList;
	}

	public String getReturnType(){
		if (returnType != null)
			return returnType;
		else
			return ""; //$NON-NLS-1$
	}

	public void setReturnType(String type){
		returnType = type;
	}

	public int getNumberOfParameters() {
		return fParameterTypes == null ? 0 : fParameterTypes.length;
	}

	public String[] getParameterTypes() {
		return fParameterTypes;
	}
	
	public void setParameterTypes(String[] parameterTypes) {
		fParameterTypes = parameterTypes;
	}		
		
	public String getSignature(){
		StringBuffer sig = new StringBuffer(getElementName());
		sig.append(getParameterClause());
		if(isConst())
			sig.append(" const"); //$NON-NLS-1$
		if(isVolatile())
			sig.append(" volatile"); //$NON-NLS-1$
		return sig.toString();
	}
	
	public String getParameterClause(){
		StringBuffer sig = new StringBuffer();
		
		if(getNumberOfParameters() > 0){
			sig.append("("); //$NON-NLS-1$
			String[] paramTypes = getParameterTypes();
			int i = 0;
			sig.append(paramTypes[i++]);
			while (i < paramTypes.length){
				sig.append(", "); //$NON-NLS-1$
				sig.append(paramTypes[i++]);
			}
			sig.append(")"); //$NON-NLS-1$
		}
		else{
			sig.append("()"); //$NON-NLS-1$
		}
		return sig.toString();	
	}
	
	public String getParameterInitializer(int pos) {
		return ""; //$NON-NLS-1$
	}
	
	public String[] getExceptions(){
		return new String[] {};
	}

	protected CElementInfo createElementInfo () {
		return new FunctionInfo(this);
	}
	
	protected FunctionInfo getFunctionInfo(){
		return (FunctionInfo) getElementInfo();
	}
	
	public boolean equals(Object other) {
		// Two function declarations are equal if
		// Their parents and names are equal and
		return ( super.equals(other) 
		// their parameter types are equal and 
		&& Util.equalArraysOrNull(fParameterTypes, ((FunctionDeclaration)other).fParameterTypes)
		// their return types are equal
		&& getReturnType().equals(((FunctionDeclaration)other).getReturnType())
		);
	}
	
	/**
	 * FunctionDeclarations and Functions can not be constant 
	 * @see org.eclipse.cdt.core.model.IDeclaration#isConst()
	 */
	public boolean isConst(){
		return getFunctionInfo().isConst();
	}

	public void setConst(boolean isConst){
		getFunctionInfo().setConst(isConst);
	}

	/**
	 * Returns the isStatic.
	 * @return boolean
	 */
	public boolean isStatic() {
		return getFunctionInfo().isStatic();
	}

	/**
	 * Returns the isVolatile.
	 * @return boolean
	 */
	public boolean isVolatile() {
		return getFunctionInfo().isVolatile();
	}

	/**
	 * Sets the isStatic.
	 * @param isStatic The isStatic to set
	 */
	public void setStatic(boolean isStatic) {
		getFunctionInfo().setStatic(isStatic);
	}

	/**
	 * Sets the isVolatile.
	 * @param isVolatile The isVolatile to set
	 */
	public void setVolatile(boolean isVolatile) {
		getFunctionInfo().setVolatile(isVolatile);
	}

}
