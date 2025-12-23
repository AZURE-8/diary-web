package com.diaryweb.demo.common;

public class BizException extends RuntimeException {

    private final int code;

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    // 常用静态方法
    public static BizException badRequest(String message) {
        return new BizException(4000, message);
    }

    public static BizException notFound(String message) {
        return new BizException(4040, message);
    }

    public static BizException forbidden(String message) {
        return new BizException(4030, message);
    }
}
