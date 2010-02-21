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
int square(int);
void for_loops();
void case_statements();
void disassembler();
void function_calls();

/*************************************************************************************************************
File:			dbg_stepping.c		
-------------------------------------------------------------------------------------------------------------			
PRECONDITIONS/ASSUMPTIONS
	
OBJECTIVE
	
	To verify stepping using function calls.
	
PROCEDURE

1. 	Select Run->Step over then Run->Step into. 
2. 	Select Run->Step over twice.
3.  Select Run->Step return.
4. 	Press F5(Step into).
5.  Press F6(Step over) twice.
6.  Press F7(Step return).
7. 	Press F5(Step into).
8.  Press F6(Step over) twice.
9.  Select Run->Step return.
10. Step into first call to noParamsWithReturn().
11. Step over twice.
12. Step return.
13. Step into second call to noParamsWithReturn().
14. Step over twice.
15. Step return.
16. Step into third call to noParamsWithReturn().
17. Step over twice.
18. Step return.
19. Step into call to oneParamWithReturn(12).
20. Step over twice.
21. Step return.
22. Step over.
23. Step into call to oneParamWithReturn(13).
24. Step over twice.
25. Step return.
26. Step over.
27. Step into call to oneParamWithReturn(14).
28. Step over twice.
29. Step return.
30. Step over.
31. Step into call to noParamsWithReturn from oneParamWithReturn(oneParamWithReturn(noParamsWithReturn())).
32. Step over twice.
33. Step return.
34. Step into call to oneParamsWithReturn from oneParamWithReturn(oneParamWithReturn(noParamsWithReturn())).
35. Step over twice.
36. Step return.
37. Step into call to oneParamsWithReturn from oneParamWithReturn(oneParamWithReturn(noParamsWithReturn())).
38. Step over twice.
39. Step return.
40. Step over.
-------------------------------------------------------------------------------------------------------------			
CHECKS

1. 	PC cursor points to noParamsNoReturn(void) function.
2.	PC cursor points to closing bracket. Carp = 23.
3.  PC cursor points to second call to noParamsNoReturn(void) function.
4. 	PC cursor points to noParamsNoReturn(void) function.
5.	PC cursor points to closing bracket. Carp = 23.
6.  PC cursor points to third call to noParamsNoReturn(void) function.
7. 	PC cursor points to noParamsNoReturn(void) function.
8.	PC cursor points to closing bracket. Carp = 23.
9.  PC cursor points to first call to noParamsWithReturn(void) function.
10. PC cursor points to noParamsWithReturn(void) function.
11.	PC cursor points to the return call. Carp = 42.
12. PC cursor points to second call to noParamsWithReturn(void) function.	
13. PC cursor points to noParamsWithReturn(void) function.
14.	PC cursor points to return call. Carp = 42.
15. TPC cursor points to third call to noParamsWithReturn(void) function.	
16. PC cursor points toe noParamsWithReturn(void) function.
17.	The PC cursor shoud be at the return call. Carp = 42.
18. PC cursor points to call to oneParamWithReturn(12) function.
19. PC cursor points to oneParamWithReturn(long in) function.
20. PC cursor points to return call. Carp = 54.
21	PC cursor points to call to oneParamWithReturn(12) function.
22. PC cursor points to call to oneParamWithReturn(13) function.
23. PC cursor points to oneParamWithReturn(long in) function.
24. PC cursor points to return call. Carp = 67.
25	PC cursor points to call to oneParamWithReturn(13) function.
26. PC cursor points to call to oneParamWithReturn(14) function.
27. PC cursor points to oneParamWithReturn(long in) function.
28. PC cursor points to return call. Carp = 81.
29. PC cursor points to call to oneParamWithReturn(14) function.
30. PC cursor points to call to oneParamWithReturn(oneParamWithReturn(noParamsWithReturn()))
31. PC cursor points to noParamsNoReturn(void) function.
32.	PC cursor points to return call. Carp = 42.
33. PC cursor points to call to oneParamWithReturn(oneParamWithReturn(noParamsWithReturn()))
34. PC cursor points to oneParamWithReturn(long in) function.
35.	PC cursor points to return call. Carp = 84.
36.	PC cursor points to call to oneParamWithReturn(oneParamWithReturn(noParamsWithReturn()))
37. PC cursor points to oneParamWithReturn(long in) function.
38.	PC cursor points to return call. Carp = 168.
39. PC cursor points to call to oneParamWithReturn(oneParamWithReturn(noParamsWithReturn()))
40. PC cursor points to closing bracket.
-------------------------------------------------------------------------------------------------------------			
*************************************************************************************************************/
//long carp = 12;

void noParamsNoReturn(void )
{	
	int carp = 23;
	return;
 }
long noParamsWithReturn(void)
{
	int carp = 42;
	return carp;
}
long oneParamWithReturn(long in)
{
	int carp = 54;
	return carp;
}
void function_calls()
{
	noParamsNoReturn(); 	//first call
	noParamsNoReturn(); 	//second call
	noParamsNoReturn(); 	//third call
	noParamsWithReturn();	//first call
	noParamsWithReturn();	//second call
	noParamsWithReturn();	//third call
	oneParamWithReturn(12);
	oneParamWithReturn(13);
	oneParamWithReturn(14);
	oneParamWithReturn(oneParamWithReturn(noParamsWithReturn()));
}

/*************************************************************************************************************
File:			dbg_stepping.c	
-------------------------------------------------------------------------------------------------------------			
PRECONDITIONS/ASSUMPTIONS
	
OBJECTIVE
	
	To verify stepping using for loops.
	
PROCEDURE

-------------------------------------------------------------------------------------------------------------			
CHECKS

1. Step over empty for loop
2. Step over single line for loop with brackets.
3. Step over single line for loop without brackets.
4. Step over for loop with single line function call with brackets.
5. Step over for loop with single line function call without brackets.
6. Step over for loop with statement k = square(k) + square(i); and brackets
7. Step over for loop with statement k = square(k) + square(i); without brackets
8. Step over for loop (i=0, j=0; square(i+j) <= 25; i++, j++) with brackets
9. Step over for loop (i=0, j=0; square(i+j) <= 25; i++, j++) without brackets
10. Step over for loop (i=2; square(i) <= 256; i = square(i)) with brackets
11. Step over for loop (i=2; square(i) <= 256; i = square(i)) without brackets
-------------------------------------------------------------------------------------------------------------			
*************************************************************************************************************/
int square(int x)
{
	return(x*x);
}

void for_loops()
{									
	int i,j,k=0;					
	
	for (i=0; i<3; i++) {}			//No. of step overs: Codeview 4, STABS 1, DWARF 1
	
	for (i=0; i<3; i++) {			//No. of step overs: Codeview 11, STABS 8, DWARF 10
		k *= k;						
	}								
	
	for (i=0; i<3; i++) 			//No. of step overs: Codeview 8, STABS 8, DWARF 10
		k *= k;						
		
	for (i=0; i<3; i++) {			//No. of step overs: Codeview 8, STABS 8, DWARF 10
		k = square(k);				
	}								
	
	for (i=0; i<3; i++)				//No. of step overs: Codeview 5, STABS 8, DWARF 10
		k = square(k);				
		
	for (i=0; i<3; i++) {			//No. of step overs: Codeview 8, STABS 8, DWARF 10
		k = square(k) + square(i);	
	}								
	
	for (i=0; i<3; i++)				//No. of step overs: Codeview 5, STABS 8, DWARF 10
		k = square(k) + square(i);	
											
	for (i=0, j=0; square(i+j) <= 25; i++, j++) { //No. of step overs: Codeview 11, STABS 8, DWARF 10
		k *= k;						
	}								
				
	for (i=0, j=0; square(i+j) <= 25; i++, j++)	 //No. of step overs: Codeview 8, STABS 8, DWARF 10
		k *= k;						

	for (i=2; square(i) <= 256; i = square(i)) { //No. of step overs: Codeview 8, STABS 8, DWARF 10
		k = square(i+k);			
	}								
									
	for (i=2; square(i) <= 256; i = square(i))   //No. of step overs: Codeview 5, STABS 8, DWARF 10
		k = square(i+k);			
		
}

/*************************************************************************************************************
File:			dbg_stepping.c		
-------------------------------------------------------------------------------------------------------------			
PRECONDITIONS/ASSUMPTIONS
	
OBJECTIVE
	
	To verify stepping using case/switch statements.
	
PROCEDURE

1. Step over 1st group of case statements with switch (i)
2. Step over 2nd group of case statements with switch (square(i))
3. Step over 3nd group of case statements
-------------------------------------------------------------------------------------------------------------			
CHECKS

1. Verify switch(i) is reached only 8 times
2. Verify switch (square(i)) is reached only 7 times
3. Verify switch (square(i)) is reached only 7 times
-------------------------------------------------------------------------------------------------------------			
*************************************************************************************************************/
void case_statements()
{							

//  Step over the following case statements. For each case statement, stepping
//  should start at the first case and continue down until the default.
//  Note: You do not need to step into the square function. Always do a step over.

	int i,k=0;				
		
	for (i=1; i<9; i++)		
	{						
		switch (i)			
		{					
		case 1:						
			k = i;			
			break;		
		case 2:				
			k = i+1;		
			break;						
		case 3:				
			k = i+2;		
			break;					
		case 4:				
			k = i+3;		
			break;					
		case 5:				
			k = i+4;		
			break;					
		case 6:				
			k = i+5;		
			break;						
		case 7:				
			k = i+6;		
			break;					
		default:			
			k = 0;			
			break;			
		}					
	}					
	
	
	for (i=2; i<9; i++)		
	{
		switch (square(i))	
		{
		case 4:				
			k = i+3;		
			break;			
		case 9:				
			k = i;			
			break;			
		case 16:
			k = i+5;		
			break;			
		case 25:
			k = i+4;		
			break;			
		case 36:
			k = i+2;		
			break;										
		case 49:
			k = i+1;		
			break;			
		case 64:
			k = i+6;		
			break;			
		default:
			k = 0;			
			break;
		}	
	}						
	
	for (i=2; i<9; i++)		
	{
		switch (square(i))	
		{
		case 4:
			if ((square(square(i)) == 16) && (i < 1000)) { 	 
				square(k);	
			}
			break;			
		case 9:
			if ((square(square(i)) == 81) && (i < 1000)) { 	 
				square(k);	
			}	
			break;			
		case 16:
			if ((square(square(i)) == 256) && (i < 1000)) {  
				square(k);	
			}
			break;			
		case 25:
			if ((square(square(i)) == 625) && (i < 1000)) {  
				square(k);	
			}
			break;			
		case 36:
			if ((square(square(i)) == 1296) && (i < 10000)) {
				square(k);	
			}
			break;												
		case 49:
			if ((square(square(i)) == 2401) && (i < 10000)) {
				square(k);	
			}
			break;			
		case 64:
			if ((square(square(i)) == 4096) && (i < 10000)) {
				square(k);	
			}
			break;			
		default:
			k = 0;
			break;
		}	
	}						//No. of step overs: Codeview 8, STABS x, DWARF x
}							//No. of step overs: Codeview 1, STABS x, DWARF x
/*************************************************************************************************************
File:			dbg_stepping.c	
-------------------------------------------------------------------------------------------------------------			
PRECONDITIONS/ASSUMPTIONS
	
OBJECTIVE
	
	To verify stepping in disassembly.
	
PROCEDURE

1. 	Press the Instruction Stepping Mode button to allow you step in disassembly
    (This should open the Disassembly view).
 	Step over first branch to square in disassembly pane
2. 	Step into the second branch to square in disassembly pane
3. 	Step out of square in disassembly pane
4. 	Step out of assembly function.
-------------------------------------------------------------------------------------------------------------			
CHECKS

1. PC cursor points to second y = square(x) call in the Source pane.
2. PC cursor points to square function in the Source pane.
3. CodeView: PC cursor points to y = square(x); in the Source pane.
4. Verify assembly function is not displayed in stack crawl
-------------------------------------------------------------------------------------------------------------			
*************************************************************************************************************/
//	Execute the following function in Disassembly mode.

void disassembler()
{					//No. of step overs: Codeview 6, STABS x, DWARF x
	volatile int x,y;
	
	x = square(y);	//No. of step overs: Codeview 4, STABS x, DWARF x
	y = square(x);	//Step to the call/branch instruction. Step Into and then Step return
}					//No. of step overs: Codeview 2, STABS x, DWARF x

/*************************************************************************************************************
File:			dbg_stepping.c		
-------------------------------------------------------------------------------------------------------------			
PRECONDITIONS/ASSUMPTIONS
	
OBJECTIVE
	
	To verify stepping using function calls.
	
PROCEDURE
	
1. Run to the breakpoint set at the call to the function
2. Step over the single line statements.

-------------------------------------------------------------------------------------------------------------			
CHECKS


1. Verify if the PC is located at the opening bracket for the function.
2. Verify the values initialized by the single line statements.

-------------------------------------------------------------------------------------------------------------			
*************************************************************************************************************/
void dbg_stepping()
{
	int i=0;		//No. of step overs: Codeview 1, STABS x, DWARF x 
	int j=2;		//No. of step overs: Codeview 1, STABS x, DWARF x
	int k=3;		//No. of step overs: Codeview 1, STABS x, DWARF x
	
	if (i || j || k)//No. of step overs: Codeview 1, STABS x, DWARF x
		i = 12;		//No. of step overs: Codeview 1, STABS x, DWARF x
	else			//Should not get here
		k = 32;
		
/* 	Step into the following functions and follow the directions in the comments. */

	function_calls();
	for_loops();		
	case_statements();	
	disassembler(); 		
	
}

