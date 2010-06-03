/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.services;

import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.dsf.debug.service.IModules.ISymbolDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;

public interface IEDCExecutionDMC extends IExecutionDMContext,IMemoryDMContext, IEDCDMContext {

	public boolean isSuspended();
	
	public ISymbolDMContext getSymbolDMContext();
	
}