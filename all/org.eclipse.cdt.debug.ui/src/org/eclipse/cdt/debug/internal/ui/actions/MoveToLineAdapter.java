/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.model.IMoveToAddress;
import org.eclipse.cdt.debug.core.model.IMoveToLine;
import org.eclipse.cdt.debug.internal.core.ICDebugInternalConstants;
import org.eclipse.cdt.debug.internal.core.model.CDebugElement;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLookupDirector;
import org.eclipse.cdt.debug.internal.ui.CDebugUIUtils;
import org.eclipse.cdt.debug.internal.ui.IInternalCDebugUIConstants;
import org.eclipse.cdt.debug.internal.ui.views.disassembly.DisassemblyEditorInput;
import org.eclipse.cdt.debug.internal.ui.views.disassembly.DisassemblyView;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Move to line target adapter for the CDI debugger
 */
public class MoveToLineAdapter implements IMoveToLineTarget {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.IMoveToLineTarget#moveToLine(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection, org.eclipse.debug.core.model.ISuspendResume)
	 */
	public void moveToLine( IWorkbenchPart part, ISelection selection, ISuspendResume target ) throws CoreException {
		String errorMessage = null;
		if ( part instanceof ITextEditor ) {
			ITextEditor textEditor = (ITextEditor)part;
			IEditorInput input = textEditor.getEditorInput();
			if ( input == null ) {
				errorMessage = ActionMessages.getString( "MoveToLineAdapter.0" ); //$NON-NLS-1$
			}
			else {
				IDocument document = textEditor.getDocumentProvider().getDocument( input );
				if ( document == null ) {
					errorMessage = ActionMessages.getString( "MoveToLineAdapter.1" ); //$NON-NLS-1$
				}
				else {
					final String fileName = getFileName( input );
					IDebugTarget debugTarget = null;
					if (target instanceof CDebugElement) { // should always be, but just in case
						debugTarget = ((CDebugElement)target).getDebugTarget();
					}
					if (debugTarget != null) {
						ITextSelection textSelection = (ITextSelection)selection;
						final int lineNumber = textSelection.getStartLine() + 1;
						if ( target instanceof IAdaptable ) {
							final IPath path = convertPath( fileName, debugTarget );
							final IMoveToLine moveToLine = (IMoveToLine)((IAdaptable)target).getAdapter( IMoveToLine.class );
							if ( moveToLine != null && moveToLine.canMoveToLine( path.toPortableString(), lineNumber ) ) {
								Runnable r = new Runnable() {
									public void run() {
										try {
											moveToLine.moveToLine(path.toPortableString(), lineNumber );
										}
										catch( DebugException e ) {
											failed( e );
										}
									}
								};
								runInBackground( r );
							}
						}
						return;
					}
				}
			}
		}
		else if ( part instanceof DisassemblyView ) {
			IEditorInput input = ((DisassemblyView)part).getInput();
			if ( !(input instanceof DisassemblyEditorInput) ) {
				errorMessage = ActionMessages.getString( "MoveToLineAdapter.2" ); //$NON-NLS-1$
			}
			else {
				ITextSelection textSelection = (ITextSelection)selection;
				int lineNumber = textSelection.getStartLine() + 1;
				final IAddress address = ((DisassemblyEditorInput)input).getAddress( lineNumber );
				if ( address != null && target instanceof IAdaptable ) {
					final IMoveToAddress moveToAddress = (IMoveToAddress)((IAdaptable)target).getAdapter( IMoveToAddress.class );
					if ( moveToAddress != null && moveToAddress.canMoveToAddress( address ) ) {
						Runnable r = new Runnable() {

							public void run() {
								try {
									moveToAddress.moveToAddress( address );
								}
								catch( DebugException e ) {
									failed( e );
								}
							}
						};
						runInBackground( r );
					}
				}
				return;
			}
		}
		else {
			errorMessage = ActionMessages.getString( "MoveToLineAdapter.3" ); //$NON-NLS-1$
		}
		throw new CoreException( new Status( IStatus.ERROR, CDebugUIPlugin.getUniqueIdentifier(), IInternalCDebugUIConstants.INTERNAL_ERROR, errorMessage, null ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.IMoveToLineTarget#canMoveToLine(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection, org.eclipse.debug.core.model.ISuspendResume)
	 */
	public boolean canMoveToLine( IWorkbenchPart part, ISelection selection, ISuspendResume target ) {
		if ( target instanceof IAdaptable ) {
			if ( part instanceof IEditorPart ) {
				IMoveToLine moveToLine = (IMoveToLine)((IAdaptable)target).getAdapter( IMoveToLine.class );
				if ( moveToLine == null)
					return false;
				IEditorPart editorPart = (IEditorPart)part;
				IEditorInput input = editorPart.getEditorInput();
				if ( input == null ) {
					return false;
				}
				if ( !(editorPart instanceof ITextEditor) ) {
					return false;
				}
				ITextEditor textEditor = (ITextEditor)editorPart;
				IDocument document = textEditor.getDocumentProvider().getDocument( input );
				if ( document == null ) {
					return false;
				}
				String fileName = null;
				try {
					fileName = getFileName( input );
				}
				catch( CoreException e ) {
				}
				if (fileName == null) {
					return false;
				}
				
				IDebugTarget debugTarget = null;
				if (target instanceof CDebugElement) { // should always be, but just in case
					debugTarget = ((CDebugElement)target).getDebugTarget();
				}
				if (debugTarget == null) {
					return false;
				}
				
				final IPath path = convertPath( fileName, debugTarget );				
				ITextSelection textSelection = (ITextSelection)selection;
				int lineNumber = textSelection.getStartLine() + 1;
				return moveToLine.canMoveToLine(path.toPortableString(), lineNumber );
			}
			if ( part instanceof DisassemblyView ) {
				IMoveToAddress moveToAddress = (IMoveToAddress)((IAdaptable)target).getAdapter( IMoveToAddress.class );
				if ( moveToAddress == null )
					return false;
				IEditorInput input = ((DisassemblyView)part).getInput();
				if ( !(input instanceof DisassemblyEditorInput) ) {
					return false;
				}
				ITextSelection textSelection = (ITextSelection)selection;
				int lineNumber = textSelection.getStartLine() + 1;
				IAddress address = ((DisassemblyEditorInput)input).getAddress( lineNumber );
				return moveToAddress.canMoveToAddress( address );
			}
		}
		return false;
	}

	private String getFileName( IEditorInput input ) throws CoreException {
		return CDebugUIUtils.getEditorFilePath(input);		
	}

	private void runInBackground( Runnable r ) {
		DebugPlugin.getDefault().asyncExec( r );
	}

	protected void failed( Throwable e ) {
		MultiStatus ms = new MultiStatus( CDIDebugModel.getPluginIdentifier(), ICDebugInternalConstants.STATUS_CODE_ERROR, ActionMessages.getString( "MoveToLineAdapter.4" ), null ); //$NON-NLS-1$
		ms.add( new Status( IStatus.ERROR, CDIDebugModel.getPluginIdentifier(), ICDebugInternalConstants.STATUS_CODE_ERROR, e.getMessage(), e ) );
		CDebugUtils.error( ms, this );
	}
	private IPath convertPath( String sourceHandle, IDebugTarget debugTarget ) {
		IPath path = null;
		if ( Path.EMPTY.isValidPath( sourceHandle ) ) {
			if ( debugTarget != null ) {
				ISourceLocator sl = debugTarget.getLaunch().getSourceLocator();
				if ( sl instanceof CSourceLookupDirector ) {
					path = ((CSourceLookupDirector)sl).getCompilationPath( sourceHandle );
				}
			}
			if ( path == null ) {
				path = new Path( sourceHandle );
			}
		}
		return path;
	}
}
