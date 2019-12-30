package com.eagle;

import com.eagle.point.ColletionDataBean;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataUtils {

    public static final String TIME_FORMAT_PATTERN = "yyyy-MM-dd'T'hh:mm:ss.S'Z'";

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
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TIME_FORMAT_PATTERN);
        return new ColletionDataBean(Instant.parse(simpleDateFormat.format(cdDate)),
                modelId, groupId, indexId, randomNum(), randomNum());
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

    public static void insertDataByModelId(InfluxDB influxDB, String modelId, String dataBase, String rpName, String measurement) {
        // 构造数据
        long startTime = 1577604000000L;
        final int insertNum =1000;
        final int periodTime = 1 * 1000;
        DataUtils dataUtils = new DataUtils(modelId,"1","1", periodTime);
        long runningTime = 0;
        for (int i = 0; i < 100; i++) {
            startTime = startTime - (insertNum * periodTime) * i;
            List<ColletionDataBean> dataBeans = dataUtils.createCDBeans(insertNum, startTime);
            BatchPoints batchPoints = BatchPoints
                    .database(dataBase)
                    .tag("async", "true")
                    .retentionPolicy(rpName)
                    .consistency(InfluxDB.ConsistencyLevel.ALL)
                    .build();
            for (ColletionDataBean dataBean:dataBeans) {
                Point point = Point.measurement(measurement).addFieldsFromPOJO(dataBean).build();
                batchPoints.point(point);
            }

            long start = System.currentTimeMillis();
            influxDB.write(batchPoints);
            runningTime += (System.currentTimeMillis() - start);
        }

        System.out.println("insert running time[ms]:" + runningTime);
    }
}
