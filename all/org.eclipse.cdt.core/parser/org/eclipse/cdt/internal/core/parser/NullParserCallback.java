package org.eclipse.cdt.internal.core.parser;

public class NullParserCallback implements IParserCallback {
	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#beginArguments()
	 */
	public Object argumentsBegin( Object container ) { 
		return null; 
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#beginClass(String, Token)
	 */
	public Object classSpecifierBegin(Object container, Token classKey) {
		return null; 
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#beginDeclarator()
	 */
	public Object declaratorBegin(Object container) {
		return null; 
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#beginFunctionBody()
	 */
	public void functionBodyBegin() {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#beginInclusion(String)
	 */
	public void inclusionBegin(String includeFile, int offset) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#beginSimpleDeclaration(Token)
	 */
	public Object simpleDeclarationBegin(Object Container) {
		return null; 
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#beginTranslationUnit()
	 */
	public Object translationUnitBegin() {
		return null; 
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#declaratorId(Token)
	 */
	public void declaratorId(Object declarator) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#declSpecifier(Token)
	 */
	public void simpleDeclSpecifier(Object Container, Token specifier) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#endArguments()
	 */
	public void argumentsEnd(Object parameterDeclarationClause) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#endClass()
	 */
	public void classSpecifierEnd(Object classSpecifier) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#endDeclarator()
	 */
	public void declaratorEnd(Object declarator) { 
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#endFunctionBody()
	 */
	public void functionBodyEnd() {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#endInclusion()
	 */
	public void inclusionEnd() {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#endSimpleDeclaration(Token)
	 */
	public void simpleDeclarationEnd(Object declaration) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#endTranslationUnit()
	 */
	public void translationUnitEnd(Object unit) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#expressionOperator(Token)
	 */
	public void expressionOperator(Token operator) throws Exception {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#expressionTerminal(Token)
	 */
	public void expressionTerminal(Token terminal) throws Exception {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#macro(String)
	 */
	public void macro(String macroName, int offset) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#nameBegin(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void nameBegin(Token firstToken) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#nameEnd(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void nameEnd(Token lastToken) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#classSpecifierName()
	 */
	public void classSpecifierName(Object classSpecifier) {
	}
	
	public Object baseSpecifierBegin( Object classSpecifier )
	{
		return null; 
	}

	public void baseSpecifierEnd( Object x )
	{
	}
	
	public void baseSpecifierName( Object baseSpecifier )
	{
	}
	
	public void baseSpecifierVisibility( Object baseSpecifier, Token visibility )
	{
	}

	public void baseSpecifierVirtual( Object baseSpecifier, boolean virtual )
	{
	}

	public Object parameterDeclarationBegin( Object container )
	{
		return null; 
	}
	
	public void  parameterDeclarationEnd( Object declaration ){
	}
	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#declaratorAbort(java.lang.Object, java.lang.Object)
	 */
	public void declaratorAbort(Object container, Object declarator) {
	}

}
