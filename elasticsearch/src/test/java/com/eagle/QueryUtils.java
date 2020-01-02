package com.eagle;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

public class QueryUtils {

    public final static String QUERY_INDEX = "collection_data_query";

    public SearchRequest queryByModelIdOrderBycdDate(String modelId, int size) {
        SearchRequest request = new SearchRequest(QUERY_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.postFilter(QueryBuilders.matchQuery("modelId", modelId));
        searchSourceBuilder.sort("cdDate", SortOrder.DESC);
        searchSourceBuilder.size(size);
        System.out.println(searchSourceBuilder);
        request.source(searchSourceBuilder);

        return request;
    }

    public SearchRequest queryByModelIdAndcdDateRangeOrderBycdDate(String modelId, Date from, Date to) {
        SearchRequest request = new SearchRequest(QUERY_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.postFilter(QueryBuilders.matchQuery("modelId", modelId));
        searchSourceBuilder.query(QueryBuilders.rangeQuery("cdDate").gte(from.getTime()).lt(to.getTime()));
        searchSourceBuilder.sort("cdDate", SortOrder.DESC);
        System.out.println(searchSourceBuilder);
        request.source(searchSourceBuilder);

        return request;
    }

    public static SearchResponse search(RestHighLevelClient client, SearchRequest request, String queryTag) throws IOException {
        long start = System.currentTimeMillis();
        SearchResponse searchResponse = client.search(request, RequestOptions.DEFAULT);
        long elapsedForBatchWrite = System.currentTimeMillis() - start;
        System.out.println("performance(ms) of " + queryTag + " :query data by time:" + elapsedForBatchWrite);
        return searchResponse;
    }

    public void concurrentuery(RestHighLevelClient client, int taskNumber, String modelId, int size) {
        ExecutorService executor = Executors.newCachedThreadPool();
        CountDownLatch countDownLatch = new CountDownLatch(taskNumber);
        List<Future<SearchResponse>> futures = new ArrayList<>();

        for (int i = 0; i < taskNumber; i++){
            futures.add(executor.submit(new QueryTask(client, countDownLatch, modelId, size)));
            countDownLatch.countDown();
        }

        long start = System.currentTimeMillis();
        executor.shutdown();
        while (!executor.isTerminated()){
            Thread.yield();
        }
        long elapsedForBatchWrite = System.currentTimeMillis() - start;
        System.out.println("performance(ms) :query data by time:" + elapsedForBatchWrite);
    }

    private class QueryTask implements Callable<SearchResponse> {

        private final RestHighLevelClient client;
        private final CountDownLatch countDownLatch;
        private final String modelId;
        private final int size;

        private QueryTask(RestHighLevelClient client, CountDownLatch countDownLatch, String modelId, int size) {
            this.client = client;
            this.countDownLatch = countDownLatch;
            this.modelId = modelId;
            this.size = size;
        }

        @Override
        public SearchResponse call() throws Exception {
            try {
                SearchRequest searchSourceBuilder = QueryUtils.this
                        .queryByModelIdOrderBycdDate(modelId, size);
                countDownLatch.await();
                return QueryUtils.search(client, searchSourceBuilder, "queryByModelIdOrderBycdDate");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
