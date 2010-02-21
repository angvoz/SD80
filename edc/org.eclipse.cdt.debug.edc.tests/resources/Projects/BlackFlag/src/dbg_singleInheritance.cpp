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
#include "dbg_singleInheritance.h"

void dbg_singleInheritance();

void dbg_singleInheritance()
{	   
	Person Jane;
	Employee Frank(5000);
	Employee Jessie(15,73,123,4000);
	
	//step into method
	volatile double _salaryFrank = Frank.Getsalary();	// 5000	
	volatile int	_ageJessie   = Jessie.Getage();		// 15
	
	//instantiate an object and initialize it with another object.
	Employee John = Frank;
	Frank.Setsalary(2000);
	
	//make sure changing Frank's salary does not change John's
	volatile double _salaryJohn = John.Getsalary(); 		// 5000
}

/*************************************************************************************************************
File:			dbg_singleInheritance.cpp	
Function:		singleInheritance()

Classes Used:	Person, Employee
Libs Used:		n/a
-------------------------------------------------------------------------------------------------------------			
PRECONDITIONS/ASSUMPTIONS
	
OBJECTIVE
	
Basic inheritance allows the derived class to inherit the member variables and methods 
of the base class.
	   	
PROCEDURE
		
1	Step over the instantiation of the base class, Person.
2	Step into the first instantiation of the derived class, Employee.
3	Step over the base constructor from the overloaded constructor of the derived class. 
	Expand the This pointer.
4  	Step out of the constructor of the derived class, Employee.
5	Step into the second instantiation of the derived class, Employee.
6	Step over the base constructor from the overloaded constructor of the derived class. 
	Expand the This pointer.
7 	Step out of the constructor of the derived class, Employee.
8   Step over the calls to the Employee methods of the Frank and Jessie objects.
9 	Step over the third instantiation of the derived class, Employee that uses another object 
	to initialize it. Expand the John object and the pointer of the base class, Person.
10  Step over call to the setSalary method of the Frank object. Expand the Frank and John objects.
11	Step over _salaryJohn = John.Getsalary(); to get John's salary.
-------------------------------------------------------------------------------------------------------------			
CHECKS

1	Each member variable of the Jane object gets initialized.
2	The PC cursor should be pointing to the overloaded constructor of the derived class, 
	Employee,that uses one parameter.
3	The member variables of the Person class are initialized using the default values of 
	the base's constructor.
4   The member variable of the derived class for the object Frank is initialized using 
	the derived class' method setSalary. The member variables of the inherited Person class 
	are displayed correctly.
5	The PC cursor should be pointing to the overloaded constructor of the derived class, 
	Employee,that uses three parameters.
6	The member variables of the Person class are initialized using the values passed to it 
	frombthe derived class Employee's overloaded constructor.
7	The member variable of the derived class for the object Jessie is initialized using the 
	derived class'method setSalary. The member variables of the inherited Person class are 
	displayed correctly.
8   Make sure the class methods return the correct value to the local variables.
9	The member variables of the John object are the same as the member variables of the Frank object.
10	Only the _salary variable of the Frank object should have been modified.
11	Make sure _salaryJohn equal 5000 (i.e, changing Frank's salary did not change John's). 
-------------------------------------------------------------------------------------------------------------			
*************************************************************************************************************/	
