/**********************************************************************
 * Copyright (c) 2007 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     Wind River Systems - Initial API and implementation
 **********************************************************************/

package org.eclipse.ffs.internal.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Doug Schaefer
 *
 * Contents of the ecproj file.
 */
public class FFSProject extends PlatformObject {

	private final URI uri;
	private final FFSFileStore root;
	private final Map<IPath, Map<String, URI>> childAdds = new HashMap<IPath, Map<String, URI>>();
	private final Map<IPath, List<Pattern>> childExcludes = new HashMap<IPath, List<Pattern>>();
	
	public FFSProject(URI uri) throws CoreException {
		this.uri = uri;
		this.root = new FFSFileStore(this, null, EFS.getStore(uri));
		loadProject();
	}

	public FFSFileStore getRoot() {
		return root;
	}
	
	public URI getURI() {
		return uri;
	}
	
	public void addChild(FFSFileStore baseFileStore, IFileStore child) {
		addChild(baseFileStore, child.getName(), child.toURI());
	}

	public void addChild(FFSFileStore baseFileStore, FFSFileStore child) {
		addChild(baseFileStore, child.getName(), child.getTargetURI());
	}

	public void addChild(FFSFileStore baseFileStore, String name, URI uri) {
		IPath path = baseFileStore.getPath();
		addChild(path, name, uri);
	}

	public void addChild(IPath path, String name, URI uri) {
		Map<String, URI> adds = childAdds.get(path);
		if (adds == null) {
			adds = new HashMap<String, URI>();
			childAdds.put(path, adds);
		}
		adds.put(name, uri);
		System.out.println("addChild: " + path + "  " + name + "  " + uri);
	}


	public void removeChild(FFSFileStore fileStore, String name) {
		Map<String, URI> adds = childAdds.get(fileStore.getPath());
		if (adds != null)
		{
			adds.remove(name);
		}
		System.out.println("removeChild: " + fileStore + "  " + name);
	}

	public void excludeChildren(FFSFileStore baseFileStore, Pattern pattern) {
		IPath path = baseFileStore.getPath();
		List<Pattern> excludes = childExcludes.get(path);
		if (excludes == null) {
			excludes = new ArrayList<Pattern>();
			childExcludes.put(path, excludes);
		}
		excludes.add(pattern);
	}
	
	public URI getChild(FFSFileStore node, String childName) {
		Map<String, URI> adds = childAdds.get(node.getPath());
		if (adds == null)
			return null;
		else
			return adds.get(childName);
	}
	
	public URI[] getAdditionalChildren(FFSFileStore baseFileStore) {
		Map<String, URI> adds = childAdds.get(baseFileStore.getPath());
		if (adds == null)
			return new URI[0];
		else
			return adds.values().toArray(new URI[adds.size()]);
	}
	
	public boolean isChildExcluded(FFSFileStore node, IFileStore child) {
		return isChildExcluded(node, child.getName());
	}
	
	public boolean isChildExcluded(FFSFileStore node, String childName) {
		List<Pattern> excludes = childExcludes.get(node.getPath());
		if (excludes == null)
			return false;
		for (Iterator<Pattern> i = excludes.iterator(); i.hasNext();) {
			Pattern pattern = i.next();
			if (pattern.matcher(childName).matches())
				return true;
		}
		return false;
	}

	public void addFile(IPath filePath, IResource resource) throws CoreException {
		IFileStore fileStore = EFS.getLocalFileSystem().getStore(filePath);
		IFileStore parentStore = FFSFileSystem.getFFSFileSystem().getStore(resource.getLocationURI());
		addChild((FFSFileStore)parentStore, fileStore);
		resource.refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());
		saveProject();
	}

	public void remove(IResource resource) throws CoreException {
		IFileStore parentStore = FFSFileSystem.getFFSFileSystem().getStore(resource.getParent().getLocationURI());
		removeChild((FFSFileStore)parentStore, resource.getName());
		resource.getParent().refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());
		saveProject();
	}

	private void loadProject() {
		try {
			IFileStore projectFile = root.getChild("ffsProject.xml");
			if (projectFile.fetchInfo().exists())
			{
				InputStream stream = projectFile.openInputStream(EFS.NONE, new NullProgressMonitor());
				
				DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				parser.setErrorHandler(new DefaultHandler());
				Element root = parser.parse(new InputSource(stream)).getDocumentElement();
				readProjectDescription(root);

				stream.close();			
			}

		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void saveProject()
	{
		try {
			ByteArrayOutputStream projectStream = writeProjectDescription();

			IFileStore projectFile = root.getChild("ffsProject.xml");
			projectFile.delete(EFS.NONE, new NullProgressMonitor());
			OutputStream stream = projectFile.openOutputStream(EFS.NONE, new NullProgressMonitor());
			projectStream.writeTo(stream);
			stream.close();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static final String FFS_PROJECT = "ffsProject"; //$NON-NLS-1$

	private void readProjectDescription(Element root) {
		if (root.getNodeName().equalsIgnoreCase(FFS_PROJECT)) { 

			NodeList elementNodes = root.getElementsByTagName("file");			
			Node node = null;
			Element element = null;
			String nodeName = null;
			for (int i = 0; i < elementNodes.getLength(); ++i) {
				node = elementNodes.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					element = (Element) node;
					try {
						addChild(new Path(element.getAttribute("path")), element.getAttribute("name"), new URI(element.getAttribute("uri")));
					} catch (URISyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
		
	}

	public ByteArrayOutputStream writeProjectDescription() throws ParserConfigurationException, TransformerException
	{
		DocumentBuilderFactory dfactory= DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder= dfactory.newDocumentBuilder();
		Document doc= docBuilder.newDocument();

		Element configRootElement = doc.createElement(FFS_PROJECT); 
		doc.appendChild(configRootElement);
		
		Element listElement = doc.createElement("files");

		
	       for (Map.Entry<IPath, Map<String, URI>> entry : childAdds.entrySet()) {
				IPath resourcePath = entry.getKey();
			       for (Map.Entry<String, URI> entry2 : entry.getValue().entrySet()) {
						String name = entry2.getKey();
						URI uri = entry2.getValue();
						Element element = doc.createElement("file"); 
						element.setAttribute("path", resourcePath.toString());
						element.setAttribute("name", name);
						element.setAttribute("uri", uri.toString());
					listElement.appendChild(element);
			        }
				
	        }

	       configRootElement.appendChild(listElement);

		
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer();
		transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
		transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
		DOMSource source = new DOMSource(doc);
		StreamResult outputTarget = new StreamResult(byteStream);
		transformer.transform(source, outputTarget);
		return byteStream;
	}
}
