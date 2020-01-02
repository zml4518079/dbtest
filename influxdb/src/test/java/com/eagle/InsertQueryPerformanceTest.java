package com.eagle;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.eagle.QueryUtils.QUERY_DATABASE;
import static com.eagle.QueryUtils.QUERY_MEASUREMENT;


public class InsertQueryPerformanceTest {

    private InfluxDB influxDB;

    @Before
    public void setUp() {
        this.influxDB = InfluxDBFactory.connect(
                "http://" + TestUtils.getInfluxIP() + ":" + TestUtils.getInfluxPORT(true),
                "root", "root");
        this.influxDB.setLogLevel(InfluxDB.LogLevel.NONE);
    }

    @Test
    public void insertPerformance() {
        influxDB.query(new Query("DROP DATABASE " + QUERY_DATABASE));
        influxDB.query(new Query("CREATE DATABASE " + QUERY_DATABASE));
        String rpName = "aRetentionPolicy";
        influxDB.query(new Query("CREATE RETENTION POLICY " + rpName + " ON " +
                QUERY_DATABASE + " DURATION 4320h REPLICATION 2 DEFAULT"));
        influxDB.enableBatch(10000, 100, TimeUnit.MILLISECONDS);

        DataUtils.insertDataByModelId(influxDB, "ThinkPad", QUERY_DATABASE, rpName, QUERY_MEASUREMENT);
        DataUtils.insertDataByModelId(influxDB, "IdealPad", QUERY_DATABASE, rpName, QUERY_MEASUREMENT);
        DataUtils.insertDataByModelId(influxDB, "Apple Mac", QUERY_DATABASE, rpName, QUERY_MEASUREMENT);

        DataUtils.insertDataByModelId(influxDB, "ABC", QUERY_DATABASE, rpName, QUERY_MEASUREMENT);
        DataUtils.insertDataByModelId(influxDB, "BBC", QUERY_DATABASE, rpName, QUERY_MEASUREMENT);
        DataUtils.insertDataByModelId(influxDB, "IBM", QUERY_DATABASE, rpName, QUERY_MEASUREMENT);
    }

    @Test
    public void queryPerformance() {
        QueryUtils queryUtils = new QueryUtils();
        Query query = null;
        query = queryUtils.queryByModelIdOrderBycdDate("ABC", 15);
        System.out.println("query result:" +
                QueryUtils.search(influxDB, query, "queryByModelIdOrderBycdDate"));
        System.out.println("=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+");
        query = queryUtils.queryByModelIdAndcdDateRangeOrderBycdDate("ABC",
                new Date(1577593996000L), new Date(1577594000000L));
        System.out.println("query result:" +
                QueryUtils.search(influxDB, query, "queryByModelIdAndcdDateRangeOrderBycdDate"));
    }

    @Test
    public void concurrenceTest() {
        QueryUtils queryUtils = new QueryUtils();
        int taskNumber = 10;
        queryUtils.concurrentuery(influxDB, taskNumber, "AppleMac", 15);
        System.out.println("=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+");
        queryUtils.concurrentuery(influxDB, taskNumber, "AppleMac", 15);
    }
}
