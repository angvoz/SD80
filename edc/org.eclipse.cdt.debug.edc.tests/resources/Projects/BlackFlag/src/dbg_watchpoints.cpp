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

//  Include Files  


// Global Variables
char g_char;
short g_short;
int g_int;
long g_long;
double g_double;
float g_float;

//  Local Functions
void dbg_watchpoints()
    {
// Local Variables
char lchar = '0';
short lshort = 0;
int lint = 0;
long llong = 0;
double ldouble = 0;
float lfloat = 0;
int stopper = 0;

// Write global variables
g_char = '1';
stopper = 1;

g_short = 2;
stopper = 2;

g_int = 3;
stopper = 3;

g_double = 4.4;
stopper = 4;

g_long = 5;
stopper = 5;

g_float = 6.6;
stopper = 6;

// Read global variables
lchar = g_char;
stopper = 7;

lshort = g_short;
stopper = 8;

lint = g_int;
stopper = 9;

ldouble = g_double;
stopper = 10;

llong = g_long;
stopper = 11;

lfloat = g_float;
stopper = 12;

    }



/***************************** Set read watchpoint **********************************
Steps:
0.. Remove all existing watchpoints and start the debugger
1.. For each and every global variable double click on the variable inside the Editor view and select "Toggle Watchpoint" from the Run menu (see result 1)
2.. Check the "expression to watch" field of the "Add Watchpoint" window (see result 2)
3.. Uncheck "Write" if selected and check "Read". Click "Ok" (see result 3)
4.. Open the properties window for every watchpoint and check the type (see result 4)
5.. Close the properties window and hit Resume (see result 5)
6.. Check the thread inside the Debug view (see result 6)
7.. Hit Resume (see result 7)
8.. Check the thread inside the Debug view (see result 8)
9.. Hit Resume (see result 9)
10. Check the thread inside the Debug view (see result 10)
11. Hit Resume (see result 11)
12. Check the thread inside the Debug view (see result 12)
13. Hit Resume (see result 13)
14. Check the thread inside the Debug view (see result 14)
15. Hit Resume (see result 15)
16. Check the thread inside the Debug view (see result 16)

Expected Results:
1.. (step 1) Once select "Toggle Watchpoint" from the Run menu, the "Add Watchpoint" window opens
2.. (step 2) The "expression to watch" field of the "Add Watchpoint" window should display the name of the variable
3.. (step 3) The "Add Watchpoint" window closes, the watchpoint is added to the Breakpoints view with the Symbol of pair of glasses. Also the Symbol is shown next to the declaration statement inside the Editor view
4.. (step 4) The type should be "C/C++ read watchpoint"
5.. (step 5) The debugger should stop at stopper = 7; statement right after the lchar = g_char statement
6.. (step 6) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
7.. (step 7) The debugger should stop at stopper = 8; statement right after the lshort = g_short statement
8.. (step 8) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
9.. (step 9) The debugger should stop at stopper = 9; statement right after the lint = g_int statement
10. (step 10) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
11. (step 11) The debugger should stop at stopper = 10; statement right after the ldouble = g_double statement
12. (step 12) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
13. (step 13) The debugger should stop at stopper = 11; statement right after the llong = g_long statement
14. (step 14) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
15. (step 15) The debugger should stop at stopper = 12; statement right after the lfloat = g_float statement
16. (step 16) You should see the explaination of the debugger stopped next to the thread name inside the Debug view

***************************** Set write watchpoint **********************************
Steps:
0.. Terminate the debugger, Remove all existing watchpoints and start the debugger
1.. For each and every global variable double click on the variable inside the Editor view and select "Toggle Watchpoint" from the Run menu (see result 1)
2.. Check the "expression to watch" field of the "Add Watchpoint" window (see result 2)
3.. Uncheck "Read" if selected and check "Write". Click "Ok" (see result 3)
4.. Open the properties window for every watchpoint and check the type (see result 4)
5.. Close the properties window and hit Resume (see result 5)
6.. Check the thread inside the Debug view (see result 6)
7.. Hit Resume (see result 7)
8.. Check the thread inside the Debug view (see result 8)
9.. Hit Resume (see result 9)
10. Check the thread inside the Debug view (see result 10)
11. Hit Resume (see result 11)
12. Check the thread inside the Debug view (see result 12)
13. Hit Resume (see result 13)
14. Check the thread inside the Debug view (see result 14)
15. Hit Resume (see result 15)
16. Check the thread inside the Debug view (see result 16)

Expected Results:
1.. (step 1) Once select "Toggle Watchpoint" from the Run menu, the "Add Watchpoint" window opens
2.. (step 2) The "expression to watch" field of the "Add Watchpoint" window should display the name of the variable
3.. (step 3) The "Add Watchpoint" window closes, the watchpoint is added to the Breakpoints view with the Symbol of a pencil. Also the Symbol is shown next to the declaration statement inside the Editor view
4.. (step 4) The type should be "C/C++ write watchpoint"
5.. (step 5) The debugger should stop at stopper = 1; statement right after the g_char = '1' statement
6.. (step 6) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
7.. (step 7) The debugger should stop at stopper = 2; statement right after the g_short = 2 statement
8.. (step 8) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
9.. (step 9) The debugger should stop at stopper = 3; statement right after the g_int = 3 statement
10. (step 10) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
11. (step 11) The debugger should stop at stopper = 4; statement right after the g_double = 4.4 statement
12. (step 12) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
13. (step 13) The debugger should stop at stopper = 5; statement right after the g_long = 5 statement
14. (step 14) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
15. (step 15) The debugger should stop at stopper = 6; statement right after the g_float = 6.6 statement
16. (step 16) You should see the explaination of the debugger stopped next to the thread name inside the Debug view

***************************** Set read-and-write watchpoint **********************************
Steps:
0.. Terminate the debugger, Remove all existing watchpoints and start the debugger
1.. For each and every global variable double click on the variable inside the Editor view and select "Toggle Watchpoint" from the Run menu (see result 1)
2.. Check the "expression to watch" field of the "Add Watchpoint" window (see result 2)
3.. Check both "Read" and "Write". Click "Ok" (see result 3)
4.. Open the properties window for every watchpoint and check the type (see result 4)
5.. Close the properties window and hit Resume (see result 5)
6.. Check the thread inside the Debug view (see result 6)
7.. Hit Resume (see result 7)
8.. Check the thread inside the Debug view (see result 8)
9.. Hit Resume (see result 9)
10. Check the thread inside the Debug view (see result 10)
11. Hit Resume (see result 11)
12. Check the thread inside the Debug view (see result 12)
13. Hit Resume (see result 13)
14. Check the thread inside the Debug view (see result 14)
15. Hit Resume (see result 15)
16. Check the thread inside the Debug view (see result 16)
17. Hit Resume (see result 17)
18. Check the thread inside the Debug view (see result 18)
19. Hit Resume (see result 19)
20. Check the thread inside the Debug view (see result 20)
21. Hit Resume (see result 21)
22. Check the thread inside the Debug view (see result 22)
23. Hit Resume (see result 23)
24. Check the thread inside the Debug view (see result 24)
25. Hit Resume (see result 25)
26. Check the thread inside the Debug view (see result 26)
27. Hit Resume (see result 27)
28. Check the thread inside the Debug view (see result 28)

Expected Results:
1.. (step 1) Once select "Toggle Watchpoint" from the Run menu, the "Add Watchpoint" window opens
2.. (step 2) The "expression to watch" field of the "Add Watchpoint" window should display the name of the variable
3.. (step 3) The "Add Watchpoint" window closes, the watchpoint is added to the Breakpoints view with the Symbol of a pencil and pair of glasses. Also the Symbol is shown next to the declaration statement inside the Editor view
4.. (step 4) The type should be "C/C++ access watchpoint"
5.. (step 5) The debugger should stop at stopper = 1 statement right after the g_char = '1' statement
6.. (step 6) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
7.. (step 7) The debugger should stop at stopper = 2; statement right after the g_short = 2 statement
8.. (step 8) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
9.. (step 9) The debugger should stop at stopper = 3; statement right after the g_int = 3 statement
10. (step 10) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
11. (step 11) The debugger should stop at stopper = 4; statement right after the g_double = 4.4 statement
12. (step 12) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
13. (step 13) The debugger should stop at stopper = 5; statement right after the g_long = 5 statement
14. (step 14) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
15. (step 15) The debugger should stop at stopper = 6; statement right after the g_float = 6.6 statement
16. (step 16) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
17. (step 17) The debugger should stop at stopper = 7; statement right after the lchar = g_char statement
18. (step 18) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
19. (step 19) The debugger should stop at stopper = 8; statement right after the lshort = g_short statement
20. (step 20) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
21. (step 21) The debugger should stop at stopper = 9; statement right after the lint = g_int statement
22. (step 22) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
23. (step 23) The debugger should stop at stopper = 10; statement right after the ldouble = g_double statement
24. (step 24) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
25. (step 25) The debugger should stop at stopper = 11; statement right after the llong = g_long statement
26. (step 26) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
27. (step 27) The debugger should stop at stopper = 12; statement right after the lfloat = g_float statement
28. (step 28) You should see the explaination of the debugger stopped next to the thread name inside the Debug view

***************************** Disable read watchpoint **********************************
Steps:
0.. Remove all existing watchpoints and start the debugger
1.. For each and every global variable double click on the variable inside the Editor view and select "Toggle Watchpoint" from the Run menu (see result 1)
2.. Check the "expression to watch" field of the "Add Watchpoint" window (see result 2)
3.. Uncheck "Write" if selected and check "Read". Click "Ok" (see result 3)
4.. Right-click on the watchpoint at g_char and select "Disable" (see result 4)
5.. Right-click on the watchpoint at g_short and open the Properties window and deselect "Enabled" (see result 5)
6.. Deselect the checkboxes of g_int, g_double, g_long and g_float inside the Breakpoints view (see result 6)
7.. Hit Resume (see result 7)

Expected Results:
1.. (step 1) Once select "Toggle Watchpoint" from the Run menu, the "Add Watchpoint" window opens
2.. (step 2) The "expression to watch" field of the "Add Watchpoint" window should display the name of the variable
3.. (step 3) The "Add Watchpoint" window closes, the watchpoint is added to the Breakpoints view with the Symbol of pair of glasses. Also the Symbol is shown next to the declaration statement inside the Editor view
4.. (step 4) The checkbox is unchecked the watchpoint get greyed out
5.. (step 5) The checkbox is unchecked the watchpoint get greyed out
6.. (step 6) The checkboxes are unchecked the watchpoints get greyed out
7.. (step 7) The debugger doesn't stop at any watchpoint

***************************** Disable write watchpoint **********************************
Steps:
0.. Remove all existing watchpoints and start the debugger
1.. For each and every global variable double click on the variable inside the Editor view and select "Toggle Watchpoint" from the Run menu (see result 1)
2.. Check the "expression to watch" field of the "Add Watchpoint" window (see result 2)
3.. Uncheck "Read" if selected and check "Write". Click "Ok" (see result 3)
4.. Right-click on the watchpoint at g_char and select "Disable" (see result 4)
5.. Right-click on the watchpoint at g_short and open the Properties window and deselect "Enabled" (see result 5)
6.. Deselect the checkboxes of g_int, g_double, g_long and g_float inside the Breakpoints view (see result 6)
7.. Hit Resume (see result 7)

Expected Results:
1.. (step 1) Once select "Toggle Watchpoint" from the Run menu, the "Add Watchpoint" window opens
2.. (step 2) The "expression to watch" field of the "Add Watchpoint" window should display the name of the variable
3.. (step 3) The "Add Watchpoint" window closes, the watchpoint is added to the Breakpoints view with the Symbol of a pencil. Also the Symbol is shown next to the declaration statement inside the Editor view
4.. (step 4) The checkbox is unchecked and the watchpoint get greyed out
5.. (step 5) The checkbox is unchecked and the watchpoint get greyed out
6.. (step 6) The checkboxes are unchecked and the watchpoints get greyed out
7.. (step 7) The debugger doesn't stop at any watchpoint

***************************** Disable read-and-write watchpoint **********************************
Steps:
0.. Remove all existing watchpoints and start the debugger
1.. For each and every global variable double click on the variable inside the Editor view and select "Toggle Watchpoint" from the Run menu (see result 1)
2.. Check the "expression to watch" field of the "Add Watchpoint" window (see result 2)
3.. Check both "Write" and "Read". Click "Ok" (see result 3)
4.. Right-click on the watchpoint at g_char and select "Disable" (see result 4)
5.. Right-click on the watchpoint at g_short and open the Properties window and deselect "Enabled" (see result 5)
6.. Deselect the checkboxes of g_int, g_double, g_long and g_float inside the Breakpoints view (see result 6)
7.. Hit Resume (see result 7)

Expected Results:
1.. (step 1) Once select "Toggle Watchpoint" from the Run menu, the "Add Watchpoint" window opens
2.. (step 2) The "expression to watch" field of the "Add Watchpoint" window should display the name of the variable
3.. (step 3) The "Add Watchpoint" window closes, the watchpoint is added to the Breakpoints view with the Symbol of pair of glasses and pencil. Also the Symbol is shown next to the declaration statement inside the Editor view
4.. (step 4) The checkbox is unchecked and the watchpoint get greyed out
5.. (step 5) The checkbox is unchecked and the watchpoint get greyed out
6.. (step 6) The checkboxes are unchecked and the watchpoints get greyed out
7.. (step 7) The debugger doesn't stop at any watchpoint

***************************** Enable read watchpoint **********************************
Steps:
0.. Remove all existing watchpoints and start the debugger
1.. For each and every global variable double click on the variable inside the Editor view and select "Toggle Watchpoint" from the Run menu (see result 1)
2.. Check the "expression to watch" field of the "Add Watchpoint" window (see result 2)
3.. Uncheck "Write" if selected and check "Read". Click "Ok" (see result 3)
4.. Right-click on the watchpoint at g_char and select "Disable" (see result 4)
5.. Right-click on the watchpoint at g_short and open the Properties window and deselect "Enabled" (see result 5)
6.. Deselect the checkboxes of g_int, g_double, g_long and g_float inside the Breakpoints view (see result 6)
7.. Hit Resume (see result 7)
8.. Terminate the debugger and Restart it and stop at the entry point (see result 8)
9.. Right-click on the watchpoint at g_char and select "Enable" (see result 9)
10. Right-click on the watchpoint at g_short and open the Properties window and select "Enabled" (see result 10)
11. Select the checkboxes of g_int, g_double, g_long and g_float inside the Breakpoints view (see result 11)
12. Hit Resume (see result 12)
13. Check the thread inside the Debug view (see result 13)
14. Hit Resume (see result 14)
15. Check the thread inside the Debug view (see result 15)
16. Hit Resume (see result 16)
17. Check the thread inside the Debug view (see result 17)
18. Hit Resume (see result 18)
19. Check the thread inside the Debug view (see result 19)
20. Hit Resume (see result 20)
21. Check the thread inside the Debug view (see result 21)
22. Hit Resume (see result 22)
23. Check the thread inside the Debug view (see result 23)

Expected Results:
1.. (step 1) Once select "Toggle Watchpoint" from the Run menu, the "Add Watchpoint" window opens
2.. (step 2) The "expression to watch" field of the "Add Watchpoint" window should display the name of the variable
3.. (step 3) The "Add Watchpoint" window closes, the watchpoint is added to the Breakpoints view with the Symbol of pair of glasses. Also the Symbol is shown next to the declaration statement inside the Editor view
4.. (step 4) The checkbox is unchecked and the watchpoint get greyed out
5.. (step 5) The checkbox is unchecked and the watchpoint get greyed out
6.. (step 6) The checkboxes are unchecked and the watchpoints get greyed out
7.. (step 7) The debugger doesn't stop at any watchpoint
8.. (step 8) All the watchpoints should appear disabled in both the Breakpoints view and Editor view
9.. (step 9) The checkbox is checked and the watchpoint should appear active
10. (step 10) The checkbox is checked and the watchpoint should appear active
11. (step 11) The checkboxes are checked and the watchpoints should appear active
12. (step 12) The debugger should stop at stopper = 7; statement right after the lchar = g_char statement
13. (step 13) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
14. (step 14) The debugger should stop at stopper = 8; statement right after the lshort = g_short statement
15. (step 15) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
16. (step 16) The debugger should stop at stopper = 9; statement right after the lint = g_int statement
17. (step 17) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
18. (step 18) The debugger should stop at stopper = 10; statement right after the ldouble = g_double statement
19. (step 19) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
20. (step 20) The debugger should stop at stopper = 11; statement right after the llong = g_long statement
21. (step 21) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
22. (step 22) The debugger should stop at stopper = 12; statement right after the lfloat = g_float statement
23. (step 23) You should see the explaination of the debugger stopped next to the thread name inside the Debug view

***************************** Enable write watchpoint **********************************
Steps:
0.. Remove all existing watchpoints and start the debugger
1.. For each and every global variable double click on the variable inside the Editor view and select "Toggle Watchpoint" from the Run menu (see result 1)
2.. Check the "expression to watch" field of the "Add Watchpoint" window (see result 2)
3.. Uncheck "Read" if selected and check "Write". Click "Ok" (see result 3)
4.. Right-click on the watchpoint at g_char and select "Disable" (see result 4)
5.. Right-click on the watchpoint at g_short and open the Properties window and deselect "Enabled" (see result 5)
6.. Deselect the checkboxes of g_int, g_double, g_long and g_float inside the Breakpoints view (see result 6)
7.. Hit Resume (see result 7)
8.. Terminate the debugger and Restart it and stop at the entry point (see result 8)
9.. Right-click on the watchpoint at g_char and select "Enable" (see result 9)
10. Right-click on the watchpoint at g_short and open the Properties window and select "Enabled" (see result 10)
11. Select the checkboxes of g_int, g_double, g_long and g_float inside the Breakpoints view (see result 11)
12. Hit Resume (see result 12)
13. Check the thread inside the Debug view (see result 13)
14. Hit Resume (see result 14)
15. Check the thread inside the Debug view (see result 15)
16. Hit Resume (see result 16)
17. Check the thread inside the Debug view (see result 17)
18. Hit Resume (see result 18)
19. Check the thread inside the Debug view (see result 19)
20. Hit Resume (see result 20)
21. Check the thread inside the Debug view (see result 21)
22. Hit Resume (see result 22)
23. Check the thread inside the Debug view (see result 23)

Expected Results:
1.. (step 1) Once select "Toggle Watchpoint" from the Run menu, the "Add Watchpoint" window opens
2.. (step 2) The "expression to watch" field of the "Add Watchpoint" window should display the name of the variable
3.. (step 3) The "Add Watchpoint" window closes, the watchpoint is added to the Breakpoints view with the Symbol of a pencil. Also the Symbol is shown next to the declaration statement inside the Editor view
4.. (step 4) The checkbox is unchecked and the watchpoint get greyed out
5.. (step 5) The checkbox is unchecked and the watchpoint get greyed out
6.. (step 6) The checkboxes are unchecked and the watchpoints get greyed out
7.. (step 7) The debugger doesn't stop at any watchpoint
8.. (step 8) All the watchpoints should appear disabled in both the Breakpoints view and Editor view
10. (step 10) The checkbox is checked and the watchpoint should appear active
11. (step 11) The checkboxes are checked and the watchpoints should appear active
12. (step 12) The debugger should stop at stopper = 1; statement 
13. (step 13) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
14. (step 14) The debugger should stop at stopper = 2; statement 
15. (step 15) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
16. (step 16) The debugger should stop at stopper = 3;+ statement 
17. (step 17) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
18. (step 18) The debugger should stop at stopper = 4; statement 
19. (step 19) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
20. (step 20) The debugger should stop at stopper = 5; statement 
21. (step 21) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
22. (step 22) The debugger should stop at stopper = 6; statement 
23. (step 23) You should see the explaination of the debugger stopped next to the thread name inside the Debug view

***************************** Enable read-and-write watchpoint **********************************
Steps:
0.. Remove all existing watchpoints and start the debugger
1.. For each and every global variable double click on the variable inside the Editor view and select "Toggle Watchpoint" from the Run menu (see result 1)
2.. Check the "expression to watch" field of the "Add Watchpoint" window (see result 2)
3.. Check both "Write" and "Read". Click "Ok" (see result 3)
4.. Right-click on the watchpoint at g_char and select "Disable" (see result 4)
5.. Right-click on the watchpoint at g_short and open the Properties window and deselect "Enabled" (see result 5)
6.. Deselect the checkboxes of g_int, g_double, g_long and g_float inside the Breakpoints view (see result 6)
7.. Hit Resume (see result 7)
8.. Terminate the debugger and Restart it and stop at the entry point (see result 8)
9.. Close the properties window and hit Resume (see result 9)
10. Check the thread inside the Debug view (see result 10)
11. Hit Resume (see result 11)
12. Check the thread inside the Debug view (see result 12)
13. Hit Resume (see result 13)
14. Check the thread inside the Debug view (see result 14)
15. Hit Resume (see result 15)
16. Check the thread inside the Debug view (see result 16)
17. Hit Resume (see result 17)
18. Check the thread inside the Debug view (see result 18)
19. Hit Resume (see result 19)
20. Check the thread inside the Debug view (see result 20)
21. Hit Resume (see result 21)
22. Check the thread inside the Debug view (see result 22)
23. Hit Resume (see result 23)
24. Check the thread inside the Debug view (see result 24)
25. Hit Resume (see result 25)
26. Check the thread inside the Debug view (see result 26)
27. Hit Resume (see result 27)
28. Check the thread inside the Debug view (see result 28)
29. Hit Resume (see result 29)
30. Check the thread inside the Debug view (see result 30)
31. Hit Resume (see result 31)
32. Check the thread inside the Debug view (see result 32)



Expected Results:
1.. (step 1) Once select "Toggle Watchpoint" from the Run menu, the "Add Watchpoint" window opens
2.. (step 2) The "expression to watch" field of the "Add Watchpoint" window should display the name of the variable
3.. (step 3) The "Add Watchpoint" window closes, the watchpoint is added to the Breakpoints view with the Symbol of pair of glasses and pencil. Also the Symbol is shown next to the declaration statement inside the Editor view
4.. (step 4) The checkbox is unchecked and the watchpoint get greyed out
5.. (step 5) The checkbox is unchecked and the watchpoint get greyed out
6.. (step 6) The checkboxes are unchecked and the watchpoints get greyed out
7.. (step 7) The debugger doesn't stop at any watchpoint
8.. (step 8) All the watchpoints should appear disabled in both the Breakpoints view and Editor view
9.. (step 9) The debugger should stop at stopper = 1; statement right after the g_char = '1' statement
10. (step 10) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
11. (step 11) The debugger should stop at stopper = 2; statement right after the g_short = 2 statement
12. (step 12) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
13. (step 13) The debugger should stop at stopper = 3; statement right after the g_int = 3 statement
14. (step 14) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
15. (step 15) The debugger should stop at stopper = 4; statement right after the g_double = 4.4 statement
16. (step 16) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
17. (step 17) The debugger should stop at stopper = 5; statement right after the g_long = 5 statement
18. (step 18) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
19. (step 19) The debugger should stop at stopper = 6 statement right after the g_float = 6.6 statement
20. (step 20) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
21. (step 21) The debugger should stop at stopper = 7; statement right after the lchar = g_char statement
22. (step 22) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
23. (step 23) The debugger should stop at stopper = 8; statement right after the lshort = g_short statement
24. (step 24) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
25. (step 25) The debugger should stop at stopper = 9; statement right after the lint = g_int statement
26. (step 26) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
27. (step 27) The debugger should stop at stopper = 10; statement right after the ldouble = g_double statement
28. (step 28) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
29. (step 29) The debugger should stop at stopper = 11; statement right after the llong = g_long statement
30. (step 30) You should see the explaination of the debugger stopped next to the thread name inside the Debug view
31. (step 31) The debugger should stop at stopper = 12; statement right after the lfloat = g_float statement
32. (step 32) You should see the explaination of the debugger stopped next to the thread name inside the Debug view

***************************** Remove read watchpoint **********************************
Steps:
0.. Remove all existing watchpoints and start the debugger
1.. For each and every global variable double click on the variable inside the Editor view and select "Toggle Watchpoint" from the Run menu (see result 1)
2.. Check the "expression to watch" field of the "Add Watchpoint" window (see result 2)
3.. Uncheck "Write" if selected and check "Read". Click "Ok" (see result 3)
4.. Right-click on the watchpoint at g_char and select "Remove" (see result 4)
5.. Select all the other watchpoints and click the 'X' remove icon on the Breakpoints view title bar (see result 5)
6.. Hit Resume (see result 6)

Expected Results:
1.. (step 1) Once select "Toggle Watchpoint" from the Run menu, the "Add Watchpoint" window opens
2.. (step 2) The "expression to watch" field of the "Add Watchpoint" window should display the name of the variable
3.. (step 3) The "Add Watchpoint" window closes, the watchpoint is added to the Breakpoints view with the Symbol of pair of glasses. Also the Symbol is shown next to the declaration statement inside the Editor view
4.. (step 4) The watchpoint should be removed from the Breakpoints view and the Symbol is removed from the declaration line in the Editor view
5.. (step 5) All the watchpoints should be removed from the Breakpoints view and the Symboles are removed from the declaration lines in the Editor view
6.. (step 6) The debugger should not stop at any line where watchpoints were removed from

***************************** Remove write watchpoint **********************************
Steps:
0.. Remove all existing watchpoints and start the debugger
1.. For each and every global variable double click on the variable inside the Editor view and select "Toggle Watchpoint" from the Run menu (see result 1)
2.. Check the "expression to watch" field of the "Add Watchpoint" window (see result 2)
3.. Uncheck "Read" if selected and check "Write". Click "Ok" (see result 3)
4.. Right-click on the watchpoint at g_char and select "Remove" (see result 4)
5.. Select all the other watchpoints and click the 'X' remove icon on the Breakpoints view title bar (see result 5)
6.. Hit Resume (see result 6)

Expected Results:
1.. (step 1) Once select "Toggle Watchpoint" from the Run menu, the "Add Watchpoint" window opens
2.. (step 2) The "expression to watch" field of the "Add Watchpoint" window should display the name of the variable
3.. (step 3) The "Add Watchpoint" window closes, the watchpoint is added to the Breakpoints view with the Symbol of a pencil. Also the Symbol is shown next to the declaration statement inside the Editor view
4.. (step 4) The watchpoint should be removed from the Breakpoints view and the Symbol is removed from the declaration line in the Editor view
5.. (step 5) All the watchpoints should be removed from the Breakpoints view and the Symboles are removed from the declaration lines in the Editor view
6.. (step 6) The debugger should not stop at any line where watchpoints were removed from

***************************** Remove read-and-write watchpoint **********************************
Steps:
0.. Remove all existing watchpoints and start the debugger
1.. For each and every global variable double click on the variable inside the Editor view and select "Toggle Watchpoint" from the Run menu (see result 1)
2.. Check the "expression to watch" field of the "Add Watchpoint" window (see result 2)
3.. Check both "Read" and "Write". Click "Ok" (see result 3)
4.. Right-click on the watchpoint at g_char and select "Remove" (see result 4)
5.. Select all the other watchpoints and click the 'X' remove icon on the Breakpoints view title bar (see result 5)
6.. Hit Resume (see result 6)

Expected Results:
1.. (step 1) Once select "Toggle Watchpoint" from the Run menu, the "Add Watchpoint" window opens
2.. (step 2) The "expression to watch" field of the "Add Watchpoint" window should display the name of the variable
3.. (step 3) The "Add Watchpoint" window closes, the watchpoint is added to the Breakpoints view with the Symbol of pair of glasses and pencil. Also the Symbol is shown next to the declaration statement inside the Editor view
4.. (step 4) The watchpoint should be removed from the Breakpoints view and the Symbol is removed from the declaration line in the Editor view
5.. (step 5) All the watchpoints should be removed from the Breakpoints view and the Symboles are removed from the declaration lines in the Editor view
6.. (step 6) The debugger should not stop at any line where watchpoints were removed from

***************************** Set a Conditional read watchpoint **********************************
Steps:
0.. Remove all existing watchpoints and start the debugger
1.. For each and every global variable double click on the variable inside the Editor view and select "Toggle Watchpoint" from the Run menu (see result 1)
2.. Check the "expression to watch" field of the "Add Watchpoint" window (see result 2)
3.. Uncheck "Write" if selected and check "Read". Click "Ok" (see result 3)
4.. Right-click on the watchpoint at g_char and open the properties window then set this condition ---> g_char=='1' then click "ok" (see result 4)
5.. Right-click on the watchpoint at g_short and open the properties window then set this condition ---> g_short==1 then click "ok" (see result 5)
6.. Right-click on the watchpoint at g_int and open the properties window then set this condition ---> i<1 then click "ok" (see result 6)
7.. Hit Resume (see result 7)
8.. Hit Resume (see result 8)

Expected Results:
1.. (step 1) Once select "Toggle Watchpoint" from the Run menu, the "Add Watchpoint" window opens
2.. (step 2) The "expression to watch" field of the "Add Watchpoint" window should display the name of the variable
3.. (step 3) The "Add Watchpoint" window closes, the watchpoint is added to the Breakpoints view with the Symbol of pair of glasses. Also the Symbol is shown next to the declaration statement inside the Editor view
4.. (step 4) The condition should appear to the right of the watchpoint at g_char inside the breakpoints view
5.. (step 5) The condition should appear to the right of the watchpoint at g_short inside the breakpoints view
6.. (step 6) The condition should appear to the right of the watchpoint at g_int inside the breakpoints view
7.. (step 7) The debugger shouild stop at stopper = 1; statement that comes right after g_char = '1'
8.. (step 8) The debugger shouild stop at stopper = 4; statement that comes right after g_double = 4.4

***************************** Set a Conditional write watchpoint **********************************
Steps:
0.. Remove all existing watchpoints and start the debugger
1.. For each and every global variable double click on the variable inside the Editor view and select "Toggle Watchpoint" from the Run menu (see result 1)
2.. Check the "expression to watch" field of the "Add Watchpoint" window (see result 2)
3.. Uncheck "Read" if selected and check "Write". Click "Ok" (see result 3)
4.. Right-click on the watchpoint at g_char and open the properties window then set this condition ---> i==1 then click "ok" (see result 4)
5.. Right-click on the watchpoint at g_short and open the properties window then set this condition ---> i==0 then click "ok" (see result 5)
6.. Right-click on the watchpoint at g_int and open the properties window then set this condition ---> i>=0 then click "ok" (see result 6)
7.. Hit Resume (see result 7)
8.. Hit Resume (see result 8)

Expected Results:
1.. (step 1) Once select "Toggle Watchpoint" from the Run menu, the "Add Watchpoint" window opens
2.. (step 2) The "expression to watch" field of the "Add Watchpoint" window should display the name of the variable
3.. (step 3) The "Add Watchpoint" window closes, the watchpoint is added to the Breakpoints view with the Symbol of pencil. Also the Symbol is shown next to the declaration statement inside the Editor view
4.. (step 4) The condition should appear to the right of the watchpoint at g_char inside the breakpoints view
5.. (step 5) The condition should appear to the right of the watchpoint at g_short inside the breakpoints view
6.. (step 6) The condition should appear to the right of the watchpoint at g_int inside the breakpoints view
7.. (step 7) The debugger shouild stop at stopper = 2; statement that comes right after g_short = 2
8.. (step 8) The debugger shouild stop at stopper = 3; statement that comes right after g_int = 3

***************************** Set a Conditional read-and-write watchpoint **********************************
 * Steps:
0.. Remove all existing watchpoints and start the debugger
1.. For each and every global variable double click on the variable inside the Editor view and select "Toggle Watchpoint" from the Run menu (see result 1)
2.. Check the "expression to watch" field of the "Add Watchpoint" window (see result 2)
3.. Check both "Read" and "Write". Click "Ok" (see result 3)
4.. Right-click on the watchpoint at g_char and open the properties window then set this condition ---> i==1 then click "ok" (see result 4)
5.. Right-click on the watchpoint at g_short and open the properties window then set this condition ---> i==0 then click "ok" (see result 5)
6.. Right-click on the watchpoint at g_int and open the properties window then set this condition ---> i>=0 then click "ok" (see result 6)
7.. Hit Resume (see result 7)
8.. Hit Resume (see result 8)
9.. Hit Resume (see result 9)
10. Hit Resume (see result 10)

Expected Results:
1.. (step 1) Once select "Toggle Watchpoint" from the Run menu, the "Add Watchpoint" window opens
2.. (step 2)  The "expression to watch" field of the "Add Watchpoint" window should display the name of the variable
3.. (step 3)  The "Add Watchpoint" window closes, the watchpoint is added to the Breakpoints view with the Symbol of pencil ans pair of glasses. Also the Symbol is shown next to the declaration statement inside the Editor view
4.. (step 4)  The condition should appear to the right of the watchpoint at g_char inside the breakpoints view
5.. (step 5)  The condition should appear to the right of the watchpoint at g_short inside the breakpoints view
6.. (step 6)  The condition should appear to the right of the watchpoint at g_int inside the breakpoints view
7.. (step 7)  The debugger shouild stop at stopper = 2; statement that comes right after g_short = 2
8.. (step 8)  The debugger shouild stop at stopper = 3; statement that comes right after g_int = 3
9.. (step 9)  The debugger shouild stop at stopper = 8; statement that comes right after lshort = g_short
10. (step 10) The debugger shouild stop at stopper = 9; statement that comes right after lint = g_int
*/
