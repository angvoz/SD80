/*******************************************************************************
 * Copyright (c) 2009, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.edc.tests;

import static org.junit.Assert.fail;

import org.eclipse.cdt.debug.edc.tcf.extension.AgentUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author LWang
 * 
 */
public class TestAgentUtils {

	/**
	 * Test method for
	 * {@link org.eclipse.cdt.debug.edc.tcf.extension.AgentUtils#hexStringToByteArray(java.lang.String)}
	 * .
	 */
	@Test
	public void testHexStringToByteArray() {
		byte[] bytes = AgentUtils.hexStringToByteArray("ff0a0120");
		Assert.assertEquals(-1, bytes[0]);
		Assert.assertEquals(10, bytes[1]);
		Assert.assertEquals(1, bytes[2]);
		Assert.assertEquals(32, bytes[3]);

		// Error case.
		try {
			bytes = AgentUtils.hexStringToByteArray("a0120");
			fail("Argument check failed.");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	/**
	 * Test method for
	 * {@link org.eclipse.cdt.debug.edc.tcf.extension.AgentUtils#byteArrayToHexString(byte[])}
	 * .
	 */
	@Test
	public void testByteArrayToHexString() {
		byte[] bytes = { -1, 10, 1, 32 };
		String str = AgentUtils.byteArrayToHexString(bytes);
		Assert.assertEquals("ff0a0120", str);
	}
}
