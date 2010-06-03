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
package org.eclipse.cdt.debug.edc.ui;

import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl.ThreadExecutionDMC;
import org.eclipse.cdt.debug.edc.internal.ui.EDCDebugUI;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMData;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.AbstractThreadVMNode;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.ILaunchVMConstants;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.concurrent.ViewerCountingRequestMonitor;
import org.eclipse.cdt.dsf.ui.concurrent.ViewerDataRequestMonitor;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.IPropertiesUpdate;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.VMDelegatingPropertiesUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest;
import org.eclipse.ui.IMemento;

@SuppressWarnings("restriction")
public class ThreadVMNode extends AbstractThreadVMNode implements IElementLabelProvider, IElementMementoProvider {
	public ThreadVMNode(AbstractDMVMProvider provider, DsfSession session) {
		super(provider, session);
	}

	@Override
	public String toString() {
		return "ThreadVMNode(" + getSession().getId() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	protected void updatePropertiesInSessionThread(IPropertiesUpdate[] updates) {
		IPropertiesUpdate[] parentUpdates = new IPropertiesUpdate[updates.length];

		for (int i = 0; i < updates.length; i++) {
			final IPropertiesUpdate update = updates[i];

			final ViewerCountingRequestMonitor countringRm = new ViewerCountingRequestMonitor(ImmediateExecutor
					.getInstance(), updates[i]);
			int count = 0;

			// Create a delegating update which will let the super-class fill in
			// the
			// standard container properties.
			parentUpdates[i] = new VMDelegatingPropertiesUpdate(updates[i], countringRm);
			count++;

			IProcesses processService = getServicesTracker().getService(IProcesses.class);
			final IThreadDMContext threadDMC = findDmcInPath(update.getViewerInput(), update.getElementPath(),
					IThreadDMContext.class);

			if (processService == null || threadDMC == null) {
				update.setStatus(EDCDebugUI.newErrorStatus(IDsfStatusConstants.INVALID_HANDLE,
						"Service or handle invalid", null)); //$NON-NLS-1$
			} else {
				processService.getExecutionData(threadDMC, new ViewerDataRequestMonitor<IThreadDMData>(getExecutor(),
						update) {
					@Override
					public void handleCompleted() {
						if (isSuccess()) {
							fillThreadDataProperties(update, getData());
						} else {
							final ThreadExecutionDMC execDmc = findDmcInPath(update.getViewerInput(), update
									.getElementPath(), ThreadExecutionDMC.class);
							if (execDmc != null) {
								update.setProperty(ILaunchVMConstants.PROP_ID, execDmc.getID());
							} else {
								update.setStatus(getStatus());
							}
						}
						countringRm.done();
					}
				});
				count++;
			}

			countringRm.setDoneCount(count);
		}
		super.updatePropertiesInSessionThread(parentUpdates);
	}

	protected void fillThreadDataProperties(IPropertiesUpdate update, IThreadDMData data) {
		update.setProperty(PROP_NAME, data.getName());
		update.setProperty(ILaunchVMConstants.PROP_ID, "Thread id: " + data.getId());
	}

	private String produceThreadElementName(String viewName, ThreadExecutionDMC execCtx) {
		return "Thread." + execCtx.getID(); //$NON-NLS-1$
	}

	private static final String MEMENTO_NAME = "THREAD_MEMENTO_NAME"; //$NON-NLS-1$

	/*
	 * @seeorg.eclipse.debug.internal.ui.viewers.model.provisional.
	 * IElementMementoProvider
	 * #compareElements(org.eclipse.debug.internal.ui.viewers
	 * .model.provisional.IElementCompareRequest[])
	 */
	public void compareElements(IElementCompareRequest[] requests) {

		for (IElementCompareRequest request : requests) {

			Object element = request.getElement();
			IMemento memento = request.getMemento();
			String mementoName = memento.getString(MEMENTO_NAME);

			if (mementoName != null) {
				if (element instanceof IDMVMContext) {

					IDMContext dmc = ((IDMVMContext) element).getDMContext();

					if (dmc instanceof ThreadExecutionDMC) {

						String elementName = produceThreadElementName(request.getPresentationContext().getId(),
								(ThreadExecutionDMC) dmc);
						request.setEqual(elementName.equals(mementoName));
					}
				}
			}
			request.done();
		}
	}

	/*
	 * @seeorg.eclipse.debug.internal.ui.viewers.model.provisional.
	 * IElementMementoProvider
	 * #encodeElements(org.eclipse.debug.internal.ui.viewers
	 * .model.provisional.IElementMementoRequest[])
	 */
	public void encodeElements(IElementMementoRequest[] requests) {

		for (IElementMementoRequest request : requests) {

			Object element = request.getElement();
			IMemento memento = request.getMemento();

			if (element instanceof IDMVMContext) {

				IDMContext dmc = ((IDMVMContext) element).getDMContext();

				if (dmc instanceof ThreadExecutionDMC) {

					String elementName = produceThreadElementName(request.getPresentationContext().getId(),
							(ThreadExecutionDMC) dmc);
					memento.putString(MEMENTO_NAME, elementName);
				}
			}
			request.done();
		}
	}

}
