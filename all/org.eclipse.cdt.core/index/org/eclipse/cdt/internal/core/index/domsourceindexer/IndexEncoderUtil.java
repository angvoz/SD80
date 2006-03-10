/***********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.internal.core.index.domsourceindexer;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.index.cindexstorage.IndexedFileEntry;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;

public class IndexEncoderUtil {

    public static int calculateIndexFlags(DOMSourceIndexerRunner indexer, IASTFileLocation loc) {
        int fileNum= 0;
        
        //Initialize the file number to be the file number for the file that triggerd
        //the indexing. Note that we should always be able to get a number for this as
        //the first step in the Source Indexer is to add the file being indexed to the index
        //which actually creates an entry for the file in the index.
        
        String fullPath = indexer.getResourceFile().getFullPath().toString(); 
        IndexedFileEntry mainIndexFile = indexer.getOutput().getIndexedFile( fullPath );
        if (mainIndexFile != null)
            fileNum = mainIndexFile.getFileID();
        
        String fileName = loc.getFileName();
        if (fileName != null) {
            IFile tempFile = CCorePlugin.getWorkspace().getRoot().getFileForLocation(new Path(fileName));   
            String filePath = ""; //$NON-NLS-1$
            if (tempFile != null){
                //File is local to workspace
                filePath = tempFile.getFullPath().toString();
            }
            else {
				//File is external to workspace
                filePath = fileName;
            }
            
            if (!filePath.equals(fullPath)) {
	            //We are not in the file that has triggered the index. Thus, we need to find the
	            //file number for the current file (if it has one). If the current file does not
	            //have a file number, we need to add it to the index.
               IndexedFileEntry indFile = indexer.getOutput().getIndexedFile(filePath);
                if (indFile != null) {
                    fileNum = indFile.getFileID();
                }
                else {
                    //Need to add file to index
                    indFile = indexer.getOutput().addIndexedFile(filePath);
                    if (indFile != null)
                        fileNum = indFile.getFileID();
                }
            }
        }
        
        return fileNum;
    }

    /**
     * @param name
     * @return
     */
    public static IASTFileLocation getFileLocation(IASTNode node) {
        return node.getFileLocation();
    }
/*
 * This method is never called
 * 
	public static boolean nodeInExternalHeader(IASTNode node) {
		String fileName = node.getContainingFilename();
		return (CCorePlugin.getWorkspace().getRoot().getFileForLocation(new Path(fileName)) == null)
				? true : false;
	}
*/
	static private boolean visitedExternalHeader = false;
	static private String lastVisitedFile = null;
	static private String wspLocation = CCorePlugin.getWorkspace().getRoot().getLocation().toOSString();
	
	public static boolean nodeInVisitedExternalHeader(IASTNode node, DOMSourceIndexer indexer) {
		String fileName = node.getContainingFilename();
		
		// if file is the same as before, return previous result.
		if (fileName.equals(lastVisitedFile)) { 
			return visitedExternalHeader;
		}
		lastVisitedFile = fileName;
		// If file is external, check whether it's encountered
		visitedExternalHeader = (!fileName.startsWith(wspLocation)) && indexer.haveEncounteredHeader(indexer.getProject().getFullPath(), fileName);  
		return visitedExternalHeader;
	}

}
