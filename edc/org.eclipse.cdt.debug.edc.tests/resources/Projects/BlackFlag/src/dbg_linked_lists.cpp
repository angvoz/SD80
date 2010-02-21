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
#include "dbg_linked_lists.h"

// Functions Prototypes
void dbg_linked_lists();

/*************************************************************************************************************
File:			dbg_linked_lists.cpp	
Function:		lifo_testcase()

Classes Used:	n/a
Libs Used:		n/a
-------------------------------------------------------------------------------------------------------------			
PRECONDITIONS/ASSUMPTIONS
	
OBJECTIVE
1	Create snode with key=1 and insert to LIFO list
2	Create snode with key=5 and insert to LIFO list
3	Create snode with key=7 and insert to LIFO list
4	Create snode with key=9 and insert to LIFO list
5	Delete head of LIFO list
6	Find snode in LIFO list with key=7
7	Find a deleted snode in LIFO list.  Snode with key=9 was deleted in Objective #5.
8	Insert a new node into the list after a node given the node's key.
	
PROCEDURE
1	Step Over lifo.insert(1);
2	Step Over lifo.insert(5);
3	Step Over lifo.insert(7);
4	Step Over lifo.insert(9);
5	Step Over lifo.RemoveFromFront();
6	Step Over find_result = lifo.find(7);
7	Step Over find_result = lifo.find(9);
8	Step Over lifo.find_insert(5,99);
9	Step Over find_result = lifo.find(99);

-------------------------------------------------------------------------------------------------------------			
CHECKS
1	lifo * head has key = 1
2	lifo * head has key = 5
3	lifo * head has key = 7
4	lifo * head has key = 9
5	lifo * head has key = 7
6	find_result = 1
7	find_result = 0
8	lifo is now 7-5-99-1
9	find_result = 3

-------------------------------------------------------------------------------------------------------------			
*************************************************************************************************************/

// LIFO testcase
void lifo_testcase()
{
	int find_result = 0;

	list lifo;					// lifo_list variable
	lifo.insert(1);				// LIFO: 1
	lifo.insert(5);				// LIFO: 5-1
	lifo.insert(7);				// LIFO: 7-5-1
	lifo.insert(9);				// LIFO: 9-7-5-1
	lifo.removeFromFront();		// Delete head of list, LIFO: 7-5-1
	find_result = lifo.find(7);	// Find node with key=7
	find_result = lifo.find(9);	// Find node with key=9 (this was deleted)
	lifo.find_insert(5,99);		// Insert node(99) after node(5) LIFO: 7-5-99-1
	find_result = lifo.find(99);// Find node(99), find_result should be 3
}

/*************************************************************************************************************
File:			dbg_linked_lists.cpp	
Function:		fifo_testcase()

Classes Used:	n/a
Libs Used:		n/a
-------------------------------------------------------------------------------------------------------------			
PRECONDITIONS/ASSUMPTIONS
	
OBJECTIVE
1	Create snode with key=1 and insert to FIFO list
2	Create snode with key=5 and insert to FIFO list
3	Create snode with key=7 and insert to FIFO list
4	Create snode with key=9 and insert to FIFO list
5	Delete last snode (ie key=1) of FIFO list
6	Find a deleted snode in FIFO list.  Snode with key=1 was deleted in Objective #5.
	
PROCEDURE
1	Step Over fifo.insert(1);
2	Step Over fifo.insert(5);
3	Step Over fifo.insert(7);
4	Step Over fifo.insert(9);
5	Step Over fifo.removeFromBack();
6	Step Over find_result = fifo.find(1);

-------------------------------------------------------------------------------------------------------------			
CHECKS
1	sf_list * head has key = 1
2	sf_list * head has key = 5
3	sf_list * head has key = 7
4	sf_list * head has key = 9
5	sf_list * head has key = 9
6	find_result = 0
-------------------------------------------------------------------------------------------------------------			
*************************************************************************************************************/

// FIFO Testcase
void fifo_testcase()
{
	int find_result = 0;

	list fifo;  // fifo_list variable
	fifo.insert(1);				// FIFO: 1
	fifo.insert(5);				// FIFO: 5-1
	fifo.insert(7);				// FIFO: 7-5-1
	fifo.insert(9);				// FIFO: 9-7-5-1
	fifo.removeFromBack();		// FIFO: 9-7-5
	find_result = fifo.find(1); // Find node(1), find_result should be 0
}

/*************************************************************************************************************
File:			dbg_linked_lists.cpp	
Function:		dlist_testcase()

Classes Used:	n/a
Libs Used:		n/a
-------------------------------------------------------------------------------------------------------------			
PRECONDITIONS/ASSUMPTIONS
	
OBJECTIVE
1	Create dnode with key=1 and insert to double linked list
2	Create dnode with key=5 and insert to double linked list
3	Create dnode with key=7 and insert to double linked list
4	Create dnode with key=9 and insert to double linked list
5	Delete head dnode of double linked list
6	Find dnode in double linked list with key=7
7	Delete head dnode of double linked list
8	Find a deleted dnode in double linked list.  dnode with key=7 was deleted in Objective #7.
	
PROCEDURE
1	Step Over d_list.dlist_insert(1);
2	Step Over d_list.dlist_insert(5);
3	Step Over d_list.dlist_insert(7);
4	Step Over d_list.dlist_insert(9);
5	Step Over d_list.dlist_delete();
6	Step Over find_result = d_list.dlist_find(7);
7	Step Over d_list.dlist_delete();
8	Step Over find_result = d_list.dlist_find(7);

-------------------------------------------------------------------------------------------------------------			
CHECKS
1	d_list * head_ptr has key = 1 AND forward_link = NULL
2	d_list * head_ptr has key = 5 AND *head_ptr->back_link->forward_link = *head_ptr
3	d_list * head_ptr has key = 7 AND *head_ptr->back_link->forward_link = *head_ptr
4	d_list * head_ptr has key = 9 AND *head_ptr->back_link->forward_link = *head_ptr
5	d_list * head_ptr has key = 7
6	find_result = 1
7	d_list *head_ptr has key = 5
8	find_result = 0
-------------------------------------------------------------------------------------------------------------			
*************************************************************************************************************/

// Double linked list Testcase
void dlist_testcase()
{
	int find_result = 0;

	dlist d_list; 				// circular double linked list variable

	d_list.insert(1);			// d_list: 1
	d_list.insert(5);			// d_list: 5-1
	d_list.insert(7);			// d_list: 7-5-1
	d_list.insert(9);			// d_list: 9-7-5-1

	d_list.dlist_delete();		// Delete head of list, d_list: 7-5-1

	find_result = d_list.find(7);

	d_list.dlist_delete();		// Delete head of list, d_list: 5-1

	find_result = d_list.find(7);
}

// Main function that calls each of the lifo, fifo, dlist testcases.
void dbg_linked_lists()
{
	lifo_testcase();
	fifo_testcase();
	dlist_testcase();
}
