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
#include "dbg_deepCopy.h"

void dbg_deepCopy();

void dbg_deepCopy()
{			
  	DeepCopy p1("Hello World\0");
  	
 	DeepCopy p2 = p1;    					// invoke the copy constructor...  	
 	
 	// now modify the first objects data member.  Shouldn't change object two's data
 	// member.  BE CAREFUL here because the last line of code has the call to the destructor.
 	p1.SetString("Hello Nokia");		        
}
