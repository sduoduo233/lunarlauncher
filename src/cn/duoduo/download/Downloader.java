package cn.duoduo.download;

import cn.duoduo.beans.Artifact;
import cn.duoduo.beans.LaunchInfo;
import cn.duoduo.beans.RequestBody;
import com.google.gson.Gson;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;

public class Downloader {

    private final static Logger log = LoggerFactory.getLogger(Downloader.class);

    private final Gson gson;
    private final ExecutorService executorService;
    private final File baseDir = new File(".");

    public Downloader() {
        gson = new Gson();
        executorService = Executors.newFixedThreadPool(8);
    }

    public void start() throws IOException, ParseException, InterruptedException {
        // 获取版本信息
        String url = "https://api.lunarclientprod.com/launcher/launch";
        RequestBody requestBody = new RequestBody();
        requestBody.hwid = "000";
        requestBody.hwid_private = "000";
        requestBody.os = "win32";
        requestBody.arch = "x64";
        requestBody.launcher_version = "";
        requestBody.version = "1.8";
        requestBody.branch = "master";
        requestBody.launch_type = "OFFLINE";
        requestBody.classifier = "optifine";
        String body = gson.toJson(requestBody);

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(url);
        StringEntity params = new StringEntity(body);
        request.addHeader("content-type", "application/json");
        request.setEntity(params);

        CloseableHttpResponse result = httpClient.execute(request);
        String json = EntityUtils.toString(result.getEntity(), "UTF-8");
        LaunchInfo launchInfo = gson.fromJson(json, LaunchInfo.class);

        // 保存版本信息
        FileOutputStream os = new FileOutputStream(new File(baseDir, "version.json"));
        os.write(json.getBytes(StandardCharsets.UTF_8));
        os.close();

        // 创建任务
        List<Callable<Boolean>> tasks = new ArrayList<>();

        // 下载 artifacts
        File artifactDir = Paths.get(baseDir.getAbsolutePath(), "offline", "1.8.9").toFile();
        for (Artifact artifact : launchInfo.launchTypeData.artifacts) {
            log.info("Artifacts: type={}, url={}, name={}", artifact.type, artifact.url, artifact.name);

            switch (artifact.type.toUpperCase(Locale.ROOT)) {
                case "CLASS_PATH":
                    tasks.add(new FileDownloadTask(new File(artifactDir, artifact.name), artifact.url, artifact.sha1));
                    break;
                case "NATIVES":
                    tasks.add(new NativeDownloadTask(new File(artifactDir, artifact.name), artifact.url, artifact.sha1));
                    break;
            }

        }

        // 下载 jre
        File jreDir = Paths.get(baseDir.getAbsolutePath(), "jre").toFile();
        tasks.add(
                new JreDownloadTask(
                        new File(jreDir, "jre.zip"),
                        launchInfo.jre.download.url,
                        launchInfo.jre.javawExeChecksum,
                        launchInfo.jre.javaExeChecksum,
                        launchInfo.jre.executablePathInArchive,
                        launchInfo.jre.javawDownload
                )
        );

        // 下载 textures
        File assetsDir = Paths.get(baseDir.getAbsolutePath(), "textures").toFile();
        for (TextureFile texture : getTextures(launchInfo.textures.indexUrl)) {
            tasks.add(new FileDownloadTask(new File(assetsDir, texture.path), launchInfo.textures.baseUrl + texture.hash, texture.hash));
        }

        // 执行
        List<Future<Boolean>> futures = executorService.invokeAll(tasks);
        executorService.shutdown();

        for (Future<Boolean> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                log.error("下载失败: ", e);
            }
        }

        httpClient.close();
    }

    private List<TextureFile> getTextures(String indexUrl) throws IOException, ParseException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(indexUrl);
        CloseableHttpResponse response = httpClient.execute(httpGet);

        String index = EntityUtils.toString(response.getEntity(), "UTF-8");

        List<TextureFile> textureFileList = new ArrayList<>();
        String[] lines = index.split("\\r?\\n|\\r");
        for (String line : lines) {
            TextureFile textureFile = new TextureFile();
            textureFile.path = line.split(" ")[0];
            textureFile.hash = line.split(" ")[1];
            textureFileList.add(textureFile);
        }
        return textureFileList;
    }
}
