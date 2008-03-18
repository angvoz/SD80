/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.templateengine.uitree;

import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.eclipse.cdt.core.templateengine.TemplateDescriptor;
import org.eclipse.cdt.core.templateengine.TemplateEngine;
import org.eclipse.cdt.core.templateengine.TemplateInfo;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.templateengine.Messages;
import org.eclipse.cdt.ui.templateengine.uitree.uiwidgets.UIBooleanWidget;
import org.eclipse.cdt.ui.templateengine.uitree.uiwidgets.UIBrowseWidget;
import org.eclipse.cdt.ui.templateengine.uitree.uiwidgets.UISelectWidget;
import org.eclipse.cdt.ui.templateengine.uitree.uiwidgets.UISpecialListWidget;
import org.eclipse.cdt.ui.templateengine.uitree.uiwidgets.UIStringListWidget;
import org.eclipse.cdt.ui.templateengine.uitree.uiwidgets.UITextWidget;

/**
 * UIElementTreeBuilderHelper provides methods to convert an Element (XML) into
 * UIElement. The UIElement can be a simple UI Widget or a group.
 */
public class UIElementTreeBuilderHelper implements IUIElementTreeBuilderHelper {
	/**
	 * TemplateDescriptor representing the TemplaeDescriptor XML.
	 */
	private TemplateDescriptor templateDescriptor = null;
	private TemplateInfo templateInfo;

	/**
	 * Constructor, takes an TemplateDescriptor instance as parameter.
	 * 
	 * @param templateDescriptor
	 */
	public UIElementTreeBuilderHelper(TemplateDescriptor templateDescriptor, TemplateInfo templateInfo) {
		this.templateDescriptor = templateDescriptor;
		this.templateInfo = templateInfo;
	}

	/**
	 * 
	 * @return List of child Elements for the given
	 */
	public List getPropertyGroupList() {
		return templateDescriptor.getPropertyGroupList();
	}

	/**
	 * Given an XML Element, representing a PropertyElement. A UIElement for the
	 * same is returned. The Type attribute is verified, based on Type
	 * appropriate UIWidget is instantiated. This class the getUIWidget private
	 * method.
	 * 
	 * @param element
	 * @return UIElement.
	 */
	public UIElement getUIElement(Element element) {
		UIAttributes uiAttributes = new UIAttributes(templateInfo);

		NamedNodeMap list = element.getAttributes();
		for (int i=0; i<list.getLength(); i++) {
			Node attribute = list.item(i);
			uiAttributes.put(attribute.getNodeName(), attribute.getNodeValue());
		}
		
		return getUIWidget(element, uiAttributes);
	}

	/**
	 * Given an XML Element, representing a PropertyElement. A UIElement for the
	 * same is returned. The Type attribute is verified, based on Type
	 * appropriate UIWidget is instantiated.
	 * 
	 * @param uiAttributes
	 * @return UIElement.
	 */
	private UIElement getUIWidget(Element element, UIAttributes uiAttributes) {
		UIElement widgetElement= null;
		String id= uiAttributes.get(InputUIElement.ID);
		String type= uiAttributes.get(InputUIElement.TYPE);
		
		if (type == null || type.length()==0 ) {
			return null;
		}
		
		if (new Boolean(uiAttributes.get(InputUIElement.HIDDEN)).booleanValue()) {
			return null;	
		}
		
		if (type.equalsIgnoreCase(InputUIElement.INPUTTYPE)) {
			widgetElement = new UITextWidget(uiAttributes);
		} else if (type.equalsIgnoreCase(InputUIElement.MULTILINETYPE)) {
			widgetElement = new UITextWidget(uiAttributes);
		} else if (type.equalsIgnoreCase(InputUIElement.SELECTTYPE)) {
			String defaultValue= element.getAttribute(InputUIElement.DEFAULT);
			
			Map<String,String> value2name= new LinkedHashMap<String,String>();
			for(Element item : TemplateEngine.getChildrenOfElement(element)) {
				String label= item.getAttribute(InputUIElement.COMBOITEM_LABEL); // item displayed in Combo
				String value= item.getAttribute(InputUIElement.COMBOITEM_NAME); // value stored when its selected
				if(value.length() == 0) {
					value= item.getAttribute(InputUIElement.COMBOITEM_VALUE);
				}
				if(label==null || value==null) {
					String msg= Messages.getString("UIElementTreeBuilderHelper.InvalidEmptyLabel"); //$NON-NLS-1$
					CUIPlugin.log(MessageFormat.format(msg, id), null);
				} else {
					if(value2name.put(value, label)!=null) {
						String msg= Messages.getString("UIElementTreeBuilderHelper.InvalidNonUniqueValue"); //$NON-NLS-1$
						CUIPlugin.log(MessageFormat.format(msg, id), null);
					}
				}
			}
			
			widgetElement = new UISelectWidget(uiAttributes, value2name, defaultValue);
		} else if (type.equalsIgnoreCase(InputUIElement.BOOLEANTYPE)) {
			String defaultValue= element.getAttribute(InputUIElement.DEFAULT);
			boolean b= Boolean.parseBoolean(defaultValue);
			widgetElement = new UIBooleanWidget(uiAttributes, b);
		} else if (type.equalsIgnoreCase(InputUIElement.BROWSETYPE)) {
			widgetElement = new UIBrowseWidget(uiAttributes);
		} else if (type.equalsIgnoreCase(InputUIElement.STRINGLISTTYPE)) {
			widgetElement = new UIStringListWidget(uiAttributes);
		} else if (type.equalsIgnoreCase(InputUIElement.SPECIALLISTTYPE)) {
			widgetElement = new UISpecialListWidget(uiAttributes);
		} else if (type.equalsIgnoreCase(UIGroupTypeEnum.PAGES_ONLY.getId())) {
			widgetElement = new SimpleUIElementGroup(uiAttributes);
		} else if (type.equalsIgnoreCase(UIGroupTypeEnum.PAGES_TAB.getId())) {
			// Note: This is not implemented now as we haven't found a use case
			// for generating UI pages as TABS in a single page. 
		} else {
			String msg= MessageFormat.format(Messages.getString("UIElementTreeBuilderHelper.UnknownWidgetType0"), type); //$NON-NLS-1$
			CUIPlugin.log(msg, null);
		}

		return widgetElement;
	}
}
