package com.shenque.model;


import java.util.List;

/**
 * xiao.py
 */
public class PageList<T>{

    List<T> data;

    private int totalPages = 0;

    private long totalElements = 0;

    private int code;

    private String message;

    private boolean flag;


    /**
     * 失败返回
     * @param code
     * @param message
     */
    public PageList(int code, String message,boolean flag) {
        this.data = null;
        this.code = code;
        this.message = message;
        this.flag = flag;
    }
    public PageList(){

    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
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
