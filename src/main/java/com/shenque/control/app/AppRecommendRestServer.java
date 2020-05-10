package com.shenque.control.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shenque.model.ApiResponse;
import com.shenque.model.BoutiqueParam;
import com.shenque.model.PageList;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
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
 * es商品给APP 推荐页
 */
@RestController
@RequestMapping("/recommend")
@Api(tags = "es商品给APP推荐页", description = "es商品给APP推荐页")
public class AppRecommendRestServer {
    private static final Logger log = Logger.getLogger(AppRecommendRestServer.class);

    private static final String index_alias_Name = "recommend";

    private static final String dou_index_alias_Name = "dou";

    private static final String pindd_index_alias_Name = "pindd";

    @Autowired
    private RestHighLevelClient esClient;

    @Autowired
    ObjectMapper objectMapper;




    /**
     * APP 获取推荐页的淘宝天猫商品
     * @param boutiqueParam
     * @return
     */
    @ApiOperation(value="获取推荐页的淘宝天猫商品", notes="获取推荐页的淘宝天猫商品")
    @ApiImplicitParam(name = "boutiqueParam", value = "获取推荐页的淘宝天猫商品参数对象",
            required = true, dataType = "BoutiqueParam")
    @RequestMapping(value = "tb", method = RequestMethod.POST)
    public String getTb(@RequestBody BoutiqueParam boutiqueParam) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        List<String> list = new ArrayList<String>();
        PageList<String> pageList = new PageList<String>();

        log.info("推荐页的淘宝天猫商品是第" + boutiqueParam.getPageNo() + "页每页" + boutiqueParam.getPageSize());

        try{
            if( null == boutiqueParam){
                return objectMapper.writeValueAsString(new PageList(ApiResponse.Status.NULL_PARAM.getCode(),
                        ApiResponse.Status.NULL_PARAM.getStandardMessage(),false));
            }


            // 1、创建search请求
            SearchRequest searchRequest = new SearchRequest(index_alias_Name);

            // 2、用SearchSourceBuilder来构造查询请求体 ,请仔细查看它的方法，构造各种查询的方法都在这。
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();


            //推荐抵扣金额在50-400元之间且优惠率达70%以上的商品，先按照优惠率进行降序排列，再按照优惠券降序排列
            QueryBuilder dQueryBuilder   = QueryBuilders.rangeQuery("youhuiquan").gte(50).lte(400);
            boolBuilder.must(dQueryBuilder);

            QueryBuilder pRateQueryBuilder   = QueryBuilders.rangeQuery("youhuiquanbilv").gt(0.7);
            boolBuilder.must(pRateQueryBuilder);

            sourceBuilder.sort(new FieldSortBuilder("s_id").order(SortOrder.DESC));
            //sourceBuilder.sort(new FieldSortBuilder("youhuiquan").order(SortOrder.DESC));

            RangeQueryBuilder coupon_end_time = QueryBuilders.rangeQuery("coupon_end_time").gte(format.format(new Date()));
            boolBuilder.must(coupon_end_time);

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
            log.error("获取推荐页的淘宝天猫商品出现异常" + e );
            StringBuilder sb = new StringBuilder();
            try {
                sb.append(objectMapper.writeValueAsString(new PageList(ApiResponse.Status.INTERNAL_SERVER_ERROR.getCode(),
                        "获取推荐页的淘宝天猫商品出现异常" + e, false)));
            }catch (Exception ee){
                ee.printStackTrace();
                log.error(ee);
            }
            return sb.toString();
        }

    }



    /**
     * APP 获取推荐页的拼多多商品
     * @param boutiqueParam
     * @return
     */
    @ApiOperation(value="获取推荐页的拼多多商品", notes="获取推荐页的拼多多商品")
    @ApiImplicitParam(name = "boutiqueParam", value = "获取推荐页的拼多多商品参数对象",
            required = true, dataType = "BoutiqueParam")
    @RequestMapping(value = "pindd", method = RequestMethod.POST)
    public String getPindd(@RequestBody BoutiqueParam boutiqueParam) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        List<String> list = new ArrayList<String>();
        PageList<String> pageList = new PageList<String>();

        log.info("推荐页的拼多多商品第" + boutiqueParam.getPageNo() + "页每页" + boutiqueParam.getPageSize());

        try{
            if( null == boutiqueParam ){
                return objectMapper.writeValueAsString(new PageList(ApiResponse.Status.NULL_PARAM.getCode(),
                        ApiResponse.Status.NULL_PARAM.getStandardMessage(),false));
            }


            // 1、创建search请求
            SearchRequest searchRequest = new SearchRequest(pindd_index_alias_Name);

            // 2、用SearchSourceBuilder来构造查询请求体 ,请仔细查看它的方法，构造各种查询的方法都在这。
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
            RangeQueryBuilder coupon_end_time = QueryBuilders.rangeQuery("end_time").gte(new Date().getTime()/1000);
            boolBuilder.must(coupon_end_time);

            //推荐抵扣金额在50-400元之间且优惠率达70%以上的商品，先按照优惠率进行降序排列，再按照优惠券降序排列
            //QueryBuilder dQueryBuilder   = QueryBuilders.rangeQuery("youhuiquan").gte(50).lte(400);
            //boolBuilder.must(dQueryBuilder);

            //QueryBuilder pRateQueryBuilder   = QueryBuilders.rangeQuery("youhuiquanbilv").gt(0.7);
            //boolBuilder.must(pRateQueryBuilder);

            sourceBuilder.sort(new FieldSortBuilder("youhuijuanbilv").order(SortOrder.DESC));
            //sourceBuilder.sort(new FieldSortBuilder("youhuiquan").order(SortOrder.DESC));

            sourceBuilder.query(boolBuilder);

            sourceBuilder.from(((boutiqueParam.getPageNo() <= 1 ? 1 : boutiqueParam.getPageNo()) - 1) * boutiqueParam.getPageSize());
            sourceBuilder.size(boutiqueParam.getPageSize());

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
            log.error("获取推荐页的拼多多商品出现异常" + e );
            StringBuilder sb = new StringBuilder();
            try {
                sb.append(objectMapper.writeValueAsString(new PageList(ApiResponse.Status.INTERNAL_SERVER_ERROR.getCode(),
                        "获取推荐页的拼多多商品出现异常" + e, false)));
            }catch (Exception ee){
                ee.printStackTrace();
                log.error(ee);
            }
            return sb.toString();
        }

    }



    /**
     * APP 推荐页的推荐栏目的商品
     * @param boutiqueParam
     * @return
     */
    @ApiOperation(value="获取推荐页的推荐栏目的商品", notes="获取推荐页的推荐栏目的商品")
    @ApiImplicitParam(name = "boutiqueParam", value = "获取推荐页的推荐栏目的商品参数对象",
            required = true, dataType = "BoutiqueParam")
    @RequestMapping(value = "tb_recom", method = RequestMethod.POST)
    public String getTbRecom(@RequestBody BoutiqueParam boutiqueParam) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        List<String> list = new ArrayList<String>();
        PageList<String> pageList = new PageList<String>();

        log.info("推荐页的推荐栏目的商品第" + boutiqueParam.getPageNo() + "页每页" + boutiqueParam.getPageSize());

        try{
            if( null == boutiqueParam){
                return objectMapper.writeValueAsString(new PageList(ApiResponse.Status.NULL_PARAM.getCode(),
                        ApiResponse.Status.NULL_PARAM.getStandardMessage(),false));
            }


            // 1、创建search请求
            SearchRequest searchRequest = new SearchRequest(index_alias_Name);

            // 2、用SearchSourceBuilder来构造查询请求体 ,请仔细查看它的方法，构造各种查询的方法都在这。
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();

            //推荐抵扣金额在50-400元之间且优惠率达70%以上的商品，先按照优惠率进行降序排列，再按照优惠券降序排列
            QueryBuilder dQueryBuilder   = QueryBuilders.rangeQuery("youhuiquan").gte(50).lte(400);
            boolBuilder.must(dQueryBuilder);

            QueryBuilder pRateQueryBuilder   = QueryBuilders.rangeQuery("youhuiquanbilv").gt(0.85);
            boolBuilder.must(pRateQueryBuilder);


            sourceBuilder.sort(new FieldSortBuilder("s_id").order(SortOrder.ASC));
            //sourceBuilder.sort(new FieldSortBuilder("activeid_id").order(SortOrder.ASC));
            //sourceBuilder.sort(new FieldSortBuilder("volume").order(SortOrder.DESC));

            RangeQueryBuilder coupon_end_time = QueryBuilders.rangeQuery("coupon_end_time").gte(format.format(new Date()));
            boolBuilder.must(coupon_end_time);

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
            log.error("获取推荐页的推荐栏目的商品出现异常" + e );
            StringBuilder sb = new StringBuilder();
            try {
                sb.append(objectMapper.writeValueAsString(new PageList(ApiResponse.Status.INTERNAL_SERVER_ERROR.getCode(),
                        "获取推荐页的推荐栏目的商品出现异常" + e, false)));
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

}
