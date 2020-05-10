package com.shenque.control;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shenque.model.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
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
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * xiao.py
 * 淘宝天猫后台管理人工精选商品选择
 */
@RestController
@RequestMapping("/es")
@Api(tags = "es推荐商品选品接口", description = "es提供商品推荐选品Rest API")
public class ESRecommendGoods {

    private static final Logger log = Logger.getLogger(ESRecommendGoods.class);

    private static final String index_alias_Name = "recommend";

    private static final String TYPE = "recommend_goods";

    @Autowired
    private RestHighLevelClient esClient;

    @Autowired
    ObjectMapper objectMapper;


    /**
     * 提供所有的淘宝天猫商品给后台管理系统
     * @param queryParam
     * @return
     */
    @ApiOperation(value="获取淘宝天猫商品作为选品推荐", notes="获取淘宝天猫商品作为选品推荐")
    @ApiImplicitParam(name = "queryParam", value = "传递淘宝天猫选品的优惠券区间范围参数对象thePage",
            required = true, dataType = "QueryParam")
    @RequestMapping(value = "getCommdity", method = RequestMethod.POST)
    public String getCommdityInfo(@RequestBody QueryParam queryParam) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        List<String> list = new ArrayList<String>();
        PageList<String> pageList = new PageList<String>();

        log.info("选品第" + queryParam.getPageNo() + "页每页" + queryParam.getPageSize());
        //构造QueryBuilder
        try{
            if( null == queryParam ){
                return objectMapper.writeValueAsString(new PageList(ApiResponse.Status.NULL_PARAM.getCode(),
                        ApiResponse.Status.NULL_PARAM.getStandardMessage(),false));
            }

            if (null == queryParam.getPageNo() || null == queryParam.getPageSize()){
                return objectMapper.writeValueAsString(new PageList(ApiResponse.Status.NOT_PAGE_PARAM.getCode(),
                        ApiResponse.Status.NOT_PARAM.getStandardMessage(),false));
            }
            if (StringUtils.isBlank(queryParam.getProductSearch()) && null == queryParam.getSortId() ){
                return objectMapper.writeValueAsString(new PageList(ApiResponse.Status.NOT_QUERY_PARAM.getCode(),
                        ApiResponse.Status.NOT_QUERY_PARAM.getStandardMessage(),false));
            }

            // 1、创建search请求
            SearchRequest searchRequest = new SearchRequest(index_alias_Name);

            // 2、用SearchSourceBuilder来构造查询请求体 ,请仔细查看它的方法，构造各种查询的方法都在这。
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

            BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();

            //商品搜索
            MatchQueryBuilder matchQueryBuilder = null;

            sourceBuilder.sort(new FieldSortBuilder("recommend").order(SortOrder.DESC));

            if (StringUtils.isNotBlank(queryParam.getProductSearch())){
                matchQueryBuilder = QueryBuilders.matchQuery("title", queryParam.getProductSearch()).minimumShouldMatch("100%");
                boolBuilder.must(matchQueryBuilder).boost(5);
            }

            //排序规则
            if (queryParam.getSortId() == Constant.SortRange.YOU_HUI_JUAN_BI_SORT.getSortId()){
                sourceBuilder.sort(new FieldSortBuilder("youhuiquanbilv").order(SortOrder.DESC));

            }else if(queryParam.getSortId() == Constant.SortRange.SALE_PRIVE_SORT.getSortId()){
                sourceBuilder.sort(new FieldSortBuilder("volume").order(SortOrder.DESC));

            }else if(queryParam.getSortId() == Constant.SortRange.PRIVE_SORT.getSortId()){
                sourceBuilder.sort(new FieldSortBuilder("zk_final_price").order(SortOrder.ASC));
            }
            sourceBuilder.sort(new FieldSortBuilder("update_time").order(SortOrder.DESC));
            //时间只取当天的
            RangeQueryBuilder coupon_end_time = QueryBuilders.rangeQuery("coupon_end_time").gte(format.format(new Date()));
            boolBuilder.must(coupon_end_time);


            //为了分页必须添加唯一ID
            sourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.ASC));

            //分页
            sourceBuilder.from(((queryParam.getPageNo() <= 1 ? 1 : queryParam.getPageNo()) - 1) * queryParam.getPageSize());
            sourceBuilder.size(queryParam.getPageSize());

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
            pageList.setTotalPages(getTotalPages(hits.totalHits, queryParam.getPageSize()));
            pageList.setCode(ApiResponse.Status.SUCCESS.getCode());
            pageList.setMessage(ApiResponse.Status.SUCCESS.getStandardMessage());
            pageList.setFlag(true);
            String s = objectMapper.writeValueAsString(pageList);
            return s;
        }catch (Exception e){
            e.printStackTrace();
            log.error("获取淘宝天猫商品作为选品推荐出现异常" + e );
            StringBuilder sb = new StringBuilder();
            try {
                sb.append(objectMapper.writeValueAsString(new PageList(ApiResponse.Status.INTERNAL_SERVER_ERROR.getCode(),
                        "获取淘宝天猫商品作为选品推荐出现异常" + e, false)));
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
     * 淘宝天猫后台管理选品后对ES中的商品进行更新
     * @param updateParam
     * @return
     */
   @ApiOperation(value="选品某个淘宝天猫商品", notes="选品某个淘宝天猫商品")
    @ApiImplicitParam(name = "updateParam", value = "传递需要选品选品某个淘宝天猫商品参数",
            required = true, dataType = "UpdateParam")
    @RequestMapping(value = "updateCommdity", method = RequestMethod.POST)
    public String updateCommdityInfo(@RequestBody UpdateParam updateParam) {
       PageList pageList= new PageList();
       List<String> idlist = new ArrayList<>();

       try{
           //如果参数传递为空
           if (StringUtils.isBlank(updateParam.getIdlist())){
               return objectMapper.writeValueAsString(new PageList(ApiResponse.Status.NULL_PARAM.getCode(),
                       ApiResponse.Status.NULL_PARAM.getStandardMessage(),false));
           }

           String[] split = updateParam.getIdlist().split(",");

           for (String idCode : split){
               String[] id = idCode.split(":");
               UpdateRequest request = new UpdateRequest(index_alias_Name, TYPE, id[0]);
               Map<String, Object> jsonMap = new HashMap<>();
               jsonMap.put("update_time", new Date().getTime());
               jsonMap.put("recommend", Integer.valueOf(id[1]));
               UpdateRequest doc = request.doc(jsonMap);

               UpdateResponse update = esClient.update(doc);
               if(update.getResult() == DocWriteResponse.Result.UPDATED){
                   idlist.add(id[0]);
               }
           }
           if (idlist.size() == split.length){
               pageList.setMessage(ApiResponse.Status.SUCCESS.getStandardMessage());
               pageList.setCode(ApiResponse.Status.SUCCESS.getCode());
           }else {
               pageList.setMessage("批量更新只完成了" + idlist.size());
               pageList.setCode(500);
           }

           pageList.setData(idlist);
           pageList.setFlag(true);
           return objectMapper.writeValueAsString(pageList);
        }catch (Exception e){
            e.printStackTrace();
            log.error("淘宝天猫商品" + updateParam.getIdlist() + " 选品出现异常" + e );
            StringBuilder sb = new StringBuilder();
            try {
                sb.append(objectMapper.writeValueAsString(new PageList(ApiResponse.Status.INTERNAL_SERVER_ERROR.getCode(),
                        "淘宝天猫商品" + updateParam.getIdlist() + " 选品出现异常" + e, false)));
            }catch (Exception ee){
                ee.printStackTrace();
                log.error(ee);
            }
            return sb.toString();

        }


    }

}
