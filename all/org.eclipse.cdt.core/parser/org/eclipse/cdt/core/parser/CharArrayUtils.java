/*
 * Created on May 28, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.cdt.core.parser;

/**
 * @author dschaefe
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CharArrayUtils {

	public static final int hash(char[] str, int start, int length) {
		int h = 0;
		
		int curr = start;
		for (int i = length; i > 0; --i, curr++)
			h += str[curr];

		return h;
	}

	public static final int hash(char[] str) {
		return hash(str, 0, str.length);
	}
	
	public static final boolean equals(char[] str1, char[] str2) {
		if (str1 == str2)
			return true;
		
		if (str1 == null || str2 == null)
			return false;
		
		if (str1.length != str2.length)
			return false;
		
		for (int i = 0; i < str1.length; ++i)
			if (str1[i] != str2[i])
				return false;
		
		return true;
	}
}
