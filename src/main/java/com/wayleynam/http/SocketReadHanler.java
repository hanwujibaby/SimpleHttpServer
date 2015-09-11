package com.wayleynam.http;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.nio.channels.CompletionHandler;

/**
 * Created by wei4liverpool on 9/10/15.
 * client 创建连接的时候, 该completed方法会被调用
 */
public class SocketReadHanler implements CompletionHandler<Integer,Void> {
    private static Log log= LogFactory.getLog(SocketReadHanler.class);

    @Override
    public void completed(Integer result, Void attachment) {
        log.info("get connected!");
    }

    @Override
    public void failed(Throwable exc, Void attachment) {

    }
}
