/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.CdiResources;
import org.eclipse.cdt.debug.mi.core.cdi.MI2CDIException;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIVarCreate;
import org.eclipse.cdt.debug.mi.core.command.MIVarDelete;
import org.eclipse.cdt.debug.mi.core.output.MIVar;
import org.eclipse.cdt.debug.mi.core.output.MIVarCreateInfo;


/**
 */
public class Expression extends CObject implements ICDIExpression {

	private static int ID_COUNT = 0;
	private int id;
	String fExpression;
	Variable fVariable;
	
	public Expression(Target target, String ex) {
		super(target);
		fExpression  = ex;
		id = ++ID_COUNT;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExpression#getExpressionText()
	 */
	public String getExpressionText() {
		return fExpression;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExpression#equals(org.eclipse.cdt.debug.core.cdi.model.ICDIExpression)
	 */
	public boolean equals(ICDIExpression obj) {
		if (obj instanceof Expression) {
			Expression other = (Expression)obj;
			return other.id == id;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExpression#getValue(org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame)
	 */
	public ICDIValue getValue(ICDIStackFrame context) throws CDIException {
		if (fVariable != null) {
			Target target = (Target)getTarget();
			MISession miSession = target.getMISession();
			removeMIVar(miSession, fVariable.getMIVar());
		}
		fVariable = createVariable(context);
		return fVariable.getValue();
	}

	public Variable getVariable() {
		return fVariable;
	}

	protected Variable createVariable(ICDIStackFrame frame) throws CDIException {
		Session session = (Session)getTarget().getSession();
		Target currentTarget = session.getCurrentTarget();
		ICDIThread currentThread = currentTarget.getCurrentThread();
		ICDIStackFrame currentFrame = currentThread.getCurrentStackFrame();
		Target target = (Target)frame.getTarget();
		session.setCurrentTarget(target);
		target.setCurrentThread(frame.getThread(), false);
		frame.getThread().setCurrentStackFrame(frame, false);
		try {
			MISession mi = target.getMISession();
			CommandFactory factory = mi.getCommandFactory();
			MIVarCreate var = factory.createMIVarCreate(fExpression);
			mi.postCommand(var);
			MIVarCreateInfo info = var.getMIVarCreateInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
			VariableObject varObj = new VariableObject(target, fExpression, frame, 0, 0);
			return new Variable(varObj, info.getMIVar());
		} catch (MIException e) {
			throw new MI2CDIException(e);
		} finally {
			session.setCurrentTarget(currentTarget);
			target.setCurrentThread(currentThread, false);
			currentThread.setCurrentStackFrame(currentFrame, false);
		}
	}

	/**
	 * Get rid of the underlying variable.
	 *
	 */
	public void deleteVariable() throws CDIException {
		if (fVariable != null) {
			Target target = (Target)getTarget();
			MISession miSession = target.getMISession();
			MIVar miVar = fVariable.getMIVar();
			removeMIVar(miSession, fVariable.getMIVar());
			fVariable = null;
		}
	}

	/**
	 * Tell gdb to remove the underlying var-object also.
	 */
	public void removeMIVar(MISession miSession, MIVar miVar) throws CDIException {
		CommandFactory factory = miSession.getCommandFactory();
		MIVarDelete var = factory.createMIVarDelete(miVar.getVarName());
		try {
			miSession.postCommand(var);
			var.getMIInfo();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

}
