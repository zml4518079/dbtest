package com.eagle.dao;

import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.RestHighLevelClient;

public class CollectionDataSearchDao {

    public static final String INDEX_NAME = "colletion_data_search";
    private RestHighLevelClient restClient;

    public CollectionDataSearchDao(RestHighLevelClient restClient) {
        this.restClient = restClient;
    }

    public void initStoredData(ColletionDataInsertDao insertDao) {

    }

}
