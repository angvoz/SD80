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
#define LIMIT	10000

void dbg_program_control()
{ 	volatile int w = 1;
	volatile int x = 1;   
	volatile int y = 1;
	   
	while (x) {	
		if (y == LIMIT)
			y = 0;
		y++;
		w = y - 1;
	}
}

/*******************************************************************************

File:			dbg_program_control.c
Function:		program_control()

Classes Used:	n/a
Libs Used:		n/a
--------------------------------------------------------------------------------			
PRECONDITIONS/ASSUMPTIONS

1. 	In the Launch Configuration setting select the Debug tab and check
	"break at entry point". This is because not every project has E32Main
2. 	A Launch Configuration should already be setup for the project.
3. 	If you wish to verify the Terminate button from Console pane's toolbar
	then you will need to run BlackFlag as an app so you can relaunch the 
	app from the emulator.
	
OBJECTIVES
	
The following work correctly: 

1. The tool bar icons: Debug, Suspend, Restart, Resume and Terminate 
2. The Run menu commands: Debug, Suspend, Restart, Resume and Terminate
3. The contexual menu commands: Debug, Suspend, Restart, Resume and Terminate
	
PROCEDURE
		
 	RUN MENU 	
1. From the Run menu select "Debug"
2. Take note of the thread number
3. From the Run menu select "Resume"
4. Using the thread number, select the thread inside the stack crawl then from 
the Run menu select "Suspend"
5. From the Run menu select "Resume"
6. From the Run menu select "Restart"
7. From the Run menu select "Terminate"

 	DEBUG/SUSPEND/RESUME/RESTART/TERMINATE icons on the tool bar
8. Click on the "Debug" tool bar icon
9. Take note of the thread number	
10. Click on the "Resume" tool bar icon
11. Using the thread number, select the thread inside the stack crawl then from 
Click the "Suspend" tool bar icon
12. Click on the "Resume" tool bar icon
13. Click on the "Restart" tool bar icon
14. Click on the "Terminate" tool bar icon
	
	CONTEXTUAL MENU: STACK CRAWL
15. Start the debugger 
16. Take note of the thread number
17. Right-click on the Debu view and select "Resume" contextual menu command
18. Using the thread number, select the thread inside the stack crawl then from 
the right-click and select "Suspend" contextual menu command
19. Right-click on the Debu view and select "Resume" contextual menu command
20. Right-click on the Debu view and select "Restart" contextual menu command
21. Right-click on the Debu view and select "Terminate" contextual menu command
	

 	

--------------------------------------------------------------------------------			
CHECKS

	RUN MENU
1. The debugger should stop at E32Main
2. NO CHECK
3. The debugger thread should show as "Running", the source file should be blank
 and the Variables view should be blank
4. The debugger thread should show as suspended, the source file should show the
 code, the PC arrow should be shown and the Variables view should display all 
 the 3 variables
5. The debugger thread should show as "Running", the source file should be blank
 and the Variables view should be blank
6. The debug session terminates then start over, the debugger stops at E32Main()
7. The debug session terminates

	DEBUG/SUSPEND/TERMINATE buttons in the Debug pane.
8. The debugger should stop at E32Main
9. NO CHECK
10. The debugger thread should show as "Running", the source file should be 
blank and the Variables view should be blank
11. The debugger thread should show as suspended, the source file should show 
the code, the PC arrow should be shown and the Variables view should display all
 the 3 variables
12. The debugger thread should show as "Running", the source file should be 
blank and the Variables view should be blank
13. The debug session terminates then start over, the debugger stops at E32Main(
14. The debug session terminates

	CONTEXTUAL MENU: STACK CRAWL
15. The debugger should stop at E32Main
16. NO CHECK
17. The debugger thread should show as "Running", the source file should be 
blank and the Variables view should be blank
18. The debugger thread should show as suspended, the source file should show 
the code, the PC arrow should be shown and the Variables view should display all
 the 3 variables
19. The debugger thread should show as "Running", the source file should be 
blank and the Variables view should be blank
20. The debug session terminates then start over, the debugger stops at E32Main(
21. The debug session terminates


--------------------------------------------------------------------------------			
											program_control and modified
*******************************************************************************/
