package com.eagle;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

import static com.eagle.QueryUtils.QUERY_INDEX;

public class PerformanceTest {

    private RestHighLevelClient client;
    private BulkProcessor bulkProcessor;

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

            QueryUtils queryUtils = new QueryUtils();
            SearchRequest searchSourceBuilder = null;

            searchSourceBuilder = queryUtils.queryByModelIdOrderBycdDate("AppleMac", 15);
            System.out.println(QueryUtils.search(client, searchSourceBuilder,
                    "queryByModelIdOrderBycdDate"));
            System.out.println("=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+");
            searchSourceBuilder = queryUtils.queryByModelIdAndcdDateRangeOrderBycdDate("ThinkPad",
                    new Date(1577603996000L), new Date(1577604000000L));
            System.out.println(QueryUtils.search(client, searchSourceBuilder,
                    "queryByModelIdAndcdDateRangeOrderBycdDate"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void concurrenceTest() {
        QueryUtils queryUtils = new QueryUtils();
        int taskNumber = 500;
        queryUtils.concurrentuery(client, taskNumber, "IdealPad", 15);
        System.out.println("=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+");
        queryUtils.concurrentuery(client, taskNumber, "AppleMac", 15);
    }
}
