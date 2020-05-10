package com.shenque.control.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shenque.model.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
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
 * es商品给APP 首页
 */
@RestController
@RequestMapping("/home")
@Api(tags = "es商品给APP首页", description = "es商品给APP首页")
public class AppHomeRestServer {

    private static final Logger log = Logger.getLogger(AppHomeRestServer.class);

    private static final String index_alias_Name = "recommend";
    private static final String dou_index_alias_Name = "dou";
    private static final String jd_index_alias_Name = "jd_index";

    @Autowired
    private RestHighLevelClient esClient;

    @Autowired
    ObjectMapper objectMapper;


    /**
     * APP推荐商品 APP 3.X版本的推荐
     * @param boutiqueParam
     * @return
     */
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

    /**
     * 获取精选商品 APP 3.X版本的推荐
     * @param boutiqueParam
     * @return
     * @throws Exception
     */
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


    /**
     * 获取除人工推荐商品外的其它商品 APP 3.X版本的推荐
     * @param boutiqueParam
     * @return
     * @throws Exception
     */
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


    /**
     * APP 淘宝天猫首页分类商品
     * @param classificationParam
     * @return
     */
    @ApiOperation(value="获取淘宝天猫首页分类商品", notes="获取淘宝天猫首页分类商品")
    @ApiImplicitParam(name = "classificationParam", value = "获取淘宝天猫首页分类商品参数对象",
            required = true, dataType = "ClassificationParam")
    @RequestMapping(value = "tb_clfi", method = RequestMethod.POST)
    public String getClassificationInfo(@RequestBody ClassificationParam classificationParam) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        List<String> list = new ArrayList<String>();
        PageList<String> pageList = new PageList<String>();

        log.info("首页分类商品分类是" + classificationParam.getClassification() +
                " 第" + classificationParam.getPageNo() + "页每页" + classificationParam.getPageSize());

        try{
            if( null == classificationParam || null == classificationParam.getClassification()){
                return objectMapper.writeValueAsString(new PageList(ApiResponse.Status.NULL_PARAM.getCode(),
                        ApiResponse.Status.NULL_PARAM.getStandardMessage(),false));
            }


            // 1、创建search请求
            SearchRequest searchRequest = new SearchRequest(index_alias_Name);

            // 2、用SearchSourceBuilder来构造查询请求体 ,请仔细查看它的方法，构造各种查询的方法都在这。
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();

            /**
             * 类目
             * 5 女装
             * 9 男装
             * 7 洗护
             * 6 美妆(对应淘宝的彩妆)
             * 12 家居清洁(对应淘宝的家居)
             * 11 厨房电器(对应淘宝的家电)
             */
            QueryBuilder the_category = QueryBuilders.termQuery("category", classificationParam.getClassification());
            boolBuilder.must(the_category);

            //推荐抵扣金额在50-400元之间且优惠率达70%以上的商品，先按照优惠率进行降序排列，再按照优惠券降序排列
            /*QueryBuilder dQueryBuilder   = QueryBuilders.rangeQuery("youhuiquan").gte(50).lte(400);
            boolBuilder.must(dQueryBuilder);*/
            //因为男装类目中出现很多女装
            if (classificationParam.getClassification() == 9){
                QueryBuilder musnot = QueryBuilders.termQuery("title", "女");
                boolBuilder.mustNot(musnot);
            }

            QueryBuilder ti = QueryBuilders.termQuery("title", "吹风机");
            boolBuilder.mustNot(ti);

            QueryBuilder pRateQueryBuilder   = QueryBuilders.rangeQuery("youhuiquanbilv").gt(0.5);
            boolBuilder.must(pRateQueryBuilder);

            sourceBuilder.sort(new FieldSortBuilder("s_id").order(SortOrder.DESC));
            //sourceBuilder.sort(new FieldSortBuilder("youhuiquan").order(SortOrder.DESC));

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
            log.error("获取淘宝天猫首页分类商品出现异常" + e );
            StringBuilder sb = new StringBuilder();
            try {
                sb.append(objectMapper.writeValueAsString(new PageList(ApiResponse.Status.INTERNAL_SERVER_ERROR.getCode(),
                        "获取淘宝天猫首页分类商品出现异常" + e, false)));
            }catch (Exception ee){
                ee.printStackTrace();
                log.error(ee);
            }
            return sb.toString();
        }

    }




    /**
     * APP 首页抖货商品(精选)
     * @param boutiqueParam
     * @return
     */
    @ApiOperation(value="获取首页抖货商品", notes="获取首页抖货商品")
    @ApiImplicitParam(name = "boutiqueParam", value = "首页抖货商品参数对象",
            required = true, dataType = "BoutiqueParam")
    @RequestMapping(value = "dou", method = RequestMethod.POST)
    public String getDou(@RequestBody BoutiqueParam boutiqueParam) {
        List<String> list = new ArrayList<String>();
        PageList<String> pageList = new PageList<String>();

        log.info("首页抖货商品第" + boutiqueParam.getPageNo() + "页每页" + boutiqueParam.getPageSize());

        try{
            if( null == boutiqueParam){
                return objectMapper.writeValueAsString(new PageList(ApiResponse.Status.NULL_PARAM.getCode(),
                        ApiResponse.Status.NULL_PARAM.getStandardMessage(),false));
            }

            // 1、创建search请求
            SearchRequest searchRequest = new SearchRequest(dou_index_alias_Name);

            // 2、用SearchSourceBuilder来构造查询请求体 ,请仔细查看它的方法，构造各种查询的方法都在这。
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

            BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();

            //推荐抵扣金额在50-400元之间且优惠率达70%以上的商品，先按照优惠率进行降序排列，再按照优惠券降序排列
            QueryBuilder dQueryBuilder   = QueryBuilders.rangeQuery("couponmoney").gte(50).lte(400);
            boolBuilder.must(dQueryBuilder);

            QueryBuilder pRateQueryBuilder   = QueryBuilders.rangeQuery("youhuijuanbilv").gt(0.70);
            boolBuilder.must(pRateQueryBuilder);

            RangeQueryBuilder coupon_end_time = QueryBuilders.rangeQuery("couponendtime").gte(new Date().getTime()/1000);
            boolBuilder.must(coupon_end_time);

            sourceBuilder.sort(new FieldSortBuilder("s_id").order(SortOrder.DESC));
            sourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.DESC));
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
            log.error("获取首页抖货商品出现异常" + e );
            StringBuilder sb = new StringBuilder();
            try {
                sb.append(objectMapper.writeValueAsString(new PageList(ApiResponse.Status.INTERNAL_SERVER_ERROR.getCode(),
                        "获取首页抖货商品出现异常" + e, false)));
            }catch (Exception ee){
                ee.printStackTrace();
                log.error(ee);
            }
            return sb.toString();
        }

    }


    /**
     * APP 获取推荐页的京东商品
     * @param boutiqueParam
     * @return
     */
    @ApiOperation(value="获取推荐页的京东商品", notes="获取推荐页的京东商品")
    @ApiImplicitParam(name = "boutiqueParam", value = "获取推荐页的京东商品",
            required = true, dataType = "BoutiqueParam")
    @RequestMapping(value = "jd", method = RequestMethod.POST)
    public String getJD(@RequestBody BoutiqueParam boutiqueParam) {
        log.info("推荐页访问JD请求开始");
        List<String> list = new ArrayList<String>();
        PageList<String> pageList = new PageList<String>();

        log.info("推荐页的京东商品是第" + boutiqueParam.getPageNo() + "页每页" + boutiqueParam.getPageSize());

        try{
            if( null == boutiqueParam){
                log.info("请求参数boutiqueParam为空，请求结束");
                return objectMapper.writeValueAsString(new PageList(ApiResponse.Status.NULL_PARAM.getCode(),
                        ApiResponse.Status.NULL_PARAM.getStandardMessage(),false));
            }


            // 1、创建search请求
            SearchRequest searchRequest = new SearchRequest(jd_index_alias_Name);

            // 2、用SearchSourceBuilder来构造查询请求体 ,请仔细查看它的方法，构造各种查询的方法都在这。
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();


            //推荐标准：除人工置顶外，按照优惠率进行降序排列，并将交易订单量作为参考标准。
            sourceBuilder.sort(new FieldSortBuilder("couponRatio").order(SortOrder.DESC));

            RangeQueryBuilder coupon_end_time = QueryBuilders.rangeQuery("end_time").gte((new Date().getTime() / 1000));
            boolBuilder.must(coupon_end_time);

            //卷后价大于0 ，有些商品原价100，卷800，结果为-700，要剔除这些负数
            RangeQueryBuilder price_after_end = QueryBuilders.rangeQuery("price_after").gte(0);
            boolBuilder.must(price_after_end);

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
            log.info("获取hits的数据量: " + searchHits.length);
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
            log.info("访问结束");
            return s;
        }catch (Exception e){
            e.printStackTrace();
            log.error("获取推荐页的京东商品出现异常" + e );
            StringBuilder sb = new StringBuilder();
            try {
                sb.append(objectMapper.writeValueAsString(new PageList(ApiResponse.Status.INTERNAL_SERVER_ERROR.getCode(),
                        "获取推荐页的京东商品出现异常" + e, false)));
            }catch (Exception ee){
                ee.printStackTrace();
                log.error(ee);
            }
            return sb.toString();
        }

    }


}
