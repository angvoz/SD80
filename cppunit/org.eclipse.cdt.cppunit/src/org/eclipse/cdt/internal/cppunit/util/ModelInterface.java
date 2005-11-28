/*
 * (c) Copyright IBM Corp. 2003.
 * Created on 27 févr. 03
 * All Rights Reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 *
 * Contributors:
 * IBM Software Group - Rational Software
 */

package org.eclipse.cdt.internal.cppunit.util;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.core.model.ITranslationUnit;

public class ModelInterface
{
	/*
	 * Given a C Project, return the IStructure
	 * named className. The corresponding file is getParent()
	 */
	public static IStructure findFile(ICElement p, String className) {
		try {
			ICElement [] elements;
			if(p instanceof ICProject) {
				ICProject project=(ICProject)p;
				elements=project.getChildren();
				for(int i=0;i<elements.length;i++) {
					IStructure s=findFile(elements[i],className);
					if(s!=null) return(s);
				}
			}
			if(p instanceof ICContainer) {
				ICContainer dir=(ICContainer)p;
				elements=dir.getChildren();
				for(int i=0;i<elements.length;i++) {
					IStructure s=findFile(elements[i],className);			
					if(s!=null) return(s);
				}
			}
			if(p instanceof ITranslationUnit) {
				ITranslationUnit f=(ITranslationUnit)p;
				elements=f.getChildren();
				for(int i=0;i<elements.length;i++) {
					IStructure s=findFile(elements[i],className);			
					if(s!=null) return(s);
				}
			}
			if(p instanceof IStructure) {
				IStructure s=(IStructure)p;
				if(s.getElementName().equals(className)) {
					return s;
				}
			}
		}
		catch(CModelException e){
			CppUnitLog.error("",e); //$NON-NLS-1$
		}
		return null;
	}
	/*
	 * Return an IMethod, contained in the class className, and
	 * which name is methodName
	 */
	public static IFunctionDeclaration findMethod(IStructure className,String methodName) {
		try {
			ICElement elements[]=className.getChildren();
			for(int i=0;i<elements.length;i++) {
				if(elements[i] instanceof IFunctionDeclaration) {
					IFunctionDeclaration m=(IFunctionDeclaration)elements[i];
					if(m.getElementName().equals(methodName))
						return m;
				}
			}
//	 Returning NULL !! Gors bug ??
//			IMethod method=className.getMethod(methodName);
//			if(method!=null && method.exists())
//				return method;
		} catch(CModelException e) {
			CppUnitLog.error("",e); //$NON-NLS-1$
			return null;
		}
		return null;
	}
}
