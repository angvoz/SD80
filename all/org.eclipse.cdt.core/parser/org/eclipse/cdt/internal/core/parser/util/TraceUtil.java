/**********************************************************************
 * Copyright (c) 2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/

package org.eclipse.cdt.internal.core.parser.util;

import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IProblem;

/**
 * @author ddaoust
 */
public class TraceUtil {
	public static void outputTrace(IParserLogService log, String preface, IProblem problem, String first, String second, String third ) {
		if( log.isTracing() ) {
			StringBuffer buffer = new StringBuffer();
			if( preface != null ) buffer.append( preface );
			if( problem != null ) buffer.append( problem.getMessage());
			if( first   != null ) buffer.append( first );
			if( second  != null ) buffer.append( second );
			if( third   != null ) buffer.append( third );
			log.traceLog( buffer.toString() );
		}
	}
	public static void outputTrace(IParserLogService log, String preface, IProblem problem, int first, String second, int third ) {
		if( log.isTracing() ) {
			outputTrace(
					log, 
					preface, 
					problem, 
					Integer.toString( first ), 
					second, 
					Integer.toString( third ) );
		}
	}
	public static void outputTrace(IParserLogService log, String preface, String first, String second, String third ) {
			outputTrace(log, preface, null, first, second, third);
	}
	public static void outputTrace(IParserLogService log, String preface) {
		if( log.isTracing() ){
			if ( preface != null )
				log.traceLog( preface );
		}				
	}
}
