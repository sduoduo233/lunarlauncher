package cn.duoduo;

import cn.duoduo.download.Downloader;
import cn.duoduo.launch.Launcher;
import org.apache.commons.cli.*;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        Options options = new Options();

        Option download = new Option("dl", "download", false, "download lunar client");
        download.setRequired(false);
        options.addOption(download);

        Option launch = new Option("l", "launch", false, "launch lunar client");
        launch.setRequired(false);
        options.addOption(launch);

        Option help = new Option("h", "help", false, "help");
        help.setRequired(false);
        options.addOption(help);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("lunar launcher", options);
            return;
        }

        if (cmd.hasOption(help)) {
            formatter.printHelp("lunar launcher", options);
            return;
        }

        if (cmd.hasOption(download)) {
            Downloader downloader = new Downloader();
            try {
                downloader.start();
            } catch (IOException | InterruptedException | org.apache.hc.core5.http.ParseException e) {
                e.printStackTrace();
            }
        }

        if (cmd.hasOption(launch)) {
            Launcher launcher = new Launcher();
            try {
                launcher.launch();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
