package com.shenque.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("删除商品传递的参数")
public class DeleteParam {
    @ApiModelProperty("删除商品传递的参数")
    private Long goodsId = 0L;

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }
}
