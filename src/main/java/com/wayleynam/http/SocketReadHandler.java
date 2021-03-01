package com.wayleynam.http;

import com.wayleynam.core.SocketSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.nio.channels.CompletionHandler;

/**
 * AIO上监听回调，有回调的话执行回调
 */

public class SocketReadHandler implements CompletionHandler<Integer, SocketSession> {

    private static Log log = LogFactory.getLog(SocketReadHandler.class);

    @Override
    public void completed(Integer result, SocketSession socketSession) {

        if (result == -1) {

        }else{

        }

    }

    @Override
    public void failed(Throwable exc, SocketSession attachment) {

    }
}
