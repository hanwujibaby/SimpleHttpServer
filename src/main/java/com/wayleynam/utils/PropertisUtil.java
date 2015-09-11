package com.wayleynam.utils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Properties;

/**
 * Created by wei4liverpool on 9/10/15.
 */
public class PropertisUtil {
    private static final byte[] lock = new byte[1];
    private static Properties instance;
    private Properties properties;

    public static Properties getInstance() {
        if (instance != null) {
            return instance;
        }
        synchronized (lock) {
            instance = new Properties();
            InputStream inputStream = PropertisUtil.class.getClassLoader().getResourceAsStream("config.properties");
            try {
                instance.load(inputStream);
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return instance;
        }

    }


    public static Long getLong(String key) {
        try {
            String value = getInstance().getProperty(key);
            return Long.valueOf(value);
        } catch (Exception e) {
            e.printStackTrace();
            return -1L;
        }
    }


    public static Integer getInteger(String key) {
        try {
            String value = getInstance().getProperty(key);
            return Integer.valueOf(value);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

    }


}
