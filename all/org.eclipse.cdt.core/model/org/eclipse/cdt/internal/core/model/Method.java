package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IMethod;

public class Method extends MethodDeclaration implements IMethod {
	
	public Method(ICElement parent, String name){
		this(parent, name, ICElement.C_METHOD);
	}

	public Method(ICElement parent, String name, int kind) {
		super(parent, name, kind);
	}
}
