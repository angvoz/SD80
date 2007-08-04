/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import org.eclipse.cdt.core.parser.ParserLanguage;

/**
 *
 * NOTE:  Once these tests pass (are fixed) then fix the test to work so that they
 * are tested for a pass instead of a failure and move them to AST2CPPSpecTest.java.
 * 
 * @author dsteffle
 */
public class AST2CPPSpecFailingTest extends AST2SpecBaseTest {

	
	
	public AST2CPPSpecFailingTest() {
	}

	public AST2CPPSpecFailingTest(String name) {
		super(name);
	}

	/**
	 [--Start Example(CPP 2.3-2):
	 ??=define arraycheck(a,b) a??(b??) ??!??! b??(a??)
	 // becomes
	 #define arraycheck(a,b) a[b] || b[a]
	 --End Example]
	 */
	public void test2_3s2()  { // TODO exists bug 64993
		StringBuffer buffer = new StringBuffer();
		buffer.append("??=define arraycheck(a,b) a??(b??) ??!??! b??(a??)\n"); //$NON-NLS-1$
		buffer.append("// becomes\n"); //$NON-NLS-1$
		buffer.append("#define arraycheck(a,b) a[b] || b[a]\n"); //$NON-NLS-1$
		
		try {
		parseCandCPP(buffer.toString(), true, 0);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 6.4-3):
	int foo() {
	if (int x = f()) {
	int x; // illformed,redeclaration of x
	}
	else {
	int x; // illformed,redeclaration of x
	}
	}
	 --End Example]
	 */
	public void test6_4s3()  { // TODO raised bug 90618
		StringBuffer buffer = new StringBuffer();
		buffer.append("int foo() {\n"); //$NON-NLS-1$
		buffer.append("if (int x = f()) {\n"); //$NON-NLS-1$
		buffer.append("int x; // illformed,redeclaration of x\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("else {\n"); //$NON-NLS-1$
		buffer.append("int x; // illformed,redeclaration of x\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 0); //Andrew, there should be problem bindings here - 2
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 6.8-2):
	class T {
	// ...
	public:
	T();
	T(int);
	T(int, int);
	};
	T(a); //declaration
	T(*b)(); //declaration
	T(c)=7; //declaration
	T(d),e,f=3; //declaration
	extern int h;
	T(g)(h,2); //declaration
	 --End Example]
	 */
	public void test6_8s2()  { // TODO raised bug 90622
		StringBuffer buffer = new StringBuffer();
		buffer.append("class T {\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("T();\n"); //$NON-NLS-1$
		buffer.append("T(int);\n"); //$NON-NLS-1$
		buffer.append("T(int, int);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("T(a); //declaration\n"); //$NON-NLS-1$
		buffer.append("T(*b)(); //declaration\n"); //$NON-NLS-1$
		buffer.append("T(c)=7; //declaration\n"); //$NON-NLS-1$
		buffer.append("T(d),e,f=3; //declaration\n"); //$NON-NLS-1$
		buffer.append("extern int h;\n"); //$NON-NLS-1$
		buffer.append("T(g)(h,2); //declaration\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
		assertTrue(false);
		} catch (Exception e) {
		}
	}



	/**
	 [--Start Example(CPP 7.3.3-12):
	struct B {
	virtual void f(int);
	virtual void f(char);
	void g(int);
	void h(int);
	};
	struct D : B {
	using B::f;
	void f(int); // OK: D::f(int) overrides B::f(int);
	using B::g;
	void g(char); // OK
	using B::h;
	void h(int); // OK: D::h(int) hides B::h(int)
	};
	void k(D* p)
	{
	p->f(1); //calls D::f(int)
	p->f('a'); //calls B::f(char)
	p->g(1); //calls B::g(int)
	p->g('a'); //calls D::g(char)
	}
	 --End Example]
	 */
	// raised bug 161562 for that
	public void test7_3_3s12() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct B {\n"); //$NON-NLS-1$
		buffer.append("virtual void f(int);\n"); //$NON-NLS-1$
		buffer.append("virtual void f(char);\n"); //$NON-NLS-1$
		buffer.append("void g(int);\n"); //$NON-NLS-1$
		buffer.append("void h(int);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("struct D : B {\n"); //$NON-NLS-1$
		buffer.append("using B::f;\n"); //$NON-NLS-1$
		buffer.append("void f(int); // OK: D::f(int) overrides B::f(int);\n"); //$NON-NLS-1$
		buffer.append("using B::g;\n"); //$NON-NLS-1$
		buffer.append("void g(char); // OK\n"); //$NON-NLS-1$
		buffer.append("using B::h;\n"); //$NON-NLS-1$
		buffer.append("void h(int); // OK: D::h(int) hides B::h(int)\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void k(D* p)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("p->f(1); //calls D::f(int)\n"); //$NON-NLS-1$
		buffer.append("p->f('a'); //calls B::f(char)\n"); //$NON-NLS-1$
		buffer.append("p->g(1); //calls B::g(int)\n"); //$NON-NLS-1$
		buffer.append("p->g('a'); //calls D::g(char)\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		try {
			parse(buffer.toString(), ParserLanguage.CPP, true, 0);
			assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 8.2-7a):
	class C { };
	void f(int(C)) { } // void f(int (*fp)(C c)) { }
	// not: void f(int C);
	int g(C);
	void foo() {
	f(1); //error: cannot convert 1 to function pointer
	f(g); //OK
	}
	 --End Example]
	 */
	public void test8_2s7a()  { // TODO raised bug 90633
		StringBuffer buffer = new StringBuffer();
		buffer.append("class C { };\n"); //$NON-NLS-1$
		buffer.append("void f(int(C)) { } // void f(int (*fp)(C c)) { }\n"); //$NON-NLS-1$
		buffer.append("// not: void f(int C);\n"); //$NON-NLS-1$
		buffer.append("int g(C);\n"); //$NON-NLS-1$
		buffer.append("void foo() {\n"); //$NON-NLS-1$
		buffer.append("f(1); //error: cannot convert 1 to function pointer\n"); //$NON-NLS-1$
		buffer.append("f(g); //OK\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 8.5.2-1):
	char msg[] = "Syntax error on line %s\n";
	 --End Example]
	 */
	public void test8_5_2s1()  { // TODO raised bug 90647
		StringBuffer buffer = new StringBuffer();
		buffer.append("char msg[] = \"Syntax error on line %s\n\";\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.5.3-1):
	template<class T> class task;
	template<class T> task<T>* preempt(task<T>*);
	template<class T> class task {
	// ...
	friend void next_time();
	friend void process(task<T>*);
	friend task<T>* preempt<T>(task<T>*);
	template<class C> friend int func(C);
	friend class task<int>;
	template<class P> friend class frd;
	// ...
	};
	 --End Example]
	 */
	public void test14_5_3s1()  { // TODO raised bug 90678
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> class task;\n"); //$NON-NLS-1$
		buffer.append("template<class T> task<T>* preempt(task<T>*);\n"); //$NON-NLS-1$
		buffer.append("template<class T> class task {\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("friend void next_time();\n"); //$NON-NLS-1$
		buffer.append("friend void process(task<T>*);\n"); //$NON-NLS-1$
		buffer.append("friend task<T>* preempt<T>(task<T>*);\n"); //$NON-NLS-1$
		buffer.append("template<class C> friend int func(C);\n"); //$NON-NLS-1$
		buffer.append("friend class task<int>;\n"); //$NON-NLS-1$
		buffer.append("template<class P> friend class frd;\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.5.4.2-2):
	template<int I, int J, class T> class X { };
	template<int I, int J> class X<I, J, int> { }; // #1
	template<int I> class X<I, I, int> { }; // #2
	template<int I, int J> void f(X<I, J, int>); // #A
	template<int I> void f(X<I, I, int>); // #B
	 --End Example]
	 */
	public void test14_5_4_2s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<int I, int J, class T> class X { };\n"); //$NON-NLS-1$
		buffer.append("template<int I, int J>          class X<I, J, int> { }; // #1\n"); //$NON-NLS-1$
		buffer.append("template<int I>                 class X<I, I, int> { }; // #2\n"); //$NON-NLS-1$
		buffer.append("template<int I, int J> void f(X<I, J, int>); // #A\n"); //$NON-NLS-1$
		buffer.append("template<int I>        void f(X<I, I, int>); // #B\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 2);
	}

	/**
	 [--Start Example(CPP 14.5.5.1-5):
	template <int I, int J> A<I+J> f(A<I>, A<J>); // #1
	template <int K, int L> A<K+L> f(A<K>, A<L>); // same as #1
	template <int I, int J> A<IJ> f(A<I>, A<J>); // different from #1
	 --End Example]
	 */
	public void test14_5_5_1s5()  { // TODO raised bug 90683
		StringBuffer buffer = new StringBuffer();
		buffer.append("template <int I, int J> A<I+J> f(A<I>, A<J>); // #1\n"); //$NON-NLS-1$
		buffer.append("template <int K, int L> A<K+L> f(A<K>, A<L>); // same as #1\n"); //$NON-NLS-1$
		buffer.append("template <int I, int J> A<IJ> f(A<I>, A<J>); // different from #1\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.5.5.1-6):
	template <int I, int J> void f(A<I+J>); // #1
	template <int K, int L> void f(A<K+L>); // same as #1
	 --End Example]
	 */
	public void test14_5_5_1s6()  { // TODO raised bug 90683
		StringBuffer buffer = new StringBuffer();
		buffer.append("template <int I, int J> void f(A<I+J>); // #1\n"); //$NON-NLS-1$
		buffer.append("template <int K, int L> void f(A<K+L>); // same as #1\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.6.2-3):
	typedef double A;
	template<class T> B {
	typedef int A;
	};
	template<class T> struct X : B<T> {
	A a; // a has type double
	};
	 --End Example]
	 */
	public void test14_6_2s3()  { // TODO this doesn't compile via g++ ?
		StringBuffer buffer = new StringBuffer();
		buffer.append("typedef double A;\n"); //$NON-NLS-1$
		buffer.append("template<class T> B {\n"); //$NON-NLS-1$
		buffer.append("typedef int A;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("template<class T> struct X : B<T> {\n"); //$NON-NLS-1$
		buffer.append("A a; // a has type double\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.8.2-2b):
	template <class T> int f(typename T::B*);
	int i = f<int>(0);
	 --End Example]
	 */
	public void test14_8_2s2b()  {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template <class T> int f(typename T::B*);\n"); //$NON-NLS-1$
		buffer.append("int i = f<int>(0);\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 1);
		assertTrue(false);
		} catch (Exception e) {
		}
	}
	
	/**
	 [--Start Example(CPP 14.8.2-2c):
	template <class T> int f(typename T::B*);
	struct A {};
	struct C { int B; };
	int i = f<A>(0);
	int j = f<C>(0);
	 --End Example]
	 */
	public void test14_8_2s2c()  {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template <class T> int f(typename T::B*);\n"); //$NON-NLS-1$
		buffer.append("struct A {};\n"); //$NON-NLS-1$
		buffer.append("struct C { int B; };\n"); //$NON-NLS-1$
		buffer.append("int i = f<A>(0);\n"); //$NON-NLS-1$
		buffer.append("int j = f<C>(0);\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 2);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.8.2-3):
	template <class T> void f(T t);
	template <class X> void g(const X x);
	template <class Z> void h(Z, Z*);
	int main()
	{
	// #1: function type is f(int), t is nonconst
	f<int>(1);
	// #2: function type is f(int), t is const
	f<const int>(1);
	// #3: function type is g(int), x is const
	g<int>(1);
	// #4: function type is g(int), x is const
	g<const int>(1);
	// #5: function type is h(int, const int*)
	h<const int>(1,0);
	}
	 --End Example]
	 */
	public void test14_8_2s3()  {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template <class T> void f(T t);\n"); //$NON-NLS-1$
		buffer.append("template <class X> void g(const X x);\n"); //$NON-NLS-1$
		buffer.append("template <class Z> void h(Z, Z*);\n"); //$NON-NLS-1$
		buffer.append("int main()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("// #1: function type is f(int), t is nonconst\n"); //$NON-NLS-1$
		buffer.append("f<int>(1);\n"); //$NON-NLS-1$
		buffer.append("// #2: function type is f(int), t is const\n"); //$NON-NLS-1$
		buffer.append("f<const int>(1);\n"); //$NON-NLS-1$
		buffer.append("// #3: function type is g(int), x is const\n"); //$NON-NLS-1$
		buffer.append("g<int>(1);\n"); //$NON-NLS-1$
		buffer.append("// #4: function type is g(int), x is const\n"); //$NON-NLS-1$
		buffer.append("g<const int>(1);\n"); //$NON-NLS-1$
		buffer.append("// #5: function type is h(int, const int*)\n"); //$NON-NLS-1$
		buffer.append("h<const int>(1,0);\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.8.2.4-7):
	template<class T> void f(const T*) {}
	int *p;
	void s()
	{
	f(p); // f(const int *)
	}
	 --End Example]
	 */
	public void test14_8_2_4s7()  {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> void f(const T*) {}\n"); //$NON-NLS-1$
		buffer.append("int *p;\n"); //$NON-NLS-1$
		buffer.append("void s()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("f(p); // f(const int *)\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.8.2.4-8):
	template <class T> struct B { };
	template <class T> struct D : public B<T> {};
	struct D2 : public B<int> {};
	template <class T> void f(B<T>&){}
	void t()
	{
	D<int> d;
	D2 d2;
	f(d); //calls f(B<int>&)
	f(d2); //calls f(B<int>&)
	}
	 --End Example]
	 */
	public void test14_8_2_4s8()  {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template <class T> struct B { };\n"); //$NON-NLS-1$
		buffer.append("template <class T> struct D : public B<T> {};\n"); //$NON-NLS-1$
		buffer.append("struct D2 : public B<int> {};\n"); //$NON-NLS-1$
		buffer.append("template <class T> void f(B<T>&){}\n"); //$NON-NLS-1$
		buffer.append("void t()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("D<int> d;\n"); //$NON-NLS-1$
		buffer.append("D2 d2;\n"); //$NON-NLS-1$
		buffer.append("f(d); //calls f(B<int>&)\n"); //$NON-NLS-1$
		buffer.append("f(d2); //calls f(B<int>&)\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.8.2.4-18):
	template <template X<class T> > struct A { };
	template <template X<class T> > void f(A<X>) { }
	template<class T> struct B { };
	int foo() {
	A<B> ab;
	f(ab); //calls f(A<B>)
	}
	 --End Example]
	 */
	public void test14_8_2_4s18()  {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template <template X<class T> > struct A { };\n"); //$NON-NLS-1$
		buffer.append("template <template X<class T> > void f(A<X>) { }\n"); //$NON-NLS-1$
		buffer.append("template<class T> struct B { };\n"); //$NON-NLS-1$
		buffer.append("int foo() {\n"); //$NON-NLS-1$
		buffer.append("A<B> ab;\n"); //$NON-NLS-1$
		buffer.append("f(ab); //calls f(A<B>)\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.5.5.1-8a):
	// Guaranteed to be the same
	template <int I> void f(A<I>, A<I+10>);
	template <int I> void f(A<I>, A<I+10>);
	// Guaranteed to be different
	template <int I> void f(A<I>, A<I+10>);
	template <int I> void f(A<I>, A<I+11>);
	 --End Example]
	 */
	public void test14_5_5_1s8a()  {
		StringBuffer buffer = new StringBuffer();
		buffer.append("// Guaranteed to be the same\n"); //$NON-NLS-1$
		buffer.append("template <int I> void f(A<I>, A<I+10>);\n"); //$NON-NLS-1$
		buffer.append("template <int I> void f(A<I>, A<I+10>);\n"); //$NON-NLS-1$
		buffer.append("// Guaranteed to be different\n"); //$NON-NLS-1$
		buffer.append("template <int I> void f(A<I>, A<I+10>);\n"); //$NON-NLS-1$
		buffer.append("template <int I> void f(A<I>, A<I+11>);\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.8.1-4):
	template<class T> void f(T);
	class Complex {
	// ...
	Complex(double);
	};
	void g()
	{
	f<Complex>(1); // OK, means f<Complex>(Complex(1))
	}
	 --End Example]
	 */
	public void test14_8_1s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> void f(T);\n"); //$NON-NLS-1$
		buffer.append("class Complex {\n"); //$NON-NLS-1$
		buffer.append("// ...\n"); //$NON-NLS-1$
		buffer.append("Complex(double);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void g()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("f<Complex>(1); // OK, means f<Complex>(Complex(1))\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.CPP, true, 1);
	}
	
	/**
	 [--Start Example(CPP 14.8.2.4-14):
	template<int i, typename T>
	T deduce(typename A<T>::X x, // T is not deduced here
	T t, // but T is deduced here
	typename B<i>::Y y); // i is not deduced here
	A<int> a;
	B<77> b;
	int x = deduce<77>(a.xm, 62, y.ym);
	// T is deduced to be int, a.xm must be convertible to
	// A<int>::X
	// i is explicitly specified to be 77, y.ym must be convertible
	// to B<77>::Y
	 --End Example]
	 */
	public void test14_8_2_4s14()  {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<int i, typename T>\n"); //$NON-NLS-1$
		buffer.append("T deduce(typename A<T>::X x, // T is not deduced here\n"); //$NON-NLS-1$
		buffer.append("T t, // but T is deduced here\n"); //$NON-NLS-1$
		buffer.append("typename B<i>::Y y); // i is not deduced here\n"); //$NON-NLS-1$
		buffer.append("A<int> a;\n"); //$NON-NLS-1$
		buffer.append("B<77> b;\n"); //$NON-NLS-1$
		buffer.append("int x = deduce<77>(a.xm, 62, y.ym);\n"); //$NON-NLS-1$
		buffer.append("// T is deduced to be int, a.xm must be convertible to\n"); //$NON-NLS-1$
		buffer.append("// A<int>::X\n"); //$NON-NLS-1$
		buffer.append("// i is explicitly specified to be 77, y.ym must be convertible\n"); //$NON-NLS-1$
		buffer.append("// to B<77>::Y\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
		assertTrue(false);
		} catch (Exception e) {
		}
	}

	/**
	 [--Start Example(CPP 14.8.3-5):
	template<class T> void f(T*,int); // #1
	template<class T> void f(T,char); // #2
	void h(int* pi, int i, char c)
	{
	f(pi,i); //#1: f<int>(pi,i)
	f(pi,c); //#2: f<int*>(pi,c)
	f(i,c); //#2: f<int>(i,c);
	f(i,i); //#2: f<int>(i,char(i))
	}
	 --End Example]
	 */
	public void test14_8_3s5()  {
		StringBuffer buffer = new StringBuffer();
		buffer.append("template<class T> void f(T*,int); // #1\n"); //$NON-NLS-1$
		buffer.append("template<class T> void f(T,char); // #2\n"); //$NON-NLS-1$
		buffer.append("void h(int* pi, int i, char c)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("f(pi,i); //#1: f<int>(pi,i)\n"); //$NON-NLS-1$
		buffer.append("f(pi,c); //#2: f<int*>(pi,c)\n"); //$NON-NLS-1$
		buffer.append("f(i,c); //#2: f<int>(i,c);\n"); //$NON-NLS-1$
		buffer.append("f(i,i); //#2: f<int>(i,char(i))\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		try {
		parse(buffer.toString(), ParserLanguage.CPP, true, 0);
		assertTrue(false);
		} catch (Exception e) {
		}
	}
    
    
}
