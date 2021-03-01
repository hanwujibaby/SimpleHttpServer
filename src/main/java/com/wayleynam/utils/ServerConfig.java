package com.wayleynam.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;

/***
 * server配置类
 */
public class ServerConfig {
    private final static Log log = LogFactory.getLog(ServerConfig.class);
    private Properties properties = new Properties();

    public ServerConfig() {
        InputStream in = null;
        try {
            in = ServerConfig.class.getClassLoader().getResourceAsStream("config.properties");
            properties.load(in);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }


    public Integer getInteger(String key, Integer defaultValue) {

        String v = properties.getProperty(key);
        if (v == null) {
            return defaultValue;
        }
        Integer intValue = Integer.parseInt(v);
        return intValue;

    }


    public Boolean getBoolean(String key, Boolean defaultValue) {
        String v = properties.getProperty(key);
        if (v == null) {
            return defaultValue;
        }

        Boolean booleanValue = Boolean.parseBoolean(v);
        return booleanValue;
    }

    public int getBytesLength(String key, int defaultValue) {
        String str = properties.getProperty(key);
        if (str != null) {
            if (str.toUpperCase().endsWith("MB")) {
                return Integer.parseInt(str.substring(0, str.length() - 2)) * 1024 * 1024;
            } else if (str.toUpperCase().endsWith("KB")) {
                return Integer.parseInt(str.substring(0, str.length() - 2)) * 1024;
            } else if (str.toUpperCase().endsWith("B")) {
                return Integer.parseInt(str.substring(0, str.length() - 1));
            } else {
                throw new IllegalArgumentException(key + " : " + str);
            }
        } else {
            return defaultValue;
        }
    }


}
