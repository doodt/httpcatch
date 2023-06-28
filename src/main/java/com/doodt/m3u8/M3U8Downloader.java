package com.doodt.m3u8;

import com.doodt.m3u8.download.M3u8DownloadFactory;
import com.doodt.m3u8.utils.Constant;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author doodt
 * @ClassName M3U8Downloader.java
 * @Description TODO
 * @createTime 2023/06/27 18:07:00
 */
public class M3U8Downloader {
    private static Logger logger = LoggerFactory.getLogger(M3U8Downloader.class);

    public static boolean download(String title, String url, String baseDir) {
        try {
            M3u8DownloadFactory.M3u8Download m3u8Download = M3u8DownloadFactory.getInstance();
            //m3u8文件地址
            m3u8Download.setDOWNLOADURL(url);
            //设置生成目录
            m3u8Download.setDir(baseDir + Constant.FILESEPARATOR + title);
            //设置视频名称
            m3u8Download.setFileName(title);
            //设置线程数
            m3u8Download.setThreadCount(100);
            //设置重试次数
            m3u8Download.setRetryCount(10);
            //设置连接超时时间（单位：毫秒）
            m3u8Download.setTimeoutMillisecond(10000L);
            //添加额外请求头
      /*  Map<String, Object> headersMap = new HashMap<>();
        headersMap.put("Content-Type", "text/html;charset=utf-8");
        m3u8Download.addRequestHeaderMap(headersMap);*/
            //如果需要的话设置http代理
            //m3u8Download.setProxy("172.50.60.3",8090);
            //添加监听器
            //开始下载
            return m3u8Download.start();
        } catch (Exception e) {
            logger.error("下载文件异常:{}", e.getMessage(), e);
        }
        return false;
    }
}
