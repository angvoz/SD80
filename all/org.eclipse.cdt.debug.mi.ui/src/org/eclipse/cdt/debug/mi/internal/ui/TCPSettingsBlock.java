/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.internal.ui;

import java.util.Observable;

import org.eclipse.cdt.debug.mi.core.IGDBServerMILaunchConfigurationConstants;
import org.eclipse.cdt.debug.mi.internal.ui.dialogfields.DialogField;
import org.eclipse.cdt.debug.mi.internal.ui.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.debug.mi.internal.ui.dialogfields.LayoutUtil;
import org.eclipse.cdt.debug.mi.internal.ui.dialogfields.StringDialogField;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Enter type comment.
 * 
 * @since Nov 20, 2003
 */
public class TCPSettingsBlock extends Observable
{
	private final static String DEFAULT_HOST_NAME = "localhost";
	private final static String DEFAULT_PORT_NUMBER = "10000";

	private Shell fShell;

	private StringDialogField fHostNameField;
	private StringDialogField fPortNumberField;
	
	private Control fControl;

	private String fErrorMessage = null;
	
	public TCPSettingsBlock()
	{
		super();
		fHostNameField = createHostNameField();
		fPortNumberField = createPortNumberField();
	}

	public void createBlock( Composite parent )
	{
		fShell = parent.getShell();
		Composite comp = ControlFactory.createCompositeEx( parent, 2, GridData.FILL_BOTH );
		((GridLayout)comp.getLayout()).makeColumnsEqualWidth = false;
		((GridLayout)comp.getLayout()).marginHeight = 0; 
		((GridLayout)comp.getLayout()).marginWidth = 0; 
		comp.setFont( JFaceResources.getDialogFont() );

		PixelConverter converter = new PixelConverter( comp );
		
		fHostNameField.doFillIntoGrid( comp, 2 );
		LayoutUtil.setWidthHint( fHostNameField.getTextControl( null ), converter.convertWidthInCharsToPixels( 20 ) );
		fPortNumberField.doFillIntoGrid( comp, 2 );
		((GridData)fPortNumberField.getTextControl( null ).getLayoutData()).horizontalAlignment = GridData.BEGINNING;
		LayoutUtil.setWidthHint( fPortNumberField.getTextControl( null ), converter.convertWidthInCharsToPixels( 10 ) );
		
		setControl( comp );
	}

	protected Shell getShell()
	{
		return fShell;
	}

	public void dispose()
	{
		deleteObservers();
	}

	public void initializeFrom( ILaunchConfiguration configuration )
	{
		initializeHostName( configuration );
		initializePortNumber( configuration );
	}

	public void setDefaults( ILaunchConfigurationWorkingCopy configuration )
	{
		configuration.setAttribute( IGDBServerMILaunchConfigurationConstants.ATTR_HOST, DEFAULT_HOST_NAME );
		configuration.setAttribute( IGDBServerMILaunchConfigurationConstants.ATTR_PORT, DEFAULT_PORT_NUMBER );
	}

	public void performApply( ILaunchConfigurationWorkingCopy configuration )
	{
		if ( fHostNameField != null )
			configuration.setAttribute( IGDBServerMILaunchConfigurationConstants.ATTR_HOST, fHostNameField.getText().trim() );
		if ( fPortNumberField != null )
			configuration.setAttribute( IGDBServerMILaunchConfigurationConstants.ATTR_PORT, fPortNumberField.getText().trim() );
	}

	private StringDialogField createHostNameField()
	{
		StringDialogField field = new StringDialogField();
		field.setLabelText( "Host name or IP address: " );
		field.setDialogFieldListener( 
						new IDialogFieldListener()
							{
								public void dialogFieldChanged( DialogField field )
								{
									hostNameFieldChanged();
								}
							} );
		return field; 
	}

	private StringDialogField createPortNumberField()
	{ 
		StringDialogField field = new StringDialogField();
		field.setLabelText( "Port number: " );
		field.setDialogFieldListener( 
						new IDialogFieldListener()
							{
								public void dialogFieldChanged( DialogField field )
								{
									portNumberFieldChanged();
								}
							} );
		return field; 
	}

	protected void hostNameFieldChanged()
	{
		updateErrorMessage();
		setChanged();
		notifyObservers();
	}

	protected void portNumberFieldChanged()
	{
		updateErrorMessage();
		setChanged();
		notifyObservers();
	}

	private void initializeHostName( ILaunchConfiguration configuration )
	{
		if ( fHostNameField != null )
		{
			try
			{
				fHostNameField.setText( configuration.getAttribute( IGDBServerMILaunchConfigurationConstants.ATTR_HOST, DEFAULT_HOST_NAME ) );
			}
			catch( CoreException e )
			{
			}
		}
	}

	private void initializePortNumber( ILaunchConfiguration configuration )
	{
		if ( fPortNumberField != null )
		{
			try
			{
				fPortNumberField.setText( configuration.getAttribute( IGDBServerMILaunchConfigurationConstants.ATTR_PORT, DEFAULT_PORT_NUMBER ) );
			}
			catch( CoreException e )
			{
			}
		}
	}

	public Control getControl()
	{
		return fControl;
	}

	protected void setControl( Control control )
	{
		fControl = control;
	}

	public boolean isValid( ILaunchConfiguration configuration )
	{
		updateErrorMessage();
		return ( getErrorMessage() == null );
	}

	private void updateErrorMessage()
	{
		setErrorMessage( null );
		if ( fHostNameField != null && fPortNumberField != null )
		{
			if ( fHostNameField.getText().trim().length() == 0 )
				setErrorMessage( "Host name or IP address must be specified." );
			else if ( !hostNameIsValid( fHostNameField.getText().trim() ) )
				setErrorMessage( "Invalid host name or IP address." );
			else if ( fPortNumberField.getText().trim().length() == 0 )
				setErrorMessage( "Port number must be specified." );
			else if ( !portNumberIsValid( fPortNumberField.getText().trim() ) )
				setErrorMessage( "Invalid port number." );
		}
	}

	public String getErrorMessage()
	{
		return fErrorMessage;
	}

	private void setErrorMessage( String string )
	{
		fErrorMessage = string;
	}

	private boolean hostNameIsValid( String hostName )
	{
		return true;
	}

	private boolean portNumberIsValid( String portNumber )
	{
		try
		{
			int port = Short.parseShort( portNumber );
			if ( port < 0 )
				return false;
		}
		catch( NumberFormatException e )
		{
			return false;
		}
		return true;
	}
}
