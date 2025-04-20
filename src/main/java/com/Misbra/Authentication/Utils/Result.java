package com.Misbra.Authentication.Utils;

import lombok.Data;

@Data
public class Result<T> {
    private final T data;
    private final String error;

    public boolean isSuccess() {
        return error == null;
    }

    public T getData() {
        return data;
    }

    public String getError() {
        return error;
    }
}
