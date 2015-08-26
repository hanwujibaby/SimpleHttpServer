package com.wayleynam;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by wei4liverpool on 8/26/15.
 */
public class Client {

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        AsynchronousSocketChannel channel= AsynchronousSocketChannel.open();
        Future<Void> future=channel.connect(new InetSocketAddress("localhost", 8080));
        //等待任务的完成直到拿到数据
        future.get();
        final ByteBuffer byteBuffer=ByteBuffer.allocate(100);
        //拿到数据之后丰富数据
        channel.read(byteBuffer, null, new CompletionHandler<Integer , Object>() {

            @Override
            public void completed(Integer result, Object attachment) {
                System.out.println(new String(byteBuffer.array()));
            }

            @Override
            public void failed(Throwable exc, Object attachment) {

            }

        });
        Thread.sleep(19999);

    }



}
