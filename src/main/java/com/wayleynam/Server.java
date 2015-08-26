package com.wayleynam;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by wei4liverpool on 8/26/15.
 */
public class Server {

  private static Charset charset = Charset.forName("utf-8");

  private static CharsetEncoder encoder = charset.newEncoder();

  public static void main(String[] args) throws IOException, InterruptedException {
    AsynchronousChannelGroup group =
        AsynchronousChannelGroup.withThreadPool(Executors.newFixedThreadPool(3));
    final AsynchronousServerSocketChannel channel =
        AsynchronousServerSocketChannel.open(group).bind(new InetSocketAddress("localhost", 8080));
    //使用hanlder方式处理处理数据
    channel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
      @Override
      public void completed(AsynchronousSocketChannel result, Void attachment) {
        channel.accept(null, this);
        try {
          ByteBuffer byteBuffer = encoder.encode(CharBuffer.wrap(new Date().toString() + "\n\r"));
          Future<Integer> future = result.write(byteBuffer);
          future.get();
          System.out.println("sent date:" + new Date().toString());
          result.close();
        } catch (Exception e) {
          e.printStackTrace();
        }

      }

      @Override
      public void failed(Throwable exc, Void attachment) {
        exc.printStackTrace();
      }
    }

    );
    group.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);

  }
}
