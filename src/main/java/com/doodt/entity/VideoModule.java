package com.doodt.entity;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

/**
 * @author doodt
 * @ClassName VideoModule.java
 * @Description TODO
 * @createTime 2023/06/27 12:35:00
 */
public class VideoModule implements Serializable {
    private String baseUrl;
    private String subUrl;
    private String title;
    //发布日期,yyyy-MM-dd
    private String dateTime;
    private String imgUrl;
    private String mediaUrl;
    private String desc;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getSubUrl() {
        return subUrl;
    }

    public void setSubUrl(String subUrl) {
        this.subUrl = subUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
