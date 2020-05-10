package com.shenque.service.inter;

import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.common.xcontent.XContentBuilder;

public interface ESOperations {

    /**
     * 创建索引库，
     * @param indexName 索引
     * @param index_alias_name 索引别名
     * @param type
     * @param xContentBuilder
     * @param indexShards
     * @param indexReplicas
     * @return
     */
    public boolean createIndex(String indexName, String index_alias_name, String type, XContentBuilder xContentBuilder, int indexShards, int indexReplicas) ;


    // 创建索引库
    public boolean createIndex(String indexName, String type, XContentBuilder xContentBuilder, int indexShards, int indexReplicas) ;

    /**
     * 判断索引是否存在
     * @param name 索引库名
     * @return true表示存在
     */
    public boolean isIndexExists(String name) ;


    /**
     * 删除索引，在删除索引的时候也会删除索引别名中存在的索引
     * @param indexName
     * @return
     */
    public boolean indexDelete(String indexName, String index_alias_name);



    /**
     * 只删除索引
     * @param indexName
     * @return
     */
    public boolean deleteIndex(String indexName);

    /**
     * 删除某个索引的别名
     * @param indexName
     */
    public void deleteAlias(String indexName, String index_alias_name);

    /**
     * 判断某个索引名是否存在某个索引别名中
     * @param indexname 索引名
     * @param index_alias_name  索引别名
     * @return
     * @throws Exception
     */
    public Boolean isExistsAlias(String indexname, String index_alias_name) throws Exception;



    public void close();

    public boolean indexAddAlia(String indexName,String index_alias_name);

    public boolean removeFromAlias(String olderIndexName,String index_alias_name,String freshIndexName);

    public boolean bulkInsert (BulkRequest request);

    public boolean deleteDucId(String indexName,String type,String id);

}
