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

void dbg_expressions()
{
	gchar = '1';
#ifdef _Floating_Point_Support_
	gdouble = 2.2;
	gfloat = 3.3;	
#endif 
	gint = 4;
	glong = 5;
#ifdef _Floating_Point_Support_
	glongdouble = 6.6;
#endif 
	gschar = '7';	
	gshort = 8;
	gsint = 9;
	gslong = 10;
#ifdef __MSL_LONGLONG_SUPPORT__	
	gslonglong = 11;
#endif
	gsshort = 12;
	guchar = 13;
	guint = 14;
	gulong = 15;
#ifdef __MSL_LONGLONG_SUPPORT__	
	gulonglong = 16;
#endif
	gushort = 17;	
	
	gstruct.achar = '1';
	gbitfield.x = 1;
	gunion.x = 1;
	genum = one;
	garray[0] = 1;
	gstring = "one";	

	gstruct.achar = '2';
	gbitfield.x = 0;
	gunion.x = 2;
	genum = two;
	garray[0] = 2;
	gstring = "two";
		
	gchar = '2';
	gchar = '3';
	gchar = '4';
	gint = 2;
	
	char lchar = '1';
	int lint = 4;
	long llong = 5;
#ifdef _Floating_Point_Support_
	double ldouble = 2.2;
	float lfloat = 3.3;		
	long double llongdouble = 6.6;
#endif	
	SCHAR lschar = '7';	
	short lshort = 8;
	SINT lsint = 9;
	SLONG lslong = 10;
#ifdef __MSL_LONGLONG_SUPPORT__		
	SLONGLONG lslonglong = 11;
#endif	
	SSHORT lsshort = 12;
	UCHAR luchar = 13;
	UINT luint = 14;
	ULONG lulong = 15;
#ifdef __MSL_LONGLONG_SUPPORT__	
	ULONGLONG lulonglong = 16;
#endif
	USHORT lushort = 17;	
	
	lchar++;	/* set breakpoint here */
	lint++;
	llong++;
#ifdef _Floating_Point_Support_
	ldouble++;
	lfloat++;
	llongdouble++;
#endif
	lschar++;
	lshort++;
	lsint++;
	lslong++;
#ifdef __MSL_LONGLONG_SUPPORT__		
	lslonglong++;
#endif
	lsshort++;
	luchar++;
	luint++;
	lulong++;
#ifdef __MSL_LONGLONG_SUPPORT__		
	lulonglong++;
#endif	
	lushort++;
	int stopper = 0;		/* set breakpoint here */
}				

/**************************************************************************************************
File:			dbg_expressions.cpp	

---------------------------------------------------------------------------------------------------			
PRECONDITIONS

1. Have both C/C++ Prespective open before start debugging
2. Open the Expressions view
3. Open the dbg_expressions.cpp file
	
OBJECTIVE
	
1	To verify the Expressions pane works correctly.
	
PROCEDURE

1	In the Variables pane right click on gchar, select Watch and Step Over
    the initialization of gchar.
2	Right click in the Expressions pane and select Add Watch Expression.In the Add
	Watch Expression window type "gdouble" Step over the initialization of gdouble.
3	In the Debug perspective, open dbg_expressions.cpp file, right click on a line 
	and select Add a Watch Expression.  In the Add Watch Expressions window type
	"gfloat" and click OK.  Step Over the initialization of gfloat.
4.	Switch to the Symbian persective, open dbg_expressions.cpp file, right click on a
	line and select Add a Watch Expression. In the Add Watch Expression window type
	"gint" and click OK. Switch to the Debug perspective and Step Over the initialization
	of gint.  .
5.	Switch to the C++ persective, open dbg_expressions.cpp file, right click on a
	line and select Add a Watch Expression. In the Add Watch Expression window type
	"glong" and click OK.. Switch to the Debug perspective and Step Over the initialization
	of glong. 
	The  value of the variable in the Expressions pane should be zero.
6	Right click on each simple data type in the Variables pane, select Watch 
	and Step Over the initilization of each variable.
7	In the Expressions pane try to change the value of each of the simple data types.
8	Right click on gstruct in the Variables pane and select Watch. Step Over the 
 	initialization of gstruct.achar. Expand gstruct in the Expressions pane.
9	Right click on gbitfield in the Variables pane and select Watch. Step Over the 
 	initialization of gbitfield.x. Expand gbitfield in the Expressions pane.
10	Right click on gunion in the Variables pane and select Watch. Step Over the 
 	initialization of gunion.x. Expand gunion in the Expressions pane. 	
11	Right click on genum in the Variables pane and select Watch. Step Over the 
 	initialization of genum. 
12	Right click on garray in the Variables pane and select Watch. Step Over the 
 	initialization of garray[0]. Expand garray in the Expressions pane. 
13	Right click on gstring in the Variables pane and select Watch. Step Over the 
 	initialization of gstring. Expand gstring in the Expressions pane. 
14 	Expand gstruct in the Variables pane, right click on gstruct.achar and select Watch.
	Step over the assignment "gstruct.achar = '2'".
15 	Expand gbitfield in the Variables pane, right click on "bitfield.x and select Watch.
	Step over the assignment "gbitfield.x = 2".	
16 	Expand gunion in the Variables pane, right click on gunion.x and select Watch.
	Step over the assignment "gunion.x = 0".  	
17 	Expand garray in the Variables pane, right click on garray[0] and select Watch.
	Step over the assignment "garray[0] = 2". 
18	In the Expressions window, click on "gchar" and hit the Delete key. 

Contextual Menu

Select All
19 	Right-click in the Expressions pane and select Select All.
20 	Unselect all first and then click anywhere inside the Expressions pane and press CTRL+A.

Change Value
21 	Go to the Variables view and for each variable and member variable of each derived type right click and select 
  	Change Value. Change the value in the Set Value window and press OK.

Add Watch Expression
22	Right click in the Expression window and select Add Watch Expression. Type
	"gchar" in the Add Watch Expression window.  Step over the assignement "gchar = 2";
	Using Add Watch Expression add back "gint" and "glong" to the Expression pane.

Disable
23  Right click on gchar in the Variables pane and select Watch. Right click on gchar
	in the Expressions pane and select Disable. Step over the assignement "gchar = 3";  
	
Enable
24  Right click on gchar in the Expressions pane and select Enable. Step Over the 
	assignement "gchar = 4";  
	
Edit Watch Expression
25	Right click on gchar in the Expression pane and select Edit Watch Expression.  Type in 
  	"gint" in the Edit Watch Expression window. Click OK. Step Over the assignment
  	"gint = 2". 
	 
Remove
26	In the Expressions window, right click on one expression and select Remove.

Remove All
27 	In the Expressions window, right click on one expression and select Remove All.

EXPRESSIONS TOOLBAR

Collapse All
28	Add all the derived types back into the Expresssion pane and expand each of them.
	On the Expressions toolbar press Collapse All

Show Types
29	In the Expression toolbar press the Show Types icon.
	
Remove
30	In the Expressions window, right click on one expression and from the Expressions
	toolbar press Remove.

Remove All
31 	In the Expressions window, right click on one expression and from the Expressions
	toolbar press Remove All.
	
Local Variables
32	From the Variables view select all the local variables, righ-click and select watch
33	Set a breakpoint at lchar++; then run to the breakpoint
34	From the Expressions view righ-click and select "Add Watch Expression" then type in lchar == '1'
35	Step over the line lchar++; and examin the value of the expression lchar == '1'
36	From the Expressions view righ-click and select "Add Watch Expression" then type in lint == 5
37	Step over the line ldouble++; and examin the value of the expression lint == 5
38	From the Expressions view righ-click and select "Add Watch Expression" then type in llong + 5
39	Step over the line lchar++; and examin the value of the expression llong + 5
40	Set a breakpoint at the close brace of the function and run to the breakpoint
-----------------------------------------------------------------------------------------------------			
CHECKS

1	The value in the Expressions pane should match the value in the Variables pane.
2	The value in the Expressions pane should match the value in the Variables pane.
3	The value in the Expressions pane should match the value in the Variables pane.
4	The value in the Expressions pane should match the value in the Variables pane.
5	The value in the Expressions pane should match the value in the Variables pane.
6	The values in the Expressions pane should match the value in the Variables pane.
7	You should not be able to change the value of any variable in the Expressions pane.
8	The values in the Expressions pane should match the value in the Variables pane.
9	The values in the Expressions pane should match the value in the Variables pane.
10	The values in the Expressions pane should match the value in the Variables pane.
11	The values in the Expressions pane should match the value in the Variables pane.
12	The values in the Expressions pane should match the value in the Variables pane.
13	The values in the Expressions pane should match the value in the Variables pane.
14	The value in the Expressions pane should match the value in the Variables pane.	
15	The value in the Expressions pane should match the value in the Variables pane.
16	The value in the Expressions pane should match the value in the Variables pane.
17	The value in the Expressions pane should match the value in the Variables pane.
18	The variable is removed from the Expressions pane.

CONTEXTUAL MENU 

Select All
19	All the variables in the Expression pane should be highilighted.
20	All the variables in the Expression pane should be highilighted.

Change Value
21 	The value for that variable should change in the Expressions pane and in the 
	Variables pane.
 
Add Watch Expresssion
22	The variable "gchar" should be added to the Expressions window. The value in
 	the Expressions pane should match the value in the Variables pane.
 	
Disable
23	The variable is marked as disabled in the Expressions pane.  That should not 
	change when the value of the variable changes.
	
Enable	
24 	The variable is no longer marked disabled in the Expressions pane. The value in 
	the Expressions pane should match the value in the Variables pane.of the variable
	in the Expressions pane.

Edit Watch Expression
25 	The value in the Expressions pane should match the value in the Variables pane.

	
Remove
26 	The variable is removed from the Expressions pane.

Remove All
27 	All the variables are removed from the Expressions pane.

EXPRESSIONS TOOLBAR

Collapse All
28	Each derived type should be collapsed.

Show Types
29	For each variable in the Expression window the type should be displayed. 

Remove
30 	The variable is removed from the Expressions pane.

Remove All
31 	All the variables are removed from the Expressions pane.

Local Variables
32	You should see all the local variables now added to the Expressions view
33	In the Expressions view you should see the values of all the local variables as the values each was initialized
34	You should see a new expression lchar == '1' was added and its value is "true"
35	The value of the expression lchar == '1' becomes "false"
36	You should see a new expression lint == 5 was added and its value is "false"
37	The value of the expression lint == 5 becomes "true"
38	You should see a new expression llong + 5 was added and its value is 10
39	The value of the expression llong + 5 becomes 11
40	The values of each local variable should be incremented by 1 inside the Expressions view
**************************************************************************************************/
