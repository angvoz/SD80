/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/
 /*
 * Created on Mar 30, 2004
 */
package org.eclipse.cdt.core.parser.tests;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;

import org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTBaseSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation;
import org.eclipse.cdt.core.parser.ast.IASTTemplateParameter;
import org.eclipse.cdt.core.parser.ast.IASTTemplateSpecialization;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.internal.core.parser.ParserException;

/**
 * @author aniefer
 */
public class CompleteParseASTTemplateTest extends CompleteParseBaseTest {
	/**
	 * @param name
	 */
	public CompleteParseASTTemplateTest(String name) {
		super(name);
	}
	
	public void testTemplateClassDeclaration() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "template < class T > class A {  T t;  }; " ); //$NON-NLS-1$
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration template = (IASTTemplateDeclaration) i.next();
		Iterator params = template.getTemplateParameters();
		
		IASTTemplateParameter T = (IASTTemplateParameter) params.next();
		assertEquals( T.getIdentifier(), "T" ); //$NON-NLS-1$
		assertFalse( params.hasNext() );
		assertFalse( i.hasNext() );
		
		i = getDeclarations( template );

		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		assertEquals( classA.getName(), "A" ); //$NON-NLS-1$
		
		assertFalse( i.hasNext() );
		
		i = getDeclarations( classA );
		
		IASTField t = (IASTField) i.next();
		assertEquals( t.getName(), "t" ); //$NON-NLS-1$

		IASTSimpleTypeSpecifier specifier = (IASTSimpleTypeSpecifier) t.getAbstractDeclaration().getTypeSpecifier();
		assertEquals( specifier.getTypename(), "T" ); //$NON-NLS-1$
		//assertEquals( specifier.getTypeSpecifier(), T ); //TODO uncomment when bug 54029 is fixed
	}
	
	public void testTemplateFunction() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "template < class T > void f( T t ){} " ); //$NON-NLS-1$
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration template = (IASTTemplateDeclaration) i.next();
		
		Iterator params = template.getTemplateParameters();
		
		IASTTemplateParameter T = (IASTTemplateParameter) params.next();
		assertEquals( T.getIdentifier(), "T" ); //$NON-NLS-1$
		assertFalse( params.hasNext() );
		assertFalse( i.hasNext() );
		
		i = getDeclarations( template );
		IASTFunction f = (IASTFunction) i.next();
		assertEquals( f.getName(), "f" ); //$NON-NLS-1$
		
		params = f.getParameters();
		IASTParameterDeclaration t = (IASTParameterDeclaration) params.next();
		assertEquals( t.getName(), "t" ); //$NON-NLS-1$
		IASTSimpleTypeSpecifier typeSpec = (IASTSimpleTypeSpecifier) t.getTypeSpecifier();
		assertEquals( typeSpec.getTypename(), "T" ); //$NON-NLS-1$
		//assertEquals( typeSpec.getTypeSpecifier(), T );  //TODO uncomment when bug 54029 is fixed
	}
	
	public void testTemplateFunctionDefinition() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "template <class T> void f( T t );" ); //$NON-NLS-1$
		writer.write( "template <class U> void f( U u ) { }" ); //$NON-NLS-1$
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration template = (IASTTemplateDeclaration) i.next();
		
		Iterator params = template.getTemplateParameters();
		
		IASTTemplateParameter T = (IASTTemplateParameter) params.next();
		assertEquals( T.getIdentifier(), "T" ); //$NON-NLS-1$
		assertFalse( params.hasNext() );
		
		Iterator tempDecls = getDeclarations( template );
		IASTFunction f = (IASTFunction) tempDecls.next();
		assertEquals( f.getName(), "f" ); //$NON-NLS-1$
		assertFalse( f.hasFunctionBody() );
		assertFalse( tempDecls.hasNext() );
		
		params = f.getParameters();
		IASTParameterDeclaration t = (IASTParameterDeclaration) params.next();
		assertEquals( t.getName(), "t" ); //$NON-NLS-1$
		IASTSimpleTypeSpecifier typeSpec = (IASTSimpleTypeSpecifier) t.getTypeSpecifier();
		assertEquals( typeSpec.getTypename(), "T" ); //$NON-NLS-1$
		//assertEquals( typeSpec.getTypeSpecifier(), T );  //TODO uncomment when bug 54029 is fixed
		
		IASTTemplateDeclaration template2 = (IASTTemplateDeclaration) i.next();
		
		params = template2.getTemplateParameters();
		
		IASTTemplateParameter U = (IASTTemplateParameter) params.next();
		assertEquals( U.getIdentifier(), "U" ); //$NON-NLS-1$
		assertFalse( params.hasNext() );
		
		tempDecls = getDeclarations( template2 );
		IASTFunction f2 = (IASTFunction) tempDecls.next();
		assertEquals( f2.getName(), "f" ); //$NON-NLS-1$
		assertTrue( f2.previouslyDeclared() );
		
		params = f2.getParameters();
		IASTParameterDeclaration u = (IASTParameterDeclaration) params.next();
		assertEquals( u.getName(), "u" ); //$NON-NLS-1$
		typeSpec = (IASTSimpleTypeSpecifier) u.getTypeSpecifier();
		assertEquals( typeSpec.getTypename(), "U" ); //$NON-NLS-1$
		//assertEquals( typeSpec.getTypeSpecifier(), U );  //TODO uncomment when bug 54029 is fixed
		
		assertFalse( i.hasNext() );
	}
	
	public void testClassMemberTemplate() throws Exception{
		Writer writer = new StringWriter();
		writer.write( "namespace N { " ); //$NON-NLS-1$
		writer.write( "   class A { " ); //$NON-NLS-1$
		writer.write( "      template < class T > T f();" ); //$NON-NLS-1$
		writer.write( "   }; " ); //$NON-NLS-1$
		writer.write( "}" ); //$NON-NLS-1$
		writer.write( "template <class U> U N::A::f() {} " ); //$NON-NLS-1$
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTNamespaceDefinition N = (IASTNamespaceDefinition) i.next();
		
		Iterator i2 = getDeclarations( N );
		
		IASTClassSpecifier A = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i2.next()).getTypeSpecifier();
		assertFalse( i2.hasNext() );
		
		i2 = getDeclarations( A );
		
		IASTTemplateDeclaration template = (IASTTemplateDeclaration) i2.next();
		Iterator params = template.getTemplateParameters();
		IASTTemplateParameter T = (IASTTemplateParameter) params.next();
		assertFalse( params.hasNext() );
		assertFalse( i2.hasNext() );
		
		i2 = getDeclarations( template );
		
		IASTMethod f = (IASTMethod) i2.next();
		assertEquals( ((IASTSimpleTypeSpecifier)f.getReturnType().getTypeSpecifier()).getTypename(), "T" ); //$NON-NLS-1$
		assertFalse( i2.hasNext() );
		
		IASTTemplateDeclaration template2 = (IASTTemplateDeclaration) i.next();
		params = template.getTemplateParameters();
		IASTTemplateParameter U = (IASTTemplateParameter) params.next();
		assertFalse( params.hasNext() );
		assertFalse( i.hasNext() );
		
		i2 = getDeclarations( template2 );
		
		IASTMethod f2 = (IASTMethod) i2.next();
		assertEquals( ((IASTSimpleTypeSpecifier)f2.getReturnType().getTypeSpecifier()).getTypename(), "U" ); //$NON-NLS-1$
		assertQualifiedName( f2.getFullyQualifiedName(), new String [] { "N", "A", "f" } ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertTrue( f2.previouslyDeclared() );
		assertFalse( i2.hasNext() );
	}
	
	public void testOverloadedFunctionTemplates() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( " template < class T > void f ( T )   {} " ); //$NON-NLS-1$
		writer.write( " template < class T > void f ( T * ) {} " ); //$NON-NLS-1$
		writer.write( " int * p;" ); //$NON-NLS-1$
		writer.write( " void main () {" ); //$NON-NLS-1$
		writer.write( "    f( p );" ); //$NON-NLS-1$
		writer.write( "    f( *p );" ); //$NON-NLS-1$
		writer.write( " }" ); //$NON-NLS-1$
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration template1 = (IASTTemplateDeclaration) i.next();
		IASTTemplateParameter T1 = (IASTTemplateParameter) template1.getTemplateParameters().next();
		
		IASTFunction f1 = (IASTFunction) template1.getOwnedDeclaration();
		
		IASTTemplateDeclaration template2 = (IASTTemplateDeclaration) i.next();
		IASTFunction f2 = (IASTFunction) template2.getOwnedDeclaration();
		IASTTemplateParameter T2 = (IASTTemplateParameter) template2.getTemplateParameters().next();
		
		IASTVariable p = (IASTVariable) i.next();
		IASTFunction main = (IASTFunction) i.next();
		assertFalse( i.hasNext() );
		
		assertAllReferences( 6, createTaskList( new Task( T1 ), 
											    new Task( T2 ), 
												new Task( f1, 1, false, false ), 
												new Task( p, 2 ), 
												new Task( f2, 1, false, false ) ) );
		
	}
	
	public void testOverloadedFunctionTemplates_2() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write("template< class T > struct A {  };                 \n"); //$NON-NLS-1$
		writer.write("template< class T > void h( const T & );	//#1     \n"); //$NON-NLS-1$
		writer.write("template< class T > void h( A<T>& );		//#2     \n"); //$NON-NLS-1$
		writer.write("void foo() {                                       \n"); //$NON-NLS-1$
		writer.write("   A<int> z;                                       \n"); //$NON-NLS-1$
		writer.write("   h( z );  //calls 2                              \n"); //$NON-NLS-1$
		
		writer.write("   const A<int> z2;                                \n"); //$NON-NLS-1$
		writer.write("   h( z2 ); //calls 1 because 2 is not callable.   \n"); //$NON-NLS-1$
		writer.write( "}                                                 \n"); //$NON-NLS-1$
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration templateA = (IASTTemplateDeclaration) i.next();
		IASTTemplateDeclaration templateh1 = (IASTTemplateDeclaration) i.next();
		IASTTemplateDeclaration templateh2 = (IASTTemplateDeclaration) i.next();
		
		IASTClassSpecifier A = (IASTClassSpecifier) templateA.getOwnedDeclaration();
		IASTFunction h1 = (IASTFunction) templateh1.getOwnedDeclaration();
		IASTFunction h2 = (IASTFunction) templateh2.getOwnedDeclaration();
		
		IASTTemplateParameter T1 = (IASTTemplateParameter) templateA.getTemplateParameters().next();
		IASTTemplateParameter T2 = (IASTTemplateParameter) templateh1.getTemplateParameters().next();
		IASTTemplateParameter T3 = (IASTTemplateParameter) templateh2.getTemplateParameters().next();
		
		IASTFunction foo = (IASTFunction) i.next();
		assertFalse( i.hasNext() );
		
		i = getDeclarations( foo );
		IASTVariable z = (IASTVariable) i.next();
		IASTVariable z2 = (IASTVariable) i.next();
		assertFalse( i.hasNext() );
		
		assertEquals( ((IASTSimpleTypeSpecifier)z.getAbstractDeclaration().getTypeSpecifier()).getTypeSpecifier(), A );
		assertEquals( ((IASTSimpleTypeSpecifier)z2.getAbstractDeclaration().getTypeSpecifier()).getTypeSpecifier(), A );
		
		assertAllReferences( 9, createTaskList( new Task( T2 ), 
											    new Task( T3 ), 
												new Task( A, 3 ), 
												new Task( z ), 
												new Task( z2 ),
												new Task( h1, 1, false, false ), 	
												new Task( h2, 1, false, false ) ) );
		
		
	}
	
	public void testTemplateClassPartialSpecialization() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "template < class T1, class T2, int I > class A {};  //#1\n" ); //$NON-NLS-1$
		writer.write( "template < class T, int I >            class A < T, T*, I >   {};  //#2\n"); //$NON-NLS-1$
		writer.write( "template < class T1, class T2, int I > class A < T1*, T2, I > {};  //#3\n"); //$NON-NLS-1$
		writer.write( "template < class T >                   class A < int, T*, 5 > {};  //#4\n"); //$NON-NLS-1$
		writer.write( "template < class T1, class T2, int I > class A < T1, T2*, I > {};  //#5\n"); //$NON-NLS-1$

		writer.write( "A <int, int, 1>   a1;		//uses #1 \n"); //$NON-NLS-1$
		writer.write( "A <int, int*, 1>  a2;		//uses #2, T is int, I is 1 \n"); //$NON-NLS-1$
		writer.write( "A <int, char*, 5> a4;		//uses #4, T is char \n"); //$NON-NLS-1$
		writer.write( "A <int, char*, 1> a5;		//uses #5, T is int, T2 is char, I is1 \n"); //$NON-NLS-1$

		Iterator i = parse( writer.toString() ).getDeclarations();
		
		writer.write( "  A <int*, int*, 2> amgiguous; //ambiguous, matches #3 & #5 \n"); //$NON-NLS-1$
		
		try{
			//we expect this parse to fail because of the ambiguity in the last line
			parse( writer.toString() );
			assertFalse( true );
		} catch ( ParserException e ){
			assertEquals( e.getMessage(), "FAILURE" ); //$NON-NLS-1$
		}
	 
		IASTTemplateDeclaration template1 = (IASTTemplateDeclaration) i.next();
		IASTTemplateDeclaration spec2 = (IASTTemplateDeclaration) i.next();
		IASTTemplateDeclaration spec3 = (IASTTemplateDeclaration) i.next();
		IASTTemplateDeclaration spec4 = (IASTTemplateDeclaration) i.next();
		IASTTemplateDeclaration spec5 = (IASTTemplateDeclaration) i.next();
		
		IASTVariable a1 = (IASTVariable) i.next();
		IASTVariable a2 = (IASTVariable) i.next();
		IASTVariable a4 = (IASTVariable) i.next();
		IASTVariable a5 = (IASTVariable) i.next();
		
		assertFalse( i.hasNext() );
		
		IASTClassSpecifier A1 = (IASTClassSpecifier)template1.getOwnedDeclaration();
		IASTClassSpecifier A2 = (IASTClassSpecifier)spec2.getOwnedDeclaration();
		IASTClassSpecifier A3 = (IASTClassSpecifier)spec3.getOwnedDeclaration();
		IASTClassSpecifier A4 = (IASTClassSpecifier)spec4.getOwnedDeclaration();
		IASTClassSpecifier A5 = (IASTClassSpecifier)spec5.getOwnedDeclaration();
		
		assertEquals( ((IASTSimpleTypeSpecifier)a1.getAbstractDeclaration().getTypeSpecifier()).getTypeSpecifier(), A1 );
		assertEquals( ((IASTSimpleTypeSpecifier)a2.getAbstractDeclaration().getTypeSpecifier()).getTypeSpecifier(), A2 );
		assertEquals( ((IASTSimpleTypeSpecifier)a4.getAbstractDeclaration().getTypeSpecifier()).getTypeSpecifier(), A4 );
		assertEquals( ((IASTSimpleTypeSpecifier)a5.getAbstractDeclaration().getTypeSpecifier()).getTypeSpecifier(), A5 );
		
	}
	
	public void testTemplateInstanceAsBaseClause() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "template< class T > class A { T t; };  \n" ); //$NON-NLS-1$
		writer.write( "class B : public A< int > {};          \n" ); //$NON-NLS-1$
		writer.write( "void f( int );                         \n" ); //$NON-NLS-1$
		
		writer.write( "void main(){                           \n" ); //$NON-NLS-1$
		writer.write( "   B b;                                \n" ); //$NON-NLS-1$
		writer.write( "   f( b.t );                           \n" );  //if this function call is good, it implies that b.t is type int //$NON-NLS-1$
		writer.write( "}                                      \n" ); //$NON-NLS-1$
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration template = (IASTTemplateDeclaration) i.next();
		IASTTemplateParameter T = (IASTTemplateParameter) template.getTemplateParameters().next();
		IASTClassSpecifier B = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTFunction f = (IASTFunction) i.next();
		IASTFunction main = (IASTFunction) i.next();
		assertFalse( i.hasNext() );
		
		IASTClassSpecifier A = (IASTClassSpecifier) template.getOwnedDeclaration();
		i = getDeclarations( A );
		IASTField t = (IASTField) i.next();
		assertFalse( i.hasNext() );
		
		i = getDeclarations( main );
		
		IASTVariable b = (IASTVariable) i.next();
		assertFalse( i.hasNext() );
		
		assertAllReferences( 6, createTaskList( new Task( T ), 
											    new Task( A ), 
												new Task( B ), 
												new Task( b ),
												new Task( t ), 	
												new Task( f ) ) );
	}
	
	public void testTemplateParameterAsBaseClause() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "template < class T > class A : public T {};  \n" ); //$NON-NLS-1$
		writer.write( "class B { int i; };                           \n" ); //$NON-NLS-1$
		writer.write( "void main() {                                \n" ); //$NON-NLS-1$
		writer.write( "   A<B> a;                                   \n" ); //$NON-NLS-1$
		writer.write( "   a.i;                                      \n" ); //$NON-NLS-1$
		writer.write( "}                                            \n" ); //$NON-NLS-1$
		writer.write( "\n" ); //$NON-NLS-1$
		
		Iterator iter = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration template = (IASTTemplateDeclaration) iter.next();
		IASTTemplateParameter T = (IASTTemplateParameter) template.getTemplateParameters().next();
		IASTClassSpecifier B = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)iter.next()).getTypeSpecifier();
		IASTFunction main = (IASTFunction) iter.next();
		assertFalse( iter.hasNext() );

		IASTClassSpecifier A = (IASTClassSpecifier) template.getOwnedDeclaration();
		
		iter = getDeclarations( B );
		IASTVariable i = (IASTVariable) iter.next();
		
		iter = getDeclarations( main );
		IASTVariable a = (IASTVariable) iter.next();
		
		assertAllReferences( 5, createTaskList( new Task( T ), new Task( A ), new Task( B ), new Task( a ), new Task( i ) ) ); 	
	}

	public void testTypedefedTemplate() throws Exception{
		Writer writer = new StringWriter();
		writer.write( "template < class T > class _A{ int x; }; \n" ); //$NON-NLS-1$
		writer.write( "typedef _A < char >  A;                  \n" ); //$NON-NLS-1$
		writer.write( "void foo() {                             \n" ); //$NON-NLS-1$
		writer.write( "   A a;                                  \n" ); //$NON-NLS-1$
		writer.write( "   a.x;                                  \n" ); //$NON-NLS-1$
		writer.write( "}                                        \n" ); //$NON-NLS-1$
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration _A = (IASTTemplateDeclaration) i.next();
		IASTTypedefDeclaration A = (IASTTypedefDeclaration) i.next();
		IASTFunction foo = (IASTFunction) i.next();
		
		IASTClassSpecifier classA = (IASTClassSpecifier) _A.getOwnedDeclaration();
		IASTVariable x = (IASTVariable) getDeclarations( classA ).next();
		IASTVariable a = (IASTVariable) getDeclarations( foo ).next();
		
		assertAllReferences( 4, createTaskList( new Task( classA ), new Task( A ), new Task( a ), new Task( x ) ) );
	}
	
	public void testTypedefedTemplate_2() throws Exception{
		Writer writer = new StringWriter();
		writer.write( "template < class T > struct A { T x; };      \n" ); //$NON-NLS-1$
		writer.write( "template < class U > struct B {              \n" ); //$NON-NLS-1$
		writer.write( "   typedef A< U > AU;                        \n" ); //$NON-NLS-1$
		writer.write( "   void f( U );                              \n" ); //$NON-NLS-1$
		writer.write( "   void f( char );                           \n" ); //$NON-NLS-1$
		writer.write( "   void g(){                                 \n" ); //$NON-NLS-1$
		writer.write( "      AU au;                                 \n" ); //$NON-NLS-1$
		writer.write( "      f( au.x );                             \n" ); //$NON-NLS-1$
		writer.write( "   }                                         \n" ); //$NON-NLS-1$
		writer.write( "};                                           \n" ); //$NON-NLS-1$
		writer.write( "void f2( int );                              \n" ); //$NON-NLS-1$
		writer.write( "void f2( char );                             \n" ); //$NON-NLS-1$
		writer.write( "void h(){                                    \n" ); //$NON-NLS-1$
		writer.write( "   B< int >::AU b;                           \n" ); //$NON-NLS-1$
		writer.write( "   f2( b.x );                                \n" ); //$NON-NLS-1$
		writer.write( "}                                            \n" ); //$NON-NLS-1$
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration tA = (IASTTemplateDeclaration) i.next();
		IASTTemplateParameter T = (IASTTemplateParameter) tA.getTemplateParameters().next();
		IASTClassSpecifier A = (IASTClassSpecifier) tA.getOwnedDeclaration();
		IASTField x = (IASTField) getDeclarations( A ).next();
		IASTTemplateDeclaration tB = (IASTTemplateDeclaration) i.next();
		IASTClassSpecifier B = (IASTClassSpecifier) tB.getOwnedDeclaration();
		IASTTemplateParameter U = (IASTTemplateParameter) tB.getTemplateParameters().next();
		IASTFunction f21 = (IASTFunction) i.next();
		IASTFunction f22 = (IASTFunction) i.next();
		IASTFunction h = (IASTFunction) i.next();
		
		i = getDeclarations( B );
		IASTTypedefDeclaration AU = (IASTTypedefDeclaration) i.next(); 
		IASTMethod f11 = (IASTMethod) i.next();
		IASTMethod f12 = (IASTMethod) i.next();
		IASTMethod g = (IASTMethod) i.next();
		
		IASTVariable au = (IASTVariable) getDeclarations( g ).next();
		IASTVariable b = (IASTVariable) getDeclarations( h ).next();
		
		assertAllReferences( 13, createTaskList( new Task( A ),
												 new Task( T ),
												 new Task( U, 2 ),
				                                 new Task( AU, 2 ),
								  			     new Task( au ),
								  			     new Task( x, 2 ),
											     new Task( f11, 1, false, false ),
											     new Task( B ),
											     new Task( b ),
											     new Task( f21, 1, false, false ) ) );
	}
	
	public void testInstantiatingDeferredInstances() throws Exception{
		Writer writer = new StringWriter();
		writer.write( "template < class T > struct A { A < T > next; };  \n" ); //$NON-NLS-1$
		writer.write( "A< int > a; \n" ); //$NON-NLS-1$
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration template = (IASTTemplateDeclaration) i.next();
		IASTTemplateParameter T = (IASTTemplateParameter) template.getTemplateParameters().next();
		IASTClassSpecifier A = (IASTClassSpecifier) template.getOwnedDeclaration();
		IASTField next = (IASTField) getDeclarations( A ).next();
		IASTVariable a = (IASTVariable) i.next();
		
		assertAllReferences( 3, createTaskList( new Task( A, 2 ), new Task( T ) ) );
	}
	
	public void testTemplateArgumentDeduction() throws Exception{
		Writer writer = new StringWriter();
		writer.write( "template< class T > struct B {};                   \n" ); //$NON-NLS-1$
		writer.write( "template< class T > struct D : public B < T > {};  \n" ); //$NON-NLS-1$
		writer.write( "struct D2 : public B< int > {};                    \n" ); //$NON-NLS-1$
		writer.write( "template< class T > T f( B<T> & ) {}               \n" ); //$NON-NLS-1$
		writer.write( "void test( int );                                  \n" ); //$NON-NLS-1$
		writer.write( "void test( char );                                 \n" ); //$NON-NLS-1$
		writer.write( "void main() {                                      \n" ); //$NON-NLS-1$
		writer.write( "   D<int> d;                                       \n" ); //$NON-NLS-1$
		writer.write( "   D2     d2;                                      \n" ); //$NON-NLS-1$
		writer.write( "   test( f( d ) );                                 \n" ); //$NON-NLS-1$
		writer.write( "   test( f( d2 ) );                                \n" ); //$NON-NLS-1$
		writer.write( "}                                                  \n" ); //$NON-NLS-1$
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration templateB = (IASTTemplateDeclaration) i.next();
		IASTTemplateDeclaration templateD = (IASTTemplateDeclaration) i.next();
		IASTClassSpecifier D2 = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTTemplateDeclaration templateF = (IASTTemplateDeclaration) i.next();
		IASTFunction test1 = (IASTFunction) i.next();
		IASTFunction test2 = (IASTFunction) i.next();
		IASTFunction main = (IASTFunction) i.next();
		
		assertFalse( i.hasNext() );		
		assertReferenceTask( new Task( test1, 2, false, false ) );
	}
	public void testClassTemplateStaticMemberDefinition() throws Exception {
		Writer writer = new StringWriter();
		writer.write( "template< class T > class A{                      \n" ); //$NON-NLS-1$
		writer.write( "   typedef T * PT;                                \n" ); //$NON-NLS-1$
		writer.write( "   static T member;                               \n" ); //$NON-NLS-1$
		writer.write( "};                                                \n" ); //$NON-NLS-1$
		writer.write( "template< class T> A<T>::PT A<T>::member = null;  \n" ); //$NON-NLS-1$
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration template = (IASTTemplateDeclaration) i.next();
		IASTTemplateParameter T1 = (IASTTemplateParameter) template.getTemplateParameters().next();
		IASTTemplateDeclaration template2 = (IASTTemplateDeclaration) i.next();
		IASTTemplateParameter T2 = (IASTTemplateParameter) template2.getTemplateParameters().next();
		
		IASTField member = (IASTField) getDeclarations( template2 ).next();
		assertEquals( member.getName(), "member" ); //$NON-NLS-1$
		
		assertReferenceTask( new Task( T1, 2, false, false ) );
		assertReferenceTask( new Task( T2, 2, false, false ) );
	}
	
	public void testTemplateTemplateParameter() throws Exception{
		Writer writer = new StringWriter();
		writer.write( " template< class T > class A {                    "); //$NON-NLS-1$
		writer.write( "    int x;                                        "); //$NON-NLS-1$
		writer.write( " };                                               "); //$NON-NLS-1$
		writer.write( " template < class T > class A < T * > {           "); //$NON-NLS-1$
		writer.write( "    long x;                                       "); //$NON-NLS-1$
		writer.write( " };                                               "); //$NON-NLS-1$
		writer.write( " template< template< class U > class V > class C{ "); //$NON-NLS-1$
		writer.write( "    V< int > y;                                   "); //$NON-NLS-1$
		writer.write( "    V< int * > z;                                 "); //$NON-NLS-1$
		writer.write( " };                                               "); //$NON-NLS-1$
		writer.write( " void f( int );                                   "); //$NON-NLS-1$
		writer.write( " void f( long );                                  "); //$NON-NLS-1$
		writer.write( " void main() {                                    "); //$NON-NLS-1$
		writer.write( "    C< A > c;                                     "); //$NON-NLS-1$
		writer.write( "    f( c.y.x );                                   "); //$NON-NLS-1$
		writer.write( "    f( c.z.x );                                   "); //$NON-NLS-1$
		writer.write( " }                                                "); //$NON-NLS-1$
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration templateA = (IASTTemplateDeclaration) i.next();
		IASTTemplateDeclaration templateA2 = (IASTTemplateDeclaration) i.next();
		IASTTemplateDeclaration templateC = (IASTTemplateDeclaration) i.next();
		
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		
		IASTFunction main = (IASTFunction) i.next();
		IASTVariable c = (IASTVariable) getDeclarations( main ).next();
		
		IASTSimpleTypeSpecifier spec = (IASTSimpleTypeSpecifier) c.getAbstractDeclaration().getTypeSpecifier();
		IASTClassSpecifier C = (IASTClassSpecifier) spec.getTypeSpecifier();
		
		assertReferenceTask( new Task( f1, 1, false, false ) );
		assertReferenceTask( new Task( f2, 1, false, false ) );
	}
	
	public void testBug56834() throws Exception{
		Iterator i = parse( "template < class T, class U = T > class A;" ).getDeclarations(); //$NON-NLS-1$
		
		IASTTemplateDeclaration template = (IASTTemplateDeclaration) i.next();
		
		assertFalse( i.hasNext() );
		
		i = template.getTemplateParameters();
		
		IASTTemplateParameter T = (IASTTemplateParameter) i.next();
		IASTTemplateParameter U = (IASTTemplateParameter) i.next();
	}
	
	public void testDefaultTemplateParameters() throws Exception {
		Iterator i = parse( "template < class T = int > class A{};  A<> a;" ).getDeclarations(); //$NON-NLS-1$
		
		IASTTemplateDeclaration template = (IASTTemplateDeclaration) i.next();
		IASTVariable a = (IASTVariable) i.next();
	}
	
	public void testBug56834WithInstantiation() throws Exception{
		Writer writer = new StringWriter();
		writer.write( "template< class T, class U = T > class A {};" ); //$NON-NLS-1$
		writer.write( "A< char > a;" ); //$NON-NLS-1$
		Iterator i = parse(  writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration template = (IASTTemplateDeclaration) i.next();
		IASTVariable a = (IASTVariable) i.next();
	}
	
	public void testDefaultTemplateParameterWithDeferedInstance() throws Exception{
		Writer writer = new StringWriter();
		writer.write( "template < class T > class A;  \n" ); //$NON-NLS-1$
		writer.write( "template < class U, class V = A< U > > class B; \n" ); //$NON-NLS-1$
		writer.write( "B< int > b;" ); //$NON-NLS-1$
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration templateA = (IASTTemplateDeclaration) i.next();
		IASTTemplateDeclaration templateB = (IASTTemplateDeclaration) i.next();
		IASTVariable b = (IASTVariable) i.next();
	}
	
	public void testExplicitInstantiation() throws Exception{
		
		Writer writer = new StringWriter();
		writer.write( "template < class T > class A { }; " ); //$NON-NLS-1$
		writer.write( "template class A< int >; " ); //$NON-NLS-1$
		writer.write( "A< int > a; " ); //$NON-NLS-1$
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration template = (IASTTemplateDeclaration) i.next();
		IASTClassSpecifier A = (IASTClassSpecifier) template.getOwnedDeclaration();
		IASTTemplateInstantiation instance = (IASTTemplateInstantiation) i.next();
		IASTVariable var = (IASTVariable) i.next();
		
		assertAllReferences( 2, createTaskList( new Task( A, 2 ) ) );
	}
	
	public void testTemplateParametersInExpressions() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "template < typename _Tp > power( _Tp, unsigned int );     \n" ); //$NON-NLS-1$
		writer.write( "template < typename _Tp > _Tp helper( _Tp __x, int _n )   \n" ); //$NON-NLS-1$
		writer.write( "{ " ); //$NON-NLS-1$
		writer.write( "   return n < 0 ? _Tp( 1 ) / power( __x, -__n )           \n" ); //$NON-NLS-1$
		writer.write( "                : power( __x, __n );                      \n" ); //$NON-NLS-1$
		writer.write( "} " ); //$NON-NLS-1$
		
		parse( writer.toString () );
	}
	
	public void testBug44338() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "template < bool T > class A {   "); //$NON-NLS-1$
		writer.write( "   void foo( bool b = T );      "); //$NON-NLS-1$
		writer.write( "};                              "); //$NON-NLS-1$
		writer.write( "typedef A< 1 < 2 > A_TRUE;      "); //$NON-NLS-1$
		writer.write( "typedef A< ( 1 > 2 ) > A_FALSE; "); //$NON-NLS-1$
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration template = (IASTTemplateDeclaration) i.next();
		IASTTypedefDeclaration  a_true = (IASTTypedefDeclaration) i.next();
		IASTTypedefDeclaration  a_false = (IASTTypedefDeclaration) i.next();
	}
	
	public void testBug44338_2() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "template < int i > class X {};   "); //$NON-NLS-1$
		writer.write( "template < class T > class Y {}; "); //$NON-NLS-1$
		writer.write( "Y< X < 1 > > y1;                 "); //$NON-NLS-1$
		writer.write( "Y< X < 6 >> 1 > > y2;            "); //$NON-NLS-1$
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration templateX = (IASTTemplateDeclaration) i.next();
		IASTTemplateDeclaration templateY = (IASTTemplateDeclaration) i.next();
		IASTVariable y1 = (IASTVariable) i.next();
		IASTVariable y2 = (IASTVariable) i.next();
	}
	
	public void testBug4338_3() throws Exception
	{
		try{
			//this is expected to fail the parse
			parse( "template < int i > class X {};  X< 1 > 2 > x; " ); //$NON-NLS-1$
			assertTrue( false );
		} catch ( ParserException e ){
			assertTrue( e.getMessage().equals( "FAILURE" ) ); //$NON-NLS-1$
		}
	}
	
	public void testBug57754() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write("template < class T > class A{                      "); //$NON-NLS-1$
		writer.write("   typedef int _type;                              "); //$NON-NLS-1$
		writer.write("   void f( _type, T );                             "); //$NON-NLS-1$
		writer.write("};                                                 "); //$NON-NLS-1$
		writer.write("template < class T > void A< T >::f( _type, T ) {} "); //$NON-NLS-1$
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration template = (IASTTemplateDeclaration) i.next();
		IASTClassSpecifier cls = (IASTClassSpecifier) template.getOwnedDeclaration();
		
		i = getDeclarations( cls );
		IASTTypedefDeclaration _type = (IASTTypedefDeclaration) i.next();
		
		assertReferenceTask( new Task( _type, 2 ) );
	}
	
	public void testContructorsAndExplicitSpecialization() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write("template < class T > class A {  "); //$NON-NLS-1$
		writer.write("   A();                         "); //$NON-NLS-1$
		writer.write("   A( int );                    "); //$NON-NLS-1$
		writer.write("   ~A();                        "); //$NON-NLS-1$
		writer.write("};                              "); //$NON-NLS-1$
		writer.write("template <> A< char >::~A();    "); //$NON-NLS-1$

		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration template = (IASTTemplateDeclaration) i.next();
		IASTTemplateSpecialization spec = (IASTTemplateSpecialization) i.next();
	}
	
	public void testTemplateMemberTemplateDefinition() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "template< typename _Tp >  						"); //$NON-NLS-1$
		writer.write( "class list										"); //$NON-NLS-1$
		writer.write( "{												"); //$NON-NLS-1$
		writer.write( "   template<typename _S> void merge(list&, _S);  "); //$NON-NLS-1$
		writer.write( "};												");         //$NON-NLS-1$
	
		writer.write( "template < typename _Tp >						"); //$NON-NLS-1$
		writer.write( "template < typename _S  >						"); //$NON-NLS-1$
		writer.write( "void list<_Tp>::merge(list<_Tp>& __x, _S __comp)	"); //$NON-NLS-1$
		writer.write( "{}												"); //$NON-NLS-1$
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration template = (IASTTemplateDeclaration) i.next();
		IASTTemplateDeclaration temp2 = (IASTTemplateDeclaration) i.next();
	}
	
	public void test_14_7_3__5_ExplicitSpecialization() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write("template< class T > struct A {  "); //$NON-NLS-1$
		writer.write("   void f( T ) {}               "); //$NON-NLS-1$
		writer.write("};                              "); //$NON-NLS-1$
		writer.write("template <> struct A< int >{    "); //$NON-NLS-1$
		writer.write("   void f( int );               "); //$NON-NLS-1$
		writer.write("};                               "); //$NON-NLS-1$
		writer.write("void A< int >::f( int ){ }      "); //$NON-NLS-1$
		
		writer.write("void main(){                    "); //$NON-NLS-1$
		writer.write("   A<int> a;                    "); //$NON-NLS-1$
		writer.write("   a.f( 1 );                    "); //$NON-NLS-1$
		writer.write("}                               "); //$NON-NLS-1$
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration template = (IASTTemplateDeclaration) i.next();
		IASTTemplateParameter T = (IASTTemplateParameter) template.getTemplateParameters().next();
		IASTTemplateSpecialization spec = (IASTTemplateSpecialization) i.next();
		IASTMethod f = (IASTMethod) i.next();
		IASTFunction main = (IASTFunction) i.next();
		
		IASTClassSpecifier ASpec = (IASTClassSpecifier) spec.getOwnedDeclaration();
		
		i = getDeclarations( main );
		IASTVariable a = (IASTVariable) i.next();
		
		assertAllReferences( 5, createTaskList( new Task( T ), new Task( ASpec, 2 ), new Task( a ), new Task( f ) ) );
	}
	public void test_14_7_3__11_ExplicitSpecializationArgumentDeduction() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write("template < class T > class Array { };             "); //$NON-NLS-1$
		writer.write("template < class T > void sort( Array< T > & );   "); //$NON-NLS-1$
		writer.write("template<> void sort( Array< int > & ){}          "); //$NON-NLS-1$
		writer.write("void f(){                                         "); //$NON-NLS-1$
		writer.write("   Array<int> a1;                                 "); //$NON-NLS-1$
		writer.write("   Array<char> a2;                                "); //$NON-NLS-1$
		writer.write("   sort( a1 );                                    "); //$NON-NLS-1$
		writer.write("   sort( a2 );                                    "); //$NON-NLS-1$
		writer.write("}                                                 "); //$NON-NLS-1$
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration templateArray = (IASTTemplateDeclaration) i.next();
		IASTTemplateDeclaration templateSort = (IASTTemplateDeclaration) i.next();
		IASTTemplateDeclaration sortSpec = (IASTTemplateDeclaration) i.next();
		IASTFunction f = (IASTFunction) i.next();
		
		IASTFunction sort1 = (IASTFunction) templateSort.getOwnedDeclaration();
		IASTFunction sort2 = (IASTFunction) sortSpec.getOwnedDeclaration();
		
		assertReferenceTask( new Task( sort1, 1, false, false ) );
		assertReferenceTask( new Task( sort2, 1, false, false ) );
	}
	
	public void test_14_8_1__2_ExplicitArgumentSpecification() throws Exception{
		Writer writer = new StringWriter();
		writer.write("void f( int ){}                    //#1   \n"); //$NON-NLS-1$
		writer.write("template < class T > void f( T ){} //#2   \n"); //$NON-NLS-1$
		writer.write("int main(){                               \n"); //$NON-NLS-1$
		writer.write("    f( 1 );      //calls #1               \n"); //$NON-NLS-1$
		writer.write("    f<int>( 1 ); //calls #2               \n"); //$NON-NLS-1$
		writer.write("    f<>   ( 1 ); //calls #2               \n"); //$NON-NLS-1$
		writer.write("}                                         \n"); //$NON-NLS-1$
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTFunction f1 = (IASTFunction) i.next();
		IASTTemplateDeclaration template = (IASTTemplateDeclaration) i.next();
		IASTFunction f2 = (IASTFunction) template.getOwnedDeclaration();
		IASTFunction main = (IASTFunction) i.next();
		
		assertReferenceTask( new Task( f1, 1, false, false ) );
		assertReferenceTask( new Task( f2, 2, false, false ) );
	}

	public void test_14_7_3__12_ExplicitSpecializationOverloadedFunction() throws Exception{
		Writer writer = new StringWriter();
		writer.write("template< class T > void f( T );   "); //$NON-NLS-1$
		writer.write("template< class T > void f( T * ); "); //$NON-NLS-1$
		writer.write("template <> void f< int*>( int * );"); //$NON-NLS-1$
		writer.write("template <> void f< int >( int * );"); //$NON-NLS-1$
		writer.write("template <> void f( char );        "); //$NON-NLS-1$

		parse( writer.toString() );
	}
	
	public void testPartialSpecializationDefinitions() throws Exception{
		Writer writer = new StringWriter();
		writer.write("template < class T1, class T2 > class A  { void f(); };"); //$NON-NLS-1$
		writer.write("template < class T > class A < T, T >    { void f(); };"); //$NON-NLS-1$
		writer.write("template < class T > class A < char, T > { void f(); };"); //$NON-NLS-1$
		
		writer.write("template < class U, class V > void A<U, V>::f(){}      "); //$NON-NLS-1$
		writer.write("template < class W > void A < W, W >::f(){}            "); //$NON-NLS-1$
		writer.write("template < class X > void A < char, X >::f(){}         "); //$NON-NLS-1$
		
		writer.write("void main(){                                           "); //$NON-NLS-1$
		writer.write("   A< int, char > a1;                                  "); //$NON-NLS-1$
		writer.write("   a1.f();                                             "); //$NON-NLS-1$
		writer.write("   A< int, int > a2;                                   "); //$NON-NLS-1$
		writer.write("   a2.f();                                             "); //$NON-NLS-1$
		writer.write("   A< char, int > a3;                                  "); //$NON-NLS-1$
		writer.write("   a3.f();                                             "); //$NON-NLS-1$
		writer.write("}                                                      "); //$NON-NLS-1$
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration t1 = (IASTTemplateDeclaration) i.next();
		IASTTemplateDeclaration t2 = (IASTTemplateDeclaration) i.next();
		IASTTemplateDeclaration t3 = (IASTTemplateDeclaration) i.next();
		IASTTemplateDeclaration t4 = (IASTTemplateDeclaration) i.next();
		IASTTemplateDeclaration t5 = (IASTTemplateDeclaration) i.next();
		IASTTemplateDeclaration t6 = (IASTTemplateDeclaration) i.next();
		
		IASTFunction main = (IASTFunction) i.next();
		assertFalse( i.hasNext() );
		
		IASTMethod f1 = (IASTMethod) t4.getOwnedDeclaration();
		IASTMethod f2 = (IASTMethod) t5.getOwnedDeclaration();
		IASTMethod f3 = (IASTMethod) t6.getOwnedDeclaration();
		
		assertReferenceTask( new Task( f1, 1, false, false ) );
		assertReferenceTask( new Task( f2, 1, false, false ) );
		assertReferenceTask( new Task( f3, 1, false, false ) );
	}
	
	public void test_14_5_2__2_MemberFunctionTemplates() throws Exception{
		Writer writer = new StringWriter();
		writer.write("template < class T > struct A {                                        "); //$NON-NLS-1$
		writer.write("   void f( int );                                                      "); //$NON-NLS-1$
		writer.write("   template < class T2 > void f( T2 );                                 "); //$NON-NLS-1$
		writer.write("};                                                                     "); //$NON-NLS-1$
		
		writer.write("template <> void A<int>::f(int) {}  //non-template member            \n"); //$NON-NLS-1$
		writer.write("template <> template<> void A<int>::f<>( int ) {} //template member  \n"); //$NON-NLS-1$
		
		writer.write("int main(){                                                            "); //$NON-NLS-1$
		writer.write("   A< int > ac;                                                        "); //$NON-NLS-1$
		writer.write("   ac.f( 1 );   //non-template                                       \n"); //$NON-NLS-1$
		writer.write("   ac.f( 'c' ); //template                                           \n"); //$NON-NLS-1$
		writer.write("   ac.f<>(1);   //template                                           \n"); //$NON-NLS-1$
		writer.write("}                                                                      "); //$NON-NLS-1$
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration template = (IASTTemplateDeclaration) i.next();
		IASTTemplateDeclaration spec1 = (IASTTemplateDeclaration) i.next();
		IASTTemplateDeclaration spec2 = (IASTTemplateDeclaration) i.next();
		
		IASTMethod f1 = (IASTMethod) spec1.getOwnedDeclaration();
		IASTMethod f2 = (IASTMethod) spec2.getOwnedDeclaration();;
		
		IASTFunction main = (IASTFunction) i.next();
		assertFalse( i.hasNext() );
		
		assertReferenceTask( new Task( f1, 1, false, false ) );
		//we aren't going to be completely correct about references to explicit specializations
		//due to limitations in the implementation, see bug 59811
	}
	
	public void testBug64753() throws Exception{
		Writer writer = new StringWriter();
		writer.write( "template < class _T > void foo () {  \n" ); //$NON-NLS-1$
		writer.write( "   if( 1 ) {                         \n" ); //$NON-NLS-1$
		writer.write( "      _T p1, p2;                     \n" ); //$NON-NLS-1$
		writer.write( "      int n = p1 - p2;               \n" ); //$NON-NLS-1$
		writer.write( "   }                                 \n" ); //$NON-NLS-1$
		writer.write( "}                                    \n" ); //$NON-NLS-1$
		
		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTTemplateDeclaration foo = (IASTTemplateDeclaration) i.next();
	}
	
	public void testBug64919() throws Exception{
		Writer writer = new StringWriter();
		writer.write("class Foo{};                                                   "); //$NON-NLS-1$
		writer.write("class Bar{};                                                   "); //$NON-NLS-1$
		writer.write("template <class T, class U> class A {};                        "); //$NON-NLS-1$
		writer.write("template < class X > class A < X, X > : public A< X, Bar>      "); //$NON-NLS-1$
		writer.write("{   typedef int TYPE;   };                                     "); //$NON-NLS-1$
		writer.write("template < class X > class A < X, Foo > : public A< X, X >     "); //$NON-NLS-1$
		writer.write("{   void f ( TYPE );  };                                       "); //$NON-NLS-1$

		Iterator i = parse( writer.toString() ).getDeclarations();
		
		IASTClassSpecifier Foo = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTClassSpecifier Bar = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTTemplateDeclaration T1 = (IASTTemplateDeclaration) i.next();
		IASTTemplateDeclaration T2 = (IASTTemplateDeclaration) i.next();
		IASTTemplateDeclaration T3 = (IASTTemplateDeclaration) i.next();
		
		IASTClassSpecifier A1 = (IASTClassSpecifier) T1.getOwnedDeclaration();
		IASTClassSpecifier A2 = (IASTClassSpecifier) T2.getOwnedDeclaration();
		IASTClassSpecifier A3 = (IASTClassSpecifier) T3.getOwnedDeclaration();
		
		IASTBaseSpecifier parent = (IASTBaseSpecifier) A2.getBaseClauses().next();
		assertEquals( parent.getParentClassSpecifier(), A1 );
		
		parent = (IASTBaseSpecifier) A3.getBaseClauses().next();
		assertEquals( parent.getParentClassSpecifier(), A2 );
	}
}
