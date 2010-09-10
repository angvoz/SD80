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
class DeepCopy
{
 public:
	DeepCopy(const char * pN);
  	DeepCopy(DeepCopy& p);
  	void SetString(const char * pN);
 	~DeepCopy();
 private:
	char * pName;
};

//DeepCopy constructor
DeepCopy::DeepCopy(const char * pN)
{
	pName = new char[12];
	for (int i=0; i<12; i++)
		pName[i] = pN[i];
	return;
}

//DeepCopy copy constructor allocates a new heap block from the heap
DeepCopy::DeepCopy(DeepCopy& p)
{
	pName = new char[12];
	for (int i=0; i<12; i++)
		pName[i] = p.pName[i];
	return;
} //copy constructor

void DeepCopy::SetString(const char * pN)
{
	for (int i=0; i<12; i++)
		pName[i] = pN[i];
	return;
} 

//destructor that deallocates the heap memory reserved for the string
DeepCopy::~DeepCopy() 
{
   delete [] pName;
   pName = '\0';   
   return;
} 

