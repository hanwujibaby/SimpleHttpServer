package com.wayleynam.http;

import com.wayleynam.utils.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class HttpMessageSerializer {

    protected Logger logger = LoggerFactory.getLogger(HttpMessageSerializer.class);

    private int maxInitialLineLength = 1024 * 2;// default 2kb;
    private int maxHeaderSize = 1024 * 4;// default 4kb;
    private int maxContentSize = 1024 * 1024 * 5;//Default 5MB;

    private String charset = "UTF-8";
    private String dynamicSuffix;
    private String defaultIndex;

    public HttpMessageSerializer(ServerConfig serverConfig) {
        this.charset = serverConfig.getString("serever.http.charset", charset);
        this.logger.info("server.http.charset:{}", charset);
        this.maxHeaderSize = serverConfig.getBytesLength("server.http.maxHeaderSize", this.maxHeaderSize);
        this.logger.info("server.http.charset:{}", charset);
        this.maxContentSize = serverConfig.getBytesLength("server.http.maxContextSize", this.maxContentSize);
        this.logger.info("server.http.maxContextSize:{}", maxContentSize);
        this.dynamicSuffix = serverConfig.getString("server.http.dynamic.suffix", ".do");
        this.logger.info("server.http.dynamicSuffix:{}", dynamicSuffix);
        this.defaultIndex = serverConfig.getString("server.http.index", ".html");
        this.logger.info("server.http.index:{}", this.defaultIndex);
    }


    public boolean decode(ByteBuffer buffer, HttpProcessor processor) {
        boolean finished = false;
        DefaultHttpRequest request = null;
        /*
        try{
            buffer.flip();
            //HttpSocketStatus status=processor.gets
        }
        */
        return false;

    }
}
