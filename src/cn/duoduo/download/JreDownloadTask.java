package cn.duoduo.download;

import cn.duoduo.utils.FileUtils;
import javafx.scene.shape.PathBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class JreDownloadTask extends FileDownloadTask {

    private final static Logger log = LoggerFactory.getLogger(JreDownloadTask.class);

    private String javawHash;
    private String javaHash;
    private List<String> executablePathInArchive;
    private String javawUrl;

    public JreDownloadTask(File path, String url, String javawHash, String javaHash, List<String> executablePathInArchive, String javawUrl) {
        super(path, url, null);
        this.javawHash = javawHash;
        this.javaHash = javaHash;
        this.executablePathInArchive = executablePathInArchive;
        this.javawUrl = javawUrl;
    }

    @Override
    public Boolean call() throws Exception {
        File binDir = Paths.get(path.getParent(), this.executablePathInArchive.toArray(new String[0])).toFile().getParentFile();

        if (!FileUtils.verifyFile(new File(binDir, "java.exe"), javaHash)) {
            // 下载jre
            FileUtils.downloadFile(path, url);

            // 解压jre
            FileUtils.unzip(path, path.getParentFile());
        }

        if (!FileUtils.verifyFile(new File(binDir, "javaw.exe"), javawHash)) {
            // 下载javaw
            FileUtils.downloadFile(new File(binDir, "javaw.exe"), javawUrl);
        }

        return true;
    }

}
