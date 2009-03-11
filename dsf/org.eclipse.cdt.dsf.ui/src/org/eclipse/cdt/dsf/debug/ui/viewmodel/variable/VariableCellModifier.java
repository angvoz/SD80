/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.variable;

import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.IDebugVMConstants;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.WatchExpressionCellModifier;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat.FormattedValueVMUtil;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.update.AbstractCachingVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.update.UserEditEvent;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

public class VariableCellModifier extends WatchExpressionCellModifier {
    
    private AbstractCachingVMProvider fProvider;
    private SyncVariableDataAccess fDataAccess = null;
    
    public VariableCellModifier(AbstractCachingVMProvider provider, SyncVariableDataAccess access) 
    {
        fProvider = provider;
        fDataAccess = access;
    }
    
    /*
     *  Used to make sure we are dealing with a valid variable.
     */
    private IExpressionDMContext getVariableDMC(Object element) {
        if (element instanceof IAdaptable) {
            return (IExpressionDMContext)((IAdaptable)element).getAdapter(IExpressionDMContext.class);
        }
        return null;
    }

    @Override
    public boolean canModify(Object element, String property) {
        // If we're in the column value, modify the register data.  Otherwise, call the super-class to edit
        // the watch expression.

        if (IDebugVMConstants.COLUMN_ID__VALUE.equals(property)) { 
            // Make sure we are are dealing with a valid set of information.
                
            if (getVariableDMC(element) == null) {
                return false;
            }
            
           return fDataAccess.canWriteExpression(element);
        }

        return super.canModify(element, property);
    }

    @Override
    public Object getValue(Object element, String property) {
        // If we're in the column value, modify the variable value.  Otherwise, call the super-class to edit
        // the watch expression.

        if (IDebugVMConstants.COLUMN_ID__VALUE.equals(property)) {
            /*
             *  We let the Model provider supply the current format.
             */
            String formatId;
            
            if ( element instanceof IVMContext) {
                /*
                 *  Find the presentation context and then use it to get the current desired format.
                 */
                IVMContext ctx = (IVMContext) element;
                IPresentationContext presCtx = ctx.getVMNode().getVMProvider().getPresentationContext();
                formatId = FormattedValueVMUtil.getPreferredFormat(presCtx);
            }
            else {
                formatId = IFormattedValues.NATURAL_FORMAT;
            }
            
            String value = fDataAccess.getFormattedValue(element, formatId);
            
            if (value == null) {
                return "...";  //$NON-NLS-1$
            }

            return value;
        }

        return super.getValue(element, property);
    }

    @Override
    public void modify(Object element, String property, Object value) {
        /* 
         * If we're in the column value, modify the register data.  Otherwise, call the super-class to edit
         * the watch expression.
         */ 
        if (IDebugVMConstants.COLUMN_ID__VALUE.equals(property)) {
            if (value instanceof String) {
                /*
                 *  We let the Model provider supply the current format.
                 */
                String formatId;
                
                if ( element instanceof IVMContext) {
                    /*
                     *  Find the presentation context and then use it to get the current desired format.
                     */
                    IVMContext ctx = (IVMContext) element;
                    IPresentationContext presCtx = ctx.getVMNode().getVMProvider().getPresentationContext();
                    formatId = FormattedValueVMUtil.getPreferredFormat(presCtx);
                }
                else {
                    formatId = IFormattedValues.NATURAL_FORMAT;
                }
                
                fDataAccess.writeVariable(element, (String) value, formatId);
                fProvider.handleEvent(new UserEditEvent(element));
            }
        }
        else {
            super.modify(element, property, value);
        }
    }

}
