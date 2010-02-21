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

/*initialize a large array to all zeros.*/
volatile int large_array[10000]={0};
	
void dbg_stress_test()
{	
	int x=1; //dummy line of code
	large_array[0] = 1;
}

/*************************************************************************************************************

File:			dbg_stress_test.c
Function:		stress_test()

Libs Used:		n/a
-------------------------------------------------------------------------------------------------------------			
PRECONDITIONS/ASSUMPTIONS
	
OBJECTIVE
	
	Large arrays can cause problems for the debugger. It may try to 
	read in the entire array, even though it can only display so many
	values at a time. For very large arrays, this can take a long time.
	Determine if stepping over an expanded large array does not take a
	long time and that the large array is displayed properly.
	
PROCEDURE
	
	Expand the large array variable and step over it.
-------------------------------------------------------------------------------------------------------------			
CHECKS

1	The step time should not be more than 15 seconds.
2	The large array is displayed properly in the Variable view.
-------------------------------------------------------------------------------------------------------------			
*************************************************************************************************************/
