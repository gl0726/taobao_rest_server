package com.shenque.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * xiao.py
 */
@ApiModel("后台选品查询参数实体")
public class SelectParam {
    @ApiModelProperty("后台选品查询参数实体")
    private String category;
    private Integer pageNo = 0;
    private Integer pageSize = 10;
    private String title;
    private Double minPrice;
    private Double maxPrice;
    private Double minCoupon;
    private Double maxCoupon;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Double getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(Double minPrice) {
        this.minPrice = minPrice;
    }

    public Double getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(Double maxPrice) {
        this.maxPrice = maxPrice;
    }

    public Double getMinCoupon() {
        return minCoupon;
    }

    public void setMinCoupon(Double minCoupon) {
        this.minCoupon = minCoupon;
    }

    public Double getMaxCoupon() {
        return maxCoupon;
    }

    public void setMaxCoupon(Double maxCoupon) {
        this.maxCoupon = maxCoupon;
    }
}