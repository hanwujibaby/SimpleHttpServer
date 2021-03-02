package com.wayleynam.constants;

public enum HttpMethod {

    GET, POST;

    public static HttpMethod getMethod(String name) {
        if (name.equals("GET")) {
            return GET;
        } else if (name.equals("POST")) {
            return POST;
        } else {
            return null;
        }
    }

}
