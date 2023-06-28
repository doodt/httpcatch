package com.doodt.task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.doodt.entity.VideoModule;
import com.doodt.m3u8.M3U8Downloader;
import com.doodt.m3u8.utils.Constant;
import com.doodt.util.HttpUtils;
import org.apache.commons.io.FileUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * * 主站:https://tiktok365.vip
 * * 抓取地址:https://tiktok365.vip/videolist/cate/1-112.html
 * * 分页抓取:https://tiktok365.vip/videolist/cate/1-112.html?p=[\\d]+
 *
 * @author doodt
 * @ClassName DownTiktok365Task.java
 * @Description TODO
 * @createTime 2023/06/28 15:03:00
 */
@Component
@Configuration
@EnableScheduling
public class DownTiktok365Task {
    private Logger logger = LoggerFactory.getLogger(DownTiktok365Task.class);

    @Value("${task.tiktok365.path}")
    private String baseDir;

    @Scheduled(cron = "${task.tiktok365.cron}")
    public void doTask() {
        logger.info("tiktok365同步任务开始");
        String url = "https://tiktok365.vip/videolist/cate/1-112.html";
        for (int i = 1; i < 100; i++) {
            getHtml(url, i);
        }
        logger.info("tiktok365同步任务结束");
    }

    public void getHtml(String url, int p) {
        try {
            String baseDir = System.getProperty("user.dir");
            //先读缓存
            String fileName = baseDir + "/tiktok365" + "/p" + p + ".json";
            File jsonFile = new File(fileName);
            List<VideoModule> videoModules = null;
            logger.info("读取{},页码{},是否有缓存:{},{}", url, p, fileName, jsonFile.exists());
            if (jsonFile.exists()) {
                String json = FileUtils.readFileToString(jsonFile, "utf-8");
                if (json != null && json.length() > 0) {
                    videoModules = JSONArray.parseArray(json, VideoModule.class);
                }
            } else {
                videoModules = getVideoListOnline(url, p);
                if (videoModules.size() > 0) {
                    if (!jsonFile.getParentFile().exists()) {
                        jsonFile.getParentFile().mkdirs();
                    }
                    //写入缓存文件
                    FileUtils.writeStringToFile(jsonFile, JSONArray.toJSONString(videoModules, SerializerFeature.PrettyFormat), "utf-8");
                    logger.info("缓存{},页码:{},json路径:{}", url, p, fileName);
                }
            }
            if (videoModules != null && videoModules.size() > 0) {
                for (VideoModule v : videoModules) {
                    downloadVideo(v);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void downloadVideo(VideoModule module) {
        try {
            if (new File(baseDir + Constant.FILESEPARATOR + module.getTitle()).exists()) return; //文件夹已存在不下载
            boolean downloadMedia = M3U8Downloader.download(module.getTitle(), module.getMediaUrl(), baseDir);
            if (downloadMedia) {
                HttpUtils.saveToFile(module.getImgUrl(), baseDir, module.getTitle(), ".jpg");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<VideoModule> getVideoListOnline(String url, int p) {
        List<VideoModule> list = new ArrayList<>();
        try {
            if (p > 1) {
                url += "?p=" + p;
            }
            logger.info("开始在线获取:{},内容", url);
            Document parse = HttpUtils.getDocument(url);
            if (parse == null) {
                logger.info("在线读取:{}内容失败", url);
                return list;
            }
            Elements eles = parse.select("a.vodlist_thumb");
            if (eles == null || eles.size() == 0) {
                logger.info("在线获取{}失败", url);
                return list;
            }
            for (Element ele : eles) {
                VideoModule videoModule = new VideoModule();
                videoModule.setBaseUrl("https://tiktok365.vip");
                videoModule.setTitle(ele.attr("title").trim());
                videoModule.setSubUrl(ele.attr("href").trim());
                videoModule.setImgUrl(ele.attr("data-original").trim());
                videoModule.setDateTime(ele.text().trim());
                StringBuilder sb = new StringBuilder(videoModule.getImgUrl().split("/fengmian")[0]);
                sb.append("/m3u8");
                sb.append("/").append(videoModule.getDateTime().split("-")[0]).append(videoModule.getDateTime().split("-")[1]);
                sb.append("/").append(videoModule.getTitle());
                sb.append("/").append(videoModule.getTitle()).append(".m3u8");
                videoModule.setMediaUrl(sb.toString());
                list.add(videoModule);
            }
        } catch (Exception e) {
            logger.error("在线获取媒体地址异常:" + e.getMessage(), e);
        }
        return list;
    }
}
