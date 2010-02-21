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
#include "dbg_friend.h"

void dbg_friend();

/*************************************************************************************************************
File:			dbg_friend.cpp	
Function:		dbg_friendClass() 

Classes Used:	first, second		
Libs Used:		n/a
-------------------------------------------------------------------------------------------------------------			
PRECONDITIONS/ASSUMPTIONS
	
OBJECTIVE
	
1 	Friend classes can exists and modify private data in the containing class.
2 	Instantiated object members display correct values in the Variables pane.
	   	
PROCEDURE

1	Expand the a object in the variable view and step over the instantiations of a and b. 
2	Step into b.func1(&a); Expand the object pointers nd step to the end of the function.
3	Step out of b.func1(&a).
4	Step into b.func2(&a); Expand the object pointers and step to the end of the function.
5	Step out of b.func2(&a).
-------------------------------------------------------------------------------------------------------------			
CHECKS

1	a.data1 = 99
	b.i = 0, b.j = 0, b.k=0
2	a.data1 = 99, this.data1 = 99, this.j = 0, this.k = 0
3   a.data1 = 99, b.i = 99, b.j = 0, b.k = 0
4	a.data1 = 101, this.i = 99, this.j = 99, this.k = 101
5   a.data1 = 101, b.i = 99, b.j == 99, b.k == 101
-------------------------------------------------------------------------------------------------------------			
*************************************************************************************************************/

void dbg_friendClass( void )
{	
	first a;               	//a.data1 == 99
	second b;				//b.i == 0, b.j == 0, b.k == 0
	
	b.func1(&a);			//a.data1 = 99, b.i = 99, b.j = 0, b.k = 0 
	b.func2(&a);            //a.data1 = 101, b.i = 99, b.j == 99, b.k == 101	
}

/*************************************************************************************************************
File:			dbg_friend.cpp	
Function:		dbg_friendFunction()

Classes Used:	alpha, beta		
Libs Used:		n/a
-------------------------------------------------------------------------------------------------------------			
PRECONDITIONS/ASSUMPTIONS
	
OBJECTIVE
	
1 	Friend functions can exists and modify
	private data in the containing class.
2 	Instantiated object members display correct
	values in the Variables pane.

PROCEDURE 

1   Expand the alpha object in the Variable pane and step over the instantiation of the object. 
2   Expand the beta object in the variable view and step over the instantiation of the object.
3	Step into the friend function call.
4	Step out of the friend function.
-------------------------------------------------------------------------------------------------------------			

CHECKS 

1	able.data == 3.
2   baker.data == 7.
3   The PC cursor should be in frifunc
4	The frifunc function is declared as a friend function of both 
	alpha and beta so after stepping out, friendly == 10. 

-------------------------------------------------------------------------------------------------------------			
*************************************************************************************************************/

void dbg_friendFunction()
{
	alpha able;                        	//able.data == 3.  
	beta  baker;                        //baker.data == 7.  
	
	volatile int friendly;                             
	
	friendly = frifunc(able, baker);	//friendly == 10.  
}

/*************************************************************************************************************
File:			dbg_friend.cpp	
Function:		dbg_friendOperatorOverload() 

Classes Used:	USDollar		
Libs Used:		n/a
-------------------------------------------------------------------------------------------------------------			
PRECONDITIONS/ASSUMPTIONS
	
OBJECTIVE
	
1 	Friend operator overloading can be used to modify private data in the containing class
2 	Instantiated object members display correct values in the Variables pane.
3	Friend functions can be used to overload operators to allow class objects to be added to C++ data types.
4	Friend functions can be used to overload operators to allow C++ data types to be added to class objects.
	   	
PROCEDURE

1	Expand the d1 object in the variables view and step over the instantiation of d1. 
2	Expand the d2 object in the variables view and step over the instantiation of d2.
3	Expand the d3 and d4 objects in the variables view and step over the instantiation of d3 and d4.
4	Step into d3 = d1 + d2. 
5	Expand the @return_struct@ object and step through the overloaded '+' operator code.
6 	Step out of the overloaded '+'operator code.
7	Step into ++d3.
8	Expand the s object and step through the overloaded '++" operator code.
9	Step out of the overloaded '++'operator code.
10	Step into d4 = pennies + d2.
11	Expand the @return_struct@ object and step through the overloaded '+'operator code.
12	Step out of the overloaded '+'operator code.
13	Step into d4 = d4 + greenbacks.
14	Expand the @return_struct@ object and step through the overloaded '+'operator code.
15	Step out of the overloaded '+'operator code.
-------------------------------------------------------------------------------------------------------------			
CHECKS

1	d1.dollars = 1, d1.cents = 60.
2   d2.dollars = 2, d2.cents = 50.
3   d3.dollars = 0, d3.cents = 0, d4.dollars == 0, d4.cents == 0.
4	The PC cursor should be in the overloaded '+' operator code.
5	@return_struct.dollars  = 4, @return_struct.cents  = 10.
6	d3.dollars = 4, d3.cents = 10.
7	The PC cursor should be in the overloaded '++' operator code
8	s.dollars  = 4, s.cents  = 11.
9	d3.dollars = 4, d3.cents = 11.
10	The PC cursor should be in the overloaded '+' operator code
11	@return_struct@.dollars = 3, @return_struct@.cents = 25.
12	d4.dollars == 3, d4.cents == 25. 
13	The PC cursor should be in the overloaded '+' operator code.
14	@return_struct@.dollars = 10, @return_struct.cents = 25.
15	d4.dollars == 10, d4.cents == 25.
-------------------------------------------------------------------------------------------------------------			
*************************************************************************************************************/


void dbg_friendOperatorOverload()
{
	volatile int greenbacks = 7;
	volatile int pennies = 75;
	
	USDollar d1(1, 60);					//declare d1 = $1.60
	USDollar d2(2, 50);					//declare d2 = $2.50
	USDollar d3(0, 0);					//initialize d3 $0.00
	USDollar d4(0, 0);					//initialize d4 $0.00	
	
	d3 = d1 + d2;						//d3.dollars == 4, d3.cents == 10
	++d3;								//d3.dollars == 4, d3.cents == 11
	d4 = pennies + d2;					//d4.dollars == 3, d4.cents == 25
	d4 = d4 + greenbacks;				//d4.dollars == 10, d4.cents == 25
}


void dbg_friend()
{
	dbg_friendClass();					//step into
	dbg_friendFunction();				//step into
	dbg_friendOperatorOverload();		//step into
}
