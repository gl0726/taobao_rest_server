package com.shenque.model;

public class ReResult {
    private int code;
    private String message;
    private boolean flag;

    public ReResult() {
    }

    public ReResult(boolean flag) {
        this.code = 200;
        this.message = "OK";
        this.flag = flag;
    }

    public ReResult(int code, String message, boolean flag) {
        this.code = code;
        this.message = message;
        this.flag = flag;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    @Override
    public String toString() {
        return "{" +
                "\"code\":" + code +
                ", \"message\":\"" + message + '\"' +
                ", \"flag\":" + flag +
                '}';
    }
}
