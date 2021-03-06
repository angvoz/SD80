/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.ui;

import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.debug.edc.internal.snapshot.Album;
import org.eclipse.cdt.debug.edc.internal.ui.EDCDebugUI;
import org.eclipse.cdt.debug.edc.services.IEDCDMContext;
import org.eclipse.cdt.debug.edc.services.IEDCExecutionDMC;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelSelectionPolicy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;

@SuppressWarnings("restriction")
public class DefaultEDCSelectionPolicy implements IModelSelectionPolicy {

	private IDMContext fDMContext;

	/**
	 * Create selection policy instance for the given data model context.
	 * 
	 * @param dmContext
	 */
	public DefaultEDCSelectionPolicy(IDMContext dmContext) {
		fDMContext= dmContext;
	}

	/*
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IModelSelectionPolicy#contains(org.eclipse.jface.viewers.ISelection, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext)
	 */
	public boolean contains(ISelection selection, IPresentationContext context) {
		if (IDebugUIConstants.ID_DEBUG_VIEW.equals(context.getId())) {
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection ss= (IStructuredSelection) selection;
				Object element= ss.getFirstElement();
				if (element instanceof IDMVMContext) {
					IDMVMContext dmvmContext= (IDMVMContext) element;
					IDMContext dmContext= dmvmContext.getDMContext();
					if (dmContext != null) {
						return fDMContext instanceof IEDCDMContext && dmContext instanceof IEDCDMContext;
					}
				}
			}
		}
		return false;
	}

	/*
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IModelSelectionPolicy#isSticky(org.eclipse.jface.viewers.ISelection, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext)
	 */
	public boolean isSticky(ISelection selection, IPresentationContext context) {
		if (IDebugUIConstants.ID_DEBUG_VIEW.equals(context.getId())) {
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection ss= (IStructuredSelection) selection;
				Object element= ss.getFirstElement();
				return isSticky(element);
			}
		}
		return false;
	}

	protected boolean isSticky(Object element) {
		if (element instanceof IDMVMContext) {
			IDMVMContext dmvmContext= (IDMVMContext) element;
			final IDMContext dmContext= dmvmContext.getDMContext();
			if (dmContext instanceof IFrameDMContext) {
				final IExecutionDMContext execContext= DMContexts.getAncestorOfType(dmContext, IExecutionDMContext.class);
				if (execContext != null) {
					Query<Boolean> query = new Query<Boolean>() {
						@Override
						protected void execute(DataRequestMonitor<Boolean> rm) {
							DsfServicesTracker servicesTracker = new DsfServicesTracker(EDCDebugUI.getBundleContext(), dmContext.getSessionId());
							try {
								IRunControl runControl= servicesTracker.getService(IRunControl.class);
								if (runControl != null) {
									boolean isSticky = runControl.isSuspended(execContext) || runControl.isStepping(execContext);
									if (execContext instanceof IEDCExecutionDMC) {
										IEDCExecutionDMC edcDMC = (IEDCExecutionDMC)execContext;
										if (! edcDMC.wantFocusInUI())
											isSticky = false; 
									}
									rm.setData(isSticky);
								}
							} finally {
								servicesTracker.dispose();
								rm.done();
							}
						}
					};
					DsfSession session = DsfSession.getSession(dmContext.getSessionId());
					if (session != null) {
						if (session.getExecutor().isInExecutorThread()) {
							query.run();
						} else {
							session.getExecutor().execute(query);
						}
						try {
							Boolean result = query.get();
							return result != null && result.booleanValue();
						} catch (InterruptedException exc) {
							Thread.currentThread().interrupt();
						} catch (ExecutionException exc) {
							EDCDebugUI.getMessageLogger().logError("", exc);
						}
					}
				}
			}
		}
		return false;
	}

	/*
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IModelSelectionPolicy#overrides(org.eclipse.jface.viewers.ISelection, org.eclipse.jface.viewers.ISelection, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext)
	 */
	public boolean overrides(ISelection existing, ISelection candidate, IPresentationContext context) {
		if (IDebugUIConstants.ID_DEBUG_VIEW.equals(context.getId())) {
			if (existing instanceof IStructuredSelection && candidate instanceof IStructuredSelection) {
				IStructuredSelection ssExisting = (IStructuredSelection) existing;
				IStructuredSelection ssCandidate = (IStructuredSelection) candidate;
				return overrides(ssExisting.getFirstElement(), ssCandidate.getFirstElement());
			}
		}
		return true;
	}

	
	protected boolean overrides(Object existing, Object candidate) {
		if (existing == null || existing.equals(candidate)) {
			return true;
		}
		if (existing instanceof IDMVMContext && candidate instanceof IDMVMContext) {
			IDMContext curr = ((IDMVMContext) existing).getDMContext();
			IDMContext cand = ((IDMVMContext) candidate).getDMContext();
			if (curr instanceof IFrameDMContext && cand instanceof IFrameDMContext) {
				IExecutionDMContext currExecContext= DMContexts.getAncestorOfType(curr, IExecutionDMContext.class);
				if (currExecContext != null) {
					IExecutionDMContext candExecContext= DMContexts.getAncestorOfType(cand, IExecutionDMContext.class);
					boolean isSnapshotSession = Album.isSnapshotSession(candExecContext.getSessionId());					
					return currExecContext.equals(candExecContext) || isSnapshotSession || !isSticky(existing);
				}
			}
		}
		return !isSticky(existing);
	}

	/*
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IModelSelectionPolicy#replaceInvalidSelection(org.eclipse.jface.viewers.ISelection, org.eclipse.jface.viewers.ISelection)
	 */
	public ISelection replaceInvalidSelection(ISelection invalidSelection, ISelection newSelection) {
        if (invalidSelection instanceof ITreeSelection) {
            ITreeSelection treeSelection = (ITreeSelection)invalidSelection;
            if (treeSelection.getPaths().length == 1) {
                TreePath path = treeSelection.getPaths()[0];
                if (path.getSegmentCount() > 1) {
                    return new TreeSelection(path.getParentPath());
                }
            }
        }
        return newSelection;
	}

}
