/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegister;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.CdiResources;
import org.eclipse.cdt.debug.mi.core.cdi.MI2CDIException;
import org.eclipse.cdt.debug.mi.core.cdi.RegisterManager;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIVarListChildren;
import org.eclipse.cdt.debug.mi.core.output.MIVar;
import org.eclipse.cdt.debug.mi.core.output.MIVarListChildrenInfo;

/**
 */
public class Register extends Variable implements ICDIRegister {

	public Register(RegisterObject obj, MIVar var) {
		super(obj, var);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.core.cdi.model.VariableObject#getFullName()
	 */
	public String getFullName() {
		if (fullName == null) {
			String n = getName();
			if (!n.startsWith("$")) { //$NON-NLS-1$
				fullName = "$" + n; //$NON-NLS-1$
			}
		}
		return fullName;
	}

	public ICDIVariable[] getChildren() throws CDIException {
			Session session = (Session)(getTarget().getSession());
			MISession mi = session.getMISession();
			RegisterManager mgr = (RegisterManager)session.getRegisterManager();
			CommandFactory factory = mi.getCommandFactory();
			MIVarListChildren var = 
			factory.createMIVarListChildren(getMIVar().getVarName());
			try {
				mi.postCommand(var);
				MIVarListChildrenInfo info = var.getMIVarListChildrenInfo();
				if (info == null) {
					throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
				}
				MIVar[] vars = info.getMIVars();
				children = new Register[vars.length];
				for (int i = 0; i < vars.length; i++) {
					String fn;
					String exp = vars[i].getExp();
					if (isCPPLanguage()) {
						if ((exp.equals("private") || exp.equals("public") || exp.equals("protected"))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							fn = getFullName();
						} else {
							fn = getFullName() + "." + exp;
						}
					} else {
						fn = getFullName() + "." + exp;
					}
					RegisterObject regObj = new RegisterObject(getTarget(),
					 exp, fn, getPosition());
					children[i] = mgr.createRegister(regObj, vars[i]);
				}
			} catch (MIException e) {
				throw new MI2CDIException(e);
			}
			return children;
	}

}
