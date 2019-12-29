package com.eagle;

import com.eagle.point.ColletionDataBean;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RunWith(JUnitPlatform.class)
public class PerformanceTest {
    private InfluxDB influxDB;

    private final static String INSERT_MEASUREMENT ="collection_data_insert";
    private final static String QUERY_MEASUREMENT = "collection_data_query";

    private final static String QUERY_DATABASE = "eagle";

    private final static int UDP_PORT = 8089;
    private final static String UDP_DATABASE = "udp";

    @BeforeEach
    public void setUp() {
        this.influxDB = InfluxDBFactory.connect("http://" + TestUtils.getInfluxIP() + ":" + TestUtils.getInfluxPORT(true), "root", "root");
        this.influxDB.setLogLevel(InfluxDB.LogLevel.NONE);
        this.influxDB.query(new Query("CREATE DATABASE " + UDP_DATABASE));

        this.influxDB.query(new Query("DROP DATABASE " + QUERY_DATABASE));
        this.influxDB.query(new Query("CREATE DATABASE " + QUERY_DATABASE));
        this.influxDB.enableBatch(10000, 100, TimeUnit.MILLISECONDS);
        // 构造数据
        long startTime = 1577604000000L;
        final int insertNum = 200;
        final int periodTime = 30 * 1000;
        DataUtils dataUtils = new DataUtils("thinkpad","1","1", periodTime);
        for (int i = 0; i < 10; i++) {
            startTime = startTime - (insertNum * periodTime) * i;
            List<ColletionDataBean> dataBeans = dataUtils.createCDBeans(insertNum, startTime);
            List<String> lineProtocols = new ArrayList<String>();
            for (ColletionDataBean dataBean:dataBeans) {
                Point point = Point.measurement(QUERY_MEASUREMENT).addFieldsFromPOJO(dataBean).build();
                lineProtocols.add(point.lineProtocol());
            }

            this.influxDB.write(UDP_PORT, lineProtocols);
        }
    }

    /**
     * delete UDP database after all tests end.
     */
    @AfterEach
    public void cleanup(){
        this.influxDB.query(new Query("CREATE DATABASE " + UDP_DATABASE));
//        influxDB.query(new Query("DROP DATABASE " + QUERY_DATABASE));
    }

    @Test
    public void butchInsertPerformance() {
        long startTime = 1577604000000L;
        DataUtils dataUtils = new DataUtils("thinkpad","1","1", 60*1000);
        List<ColletionDataBean> dataBeans = dataUtils.createCDBeans(500, startTime);
        List<String> lineProtocols = new ArrayList<String>();
        for (ColletionDataBean dataBean:dataBeans) {
            Point point = Point.measurement(INSERT_MEASUREMENT).addFieldsFromPOJO(dataBean).build();
            lineProtocols.add(point.lineProtocol());
        }

        String dbName = "write_compare_udp_" + System.currentTimeMillis();
        this.influxDB.query(new Query("CREATE DATABASE " + dbName));
        this.influxDB.enableBatch(10000, 100, TimeUnit.MILLISECONDS);

        int repetitions = 15;
        long start = System.currentTimeMillis();
        for (int i = 0; i < repetitions; i++) {
            //write batch of 2000 single string.
            this.influxDB.write(UDP_PORT, lineProtocols);
        }
        long elapsedForBatchWrite = System.currentTimeMillis() - start;
        System.out.println("performance(ms):write udp with batch of 1000 string:" + elapsedForBatchWrite);

        this.influxDB.query(new Query("DROP DATABASE " + dbName));
    }

    @Test
    public void queryPerformance() {
//        String queryStr = "SELECT * FROM " + QUERY_MEASUREMENT + " WHERE cd_date > '2019-12-29T00:00:00Z'";
        String queryStr = "SELECT * FROM " + QUERY_MEASUREMENT;
        long start = System.currentTimeMillis();
        QueryResult queryResult = influxDB.query(new Query(queryStr, QUERY_DATABASE));
        long elapsedForBatchWrite = System.currentTimeMillis() - start;
        System.out.println("performance(ms):query data by time:" + elapsedForBatchWrite);
        System.out.println("result size:" + queryResult.getResults().size());
        System.out.println(queryResult.toString());
    }
}
