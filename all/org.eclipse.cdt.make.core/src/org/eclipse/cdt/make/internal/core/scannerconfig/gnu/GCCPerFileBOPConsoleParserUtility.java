/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Martin Oberhuber (Wind River Systems) - bug 155096
 *     Gerhard Schaber (Wind River Systems)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig.gnu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.CCommandDSC;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.KVStringPair;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.SCDOptionsEnum;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.TraceUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * TODO Provide description
 * 
 * @author vhirsl
 */
public class GCCPerFileBOPConsoleParserUtility extends AbstractGCCBOPConsoleParserUtility {
    private Map directoryCommandListMap;
    private List compiledFileList;
    
    private List commandsList2;
    
    private int workingDirsN = 0;
    private int commandsN = 0;
    private int filesN = 0;
	private String fDefaultMacroDefinitionValue= "1"; //$NON-NLS-1$


    /**
     * @param markerGenerator 
     * @param workingDirectory 
     * @param project 
     */
    public GCCPerFileBOPConsoleParserUtility(IProject project, IPath workingDirectory, IMarkerGenerator markerGenerator) {
        super(project, workingDirectory, markerGenerator);
    }

    /**
     * Adds a mapping filename, generic_command
     * @param longFileName
     * @param genericLine
     */
    void addGenericCommandForFile(String longFileName, String genericCommand) {
        // if a file name has already been added once, return
        if (compiledFileList.contains(longFileName))
            return;
        compiledFileList.add(longFileName);
        
        String workingDir = getWorkingDirectory().toString();
        List directoryCommandList = (List) directoryCommandListMap.get(workingDir);
        if (directoryCommandList == null) {
            directoryCommandList = new ArrayList();
            directoryCommandListMap.put(workingDir, directoryCommandList);
            ++workingDirsN;
        }
        Map command21FileListMap = null;
        for (Iterator i = directoryCommandList.iterator(); i.hasNext(); ) {
            command21FileListMap = (Map) i.next();
            List fileList = (List) command21FileListMap.get(genericCommand);
            if (fileList != null) {
                if (!fileList.contains(longFileName)) {
                    fileList.add(longFileName);
                    ++filesN;
                }
                return;
            }
        }
        command21FileListMap = new HashMap(1);
        directoryCommandList.add(command21FileListMap);
        ++commandsN;
        List fileList = new ArrayList();
        command21FileListMap.put(genericCommand, fileList);
        fileList.add(longFileName);
        ++filesN;
    }

    /**
     * 
     */
    void generateReport() {
        TraceUtil.metricsTrace("Stats for directory ", //$NON-NLS-1$
                   "Generic command: '", "' applicable for:",  //$NON-NLS-1$ //$NON-NLS-2$
                   directoryCommandListMap);
        TraceUtil.summaryTrace("Discovery summary", workingDirsN, commandsN, filesN); //$NON-NLS-1$
    }

    /**
     * Adds a mapping command line -> file, this time without a dir
     * @param longFileName
     * @param genericLine
     */
    void addGenericCommandForFile2(String longFileName, String genericLine) {
        // if a file name has already been added once, return
        if (compiledFileList.contains(longFileName))
            return;
        compiledFileList.add(longFileName);

        String[] tokens = genericLine.split("\\s+"); //$NON-NLS-1$
        CCommandDSC command = getNewCCommandDSC(tokens, 0, false); // assume .c file type
        int index = commandsList2.indexOf(command);
        if (index == -1) {
            commandsList2.add(command);
            ++commandsN;
        }
        else {
            command = (CCommandDSC) commandsList2.get(index);
        }
//        // add a file
//        command.addFile(longFileName);
//        ++filesN;
    }

    /**
     * @param genericLine
     * @param cppFileType
     * @return CCommandDSC compile command description 
     */
    public CCommandDSC getNewCCommandDSC(String[] tokens, final int idxOfCompilerCommand, boolean cppFileType) {
		ArrayList dirafter = new ArrayList();
		ArrayList includes = new ArrayList();
        CCommandDSC command = new CCommandDSC(cppFileType, getProject());
        command.addSCOption(new KVStringPair(SCDOptionsEnum.COMMAND.toString(), tokens[idxOfCompilerCommand]));
        for (int i = idxOfCompilerCommand+1; i < tokens.length; ++i) {
        	String token = tokens[i];
        	//Target specific options: see GccScannerInfoConsoleParser
			if (token.startsWith("-m") ||		//$NON-NLS-1$
				token.equals("-ansi") ||		//$NON-NLS-1$
				token.equals("-posix") ||		//$NON-NLS-1$
				token.equals("-pthread") ||		//$NON-NLS-1$
				token.startsWith("-O") ||		//$NON-NLS-1$
				token.equals("-fno-inline") ||	//$NON-NLS-1$
				token.startsWith("-finline") ||	//$NON-NLS-1$
				token.equals("-fno-exceptions") ||	//$NON-NLS-1$
				token.equals("-fexceptions") ||		//$NON-NLS-1$
				token.equals("-fshort-wchar") ||	//$NON-NLS-1$
				token.equals("-fshort-double") ||	//$NON-NLS-1$
				token.equals("-fno-signed-char") ||	//$NON-NLS-1$
				token.equals("-fsigned-char") ||	//$NON-NLS-1$
				token.startsWith("-fabi-version=")	//$NON-NLS-1$
			) {		
		        command.addSCOption(new KVStringPair(SCDOptionsEnum.COMMAND.toString(), token));
				continue;
        	}
            for (int j = SCDOptionsEnum.MIN; j <= SCDOptionsEnum.MAX; ++j) {
                final SCDOptionsEnum optionKind = SCDOptionsEnum.getSCDOptionsEnum(j);
				if (token.startsWith(optionKind.toString())) {
                    String option = token.substring(
                            optionKind.toString().length()).trim();
                    if (option.length() > 0) {
                        // ex. -I/dir
                    }
                    else if (optionKind.equals(SCDOptionsEnum.IDASH)) {
                    	for (Iterator iter=includes.iterator(); iter.hasNext(); ) {
                    		option = (String)iter.next();
                            KVStringPair pair = new KVStringPair(SCDOptionsEnum.IQUOTE.toString(), option);
                        	command.addSCOption(pair);                    		
                    	}
                    	includes = new ArrayList();
                        // -I- has no parameter
                    }
                    else {
                        // ex. -I /dir
                        // take a next token
                        if (i+1 < tokens.length && !tokens[i+1].startsWith("-")) { //$NON-NLS-1$
                            option = tokens[++i];
                        }
                        else break;
                    }
                    
                    if (option.length() > 0 && (
                            optionKind.equals(SCDOptionsEnum.INCLUDE) ||
                            optionKind.equals(SCDOptionsEnum.INCLUDE_FILE) ||
                            optionKind.equals(SCDOptionsEnum.IMACROS_FILE) ||
                            optionKind.equals(SCDOptionsEnum.IDIRAFTER) ||
                            optionKind.equals(SCDOptionsEnum.ISYSTEM) || 
                            optionKind.equals(SCDOptionsEnum.IQUOTE) )) {
                        option = (getAbsolutePath(option)).toString();
                    }
                    
                    if (optionKind.equals(SCDOptionsEnum.IDIRAFTER)) {
                        KVStringPair pair = new KVStringPair(SCDOptionsEnum.INCLUDE.toString(), option);
                    	dirafter.add(pair);
                    }
                    else if (optionKind.equals(SCDOptionsEnum.INCLUDE)) {
                    	includes.add(option);
                    }
                    else { // add the pair
                    	if (optionKind.equals(SCDOptionsEnum.DEFINE)) {
                        	if (option.indexOf('=') == -1) {
                        		option += '='+ fDefaultMacroDefinitionValue;
                        	}
                    	}
                        KVStringPair pair = new KVStringPair(optionKind.toString(), option);
                    	command.addSCOption(pair);
                    }
                    break;
                }
            }
        }
        String option;
    	for (Iterator iter=includes.iterator(); iter.hasNext(); ) {
    		option = (String)iter.next();
            KVStringPair pair = new KVStringPair(SCDOptionsEnum.INCLUDE.toString(), option);
        	command.addSCOption(pair);                    		
    	}
    	for (Iterator iter=dirafter.iterator(); iter.hasNext(); ) {
        	command.addSCOption((KVStringPair)iter.next());                    		
    	}
        return command;
    }

    public void setDefaultMacroDefinitionValue(String val) {
    	if (val != null) {
    		fDefaultMacroDefinitionValue= val;
    	}
	}

	/**
     * @param filePath : String
     * @return filePath : IPath - not <code>null</code>
     */
    public IPath getAbsolutePath(String filePath) {
        IPath pFilePath;
        if (filePath.startsWith("/")) { //$NON-NLS-1$
        	return convertCygpath(new Path(filePath));
        }
        else if (filePath.startsWith("\\") || //$NON-NLS-1$
            (!filePath.startsWith(".") && //$NON-NLS-1$
             filePath.length() > 2 && filePath.charAt(1) == ':' && 
             (filePath.charAt(2) == '\\' || filePath.charAt(2) == '/'))) {
            // absolute path
            pFilePath = new Path(filePath);
        }
        else {
            // relative path
            IPath cwd = getWorkingDirectory();
            if (!cwd.isAbsolute()) {
                cwd = getBaseDirectory().append(cwd);
            }
            if (filePath.startsWith("`pwd`")) { //$NON-NLS-1$
            	if (filePath.length() > 5 && (filePath.charAt(5) == '/' || filePath.charAt(5) == '\\')) {
            		filePath = filePath.substring(6);
            	}
            	else {
            		filePath = filePath.substring(5);
            	}
            }
            pFilePath = cwd.append(filePath);
        }
        return pFilePath;
    }

    /**
     * 
     */
//    void generateReport2() {
//        StringWriter buffer = new StringWriter();
//        PrintWriter writer = new PrintWriter(buffer);
//        for (Iterator i = commandsList2.iterator(); i.hasNext(); ) {
//            CCommandDSC cmd = (CCommandDSC)i.next();
//            writer.println("Stats for generic command: '" + cmd.getCommandAsString() + "' applicable for " + 
//                    Integer.toString(cmd.getNumberOfFiles()) + " files: ");
//            List filesList = cmd.getFilesList();
//            if (filesList != null) {
//                for (Iterator j = filesList.iterator(); j.hasNext(); ) {
//                    writer.println("    " + (String)j.next());
//                }
//            }
//        }
//        writer.close();
//            
//        TraceUtil.metricsTrace(buffer.toString());
//        TraceUtil.summaryTrace("Discovery summary", workingDirsN, commandsN, filesN);
//    }

    /**
     * Returns all CCommandDSC collected so far.
     * Currently this list is not filled, so it will always return an empty list.
     * @return List of CCommandDSC
     */
    public List getCCommandDSCList() {
        return new ArrayList(commandsList2);
    }

}
