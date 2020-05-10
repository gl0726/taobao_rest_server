package com.shenque.model;
/**
 * xiao.py
 */
public class ServiceResult {
    private boolean success;
    private String message;

    public ServiceResult() {
    }

    public ServiceResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "{" +
                "\"success\":" + success +
                ", \"message\":\"" + message + '\"' +
                '}';
    }
}
