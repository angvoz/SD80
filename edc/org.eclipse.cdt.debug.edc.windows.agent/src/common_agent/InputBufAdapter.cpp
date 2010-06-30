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
#include "InputBufAdapter.h"


std::map<InputBuf*, TCFInputBufAdpter*> TCFInputBufAdpter::adapterMap;

TCFInputBufAdpter* TCFInputBufAdpter::findInputStream(InputBuf* ibuf) {
    std::map<InputBuf*, TCFInputBufAdpter*>::iterator iter = adapterMap.find(ibuf);
    if (iter != adapterMap.end()) {
        return adapterMap[ibuf];
    } else {
        // should not get here
        check_error(ERR_CHANNEL_CLOSED);
        exit(1);
        return 0; // quell warning
    }
}

TCFInputBufAdpter::TCFInputBufAdpter(InputBuf* ibuf) {
theBuf = ibuf;
//theBuf->cur = theBuf->end = 0;
theBuf->post_read = TCFInputBufAdpter::post_read_impl;
theBuf->wait_read = TCFInputBufAdpter::wait_read_impl;
theBuf->trigger_message = TCFInputBufAdpter::trigger_message_impl;
        
    adapterMap[theBuf] = this;
}

TCFInputBufAdpter::~TCFInputBufAdpter() {
    adapterMap.erase(theBuf);
}

void  TCFInputBufAdpter::post_read_impl(InputBuf * ibuf,unsigned char *buf, int size) {
      TCFInputBufAdpter* tcfInputBuf = findInputStream(ibuf);
      tcfInputBuf->post_read(ibuf,buf,size);
}
void TCFInputBufAdpter::wait_read_impl(InputBuf * ibuf) {
     TCFInputBufAdpter* tcfInputBuf = findInputStream(ibuf);
     tcfInputBuf->wait_read(ibuf);
}

void TCFInputBufAdpter::trigger_message_impl(InputBuf * ibuf) {
     TCFInputBufAdpter* tcfInputBuf = findInputStream(ibuf);
     tcfInputBuf->trigger_message(ibuf);
}
