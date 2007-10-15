/*******************************************************************************
* Copyright (c) 2006, 2007 IBM Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     IBM Corporation - initial API and implementation
*********************************************************************************/
package org.eclipse.cdt.core.dom.parser.c99;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.c99.IPreprocessorExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.GCCScannerExtensionConfiguration;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.internal.core.parser.scanner2.GCCOldScannerExtensionConfiguration;
import org.eclipse.cdt.internal.core.parser.scanner2.ObjectStyleMacro;

/**
 * Adds some GCC specific extensions to the C99 parser.
 * 
 * @author Mike Kucera
 *
 */
public class GCCPreprocessorExtensionConfiguration implements
		IPreprocessorExtensionConfiguration {

	private final Map<String,String> additionalMacros = new HashMap<String,String>();
	
	public GCCPreprocessorExtensionConfiguration() {
		// Steal our configuration from the DOM scanner
		GCCScannerExtensionConfiguration wrappedConf = new GCCOldScannerExtensionConfiguration();
		CharArrayObjectMap caom = wrappedConf.getAdditionalMacros();
		
		for(int i = 0; i < caom.size(); ++i) {
			ObjectStyleMacro macro = (ObjectStyleMacro)caom.getAt(i);
			String signature = String.valueOf(macro.getSignature());
			String expansion = String.valueOf(macro.getExpansion());
			additionalMacros.put(signature, expansion);
		}
	}
	
	
	public Map<String,String> getAdditionalMacros() {
		return additionalMacros;
	}
}
