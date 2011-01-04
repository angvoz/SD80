/*******************************************************************************
 * Copyright (c) 2010, 2011 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.android.build.internal.core.templates;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Reads from a template substituting marked values from the supplied Map.
 *
 */
public class TemplatedInputStream extends InputStream {

	private final InputStream in;
	private final Map<String, String> map;
	private char[] sub;
	private int pos;
	private int mark;
	
	public TemplatedInputStream(InputStream in, Map<String, String> map) {
		this.in = in;
		this.map = map;
	}
	
	@Override
	public int read() throws IOException {
		// if from a mark, return the char
		if (mark != 0) {
			int c = mark;
			mark = 0;
			return c;
		}

		// return char from sub layer if available
		if (sub != null) {
			char c = sub[pos++];
			if (pos >= sub.length)
				sub = null;
			return c;
		}
		
		int c = in.read();
		if (c == '%') {
			// check if it's a sub
			c = in.read();
			if (c == '{') {
				// it's a sub
				StringBuffer buff = new StringBuffer();
				for (c = in.read(); c != '}' && c >= 0; c = in.read())
					buff.append((char)c);
				String str = map.get(buff.toString());
				if (str != null) {
					sub = str.toCharArray();
					pos = 0;
				}
				return read(); // recurse to get the real char
			} else {
				// not a sub
				mark = c;
				return '%';
			}
		}
		
		return c;
	}

	@Override
	public void close() throws IOException {
		super.close();
		in.close();
	}
	
}
