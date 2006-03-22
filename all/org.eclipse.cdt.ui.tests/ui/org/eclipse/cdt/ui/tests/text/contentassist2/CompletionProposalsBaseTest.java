/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text.contentassist2;

/**
 * @author hamer
 *
 *	This abstract class is the base class for all completion proposals test cases
 *	 
 */
import java.io.FileInputStream;

import junit.framework.TestCase;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.internal.core.browser.cache.TypeCacheManager;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.text.contentassist.CCompletionProcessor2;
import org.eclipse.cdt.ui.testplugin.CTestPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

public abstract class CompletionProposalsBaseTest  extends TestCase{
	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private final String projectName = "TestProject1"; //$NON-NLS-1$
	private final String projectType = "bin"; //$NON-NLS-1$
	private ICProject fCProject;
	private IFile fCFile;
	private IFile fHeaderFile;
	private NullProgressMonitor monitor;
	private ITranslationUnit tu = null;
	private String buffer = EMPTY_STRING;
	private Document document = null;
	
		
	public CompletionProposalsBaseTest(String name) {
		super(name);
	}
	
	/*
	 * Derived classes have to provide there abstract methods
	 */
	protected abstract String getFileName();
	protected abstract String getFileFullPath();
	protected abstract String getHeaderFileName();
	protected abstract String getHeaderFileFullPath();
	protected abstract int getCompletionPosition();
	protected abstract String getExpectedScopeClassName();
	protected abstract String getExpectedContextClassName();
	protected abstract String getExpectedPrefix();
	protected abstract IASTCompletionNode.CompletionKind getExpectedKind();
	protected abstract String[] getExpectedResultsValues();
	protected String getFunctionOrConstructorName()	{ return EMPTY_STRING; }
	
	protected void setUp() throws Exception {
		monitor = new NullProgressMonitor();
		
		fCProject= CProjectHelper.createCProject(projectName, projectType);
		fHeaderFile = fCProject.getProject().getFile(getHeaderFileName());
		String fileName = getFileName();
		fCFile = fCProject.getProject().getFile(fileName);
		if ( (!fCFile.exists()) &&( !fHeaderFile.exists() )) {
			try{
				FileInputStream headerFileIn = new FileInputStream(
						CTestPlugin.getDefault().getFileInPlugin(new Path(getHeaderFileFullPath()))); 
				fHeaderFile.create(headerFileIn,false, monitor);  
				FileInputStream bodyFileIn = new FileInputStream(
						CTestPlugin.getDefault().getFileInPlugin(new Path(getFileFullPath()))); 
				fCFile.create(bodyFileIn,false, monitor);        
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		if (!fCProject.getProject().hasNature(CCProjectNature.CC_NATURE_ID)) {
			addNatureToProject(fCProject.getProject(), CCProjectNature.CC_NATURE_ID, null);
		}

		//TEMPORARY: Disable type cache
		TypeCacheManager typeCacheManager = TypeCacheManager.getInstance();
		typeCacheManager.setProcessTypeCacheEvents(false);
		
		// use the new indexer
		//IndexManager indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();
	}

	private static void addNatureToProject(IProject proj, String natureId, IProgressMonitor monitor) throws CoreException {
		IProjectDescription description = proj.getDescription();
		String[] prevNatures= description.getNatureIds();
		String[] newNatures= new String[prevNatures.length + 1];
		System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
		newNatures[prevNatures.length]= natureId;
		description.setNatureIds(newNatures);
		proj.setDescription(description, monitor);
	}
	
	protected void tearDown() {
		CProjectHelper.delete(fCProject);
	}	
	
	public void testCompletionProposals() throws Exception {
			// setup the translation unit, the buffer and the document
			//ITranslationUnit header = (ITranslationUnit)CoreModel.getDefault().create(fHeaderFile);
			ITranslationUnit tu = (ITranslationUnit)CoreModel.getDefault().create(fCFile);
			buffer = tu.getBuffer().getContents();
//			document = new Document(buffer);
			
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			FileEditorInput editorInput = new FileEditorInput(fCFile);
			IEditorPart editorPart = page.openEditor(editorInput, "org.eclipse.cdt.ui.editor.CEditor");
			
			CEditor editor = (CEditor) editorPart ;
			IAction completionAction = editor.getAction("ContentAssistProposal");
			
			CCompletionProcessor2 completionProcessor = new CCompletionProcessor2(editorPart);
//			IWorkingCopy wc = null;
//			try{
//				wc = tu.getWorkingCopy();
//			}catch (CModelException e){
//				fail("Failed to get working copy"); //$NON-NLS-1$
//			}
		
			// call the CompletionProcessor
//			ICompletionProposal[] results = completionProcessor.evalProposals(document, pos, wc, null);
			ICompletionProposal[] results = completionProcessor.computeCompletionProposals(editor.getCSourceViewer(), getCompletionPosition()); // (document, pos, wc, null);
			assertTrue(results != null);
			if (CTestPlugin.getDefault().isDebugging())  {
				for (int i = 0; i < results.length; i++) {
					ICompletionProposal proposal = results[i];
					System.out.println("Result: " + proposal.getDisplayString());
				}
			}
			
			// check the completion node
			ASTCompletionNode currentCompletionNode = completionProcessor.getCurrentCompletionNode();
			IASTName[] names = currentCompletionNode.getNames();
			IASTName currentName = names[0];
//			IASTCompletionNode completionNode = (IASTCompletionNode) currentCompletionNode ;
//			assertNotNull(completionNode);
			assertNotNull(currentCompletionNode);
			// scope
//			IASTScope scope = completionNode.getCompletionScope();
//			assertNotNull(scope);
//			assertTrue(scope.getClass().getName().endsWith(getExpectedScopeClassName()));
//			// context
//			IASTNode context = completionNode.getCompletionContext();
//			if(context == null)
//				assertTrue(getExpectedContextClassName().equals("null")); //$NON-NLS-1$
//			else
//				assertTrue(context.getClass().getName().endsWith(getExpectedContextClassName()));
//			// kind
//			IASTCompletionNode.CompletionKind kind = completionNode.getCompletionKind();
//			assertTrue(kind == getExpectedKind());
			// prefix
//			String prefix = completionNode.getCompletionPrefix();
			String prefix = currentCompletionNode.getPrefix();
			assertEquals(prefix, getExpectedPrefix());
			
//			assertEquals( completionNode.getFunctionName(), getFunctionOrConstructorName() );
			
			String[] expected = getExpectedResultsValues();

			boolean allFound = true ;  // for the time being, let's be optimistic
			
			for (int i = 0; i< expected.length; i++){
				boolean found = false;
				for(int j = 0; j< results.length; j++){
					ICompletionProposal proposal = results[j];
					String displayString = proposal.getDisplayString();
					if(expected[i].equals(displayString)){
						found = true;
						if (CTestPlugin.getDefault().isDebugging())  {
							System.out.println("Lookup success for " + expected[i]);
						}
						break;
					}
				}
				if (!found)  {
					allFound = false ;
					if (CTestPlugin.getDefault().isDebugging())  {
						System.out.println( "Lookup failed for " + expected[i]); //$NON-NLS-1$
					}
				}
			}	
			assertTrue("Not enough results!", results.length >= expected.length);
			assertTrue("Some expected results were not found!", allFound);
			
	}	
	
	/**
	 * @return Returns the buffer.
	 */
	public String getBuffer() {
		return buffer;
	}

	/**
	 * @return Returns the document.
	 */
	public Document getDocument() {
		return document;
	}

	/**
	 * @return Returns the tu.
	 */
	public ITranslationUnit getTranslationUnit() {
		return tu;
	}

}
