package com.shenque.control.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shenque.model.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.log4j.Logger;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * gl.py
 * es商品给App删除接口
 */
@RestController
@RequestMapping("/delete")
@Api(tags = "jd商品删除接口", description = "jd商品删除接口")
public class APPJDDeleteServer {

    private static final Logger log = Logger.getLogger(AppHomeRestServer.class);

    private static final String jd_index_alias_Name = "jd_index";

    @Autowired
    private RestHighLevelClient esClient;

    @Autowired
    ObjectMapper objectMapper;

    /**
     * APP根据jd商品id删除商品接口
     * @param deleteParam
     * @return
     */
    @ApiOperation(value="根据jd商品id删除商品接口", notes="根据jd商品id删除商品接口")
    @ApiImplicitParam(name = "id", value = "要删除的商品id",
            required = true, dataType = "BoutiqueParam")
    @RequestMapping(value = "jd", method = RequestMethod.POST)
    public String getCommdityInfo(@RequestBody DeleteParam deleteParam) {

        log.info("删除jd商品id为 " + deleteParam.getGoodsId() + " 的商品");
        DeleteResult deleteResult = new DeleteResult();
        try{
            DeleteRequest deleteRequest = new DeleteRequest(jd_index_alias_Name,"jd_goods",deleteParam.getGoodsId().toString());
            DeleteResponse delete = esClient.delete(deleteRequest);
            log.info("删除jd商品ID状态：" + delete.status().toString());
            deleteResult.setCode(ApiResponse.Status.SUCCESS.getCode());
            deleteResult.setMessage(ApiResponse.Status.SUCCESS.getStandardMessage());
            deleteResult.setFlag(true);
            String s = objectMapper.writeValueAsString(deleteResult);
            return s;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("删除jd商品ID出现异常" + e );
            StringBuilder sb = new StringBuilder();
            try {
                sb.append(objectMapper.writeValueAsString(new DeleteResult(ApiResponse.Status.INTERNAL_SERVER_ERROR.getCode(),
                        "删除商品ID出现异常" + e, false)));
            }catch (Exception ee) {
                ee.printStackTrace();
                log.error(ee);
            }
            return sb.toString();
        }
    }


}
