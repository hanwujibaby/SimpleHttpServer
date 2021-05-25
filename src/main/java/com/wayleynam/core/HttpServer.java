package com.wayleynam.core;

import com.sun.corba.se.pept.transport.ByteBufferPool;
import com.wayleynam.http.*;
import com.wayleynam.utils.PropertisUtil;
import com.wayleynam.utils.ServerConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.GenericObjectPoolFactory;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.log.Log4JLogChute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.FactoryConfigurationError;
import java.io.File;
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

    private static final Logger logger = LoggerFactory.getLogger(HttpServer
            .class);

    private ExecutorService channrlWorkers;

    private ExecutorService processWorkers;

    private ExecutorService workGroup;

    private AsynchronousChannelGroup workerGroup = null;

    private AsynchronousServerSocketChannel serverSocket = null;

    private GenericObjectPool<ByteBuffer> byteBufferPool = null;

    private HttpMessageHandler dynamicHandler;

    private volatile boolean initialized = false;

    private volatile boolean started = false;

    private ServerConfig serverConfig;

    private SocketAcceptHandler socketAcceptHandler;

    private SocketReadHanler socketReadHanler;

    private String name;

    private StaticHandler staticHandler;

    private int cacheControlMaxAge;

    private String htmlContentType;

    private HttpMessageSerializer httpMessageSerializer;

    private long timeout;

    private void init() {
        logger.info("http server initial");
        this.socketAcceptHandler = new SocketAcceptHandler(this);
        this.socketReadHanler = new SocketReadHanler();
        this.serverConfig = new ServerConfig();
        this.name = serverConfig.getString("server.name", "unknwown-name");
        logger.info("server.name,{}", this.name);
        String charset = serverConfig.getString("server.http.charset", "UTF-8");
        htmlContentType = "text/html;charset=" + charset;
        cacheControlMaxAge = serverConfig.getInteger("server.http.static.expire", 0);
        String userDir = System.getProperty("user.dir");
        String tempDir;
        String templateDir;
        if (userDir.endsWith("/")) {
            tempDir = userDir + "temp/";
            templateDir = userDir + "templates/";
        } else {
            tempDir = userDir + "/temp/";
            templateDir = userDir + "/templates/";
        }
        File dir = new File(tempDir);
        if (dir.exists() == false) {
            dir.mkdirs();
        }

        logger.info("tmep.dir:{}", tempDir);
        System.setProperty("java.io.tmpdir", tempDir);
        Velocity.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM_CLASS, Log4JLogChute.class.getName());
        Velocity.setProperty(Log4JLogChute.RUNTIME_LOG_LOG4J_LOGGER, "Velocity");
        Velocity.setProperty(Log4JLogChute.RUNTIME_LOG_LOG4J_LOGGER_LEVEL, "INFO");
        Velocity.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, templateDir);
        Velocity.setProperty(Velocity.INPUT_ENCODING, charset);
        Velocity.setProperty(Velocity.OUTPUT_ENCODING, charset);
        Velocity.setProperty(Velocity.RUNTIME_LOG_REFERENCE_LOG_INVALID, "false");
        Velocity.setProperty(Velocity.FILE_RESOURCE_LOADER_CACHE, "false");

        logger.info("templates dir:{}", templateDir);
        int buffer = serverConfig.getBytesLength("server.channel.buffer", 8192);
        boolean direct = serverConfig.getBoolean("server.channel.direct", false);
        int maxActive = serverConfig.getInteger("server.channel.maxActivew", 100);
        int maxWait = serverConfig.getInteger("server.channel.maexWait", 1000);
        logger.info("server.channel.buffer : {}", buffer);
        logger.info("server.channel.direct : {}", direct);
        logger.info("server.channel.maxActive : {}", maxActive);
        logger.info("server.channel.maxWait : {}", maxWait);
        GenericObjectPool.Config poolConfig = new GenericObjectPool.Config();
        poolConfig.maxActive = maxActive;
        poolConfig.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_FAIL;
        poolConfig.maxWait = maxWait;
        poolConfig.testOnBorrow = false;
        poolConfig.testOnReturn = false;
        poolConfig.timeBetweenEvictionRunsMillis = 900000;
        poolConfig.minEvictableIdleTimeMillis = 6;
        poolConfig.testWhileIdle = false;
        byteBufferPool = new GenericObjectPool<ByteBuffer>(new ByteBufferFactory(direct, buffer), poolConfig);
        httpMessageSerializer = new HttpMessageSerializer(this.serverConfig);
    }

    public synchronized void startup() throws Exception {
        if (!initialized) {
            init();
            initialized = true;
        }

        if (started) {
            return;
        }
        logger.info("http-server startup");
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        channrlWorkers = Executors.newFixedThreadPool(availableProcessors + 1, new ProcessorThreadFactory());
        int threads = serverConfig.getInteger("server.process.workers", 0);
        if (threads > 0) {
            logger.info("used fixed thread:{} pool", threads);
            processWorkers = Executors.newFixedThreadPool(threads);
        } else {
            logger.info("used cached thread pool");
            processWorkers = Executors.newCachedThreadPool();
        }

        workerGroup = AsynchronousChannelGroup.withCachedThreadPool(channrlWorkers, 1);
        serverSocket = AsynchronousSocketChannel.open(workerGroup);
        int port = serverConfig.getInteger("server.socket.port", 80);
        int backlog = serverConfig.getInteger("server.socket.backlog", 100);
        timeout = serverConfig.getInteger("server.socket.timeout", 0);
        logger.info("server.socket.port:{]", port);
        logger.info("server.socket.backlog:{]", backlog);
        logger.info("server.socket.timeout:{]", timeout);
        serverSocket.bind(new InetSocketAddress(port), backlog);

        String dynamicSuffix = serverConfig.getString("server.http.dynamic.suffix", ".do");
        logger.info("server.http.dynamic.suffix", dynamicSuffix);
        String handleClass = serverConfig.getString("server.http.dynamic.handler", null);
        Class<HttpMessageHandler> clazz = (Class<HttpMessageHandler>) Class.forName(handleClass);
        dynamicHandler = clazz.newInstance();
        logger.info("server.http.dynamic.hanlder:{}", handleClass);
        dynamicHandler.initialize(this.serverConfig);
        logger.info("http server is started", timeout);
        started = true;
        accept();

    }


    public void accept() {
        if (started) {
            serverSocket.accept(null, this.socketAcceptHandler);
        }
    }

    public void execute(Runnable session){
        processWorkers.submit(session);
    }

    public synchronized void shutdown(){
        if(!started){
            return;
        }

        started=false;
        this.serverSocket.close();
        this.dynamicHandler.destroy();
        this.workerGroup.shutdown();
        this.channrlWorkers.shutdown();
        this.processWorkers.shutdown();
        this.channrlWorkers=null;
        this.processWorkers=null;
        this.serverSocket=null;
        this.workerGroup=null;
        this.dynamicHandler=null;

        logger.info("http server is shutdown");
    }

    public boolean isStarted(){
        return started;
    }

    public ByteBuffer borrowObject() throws Exception {
        return byteBufferPool.borrowObject();

    }

    public void returnObject(ByteBuffer buffer) throws Exception {
        byteBufferPool.returnObject(buffer);
    }


    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public SocketReadHanler getSocketReadHanler() {
        return socketReadHanler;
    }

    public void setSocketReadHanler(SocketReadHanler socketReadHanler) {
        this.socketReadHanler = socketReadHanler;
    }

    public ExecutorService getChannrlWorkers() {
        return channrlWorkers;
    }

    public void setChannrlWorkers(ExecutorService channrlWorkers) {
        this.channrlWorkers = channrlWorkers;
    }

    public ExecutorService getProcessWorkers() {
        return processWorkers;
    }

    public void setProcessWorkers(ExecutorService processWorkers) {
        this.processWorkers = processWorkers;
    }

    public AsynchronousChannelGroup getWorkerGroup() {
        return workerGroup;
    }

    public void setWorkerGroup(AsynchronousChannelGroup workerGroup) {
        this.workerGroup = workerGroup;
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public void setServerConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public SocketAcceptHandler getSocketAcceptHandler() {
        return socketAcceptHandler;
    }

    public void setSocketAcceptHandler(SocketAcceptHandler socketAcceptHandler) {
        this.socketAcceptHandler = socketAcceptHandler;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public StaticHandler getStaticHandler() {
        return staticHandler;
    }

    public void setStaticHandler(StaticHandler staticHandler) {
        this.staticHandler = staticHandler;
    }

    public HttpMessageSerializer getHttpMessageSerializer() {
        return httpMessageSerializer;
    }

    public void setHttpMessageSerializer(HttpMessageSerializer httpMessageSerializer) {
        this.httpMessageSerializer = httpMessageSerializer;
    }

    public int getCacheControlMaxAge() {
        return cacheControlMaxAge;
    }

    public void setCacheControlMaxAge(int cacheControlMaxAge) {
        this.cacheControlMaxAge = cacheControlMaxAge;
    }

    public String getHtmlContentType() {
        return htmlContentType;
    }

    public void setHtmlContentType(String htmlContentType) {
        this.htmlContentType = htmlContentType;
    }
}
