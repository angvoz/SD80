/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.parser.scanner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;

/**
 * Various location contexts which are suitable for interpreting local offsets. These offsets are
 * converted in a global sequence-number to make all ast nodes comparable with each other.
 * @since 5.0
 */
abstract class LocationCtx implements ILocationCtx {
	final ContainerLocationCtx fParent;
	final int fSequenceNumber;
	final int fParentOffset;
	final int fParentEndOffset;

	public LocationCtx(ContainerLocationCtx parent, int parentOffset, int parentEndOffset, int sequenceNumber) {
		fParent= parent;
		fParentOffset= parentOffset;
		fParentEndOffset= parentEndOffset;
		fSequenceNumber= sequenceNumber;
		if (parent != null) {
			parent.addChild(this);
		}
	}
	
	public String getFilePath() {
		return fParent.getFilePath();
	}
	
	final public ILocationCtx getParent() {
		return fParent;
	}
	/**
	 * Returns the amount of sequence numbers occupied by this context including its children.
	 */
	public abstract int getSequenceLength();
	
	/**
	 * Converts an offset within this context to the sequence number. In case there are child-contexts
	 * behind the given offset, you need to set checkChildren to <code>true</code>.
	 */
	public int getSequenceNumberForOffset(int offset, boolean checkChildren) {
		return fSequenceNumber+offset;
	}

	/**
	 * When a child-context is finished it reports its total sequence length, such that offsets in this
	 * context can be converted to sequence numbers.
	 */
	public void addChildSequenceLength(int childLength) {
		assert false;
	}
	
	/**
	 * Returns the line number for an offset within this context. Not all contexts support line numbers,
	 * so this may return 0.
	 */
	public int getLineNumber(int offset) {
		return 0;
	}

	/**
	 * Returns the minimal context containing the specified range, assuming that it is contained in
	 * this context.
	 */
	public LocationCtx ctxForNumberRange(int sequenceNumber, int length) {
		return this;
	}

	/**
	 * Returns the minimal file location containing the specified sequence number range, assuming 
	 * that it is contained in this context.
	 */
	public IASTFileLocation fileLocationForNumberRange(int sequenceNumber, int length) {
		return fParent.fileLocationForOffsetRange(fParentOffset, fParentEndOffset-fParentOffset);
	}

	/**
	 * Returns the file location containing the specified offset range in this context.
	 */
	public IASTFileLocation fileLocationForOffsetRange(int parentOffset, int length) {
		return fParent.fileLocationForOffsetRange(fParentOffset, fParentEndOffset-fParentOffset);
	}

	/**
	 * Support for the dependency tree, add inclusion statements found in this context.
	 */
	public void getInclusions(ArrayList target) {
	}

	/**
	 * Support for the dependency tree, returns inclusion statement that created this context, or <code>null</code>.
	 */
	public ASTInclusionStatement getInclusionStatement() {
		return null;
	}
}

class ContainerLocationCtx extends LocationCtx {
	private int fChildSequenceLength;
	private ArrayList fChildren;
	private char[] fSource;
	private int[] fLineOffsets;
	
	public ContainerLocationCtx(ContainerLocationCtx parent, char[] source, int parentOffset, int parentEndOffset, int sequenceNumber) {
		super(parent, parentOffset, parentEndOffset, sequenceNumber);
		fSource= source;
	}
	
	public void addChild(LocationCtx locationCtx) {
		if (fChildren == null) {
			fChildren= new ArrayList();
		}
		fChildren.add(locationCtx);
	}

	public char[] getSource(int offset, int length) {
		offset= Math.max(0, Math.min(offset, fSource.length));
		length= Math.max(0, Math.min(length, fSource.length-offset));
		char[] result= new char[length];
		System.arraycopy(fSource, offset, result, 0, length);
		return result;
	}

	public final int getSequenceLength() {
		return fSource.length + fChildSequenceLength;
	}
	public final int getSequenceNumberForOffset(int offset, boolean checkChildren) {
		int result= fSequenceNumber + fChildSequenceLength + offset;
		if (checkChildren && fChildren != null) {
			for (int i= fChildren.size()-1; i >= 0; i--) {
				final LocationCtx child= (LocationCtx) fChildren.get(i);
				if (child.fParentEndOffset > offset) {	// child was inserted behind the offset, adjust sequence number
					result-= child.getSequenceLength();
				}
				else {
					return result;
				}
			}
		}
		return result;
	}
	
	public void addChildSequenceLength(int childLength) {
		fChildSequenceLength+= childLength;
	}

	public final LocationCtx ctxForNumberRange(int sequenceNumber, int length) {
		int testEnd= length > 1 ? sequenceNumber+length-1 : sequenceNumber;
		final LocationCtx child= findChildLessOrEqualThan(sequenceNumber);
		if (child != null && child.fSequenceNumber+child.getSequenceLength() > testEnd) {
			return child.ctxForNumberRange(sequenceNumber, length);
		}
		return this;
	}
	
	public IASTFileLocation fileLocationForNumberRange(int sequenceNumber, int length) {
		// try to delegate to a child.
		int testEnd= length > 1 ? sequenceNumber+length-1 : sequenceNumber;
		final LocationCtx child= findChildLessOrEqualThan(sequenceNumber);
		if (child != null && child.fSequenceNumber+child.getSequenceLength() > testEnd) {
			if (testEnd == sequenceNumber || findChildLessOrEqualThan(testEnd) == child) {
				return child.fileLocationForNumberRange(sequenceNumber, length);
			}
		}
		return super.fileLocationForNumberRange(sequenceNumber, length);
	}

	final LocationCtx findChildLessOrEqualThan(final int sequenceNumber) {
		if (fChildren == null) {
			return null;
		}
		int upper= fChildren.size();
		int lower= 0;
		while (upper > lower) {
			int middle= (upper+lower)/2;
			LocationCtx child= (LocationCtx) fChildren.get(middle);
			if (child.fSequenceNumber <= sequenceNumber) {
				lower= middle+1;
			}
			else {
				upper= middle;
			}
		}
		if (lower > 0) {
			return (LocationCtx) fChildren.get(lower-1);
		}
		return null;
	}

	public void getInclusions(ArrayList result) {
		if (fChildren != null) {
			for (Iterator iterator = fChildren.iterator(); iterator.hasNext();) {
				LocationCtx ctx= (LocationCtx) iterator.next();
				if (ctx.getInclusionStatement() != null) {
					result.add(new ASTInclusionNode(ctx));
				}
				else {
					ctx.getInclusions(result);
				}
			}
		}
	}
	

	public int getLineNumber(int offset) {
		if (fLineOffsets == null) {
			fLineOffsets= computeLineOffsets();
		}
		int idx= Arrays.binarySearch(fLineOffsets, offset);
		if (idx < 0) {
			return -idx;
		}
		return idx+1;
	}

	private int[] computeLineOffsets() {
		ArrayList offsets= new ArrayList();
		for (int i = 0; i < fSource.length; i++) {
			if (fSource[i] == '\n') {
				offsets.add(new Integer(i));
			}
		}
		int[] result= new int[offsets.size()];
		for (int i = 0; i < result.length; i++) {
			result[i]= ((Integer) offsets.get(i)).intValue();
			
		}
		return result;
	}

}

class FileLocationCtx extends ContainerLocationCtx {
	private final String fFilename;
	private final ASTInclusionStatement fASTInclude;

	public FileLocationCtx(ContainerLocationCtx parent, String filename, char[] source, int parentOffset, int parentEndOffset, int sequenceNumber, ASTInclusionStatement inclusionStatement) {
		super(parent, source, parentOffset, parentEndOffset, sequenceNumber);
		fFilename= new String(filename);
		fASTInclude= inclusionStatement;
	}
	
	public final void addChildSequenceLength(int childLength) {
		super.addChildSequenceLength(childLength);
		if (fASTInclude != null) {
			fASTInclude.setLength(fASTInclude.getLength()+childLength);
		}
	}

	public final String getFilePath() {
		return fFilename;
	}

	public IASTFileLocation fileLocationForNumberRange(int sequenceNumber, int length) {
		// try to delegate to a child.
		final int testEnd= length > 1 ? sequenceNumber+length-1 : sequenceNumber;
		final int sequenceEnd= sequenceNumber+length;
		final LocationCtx child1= findChildLessOrEqualThan(sequenceNumber);
		final LocationCtx child2= testEnd == sequenceNumber ? child1 : findChildLessOrEqualThan(testEnd);

		if (child1 == child2 && child1 != null && child1.fSequenceNumber + child1.getSequenceLength() > testEnd) {
			return child1.fileLocationForNumberRange(sequenceNumber, length);
		}
		
		// handle here
		int startOffset;
		int endOffset;
		
		if (child1 == null) {
			startOffset= sequenceNumber-fSequenceNumber;
		}
		else {
			int childSequenceEnd= child1.fSequenceNumber + child1.getSequenceLength();
			if (sequenceNumber < childSequenceEnd) {
				startOffset= child1.fParentOffset;
			}
			else {	// start beyond child1
				startOffset= child1.fParentEndOffset + sequenceNumber-childSequenceEnd;
			}
		}
		if (child2 == null) {
			endOffset= sequenceEnd-fSequenceNumber;
		}
		else {
			int childSequenceEnd= child2.fSequenceNumber + child2.getSequenceLength();
			if (childSequenceEnd < sequenceEnd) { // beyond child2
				endOffset= child2.fParentEndOffset+sequenceEnd-childSequenceEnd;
			}
			else {
				endOffset= child2.fParentEndOffset;
			}
		}
		return new ASTFileLocation(this, startOffset, endOffset-startOffset);
	}
	
	public IASTFileLocation fileLocationForOffsetRange(int offset, int length) {
		return new ASTFileLocation(this, offset, length);
	}

	public ASTInclusionStatement getInclusionStatement() {
		return fASTInclude;
	}
}


class MacroExpansionCtx extends LocationCtx {
	private final int fLength;

	public MacroExpansionCtx(ContainerLocationCtx parent, int parentOffset, int parentEndOffset,
			int sequenceNumber, int length, ImageLocationInfo[] imageLocations,	ASTPreprocessorName expansion) {
		super(parent, parentOffset, parentEndOffset, sequenceNumber);
		fLength= length;
	}

	public int getSequenceLength() {
		return fLength;
	}
	
	// mstodo- image locations
}