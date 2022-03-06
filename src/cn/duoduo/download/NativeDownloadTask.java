package cn.duoduo.download;

import cn.duoduo.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class NativeDownloadTask extends FileDownloadTask {

    private final static Logger log = LoggerFactory.getLogger(NativeDownloadTask.class);

    private File nativeDir = new File(path.getParentFile(), "natives");

    public NativeDownloadTask(File path, String url, String hash) {
        super(path, url, hash);
    }

    @Override
    public Boolean call() throws Exception {
        // 下载
        download();

        // 解压native
        FileUtils.unzip(path, nativeDir);
        return true;
    }

}
