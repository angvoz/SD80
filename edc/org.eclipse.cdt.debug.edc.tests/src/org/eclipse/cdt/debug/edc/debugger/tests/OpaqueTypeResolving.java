/*******************************************************************************
 * Copyright (c) 2011 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Nokia - Initial API and implementation. Jun 9, 2011
 *******************************************************************************/

package org.eclipse.cdt.debug.edc.debugger.tests;

import org.eclipse.cdt.debug.edc.internal.services.dsf.Symbols;
import org.eclipse.cdt.debug.edc.internal.symbols.ICompositeType;
import org.eclipse.cdt.debug.edc.internal.symbols.PointerType;
import org.eclipse.cdt.debug.edc.internal.symbols.TypedefType;
import org.eclipse.cdt.debug.edc.services.IEDCExpression;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.debug.edc.tests.TestUtils;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IModules.ISymbolDMContext;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the opaque type resolution.
 */
public class OpaqueTypeResolving extends SimpleDebuggerTest {

	@Override
	public String getAlbumName() {
		// This EDC Snapshot album is based on a CPP project that has opaque pointer.
		// Please unzip the .dsa file and look into the resources folder for the source
		// files of the project.
		return "OpaquePtr_resolution.dsa";
	}

	@Test
	public void testOpaqueTypeResolving() throws Exception {
		// An opaque type that's defined somewhere else. We can resolve it.
		//
		//     typedef struct PrivateStruct* PrivatePTR;
		//	   PrivatePTR  opaque_ptr = 0;
		//
		// At this point, debug session from the snapshot is stopped at
		// a the end of the executable.
		
		IEDCExpression exprVal = TestUtils.getExpressionDMC(session, frame, "opaque_ptr");
		IType type = exprVal.getEvaluatedType();
		
		// First ensure if the original type of the var is an opaque type.
		//
		Assert.assertTrue(type instanceof TypedefType);
		type = type.getType();	// de-typedef
		Assert.assertTrue(type instanceof PointerType);
		type = type.getType();	// de-reference

		Assert.assertTrue(type instanceof ICompositeType);
		Assert.assertTrue(((ICompositeType)type).isOpaque());
		Assert.assertTrue("Type is not opaque type.", type.getByteSize() == 0);
		
		// Now resolve the opaque type
		//
		Symbols symService = TestUtils.getService(session, Symbols.class);
		ISymbolDMContext symCtx = DMContexts.getAncestorOfType(exprVal, ISymbolDMContext.class);
		ICompositeType defined = symService.resolveOpaqueType(symCtx, (ICompositeType) type);
		Assert.assertFalse(defined.isOpaque());
		Assert.assertEquals(type.getName(), defined.getName());

		// Resolve one that's not an opaque type, fail.
		type = symService.resolveOpaqueType(symCtx, defined);
		Assert.assertNull(type);
	}
	
	@Test
	public void testOpaqueTypeNeverDefined() throws Exception {
		// An opaque type that's never defined anywhere. We can resolve it.
		//
		//		typedef struct UndefinedStruct* UndefinedPTR;
		//		UndefinedPTR opaque_ptr_to_undefined;
		//
		// At this point, debug session from the snapshot is stopped at
		// a the end of the executable.
		
		IEDCExpression exprVal = TestUtils.getExpressionDMC(session, frame, "opaque_ptr_to_undefined");
		IType type = exprVal.getEvaluatedType();
		
		// First ensure if the original type of the var is an opaque type.
		//
		Assert.assertTrue(type instanceof TypedefType);
		type = type.getType();	// de-typedef
		Assert.assertTrue(type instanceof PointerType);
		type = type.getType();	// de-reference

		Assert.assertTrue(type instanceof ICompositeType);
		Assert.assertTrue(((ICompositeType)type).isOpaque());
		Assert.assertTrue("Type is not opaque type.", type.getByteSize() == 0);
		
		// Now try to resolve the opaque type, should fail
		//
		Symbols symService = TestUtils.getService(session, Symbols.class);
		ISymbolDMContext symCtx = DMContexts.getAncestorOfType(exprVal, ISymbolDMContext.class);
		ICompositeType defined = symService.resolveOpaqueType(symCtx, (ICompositeType) type);
		Assert.assertNull(defined);
	}
}
