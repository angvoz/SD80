package org.eclipse.cdt.debug.edc.internal.ui.views;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider;
import org.eclipse.swt.graphics.Image;

@SuppressWarnings("restriction")
public class SystemViewInput implements IAdaptable, ISystemVMContainer {

	private SystemVMContainer container;

	public SystemViewInput(SystemVMContainer container) {
		super();
		this.container = container;
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IElementLabelProvider.class))
			return container.getElementLabelProvider();
		if (adapter.equals(IElementContentProvider.class))
			return container.getElementContextProvider();
		if (adapter.equals(IColumnPresentationFactory.class))
			return container.getColumnPresentationFactory();
		if (adapter.equals(IElementMementoProvider.class))
			return container.getElementMementoProvider();
		return null;
	}

	public ISystemVMContainer getContainer() {
		return container;
	}

	public void setContainer(SystemVMContainer container) {
		this.container = container;
	}

	public Map<String, Object> getProperties() {
		return getContainer().getProperties();
	}

	public List<SystemVMContainer> getChildren() {
		return getContainer().getChildren();
	}

	public String getName() {
		return getContainer().getName();
	}

	public ISystemVMContainer getParent() {
		return getContainer().getParent();
	}

	public Image getImage() {
		return getContainer().getImage();
	}

	public int getChildCount() {
		return getContainer().getChildCount();
	}

}
