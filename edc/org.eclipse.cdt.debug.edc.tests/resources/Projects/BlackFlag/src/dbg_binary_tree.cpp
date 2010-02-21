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

#include "dbg_binary_tree.h"
#include "dbg_prototypes.h"

void dbg_binary_tree()
{	
	int counter;
	int boolExistInTree;
	const int ArrayMax = 20;
	binary_tree root;

	// Array of binary tree nodes to be inserted in the tree 
	int intArray[ArrayMax] = {20,15,17,8,5,45,37,25,33,21,23,55,65,60,1,6,7,36,30,99};

	// For loop to insert binary tree nodes to binary tree 
	for (counter=0; counter<ArrayMax; counter++)
	{
		root.InsertToTree(intArray[counter]);
	}	// CHECK 1-10 is performed AFTER this loop 
	
	// Verify nodes that exist in the binary tree 
	boolExistInTree = root.ExistInTree(7);	// boolExistInTree = 1, CHECK 11 performed AFTER this line 
	boolExistInTree = root.ExistInTree(17);	// boolExistInTree = 1, CHECK 12 performed AFTER this line 
	boolExistInTree = root.ExistInTree(21);	// boolExistInTree = 1, CHECK 13 performed AFTER this line 
	boolExistInTree = root.ExistInTree(45);	// boolExistInTree = 1, CHECK 14 performed AFTER this line 
	boolExistInTree = root.ExistInTree(69);	// boolExistInTree = 0, CHECK 15 performed AFTER this line 
	
	// Delete Some nodes from binary tree 
	root.DeleteFromTree(55);				// CHECK 16-19 performed AFTER this line 
	root.DeleteFromTree(8);					// CHECK 20-23 performed AFTER this line 
	
	root.DeleteFromTree(30);
	boolExistInTree = root.ExistInTree(30);	// boolExistInTree = 0, CHECK 24 performed AFTER this line 
	
	root.DeleteFromTree(21);
	boolExistInTree = root.ExistInTree(21);	// boolExistInTree = 0, CHECK 25 performed AFTER this line 
	
	// Verify that sub-tree of deleted node is re-inserted correctly in binary tree 
	boolExistInTree = root.ExistInTree(23);	// boolExistInTree = 1, CHECK 26 performed AFTER this line 
	root.DeleteFromTree(5);
	boolExistInTree = root.ExistInTree(5);	// boolExistInTree = 1, CHECK 30 performed AFTER this line 
}
