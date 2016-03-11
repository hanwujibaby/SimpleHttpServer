package com.wayleynam.http;

import com.wayleynam.core.HttpServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Objects;

/**
 * Created by wei4liverpool on 9/11/15. 连接建立的时候需要她;
 */
public class SocketAcceptorHandler implements CompletionHandler<AsynchronousSocketChannel, Object> {

  private static Log log = LogFactory.getLog(SocketReadHanler.class);
  private HttpServer httpServer;

  public SocketAcceptorHandler(HttpServer httpServer) {
    this.httpServer = httpServer;
  }

  @Override
  public void completed(AsynchronousSocketChannel result, Object attachment) {
    log.info("connection accepted");

    this.httpServer.accept();
  }

  @Override
  public void failed(Throwable exc, Object attachment) {

  }
}
