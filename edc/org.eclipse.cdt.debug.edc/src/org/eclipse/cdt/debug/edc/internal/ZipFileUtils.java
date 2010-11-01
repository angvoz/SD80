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
package org.eclipse.cdt.debug.edc.internal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.IProgressMonitor;

import de.schlichtherle.io.ArchiveDetector;
import de.schlichtherle.io.ArchiveException;
import de.schlichtherle.io.DefaultArchiveDetector;
import de.schlichtherle.io.File;
import de.schlichtherle.io.FileInputStream;


/**
 * Provides a convenience wrapper around TrueZip and java.util.zip for read/write access to zip archives.
 * The API under java.util.zip does not provide ability to update individual entries in archives
 * and hence can be cumbersome and slow to use so TrueZip is added to ease this pain.
 * <p>
 * This wrapper also encapsulates the use of de.schlichtherle.io.File and only takes java.io.File
 * as explicit arguments. When differentiating the two, only java.io.File must be explicit.
 * <p>
 * For more information see https://truezip.dev.java.net/
 *
 */
public class ZipFileUtils {
	
	/**
	 * Delete a file from an archive
	 * @param fileName - File name relative path in the archive
	 * @param zipFile - The zip file on disk
	 * @param extensions - File extension of the zip format archive
	 * @return true on success
	 */
	public static boolean deleteFileFromZip(String fileName, java.io.File zipFile, String[] extensions){
		String archiveFileName = zipFile + File.separator + fileName;	
		
		ArchiveDetector detector = getArchiveDetector(extensions);
		
		File file = null;
		if (detector != null){
			file = new File(archiveFileName, detector);
		} else {
			file = new File(archiveFileName);
		}
		
		boolean success = file.delete();

		unmount();
		
		return success; 
	}
	
	/**
	 * Delete a file from an archive
	 * @param fileName - File name relative path in the archive
	 * @param zipFile - File extension of the zip format archive
	 * @return true on success
	 * 
	 * @see {@link deleteFileFromZip(String fileName, java.io.File zipFile, String[] extensions)}
	 */
	public static boolean deleteFileFromZip(String fileName, java.io.File zipFile) {
		return deleteFileFromZip(fileName, zipFile, null);
	}
	
	/**
	 * Copies a source file into a specified zip file. If the file exists it will be overwritten.
	 * @param src - The originating source to be copied
	 * @param zipFile - The destination for src
	 * @param extensions - File extension of the zip archive
	 * @return true on success
	 */
	public static boolean addFileToZip(java.io.File src, java.io.File zipFile, String[] extensions){
		
		boolean success = false;
		ArchiveDetector detector = getArchiveDetector(extensions);
		
		File toBeAddedFile = null;

		if (detector != null){
			toBeAddedFile = new File(src, detector);
			success = toBeAddedFile.archiveCopyTo(new File(zipFile, toBeAddedFile.getName(), detector));
		} else {
			toBeAddedFile = new File(src);
			success = toBeAddedFile.archiveCopyTo(new File(zipFile, toBeAddedFile.getName()));
		}
		
		unmount();
		
		return success; 
	}
	
	/**
	 * Copies a source file into a specified zip file. If the file exists it will be overwritten.
	 * @param src - The originating source to be copied
	 * @param zipFile - The destination for src
	 * @return true on success
	 * 
	 * @see {@link addFileToZip(java.io.File src, java.io.File zipFile, String[] extensions)}
	 */
	public static boolean addFileToZip(java.io.File srcFile, java.io.File zipFile) {
		return addFileToZip(srcFile, zipFile, null);
	}
	
	/**
	 * Copies source file(s) into a specified zip file. If the file exists it will be overwritten.
	 * @param src - The originating sources to be copied
	 * @param zipFile - The destination for src
	 * @param extensions - File extension of the zip archive
	 * @return true on success
	 */
	public static boolean addFilesToZip(java.io.File[] src, java.io.File zipFile, String[] extensions){
		
		boolean success = false;
		ArchiveDetector detector = getArchiveDetector(extensions);
		
		for (java.io.File currSrcFile : src) {
			
			try {
			
			File toBeAddedFile = null;
				if (detector != null) {

					toBeAddedFile = new File(currSrcFile.getCanonicalFile(),
							detector);

					success = toBeAddedFile.archiveCopyTo(new File(zipFile,
							toBeAddedFile.getName(), detector));
				} else {
					toBeAddedFile = new File(currSrcFile.getCanonicalFile());
					success = toBeAddedFile.archiveCopyTo(new File(zipFile,
							toBeAddedFile.getName()));
				}

			unmount();
			
			} catch (ArchiveException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return success; 
	}
	
	/**
	 * Copies source file(s) into a specified zip file. If the file exists it will be overwritten.
	 * @param src - The originating source to be copied
	 * @param zipFile - The destination for src
	 * @return true on success
	 * 
	 * @see ZipFileUtils#addFilesToZip(java.io.File[], java.io.File, String[])
	 */
	public static boolean addFilesToZip(java.io.File[] src, java.io.File zipFile) {
		return addFilesToZip(src, zipFile, null);
	}
	
	/**
	 * TrueZip detects archive types by file extension and only has built in support for known types.
	 * If other file extensions are used that TrueZip does not have in it's default configuration
	 * it will complain that it does not recognize the archive type. Passing an array of extensions will
	 * allow TrueZip to recognize any arbitrary file extension as a zip archive. This only works for zip archvies.
	 * 
	 * @param extensions - array of extension TrueZip should recognize as zip files.
	 * @return ArchiveDetector
	 */
	private static ArchiveDetector getArchiveDetector(String[] extensions){
		List<Object> zipFileExtensions = new ArrayList<Object>();
		ArchiveDetector detector = null;
		if (extensions != null && extensions.length > 0){
			for (String ext : extensions){
				zipFileExtensions.add(ext);
				zipFileExtensions.add(new de.schlichtherle.io.archive.zip.ZipDriver());
			}
			
			detector = new DefaultArchiveDetector(ArchiveDetector.NULL,
					zipFileExtensions.toArray());
		}
		
		return detector;
	}
	
	/**
	 * 
	 * @param zipFileName
	 * @return
	 * @throws IOException
	 * 
	 */
	public static List<ZipEntry> listZipContents(String zipFileName) throws IOException{
		List<ZipEntry> zipContents = new ArrayList<ZipEntry>();

		ZipFile zipFile = new ZipFile(zipFileName);
		Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
		while (zipEntries.hasMoreElements()) {
			zipContents.add(zipEntries.nextElement());
		}
		
		return zipContents;
	}
	
	/**
	 * Get a java.io.BufferedInputStream for reading 'src' from the specified 'zipFile'. Clients should make sure to call {@link ZipFileUtils#unmount()} when reading is complete.
	 * @param zipFile - Archive to read
	 * @param src - File to open for reading in the zipFile
	 * @param extensions - Extensions for zip file used if not standard
	 * @return
	 */
	public static java.io.BufferedInputStream openFile(java.io.File zipFile, String src, String[] extensions){
		ArchiveDetector detector = getArchiveDetector(extensions);
		String archiveFileName = zipFile + File.separator + src;
		try {
			File.setDefaultArchiveDetector(detector);
			FileInputStream fs = new FileInputStream(archiveFileName);
			java.io.BufferedInputStream stream = new java.io.BufferedInputStream(fs);
			return stream;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		
		return null;
	}

	/**
	 * Close all input and output streams.
	 * Equivalent to {@link File#umount(boolean, boolean, boolean, boolean)
	 */
	public static void unmount(){
		try {
			File.umount();
		} catch (ArchiveException e) {
			e.printStackTrace();
		}
	}

	public static boolean createNewZip(java.io.File zipFileToCreate) {
		boolean success = false;	
		if (zipFileToCreate.exists()){
			return true;
		}
		
		if (!zipFileToCreate.getParentFile().exists()){
			zipFileToCreate.mkdirs();
		}
		
		File f = new File(zipFileToCreate);

		try {
			success = f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			unmount();
		}
		
		return success;
	}
	
	/**
	 * Only unzips files in zip file not directories
	 * 
	 * @param zipped
	 *            file
	 * @param destPath
	 *            Destination path
	 * @return Files that were unzipped
	 */
	public static List<File> unzipFiles(java.io.File zippedfile, String destPath, IProgressMonitor monitor)
			throws FileNotFoundException, IOException {
		ZipFile zipFile = new ZipFile(zippedfile);

		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		List<File> outputFiles = new ArrayList<File>();
		File destinationFile = new File(destPath);
		if (!destinationFile.exists()) {
			destinationFile.mkdirs();
		}
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			File outputFile = new File(destinationFile, entry.getName());
			if (entry.isDirectory() && !outputFile.exists()) {
				outputFile.mkdirs();
				continue;
			}

			if (!outputFile.getParentFile().exists()) {
				outputFile.getParentFile().mkdirs();
			}

			java.io.InputStream inputStream = zipFile.getInputStream(entry);
			java.io.FileOutputStream outStream = new java.io.FileOutputStream(outputFile);
			copyByteStream(inputStream, outStream);

			outputFiles.add(outputFile);
			if (monitor != null) {
				monitor.worked(1);
			}
			outStream.close();
			inputStream.close();
		}
		zipFile.close();
		return outputFiles;
	}
	
	public static void copyByteStream(java.io.InputStream in, java.io.OutputStream out) throws IOException {
		if (in != null && out != null) {
			java.io.BufferedInputStream inBuffered = new java.io.BufferedInputStream(in);

			int bufferSize = 1000;
			byte[] buffer = new byte[bufferSize];

			int readCount;

			java.io.BufferedOutputStream fout = new java.io.BufferedOutputStream(out);

			while ((readCount = inBuffered.read(buffer)) != -1) {
				if (readCount < bufferSize) {
					fout.write(buffer, 0, readCount);
				} else {
					fout.write(buffer);
				}
			}
			fout.flush();
			fout.close();
			in.close();
		}
	}



	
}
