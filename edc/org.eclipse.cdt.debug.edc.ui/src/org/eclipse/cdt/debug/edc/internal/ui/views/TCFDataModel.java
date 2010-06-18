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
package org.eclipse.cdt.debug.edc.internal.ui.views;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.edc.ITCFAgentLauncher;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.TCFServiceManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IProcesses;
import org.eclipse.tm.tcf.services.IProcesses.DoneGetChildren;
import org.eclipse.tm.tcf.services.IProcesses.DoneGetContext;
import org.eclipse.tm.tcf.services.IProcesses.ProcessContext;

public abstract class TCFDataModel extends SystemDataModel {

	private IPeer peer;
	private Map<String, String> peerAttributes = new HashMap<String, String>();
	private boolean buildComplete;
	
	@Override
	public void buildDataModel(final IProgressMonitor monitor)throws Exception {
		try {
			final TCFServiceManager tcfServiceManager = (TCFServiceManager)EDCDebugger.getDefault().getServiceManager();
			if (peer == null)
				findPeer();
			
			setBuildComplete(false);
	        final IProcesses processesService = (IProcesses) ((TCFServiceManager) tcfServiceManager).getPeerService(getPeer(), IProcesses.NAME);		
			Protocol.invokeLater(new Runnable() {
				public void run() {
	                processesService.getChildren(null, false, new DoneGetChildren() {
	                        public void doneGetChildren(IToken token, Exception error,
	                        		String[] context_ids) {
	                        	receiveContextIDs(context_ids);
	                        }
	                });
				}
			    
			});
			
			while (!isBuildComplete() && !monitor.isCanceled())	{
				Thread.sleep(1000);
			}
		}
		finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	protected abstract void receiveContextIDs(String[] context_ids);

	protected void getProcessContextInfo(String contextID) throws CoreException
	{
		TCFServiceManager tcfServiceManager = (TCFServiceManager)EDCDebugger.getDefault().getServiceManager();
		IProcesses processesService = (IProcesses) ((TCFServiceManager) tcfServiceManager).getPeerService(getPeer(), IProcesses.NAME);
		processesService.getContext(contextID, new DoneGetContext() {
			
			public void doneGetContext(IToken token, Exception error,
					ProcessContext context) {
				receiveContextInfo(context);
			}
		});
	}
	
	protected abstract void receiveContextInfo(ProcessContext context);

	public Map<String, String> getPeerAttributes() {
		return peerAttributes;
	}

	public void setPeerAttributes(Map<String, String> peerAttributes) {
		this.peerAttributes = peerAttributes;
	}

	protected void findPeer() throws CoreException {
		TCFServiceManager tcfServiceManager = (TCFServiceManager)EDCDebugger.getDefault().getServiceManager();
		
		IPeer[] runningPeers = tcfServiceManager.getRunningPeers(IProcesses.NAME, peerAttributes, true);
		if (runningPeers.length > 0)
			setPeer(choosePeer(runningPeers));
		else
		{
			ITCFAgentLauncher[] registered = tcfServiceManager.getRegisteredAgents(IProcesses.NAME, peerAttributes);
			if (registered.length > 0)
			{
				IPeer tcfPeer = tcfServiceManager.launchAgent(registered[0]);
				setPeer(tcfPeer);
			}
			 
		}
		if (getPeer() == null) {
			throw new CoreException(new Status(IStatus.ERROR, EDCDebugger.getUniqueIdentifier(), "Could not find a suitable TCF peer", null));
		}
		
	}

	public abstract IPeer choosePeer(IPeer[] runningPeers);

	public IPeer getPeer() {
		return peer;
	}

	public void setPeer(IPeer peer) {
		this.peer = peer;
	}

	public void setBuildComplete(boolean buildComplete) {
		this.buildComplete = buildComplete;
	}

	public boolean isBuildComplete() {
		return buildComplete;
	}

}
