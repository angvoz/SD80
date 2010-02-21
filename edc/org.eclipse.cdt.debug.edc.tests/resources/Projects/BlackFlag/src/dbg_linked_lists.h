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

/* Define a simple node class */
class snode {
public:
	int key;
	snode* link;
	snode(int);
};

snode::snode(int k)
{
	key = k;
	link = '\0';
}

/* Define List Class */
class list {
public:
	snode *head;
	list();
	~list();
	void insert(int);
	int find(int);
	void removeFromFront(void);
	void removeFromBack(void);
	void find_insert(int, int);
};

list::list()
{
	head = '\0';
}

list::~list()
{
	while (head)
		removeFromFront();
}

/* Creates new snode, then adds it to head of current list */
void list::insert(int k)
{
	snode *aux = new snode(k);
	aux->link = head;
	head = aux;
}

/* Deletes the head of the list */
void list::removeFromFront(void)
{
	snode *aux = head;
	head = head->link;
	delete aux;
}

/* Deletes last node in list */
void list::removeFromBack(void)
{
	snode *aux, *aux1;
	aux = head;
	if (head->link)
	
	// Search list untill a '\0' pointer is reached, that will be the last node.  Delete it
	while (aux->link)
		{
		aux1=aux->link;
		if (aux1->link)
			aux = aux->link;
			else
			{
			aux->link = '\0';
			delete aux1;
			}
		}
	else
	head = '\0';
}

/* Finds snode with key value = k, returns int found.  found represents the # of the 
node with matching key with head snode being found = 1.  0 is returned if key==k is not found */
int list::find(int k)
{
	int found=0;
	for (snode *aux=head; aux!='\0'; aux=aux->link)
	{
		found++;
		if (aux->key==k) return found;
	}
	
	return 0;
}

/* Find snode(k) and adds snode(key) after it while keeping the rest of the list intact*/
void list::find_insert(int k, int key)
{
	int i=1;
	int found;
	found = find(k);			// Finds the position of snode(k)
	
	snode *insert_node;
	insert_node = new snode(key);	// Create new snode(key)
	
	snode *aux1, *aux2;

	aux1 = head;

	if (found != 0)
	{
		while (i != found)
			{ 
			aux1 = aux1->link;
			i++;
			}
	
		aux2 = aux1->link;			
		aux1->link = insert_node;
		insert_node->link = aux2;
	}
}
		
/* Defines a double linked node class */
class dnode {
public :
	int key;
	dnode *forward_link;
	dnode *back_link;
	dnode(int);
	~dnode();
		
};

dnode :: dnode(int k)
{
	key = k;
	forward_link = '\0';
	back_link = '\0';
}

dnode :: ~dnode()
{
	key = 0;
	forward_link = '\0';
	back_link = '\0';
}

/* Define dlist Class */
class dlist {
public:
	dnode *head_ptr;
//	dnode *current_ptr;
	dlist();
	~dlist();
	void insert(int);
	void dlist_delete(void);
	int find(int);	
};

dlist::dlist()
{
	head_ptr = '\0';
//	current_ptr ='\0';
}

dlist::~dlist()
{
	while (head_ptr)
		dlist_delete();

}

/* Creates new dnode and adds it to head of list */
void dlist::insert(int k)
{
	dnode *insert_dnode;
	insert_dnode = new dnode(k);
	if (head_ptr == '\0')
		head_ptr = insert_dnode;
	else
		{
		insert_dnode->back_link = head_ptr;
		head_ptr->forward_link = insert_dnode;
		head_ptr = insert_dnode;
		}
}

/* Deletes head of list */
void dlist::dlist_delete()
{
	dnode *delete_dnode;
	
	if (head_ptr != '\0')
		{
		delete_dnode = head_ptr;
		head_ptr = head_ptr->back_link;
		delete delete_dnode;
		}
}

/* Finds dnode with key = k */
int dlist::find(int k)
{
	int found = 1;
	dnode *search_node;
	search_node = head_ptr;
	while (search_node)
		{
			if (search_node->key == k)
				return found;
			else
				{
				search_node = search_node->back_link;
				found++;
				}
		}
	return 0;
}
