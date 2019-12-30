package com.eagle;

import com.eagle.bean.ColletionDataBean;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataUtils {

    private final String modelId;
    private final String groupId;
    private final String indexId;
    private final long period;

    public DataUtils(String modelId, String groupId, String indexId, long period) {
        this.modelId = modelId;
        this.groupId = groupId;
        this.indexId = indexId;
        this.period = period;
    }

    private double randomNum() {
        double a = Math.random();
        a *= 90;
        a += 10;
        return a;
    }

    public ColletionDataBean createCDBean(Date cdDate) {
        return new ColletionDataBean(modelId, groupId, indexId, cdDate, randomNum(), randomNum());
    }

    public List<ColletionDataBean> createCDBeans(int number, Long startTime) {
        List<ColletionDataBean> beans = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            Date time = new Date(startTime - period * i);
            beans.add(createCDBean(time));
        }
        return beans;
    }

    public long getPeriod() {
        return period;
    }

    public static void insertDataByModelIdWithClient(RestHighLevelClient client, String modelId, String indexName) throws IOException {
        long startTime = 1577604000000L;
        final int insertNum =1000;
        final int periodTime = 1 * 1000;
        DataUtils dataUtils = new DataUtils(modelId,"1","1", periodTime);
        long runningTime = 0;
        for (int i = 0; i < 100; i++) {
            startTime = startTime - (insertNum * periodTime) * i;
            List<ColletionDataBean> dataBeans = dataUtils.createCDBeans(insertNum, startTime);
            BulkRequest request = new BulkRequest();
            for (ColletionDataBean dataBean:dataBeans) {
                request.add(new IndexRequest(indexName).id(dataBean.getDataId()).source(dataBean.toSourceString(),
                        XContentType.JSON));
            }

            long start = System.currentTimeMillis();
            client.bulk(request, RequestOptions.DEFAULT);
            runningTime += (System.currentTimeMillis() - start);
        }

        System.out.println("insert with client running time[ms]:" + runningTime);
    }

    public static void insertDataByModelIdWithBulkProcessor(BulkProcessor bulkProcessor, String modelId, String indexName) throws IOException {
        long startTime = 1577604000000L;
        final int insertNum =1000;
        final int periodTime = 1 * 1000;
        DataUtils dataUtils = new DataUtils(modelId,"1","1", periodTime);
        long runningTime = 0;
        for (int i = 0; i < 100; i++) {
            startTime = startTime - (insertNum * periodTime) * i;
            List<ColletionDataBean> dataBeans = dataUtils.createCDBeans(insertNum, startTime);

            long start = System.currentTimeMillis();
            for (ColletionDataBean dataBean:dataBeans) {
                bulkProcessor.add(new IndexRequest(indexName).id(dataBean.getDataId()).source(dataBean.toSourceString(),
                        XContentType.JSON));
            }
            runningTime += (System.currentTimeMillis() - start);
        }

        System.out.println("insert with bulk processor running time[ms]:" + runningTime);
    }
}
