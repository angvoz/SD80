/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateArgument;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMValue;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;

/**
 * Collects methods to store an argument list in the database
 */
public class PDOMCPPTemplateParameterMap {
	/**
	 * Stores the given template parameter map in the database.
	 * @return the record by which the arguments can be referenced.
	 */
	public static long putMap(PDOMNode parent, ICPPTemplateParameterMap map) throws CoreException {
		final PDOMLinkage linkage= parent.getLinkage();
		final Database db= linkage.getDB();
		Integer[] keys= map.getAllParameterPositions();
		final short len= (short) Math.min(keys.length, (Database.MAX_MALLOC_SIZE-2)/12); 
		final long block= db.malloc(2+12*len);
		long p= block;

		db.putShort(p, len); p+=2;
		for (int i=0; i<len; i++) {
			final Integer paramPos = keys[i];
			db.putInt(p, paramPos); 
			p+=4; //TODO? assumes stored pointer size is 4?
			final ICPPTemplateArgument arg = map.getArgument(paramPos);
			if (arg.isNonTypeValue()) {
				final PDOMNode type= linkage.addType(parent, arg.getTypeOfNonTypeValue());
				// type can be null, if it is local
				db.putRecPtr(p, type == null ? 0 : type.getRecord());
				long valueRec= PDOMValue.store(db, linkage, arg.getNonTypeValue());
				db.putRecPtr(p+4, valueRec); //TODO: assumes that stored pointer size is 4.
			} else {
				final PDOMNode type= linkage.addType(parent, arg.getTypeValue());
				// type can be null, if it is local
				db.putRecPtr(p, type == null ? 0 : type.getRecord()); 
			}
			p+=8; //TODO; assumes stored pointer size
		}
		return block;
	}


	/**
	 * Clears the map in the database.
	 */
	public static void clearMap(PDOMNode parent, int rec) throws CoreException {
		final PDOMLinkage linkage= parent.getLinkage();
		final Database db= linkage.getDB();
		final short len= db.getShort(rec);
		
		Assert.isTrue(len >= 0 && len <= (Database.MAX_MALLOC_SIZE-2)/12);
		rec+=2;
		for (int i=0; i<len; i++) {
			rec+=4;
			final long typeRec= db.getRecPtr(rec);
			if (typeRec != 0) {
				final IType t= (IType) linkage.getNode(typeRec);
				linkage.deleteType(t, parent.getRecord());
			}			
			final long nonTypeValueRec= db.getRecPtr(rec+4);
			PDOMValue.delete(db, nonTypeValueRec);
			rec+= 8;
		}
		db.free(rec);
	}

	/**
	 * Restores the map from from the database.
	 */
	public static CPPTemplateParameterMap getMap(PDOMNode parent, long rec) throws CoreException {
		final PDOMLinkage linkage= parent.getLinkage();
		final Database db= linkage.getDB();
		final short len= db.getShort(rec);
		
		Assert.isTrue(len >= 0 && len <= (Database.MAX_MALLOC_SIZE-2)/12);
		if (len == 0) {
			return CPPTemplateParameterMap.EMPTY;
		}
		
		rec+=2;
		CPPTemplateParameterMap result= new CPPTemplateParameterMap(len);
		for (int i=0; i<len; i++) {
			final int parPos= db.getInt(rec);
			final long typeRec= db.getRecPtr(rec+4);
			final IType type= typeRec == 0 ? new CPPBasicType(-1, 0) : (IType) linkage.getNode(typeRec);
			final long nonTypeValRec= db.getRecPtr(rec+8); 
			ICPPTemplateArgument arg;
			if (nonTypeValRec != 0) {
				IValue val= PDOMValue.restore(db, linkage, nonTypeValRec);
				arg= new CPPTemplateArgument(val, type);
			} else {
				arg= new CPPTemplateArgument(type);
			}
			result.put(parPos, arg);
			rec+= 12;
		}
		return result;
	}
}
