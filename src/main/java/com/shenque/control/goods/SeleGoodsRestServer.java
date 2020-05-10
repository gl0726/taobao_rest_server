package com.shenque.control.goods;

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
 * 后台管理人工精选商品
 */
@RestController
@RequestMapping("/select")
@Api(tags = "后台管理人工精选商品接口", description = "后台管理人工精选商品接口")
public class SeleGoodsRestServer {
    private static final Logger log = Logger.getLogger(SeleGoodsRestServer.class);

    private static final String tb_index_alias_Name = "recommend";

    private static final String jd_index_alias_Name = "jd_index";

    //private static final String TB_TYPE = "recommend_goods";


    private static final String dou_index_alias_Name = "dou";

    //private static final String DOU_TYPE = "dou_goods";


    private static final String pindd_index_alias_Name = "pindd";

   // private static final String PINDD_TYPE = "pindd_goods";

    @Autowired
    private RestHighLevelClient esClient;

    @Autowired
    ObjectMapper objectMapper;


    /**
     * 提供所有的淘宝天猫商品给后台管理系统
     * @param selectParam
     * @return
     */
    @ApiOperation(value="提供淘宝天猫商品给后台管理", notes="提供淘宝天猫商品给后台管理")
    @ApiImplicitParam(name = "selectParam", value = "提供淘宝天猫商品给后台管理需要的参数",
            required = true, dataType = "SelectParam")
    @RequestMapping(value = "tb", method = RequestMethod.POST)
    public String getTb(@RequestBody SelectParam selectParam) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        List<String> list = new ArrayList<String>();
        PageList<String> pageList = new PageList<String>();

        //构造QueryBuilder
        try{
            // 1、创建search请求
            SearchRequest searchRequest = new SearchRequest(tb_index_alias_Name);
            // 2、用SearchSourceBuilder来构造查询请求体
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
            //类目
            QueryBuilder category = null;
            //商品搜索
            MatchQueryBuilder matchQueryBuilder = null;
            //劵后价格区间
            RangeQueryBuilder priceQueryBuilder = null;
            //优惠券区间
            RangeQueryBuilder couponQueryBuilder = null;

            if( null == selectParam ){
                return objectMapper.writeValueAsString(new PageList(ApiResponse.Status.NULL_PARAM.getCode(),
                        ApiResponse.Status.NULL_PARAM.getStandardMessage(),false));
            }

            if (StringUtils.isNotBlank(selectParam.getCategory()) && !selectParam.getCategory().trim().toLowerCase().equals("null")){
                category = QueryBuilders.termQuery("category", selectParam.getCategory());
                boolBuilder.must(category);
            }

            if (StringUtils.isNotBlank(selectParam.getTitle())){
                matchQueryBuilder = QueryBuilders.matchQuery("title",selectParam.getTitle()).minimumShouldMatch("100%");
                boolBuilder.must(matchQueryBuilder);//.boost(5);
            }

            if (null != selectParam.getMinPrice() || null != selectParam.getMaxPrice()){
                if(null != selectParam.getMinPrice() && null != selectParam.getMaxPrice()){
                    priceQueryBuilder  = QueryBuilders.rangeQuery("quanhoujia").gte(selectParam.getMinPrice()).lte(selectParam.getMaxPrice());
                }else {
                    System.out.println(selectParam.getMinPrice());
                    System.out.println(selectParam.getMaxPrice());
                    if (null != selectParam.getMinPrice()){
                        priceQueryBuilder  = QueryBuilders.rangeQuery("quanhoujia").gte(selectParam.getMinPrice());
                    }
                    if (null != selectParam.getMaxPrice()){
                        priceQueryBuilder  = QueryBuilders.rangeQuery("quanhoujia").lte(selectParam.getMaxPrice());
                    }
                }
            }

            if (null != selectParam.getMinCoupon() || null != selectParam.getMaxCoupon()){
                if(null != selectParam.getMinCoupon() && null != selectParam.getMaxCoupon()){
                    couponQueryBuilder  = QueryBuilders.rangeQuery("youhuiquan").gte(selectParam.getMinCoupon()).lte(selectParam.getMaxCoupon());
                }else {

                    if (null != selectParam.getMinCoupon()){
                        couponQueryBuilder  = QueryBuilders.rangeQuery("youhuiquan").gte(selectParam.getMinCoupon());
                    }
                    if (null != selectParam.getMaxCoupon()){
                        couponQueryBuilder  = QueryBuilders.rangeQuery("youhuiquan").lte(selectParam.getMaxCoupon());
                    }
                }
            }


            if (null != priceQueryBuilder){
                boolBuilder.must(priceQueryBuilder);
            }

            if (null != couponQueryBuilder){
                boolBuilder.must(couponQueryBuilder);
            }
            //时间只取当天的(优惠券结束时间要大于等于今天的时间)
            RangeQueryBuilder coupon_end_time = QueryBuilders.rangeQuery("coupon_end_time").gte(format.format(new Date()));
            boolBuilder.must(coupon_end_time);


            //为了分页必须添加唯一ID
            sourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.ASC));

            //分页
            sourceBuilder.from(((selectParam.getPageNo() <= 1 ? 1 : selectParam.getPageNo()) - 1) * selectParam.getPageSize());
            sourceBuilder.size(selectParam.getPageSize());

            sourceBuilder.query(boolBuilder);
            sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

            //将请求体加入到请求中 //各种组合条件
            searchRequest.source(sourceBuilder);

            System.out.println(searchRequest);
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
            pageList.setTotalPages(getTotalPages(hits.totalHits, selectParam.getPageSize()));
            pageList.setCode(ApiResponse.Status.SUCCESS.getCode());
            pageList.setMessage(ApiResponse.Status.SUCCESS.getStandardMessage());
            pageList.setFlag(true);
            String s = objectMapper.writeValueAsString(pageList);
            return s;
        }catch (Exception e){
            e.printStackTrace();
            log.error("提供淘宝天猫商品给后台管理出现异常" + e );
            StringBuilder sb = new StringBuilder();
            try {
                sb.append(objectMapper.writeValueAsString(new PageList(ApiResponse.Status.INTERNAL_SERVER_ERROR.getCode(),
                        "提供淘宝天猫商品给后台管理出现异常" + e, false)));
            }catch (Exception ee){
                ee.printStackTrace();
                log.error(ee);
            }
            return sb.toString();
        }

    }



    /**
     * 提供所有的拼多多商品给后台管理系统
     * @param selectParam
     * @return
     */
    @ApiOperation(value="提供拼多多商品给后台管理", notes="提供拼多多商品给后台管理")
    @ApiImplicitParam(name = "selectParam", value = "提供拼多多商品给后台管理需要的参数",
            required = true, dataType = "SelectParam")
    @RequestMapping(value = "pindd", method = RequestMethod.POST)
    public String getPindd(@RequestBody SelectParam selectParam) {
        List<String> list = new ArrayList<String>();
        PageList<String> pageList = new PageList<String>();

        //构造QueryBuilder
        try{
            // 1、创建search请求
            SearchRequest searchRequest = new SearchRequest(pindd_index_alias_Name);
            // 2、用SearchSourceBuilder来构造查询请求体
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();

            //类目
            QueryBuilder category = null;
            //商品搜索
            MatchQueryBuilder matchQueryBuilder = null;
            //劵后价格区间
            RangeQueryBuilder priceQueryBuilder = null;
            //优惠券区间
            RangeQueryBuilder couponQueryBuilder = null;

            if( null == selectParam ){
                return objectMapper.writeValueAsString(new PageList(ApiResponse.Status.NULL_PARAM.getCode(),
                        ApiResponse.Status.NULL_PARAM.getStandardMessage(),false));
            }

            if (StringUtils.isNotBlank(selectParam.getCategory()) && !selectParam.getCategory().trim().toLowerCase().equals("null")){
                category = QueryBuilders.termQuery("pf_cname", selectParam.getCategory());
                boolBuilder.must(category);
            }

            if (StringUtils.isNotBlank(selectParam.getTitle())){
                matchQueryBuilder = QueryBuilders.matchQuery("goods_short_name",selectParam.getTitle()).minimumShouldMatch("100%");
                boolBuilder.must(matchQueryBuilder);//.boost(5);
            }

            if (null != selectParam.getMinPrice() || null != selectParam.getMaxPrice()){
                if(null != selectParam.getMinPrice() && null != selectParam.getMaxPrice()){
                    priceQueryBuilder  = QueryBuilders.rangeQuery("price_after").gte(selectParam.getMinPrice()).lte(selectParam.getMaxPrice());
                }else {
                    System.out.println(selectParam.getMinPrice());
                    System.out.println(selectParam.getMaxPrice());
                    if (null != selectParam.getMinPrice()){
                        priceQueryBuilder  = QueryBuilders.rangeQuery("price_after").gte(selectParam.getMinPrice());
                    }
                    if (null != selectParam.getMaxPrice()){
                        priceQueryBuilder  = QueryBuilders.rangeQuery("price_after").lte(selectParam.getMaxPrice());
                    }
                }
            }

            if (null != selectParam.getMinCoupon() || null != selectParam.getMaxCoupon()){
                if(null != selectParam.getMinCoupon() && null != selectParam.getMaxCoupon()){
                    couponQueryBuilder  = QueryBuilders.rangeQuery("discount").gte(selectParam.getMinCoupon()).lte(selectParam.getMaxCoupon());
                }else {

                    if (null != selectParam.getMinCoupon()){
                        couponQueryBuilder  = QueryBuilders.rangeQuery("discount").gte(selectParam.getMinCoupon());
                    }
                    if (null != selectParam.getMaxCoupon()){
                        couponQueryBuilder  = QueryBuilders.rangeQuery("discount").lte(selectParam.getMaxCoupon());
                    }
                }
            }


            if (null != priceQueryBuilder){
                boolBuilder.must(priceQueryBuilder);
            }

            if (null != couponQueryBuilder){
                boolBuilder.must(couponQueryBuilder);
            }

            RangeQueryBuilder coupon_end_time = QueryBuilders.rangeQuery("end_time").gte(new Date().getTime()/1000);
            boolBuilder.must(coupon_end_time);

            //为了分页必须添加唯一ID
            sourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.ASC));

            //分页
            sourceBuilder.from(((selectParam.getPageNo() <= 1 ? 1 : selectParam.getPageNo()) - 1) * selectParam.getPageSize());
            sourceBuilder.size(selectParam.getPageSize());

            sourceBuilder.query(boolBuilder);
            sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

            //将请求体加入到请求中 //各种组合条件
            searchRequest.source(sourceBuilder);

            System.out.println(searchRequest);
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
            pageList.setTotalPages(getTotalPages(hits.totalHits, selectParam.getPageSize()));
            pageList.setCode(ApiResponse.Status.SUCCESS.getCode());
            pageList.setMessage(ApiResponse.Status.SUCCESS.getStandardMessage());
            pageList.setFlag(true);
            String s = objectMapper.writeValueAsString(pageList);
            return s;
        }catch (Exception e){
            e.printStackTrace();
            log.error("提供拼多多商品给后台管理出现异常" + e );
            StringBuilder sb = new StringBuilder();
            try {
                sb.append(objectMapper.writeValueAsString(new PageList(ApiResponse.Status.INTERNAL_SERVER_ERROR.getCode(),
                        "提供拼多多商品给后台管理出现异常" + e, false)));
            }catch (Exception ee){
                ee.printStackTrace();
                log.error(ee);
            }
            return sb.toString();
        }

    }


    /**
     * 提供所有的抖货商品给后台管理系统
     * @param selectParam
     * @return
     */
    @ApiOperation(value="提供抖货商品给后台管理", notes="提供抖货商品给后台管理")
    @ApiImplicitParam(name = "selectParam", value = "提供抖货商品给后台管理需要的参数",
            required = true, dataType = "SelectParam")
    @RequestMapping(value = "dou", method = RequestMethod.POST)
    public String getDou(@RequestBody SelectParam selectParam) {
        List<String> list = new ArrayList<String>();
        PageList<String> pageList = new PageList<String>();

        //构造QueryBuilder
        try{
            // 1、创建search请求
            SearchRequest searchRequest = new SearchRequest(dou_index_alias_Name);
            // 2、用SearchSourceBuilder来构造查询请求体
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
            //类目
            QueryBuilder category = null;
            //商品搜索
            MatchQueryBuilder matchQueryBuilder = null;
            //劵后价格区间
            RangeQueryBuilder priceQueryBuilder = null;
            //优惠券区间
            RangeQueryBuilder couponQueryBuilder = null;

            if( null == selectParam ){
                return objectMapper.writeValueAsString(new PageList(ApiResponse.Status.NULL_PARAM.getCode(),
                        ApiResponse.Status.NULL_PARAM.getStandardMessage(),false));
            }

            if (StringUtils.isNotBlank(selectParam.getCategory()) && !selectParam.getCategory().trim().toLowerCase().equals("null")){
                category = QueryBuilders.termQuery("fqcat", selectParam.getCategory());
                boolBuilder.must(category);
            }

            if (StringUtils.isNotBlank(selectParam.getTitle())){
                matchQueryBuilder = QueryBuilders.matchQuery("itemshorttitle",selectParam.getTitle()).minimumShouldMatch("100%");
                boolBuilder.must(matchQueryBuilder);//.boost(5);
            }

            if (null != selectParam.getMinPrice() || null != selectParam.getMaxPrice()){
                if(null != selectParam.getMinPrice() && null != selectParam.getMaxPrice()){
                    priceQueryBuilder  = QueryBuilders.rangeQuery("itemendprice").gte(selectParam.getMinPrice()).lte(selectParam.getMaxPrice());
                }else {
                    System.out.println(selectParam.getMinPrice());
                    System.out.println(selectParam.getMaxPrice());
                    if (null != selectParam.getMinPrice()){
                        priceQueryBuilder  = QueryBuilders.rangeQuery("itemendprice").gte(selectParam.getMinPrice());
                    }
                    if (null != selectParam.getMaxPrice()){
                        priceQueryBuilder  = QueryBuilders.rangeQuery("itemendprice").lte(selectParam.getMaxPrice());
                    }
                }
            }

            if (null != selectParam.getMinCoupon() || null != selectParam.getMaxCoupon()){
                if(null != selectParam.getMinCoupon() && null != selectParam.getMaxCoupon()){
                    couponQueryBuilder  = QueryBuilders.rangeQuery("couponmoney").gte(selectParam.getMinCoupon()).lte(selectParam.getMaxCoupon());
                }else {

                    if (null != selectParam.getMinCoupon()){
                        couponQueryBuilder  = QueryBuilders.rangeQuery("couponmoney").gte(selectParam.getMinCoupon());
                    }
                    if (null != selectParam.getMaxCoupon()){
                        couponQueryBuilder  = QueryBuilders.rangeQuery("couponmoney").lte(selectParam.getMaxCoupon());
                    }
                }
            }


            if (null != priceQueryBuilder){
                boolBuilder.must(priceQueryBuilder);
            }

            if (null != couponQueryBuilder){
                boolBuilder.must(couponQueryBuilder);
            }

            RangeQueryBuilder coupon_end_time = QueryBuilders.rangeQuery("couponendtime").gte(new Date().getTime()/1000);
            boolBuilder.must(coupon_end_time);

            //为了分页必须添加唯一ID
            sourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.ASC));

            //分页
            sourceBuilder.from(((selectParam.getPageNo() <= 1 ? 1 : selectParam.getPageNo()) - 1) * selectParam.getPageSize());
            sourceBuilder.size(selectParam.getPageSize());

            sourceBuilder.query(boolBuilder);
            sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

            //将请求体加入到请求中 //各种组合条件
            searchRequest.source(sourceBuilder);

            System.out.println(searchRequest);
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
            pageList.setTotalPages(getTotalPages(hits.totalHits, selectParam.getPageSize()));
            pageList.setCode(ApiResponse.Status.SUCCESS.getCode());
            pageList.setMessage(ApiResponse.Status.SUCCESS.getStandardMessage());
            pageList.setFlag(true);
            String s = objectMapper.writeValueAsString(pageList);
            return s;
        }catch (Exception e){
            e.printStackTrace();
            log.error("提供抖货商品给后台管理出现异常" + e );
            StringBuilder sb = new StringBuilder();
            try {
                sb.append(objectMapper.writeValueAsString(new PageList(ApiResponse.Status.INTERNAL_SERVER_ERROR.getCode(),
                        "提供抖货商品给后台管理出现异常" + e, false)));
            }catch (Exception ee){
                ee.printStackTrace();
                log.error(ee);
            }
            return sb.toString();
        }

    }


    /**
     * 提供所有的京东商品给后台管理系统
     * @param selectParam
     * @return
     */
    @ApiOperation(value="提供所有的京东商品给后台管理系统", notes="提供所有的京东商品给后台管理系统")
    @ApiImplicitParam(name = "selectParam", value = "提供所有的京东商品给后台管理系统",
            required = true, dataType = "SelectParam")
    @RequestMapping(value = "jd", method = RequestMethod.POST)
    public String getJD(@RequestBody SelectParam selectParam) {
        log.info("访问JD请求开始");
        List<String> list = new ArrayList<String>();
        PageList<String> pageList = new PageList<String>();

        //构造QueryBuilder
        try{
            // 1、创建search请求
            SearchRequest searchRequest = new SearchRequest(jd_index_alias_Name);
            // 2、用SearchSourceBuilder来构造查询请求体
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
            //类目
            QueryBuilder category = null;
            //商品搜索
            MatchQueryBuilder matchQueryBuilder = null;
            //劵后价格区间
            RangeQueryBuilder priceQueryBuilder = null;
            //优惠券区间
            RangeQueryBuilder couponQueryBuilder = null;

            if( null == selectParam){
                log.info("selectParam参数不为空 selectParam = " + selectParam);
                return objectMapper.writeValueAsString(new PageList(ApiResponse.Status.NULL_PARAM.getCode(),
                        ApiResponse.Status.NULL_PARAM.getStandardMessage(),false));
            }

            if (StringUtils.isNotBlank(selectParam.getCategory()) && !selectParam.getCategory().trim().toLowerCase().equals("null")){
                log.info("category 参数不为空: category = " + selectParam.getCategory());
                //类目会有两个类目：一级目录 + , + 二级目录 定位商品类目商品 如果没有二级目录就传0
                String[] split = selectParam.getCategory().split(",");
                boolBuilder.must(QueryBuilders.termQuery("pf_cid1", split[0]));
                if(!split[1].equals("0")) {
                    boolBuilder.must(QueryBuilders.termQuery("pf_cid2", split[1]));
                }
            }

            if (StringUtils.isNotBlank(selectParam.getTitle())){
                log.info("title 参数不为空：" + selectParam.getTitle());
                matchQueryBuilder = QueryBuilders.matchQuery("goods_name",selectParam.getTitle()).minimumShouldMatch("100%");
                boolBuilder.must(matchQueryBuilder);//.boost(5);
            }

            if (null != selectParam.getMinPrice() || null != selectParam.getMaxPrice()){
                if(null != selectParam.getMinPrice() && null != selectParam.getMaxPrice()){
                    log.info("minPrice 不为空 minPrice = " + selectParam.getMinPrice() + ", maxPrice 不为空 maxPrice = " + selectParam.getMaxPrice());
                    priceQueryBuilder  = QueryBuilders.rangeQuery("price_after").gte(selectParam.getMinPrice()).lte(selectParam.getMaxPrice());
                }else {
                    if (null != selectParam.getMinPrice()){
                        log.info("minPrice 不为空 minPrice = " + selectParam.getMinPrice());
                        priceQueryBuilder  = QueryBuilders.rangeQuery("price_after").gte(selectParam.getMinPrice());
                    }
                    if (null != selectParam.getMaxPrice()){
                        log.info("maxPrice 不为空 maxPrice = " + selectParam.getMaxPrice());
                        priceQueryBuilder  = QueryBuilders.rangeQuery("price_after").lte(selectParam.getMaxPrice());
                    }
                }
            }

            if (null != selectParam.getMinCoupon() || null != selectParam.getMaxCoupon()){
                if(null != selectParam.getMinCoupon() && null != selectParam.getMaxCoupon()){
                    log.info("minCoupon 不为空 minCoupon = " + selectParam.getMinCoupon() + ", maxCoupon 不为空 maxCoupon = " + selectParam.getMaxCoupon());
                    couponQueryBuilder  = QueryBuilders.rangeQuery("discount").gte(selectParam.getMinCoupon()).lte(selectParam.getMaxCoupon());
                }else {

                    if (null != selectParam.getMinCoupon()){
                        log.info("minCoupon 不为空 minCoupon = " + selectParam.getMinCoupon());
                        couponQueryBuilder  = QueryBuilders.rangeQuery("discount").gte(selectParam.getMinCoupon());
                    }
                    if (null != selectParam.getMaxCoupon()){
                        log.info("maxCoupon 不为空 maxCoupon = " + selectParam.getMaxCoupon());
                        couponQueryBuilder  = QueryBuilders.rangeQuery("discount").lte(selectParam.getMaxCoupon());
                    }
                }
            }


            if (null != priceQueryBuilder){
                boolBuilder.must(priceQueryBuilder);
            }

            if (null != couponQueryBuilder){
                boolBuilder.must(couponQueryBuilder);
            }
            //时间只取当天的(优惠券结束时间要大于等于今天的时间)
            RangeQueryBuilder coupon_end_time = QueryBuilders.rangeQuery("end_time").gte((new Date().getTime() / 1000));
            boolBuilder.must(coupon_end_time);


            //为了分页必须添加唯一ID 防止重复 分页和排序不属于bool查询体，属于query查询体
            sourceBuilder.sort(new FieldSortBuilder("goods_id").order(SortOrder.ASC));

            //分页
            sourceBuilder.from(((selectParam.getPageNo() <= 1 ? 1 : selectParam.getPageNo()) - 1) * selectParam.getPageSize());
            sourceBuilder.size(selectParam.getPageSize());
            log.info("分页： 当前页数:" + selectParam.getPageNo() + ", 每页数量 ：" + selectParam.getPageSize());
            sourceBuilder.query(boolBuilder);
            sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

            //将请求体加入到请求中 //各种组合条件
            searchRequest.source(sourceBuilder);

            log.info("打印请求体：searchRequest ：" + searchRequest);
            //3、发送请求
            SearchResponse searchResponse = esClient.search(searchRequest);
            log.info("发送请求");

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
            pageList.setTotalPages(getTotalPages(hits.totalHits, selectParam.getPageSize()));
            pageList.setCode(ApiResponse.Status.SUCCESS.getCode());
            pageList.setMessage(ApiResponse.Status.SUCCESS.getStandardMessage());
            pageList.setFlag(true);
            String s = objectMapper.writeValueAsString(pageList);
            log.info("访问结束");
            return s;
        }catch (Exception e){
            e.printStackTrace();
            log.error("提供京东商品给后台管理出现异常" + e );
            StringBuilder sb = new StringBuilder();
            try {
                sb.append(objectMapper.writeValueAsString(new PageList(ApiResponse.Status.INTERNAL_SERVER_ERROR.getCode(),
                        "提供京东商品给后台管理出现异常" + e, false)));
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
