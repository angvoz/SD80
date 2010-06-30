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
#ifndef TCFOUTPUTSTREAM_H
#define TCFOUTPUTSTREAM_H

#include <string>
#include <map>

#ifdef __cplusplus
extern "C" {
#endif

#include "mdep.h"
#include "streams.h"
#include "json.h"

#ifdef __cplusplus
}
#endif

class TCFOutputStream {
public:
	TCFOutputStream(OutputStream* out);
	~TCFOutputStream();

	void writeReplyHeader(const char* token);

    /** 
     * Write an error reply, indicating an error and optionally emitting null objects
     * in place of the contents that would be expected in a successful case, then
     * complete the reply.
     * @error if message==NULL, then a system or TCF error code
     * @addNullObjects number of null objects to emit after the error code
     * @message if not NULL, the custom format (text) for the message
     * @serviceName if not NULL, an additional specifier for the service that caused the error  
     */
    void writeErrorReply(int error, int addNullObjects = 0, const char* message = 0, const char* serviceName = 0);
	
    /** 
     * Write the given error code and optional message and service identifier. 
     * @error if message==NULL, then a system or TCF error code, otherwise a custom code
     * @message if not NULL, the custom format (text) for the message
     * @serviceName if not NULL, an additional specifier for the service that caused the error  
     */
    void writeError(int error, const char* message = 0, const char* serviceName = 0);

	void writeZero();

	void writeComplete();

	
    /** 
     * Write a complete reply, indicating success or an error, emitting N reply objects, and completing.
     * @token the token for the reply header 
     * @error if message==NULL, then a system or TCF error code
     * @addNullObjects number of null objects to emit after the error code
     * @message if not NULL, the custom format (text) for the message
     * @serviceName if not NULL, an additional specifier for the service that caused the error  
     */
    void writeCompleteReply(const char* token, int error, int addNullObjects = 0, 
    		const char* message = 0, const char* serviceName = 0)
	{
    	writeReplyHeader(token);
    	writeErrorReply(error, addNullObjects, message, serviceName);
    	writeComplete();
	}
	
	void writeBinaryData(char* buffer, int bufferSize);

	void writeCharacter(char character);

	void writeBoolean(bool value);

	void writeLong(long value);
	void writeULong(unsigned long value);

	void writeString(const std::string& str);

	void writeStringZ(const std::string& str);

	void flush();

private:

	OutputStream* out_; /* Output stream */
};

/**
 * C++ wrapper for OutputStream.  
 */
class TCFOutputStreamAdapter {
public:
	/**
	 * Initialize, passing a pointer to an input stream to use.
	 * This is usually the address of "out" inside a Channel.
	 */
	TCFOutputStreamAdapter(OutputStream* output);
	virtual ~TCFOutputStreamAdapter();
	
	OutputStream* getOutputStream() { return theStream_; }
	
	void setSupportsZeroCopy(bool supportsZeroCopy) { theStream_->supports_zero_copy = supportsZeroCopy; }
	bool supportsZeroCopy() { return theStream_->supports_zero_copy; }
	
    virtual void write(int byte) = 0;
    virtual void write_block(const char * bytes, size_t size) = 0;
    virtual int splice_block(int fd, size_t size, off_t * offset) = 0;
    virtual void flush() = 0;

private:
	OutputStream* theStream_;
    
	static std::map<OutputStream*, TCFOutputStreamAdapter*> adapterMap;
	static TCFOutputStreamAdapter* findOutputStream(OutputStream* stream);

	static void write_impl(OutputStream* output, int byte);
	static void write_block_impl(OutputStream * stream, const char * bytes, size_t size);
	static int splice_block_impl(OutputStream * stream, int fd, size_t size, off_t * offset);
	static void flush_impl(OutputStream * stream);
};
#endif
