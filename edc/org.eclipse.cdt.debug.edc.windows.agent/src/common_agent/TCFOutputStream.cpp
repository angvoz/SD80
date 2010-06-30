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
#include "TCFOutputStream.h"

extern "C" {
#include "errors.h"
};

TCFOutputStream::TCFOutputStream(OutputStream* out) {
	out_ = out;
}

TCFOutputStream::~TCFOutputStream() {
}

void TCFOutputStream::writeZero() {
	out_->write(out_, 0);
}

void TCFOutputStream::writeComplete() {
	out_->write(out_, MARKER_EOM);
}

void TCFOutputStream::writeReplyHeader(const char* token) {
	write_stringz(out_, "R");
	write_stringz(out_, token);
}

void TCFOutputStream::writeErrorReply(int error, int addNullObjects, const char* message, const char* serviceName) {
	writeError(error, message, serviceName);
	while (addNullObjects-- > 0)
		writeZero();	// this puts a null object in the reply
	writeComplete();
}

void TCFOutputStream::writeString(const std::string& str) {
	json_write_string(out_, str.c_str());
}

void TCFOutputStream::writeError(int error, const char* message, const char* serviceName) {
    if (!message) {
        write_errno(out_, error);
    } else {
        // write custom error report
        write_stream(out_, '{');

        if (serviceName) {
            json_write_string(out_, "Service");
            write_stream(out_, ':');
            json_write_string(out_, serviceName);
            write_stream(out_, ',');
        }

        json_write_string(out_, "Code");
        write_stream(out_, ':');
        json_write_long(out_, error);

        write_stream(out_, ',');

        std::string errMsg = message;
        if (error != ERR_OTHER && error != 0) {
			errMsg += " (";
			errMsg += errno_to_str(error);
			errMsg += ')';
        }

        json_write_string(out_, "Format");
        write_stream(out_, ':');
        json_write_string(out_, errMsg.c_str());

        write_stream(out_, '}');
        writeZero();
    }
}

void TCFOutputStream::writeBinaryData(char* buffer, int bufferSize) {
	JsonWriteBinaryState state;
	json_write_binary_start(&state, out_, bufferSize);
	json_write_binary_data(&state, buffer, bufferSize);
	json_write_binary_end(&state);
	out_->write(out_, 0);
}

void TCFOutputStream::writeLong(long value) {
	json_write_long(out_, value);
}

void TCFOutputStream::writeULong(unsigned long value) {
    json_write_ulong(out_, value); 
}

void TCFOutputStream::writeBoolean(bool value) { 
	json_write_boolean(out_, value);
}

void TCFOutputStream::writeCharacter(char character) {
	out_->write(out_, character);
}

void TCFOutputStream::writeStringZ(const std::string& str) {
	write_string(out_, str.c_str());
	writeZero();
}

void TCFOutputStream::flush() {
	out_->flush(out_);
}

std::map<OutputStream*, TCFOutputStreamAdapter*> TCFOutputStreamAdapter::adapterMap;

TCFOutputStreamAdapter* TCFOutputStreamAdapter::findOutputStream(OutputStream* stream) {
	std::map<OutputStream*, TCFOutputStreamAdapter*>::iterator iter = adapterMap.find(stream);
	if (iter != adapterMap.end()) {
	    return adapterMap[stream];
	} else {
		// should not get here
		check_error(ERR_CHANNEL_CLOSED);
		exit(1);
		return 0; // quell warning
	}
}


TCFOutputStreamAdapter::TCFOutputStreamAdapter(OutputStream* output) {
	theStream_ = output;
	theStream_->cur = theStream_->end = 0;
	theStream_->supports_zero_copy = false;
	theStream_->write = TCFOutputStreamAdapter::write_impl;
	theStream_->write_block = TCFOutputStreamAdapter::write_block_impl;
	theStream_->splice_block = TCFOutputStreamAdapter::splice_block_impl;
	theStream_->flush = TCFOutputStreamAdapter::flush_impl;

	adapterMap[theStream_] = this;
}

TCFOutputStreamAdapter::~TCFOutputStreamAdapter() {
	adapterMap.erase(theStream_);
}

void TCFOutputStreamAdapter::write_impl(OutputStream* stream, int byte) {
	TCFOutputStreamAdapter* tcfStream = findOutputStream(stream);
	tcfStream->write(byte);
}
void TCFOutputStreamAdapter::write_block_impl(OutputStream * stream, const char * bytes, size_t size) {
	TCFOutputStreamAdapter* tcfStream = findOutputStream(stream);
	tcfStream->write_block(bytes, size);
}
int TCFOutputStreamAdapter::splice_block_impl(OutputStream * stream, int fd, size_t size, off_t * offset) {
	TCFOutputStreamAdapter* tcfStream = findOutputStream(stream);
	return tcfStream->splice_block(fd, size, offset);
}
void TCFOutputStreamAdapter::flush_impl(OutputStream * stream) {
	TCFOutputStreamAdapter* tcfStream = findOutputStream(stream);
	tcfStream->flush();
}
