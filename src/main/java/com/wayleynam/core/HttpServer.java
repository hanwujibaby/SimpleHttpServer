package com.wayleynam.core;

import com.sun.corba.se.pept.transport.ByteBufferPool;
import com.wayleynam.http.ByteBufferFactory;
import com.wayleynam.http.ProcessThreadFactory;
import com.wayleynam.http.SocketAcceptorHandler;
import com.wayleynam.http.SocketReadHanler;
import com.wayleynam.utils.PropertisUtil;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.GenericObjectPoolFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
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


    private GenericObjectPool<ByteBuffer> genericObjectPool;


    void init() {
        try {
            acceptorHandler = new SocketAcceptorHandler(this);
            workGroup = Executors.newFixedThreadPool(PropertisUtil.getInteger("server.socket.threadNum"),
                    new ProcessThreadFactory());
            int proccessorNum = Runtime.getRuntime().availableProcessors();
            channelGroup = AsynchronousChannelGroup.withCachedThreadPool(workGroup, 1);
            serverSocket = AsynchronousServerSocketChannel.open(channelGroup);

            int maxActive = PropertisUtil.getInteger("server.channel.maxActive");
            int maxWait = PropertisUtil.getInteger("server.channel.maxWait");
            GenericObjectPool.Config config = new GenericObjectPool.Config();
            config.maxActive = maxActive;
            config.maxWait = maxWait;
            config.testOnBorrow = false;
            config.testOnReturn = false;
            config.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_FAIL;
            config.timeBetweenEvictionRunsMillis = 90000;
            config.testWhileIdle = false;
            // 设定连接池
            genericObjectPool =
                    new GenericObjectPool<ByteBuffer>(new ByteBufferFactory(true, 8192), config);

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

    public ByteBuffer borrowObject() {
        try {
            return genericObjectPool.borrowObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void main(String[] args) {
        byte b = (byte) 11111011;
        System.out.println(Integer.toBinaryString(255));
    }

}
