package com.eagle;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;


public class InsertQueryPerformanceTest {

    private final static String QUERY_MEASUREMENT = "collection_data_query";

    private final static String QUERY_DATABASE = "eagle";

    private InfluxDB influxDB;

    @Before
    public void setUp() {
        this.influxDB = InfluxDBFactory.connect("http://" + TestUtils.getInfluxIP() + ":" + TestUtils.getInfluxPORT(true), "root", "root");
        this.influxDB.setLogLevel(InfluxDB.LogLevel.NONE);
    }

    @Test
    public void insertPerformance() {
        influxDB.query(new Query("DROP DATABASE " + QUERY_DATABASE));
        influxDB.query(new Query("CREATE DATABASE " + QUERY_DATABASE));
        String rpName = "aRetentionPolicy";
        influxDB.query(new Query("CREATE RETENTION POLICY " + rpName + " ON " + QUERY_DATABASE + " DURATION 4320h REPLICATION 2 DEFAULT"));
        influxDB.enableBatch(10000, 100, TimeUnit.MILLISECONDS);

        DataUtils.insertDataByModelId(influxDB, "ThinkPad", QUERY_DATABASE, rpName, QUERY_MEASUREMENT);
        DataUtils.insertDataByModelId(influxDB, "IdealPad", QUERY_DATABASE, rpName, QUERY_MEASUREMENT);
        DataUtils.insertDataByModelId(influxDB, "Apple Mac", QUERY_DATABASE, rpName, QUERY_MEASUREMENT);
    }

    @Test
    public void queryPerformance() {
        String queryStr = "SELECT * FROM " + QUERY_MEASUREMENT + " WHERE model_id = 'ThinkPad' and time > '2019-12-28T14:00:00Z' ORDER BY time DESC";
//        String queryStr = "SELECT * FROM " + QUERY_MEASUREMENT;
        long start = System.currentTimeMillis();
        QueryResult queryResult = influxDB.query(new Query(queryStr, QUERY_DATABASE));
        long elapsedForBatchWrite = System.currentTimeMillis() - start;
        System.out.println("performance(ms):query data by time:" + elapsedForBatchWrite);
        System.out.println("query result:" + queryResult);
    }
}
