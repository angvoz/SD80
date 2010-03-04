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

#include <string>

#ifdef __cplusplus
extern "C" {
#endif

#include "streams.h"

#ifdef __cplusplus
}
#endif

class TCFOutputStream {
public:
	TCFOutputStream(OutputStream* out);
	~TCFOutputStream(void);

	void writeReplyHeader(char* token);

	void writeError(int error);

	void writeZero();

	void writeComplete();

	void writeBinaryData(char* buffer, int bufferSize);

	void writeCharacter(char character);

	void writeBoolean(bool value);

	void writeLong(long value);

	void writeString(std::string str);

	void writeStringZ(std::string str);

	void flush();

private:

	OutputStream* out_; /* Output stream */

};
