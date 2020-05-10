package com.shenque.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shenque.utils.ESUtil;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * xiao.py
 */
@Configuration
public class ElasticSearchConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchConfig.class);
    private static final int ADDRESS_LENGTH = 2;
    private static final String HTTP_SCHEME = "http";

    /**
     * 使用冒号隔开ip和端口
     */
    @Value("${elasticsearch.host}")
    private String ipAddress;

    @Value("${elasticsearch.port}")
    private String port;


    @Bean
    public RestHighLevelClient getRestHighLevelClient() {
        LOGGER.info("Elasticsearch初始化开始。。。。。");

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("elastic", "Elastic123&"));
        LOGGER.info("连接地址：" + ipAddress + " 端口为：" + port);
        RestClientBuilder builder = RestClient.builder(new HttpHost(ipAddress, Integer.valueOf(port)));

        //设置连接超时
        builder.setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
            @Override
            public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
                return requestConfigBuilder.setConnectTimeout(5000).
                        setSocketTimeout(40000).
                        setConnectionRequestTimeout(1000);
            }
        });
        builder.setMaxRetryTimeoutMillis(5 * 60 * 1000);

        builder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
            @Override
            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                //设置用户信息
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                //设置线程数
                return httpClientBuilder.setDefaultIOReactorConfig(
                        IOReactorConfig.custom().setIoThreadCount(50).build());
            }
        });

        RestHighLevelClient client = new RestHighLevelClient(builder);
        LOGGER.info("Elasticsearch初始化完成。。。。。");
        return client;

    }

    @Bean
    public ESUtil getESUtil(){
        return new ESUtil(getRestHighLevelClient());
    }

    @Bean
    public ObjectMapper getObjectMapper() {

        return new ObjectMapper();
    }
}

