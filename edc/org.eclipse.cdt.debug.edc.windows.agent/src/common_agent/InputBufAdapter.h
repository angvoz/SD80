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

#ifndef INPUTBUFADAPTER_H_
#define INPUTBUFADAPTER_H_
#include "TCFHeaders.h"
#include "inputbuf.h"
#include <string>
#include <map>

class TCFInputBufAdpter
    {
public:
    TCFInputBufAdpter(InputBuf* ibuf);
    virtual ~TCFInputBufAdpter();
    
    InputBuf* getInputBuf() { return theBuf; }

    virtual void post_read(InputBuf *, unsigned char *, int) = 0;
    virtual void wait_read(InputBuf *) = 0;
    virtual void trigger_message(InputBuf *) = 0;
    
private:
    
    InputBuf* theBuf;
    static std::map<InputBuf*, TCFInputBufAdpter*> adapterMap;
    static TCFInputBufAdpter* findInputStream(InputBuf* stream);
    
    static void post_read_impl(InputBuf *, unsigned char *, int);
    static void wait_read_impl(InputBuf *);
    static void trigger_message_impl(InputBuf *);
    
    };

#endif /* INPUTBUFADAPTER_H_ */
