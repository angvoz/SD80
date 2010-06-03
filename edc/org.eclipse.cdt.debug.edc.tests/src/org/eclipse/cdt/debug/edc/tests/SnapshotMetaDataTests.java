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

package org.eclipse.cdt.debug.edc.tests;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.cdt.debug.edc.internal.ZipFileUtils;
import org.eclipse.cdt.debug.edc.internal.snapshot.Album;
import org.eclipse.cdt.debug.edc.internal.snapshot.Snapshot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Test;

public class SnapshotMetaDataTests extends TestCase {
	
	private static File testDSA1;
	private static File testDSA_DeleteSnapsTest;
	
	private static File testSnapshotFile;
	
	private static List<File> testSnapShotFileArray = new ArrayList<File>();
	
	private static String[] DSA_FILE_EXT = new String[] {"dsa"};
	

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		if (testDSA1 == null || !testDSA1.exists()){
			String res_folder = EDCTestPlugin.projectRelativePath("resources/Snapshots");
			IPath dsaPath = new Path(res_folder);
			dsaPath = dsaPath.append("Snapshot_MetaDataTest.dsa");
			
			testDSA1 = File.createTempFile("testDSA1", ".dsa", new File(res_folder));
			
			copyFile(dsaPath.toFile(), testDSA1);
			assertTrue("Copy failed on test DSA.", testDSA1.exists());
		}
		
		if (testDSA_DeleteSnapsTest == null || !testDSA_DeleteSnapsTest.exists()){
			String res_folder = EDCTestPlugin.projectRelativePath("resources/Snapshots");
			IPath dsaPath = new Path(res_folder);
			dsaPath = dsaPath.append("DeleteSnapsTest.dsa");
			
			testDSA_DeleteSnapsTest = File.createTempFile("testDSA_DeleteSnapsTest", ".dsa", new File(res_folder));
			
			copyFile(dsaPath.toFile(), testDSA_DeleteSnapsTest);
			assertTrue("Copy failed on DeleteSnapsTest.dsa.", testDSA_DeleteSnapsTest.exists());
		}
		
		
		if (testSnapshotFile == null || !testSnapshotFile.exists()){
			String res_folder = EDCTestPlugin.projectRelativePath("resources/Snapshots/snapshots");
			IPath snapPath = new Path(res_folder);
			snapPath = snapPath.append("snapshot_1258585222707.xml");
			
			testSnapshotFile = snapPath.toFile();
			
			assertTrue("Copy failed on test DSA.", testDSA1.exists());
		}
		
		if (testSnapShotFileArray.size() == 0){
			String res_folder = EDCTestPlugin.projectRelativePath("resources/Snapshots/snapshots");
			IPath snapShotsXMLFolder = new Path(res_folder);
			
			testSnapShotFileArray.add(snapShotsXMLFolder.append("snapshot_1258585222708.xml").toFile());
			testSnapShotFileArray.add(snapShotsXMLFolder.append("snapshot_1258585222709.xml").toFile());
			testSnapShotFileArray.add(snapShotsXMLFolder.append("snapshot_1258585222710.xml").toFile());
		}
		
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	@Test
	public void testAlbumMetaData() throws Exception {
		
		Album album = new Album();
		album.setLocation(new Path(testDSA1.getAbsolutePath()));
		
		List<Snapshot> snapshotList = album.getSnapshots();
		assertEquals(3, snapshotList.size());
		assertEquals(0, album.getCurrentSnapshotIndex());
		
		Snapshot snap = snapshotList.get(0);
		assertEquals(0, album.getIndexOfSnapshot(snap));
		
		assertEquals("Wed Nov 18 17:45:52 CST 2009", snap.getCreationDate());
		assertEquals("", snap.getSnapshotDescription());
		assertEquals("snapshot_1258587952190.xml", snap.getSnapshotDisplayName());
		assertEquals("snapshot_1258587952190.xml", snap.getSnapshotFileName());
		
		snap.setSnapshotDescription("DESCRIPTION");
		snap.setSnapshotDisplayName("DISPLAYNAME");
		assertEquals("DESCRIPTION", snap.getSnapshotDescription());
		assertEquals("DISPLAYNAME", snap.getSnapshotDisplayName());
		album.updateSnapshotMetaData(null, snap);
	}
	
	@Test
	public void testReLoadAlbumMetaData() throws Exception {
		
		Album album = new Album();
		album.setLocation(new Path(testDSA1.getAbsolutePath()));
		
		List<Snapshot> snapshotList = album.getSnapshots();
		assertEquals(3, snapshotList.size());
		assertEquals(0, album.getCurrentSnapshotIndex());
		
		Snapshot snap = snapshotList.get(0);
		assertEquals(0, album.getIndexOfSnapshot(snap));
		
		assertEquals("Wed Nov 18 17:45:52 CST 2009", snap.getCreationDate());
		assertEquals("DESCRIPTION", snap.getSnapshotDescription());
		assertEquals("DISPLAYNAME", snap.getSnapshotDisplayName());
		assertEquals("snapshot_1258587952190.xml", snap.getSnapshotFileName());
	}
	
	@Test
	public void testDeleteFilesFromArchiveTest() throws Exception {
		assertTrue(ZipFileUtils.deleteFileFromZip("snapshot_1258587952190.xml", testDSA1, new String[] {"dsa"}));
	}
	
	@Test
	public void testAddFilesFromArchiveTest() throws Exception {
		assertTrue(ZipFileUtils.addFileToZip(testSnapshotFile, testDSA1, DSA_FILE_EXT));
		
		assertTrue(ZipFileUtils.addFilesToZip(testSnapShotFileArray.toArray(new File[testSnapShotFileArray.size()]), testDSA1, DSA_FILE_EXT));
	}
	
	@Test
	public void testReadSnapshotXML() throws Exception {
		BufferedInputStream stream = ZipFileUtils.openFile(testDSA1, "snapshot_1258585222710.xml", DSA_FILE_EXT);
		assertNotNull(stream);
		stream.close();
		ZipFileUtils.unmount();
	}
	
	@Test
	public void testDeleteSnapshotsTest() throws Exception {
		Album album = new Album();
		album.setLocation(new Path(testDSA_DeleteSnapsTest.getAbsolutePath()));
		
		List<Snapshot> snapshotList = album.getSnapshots();
		assertEquals(5, snapshotList.size());
		
		album.deleteSnapshot(snapshotList.get(2)); // delete from middle
		assertEquals(4, snapshotList.size());
		
		album.deleteSnapshot(snapshotList.get(0)); // delete from start
		assertEquals(3, snapshotList.size());
		
		album.deleteSnapshot(snapshotList.get(1)); // delete from middle
		assertEquals(2, snapshotList.size());
		
		album.deleteSnapshot(snapshotList.get(1)); // delete from end
		assertEquals(1, snapshotList.size());
		
		assertEquals("dbg_derived_types::285", snapshotList.get(0).getSnapshotDisplayName());
	}
	
	@Test
	public void testCreateAlbumFromScratch() throws Exception
	{
		Album album = new Album();
		IPath albumPath = album.createEmptyAlbum();
		assertTrue(albumPath.toFile().exists());
		assertTrue(albumPath.toFile().isFile());
		// delete the album now we're done
		albumPath.toFile().delete();
		assertTrue(!albumPath.toFile().exists());
	}
	
	@Test
	public void testLastTestForCleanup(){
		testDSA1.delete();
		assertFalse(testDSA1.exists());
		
		testDSA_DeleteSnapsTest.delete();
		assertFalse(testDSA_DeleteSnapsTest.exists());
	}

	public static void copyFile(File in, File out) throws Exception {
	    FileInputStream fis  = new FileInputStream(in);
	    FileOutputStream fos = new FileOutputStream(out);
	    try {
	        byte[] buf = new byte[1024];
	        int i = 0;
	        while ((i = fis.read(buf)) != -1) {
	            fos.write(buf, 0, i);
	        }
	    } 
	    catch (Exception e) {
	        throw e;
	    }
	    finally {
	        if (fis != null) fis.close();
	        if (fos != null) fos.close();
	    }
	  }

}
