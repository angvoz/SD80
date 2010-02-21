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
#include "dbg_typedefs.h"
#include "dbg_prototypes.h"

/**************************************************************************************************
File:			dbg_derived_types.c	
Function:		structs

---------------------------------------------------------------------------------------------------			
PRECONDITIONS/ASSUMPTIONS
	
OBJECTIVE

1 	A local struct, global struct and local struct that contains 
    other structs and arrays is displayed properly in the Variables 
    pane of the Carbide IDE.
	 
PROCEDURE

	Set a breakpoint at the end of the function and run to it.
	
-----------------------------------------------------------------------------------------------------			

CHECKS

	In the Variable pane the following should be displayed:
	
    - lnested_struct.structa should have member values from 1 to 17.
    - lnested_struct.structb should have member values from 1 to 17.
    - lnested_struct.arraya should have values 0 to 9.
    - lnested_struct.arrayb should be an array of 10 structs with member values from 1 to 17.
	- lstruct should have member values from 1 to 17.
    - gstruct should have member values from 1 to 17.
**************************************************************************************************/
void structs()
{
	typedef struct {
		struct_type structa;
		struct_type structb;
		int arraya[10];
		struct_type arrayb[10];
	} nested_structs;
	
	int i;		
	struct_type lstruct;
	nested_structs lnested_struct;
	
	lstruct.achar = '1';
	lstruct.auchar = 2;
	lstruct.aschar = '3';
	lstruct.ashort = 4;
	lstruct.aushort = 5;
	lstruct.asshort = 6;
	lstruct.aint = 7;
	lstruct.auint = 8;
	lstruct.asint = 9;
	lstruct.along = 10;
	lstruct.aulong = 11;
	lstruct.aslong = 12;
#ifdef __MSL_LONGLONG_SUPPORT__
	lstruct.aulonglong = 13;
	lstruct.aslonglong = 14;
#endif /* __MSL_LONGLONG_SUPPORT__ */
#ifdef _Floating_Point_Support_
	lstruct.afloat = 15.0;
	lstruct.adouble = 16.0;
	lstruct.alongdouble = 17.0;
#endif /* _Floating_Point_Support_ */

	gstruct = lstruct;
	
	lnested_struct.structa = lstruct;
	lnested_struct.structb = gstruct;
	for (i=0; i<10; i++) {
		lnested_struct.arraya[i] = i;
		lnested_struct.arrayb[i] = gstruct;
	}
} /* set breakpoint here */

/**************************************************************************************************
File:			dbg_derived_types.cpp	
Function:		arrays

---------------------------------------------------------------------------------------------------			
PRECONDITIONS/ASSUMPTIONS
	
OBJECTIVE
	 
The following are displayed properly in the Variables pane of the Carbide IDE:
- 	A two dimensional array.	
- 	A local array.
- 	A string array.
- 	An array of structs.	
- 	A global array.
	 
PROCEDURE

	Set a breakpoint at the end of the function and run to it.
-----------------------------------------------------------------------------------------------------			
CHECKS

1   l2darray should be a two-dimensional array. There should be nine elements in the first array
	and each element is filled with the value 123456789.
2   larray should have values 0 to 39.
3   str_array should be displayed as "testing".
4   struct_array should be an array of structs with member values going from 1 to 17. 
5   garray should have values 40 to 1. 
**************************************************************************************************/
void arrays()
{
	int larray[40], i, j;
	struct_type struct_array[20];
	unsigned long l2darray[10][10];
	char str_array[8] = "testing";

	for (i=0; i<40; i++) {
		larray[i] = i;
		garray[i] = 40 - i;
	}
	
	for (i=0; i<20; i++) {
		struct_array[i].achar = '1';
		struct_array[i].auchar = 2;
		struct_array[i].aschar = '3';
		struct_array[i].ashort = 4;
		struct_array[i].aushort = 5;
		struct_array[i].asshort = 6;
		struct_array[i].aint = 7;
		struct_array[i].auint = 8;
		struct_array[i].asint = 9;
		struct_array[i].along = 10;
		struct_array[i].aulong = 11;
		struct_array[i].aslong = 12;
	#ifdef __MSL_LONGLONG_SUPPORT__
		struct_array[i].aulonglong = 13;
		struct_array[i].aslonglong = 14;
	#endif /* __MSL_LONGLONG_SUPPORT__ */
	#ifdef _Floating_Point_Support_
		struct_array[i].afloat = 15.0;
		struct_array[i].adouble = 16.0;
		struct_array[i].alongdouble = 17.0;
	#endif /* _Floating_Point_Support_ */
	}
	
	for (i=0; i<10; i++)
		for (j=0; j<10; j++)
			l2darray[i][j] = 123456789;
} /* set breakpoint here */

/**************************************************************************************************
File:			dbg_derived_types.cpp	
Function:		unions


---------------------------------------------------------------------------------------------------			
PRECONDITIONS/ASSUMPTIONS
	
OBJECTIVE
	 
	A union is displayed properly in the Variables pane of the Carbide IDE.
	 
PROCEDURE

1 	Step over the assignment of lunion.x
2	Step over the assignment of lunion.y
3	Step over the assignment of gunion.
-----------------------------------------------------------------------------------------------------			
CHECKS

1	Both lunion.x and lunion.y should change to 1.
2	Both lunion.x and lunion.y should change to 2.
3	Both gunion.x and gunion.y should change to 2. 
---------------------------------------------------------------------------------------------------			
**************************************************************************************************/
void unions()
{
	union_type lunion;	/*declared as volatile to prevent deadstripping*/ 
	
	lunion.x = 1;		/*lunion.x = 1, lunion.y = 1*/
	lunion.y = 2;		/*lunion.x = 2, lunion.y = 2*/
	
	gunion = lunion;	/*gunion.x = 2, gunion.y = 2*/
}

/**************************************************************************************************
File:			dbg_derived_types.cpp	
Function:		bitfields


---------------------------------------------------------------------------------------------------			
PRECONDITIONS/ASSUMPTIONS
	
OBJECTIVE
	 
 	A bitfield is displayed properly in the Variables pane of the Carbide IDE.
	 
PROCEDURE

1 	Expand lbitfield and gbitfield in the Variables pane and step
    over the assignments of the members of lbitfield.
2 	Step over the assignment of gbitfield. 
  
-----------------------------------------------------------------------------------------------------			
CHECKS

1 	lbitfield.x = 1, lbitfield.y = 2, lbitfield.z = 3, lbitfield.w = 26.
2 	gbitfield.x = 1, gbitfield.y = 2, gbitfield.z = 3, gbitfield.w = 26.
**************************************************************************************************/
void bitfields()
{
	bitfield_type lbitfield; /*declared as volatile to prevent deadstripping*/
	
	lbitfield.x = 1;
	lbitfield.y = 2;
	lbitfield.z = 3;
	lbitfield.w = 26;
	
	gbitfield = lbitfield;
}

/**************************************************************************************************
File:			dbg_derived_types.cpp	
Function:		enums


---------------------------------------------------------------------------------------------------			
PRECONDITIONS/ASSUMPTIONS
	
OBJECTIVE
	 
	An enum is displayed properly in the Variables pane of the Carbide IDE.
	 
PROCEDURE

	1. Step over the assignment lenum = zero.
	2. Step over the assignment lenum = one.
	3. Step over the assignment lenum = two.
	4. Step over the assignment lenum = three.
	5. Step over the assignment lenum = four.
-----------------------------------------------------------------------------------------------------			
CHECK

	1. lenum should be equal to zero.
	2. lenum should be equal to one.
	3. lenum should be equal to two.
	4. lenum should be equal to three.
	5. lenum should be equal to four.
---------------------------------------------------------------------------------------------------			
**************************************************************************************************/
void enums()
{
   	volatile enum enum_type lenum; /*declared as volatile to prevent deadstripping*/

	lenum = zero;
	lenum = one;
	lenum = two;
	lenum = three;
	lenum = four;
}

void dbg_derived_types()
{
	structs();
	arrays();	
	unions();	
	bitfields();
	enums();	
}

