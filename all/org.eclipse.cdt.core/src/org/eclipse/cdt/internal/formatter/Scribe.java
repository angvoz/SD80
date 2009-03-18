/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.formatter;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.internal.formatter.align.Alignment;
import org.eclipse.cdt.internal.formatter.align.AlignmentException;
import org.eclipse.cdt.internal.formatter.scanner.Scanner;
import org.eclipse.cdt.internal.formatter.scanner.Token;
import org.eclipse.jface.text.Position;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * This class is responsible for dumping formatted source.
 * 
 * @since 4.0
 */
public class Scribe {

	private static final String EMPTY_STRING= ""; //$NON-NLS-1$
	private static final String SPACE= " "; //$NON-NLS-1$

	private static final int INITIAL_SIZE= 100;

	private final DefaultCodeFormatterOptions preferences;
	public final Scanner scanner;

	/** one-based column */
	public int column= 1;

	// Most specific alignment.
	public Alignment currentAlignment;
	public Alignment memberAlignment;
	public AlignmentException currentAlignmentException;

	public Token currentToken;

	// edits management
	private OptimizedReplaceEdit[] edits;
	public int editsIndex;

	public int indentationLevel;
	public int numberOfIndentations;
	public int indentationSize;

	private final String lineSeparator;
	private final boolean indentEmptyLines;
	private final int pageWidth;
	private boolean preserveNewLines;
	private boolean checkLineWrapping;
	public int lastNumberOfNewLines;
	public int line;

	public boolean needSpace= false;
	public boolean pendingSpace= false;

	public int tabLength;
	public int tabChar;
	private final boolean useTabsOnlyForLeadingIndents;

	private int textRegionEnd;
	private int textRegionStart;
	public int scannerEndPosition;

	private List<Position> fSkipPositions= Collections.emptyList();

	private boolean skipOverInactive;

	private int fSkipStartOffset= Integer.MAX_VALUE;
	private int fSkipEndOffset;
	private int fSkippedIndentations;

	Scribe(CodeFormatterVisitor formatter, int offset, int length) {
		scanner= new Scanner();
		preferences= formatter.preferences;
		pageWidth= preferences.page_width;
		tabLength= preferences.tab_size;
		indentationLevel= 0; // initialize properly
		numberOfIndentations= 0;
		useTabsOnlyForLeadingIndents= preferences.use_tabs_only_for_leading_indentations;
		indentEmptyLines= preferences.indent_empty_lines;
		tabChar= preferences.tab_char;
		if (tabChar == DefaultCodeFormatterOptions.MIXED) {
			indentationSize= preferences.indentation_size;
		} else {
			indentationSize= tabLength;
		}
		lineSeparator= preferences.line_separator;
		indentationLevel= preferences.initial_indentation_level * indentationSize;
		textRegionStart= offset;
		textRegionEnd= offset + length - 1;
		reset();
	}

	private final void addDeleteEdit(int start, int end) {
		if (edits.length == editsIndex) {
			// resize
			resize();
		}
		addOptimizedReplaceEdit(start, end - start + 1, EMPTY_STRING);
	}

	public final void addInsertEdit(int insertPosition, String insertedString) {
		if (edits.length == editsIndex) {
			// resize
			resize();
		}
		addOptimizedReplaceEdit(insertPosition, 0, insertedString);
	}

	private final void addOptimizedReplaceEdit(int offset, int length, String replacement) {
		if (editsIndex > 0) {
			// try to merge last two edits
			final OptimizedReplaceEdit previous= edits[editsIndex - 1];
			final int previousOffset= previous.offset;
			final int previousLength= previous.length;
			final int endOffsetOfPreviousEdit= previousOffset + previousLength;
			final int replacementLength= replacement.length();
			final String previousReplacement= previous.replacement;
			final int previousReplacementLength= previousReplacement.length();
			if (previousOffset == offset && previousLength == length
					&& (replacementLength == 0 || previousReplacementLength == 0)) {
				if (currentAlignment != null) {
					final Location location= currentAlignment.location;
					if (location.editsIndex == editsIndex) {
						location.editsIndex--;
						location.textEdit= previous;
					}
				}
				editsIndex--;
				return;
			}
			if (endOffsetOfPreviousEdit == offset) {
				if (length != 0) {
					if (replacementLength != 0) {
						edits[editsIndex - 1]= new OptimizedReplaceEdit(previousOffset, previousLength + length,
								previousReplacement + replacement);
					} else if (previousLength + length == previousReplacementLength) {
						// check the characters. If they are identical, we can
						// get rid of the previous edit
						boolean canBeRemoved= true;
						loop: for (int i= previousOffset; i < previousOffset + previousReplacementLength; i++) {
							if (scanner.source[i] != previousReplacement.charAt(i - previousOffset)) {
								edits[editsIndex - 1]= new OptimizedReplaceEdit(previousOffset,
										previousReplacementLength, previousReplacement);
								canBeRemoved= false;
								break loop;
							}
						}
						if (canBeRemoved) {
							if (currentAlignment != null) {
								final Location location= currentAlignment.location;
								if (location.editsIndex == editsIndex) {
									location.editsIndex--;
									location.textEdit= previous;
								}
							}
							editsIndex--;
						}
					} else {
						edits[editsIndex - 1]= new OptimizedReplaceEdit(previousOffset, previousLength + length,
								previousReplacement);
					}
				} else {
					if (replacementLength != 0) {
						edits[editsIndex - 1]= new OptimizedReplaceEdit(previousOffset, previousLength,
								previousReplacement + replacement);
					}
				}
			} else {
				assert endOffsetOfPreviousEdit < offset;
				edits[editsIndex++]= new OptimizedReplaceEdit(offset, length, replacement);
			}
		} else {
			edits[editsIndex++]= new OptimizedReplaceEdit(offset, length, replacement);
		}
	}

	/**
	 * Add a replace edit.
	 * @param start  start offset (inclusive)
	 * @param end  end offset (inclusive)
	 * @param replacement  the replacement string
	 */
	public final void addReplaceEdit(int start, int end, String replacement) {
		if (edits.length == editsIndex) {
			// resize
			resize();
		}
		addOptimizedReplaceEdit(start, end - start + 1, replacement);
	}

	public void alignFragment(Alignment alignment, int fragmentIndex) {
		alignment.fragmentIndex= fragmentIndex;
		alignment.checkColumn();
		alignment.performFragmentEffect();
	}

	public void consumeNextToken() {
		printComment();
		currentToken= scanner.nextToken();
		addDeleteEdit(scanner.getCurrentTokenStartPosition(), scanner.getCurrentTokenEndPosition());
	}

	public Alignment createAlignment(String name, int mode, int count, int sourceRestart) {
		return createAlignment(name, mode, Alignment.R_INNERMOST, count, sourceRestart);
	}

	public Alignment createAlignment(String name, int mode, int count, int sourceRestart, boolean adjust) {
		return createAlignment(name, mode, Alignment.R_INNERMOST, count, sourceRestart, adjust);
	}

	public Alignment createAlignment(String name, int mode, int tieBreakRule, int count, int sourceRestart) {
		return createAlignment(name, mode, tieBreakRule, count, sourceRestart,
				preferences.continuation_indentation, false);
	}

	public Alignment createAlignment(String name, int mode, int count, int sourceRestart, int continuationIndent,
			boolean adjust) {
		return createAlignment(name, mode, Alignment.R_INNERMOST, count, sourceRestart, continuationIndent, adjust);
	}

	public Alignment createAlignment(String name, int mode, int tieBreakRule, int count, int sourceRestart,
			int continuationIndent, boolean adjust) {
		Alignment alignment= new Alignment(name, mode, tieBreakRule, this, count, sourceRestart, continuationIndent);
		// adjust break indentation
		if (adjust && memberAlignment != null) {
			Alignment current= memberAlignment;
			while (current.enclosing != null) {
				current= current.enclosing;
			}
			if ((current.mode & Alignment.M_MULTICOLUMN) != 0) {
				final int indentSize= indentationSize;
				switch (current.chunkKind) {
				case Alignment.CHUNK_METHOD:
				case Alignment.CHUNK_TYPE:
					if ((mode & Alignment.M_INDENT_BY_ONE) != 0) {
						alignment.breakIndentationLevel= indentationLevel + indentSize;
					} else {
						alignment.breakIndentationLevel= indentationLevel + continuationIndent * indentSize;
					}
					alignment.update();
					break;
				case Alignment.CHUNK_FIELD:
					if ((mode & Alignment.M_INDENT_BY_ONE) != 0) {
						alignment.breakIndentationLevel= current.originalIndentationLevel + indentSize;
					} else {
						alignment.breakIndentationLevel= current.originalIndentationLevel + continuationIndent
								* indentSize;
					}
					alignment.update();
					break;
				}
			} else {
				switch (current.mode & Alignment.SPLIT_MASK) {
				case Alignment.M_COMPACT_SPLIT:
				case Alignment.M_COMPACT_FIRST_BREAK_SPLIT:
				case Alignment.M_NEXT_PER_LINE_SPLIT:
				case Alignment.M_NEXT_SHIFTED_SPLIT:
				case Alignment.M_ONE_PER_LINE_SPLIT:
					final int indentSize= indentationSize;
					switch (current.chunkKind) {
					case Alignment.CHUNK_METHOD:
					case Alignment.CHUNK_TYPE:
						if ((mode & Alignment.M_INDENT_BY_ONE) != 0) {
							alignment.breakIndentationLevel= indentationLevel + indentSize;
						} else {
							alignment.breakIndentationLevel= indentationLevel + continuationIndent * indentSize;
						}
						alignment.update();
						break;
					case Alignment.CHUNK_FIELD:
						if ((mode & Alignment.M_INDENT_BY_ONE) != 0) {
							alignment.breakIndentationLevel= current.originalIndentationLevel + indentSize;
						} else {
							alignment.breakIndentationLevel= current.originalIndentationLevel + continuationIndent
									* indentSize;
						}
						alignment.update();
						break;
					}
					break;
				}
			}
		}
		return alignment;
	}

	public Alignment createMemberAlignment(String name, int mode, int count, int sourceRestart) {
		Alignment mAlignment= createAlignment(name, mode, Alignment.R_INNERMOST, count, sourceRestart);
		mAlignment.breakIndentationLevel= indentationLevel;
		return mAlignment;
	}

	public void enterAlignment(Alignment alignment) {
		alignment.enclosing= currentAlignment;
		currentAlignment= alignment;
	}

	public void enterMemberAlignment(Alignment alignment) {
		alignment.enclosing= memberAlignment;
		memberAlignment= alignment;
	}

	public void exitAlignment(Alignment alignment, boolean discardAlignment) {
		Alignment current= currentAlignment;
		while (current != null) {
			if (current == alignment)
				break;
			current= current.enclosing;
		}
		if (current == null) {
			throw new AbortFormatting("could not find matching alignment: " + alignment); //$NON-NLS-1$
		}
		indentationLevel= alignment.location.outputIndentationLevel;
		numberOfIndentations= alignment.location.numberOfIndentations;
		if (discardAlignment) {
			currentAlignment= alignment.enclosing;
		}
	}

	public void exitMemberAlignment(Alignment alignment) {
		Alignment current= memberAlignment;
		while (current != null) {
			if (current == alignment)
				break;
			current= current.enclosing;
		}
		if (current == null) {
			throw new AbortFormatting("could not find matching alignment: " + alignment); //$NON-NLS-1$
		}
		indentationLevel= current.location.outputIndentationLevel;
		numberOfIndentations= current.location.numberOfIndentations;
		memberAlignment= current.enclosing;
	}

	public Alignment getAlignment(String name) {
		if (currentAlignment != null) {
			return currentAlignment.getAlignment(name);
		}
		return null;
	}

	/**
	 * Answer actual indentation level based on true column position
	 * 
	 * @return int
	 */
	public int getColumnIndentationLevel() {
		return column - 1;
	}

	public String getEmptyLines(int linesNumber) {
		StringBuilder buffer= new StringBuilder();
		if (lastNumberOfNewLines == 0) {
			linesNumber++; // add an extra line breaks
			for (int i= 0; i < linesNumber; i++) {
				if (indentEmptyLines)
					printIndentationIfNecessary(buffer);
				buffer.append(lineSeparator);
			}
			lastNumberOfNewLines+= linesNumber;
			line+= linesNumber;
			column= 1;
			needSpace= false;
			pendingSpace= false;
		} else if (lastNumberOfNewLines == 1) {
			for (int i= 0; i < linesNumber; i++) {
				if (indentEmptyLines)
					printIndentationIfNecessary(buffer);
				buffer.append(lineSeparator);
			}
			lastNumberOfNewLines+= linesNumber;
			line+= linesNumber;
			column= 1;
			needSpace= false;
			pendingSpace= false;
		} else {
			if ((lastNumberOfNewLines - 1) >= linesNumber) {
				// there is no need to add new lines
				return EMPTY_STRING;
			}
			final int realNewLineNumber= linesNumber - lastNumberOfNewLines + 1;
			for (int i= 0; i < realNewLineNumber; i++) {
				if (indentEmptyLines)
					printIndentationIfNecessary(buffer);
				buffer.append(lineSeparator);
			}
			lastNumberOfNewLines+= realNewLineNumber;
			line+= realNewLineNumber;
			column= 1;
			needSpace= false;
			pendingSpace= false;
		}
		return String.valueOf(buffer);
	}

	public OptimizedReplaceEdit getLastEdit() {
		if (editsIndex > 0) {
			return edits[editsIndex - 1];
		}
		return null;
	}

	Alignment getMemberAlignment() {
		return memberAlignment;
	}

	public String getNewLine() {
		if (lastNumberOfNewLines >= 1) {
			column= 1; // ensure that the scribe is at the beginning of a new
						// line
			return EMPTY_STRING;
		}
		line++;
		lastNumberOfNewLines= 1;
		column= 1;
		needSpace= false;
		pendingSpace= false;
		return lineSeparator;
	}

	/**
	 * Answer next indentation level based on column estimated position (if
	 * column is not indented, then use indentationLevel)
	 */
	public int getNextIndentationLevel(int someColumn) {
		int indent= someColumn - 1;
		if (indent == 0)
			return indentationLevel;
		if (tabChar == DefaultCodeFormatterOptions.TAB) {
			if (useTabsOnlyForLeadingIndents) {
				return indent;
			}
			int rem= indent % indentationSize;
			int addition= rem == 0 ? 0 : indentationSize - rem; // round to
																// superior
			return indent + addition;
		} else {
			return indent;
		}
	}

	private String getPreserveEmptyLines(int count) {
		if (count > 0) {
			if (preferences.number_of_empty_lines_to_preserve != 0) {
				int linesToPreserve= Math.min(count, preferences.number_of_empty_lines_to_preserve);
				return getEmptyLines(linesToPreserve);
			} else {
				return getNewLine();
			}
		} else if (preserveNewLines) {
			return getNewLine();
		}
		return EMPTY_STRING;
	}

	public TextEdit getRootEdit() {
		MultiTextEdit edit= null;
		int length= textRegionEnd - textRegionStart + 1;
		if (textRegionStart <= 0) {
			if (length <= 0) {
				edit= new MultiTextEdit(0, 0);
			} else {
				edit= new MultiTextEdit(0, textRegionEnd + 1);
			}
		} else {
			edit= new MultiTextEdit(textRegionStart, textRegionEnd - textRegionStart + 1);
		}
		for (int i= 0, max= editsIndex; i < max; i++) {
			OptimizedReplaceEdit currentEdit= edits[i];
			if (isValidEdit(currentEdit)) {
				edit.addChild(new ReplaceEdit(currentEdit.offset, currentEdit.length, currentEdit.replacement));
			}
		}
		edits= null;
		return edit;
	}

	public void handleLineTooLong() {
		// search for closest breakable alignment, using tiebreak rules
		// look for outermost breakable one
		int relativeDepth= 0, outerMostDepth= -1;
		Alignment targetAlignment= currentAlignment;
		while (targetAlignment != null) {
			if (targetAlignment.tieBreakRule == Alignment.R_OUTERMOST && targetAlignment.couldBreak()) {
				outerMostDepth= relativeDepth;
			}
			targetAlignment= targetAlignment.enclosing;
			relativeDepth++;
		}
		if (outerMostDepth >= 0) {
			throwAlignmentException(AlignmentException.LINE_TOO_LONG, outerMostDepth);
		}
		// look for innermost breakable one
		relativeDepth= 0;
		targetAlignment= currentAlignment;
		while (targetAlignment != null) {
			if (targetAlignment.couldBreak()) {
				throwAlignmentException(AlignmentException.LINE_TOO_LONG, relativeDepth);
			}
			targetAlignment= targetAlignment.enclosing;
			relativeDepth++;
		}
		// did not find any breakable location - proceed
	}

	private void throwAlignmentException(int kind, int relativeDepth) {
		AlignmentException e= new AlignmentException(kind, relativeDepth);
		currentAlignmentException= e;
		throw e;
	}

	public void indent() {
		if (shouldSkip(scanner.getCurrentPosition())) {
			fSkippedIndentations++;
			return;
		}
		indentationLevel+= indentationSize;
		numberOfIndentations++;
	}

	/**
	 * @param translationUnitSource
	 */
	public void initializeScanner(char[] translationUnitSource) {
		scanner.setSource(translationUnitSource);
		scannerEndPosition= translationUnitSource.length;
		scanner.resetTo(0, scannerEndPosition);
		edits= new OptimizedReplaceEdit[INITIAL_SIZE];
	}

	/**
	 * @param list
	 */
	public void setSkipPositions(List<Position> list) {
		fSkipPositions= list;
		skipOverInactive= !list.isEmpty();
	}

	private boolean isValidEdit(OptimizedReplaceEdit edit) {
		final int editLength= edit.length;
		final int editReplacementLength= edit.replacement.length();
		final int editOffset= edit.offset;
		if (editLength != 0) {
			if (textRegionStart <= editOffset && (editOffset + editLength - 1) <= textRegionEnd) {
				if (editReplacementLength != 0 && editLength == editReplacementLength) {
					for (int i= editOffset, max= editOffset + editLength; i < max; i++) {
						if (scanner.source[i] != edit.replacement.charAt(i - editOffset)) {
							return true;
						}
					}
					return false;
				} else {
					return true;
				}
			} else if (editOffset + editLength == textRegionStart) {
				int i= editOffset;
				for (int max= editOffset + editLength; i < max; i++) {
					int replacementStringIndex= i - editOffset;
					if (replacementStringIndex >= editReplacementLength
							|| scanner.source[i] != edit.replacement.charAt(replacementStringIndex)) {
						break;
					}
				}
				if (i - editOffset != editReplacementLength && i != editOffset + editLength - 1) {
					edit.offset= textRegionStart;
					edit.length= 0;
					edit.replacement= edit.replacement.substring(i - editOffset);
					return true;
				}
			}
		} else if (textRegionStart <= editOffset && editOffset <= textRegionEnd) {
			return true;
		} else if (editOffset == scannerEndPosition && editOffset == textRegionEnd + 1) {
			return true;
		}
		return false;
	}

	private void preserveEmptyLines(int count, int insertPosition) {
		if (count > 0) {
			if (preferences.number_of_empty_lines_to_preserve != 0) {
				int linesToPreserve= Math.min(count, preferences.number_of_empty_lines_to_preserve);
				printEmptyLines(linesToPreserve, insertPosition);
			} else {
				printNewLine(insertPosition);
			}
		} else {
			printNewLine(insertPosition);
		}
	}

	public void printRaw(int startOffset, int length) {
		if (length <= 0) {
			return;
		}
		int currentPosition= scanner.getCurrentPosition();
		if (shouldSkip(currentPosition)) {
			return;
		}
		if (startOffset > currentPosition) {
			printComment();
			currentPosition= scanner.getCurrentPosition();
		}
		if (pendingSpace) {
			addInsertEdit(currentPosition, SPACE);
			pendingSpace= false;
			needSpace= false;
		}
		if (startOffset + length < currentPosition) {
			// don't move backwards
			return;
		}
		boolean savedPreserveNL= preserveNewLines;
		boolean savedSkipOverInactive= skipOverInactive;
		int savedScannerEndPos= scannerEndPosition;
		preserveNewLines= true;
		skipOverInactive= false;
		scannerEndPosition= startOffset + length;
		try {
			scanner.resetTo(Math.max(startOffset, currentPosition), startOffset + length - 1);
			int parenLevel= 0;
			while (true) {
				boolean hasWhitespace= printComment();
				currentToken= scanner.nextToken();
				if (currentToken == null) {
					if (hasWhitespace) {
						space();
					}
					break;
				}
				if (pendingSpace) {
					addInsertEdit(scanner.getCurrentTokenStartPosition(), SPACE);
					pendingSpace= false;
					needSpace= false;
				}
				switch (currentToken.type) {
				case Token.tLBRACE: {
					scanner.resetTo(scanner.getCurrentTokenStartPosition(), scannerEndPosition-1);
					formatOpeningBrace(preferences.brace_position_for_block, preferences.insert_space_before_opening_brace_in_block);
					if (preferences.indent_statements_compare_to_block) {
						indent();
					}
					break;
				}
				case Token.tRBRACE: {
					scanner.resetTo(scanner.getCurrentTokenStartPosition(), scannerEndPosition-1);
					if (preferences.indent_statements_compare_to_block) {
						unIndent();
					}
					formatClosingBrace(preferences.brace_position_for_block);
					break;
				}
				case Token.tLPAREN:
					++parenLevel;
					print(currentToken.getLength(), hasWhitespace);
					if (parenLevel > 0) {
						for (int i= 0; i < preferences.continuation_indentation; i++) {
							indent();
						}
						if (column <= indentationLevel) {
							// HACK: avoid indent in same line
							column= indentationLevel + 1;
						}
					}
					break;
				case Token.tRPAREN:
					--parenLevel;
					if (parenLevel >= 0) {
						for (int i= 0; i < preferences.continuation_indentation; i++) {
							unIndent();
						}
					}
					print(currentToken.getLength(), hasWhitespace);
					break;
				case Token.tSEMI:
					print(currentToken.getLength(), preferences.insert_space_before_semicolon);
					break;
				case Token.t_catch:
				case Token.t_else:
					if (preferences.insert_new_line_before_else_in_if_statement) {
						printNewLine(currentToken.offset);
					} else {
						hasWhitespace= true;
					}
					print(currentToken.getLength(), hasWhitespace);
					break;
				default:
					if (currentToken.isVisibilityModifier()
							&& !preferences.indent_access_specifier_compare_to_type_header) {
						int indentLevel= indentationLevel;
						if (indentationLevel > 0)
							unIndent();
						print(currentToken.getLength(), hasWhitespace);
						while (indentationLevel < indentLevel) {
							indent();
						}
					} else {
						print(currentToken.getLength(), hasWhitespace);
					}
				}
				hasWhitespace= false;
			}
		} finally {
			scannerEndPosition= savedScannerEndPos;
			scanner.resetTo(startOffset + length, scannerEndPosition - 1);
			skipOverInactive= savedSkipOverInactive;
			preserveNewLines= savedPreserveNL;
		}
	}

	public void formatOpeningBrace(String bracePosition, boolean insertSpaceBeforeBrace) {
		if (DefaultCodeFormatterConstants.NEXT_LINE.equals(bracePosition)) {
			printNewLine();
		} else if (DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED.equals(bracePosition)) {
			printNewLine();
			indent();
		}
		printNextToken(Token.tLBRACE, insertSpaceBeforeBrace);

		printTrailingComment();
	}

	public void formatClosingBrace(String block_brace_position) {
		printNextToken(Token.tRBRACE);
		printTrailingComment();
		if (DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED.equals(block_brace_position)) {
			unIndent();
		}
	}

	private void print(int length, boolean considerSpaceIfAny) {
		if (checkLineWrapping && length + column > pageWidth) {
			handleLineTooLong();
		}
		lastNumberOfNewLines= 0;
		printIndentationIfNecessary();
		if (considerSpaceIfAny) {
			if (currentAlignment != null && currentAlignment.isIndentOnColumn(column)) {
				needSpace= true;
			}
			space();
		}
		if (pendingSpace) {
			addInsertEdit(scanner.getCurrentTokenStartPosition(), SPACE);
		}
		pendingSpace= false;
		column+= length;
		needSpace= true;
	}

	private void printBlockComment(boolean forceNewLine) {
		int currentTokenStartPosition= scanner.getCurrentTokenStartPosition();
		int currentTokenEndPosition= scanner.getCurrentTokenEndPosition() + 1;

		scanner.resetTo(currentTokenStartPosition, currentTokenEndPosition - 1);
		int currentCharacter;
		boolean isNewLine= false;
		int start= currentTokenStartPosition;
		int nextCharacterStart= currentTokenStartPosition;
		printIndentationIfNecessary();
		if (pendingSpace) {
			addInsertEdit(currentTokenStartPosition, SPACE);
		}
		needSpace= false;
		pendingSpace= false;
		int previousStart= currentTokenStartPosition;

		while (nextCharacterStart <= currentTokenEndPosition && (currentCharacter= scanner.getNextChar()) != -1) {
			nextCharacterStart= scanner.getCurrentPosition();

			switch (currentCharacter) {
			case '\r':
				if (isNewLine) {
					line++;
				}
				start= previousStart;
				isNewLine= true;
				if (scanner.getNextChar('\n')) {
					currentCharacter= '\n';
					nextCharacterStart= scanner.getCurrentPosition();
				}
				break;
			case '\n':
				if (isNewLine) {
					line++;
				}
				start= previousStart;
				isNewLine= true;
				break;
			default:
				if (isNewLine) {
					if (Character.isWhitespace((char) currentCharacter)) {
						int previousStartPosition= scanner.getCurrentPosition();
						while (currentCharacter != -1 && currentCharacter != '\r' && currentCharacter != '\n'
								&& Character.isWhitespace((char) currentCharacter)) {
							previousStart= nextCharacterStart;
							previousStartPosition= scanner.getCurrentPosition();
							currentCharacter= scanner.getNextChar();
							nextCharacterStart= scanner.getCurrentPosition();
						}
						if (currentCharacter == '\r' || currentCharacter == '\n') {
							nextCharacterStart= previousStartPosition;
						}
					}
					column= 1;
					line++;

					StringBuilder buffer= new StringBuilder();
					buffer.append(lineSeparator);
					printIndentationIfNecessary(buffer);
					buffer.append(' ');

					addReplaceEdit(start, previousStart - 1, String.valueOf(buffer));
				} else {
					column+= (nextCharacterStart - previousStart);
				}
				isNewLine= false;
			}
			previousStart= nextCharacterStart;
			scanner.setCurrentPosition(nextCharacterStart);
		}
		lastNumberOfNewLines= 0;
		needSpace= false;
		scanner.resetTo(currentTokenEndPosition, scannerEndPosition - 1);
		if (forceNewLine) {
			startNewLine();
		}
	}

	private void printPreprocessorDirective() {
		int currentTokenStartPosition= scanner.getCurrentTokenStartPosition();
		int currentTokenEndPosition= scanner.getCurrentTokenEndPosition() + 1;

		scanner.resetTo(currentTokenStartPosition, currentTokenEndPosition - 1);
		int currentCharacter;
		boolean isNewLine= false;
		int nextCharacterStart= currentTokenStartPosition;
		needSpace= false;
		pendingSpace= false;
		int previousStart= currentTokenStartPosition;

		while (nextCharacterStart <= currentTokenEndPosition && (currentCharacter= scanner.getNextChar()) != -1) {
			nextCharacterStart= scanner.getCurrentPosition();

			switch (currentCharacter) {
			case '\r':
				isNewLine= true;
				if (scanner.getNextChar('\n')) {
					currentCharacter= '\n';
					nextCharacterStart= scanner.getCurrentPosition();
				}
				break;
			case '\n':
				isNewLine= true;
				break;
			default:
				if (isNewLine) {
					column= 1;
					line++;
				} else {
					column+= (nextCharacterStart - previousStart);
				}
				isNewLine= false;
			}
			previousStart= nextCharacterStart;
			scanner.setCurrentPosition(nextCharacterStart);
		}
		lastNumberOfNewLines= isNewLine ? 1 : 0;
		needSpace= false;
		if (currentAlignment != null) {
			indentationLevel= currentAlignment.breakIndentationLevel;
		}
		scanner.resetTo(currentTokenEndPosition, scannerEndPosition - 1);
	}

	public void printEndOfTranslationUnit() {
		int currentTokenStartPosition= scanner.getCurrentPosition();
		if (currentTokenStartPosition <= scannerEndPosition) {
			printRaw(currentTokenStartPosition, scannerEndPosition - currentTokenStartPosition + 1);
		}
	}

	public boolean printComment() {
		// if we have a space between two tokens we ensure it will be dumped in
		// the formatted string
		int currentTokenStartPosition= scanner.getCurrentPosition();
		if (shouldSkip(currentTokenStartPosition)) {
			return false;
		}
		boolean hasComment= false;
		boolean hasLineComment= false;
		boolean hasWhitespace= false;
		int count= 0;
		while ((currentToken= scanner.nextToken()) != null) {
			if (skipOverInactive) {
				Position inactivePos= getInactivePosAt(scanner.getCurrentTokenStartPosition());
				if (inactivePos != null) {
					int startOffset= Math.min(scanner.getCurrentTokenStartPosition(), inactivePos.getOffset());
					int endOffset= Math.min(scannerEndPosition, inactivePos.getOffset() + inactivePos.getLength());
					if (startOffset < endOffset) {
						int savedIndentLevel= indentationLevel;
						scanner.resetTo(scanner.getCurrentTokenStartPosition(), scanner.eofPosition - 1);
						printRaw(startOffset, endOffset - startOffset);
						while (indentationLevel > savedIndentLevel) {
							unIndent();
						}
						while (indentationLevel < savedIndentLevel) {
							indent();
						}
						scanner.resetTo(endOffset, scanner.eofPosition - 1);
						continue;
					}
				}
			}
			switch (currentToken.type) {
			case Token.tWHITESPACE:
				char[] whiteSpaces= scanner.getCurrentTokenSource();
				count= 0;
				for (int i= 0, max= whiteSpaces.length; i < max; i++) {
					switch (whiteSpaces[i]) {
					case '\r':
						if ((i + 1) < max) {
							if (whiteSpaces[i + 1] == '\n') {
								i++;
							}
						}
						count++;
						break;
					case '\n':
						count++;
					}
				}
				if (count == 0) {
					hasWhitespace= true;
					addDeleteEdit(scanner.getCurrentTokenStartPosition(), scanner.getCurrentTokenEndPosition());
				} else if (hasComment) {
					if (count == 1) {
						printNewLine(scanner.getCurrentTokenStartPosition());
					} else {
						preserveEmptyLines(count - 1, scanner.getCurrentTokenStartPosition());
					}
					addDeleteEdit(scanner.getCurrentTokenStartPosition(), scanner.getCurrentTokenEndPosition());
				} else if (hasLineComment) {
					preserveEmptyLines(count - 1, scanner.getCurrentTokenStartPosition());
					addDeleteEdit(scanner.getCurrentTokenStartPosition(), scanner.getCurrentTokenEndPosition());
				} else if (count != 0 && preferences.number_of_empty_lines_to_preserve != 0) {
					String preservedEmptyLines= getPreserveEmptyLines(count - 1);
					addReplaceEdit(scanner.getCurrentTokenStartPosition(), scanner.getCurrentTokenEndPosition(),
							preservedEmptyLines);
					hasWhitespace= preservedEmptyLines.length() == 0;
				} else {
					addDeleteEdit(scanner.getCurrentTokenStartPosition(), scanner.getCurrentTokenEndPosition());
					hasWhitespace= true;
				}
				currentTokenStartPosition= scanner.getCurrentPosition();
				break;
			case Token.tLINECOMMENT:
				if (count >= 1) {
					if (count > 1) {
						preserveEmptyLines(count - 1, scanner.getCurrentTokenStartPosition());
					} else if (count == 1) {
						printNewLine(scanner.getCurrentTokenStartPosition());
					}
				} else if (hasWhitespace) {
					space();
				}
				hasWhitespace= false;
				printCommentLine();
				currentTokenStartPosition= scanner.getCurrentPosition();
				hasLineComment= true;
				count= 0;
				break;
			case Token.tBLOCKCOMMENT:
				if (count >= 1) {
					if (count > 1) {
						preserveEmptyLines(count - 1, scanner.getCurrentTokenStartPosition());
					} else if (count == 1) {
						printNewLine(scanner.getCurrentTokenStartPosition());
					}
				} else if (hasWhitespace) {
					space();
				}
				hasWhitespace= false;
				printBlockComment(false);
				currentTokenStartPosition= scanner.getCurrentPosition();
				hasLineComment= false;
				hasComment= true;
				count= 0;
				break;
			case Token.tPREPROCESSOR:
			case Token.tPREPROCESSOR_DEFINE:
			case Token.tPREPROCESSOR_INCLUDE:
				if (column != 1)
					printNewLine(scanner.getCurrentTokenStartPosition());
				if (count >= 1) {
					if (count > 1) {
						preserveEmptyLines(count - 1, scanner.getCurrentTokenStartPosition());
					} else if (count == 1) {
						// printNewLine(scanner.getCurrentTokenStartPosition());
					}
				}
				hasWhitespace= false;
				printPreprocessorDirective();
				printNewLine();
				currentTokenStartPosition= scanner.getCurrentPosition();
				hasLineComment= false;
				hasComment= false;
				count= 0;
				break;
			default:
				// step back one token
				scanner.resetTo(currentTokenStartPosition, scannerEndPosition - 1);
				return hasWhitespace;
			}
		}
		return hasWhitespace;
	}

	/**
	 * @param offset
	 * @return
	 */
	private Position getInactivePosAt(int offset) {
		for (Iterator<Position> iter= fSkipPositions.iterator(); iter.hasNext();) {
			Position pos= iter.next();
			if (pos.includes(offset)) {
				return pos;
			}
		}
		return null;
	}

	private void printCommentLine() {
		int currentTokenStartPosition= scanner.getCurrentTokenStartPosition();
		int currentTokenEndPosition= scanner.getCurrentTokenEndPosition() + 1;
		scanner.resetTo(currentTokenStartPosition, currentTokenEndPosition - 1);
		int currentCharacter;
		int start= currentTokenStartPosition;
		int nextCharacterStart= currentTokenStartPosition;
		printIndentationIfNecessary();
		if (pendingSpace) {
			addInsertEdit(currentTokenStartPosition, SPACE);
		}
		needSpace= false;
		pendingSpace= false;
		int previousStart= currentTokenStartPosition;

		loop: while (nextCharacterStart <= currentTokenEndPosition && (currentCharacter= scanner.getNextChar()) != -1) {
			nextCharacterStart= scanner.getCurrentPosition();

			switch (currentCharacter) {
			case '\r':
				start= previousStart;
				break loop;
			case '\n':
				start= previousStart;
				break loop;
			}
			previousStart= nextCharacterStart;
		}
		if (start != currentTokenStartPosition) {
			addReplaceEdit(start, currentTokenEndPosition - 1, lineSeparator);
		}
		line++;
		column= 1;
		needSpace= false;
		pendingSpace= false;
		lastNumberOfNewLines= 0;
		scanner.resetTo(currentTokenEndPosition, scannerEndPosition - 1);
		// realign to the proper value
		if (currentAlignment != null) {
			if (memberAlignment != null) {
				// select the last alignment
				if (currentAlignment.location.inputOffset > memberAlignment.location.inputOffset) {
					if (currentAlignment.couldBreak() && currentAlignment.wasSplit) {
						currentAlignment.performFragmentEffect();
					}
				} else {
					indentationLevel= Math.max(indentationLevel, memberAlignment.breakIndentationLevel);
				}
			} else if (currentAlignment.couldBreak() && currentAlignment.wasSplit) {
				currentAlignment.performFragmentEffect();
			}
		}
	}

	public void printEmptyLines(int linesNumber) {
		printEmptyLines(linesNumber, scanner.getCurrentTokenEndPosition() + 1);
	}

	private void printEmptyLines(int linesNumber, int insertPosition) {
		final String buffer= getEmptyLines(linesNumber);
		if (EMPTY_STRING == buffer)
			return;

		addInsertEdit(insertPosition, buffer);
	}

	void printIndentationIfNecessary() {
		StringBuilder buffer= new StringBuilder();
		printIndentationIfNecessary(buffer);
		if (buffer.length() > 0) {
			addInsertEdit(scanner.getCurrentTokenStartPosition(), buffer.toString());
			pendingSpace= false;
		}
	}

	private void printIndentationIfNecessary(StringBuilder buffer) {
		switch (tabChar) {
		case DefaultCodeFormatterOptions.TAB:
			boolean useTabsForLeadingIndents= useTabsOnlyForLeadingIndents;
			int numberOfLeadingIndents= numberOfIndentations;
			int indentationsAsTab= 0;
			if (useTabsForLeadingIndents) {
				while (column <= indentationLevel) {
					if (indentationsAsTab < numberOfLeadingIndents) {
						buffer.append('\t');
						indentationsAsTab++;
						lastNumberOfNewLines= 0;
						int complement= tabLength - ((column - 1) % tabLength); // amount
																				// of
																				// space
						column+= complement;
						needSpace= false;
					} else {
						buffer.append(' ');
						column++;
						needSpace= false;
					}
				}
			} else {
				while (column <= indentationLevel) {
					buffer.append('\t');
					lastNumberOfNewLines= 0;
					int complement= tabLength - ((column - 1) % tabLength); // amount
																			// of
																			// space
					column+= complement;
					needSpace= false;
				}
			}
			break;
		case DefaultCodeFormatterOptions.SPACE:
			while (column <= indentationLevel) {
				buffer.append(' ');
				column++;
				needSpace= false;
			}
			break;
		case DefaultCodeFormatterOptions.MIXED:
			useTabsForLeadingIndents= useTabsOnlyForLeadingIndents;
			numberOfLeadingIndents= numberOfIndentations;
			indentationsAsTab= 0;
			if (useTabsForLeadingIndents) {
				final int columnForLeadingIndents= numberOfLeadingIndents * indentationSize;
				while (column <= indentationLevel) {
					if (column <= columnForLeadingIndents) {
						if ((column - 1 + tabLength) <= indentationLevel) {
							buffer.append('\t');
							column+= tabLength;
						} else if ((column - 1 + indentationSize) <= indentationLevel) {
							// print one indentation
							for (int i= 0, max= indentationSize; i < max; i++) {
								buffer.append(' ');
								column++;
							}
						} else {
							buffer.append(' ');
							column++;
						}
					} else {
						for (int i= column, max= indentationLevel; i <= max; i++) {
							buffer.append(' ');
							column++;
						}
					}
					needSpace= false;
				}
			} else {
				while (column <= indentationLevel) {
					if ((column - 1 + tabLength) <= indentationLevel) {
						buffer.append('\t');
						column+= tabLength;
					} else if ((column - 1 + indentationSize) <= indentationLevel) {
						// print one indentation
						for (int i= 0, max= indentationSize; i < max; i++) {
							buffer.append(' ');
							column++;
						}
					} else {
						buffer.append(' ');
						column++;
					}
					needSpace= false;
				}
			}
			break;
		}
	}

	public void startNewLine() {
		if (column > 1) {
			printNewLine();
		}
	}
	public void printNewLine() {
		printNewLine(scanner.getCurrentTokenEndPosition() + 1);
	}

	public void printNewLine(int insertPosition) {
		if (shouldSkip(insertPosition - 1)) {
			return;
		}
		if (lastNumberOfNewLines >= 1) {
			column= 1; // ensure that the scribe is at the beginning of a new line
			return;
		}
		addInsertEdit(insertPosition, lineSeparator);
		line++;
		lastNumberOfNewLines= 1;
		column= 1;
		needSpace= false;
		pendingSpace= false;
	}

	public void printNextToken(int expectedTokenType) {
		printNextToken(expectedTokenType, false);
	}

	public void printNextToken(int expectedTokenType, boolean considerSpaceIfAny) {
		printComment();
		if (shouldSkip(scanner.getCurrentPosition())) {
			return;
		}
		currentToken= scanner.nextToken();
		if (currentToken == null || expectedTokenType != currentToken.type) {
			if (pendingSpace) {
				addInsertEdit(scanner.getCurrentTokenStartPosition(), SPACE);
			}
			pendingSpace= false;
			needSpace= true;
			throw new AbortFormatting(
					"["	+ (line+1) + "/" + column + "] unexpected token type, expecting:" + expectedTokenType + ", actual:" + currentToken);//$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
		print(currentToken.getLength(), considerSpaceIfAny);
	}

	public void printNextToken(int[] expectedTokenTypes) {
		printNextToken(expectedTokenTypes, false);
	}

	public void printNextToken(int[] expectedTokenTypes, boolean considerSpaceIfAny) {
		printComment();
		if (shouldSkip(scanner.getCurrentPosition())) {
			return;
		}
		currentToken= scanner.nextToken();
		if (Arrays.binarySearch(expectedTokenTypes, currentToken.type) < 0) {
			if (pendingSpace) {
				addInsertEdit(scanner.getCurrentTokenStartPosition(), SPACE);
			}
			pendingSpace= false;
			needSpace= true;
			StringBuilder expectations= new StringBuilder(5);
			for (int i= 0; i < expectedTokenTypes.length; i++) {
				if (i > 0) {
					expectations.append(',');
				}
				expectations.append(expectedTokenTypes[i]);
			}
			throw new AbortFormatting(
					"["	+ (line+1) + "/" + column + "] unexpected token type, expecting:[" + expectations.toString() + "], actual:" + currentToken);//$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
		print(currentToken.getLength(), considerSpaceIfAny);
	}

	private void printRule(StringBuilder stringBuffer) {
		for (int i= 0; i < pageWidth; i++) {
			if ((i % tabLength) == 0) {
				stringBuffer.append('+');
			} else {
				stringBuffer.append('-');
			}
		}
		stringBuffer.append(lineSeparator);

		for (int i= 0; i < (pageWidth / tabLength); i++) {
			stringBuffer.append(i);
			stringBuffer.append('\t');
		}
	}

	public void printTrailingComment() {
		// if we have a space between two tokens we ensure it will be dumped in
		// the formatted string
		int currentTokenStartPosition= scanner.getCurrentPosition();
		if (shouldSkip(currentTokenStartPosition)) {
			return;
		}
		boolean hasWhitespaces= false;
		boolean hasComment= false;
		boolean hasLineComment= false;
		int count= 0;
		while ((currentToken= scanner.nextToken()) != null) {
			switch (currentToken.type) {
			case Token.tWHITESPACE:
				char[] whiteSpaces= scanner.getCurrentTokenSource();
				for (int i= 0, max= whiteSpaces.length; i < max; i++) {
					switch (whiteSpaces[i]) {
					case '\r':
						if ((i + 1) < max) {
							if (whiteSpaces[i + 1] == '\n') {
								i++;
							}
						}
						count++;
						break;
					case '\n':
						count++;
					}
				}
				if (hasLineComment) {
					if (count >= 1) {
						currentTokenStartPosition= scanner.getCurrentTokenStartPosition();
						preserveEmptyLines(count - 1, currentTokenStartPosition);
						addDeleteEdit(currentTokenStartPosition, scanner.getCurrentTokenEndPosition());
						scanner.resetTo(scanner.getCurrentPosition(), scannerEndPosition - 1);
						return;
					} else {
						scanner.resetTo(currentTokenStartPosition, scannerEndPosition - 1);
						return;
					}
				} else if (count >= 1) {
					if (hasComment) {
						printNewLine(scanner.getCurrentTokenStartPosition());
					}
					scanner.resetTo(currentTokenStartPosition, scannerEndPosition - 1);
					return;
				} else {
					hasWhitespaces= true;
					currentTokenStartPosition= scanner.getCurrentPosition();
					addDeleteEdit(scanner.getCurrentTokenStartPosition(), scanner.getCurrentTokenEndPosition());
				}
				break;
			case Token.tLINECOMMENT:
				if (hasWhitespaces) {
					space();
				}
				printCommentLine();
				currentTokenStartPosition= scanner.getCurrentPosition();
				hasLineComment= true;
				break;
			case Token.tBLOCKCOMMENT:
				if (hasWhitespaces) {
					space();
				}
				printBlockComment(false);
				currentTokenStartPosition= scanner.getCurrentPosition();
				hasComment= true;
				break;
			case Token.tPREPROCESSOR:
			case Token.tPREPROCESSOR_DEFINE:
			case Token.tPREPROCESSOR_INCLUDE:
				if (column != 1)
					printNewLine(scanner.getCurrentTokenStartPosition());
				hasWhitespaces= false;
				printPreprocessorDirective();
				startNewLine();
				currentTokenStartPosition= scanner.getCurrentPosition();
				hasLineComment= false;
				hasComment= false;
				count= 0;
				break;
			default:
				// step back one token
				scanner.resetTo(currentTokenStartPosition, scannerEndPosition - 1);
				return;
			}
		}
	}

	void redoAlignment(AlignmentException e) {
		if (e.relativeDepth > 0) { // if exception targets a distinct context
			e.relativeDepth--; // record fact that current context got
								// traversed
			currentAlignment= currentAlignment.enclosing; // pop currentLocation
			throw e; // rethrow
		}
		// reset scribe/scanner to restart at this given location
		resetAt(currentAlignment.location);
		scanner.resetTo(currentAlignment.location.inputOffset, scanner.eofPosition - 1);
		// clean alignment chunkKind so it will think it is a new chunk again
		currentAlignment.chunkKind= 0;
		currentAlignmentException= null;
	}

	void redoMemberAlignment(AlignmentException e) {
		// reset scribe/scanner to restart at this given location
		resetAt(memberAlignment.location);
		scanner.resetTo(memberAlignment.location.inputOffset, scanner.eofPosition - 1);
		// clean alignment chunkKind so it will think it is a new chunk again
		memberAlignment.chunkKind= 0;
		currentAlignmentException= null;
	}

	public void reset() {
		checkLineWrapping= true;
		line= 0;
		column= 1;
		editsIndex= 0;
	}

	private void resetAt(Location location) {
		line= location.outputLine;
		column= location.outputColumn;
		indentationLevel= location.outputIndentationLevel;
		numberOfIndentations= location.numberOfIndentations;
		lastNumberOfNewLines= location.lastNumberOfNewLines;
		needSpace= location.needSpace;
		pendingSpace= location.pendingSpace;
		editsIndex= location.editsIndex;
		if (editsIndex > 0) {
			edits[editsIndex - 1]= location.textEdit;
		}
	}

	private void resize() {
		System.arraycopy(edits, 0, (edits= new OptimizedReplaceEdit[editsIndex * 2]), 0, editsIndex);
	}

	public void space() {
		if (!needSpace)
			return;
		if (shouldSkip(scanner.getCurrentPosition())) {
			return;
		}
		lastNumberOfNewLines= 0;
		pendingSpace= true;
		column++;
		needSpace= false;
	}

	@Override
	public String toString() {
		StringBuilder buffer= new StringBuilder();
		buffer.append("(page width = " + pageWidth + ") - (tabChar = ");//$NON-NLS-1$//$NON-NLS-2$
		switch (tabChar) {
		case DefaultCodeFormatterOptions.TAB:
			buffer.append("TAB");//$NON-NLS-1$
			break;
		case DefaultCodeFormatterOptions.SPACE:
			buffer.append("SPACE");//$NON-NLS-1$
			break;
		default:
			buffer.append("MIXED");//$NON-NLS-1$
		}
		buffer
			.append(") - (tabSize = " + tabLength + ")")//$NON-NLS-1$//$NON-NLS-2$
			.append(lineSeparator)
			.append(
					"(line = " + line + ") - (column = " + column + ") - (identationLevel = " + indentationLevel + ")") //$NON-NLS-1$	//$NON-NLS-2$	//$NON-NLS-3$	//$NON-NLS-4$
			.append(lineSeparator)
			.append(
					"(needSpace = " + needSpace + ") - (lastNumberOfNewLines = " + lastNumberOfNewLines + ") - (checkLineWrapping = " + checkLineWrapping + ")") //$NON-NLS-1$	//$NON-NLS-2$	//$NON-NLS-3$	//$NON-NLS-4$
			.append(lineSeparator).append(
					"==================================================================================") //$NON-NLS-1$
			.append(lineSeparator);
		printRule(buffer);
		return buffer.toString();
	}

	public void unIndent() {
		if (shouldSkip(scanner.getCurrentPosition())) {
			fSkippedIndentations--;
			return;
		}
		indentationLevel-= indentationSize;
		numberOfIndentations--;
	}

	/**
	 */
	public boolean printModifiers() {
		int currentTokenStartPosition= scanner.getCurrentPosition();
		if (shouldSkip(currentTokenStartPosition)) {
			return false;
		}
		boolean isFirstModifier= true;
		boolean hasComment= false;
		while ((currentToken= scanner.nextToken()) != null) {
			switch (currentToken.type) {
			case Token.t_typedef:
			case Token.t_extern:
			case Token.t_static:
			case Token.t_auto:
			case Token.t_register:
			case Token.t_const:
			case Token.t_signed:
			case Token.t_unsigned:
			case Token.t_volatile:
			case Token.t_virtual:
			case Token.t_mutable:
			case Token.t_explicit:
			case Token.t_friend:
			case Token.t_inline:
			case Token.t_restrict:
				print(currentToken.getLength(), !isFirstModifier);
				isFirstModifier= false;
				currentTokenStartPosition= scanner.getCurrentPosition();
				break;
			case Token.tBLOCKCOMMENT:
				printBlockComment(false);
				currentTokenStartPosition= scanner.getCurrentPosition();
				hasComment= true;
				break;
			case Token.tLINECOMMENT:
				printCommentLine();
				currentTokenStartPosition= scanner.getCurrentPosition();
				break;
			case Token.tWHITESPACE:
				addDeleteEdit(scanner.getCurrentTokenStartPosition(), scanner.getCurrentTokenEndPosition());
				int count= 0;
				char[] whiteSpaces= scanner.getCurrentTokenSource();
				for (int i= 0, max= whiteSpaces.length; i < max; i++) {
					switch (whiteSpaces[i]) {
					case '\r':
						if ((i + 1) < max) {
							if (whiteSpaces[i + 1] == '\n') {
								i++;
							}
						}
						count++;
						break;
					case '\n':
						count++;
					}
				}
				if (count >= 1 && hasComment) {
					printNewLine();
				}
				currentTokenStartPosition= scanner.getCurrentPosition();
				hasComment= false;
				break;
			case Token.tPREPROCESSOR:
			case Token.tPREPROCESSOR_DEFINE:
			case Token.tPREPROCESSOR_INCLUDE:
				if (column != 1)
					printNewLine(scanner.getCurrentTokenStartPosition());
				printPreprocessorDirective();
				printNewLine();
				currentTokenStartPosition= scanner.getCurrentPosition();
				hasComment= false;
				break;
			default:
				if (currentToken.getType() == Token.tIDENTIFIER) {
					if (currentToken.getText().startsWith("__")) { //$NON-NLS-1$
						// assume this is a declspec modifier
						print(currentToken.getLength(), !isFirstModifier);
						isFirstModifier= false;
						currentTokenStartPosition= scanner.getCurrentPosition();
						if ((currentToken= scanner.nextToken()) != null) {
							if (currentToken.getType() == Token.tLPAREN) {
								if (skipToToken(Token.tRPAREN)) {
									currentToken= scanner.nextToken();
									currentToken= scanner.nextToken();
									currentTokenStartPosition= scanner.getCurrentPosition();
								}
							}
						}
						break;
					}
				}
				// step back one token
				scanner.resetTo(currentTokenStartPosition, scannerEndPosition - 1);
				return !isFirstModifier;
			}
		}
		return !isFirstModifier;
	}

	public boolean preserveNewLine() {
		boolean savedPreserveNewLines= preserveNewLines;
		try {
			preserveNewLines= true;
			return printComment();
		} finally {
			preserveNewLines= savedPreserveNewLines;
		}
	}

	/**
	 * Skip to the next occurrence of the given token type.
	 * If successful, the next token will be the epxected token,
	 * otherwise the scanner position is left unchanged.
	 * 
	 * @param expectedTokenType
	 * @return  <code>true</code> if a matching token was skipped to
	 */
	public boolean skipToToken(int expectedTokenType) {
		int skipStart= scanner.getCurrentPosition();
		if (shouldSkip(skipStart)) {
			return true;
		}
		int braceLevel= 0;
		int parenLevel= 0;
		switch (expectedTokenType) {
		case Token.tRBRACE:
			++braceLevel;
			break;
		case Token.tRPAREN:
			++parenLevel;
			break;
		}
		while ((currentToken= scanner.nextToken()) != null) {
			switch (currentToken.type) {
			case Token.tLBRACE:
				if (expectedTokenType != Token.tLBRACE) {
					++braceLevel;
				}
				break;
			case Token.tRBRACE:
				--braceLevel;
				break;
			case Token.tLPAREN:
				if (expectedTokenType != Token.tLPAREN) {
					++parenLevel;
				}
				break;
			case Token.tRPAREN:
				--parenLevel;
				break;
			case Token.tWHITESPACE:
			case Token.tLINECOMMENT:
			case Token.tBLOCKCOMMENT:
			case Token.tPREPROCESSOR:
			case Token.tPREPROCESSOR_DEFINE:
			case Token.tPREPROCESSOR_INCLUDE:
				continue;
			}
			if (braceLevel <= 0 && parenLevel <= 0) {
				if (currentToken.type == expectedTokenType) {
					int tokenStart= scanner.getCurrentTokenStartPosition();
					printRaw(skipStart, tokenStart - skipStart);
					scanner.resetTo(tokenStart, scannerEndPosition - 1);
					return true;
				}
			}
			if (braceLevel < 0 || parenLevel < 0) {
				break;
			}
		}
		scanner.resetTo(skipStart, scannerEndPosition - 1);
		return false;
	}

	public boolean printCommentPreservingNewLines() {
		final boolean savedPreserveNL= this.preserveNewLines;
		this.preserveNewLines= true;
		try {
			return printComment();
		} finally {
			this.preserveNewLines= savedPreserveNL;
		}
	}

	boolean shouldSkip(int offset) {
		return offset >= fSkipStartOffset;
	}

	void skipRange(int offset, int endOffset) {
		if (offset == fSkipStartOffset) {
			return;
		}
		final int currentPosition= scanner.getCurrentPosition();
		if (offset > currentPosition) {
			printRaw(currentPosition, currentPosition - offset);
		}
		fSkipStartOffset= offset;
		fSkipEndOffset= endOffset;
	}

	boolean skipRange() {
		return fSkipEndOffset > 0;
	}

	void restartAtOffset(int offset) {
		final int currentPosition= scanner.getCurrentPosition();
		if (fSkipEndOffset > 0) {
			fSkipStartOffset= Integer.MAX_VALUE;
			fSkipEndOffset= 0;
			while (fSkippedIndentations < 0) {
				unIndent();
				fSkippedIndentations++;
			}
			if (offset > currentPosition) {
				printRaw(currentPosition, offset - currentPosition);
				scanner.resetTo(offset, scannerEndPosition - 1);
			}
			while (fSkippedIndentations > 0) {
				indent();
				fSkippedIndentations--;
			}
		} else if (offset > currentPosition) {
			boolean hasSpace= printComment();
			final int nextPosition= scanner.getCurrentPosition();
			if (offset > nextPosition) {
				if (hasSpace) {
					space();
				}
				printRaw(nextPosition, offset - nextPosition);
				scanner.resetTo(offset, scannerEndPosition - 1);
			}
		}

	}

}
