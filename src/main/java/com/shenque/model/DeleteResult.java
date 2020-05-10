package com.shenque.model;


import java.util.List;

/**
 * gl.py
 */
public class DeleteResult {


    private int code;

    private String message;

    private boolean flag;


    /**
     * 失败返回
     * @param code
     * @param message
     */
    public DeleteResult(int code, String message, boolean flag) {
        this.code = code;
        this.message = message;
        this.flag = flag;
    }
    public DeleteResult(){

    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
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
}
