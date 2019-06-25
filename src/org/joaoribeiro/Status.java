package org.joaoribeiro;

import java.util.PrimitiveIterator;

public enum Status {
    OK(200),
    CLIENT_ERROR(400);

    private int code;

    Status(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
