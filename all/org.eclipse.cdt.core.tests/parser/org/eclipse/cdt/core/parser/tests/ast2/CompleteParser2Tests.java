/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.ILabel;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.dom.parser.ISourceCodeParser;
import org.eclipse.cdt.core.dom.parser.c.ANSICParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.GCCParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.ANSICPPParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.cdt.internal.core.dom.parser.c.GNUCSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GNUCPPSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.parser.ParserException;
import org.eclipse.core.runtime.Platform;

/**
 * @author jcamelon
 */
public class CompleteParser2Tests extends BaseTestCase {

    private static final NullLogService NULL_LOG = new NullLogService();
    
    public CompleteParser2Tests() {
	}
	public CompleteParser2Tests(String name) {
		super(name);
	}

    public static TestSuite suite() {
    	return suite(CompleteParser2Tests.class);
    }

	static private class CPPNameCollector extends CPPASTVisitor {
        {
            shouldVisitNames = true;
        }
        public List nameList = new ArrayList();
        @Override
		public int visit( IASTName name ){
            nameList.add( name );
            return PROCESS_CONTINUE;
        }
        public IASTName getName( int idx ){
            if( idx < 0 || idx >= nameList.size() )
                return null;
            return (IASTName) nameList.get( idx );
        }
        public int size() { return nameList.size(); } 
    }
    static protected class CNameCollector extends CASTVisitor {
        {
            shouldVisitNames = true;
        }
        public List nameList = new ArrayList();
        @Override
		public int visit( IASTName name ){
            nameList.add( name );
            return PROCESS_CONTINUE;
        }
        public IASTName getName( int idx ){
            if( idx < 0 || idx >= nameList.size() )
                return null;
            return (IASTName) nameList.get( idx );
        }
        public int size() { return nameList.size(); } 
    }
    protected void assertInstances( CPPNameCollector nameCollector, IBinding binding, int num ) throws Exception {
        int count = 0;
        for( int i = 0; i < nameCollector.size(); i++ )
            if( nameCollector.getName( i ).resolveBinding() == binding )
                count++;
        
        assertEquals( count, num );
    }
    protected void assertInstances( CNameCollector nameCollector, IBinding binding, int num ) throws Exception {
        int count = 0;
        for( int i = 0; i < nameCollector.size(); i++ )
            if( nameCollector.getName( i ).resolveBinding() == binding )
                count++;
        
        assertEquals( count, num );
    }
    protected IASTTranslationUnit parse(String code, boolean expectedToPass,
            ParserLanguage lang) throws Exception {
        return parse(code, expectedToPass, lang, false);
    }

    protected IASTTranslationUnit parse(String code, boolean expectedToPass) throws Exception {
        return parse(code, expectedToPass, ParserLanguage.CPP);
    }

    /**
     * @param code
     */
    protected IASTTranslationUnit parse(String code) throws Exception {
        return parse(code, true, ParserLanguage.CPP);
    }
    
    /**
     * @param string
     * @param b
     * @param c
     * @param d
     */
    protected IASTTranslationUnit parse(String code, boolean expectedToPass,
            ParserLanguage lang, boolean gcc) throws Exception {

        
        CodeReader codeReader = new CodeReader(code
                .toCharArray());
        ScannerInfo scannerInfo = new ScannerInfo();
        ISourceCodeParser parser2 = null;
        IScanner scanner= AST2BaseTest.createScanner(codeReader, lang, ParserMode.COMPLETE_PARSE, scannerInfo);
        if (lang == ParserLanguage.CPP) {
            ICPPParserExtensionConfiguration config = null;
            if (gcc)
                config = new GPPParserExtensionConfiguration();
            else
                config = new ANSICPPParserExtensionConfiguration();
            parser2 = new GNUCPPSourceParser(scanner, ParserMode.COMPLETE_PARSE,
                    NULL_LOG, config);
        } else {
            ICParserExtensionConfiguration config = null;
            if (gcc)
                config = new GCCParserExtensionConfiguration();
            else
                config = new ANSICParserExtensionConfiguration();

            parser2 = new GNUCSourceParser(scanner, ParserMode.COMPLETE_PARSE,
                     NULL_LOG, config);
        }
        IASTTranslationUnit tu = parser2.parse();
        if (parser2.encounteredError() && expectedToPass)
            throw new ParserException("FAILURE"); //$NON-NLS-1$
        if (expectedToPass)
        {
            if( lang == ParserLanguage.C )
            {
            	IASTProblem [] problems = CVisitor.getProblems(tu);
            	assertEquals( problems.length, 0 );
            }
            else if ( lang == ParserLanguage.CPP )
            {
            	IASTProblem [] problems = CPPVisitor.getProblems(tu);
            	assertEquals( problems.length, 0 );
            }
        }
        return tu;
    }
    
    public void testEmptyCompilationUnit() throws Exception
    {
    	parse( "// no real code "); //$NON-NLS-1$
    }
    
    public void testSimpleNamespace() throws Exception
    {
    	IASTTranslationUnit tu = parse( "namespace A { }"); //$NON-NLS-1$
    	CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		assertEquals( col.size(), 1 );
		assertTrue( col.getName(0).resolveBinding() instanceof ICPPNamespace );
    }

	public void testMultipleNamespaceDefinitions() throws Exception
	{
	    IASTTranslationUnit tu = parse( "namespace A { } namespace A { }"); //$NON-NLS-1$
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		assertEquals( col.size(), 2 );
		ICPPNamespace A = (ICPPNamespace) col.getName(0).resolveBinding();
		assertInstances( col, A, 2 );
	}

    public void testNestedNamespaceDefinitions() throws Exception
    {
        IASTTranslationUnit tu = parse( "namespace A { namespace B { } }"); //$NON-NLS-1$
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		assertEquals( col.size(), 2 );
		ICPPNamespace A = (ICPPNamespace) col.getName(0).resolveBinding();
		ICPPNamespace B = (ICPPNamespace) col.getName(1).resolveBinding();
		
		assertSame( A.getNamespaceScope(), B.getNamespaceScope().getParent() );
    }
    
    public void testEmptyClassDeclaration() throws Exception
    {
        IASTTranslationUnit tu = parse( "class A { };"); //$NON-NLS-1$
        CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		assertEquals( col.size(), 1 );
		assertTrue( col.getName(0).resolveBinding() instanceof ICPPClassType );
    }
    
    public void testSimpleSubclass() throws Exception
    {
        IASTTranslationUnit tu = parse( "class A { };  class B : public A { };"); //$NON-NLS-1$
        CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		assertEquals( col.size(), 3 );
		ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
		ICPPClassType B = (ICPPClassType) col.getName(1).resolveBinding();
		
		assertInstances( col, A, 2 );
		
		assertEquals( B.getBases().length, 1 );
		ICPPBase base = B.getBases()[0];
		assertSame( base.getBaseClass(), A );
		assertEquals( base.getVisibility(), ICPPBase.v_public );
		assertFalse( base.isVirtual() );
    }
    
    public void testNestedSubclass() throws Exception
    {
        IASTTranslationUnit tu = parse( "namespace N { class A { }; } class B : protected virtual N::A { };"); //$NON-NLS-1$
        CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		assertEquals( col.size(), 6 );
		ICPPNamespace N = (ICPPNamespace) col.getName(0).resolveBinding();
		ICPPClassType A = (ICPPClassType) col.getName(1).resolveBinding();
		ICPPClassType B = (ICPPClassType) col.getName(2).resolveBinding();
		
		assertInstances( col, N, 2 );
		assertInstances( col, A, 3 );
		assertInstances( col, B, 1 );
		
		assertSame( A.getScope(), N.getNamespaceScope() );
		
		ICPPBase base = B.getBases()[0];
		assertSame( base.getBaseClass(), A );
		assertTrue( base.isVirtual() );
		assertEquals( base.getVisibility(), ICPPBase.v_protected );
    }
    
    public void testSimpleVariable() throws Exception
    {
        IASTTranslationUnit tu = parse( "int x;"); //$NON-NLS-1$
        CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 1 );
 		IVariable x = (IVariable) col.getName(0).resolveBinding();
 		
 		assertTrue( x.getType() instanceof IBasicType );
 		IBasicType t = (IBasicType) x.getType();
 		assertEquals( t.getType(), IBasicType.t_int );
    }
    
	public void testSimpleClassReferenceVariable() throws Exception
	{
	    IASTTranslationUnit tu = parse( "class A { }; A x;"); //$NON-NLS-1$
        CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 3 );
 		ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
 		IVariable x = (IVariable) col.getName(2).resolveBinding();
 		
 		assertInstances( col, A, 2 );
 		assertSame( x.getType(), A );
	}
    
	public void testNestedClassReferenceVariable() throws Exception
	{
	    IASTTranslationUnit tu = parse( "namespace N { class A { }; } N::A x;"); //$NON-NLS-1$
        CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 6 );
 		ICPPNamespace N = (ICPPNamespace) col.getName(0).resolveBinding();
 		ICPPClassType A = (ICPPClassType) col.getName(1).resolveBinding();
 		IVariable x =  (IVariable) col.getName(5).resolveBinding();
 		
 		assertInstances( col, N, 2 );
 		assertInstances( col, A, 3 );
 		assertSame( x.getType(), A );
 		assertSame( A.getScope(), N.getNamespaceScope() );
	}
	
	public void testMultipleDeclaratorsVariable() throws Exception
	{
	    IASTTranslationUnit tu = parse( "class A { }; A x, y, z;"); //$NON-NLS-1$
        CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 5 );
 		ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
 		IVariable x = (IVariable) col.getName(2).resolveBinding();
 		IVariable y = (IVariable) col.getName(3).resolveBinding();
 		IVariable z = (IVariable) col.getName(4).resolveBinding();
 		
 		assertInstances( col, A, 2 );
 		assertSame( A, x.getType() );
 		assertSame( x.getType(), y.getType() );
 		assertSame( y.getType(), z.getType() );
	}
	
	public void testSimpleField() throws Exception
	{
	    IASTTranslationUnit tu = parse( "class A { double x; };"); //$NON-NLS-1$
        CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 2 );
 		ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
 		ICPPField x = (ICPPField) col.getName(1).resolveBinding();
 		
 		assertSame( x.getScope(), A.getCompositeScope() );
 		IField [] fields = A.getFields();
 		assertEquals( fields.length, 1 );
 		assertSame( fields[0], x );
 	}
	
	public void testUsingClauses() throws Exception
	{
	    IASTTranslationUnit tu = parse( "namespace A { namespace B { int x;  class C { static int y = 5; }; } } \n " + //$NON-NLS-1$
	    		                        "using namespace A::B;\n " + //$NON-NLS-1$
	    		                        "using A::B::x;" + //$NON-NLS-1$
	    		                        "using A::B::C;" + //$NON-NLS-1$
	    		                        "using A::B::C::y;"); //$NON-NLS-1$
        CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 21 );
 		ICPPNamespace A = (ICPPNamespace) col.getName(0).resolveBinding();
 		ICPPNamespace B = (ICPPNamespace) col.getName(1).resolveBinding();
 		IVariable x =  (IVariable) col.getName(2).resolveBinding();
 		ICPPClassType C =  (ICPPClassType) col.getName(3).resolveBinding();
 		ICPPField y = (ICPPField) col.getName(4).resolveBinding();
 		
 		ICPPUsingDeclaration using_x = (ICPPUsingDeclaration) col.getName(11).resolveBinding();
 		ICPPUsingDeclaration using_C = (ICPPUsingDeclaration) col.getName(15).resolveBinding();
 		ICPPUsingDeclaration using_y = (ICPPUsingDeclaration) col.getName(20).resolveBinding();
 		
 		assertInstances( col, A, 5 );
 		assertInstances( col, B, 6 );
 		assertInstances( col, x, 1 );
 		assertInstances( col, C, 2 );
 		assertInstances( col, y, 1 );
 		
 		IBinding [] ds = using_x.getDelegates();
 		assertSame( ds[0], x );
 		assertSame( using_C.getDelegates()[0], C );
 		assertSame( using_y.getDelegates()[0], y );
	}
	
	public void testEnumerations() throws Exception
	{
	    IASTTranslationUnit tu = parse( "namespace A { enum E { e1, e2, e3 }; E varE;}"); //$NON-NLS-1$
        CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 7 );
 		ICPPNamespace A = (ICPPNamespace) col.getName(0).resolveBinding();
 		IEnumeration E = (IEnumeration) col.getName(1).resolveBinding();
 		IEnumerator e1 = (IEnumerator) col.getName(2).resolveBinding();
 		IEnumerator e2 = (IEnumerator) col.getName(3).resolveBinding();
 		IEnumerator e3 = (IEnumerator) col.getName(4).resolveBinding();
 		IVariable varE = (IVariable) col.getName(6).resolveBinding();
 		
 		assertInstances( col, E, 2 );
 		assertSame( E.getScope(), A.getNamespaceScope() );
 		assertSame( e1.getScope(), A.getNamespaceScope() );
 		assertNotNull( e2 );
 		assertNotNull( e3 );
 		assertNotNull( varE );
	}
	
	public void testSimpleFunction() throws Exception
	{
	    IASTTranslationUnit tu = parse( "void foo( void );"); //$NON-NLS-1$
        CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 2 );
 		IFunction foo = (IFunction) col.getName(0).resolveBinding();
 		IParameter p =  (IParameter) col.getName(1).resolveBinding();
 		
 		assertEquals( foo.getParameters().length, 1 );
 		assertSame( foo.getParameters()[0], p );
 		assertSame( p.getScope(), foo.getFunctionScope() );
	}
	
	public void testSimpleFunctionWithTypes() throws Exception
	{
	    IASTTranslationUnit tu = parse( "class A { public: \n class B { }; }; const A::B &  foo( A * myParam );"); //$NON-NLS-1$
        CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 8 );
 		ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
 		ICPPClassType B = (ICPPClassType) col.getName(1).resolveBinding();
 		IFunction foo = (IFunction) col.getName(5).resolveBinding();
 		IParameter p = (IParameter) col.getName(7).resolveBinding();
 		
 		assertInstances( col, A, 3 );
 		assertInstances( col, B, 3 );
 		
 		IFunctionType ftype = foo.getType();
 		assertTrue( ftype.getReturnType() instanceof ICPPReferenceType );
 		ICPPReferenceType rt = (ICPPReferenceType) ftype.getReturnType();
 		assertTrue( rt.getType() instanceof IQualifierType );
 		assertSame( ((IQualifierType)rt.getType()).getType(), B );
 		
 		IType pt = ftype.getParameterTypes()[0];
 		assertTrue( p.getType().isSameType( pt ) );
 		assertTrue( pt instanceof IPointerType );
 		assertSame( ((IPointerType) pt).getType(), A );
	}
	
	public void testSimpleMethod() throws Exception
	{
	    IASTTranslationUnit tu = parse( "class A { void foo(); };"); //$NON-NLS-1$
        CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 2 );
 		ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
 		ICPPMethod foo = (ICPPMethod) col.getName(1).resolveBinding();
 		
 		assertSame( foo.getScope(), A.getCompositeScope() );
	}
	
	public void testSimpleMethodWithTypes() throws Exception
	{
	    IASTTranslationUnit tu = parse( "class U { }; class A { U foo( U areDumb ); };"); //$NON-NLS-1$
        CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 6 );
 		ICPPClassType U = (ICPPClassType) col.getName(0).resolveBinding();
 		ICPPClassType A = (ICPPClassType) col.getName(1).resolveBinding();
 		ICPPMethod foo = (ICPPMethod) col.getName(3).resolveBinding();
 		IParameter p = (IParameter) col.getName(5).resolveBinding();
 		
 		assertInstances( col, U, 3 );
 		assertSame( foo.getScope(), A.getCompositeScope() );
 		IFunctionType ft = foo.getType();
 		assertSame( ft.getReturnType(), U );
 		assertSame( p.getType(), U );
	}
	
	public void testUsingDeclarationWithFunctionsAndMethods() throws Exception
	{
		IASTTranslationUnit tu = parse( "namespace N { int foo(void); } class A { static int bar(void); }; using N::foo; using ::A::bar;" ); //$NON-NLS-1$
        CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 12 );
 		ICPPNamespace N = (ICPPNamespace) col.getName(0).resolveBinding();
 		IFunction foo = (IFunction) col.getName(1).resolveBinding();
 		ICPPClassType A = (ICPPClassType) col.getName(3).resolveBinding();
 		ICPPMethod bar = (ICPPMethod) col.getName(4).resolveBinding();
 		
 		ICPPUsingDeclaration using_foo = (ICPPUsingDeclaration) col.getName(8).resolveBinding();
 		ICPPUsingDeclaration using_bar = (ICPPUsingDeclaration) col.getName(11).resolveBinding();
 		
 		assertInstances( col, N, 2 );
 		assertInstances( col, foo, 1 );
 		assertInstances( col, A, 2 );
 		assertInstances( col, bar, 1 );
 		
 		assertSame( using_foo.getDelegates()[0], foo );
 		assertSame( using_bar.getDelegates()[0], bar );
	}
	
	public void testLinkageSpec() throws Exception
	{
		IASTTranslationUnit tu = parse( "extern \"C\" { int foo(); }"); //$NON-NLS-1$
        CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 1 );
 		IFunction foo = (IFunction) col.getName(0).resolveBinding();
 		assertNotNull( foo );
	}
	

	public void testBogdansExample() throws Exception
	{
		IASTTranslationUnit tu = parse( "namespace A { namespace B {	enum e1{e_1,e_2};	int x;	class C	{	static int y = 5;	}; }} "); //$NON-NLS-1$
        CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 8 );
 		ICPPNamespace A = (ICPPNamespace) col.getName(0).resolveBinding();
 		ICPPNamespace B = (ICPPNamespace) col.getName(1).resolveBinding();
 		IEnumeration e1 = (IEnumeration) col.getName(2).resolveBinding();
 		IEnumerator e_1 = (IEnumerator) col.getName(3).resolveBinding();
 		IEnumerator e_2 = (IEnumerator) col.getName(4).resolveBinding();
 		IVariable x = (IVariable) col.getName(5).resolveBinding();
 		ICPPClassType C = (ICPPClassType) col.getName(6).resolveBinding();
 		ICPPField y = (ICPPField) col.getName(7).resolveBinding();
 		
 		assertSame( B.getScope(), A.getNamespaceScope() );
 		assertSame( e1.getScope(), B.getNamespaceScope() );
 		assertSame( e_1.getScope(), B.getNamespaceScope() );
 		assertSame( e_2.getType(), e1 );
 		assertNotNull( x );
 		assertNotNull( C );
 		assertNotNull( y );
	}
	
	public void testAndrewsExample() throws Exception
	{
		IASTTranslationUnit tu = parse( "namespace N{ class A {}; }	using namespace N;	class B: public A{};"); //$NON-NLS-1$
        CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 5 );
 		ICPPNamespace N = (ICPPNamespace) col.getName(0).resolveBinding();
 		ICPPClassType A = (ICPPClassType) col.getName(1).resolveBinding();
 		ICPPClassType B = (ICPPClassType) col.getName(3).resolveBinding();
 		
 		assertInstances( col, N, 2 );
 		assertInstances( col, A, 2 );
 		
 		ICPPBase base = B.getBases()[0];
 		assertSame( base.getBaseClass(), A );
	}
	
	public void testSimpleTypedef() throws Exception
	{
		IASTTranslationUnit tu = parse( "typedef int myInt;\n myInt var;"); //$NON-NLS-1$
        CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 3 );
 		ITypedef myInt = (ITypedef) col.getName(0).resolveBinding();
 		IVariable var = (IVariable) col.getName(2).resolveBinding();
 		
 		assertInstances( col, myInt, 2 );
 		assertTrue( myInt.getType() instanceof IBasicType );
 		assertSame( var.getType(), myInt );
	}
	
	public void testComplexTypedef() throws Exception
	{
		IASTTranslationUnit tu = parse( "class A{ }; typedef A ** A_DOUBLEPTR;"); //$NON-NLS-1$
        CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 3 );
 		ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
 		ITypedef APTR = (ITypedef) col.getName(2).resolveBinding();
 		
 		assertInstances( col, A, 2 );
 		assertTrue( APTR.getType() instanceof IPointerType );
 		IPointerType pt = (IPointerType) APTR.getType();
 		assertTrue( pt.getType() instanceof IPointerType );
 		pt = (IPointerType) pt.getType();
 		assertSame( pt.getType(), A );
	}
	
	
	protected void assertQualifiedName(String [] fromAST, String [] theTruth)
	 {
		 assertNotNull( fromAST );
		 assertNotNull( theTruth );
		 assertEquals( fromAST.length, theTruth.length );
		 for( int i = 0; i < fromAST.length; ++i )
		 {
			 assertEquals( fromAST[i], theTruth[i]);
		 }
	 }

	public void testBug40842() throws Exception{
		Writer code = new StringWriter();		
		code.write("class A {} a;\n"); //$NON-NLS-1$
		IASTTranslationUnit tu = parse(code.toString());

		IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		IASTCompositeTypeSpecifier comp = (IASTCompositeTypeSpecifier) decl.getDeclSpecifier();
		ICPPClassType A = (ICPPClassType) comp.getName().resolveBinding();
		IVariable a = (IVariable) decl.getDeclarators()[0].getName().resolveBinding();
		assertSame( a.getType(), A );
	}
	
	public void testNestedClassname() throws Exception
	{
		IASTTranslationUnit tu = parse( "namespace A { } \n class A::B { };"); //$NON-NLS-1$
        CPPNameCollector col = new CPPNameCollector();
        tu.accept( col );
        
        ICPPNamespace A = (ICPPNamespace) col.getName(0).resolveBinding();
        ICPPClassType B = (ICPPClassType) col.getName(1).resolveBinding();
        assertInstances( col, A, 2 );
        assertEquals( B.getScope(), A.getNamespaceScope() );
	}
	
	public void testForwardDeclaration() throws Exception
	{
		IASTTranslationUnit tu = parse( "class forward;"); //$NON-NLS-1$
		IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		assertEquals( decl.getDeclarators().length, 0 );
		
		IASTElaboratedTypeSpecifier spec = (IASTElaboratedTypeSpecifier) decl.getDeclSpecifier();
		ICPPClassType forward = (ICPPClassType) spec.getName().resolveBinding();
		assertNotNull( forward );
	}
	
	public void testElaboratedType() throws Exception
	{
		IASTTranslationUnit tu = parse( "class A; class A * a;"); //$NON-NLS-1$
        CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 3 );
 		ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
 		IVariable a = (IVariable) col.getName(2).resolveBinding();
 		IPointerType ptr = (IPointerType) a.getType();
 		assertInstances( col, A, 2 );
 		assertSame( ptr.getType(), A );
	}
	
	public void testForewardDeclarationWithUsage() throws Exception
	{
		IASTTranslationUnit tu = parse( "class A; A * anA;class A { };"); //$NON-NLS-1$
        CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 4 );
 		ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
 		IVariable anA = (IVariable) col.getName(2).resolveBinding();
 		assertInstances( col, A, 3 );
 		IPointerType ptr = (IPointerType) anA.getType();
 		assertSame( ptr.getType(), A );
	}
		
	
	public void testASM() throws Exception
	{
		parse( "asm ( \"blah blah blah\" );" ); //$NON-NLS-1$
	}

	
	/** 
	 *  Tests GNU extensions to asm
	 *  e.g. asm volatile ("stuff");
	 *       asm ("addl %%ebx,%%eax" : "=a"(foo) : "a"(foo),"b"(bar) ); 
	 */
	public void testGNUASMExtension() throws Exception
	{
		// volatile keyword
		parse( "asm volatile( \"blah blah blah\" );", true, ParserLanguage.C, true ); //$NON-NLS-1$
		parse( "asm volatile( \"blah blah blah\" );", true, ParserLanguage.CPP, true ); //$NON-NLS-1$
		
		// Use of operands
		parse( "asm (\"addl  %%ebx,%%eax\" : \"=a\"(foo) :\"a\"(foo), \"b\"(bar) );", true, ParserLanguage.C, true );//$NON-NLS-1$
		parse( "asm (\"addl  %%ebx,%%eax\" : \"=a\"(foo) :\"a\"(foo), \"b\"(bar) );", true, ParserLanguage.CPP, true );//$NON-NLS-1$
		
		// Invalid use of operands
		parse( "asm (\"addl  %%ebx,%%eax\"  \"=a\"(foo) :\"a\"(foo) : \"b\"(bar) );", false, ParserLanguage.C, true );//$NON-NLS-1$
		parse( "asm (\"addl  %%ebx,%%eax\"  \"=a\"(foo) :\"a\"(foo) : \"b\"(bar) );", false, ParserLanguage.CPP, true );//$NON-NLS-1$

		// Code from bug 145389.
		parse("#define mb()  __asm__ __volatile__ (\"sync\" : : : \"memory\")\r\n" + 
				"\r\n" + 
				"int main(int argc, char **argv) {\r\n" + 
				"        mb();\r\n" + 
				"}");
		// Code from bug 117001
		parse("static inline long\r\n" + 
				"div_ll_X_l_rem(long long divs, long div, long *rem)\r\n" + 
				"{\r\n" + 
				"        long dum2;\r\n" + 
				"      __asm__(\"divl %2\":\"=a\"(dum2), \"=d\"(*rem) // syntax error indicated at \":\"\r\n" + 
				"      : \"rm\"(div), \"A\"(divs));\r\n" + 
				"\r\n" + 
				"        return dum2;\r\n" + 
				"\r\n" + 
				"}");
	}

	public void testOverride() throws Exception
	{
		IASTTranslationUnit tu = parse( "void foo();\n void foo( int );\n"); //$NON-NLS-1$
        CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 3 );
 		IFunction foo1 = (IFunction) col.getName(0).resolveBinding();
 		IFunction foo2 = (IFunction) col.getName(1).resolveBinding();
 		
 		assertNotSame( foo1, foo2 );
	}	 
	
	public void testSimpleExpression() throws Exception
	{
		IASTTranslationUnit tu = parse( "int x; int y = x;"); //$NON-NLS-1$
        CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 3 );
 		IVariable x = (IVariable) col.getName(0).resolveBinding();
 		IVariable y = (IVariable) col.getName(1).resolveBinding();
 		assertInstances( col, x, 2 );
 		assertNotNull( y );
	}
	
	public void testParameterExpressions() throws Exception
	{
		IASTTranslationUnit tu = parse( "int x = 5; void foo( int sub = x ) { }"); //$NON-NLS-1$
        CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 4 );
 		IVariable x = (IVariable) col.getName(0).resolveBinding();
 		assertInstances( col, x, 2 );
	}
	
	public void testNestedNamespaceExpression() throws Exception
	{
		IASTTranslationUnit tu = parse( "namespace A { int x = 666; } int y  = A::x;"); //$NON-NLS-1$
		CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 6 );
 		ICPPNamespace A = (ICPPNamespace) col.getName(0).resolveBinding();
 		IVariable x = (IVariable) col.getName(1).resolveBinding();
 		assertInstances( col, A, 2 );
 		assertInstances( col, x, 3 );
	}
	
	public void testConstructorChain() throws Exception
	{
		IASTTranslationUnit tu = parse( "int x = 5;\n class A \n{ public : \n int a; \n A() : a( x ) { } };");  //$NON-NLS-1$
		CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 6 );
 		IVariable x = (IVariable) col.getName(0).resolveBinding();
 		ICPPField a = (ICPPField) col.getName(2).resolveBinding();
 		ICPPConstructor A = (ICPPConstructor) col.getName(3).resolveBinding();
 		assertNotNull( A );
 		assertInstances( col, x, 2 );
 		assertInstances( col, a, 2 );
	}
	
	public void testArrayModExpression() throws Exception
	{
		IASTTranslationUnit tu = parse( "const int x = 5; int y [ x ]; "); //$NON-NLS-1$
		CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 3 );
 		IVariable x = (IVariable) col.getName(0).resolveBinding();
 		IVariable y = (IVariable) col.getName(1).resolveBinding();
 		assertInstances( col, x, 2 );
 		assertTrue( y.getType() instanceof IArrayType );
 		assertTrue( ((IArrayType)y.getType()).getType() instanceof IBasicType );
	}


	public void testPointerVariable() throws Exception
	{
		IASTTranslationUnit tu = parse( "class A { }; A * anA;"); //$NON-NLS-1$
		CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 3 );
 		ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
 		IVariable anA = (IVariable) col.getName(2).resolveBinding();
 		assertInstances( col, A, 2 );
 		assertTrue( anA.getType() instanceof IPointerType );
 		assertSame( ((IPointerType) anA.getType()).getType(), A );
	}	
	
	public void testExceptionSpecification() throws Exception
	{
		IASTTranslationUnit tu = parse( "class A { }; void foo( void ) throw ( A );"); //$NON-NLS-1$
		CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 4 );
 		ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
 		assertInstances( col, A, 2 );
	}
	 
	public void testNewExpressions() throws Exception {
		IASTTranslationUnit tu = parse( "typedef int A; int B; int C; int D; int P; int*p = new  (P) (A[B][C][D]);" ); //$NON-NLS-1$
		CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 11 );
 		ITypedef A = (ITypedef) col.getName(0).resolveBinding();
 		IVariable B = (IVariable) col.getName(1).resolveBinding();
 		IVariable C = (IVariable) col.getName(2).resolveBinding();
 		IVariable D = (IVariable) col.getName(3).resolveBinding();
 		IVariable P = (IVariable) col.getName(4).resolveBinding();
 		IVariable p = (IVariable) col.getName(5).resolveBinding();
 		
 		assertInstances( col, A, 2 );
 		assertInstances( col, B, 2 );
 		assertInstances( col, C, 2 );
 		assertInstances( col, D, 2 );
 		assertInstances( col, P, 2 );
 		
 		assertTrue( p.getType() instanceof IPointerType );
	}

	public void testBug41520() throws Exception 
	{
		IASTTranslationUnit tu = parse( "int f() { const int x = 666; const int y( x ); }"); //$NON-NLS-1$
		IASTCompoundStatement s = (IASTCompoundStatement) ((IASTFunctionDefinition)tu.getDeclarations()[0]).getBody();
        IASTDeclarationStatement ds = (IASTDeclarationStatement) s.getStatements()[1];
        
		IASTSimpleDeclaration decl = (IASTSimpleDeclaration) ds.getDeclaration();
		IASTDeclarator dtor = decl.getDeclarators()[0];
		assertFalse( dtor instanceof IASTFunctionDeclarator );
		assertNotNull( dtor.getInitializer() );
		
		CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 4 );
 		IVariable x = (IVariable) col.getName(1).resolveBinding();
 		IVariable y = (IVariable) col.getName(2).resolveBinding();
 		assertNotNull(y);
 		assertInstances( col, x, 2 );
	}
	
	public void testNewXReferences() throws Exception
	{
		IASTTranslationUnit tu = parse( "const int max = 5;\n int * x = new int[max];"); //$NON-NLS-1$
		CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 3 );
 		IVariable max = (IVariable) col.getName(0).resolveBinding();
 		assertInstances( col, max, 2 );
	}
	
	public void testQualifiedNameReferences() throws Exception
	{
		// Used to cause AST Semantic exception
		IASTTranslationUnit tu = parse( "class A{ class B{ class C { public: int cMethod(); }; }; }; \n  int A::B::C::cMethod() {}; \n" ); //$NON-NLS-1$
		CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 9 );
 		ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
 		ICPPClassType B = (ICPPClassType) col.getName(1).resolveBinding();
 		ICPPClassType C = (ICPPClassType) col.getName(2).resolveBinding();
 		ICPPMethod cMethod = (ICPPMethod) col.getName(3).resolveBinding();
 		
 		assertInstances( col, A, 2 );
 		assertInstances( col, B, 2 );
 		assertInstances( col, C, 2 );
 		assertInstances( col, cMethod, 3 );
 		assertEquals( cMethod.getVisibility(), ICPPMember.v_public );
 		assertSame( cMethod.getScope(), C.getCompositeScope() );
 		assertSame( C.getScope(), B.getCompositeScope() );
 		assertSame( B.getScope(), A.getCompositeScope() );
	}

	public void testIsConstructor() throws Exception
	{
		IASTTranslationUnit tu = parse( "class A{ public: A(); }; \n  A::A() {}; \n" ); //$NON-NLS-1$
		CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 5 );
 		ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
 		ICPPConstructor ctor = (ICPPConstructor) col.getName(1).resolveBinding();
 		
 		assertInstances( col, A, 2 );
 		assertInstances( col, ctor, 3 );
	}

	public void testIsDestructor() throws Exception
	{
		IASTTranslationUnit tu = parse( "class A{ public: ~A(); }; \n  A::~A() {}; \n" ); //$NON-NLS-1$
		CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 5 );
 		ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
 		ICPPMethod dtor = (ICPPMethod) col.getName(1).resolveBinding();
 		
 		assertInstances( col, A, 2 );
 		assertInstances( col, dtor, 3 );
	}
	
	public void testBug41445() throws Exception
	{
		IASTTranslationUnit tu = parse( "class A { }; namespace N { class B : public A { struct A {}; }; }"); //$NON-NLS-1$
		CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 5 );
 		ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
 		ICPPNamespace N = (ICPPNamespace) col.getName(1).resolveBinding();
 		ICPPClassType B = (ICPPClassType) col.getName(2).resolveBinding();
 		ICPPClassType A2 = (ICPPClassType) col.getName(4).resolveBinding();
 		
 		assertInstances( col, A, 2 );
 		assertNotSame( A, A2 );
 		assertSame( A2.getScope(), B.getCompositeScope() );
 		assertSame( B.getScope(), N.getNamespaceScope() );
 		assertSame( B.getBases()[0].getBaseClass(), A );
 		
	}
	
	public void testSimpleFunctionBody() throws Exception
	{
		IASTTranslationUnit tu = parse( "class A { int f1(); }; const int x = 4; int f() { return x; } int A::f1() { return x; }"); //$NON-NLS-1$
		CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 9 );
 		ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
 		ICPPMethod f1 = (ICPPMethod) col.getName(1).resolveBinding();
 		IVariable x = (IVariable) col.getName(2).resolveBinding();
 		
 		assertInstances( col, A, 2 );
 		assertInstances( col, f1, 3 );
 		assertInstances( col, x, 3 );
	}


	public void testSimpleForLoop() throws Exception
	{
		IASTTranslationUnit tu = parse( "const int FIVE = 5;  void f() {  int x = 0; for( int i = 0; i < FIVE; ++i ) { x += i; }  }"); //$NON-NLS-1$
		CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 9 );
 		IVariable FIVE = (IVariable) col.getName(0).resolveBinding();
 		IVariable x = (IVariable) col.getName(2).resolveBinding();
 		IVariable i = (IVariable) col.getName(3).resolveBinding();
 		
 		assertInstances( col, FIVE, 2 );
 		assertInstances( col, x, 2 );
 		assertInstances( col, i, 4 );
	}

	public void testBug42541() throws Exception
	{
		IASTTranslationUnit tu = parse( "union{ int v; char a; } id;" ); //$NON-NLS-1$
		CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 4 );
 		
 		ICPPClassType unnamed = (ICPPClassType) col.getName(0).resolveBinding();
 		ICPPField v = (ICPPField) col.getName(1).resolveBinding();
 		ICPPField a = (ICPPField) col.getName(2).resolveBinding();
 		IVariable id = (IVariable) col.getName(3).resolveBinding();
 		
 		assertEquals( unnamed.getKey(), ICompositeType.k_union );
 		assertSame( v.getScope(), unnamed.getCompositeScope() );
 		assertSame( a.getScope(), unnamed.getCompositeScope() );
 		assertSame( id.getType(), unnamed );
	}
	
	
	
	public void testSimpleIfStatement() throws Exception
	{
		IASTTranslationUnit tu =parse( "const bool T = true; int foo() { if( T ) { return 5; } else if( ! T ) return 20; else { return 10; } }"); //$NON-NLS-1$
		
		IASTFunctionDefinition foo = (IASTFunctionDefinition) tu.getDeclarations()[1];
		IASTCompoundStatement compound  = (IASTCompoundStatement) foo.getBody();
		IASTIfStatement ifstmt = (IASTIfStatement) compound.getStatements()[0];
		assertTrue( ifstmt.getConditionExpression() instanceof IASTIdExpression );
		assertTrue( ifstmt.getThenClause() instanceof IASTCompoundStatement );
		assertTrue( ifstmt.getElseClause() instanceof IASTIfStatement );
		ifstmt = (IASTIfStatement) ifstmt.getElseClause();
		assertTrue( ifstmt.getConditionExpression() instanceof IASTUnaryExpression );
		assertTrue( ifstmt.getThenClause() instanceof IASTReturnStatement );
		assertTrue( ifstmt.getElseClause() instanceof IASTCompoundStatement );
		
		CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 4 );
 		
 		IVariable T = (IVariable) col.getName(0).resolveBinding();
 		assertInstances( col, T, 3 );
	}
	
	public void testSimpleWhileStatement() throws Exception
	{
		IASTTranslationUnit tu = parse( "const bool T = true; void foo() { int x = 0; while( T ) {  ++x;  if( x == 100 ) break; } }"); //$NON-NLS-1$
		CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 6 );
 		IVariable T = (IVariable) col.getName(0).resolveBinding();
 		IVariable x = (IVariable) col.getName(2).resolveBinding();
 		assertInstances( col, T, 2 );
 		assertInstances( col, x, 3 );
	}
	
	public void testSimpleSwitchStatement() throws Exception
	{
		IASTTranslationUnit tu = parse( "const int x = 5; const int y = 10; " + //$NON-NLS-1$
										"void foo() {                       " + //$NON-NLS-1$
										"	while( true ) {                 " + //$NON-NLS-1$
										"      switch( x ) {                " + //$NON-NLS-1$
										"         case 1: break;            " + //$NON-NLS-1$
										"         case 2: goto blah;        " + //$NON-NLS-1$
										"         case y: continue;         " + //$NON-NLS-1$
										"         default: break;           " + //$NON-NLS-1$
										"      }                            " + //$NON-NLS-1$
										"   }                               " + //$NON-NLS-1$
										"   blah : ;                        " + //$NON-NLS-1$
										"}                                  "); //$NON-NLS-1$
		CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 7 );
 		IVariable x = (IVariable) col.getName(0).resolveBinding();
 		IVariable y = (IVariable) col.getName(1).resolveBinding();
 		ILabel blah = (ILabel) col.getName(4).resolveBinding();
 		assertNotNull( blah );
 		assertInstances( col, x, 2 );
 		assertInstances( col, y, 2 );
 		assertInstances( col, blah, 2 );
	}
	
	public void testSimpleDoStatement() throws Exception
	{
	    IASTTranslationUnit tu = parse( "const int x = 3; int counter = 0; void foo() { do { ++counter; } while( counter != x ); } "); //$NON-NLS-1$
		CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 6 );
 		IVariable x = (IVariable) col.getName(0).resolveBinding();
 		IVariable counter = (IVariable) col.getName(1).resolveBinding();
 		assertInstances( col, x, 2 );
 		assertInstances( col, counter, 3 );
	}
	
	public void testThrowStatement() throws Exception {
		IASTTranslationUnit tu = parse("class A { }; void foo() throw ( A ) { A a; throw a; throw; } "); //$NON-NLS-1$
		CPPNameCollector col = new CPPNameCollector();
		tu.accept(col);

		assertEquals(6, col.size());
		ICompositeType A = (ICompositeType) col.getName(0).resolveBinding();
		assertInstances(col, A, 3);

		IVariable a = (IVariable) col.getName(4).resolveBinding();
		assertInstances(col, a, 2);
	}
	
	public void testScoping() throws Exception
	{
	    IASTTranslationUnit tu = parse( "void foo() { int x = 3; if( x == 1 ) { int x = 4; } else int x = 2; }");  //$NON-NLS-1$
		CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 5 );
 		IVariable x1 = (IVariable) col.getName(1).resolveBinding();
 		IVariable x2 = (IVariable) col.getName(3).resolveBinding();
 		IVariable x3 = (IVariable) col.getName(4).resolveBinding();
 		
 		assertInstances( col, x1, 2 );
 		assertInstances( col, x2, 1 );
 		assertInstances( col, x3, 1 );
	}
	
	public void testEnumeratorReferences() throws Exception
	{
	    IASTTranslationUnit tu = parse( "enum E { e1, e2, e3 }; E anE = e1;"); //$NON-NLS-1$
		CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 7 );
 		IEnumeration E = (IEnumeration) col.getName(0).resolveBinding();
 		IEnumerator e1 = (IEnumerator) col.getName(1).resolveBinding();
 		IEnumerator e2 = (IEnumerator) col.getName(2).resolveBinding();
 		IEnumerator e3 = (IEnumerator) col.getName(3).resolveBinding();
 		IVariable anE =  (IVariable) col.getName(5).resolveBinding();
 		
 		assertInstances( col, E, 2 );
 		assertInstances( col, e1, 2 );
 		assertInstances( col, e2, 1 );
 		assertInstances( col, e3, 1 );
 		assertInstances( col, anE, 1 );
	}
	
	public void testBug42840() throws Exception
	{
	    IASTTranslationUnit tu = parse( "void foo(); void foo() { } class SearchMe { };"); //$NON-NLS-1$
		CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 3 );
 		IFunction foo = (IFunction) col.getName(0).resolveBinding();
 		
 		assertInstances( col, foo, 2 );
	}
	
	public void testBug42872() throws Exception
	{
	    IASTTranslationUnit tu = parse( "struct B {}; struct D : B {}; void foo(D* dp) { B* bp = dynamic_cast<B*>(dp); }" );  //$NON-NLS-1$
		CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 10 );
 		ICompositeType B = (ICompositeType) col.getName(0).resolveBinding();
 		ICompositeType D = (ICompositeType) col.getName(1).resolveBinding();
 		
 		assertInstances( col, B, 4 );
 		assertInstances( col, D, 2 );
	}
	
	public void testBug43503A() throws Exception {
	    IASTTranslationUnit tu = parse("class SD_01 { void f_SD_01() {}}; int main(){ SD_01 * a = new SD_01(); a->f_SD_01();	} "); //$NON-NLS-1$
		CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 8 );
 		ICPPClassType SD_01 = (ICPPClassType) col.getName(0).resolveBinding();
 		ICPPMethod f_SD_01 = (ICPPMethod) col.getName(1).resolveBinding();
 		ICPPConstructor ctor = SD_01.getConstructors()[0];
 		assertInstances( col, SD_01, 2 );
 		assertInstances( col, ctor, 1 );
 		assertInstances( col, f_SD_01, 2 );
	}	
	
	
	public void testBug42979() throws Exception
	{
		Writer code = new StringWriter(); 
		code.write( "class OperatorOverload{\n" ); //$NON-NLS-1$
		code.write( "public:\n" ); //$NON-NLS-1$
		code.write( "  bool operator==( const class OperatorOverload& that )\n" ); //$NON-NLS-1$
		code.write( "  { return true; }\n" ); //$NON-NLS-1$
		code.write( "  bool operator!=( const class OperatorOverload& that );\n" ); //$NON-NLS-1$
		code.write( "}; \n" ); //$NON-NLS-1$
  
		code.write( "bool OperatorOverload::operator!=( const class OperatorOverload& that )\n" ); //$NON-NLS-1$
		code.write( "{ return false; }\n" ); //$NON-NLS-1$

		IASTTranslationUnit tu = parse( code.toString() );
		CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 12 );
 		ICompositeType OperatorOverload = (ICompositeType) col.getName(0).resolveBinding();
 		ICPPMethod op1 = (ICPPMethod) col.getName(1).resolveBinding();
 		ICPPMethod op2 = (ICPPMethod) col.getName(4).resolveBinding();
 		
 		assertInstances( col, OperatorOverload, 5 );
 		assertInstances( col, op1, 1 );
 		assertInstances( col, op2, 3 );
	}
	/** 
	 * class A { static int x; } int A::x = 5;
	 */
	public void testBug43373() throws Exception
	{
	    IASTTranslationUnit tu = parse( "class A { static int x; }; int A::x = 5;" ); //$NON-NLS-1$
		CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 5 );
 		ICompositeType A = (ICompositeType) col.getName(0).resolveBinding();
 		ICPPField x = (ICPPField) col.getName(1).resolveBinding();
 		
 		assertInstances( col, A, 2 );
 		assertInstances( col, x, 3 );
	}
	
	public void testBug39504() throws Exception
	{
	    IASTTranslationUnit tu = parse( "const int w = 2; int x[ 5 ]; int y = sizeof ( x[w] );" ); //$NON-NLS-1$
		CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 5 );
 		IVariable w = (IVariable) col.getName(0).resolveBinding();
 		IVariable x = (IVariable) col.getName(1).resolveBinding();
 		
 		assertInstances( col, w, 2 );
 		assertInstances( col, x, 2 );		
	}
	
	public void testBug43375() throws Exception
	{
		parse( "extern int x;"); //$NON-NLS-1$
	}

	public void testBug43503() throws Exception
	{
		StringBuffer buff = new StringBuffer(); 
		
		buff.append( "class SD_02 {                "); //$NON-NLS-1$
		buff.append( "	public:                    "); //$NON-NLS-1$
		buff.append( "   void f_SD_02();           "); //$NON-NLS-1$
		buff.append( " };                          "); //$NON-NLS-1$
		buff.append( "class SD_01 {              \n"); //$NON-NLS-1$
		buff.append( " public:                   \n"); //$NON-NLS-1$
		buff.append( "   SD_02 *next;            \n"); //$NON-NLS-1$  // REFERENCE SD_02 
		buff.append( "   void f_SD_01();         \n"); //$NON-NLS-1$
		buff.append( "};                         \n"); //$NON-NLS-1$
		buff.append( "int main(){                \n"); //$NON-NLS-1$
		buff.append( "   SD_01* a = new SD_01(); \n"); //$NON-NLS-1$  // REFERENCE SD_01 * 2 
		buff.append( "   a->f_SD_01();           \n"); //$NON-NLS-1$  // REFERENCE a && REFERENCE f_SD_01 
		buff.append( "}                          \n"); //$NON-NLS-1$
		buff.append( "void SD_01::f_SD_01()      \n"); //$NON-NLS-1$ // REFERENCE SD_01 
		buff.append( "{                          \n"); //$NON-NLS-1$
		buff.append( "   next->f_SD_02();        \n"); //$NON-NLS-1$ // REFERENCE next && reference f_SD_02 
		buff.append( "}                          \n"); //$NON-NLS-1$
		IASTTranslationUnit tu = parse( buff.toString() );
		
		CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 17 );
 		ICompositeType SD_02 = (ICompositeType) col.getName(0).resolveBinding();
 		ICPPMethod f_SD_02 = (ICPPMethod) col.getName(1).resolveBinding();
 		ICPPClassType SD_01 = (ICPPClassType) col.getName(2).resolveBinding();
 		ICPPField next = (ICPPField) col.getName(4).resolveBinding();
 		ICPPMethod f_SD_01 = (ICPPMethod) col.getName(5).resolveBinding();
 		ICPPConstructor ctor = SD_01.getConstructors()[0];
 		
 		assertInstances( col, SD_02, 2 );
 		assertInstances( col, f_SD_02, 2 );
 		assertInstances( col, SD_01, 3 );
 		assertInstances( col, ctor, 1 );
 		assertInstances( col, next, 2 );
 		assertInstances( col, f_SD_01, 4 );
	}
		
	public void testBug43679_A () throws Exception
	{
	    IASTTranslationUnit tu = parse( "struct Sample { int size() const; }; extern const Sample * getSample(); int trouble() {  return getSample()->size(); } " ); //$NON-NLS-1$
		CPPNameCollector col = new CPPNameCollector();
 		tu.accept( col );
 		
 		assertEquals( col.size(), 7 );
 		ICompositeType sample = (ICompositeType) col.getName(0).resolveBinding();
 		ICPPMethod size = (ICPPMethod) col.getName(1).resolveBinding();
 		IFunction getSample = (IFunction) col.getName(3).resolveBinding();

 		assertInstances( col, sample, 2 );
 		assertInstances( col, size, 2 );
 		assertInstances( col, getSample, 2 );
	}

	public void testBug43679_B () throws Exception
	{ 
	    IASTTranslationUnit tu = parse( "struct Sample{int size() const; }; struct Sample; " ); //$NON-NLS-1$
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		assertEquals( col.size(), 3 );
		ICompositeType sample = (ICompositeType) col.getName(0).resolveBinding();
		ICPPMethod size = (ICPPMethod) col.getName(1).resolveBinding();
		
		assertInstances( col, sample, 2 );
		assertInstances( col, size, 1 );
	}
	
	public void testBug43951() throws Exception
	{
		IASTTranslationUnit tu = parse( "class B{ B(); ~B(); }; B::B(){} B::~B(){}" ); //$NON-NLS-1$
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		assertEquals( col.size(), 9 );
		ICPPClassType B = (ICPPClassType) col.getName(0).resolveBinding();
		ICPPConstructor constructor = (ICPPConstructor) col.getName(1).resolveBinding();
		ICPPMethod destructor = (ICPPMethod) col.getName(2).resolveBinding();
		
		assertInstances( col, B, 3 );
		assertInstances( col, constructor, 3 );
		assertInstances( col, destructor, 3 );
	}	

	public void testBug44342() throws Exception {
		IASTTranslationUnit tu = parse("class A { void f(){} void f(int){} }; int main(){ A * a = new A(); a->f();} "); //$NON-NLS-1$
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		assertEquals( col.size(), 10 );
		ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
		ICPPMethod f1 = (ICPPMethod) col.getName(1).resolveBinding();
		ICPPMethod f2 = (ICPPMethod) col.getName(2).resolveBinding();
		
		ICPPConstructor ctor = A.getConstructors()[0];
		IVariable a = (IVariable) col.getName( 6 ).resolveBinding();
		
		assertInstances( col, A, 2 );
		assertInstances( col, f1, 2 );
		assertInstances( col, f2, 1 );
		assertInstances( col, ctor, 1 );
		assertInstances( col, a, 2 );
	}	

	
	public void testCDesignatedInitializers() throws Exception
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append( "struct Inner { int a,b,c; };");  //$NON-NLS-1$
		buffer.append( "struct A { int x; int y[]; struct Inner innerArray[]; int z []; };"); //$NON-NLS-1$
		buffer.append( "struct A myA = { .x = 4, .y[3] = 4, .y[4] = 3, .innerArray[0].a = 3, .innerArray[1].b = 5, .innerArray[2].c=6, .z = { 1,4,5} };"); //$NON-NLS-1$
		parse( buffer.toString(), true, ParserLanguage.C );
	}
	
	public void testBug39551A() throws Exception
	{
		parse("extern float _Complex conjf (float _Complex);", true, ParserLanguage.C); //$NON-NLS-1$
	}

	public void testBug39551B() throws Exception
	{
	    //this used to be 99.99 * __I__, but I don't know where the __I__ came from, its not in C99, nor in GCC
		parse("_Imaginary double id = 99.99 * 1i;", true, ParserLanguage.C); //$NON-NLS-1$
	}
	
	public void testCBool() throws Exception
	{
		parse( "_Bool x;", true, ParserLanguage.C ); //$NON-NLS-1$
	}
	
	public void testCBoolAsParameter() throws Exception
	{
		parse( "void f( _Bool b ) {} " + //$NON-NLS-1$
							"_Bool g( _Bool b ) {} " + //$NON-NLS-1$
							"void main(){" + //$NON-NLS-1$
							"   _Bool b;  " + //$NON-NLS-1$
							"   f(b);" + //$NON-NLS-1$
							"	f( g( (_Bool) 1 )  );" + //$NON-NLS-1$
							"}",  //$NON-NLS-1$
							true, ParserLanguage.C );
	}
	
	public void testBug44510() throws Exception
	{
		IASTTranslationUnit tu = parse( "int initialize(); " + //$NON-NLS-1$
							"int initialize( char ){} " + //$NON-NLS-1$
							"int initialize(){ return 1; } " + //$NON-NLS-1$
							"void main(){ int i = initialize(); }" ); //$NON-NLS-1$
		
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		assertEquals( col.size(), 7 );
		IFunction init1 = (IFunction) col.getName(0).resolveBinding();
		IFunction init2 = (IFunction) col.getName(1).resolveBinding();
		
		assertInstances( col, init1, 3 );
		assertInstances( col, init2, 1 );
	}	
	
	public void testBug44925() throws Exception
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append( "class MyClass { };");  //$NON-NLS-1$
		buffer.append( "class MyClass myObj1;"); //$NON-NLS-1$
		buffer.append( "enum MyEnum { Item1 };"); //$NON-NLS-1$
		buffer.append( "enum MyEnum myObj2;"); //$NON-NLS-1$
		IASTTranslationUnit tu = parse( buffer.toString() );	
		
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		assertEquals( col.size(), 7 );
		ICPPClassType myClass = (ICPPClassType) col.getName(0).resolveBinding();
		IVariable obj1 = (IVariable) col.getName(2).resolveBinding();
		IEnumeration myEnum = (IEnumeration) col.getName(3).resolveBinding();
		IEnumerator item = (IEnumerator) col.getName(4).resolveBinding();
		IVariable obj2 = (IVariable)col.getName(6).resolveBinding();
		
		assertInstances( col, myClass, 2 );
		assertInstances( col, myEnum, 2 );
		assertSame( obj1.getType(), myClass );
		assertSame( obj2.getType(), myEnum );
		assertSame( item.getType(), myEnum );
	}
	
	public void testBug44838() throws Exception
	{
		StringBuffer buffer = new StringBuffer(); 
		buffer.append( "class A { int myX; A( int x ); };\n"); //$NON-NLS-1$
		buffer.append( "A::A( int x ) : myX( x ) { if( x == 5 ) myX++; }\n"); //$NON-NLS-1$
		IASTTranslationUnit tu = parse( buffer.toString() );	
		
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		assertEquals( col.size(), 12 );
		ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
		ICPPField myX = (ICPPField) col.getName(1).resolveBinding();
		ICPPConstructor ctor = (ICPPConstructor) col.getName(2).resolveBinding();
		IParameter x = (IParameter) col.getName(3).resolveBinding();
		
		assertInstances( col, A, 2 );
		assertInstances( col, myX, 3 );
		assertInstances( col, ctor, 3 );
		assertInstances( col, x, 4 );
	}
	
	public void testBug46165() throws Exception
	{
		StringBuffer buffer = new StringBuffer(); 
		buffer.append( "class A { int myX; A( int x ); };\n"); //$NON-NLS-1$
		buffer.append( "A::A( int x ) : myX( x ) { if( x == 5 ) myX++; }\n"); //$NON-NLS-1$
		parse( buffer.toString() ); 
	}

	public void testBug47624() throws Exception
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append( "struct s { }; \n" ); //$NON-NLS-1$
		buffer.append( "void f ( int s ) { \n" ); //$NON-NLS-1$
		buffer.append( "   struct s sInstance; \n" ); //$NON-NLS-1$
		buffer.append( "}\n"); //$NON-NLS-1$		
		IASTTranslationUnit tu = parse( buffer.toString() );	
		
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		assertEquals( col.size(), 5 );
		ICPPClassType s = (ICPPClassType) col.getName(0).resolveBinding();
		IParameter s2 = (IParameter) col.getName(2).resolveBinding();
		IVariable instance = (IVariable) col.getName(4).resolveBinding();

		assertInstances( col, s, 2 );
		assertInstances( col, s2, 1 );
		assertSame( instance.getType(), s );
	}
	
	public void testQualifiedLookup() throws Exception{
		//this is meant to test that on a->f, the lookup for f is qualified
		//the namespace is necessary because of bug 47926
		StringBuffer buffer = new StringBuffer();
		buffer.append( "namespace N {" ); //$NON-NLS-1$
		buffer.append( "   void f () {} \n" ); //$NON-NLS-1$
		buffer.append( "   class A { }; \n" ); //$NON-NLS-1$
		buffer.append( "}" ); //$NON-NLS-1$
		buffer.append( "void main() { N::A * a = new N::A();  a->f(); } "); //$NON-NLS-1$		
		IASTTranslationUnit tu = parse( buffer.toString() );	
		
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		assertEquals( col.size(), 13 );
		ICPPNamespace N = (ICPPNamespace) col.getName(0).resolveBinding();
		IFunction f = (IFunction) col.getName(1).resolveBinding();
		ICPPClassType A = (ICPPClassType) col.getName(2).resolveBinding();
		
		ICPPConstructor ctor = A.getConstructors()[0];
		
		IProblemBinding fp = (IProblemBinding) col.getName(12).resolveBinding();
		assertEquals( fp.getID(), IProblemBinding.SEMANTIC_NAME_NOT_FOUND );
		
		assertInstances( col, N, 3 );
		assertInstances( col, f, 1 );
		assertInstances( col, A, 3 );
		assertInstances( col, ctor, 2 );
	}
	
	public void testBug43110() throws Exception
	{
		StringBuffer buffer = new StringBuffer(); 
		buffer.append("void x( int y, ... );\n"); //$NON-NLS-1$
		buffer.append("void y( int x... );\n"); //$NON-NLS-1$
		buffer.append("void z(...);"); //$NON-NLS-1$
		IASTTranslationUnit tu = parse( buffer.toString() );	
		
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		assertEquals( col.size(), 5 );
		IFunction x = (IFunction) col.getName(0).resolveBinding();
		IFunction y = (IFunction) col.getName(2).resolveBinding();
		IFunction z = (IFunction) col.getName(4).resolveBinding();
		assertNotNull(x);
		assertNotNull(y);
		assertNotNull(z);
	}
	
	public void testBug43110_XRef() throws Exception
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append( "void foo( ... ) {}\n" ); //$NON-NLS-1$
		buffer.append( "void main( ){ foo( 1 ); }\n" ); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString() );	
		
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		assertEquals( col.size(), 3 );
		IFunction foo = (IFunction) col.getName(0).resolveBinding();
		assertInstances( col, foo, 2 );
	}
	
	public void testErrorHandling_1() throws Exception
	{
		IASTTranslationUnit tu = parse( "A anA; int x = c; class A {}; A * anotherA = &anA; int b;", false ); //$NON-NLS-1$
		
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		assertEquals( col.size(), 9 );
		IProblemBinding p = (IProblemBinding) col.getName(0).resolveBinding();
		IVariable anA = (IVariable) col.getName(1).resolveBinding();
		assertNotNull( col.getName(2).resolveBinding() );
		IProblemBinding p2 = (IProblemBinding) col.getName(3).resolveBinding();
		ICPPClassType A = (ICPPClassType) col.getName(4).resolveBinding();
		
		assertInstances( col, anA, 2 );
		assertInstances( col, A, 2 );
		
		assertNotNull( p );
		assertNotNull( p2 );
		
		assertSame( anA.getType(), p );
	}
	
	public void testBug44340() throws Exception {
		// inline function with reference to variables declared after them
		IASTTranslationUnit tu = parse ("class A{ int getX() {return x[1];} int x[10];};"); //$NON-NLS-1$
		
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		assertEquals( col.size(), 4 );
		
		ICPPField x = (ICPPField) col.getName(2).resolveBinding();
		assertInstances( col, x, 2 );
	}
	
	public void testBug47628() throws Exception
	{
		Writer writer = new StringWriter(); 
		writer.write( "void h(char) { }\n"); //$NON-NLS-1$
		writer.write( "void h(unsigned char) { }\n"); //$NON-NLS-1$
		writer.write( "void h(signed char) { }  // not shown in outline, parsed as char\n"); //$NON-NLS-1$
		
		parse( writer.toString() );
	}
	
	public void testBug47636() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "void f( char [] ); \n" ); //$NON-NLS-1$
		writer.write( "void f( char * ){} \n" ); //$NON-NLS-1$
		IASTTranslationUnit tu = parse( writer.toString() );		
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		assertEquals( col.size(), 4 );
		IFunction f1 = (IFunction) col.getName(0).resolveBinding();
		IParameter p1 = (IParameter)col.getName(1).resolveBinding();
		IFunction f2 = (IFunction) col.getName(2).resolveBinding();
		IParameter p2 = (IParameter)col.getName(3).resolveBinding();
		assertSame( f1, f2 );
		assertSame( p1, p2 );
	}
	
	public void testBug45697() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( " int f( bool ); \n"); //$NON-NLS-1$
		writer.write( " int f( char ){ } "); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( writer.toString() );		
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		assertEquals( col.size(), 4 );
		IFunction f1 = (IFunction) col.getName(0).resolveBinding();
		IParameter p1 = (IParameter)col.getName(1).resolveBinding();
		IFunction f2 = (IFunction) col.getName(2).resolveBinding();
		IParameter p2 = (IParameter)col.getName(3).resolveBinding();
		
		assertNotSame( f1, f2 );
		assertNotSame( p1, p2 );
	}

	public void testBug54639() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "typedef enum _A { } A, *pA; " ); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( writer.toString() );		
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		assertEquals( col.size(), 3 );
		
		IEnumeration _A = (IEnumeration) col.getName(0).resolveBinding();
		ITypedef A = (ITypedef) col.getName(1).resolveBinding();
		ITypedef pA = (ITypedef)col.getName(2).resolveBinding();
		
		assertNotNull( _A );
		assertSame( A.getType(), _A );
		assertTrue( pA.getType() instanceof IPointerType );
		assertSame( ((IPointerType)pA.getType()).getType(), _A );
	}
	
	public void testBug55163() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "void foo() { \n"); //$NON-NLS-1$
		writer.write( "   int i, n; \n"); //$NON-NLS-1$
		writer.write( "   double di; \n"); //$NON-NLS-1$
		writer.write( "   for( i = n - 1, di = (double)( i + i ); i > 0; i-- ){ } \n"); //$NON-NLS-1$
		writer.write( "}\n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( writer.toString() );		
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		assertEquals( col.size(), 11 );
		IVariable i = (IVariable)col.getName(1).resolveBinding();
		IVariable n = (IVariable)col.getName(2).resolveBinding();
		IVariable di = (IVariable)col.getName(3).resolveBinding();
		
		assertInstances( col, i, 6 );
		assertInstances( col, n, 2 );
		assertInstances( col, di, 2 );
	}
	public void testBug55673() throws Exception{
		Writer writer = new StringWriter();
		writer.write( "struct Example { int i;  int ( * pfi ) ( int ); }; "); //$NON-NLS-1$
		
		parse( writer.toString() );
		IASTTranslationUnit tu = parse( writer.toString() );		
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		assertEquals( col.size(), 4 );
		ICPPField pfi = (ICPPField)col.getName(2).resolveBinding();
		
		assertNotNull( pfi );
		assertTrue( pfi.getType() instanceof IPointerType );
		assertTrue( ((IPointerType)pfi.getType()).getType() instanceof IFunctionType );
	}
	
	public void testBug54531() throws Exception
	{
		parse( "typedef enum _A {} A, *pA;" ); //$NON-NLS-1$
	}
	
	public void testBug56516() throws Exception
	{
		IASTTranslationUnit tu = parse( "typedef struct blah sb;"); //$NON-NLS-1$		
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		assertEquals( col.size(), 2 );
		
		ICPPClassType blah = (ICPPClassType) col.getName(0).resolveBinding();
		ITypedef sb = (ITypedef) col.getName(1).resolveBinding();
		assertSame( sb.getType(), blah );
	}
	
	public void testBug53786() throws Exception
	{
		parse( "struct Example {  struct Data * data; };"); //$NON-NLS-1$
	}
	
	public void testBug54029() throws Exception
	{
		parse( "typedef int T; T i;" ); //$NON-NLS-1$
	}

	public void testBug47625() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write("struct s { int num; }; "); //$NON-NLS-1$
		writer.write("namespace ns{ "); //$NON-NLS-1$
		writer.write("   struct s { double num; };"); //$NON-NLS-1$
		writer.write("   s inner = { 3.14 };"); //$NON-NLS-1$
		writer.write("   ::s outer = { 42 };"); //$NON-NLS-1$
		writer.write("}"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( writer.toString() );
		
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );

		assertEquals( col.size(), 10 );
		
		ICPPClassType s = (ICPPClassType) col.getName(0).resolveBinding();
		ICPPClassType s2 = (ICPPClassType) col.getName(3).resolveBinding();
		
		ICPPClassType ref1 = (ICPPClassType) col.getName(5).resolveBinding();
		ICPPClassType ref2 = (ICPPClassType) col.getName( 8 ).resolveBinding();
		
		assertSame( s, ref2 );
		assertSame( s2, ref1 );
	}
	
	public void testBug57754() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "struct X {          " ); //$NON-NLS-1$
		writer.write( "   typedef int T;   " ); //$NON-NLS-1$
		writer.write( "   void f( T );     " ); //$NON-NLS-1$
		writer.write( "};                  " ); //$NON-NLS-1$
		writer.write( "void X::f( T ) { }  " ); //$NON-NLS-1$
		IASTTranslationUnit tu = parse( writer.toString() );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		assertEquals( col.size(), 10 );
		ICPPClassType X = (ICPPClassType) col.getName(0).resolveBinding();
		ITypedef T = (ITypedef) col.getName(1).resolveBinding();
		ICPPMethod f = (ICPPMethod) col.getName(2).resolveBinding();
		
		assertInstances( col, X, 2 );
		assertInstances( col, T, 3 );
		assertInstances( col, f, 3 );
	}	
	
	public void testBug57800() throws Exception
	{
		Writer writer= new StringWriter();
		writer.write( "class G2 { int j; };"); //$NON-NLS-1$
		writer.write( "typedef G2 AltG2;"); //$NON-NLS-1$
		writer.write( "class AltG3 : AltG2 {  int x;};"); //$NON-NLS-1$
		IASTTranslationUnit tu = parse( writer.toString() );
		CPPNameCollector col = new CPPNameCollector();
		tu.accept( col );
		
		assertEquals( col.size(), 7 );
		ICPPClassType G2 = (ICPPClassType) col.getName(0).resolveBinding();
		ITypedef alt = (ITypedef) col.getName(3).resolveBinding();
				
		assertInstances( col, G2, 2 );
		assertInstances( col, alt, 2 );
	}
	
	public void testBug46246() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "struct A {                 "); //$NON-NLS-1$
		writer.write( "   struct B { int ab; } b; "); //$NON-NLS-1$
		writer.write( "   int a;                  "); //$NON-NLS-1$
		writer.write( "};                         "); //$NON-NLS-1$
		writer.write( "struct A a1;               "); //$NON-NLS-1$
		writer.write( "struct B b1;               "); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( writer.toString(), true, ParserLanguage.C );
		CNameCollector col = new CNameCollector();
		tu.accept( col );
		
		assertEquals( col.size(), 9 );
		ICompositeType A = (ICompositeType) col.getName(0).resolveBinding();
		ICompositeType B = (ICompositeType) col.getName(1).resolveBinding();
				
		assertInstances( col, A, 2 );
		assertInstances( col, B, 2 );
	}
	
	public void testBug45235() throws Exception
	{
		parse( "class A { friend class B; friend void f(); }; " ); //$NON-NLS-1$		
	}
	
	public void testBug57791() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write(" void f() {                  "); //$NON-NLS-1$
		writer.write("    struct astruct astruct;  "); //$NON-NLS-1$
		writer.write("    astruct.foo++;           "); //$NON-NLS-1$
		writer.write(" }"); //$NON-NLS-1$
		
		parse( writer.toString(), true, ParserLanguage.C );
	}
	
	public void testBug44249() throws Exception
	{

		parse( "class SD_01 { public:\n	void SD_01::f_SD_01();};" ); //$NON-NLS-1$
	}
	
	public void testBug59149() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "class A{ friend class B; friend class B; };" ); //$NON-NLS-1$
		writer.write( "class B{ };" ); //$NON-NLS-1$		
		parse( writer.toString() );
	}	
    
    public void testBug59302() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write("class A { class N{}; };         "); //$NON-NLS-1$
    	writer.write("class B { friend class A::N; }; "); //$NON-NLS-1$    	
    	parse( writer.toString() );
	}
	
    

    public void testULong() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "#ifndef ASMINCLUDE\n"); //$NON-NLS-1$
    	writer.write( "typedef unsigned short         ushort;\n"); //$NON-NLS-1$
    	writer.write( "typedef volatile unsigned long semaphore;\n"); //$NON-NLS-1$
    	writer.write( "typedef unsigned long          ulong;\n"); //$NON-NLS-1$
    	writer.write( "#ifndef _NO_LONGLONG\n"); //$NON-NLS-1$
    	writer.write( "typedef long long              longlong;\n"); //$NON-NLS-1$
    	writer.write( "typedef unsigned long long     ulonglong;\n"); //$NON-NLS-1$
    	writer.write( "#endif  /* _NO_LONGLONG */\n"); //$NON-NLS-1$
    	writer.write( "#endif  /*  ASMINCLUDE  */\n"); //$NON-NLS-1$
    	writer.write( "typedef struct section_type_ {\n"); //$NON-NLS-1$
    	writer.write( "ulong source;\n"); //$NON-NLS-1$
    	writer.write( "ulong dest;\n"); //$NON-NLS-1$
    	writer.write( "ulong bytes;\n"); //$NON-NLS-1$
    	writer.write( "} section_type;\n"); //$NON-NLS-1$
    	parse( writer.toString() );
	}
 
    public void testBug47926() throws Exception
	{
    	parse( "void f() {} class A {}; void main() { A * a = new A(); a->f();	}", false ); //$NON-NLS-1$
	}
    
    public void testBug50984_ASTMethod_getOwnerClassSpecifier_ClassCastException() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "template < typename _OutIter >                                 " ); //$NON-NLS-1$
    	writer.write( "class num_put {                                                " ); //$NON-NLS-1$
    	writer.write( "   typedef _OutIter iter_type;                                 " ); //$NON-NLS-1$
    	writer.write( "   template< typename _ValueT >                                " ); //$NON-NLS-1$
    	writer.write( "    iter_type _M_convert_float( iter_type );                   " ); //$NON-NLS-1$
    	writer.write( "};                                                             " ); //$NON-NLS-1$
    	writer.write( "template < typename _OutIter >                                 " ); //$NON-NLS-1$
    	writer.write( "template < typename _ValueT  >                                 " ); //$NON-NLS-1$
    	writer.write( "_OutIter num_put<_OutIter>::_M_convert_float( _OutIter ) { }   " ); //$NON-NLS-1$
    	parse( writer.toString() );    	
   	}
    
    public void testGloballyQualifiedUsingDeclaration() throws Exception
	{
		parse( "int iii; namespace N { using ::iii; }" ); //$NON-NLS-1$
	}
    
    public void test57513_new() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "class A{ A(); A( int ); };   \n" ); //$NON-NLS-1$
    	writer.write( " void f() {                  \n" ); //$NON-NLS-1$
    	writer.write( "    A * a1 = new A;          \n" ); //$NON-NLS-1$
    	writer.write( "    A * a2 = new(1)A();      \n" ); //$NON-NLS-1$
    	writer.write( "    A * a3 = new A( 1 );     \n" ); //$NON-NLS-1$
    	writer.write( "}                            \n" ); //$NON-NLS-1$
    	
    	parse( writer.toString() );
	}

    public void test57513_NoConstructor() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "class A{  };   \n" ); //$NON-NLS-1$
    	writer.write( " void f() {                  \n" ); //$NON-NLS-1$
    	writer.write( "    A * a1 = new A;          \n" ); //$NON-NLS-1$
    	writer.write( "}                            \n" ); //$NON-NLS-1$
    	
    	parse( writer.toString() );
	}
    
    public void test57513_ctorinit() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "class A{ A(); A( A * ); };   \n" ); //$NON-NLS-1$
    	writer.write( "class B : public A { B(); }; \n" ); //$NON-NLS-1$
    	writer.write( "B::B():A( new A ){}          \n" ); //$NON-NLS-1$
    	
    	parse( writer.toString() );
   	}
    
    public void test575513_qualified() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "namespace Foo{                     " ); //$NON-NLS-1$
    	writer.write( "   class Bar{ public : Bar(); };   " ); //$NON-NLS-1$
    	writer.write( "}                                  " ); //$NON-NLS-1$
    	writer.write( "void main(){                       " ); //$NON-NLS-1$
    	writer.write( "  Foo::Bar * bar = new Foo::Bar(); " ); //$NON-NLS-1$
    	writer.write( "}                                  " ); //$NON-NLS-1$
    	
    	parse( writer.toString() );    	
	}
    
    public void testBug60944() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "typedef int OurInt;\n"); //$NON-NLS-1$
    	writer.write( "class A { int x; };\n"); //$NON-NLS-1$
    	writer.write( "typedef A AnotherA;\n"); //$NON-NLS-1$
    	writer.write( "typedef AnotherA SecondA;\n"); //$NON-NLS-1$
    	writer.write( "typedef OurInt AnotherInt;\n" ); //$NON-NLS-1$
    	parse( writer.toString() );
	}
    
    public void testDestructorReference() throws Exception
    {
    	Writer writer = new StringWriter();
    	writer.write( "class ABC {\n"); //$NON-NLS-1$
    	writer.write( " public:\n"); //$NON-NLS-1$
    	writer.write( " ~ABC(){ }\n"); //$NON-NLS-1$
    	writer.write( "};\n"); //$NON-NLS-1$
    	writer.write( "int main() { ABC * abc = new ABC();\n"); //$NON-NLS-1$
    	writer.write( "abc->~ABC();\n"); //$NON-NLS-1$
    	writer.write( "}\n"); //$NON-NLS-1$
    	
		parse( writer.toString() );  
    }
    
    public void testBug39676_tough() throws Exception
	{
    	parse( "int widths[] = { [0 ... 9] = 1, [10 ... 99] = 2, [100] = 3 };", true, ParserLanguage.C, true ); //$NON-NLS-1$
	}
    
    public void testBug60939() throws Exception
	{
    	for( int i = 0; i < 2; ++i )
    	{
	    	Writer writer = new StringWriter();
	    	writer.write( "namespace ABC { class DEF { }; }\n"); //$NON-NLS-1$
	    	if( i == 0 )
	    		writer.write( "using namespace ABC;\n"); //$NON-NLS-1$
	    	else
	    		writer.write( "using ABC::DEF;\n"); //$NON-NLS-1$
	    	writer.write( "class GHI : public DEF { };"); //$NON-NLS-1$
	    	parse( writer.toString() );
    	}
    	

	}
    
    public void testBug64010() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( " #define ONE	else if (0) { } \n"); //$NON-NLS-1$
    	writer.write( " #define TEN	ONE ONE ONE ONE ONE ONE ONE ONE ONE ONE \n "); //$NON-NLS-1$
    	writer.write( " #define HUN	TEN TEN TEN TEN TEN TEN TEN TEN TEN TEN \n "); //$NON-NLS-1$
    	writer.write( " #define THOU	HUN HUN HUN HUN HUN HUN HUN HUN HUN HUN \n"); //$NON-NLS-1$
		writer.write("void foo()                                                "); //$NON-NLS-1$
		writer.write("{                                                         "); //$NON-NLS-1$
		writer.write("   if (0) { }                                             "); //$NON-NLS-1$
		writer.write("   /* 2,500 else if's.  */                               "); //$NON-NLS-1$
		writer.write("   THOU THOU HUN HUN HUN HUN HUN "); //$NON-NLS-1$
		writer.write("}                                                         "); //$NON-NLS-1$
		
		parse( writer.toString() );
	}
    
    public void testBug64271() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "typedef int DWORD;\n" ); //$NON-NLS-1$
		writer.write( "typedef char BYTE;\n"); //$NON-NLS-1$
		writer.write( "#define MAKEFOURCC(ch0, ch1, ch2, ch3)                              \\\n"); //$NON-NLS-1$
		writer.write( "((DWORD)(BYTE)(ch0) | ((DWORD)(BYTE)(ch1) << 8) |       \\\n"); //$NON-NLS-1$
		writer.write( "((DWORD)(BYTE)(ch2) << 16) | ((DWORD)(BYTE)(ch3) << 24 ))\n"); //$NON-NLS-1$
		writer.write( "enum e {\n"); //$NON-NLS-1$
		writer.write( "blah1 = 5,\n"); //$NON-NLS-1$
		writer.write( "blah2 = MAKEFOURCC('a', 'b', 'c', 'd'),\n"); //$NON-NLS-1$
		writer.write( "blah3\n"); //$NON-NLS-1$
		writer.write( "};\n"); //$NON-NLS-1$
		writer.write( "e mye = blah;\n"); //$NON-NLS-1$
		parse( writer.toString() );
	}
    
    public void testBug47752() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "class BBC\n"); //$NON-NLS-1$
    	writer.write( "{\n"); //$NON-NLS-1$
    	writer.write( "int x;\n"); //$NON-NLS-1$
    	writer.write( "};\n"); //$NON-NLS-1$
    	writer.write( "void func( BBC bar )\n"); //$NON-NLS-1$
    	writer.write( "try\n"); //$NON-NLS-1$
    	writer.write( "{\n"); //$NON-NLS-1$
    	writer.write( "}\n"); //$NON-NLS-1$
    	writer.write( "catch ( BBC error )\n"); //$NON-NLS-1$
    	writer.write( "{\n"); //$NON-NLS-1$
    	writer.write( "		  //... error handling code ...\n"); //$NON-NLS-1$
    	writer.write( "}\n"); //$NON-NLS-1$
    	parse( writer.toString() );
	}
    public void testBug61972() throws Exception
	{
    	parse( "#define DEF1(A1) A1\n#define DEF2     DEF1(DEF2)\nDEF2;", false ); //$NON-NLS-1$
	}
    
    public void testBug65569() throws Exception
	{
    	parse( "class Sample;\nstruct Sample { /* ... */ };" ); //$NON-NLS-1$
	}
    
    public void testBug64268() throws Exception
	{
    	Writer writer = new StringWriter();
		writer.write("#define BODY \\\n"); //$NON-NLS-1$
		writer.write("for (;;) {	 \\\n"); //$NON-NLS-1$
		writer.write("/* this multi-line comment messes \\\n"); //$NON-NLS-1$
		writer.write("up the parser.  */ }\n"); //$NON-NLS-1$
		writer.write("	void abc() {\n"); //$NON-NLS-1$
		writer.write("BODY\n"); //$NON-NLS-1$
		writer.write("}\n"); //$NON-NLS-1$
		parse( writer.toString() );
	}
    
    public void testBug67622() throws Exception
	{
    	parse( "const char * x = __FILE__;"); //$NON-NLS-1$
	}
    
    public void testBug67680() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "template < class T> class Base {};                  \n" ); //$NON-NLS-1$
    	writer.write( "class Derived : public Base, Base<int>, foo {};     \n" ); //$NON-NLS-1$
    	
    	parse( writer.toString(), false );
	}
    
    public void testTypeIDSignature() throws Exception
    {
    	parse( "int * v = (int*)0;");//$NON-NLS-1$
    }
    
    public void testUnaryAmperCast() throws Exception{
    	Writer writer = new StringWriter();
    	writer.write( "void f( char * );              \r\n "); //$NON-NLS-1$
    	writer.write( "void f( char   );              \n "); //$NON-NLS-1$
    	writer.write( "void main() {                  \n "); //$NON-NLS-1$
    	writer.write( "   char * t = new char [ 5 ];  \n "); //$NON-NLS-1$
    	writer.write( "   f( &t[1] );                 \n "); //$NON-NLS-1$
    	writer.write( "}                              \n "); //$NON-NLS-1$
    	
    	parse( writer.toString() );
    }
	
    public void testBug68235() throws Exception{
    	Writer writer = new StringWriter();
    	writer.write( " struct xTag { int x; };               "); //$NON-NLS-1$
    	writer.write( " typedef xTag xType;                   "); //$NON-NLS-1$
    	writer.write( " typedef struct yTag { int x; } yType; "); //$NON-NLS-1$
    	writer.write( " class C1 { xType x; yType y; };       "); //$NON-NLS-1$
    	
    	parse( writer.toString() );    	
    }
    
    public void testBug60407() throws Exception
    {
    	Writer writer = new StringWriter();
    	writer.write( "struct ZZZ { int x, y, z; };\r\n" ); //$NON-NLS-1$
    	writer.write( "typedef struct ZZZ _FILE;\n" ); //$NON-NLS-1$
    	writer.write( "typedef _FILE FILE;\n" ); //$NON-NLS-1$
    	writer.write( "static void static_function(FILE * lcd){}\n" ); //$NON-NLS-1$
    	writer.write( "int	main(int argc, char **argv) {\n" ); //$NON-NLS-1$
    	writer.write( "FILE * file = 0;\n" ); //$NON-NLS-1$
    	writer.write( "static_function( file );\n" ); //$NON-NLS-1$
    	writer.write( "return 0;\n" );	 //$NON-NLS-1$
    	writer.write( "}\n" ); //$NON-NLS-1$
    	parse( writer.toString() );
    }
    
    public void testBug68623() throws Exception{
        Writer writer = new StringWriter();
        writer.write( "class A {                         \n" ); //$NON-NLS-1$
        writer.write( "   A();                           \n" ); //$NON-NLS-1$
        writer.write( "   class sub{};                   \n" ); //$NON-NLS-1$
        writer.write( "   sub * x;                       \n" ); //$NON-NLS-1$
        writer.write( "};                                \n" ); //$NON-NLS-1$
        writer.write( "A::A() : x( (sub *) 0 ) {}        \n" ); //$NON-NLS-1$
        
        parse( writer.toString() );
        
        writer = new StringWriter();
        writer.write( "class A {                         \n" ); //$NON-NLS-1$
        writer.write( "   A() : x (0) {}                 \n" ); //$NON-NLS-1$
        writer.write( "   int x;                         \n" ); //$NON-NLS-1$
        writer.write( "};                                \n" ); //$NON-NLS-1$
        
        parse( writer.toString() );
    }
    
    public void testBug69798() throws Exception{
        Writer writer = new StringWriter();
        writer.write( "enum Flags { FLAG1, FLAG2 };                          \n" ); //$NON-NLS-1$
        writer.write( "int f() { int a, b;  b = ( a ? FLAG1 : 0 ) | FLAG2; } \n" ); //$NON-NLS-1$
        
        parse( writer.toString() );
    }
    
    public void testBug69662() throws Exception{
        Writer writer = new StringWriter();
        writer.write( "class A { operator float * (); };  \n" ); //$NON-NLS-1$
        writer.write( "A::operator float * () { }         \n" ); //$NON-NLS-1$
        
        parse( writer.toString() );
    }
    
    
    public void testBug68528() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "namespace N526026\n" ); //$NON-NLS-1$
    	writer.write( "{\n" ); //$NON-NLS-1$
    	writer.write( "template <typename T>\n" ); //$NON-NLS-1$
    	writer.write( "class T526026\n" ); //$NON-NLS-1$
    	writer.write( "{\n" ); //$NON-NLS-1$
    	writer.write( "typedef int diff;\n" ); //$NON-NLS-1$
    	writer.write( "};\n" ); //$NON-NLS-1$
    	writer.write( "\n" ); //$NON-NLS-1$
    	writer.write( "template<typename T>\n" ); //$NON-NLS-1$
    	writer.write( "inline T526026< T >\n" );  //$NON-NLS-1$
    	writer.write( "operator+(typename T526026<T>::diff d, const T526026<T> & x )\n" );  //$NON-NLS-1$
    	writer.write( "{ return T526026< T >(); }\n" ); //$NON-NLS-1$
    	writer.write( "}\n" ); //$NON-NLS-1$
    	parse( writer.toString(), false );
	}
    
    public void testBug71094() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "using namespace DOESNOTEXIST;\n" );  //$NON-NLS-1$
    	writer.write( "class A { int x; };\n" ); //$NON-NLS-1$
    	parse( writer.toString(), false );
	}
    
	public void testPredefinedSymbol_bug70928() throws Exception {
		// GNU builtin storage class type __cdecl preceded by a custom return type 
		Writer writer = new StringWriter();
		writer.write( "#define __cdecl __attribute__ ((__cdecl__))\n" ); //$NON-NLS-1$
		writer.write( "typedef int size_t; \n int __cdecl foo(); \n" ); //$NON-NLS-1$
		parse(writer.toString(), true, ParserLanguage.CPP, true);
	}
	
	public void testPredefinedSymbol_bug70928_infinite_loop_test1() throws Exception {
		// GNU builtin storage class type __cdecl preceded by a custom return type 
		Writer writer = new StringWriter();
		writer.write( "#define __cdecl __attribute__ ((__cdecl__))\n" ); //$NON-NLS-1$
		writer.write( "typedef int size_t; \n int __cdecl foo(); \n" ); //$NON-NLS-1$
		parse(writer.toString(), false, ParserLanguage.CPP, false);// test for an infinite loop if the GCC extensions aren't supported
		parse(writer.toString(), false, ParserLanguage.C, false);// test for an infinite loop if the GCC extensions aren't supported
	}
	
	public void testPredefinedSymbol_bug70928_infinite_loop_test2() throws Exception {
		// GNU builtin storage class type __cdecl preceded by a custom return type 
		Writer writer = new StringWriter();
		writer.write( "int x __attribute__ ((aligned (16))) = 0;\n" ); //$NON-NLS-1$
		parse(writer.toString(), false, ParserLanguage.CPP, false);// test for an infinite loop if the GCC extensions aren't supported
		parse(writer.toString(), false, ParserLanguage.C, false);// test for an infinite loop if the GCC extensions aren't supported
	}
	
	public void testBug102376() throws Exception {
		Writer writer = new StringWriter();
		writer.write( "int func1 (void) __attribute__((,id2,id (,,),,,));\n" ); //$NON-NLS-1$
		writer.write( "int func2 (void) __attribute__((id,id (id)));\n" ); //$NON-NLS-1$
		writer.write( "int func3 (void) __attribute__((id,id (3)));\n" ); //$NON-NLS-1$
		writer.write( "int func4 (void) __attribute__((id,id (1+2)));\n" ); //$NON-NLS-1$
		writer.write( "void (****f1)(void) __attribute__((noreturn));\n" ); //$NON-NLS-1$
  	    writer.write( "void (__attribute__((noreturn)) ****f2) (void);\n" ); //$NON-NLS-1$
 		writer.write( "char *__attribute__((aligned(8))) *f3;\n" ); //$NON-NLS-1$
 		writer.write( "char * __attribute__((aligned(8))) * f3;\n" ); //$NON-NLS-1$
		writer.write( "void fatal1 () __attribute__ ((noreturn));\n" ); //$NON-NLS-1$
		writer.write( "int square1 (int) __attribute__ ((pure));\n" ); //$NON-NLS-1$
		writer.write( "extern int\n" ); //$NON-NLS-1$
		writer.write( "my_printf1 (void *my_object, const char *my_format, ...)\n" ); //$NON-NLS-1$
		writer.write( "__attribute__ ((format (printf, 2, 3)));\n" ); //$NON-NLS-1$
		writer.write( "extern char *\n" ); //$NON-NLS-1$
		writer.write( "my_dgettext1 (char *my_domain, const char *my_format)\n" ); //$NON-NLS-1$
		writer.write( "__attribute__ ((format_arg (2)));\n" ); //$NON-NLS-1$
		writer.write( "extern void *\n" ); //$NON-NLS-1$
		writer.write( "my_memcpy1 (void *dest, const void *src, size_t len)\n" ); //$NON-NLS-1$
		writer.write( "__attribute__((nonnull (1, 2)));\n" ); //$NON-NLS-1$
		writer.write( "extern void *\n" ); //$NON-NLS-1$
		writer.write( "my_memcpy2 (void *dest, const void *src, size_t len)\n" ); //$NON-NLS-1$
		writer.write( "__attribute__((nonnull));\n" ); //$NON-NLS-1$
		writer.write( "extern void foobar3 (void) __attribute__ ((section (\"bar\")));\n" ); //$NON-NLS-1$
		writer.write( "int old_fn () __attribute__ ((deprecated));\n" ); //$NON-NLS-1$
		writer.write( "void f5 () __attribute__ ((weak, alias (\"__f\")));\n" ); //$NON-NLS-1$
		writer.write( "void __attribute__ ((visibility (\"protected\")))\n" ); //$NON-NLS-1$
		writer.write( "f6 () { /* Do something. */; }\n" ); //$NON-NLS-1$
		writer.write( "int i2 __attribute__ ((visibility (\"hidden\")));\n" ); //$NON-NLS-1$
		writer.write( "void f7 () __attribute__ ((interrupt (\"IRQ\")));\n" ); //$NON-NLS-1$
		writer.write( "void *alt_stack9;\n" ); //$NON-NLS-1$
		writer.write( "void f8 () __attribute__ ((interrupt_handler,\n" ); //$NON-NLS-1$
		writer.write( "sp_switch (\"alt_stack\")));\n" ); //$NON-NLS-1$
		writer.write( "int x1 __attribute__ ((aligned (16))) = 0;\n" ); //$NON-NLS-1$
		writer.write( "struct foo11 { int x[2] __attribute__ ((aligned (8))); };\n" ); //$NON-NLS-1$
		writer.write( "short array12[3] __attribute__ ((aligned));\n" ); //$NON-NLS-1$
		writer.write( "extern int old_var14 __attribute__ ((deprecated));\n" ); //$NON-NLS-1$
		writer.write( "struct foo13\n" ); //$NON-NLS-1$
		writer.write( "{\n" ); //$NON-NLS-1$
		writer.write( "char a15;\n" ); //$NON-NLS-1$
		writer.write( "int x16[2] __attribute__ ((packed));\n" ); //$NON-NLS-1$
		writer.write( "};\n" ); //$NON-NLS-1$
		writer.write( "struct duart15 a16 __attribute__ ((section (\"DUART_A\"))) = { 0 };\n" ); //$NON-NLS-1$
		writer.write( "struct duart15 b17 __attribute__ ((section (\"DUART_B\"))) = { 0 };\n" ); //$NON-NLS-1$
		writer.write( "char stack18[10000] __attribute__ ((section (\"STACK\"))) = { 0 };\n" ); //$NON-NLS-1$
		writer.write( "int init_data19 __attribute__ ((section (\"INITDATA\"))) = 0;\n" ); //$NON-NLS-1$
		writer.write( "int foo20 __attribute__((section (\"shared\"), shared)) = 0;\n" ); //$NON-NLS-1$
		writer.write( "int foo21 __attribute__ ((vector_size (16)));\n" ); //$NON-NLS-1$
		writer.write( "struct S22 { int a23; };\n" ); //$NON-NLS-1$
		writer.write( "struct S24  __attribute__ ((vector_size (16))) foo;\n" ); //$NON-NLS-1$
		writer.write( "struct S25 { short f27[3]; } __attribute__ ((aligned (8)));\n" ); //$NON-NLS-1$
		writer.write( "typedef int more_aligned_int __attribute__ ((aligned (8)));\n" ); //$NON-NLS-1$
		writer.write( "struct S26 { short f28[3]; } __attribute__ ((aligned));\n" ); //$NON-NLS-1$
		writer.write( "\n" ); //$NON-NLS-1$
		writer.write( "struct my_unpacked_struct29\n" ); //$NON-NLS-1$
		writer.write( "{\n" ); //$NON-NLS-1$
		writer.write( "char c;\n" ); //$NON-NLS-1$
		writer.write( "int i;\n" ); //$NON-NLS-1$
		writer.write( "};\n" ); //$NON-NLS-1$
		writer.write( "          \n" ); //$NON-NLS-1$
		writer.write( "struct my_packed_struct __attribute__ ((__packed__))\n" ); //$NON-NLS-1$
		writer.write( "{\n" ); //$NON-NLS-1$
		writer.write( "char c;\n" ); //$NON-NLS-1$
		writer.write( "int  i;\n" ); //$NON-NLS-1$
		writer.write( "struct my_unpacked_struct29 s;\n" ); //$NON-NLS-1$
		writer.write( "};\n" ); //$NON-NLS-1$
		writer.write( "\n" ); //$NON-NLS-1$
		writer.write( "typedef union\n" ); //$NON-NLS-1$
		writer.write( "{\n" ); //$NON-NLS-1$
		writer.write( "int *__ip;\n" ); //$NON-NLS-1$
		writer.write( "union wait *__up;\n" ); //$NON-NLS-1$
		writer.write( "} wait_status_ptr_t __attribute__ ((__transparent_union__));\n" ); //$NON-NLS-1$
		writer.write( "\n" ); //$NON-NLS-1$
		writer.write( "typedef int T1 __attribute__ ((deprecated));\n" ); //$NON-NLS-1$
		writer.write( "typedef short __attribute__((__may_alias__)) short_a;\n" ); //$NON-NLS-1$
		writer.write( "extern const unsigned short int ** __ctype_b_loc (void) __attribute__ ((__const));" ); //$NON-NLS-1$
		parse( writer.toString(), true, ParserLanguage.C, true );
		parse( writer.toString(), true, ParserLanguage.CPP, true );
	}

    public void testBug73652() throws Exception
	{
    	StringWriter writer = new StringWriter();
    	writer.write( "#define DoSuperMethodA IDoSuperMethodA\n" ); //$NON-NLS-1$
    	writer.write( "#define IDoSuperMethodA(a,b,c) IIntuition->IDoSuperMethodA(a,b,c)\n" ); //$NON-NLS-1$
		writer.write( "void hang(void)\n" ); //$NON-NLS-1$
		writer.write( "{\n" ); //$NON-NLS-1$
		writer.write( "DoSuperMethodA(0,0,0);\n" ); //$NON-NLS-1$
		writer.write( "}\n" ); //$NON-NLS-1$
		parse( writer.toString() , false );
	}
    
    public void testBug73428() throws Exception
	{
    	parse( "namespace {  }");//$NON-NLS-1$
    	parse( "namespace {  };");//$NON-NLS-1$
    	parse( "namespace {  int abc; };");//$NON-NLS-1$
    	parse( "namespace {  int abc; }");//$NON-NLS-1$
	}
    
    public void testBug73615() throws Exception
	{
    	for( int i = 0; i < 2; ++i )
    	{
    		StringWriter writer = new StringWriter();
    		if( i == 0 )
    			writer.write( "class B;\n"); //$NON-NLS-1$
    		writer.write( "class A { A( B * ); };\n"); //$NON-NLS-1$
    		if( i == 0 )
    			parse( writer.toString() );
    		else
    			parse( writer.toString(), false );
    	}
	}
    
    public void testBug74180() throws Exception
    {
        parse( "enum DHCPFOBoolean { false, true } additionalHB, more_payload; \n", true, ParserLanguage.C ); //$NON-NLS-1$
    }
    
    public void testBug72691() throws Exception{
        StringWriter writer = new StringWriter();
        writer.write( "typedef int * PINT; \n" ); //$NON-NLS-1$
        writer.write( "typedef int * PINT; \n" ); //$NON-NLS-1$
        writer.write( "PINT pint;          \n" ); //$NON-NLS-1$
        parse( writer.toString() );
    }
    
    public void testBug72691_2() throws Exception{
        StringWriter writer = new StringWriter();
        writer.write( "typedef int * PINT;    \n" ); //$NON-NLS-1$
        writer.write( "namespace N {          \n" ); //$NON-NLS-1$
        writer.write( "   typedef int * PINT; \n" ); //$NON-NLS-1$
        writer.write( "}                      \n" ); //$NON-NLS-1$
        writer.write( "using namespace N;     \n" ); //$NON-NLS-1$
        writer.write( "PINT pint;             \n" ); //$NON-NLS-1$
        parse( writer.toString() );
    }
    
    public void testBug74328() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "int\n" );  //$NON-NLS-1$
    	writer.write( "main(int argc, char **argv) {\n" ); //$NON-NLS-1$
    	writer.write( "	char *sign;\n" ); //$NON-NLS-1$
    	writer.write( "sign = \"\"; // IProblem generated here, syntax error\n" ); //$NON-NLS-1$
    	writer.write( "return argc;\n" ); //$NON-NLS-1$
    	writer.write( "}\n" ); //$NON-NLS-1$
    	parse( writer.toString() );
	}
    
    public void testBug71733() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "void foo( int );\n"); //$NON-NLS-1$
    	writer.write( "#define BLAH() \\\n"); //$NON-NLS-1$
    	writer.write( "  foo ( /*  slash / is misinterpreted as end of comment */ \\\n"); //$NON-NLS-1$
    	writer.write( "    4 );\n"); //$NON-NLS-1$
    	writer.write( "int f() { BLAH() }\n"); //$NON-NLS-1$
    	parse( writer.toString() );
	}
    
    public void testBug69526() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "unsigned inkernel;\n" ); //$NON-NLS-1$
    	writer.write( "#define lock_kernel() (inkernel |= 0x01)" ); //$NON-NLS-1$
    	writer.write( "int main(int argc, char **argv) {" ); //$NON-NLS-1$
    	writer.write( "lock_kernel();" ); //$NON-NLS-1$
    	writer.write( "}" ); //$NON-NLS-1$
    	parse( writer.toString() );
	}
    
    public void testBug69454() throws Exception
    {
        Writer writer = new StringWriter();
        writer.write( "#define CATCH_ALL_EXCEPTIONS()                         \\\n" ); //$NON-NLS-1$
        writer.write( "   catch( Exception &ex ) { handleException( ex ); }   \\\n" ); //$NON-NLS-1$
        writer.write( "   catch( ... )           { handleException();    }      \n" ); //$NON-NLS-1$
        writer.write( "class Exception;                                         \n" ); //$NON-NLS-1$
        writer.write( "void handleException( Exception & ex ) {}                \n" ); //$NON-NLS-1$
        writer.write( "void handleException() {}                                \n" ); //$NON-NLS-1$
        writer.write( "void f() {                                               \n" ); //$NON-NLS-1$
        writer.write( "   try { int i; }                                        \n" ); //$NON-NLS-1$
        writer.write( "   CATCH_ALL_EXCEPTIONS();                               \n" ); //$NON-NLS-1$
        writer.write( "}                                                        \n" ); //$NON-NLS-1$

        parse( writer.toString() );
    }
    

    public void testBug72692A() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "extern double pow(double, double);\n"); //$NON-NLS-1$
    	writer.write( "extern double pow2(double, double){}\n"); //$NON-NLS-1$
    	writer.write( "namespace DS {\n"); //$NON-NLS-1$
    	writer.write( "using ::pow;\n"); //$NON-NLS-1$
    	writer.write( "using ::pow2;\n"); //$NON-NLS-1$
    	writer.write( "}\n"); //$NON-NLS-1$
    	writer.write( "using DS::pow;\n"); //$NON-NLS-1$
    	writer.write( "using DS::pow2;\n"); //$NON-NLS-1$
    	parse( writer.toString() );
	}
    
    public void testBug72692B() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "extern double pow(double, double);\n"); //$NON-NLS-1$
    	writer.write( "namespace DS {\n"); //$NON-NLS-1$
    	writer.write( "using ::pow;\n"); //$NON-NLS-1$
    	writer.write( "inline float pow(float __x, float __y)\n" ); //$NON-NLS-1$
    	writer.write( "{ return ::pow(static_cast<double>(__x), static_cast<double>(__y)); }\n" ); //$NON-NLS-1$
    	writer.write( "}\n"); //$NON-NLS-1$
    	writer.write( "using namespace DS;\n"); //$NON-NLS-1$
    	writer.write( "float foo() { double d1 = 3.0, d2 = 4.0; return pow(d1, d2); }"); //$NON-NLS-1$
    	parse( writer.toString() );
	}

    public void testBug72692C() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "extern double pow(double, double){}\n"); //$NON-NLS-1$
    	writer.write( "namespace DS {\n"); //$NON-NLS-1$
    	writer.write( "using ::pow;\n"); //$NON-NLS-1$
    	writer.write( "}\n"); //$NON-NLS-1$
    	writer.write( "using DS::pow;\n"); //$NON-NLS-1$
    	parse( writer.toString() );
	}

    
    public void testBug74575A() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "double pow(double, double);\n"); //$NON-NLS-1$
    	writer.write( "float pow(float __x, float __y)\n" ); //$NON-NLS-1$
    	writer.write( "{ return 0; }\n"); //$NON-NLS-1$
    	parse( writer.toString() );
	}
    
    public void testBug75338() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "class Thrown { };\n"); //$NON-NLS-1$
    	writer.write( "void foo() throw( Thrown );"); //$NON-NLS-1$
    	parse( writer.toString() );
	}
        
    public void testBug74847() throws Exception {
        String code = "class A : public FOO {};"; //$NON-NLS-1$
        parse( code, false );
    }
    
    public void testBug76696() throws Exception{
        Writer writer = new StringWriter();
		writer.write(" void f(){       \n"); //$NON-NLS-1$
		writer.write("    if( A a) {   \n"); //$NON-NLS-1$
		writer.write("    } else {     \n"); //$NON-NLS-1$
		writer.write("    }	           \n"); //$NON-NLS-1$
		writer.write(" }               \n"); //$NON-NLS-1$
		
		parse( writer.toString(), false );
    }
    
    public void testBug74069() throws Exception{
        Writer writer = new StringWriter();
        writer.write( "int f() {                \n"); //$NON-NLS-1$
        writer.write( "   int a, b, c;          \n"); //$NON-NLS-1$
        writer.write( "   if( a < b )           \n"); //$NON-NLS-1$
        writer.write( "      if( b < c )        \n"); //$NON-NLS-1$
        writer.write( "         return b;       \n"); //$NON-NLS-1$
        writer.write( "      else if ( a < c )  \n"); //$NON-NLS-1$
        writer.write( "         return c;       \n"); //$NON-NLS-1$
        writer.write( "      else               \n"); //$NON-NLS-1$
        writer.write( "         return a;       \n"); //$NON-NLS-1$
        writer.write( "   else if( a < c )      \n"); //$NON-NLS-1$
        writer.write( "      return a;          \n"); //$NON-NLS-1$
        writer.write( "   else if( b < c )      \n"); //$NON-NLS-1$
        writer.write( "      return c;          \n"); //$NON-NLS-1$
        writer.write( "   else                  \n"); //$NON-NLS-1$
        writer.write( "      return b;          \n"); //$NON-NLS-1$
        writer.write( "}                        \n"); //$NON-NLS-1$
        
        parse( writer.toString() );
    }
    public void testBug77805() throws Exception {
    	Writer writer = new StringWriter();
    	writer.write("#if X // Do something only if X is true\n"); //$NON-NLS-1$
    	writer.write("/* some statements */\n"); //$NON-NLS-1$
    	writer.write("#endif\n"); //$NON-NLS-1$
    	parse(writer.toString());
    }
    
    public void testBug77821() throws Exception {
    	Writer writer = new StringWriter();
    	writer.write("typedef struct { /* ... */ }TYPE;\n"); //$NON-NLS-1$
    	writer.write("void ptrArith(const TYPE* pType) {\n"); //$NON-NLS-1$
    	writer.write("TYPE *temp = 0;\n"); //$NON-NLS-1$
    	writer.write("temp = (TYPE*)(pType + 1); /* Parser error is here */\n}\n"); //$NON-NLS-1$
    	parse(writer.toString());
    }
    
    public void testBug77009() throws Exception
	{
		parse("int foo(volatile int &);\n"); //$NON-NLS-1$
	}
    
    
    public void testBug77281() throws Exception {
    	Writer writer = new StringWriter();
    	writer.write("void fun2(float a, float b) {}\n"); //$NON-NLS-1$
		writer.write("int main() { fun2(0.24f, 0.25f); }\n"); //$NON-NLS-1$
    	parse(writer.toString());
    }
    
    public void testBug77921() throws Exception {
    	Writer writer = new StringWriter();
    	writer.write("void f()\n{\n"); //$NON-NLS-1$
    	writer.write("static float v0[] = { -1.0f, -1.0f,  1.0f };\n}\n"); //$NON-NLS-1$
    	parse(writer.toString());
    }
    
    
    public void testBug71317A() throws Exception {
    	Writer writer = new StringWriter();
    	writer.write("void f();\n"); //$NON-NLS-1$
	    writer.write("namespace NS {\n"); //$NON-NLS-1$
	    writer.write("using ::f;\n"); //$NON-NLS-1$
	    writer.write("using ::f;\n}"); //$NON-NLS-1$
	    parse(writer.toString());
    }

    public void testBug71317B() throws Exception {
    	Writer writer = new StringWriter();
    	writer.write("void f();\n"); //$NON-NLS-1$
	    writer.write("namespace NS {\n"); //$NON-NLS-1$
	    writer.write("void f();\n"); //$NON-NLS-1$
	    writer.write("using ::f;\n}"); //$NON-NLS-1$
	    parse(writer.toString());
    }
    
    public void testBug77097() throws Exception {
    	Writer writer = new StringWriter();
    	writer.write("#define SOME_MACRO() { \\\r\n"); //$NON-NLS-1$
    	writer.write("printf(\"Hello World\"); \\\r\n"); //$NON-NLS-1$
    	writer.write("printf(\"Good morning\"); \\\r\n"); //$NON-NLS-1$
    	parse(writer.toString());
    }
    
    public void testBug77276() throws Exception {
    	Writer writer = new StringWriter();
    	writer.write("#if (!defined(OS_LIBMODE_R) && !defined(OS_LIBMODE_RP) && \\\r\n"); //$NON-NLS-1$
    	writer.write("!defined(OS_LIBMODE_T))\r\n"); //$NON-NLS-1$
    	writer.write("#define OS_LIBMODE_DP\r\n"); //$NON-NLS-1$
    	writer.write("#endif\r\n"); //$NON-NLS-1$
    	parse(writer.toString());
    }
    
    public void testBug78165() throws Exception {
    	Writer writer = new StringWriter();
    	writer.write("struct Node {\n"); //$NON-NLS-1$
    	writer.write("struct Node* Next; // OK: Refers to Node at global scope\n"); //$NON-NLS-1$
    	writer.write("struct Data* Data; // OK: Declares type Data at global scope and member Data\n"); //$NON-NLS-1$
    	writer.write("};\n"); //$NON-NLS-1$
    	writer.write("struct Data {\n"); //$NON-NLS-1$
    	writer.write("struct Node* Node; // OK: Refers to Node at global scope\n"); //$NON-NLS-1$
    	writer.write("friend struct Glob; // OK: Refers to (as yet) undeclared Glob at global scope.\n"); //$NON-NLS-1$
    	writer.write("};\n"); //$NON-NLS-1$
    	writer.write("struct Base {\n"); //$NON-NLS-1$
    	writer.write("struct Data; // OK: Declares nested Data\n"); //$NON-NLS-1$
    	writer.write("struct ::Data* thatData; // OK: Refers to ::Data\n"); //$NON-NLS-1$
    	writer.write("struct Base::Data* thisData; // OK: Refers to nested Data\n"); //$NON-NLS-1$
    	writer.write("friend class ::Data; // OK: global Data is a friend\n"); //$NON-NLS-1$
    	writer.write("friend class Data; // OK: nested Data is a friend\n"); //$NON-NLS-1$
    	writer.write("struct Data { /* ... */ }; // Defines nested Data\n"); //$NON-NLS-1$
    	writer.write("struct Data; // OK: Redeclares nested Data\n"); //$NON-NLS-1$
    	writer.write("};\n"); //$NON-NLS-1$
    	writer.write("struct Data; // OK: Redeclares Data at global scope\n"); //$NON-NLS-1$
    	writer.write("struct Base::Data* pBase; // OK: refers to nested Data\n"); //$NON-NLS-1$

    	parse( writer.toString() );
    }

    public void testBug103560() throws Exception {
    	Writer writer = new StringWriter();
    	writer.write("#define A( a, b ) a ## b               \n"); //$NON-NLS-1$
    	writer.write("#define FOOBAR 1                       \n"); //$NON-NLS-1$
    	writer.write("int i = A( FOO, BAR );                 \n"); //$NON-NLS-1$
    	parse( writer.toString(), true, ParserLanguage.CPP );
    }
    
    public void test158192_declspec_on_class() throws Exception {
    	if(!Platform.getOS().equals(Platform.OS_WIN32))
    		return; // XXX: see GPPParserExtensionConfiguration.supportDeclspecSpecifiers()
    	
    	Writer writer = new StringWriter();
    	writer.write("class __declspec(foobar) Foo1 {};\n");
    	writer.write("union __declspec(foobar) Foo2 {};\n");
    	writer.write("struct __declspec(foobar) Foo3 {};\n");
    	IASTTranslationUnit tu = parse( writer.toString(), true, ParserLanguage.CPP, true );

    	CPPNameCollector col = new CPPNameCollector();
    	tu.accept( col );

    	assertEquals( 3, col.size());
    	ICompositeType fooClass = (ICompositeType) col.getName(0).resolveBinding();
    	ICompositeType fooUnion = (ICompositeType) col.getName(1).resolveBinding();
    	ICompositeType fooStruct = (ICompositeType) col.getName(2).resolveBinding();

    	assertEquals(ICPPClassType.k_class, fooClass.getKey());
    	assertEquals(ICompositeType.k_union, fooUnion.getKey());
    	assertEquals(ICompositeType.k_struct, fooStruct.getKey());

    	assertInstances(col, fooClass, 1);
    	assertInstances(col, fooUnion, 1);
    	assertInstances(col, fooStruct, 1);
    }

    public void test158192_declspec_on_variable() throws Exception {
    	if(!Platform.getOS().equals(Platform.OS_WIN32))
    		return; // XXX: see GPPParserExtensionConfiguration.supportDeclspecSpecifiers()
    
    	Writer writer = new StringWriter();
    	writer.write("__declspec(foobar) class Foo {} bar;\n");
    	IASTTranslationUnit tu = parse( writer.toString(), true, ParserLanguage.CPP, true);

    	CPPNameCollector col = new CPPNameCollector();
    	tu.accept( col );

    	assertEquals( 2, col.size());
    	ICompositeType fooClass = (ICompositeType) col.getName(0).resolveBinding();
    	ICPPVariable bar = (ICPPVariable) col.getName(1).resolveBinding();

    	assertInstances(col, fooClass, 1);
    	assertInstances(col, bar, 1);
    }

    // MSVC does not allow declspec in this position, GCC does so we test for this
    // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=158192
    public void test158192_declspec_in_declarator() throws Exception {
    	if(!Platform.getOS().equals(Platform.OS_WIN32))
    		return; // XXX: see GPPParserExtensionConfiguration.supportDeclspecSpecifiers()
    	
    	Writer writer = new StringWriter();

    	writer.write("int * __declspec(foo) bar = 0;\n");
    	IASTTranslationUnit tu = parse( writer.toString(), true, ParserLanguage.CPP, true);

    	IASTProblem [] problems = CPPVisitor.getProblems(tu);
    	assertFalse("__declspec rejected inside declarator", problems.length>0 );

    	CPPNameCollector col = new CPPNameCollector();
    	tu.accept( col );

    	assertEquals( 1, col.size());
    	ICPPVariable bar = (ICPPVariable) col.getName(0).resolveBinding();

    	assertInstances(col, bar, 1);
    }
    
    public void test173874_nestedClasses() throws Exception {
    	String code = "class aClass { class bClass; int x; };";
    	IASTTranslationUnit tu = parse(code, true, ParserLanguage.CPP, true);
    	
    	CPPNameCollector col = new CPPNameCollector();
    	tu.accept(col);
    	
    	ICPPClassType cls = (ICPPClassType)col.getName(0).resolveBinding();
    	ICPPClassType[] nested = cls.getNestedClasses();
    	assertEquals(1, nested.length);
    }
}
