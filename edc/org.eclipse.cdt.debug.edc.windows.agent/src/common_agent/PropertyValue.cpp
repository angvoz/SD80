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

#include <assert.h>
#include "PropertyValue.h"

PropertyValue::PropertyValue() {}

PropertyValue::PropertyValue(PropertyValue &src) {
	type = src.type;
	if (type == PVT_INT)
		v.v_int = src.v.v_int;
	else if (type == PVT_ULONG_INT)
		v.v_int = src.v.v_ulong_int;
	else if (type == PVT_BOOL)
		v.v_bool = src.v.v_bool;
	else if (type == PVT_STRING)
		v_string = src.v_string;	// copy !
}

PropertyValue::PropertyValue(int x) {
	type = PVT_INT;
	v.v_int = x;
}
PropertyValue::PropertyValue(unsigned long int x) {
	type = PVT_ULONG_INT;
	v.v_ulong_int = x;
}

PropertyValue::PropertyValue(bool x) {
	type = PVT_BOOL;
	v.v_bool = x;
}

PropertyValue::PropertyValue(const char *x) {
	type = PVT_STRING;
	v_string = x;
}

PropertyValue::PropertyValue(const std::string& x) {
	type = PVT_STRING;
	v_string = x;
}

PropertyValue::~PropertyValue() {
}


PropertyType PropertyValue::getType() {
	return type;
}

int PropertyValue::getIntValue() {
	return v.v_int;
}

unsigned long int PropertyValue::getUnsignedLongIntValue() {
	return v.v_ulong_int;
}

bool PropertyValue::getBoolValue() {
	return v.v_bool;
}

const std::string& PropertyValue::getStringValue() {
	return v_string;
}

void PropertyValue::writeToTCFChannel(TCFOutputStream& tcf_stream) {
	switch (type) {
	case PVT_INT:
		tcf_stream.writeLong(v.v_int); break;
	case PVT_ULONG_INT:
		tcf_stream.writeULong(v.v_ulong_int); break;
	case PVT_BOOL:
		tcf_stream.writeBoolean(v.v_bool); break;
	case PVT_STRING:
		tcf_stream.writeString(v_string); break;
	default:
		assert(false);
		break;
	}
}
