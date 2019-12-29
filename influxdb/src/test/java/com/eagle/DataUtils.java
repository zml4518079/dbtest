package com.eagle;

import com.eagle.point.ColletionDataBean;

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
}
