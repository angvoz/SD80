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

package org.eclipse.cdt.debug.edc.services;

import org.eclipse.cdt.dsf.service.IDsfService;
import org.eclipse.tm.tcf.protocol.IService;

/**
 * This is used to link a TCF service to the DSF service that needs it. Objects
 * that implement {@link IDsfService} using TCF should implement this as well.
 * 
 * @author LWang
 * 
 */
public interface IDSFServiceUsingTCF {
	public void tcfServiceReady(IService service);
}
