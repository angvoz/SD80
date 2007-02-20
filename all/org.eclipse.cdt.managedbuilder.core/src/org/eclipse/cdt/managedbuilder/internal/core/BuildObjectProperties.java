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
import java.util.List;

import org.eclipse.cdt.managedbuilder.buildproperties.IBuildProperty;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyType;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyValue;
import org.eclipse.cdt.managedbuilder.core.IBuildObjectProperties;
import org.eclipse.cdt.managedbuilder.core.IBuildPropertiesRestriction;
import org.eclipse.cdt.managedbuilder.internal.buildproperties.BuildProperties;
import org.eclipse.cdt.managedbuilder.internal.buildproperties.BuildPropertyManager;
import org.eclipse.core.runtime.CoreException;

public class BuildObjectProperties extends BuildProperties implements
		IBuildObjectProperties {
	private IBuildPropertiesRestriction fRestriction;
	private IBuildPropertyChangeListener fListener;
	
	public BuildObjectProperties(IBuildPropertiesRestriction restriction, IBuildPropertyChangeListener listener) {
		super();
		fRestriction = restriction;
		fListener = listener;
	}
	
	public BuildObjectProperties(BuildObjectProperties properties, IBuildPropertiesRestriction restriction, IBuildPropertyChangeListener listener) {
		super(properties);
		fRestriction = restriction;
		fListener = listener;
	}

	public BuildObjectProperties(String properties, IBuildPropertiesRestriction restriction, IBuildPropertyChangeListener listener) {
		super(properties);
		fRestriction = restriction;
		fListener = listener;
	}

	public IBuildPropertyType[] getSupportedTypes() {
		IBuildPropertyType types[] = BuildPropertyManager.getInstance().getPropertyTypes();
		
		if(fRestriction != null && types.length != 0){
			List list = new ArrayList(types.length);
			for(int i = 0; i < types.length; i++){
				if(fRestriction.supportsType(types[i].getId()))
					list.add(types[i]);
			}
			
			types = (IBuildPropertyType[])list.toArray(new IBuildPropertyType[list.size()]);
		}

		return types;
	}

	public IBuildPropertyValue[] getSupportedValues(String typeId) {
		IBuildPropertyType type = BuildPropertyManager.getInstance().getPropertyType(typeId);
		if(type != null){
			IBuildPropertyValue values[] = type.getSupportedValues();
			if(fRestriction != null && values.length != 0){
				List list = new ArrayList(values.length);
				for(int i = 0; i < values.length; i++){
					if(fRestriction.supportsValue(type.getId(), values[i].getId()))
						list.add(values[i]);
				}
				
				return (IBuildPropertyValue[])list.toArray(new IBuildPropertyValue[list.size()]);
			}
		}
		return new IBuildPropertyValue[0];
	}

	public boolean supportsType(String id) {
		return fRestriction.supportsType(id);
//		IBuildPropertyType type = BuildPropertyManager.getInstance().getPropertyType(id);
//		if(type != null){
//			if(fRestriction != null){
//				return fRestriction.supportsType(type.getId());
//			}
//			return true;
//		}
//		return false;
	}

	public boolean supportsValue(String typeId, String valueId) {
		return fRestriction.supportsValue(typeId, valueId);
//		IBuildPropertyType type = BuildPropertyManager.getInstance().getPropertyType(typeId);
//		if(type != null){
//			IBuildPropertyValue value = type.getSupportedValue(valueId);
//			if(value != null){
//				if(fRestriction != null){
//					return fRestriction.supportsValue(type.getId(), value.getId());
//				}
//				return true;
//			}
//		}
//		return false;
	}

	public void clear() {
		super.clear();
		fListener.propertiesChanged();
	}

	public IBuildProperty removeProperty(String id) {
		IBuildProperty property = super.removeProperty(id);
		if(property != null)
			fListener.propertiesChanged();
		return property;
	}
	
	IBuildProperty internalSetProperty(String propertyId, String propertyValue) throws CoreException{
		return super.setProperty(propertyId, propertyValue);
	}

	public IBuildProperty setProperty(String propertyId, String propertyValue)
			throws CoreException {
//		if(!supportsType(propertyId))
//			throw new CoreException(new Status(IStatus.ERROR,
//					ManagedBuilderCorePlugin.getUniqueIdentifier(),
//					"property type is not supported"));
//		if(!supportsValue(propertyId, propertyValue))
//			throw new CoreException(new Status(IStatus.ERROR,
//					ManagedBuilderCorePlugin.getUniqueIdentifier(),
//					"property value is not supported"));
		
		IBuildProperty property = super.setProperty(propertyId, propertyValue);
		fListener.propertiesChanged();
		return property;
	}

	public String[] getRequiredTypeIds() {
		return fRestriction.getRequiredTypeIds();
	}

	public boolean requiresType(String typeId) {
		return fRestriction.requiresType(typeId);
	}

	public String[] getSupportedTypeIds() {
		return fRestriction.getSupportedTypeIds();
	}

	public String[] getSupportedValueIds(String typeId) {
		return fRestriction.getSupportedValueIds(typeId);
	}
}
