/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.debug.internal.core;

import java.util.HashMap;
import java.util.Set;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.event.ICDIChangedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDICreatedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIDestroyedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocationBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint;
import org.eclipse.cdt.debug.core.model.ICAddressBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.cdt.debug.internal.core.breakpoints.CBreakpoint;
import org.eclipse.cdt.debug.internal.core.model.CDebugTarget;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ISourceLocator;

/**
 * The breakpoint manager manages all breakpoints set to the associated 
 * debug target.
 */
public class CBreakpointManager implements ICDIEventListener, IAdaptable {

	private class BreakpointMap {

		/**
		 * Maps CBreakpoints to CDI breakpoints.
		 */
		private HashMap fCBreakpoints;

		/**
		 * Maps CDI breakpoints to CBreakpoints.
		 */
		private HashMap fCDIBreakpoints;

		protected BreakpointMap() {
			fCBreakpoints = new HashMap( 10 );
			fCDIBreakpoints = new HashMap( 10 );
		}

		protected synchronized void put( ICBreakpoint breakpoint, ICDIBreakpoint cdiBreakpoint ) {
			fCBreakpoints.put( breakpoint, cdiBreakpoint );
			fCDIBreakpoints.put( cdiBreakpoint, breakpoint );
		}

		protected synchronized ICDIBreakpoint getCDIBreakpoint( ICBreakpoint breakpoint ) {
			return (ICDIBreakpoint)fCBreakpoints.get( breakpoint );
		}

		protected synchronized ICBreakpoint getCBreakpoint( ICDIBreakpoint cdiBreakpoint ) {
			return (ICBreakpoint)fCDIBreakpoints.get( cdiBreakpoint );
		}

		protected void removeCBreakpoint( ICBreakpoint breakpoint ) {
			if ( breakpoint != null ) {
				ICDIBreakpoint cdiBreakpoint = (ICDIBreakpoint)fCBreakpoints.remove( breakpoint );
				if ( cdiBreakpoint != null )
					fCDIBreakpoints.remove( cdiBreakpoint );
			}
		}

		protected void removeCDIBreakpoint( ICBreakpoint breakpoin, ICDIBreakpoint cdiBreakpoint ) {
			if ( cdiBreakpoint != null ) {
				ICBreakpoint breakpoint = (ICBreakpoint)fCDIBreakpoints.remove( cdiBreakpoint );
				if ( breakpoint != null )
					fCBreakpoints.remove( breakpoint );
			}
		}

		protected ICBreakpoint[] getAllCBreakpoints() {
			Set set = fCBreakpoints.keySet();
			return (ICBreakpoint[])set.toArray( new ICBreakpoint[set.size()] );
		}

		protected ICDIBreakpoint[] getAllCDIBreakpoints() {
			Set set = fCDIBreakpoints.keySet();
			return (ICDIBreakpoint[])set.toArray( new ICDIBreakpoint[set.size()] );
		}

		protected void dispose() {
			fCBreakpoints.clear();
			fCDIBreakpoints.clear();
		}
	}

	private CDebugTarget fDebugTarget;

	private BreakpointMap fMap;

	public CBreakpointManager( CDebugTarget target ) {
		super();
		setDebugTarget( target );
		fMap = new BreakpointMap();
		getDebugTarget().getCDISession().getEventManager().addEventListener( this );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter( Class adapter ) {
		if ( CBreakpointManager.class.equals( adapter ) )
			return this;
		if ( CDebugTarget.class.equals( adapter ) )
			return getDebugTarget();
		if ( ICDebugTarget.class.equals( adapter ) )
			return getDebugTarget();
		if ( IDebugTarget.class.equals( adapter ) )
			return getDebugTarget();
		return null;
	}

	public CDebugTarget getDebugTarget() {
		return fDebugTarget;
	}

	private void setDebugTarget( CDebugTarget target ) {
		fDebugTarget = target;
	}

	protected ICDIBreakpointManager getCDIBreakpointManager() {
		return getDebugTarget().getCDISession().getBreakpointManager();
	}

	protected ICSourceLocator getCSourceLocator() {
		ISourceLocator locator = getDebugTarget().getLaunch().getSourceLocator();
		if ( locator instanceof IAdaptable )
			return (ICSourceLocator)((IAdaptable)locator).getAdapter( ICSourceLocator.class );
		return null;
	}

	public void dispose() {
		getDebugTarget().getCDISession().getEventManager().removeEventListener( this );
		removeAllBreakpoints();
		getBreakpointMap().dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener#handleDebugEvents(org.eclipse.cdt.debug.core.cdi.event.ICDIEvent)
	 */
	public void handleDebugEvents( ICDIEvent[] events ) {
		for( int i = 0; i < events.length; i++ ) {
			ICDIEvent event = events[i];
			ICDIObject source = event.getSource();
			if ( source != null && source.getTarget().equals( getDebugTarget().getCDITarget() ) ) {
				if ( event instanceof ICDICreatedEvent ) {
					if ( source instanceof ICDIBreakpoint )
						handleBreakpointCreatedEvent( (ICDIBreakpoint)source );
				}
				else if ( event instanceof ICDIDestroyedEvent ) {
					if ( source instanceof ICDIBreakpoint )
						handleBreakpointDestroyedEvent( (ICDIBreakpoint)source );
				}
				else if ( event instanceof ICDIChangedEvent ) {
					if ( source instanceof ICDIBreakpoint )
						handleBreakpointChangedEvent( (ICDIBreakpoint)source );
				}
			}
		}
	}

	public boolean isTargetBreakpoint( ICBreakpoint breakpoint ) {
		IResource resource = breakpoint.getMarker().getResource();
		if ( breakpoint instanceof ICAddressBreakpoint )
			return supportsAddressBreakpoint( (ICAddressBreakpoint)breakpoint );
		if ( breakpoint instanceof ICLineBreakpoint ) {
			ICSourceLocator sl = getSourceLocator();
			if ( sl != null )
				return sl.contains( resource );
		}
		else {
			IProject project = resource.getProject();
			if ( project != null && project.exists() ) {
				ICSourceLocator sl = getSourceLocator();
				if ( sl != null )
					return sl.contains( project );
				else {
					if ( project.equals( getExecFile().getProject() ) )
						return true;
					return CDebugUtils.isReferencedProject( getExecFile().getProject(), project );
				}
			}
		}
		return true;
	}

	public boolean isCDIRegistered( ICBreakpoint breakpoint ) {
		return (getBreakpointMap().getCDIBreakpoint( breakpoint ) != null);
	}

	public boolean supportsAddressBreakpoint( ICAddressBreakpoint breakpoint ) {
		return (getExecFile() != null && getExecFile().getLocation().toOSString().equals( breakpoint.getMarker().getResource().getLocation().toOSString() ));
	}

	public IFile getCDIBreakpointFile( ICDIBreakpoint cdiBreakpoint ) {
		IBreakpoint breakpoint = getBreakpointMap().getCBreakpoint( cdiBreakpoint );
		if ( breakpoint instanceof ICLineBreakpoint && !(breakpoint instanceof ICAddressBreakpoint) ) {
			IResource resource = ((ICLineBreakpoint)breakpoint).getMarker().getResource();
			if ( resource instanceof IFile )
				return (IFile)resource;
		}
		return null;
	}

	public ICBreakpoint getBreakpoint( ICDIBreakpoint cdiBreakpoint ) {
		return getBreakpointMap().getCBreakpoint( cdiBreakpoint );
	}

	public long getBreakpointAddress( ICBreakpoint breakpoint ) {
		if ( breakpoint != null ) {
			ICDIBreakpoint cdiBreakpoint = getBreakpointMap().getCDIBreakpoint( breakpoint );
			if ( cdiBreakpoint instanceof ICDILocationBreakpoint ) {
				try {
					ICDILocation location = ((ICDILocationBreakpoint)cdiBreakpoint).getLocation();
					if ( location != null )
						return location.getAddress();
				}
				catch( CDIException e ) {
				}
			}
		}
		return 0;
	}

	public void setBreakpoint( final ICBreakpoint breakpoint ) throws DebugException {
		Runnable runnable = new Runnable() {

			public void run() {
				try {
					doSetBreakpoint( breakpoint );
				}
				catch( DebugException e ) {
				}
			}
		};
		CDebugCorePlugin.getDefault().asyncExec( runnable );
	}

	protected void doSetBreakpoint( ICBreakpoint breakpoint ) throws DebugException {
		try {
			ICDIBreakpoint cdiBreakpoint = getBreakpointMap().getCDIBreakpoint( breakpoint );
			if ( cdiBreakpoint == null ) {
				if ( breakpoint instanceof ICFunctionBreakpoint )
					cdiBreakpoint = setFunctionBreakpoint( (ICFunctionBreakpoint)breakpoint );
				else if ( breakpoint instanceof ICAddressBreakpoint )
					cdiBreakpoint = setAddressBreakpoint( (ICAddressBreakpoint)breakpoint );
				else if ( breakpoint instanceof ICLineBreakpoint )
					cdiBreakpoint = setLineBreakpoint( (ICLineBreakpoint)breakpoint );
				else if ( breakpoint instanceof ICWatchpoint )
					cdiBreakpoint = setWatchpoint( (ICWatchpoint)breakpoint );
			}
			if ( cdiBreakpoint == null )
				return;
			if ( !breakpoint.isEnabled() )
				cdiBreakpoint.setEnabled( false );
			setBreakpointCondition( breakpoint );
		}
		catch( CoreException e ) {
			requestFailed( CDebugCorePlugin.getResourceString( "internal.core.CBreakpointManager.Set_breakpoint_failed" ) + e.getMessage(), e ); //$NON-NLS-1$
		}
		catch( NumberFormatException e ) {
			requestFailed( CDebugCorePlugin.getResourceString( "internal.core.CBreakpointManager.Set_breakpoint_failed" ) + e.getMessage(), e ); //$NON-NLS-1$
		}
		catch( CDIException e ) {
			targetRequestFailed( CDebugCorePlugin.getResourceString( "internal.core.CBreakpointManager.Set_breakpoint_failed" ) + e.getMessage(), e ); //$NON-NLS-1$
		}
	}

	public void removeBreakpoint( final ICBreakpoint breakpoint ) throws DebugException {
		Runnable runnable = new Runnable() {
			public void run() {
				try {
					doRemoveBreakpoint( breakpoint );
				}
				catch( DebugException e ) {
				}
			}
		};
		CDebugCorePlugin.getDefault().asyncExec( runnable );
	}

	protected void doRemoveBreakpoint( ICBreakpoint breakpoint ) throws DebugException {
		ICDIBreakpoint cdiBreakpoint = getBreakpointMap().getCDIBreakpoint( breakpoint );
		if ( cdiBreakpoint != null ) {
			ICDIBreakpointManager bm = getCDIBreakpointManager();
			try {
				bm.deleteBreakpoints( new ICDIBreakpoint[]{ cdiBreakpoint } );
			}
			catch( CDIException e ) {
				targetRequestFailed( CDebugCorePlugin.getResourceString( "internal.core.CBreakpointManager.Delete_breakpoint_failed" ) + e.getMessage(), e ); //$NON-NLS-1$
			}
		}
	}

	public void changeBreakpointProperties( final ICBreakpoint breakpoint, final IMarkerDelta delta ) throws DebugException {
		Runnable runnable = new Runnable() {

			public void run() {
				try {
					doChangeBreakpointProperties( breakpoint, delta );
				}
				catch( DebugException e ) {
				}
			}
		};
		CDebugCorePlugin.getDefault().asyncExec( runnable );
	}

	protected void doChangeBreakpointProperties( ICBreakpoint breakpoint, IMarkerDelta delta ) throws DebugException {
		ICDIBreakpoint cdiBreakpoint = getBreakpointMap().getCDIBreakpoint( breakpoint );
		if ( cdiBreakpoint == null )
			return;
		ICDIBreakpointManager bm = getCDIBreakpointManager();
		try {
			boolean enabled = breakpoint.isEnabled();
			boolean oldEnabled = delta.getAttribute( IBreakpoint.ENABLED, true );
			int ignoreCount = breakpoint.getIgnoreCount();
			int oldIgnoreCount = delta.getAttribute( ICBreakpoint.IGNORE_COUNT, 0 );
			String condition = breakpoint.getCondition();
			String oldCondition = delta.getAttribute( ICBreakpoint.CONDITION, "" ); //$NON-NLS-1$
			if ( enabled != oldEnabled ) {
				cdiBreakpoint.setEnabled( enabled );
			}
			if ( ignoreCount != oldIgnoreCount || !condition.equals( oldCondition ) ) {
				ICDICondition cdiCondition = bm.createCondition( ignoreCount, condition );
				cdiBreakpoint.setCondition( cdiCondition );
			}
		}
		catch( CoreException e ) {
			requestFailed( CDebugCorePlugin.getResourceString( "internal.core.CBreakpointManager.Change_brkpt_properties_failed" ) + e.getMessage(), e ); //$NON-NLS-1$
		}
		catch( CDIException e ) {
			targetRequestFailed( CDebugCorePlugin.getResourceString( "internal.core.CBreakpointManager.Change_brkpt_properties_failed" ) + e.getMessage(), e ); //$NON-NLS-1$
		}
	}

	private void handleBreakpointCreatedEvent( final ICDIBreakpoint cdiBreakpoint ) {
		Runnable runnable = new Runnable() {

			public void run() {
				if ( cdiBreakpoint instanceof ICDILocationBreakpoint )
					doHandleLocationBreakpointCreatedEvent( (ICDILocationBreakpoint)cdiBreakpoint );
				else if ( cdiBreakpoint instanceof ICDIWatchpoint )
					doHandleWatchpointCreatedEvent( (ICDIWatchpoint)cdiBreakpoint );
			}
		};
		CDebugCorePlugin.getDefault().asyncExec( runnable );
	}

	protected void doHandleLocationBreakpointCreatedEvent( ICDILocationBreakpoint cdiBreakpoint ) {
		if ( cdiBreakpoint.isTemporary() )
			return;
		ICBreakpoint breakpoint = getBreakpointMap().getCBreakpoint( cdiBreakpoint );
		if ( breakpoint == null ) {
			try {
				if ( cdiBreakpoint.getLocation().getFile() != null && cdiBreakpoint.getLocation().getFile().length() > 0 ) {
					ICSourceLocator locator = getSourceLocator();
					if ( locator != null ) {
						Object sourceElement = locator.findSourceElement( cdiBreakpoint.getLocation().getFile() );
						if ( sourceElement != null && sourceElement instanceof IFile ) {
							breakpoint = createLineBreakpoint( (IFile)sourceElement, cdiBreakpoint );
						}
						else if ( cdiBreakpoint.getLocation().getAddress() > 0 ) {
							breakpoint = createAddressBreakpoint( cdiBreakpoint );
						}
					}
				}
				else if ( cdiBreakpoint.getLocation().getAddress() > 0 ) {
					breakpoint = createAddressBreakpoint( cdiBreakpoint );
				}
			}
			catch( CDIException e ) {
			}
			catch( CoreException e ) {
			}
		}
		if ( breakpoint != null ) {
			try {
				((CBreakpoint)breakpoint).incrementInstallCount();
			}
			catch( CoreException e ) {
				CDebugCorePlugin.log( e.getStatus() );
			}
		}
	}

	protected void doHandleWatchpointCreatedEvent( ICDIWatchpoint cdiWatchpoint ) {
		ICBreakpoint breakpoint = getBreakpointMap().getCBreakpoint( cdiWatchpoint );
		if ( breakpoint == null ) {
			try {
				breakpoint = createWatchpoint( cdiWatchpoint );
			}
			catch( CDIException e ) {
			}
			catch( CoreException e ) {
			}
		}
		if ( breakpoint != null ) {
			try {
				((CBreakpoint)breakpoint).incrementInstallCount();
			}
			catch( CoreException e ) {
				CDebugCorePlugin.log( e.getStatus() );
			}
		}
	}

	private void handleBreakpointDestroyedEvent( final ICDIBreakpoint cdiBreakpoint ) {
		Runnable runnable = new Runnable() {

			public void run() {
				doHandleBreakpointDestroyedEvent( cdiBreakpoint );
			}
		};
		CDebugCorePlugin.getDefault().asyncExec( runnable );
	}

	protected void doHandleBreakpointDestroyedEvent( ICDIBreakpoint cdiBreakpoint ) {
		ICBreakpoint breakpoint = getBreakpointMap().getCBreakpoint( cdiBreakpoint );
		if ( breakpoint != null ) {
			getBreakpointMap().removeCDIBreakpoint( breakpoint, cdiBreakpoint );
			try {
				((CBreakpoint)breakpoint).decrementInstallCount();
			}
			catch( CoreException e ) {
				CDebugCorePlugin.log( e.getStatus() );
			}
		}
	}

	private void handleBreakpointChangedEvent( final ICDIBreakpoint cdiBreakpoint ) {
		Runnable runnable = new Runnable() {

			public void run() {
				doHandleBreakpointChangedEvent( cdiBreakpoint );
			}
		};
		CDebugCorePlugin.getDefault().asyncExec( runnable );
	}

	protected void doHandleBreakpointChangedEvent( ICDIBreakpoint cdiBreakpoint ) {
		ICBreakpoint breakpoint = getBreakpointMap().getCBreakpoint( cdiBreakpoint );
		if ( breakpoint != null ) {
			try {
				breakpoint.setEnabled( cdiBreakpoint.isEnabled() );
				breakpoint.setIgnoreCount( cdiBreakpoint.getCondition().getIgnoreCount() );
				breakpoint.setCondition( cdiBreakpoint.getCondition().getExpression() );
			}
			catch( CDIException e ) {
			}
			catch( CoreException e ) {
			}
		}
	}

	private void removeAllBreakpoints() {
		ICDIBreakpoint[] cdiBreakpoints = getBreakpointMap().getAllCDIBreakpoints();
		ICDIBreakpointManager bm = getCDIBreakpointManager();
		if ( cdiBreakpoints.length > 0 ) {
			try {
				bm.deleteBreakpoints( cdiBreakpoints );
			}
			catch( CDIException e ) {
				CDebugCorePlugin.log( e.getMessage() );
			}
			ICBreakpoint[] breakpoints = getBreakpointMap().getAllCBreakpoints();
			for( int i = 0; i < breakpoints.length; ++i ) {
				try {
					((CBreakpoint)breakpoints[i]).decrementInstallCount();
				}
				catch( CoreException e ) {
					CDebugCorePlugin.log( e.getMessage() );
				}
			}
		}
	}

	private synchronized ICDIBreakpoint setFunctionBreakpoint( ICFunctionBreakpoint breakpoint ) throws CDIException, CoreException {
		ICDIBreakpointManager bm = getCDIBreakpointManager();
		String function = breakpoint.getFunction();
		String fileName = (function != null && function.indexOf( "::" ) == -1) ? breakpoint.getFileName() : null; //$NON-NLS-1$
		ICDILocation location = bm.createLocation( fileName, function, -1 );
		ICDIBreakpoint cdiBreakpoint = bm.setLocationBreakpoint( ICDIBreakpoint.REGULAR, location, null, null, true );
		getBreakpointMap().put( breakpoint, cdiBreakpoint );
		return cdiBreakpoint;
	}

	private synchronized ICDIBreakpoint setAddressBreakpoint( ICAddressBreakpoint breakpoint ) throws CDIException, CoreException, NumberFormatException {
		ICDIBreakpointManager bm = getCDIBreakpointManager();
		ICDILocation location = bm.createLocation( Long.parseLong( breakpoint.getAddress() ) );
		ICDIBreakpoint cdiBreakpoint = bm.setLocationBreakpoint( ICDIBreakpoint.REGULAR, location, null, null, true );
		getBreakpointMap().put( breakpoint, cdiBreakpoint );
		return cdiBreakpoint;
	}

	private synchronized ICDIBreakpoint setLineBreakpoint( ICLineBreakpoint breakpoint ) throws CDIException, CoreException {
		ICDIBreakpointManager bm = getCDIBreakpointManager();
		ICDILocation location = bm.createLocation( breakpoint.getMarker().getResource().getLocation().lastSegment(), null, breakpoint.getLineNumber() );
		ICDIBreakpoint cdiBreakpoint = bm.setLocationBreakpoint( ICDIBreakpoint.REGULAR, location, null, null, true );
		getBreakpointMap().put( breakpoint, cdiBreakpoint );
		return cdiBreakpoint;
	}

	private synchronized ICDIBreakpoint setWatchpoint( ICWatchpoint watchpoint ) throws CDIException, CoreException {
		ICDIBreakpointManager bm = getCDIBreakpointManager();
		int accessType = 0;
		accessType |= (watchpoint.isWriteType()) ? ICDIWatchpoint.WRITE : 0;
		accessType |= (watchpoint.isReadType()) ? ICDIWatchpoint.READ : 0;
		String expression = watchpoint.getExpression();
		ICDIWatchpoint cdiWatchpoint = bm.setWatchpoint( ICDIBreakpoint.REGULAR, accessType, expression, null );
		getBreakpointMap().put( watchpoint, cdiWatchpoint );
		return cdiWatchpoint;
	}

	private void setBreakpointCondition( ICBreakpoint breakpoint ) throws CoreException, CDIException {
		ICDIBreakpoint cdiBreakpoint = getBreakpointMap().getCDIBreakpoint( breakpoint );
		ICDIBreakpointManager bm = getCDIBreakpointManager();
		ICDICondition condition = bm.createCondition( breakpoint.getIgnoreCount(), breakpoint.getCondition() );
		cdiBreakpoint.setCondition( condition );
	}

	private BreakpointMap getBreakpointMap() {
		return fMap;
	}

	protected void targetRequestFailed( String message, Throwable e ) throws DebugException {
		requestFailed0( message, e, DebugException.TARGET_REQUEST_FAILED );
	}

	protected void requestFailed( String message, Throwable e ) throws DebugException {
		requestFailed0( message, e, DebugException.REQUEST_FAILED );
	}

	private void requestFailed0( String message, Throwable e, int code ) throws DebugException {
		throw new DebugException( new Status( IStatus.ERROR, CDIDebugModel.getPluginIdentifier(), code, message, e ) );
	}

	private ICLineBreakpoint createLineBreakpoint( IFile file, ICDILocationBreakpoint cdiBreakpoint ) throws CDIException, CoreException {
		ICLineBreakpoint breakpoint = CDIDebugModel.createLineBreakpoint( cdiBreakpoint.getLocation().getFile(), 
																		  file, 
																		  cdiBreakpoint.getLocation().getLineNumber(), 
																		  cdiBreakpoint.isEnabled(), 
																		  cdiBreakpoint.getCondition().getIgnoreCount(), 
																		  cdiBreakpoint.getCondition().getExpression(), 
																		  false );
		getBreakpointMap().put( breakpoint, cdiBreakpoint );
		((CBreakpoint)breakpoint).register( true );
		return breakpoint;
	}

	private ICAddressBreakpoint createAddressBreakpoint( ICDILocationBreakpoint cdiBreakpoint ) throws CDIException, CoreException {
		IFile execFile = getExecFile();
		String sourceHandle = execFile.getFullPath().toOSString();
		ICAddressBreakpoint breakpoint = CDIDebugModel.createAddressBreakpoint( sourceHandle, 
																				execFile, 
																				cdiBreakpoint.getLocation().getAddress(), 
																				cdiBreakpoint.isEnabled(), 
																				cdiBreakpoint.getCondition().getIgnoreCount(), 
																				cdiBreakpoint.getCondition().getExpression(), 
																				false );
		getBreakpointMap().put( breakpoint, cdiBreakpoint );
		((CBreakpoint)breakpoint).register( true );
		return breakpoint;
	}

	private ICWatchpoint createWatchpoint( ICDIWatchpoint cdiWatchpoint ) throws CDIException, CoreException {
		IFile execFile = getExecFile();
		String sourceHandle = execFile.getFullPath().toOSString();
		ICWatchpoint watchpoint = CDIDebugModel.createWatchpoint( sourceHandle, 
																  execFile.getProject(), 
																  cdiWatchpoint.isWriteType(), 
																  cdiWatchpoint.isReadType(), 
																  cdiWatchpoint.getWatchExpression(), 
																  cdiWatchpoint.isEnabled(), 
																  cdiWatchpoint.getCondition().getIgnoreCount(), 
																  cdiWatchpoint.getCondition().getExpression(), 
																  false );
		getBreakpointMap().put( watchpoint, cdiWatchpoint );
		((CBreakpoint)watchpoint).register( true );
		return watchpoint;
	}

	private ICSourceLocator getSourceLocator() {
		ISourceLocator locator = getDebugTarget().getLaunch().getSourceLocator();
		return (locator instanceof IAdaptable) ? (ICSourceLocator)((IAdaptable)locator).getAdapter( ICSourceLocator.class ) : null;
	}

	private IFile getExecFile() {
		return getDebugTarget().getExecFile();
	}
}