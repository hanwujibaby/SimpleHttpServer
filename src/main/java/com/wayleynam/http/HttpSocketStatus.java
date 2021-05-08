package com.wayleynam.http;

public enum HttpSocketStatus {
    SKIP_CONTROL_CHARS,
    READ_INITIAL,
    READ_HEADER,
    READ_VARIABLE_LENGTH_CONTENT,
    RUNNING,
    WRITING;
}
