package com.wayleynam.core;

public interface ProtocolProcessor {

    void process() throws Throwable;

    void close();
}
