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
#include "dbg_typedefs.h"
                         
void dbg_variables_global();
void dbg_variables_local();


void dbg_variables_global()
{
	int dummyVar = 0; // dummy variable

	gint = 1024;			 /*	Natural 			1024
								Decimal 			1024
								Hexadecimal			0x400
							 */
								
	gchar = 'a';			 /*	Natural 			'a'
								Decimal 			97
								Hexadecimal			0x61
							 */

#ifdef _Floating_Point_Support_
	
	gfloat = 55.55f;		 /*	Natural 			55.55 
								Decimal 			1113469747
								Hexadecimal			0x425e3333


							 */

	gdouble = 222.222;		 /*	Natural 			222.222
								Decimal 			4642022758098564809						
								Hexadecimal			0x406bc71a9fbe76c9

							 */

#endif /* _Floating_Point_Support_ */
	                           
	glong = 123456789;		 /*	Natural 			123456789
								Decimal 			123456789							
								Hexadecimal			0x075BCD15
							*/
	                                        
	guint = 256;			 /*	Natural 			256
								Decimal 			256
								Hexadecimal			0x00000100
							 */
	                               
	gstring = "hello";		 /*	Natural 			'h'
								Decimal 			104
								Hexadecimal			0x68
							 */	
}
/****************************************************************************************************/
void dbg_variables_local()
{
	volatile int SizeOfInt = sizeof (int);
	#if SizeOfInt == 2
	volatile long int lint;
	#else
	volatile int lint;
	#endif
	volatile char lchar;
#ifdef _Floating_Point_Support_
	volatile float lfloat;
	volatile double ldouble;
#endif /* _Floating_Point_Support_ */
	volatile long llong;
	volatile UINT luint;
	volatile char *lstring;
	volatile char larray[8] = "testing";	
	   
	lint = 1024;			 /*	Natural 			1024
								Decimal 		    1024								
								Hexadecimal			0x400
							 */
							 			
	lchar = 'a';			 /*	Natural 			'a'
								Decimal 			97
								Hexadecimal			0x61
							 */

#ifdef _Floating_Point_Support_
	
	lfloat = 55.55f;		 /*	Natural 			55.55 
								Decimal 			1113469747
								Hexadecimal			0x425e3333
							 */

	ldouble = 222.222;		 /*	Natural 			222.222
								Decimal 			4642022758098564809
								Hexadecimal			0x406bc71a9fbe76c9
							*/

#endif /* _Floating_Point_Support_ */
	                          
	llong = 123456789;		 /*	Natural 			123456789
								Decimal 			123456789
								Hexadecimal			0x075BCD15
							*/
	                                       
	luint = 256;			 /*	Natural 			256
								Decimal 			256
								Hexadecimal			0x100
							*/
	                               
	lstring = (char *)"hello";		/*	Natural 			'h'
								Decimal 			104
								Hexadecimal			0x68
							*/	
	}

/****************************************************************************************************/
int dbg_call_from_nested_scope(int arg_lint, char arg_lchar);
int dbg_call_from_nested_scope(int arg_lint, char arg_lchar)
{
	/* step over each of the instructions, and   */

	char lchar = arg_lchar + 2;
	int lint = arg_lint * 10;

	return lchar + lint;
}

/****************************************************************************************************/
void dbg_variables_scoped()
{
	volatile int SizeOfInt = sizeof (int);
	#if SizeOfInt == 2
	volatile long int lint;
	#else
	volatile int lint;
	#endif
	volatile char lchar;
#ifdef _Floating_Point_Support_
	volatile float lfloat;
	volatile double ldouble;
#endif /* _Floating_Point_Support_ */
	volatile long llong;
	volatile UINT luint;
	volatile char *lstring;
	volatile char larray[8] = "_estin_";
	   
	lint = 1024;			 /*	Natural 			1024
								Decimal 		    1024								
								Hexadecimal			0x400
							 */

	/* step into the following and follow the instructions */
	luint = dbg_call_from_nested_scope(90, 'z') / 2;

	/* after stepping out of the previous function,
	 * re-examine local variables */

	lint *= 2;

	{
		volatile int nested_lint = lint/2;

		lchar = 'a';			 /*	Natural 			'a'
									Decimal 			97
									Hexadecimal			0x61
								 */

		{
			volatile char nested_lchar = 'g';
#ifdef _Floating_Point_Support_

			lfloat = 55.55f;		/*	Natural 			55.55
										Decimal 			1113469747
										Hexadecimal			0x425e3333
									 */
#endif /* _Floating_Point_Support_ */
			{
#ifdef _Floating_Point_Support_
				volatile float nested_float = lfloat / 5.0;
	
				ldouble = ((double)nested_float) * 20.0 + 0.022;
										/*	Natural 			222.222
											Decimal 			4642022758098564809
											Hexadecimal			0x406bc71a9fbe76c9
										*/
#endif /* _Floating_Point_Support_ */
				{
#ifdef _Floating_Point_Support_
					volatile double nested_double = ldouble / 2.0;
					/* step into the following and follow the instructions */
					nested_double
					  = dbg_call_from_nested_scope((int)nested_double,
												   (char)nested_float);
#else
					llong = dbg_call_from_nested_scope(102, 2);
#endif /* _Floating_Point_Support_ */

					/* after stepping out of the previous function,
					 * re-examine local variables */

					llong = 123456789;		/*	Natural 			123456789
												Decimal 			123456789
												Hexadecimal			0x075BCD15
											*/
				}

				luint = nested_lint / 2;	/*	Natural 			256
												Decimal 			256
												Hexadecimal			0x100
				 	 	 	 	 	 	 	 */
			}
			larray[6] = nested_lchar;
		}

		lstring = (char*)"hello";	/*	Natural 			'h'
										Decimal 			104
										Hexadecimal			0x68
									*/
	}

	larray[0] = lchar + 19;

	/* step into the following and follow the instructions */
	lint = dbg_call_from_nested_scope((int)larray[0], larray[0]);

	/* after stepping out of the previous function,
	 * re-examine local variables */

	gint = ++lint;
}

/****************************************************************************************************/
void dbg_variables()
{
	dbg_variables_global();
	dbg_variables_local();
	dbg_variables_scoped();
}

/*-------------------------------------------------------------------------------------------------------------           
*************************************************************************************************************

Precondition:
1. Open the Variables view
****************************************************************************************************
Procedure:
Globals:
1. Step into dbg_variables_global()and check the variables view to see if all global vars exist
2. Run to the end of the function then click on every variable and check it's value is shown inside the variable detail pane
3. Click on the "Show Types Names" icon 
4. Click on the "Show Types Names" icon on more time
5. Expand the gstring variable
6. Click on the "Collapse All" icon 
7. For every variable, right-click and cast to a different type then right-click and "Restore to Original Type"
8. For every variable, right-click and choose "Format"
9. From the Format menu, choose Decimal
10. From the Format menu, choose Hexdecimal
11. From the Format menu, choose Natural
12. For every variable, right-click and choose "Change Value"
13. Type a new value inside the "Set Value" window and click "Ok"

Locals:
14. Step out of dbg_variables_global and step into dbg_variables_local.
15. Run to the end of the function then click on every variable and check it's value is shown inside the variable detail pane (For larray, you need to expand it and examine it's elements)
16. Click on the "Show Types Names" icon 
17. Click on the "Show Types Names" icon on more time
18. Expand the lstring and larray variables
19. Click on the "Collapse All" icon 
20. For every variable, right-click and cast to a different type then right-click and "Restore to Original Type"
21. For every variable, right-click and choose "Format"
22. From the Format menu, choose Decimal
23. From the Format menu, choose Hexdecimal
24. From the Format menu, choose Natural
25. For every variable, right-click and choose "Change Value"
26. Type a new value inside the "Set Value" window and click "Ok"
27. Click on the "Add Global Variables" icon
28. Scroll the window and choose: gint, gchar, gdouble, glong and gstring
29. Check the values of each global variable added
30. Highlight both gint and gchar and click on the "Remove Selected Global Variables" icon
31. Without selecting any variables, click on the "Remove All Global Variables" icon
****************************************************************************************************
Expected Results:
Globals
1. The following variables should be displayed inside the variables pane:
	gint
	gchar
	gfloat
	gdouble
	glong
	guint
	gstring
2. The values of the variables inside the variables details pane should be as follow:
	1024
	'a'
	55.55
	222.222
	123456789
	256
	'h'
(This order of values match the order of variables as in step #1)
3. The type names should appear to the left of every variable name as follow:
	int gint = 1024
	char gchar = 'a'
	float gfloat = 55.55
	double gdouble = 222.222
	long glong = 123456789
	unsigned int guint = 256
	char* gstring = 0x0040c808
4. The type names should disppear from the variables view
5. gstring should expand to *gstring = 'h'
6. gstring collapses
7. The variable details pane should display the casted value. Then when restored, should display the original value
(R.S. The exact value for casted variables to be determined later)
8. Sub-menu appear displaying 3 choices: Natural, Decimal and Hexdecimal. The Natural should have a check sign
9. The Decimal format for the variables are as follow:
	gint = 1024
	gchar = 97
	gfloat = 55
	gdouble = 222
	glong = 123456789
	guint = 256
	*gstring = 104
	(in the same order as in step #1) 
10. The Hexdecimal format for the variables are as follow:
	gint = 0x400
	gchar = 0x61
	gfloat = 0x37
	gdouble = 0xde
	glong = 0x075BCD15
	guint = 0x100
	*gstring = 0x68
	(in the same order as in step #1)
11. The original values should be restored:
	gint = 1024
	gchar = 'a'
	gfloat = 55.55
	gdouble = 222.222
	glong = 123456789
	guint = 256
	*gstring = 'h'
12. The "Set Value" window should come up
13. The "Set Value" window disappear and the new value should be displayed inside the variable details pane

Locals:
14. The following variables should be displayed inside the variables pane:
	larray
	lstring
	luint
	llong
	ldouble
	lfloat
	lchar
	lint
	SizeOfInt
15. The values of the variables inside the variables details pane should be as follow:
	larray = "testing"
	*lstring = 'h'
	luint = 256
	llong = 123456789
	ldouble = 222.222
	lfloat = 55.55
	lchar = 'a'
	lint = 1024
	SizeOfInt = 4
16. The type names should appear to the left of every variable name as follow:
	char[8] larray
	char* lstring = 0x0040c808
	unsigned int luint = 256
	long llong = 123456789
	double ldouble = 222.222
	float lfloat = 55.55
	char lchar = 'a'
	int lint = 1024
	int SizeOfInt = 4
17. The type names should disappear from the variables view
18. lstring should expand to --> char *lstring = 'h'
and larray should expand to
	char [0] = 't'
	char [1] = 'e'
	char [2] = 's'
	char [3] = 't'
	char [4] = 'i'
	char [5] = 'n'
	char [6] = 'g'
	char [7] = 0
19. Both lstring and larray collapse
20. The variable details pane should display the casted value. Then when restored, should display the original value
(R.S. The exact value for casted variables to be determined later)
21. Sub-menu appear displaying 3 choices: Natural, Decimal and Hexdecimal. The Natural should have a check sign
22. The Decimal format for the variables are as follow:
	*lstring = 104
	luint = 256
	llong = 123456789
	ldouble = 222
	lfloat = 55
	lchar = 97
	lint = 1024
	(in the same order as in step #15) 
23. The Hexdecimal format for the variables are as follow:
	*lstring = 0x68
	luint = 0x100
	llong = 0x075BCD15
	ldouble = 0xde
	lfloat = 0x37
	lchar = 0x61
	lint = 0x400
	(in the same order as in step #22)
24. The original values should be restored:
	*lstring = 'h'
	luint = 256
	llong = 123456789
	ldouble = 222.222
	lfloat = 55.55
	lchar = 'a'
	lint = 1024
25. The "Set Value" window should come up
26. The "Set Value" window disappear and the new value should be displayed inside the variable details pane
27. Window titled "Selection Needed" should come up
28. The global variables gint, gchar, gdouble, glong and gstring should be added to the variables view
29. All global variables that were just added should have the correct value (as checked in dbg_variables_globals() function)
30. gint and gchar should be removed from the variables view
31. The rest of the global variables should be removed from the variables view
*/
