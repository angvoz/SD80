/*******************************************************************************
 * Copyright (c) 2009 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
#include "dbg_prototypes.h"

struct Base01 
{
	int a;
	Base01() { a=1; }
	virtual ~Base01() {}
};

struct Der1 : public Base01
{
	int b1;
	Der1() : Base01() { b1=2; }
};

struct Der2 : public Base01
{
	int b2;
	Der2() : Base01() { b2=3; }
	virtual void extra() {}
};

struct DerDer : public Der2
{
	int c;
	DerDer() : Der2() { c=4; }
};

struct Iface1
{
	virtual void func1() { }
};

struct Iface2
{
	virtual void func2() { }
};

struct IFaceDerived : public Der2, public Iface1, public Iface2
{
	int d;
	IFaceDerived() : Der2(), Iface1(), Iface2() { d=5; }
};

void dbg_rtti()
{
	Base01* b = new Base01;
	Base01* der1 = new Der1;		//Should be of type Der1*
	Base01* der2 = new Der2;		//Should be of type Der2*
			
	der2 = new DerDer();		//Should be of type DerDer*
	der2 = new IFaceDerived;	//Should be of type IFaceDerived*

	Iface1* iface1 = new IFaceDerived;	//Should be of type IFaceDerived*
	Iface2* iface2 = new IFaceDerived;	//Should be of type IFaceDerived*
}

/*************************************************************************************************************

File:			dbg_rtti.cpp	
Function:		rtti()

Classes Used:	DeepCopy, ShallowCopy		
-------------------------------------------------------------------------------------------------------------			
PRECONDITIONS/ASSUMPTIONS
	
OBJECTIVE
	
"Show dynamic runtime type of objects" feature should work.
Seen in: BlackFlag C++ virtualInheritance(), any Symbian code

This test should have the same results under the Carbide debugger and under MSVC.
	
PROCEDURE

1.	From the IDE select Window->Preferences->C/C++->Debug->Carbide.c++ Debugger. 
	Enable "Attempt to show dynamic runtime types of objects".
	In the Variables pane press the "Show Types Names" icon.
    Step to the assignment der2 = new DerDer();
    Expand 'der1' and 'der2'.  
2.	Step over the assignment der2 = new DerDer();
	Expand 'der2'.  
3.	Step over the assignment der2 = new IFaceDerived;
	Expand 'der2'.  
4. 	Step over the assignemnts of iface1 and iface2.
	Expand 'iface1' and 'iface2'.  
-------------------------------------------------------------------------------------------------------------			
CHECKS

1. 	der1 and der2 should be of types 'Der1*'and 'Der2*' respectively 
	(NOT 'Base01*' or 'Der2*' or 'DerDer*'). 
	der1 and der2 should each contain a member variable, int a=1 inherited from Base01
	der1 should contain a member variable, int b1=2.
	der2 should contain a member variable, int b2=3.
2. 	der2 should be of type 'DerDer*' (NOT 'Base01*' or 'IFaceDerived*').
	der2 should contain a member variable, int a=1 inherited from Base01.
	der2 should contain a member variable, int b2=3 inherited from Der2.	 
	der2 should contain a member variable, int c=4.
3. 	der2 should be of type 'IFaceDerived*' (NOT 'Base01*').
	der2 should contain a member variable, int a=1 inherited from Base01.
	der2 should contain a member variable, int b2=3 inherited from Der2.	 
	der2 should contain a member variable, int d=5.
	der2 should contain two structs Iface1 and Iface2.
4.  iface1 and iface2 should be of type 'IFaceDerived*'(NOT 'Iface1*' and 'Iface2*').
	iface1 and iface2 should each contain a member variable, int a=1 inherited from Base01
	iface1 and iface2 should contain a member variable, int b2=3 inherited from Der2.	 
	iface1 and iface2 should contain a member variable, int d=5.
	iface1 and iface2 should contain two structs Iface1 and Iface2.
	iface1 and iface2 should contain two structs Iface1 and Iface2.
-------------------------------------------------------------------------------------------------------------			
*************************************************************************************************************/

