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

#ifndef TCFCHANNEL_H_
#define TCFCHANNEL_H_

#include "TCFHeaders.h"
#include "TCFOutputStream.h"
#include "TCFInputStream.h"
#include "InputBufAdapter.h"

#include <string>
#include <map>

#define BUF_SIZE 0x1000

class TCFChannel: public TCFOutputStream, public TCFInputStream {
public:
	TCFChannel(Channel * c);
	~TCFChannel(void);

};

class TCFChannelAdapter;

/**
 * Callbacks for C++ TCFChannelAdapter.  This provides default
 * implementations; you may override with your own handlers.
 */
struct TCFChannelCallbackAdapter 
{
	/* Called when channel is ready for transmit */
    virtual void connecting(TCFChannelAdapter* channel);      
    /* Called when channel negotiation is complete */
    virtual void connected(TCFChannelAdapter* channel);       
    /* Called when messages has been received */
    virtual void receive(TCFChannelAdapter* channel);         
    /* Called when channel is disconnected */
    virtual void disconnected(TCFChannelAdapter* channel);    
};


class TCPInputStreamAdapter;
class TCPOutputStreamAdapter;
class TCFInputBufAdpter;

/**
 * C++ wrapper for Channel. 
 */
class TCFChannelAdapter 
{
public:
	TCFChannelAdapter();
	virtual ~TCFChannelAdapter();
	
	/** Call to initialize the adapter after creation. */
	void init();
	
	Channel* getChannel() { return theChannel_; }

	/** 
	 * Provide the callback adapter for channel status messages.
	 * Pass NULL to use default callbacks.
	 */
	void setCallbacks(TCFChannelCallbackAdapter* cb);
	
	/** Start communication */
	virtual void start_comm() = 0;      
	/** Check for pending messages */
	virtual void check_pending() = 0;   
	/** Return number of pending messages */
	virtual int message_count() = 0;    
	/** Lock channel from deletion */
	virtual void lock() = 0;            
	/** Unlock channel */
	virtual void unlock() = 0;          
	/** Return true if channel is closed */
	virtual int is_closed() = 0;
	/** Close channel */
	virtual void close(int) = 0;      

protected:
	TCFInputStreamAdapter* inputStream_;
	TCFOutputStreamAdapter* outputStream_;
	
    unsigned char obuf[BUF_SIZE];
	
private:
	Channel* theChannel_;
	TCFChannelCallbackAdapter* cb_;
	TCFChannelCallbackAdapter defaultCb_;
	
	/**
	 * Create the input stream and output stream adapters
	 * (inputStream_, outputStream_) for the streams using
	 * these TCF streams.
	 */
	virtual void createStreamAdapters(InputStream* input, OutputStream* output) = 0;

	static std::map<Channel*, TCFChannelAdapter*> adapterMap;
	static TCFChannelAdapter* findChannel(Channel* stream);
	
	static void start_comm_impl(Channel* channel); /* Start communication */
	static void check_pending_impl(Channel *);   /* Check for pending messages */
    static int message_count_impl(Channel *);    /* Return number of pending messages */
    static void lock_impl(Channel *);            /* Lock channel from deletion */
    static void unlock_impl(Channel *);          /* Unlock channel */
    static int is_closed_impl(Channel *);        /* Return true if channel is closed */
    static void close_impl(Channel *, int);      /* Close channel */

    static void connecting_cb_impl(Channel*);
    static void connected_cb_impl(Channel*);
    static void receive_cb_impl(Channel*);
    static void disconnected_cb_impl(Channel*);
};

#endif
