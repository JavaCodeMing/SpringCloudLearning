package com.example.cloud.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * @author dengzhiming
 * @date 2020/3/10 23:43
 */
@Data
public class CommonResult<T> implements Serializable {
    private Integer code;
    private String message;
    private T data;

    public CommonResult() {
    }

    public CommonResult(T data, String message, Integer code) {
        this.data = data;
        this.message = message;
        this.code = code;
    }

    public CommonResult(String message, Integer code) {
        this(null, message, code);
    }

    public CommonResult(T data) {
        this(data, "操作成功", 200);
    }
}
