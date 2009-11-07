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
#include "TCFInputStream.h"

TCFInputStream::TCFInputStream(InputStream * inp) {
	inp_ = inp;
}

TCFInputStream::~TCFInputStream(void) {
}

static int readHex(InputStream * inp) {
	int ch = read_stream(inp);
	if (ch >= '0' && ch <= '9')
		return ch - '0';
	if (ch >= 'A' && ch <= 'F')
		return ch - 'A' + 10;
	if (ch >= 'a' && ch <= 'f')
		return ch - 'a' + 10;
	exception(ERR_JSON_SYNTAX);
	return 0;
}

static int readHexChar(InputStream * inp) {
	int n = readHex(inp) << 12;
	n |= readHex(inp) << 8;
	n |= readHex(inp) << 4;
	n |= readHex(inp);
	return n;
}

static int read_esc_char(InputStream * inp) {
	int ch = read_stream(inp);
	switch (ch) {
	case '"':
		break;
	case '\\':
		break;
	case '/':
		break;
	case 'b':
		ch = '\b';
		break;
	case 'f':
		ch = '\f';
		break;
	case 'n':
		ch = '\n';
		break;
	case 'r':
		ch = '\r';
		break;
	case 't':
		ch = '\t';
		break;
	case 'u':
		ch = readHexChar(inp);
		break;
	default:
		exception(ERR_JSON_SYNTAX);
	}
	return ch;
}

std::string TCFInputStream::readString() {
	std::string result;

	InputStream* inp = inp_;
	int ch = read_stream(inp);
	if (ch == 'n') {
		if (read_stream(inp) != 'u')
			exception(ERR_JSON_SYNTAX);
		if (read_stream(inp) != 'l')
			exception(ERR_JSON_SYNTAX);
		if (read_stream(inp) != 'l')
			exception(ERR_JSON_SYNTAX);
	} else {
		if (ch != '"')
			exception(ERR_JSON_SYNTAX);
		for (;;) {
			ch = read_stream(inp);
			if (ch == '"')
				break;
			if (ch == '\\')
				ch = read_esc_char(inp);
			result += (char) ch;
		}
	}

	return result;
}

unsigned long TCFInputStream::readULong() {
	return json_read_ulong(inp_);
}

long TCFInputStream::readLong() {
	return json_read_long(inp_);
}

void TCFInputStream::readZero() {
	if (inp_->read(inp_) != 0)
		exception(ERR_JSON_SYNTAX);
}

void TCFInputStream::readComplete() {
	if (inp_->read(inp_) != MARKER_EOM)
		exception(ERR_JSON_SYNTAX);
}

char* TCFInputStream::readBinaryData(int dataSize) {
	char* memBuffer = new char[4 * ((dataSize + 2) / 3)];

	JsonReadBinaryState state;
	json_read_binary_start(&state, inp_);
	json_read_binary_data(&state, memBuffer, dataSize);
	json_read_binary_end(&state);

	return memBuffer;
}
