/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.ast.complete;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.internal.core.parser.pst.TypeInfo;

/**
 * @author hamer
 *
 */
public class ExpressionResultList extends ExpressionResult {
	private List resultList = new ArrayList();
	ExpressionResultList(){
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ExpressionResult#getResult()
	 */
	public TypeInfo getResult() {
		// TODO Auto-generated method stub
		return (TypeInfo)resultList.get(0);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ExpressionResult#setResult(org.eclipse.cdt.internal.core.parser.pst.TypeInfo)
	 */
	public void setResult(TypeInfo info) {
		// TODO Auto-generated method stub
		resultList.add(info);
	}

	/**
	 * @return
	 */
	public List getResultList() {
		return resultList;
	}

	/**
	 * @param list
	 */
	public void setResultList(List list) {
		resultList = list;
	}

}
