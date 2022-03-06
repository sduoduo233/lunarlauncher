package cn.duoduo.download;

import cn.duoduo.utils.FileUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.concurrent.Callable;

public class FileDownloadTask implements Callable<Boolean> {

    private final static Logger log = LoggerFactory.getLogger(FileDownloadTask.class);

    protected File path;
    protected String url;
    protected String hash;

    public FileDownloadTask(File path, String url, String hash) {
        this.path = path;
        this.url = url;
        this.hash = hash;
    }

    @Override
    public Boolean call() throws Exception {
        return download();
    }

    protected boolean download()  throws Exception {
        // 验证是否已经下载
        if (hash != null)
            if (path.isFile() && FileUtils.verifyFile(path, hash)) {
                log.info("跳过下载: {} => {} {}", url, path, hash);
                return true;
            }

        // 下载
        log.info("开始下载: {} => {}", url, path);
        FileUtils.downloadFile(path, url);

        // 验证文件
        if (hash == null) return true; // 不验证hash
        if (!FileUtils.verifyFile(path, hash)) {
            throw new IOException(String.format("hash验证失败: %s %s", url, hash));
        }
        return true;
    }

}
