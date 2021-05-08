package com.wayleynam.http;

import com.wayleynam.utils.CaseIgnoringComparator;
import com.wayleynam.utils.ServerConfig;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class StaticHandler extends TimerTask {
    private static Logger logger = LoggerFactory.getLogger(StaticHandler.class);
    private final Map<String, String> contentTypes = new TreeMap<String, String>(CaseIgnoringComparator.INSTANCE);
    private Map<String, StaticFile> caches = new ConcurrentHashMap<>();
    private String path;
    private Timer timer;


    public StaticHandler(ServerConfig serverConfig) {
        String charset = serverConfig.getString("server.http.charset", "UTF-8");
        contentTypes.put("txt", "text/plain;charset=" + charset);
        contentTypes.put("html", "text/html;charset=" + charset);
        contentTypes.put("htm", "text/htm;charset=" + charset);
        contentTypes.put("xml", "text/xml;charset=" + charset);
        contentTypes.put("xhtml", "text/xhtml;charset=" + charset);
        contentTypes.put("css", "text/css;charset=" + charset);
        contentTypes.put("js", "text/js;charset=" + charset);
        contentTypes.put("jpg", "image/jpg;charset=" + charset);
        contentTypes.put("jpeg", "image/jpeg;charset=" + charset);
        contentTypes.put("png", "image/png;charset=" + charset);
        contentTypes.put("gif", "image/gif;charset=" + charset);

        String path = serverConfig.getString("server.http.static.dir", null);
        if (path == null) {
            String userDir = System.getProperty("user.dir");
            if (userDir.endsWith("/")) {
                path = userDir + "static/";
            } else {
                path = userDir + "/static/";
            }
        }

        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        this.path = directory.getAbsolutePath();
        logger.info("static file path:{}", this.path);
        int check = serverConfig.getInteger("server.http.static.check", 0) * 1000;
        if (check > 0) {
            this.timer = new Timer();
            this.timer.schedule(this, check, check);
        }

    }

    public StaticFile find(String queryString) {
        StaticFile cache = caches.get(queryString);
        if (cache == null) {
            if (queryString.startsWith("/")) {
                File file = new File(path + queryString);
                if (file.exists() && file.getAbsolutePath().startsWith(path)) {
                    try {
                        byte[] content = FileUtils.readFileToByteArray(file);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("EE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
                        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                        String name = file.getName();
                        int index = name.lastIndexOf(".");
                        String contentType = null;
                        if (index > 0) {
                            String suffix = name.substring(index + 1);
                            contentType = contentTypes.get(suffix);
                        }
                        if (contentType == null) {
                            contentType = "application/octet-stream";
                        }
                        cache = new StaticFile(queryString, content, dateFormat.format(new Date(file.lastModified())), contentType);
                        caches.put(queryString, cache);
                    } catch (IOException e) {
                        return null;
                    }
                } else {
                    return null;
                }
            }

        }
        return cache;

    }


    @Override
    public void run() {

    }
}
