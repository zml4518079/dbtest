package com.eagle;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class ClientTest {

    @Test
    public void test() {
        InfluxDB influxDB = InfluxDBFactory.connect("http://" + TestUtils.getInfluxIP() + ":" + TestUtils.getInfluxPORT(true), "root", "root");

        String db1 = "database1";
        influxDB.query(new Query("CREATE DATABASE " + db1));
        String rp1 = "aRetentionPolicy1";
        influxDB.query(new Query("CREATE RETENTION POLICY " + rp1 + " ON " + db1 + " DURATION 30h REPLICATION 2 DEFAULT"));
        Point point1 = Point.measurement("cpu")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("idle", 90L)
                .addField("user", 9L)
                .addField("system", 1L)
                .build();
        influxDB.write(db1, rp1, point1); // Write to db1

        String db2 = "database2";
        influxDB.query(new Query("CREATE DATABASE " + db2));
        String rp2 = "aRetentionPolicy1";
        influxDB.query(new Query("CREATE RETENTION POLICY " + rp2 + " ON " + db2 + " DURATION 30h REPLICATION 2 DEFAULT"));
        Point point2 = Point.measurement("cpu")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("idle", 80L)
                .addField("user", 8L)
                .addField("system", 2L)
                .build();
        influxDB.write(db2, rp2, point2); // Write to db2


        influxDB.query(new Query("SELECT * FROM cpu"));     // Returns Point1
        influxDB.query(new Query("SELECT * FROM cpu", db2)); // Returns Point2
    }
}
