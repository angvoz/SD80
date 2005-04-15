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
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchConstants.LimitTo;
import org.eclipse.cdt.internal.core.index.cindexstorage.IndexedFileEntry;
import org.eclipse.cdt.internal.core.index.sourceindexer.AbstractIndexer;
import org.eclipse.cdt.internal.core.search.indexing.IIndexEncodingConstants;
import org.eclipse.cdt.internal.core.search.indexing.IIndexEncodingConstants.EntryType;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;

public class IndexEncoderUtil {
    
    public static final char[] encodeEntry(char[][] elementName, EntryType entryType, LimitTo encodeType) {
        // Temporarily
        if (elementName == null) {
            return "NPE".toCharArray(); //$NON-NLS-1$
        }
        int pos, nameLength = 0;
        for (int i=0; i < elementName.length; i++){
            char[] namePart = elementName[i];
            nameLength += namePart.length;
        }
        char[][] encodedTypeNames = null;
        if (encodeType == ICSearchConstants.DECLARATIONS) {
            encodedTypeNames = IIndexEncodingConstants.encodedTypeNames_Decl;
        }
        else if (encodeType == ICSearchConstants.REFERENCES) {
            encodedTypeNames = IIndexEncodingConstants.encodedTypeNames_Ref;
        }
        char[] encodedTypeName = encodedTypeNames[entryType.toInt()];
        
        //char[] has to be of size - [type length + length of the name (including qualifiers) + 
        //separators (need one less than fully qualified name length)
        char[] result = new char[encodedTypeName.length + nameLength + elementName.length - 1];
        System.arraycopy(encodedTypeName, 0, result, 0, pos = encodedTypeName.length);
        if (elementName.length > 0) {
        //Extract the name first
            char [] tempName = elementName[elementName.length-1];
            System.arraycopy(tempName, 0, result, pos, tempName.length);
            pos += tempName.length;
        }
        //Extract the qualifiers
        for (int i=elementName.length - 2; i>=0; i--){
            result[pos++] = IIndexEncodingConstants.SEPARATOR;
            char [] tempName = elementName[i];
            System.arraycopy(tempName, 0, result, pos, tempName.length);
            pos+=tempName.length;               
        }
        
        if (AbstractIndexer.VERBOSE)
            AbstractIndexer.verbose(new String(result));
            
        return result;
    }

    public static int calculateIndexFlags(DOMSourceIndexerRunner indexer, IASTFileLocation loc) {
        int fileNum= 0;
        
        //Initialize the file number to be the file number for the file that triggerd
        //the indexing. Note that we should always be able to get a number for this as
        //the first step in the Source Indexer is to add the file being indexed to the index
        //which actually creates an entry for the file in the index.
        
        IndexedFileEntry mainIndexFile = indexer.getOutput().getIndexedFile(
                indexer.getResourceFile().getFullPath().toString());
        if (mainIndexFile != null)
            fileNum = mainIndexFile.getFileID();
        
        String fileName = loc.getFileName();
        if (fileName != null) {
            IFile tempFile = CCorePlugin.getWorkspace().getRoot().getFileForLocation(new Path(fileName));   
            String filePath = ""; //$NON-NLS-1$
            if (tempFile != null){
                //File is local to workspace
                filePath = tempFile.getFullPath().toOSString();
            }
            else {
				//File is external to workspace
                filePath = fileName;
            }
            
            if (!filePath.equals(indexer.getResourceFile().getFullPath().toOSString())) {
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
        IASTFileLocation fileLoc = null;
        
        IASTNodeLocation[] locs = node.getNodeLocations();
        if (locs.length == 1) {
            if (locs[0] instanceof IASTFileLocation) {
                fileLoc = (IASTFileLocation) locs[0];
            }
        }
        else if (locs.length > 1) {
            fileLoc = node.getTranslationUnit().flattenLocationsToFile(locs);
        }
        return fileLoc;
    }

}
