/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.core.index.ctagsindexer;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.IConsoleParser;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.internal.core.ConsoleOutputSniffer;
import org.eclipse.cdt.internal.core.index.impl.IndexedFile;
import org.eclipse.cdt.internal.core.index.sourceindexer.AbstractIndexer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * @author Bogdan Gheorghe
 */
public class CTagsIndexerRunner extends AbstractIndexer {
	IFile resourceFile;
	private CTagsIndexer indexer;
    /**
     * @param resource
     * @param indexer
     */
    public CTagsIndexerRunner(IFile resource, CTagsIndexer indexer) {
        this.resourceFile = resource;
        this.indexer = indexer;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.index.sourceindexer.AbstractIndexer#getResourceFile()
     */
    public IFile getResourceFile() {
        return resourceFile;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.index.sourceindexer.AbstractIndexer#indexFile(org.eclipse.cdt.internal.core.index.IDocument)
     */
    protected void indexFile(IFile file) throws IOException {
    	IndexedFile indFile =output.addIndexedFile(file.getFullPath().toString());
       
    	String[] args = {"ctags", //$NON-NLS-1$
		        "--excmd=number",  //$NON-NLS-1$
		        "--format=2", //$NON-NLS-1$
				"--sort=no",  //$NON-NLS-1$
				"--fields=aiKlmnsz", //$NON-NLS-1$
				"--c-types=cdefgmnpstuvx", //$NON-NLS-1$
				"--c++-types=cdefgmnpstuvx", //$NON-NLS-1$
				"--languages=c,c++", //$NON-NLS-1$
				"-f", "-", resourceFile.getName()}; //$NON-NLS-1$ //$NON-NLS-2$
    	
        try {
            IConsole console = CCorePlugin.getDefault().getConsole(null);
            console.start(resourceFile.getProject());
            OutputStream cos = console.getOutputStream();

            String errMsg = null;
            CommandLauncher launcher = new CommandLauncher();
            // Print the command for visual interaction.
            launcher.showCommand(true);
            
            long startTime=0;
            if (AbstractIndexer.TIMING)
                startTime = System.currentTimeMillis();
            
            CTagsConsoleParser parser = new CTagsConsoleParser(this);
            IConsoleParser[] parsers = { parser };
            ConsoleOutputSniffer sniffer = new ConsoleOutputSniffer(parsers);
            
            OutputStream consoleOut = (sniffer == null ? cos : sniffer.getOutputStream());
            OutputStream consoleErr = (sniffer == null ? cos : sniffer.getErrorStream());
            
            IPath fileDirectory = resourceFile.getRawLocation().removeLastSegments(1);
            //Process p = launcher.execute(fCompileCommand, args, setEnvironment(launcher), fWorkingDirectory);
            Process p = launcher.execute(new Path(""), args, null, fileDirectory); //$NON-NLS-1$
            if (p != null) {
                try {
                    // Close the input of the Process explicitely.
                    // We will never write to it.
                    p.getOutputStream().close();
                } catch (IOException e) {
                }
                if (launcher.waitAndRead(consoleOut, consoleErr, new NullProgressMonitor()) != CommandLauncher.OK) {
                    errMsg = launcher.getErrorMessage();
                }
            }
            else {
                errMsg = launcher.getErrorMessage();
            }

            if (errMsg != null) {
               System.out.println(errMsg);
            }

            consoleOut.close();
            consoleErr.close();
            cos.close();
            
            if (AbstractIndexer.TIMING){
                System.out.println("CTagsIndexer Total Time: " + (System.currentTimeMillis() - startTime)); //$NON-NLS-1$
                System.out.flush();
            }  
               
        }
        catch (Exception e) {
            CCorePlugin.log(e);
        }
        finally {
           
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.index.sourceindexer.AbstractIndexer#addMarkers(org.eclipse.core.resources.IFile, org.eclipse.core.resources.IFile, java.lang.Object)
     */
    protected void addMarkers(IFile tempFile, IFile originator, Object problem) {
        // TODO Auto-generated method stub
        
    }
    
    
    
}
