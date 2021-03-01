package com.wayleynam.constants;

public enum HttpVersion {

    HTTP_1_0("HTTP/1.0", false),
    HTTP_1_1("HTTP/1.1", true);

    private boolean keepAliveDefault;
    private String name;

    HttpVersion(String name, boolean keepAliveDefault) {
        this.name = name;
        this.keepAliveDefault = keepAliveDefault;
    }


    public boolean isKeepAliveDefault() {
        return keepAliveDefault;
    }

    @Override
    public String toString() {
        return this.name;
    }

}
