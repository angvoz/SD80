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

class t_node
{
public:
	
	// CONSTRUCTOR
	t_node( const int init_key,
			t_node* init_left  = '\0', 
            t_node* init_right = '\0' );

	// MEMBER FUNCTIONS
	int data();
	t_node*& left();
	t_node*& right();
	void set_left( t_node* new_left);
	void set_right(t_node* new_right);
private:
	int key;
	t_node* left_field;
	t_node* right_field;
};
	
	
// 	CONSTRUCTOR
t_node::t_node( const int init_key, t_node* init_left, t_node* init_right )
{
	key = init_key;
	left_field = init_left;
  	right_field = init_right;	
};

// MEMBER FUNCTIONS
int t_node::data()
{
	return key;
}

t_node*& t_node::left() 
// return pointer to left node
{
	return left_field;
}

t_node*& t_node::right()
// return pointer to right node
{
	return right_field;
}

void t_node::set_left( t_node* new_left )
// set left node
{
	left_field = new_left;
}

void t_node::set_right( t_node* new_right )
// set right node
{
	right_field = new_right;
}





