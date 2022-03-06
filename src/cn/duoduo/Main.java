package cn.duoduo;

import cn.duoduo.download.Downloader;
import cn.duoduo.launch.Launcher;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        Downloader downloader = new Downloader();

        /*
        try {
            downloader.start();
        } catch (IOException | ParseException | InterruptedException e) {
            e.printStackTrace();
        }
         */

        Launcher launcher = new Launcher();
        try {
            launcher.launch();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
