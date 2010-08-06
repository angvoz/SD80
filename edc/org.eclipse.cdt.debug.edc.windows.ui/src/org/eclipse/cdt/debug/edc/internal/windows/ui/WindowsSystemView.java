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
package org.eclipse.cdt.debug.edc.internal.windows.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.edc.IEDCConstants;
import org.eclipse.cdt.debug.edc.internal.ui.views.SystemDMContainer;
import org.eclipse.cdt.debug.edc.internal.ui.views.SystemVMContainer;
import org.eclipse.cdt.debug.edc.internal.ui.views.SystemView;
import org.eclipse.cdt.debug.edc.internal.ui.views.SystemViewModel;
import org.eclipse.cdt.debug.edc.internal.ui.views.TCFDataModel;
import org.eclipse.cdt.debug.edc.tcf.extension.ProtocolConstants;
import org.eclipse.cdt.internal.ui.util.StringMatcher;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.services.IProcesses.ProcessContext;

@SuppressWarnings("restriction")
public class WindowsSystemView extends SystemView {

	public static final String VIEW_ID = "org.eclipse.cdt.debug.edc.windows.system";

	public class WindowsDataModel extends TCFDataModel{

		private SystemDMContainer processes = new SystemDMContainer();
		private int contextCountdown;

		public WindowsDataModel() {
			super();
			getPeerAttributes().put(IEDCConstants.PEER_ATTR_DEBUG_SUPPORT, "Win32 Debug API");
		}

		@Override
		public void buildDataModel(IProgressMonitor monitor) throws Exception {
			processes = new SystemDMContainer();
			super.buildDataModel(monitor);
		}

		@Override
		public IPeer choosePeer(IPeer[] runningPeers) {
			if (runningPeers.length > 0)
			 return runningPeers[0];
			return null;
		}

		@Override
		protected void receiveContextIDs(String parentID, String[] context_ids) {
			contextCountdown = context_ids.length;
			for (int i = 0; i < context_ids.length; i++) {
				try {
					getProcessContextInfo(context_ids[i]);
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		@Override
		protected void receiveContextInfo(ProcessContext context) {
			new SystemDMContainer(getProcesses(), context.getProperties());
			setBuildComplete(--contextCountdown <= 0);
		}

		public SystemDMContainer getProcesses() {
			return processes;
		}

	}

	public class WindowsViewModel extends SystemViewModel {

		private List<SystemVMContainer> rootVMContainers = new ArrayList<SystemVMContainer>();

		@Override
		public void buildViewModel() {
			rootVMContainers = new ArrayList<SystemVMContainer>();
			rootVMContainers.add(getOverviewVMContainer());
		}

		private SystemVMContainer getOverviewVMContainer() {
			SystemVMContainer root = new SystemVMContainer(null, "Overview");
			root.getProperties().put(SystemVMContainer.PROP_ID, getPresentationContext().getId() + "_overview");
			SystemVMContainer processesVMC = new SystemVMContainer(root, "Processes");

			StringMatcher  matcher = getFilterMatcher();

			for (SystemDMContainer process : ((WindowsDataModel) getDataModel()).getProcesses().getChildren()) {
				if (matcher.match(process.getName()))
					new SystemVMContainer(processesVMC, process);
			}

			return root;
		}

		@Override
		public List<SystemVMContainer> getRootContainers() {
			return rootVMContainers;
		}
		
	}

	@Override
	public void createPartControl(Composite parent) {
		setPresentationContext(new PresentationContext(VIEW_ID));
		setDataModel(new WindowsDataModel());
		setViewModel(new WindowsViewModel());
		getViewModel().buildViewModel();
		createRootComposite(parent);
		createRefreshAction();
		createAttachAction();
		hookContextMenu();
		contributeToActionBars();
		getRefreshJob().schedule();
	}

	protected void doAttach(SystemVMContainer target) {
		Map<String, Object> targetProps = target.getDMContainer().getProperties();
		int pid = Integer.parseInt((String) targetProps.get(ProtocolConstants.PROP_OS_ID));
		
		ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType launchType = lm.getLaunchConfigurationType(ICDTLaunchConfigurationConstants.ID_LAUNCH_C_ATTACH);
		if (launchType != null) {
			String lcName = lm.generateLaunchConfigurationName(target.getName());
			try {
				ILaunchConfigurationWorkingCopy attachLaunchConfigWC = launchType.newInstance(null, lcName);
				
				attachLaunchConfigWC.setAttribute(ICDTLaunchConfigurationConstants.ATTR_ATTACH_PROCESS_ID, pid);
				attachLaunchConfigWC.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE, ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH);
				final ILaunchConfiguration attachLaunchConfig = attachLaunchConfigWC.doSave();
				Job launchJob = new Job("Launching " + attachLaunchConfig.getName()) {

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						try {
							attachLaunchConfig.launch(ILaunchManager.DEBUG_MODE, new NullProgressMonitor(), false, true);
						} catch (CoreException e) {
							WindowsDebuggerUI.getMessageLogger().logError(null, e);
							return Status.CANCEL_STATUS;
						}
						return Status.OK_STATUS;
					}
				};
				launchJob.schedule();
				
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
