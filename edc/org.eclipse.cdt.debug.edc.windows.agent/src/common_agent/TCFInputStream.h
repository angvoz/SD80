/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
#ifndef TCFINPUTSTREAM_H
#define TCFINPUTSTREAM_H

#include "TCFHeaders.h"
#include <string>
#include <map>

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


/**
 * C++ wrapper for InputStream. 
 */
class TCFInputStreamAdapter {
public:
	/**
	 * Initialize, passing a pointer to an input stream to use.
	 * This is usually the address of "inp" inside a Channel.
	 */
	TCFInputStreamAdapter(InputStream* input);
	virtual ~TCFInputStreamAdapter();

	InputStream* getInputStream() { return theStream_; }
	
	virtual int read() = 0;
	virtual int peek() = 0;

private:
	InputStream* theStream_;
	
	static std::map<InputStream*, TCFInputStreamAdapter*> adapterMap;
	static TCFInputStreamAdapter* findInputStream(InputStream* stream);
	
	static int read_impl(InputStream * stream);
	static int peek_impl(InputStream * stream);

};
#endif
