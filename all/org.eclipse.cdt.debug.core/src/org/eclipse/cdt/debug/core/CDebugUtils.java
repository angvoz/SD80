/*******************************************************************************
 * Copyright (c) 2000, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Freescale Semiconductor - Address watchpoints, https://bugs.eclipse.org/bugs/show_bug.cgi?id=118299
 *******************************************************************************/
package org.eclipse.cdt.debug.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import com.ibm.icu.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.model.ICAddressBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpointType;
import org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICValue;
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
import org.eclipse.cdt.debug.core.model.ICWatchpoint2;
import org.eclipse.cdt.debug.internal.core.model.CFloatingPointValue;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.IBreakpoint;
import org.w3c.dom.Document;

/**
 * Utility methods.
 */
public class CDebugUtils {

	public static boolean question( IStatus status, Object source ) {
		Boolean result = Boolean.FALSE;
		IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler( status );
		if ( handler != null ) {
			try {
				result = (Boolean)handler.handleStatus( status, source );
			}
			catch( CoreException e ) {
			}
		}
		return result.booleanValue();
	}

	public static void info( IStatus status, Object source ) {
		IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler( status );
		if ( handler != null ) {
			try {
				handler.handleStatus( status, source );
			}
			catch( CoreException e ) {
			}
		}
	}

	public static void error( IStatus status, Object source ) {
		IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler( status );
		if ( handler != null ) {
			try {
				handler.handleStatus( status, source );
			}
			catch( CoreException e ) {
			}
		}
	}

	public static char[] getByteText( byte b ) {
		return new char[]{ charFromByte( (byte)((b >>> 4) & 0x0f) ), charFromByte( (byte)(b & 0x0f) ) };
	}

	public static byte textToByte( char[] text ) {
		byte result = 0;
		if ( text.length == 2 ) {
			byte[] bytes = { charToByte( text[0] ), charToByte( text[1] ) };
			result = (byte)((bytes[0] << 4) + bytes[1]);
		}
		return result;
	}

	public static char charFromByte( byte value ) {
		if ( value >= 0x0 && value <= 0x9 )
			return (char)(value + '0');
		if ( value >= 0xa && value <= 0xf )
			return (char)(value - 0xa + 'a');
		return '0';
	}

	public static byte charToByte( char ch ) {
		if ( Character.isDigit( ch ) ) {
			return (byte)(ch - '0');
		}
		if ( ch >= 'a' && ch <= 'f' ) {
			return (byte)(0xa + ch - 'a');
		}
		if ( ch >= 'A' && ch <= 'F' ) {
			return (byte)(0xa + ch - 'A');
		}
		return 0;
	}

	public static char bytesToChar( byte[] bytes ) {
		try {
			return (char)Short.parseShort( new String( bytes ), 16 );
		}
		catch( RuntimeException e ) {
		}
		return 0;
	}

	public static byte toByte( char[] bytes, boolean le ) {
		if ( bytes.length != 2 )
			return 0;
		return (byte)Long.parseLong( bytesToString( bytes, le, true ), 16 );
	}

	public static short toUnsignedByte( char[] bytes, boolean le ) {
		if ( bytes.length != 2 )
			return 0;
		return (short)Long.parseLong( bytesToString( bytes, le, false ), 16 );
	}

	public static short toShort( char[] bytes, boolean le ) {
		if ( bytes.length != 4 )
			return 0;
		return (short)Long.parseLong( bytesToString( bytes, le, true ), 16 );
	}

	public static int toUnsignedShort( char[] bytes, boolean le ) {
		if ( bytes.length != 4 )
			return 0;
		return (int)Long.parseLong( bytesToString( bytes, le, false ), 16 );
	}

	public static int toInt( char[] bytes, boolean le ) {
		if ( bytes.length != 8 )
			return 0;
		return (int)Long.parseLong( bytesToString( bytes, le, true ), 16 );
	}

	public static long toUnsignedInt( char[] bytes, boolean le ) {
		if ( bytes.length != 8 )
			return 0;
		return Long.parseLong( bytesToString( bytes, le, false ), 16 );
	}

	private static String bytesToString( char[] bytes, boolean le, boolean signed ) {
		char[] copy = new char[bytes.length];
		if ( le ) {
			for( int i = 0; i < bytes.length / 2; ++i ) {
				copy[2 * i] = bytes[bytes.length - 2 * i - 2];
				copy[2 * i + 1] = bytes[bytes.length - 2 * i - 1];
			}
		}
		else {
			System.arraycopy( bytes, 0, copy, 0, copy.length );
		}
		return new String( copy );
	}

	public static String prependString( String text, int length, char ch ) {
		StringBuffer sb = new StringBuffer( length );
		if ( text.length() > length ) {
			sb.append( text.substring( 0, length ) );
		}
		else {
			char[] prefix = new char[length - text.length()];
			Arrays.fill( prefix, ch );
			sb.append( prefix );
			sb.append( text );
		}
		return sb.toString();
	}

	public static boolean isReferencedProject( IProject parent, IProject project ) {
		if ( parent != null && parent.exists() ) {
			List projects = CDebugUtils.getReferencedProjects( project );
			Iterator it = projects.iterator();
			while( it.hasNext() ) {
				IProject prj = (IProject)it.next();
				if ( prj.exists() && (prj.equals( project )) )
					return true;
			}
		}
		return false;
	}

	/**
	 * Serializes a XML document into a string - encoded in UTF8 format, with platform line separators.
	 * 
	 * @param doc document to serialize
	 * @param indent if the xml text should be indented.
	 * 
	 * @return the document as a string
	 */
	public static String serializeDocument( Document doc, boolean indent ) throws IOException, TransformerException {
		ByteArrayOutputStream s = new ByteArrayOutputStream();
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer();
		transformer.setOutputProperty( OutputKeys.METHOD, "xml" ); //$NON-NLS-1$
		transformer.setOutputProperty( OutputKeys.INDENT, indent ? "yes" : "no" ); //$NON-NLS-1$
		DOMSource source = new DOMSource( doc );
		StreamResult outputTarget = new StreamResult( s );
		transformer.transform( source, outputTarget );
		return s.toString( "UTF8" ); //$NON-NLS-1$			
	}

	/**
	 * Serializes a XML document into a string - encoded in UTF8 format, with platform line separators.
	 * 
	 * @param doc document to serialize
	 * @return the document as a string
	 */
	public static String serializeDocument( Document doc ) throws IOException, TransformerException {
		return serializeDocument(doc, true);
	}

	public static Number getFloatingPointValue( ICValue value ) {
		if ( value instanceof CFloatingPointValue ) {
			try {
				return ((CFloatingPointValue)value).getFloatingPointValue();
			}
			catch( CDIException e ) {
			}
		}
		return null;
	}

	public static boolean isNaN( Number value ) {
		if ( value instanceof Double ) {
			return ((Double)value).isNaN();
		}
		if ( value instanceof Float ) {
			return ((Float)value).isNaN();
		}
		return false;
	}

	public static boolean isPositiveInfinity( Number value ) {
		if ( value instanceof Double ) {
			return (((Double)value).isInfinite() && value.doubleValue() == Double.POSITIVE_INFINITY);
		}
		if ( value instanceof Float ) {
			return (((Float)value).isInfinite() && value.floatValue() == Float.POSITIVE_INFINITY);
		}
		return false;
	}

	public static boolean isNegativeInfinity( Number value ) {
		if ( value instanceof Double ) {
			return (((Double)value).isInfinite() && value.doubleValue() == Double.NEGATIVE_INFINITY);
		}
		if ( value instanceof Float ) {
			return (((Float)value).isInfinite() && value.floatValue() == Float.NEGATIVE_INFINITY);
		}
		return false;
	}

	public static List getReferencedProjects( IProject project ) {
		ArrayList list = new ArrayList( 10 );
		if ( project != null && project.exists() && project.isOpen() ) {
			IProject[] refs = new IProject[0];
			try {
				refs = project.getReferencedProjects();
			}
			catch( CoreException e ) {
			}
			for( int i = 0; i < refs.length; ++i ) {
				if ( !project.equals( refs[i] ) && refs[i] != null && refs[i].exists() && refs[i].isOpen() ) {
					list.add( refs[i] );
					getReferencedProjects( project, refs[i], list );
				}
			}
		}
		return list;
	}

	private static void getReferencedProjects( IProject root, IProject project, List list ) {
		if ( project != null && project.exists() && project.isOpen() ) {
			IProject[] refs = new IProject[0];
			try {
				refs = project.getReferencedProjects();
			}
			catch( CoreException e ) {
			}
			for( int i = 0; i < refs.length; ++i ) {
				if ( !list.contains( refs[i] ) && refs[i] != null && !refs[i].equals( root ) && refs[i].exists() && refs[i].isOpen() ) {
					list.add( refs[i] );
					getReferencedProjects( root, refs[i], list );
				}
			}
		}
	}

	public static String getBreakpointText( IBreakpoint breakpoint, boolean qualified ) throws CoreException {
		if ( breakpoint instanceof ICAddressBreakpoint ) {
			return getAddressBreakpointText( (ICAddressBreakpoint)breakpoint, qualified );
		}
		if ( breakpoint instanceof ICFunctionBreakpoint ) {
			return getFunctionBreakpointText( (ICFunctionBreakpoint)breakpoint, qualified );
		}
		if ( breakpoint instanceof ICLineBreakpoint ) {
			return getLineBreakpointText( (ICLineBreakpoint)breakpoint, qualified );
		}
		if ( breakpoint instanceof ICWatchpoint ) {
			return getWatchpointText( (ICWatchpoint)breakpoint, qualified );
		}
		// this allow to create new breakpoint without implemention one the interfaces above and still see a label
		Object message = breakpoint.getMarker().getAttribute(IMarker.MESSAGE);
		if (message!=null) return message.toString();
		return ""; //$NON-NLS-1$
	}

	protected static String getLineBreakpointText( ICLineBreakpoint breakpoint, boolean qualified ) throws CoreException {
		StringBuffer label = new StringBuffer();
		appendSourceName( breakpoint, label, qualified );
		appendLineNumber( breakpoint, label );
		appendBreakpointType( breakpoint, label );
		appendIgnoreCount( breakpoint, label );
		appendCondition( breakpoint, label );
		return label.toString();
	}

	protected static String getWatchpointText( ICWatchpoint watchpoint, boolean qualified ) throws CoreException {
		StringBuffer label = new StringBuffer();
		appendSourceName( watchpoint, label, qualified );
		appendWatchExpression( watchpoint, label );
		if ( watchpoint instanceof ICWatchpoint2 ) {
			ICWatchpoint2 wp2 = (ICWatchpoint2)watchpoint;
			appendWatchMemorySpace( wp2, label );
			appendWatchRange( wp2, label );
		}
		appendIgnoreCount( watchpoint, label );
		appendCondition( watchpoint, label );
		return label.toString();
	}

	protected static String getAddressBreakpointText( ICAddressBreakpoint breakpoint, boolean qualified ) throws CoreException {
		StringBuffer label = new StringBuffer();
		appendSourceName( breakpoint, label, qualified );
		appendAddress( breakpoint, label );
		appendBreakpointType( breakpoint, label );
		appendIgnoreCount( breakpoint, label );
		appendCondition( breakpoint, label );
		return label.toString();
	}

	protected static String getFunctionBreakpointText( ICFunctionBreakpoint breakpoint, boolean qualified ) throws CoreException {
		StringBuffer label = new StringBuffer();
		appendSourceName( breakpoint, label, qualified );
		appendFunction( breakpoint, label );
		appendBreakpointType( breakpoint, label );
		appendIgnoreCount( breakpoint, label );
		appendCondition( breakpoint, label );
		return label.toString();
	}

	protected static StringBuffer appendSourceName( ICBreakpoint breakpoint, StringBuffer label, boolean qualified ) throws CoreException {
		String handle = breakpoint.getSourceHandle();
		if ( !isEmpty( handle ) ) {
			IPath path = new Path( handle );
			if ( path.isValidPath( handle ) ) {
				label.append( qualified ? path.toOSString() : path.lastSegment() );
			}
		}
		return label;
	}

	protected static StringBuffer appendLineNumber( ICLineBreakpoint breakpoint, StringBuffer label ) throws CoreException {
		int lineNumber = breakpoint.getLineNumber();
		if ( lineNumber > 0 ) {
			label.append( ' ' );
			label.append( MessageFormat.format( DebugCoreMessages.getString( "CDebugUtils.0" ), new String[]{ Integer.toString( lineNumber ) } ) ); //$NON-NLS-1$
		}
		return label;
	}

	protected static StringBuffer appendAddress( ICAddressBreakpoint breakpoint, StringBuffer label ) throws CoreException {
		try {
			label.append( ' ' );
			label.append( MessageFormat.format( DebugCoreMessages.getString( "CDebugUtils.1" ), new String[]{ breakpoint.getAddress() } ) ); //$NON-NLS-1$
		}
		catch( NumberFormatException e ) {
		}
		return label;
	}

	protected static StringBuffer appendFunction( ICFunctionBreakpoint breakpoint, StringBuffer label ) throws CoreException {
		String function = breakpoint.getFunction();
		if ( function != null && function.trim().length() > 0 ) {
			label.append( ' ' );
			label.append( MessageFormat.format( DebugCoreMessages.getString( "CDebugUtils.2" ), new String[]{ function.trim() } ) ); //$NON-NLS-1$
		}
		return label;
	}

	protected static StringBuffer appendIgnoreCount( ICBreakpoint breakpoint, StringBuffer label ) throws CoreException {
		int ignoreCount = breakpoint.getIgnoreCount();
		if ( ignoreCount > 0 ) {
			label.append( ' ' );
			label.append( MessageFormat.format( DebugCoreMessages.getString( "CDebugUtils.3" ), new String[]{ Integer.toString( ignoreCount ) } ) ); //$NON-NLS-1$
		}
		return label;
	}

	protected static void appendCondition( ICBreakpoint breakpoint, StringBuffer buffer ) throws CoreException {
		String condition = breakpoint.getCondition();
		if ( condition != null && condition.length() > 0 ) {
			buffer.append( ' ' );
			buffer.append( MessageFormat.format( DebugCoreMessages.getString( "CDebugUtils.4" ), new String[] { condition } ) ); //$NON-NLS-1$
		}
	}

	private static void appendWatchExpression( ICWatchpoint watchpoint, StringBuffer label ) throws CoreException {
		String expression = watchpoint.getExpression();
		if ( expression != null && expression.length() > 0 ) {
			label.append( ' ' );
			label.append( MessageFormat.format(  DebugCoreMessages.getString( "CDebugUtils.5" ), new String[] { expression } ) ); //$NON-NLS-1$
		}
	}

	private static void appendWatchMemorySpace( ICWatchpoint2 watchpoint, StringBuffer label ) throws CoreException {
		String memorySpace = watchpoint.getMemorySpace();
		if ( memorySpace != null && memorySpace.length() > 0 ) {
			label.append( ' ' );
			label.append( MessageFormat.format(  DebugCoreMessages.getString( "CDebugUtils.6" ), new String[] { memorySpace } ) ); //$NON-NLS-1$
		}
	}

	private static void appendWatchRange( ICWatchpoint2 watchpoint, StringBuffer label ) throws CoreException {
		String range = watchpoint.getRange().toString();
		if ( range.length() > 0 && !range.equals( "0" ) ) {
			label.append( ' ' );
			label.append( MessageFormat.format( DebugCoreMessages.getString( "CDebugUtils.7" ), new String[]{ range } ) ); //$NON-NLS-1$
		}
	}
	
	protected static StringBuffer appendBreakpointType( ICBreakpoint breakpoint, StringBuffer label ) throws CoreException {
		if (breakpoint instanceof ICBreakpointType) {
			String typeString = "";
			int type = ((ICBreakpointType) breakpoint).getType();

			// Need to filter out the TEMPORARY bit-flag to get the real type
			// The REGULAR type is implicit so we don't report that.
			switch (type & ~ICBreakpointType.TEMPORARY) {
			case ICBreakpointType.HARDWARE:
				typeString = DebugCoreMessages.getString("CDebugUtils.Hardware");
				break;
			case ICBreakpointType.SOFTWARE:
				typeString = DebugCoreMessages.getString("CDebugUtils.Software");
				break;
			}
			
			// Now factor in the TEMPORARY qualifier to form, .e.,g "Hardware/Temporary"
			// Thing is, a temporary breakpoint should never show in the GUI, so this is
			// here as a just-in-case.
			if ((type & ICBreakpointType.TEMPORARY) != 0) {
				if (typeString.length() > 0) {
					typeString += "/";	
				}
				typeString += DebugCoreMessages.getString("CDebugUtils.Temporary");
			}
			
			if (typeString.length() > 0) {
				label.append(' ');
				label.append(MessageFormat.format(
						DebugCoreMessages.getString("CDebugUtils.8"), new String[] { typeString })); //$NON-NLS-1$
			}
		}
		return label;
	
	}

	private static boolean isEmpty( String string ) {
		return ( string == null || string.trim().length() == 0 );
	}
	
	private static CharsetDecoder fDecoder;

	public static CharsetDecoder getCharsetDecoder() {
		String charsetName = CDebugCorePlugin.getDefault().getPluginPreferences().getString( ICDebugConstants.PREF_CHARSET );
		if (fDecoder == null || !fDecoder.charset().name().equals(charsetName))
		{
			Charset charset = Charset.forName(charsetName);
			fDecoder = charset.newDecoder();
		}
		return fDecoder;
	}

	/**
     * Note: Moved from AbstractCLaunchDelegate
     * @since 6.0
	 */
    public static ICProject getCProject(ILaunchConfiguration configuration) throws CoreException {
        String projectName = getProjectName(configuration);
        if (projectName != null) {
            projectName = projectName.trim();
            if (projectName.length() > 0) {
                IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
                ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(project);
                if (cProject != null && cProject.exists()) {
                    return cProject;
                }
            }
        }
        return null;
    }

    /**
     * Note: Moved from AbstractCLaunchDelegate
     * @since 6.0
     */
    public static String getProjectName(ILaunchConfiguration configuration) throws CoreException {
        return configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
    }

    /**
     * Note: Moved from AbstractCLaunchDelegate
     * @since 6.0
     */
    public static String getProgramName(ILaunchConfiguration configuration) throws CoreException {
        return configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, (String)null);
    }

    /**
     * Note: Moved from AbstractCLaunchDelegate
     * @since 6.0
     */
    public static IPath getProgramPath(ILaunchConfiguration configuration) throws CoreException {
        String path = getProgramName(configuration);
        if (path == null || path.trim().length() == 0) {
            return null;
        }
        return new Path(path);
    }
}
