/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.search.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.ExternalSearchEditor;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.internal.ui.util.ExternalEditorInput;

/**
 * @author aniefer
 * Created on Jun 2, 2004
 */
public class SelectionParseAction extends Action {
	private static final String OPERATOR = "operator"; //$NON-NLS-1$
	protected static final String CSEARCH_OPERATION_NO_NAMES_SELECTED_MESSAGE = "CSearchOperation.noNamesSelected.message"; //$NON-NLS-1$
	protected static final String CSEARCH_OPERATION_OPERATION_UNAVAILABLE_MESSAGE = "CSearchOperation.operationUnavailable.message"; //$NON-NLS-1$
    protected static final String CSEARCH_OPERATION_NO_DEFINITION_MESSAGE = "CSearchOperation.noDefinitionFound.message"; //$NON-NLS-1$
    protected static final String CSEARCH_OPERATION_NO_DECLARATION_MESSAGE = "CSearchOperation.noDeclarationFound.message"; //$NON-NLS-1$
        
	protected IWorkbenchSite fSite;
	protected CEditor fEditor;

	public SelectionParseAction() {
		super();
	}
	
	public SelectionParseAction( CEditor editor ) {
		super();
		fEditor=editor;
		fSite=editor.getSite();
	}
	
	public SelectionParseAction(IWorkbenchSite site){
		super();
		fSite=site;
	}

	protected void operationNotAvailable(final String message) {
		// run the code to update the status line on the Display thread
		// this way any other thread can invoke operationNotAvailable(String)
		CUIPlugin.getStandardDisplay().asyncExec(new Runnable(){
			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				IStatusLineManager statusManager = null;
				 if (fSite instanceof IViewSite){
				 	statusManager = ((IViewSite) fSite).getActionBars().getStatusLineManager();
				 }
				 else if (fSite instanceof IEditorSite){
				 	statusManager = ((IEditorSite) fSite).getActionBars().getStatusLineManager();
				 }	
				 if( statusManager != null )
				 	statusManager.setErrorMessage(CSearchMessages.getString(message));
			}
		});
	}
	protected void clearStatusLine() {
		// run the code to update the status line on the Display thread
		// this way any other thread can invoke clearStatusLine()
		CUIPlugin.getStandardDisplay().asyncExec(new Runnable(){
			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				IStatusLineManager statusManager = null;
				 if (fSite instanceof IViewSite){
				 	statusManager = ((IViewSite) fSite).getActionBars().getStatusLineManager();
				 }
				 else if (fSite instanceof IEditorSite){
				 	statusManager = ((IEditorSite) fSite).getActionBars().getStatusLineManager();
				 }	
				 if( statusManager != null )
				 	statusManager.setErrorMessage( "" ); //$NON-NLS-1$
			}
		});
	}

	//TODO: Change this to work with qualified identifiers
	public ITextSelection getSelection( int fPos ) {
 		IDocumentProvider prov = ( fEditor != null ) ? fEditor.getDocumentProvider() : null;
 		IDocument doc = ( prov != null ) ? prov.getDocument(fEditor.getEditorInput()) : null;
 		
 		if( doc == null )
 			return null;
 		 
		int pos= fPos;
		char c;
		int fStartPos =0, fEndPos=0;
        int nonJavaStart=-1, nonJavaEnd=-1;
		String selectedWord=null;
		
		try{
			while (pos >= 0) {
				c= doc.getChar(pos);

                // TODO this logic needs to be improved
                // ex: ~destr[cursor]uctors, p2->ope[cursor]rator=(zero), etc
                if (nonJavaStart == -1 && !Character.isJavaIdentifierPart(c)) {
                    nonJavaStart=pos+1;
                }
                    
                if (Character.isWhitespace(c))
                    break;

				--pos;
			}
			fStartPos= pos + 1;
			
			pos= fPos;
			int length= doc.getLength();
			while (pos < length) {
				c= doc.getChar(pos);

                if (nonJavaEnd == -1 && !Character.isJavaIdentifierPart(c)) {
                    nonJavaEnd=pos;
                }
                if (Character.isWhitespace(c))
                    break;
				++pos;
			}
			fEndPos= pos;
			selectedWord = doc.get(fStartPos, (fEndPos - fStartPos));
        }
        catch(BadLocationException e){
        }
        
        boolean selectedOperator=false;
        if (selectedWord != null && selectedWord.indexOf(OPERATOR) >= 0 && fPos >= fStartPos + selectedWord.indexOf(OPERATOR) && fPos < fStartPos + selectedWord.indexOf(OPERATOR) + OPERATOR.length()) {
            selectedOperator=true;
        }
    
        // if the operator was selected, get its proper bounds
        if (selectedOperator && fEditor.getEditorInput() instanceof IFileEditorInput &&  
                CoreModel.hasCCNature(((IFileEditorInput)fEditor.getEditorInput()).getFile().getProject())) {
            int actualStart=fStartPos + selectedWord.indexOf(OPERATOR);
            int actualEnd=getOperatorActualEnd(doc, fStartPos + selectedWord.indexOf(OPERATOR) + OPERATOR.length());
            
            actualEnd=(actualEnd>0?actualEnd:fEndPos);
            
            return new TextSelection(doc, actualStart, actualEnd - actualStart);
        // TODO Devin this only works for definitions of destructors right now
        // if there is a destructor and the cursor is in the destructor name's segment then get the entire destructor
        } else if (selectedWord != null && selectedWord.indexOf('~') >= 0 && fPos - 2 >= fStartPos + selectedWord.lastIndexOf(new String(Keywords.cpCOLONCOLON))) {
            int tildePos = selectedWord.indexOf('~');
            int actualStart=fStartPos + tildePos;
            int length=0;
            char temp;
            char[] lastSegment = selectedWord.substring(tildePos).toCharArray();
            for(int i=1; i<lastSegment.length; i++) {
                temp = lastSegment[i];
                if (!Character.isJavaIdentifierPart(temp)) {
                    length=i;
                    break;
                }
            }
            
            // if the cursor is after the destructor name then use the regular boundaries 
            if (fPos >= actualStart + length) {
            	return new TextSelection(doc, nonJavaStart, length);
            } else {
            	return new TextSelection(doc, actualStart, length);
            }
        } else {
            // otherwise use the non-java identifier parts as boundaries for the selection
        	return new TextSelection(doc, nonJavaStart, nonJavaEnd - nonJavaStart);
        }
	}
    
    private int getOperatorActualEnd(IDocument doc, int index) {
        char c1, c2;
        int actualEnd=-1;
        boolean multiComment=false;
        boolean singleComment=false;
        int possibleEnd=-1;
        while (actualEnd==-1) {
            try {
                c1=doc.getChar(index);
                c2=doc.getChar(index+1);
                
                // skip anything within a single-line comment
                if (singleComment) {
                    char c3=doc.getChar(index-1);
                    if (c3 != '\\' && (c1 == '\n' || c1 == '\r' && c2 == '\n' )) {
                        singleComment=false;
                    }
                    index++;
                    continue;
                }
                // skip anything within a multi-line comment
                if (multiComment) {
                    if (c1 == '*' && c2 == '/') {
                        multiComment=false;
                        index+=2;
                        continue;
                    }
                    index++;
                    continue;
                }
                
                switch(c1) {
                case '+': {
                    switch(c2) {
                    case '=':
                    case '+':
                        actualEnd=index+2;
                        break;
                    default:
                        actualEnd=index+1;
                        break;
                    }
                    break;
                }
                case '-': {
                    switch(c2) {
                    case '=':
                        actualEnd=index+2;
                        break;
                    case '-':
                        switch(doc.getChar(index+2)) {
                        case '>': {
                            switch(doc.getChar(index+3)) {
                            case '*':
                                actualEnd=index+4;
                                break;
                            default:
                                actualEnd=index+3;
                                break;
                            }
                            break;
                        }
                        default:
                            actualEnd=index+2;
                            break;                                
                        }
                        break;
                    default:
                        
                        break;
                    }
                    break;
                }
                case '|': {
                    switch(c2) {
                    case '=':
                    case '|':
                        actualEnd=index+2;
                        break;
                    default:
                        actualEnd=index+1;
                        break;
                    }
                    break;                  
                }
                case '&': {
                    switch(c2) {
                    case '=':
                    case '&':
                        actualEnd=index+2;
                        break;
                    default:
                        actualEnd=index+1;
                        break;
                    }
                    break;
                }
                case '/': {
                    switch(c2) {
                    case '/':
                        singleComment=true;
                        index+=2;
                        break;
                    case '*':
                        multiComment=true;
                        index+=2;
                        break;
                    case '=':
                        actualEnd=index+2;
                        break;
                    default:
                        actualEnd=index+1;
                        break;
                    }
                    break;
                }
                case '*':
                case '%': 
                case '^': 
                case '!': 
                case '=': {
                    switch(c2) {
                    case '=':
                        actualEnd=index+2;
                        break;
                    default:
                        actualEnd=index+1;
                        break;
                    }
                    break;
                }
                case '(': {
                    if (possibleEnd > 0)
                        actualEnd = possibleEnd;
                    
                    index++;
                    
                    break;
                }   
                case ']':
                case ')':
                case ',':
                case '~': {
                    actualEnd=index+1;
                    break;
                }
                case '<': {
                    switch(c2) {
                    case '=':
                    case '<':
                        switch(doc.getChar(index+2)) {
                        case '=':
                            actualEnd=index+3;
                            break;
                        default:
                            actualEnd=index+2;
                            break;
                        }
                        break;
                    default:
                        actualEnd=index;
                        break;
                    }
                    break;                  
                }
                case '>': {
                    switch(c2) {
                    case '=':
                    case '>':
                        switch(doc.getChar(index+2)) {
                        case '=':
                            actualEnd=index+3;
                            break;
                        default:
                            actualEnd=index+2;
                            break;
                        }
                        break;
                    default:
                        actualEnd=index;
                        break;
                    }
                    break;  
                }
                case 'n': { // start of "new"
                    while (doc.getChar(++index) != 'w') {}
                    possibleEnd=++index;
                    break;
                }
                case 'd': { // start of "delete"
                    while (doc.getChar(++index) != 't' && doc.getChar(index+1) != 'e'){}
                    index+=2;
                    possibleEnd=index;
                    break;
                }
                default:
                    index++;
                    break;
                }
            } catch (BadLocationException e) {
                // something went wrong
                return -1;
            }
        }
        
        return actualEnd;
    }
    
	/**
	  * Return the selected string from the editor
	  * @return The string currently selected, or null if there is no valid selection
	  */
	protected ITextSelection getSelection( ITextSelection textSelection ) {
		if( textSelection == null )
			return null;
		
		 if (textSelection.getLength() == 0) {
	 		 return getSelection(textSelection.getOffset());
		 } else {
			 return textSelection;
		 }
	}
	
	protected ISelection getSelection() {
		ISelection sel = null;
		if (fSite != null && fSite.getSelectionProvider() != null ){
			sel = fSite.getSelectionProvider().getSelection();
		}
		
		return sel;
	}
	
    protected ITextSelection getSelectedStringFromEditor() {
        ISelection selection = getSelection();
        if( selection == null || !(selection instanceof ITextSelection) ) 
             return null;

        return getSelection( (ITextSelection)selection );
    }
    
    /**
     * Open the editor on the given name.
     * 
     * @param name
     */
    protected void open(IName name) throws CoreException {
    	IASTFileLocation fileloc = name.getFileLocation();
    	if (fileloc == null)
    		// no source location - TODO spit out an error in the status bar
    		return;
    	
		IPath path = new Path(fileloc.getFileName());
    	int currentOffset = fileloc.getNodeOffset();
    	int currentLength = fileloc.getNodeLength();
    	
		open(path, currentOffset, currentLength);
    }

	protected void open(IPath path, int currentOffset, int currentLength) throws PartInitException {
		IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(path);
		if (files.length > 0) {
			IEditorPart editor = IDE.openEditor(CUIPlugin.getActivePage(), files[0]);
			try {
				IMarker marker = files[0].createMarker(NewSearchUI.SEARCH_MARKER);
				marker.setAttribute(IMarker.CHAR_START, currentOffset);
				marker.setAttribute(IMarker.CHAR_END, currentOffset + currentLength);
				IDE.gotoMarker(editor, marker);
				marker.delete();
			} catch (CoreException e) {
				CUIPlugin.getDefault().log(e);
			}
		} else {
			// external file
			IEditorInput input = new ExternalEditorInput(new FileStorage(path));
			IEditorPart editor = CUIPlugin.getActivePage().openEditor(input, ExternalSearchEditor.EDITOR_ID);
			if (editor instanceof ITextEditor) {
				ITextEditor textEditor = (ITextEditor)editor;
				textEditor.selectAndReveal(currentOffset, currentLength);
			}
		}
	}
    
    public void update() {
		setEnabled(getSelectedStringFromEditor() != null);
	}

}
