package com.eagle.dao;

import com.eagle.bean.ColletionDataBean;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.util.List;

public class ColletionDataInsertDao {
    public final static String INDEX_NAME = "collection_data_insert";

    private RestHighLevelClient restClient;

    public ColletionDataInsertDao(RestHighLevelClient restClient) {
        this.restClient = restClient;
    }

    public BulkResponse batchSubmit(List<ColletionDataBean> cdBeans) throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        for (ColletionDataBean cdBean:cdBeans) {
            bulkRequest.add(new IndexRequest(INDEX_NAME).id(cdBean.getDataId())
                    .source(cdBean.toSourceString(), XContentType.JSON));
        }

        return restClient.bulk(bulkRequest, RequestOptions.DEFAULT);
    }
}
