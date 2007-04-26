/*******************************************************************************
 * Copyright (c) 2004, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Anton Leherbauer (Wind River Systems) - bug 183397
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import java.util.Iterator;

import org.eclipse.cdt.debug.internal.ui.views.disassembly.DisassemblyView;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.IUpdate;
import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;

/**
 * Abstract base implementation of the breakpoint ruler actions.
 * 
 * @see {@link RulerBreakpointAction} 
 */
public abstract class AbstractBreakpointRulerAction extends Action implements IUpdate {
	
	private final IWorkbenchPart fTargetPart;
	private final IVerticalRulerInfo fRulerInfo;
	
	/**
	 * Constructs an action to work on breakpoints in the specified
	 * part with the specified vertical ruler information.
	 * 
	 * @param part  a text editor or DisassemblyView
	 * @param info  vertical ruler information
	 */
	public AbstractBreakpointRulerAction(IWorkbenchPart part, IVerticalRulerInfo info) {
		Assert.isTrue(part instanceof ITextEditor || part instanceof DisassemblyView);
		fTargetPart = part;
		fRulerInfo = info;
	}

	/**
	 * Returns the breakpoint at the last line of mouse activity in the ruler
	 * or <code>null</code> if none.
	 * 
	 * @return breakpoint associated with activity in the ruler or <code>null</code>
	 */
	protected IBreakpoint getBreakpoint() {
		IAnnotationModel annotationModel = getAnnotationModel();
		IDocument document = getDocument();
		if (annotationModel != null) {
			Iterator iterator = annotationModel.getAnnotationIterator();
			while (iterator.hasNext()) {
				Object object = iterator.next();
				if (object instanceof SimpleMarkerAnnotation) {
					SimpleMarkerAnnotation markerAnnotation = (SimpleMarkerAnnotation) object;
					IMarker marker = markerAnnotation.getMarker();
					try {
						if (marker.isSubtypeOf(IBreakpoint.BREAKPOINT_MARKER)) {
							Position position = annotationModel.getPosition(markerAnnotation);
							int line = document.getLineOfOffset(position.getOffset());
							if (line == fRulerInfo.getLineOfLastMouseButtonActivity()) {
								IBreakpoint breakpoint = DebugPlugin.getDefault().getBreakpointManager().getBreakpoint(marker);
								if (breakpoint != null) {
									return breakpoint;
								}
							}
						}
					} catch (CoreException e) {
					} catch (BadLocationException e) {
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Returns the workbench part this action was created for.
	 * 
	 * @return workbench part, a text editor or a DisassemblyView
	 */
	protected IWorkbenchPart getTargetPart() {
		return fTargetPart;
	}
	
	/**
	 * Returns the vertical ruler information this action was created for.
	 * 
	 * @return vertical ruler information
	 */
	protected IVerticalRulerInfo getVerticalRulerInfo() {
		return fRulerInfo;
	}

	private IDocument getDocument() {
		IWorkbenchPart targetPart = getTargetPart();
		if ( targetPart instanceof ITextEditor ) {
			ITextEditor textEditor = (ITextEditor)targetPart; 
			IDocumentProvider provider = textEditor.getDocumentProvider();
			if ( provider != null )
				return provider.getDocument( textEditor.getEditorInput() );
		}
		else if ( targetPart instanceof DisassemblyView ) {
			DisassemblyView dv = (DisassemblyView)targetPart;
			IDocumentProvider provider = dv.getDocumentProvider();
			if ( provider != null )
				return provider.getDocument( dv.getInput() );
		}
		return null;
	}

	private IAnnotationModel getAnnotationModel() {
		IWorkbenchPart targetPart = getTargetPart();
		if ( targetPart instanceof ITextEditor ) {
			ITextEditor textEditor = (ITextEditor)targetPart; 
			IDocumentProvider provider = textEditor.getDocumentProvider();
			if ( provider != null )
				return provider.getAnnotationModel( textEditor.getEditorInput() );
		}
		else if ( targetPart instanceof DisassemblyView ) {
			DisassemblyView dv = (DisassemblyView)targetPart;
			IDocumentProvider provider = dv.getDocumentProvider();
			if ( provider != null )
				return provider.getAnnotationModel( dv.getInput() );
		}
		return null;
	}
}
