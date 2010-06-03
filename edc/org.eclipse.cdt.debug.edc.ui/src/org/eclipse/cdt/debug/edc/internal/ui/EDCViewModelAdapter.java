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
package org.eclipse.cdt.debug.edc.internal.ui;

import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.debug.ui.IDsfDebugUIConstants;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.AbstractDebugVMAdapter;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.SteppingController;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.breakpoints.BreakpointVMProvider;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.ExpressionVMProvider;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.modules.ModulesVMProvider;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.register.RegisterVMProvider;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.VariableVMProvider;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;

/* 
 * 
 */
@ThreadSafe
@SuppressWarnings("restriction")
public class EDCViewModelAdapter extends AbstractDebugVMAdapter {
	public EDCViewModelAdapter(DsfSession session, SteppingController controller) {
		super(session, controller);
		getSession().registerModelAdapter(IColumnPresentationFactory.class, this);
	}

	@Override
	public void dispose() {
		getSession().unregisterModelAdapter(IColumnPresentationFactory.class);
		super.dispose();
	}

	@Override
	protected IVMProvider createViewModelProvider(IPresentationContext context) {
		if (IDebugUIConstants.ID_DEBUG_VIEW.equals(context.getId())) {
			return new LaunchVMProvider(this, context, getSession());
		} else if (IDebugUIConstants.ID_VARIABLE_VIEW.equals(context.getId())) {
			return new VariableVMProvider(this, context, getSession());
		} else if (IDebugUIConstants.ID_REGISTER_VIEW.equals(context.getId())) {
			return new RegisterVMProvider(this, context, getSession());
		} else if (IDebugUIConstants.ID_EXPRESSION_VIEW.equals(context.getId())) {
			return new ExpressionVMProvider(this, context, getSession());
        } else if (IDsfDebugUIConstants.ID_EXPRESSION_HOVER.equals(context.getId()) ) {
            return new ExpressionVMProvider(this, context, getSession());
		} else if (IDebugUIConstants.ID_MODULE_VIEW.equals(context.getId())) {
			return new ModulesVMProvider(this, context, getSession());
		} else if (IDebugUIConstants.ID_BREAKPOINT_VIEW.equals(context.getId())) {
			return new BreakpointVMProvider(this, context);
		}
		return null;
	}
}
