package cn.duoduo.utils;

import cn.duoduo.download.FileDownloadTask;
import jdk.internal.util.xml.impl.Input;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.IOUtils;
import sun.nio.ch.IOUtil;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileUtils {

    private final static Logger log = LoggerFactory.getLogger(FileUtils.class);

    /**
     * 计算SHA1
     *
     * @param file 要计算的文件
     * @return sha1
     */
    public static String calcSHA1(File file) throws IOException {
        InputStream fis = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            fis = new FileInputStream(file);
            int n = 0;
            byte[] buffer = new byte[1024];
            while (n != -1) {
                n = fis.read(buffer);
                if (n > 0) {
                    digest.update(buffer, 0, n);
                }
            }
            fis.close();
            return new HexBinaryAdapter().marshal(digest.digest());
        } catch (NoSuchAlgorithmException | IOException e) {
            return "";
        } finally {
            if (fis != null)
                fis.close();
        }
    }

    public static boolean verifyFile(File file, String sha1) throws IOException {
        return calcSHA1(file).equalsIgnoreCase(sha1);
    }

    public static void unzip(File file, File outDir) throws IOException {
        ZipFile zipFile = new ZipFile(file);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            File entryDestination = new File(outDir, entry.getName());
            if (entry.isDirectory()) {
                entryDestination.mkdirs();
            } else {
                entryDestination.getParentFile().mkdirs();
                InputStream in = zipFile.getInputStream(entry);
                OutputStream out = new FileOutputStream(entryDestination);

                byte[] bytes = new byte[1024];
                int n;
                while ((n = in.read(bytes)) != -1) {
                    out.write(bytes, 0, n);
                }

                in.close();
                out.close();
            }
        }
        zipFile.close();
    }

    public static RequestConfig getRequestConfig() {
        String proxyHost = System.getProperty("http.proxyHost");
        int proxyPort = 0;
        try {
            proxyPort = Integer.parseInt(System.getProperty("http.proxyPort"));
        } catch (Exception ignored) {
        }

        HttpHost proxy = new HttpHost("http", proxyHost, proxyPort);
        if (proxyHost != null)
            return RequestConfig.custom()
                    .setProxy(proxy)
                    .build();
        else
            return RequestConfig.DEFAULT;
    }

    public static void downloadFile(File path, String url) throws IOException {
        try {
            // 下载
            CloseableHttpClient httpClient = HttpClientBuilder.create().useSystemProperties().build();

            HttpGet httpGet = new HttpGet(url);
            httpGet.setConfig(FileUtils.getRequestConfig());
            CloseableHttpResponse response = httpClient.execute(httpGet);

            if (response.getCode() != 200) {
                throw new IOException(String.format("HTTP CODE: %s %s", response.getCode(), url));
            }

            // 保存文件
            path.getParentFile().mkdirs();
            BufferedInputStream is = new BufferedInputStream(response.getEntity().getContent());
            BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(path));
            int inByte;
            while ((inByte = is.read()) != -1) os.write(inByte);
            is.close();
            os.close();
            httpClient.close();
        } catch (IOException exception) {
            log.error(String.format("下载失败: %s", url), exception);
            throw exception;
        }
    }

}
