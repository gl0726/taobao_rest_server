package com.shenque.control.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shenque.model.*;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * xiao.py
 */
@RestController
@RequestMapping("/es")
@Api(tags = "es商品接口", description = "es提供商品优惠券区间范围的Rest API")
public class ESTaoBaoRestServer {

    private static final Logger log = Logger.getLogger(ESTaoBaoRestServer.class);

    private static final String index_alias_Name = "taobao";

    @Autowired
    private RestHighLevelClient esClient;

    @Autowired
    ObjectMapper objectMapper;


    @ApiOperation(value="获取淘宝天猫优惠券范围", notes="根据传递的优惠券范围参数提供商品信息")
    @ApiImplicitParam(name = "page", value = "淘宝天猫优惠券区间范围参数实体对象page",
            required = true, dataType = "Page")
    @RequestMapping(value = "restpara", method = RequestMethod.POST)
    public String getCommdityInfo(@RequestBody Page page) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        List<String> list = new ArrayList<String>();
        PageList<String> pageList = new PageList<String>();

        log.info("第" + page.getCurrentPage() + "页每页" + page.getPageSize());
        //构造QueryBuilder
        RangeQueryBuilder rangeQueryBuilder = null;
        try{
            if( null == page ){
                return objectMapper.writeValueAsString(new PageList(ApiResponse.Status.NULL_PAGE.getCode(),
                        ApiResponse.Status.NULL_PAGE.getStandardMessage(),false));
            }

            if (null == page.getCurrentPage() || null == page.getPageSize()){
                return objectMapper.writeValueAsString(new PageList(ApiResponse.Status.NOT_PAGE_PARAM.getCode(),
                        ApiResponse.Status.NOT_PAGE_PARAM.getStandardMessage(),false));
            }
            if (StringUtils.isBlank(page.getCouponRange())){
                return objectMapper.writeValueAsString(new PageList(ApiResponse.Status.NOT_RANGE_PARAM.getCode(),
                        ApiResponse.Status.NOT_RANGE_PARAM.getStandardMessage(),false));
            }

            // 1、创建search请求
            SearchRequest searchRequest = new SearchRequest(index_alias_Name);

            // 2、用SearchSourceBuilder来构造查询请求体 ,请仔细查看它的方法，构造各种查询的方法都在这。
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();


            if (page.getCouponRange().equals(Constant.TaobaoRange.MORE_THAN_500.getStandardMessage())){
                rangeQueryBuilder   = QueryBuilders.rangeQuery("youhuiquan").gte(500);

            }else if(page.getCouponRange().equals(Constant.TaobaoRange.RANGE_500_300.getStandardMessage())){
                rangeQueryBuilder  = QueryBuilders.rangeQuery("youhuiquan").gte(300).lt(500);
                sourceBuilder.sort(new FieldSortBuilder("volume").order(SortOrder.DESC));
            }else if(page.getCouponRange().equals(Constant.TaobaoRange.RANGE_300_200.getStandardMessage())){
                rangeQueryBuilder  = QueryBuilders.rangeQuery("youhuiquan").gte(200).lt(300);
                sourceBuilder.sort(new FieldSortBuilder("volume").order(SortOrder.DESC));
            }else if(page.getCouponRange().equals(Constant.TaobaoRange.RANGE_200_100.getStandardMessage())){
                rangeQueryBuilder  = QueryBuilders.rangeQuery("youhuiquan").gte(100).lt(200);
                sourceBuilder.sort(new FieldSortBuilder("volume").order(SortOrder.DESC));
            }else if(page.getCouponRange().equals(Constant.TaobaoRange.RANGE_100_1.getStandardMessage())){
                rangeQueryBuilder  = QueryBuilders.rangeQuery("youhuiquan").gte(1).lt(100);
                sourceBuilder.sort(new FieldSortBuilder("volume").order(SortOrder.DESC));
            }
            BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
            RangeQueryBuilder coupon_end_time = QueryBuilders.rangeQuery("coupon_end_time").gte(format.format(new Date()));
            boolBuilder.must(coupon_end_time);

            boolBuilder.must(rangeQueryBuilder);



            sourceBuilder.from(((page.getCurrentPage() <= 1 ? 1 : page.getCurrentPage()) - 1) * page.getPageSize());
            sourceBuilder.size(page.getPageSize());

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
            pageList.setTotalPages(getTotalPages(hits.totalHits, page.getPageSize()));
            pageList.setCode(ApiResponse.Status.SUCCESS.getCode());
            pageList.setMessage(ApiResponse.Status.SUCCESS.getStandardMessage());
            pageList.setFlag(true);
            String s = objectMapper.writeValueAsString(pageList);
            return s;
        }catch (Exception e){
            e.printStackTrace();
            log.error("根据传递的优惠券范围参数提供商品信息出现异常" + e );
            StringBuilder sb = new StringBuilder();
            try {
                sb.append(objectMapper.writeValueAsString(new PageList(ApiResponse.Status.INTERNAL_SERVER_ERROR.getCode(),
                        "根据传递的优惠券范围参数提供商品信息出现异常" + e, false)));
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
