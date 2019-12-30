package com.eagle;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class PerformanceTest {

    private RestHighLevelClient client;
    private BulkProcessor bulkProcessor;

    private final static String QUERY_INDEX = "collection_data_query";

    @Before
    public void setUp() {
        client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http"),
                        new HttpHost("localhost", 9201, "http"),
                        new HttpHost("localhost", 9202, "http")));

        BulkProcessor.Listener listener = new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {

            }

            @Override
            public void afterBulk(long executionId, BulkRequest request,
                                  BulkResponse response) {

            }

            @Override
            public void afterBulk(long executionId, BulkRequest request,
                                  Throwable failure) {
                failure.printStackTrace();
            }
        };

        bulkProcessor = BulkProcessor.builder(
                (request, bulkListener) ->
                        client.bulkAsync(request, RequestOptions.DEFAULT, bulkListener),
                listener).build();
    }

    @Test
    public void insertPerformance() {
        try {
            GetIndexRequest request = new GetIndexRequest(QUERY_INDEX);
            boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
            if (exists) {
                client.indices().delete(new DeleteIndexRequest(QUERY_INDEX), RequestOptions.DEFAULT);
            } else {
                System.out.println("index " + QUERY_INDEX + " doesn't exist");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            DataUtils.insertDataByModelIdWithClient(client, "ThinkPad", QUERY_INDEX);
            DataUtils.insertDataByModelIdWithClient(client, "IdealPad", QUERY_INDEX);
            DataUtils.insertDataByModelIdWithClient(client, "AppleMac", QUERY_INDEX);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            DataUtils.insertDataByModelIdWithBulkProcessor(bulkProcessor, "IBM", QUERY_INDEX);
            DataUtils.insertDataByModelIdWithBulkProcessor(bulkProcessor, "ABC", QUERY_INDEX);
            DataUtils.insertDataByModelIdWithBulkProcessor(bulkProcessor, "BBC", QUERY_INDEX);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void queryPerformance() {
        try {
//            SearchResponse searchResponse = client.search(new SearchRequest(QUERY_INDEX), RequestOptions.DEFAULT);
//            System.out.println(searchResponse);

            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
//            sourceBuilder.query(QueryBuilders.termQuery("modelId", "AppleMac"));
//            sourceBuilder.query(QueryBuilders.rangeQuery("cdDate").gte(1577512800000L));
//            sourceBuilder.sort("cdDate", SortOrder.DESC);
            sourceBuilder.size(20);
            SearchRequest searchRequest = new SearchRequest(QUERY_INDEX);
            searchRequest.source(sourceBuilder);

            long start = System.currentTimeMillis();
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            long elapsedForBatchWrite = System.currentTimeMillis() - start;
            System.out.println("performance(ms):query data by time:" + elapsedForBatchWrite);
            System.out.println(searchResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
