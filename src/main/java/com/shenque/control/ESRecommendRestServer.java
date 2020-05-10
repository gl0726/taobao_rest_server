package com.shenque.control;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shenque.model.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * xiao.py
 */
@RestController
@RequestMapping("/recommend")
@Api(tags = "es推荐商品给APP", description = "es推荐商品给APP")
public class ESRecommendRestServer {

    private static final Logger log = Logger.getLogger(ESRecommendRestServer.class);

    private static final String index_alias_Name = "recommend";

    @Autowired
    private RestHighLevelClient esClient;

    @Autowired
    ObjectMapper objectMapper;


    @ApiOperation(value="获取淘宝天猫推荐精品商品", notes="获取淘宝天猫推荐精品商品")
    @ApiImplicitParam(name = "boutiqueParam", value = "获取淘宝天猫推荐精品商品参数对象",
            required = true, dataType = "BoutiqueParam")
    @RequestMapping(value = "boutique", method = RequestMethod.POST)
    public String getCommdityInfo(@RequestBody BoutiqueParam boutiqueParam) {
        log.info("推荐精品商品第" + boutiqueParam.getPageNo() + "页每页" + boutiqueParam.getPageSize());
        //        //构造QueryBuilder
        try{
            // 1、创建search请求
            SearchRequest searchRequest = new SearchRequest(index_alias_Name);

            // 2、用SearchSourceBuilder来构造查询请求体 ,请仔细查看它的方法，构造各种查询的方法都在这。
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            QueryBuilder recommend = QueryBuilders.termQuery("recommend", 10);
            BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
            boolBuilder.must(recommend);
            sourceBuilder.query(boolBuilder);
            //将请求体加入到请求中 //各种组合条件
            searchRequest.source(sourceBuilder);
            //3、发送请求
            SearchResponse searchResponse = esClient.search(searchRequest);

            //处理搜索命中文档结果
            SearchHits hits = searchResponse.getHits();
            if (hits.totalHits > 0){
                String boutiqueGoods = getBoutiqueGoods(boutiqueParam);
                return boutiqueGoods;
            }else {
                String goods = getGoods(boutiqueParam);
                return goods;
            }
        }catch (Exception e){
            e.printStackTrace();
            log.error("获取淘宝天猫推荐精品商品出现异常" + e );
            StringBuilder sb = new StringBuilder();
            try {
                sb.append(objectMapper.writeValueAsString(new PageList(ApiResponse.Status.INTERNAL_SERVER_ERROR.getCode(),
                        "获取淘宝天猫推荐精品商品出现异常" + e, false)));
            }catch (Exception ee){
                ee.printStackTrace();
                log.error(ee);
            }
            return sb.toString();
        }
    }

    public String getBoutiqueGoods(BoutiqueParam boutiqueParam) throws Exception{
        log.info("开始推荐精品商品第" + boutiqueParam.getPageNo() + "页每页" + boutiqueParam.getPageSize());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        List<String> list = new ArrayList<String>();
        PageList<String> pageList = new PageList<String>();
        //构造QueryBuilder
        try{
            // 1、创建search请求
            SearchRequest searchRequest = new SearchRequest(index_alias_Name);

            // 2、用SearchSourceBuilder来构造查询请求体 ,请仔细查看它的方法，构造各种查询的方法都在这。
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

            BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
            RangeQueryBuilder coupon_end_time = QueryBuilders.rangeQuery("coupon_end_time").gte(format.format(new Date()));
            boolBuilder.must(coupon_end_time);

            sourceBuilder.sort(new FieldSortBuilder("recommend").order(SortOrder.DESC));
            sourceBuilder.sort(new FieldSortBuilder("update_time").order(SortOrder.DESC));
            sourceBuilder.sort(new FieldSortBuilder("volume").order(SortOrder.DESC));
            //sourceBuilder.sort(new FieldSortBuilder("youhuiquanbilv").order(SortOrder.DESC));

            sourceBuilder.from(((boutiqueParam.getPageNo() <= 1 ? 1 : boutiqueParam.getPageNo()) - 1) * boutiqueParam.getPageSize());
            sourceBuilder.size(boutiqueParam.getPageSize());

            sourceBuilder.query(boolBuilder);
            sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

            //将请求体加入到请求中 //各种组合条件
            searchRequest.source(sourceBuilder);

            //3、发送请求
            SearchResponse searchResponse = esClient.search(searchRequest);

            //处理搜索命中文档结果
            SearchHits hits = searchResponse.getHits();

            SearchHit[] searchHits = hits.getHits();
            for (SearchHit hit : searchHits) {
                String sourceAsString = hit.getSourceAsString(); //取成json串
                list.add(sourceAsString);
            }

            pageList.setData(list);
            pageList.setTotalElements(hits.totalHits);
            pageList.setTotalPages(getTotalPages(hits.totalHits, boutiqueParam.getPageSize()));
            pageList.setCode(ApiResponse.Status.SUCCESS.getCode());
            pageList.setMessage(ApiResponse.Status.SUCCESS.getStandardMessage());
            pageList.setFlag(true);
            String s = objectMapper.writeValueAsString(pageList);
            return s;
        }catch (Exception e){
            e.printStackTrace();
            log.error("开始获取淘宝天猫推荐精品商品出现异常" + e );
            StringBuilder sb = new StringBuilder();
            try {
                sb.append(objectMapper.writeValueAsString(new PageList(ApiResponse.Status.INTERNAL_SERVER_ERROR.getCode(),
                        "开始获取淘宝天猫推荐精品商品出现异常" + e, false)));
            }catch (Exception ee){
                ee.printStackTrace();
                log.error(ee);
            }
            return sb.toString();
        }
    }


    public String getGoods(BoutiqueParam boutiqueParam) throws Exception{
        log.info("开始推荐普通商品第" + boutiqueParam.getPageNo() + "页每页" + boutiqueParam.getPageSize());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        List<String> list = new ArrayList<String>();
        PageList<String> pageList = new PageList<String>();
        //构造QueryBuilder
        try{
            // 1、创建search请求
            SearchRequest searchRequest = new SearchRequest(index_alias_Name);

            // 2、用SearchSourceBuilder来构造查询请求体 ,请仔细查看它的方法，构造各种查询的方法都在这。
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

            BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
            QueryBuilder rangeQueryBuilder   = QueryBuilders.rangeQuery("quanhoujia").gte(30);
            QueryBuilder range   = QueryBuilders.rangeQuery("youhuiquan").gte(50);
            RangeQueryBuilder coupon_end_time = QueryBuilders.rangeQuery("coupon_end_time").gte(format.format(new Date()));
            boolBuilder.must(coupon_end_time);
            boolBuilder.must(rangeQueryBuilder);
            boolBuilder.must(range);

            //sourceBuilder.sort(new FieldSortBuilder("recommend").order(SortOrder.DESC));
            //sourceBuilder.sort(new FieldSortBuilder("update_time").order(SortOrder.DESC));
            sourceBuilder.sort(new FieldSortBuilder("volume").order(SortOrder.DESC));
            //sourceBuilder.sort(new FieldSortBuilder("youhuiquan").order(SortOrder.DESC));
            sourceBuilder.sort(new FieldSortBuilder("youhuiquanbilv").order(SortOrder.DESC));


            sourceBuilder.from(((boutiqueParam.getPageNo() <= 1 ? 1 : boutiqueParam.getPageNo()) - 1) * boutiqueParam.getPageSize());
            sourceBuilder.size(boutiqueParam.getPageSize());

            sourceBuilder.query(boolBuilder);
            sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

            //将请求体加入到请求中 //各种组合条件
            searchRequest.source(sourceBuilder);

            //3、发送请求
            SearchResponse searchResponse = esClient.search(searchRequest);

            //处理搜索命中文档结果
            SearchHits hits = searchResponse.getHits();

            SearchHit[] searchHits = hits.getHits();
            for (SearchHit hit : searchHits) {
                String sourceAsString = hit.getSourceAsString(); //取成json串
                list.add(sourceAsString);
            }

            pageList.setData(list);
            pageList.setTotalElements(hits.totalHits);
            pageList.setTotalPages(getTotalPages(hits.totalHits, boutiqueParam.getPageSize()));
            pageList.setCode(ApiResponse.Status.SUCCESS.getCode());
            pageList.setMessage(ApiResponse.Status.SUCCESS.getStandardMessage());
            pageList.setFlag(true);
            String s = objectMapper.writeValueAsString(pageList);
            return s;
        }catch (Exception e){
            e.printStackTrace();
            log.error("开始获取淘宝天猫推荐普通商品出现异常" + e );
            StringBuilder sb = new StringBuilder();
            try {
                sb.append(objectMapper.writeValueAsString(new PageList(ApiResponse.Status.INTERNAL_SERVER_ERROR.getCode(),
                        "开始获取淘宝天猫推荐普通商品出现异常" + e, false)));
            }catch (Exception ee){
                ee.printStackTrace();
                log.error(ee);
            }
            return sb.toString();
        }

    }



    private int getTotalPages(long totalHits, int pageSize) {
        return pageSize == 0 ? 1 : (int) Math.ceil((double) totalHits / (double) pageSize);
    }



    @ApiOperation(value="获取淘宝天猫推荐分类商品", notes="获取淘宝天猫推荐分类商品")
    @ApiImplicitParam(name = "classificationParam", value = "获取淘宝天猫推荐分类商品参数对象",
            required = true, dataType = "ClassificationParam")
    @RequestMapping(value = "classification", method = RequestMethod.POST)
    public String getClassificationInfo(@RequestBody ClassificationParam classificationParam) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        List<String> list = new ArrayList<String>();
        PageList<String> pageList = new PageList<String>();

        log.info("推荐分类商品分类是" + classificationParam.getClassification() +  " 第" + classificationParam.getPageNo() + "页每页" + classificationParam.getPageSize());

        try{
            if( null == classificationParam || null == classificationParam.getClassification()){
                return objectMapper.writeValueAsString(new PageList(ApiResponse.Status.NULL_PARAM.getCode(),
                        ApiResponse.Status.NULL_PARAM.getStandardMessage(),false));
            }


            // 1、创建search请求
            SearchRequest searchRequest = new SearchRequest(index_alias_Name);

            // 2、用SearchSourceBuilder来构造查询请求体 ,请仔细查看它的方法，构造各种查询的方法都在这。
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

            QueryBuilder recommend = QueryBuilders.termQuery("recommend", 0);
            QueryBuilder the_category = QueryBuilders.termQuery("category", classificationParam.getClassification());

            sourceBuilder.sort(new FieldSortBuilder("volume").order(SortOrder.DESC));

            //sourceBuilder.sort(new FieldSortBuilder("youhuiquanbilv").order(SortOrder.DESC));
            BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
            boolBuilder.must(recommend);
            boolBuilder.must(the_category);
            RangeQueryBuilder coupon_end_time = QueryBuilders.rangeQuery("coupon_end_time").gte(format.format(new Date()));
            boolBuilder.must(coupon_end_time);

            sourceBuilder.from(((classificationParam.getPageNo() <= 1 ? 1 : classificationParam.getPageNo()) - 1) * classificationParam.getPageSize());
            sourceBuilder.size(classificationParam.getPageSize());

            sourceBuilder.query(boolBuilder);
            sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

            //将请求体加入到请求中 //各种组合条件
            searchRequest.source(sourceBuilder);

            //3、发送请求
            SearchResponse searchResponse = esClient.search(searchRequest);

            //处理搜索命中文档结果
            SearchHits hits = searchResponse.getHits();

            SearchHit[] searchHits = hits.getHits();
            for (SearchHit hit : searchHits) {
                String sourceAsString = hit.getSourceAsString(); //取成json串
                list.add(sourceAsString);
            }

            pageList.setData(list);
            pageList.setTotalElements(hits.totalHits);
            pageList.setTotalPages(getTotalPages(hits.totalHits, classificationParam.getPageSize()));
            pageList.setCode(ApiResponse.Status.SUCCESS.getCode());
            pageList.setMessage(ApiResponse.Status.SUCCESS.getStandardMessage());
            pageList.setFlag(true);
            String s = objectMapper.writeValueAsString(pageList);
            return s;
        }catch (Exception e){
            e.printStackTrace();
            log.error("获取淘宝天猫推荐分类商品出现异常" + e );
            StringBuilder sb = new StringBuilder();
            try {
                sb.append(objectMapper.writeValueAsString(new PageList(ApiResponse.Status.INTERNAL_SERVER_ERROR.getCode(),
                        "获取淘宝天猫推荐分类商品出现异常" + e, false)));
            }catch (Exception ee){
                ee.printStackTrace();
                log.error(ee);
            }
            return sb.toString();
        }

    }

}
