/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Nokia - Initial API and implementation. Feb 26, 2010
 *******************************************************************************/


#ifndef PROPERTYVALUE_H_
#define PROPERTYVALUE_H_

#include <map>
#include <string>
#include "TCFOutputStream.h"

enum PropertyType {
	PVT_UNKNOWN,
	PVT_INT,
	PVT_ULONG_INT,
	PVT_BOOL,
	PVT_STRING
};

class PropertyValue {
public:
	PropertyValue();

	PropertyValue(PropertyValue &src);

	PropertyValue(int x);
	
	PropertyValue(unsigned long int x);

	PropertyValue(bool x);

	PropertyValue(const char *x);

	PropertyValue(const std::string& x);

	~PropertyValue();

	PropertyType getType();

	int getIntValue();
	unsigned long int getUnsignedLongIntValue();

	bool getBoolValue();

	const std::string& getStringValue();


	void writeToTCFChannel(TCFOutputStream& tcf_stream);

private:
	PropertyType type;

	union {
		int v_int;		
		unsigned long int v_ulong_int;
		bool v_bool;
	} v;

	// "std::string" is not allowed to be in a union.
	// But I still prefer std::string to char* for easier memory management.
	std::string v_string;
};

typedef std::map<std::string, PropertyValue*>	Properties;

#endif /* PROPERTYVALUE_H_ */
