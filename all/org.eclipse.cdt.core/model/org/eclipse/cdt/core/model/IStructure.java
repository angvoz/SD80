/**********************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.model;

/**
 * Represent struct(ure), class or union.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IStructure extends IInheritance, IParent, IStructureDeclaration {
	public IField getField(String name);
	
	/**
	 * Returns the fields of a structure. 
	 * @return an array of IField elements
	 * @throws CModelException
	 */
	public IField[] getFields() throws CModelException;

	/**
	 * Returns the specific method with the given name within the structure.
	 * Returns the first occurance more than one method has the same name.
	 * @param name
	 * @return IMethodDeclaration
	 */
	public IMethodDeclaration getMethod(String name);
	
	/**
	 * Returns all methods within the structure.
	 * @return array of IMethodDeclaration.
	 * @throws CModelException
	 */
	public IMethodDeclaration [] getMethods() throws CModelException;

	/**
	 * Checks if the structure is abstract
	 * @return boolean
	 * @throws CModelException
	 */
	public boolean isAbstract() throws CModelException;
}
