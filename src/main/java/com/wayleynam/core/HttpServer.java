package com.wayleynam.core;

import com.wayleynam.http.SocketReadHanler;
import com.wayleynam.utils.PropertisUtil;

import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by wei4liverpool on 9/10/15. http server的具体实现
 */
public class HttpServer {

  private ExecutorService channelGroup;
  /**
   * http的socket
   */
  private SocketReadHanler socketReadHanler;

  private volatile boolean inited;


  void init() {
    channelGroup =
        Executors.newFixedThreadPool(PropertisUtil.getInteger("server.socket.threadNum"));
    socketReadHanler=new SocketReadHanler();

  }

  public static void main(String[] args) {
    System.out.println(PropertisUtil.getInstance().getProperty("server.socket.port"));
  }

}
