package com.shenque.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
/**
 * xiao.py
 */
@ApiModel("用户实体")
public class Page {
    @ApiModelProperty("淘宝天猫优惠券区间范围参数")
    private String  couponRange;
    private Integer currentPage = 0;
    private Integer pageSize = 10;

    public String getCouponRange() {
        return couponRange;
    }

    public void setCouponRange(String couponRange) {
        this.couponRange = couponRange;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}