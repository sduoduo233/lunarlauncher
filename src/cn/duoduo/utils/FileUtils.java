package cn.duoduo.utils;

import jdk.internal.util.xml.impl.Input;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
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

    public static boolean verifyFile(File file, String sha1) throws IOException, NoSuchAlgorithmException {
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
                int n = 0;
                while ((n = in.read(bytes)) != -1) {
                    out.write(bytes, 0, n);
                }

                in.close();
                out.close();
            }
        }
        zipFile.close();
    }

    public static void downloadFile(File path, String url) throws IOException {
        // 下载
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = httpClient.execute(httpGet);

        if (response.getCode() != 200) {
            throw new IOException(String.format("HTTP CODE: %s", response.getCode()));
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
    }

}
