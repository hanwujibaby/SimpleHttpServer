package com.wayleynam.core;

import com.wayleynam.http.SocketReadHanler;

import java.nio.ByteBuffer;

/**
 * Created by wei4liverpool on 9/14/15. socket 在channel之间的流转
 */
public class SocketSession {

  private ByteBuffer byteBuffer;

  private SocketReadHanler socketReadHanler;

  public SocketSession(HttpServer httpServer) {
    byteBuffer = httpServer.borrowObject();
  }


  public void read(){
    byteBuffer.clear();
    //cketReadHanler=

  }


}
