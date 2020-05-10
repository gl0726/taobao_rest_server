package com.shenque.utils;

import com.shenque.service.inter.ESOperations;
import org.apache.log4j.Logger;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


public class ESUtil implements ESOperations {
    private static final Logger logger = Logger.getLogger(ESUtil.class);

    private RestHighLevelClient esClient;

    public ESUtil(){}

    public ESUtil(RestHighLevelClient esClient){
        this.esClient = esClient;
    }

    public RestHighLevelClient getEsClient() {
        return esClient;
    }

    public void setEsClient(RestHighLevelClient esClient) {
        this.esClient = esClient;
    }

    /**
     * 创建索引库，需去关联索引别名
     * @param indexName 索引
     * @param index_alias_name 索引别名
     * @param type
     * @param xContentBuilder
     * @param indexShards
     * @param indexReplicas
     * @return
     */
    @Override
    public boolean createIndex(String indexName,String index_alias_name ,String type , XContentBuilder xContentBuilder,int indexShards,int indexReplicas) {
        try {
            // 如果索引存在，先进行删除
            if (isIndexExists(indexName)) {
                logger.info("Index  " + indexName + " 当天时间的索引already exits! 先进行删除");
                indexDelete(indexName,index_alias_name );
            }
            //创建索引
            CreateIndexRequest request = new CreateIndexRequest(indexName);
            request.settings(Settings.builder()
                    .put("index.number_of_shards", indexShards)
                    .put("index.number_of_replicas", indexReplicas)
            );
            request.mapping(type,xContentBuilder);

            CreateIndexResponse createIndexResponse = esClient.indices().create(request);
            if (createIndexResponse.isAcknowledged()) {
                logger.info( "create " + indexName + " index successfully！");
                return true;
            } else {
                logger.error(indexName + "Fail to create index!");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(indexName + "创建索引失败" + e);
        }
        return false;
    }


    //

    /**
     * 创建索引库 无需去关联索引别名
     * @param indexName
     * @param type
     * @param xContentBuilder
     * @param indexShards
     * @param indexReplicas
     * @return
     */
    @Override
    public boolean createIndex(String indexName,String type , XContentBuilder xContentBuilder,int indexShards,int indexReplicas) {
        try {
            // 如果索引存在，先进行删除
            if (isIndexExists(indexName)) {
                logger.info("Index  " + indexName + " 当天时间的索引already exits! 先进行删除");
                deleteIndex(indexName);
            }
            //创建索引
            CreateIndexRequest request = new CreateIndexRequest(indexName);
            request.settings(Settings.builder()
                    .put("index.number_of_shards", indexShards)
                    .put("index.number_of_replicas", indexReplicas)
            );
            request.mapping(type,xContentBuilder);

            CreateIndexResponse createIndexResponse = esClient.indices().create(request);
            if (createIndexResponse.isAcknowledged()) {
                logger.info( "create " + indexName + " index successfully！");
                return true;
            } else {
                logger.error(indexName + "Fail to create index!");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(indexName + "创建索引失败" + e);
        }
        return false;
    }

    /**
     * 判断索引是否存在
     * @param name 索引库名
     * @return true表示存在
     */
    @Override
    public boolean isIndexExists(String name) {
        boolean flag = false;
        try {
            GetIndexRequest request = new GetIndexRequest();
            request.indices(name);
            request.local(false);
            request.humanReadable(true);
            boolean exists  = esClient.indices().exists(request) ;
            if (exists) {
                flag = true;
            } else {
                flag = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("判断索引名" + name + "是否存在出现异常" + e);
        }
        return flag;
    }

    /**
     * 删除索引，在删除索引的时候也会删除索引别名中存在的索引
     * @param indexName
     * @return
     */
    @Override
    public boolean indexDelete(String indexName,String index_alias_name){
        boolean flag = false;
        try {
            DeleteIndexRequest request = new DeleteIndexRequest(indexName);
            request.indicesOptions(IndicesOptions.lenientExpandOpen());
            DeleteIndexResponse delete = esClient.indices().delete(request);

            //判断索引是否删除成功
            if (delete.isAcknowledged()) {
                //调用删除别名方法
                deleteAlias(indexName,index_alias_name);
                logger.info("删除索引" + indexName + "的同时也从索引别名" + index_alias_name +  "中剔除successfully!");
                return true;
            } else {
                logger.error("Fail to delete index " + indexName + " " + index_alias_name);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("删除索引" + indexName + "出现异常" + e);
        }
        return flag;
    }



    /**
     * 只删除索引
     * @param indexName
     * @return
     */
    @Override
    public boolean deleteIndex(String indexName){
        boolean flag = false;
        try {
            DeleteIndexRequest request = new DeleteIndexRequest(indexName);
            request.indicesOptions(IndicesOptions.lenientExpandOpen());
            DeleteIndexResponse delete = esClient.indices().delete(request);

            //判断索引是否删除成功
            if (delete.isAcknowledged()) {
                logger.info("delete index " + indexName + "  successfully!");
                return true;
            } else {
                logger.error("Fail to delete index " + indexName);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("删除索引" + indexName + "出现异常" + e);
        }
        return flag;
    }

    /**
     * 删除某个索引的别名
     * @param indexName
     */
    @Override
    public void deleteAlias(String indexName,String index_alias_name){
        try{
            Boolean exists = isExistsAlias(indexName,index_alias_name);
            //如果索引存在于这个索引别名中就从索引别名中移除
            if(exists) {
                IndicesAliasesRequest req = new IndicesAliasesRequest();
                IndicesAliasesRequest.AliasActions removeAction = new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.REMOVE)
                        .index(indexName)
                        .alias(index_alias_name);
                req.addAliasAction(removeAction);
                req.timeout("2m");
                AcknowledgedResponse indicesAliasesResponse = esClient.indices().updateAliases(req);
                if (indicesAliasesResponse.isAcknowledged()) {
                    logger.info(indexName + " 从索引别名" + index_alias_name + "中移除成功");
                } else {
                    logger.info(indexName + " 从索引别名" + index_alias_name + "中移除成功");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.error(indexName + " 从索引别名" + index_alias_name + "中移除失败" + e);
        }
    }

    /**
     * 判断某个索引名是否存在某个索引别名中
     * @param indexname 索引名
     * @param index_alias_name  索引别名
     * @return
     * @throws Exception
     */
    @Override
    public Boolean isExistsAlias(String indexname, String index_alias_name ) throws Exception{
        GetAliasesRequest ga = new GetAliasesRequest();
        ga.aliases(index_alias_name);
        ga.indices(indexname);
        return esClient.indices().existsAlias(ga);
    }


    /**
     * 关闭es客户端
     */
    @Override
    public void close(){
        try{
            if(null != esClient){
                esClient.close();
            }
            esClient = null;
            logger.info("关闭es客户端");
        }catch (Exception e){
            e.printStackTrace();
            logger.error("关闭es客户端异常");
        }
    }


    /**
     * 为某个索引添加索引别名
     * @param indexName
     * @param index_alias_name
     */
    @Override
    public boolean indexAddAlia(String indexName,String index_alias_name){
        boolean flag = false;
        try {
            IndicesAliasesRequest request = new IndicesAliasesRequest();
            IndicesAliasesRequest.AliasActions aliasAction = new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.ADD)
                    .index(indexName)
                    .alias(index_alias_name);
            request.addAliasAction(aliasAction);
            request.timeout("2m");
            AcknowledgedResponse indicesAliasesResponse = esClient.indices().updateAliases(request);
            if(indicesAliasesResponse.isAcknowledged()){
                flag = true;
                logger.info(indexName + "add Alias "  + " to " + index_alias_name + "成功");
            } else {
                logger.info(indexName + "add Alias "  + " to " + index_alias_name + "失败");
            }

        }catch (Exception e){
            e.printStackTrace();
            logger.error(indexName + "add Alias "  + " to " + index_alias_name + "异常" + e);
        }
       return flag;
    }


    /**
     * 把旧的索引名从索引别名中移除，并添加新的索引名到索引别名中
     * @param olderIndexName 旧索引名
     * @param index_alias_name   索引别名
     * @param freshIndexName 新索引名
     * @return
     */
    @Override
    public boolean removeFromAlias(String olderIndexName,String index_alias_name,String freshIndexName){
        boolean flag = false;
        try{
            IndicesAliasesRequest request = new IndicesAliasesRequest();
            IndicesAliasesRequest.AliasActions removeAction = new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.REMOVE)
                    .index(olderIndexName)
                    .alias(index_alias_name);
            request.addAliasAction(removeAction);

            //新进来的索引不存在,就把新进来的索引添加到索引别名中
            if(!isExistsAlias(freshIndexName,index_alias_name)){
                IndicesAliasesRequest.AliasActions aliasAction = new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.ADD)
                        .index(freshIndexName)
                        .alias(index_alias_name);
                request.addAliasAction(aliasAction);
            }
            request.timeout("2m");
            AcknowledgedResponse indicesAliasesResponse = esClient.indices().updateAliases(request);
            if(indicesAliasesResponse.isAcknowledged()){
                flag = true;
                logger.info("成功remove Alias " + olderIndexName + " from " + index_alias_name + " and add Alias " + freshIndexName + " to " + index_alias_name);
            } else {
                logger.info("失败remove Alias " + olderIndexName + " from " + index_alias_name + " and add Alias " + freshIndexName + " to " + index_alias_name);
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.error("异常remove Alias " + olderIndexName + " from " + index_alias_name + " and add Alias " + freshIndexName + " to " + index_alias_name + e);
        }
        return  flag;
    }


    /**
     * 批量插入
     * @param request
     * @return
     */
    @Override
    public boolean bulkInsert (BulkRequest request){
        Boolean flag = false;
        try {
            BulkResponse bulkResponse = esClient.bulk(request);
            //4、处理响应
            if(bulkResponse != null) {
                for (BulkItemResponse bulkItemResponse : bulkResponse) {
                    if (bulkItemResponse.isFailed()) {
                        BulkItemResponse.Failure failure = bulkItemResponse.getFailure();
                        continue;
                    }
                    DocWriteResponse itemResponse = bulkItemResponse.getResponse();
                    if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.INDEX
                            || bulkItemResponse.getOpType() == DocWriteRequest.OpType.CREATE) {
                        IndexResponse indexResponse = (IndexResponse) itemResponse;
                        //TODO 新增成功的处理

                    } else if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.UPDATE) {
                        UpdateResponse updateResponse = (UpdateResponse) itemResponse;
                        //TODO 修改成功的处理

                    } else if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.DELETE) {
                        DeleteResponse deleteResponse = (DeleteResponse) itemResponse;
                        //TODO 删除成功的处理
                    }
                }
            }
            flag = true;
            return flag;
        }catch (Exception e){
            e.printStackTrace();
            logger.error("批量插入异常" + e);
        }
        return  flag;
    }


    /**
     * 删除索引id
     * @param indexName
     * @param type
     * @param id
     * @return
     */
    @Override
    public boolean deleteDucId(String indexName,String type,String id){
        logger.info("============>"+Thread.currentThread().getName());
        boolean flag = false;
        try {
            DeleteRequest request = new DeleteRequest(indexName, type,id);
            request.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
            DeleteResponse deleteResponse = esClient.delete(request);
            if (deleteResponse.getResult() == DocWriteResponse.Result.NOT_FOUND) {
                logger.info("索引名：" + indexName +" id是：" +  id + "已经被删除");
                flag = true;
            }else  if (deleteResponse.getResult() == DocWriteResponse.Result.DELETED){
                logger.info("索引名：" + indexName +" id是：" +  id + "被删除");
                flag = true;
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.error("删除索引名：" + indexName +" id是：" +  id + "出现异常" + e );
        }
        return  flag;
    }
}
