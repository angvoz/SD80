/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Ed Swartz (Nokia)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.search.actions;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.widgets.Display;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.model.util.CElementBaseLabels;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable;
import org.eclipse.cdt.internal.core.model.ext.CElementHandleFactory;
import org.eclipse.cdt.internal.core.model.ext.ICElementHandle;

import org.eclipse.cdt.internal.ui.actions.OpenActionUtil;
import org.eclipse.cdt.internal.ui.editor.ASTProvider;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.CEditorMessages;
import org.eclipse.cdt.internal.ui.viewsupport.IndexUI;

public class OpenDeclarationsAction extends SelectionParseAction {
	public static boolean sIsJUnitTest = false;	
	public static final IASTName[] BLANK_NAME_ARRAY = new IASTName[0];
	ITextSelection selNode;

	/**
	 * Creates a new action with the given editor
	 */
	public OpenDeclarationsAction(CEditor editor) {
		super( editor );
		setText(CEditorMessages.getString("OpenDeclarations.label")); //$NON-NLS-1$
		setToolTipText(CEditorMessages.getString("OpenDeclarations.tooltip")); //$NON-NLS-1$
		setDescription(CEditorMessages.getString("OpenDeclarations.description")); //$NON-NLS-1$
	}

	private class Runner extends Job implements ASTRunnable {
		private IWorkingCopy fWorkingCopy;
		private IIndex fIndex;

		Runner() {
			super(CEditorMessages.getString("OpenDeclarations.dialog.title")); //$NON-NLS-1$
		}

		protected IStatus run(IProgressMonitor monitor) {
			try {
				clearStatusLine();
				
				fWorkingCopy = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(fEditor.getEditorInput());
				if (fWorkingCopy == null)
					return Status.CANCEL_STATUS;

				fIndex= CCorePlugin.getIndexManager().getIndex(fWorkingCopy.getCProject(),
						IIndexManager.ADD_DEPENDENCIES | IIndexManager.ADD_DEPENDENT);
				
				try {
					fIndex.acquireReadLock();
				} catch (InterruptedException e) {
					return Status.CANCEL_STATUS;
				}

				try {
					return ASTProvider.getASTProvider().runOnAST(fWorkingCopy, ASTProvider.WAIT_YES, monitor, this);
				} finally {
					fIndex.releaseReadLock();
				}
			} catch (CoreException e) {
				return e.getStatus();
			}
		}

		public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) throws CoreException {
			if (ast == null) {
				return Status.OK_STATUS;
			}
			int selectionStart = selNode.getOffset();
			int selectionLength = selNode.getLength();
				
			IASTName[] selectedNames = lang.getSelectedNames(ast, selectionStart, selectionLength);
			 
			if (selectedNames.length > 0 && selectedNames[0] != null) { // just right, only one name selected
				boolean found = false;
				IASTName searchName = selectedNames[0];
				final IASTNode parent = searchName.getParent();
				if (parent instanceof IASTPreprocessorIncludeStatement) {
					openInclude(((IASTPreprocessorIncludeStatement) parent));
					return Status.OK_STATUS;
				}
				boolean isDefinition= searchName.isDefinition();
				IBinding binding = searchName.resolveBinding();
				if (binding != null && !(binding instanceof IProblemBinding)) {
					IName[] declNames = findNames(fIndex, ast, isDefinition, binding);
					if (declNames.length == 0) {
						if(binding instanceof ICPPTemplateInstance) {
							// bug 207320, handle template instances
							IBinding definition= ((ICPPTemplateInstance)binding).getTemplateDefinition();
							if(definition != null && !(definition instanceof IProblemBinding)) {
								declNames = findNames(fIndex, ast, true, definition);
							}
						} else if (binding instanceof ICPPMethod) {
							// bug 86829, handle implicit methods.
							ICPPMethod method= (ICPPMethod) binding;
							if (method.isImplicit()) {
								try {
									IBinding clsBinding= method.getClassOwner();
									if (clsBinding != null && !(clsBinding instanceof IProblemBinding)) {
										declNames= findNames(fIndex, ast, false, clsBinding);
									}
								} catch (DOMException e) {
									// don't log problem bindings.
								}
							}
						}
					}
					if (navigateViaCElements(fWorkingCopy.getCProject(), fIndex, declNames)) {
						found= true;
					}
					else {
						// leave old method as fallback for local variables, parameters and 
						// everything else not covered by ICElementHandle.
						found = navigateOneLocation(declNames);
					}
				}
				if (!found) {
					reportSymbolLookupFailure(new String(searchName.toCharArray()));
				}
				return Status.OK_STATUS;
			} 
			
			// Check if we're in an include statement
			IASTPreprocessorStatement[] preprocs = ast.getAllPreprocessorStatements();
			for (int i = 0; i < preprocs.length; ++i) {
				if (!(preprocs[i] instanceof IASTPreprocessorIncludeStatement))
					continue;
				IASTPreprocessorIncludeStatement incStmt = (IASTPreprocessorIncludeStatement)preprocs[i];
				IASTFileLocation loc = preprocs[i].getFileLocation();
				if (loc != null
						&& loc.getFileName().equals(ast.getFilePath())
						&& loc.getNodeOffset() < selectionStart
						&& loc.getNodeOffset() + loc.getNodeLength() > selectionStart) {
					// Got it
					openInclude(incStmt);
					return Status.OK_STATUS;
				}
			}
			reportSelectionMatchFailure();
			return Status.OK_STATUS; 
		}

		private void openInclude(IASTPreprocessorIncludeStatement incStmt) {
			String name = null;
			if (incStmt.isResolved())
				name = incStmt.getPath();
			
			if (name != null) {
				final IPath path = new Path(name);
				runInUIThread(new Runnable() {
					public void run() {
						try {
							open(path, 0, 0);
						} catch (CoreException e) {
							CUIPlugin.getDefault().log(e);
						}
					}
				});
			} else {
				reportIncludeLookupFailure(new String(incStmt.getName().toCharArray()));
			}
		}

		private boolean navigateOneLocation(IName[] declNames) {
			for (int i = 0; i < declNames.length; i++) {
				IASTFileLocation fileloc = declNames[i].getFileLocation();
				if (fileloc != null) {

					final IPath path = new Path(fileloc.getFileName());
					final int offset = fileloc.getNodeOffset();
					final int length = fileloc.getNodeLength();

					runInUIThread(new Runnable() {
						public void run() {
							try {
								open(path, offset, length);
							} catch (CoreException e) {
								CUIPlugin.getDefault().log(e);
							}
						}
					});
					return true;
				}
			}
			return false;
		}

		private boolean navigateViaCElements(ICProject project, IIndex index, IName[] declNames) {
			final ArrayList elements= new ArrayList();
			for (int i = 0; i < declNames.length; i++) {
				try {
					ICElement elem = getCElementForName(project, index, declNames[i]);
					if (elem instanceof ISourceReference) {
						elements.add(elem);
					}
				} catch (CoreException e) {
					CUIPlugin.getDefault().log(e);
				}
			}
			if (elements.isEmpty()) {
				return false;
			}
			
			runInUIThread(new Runnable() {
				public void run() {
					ISourceReference target= null;
					if (elements.size() == 1) {
						target= (ISourceReference) elements.get(0);
					}
					else {
						if (sIsJUnitTest) {
							throw new RuntimeException("ambiguous input"); //$NON-NLS-1$
						}
						ICElement[] elemArray= (ICElement[]) elements.toArray(new ICElement[elements.size()]);
						target = (ISourceReference) OpenActionUtil.selectCElement(elemArray, getSite().getShell(),
								CEditorMessages.getString("OpenDeclarationsAction.dialog.title"), CEditorMessages.getString("OpenDeclarationsAction.selectMessage"), //$NON-NLS-1$ //$NON-NLS-2$
								CElementBaseLabels.ALL_DEFAULT | CElementBaseLabels.MF_POST_FILE_QUALIFIED, 0);
					}
					if (target != null) {
						ITranslationUnit tu= target.getTranslationUnit();
						ISourceRange sourceRange;
						try {
							sourceRange = target.getSourceRange();
							if (tu != null && sourceRange != null) {
								open(tu.getLocation(), sourceRange.getIdStartPos(), sourceRange.getIdLength());
							}
						} catch (CoreException e) {
							CUIPlugin.getDefault().log(e);
						}
					}
				}
			});
			return true;
		}

		private ICElementHandle getCElementForName(ICProject project, IIndex index, IName declName) 
				throws CoreException {
			if (declName instanceof IIndexName) {
				return IndexUI.getCElementForName(project, index, (IIndexName) declName);
			}
			if (declName instanceof IASTName) {
				IASTName astName = (IASTName) declName;
				IBinding binding= astName.resolveBinding();
				if (binding != null) {
					ITranslationUnit tu= IndexUI.getTranslationUnit(project, astName);
					if (tu != null) {
						IASTFileLocation loc= astName.getFileLocation();
						IRegion region= new Region(loc.getNodeOffset(), loc.getNodeLength());
						return CElementHandleFactory.create(tu, binding, astName.isDefinition(), region, 0);
					}
				}
				return null;
			}
			return null;
		}

		private IName[] findNames(IIndex index, IASTTranslationUnit ast,
				boolean isDefinition, IBinding binding) throws CoreException {
			IName[] declNames= isDefinition ?
					findDeclarations(index, ast, binding) :
					findDefinitions(index, ast, binding);
			
			if (declNames.length == 0) {
				declNames= isDefinition ?
						findDefinitions(index, ast, binding) :
						findDeclarations(index, ast, binding);
			}
			return declNames;
		}

		private IName[] findDefinitions(IIndex index, IASTTranslationUnit ast,
				IBinding binding) throws CoreException {
			IName[] declNames= ast.getDefinitionsInAST(binding);
			if (declNames.length == 0) {
					// 2. Try definition in index
				declNames = index.findDefinitions(binding);
			}
			return declNames;
		}

		private IName[] findDeclarations(IIndex index, IASTTranslationUnit ast,
				IBinding binding) throws CoreException {
			IName[] declNames= ast.getDeclarationsInAST(binding);
			for (int i = 0; i < declNames.length; i++) {
				IName name = declNames[i];
				if (name.isDefinition()) 
					declNames[i]= null;
			}
			declNames= (IName[]) ArrayUtil.removeNulls(IName.class, declNames);
			if (declNames.length == 0) {
				declNames= index.findNames(binding, IIndex.FIND_DECLARATIONS);
			}
			return declNames;
		}
	}

	public void run() {
		selNode = getSelectedStringFromEditor();
		if (selNode != null) {
			new Runner().schedule();
		}
	}

	private void runInUIThread(Runnable runnable) {
		if (Display.getCurrent() != null) {
			runnable.run();
		}
		else {
			Display.getDefault().asyncExec(runnable);
		}
	}

	/**
	 * For the purpose of regression testing.
	 * @since 4.0
	 */
	public void runSync() {
		selNode = getSelectedStringFromEditor();
		if (selNode != null) {
			new Runner().run(new NullProgressMonitor());
		}
	}
}

