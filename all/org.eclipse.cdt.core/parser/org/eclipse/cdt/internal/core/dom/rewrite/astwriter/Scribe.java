/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.astwriter;

/**
 * 
 * This class is responsible for the string concatination and the management of
 * the indentations.
 * 
 * @since 5.0
 * @author Emanuel Graf IFS
 * 
 */
public class Scribe {
	
	
	private int indentationLevel = 0;
	private int indentationSize = 4; //HSR tcorbat: could be a tab character too - this is not a very elegant solution
	private StringBuffer buffer = new StringBuffer();
	private boolean isAtLineBeginning = true;
	private String newLine = System.getProperty("line.separator"); //$NON-NLS-1$
	private String givenIndentation = null;

	private boolean noNewLine = false;
	private boolean noSemicolon = false;
	
	public void newLine(){
		if(!noNewLine) {
			isAtLineBeginning = true;
			buffer.append(getNewline());
		}
	}
	
	private void indent(){
		if( givenIndentation != null ){
			buffer.append( givenIndentation );
		}
		printSpaces(indentationLevel * indentationSize);
	}
	
	private void indentIfNewLine(){
		if(isAtLineBeginning){
			isAtLineBeginning = false;
			indent();
		}
	}
	
	private String getNewline(){
		return newLine;
	}
	
	public void print(String code){
		indentIfNewLine();
		buffer.append(code);
	}
	
	public void println(String code) {
		print(code);
		newLine();
	}
	
	public void print(String code, String code2) {
		print(code);
		buffer.append(code2);
	}
	
	public void println(String code, String code2) {
		print(code, code2);
		newLine();
	}
	
	public void println(String code , char[] code2) {
		print(code);
		buffer.append(code2);
		newLine();
	}
	
	public void printSpaces(int number){
		indentIfNewLine();
		for(int i = 0; i < number; ++i){
			printSpace();
		}
	}
	
	public void noSemicolon() {
		noSemicolon = true;
	}
	
	public void printSemicolon(){
		if(!noSemicolon) {
			indentIfNewLine();
			buffer.append(';');
		}
		else {
			noSemicolon = false;
		}
	}
	
	@Override
	public String toString(){
		return buffer.toString();
	}
	
	public void print (char code) {
		indentIfNewLine();
		buffer.append(code);
	}

	public void print(char[] code) {
		indentIfNewLine();
		buffer.append(code);
	}
	
	public void println(char[] code) {
		print(code);
		newLine();
	}
	
	public void printStringSpace(String code){
		print(code);
		printSpace();
	}

	/**
	 * Prints a { to the Buffer an increases the Indentationlevel.
	 */
	public void printLBrace() {
		print('{');
		++indentationLevel;
	}

	/**
	 * Prints a } to the Buffer an decrease the Indentationlevel.
	 */
	public void printRBrace() {
		--indentationLevel;
		print('}');
	}
	
	public void incrementIndentationLevel(){
		++indentationLevel;
	}
	
	public void decrementIndentationLevel(){
		if(indentationLevel>0) {
			--indentationLevel;
		}
	}
	
	protected void noNewLines(){
		noNewLine = true;
	}
	
	protected void newLines(){
		noNewLine = false;
	}
	
	public void newLine(int i) {
		while(i > 0) {
			newLine();
			--i;
		}
	}

	public void printSpace() {
		buffer.append(' ');		
	}

	public String getGivenIndentation() {
		return givenIndentation;
	}

	public void setGivenIndentation(String givenIndentation) {
		this.givenIndentation = givenIndentation;
	}

	public void cleanCache() {
		buffer = new StringBuffer();	
	}
}
