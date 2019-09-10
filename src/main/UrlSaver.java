import org.jsoup.Jsoup;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class UrlSaver implements Runnable {

    private static Logger log = Slf4jLogger.getLogger();

    private ScanManager manager;
    private String url;
    private File dir;
    private String fileName;

    UrlSaver(ScanManager manager, File fileDir, String url, Integer depth) {
        this.manager = manager;
        this.url = url;
        dir = new File(fileDir, depth.toString());
        this.fileName = url.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
    }

    @Override
    public void run() {
        setThreadName();

        String html = getHtmlAsString();
        if (html != null) {
            saveFile(html);
        }
        manager.workFinished();
    }

    private void saveFile(String content) {
        File file = createFileInDir();
        if (file == null) {
            return;
        }
        writeToFile(file, content);
    }

    private void writeToFile(File file, String content) {
        try {
            FileWriter fileWriter = new FileWriter(file);
            log.info("Saving file \"" + file.getAbsolutePath() + "\"");
            fileWriter.write(content);
            fileWriter.close();
        } catch (IOException e) {
            log.error("IO Exception when creating new file " + file.getAbsolutePath(), e);
        }
    }

    private File createFileInDir() {
        if (dir.mkdirs()) {
            log.info("Created new dir " + dir.getAbsolutePath());
        }
        File file = new File(dir, fileName);
        try {
            if (!file.exists() || file.delete()) {
                if (file.createNewFile()) {
                    return file;
                }
            }
        } catch (IOException e) {
            log.error("IO Exception when creating new file " + file.getAbsolutePath());
        }
        return null;
    }

    private String getHtmlAsString() {
        try {
            return Jsoup.connect(url).get().html();
        } catch (IOException e) {
            log.error("Unable to connect to url " + url);
        }
        return null;
    }

    private void setThreadName() {
        Thread.currentThread().setName("UrlSaver-" + fileName.substring(0, 15));
    }
}