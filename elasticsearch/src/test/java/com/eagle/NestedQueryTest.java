package com.eagle;

import org.apache.http.HttpHost;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Before;

import java.io.IOException;

public class NestedQueryTest {

    private RestHighLevelClient client;
    private final String indexName = "nested_query_index_test";

    @Before
    public void setUp() {
        client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http"),
                        new HttpHost("localhost", 9201, "http"),
                        new HttpHost("localhost", 9202, "http")));
    }

    public void searchTest() {

    }

    public SearchSourceBuilder NestedBoolSearchSource() {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        BoolQueryBuilder nestedBoolQueryBuilder = QueryBuilders.boolQuery();
        NestedQueryBuilder nestedQueryBuilder = new NestedQueryBuilder("", nestedBoolQueryBuilder, ScoreMode.None);
        


        return searchSourceBuilder;
    }

    private SearchResponse searchHandler(SearchSourceBuilder searchSourceBuilder, String indexName) throws IOException {
        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.source(searchSourceBuilder);
        return client.search(searchRequest, RequestOptions.DEFAULT);
    }
}
