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
#include "TCFOutputStream.h"

TCFOutputStream::TCFOutputStream(OutputStream* out) {
	out_ = out;
}

TCFOutputStream::~TCFOutputStream(void) {
}

void TCFOutputStream::writeZero() {
	out_->write(out_, 0);
}

void TCFOutputStream::writeComplete() {
	out_->write(out_, MARKER_EOM);
}

void TCFOutputStream::writeReplyHeader(char* token) {
	write_stringz(out_, "R");
	write_stringz(out_, token);
}

void TCFOutputStream::writeString(std::string str) {
	json_write_string(out_, str.c_str());
}

void TCFOutputStream::writeError(int error) {
	write_errno(out_, error);
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

void TCFOutputStream::writeBoolean(bool value) {
	json_write_boolean(out_, value);
}

void TCFOutputStream::writeCharacter(char character) {
	out_->write(out_, character);
}

void TCFOutputStream::writeStringZ(std::string str) {
	write_string(out_, str.c_str());
	writeZero();
}

void TCFOutputStream::flush() {
	out_->flush(out_);
}
