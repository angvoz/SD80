/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.core.model;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.CDebugModel;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.model.ICDebugElementErrorStatus;
import org.eclipse.cdt.debug.internal.core.ICDebugInternalConstants;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;

/**
 * 
 * Enter type comment.
 * 
 * @since Aug 1, 2002
 */
public class CDebugElement extends PlatformObject 
						   implements IDebugElement,
						   			  ICDebugElementErrorStatus
{
	private CDebugTarget fDebugTarget;

	private int fSeverity = ICDebugElementErrorStatus.OK;
	private String fMessage = null;

	/**
	 * Constructor for CDebugElement.
	 */
	public CDebugElement( CDebugTarget target )
	{
		setDebugTarget( target );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugElement#getModelIdentifier()
	 */
	public String getModelIdentifier()
	{
		return CDebugModel.getPluginIdentifier();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugElement#getDebugTarget()
	 */
	public IDebugTarget getDebugTarget()
	{
		return fDebugTarget;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugElement#getLaunch()
	 */
	public ILaunch getLaunch()
	{
		return getDebugTarget().getLaunch();
	}

	protected void setDebugTarget( CDebugTarget target )
	{
		fDebugTarget = target;
	}

	/**
	 * Logs the given exception.
	 * 
	 * @param e the internal exception
	 */
	public void internalError( Exception e )
	{
		logError( e );
	}

	/**
	 * Logs the given message.
	 * 
	 * @param message the message
	 */
	public void internalError( String message )
	{
		logError( message );
	}

	/**
	 * Convenience method to log errors
	 * 
	 */
	protected void logError( Exception e ) 
	{
		CDebugCorePlugin.log( e );
	}

	/**
	 * Convenience method to log errors
	 * 
	 */
	protected void logError( String message ) 
	{
		CDebugCorePlugin.log( message );
	}

	/**
	 * Fires a debug event
	 * 
	 * @param event The debug event to be fired to the listeners
	 * @see org.eclipse.debug.core.DebugEvent
	 */
	protected void fireEvent(DebugEvent event)
	{
		DebugPlugin.getDefault().fireDebugEventSet( new DebugEvent[] { event } );
	}

	/**
	 * Fires a debug event marking the creation of this element.
	 */
	public void fireCreationEvent()
	{
		fireEvent( new DebugEvent( this, DebugEvent.CREATE ) );
	}

	/**
	 * Fires a debug event marking the RESUME of this element with
	 * the associated detail.
	 * 
	 * @param detail The int detail of the event
	 * @see org.eclipse.debug.core.DebugEvent
	 */
	public void fireResumeEvent( int detail )
	{
		fireEvent( new DebugEvent( this, DebugEvent.RESUME, detail ) );
	}

	/**
	 * Fires a debug event marking the SUSPEND of this element with
	 * the associated detail.
	 * 
	 * @param detail The int detail of the event
	 * @see org.eclipse.debug.core.DebugEvent
	 */
	public void fireSuspendEvent( int detail )
	{
		fireEvent( new DebugEvent( this, DebugEvent.SUSPEND, detail ) );
	}

	/**
	 * Fires a debug event marking the termination of this element.
	 */
	public void fireTerminateEvent()
	{
		fireEvent( new DebugEvent( this, DebugEvent.TERMINATE ) );
	}

	/**
	 * Fires a debug event marking the CHANGE of this element
	 * with the specifed detail code.
	 * 
	 * @param detail one of <code>STATE</code> or <code>CONTENT</code>
	 */
	public void fireChangeEvent( int detail )
	{
		fireEvent( new DebugEvent( this, DebugEvent.CHANGE, detail ) );
	}

	/**
	 * Returns the CDI session associated with this element.
	 * 
	 * @return the CDI session
	 */
	public ICDISession getCDISession() 
	{
		return getCDITarget().getSession();
	}

	/**
	 * Returns the underlying CDI target associated with this element.
	 * 
	 * @return the underlying CDI target
	 */
	public ICDITarget getCDITarget() 
	{
		return (ICDITarget)getDebugTarget().getAdapter( ICDITarget.class );
	}

	/**
	 * Throws a new debug exception with a status code of <code>REQUEST_FAILED</code>.
	 * 
	 * @param message Failure message
	 * @param e Exception that has occurred (<code>can be null</code>)
	 * @throws DebugException The exception with a status code of <code>REQUEST_FAILED</code>
	 */
	public void requestFailed( String message, Exception e ) throws DebugException
	{
		requestFailed( message, e, DebugException.REQUEST_FAILED );
	}
	
	/**
	 * Throws a new debug exception with a status code of <code>TARGET_REQUEST_FAILED</code>
	 * with the given underlying exception. If the underlying exception is not a JDI
	 * exception, the original exception is thrown.
	 * 
	 * @param message Failure message
	 * @param e underlying exception that has occurred
	 * @throws DebugException The exception with a status code of <code>TARGET_REQUEST_FAILED</code>
	 */
	public void targetRequestFailed( String message, CDIException e ) throws DebugException
	{
		requestFailed( "Target request failed: " + message, e, DebugException.TARGET_REQUEST_FAILED );
	}

	/**
	 * Throws a new debug exception with the given status code.
	 * 
	 * @param message Failure message
	 * @param e Exception that has occurred (<code>can be null</code>)
	 * @param code status code
	 * @throws DebugException a new exception with given status code
	 */
	public void requestFailed( String message, Throwable e, int code ) throws DebugException
	{
		throwDebugException( message, code, e );
	}
		
	/**
	 * Throws a new debug exception with a status code of <code>TARGET_REQUEST_FAILED</code>.
	 * 
	 * @param message Failure message
	 * @param e Throwable that has occurred
	 * @throws DebugException The exception with a status code of <code>TARGET_REQUEST_FAILED</code>
	 */
	public void targetRequestFailed( String message, Throwable e ) throws DebugException
	{
		throwDebugException( "Target request failed: " + message, DebugException.TARGET_REQUEST_FAILED, e );
	}
	
	/**
	 * Throws a new debug exception with a status code of <code>NOT_SUPPORTED</code>.
	 * 
	 * @param message Failure message
	 * @throws DebugException The exception with a status code of <code>NOT_SUPPORTED</code>.
	 */
	public void notSupported(String message) throws DebugException {
		throwDebugException(message, DebugException.NOT_SUPPORTED, null);
	}
	
	/**
	 * Throws a debug exception with the given message, error code, and underlying
	 * exception.
	 */
	protected void throwDebugException( String message, int code, Throwable exception ) throws DebugException 
	{
		throw new DebugException( new Status( IStatus.ERROR, 
											  CDebugModel.getPluginIdentifier(),
											  code, 
											  message, 
											  exception ) );
	}
	
	protected void infoMessage( Throwable e )
	{
		IStatus newStatus = new Status( IStatus.INFO, 
										CDebugCorePlugin.getUniqueIdentifier(), 
										ICDebugInternalConstants.STATUS_CODE_INFO, 
										e.getMessage(),
										null );
		CDebugUtils.info( newStatus, getDebugTarget() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter( Class adapter )
	{
		if ( adapter.equals( ICDISession.class ) )
			return getCDISession(); 
		return super.getAdapter(adapter);
	}

	protected void setStatus( int severity, String message )
	{
		fSeverity = severity;
		fMessage = message;
		if ( fMessage != null )
			fMessage.trim();
	}

	protected void resetStatus()
	{
		fSeverity = ICDebugElementErrorStatus.OK;
		fMessage = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICDebugElementErrorStatus#isOK()
	 */
	public boolean isOK()
	{
		return ( fSeverity == ICDebugElementErrorStatus.OK );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICDebugElementErrorStatus#getSeverity()
	 */
	public int getSeverity()
	{
		return fSeverity;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICDebugElementErrorStatus#getMessage()
	 */
	public String getMessage()
	{
		return fMessage;
	}
}
