/*
* Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies).
* All rights reserved.
* This component and the accompanying materials are made available
* under the terms of the License "Eclipse Public License v1.0"
* which accompanies this distribution, and is available
* at the URL "http://www.eclipse.org/legal/epl-v10.html".
*
* Initial Contributors:
* Nokia Corporation - initial contribution.

* Description : test case for inline function related issues.
*/
extern int basic_inline();
extern int inline_from_header_1();
extern int inline_from_header_2();

int main() {
	basic_inline();
	// set breakpoint on this line, it should end at the line above 
	// set bp on this line, it should end at code line below
	// set bp on this line, it should end at next line
	inline_from_header_1();
	
	inline_from_header_2();
	
	return 0;
}
