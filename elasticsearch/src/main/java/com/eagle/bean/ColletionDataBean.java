package com.eagle.bean;

import com.alibaba.fastjson.JSONObject;

import java.util.Date;
import java.util.UUID;

public class ColletionDataBean {

    private String dataId;
    private String modelId;
    private String groupId;
    private String indexId;
    private Date cdDate;
    private Double cpuRate;
    private Double ramRate;

    public ColletionDataBean() {
    }

    public ColletionDataBean(String modelId, String groupId, String indexId, Date cdDate, Double cpuRate, Double ramRate) {
        this.dataId = UUID.randomUUID().toString();
        this.modelId = modelId;
        this.groupId = groupId;
        this.indexId = indexId;
        this.cdDate = cdDate;
        this.cpuRate = cpuRate;
        this.ramRate = ramRate;
    }

    public String toSourceString() {
        JSONObject object = new JSONObject();
        object.put("modelId", modelId);
        object.put("groupId", groupId);
        object.put("indexId", indexId);
        object.put("cdDate", cdDate);
        object.put("cpuRate", cpuRate);
        object.put("ramRate", ramRate);
        return object.toJSONString();
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
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

    public Date getCdDate() {
        return cdDate;
    }

    public void setCdDate(Date cdDate) {
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
