package cn.duoduo.launch;

import cn.duoduo.beans.Artifact;
import cn.duoduo.beans.LaunchInfo;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;

public class Launcher {

    private final static Logger log = LoggerFactory.getLogger(Launcher.class);

    private final Gson gson;
    private final File baseDir = new File(".");

    public Launcher() {
        this.gson = new Gson();
    }

    public void launch() throws IOException, InterruptedException {
        String version;
        try(BufferedReader br = new BufferedReader(new FileReader(new File(baseDir, "version.json")))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            version = sb.toString();
        }

        LaunchInfo launchInfo = gson.fromJson(version, LaunchInfo.class);

        File artifactDir = Paths.get(baseDir.getAbsolutePath(), "offline", "1.8.9").toFile();
        File jreDir = Paths.get(baseDir.getAbsolutePath(), "jre").toFile();
        File assetsDir = Paths.get(baseDir.getAbsolutePath(), "textures").toFile();

        StringBuilder args = new StringBuilder();

        // jre
        File javaw = Paths.get(jreDir.getAbsolutePath(), launchInfo.jre.executablePathInArchive.toArray(new String[0])).toFile();
        args.append(javaw.getAbsolutePath().replace("javaw.exe", "java.exe")).append(" ");

        // extraArguments
        for (String extraArgument : launchInfo.jre.extraArguments) {
            args.append(extraArgument).append(" ");
        }

        // 内存
        args.append("-Xms2048m -Xmx2048m ");

        // native
        args.append("-Djava.library.path=natives").append(" ");

        // classpath
        args.append("-cp ");
        for (Artifact artifact : launchInfo.launchTypeData.artifacts) {
            args.append(artifact.name).append(";");
        }
        args.append(" ");

        // main class
        args.append(launchInfo.launchTypeData.mainClass).append(" ");

        // minecraft args
        args.append(String.format(" --version %s --accessToken 0 --assetIndex %s --userProperties {}", "1.8.9", "1.8.9"));
        args.append(
                String.format(
                        " --gameDir %s --texturesDir %s --launcherVersion 2.9.3 --hwid 0 --width 854 --height 480",
                        new File(baseDir, "minecraft").getAbsolutePath(),
                        assetsDir
                )
        );

        log.info("args: " + args);

        // 启动
        Process process = Runtime.getRuntime().exec(args.toString(), null, artifactDir);
    }
}
