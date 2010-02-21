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

int function1(int);
int function2(int);
int function3(int);

int function3(int value)
{	
	return (++value); 
}

int function2(int value)
{
	value = function3(value);	/*step into*/
	return (value);				/*step out*/
}

int function1(int value)
{
	value = function2(value);	/*step into*/
	return (value);				/*step out*/
}

void dbg_stack_crawl()
{

/*	Step into function1() */ 

	volatile int x = 0; 
	
	x = function1(x);			/*step into*/
}

/*************************************************************************************************************
File:			dbg_stack_crawl.c	
Libs Used:		n/a
-------------------------------------------------------------------------------------------------------------			
PRECONDITIONS/ASSUMPTIONS
	
OBJECTIVE
	
1	The stack crawl pane displays the stack frames correctly.
	
PROCEDURE

1	Step into function1, function2 and then function3.  
2	Step out from function3, function2. and then function1.
3	Click on each function in the stack pane. 
------------------------------------------------------------------------------------------------------------			
CHECKS

1.	function3() at dbg_stack_crawl.cpp: 11
	function2() at dbg_stack_crawl.cpp: 16
	function1() at dbg_stack_crawl.cpp: 22
	dbg_stack_crawl() at dbg_stack_crawl.cpp: 33
	doExampleL() at main.cpp: 25
	callExampleL() at CommonFramework.h: 40
	E32Main() at CommonFramework.h: 29
	
2	dbg_stack_crawl() at dbg_stack_crawl.cpp: 33
	doExampleL() at main.cpp: 25
	callExampleL() at CommonFramework.h: 40
	E32Main() at CommonFramework.h: 29

3	The debugger should show the source code of that function,
	if the source code is available, with the PC arrow pointing
	to the next instruction.
-------------------------------------------------------------------------------------------------------------			
*************************************************************************************************************/
