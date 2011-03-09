/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.edc.tests;

import junit.framework.TestCase;

import org.eclipse.cdt.debug.edc.internal.symbols.files.UnmanglerEABI;
import org.eclipse.cdt.debug.edc.internal.symbols.files.UnmanglingException;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 */
public class TestUnmanglerEABI extends TestCase {
	private UnmanglerEABI um;
	
	@Before
	public void setUp() {
		um = new UnmanglerEABI();
	}
	
	protected String unmangle(String symbol) throws UnmanglingException {
		return um.unmangle(symbol);
	}
	
	protected String unmangleType(String symbol) throws UnmanglingException {
		return um.unmangleType(symbol);
	}

	@Test
	public void testNotMangled() throws UnmanglingException {
		assertNull(unmangle(null));
		assertEquals("global", unmangle("global"));
		assertEquals("?myfunc@@YAHXZ", unmangle("?myfunc@@YAHXZ"));	// win32, not EABI
	}
	
	@Test
	public void testSimpleGlobal() throws UnmanglingException {
		assertEquals("myfunc()", unmangle("_Z6myfuncv"));
		
		// some errors
		try {
			assertEquals("myfunc()", unmangle("_Z16myfuncv"));
			fail("expected error");
		} catch (UnmanglingException e) {
			
		}
		try {
			assertEquals("myfunc()", unmangle("_Z1myfuncv"));
			fail("expected error");
		} catch (UnmanglingException e) {
			
		}

	}
	
	@Test
	public void testOtherFuncs() throws UnmanglingException {
		assertEquals("foo(double,float)", unmangle("_Z3foodf"));
		
		// hmm, this example doesn't work in our unmangler or c++filt
		//assertEquals("foo()::C::bar::E::baz()", unmangle("_ZZ3foovEN1C3barEvEN1E3bazEv"));
	}
	@Test
	public void testTemplateFuncs1() throws UnmanglingException {
		assertEquals("int bar<float>(float)", unmangle("_Z3barIfEiT_"));
		assertEquals("List<char>::add(char)", unmangle("_ZN4ListIcE3addEc"));
		assertEquals("List<float>::add(float)", unmangle("_ZN4ListIfE3addEf"));
	}
	
	@Test
	public void testTemplateFuncs2() throws UnmanglingException {
		// (Note: first template is S_, Duo is S0_, first(Duo) is S1_.
		// Since the function parameter is not dependent, don't use T_.) 
		assertEquals("void first<Duo>(Duo)", unmangle("_Z5firstI3DuoEvS0_"));
		
		// Ret? operator<< (X const&, X const&);
		// (Note: X is S_, X const is S0_, X const& is S1_) 
		assertEquals("operator <<(X const&,X const&)", unmangle("_ZlsRK1XS1_"));
		assertEquals("operator %(X,X)", unmangle("_Zrm1XS_"));

	}
	@Test
	public void testStaticMember() throws UnmanglingException {
		assertEquals("S::x", unmangle("_ZN1S1xE"));
	}
	@Test
	public void testPtmfTypes1() throws UnmanglingException {
		// struct A;
	    // void f (void (A::*)() const) {}
		// produces the mangled name "_Z1fM1AKFvvE".
		// 	  name -> f
		//		args -> M1AKFvvE
		assertEquals("f(void (A::*)() const)", unmangle("_Z1fM1AKFvvE"));
	}
	@Test
	public void testPtmfTypes2() throws UnmanglingException {
		assertEquals("foo(void (Foo::*)(),int (Foo::*)(float))", unmangle("_Z3fooM3FooFvvEMS_FifE"));
		
		assertEquals("foo(int AB::**)", unmangle("_Z3fooPM2ABi"));
	}
	
	@Test
	public void testBlackflag() throws UnmanglingException {
		assertEquals("show_Const_Arguments(TDesC8 const&,TDesC8 const*,TDesC8&,TDesC8*)", unmangle("_Z20show_Const_ArgumentsRK6TDesC8PS0_RS_PS_"));
		assertEquals("show_Arguments(TDes16&,TDes16*,TDes16*)", unmangle("_Z14show_ArgumentsR6TDes16PS_S1_"));
		//  void tree_clear(t_node*& pRoot)
		assertEquals("tree_clear(t_node*&)", unmangle("_Z10tree_clearRP6t_node"));
		assertEquals("dbg_namespace::dbg_nameSpace_InitNamespace()", unmangle("_ZN13dbg_namespace27dbg_nameSpace_InitNamespaceEv"));
	}
	
	@Test
	public void testSpecialClassMethods() throws UnmanglingException {
		assertEquals("Der2::~Der2()", unmangle("_ZN4Der2D0Ev"));
		assertEquals("DerivedTypes::DerivedTypes()", unmangle("_ZN12DerivedTypesC2Ev"));
	}
	
	@Test
	public void testOperatorMethods() throws UnmanglingException {
		assertEquals("Klass::operator &=(Klass)", unmangle("_ZN5KlassaNES_"));
		assertEquals("Klass::operator +(Klass const&)", unmangle("_ZN5KlassplERKS_"));
		assertEquals("Klass::operator =(Klass const&)", unmangle("_ZN5KlassaSERKS_"));
		// note: c++filt doesn't show (), but 'v' is clearly there
		assertEquals("Klass::operator int() const()", unmangle("_ZNK5KlasscviEv"));
		assertEquals("List<float>::operator [](unsigned int)", unmangle("_ZN4ListIfEixEj"));
		assertEquals("operator delete[](void*)", unmangle("_ZdaPv"));
		assertEquals("operator new(unsigned int)", unmangle("_Znwj"));


	}
	
	@Test
	public void testStdExpansions() throws UnmanglingException {
		assertEquals("operator <<(::std::basic_ostream<char,::std::char_traits<char> >&,::std::basic_string<char,::std::char_traits<char>,::std::allocator<char> > const&)", unmangle("_ZlsRSoRKSs"));
		assertEquals("::std::__codecvt_abstract_base<char,char,int>::~__codecvt_abstract_base()", unmangle("_ZNSt23__codecvt_abstract_baseIcciED0Ev"));
	}

	@Test
	public void testStdTypeSubstitutions() throws UnmanglingException {
		assertEquals("::std::string", unmangleType("_ZSt6string"));
		assertEquals("::std::wstring", unmangleType("_ZSt7wstring"));
		assertEquals("::std::basic_string<char,::std::char_traits<char>,::std::allocator<char> >",
					 unmangleType("_ZSs"));		
	}

	@Test
	public void testStdTemplateSubstitutions() throws UnmanglingException {
		assertEquals("::std::allocator<::std::pair<int const,::std::basic_string<char,::std::char_traits<char>,::std::allocator<char> > > >",
					 unmangleType("_ZSaISt4pairIKiSsEE"));
		assertEquals("::std::allocator<::std::pair<::std::basic_string<char,::std::char_traits<char>,::std::allocator<char> > const,Json::Value> >",
					 unmangleType("_ZSaISt4pairIKSsN4Json5ValueEEE"));

		assertEquals("::std::less<int>", unmangleType("_ZSt4lessIiE"));
		assertEquals("::std::less<::std::basic_string<char,::std::char_traits<char>,::std::allocator<char> > >",
					 unmangleType("_ZSt4lessISs"));

		assertEquals("::std::map<int,::std::basic_string<char,::std::char_traits<char>,::std::allocator<char> >,::std::less<int>,::std::allocator<::std::pair<int const,::std::basic_string<char,::std::char_traits<char>,::std::allocator<char> > > > >",
					 unmangleType("_ZSt3mapIiSsSt4lessIiESaISt4pairIKiSsEEE"));

		assertEquals("::std::map<::std::basic_string<char,::std::char_traits<char>,::std::allocator<char> >,Json::Value,::std::less<::std::basic_string<char,::std::char_traits<char>,::std::allocator<char> > >,::std::allocator<::std::pair<::std::basic_string<char,::std::char_traits<char>,::std::allocator<char> > const,Json::Value> > >",
					 unmangleType("_ZSt3mapISsN4Json5ValueESt4lessISsESaISt4pairIKSsS1_EEE"));

		assertEquals("::std::pair<::std::basic_string<char,::std::char_traits<char>,::std::allocator<char> > const,Json::Value>",
					 unmangleType("_ZSt4pairIKSsN4Json5ValueEE"));
		assertEquals("::std::pair<int const,::std::basic_string<char,::std::char_traits<char>,::std::allocator<char> > >",
					 unmangleType("_ZSt4pairIKiSsE"));
	}
	
	@Test
	public void testExtraStuff() throws UnmanglingException {
		assertEquals("_ZdaPv", um.undecorate("_ZdaPv@@GLIBCXX_3.4"));
		assertEquals("_Znwj", um.undecorate("_Znwj@@GLIBCXX_3.4"));
	}
	
	
	@Test
	public void testTemplateWithSubst() throws UnmanglingException {
		// <encoding> = _Z + ...
		//   <function-name> = "N1N1TiiE2mfE" + <bare-function-type> = "S0_IddE"
		// 	   <nested-name> = "N" + <prefix> = "1N1TIiiE" + <unqualified-name> = "2mf" + "E"
		//         <prefix> = <template-prefix> = "1N1T" + <template-args> = "IiiE" 
		//  S0_ = N::T (template name), so must be followed with template-args = "IddE"
		assertEquals("N::T<int,int>::mf(N::T<double,double>)", unmangle("_ZN1N1TIiiE2mfES0_IddE"));
		
		// /*T1=*/Factory</*T2=*/int> make<Factory, int>();
		// (Note: T_ = factory (a template), T0_ = int) 
		assertEquals("Factory<int> make<Factory,int>()", unmangle("_Z4makeI7FactoryiET_IT0_Ev"));
	}
	
	@Test
	public void testUnnamedTypes() throws UnmanglingException {
		assertEquals("g(int)::S::f#2(int)", unmangle("_ZZ1giEN1S1fE_2i"));
		assertEquals("g(int)::S::f#2(int)::<unnamed #3>", unmangle("_ZZZ1giEN1S1fE_2iEUt1_"));
		assertEquals("g()::S::S()", unmangle("_ZZZ1gvEN1SC1EvEs"));
		assertEquals("g()::str4a", unmangle("_ZZ1gvE5str4a"));
		assertEquals("g()::string literal#1", unmangle("_ZZ1gvEs_1"));
	}
	
	@Test
	public void testTemplateArrays() throws UnmanglingException {
		assertEquals("Foo<int[4]>::bar", unmangle("_ZN3FooIA4_iE3barE"));
	}
	
	@Test
	public void testFieldNames() throws UnmanglingException {
		assertEquals("Arena::level", unmangle("_ZN5Arena5levelE"));
		assertEquals("Stack<int,int>::level", unmangle("_ZN5StackIiiE5levelE"));
	}
	@Test
	public void testSpecialNames() throws UnmanglingException {
		assertEquals("<typeinfo name for Derv1>", unmangle("_ZTS5Derv1"));
		assertEquals("<typeinfo structure for Derv1>", unmangle("_ZTI5Derv1"));
		assertEquals("<virtual table for IFaceDerived>", unmangle("_ZTV12IFaceDerived"));
		assertEquals("<VTT structure for Iface2>", unmangle("_ZTT6Iface2"));
		assertEquals("<one-time-init guard for Foo::bar()::val>", unmangle("_ZGVZN3Foo3barEvE3val"));
		assertEquals("<virtual base override at offset 0x0, vcall offset -0x10 for Bar::first()>", unmangle("_ZTv0_n16_N3Bar5firstEv"));
		assertEquals("<non-virtual base override at offset -0x4 for Bar::first()>", unmangle("_ZThn4_N3Bar5firstEv"));
	}
	
	/*@Test
	public void testClosureAndLambdaSig() throws UnmanglingException {
		assertEquals("S<int>::x::operator... ???", unmangle("_Z4algoIZ1giEUlvE0_EiT_"));
		assertEquals("???", unmangle("_ZNK1SIiE1xMUlvE_clEv"));
		assertEquals("???", unmangle("_ZZ1giENKUlvE_clEv"));
	}
	
	@Test
	public void testTemplateExprs() throws UnmanglingException {
		assertEquals("void operator-<42>(A<J+2>::T)", "_ZngILi42EEvN1AIXplT_Li2EEE1TE");
		assertEquals("...B<(J+1)/2>...", "_Z1BIXdvplT1_Li1ELi2EEE");
	*/
	
	@Test
	public void testSubstitutions() throws UnmanglingException {
		// 0: TPckgBuf
		// 1: WiFiProt
		// 2: WiFiProt::TWiFiInputParams
		// 3: TPckgBuf<WiFiProt::TWiFiInputParams>
		assertEquals("TPckgBuf<WiFiProt::TWiFiInputParams>::TPckgBuf(TPckgBuf<WiFiProt::TWiFiInputParams> const&)",
				unmangle("_ZN8TPckgBufIN8WiFiProt16TWiFiInputParamsEEC1ERKS2_"));	

		// 0: long long const
		// 1: long long const [4]
		// 2: long long const (&) [4]
		assertEquals("dVector3Dot(long long const(&)[4],long long const(&)[4])",
				unmangle("_Z11dVector3DotRA4_KxS1_"));
		
		/*
typedef void T();
struct S {};
void f(T*, T (S::*)) {}

		 */
		//assertEquals("void f(T*, T (S::*))",
		//		unmangle("_Z1fPFvvEM1SFvvE"));
		assertEquals("f(void (*)(),void (S::*)())",
				unmangle("_Z1fPFvvEM1SFvvE"));

		assertEquals("CActiveScheduler::Start(CActiveScheduler::TLoop**)",
				unmangle("_ZN16CActiveScheduler5StartEPPNS_5TLoopE"));

		assertEquals("CActiveScheduler::DoRunL(CActiveScheduler::TLoop** volatile const&,CActive* volatile&,CActiveScheduler::TCleanupBundle*)", 
				unmangle("_ZN16CActiveScheduler6DoRunLERVKPPNS_5TLoopERVP7CActivePNS_14TCleanupBundleE"));
	}
	
	@Test
	public void testRegressions() throws UnmanglingException {
		assertEquals("void Klass<Data,Type<Data> >::method<Type<Data> >()",
				unmangle("_ZN5KlassI4Data4TypeIS0_EE6methodIS2_EEvv"));
		assertEquals("CMMFFormatEncode::GetSupportedNumChannelsL(RArray<unsigned int>&)",
				unmangle("_ZN16CMMFFormatEncode24GetSupportedNumChannelsLER6RArrayIjE"));
		assertEquals("MeshMachine::TAggregatedTransition<CoreNetStates::TAddDataClient,PRStates::TSendProvision,MeshMachine::TNodeContext<ESock::CMMCommsProviderBase,MeshMachine::TNodeContext<ESock::ACFMMNodeBase,MeshMachine::TNodeContext<MeshMachine::AMMNodeBase,MeshMachine::TNodeContextBase,MeshMachine::CNodeActivityBase>,MeshMachine::CNodeActivityBase>,MeshMachine::CNodeActivityBase> >::~TAggregatedTransition()",
				unmangle("_ZN11MeshMachine21TAggregatedTransitionIN13CoreNetStates14TAddDataClientEN8PRStates14TSendProvisionENS_12TNodeContextIN5ESock20CMMCommsProviderBaseENS5_INS6_13ACFMMNodeBaseENS5_INS_11AMMNodeBaseENS_16TNodeContextBaseENS_17CNodeActivityBaseEEESB_EESB_EEED1Ev"));
	}
	
	
	@Test
	public void testExpressions1() throws UnmanglingException {
		assertEquals("TBuf8<128>::TBuf8(TBuf8<128> const&)",
				unmangle("_ZN5TBuf8ILi128EEC1ERKS0_"));

		// int types have suffixes
		assertEquals("CArrayFix<TBuf8<6> >::~CArrayFix()",
				unmangle("_ZN9CArrayFixI5TBuf8ILi6EEED1Ev"));
		assertEquals("int something<10UL>(float)",
				unmangle("_Z9somethingILm10EEif"));
		assertEquals("CArrayFix<TBuf8<6LL> >::~CArrayFix()",
				unmangle("_ZN9CArrayFixI5TBuf8ILx6EEED1Ev"));
		
		// show cast-to-type name if not simple int
		assertEquals("CArrayFix<TBuf8<(__int128)6> >::~CArrayFix()",
				unmangle("_ZN9CArrayFixI5TBuf8ILn6EEED1Ev"));
	}
	
	@Test
	public void testEnumConstants() throws UnmanglingException {
		assertEquals("Json::objectValue",
				unmangle("__N4Json11objectValueE"));
		assertEquals("Json::ValueType",
				unmangle("_ZN4Json9ValueTypeE"));
	}
}
