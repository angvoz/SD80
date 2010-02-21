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
#include "dbg_binary_tree_node.h"

class binary_tree
{
public:
	
	//CONSTRUCTORS AND DESTRUCTORS
	binary_tree();
	binary_tree(binary_tree& source);
	~binary_tree();
	
	//MODIFICATION MEMBER FUNCTIONS
	void InsertToTree(const int entry);
	bool ExistInTree (int key);
	bool DeleteFromTree(int key);
	void operator =(const binary_tree& source);
private:
	t_node* find(int key, t_node*& previous, char& destination);
	t_node* pRoot;	// Root pointer of binary search tree
};

//	NONMEMBER FUNCTIONS to manipulate binary tree nodes:				 		
void tree_clear(t_node*& pRoot);

t_node* tree_copy(t_node* pRoot);

// CONSTRUCTORS AND DESTRUCTORS
binary_tree::binary_tree()
{
	pRoot = '\0';
}
			
binary_tree::binary_tree(binary_tree& source)
{
	pRoot = tree_copy(source.pRoot);	
}
	
binary_tree::~binary_tree()
// deallocate all nodes within the tree
{
	tree_clear(pRoot);
}

//	MEMBER FUNCTIONS to manipulate binary search tree
void binary_tree::InsertToTree( int key)
// inserts a node with the given key
{
	t_node* pcurrentNode;
	t_node* pnewNode;
			
	if (pRoot) //if root exists
	{
		pcurrentNode = pRoot;
		
		while (pcurrentNode) //keep going while there's a pointer
		{	
			if (key <= pcurrentNode->data())
			{	// go down the left side
				if ( pcurrentNode->left() == '\0')
				{	
					pnewNode = new t_node(key);
					pcurrentNode->set_left(pnewNode);
					return;
				}
				else 
					pcurrentNode = pcurrentNode->left();
			}
			else if (key > pcurrentNode->data())
			{	// go down the right side
				if (pcurrentNode->right() == '\0')
				{	
					pnewNode = new t_node(key);
					pcurrentNode->set_right(pnewNode);
					return;
				}
				else 
					pcurrentNode = pcurrentNode->right();
			}				
		}
	}
	else // root doesn't exist
		pRoot = new t_node(key);
}

bool binary_tree::ExistInTree (int key)
// searches a binary tree for a node with the given key.  Returns true if the key is found
// and returns false if it is not found.
{ 
	t_node* pcurrentNode;
	
	if (pRoot) // make sure there is at least one node 
	{
		pcurrentNode = pRoot;
		while (pcurrentNode) //as long as there's a pointer
		{
			if (pcurrentNode->data() == key) 
				return true;
			// if less, go down right side
			if (pcurrentNode->data() < key) 
				pcurrentNode = pcurrentNode->right();
			// if greater, go down the left side
			else 
				pcurrentNode = pcurrentNode->left();
		}
	}
	return false;
}

t_node* binary_tree::find(int key, t_node*& previous, char& direction)
// looks for a node within a binary tree that has the same data value as the 
// given key.
{
	t_node* pcurrentNode;
	
	if (pRoot) // make sure there is at least one node
	{
		pcurrentNode = pRoot;
		previous = pcurrentNode;
		while (pcurrentNode) // as long as there's a pointer
		{
			if (pcurrentNode->data() == key)
				return pcurrentNode; // send back pointer to found object
			// if less, go down right side
			if (pcurrentNode->data() < key)
			{
				previous = pcurrentNode;
				direction = 'r';
				pcurrentNode = pcurrentNode->right();
			}
			// if greater, go down left side
			else
			{
				previous = pcurrentNode;
				direction = 'l';
				pcurrentNode = pcurrentNode->left();
			}
		}
	}
	return 0;	
}

bool binary_tree::DeleteFromTree(int key)
// deletes a node from a binary tree that has the same data as the given key.
{
	char direction;
	t_node* previous = 0; // use to save parent of found node
	t_node* theNode = find(key, previous, direction);
	if (theNode == 0)
		return false; // key not found in tree
	// if no children, just disconnect; set parent pointer to 0
	if (theNode->right() ==0 && theNode->left() == 0)
	{
		if (theNode == pRoot)
			pRoot = 0; //empty tree
		else
			if (direction == 'r')
				previous->set_right(0);
			else
				previous->set_left(0);
	}
	// right subtree but no left subtree
	else if (theNode->right() !=0 && theNode->left() == 0)
	{
		t_node* subtree = theNode->right();
		if (theNode == pRoot)
			pRoot = subtree;
		else
		{
			if (direction == 'r')
				previous->set_right(subtree);
			else
				previous->set_left(subtree);
			
		}
	}
	// left subtree but no right subtree
	else if (theNode->right() == 0 && theNode->left() != 0)
	{
		t_node* subtree = theNode->left();
		if (theNode == pRoot)
			pRoot = subtree;
		else
		{
			if (direction == 'r')
				previous->set_right(subtree);
			else
				previous->set_left(subtree);
		}
	}
	else //must have both left and right subtrees
	{
		t_node* next;
		t_node* pcurrentNode = theNode->left();
		if (pcurrentNode->right() != 0) //if there is a right subtree of left child...
		{
			next = pcurrentNode->right();
			while (next->right() != 0) // find last right child
			{
				pcurrentNode = next;
				next = pcurrentNode->right();
			}
			
			// replace deleted node with node found
			pcurrentNode->set_right(0);
			next->set_left(theNode->left());
			next->set_right(theNode->right());
			// set parent pointers
			if (theNode != pRoot)
				if (direction == 'l')
					previous->set_left(next);
				else
					previous->set_right(next);
			else pRoot = next;
		}
		// since no right subtree, replace with left child
		else
		{
			next = pcurrentNode;
			next->set_right(theNode->right());
			if (theNode != pRoot)
				if (direction == 'l')
					previous->set_left(next);
				else
					previous->set_right(next);
			else pRoot = next;	
		}	
	}
	// remove theNode object from memory
	delete theNode;
	return true;
}

void binary_tree::operator =(const binary_tree& source)
// overloaded assignment operator
{
	if (this == &source)		// Handle self assignment
		return;
			
	tree_clear(pRoot);
	pRoot = tree_copy(source.pRoot);
}

void tree_clear(t_node*& pRoot)
// deallocates all nodes within given binary tree
{	
	if (pRoot != '\0')
	{
		tree_clear( pRoot->left() );
		tree_clear( pRoot->right() );
		delete pRoot;
		pRoot = '\0';
	}
}
	
t_node* tree_copy(t_node* pRoot)
// copies all nodes to another binary tree given the root.	
{
	t_node *l_ptr;
	t_node *r_ptr;
		
	if (pRoot == '\0')
		return '\0';
	else
	{
		l_ptr = tree_copy( pRoot->left() );
		r_ptr = tree_copy( pRoot->right() );
		return
			new t_node(pRoot->data(), l_ptr, r_ptr);
	}
}
