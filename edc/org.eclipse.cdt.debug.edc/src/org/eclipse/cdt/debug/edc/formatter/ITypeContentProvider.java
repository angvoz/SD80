/*******************************************************************************
 * Copyright (c) 2009, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.formatter;

import java.util.Iterator;

import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.core.runtime.CoreException;



/**
 * An interface describing structure for a type
 */
public interface ITypeContentProvider {
	
	/**
	 * Return an iterator starting at a start index generating the
	 * list of IExpressionDMContext for each of the direct children 
	 * of the current object.
	 * @param variable IExpressionDMContext
	 * @return Iterator<IExpressionDMContext> 
	 * @throws CoreException on errors
	 */
	Iterator<IExpressionDMContext> getChildIterator(IExpressionDMContext variable) throws CoreException;
}
