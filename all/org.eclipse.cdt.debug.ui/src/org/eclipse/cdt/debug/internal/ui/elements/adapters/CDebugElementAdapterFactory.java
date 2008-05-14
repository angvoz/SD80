/*******************************************************************************
 * Copyright (c) 2004, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * IBM Corporation
 * ARM Limited - https://bugs.eclipse.org/bugs/show_bug.cgi?id=186981
 * Wind River Systems - adapted to work with platform Modules view (bug 210558)
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.elements.adapters; 

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.debug.core.disassembly.IDisassemblyContextProvider;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.core.model.ICModule;
import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.cdt.debug.core.model.ICThread;
import org.eclipse.cdt.debug.core.model.IDisassemblyLine;
import org.eclipse.cdt.debug.core.model.IModuleRetrieval;
import org.eclipse.cdt.debug.internal.core.CDisassemblyContextProvider;
import org.eclipse.cdt.debug.internal.core.model.DisassemblyRetrieval;
import org.eclipse.cdt.debug.internal.ui.sourcelookup.SourceDisplayAdapter;
import org.eclipse.cdt.debug.internal.ui.views.modules.ModuleContentProvider;
import org.eclipse.cdt.debug.internal.ui.views.modules.ModuleMementoProvider;
import org.eclipse.cdt.debug.ui.disassembly.IDocumentElementAnnotationProvider;
import org.eclipse.cdt.debug.ui.disassembly.IDocumentElementContentProvider;
import org.eclipse.cdt.debug.ui.disassembly.IDocumentElementLabelProvider;
import org.eclipse.cdt.debug.ui.disassembly.IElementToggleBreakpointAdapter;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;
import org.eclipse.debug.ui.sourcelookup.ISourceDisplay;
 
public class CDebugElementAdapterFactory implements IAdapterFactory {

    private static IElementContentProvider fgDebugTargetContentProvider = new CDebugTargetContentProvider();
    private static IElementContentProvider fgThreadContentProvider = new CThreadContentProvider();
    private static IElementContentProvider fgStackFrameContentProvider = new CStackFrameContentProvider();
    private static IElementContentProvider fgModuleContentProvider = new ModuleContentProvider();

    private static IModelProxyFactory fgDebugElementProxyFactory = new CDebugElementProxyFactory();
    
    private static IElementMementoProvider fgStackFrameMementoProvider = new CStackFrameMementoProvider();
    private static IElementMementoProvider fgModuleMementoProvider = new ModuleMementoProvider();

    private static IDisassemblyContextProvider fgDisassemblyContextProvider = new CDisassemblyContextProvider();
    private static IDocumentElementContentProvider fgDisassemblyContentProvider = new DisassemblyElementContentProvider();
    private static IDocumentElementLabelProvider fgDisassemblyLabelProvider = new DisassemblyElementLabelProvider();
    private static IDocumentElementAnnotationProvider fgDisassemblyAnnotationProvider = new DisassemblyElementAnnotationProvider();
    private static IElementToggleBreakpointAdapter fgDisassemblyToggleBreakpointAdapter = new DisassemblyToggleBreakpointAdapter();
    private static ISourceDisplay fgSourceDisplayAdapter = new SourceDisplayAdapter();
    
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	public Object getAdapter( Object adaptableObject, Class adapterType ) {
	    if ( adapterType.isInstance( adaptableObject ) ) {
			return adaptableObject;
		}
		if ( adapterType.equals( IElementContentProvider.class ) ) {
			if ( adaptableObject instanceof ICDebugTarget ) {
				return fgDebugTargetContentProvider;
			}
            if ( adaptableObject instanceof ICThread ) {
                return fgThreadContentProvider;
            }
            if ( adaptableObject instanceof ICStackFrame ) {
                return fgStackFrameContentProvider;
            }
			if ( adaptableObject instanceof ICModule || 
			     adaptableObject instanceof ICElement ) {
				return fgModuleContentProvider;
			}
		}
		if ( adapterType.equals( IModelProxyFactory.class ) ) {
			if ( adaptableObject instanceof ICDebugTarget ) {
				return fgDebugElementProxyFactory;
			}
            if ( adaptableObject instanceof ICThread ) {
                return fgDebugElementProxyFactory;
            }
			if ( adaptableObject instanceof ICStackFrame ) {
			    return fgDebugElementProxyFactory;
			}
			if ( adaptableObject instanceof IModuleRetrieval ) {
                return fgDebugElementProxyFactory;
            }
            if ( adaptableObject instanceof DisassemblyRetrieval ) {
                return fgDebugElementProxyFactory;
            }
		}
        if ( adapterType.equals( IElementMementoProvider.class ) ) {
            if ( adaptableObject instanceof ICStackFrame ) {
                return fgStackFrameMementoProvider;
            }
            if ( adaptableObject instanceof IModuleRetrieval ||
                adaptableObject instanceof ICThread ||
                 adaptableObject instanceof ICModule || 
                 adaptableObject instanceof ICElement) 
            {
                return fgModuleMementoProvider;
            }
        }
        if ( adapterType.equals( IDisassemblyContextProvider.class ) ) {
            if ( adaptableObject instanceof ICStackFrame ) {
                return fgDisassemblyContextProvider;
            }
        }
        if ( adapterType.equals( IDocumentElementContentProvider.class ) ) {
            if ( adaptableObject instanceof ICStackFrame ) {
                return fgDisassemblyContentProvider;
            }
        }
        if ( adapterType.equals( IDocumentElementLabelProvider.class ) ) {
            if ( adaptableObject instanceof IDisassemblyLine ) {
                return fgDisassemblyLabelProvider;
            }
        }
        if ( adapterType.equals( IDocumentElementAnnotationProvider.class ) ) {
            if ( adaptableObject instanceof IDisassemblyLine ) {
                return fgDisassemblyAnnotationProvider;
            }
        }
        if ( adapterType.equals( IElementToggleBreakpointAdapter.class ) ) {
            if ( adaptableObject instanceof IDisassemblyLine ) {
                return fgDisassemblyToggleBreakpointAdapter;
            }
        }
        if ( adapterType.equals( ISourceDisplay.class ) ) {
            if ( adaptableObject instanceof ICStackFrame ) {
                return fgSourceDisplayAdapter;
            }
        }
    	return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return new Class[] {
				IElementContentProvider.class,
				IModelProxyFactory.class,
        		IElementMementoProvider.class,
        		IDisassemblyContextProvider.class,
        		IDocumentElementContentProvider.class,
                IDocumentElementLabelProvider.class,
                IDocumentElementAnnotationProvider.class,
                IElementToggleBreakpointAdapter.class,
                ISourceDisplay.class,
			};
	}
}
