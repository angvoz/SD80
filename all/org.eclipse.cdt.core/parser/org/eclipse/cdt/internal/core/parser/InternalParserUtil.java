/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser;

import java.io.File;
import java.io.IOException;

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.ParserFactory;

/**
 * @author jcamelon
 */
public class InternalParserUtil extends ParserFactory {

	

	/**
	 * @param finalPath
	 * @return
	 */
	public static CodeReader createFileReader(String finalPath) {
		File includeFile = new File(finalPath);
		if (includeFile.exists() && includeFile.isFile()) 
		{
			try {
				return new CodeReader(finalPath);
			} catch (IOException e) {
			}
		}
		return null;
	}
}
