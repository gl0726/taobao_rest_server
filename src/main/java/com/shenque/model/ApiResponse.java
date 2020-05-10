package com.shenque.model;

/**
 * xiao.py
 */

public class ApiResponse {
    private int code;
    private String message;
    private Object data;
    private boolean more;

    public ApiResponse(int code, String message, Object data,boolean more) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.more = more;
    }

    public ApiResponse() {
        this.code = Status.SUCCESS.getCode();
        this.message = Status.SUCCESS.getStandardMessage();
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

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public boolean isMore() {
        return more;
    }

    public void setMore(boolean more) {
        this.more = more;
    }

    public enum Status {
        NOT_PARAM(40005, "pageNo or pageSize is null,Please provide currentPage or pageSize param values"),
        NOT_QUERY_PARAM(40005, "sortId and productSearch is null,Please provide sortId or productSearch param values"),
        NULL_PARAM(50000, "Param is null"),
        SUCCESS(200, "OK"),
        BAD_REQUEST(400, "Bad Request"),
        NOT_FOUND(404, "Not Found"),
        INTERNAL_SERVER_ERROR(500, "Unknown Internal Error"),
        NOT_PAGE_PARAM(40005, "currentPage or pageSize is null,Please provide currentPage or pageSize param values"),
        NOT_RANGE_PARAM(40006, "couponRange is null,Please provide couponRange param values"),
        NULL_PAGE(50000, "Page is null");



        private int code;
        private String standardMessage;

        Status(int code, String standardMessage) {
            this.code = code;
            this.standardMessage = standardMessage;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getStandardMessage() {
            return standardMessage;
        }

        public void setStandardMessage(String standardMessage) {
            this.standardMessage = standardMessage;
        }
    }
}
