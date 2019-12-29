package com.eagle.point;

import org.influxdb.annotation.Column;
import org.influxdb.annotation.TimeColumn;

import java.time.Instant;

public class ColletionDataBean {

    @Column(name = "cd_date")
    @TimeColumn
    private Instant cdDate;
    @Column(name = "model_id", tag = true)
    private String modelId;
    @Column(name = "group_id", tag = true)
    private String groupId;
    @Column(name = "index_id", tag = true)
    private String indexId;
    @Column(name = "cpu_rate")
    private Double cpuRate;
    @Column(name = "ram_rate")
    private Double ramRate;

    public ColletionDataBean() {
    }

    public ColletionDataBean(Instant cdDate, String modelId, String groupId, String indexId, Double cpuRate, Double ramRate) {
        this.cdDate = cdDate;
        this.modelId = modelId;
        this.groupId = groupId;
        this.indexId = indexId;
        this.cpuRate = cpuRate;
        this.ramRate = ramRate;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getIndexId() {
        return indexId;
    }

    public void setIndexId(String indexId) {
        this.indexId = indexId;
    }

    public Instant getCdDate() {
        return cdDate;
    }

    public void setCdDate(Instant cdDate) {
        this.cdDate = cdDate;
    }

    public Double getCpuRate() {
        return cpuRate;
    }

    public void setCpuRate(Double cpuRate) {
        this.cpuRate = cpuRate;
    }

    public Double getRamRate() {
        return ramRate;
    }

    public void setRamRate(Double ramRate) {
        this.ramRate = ramRate;
    }
}
