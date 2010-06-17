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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.internal.ui.model.elements.ElementContentProvider;
import org.eclipse.debug.internal.ui.model.elements.ElementLabelProvider;
import org.eclipse.debug.internal.ui.model.elements.ElementMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.ui.IMemento;

@SuppressWarnings("restriction")
public class SystemVMContainer implements IAdaptable {

	private static int nextID = 100;
	/**
	 * Context property id.
	 */
	public static final String PROP_ID = "ID";

	/**
	 * Context property name.
	 */
	public static final String PROP_NAME = "Name";

	public static final String PROP_COLUMN_KEYS = "Column_Keys";
	public static final String PROP_COLUMN_NAMES = "Column_Names";

	protected Map<String, Object> properties = Collections.synchronizedMap(new HashMap<String, Object>());

	private final List<SystemVMContainer> children = Collections.synchronizedList(new ArrayList<SystemVMContainer>());

	private SystemVMContainer parent;

	private SystemDMContainer dmContainer;

	private IElementMementoProvider elementMementoProvider = new ElementMementoProvider() {

		@Override
		protected boolean isEqual(Object element, IMemento memento,
				IPresentationContext context) throws CoreException {
			return memento.getString(PROP_NAME).equals(((SystemVMContainer) element).getName());
		}

		@Override
		protected boolean encodeElement(Object element, IMemento memento,
				IPresentationContext context) throws CoreException {
			memento.putString(PROP_NAME, ((SystemVMContainer) element).getName());
			return true;
		}
		
	};
	
	private IElementLabelProvider elementLabelProvider = new ElementLabelProvider() {

		@Override
		protected String getLabel(TreePath elementPath,
				IPresentationContext presentationContext, String columnId)
		throws CoreException {
			Object element = elementPath.getLastSegment();
			Object root = elementPath.getFirstSegment();
			if (element instanceof SystemVMContainer && root instanceof SystemVMContainer)
			{
				if (columnId == null)
					return ((SystemVMContainer) element).getName();
				else
					return (String) ((SystemVMContainer) element).getProperties().get(columnId);
			}
			return null;
		}

	};

	private IColumnPresentationFactory columnPresentationFactory = new IColumnPresentationFactory() {

		private IColumnPresentation columnPresentation;

		public IColumnPresentation createColumnPresentation(
				IPresentationContext context, Object element) {

			final String[] columnKeys = (String[]) getProperties().get(SystemVMContainer.PROP_COLUMN_KEYS);
			@SuppressWarnings("unchecked")
			final Map<String, String> columnNames = (Map<String, String>) getProperties().get(SystemVMContainer.PROP_COLUMN_NAMES);

			if (columnKeys == null)
				return null;


			if (columnPresentation != null)
				return columnPresentation;

			columnPresentation = new IColumnPresentation() {

				public void init(IPresentationContext context) {}

				public void dispose() {}

				public String[] getAvailableColumns() {
					return columnKeys;
				}

				public String[] getInitialColumns() {
					return columnKeys;
				}

				public String getHeader(String id) {
					return (String) columnNames.get(id);
				}

				public ImageDescriptor getImageDescriptor(String id) {
					// TODO Auto-generated method stub
					return null;
				}

				public String getId() {
					return (String) getProperties().get(SystemVMContainer.PROP_ID);
				}

				public boolean isOptional() {
					return false;
				}};

				return columnPresentation;
		}

		public String getColumnPresentationId(IPresentationContext context,
				Object element) {
			if (element instanceof SystemVMContainer)
			{
				return (String)getProperties().get(SystemVMContainer.PROP_ID);
			}
			return null;
		}
	};

	private IElementContentProvider elementContextProvider = new ElementContentProvider() {

		@Override
		protected Object[] getChildren(Object parent, int index, int length,
				IPresentationContext context, IViewerUpdate monitor)
		throws CoreException {
			if (parent instanceof SystemVMContainer)
			{
				List<SystemVMContainer> children = ((SystemVMContainer) parent).getChildren();
				return children.subList(index, index + length).toArray();
			}
			return null;
		}

		@Override
		protected int getChildCount(Object element,
				IPresentationContext context, IViewerUpdate monitor)
		throws CoreException {
			if (element instanceof SystemVMContainer)
				return ((SystemVMContainer) element).getChildCount();
			return 0;
		}

		@Override
		protected boolean supportsContextId(String id) {
			return true;
		}


	};

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	int getChildCount()
	{
		return children.size();	
	}

	public List<SystemVMContainer> getChildren() {
		return Collections.unmodifiableList(children) ;
	}

	public SystemVMContainer(Map<String, Object> props) {
		if (props != null) {
			properties.putAll(props);
		}
		if ((String) properties.get(PROP_ID) == null)
		{
			properties.put(PROP_ID, generateID());
		}
	}

	private String generateID() {
		return Integer.toString(nextID++);
	}

	public SystemVMContainer(SystemVMContainer parent, Map<String, Object> props) {
		this(props);
		if (parent != null)
		{
			parent.addChild(this);
			this.setParent(parent);
		}
	}

	public SystemVMContainer(SystemVMContainer parent, String name, Map<String, Object> props) {
		this(parent, props);
		properties.put(SystemVMContainer.PROP_NAME, name);
	}

	public SystemVMContainer(SystemVMContainer parent, String name) {
		this(parent, (Map<String, Object>)null);
		properties.put(SystemVMContainer.PROP_NAME, name);
	}

	public SystemVMContainer(SystemVMContainer parent, SystemDMContainer dmContainer) {
		this(parent, dmContainer.getProperties());
		this.setDMContainer(dmContainer);
	}

	private void addChild(SystemVMContainer systemDMC) {
		children.add(systemDMC);
	}

	public String getName()
	{
		String name = (String) properties.get(PROP_NAME);
		if (name == null)
			return "";
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SystemVMContainer)
			return getID().equals(((SystemVMContainer) obj).getID());
		return false;
	}

	public String getID() {
		return (String) properties.get(PROP_ID);
	}

	@Override
	public int hashCode() {
		return getID().hashCode();
	}

	@Override
	public String toString() {
		return properties.toString();
	}

	public void setParent(SystemVMContainer parent) {
		this.parent = parent;
	}

	public SystemVMContainer getParent() {
		return parent;
	}

	public void setDMContainer(SystemDMContainer dmContainer) {
		this.dmContainer = dmContainer;
	}

	public SystemDMContainer getDMContainer() {
		return dmContainer;
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IElementLabelProvider.class))
			return elementLabelProvider;
		if (adapter.equals(IElementContentProvider.class))
			return elementContextProvider;
		if (adapter.equals(IColumnPresentationFactory.class))
			return columnPresentationFactory;
		if (adapter.equals(IElementMementoProvider.class))
			return elementMementoProvider;
		return null;
	}

}
