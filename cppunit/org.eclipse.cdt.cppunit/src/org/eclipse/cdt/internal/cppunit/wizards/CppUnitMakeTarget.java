/*
 * (c) Copyright IBM Corp. 2003.
 * Created on 27 févr. 03
 * All Rights Reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 *
 * Contributors:
 * IBM Software Group - Rational Software
 */

package org.eclipse.cdt.internal.cppunit.wizards;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IViewPart;

import org.eclipse.cdt.core.resources.MakeUtil;
import org.eclipse.cdt.internal.ui.makeview.MakeView;

import org.eclipse.cdt.internal.cppunit.ui.CppUnitPlugin;

import org.eclipse.cdt.ui.CUIPlugin;


public class CppUnitMakeTarget implements IResourceChangeListener
{
	IProject fProject;
	IResource fFile;
	String fLabel;
	
	public CppUnitMakeTarget(IProject project,IResource file,String label)
	{
		fProject=project;
		fFile=file;
		fLabel=label;
		addMakeTarget(project,label);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}
	public void addMakeTarget(IProject project,String label)
	{
		MakeUtil.removePersistentTarget(project,label);
		MakeUtil.addPersistentTarget(project,label);
		refreshMakeView();
	}
	public void removeMakeTarget(IProject project,String label)
	{
		MakeUtil.removePersistentTarget(project,label);
		refreshMakeView();
	}
	public void refreshMakeView()
	{
		IWorkbenchPage page=CUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IViewPart makeView=page.findView("org.eclipse.cdt.ui.MakeView");
		if(makeView!=null)
		{
			if(makeView instanceof MakeView)
			{
				MakeView v=(MakeView)makeView;
//				v.refresh();
			}
		}
		else
		{
			System.out.println("NULL");
		}
	}
	public void resourceChanged(IResourceChangeEvent event)
	{
		// If the element deleted corresponds to fProject/fFile
		// then remove the make target, and remove the listener.
		final IResourceDelta delta=event.getDelta();
		CppUnitPlugin.getDisplay().syncExec(new Runnable() {
				public void run()
				{
					processDelta(delta);
				}
			}
		);
	}
	public void processDelta(IResourceDelta delta)
	{
		if(delta==null) return;
		IResourceDelta d=delta.findMember(fFile.getFullPath());
		if(d==null) return;
		IResource resource = d.getResource();
		if(resource==null) return;
		if(! resource.getName().equals(fFile.getName())) return;
		if(d.getKind()!=IResourceDelta.REMOVED) return;
		// Do it at last...
		removeMakeTarget(fProject,fLabel);
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}
}
