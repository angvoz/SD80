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
package org.eclipse.cdt.debug.edc.debugger.tests;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.cdt.debug.edc.internal.HostOS;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl.ExecutionDMC;
import org.eclipse.cdt.debug.edc.internal.snapshot.Album;
import org.eclipse.cdt.debug.edc.internal.snapshot.Snapshot;
import org.eclipse.cdt.debug.edc.internal.snapshot.SnapshotUtils;
import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.cdt.debug.edc.services.IEDCExecutionDMC;
import org.eclipse.cdt.debug.edc.tests.TestUtils;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SnapshotTests extends BaseLaunchTest {

	@Test
	public void testSnapshot() throws Exception {
		if (!HostOS.IS_WIN32)
			return;
		EDCLaunch launch = createLaunch();
		assertNotNull(launch);
		final DsfSession session = waitForSession(launch);
		assertNotNull(session);
		IEDCExecutionDMC executionDMC = waitForExecutionDMC(session);
		assertNotNull(executionDMC);
		ExecutionDMC threadDMC = TestUtils.waitForSuspendedThread(session);
		Assert.assertNotNull(threadDMC);

		
		Album.createSnapshotForSession(session, null, new NullProgressMonitor());
		Album album = Album.getRecordingForSession(session.getId());
		assertTrue(album.isRecording());
		assertEquals(1, album.getSnapshots().size());
		assertAlbumStructureCorrect(album);
		final Snapshot snap = album.getSnapshots().get(0);
		assertNotNull(snap);

		Query<Boolean> query = new Query<Boolean>() {

			@Override
			protected void execute(DataRequestMonitor<Boolean> rm) {
				snap.open(session); // parse snapshot data in album (.dsa)
				rm.setData(true);
				rm.done();
			}};

			session.getExecutor().execute(query);
			query.get();

		assertSnapshotStructureCorrect(snap);
	}

	private void assertAlbumStructureCorrect(Album album) throws Exception {
		Document document = (Document) album.getAdapter(Document.class);
		
		assertNotNull(document);
		// find the snapshot meta data element
		NodeList nodeList = document.getElementsByTagName(Album.METADATA);
		assertEquals(1, nodeList.getLength());
		
		assertTrue(album.getLocation().toOSString().endsWith(".dsa"));
		assertEquals(0, album.getCurrentSnapshotIndex());
		
	}
	
	private void assertSnapshotStructureCorrect(Snapshot snapshot) throws Exception {
		
		assertTrue(snapshot.getCreationDate().length() > 0);
		assertTrue(snapshot.getSnapshotFileName().endsWith(".xml"));
		
		Document snapShotdocument = (Document) snapshot.getAdapter(Document.class);
		assertNotNull(snapShotdocument);
		
		// find the snapshot element
		NodeList nodeList = snapShotdocument.getElementsByTagName(Snapshot.SNAPSHOT);
		assertEquals(1, nodeList.getLength());
		
		Element snapshotElement = (Element) nodeList.item(0);
		// check nested execution contexts as list
		nodeList = snapshotElement.getElementsByTagName("execution_context");
		assertEquals(2, nodeList.getLength());
		// check properties of top-level execution context
		// (BlackFlagMinGW.exe)
		Element bfElement = (Element) nodeList.item(0);
		assertNotNull(bfElement.getAttribute("ID"));
		NodeList propsList = bfElement.getChildNodes();
		
		assertTrue(propsList.getLength() > 0);
		
		Element propElement = (Element) bfElement.getElementsByTagName(SnapshotUtils.PROPERTIES)
		.item(0);
		
		Properties properties = createPropertiesFromElement(propElement);
		assertEquals("BlackFlagMinGW.exe", properties.get("Name"));
		assertEquals("root", properties.get("ParentID"));
		String mainExeContextID = (String) properties.get("ID");
// TODO: CanResume is being set as a integer and not a boolean
//		assertEquals("true", properties.get("CanResume"));
		assertEquals(true, properties.get("CanSuspend"));
		assertEquals(true, properties.get("CanTerminate"));
		// check properties of second one (shared lib)
		Element slElement = (Element) nodeList.item(1);

		propElement = (Element) bfElement.getElementsByTagName(SnapshotUtils.PROPERTIES)
		.item(1);
		
		properties = createPropertiesFromElement(propElement);
		assertTrue(properties.containsKey("Name"));
		assertTrue(properties.containsKey("OSID"));
		String subExecContextID = (String) properties.get("ID");
		assertEquals(mainExeContextID, properties.get("ParentID"));
// TODO: CanResume is being set as a integer and not a boolean
//		assertEquals("true", properties.get("CanResume"));
		assertEquals(true, properties.get("CanSuspend"));
		assertEquals(true, properties.get("CanTerminate"));
//		assertEquals("Exception", properties.get("Message")); // This is sometimes "Exception" and sometimes "Shared Library"
		assertEquals(true, properties.get("State"));
		
		// check the registers
		nodeList = slElement.getElementsByTagName("execution_context_registers");
		assertEquals(1, nodeList.getLength());
		Element regGroupElement = (Element) slElement.getElementsByTagName("register_group")
		.item(0);
		assertEquals("register_group", regGroupElement.getTagName());
		String regGroupID = regGroupElement.getAttribute("ID");
		assertTrue("Wrong register group ID: " + regGroupID, regGroupID.equals("GPX") || regGroupID.contains("Basic"));
		
		propElement = (Element) regGroupElement.getElementsByTagName(SnapshotUtils.PROPERTIES)
		.item(0);
		
		properties = createPropertiesFromElement(propElement);
		String name = (String)properties.get("Name");
		assertTrue("Wrong register group name: " + name, name.equals("General") || name.contains("Basic"));
		assertEquals(subExecContextID, properties.get("Context_ID"));
		assertX86RegisterValuesOk(regGroupElement, subExecContextID);

		// check the modules
		nodeList = snapshotElement.getElementsByTagName("execution_context_modules");
		assertEquals(1, nodeList.getLength());
		Element moduleE = (Element) nodeList.item(0);
		Element stackFrameElement = (Element) moduleE.getElementsByTagName(SnapshotUtils.PROPERTIES)
		.item(0);
		
		properties = createPropertiesFromElement(stackFrameElement);
		assertTrue(properties.containsKey("File"));
		assertTrue(properties.containsKey("Loaded"));
		assertTrue(properties.containsKey("ImageBaseAddress"));
		assertTrue(properties.containsKey("CodeSize"));
		
	}

	private void assertX86RegisterValuesOk(Element regGroupElement, String execContextID) {
		NodeList registerNodes = regGroupElement.getElementsByTagName("register");
		assertEquals(16, registerNodes.getLength());
		Set<String> registerNames = new HashSet<String>();
		for (int i = 0; i < registerNodes.getLength(); i++) {
			Element register = (Element) registerNodes.item(i);
			String id = register.getAttribute("ID");
			Node propertiesNode = register.getFirstChild();
			assertNotNull(propertiesNode);
			
			Element propElement = (Element) regGroupElement.getElementsByTagName(SnapshotUtils.PROPERTIES)
			.item(i+1);
			
			Properties properties = createPropertiesFromElement(propElement);
			String name = (String)properties.get("Name");
			assertEquals(id, properties.get("ID"));
			assertEquals(execContextID, properties.get("Context_ID"));
			registerNames.add(name);
		}
		String[] registerNameVals = new String[] { "EAX", "ECX", "EDX", "EBX", "ESP", "EBP", "ESI", "EDI", "GS", "FS",
				"ES", "DS", "EIP", "CS", "EFL", "SS" };
		Set<String> expectedRegisterIds = new HashSet<String>(Arrays.asList(registerNameVals));
		assertEquals(expectedRegisterIds, registerNames);
	}

	private Properties createPropertiesFromElement(Element propertyElement) {
		Properties properties = new Properties();
		HashMap<String, Object> propMap = new HashMap<String, Object>();
		try {
			SnapshotUtils.initializeFromXML(propertyElement, propMap);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Iterator<?> it = propMap.entrySet().iterator();
		while (it.hasNext()){
			Map.Entry<?,?> pairs = (Map.Entry<?,?>)it.next();
	        //System.out.println(pairs.getKey() + " = " + pairs.getValue());
	        properties.put(pairs.getKey(), pairs.getValue());
		}

		return properties;
	}

}
