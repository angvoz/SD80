/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.pda.service.commands;

import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.examples.dsf.pda.service.PDAThreadDMContext;

/**
 * Retrieves variable value 
 * 
 * <pre>
 *    C: var  {thread_id} {frame_number} {variable_name}
 *    R: {variable_value}
 *    
 * Errors:
 *    error: invalid thread
 *    error: variable undefined
 * </pre>
 */
@Immutable
public class PDAVarCommand extends AbstractPDACommand<PDACommandResult> {

    public PDAVarCommand(PDAThreadDMContext thread, int frameId, String name) {
        super(thread, "var " + thread.getID() + " " + frameId + " " + name);
    }
    
    @Override
    public PDACommandResult createResult(String resultText) {
        return new PDACommandResult(resultText);
    }
}
