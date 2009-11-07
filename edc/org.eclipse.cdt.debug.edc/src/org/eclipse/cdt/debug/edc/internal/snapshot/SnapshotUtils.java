/*******************************************************************************
 * Copyright (c) 2009 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.snapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PlatformObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SnapshotUtils extends PlatformObject {

	/**
	 * Constants for XML element names and attributes
	 */
	public static final String PROPERTIES = "properties"; //$NON-NLS-1$
	private static final String KEY = "key"; //$NON-NLS-1$
	private static final String VALUE = "value"; //$NON-NLS-1$
	private static final String SET_ENTRY = "setEntry"; //$NON-NLS-1$
	private static final String MAP_ENTRY = "mapEntry"; //$NON-NLS-1$
	private static final String LIST_ENTRY = "listEntry"; //$NON-NLS-1$
	private static final String SET_ATTRIBUTE = "setAttribute"; //$NON-NLS-1$
	private static final String MAP_ATTRIBUTE = "mapAttribute"; //$NON-NLS-1$
	private static final String LIST_ATTRIBUTE = "listAttribute"; //$NON-NLS-1$
	private static final String BOOLEAN_ATTRIBUTE = "booleanAttribute"; //$NON-NLS-1$
	private static final String INT_ATTRIBUTE = "intAttribute"; //$NON-NLS-1$
	private static final String STRING_ATTRIBUTE = "stringAttribute"; //$NON-NLS-1$

	@SuppressWarnings("unchecked")
	static public Element makeXMLFromProperties(Document doc, Map<String, Object> properties) {

		Element rootElement = doc.createElement(PROPERTIES);
		Iterator<String> keys = properties.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			if (key != null) {
				Object value = properties.get(key);
				if (value == null) {
					continue;
				}
				Element element = null;
				String valueString = null;
				if (value instanceof String) {
					valueString = (String) value;
					element = createKeyValueElement(doc, STRING_ATTRIBUTE, key, valueString);
				} else if (value instanceof Integer) {
					valueString = ((Integer) value).toString();
					element = createKeyValueElement(doc, INT_ATTRIBUTE, key, valueString);
				} else if (value instanceof Long) {
					valueString = ((Long) value).toString();
					element = createKeyValueElement(doc, INT_ATTRIBUTE, key, valueString);
				} else if (value instanceof Boolean) {
					valueString = ((Boolean) value).toString();
					element = createKeyValueElement(doc, BOOLEAN_ATTRIBUTE, key, valueString);
				} else if (value instanceof List) {
					element = createListElement(doc, LIST_ATTRIBUTE, key, (List<Object>) value);
				} else if (value instanceof Map) {
					element = createMapElement(doc, MAP_ATTRIBUTE, key, (Map<Object, Object>) value);
				} else if (value instanceof Set) {
					element = createSetElement(doc, SET_ATTRIBUTE, key, (Set<Object>) value);
				}
				rootElement.appendChild(element);
			}
		}
		return rootElement;
	}

	/**
	 * Helper method that creates a 'key value' element of the specified type
	 * with the specified attribute values.
	 */
	static protected Element createKeyValueElement(Document doc, String elementType, String key, String value) {
		Element element = doc.createElement(elementType);
		element.setAttribute(KEY, key);
		element.setAttribute(VALUE, value);
		return element;
	}

	/**
	 * Creates a new <code>Element</code> for the specified
	 * <code>java.util.List</code>
	 * 
	 * @param doc
	 *            the doc to add the element to
	 * @param elementType
	 *            the type of the element
	 * @param setKey
	 *            the key for the element
	 * @param list
	 *            the list to fill the new element with
	 * @return the new element
	 */
	static protected Element createListElement(Document doc, String elementType, String listKey, List<Object> list) {
		Element listElement = doc.createElement(elementType);
		listElement.setAttribute(KEY, listKey);
		Iterator<Object> iterator = list.iterator();
		while (iterator.hasNext()) {
			String value = (String) iterator.next();
			Element element = doc.createElement(LIST_ENTRY);
			element.setAttribute(VALUE, value);
			listElement.appendChild(element);
		}
		return listElement;
	}

	/**
	 * Creates a new <code>Element</code> for the specified
	 * <code>java.util.Set</code>
	 * 
	 * @param doc
	 *            the doc to add the element to
	 * @param elementType
	 *            the type of the element
	 * @param setKey
	 *            the key for the element
	 * @param set
	 *            the set to fill the new element with
	 * @return the new element
	 * 
	 * @since 3.3
	 */
	static protected Element createSetElement(Document doc, String elementType, String setKey, Set<Object> set) {
		Element setElement = doc.createElement(elementType);
		setElement.setAttribute(KEY, setKey);
		Element element = null;
		for (Object object : set) {
			element = doc.createElement(SET_ENTRY);
			element.setAttribute(VALUE, (String) object);
			setElement.appendChild(element);
		}
		return setElement;
	}

	/**
	 * Creates a new <code>Element</code> for the specified
	 * <code>java.util.Map</code>
	 * 
	 * @param doc
	 *            the doc to add the element to
	 * @param elementType
	 *            the type of the element
	 * @param setKey
	 *            the key for the element
	 * @param map
	 *            the map to fill the new element with
	 * @return the new element
	 * 
	 */
	static protected Element createMapElement(Document doc, String elementType, String mapKey, Map<Object, Object> map) {
		Element mapElement = doc.createElement(elementType);
		mapElement.setAttribute(KEY, mapKey);
		Iterator<Object> iterator = map.keySet().iterator();
		while (iterator.hasNext()) {
			String key = (String) iterator.next();
			String value = (String) map.get(key);
			Element element = doc.createElement(MAP_ENTRY);
			element.setAttribute(KEY, key);
			element.setAttribute(VALUE, value);
			mapElement.appendChild(element);
		}
		return mapElement;
	}

	/**
	 * Initializes the mapping of attributes from the XML file
	 * 
	 * @param root
	 *            the root node from the XML document
	 * @throws CoreException
	 */
	static public void initializeFromXML(Element root, Map<String, Object> properties) throws CoreException {
		if (root.getNodeName().equalsIgnoreCase(PROPERTIES)) {
			NodeList list = root.getChildNodes();
			Node node = null;
			Element element = null;
			String nodeName = null;
			for (int i = 0; i < list.getLength(); ++i) {
				node = list.item(i);
				short nodeType = node.getNodeType();
				if (nodeType == Node.ELEMENT_NODE) {
					element = (Element) node;
					nodeName = element.getNodeName();
					if (nodeName.equalsIgnoreCase(STRING_ATTRIBUTE)) {
						setStringAttribute(element, properties);
					} else if (nodeName.equalsIgnoreCase(INT_ATTRIBUTE)) {
						setIntegerAttribute(element, properties);
					} else if (nodeName.equalsIgnoreCase(BOOLEAN_ATTRIBUTE)) {
						setBooleanAttribute(element, properties);
					} else if (nodeName.equalsIgnoreCase(LIST_ATTRIBUTE)) {
						setListAttribute(element, properties);
					} else if (nodeName.equalsIgnoreCase(MAP_ATTRIBUTE)) {
						setMapAttribute(element, properties);
					} else if (nodeName.equalsIgnoreCase(SET_ATTRIBUTE)) {
						setSetAttribute(element, properties);
					}
				}
			}
		}

	}

	/**
	 * Loads a <code>String</code> from the specified element into the local
	 * attribute mapping
	 * 
	 * @param element
	 *            the element to load from
	 * @throws CoreException
	 */
	static protected void setStringAttribute(Element element, Map<String, Object> properties) throws CoreException {
		properties.put(element.getAttribute(KEY), element.getAttribute(VALUE));
	}

	/**
	 * Loads an <code>Integer</code> from the specified element into the local
	 * attribute mapping
	 * 
	 * @param element
	 *            the element to load from
	 * @throws CoreException
	 */
	static protected void setIntegerAttribute(Element element, Map<String, Object> properties) throws CoreException {
		properties.put(element.getAttribute(KEY), new Integer(element.getAttribute(VALUE)));
	}

	/**
	 * Loads a <code>Boolean</code> from the specified element into the local
	 * attribute mapping
	 * 
	 * @param element
	 *            the element to load from
	 * @throws CoreException
	 */
	static protected void setBooleanAttribute(Element element, Map<String, Object> properties) throws CoreException {
		properties.put(element.getAttribute(KEY), Boolean.valueOf(element.getAttribute(VALUE)));
	}

	/**
	 * Reads a <code>List</code> attribute from the specified XML node and loads
	 * it into the mapping of attributes
	 * 
	 * @param element
	 *            the element to read the list attribute from
	 * @throws CoreException
	 *             if the element has an invalid format
	 */
	static protected void setListAttribute(Element element, Map<String, Object> properties) throws CoreException {
		String listKey = element.getAttribute(KEY);
		NodeList nodeList = element.getChildNodes();
		int entryCount = nodeList.getLength();
		List<Object> list = new ArrayList<Object>(entryCount);
		Node node = null;
		Element selement = null;
		for (int i = 0; i < entryCount; i++) {
			node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				selement = (Element) node;
				if (selement.getNodeName().equalsIgnoreCase(LIST_ENTRY)) {
					String value = selement.getAttribute(VALUE);
					list.add(value);
				}
			}
		}
		properties.put(listKey, list);
	}

	/**
	 * Reads a <code>Set</code> attribute from the specified XML node and loads
	 * it into the mapping of attributes
	 * 
	 * @param element
	 *            the element to read the set attribute from
	 * @throws CoreException
	 *             if the element has an invalid format
	 * 
	 * @since 3.3
	 */
	static protected void setSetAttribute(Element element, Map<String, Object> properties) throws CoreException {
		String setKey = element.getAttribute(KEY);
		NodeList nodeList = element.getChildNodes();
		int entryCount = nodeList.getLength();
		Set<Object> set = new HashSet<Object>(entryCount);
		Node node = null;
		Element selement = null;
		for (int i = 0; i < entryCount; i++) {
			node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				selement = (Element) node;
				if (selement.getNodeName().equalsIgnoreCase(SET_ENTRY)) {
					set.add(element.getAttribute(VALUE));
				}
			}
		}
		properties.put(setKey, set);
	}

	/**
	 * Reads a <code>Map</code> attribute from the specified XML node and loads
	 * it into the mapping of attributes
	 * 
	 * @param element
	 *            the element to read the map attribute from
	 * @throws CoreException
	 *             if the element has an invalid format
	 */
	static protected void setMapAttribute(Element element, Map<String, Object> properties) throws CoreException {
		String mapKey = element.getAttribute(KEY);
		NodeList nodeList = element.getChildNodes();
		int entryCount = nodeList.getLength();
		Map<Object, Object> map = new HashMap<Object, Object>(entryCount);
		Node node = null;
		Element selement = null;
		for (int i = 0; i < entryCount; i++) {
			node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				selement = (Element) node;
				if (selement.getNodeName().equalsIgnoreCase(MAP_ENTRY)) {
					map.put(selement.getAttribute(KEY), selement.getAttribute(VALUE));
				}
			}
		}
		properties.put(mapKey, map);
	}

}
