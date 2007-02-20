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
package org.eclipse.cdt.managedbuilder.internal.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.IBuildPropertiesRestriction;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;

public class SupportedProperties implements IBuildPropertiesRestriction {
	public static final String SUPPORTED_PROPERTIES = "supportedProperties";				//$NON-NLS-1$
	public static final String PROPERTY = "property";				//$NON-NLS-1$
	public static final String PROPERTY_VALUE = "value";				//$NON-NLS-1$
	public static final String ID = "id";				//$NON-NLS-1$
	public static final String REQUIRED = "required";				//$NON-NLS-1$

	private HashMap fSupportedProperties = new HashMap();
	
	private class SupportedProperty {
		private boolean fIsRequired;
		private Set fValues = new HashSet();
		private String fId;
		
		SupportedProperty(String id){
			fId = id;
		}
		
		void updateRequired(boolean required){
			if(!fIsRequired)
				fIsRequired = required;
		}
		
		public String getId(){
			return fId;
		}
		
/*		SupportedProperty(IManagedConfigElement el) {
			fId = el.getAttribute(ID);
			
//			IBuildPropertyType type = mngr.getPropertyType(id);
//			if(type == null)
//				continue;
			
			fIsRequired = Boolean.valueOf(el.getAttribute(REQUIRED)).booleanValue();
			
			fValues = new HashSet();
			
			IManagedConfigElement values[] = el.getChildren();
			for(int k = 0; k < values.length; k++){
				IManagedConfigElement value = values[k];
				if(PROPERTY_VALUE.equals(value.getName())){
					String valueId = value.getAttribute(ID);
					if(valueId == null && valueId.length() == 0)
						continue;
					
//					IBuildPropertyValue val = type.getSupportedValue(valueId);
//					if(val != null)
//						set.add(val.getId());
					fValues.add(valueId);
				}
			}
		}
*/		
		public boolean isValid(){
			return fId != null && fValues.size() != 0;
		}
		
		public boolean isRequired(){
			return fIsRequired;
		}
		
		public void addValueIds(Set ids){
			fValues.addAll(ids);
		}
		
		public boolean supportsValue(String id){
			return fValues.contains(id);
		}
		
		public String[] getSupportedValues(){
			return (String[])fValues.toArray(new String[fValues.size()]);
		}
		
	}

	public SupportedProperties(IManagedConfigElement el){
//		IBuildPropertyManager mngr = BuildPropertyManager.getInstance();
		
		IManagedConfigElement children[] = el.getChildren();
		for(int i = 0; i < children.length; i++){
			IManagedConfigElement child = children[i];
			if(PROPERTY.equals(child.getName())){
				String id = child.getAttribute(ID);
				if(id == null)
					continue;
				
				boolean required = Boolean.valueOf(el.getAttribute(REQUIRED)).booleanValue();

//				IBuildPropertyType type = mngr.getPropertyType(id);
//				if(type == null)
//					continue;
				
				Set set = new HashSet();
				
				IManagedConfigElement values[] = child.getChildren();
				for(int k = 0; k < values.length; k++){
					IManagedConfigElement value = values[k];
					if(PROPERTY_VALUE.equals(value.getName())){
						String valueId = value.getAttribute(ID);
						if(valueId == null || valueId.length() == 0)
							continue;
						
//						IBuildPropertyValue val = type.getSupportedValue(valueId);
//						if(val != null)
//							set.add(val.getId());
						
						set.add(valueId);
					}
				}
				
				if(set.size() != 0){
					SupportedProperty stored = (SupportedProperty)fSupportedProperties.get(id);
					if(stored == null){
						stored = new SupportedProperty(id);
						fSupportedProperties.put(id, stored);
					}
					stored.addValueIds(set);
					stored.updateRequired(required);
				}
			}
		}

	}
	
//	public boolean supportsType(IBuildPropertyType type) {
//		return supportsType(type.getId());
//	}
	
	public boolean supportsType(String type) {
		return fSupportedProperties.containsKey(type);
	}
	
	public boolean supportsValue(String type, String value){
		boolean suports = false;
		SupportedProperty prop = (SupportedProperty)fSupportedProperties.get(type);
		if(prop != null){
			suports = prop.supportsValue(value);
		}
		return suports;
	}

//	public boolean supportsValue(IBuildPropertyType type,
//			IBuildPropertyValue value) {
//		return supportsValue(type.getId(), value.getId());
//	}

	public String[] getRequiredTypeIds() {
		List list = new ArrayList(fSupportedProperties.size());
		for(Iterator iter = fSupportedProperties.values().iterator(); iter.hasNext();){
			SupportedProperty prop = (SupportedProperty)iter.next();
			if(prop.isRequired())
				list.add(prop.getId());
		}
		return (String[])list.toArray(new String[list.size()]);
	}

	public String[] getSupportedTypeIds() {
		String result[] = new String[fSupportedProperties.size()];
		fSupportedProperties.keySet().toArray(result);
		return result;
	}

	public String[] getSupportedValueIds(String typeId) {
		SupportedProperty prop = (SupportedProperty)fSupportedProperties.get(typeId);
		if(prop != null)
			return prop.getSupportedValues();
		return new String[0];
	}

	public boolean requiresType(String typeId) {
		SupportedProperty prop = (SupportedProperty)fSupportedProperties.get(typeId);
		if(prop != null)
			return prop.isRequired();
		return false;
	}

}
