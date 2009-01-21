/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICExclusionPatternPathEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.WriteAccessException;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.settings.model.util.ResourceChangeHandlerBase;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * This resource change handler notices external changes to the cdt projects
 * and associated project storage metadata files, as well as changes to
 * source folders 
 * 
 * 
 * delegates parts of it's functionality to CProjectDescriptio 
 */
public class ResourceChangeHandler extends ResourceChangeHandlerBase implements  ISaveParticipant {
	CProjectDescriptionManager fMngr = CProjectDescriptionManager.getInstance();
	
	class RcMoveHandler implements IResourceMoveHandler {
		Map<IProject, ICProjectDescription> fProjDesMap = new HashMap<IProject, ICProjectDescription>();
		Set<IProject> fRemovedProjSet = new HashSet<IProject>();

		public void handleProjectClose(IProject project) {
			fMngr.projectClosedRemove(project);
		}
		
		private ICExclusionPatternPathEntry[] checkMove(IPath fromFullPath, IPath toFullPath, ICExclusionPatternPathEntry[] entries){
			boolean modified = false;
			for(int k = 0; k < entries.length; k++){
				if(entries[k].getFullPath().equals(fromFullPath)){
					ICExclusionPatternPathEntry entry = entries[k];
					entries[k] = (ICExclusionPatternPathEntry)CDataUtil.createEntry(entry.getKind(), toFullPath.toString(), null, entry.getExclusionPatterns(), entry.getFlags());
					modified = true;
				}
			}
			return modified ? entries : null;
		}
	
		@SuppressWarnings("fallthrough")
		public boolean handleResourceMove(IResource fromRc, IResource toRc) {
			boolean proceed = true;
			IProject fromProject = fromRc.getProject();
			IProject toProject = toRc.getProject();
			switch(toRc.getType()){
			case IResource.PROJECT:{
				fMngr.projectMove(fromProject, toProject);
				fRemovedProjSet.add(fromProject);
			}
			break;
			case IResource.FOLDER:{
				IPath fromFullPath = fromRc.getFullPath();
				IPath toFullPath = toRc.getFullPath();
				if(toFullPath.equals(fromFullPath))
					break;

				if(!toProject.equals(fromProject))
					break;
				
				ICProjectDescription des = getProjectDescription(toProject, true);
				if(des != null){
					ICConfigurationDescription cfgDess[] = des.getConfigurations();
					for(int i = 0; i < cfgDess.length; i++){
						ICConfigurationDescription cfg = cfgDess[i];
						ICExclusionPatternPathEntry entries[] = cfg.getSourceEntries();
						entries = checkMove(fromFullPath, toFullPath, entries);
						if(entries != null){
							try {
								cfg.setSourceEntries((ICSourceEntry[])entries);
							} catch (WriteAccessException e) {
								CCorePlugin.log(e);
							} catch (CoreException e) {
								CCorePlugin.log(e);
							}
						}
						
//don't do anything about output entries						ICBuildSetting bs = cfg.getBuildSetting();
//						if(bs != null){
//							entries = bs.getOutputDirectories();
//							entries = checkMove(fromFullPath, toFullPath, entries);
//							if(entries != null){
//								bs.setOutputDirectories((ICOutputEntry[])entries);
//							}
//						}
					}
				}
				//do not break.. proceed with rc description move
			}
			case IResource.FILE:{
				IPath fromRcProjPath = fromRc.getProjectRelativePath();
				IPath toRcProjPath = toRc.getProjectRelativePath();
				if(toRcProjPath.equals(fromRcProjPath))
					break;

				if(!toProject.equals(fromProject))
					break;
				
				ICProjectDescription des = getProjectDescription(toProject, true);
				if(des != null){
					ICConfigurationDescription cfgDess[] = des.getConfigurations();
					for(int i = 0; i < cfgDess.length; i++){
						ICResourceDescription rcDescription = cfgDess[i].getResourceDescription(fromRcProjPath, true);
						if(rcDescription != null){
							try {
								rcDescription.setPath(toRcProjPath);
							} catch (WriteAccessException e) {
//							} catch (CoreException e) {
							}
						}
					}
				}
				break;
			}
			}
			
			return proceed;
		}
		
		private ICProjectDescription getProjectDescription(IResource rc, boolean load){
			IProject project = rc.getProject(); 
			ICProjectDescription des = fProjDesMap.get(project);
			if(des == null && !fProjDesMap.containsKey(project)){
				int flags = load ? 0 : CProjectDescriptionManager.GET_IF_LOADDED;
				flags |= CProjectDescriptionManager.INTERNAL_GET_IGNORE_CLOSE;
				flags |= CProjectDescriptionManager.GET_WRITABLE;
				des = fMngr.getProjectDescription(project, flags);
				if(des != null)
					fProjDesMap.put(project, des);
			}
			return des;
		}
		
		private void setProjectDescription(IProject project, ICProjectDescription des){
			fProjDesMap.put(project, des);
		}
		
		private List<ICExclusionPatternPathEntry> checkRemove(IPath rcFullPath, ICExclusionPatternPathEntry[] entries){
			List<ICExclusionPatternPathEntry> updatedList = null;
			int num = 0;
			for(int k = 0; k < entries.length; k++){
				if(entries[k].getFullPath().equals(rcFullPath)){
					if(updatedList == null){
						updatedList = new ArrayList<ICExclusionPatternPathEntry>(Arrays.asList(entries));
					}
					updatedList.remove(num);
				} else {
					num++;
				}
			}
			return updatedList;
		}
	
		@SuppressWarnings("fallthrough")
		public boolean handleResourceRemove(IResource rc) {
			boolean proceed = true;
			IProject project = rc.getProject();
			switch(rc.getType()){
			case IResource.PROJECT:
				fMngr.projectClosedRemove(project);
				fRemovedProjSet.add(project);
				proceed = false;
				break;
			case IResource.FOLDER:
				if(project.exists()){
					ICProjectDescription des = getProjectDescription(project, true);
					if(des != null){
						IPath rcFullPath = rc.getFullPath();
						ICConfigurationDescription cfgDess[] = des.getConfigurations();
						for(int i = 0; i < cfgDess.length; i++){
							ICConfigurationDescription cfg = cfgDess[i];
							ICExclusionPatternPathEntry[] entries = cfg.getSourceEntries();
							List<ICExclusionPatternPathEntry> updatedList = checkRemove(rcFullPath, entries);
							
							if(updatedList != null){
								try {
									cfg.setSourceEntries(updatedList.toArray(new ICSourceEntry[updatedList.size()]));
								} catch (WriteAccessException e) {
									CCorePlugin.log(e);
								} catch (CoreException e) {
									CCorePlugin.log(e);
								}
							}
							
//don't do anything about output entries							ICBuildSetting bs = cfg.getBuildSetting();
//							if(bs != null){
//								entries = bs.getOutputDirectories();
//								updatedList = checkRemove(rcFullPath, entries);
//								if(updatedList != null){
//									bs.setOutputDirectories((ICOutputEntry[])updatedList.toArray(new ICOutputEntry[updatedList.size()]));
//								}
//							}
						}
					}
				}
				//do not break.. proceed with rc description remove
			case IResource.FILE:
				if(project.exists()){
					ICProjectDescription des = getProjectDescription(project, true);
					if(des != null){
						IPath rcProjPath = rc.getProjectRelativePath();
						ICConfigurationDescription cfgDess[] = des.getConfigurations();
						for(int i = 0; i < cfgDess.length; i++){
							ICResourceDescription rcDescription = cfgDess[i].getResourceDescription(rcProjPath, true);
							if(rcDescription != null){
								try {
									cfgDess[i].removeResourceDescription(rcDescription);
								} catch (WriteAccessException e) {
									CCorePlugin.log(e);
								} catch (CoreException e) {
									CCorePlugin.log(e);
								}
							}
						}
					}
				}
				break;
			}
			
			return proceed;
		}

		public void done() {
			for(Iterator<Map.Entry<IProject, ICProjectDescription>> iter = fProjDesMap.entrySet().iterator(); iter.hasNext();){
				Map.Entry<IProject, ICProjectDescription> entry = iter.next();
				if(fRemovedProjSet.contains(entry.getKey())){
					iter.remove();
				} else {
					ICProjectDescription des = entry.getValue();
					if(des != null && !des.isModified())
						iter.remove();
				}
			}
			
			if(!fProjDesMap.isEmpty()){
				CProjectDescriptionManager.runWspModification(new IWorkspaceRunnable(){

					public void run(IProgressMonitor monitor) throws CoreException {
						for(Iterator<Map.Entry<IProject, ICProjectDescription>> iter = fProjDesMap.entrySet().iterator(); iter.hasNext();){
							Map.Entry<IProject, ICProjectDescription> entry = iter.next();
							IProject project = entry.getKey();
							if(!project.isOpen())
								continue;
							
							ICProjectDescription des = entry.getValue();
							
							try {
								fMngr.setProjectDescription(project, des);
							} catch (CoreException e) {
								CCorePlugin.log(e);
							}
						}
					}
					
				}, new NullProgressMonitor());
			}
		}
	}
	

	@Override
	protected IResourceMoveHandler createResourceMoveHandler(
			IResourceChangeEvent event) {
		return new RcMoveHandler();
	}

	/*
	 *  I S a v e P a r t i c i p a n t 
	 */

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.ISaveParticipant#saving(org.eclipse.core.resources.ISaveContext)
	 */
	public void saving(ISaveContext context) throws CoreException {
		//Request a resource delta to be used on next activation.
	    context.needDelta();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.ISaveParticipant#doneSaving(org.eclipse.core.resources.ISaveContext)
	 */
	public void doneSaving(ISaveContext context) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.ISaveParticipant#prepareToSave(org.eclipse.core.resources.ISaveContext)
	 */
	public void prepareToSave(ISaveContext context) throws CoreException {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.ISaveParticipant#rollback(org.eclipse.core.resources.ISaveContext)
	 */
	public void rollback(ISaveContext context) {
	}

	@Override
	protected void doHandleResourceMove(IResourceChangeEvent event,
			IResourceMoveHandler handler) {
		switch(event.getType()){
		case IResourceChangeEvent.POST_CHANGE:
			IResourceDelta delta = event.getDelta();
			if(delta != null){
				IResourceDelta projs[] = delta.getAffectedChildren();
				for(int i = 0; i < projs.length; i++){
					IResourceDelta projDelta = projs[i];
					if(!shouldVisit((IProject)projDelta.getResource()))
						continue;
					
					if((projDelta.getKind() & IResourceDelta.REMOVED) == IResourceDelta.REMOVED)
						continue;
					
					IResourceDelta children[] = projDelta.getAffectedChildren();
					for(int k = 0; k < children.length; k++){
						IResourceDelta child = children[k];
						IResource rc = child.getResource();
						if(rc.getType() != IResource.FILE)
							continue;					
					}
				}
			}
			break;
		}
		super.doHandleResourceMove(event, handler);
	}
	
	

}
