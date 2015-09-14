package com.wayleynam.core;

import com.wayleynam.http.ProcessThreadFactory;
import com.wayleynam.http.SocketAcceptorHandler;
import com.wayleynam.http.SocketReadHanler;
import com.wayleynam.utils.PropertisUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by wei4liverpool on 9/10/15. http server的具体实现
 */
public class HttpServer {

  private ExecutorService workGroup;

  private AsynchronousChannelGroup channelGroup;
  // 服务器的socket channel
  private AsynchronousServerSocketChannel serverSocket;
  private SocketAcceptorHandler acceptorHandler;

  /**
   * http的socket
   */
  private SocketReadHanler socketReadHanler;

  private volatile boolean started;
  private volatile boolean inited;


  void init() {
    try {
      acceptorHandler = new SocketAcceptorHandler(this);
      workGroup = Executors.newFixedThreadPool(PropertisUtil.getInteger("server.socket.threadNum"),
          new ProcessThreadFactory());
      int proccessorNum = Runtime.getRuntime().availableProcessors();
      channelGroup = AsynchronousChannelGroup.withCachedThreadPool(workGroup, 1);
      serverSocket = AsynchronousServerSocketChannel.open(channelGroup);
    } catch (IOException e) {
      e.printStackTrace();
    }

    inited = true;

  }

  public void accept() {
    if (started) {
      serverSocket.accept(null, this.acceptorHandler);
    }
  }


  public void start() {
    if (inited == false)
      init();
    if (started)
      return;

    try {
      serverSocket.bind(new InetSocketAddress(PropertisUtil.getInteger("server.socket.port")), 100);
    } catch (IOException e) {
      e.printStackTrace();
    }

    started = true;

    accept();
  }


}
