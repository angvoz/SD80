/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.cdt.core.CDescriptorEvent;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICExtension;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.core.ICOwnerInfo;
import org.eclipse.cdt.core.settings.model.ICConfigExtensionReference;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.settings.model.util.CExtensionUtil;
import org.eclipse.cdt.internal.core.settings.model.CConfigurationDescriptionCache;
import org.eclipse.cdt.internal.core.settings.model.CConfigurationSpecSettings;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescriptionManager;
import org.eclipse.cdt.internal.core.settings.model.CStorage;
import org.eclipse.cdt.internal.core.settings.model.ExceptionFactory;
import org.eclipse.cdt.internal.core.settings.model.IInternalCCfgInfo;
import org.eclipse.cdt.internal.core.settings.model.InternalXmlStorageElement;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CConfigBasedDescriptor implements ICDescriptor {
	private static final String CEXTENSION_NAME = "cextension"; //$NON-NLS-1$
	
	private ICConfigurationDescription fCfgDes;
	private IProject fProject;
	private COwner fOwner;
	private final HashMap<String, ArrayList<ICExtensionReference>> fDesMap = new HashMap<String, ArrayList<ICExtensionReference>>();
	private final HashMap<String, Element> fStorageDataElMap = new HashMap<String, Element>();
	private boolean fApplyOnChange = true;
	private boolean fIsDirty;
	private CDescriptorEvent fOpEvent;
	private boolean fIsOpStarted;
	
	final class CConfigBaseDescriptorExtensionReference implements ICExtensionReference{
		private ICConfigExtensionReference fCfgExtRef;
		CConfigBaseDescriptorExtensionReference(ICConfigExtensionReference cfgRef){
			fCfgExtRef = cfgRef; 
		}

		public ICExtension createExtension() throws CoreException {
			InternalCExtension cExtension = null;
			IConfigurationElement el = CExtensionUtil.getFirstConfigurationElement(fCfgExtRef, CEXTENSION_NAME, false);
			cExtension = (InternalCExtension)el.createExecutableExtension("run"); //$NON-NLS-1$
			cExtension.setExtensionReference(this);
			cExtension.setProject(fProject);
			return (ICExtension)cExtension;
		}

		public ICDescriptor getCDescriptor() {
			return CConfigBasedDescriptor.this;
		}

		public String getExtension() {
			return fCfgExtRef.getExtensionPoint();
		}

		public String getExtensionData(String key) {
			return fCfgExtRef.getExtensionData(key);
		}

		public IConfigurationElement[] getExtensionElements()
				throws CoreException {
			IConfigurationElement el = CExtensionUtil.getFirstConfigurationElement(fCfgExtRef, CEXTENSION_NAME, false);
			if(el != null)
				return el.getChildren();
			return new IConfigurationElement[0];
		}

		public String getID() {
			return fCfgExtRef.getID();
		}

		public void setExtensionData(String key, String value)
				throws CoreException {
			if(!CDataUtil.objectsEqual(fCfgExtRef.getExtensionData(key), value)){
				fIsDirty = true;
				fCfgExtRef.setExtensionData(key, value);
				checkApply();
				if(isOperationStarted())
					setOpEvent(new CDescriptorEvent(CConfigBasedDescriptor.this, CDescriptorEvent.CDTPROJECT_CHANGED, 0));
			}
		}
	}

	public CConfigBasedDescriptor(ICConfigurationDescription des) throws CoreException{
		this(des, true);
	}

	public CConfigBasedDescriptor(ICConfigurationDescription des, boolean write) throws CoreException{
		updateConfiguration(des, write);
	}
	
	public void setApplyOnChange(boolean apply){
		if(fApplyOnChange == apply)
			return;
		
		fApplyOnChange = apply;
	}
	
	public boolean isApplyOnChange(){
		return fApplyOnChange;
	}
	
	public void apply(boolean force) throws CoreException{
		if(force || fIsDirty){
			ICProjectDescription des = fCfgDes.getProjectDescription();
			if(des.isCdtProjectCreating())
				des.setCdtProjectCreated();
			CProjectDescriptionManager.getInstance().setProjectDescription(fProject, des);
			fIsDirty = false;
		}
	}
	
	private void checkApply() throws CoreException {
		if(fApplyOnChange){
			apply(false);
			fIsDirty = false;
		} else {
			fIsDirty = true;
		}
	}
	
	public ICExtensionReference create(String extensionPoint, String id)
			throws CoreException {
		ICConfigExtensionReference ref = fCfgDes.create(extensionPoint, id);

		//write is done for all configurations to avoid "data loss" on configuration change
		ICProjectDescription des = fCfgDes.getProjectDescription();
		ICConfigurationDescription cfgs[] = des.getConfigurations();
		for (ICConfigurationDescription cfg : cfgs) {
			if(cfg != fCfgDes){
				try {
					cfg.create(extensionPoint, id);
				} catch (CoreException e){
				}
			}
		}
		
		ICExtensionReference r = create(ref);
		fIsDirty = true;
		checkApply();
		if(isOperationStarted())
			setOpEvent(new CDescriptorEvent(this, CDescriptorEvent.CDTPROJECT_CHANGED, CDescriptorEvent.EXTENSION_CHANGED));
		return r;
	}
	
	void setDirty(boolean dirty){
		fIsDirty = dirty;
	}
	
	public void updateConfiguration(ICConfigurationDescription des) throws CoreException{
		updateConfiguration(des, true);
	}

	public void updateConfiguration(ICConfigurationDescription des, boolean write) throws CoreException{
		if(write && des instanceof CConfigurationDescriptionCache)
			throw new IllegalArgumentException();
		
		fCfgDes = des;
		fProject = fCfgDes.getProjectDescription().getProject();
		CConfigurationSpecSettings settings = ((IInternalCCfgInfo)fCfgDes).getSpecSettings(); 
		fOwner = settings.getCOwner();
//		settings.setDescriptor(this);
		fStorageDataElMap.clear();
	}
	
	private CConfigBaseDescriptorExtensionReference create(ICConfigExtensionReference ref){
		CConfigBaseDescriptorExtensionReference dr = new CConfigBaseDescriptorExtensionReference(ref);
		synchronized (fDesMap) {
			ArrayList<ICExtensionReference> list = fDesMap.get(ref.getExtensionPoint());
			if(list == null){
				list = new ArrayList<ICExtensionReference>(1);
				fDesMap.put(ref.getExtensionPoint(), list);
			} else {
				list.ensureCapacity(list.size() + 1);
			}
			list.add(dr);
		}
		return dr;
	}

	public ICExtensionReference[] get(String extensionPoint) {
		ICConfigExtensionReference[] rs = fCfgDes.get(extensionPoint);
		ArrayList<ICConfigExtensionReference> refs = new ArrayList<ICConfigExtensionReference>();
		refs.addAll(Arrays.asList(rs));
		
		ICConfigurationDescription[] cfgs = 
			fCfgDes.getProjectDescription().getConfigurations();
		
		for (int i=0; i<cfgs.length; i++) {
			if (!fCfgDes.equals(cfgs[i])) {
				rs = cfgs[i].get(extensionPoint); 
				for (int j=0; j<rs.length; j++) {
					if (!refs.contains(rs[j]))
						refs.add(rs[j]);
				}
			}
		}
		ICConfigExtensionReference cfgRefs[] = 
			refs.toArray(
							new ICConfigExtensionReference[refs.size()]);

		if(cfgRefs.length == 0){
			return new ICExtensionReference[0];
		}
		
		ICExtensionReference[] extRefs = new ICExtensionReference[cfgRefs.length];
		synchronized (fDesMap) {
			ArrayList<ICExtensionReference> list = fDesMap.get(extensionPoint);
			//		if(list == null){
			//			list = new ArrayList(cfgRefs.length);
			//			fDesMap.put(extensionPoint, list);
			//		}

			//		list = (ArrayList)list.clone();
			//
			//		CConfigBaseDescriptorExtensionReference[] refs = (CConfigBaseDescriptorExtensionReference[])list.
			//			toArray(new CConfigBaseDescriptorExtensionReference[list.size()]);
			int num = cfgRefs.length - 1;

			for(int i = cfgRefs.length - 1; i >= 0; i--){
				ICConfigExtensionReference ref = cfgRefs[i];
				int k= -1;
				if (list != null) {
					for(k= list.size()-1; k >= 0; k--){
						CConfigBaseDescriptorExtensionReference r = (CConfigBaseDescriptorExtensionReference)list.get(k);
						if(r.fCfgExtRef == ref){
							extRefs[num--] = r;
							list.remove(k);
							break;
						}
					}
				}
				if(k < 0){
					extRefs[num--] = new CConfigBaseDescriptorExtensionReference(ref);
				}
			}

			if(list == null){
				list = new ArrayList<ICExtensionReference>(cfgRefs.length);
				fDesMap.put(extensionPoint, list);
			} else {
				list.clear();
				list.ensureCapacity(cfgRefs.length);
			}

			list.addAll(Arrays.asList(extRefs));
			list.trimToSize();
		}
		return extRefs;
	}

	public ICExtensionReference[] get(String extensionPoint, boolean update)
			throws CoreException {
		ICExtensionReference[] refs = get(extensionPoint);
		if(refs.length == 0 && update){
			boolean prevApplyOnChange = fApplyOnChange;
			fApplyOnChange = false;
			fOwner.update(fProject, this, extensionPoint);
			fApplyOnChange = prevApplyOnChange;
			checkApply();
			refs = get(extensionPoint);
		}
		return get(extensionPoint);
	}

	public String getPlatform() {
		return fOwner.getPlatform();
	}

	public IProject getProject() {
		return fProject;
	}

	public Element getProjectData(String id) throws CoreException {
	    // avoid deadlock by using different lock here.                                  
		synchronized(fStorageDataElMap /*CProjectDescriptionManager.getInstance()*/){
			Element el = fStorageDataElMap.get(id);
			if(el == null || el.getParentNode() == null){
				InternalXmlStorageElement storageEl = (InternalXmlStorageElement)fCfgDes.getStorage(id, false);
				if(storageEl == null){
					try {
						DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
						Document doc = builder.newDocument();
						el = CStorage.createStorageXmlElement(doc, id);
						doc.appendChild(el);
					} catch (ParserConfigurationException e) {
						throw ExceptionFactory.createCoreException(e);
					}
				} else {
					el = CProjectDescriptionManager.getInstance().createXmlElementCopy(storageEl);
				}
				fStorageDataElMap.put(id, el);
			}
			return el;
		}
	}

	public ICOwnerInfo getProjectOwner() {
		return fOwner;
	}

	public void remove(ICExtensionReference extension) throws CoreException {
		ICConfigExtensionReference ref =((CConfigBaseDescriptorExtensionReference)extension).fCfgExtRef;
		fCfgDes.remove(ref);

		//write is done for all configurations to avoid "data loss" on configuration change
		ICProjectDescription des = fCfgDes.getProjectDescription();
		ICConfigurationDescription cfgs[] = des.getConfigurations();
		for (ICConfigurationDescription cfg : cfgs) {
			if(cfg != fCfgDes){
				try {
					ICConfigExtensionReference rs[] = cfg.get(ref.getExtensionPoint());
					for (ICConfigExtensionReference element : rs) {
						if(ref.getID().equals(element.getID())){
							cfg.remove(element);
							break;
						}
					}
				} catch (CoreException e) {
				}
			}
		}
		fIsDirty = true;
		checkApply();
		if(isOperationStarted())
			setOpEvent(new CDescriptorEvent(this, CDescriptorEvent.CDTPROJECT_CHANGED, CDescriptorEvent.EXTENSION_CHANGED));
	}

	public void remove(String extensionPoint) throws CoreException {
		fCfgDes.remove(extensionPoint);
		
		//write is done for all configurations to avoid "data loss" on configuration change
		ICProjectDescription des = fCfgDes.getProjectDescription();
		ICConfigurationDescription cfgs[] = des.getConfigurations();
		for (ICConfigurationDescription cfg : cfgs) {
			if(cfg != fCfgDes){
				try {
					cfg.remove(extensionPoint);
				} catch (CoreException e) {
				}
			}
		}
		fIsDirty = true;
		checkApply();
		if(isOperationStarted())
			setOpEvent(new CDescriptorEvent(this, CDescriptorEvent.CDTPROJECT_CHANGED, CDescriptorEvent.EXTENSION_CHANGED));
	}

	public void saveProjectData() throws CoreException {
		if(CProjectDescriptionManager.getInstance().getDescriptorManager().reconsile(this, fCfgDes.getProjectDescription()))
			fIsDirty = true;
		
		checkApply();
		if(isOperationStarted())
			setOpEvent(new CDescriptorEvent(this, CDescriptorEvent.CDTPROJECT_CHANGED, 0));
	}
	
	public Map<String, Element> getStorageDataElMap(){
		@SuppressWarnings("unchecked")
		final HashMap<String, Element> clone = (HashMap<String, Element>)fStorageDataElMap.clone();
		return clone; 
	}
	
	public ICConfigurationDescription getConfigurationDescription() {
		return fCfgDes;
	}

	void setOpEvent(CDescriptorEvent event) {
		if(!isOperationStarted())
			return;

		if (event.getType() == CDescriptorEvent.CDTPROJECT_ADDED) {
			fOpEvent = event;
		} else if (event.getType() == CDescriptorEvent.CDTPROJECT_REMOVED) {
			fOpEvent = event;
		} else {
			if (fOpEvent == null) {
				fOpEvent = event;
			} else if ( (fOpEvent.getFlags() & event.getFlags()) != event.getFlags()) {
				fOpEvent = new CDescriptorEvent(event.getDescriptor(), event.getType(),
						fOpEvent.getFlags() | event.getFlags());
			}
		}
	}
	
	boolean isOperationStarted(){
		return fIsOpStarted;
	}
	
	void operationStart(){
		fIsOpStarted = true;
	}

	CDescriptorEvent operationStop(){
		fIsOpStarted = false;
		CDescriptorEvent e = fOpEvent;
		fOpEvent = null;
		
		return e;
	}

}
