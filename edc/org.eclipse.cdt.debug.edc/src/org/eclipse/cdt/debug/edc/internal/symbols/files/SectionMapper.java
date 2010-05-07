/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.symbols.files;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.edc.IStreamBuffer;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.FileStreamBuffer;
import org.eclipse.cdt.debug.edc.internal.MemoryStreamBuffer;
import org.eclipse.cdt.utils.ERandomAccessFile;
import org.eclipse.core.runtime.IPath;

/**
 * Handle mapping sections into memory and caching this content.
 */
public class SectionMapper implements ISectionMapper {
	
	private final IPath hostFile;
	private final boolean isLE;
	private ERandomAccessFile efile;
	/** all sections loaded for any reason */
	private Map<SectionInfo, IStreamBuffer> loadedSections;	
	/** subset of sections loaded into memory maps */
	private Map<SectionInfo, IStreamBuffer> mappedBuffers;
	
	public SectionMapper(IPath hostFile, boolean isLE) {
		this.hostFile = hostFile;
		this.isLE = isLE;
		this.efile = null;
		this.loadedSections = new HashMap<SectionInfo, IStreamBuffer>();
		this.mappedBuffers = new HashMap<SectionInfo, IStreamBuffer>();
	}
	
	public void dispose() {
		for (Map.Entry<SectionInfo, IStreamBuffer> entry: loadedSections.entrySet()) {
			IStreamBuffer buffer = entry.getValue();
			if (buffer == null)
				continue;
			if (mappedBuffers.containsKey(entry.getKey()))
				FileStatistics.currentMemoryMappedBuffers -= buffer.capacity();
			else
				FileStatistics.currentHeapAllocatedBuffers -= buffer.capacity();
		}
		loadedSections.clear();
		mappedBuffers.clear();
		close();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.exe.ISectionMapper#getMappedFile()
	 */
	public IPath getMappedFile() {
		return hostFile;
	}
	
	/**
	 * 
	 */
	private void ensureOpen() throws IOException {
		if (efile == null) {
			FileStatistics.log("Opening " + hostFile.toFile());
			FileStatistics.executablesOpened++;
			FileStatistics.executablesOpen++;
			efile = new ERandomAccessFile(hostFile.toFile(), "r");
		}
	}


	private void close() {
		if (!mappedBuffers.isEmpty())
			throw new IllegalStateException("cannot close file; mapped buffers open");
		if (efile != null) {
			try {
				FileStatistics.log("Closing " + hostFile.toFile());
				FileStatistics.executablesOpen--;
				efile.close();
			} catch (IOException e) {
				// ignore
			}
		}
		efile = null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.exe.IExecutableSectionMapper#getSectionBuffer(org.eclipse.cdt.debug.edc.internal.symbols.exe.SectionInfo)
	 */
	public IStreamBuffer getSectionBuffer(SectionInfo section) throws IOException {
		
		ensureOpen();

		IStreamBuffer buffer = loadedSections.get(section);
		if (buffer == null) {
			buffer = loadSection(section);
			
			loadedSections.put(section, buffer);
			// TODO: flush data occasionally, before #dispose()
		}
		
		return buffer;
	}

	private IStreamBuffer loadSection(SectionInfo section)
			throws IOException {
		FileStatistics.log("Loading " + section + " from " + hostFile.toFile());
		
		IStreamBuffer buffer = null;
		
		// If the sym file is too large, it's useless reading it 
		// into the heap and choking the memory.
		// Just read it on-demand from disk.
		try {
			if (section.sectionSize > 4 * 1024 * 1024) {
				buffer = loadSectionIntoFileStreamBuffer(section);
			} else {
				buffer = loadSectionIntoHeap(section);
			}
		} catch (IOException e) {
			buffer = loadSectionIntoHeap(section);
		}
		
		return buffer;
	}

	/**
	 * Load section contents into a streaming buffer.  This is a little slower but 
	 * does not allocate any more than a page of memory at a time.
	 * 
	 * @param section
	 * @param buffer
	 * @return new {@link IStreamBuffer}
	 */
	private IStreamBuffer loadSectionIntoFileStreamBuffer(SectionInfo section) throws IOException {
		IStreamBuffer buffer = null;
		try {
			buffer = new FileStreamBuffer(efile, isLE ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN, 
					section.fileOffset, section.sectionSize);
			mappedBuffers.put(section, buffer);
			FileStatistics.currentMemoryMappedBuffers += buffer.capacity();
			FileStatistics.totalMemoryMappedBuffers += buffer.capacity();
		} catch (Throwable e2) {
			EDCDebugger.getMessageLogger().logError("Failed to make buffer for section " + section, e2);
		}
		return buffer;
	}

	private IStreamBuffer loadSectionIntoHeap(SectionInfo section)
			throws IOException {
		IStreamBuffer buffer;
		// try to load the section into memory because it will
		// be faster
		byte[] data = new byte[(int)section.sectionSize];
		efile.seek(section.fileOffset);
		efile.read(data);
		buffer = new MemoryStreamBuffer(data, isLE ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
		FileStatistics.currentHeapAllocatedBuffers += data.length;
		FileStatistics.totalHeapAllocatedBuffers += data.length;
		return buffer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.exe.ISectionMapper#releaseBuffer(java.nio.ByteBuffer)
	 */
	public void releaseSectionBuffer(SectionInfo section) {
		IStreamBuffer buffer = loadedSections.remove(section);
		if (buffer != null) {
			if (mappedBuffers.remove(section) != null) {
				FileStatistics.currentMemoryMappedBuffers -= buffer.capacity();
				if (mappedBuffers.isEmpty()) {
					close();
				}
			} else {
				FileStatistics.currentHeapAllocatedBuffers -= buffer.capacity();
			}
		}
	}
	
}
