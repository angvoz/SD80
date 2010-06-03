/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

/**
 * GDB/MI var-delete.
 */
public class MIVarDeleteInfo extends MIInfo {

    int ndeleted;

    public MIVarDeleteInfo(MIOutput record) {
        super(record);
        ndeleted=0;
        if (isDone()) {
            MIOutput out = getMIOutput();
            MIResultRecord rr = out.getMIResultRecord();
            if (rr != null) {
                MIResult[] results =  rr.getMIResults();
                for (int i = 0; i < results.length; i++) {
                    String var = results[i].getVariable();
                    if (var.equals("ndeleted")) { //$NON-NLS-1$
                        MIValue value = results[i].getMIValue();
                        if (value instanceof MIConst) {
                            String str = ((MIConst)value).getString();
                            try {
                                ndeleted = Integer.parseInt(str.trim());
                            } catch (NumberFormatException e) {
                            }
                        }
                    }
                }
            }
        }
    }

    public int getNumberDeleted () {
        return ndeleted;
    }
}
