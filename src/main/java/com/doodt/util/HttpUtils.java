package com.doodt.util;

import cn.hutool.core.io.FileUtil;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author doodt
 * @ClassName HttpUtils.java
 * @Description TODO
 * @createTime 2022/08/03 16:32:00
 */
public class HttpUtils {
    /**
     * 信任任何站点，实现https页面的正常访问
     */
    public static void trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    /**
     * 获取推特列表内容
     *
     * @param urlstr url地址
     * @return
     */
    public static String getHtml(String urlstr) {
        HttpURLConnection conn = null;
        try {
            if (StringUtils.isEmpty(urlstr)) return null;
            URL url = new URL(urlstr);
            //打开链接
            if (urlstr.toLowerCase().contains("https")) {
                //  直接通过主机认证
                trustEveryone();
            }
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.159 Safari/537.36");
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("cookie", "bnState={\"impressions\":4,\"delayStarted\":0}; _ga=GA1.1.136969342.1668851807; PHPSESSID=sf23ldjmrbnc9p1p6nneppiit6; bnState={\"impressions\":2,\"delayStarted\":0}; _ga_86QRZX7FN0=GS1.1.1687921718.12.1.1687922034.54.0.0");
            conn.setRequestProperty("cache-control", "max-age=0");
            conn.setUseCaches(false);
            conn.setConnectTimeout(20000);
            conn.setReadTimeout(20000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.connect();

            InputStream is = null;
            if (conn.getResponseCode() == 200) {
                is = conn.getInputStream();
            } else if (conn.getResponseCode() == 400) {
                is = conn.getErrorStream();
            }
            if (is == null) return null;
            BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            conn.disconnect();
            System.out.println(sb.toString());
            return sb.toString();
        } catch (Exception e) {
            System.err.println("请求接口" + urlstr + ",异常:" + e.getMessage());
            e.printStackTrace();
        } finally {
        }
        return null;
    }

    /**
     * 写入文件
     *
     * @param content
     * @param fileName
     */
    public static void writeFile(String content, String fileName) {
        if (StringUtils.isEmpty(content) || StringUtils.isEmpty(fileName)) return;
        File file = new File(fileName);
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        try {
            if (!file.exists()) {
                if (!FileUtil.exist(file.getParent())) {
                    FileUtil.mkdir(file.getParent());
                    file.createNewFile();
                }
                fos = new FileOutputStream(file);
            } else {
                fos = new FileOutputStream(file, true);
            }

            osw = new OutputStreamWriter(fos, "utf-8");
            // 写入内容
            osw.write(content);
            // 换行
            osw.write("\r\n");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭流
            try {
                if (osw != null) {
                    osw.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * HTTP 获取下载文件
     *
     * @param destUrl 网络文件地址
     * @param baseDir 根目录
     * @param title   文件名
     * @param suffix  文件后缀
     * @return
     */
    public static boolean saveToFile(String destUrl, String baseDir, String title, String suffix) {
        FileOutputStream fos = null;
        BufferedInputStream bis = null;
        HttpURLConnection httpUrl = null;
        URL url = null;
        byte[] buf = new byte[1024];
        int size = 0;
        boolean isflag = false;

        try {
            if (StringUtils.isAnyEmpty(destUrl, baseDir, title, suffix)) {
                return false;
            }
            trustEveryone();
            System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
            String encodeUrl = HttpUtils.encordHttpUrl(destUrl);
            // 建立链接
            url = new URL(encodeUrl);
            httpUrl = (HttpURLConnection) url.openConnection();
            // 连接指定的资源
            httpUrl.connect();
            // 获取网络输入流
            bis = new BufferedInputStream(httpUrl.getInputStream());
            // 建立文件
            File file = new File(baseDir + File.separator + title);
            if (!file.exists()) {
                file.mkdirs();
            }
            fos = new FileOutputStream(file.getPath() + File.separator + title + suffix);
            // 保存文件
            while ((size = bis.read(buf)) != -1) {
                fos.write(buf, 0, size);
            }

            isflag = true;
        } catch (Exception e) {
            System.out.println("目标文件:" + title + ",资源路径:" + destUrl + "下载异常:" + e.getMessage());
            e.printStackTrace();
            isflag = false;
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (bis != null) {
                    bis.close();
                }
                if (httpUrl != null) {
                    httpUrl.disconnect();
                }
            } catch (Exception e) {
            }
        }
        return isflag;
    }

    /**
     * 抓取html内容
     *
     * @param url
     * @return
     */
    public static Document getDocument(String url) {
        try {
            WebClient wc = new WebClient(BrowserVersion.CHROME);
            wc.getOptions().setUseInsecureSSL(true);
            // 启用JS解释器，默认为true
            wc.getOptions().setJavaScriptEnabled(false);
            // 禁用css支持
            wc.getOptions().setCssEnabled(false);
            // js运行错误时，是否抛出异常
            wc.getOptions().setThrowExceptionOnScriptError(false);
            // 状态码错误时，是否抛出异常
            wc.getOptions().setThrowExceptionOnFailingStatusCode(false);
            // 设置连接超时时间 ，这里是5S。如果为0，则无限期等待
            wc.getOptions().setTimeout(10000);
            // 是否允许使用ActiveX
            wc.getOptions().setActiveXNative(false);
            // 等待js时间
            wc.waitForBackgroundJavaScript(3 * 1000);
            // 设置Ajax异步处理控制器即启用Ajax支持
            wc.setAjaxController(new NicelyResynchronizingAjaxController());
            // 不跟踪抓取
            wc.getOptions().setDoNotTrackEnabled(false);
            HtmlPage page = wc.getPage(url);
            Document parse = Jsoup.parse(page.asXml(), url);
            return parse;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 抓取html内容
     *
     * @param url
     * @return
     */
    public static Document getDocumentByUrlConn(String url) {
        try {
            String htmlText = HttpUtils.getHtml(url);
            if (htmlText != null && htmlText.length() > 0) {
                return Jsoup.parse(htmlText, url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * http url 转码
     *
     * @param urls
     * @return
     */
    public static String encordHttpUrl(String urls) {
        try {
            StringBuilder sb = new StringBuilder();
            String[] u = urls.trim().split("/");
            for (String s : u) {
                sb.append(HttpUtils.isContainChinese(s) ? URLEncoder.encode(s, "utf-8") : s);
                sb.append("/");
            }
            sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 字符串是否包含中文
     *
     * @param str 待校验字符串
     * @return true 包含中文字符  false 不包含中文字符
     */
    public static boolean isContainChinese(String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        Pattern p = Pattern.compile("[\u4E00-\u9FA5|\\！|\\，|\\。|\\（|\\）|\\《|\\》|\\“|\\”|\\？|\\：|\\；|\\【|\\】|\\s]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }
}
