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
#pragma once
#include "TCFHeaders.h"
#include <string>

class TCFInputStream {
public:
	TCFInputStream(InputStream * inp);
	~TCFInputStream(void);

	std::string readString();

	unsigned long readULong();

	long readLong();

	void readZero();

	void readComplete();

	char* readBinaryData(int dataSize);

private:

	InputStream* inp_; /* Input stream */

};
