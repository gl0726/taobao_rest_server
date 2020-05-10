package com.shenque.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("选品某个商品传递的参数")
public class UpdateParam {
    @ApiModelProperty("选品某个商品传递的参数")
    private String idlist;

    public String getIdlist() {
        return idlist;
    }

    public void setIdlist(String idlist) {
        this.idlist = idlist;
    }
}
