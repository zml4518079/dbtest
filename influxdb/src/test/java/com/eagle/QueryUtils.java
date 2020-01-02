package com.eagle;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

import static com.eagle.DataUtils.TIME_FORMAT_PATTERN;

public class QueryUtils {

    public final static String QUERY_MEASUREMENT = "collection_data_query";
    public final static String QUERY_DATABASE = "eagle";

    public Query queryByModelIdOrderBycdDate(String modelId, int size) {
        String queryStr = "SELECT * FROM " + QUERY_MEASUREMENT + " WHERE model_id = '" + modelId + "' ORDER BY time DESC LIMIT " + size;
        return new Query(queryStr, QUERY_DATABASE);
    }

    public Query queryByModelIdAndcdDateRangeOrderBycdDate(String modelId, Date from, Date to) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TIME_FORMAT_PATTERN);
        String queryStr = "SELECT * FROM " + QUERY_MEASUREMENT +
                " WHERE " +
                    "model_id = '" + modelId + "' " +
                    "and time >= '" + simpleDateFormat.format(from) + "' " +
                    "and time < '" + simpleDateFormat.format(to) + "' " +
                "ORDER BY time DESC";
        return new Query(queryStr, QUERY_DATABASE);
    }

    public static QueryResult search(InfluxDB influxDB, Query query, String queryTag) {
        long start = System.currentTimeMillis();
        QueryResult queryResult = influxDB.query(query);
        long elapsedForBatchWrite = System.currentTimeMillis() - start;
        System.out.println("performance(ms) of " + queryTag + " :query data by time:" + elapsedForBatchWrite);
        return queryResult;
    }

    public void concurrentuery(InfluxDB influxDB, int taskNumber, String modelId, int size) {
        ExecutorService executor = Executors.newCachedThreadPool();
        CountDownLatch countDownLatch = new CountDownLatch(taskNumber);
        List<Future<QueryResult>> futures = new ArrayList<>();

        for (int i = 0; i < taskNumber; i++){
            futures.add(executor.submit(new QueryTask(influxDB, countDownLatch, modelId, size)));
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

    private class QueryTask implements Callable<QueryResult> {

        private final InfluxDB influxDB;
        private final CountDownLatch countDownLatch;
        private final String modelId;
        private final int size;

        private QueryTask(InfluxDB influxDB, CountDownLatch countDownLatch, String modelId, int size) {
            this.influxDB = influxDB;
            this.countDownLatch = countDownLatch;
            this.modelId = modelId;
            this.size = size;
        }

        @Override
        public QueryResult call() throws Exception {
            Query query = QueryUtils.this
                    .queryByModelIdOrderBycdDate(modelId, size);
            countDownLatch.await();
            return QueryUtils.search(influxDB, query, "queryByModelIdOrderBycdDate");
        }
    }
}
