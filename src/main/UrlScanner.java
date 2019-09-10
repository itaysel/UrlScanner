import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UrlScanner implements Runnable {

    private static Logger log = Slf4jLogger.getLogger();

    private ScanManager manager;
    private String url;
    private int depth;

    UrlScanner(ScanManager manager, String url, int depth) {
        this.manager = manager;
        this.url = url;
        this.depth = depth;
    }

    @Override
    public void run() {
        setThreadName();
        int limit = manager.getMaxUrlsPerPage();

        log.info("Scanning URL " + url + " for maximum of " + limit + " pagse");
        List<String> links = getLinks();
        for (int i = 0; i < links.size() && limit > 0; i++) {
            String link = links.get(i);
            if (manager.addUrlToDepth(link, depth + 1)) {
                limit--;
                log.info("Found " + link);
            }
        }
        manager.workFinished();
    }

    private void setThreadName() {
        Thread.currentThread().setName("UrlScanner-" + depth + "/" + url.substring(0, 15));
    }

    private List<String> getLinks() {
        Document doc;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            log.error("Unable to connect to url " + url);
            return new ArrayList<>();
        }
        return doc.select("a[href]").stream()
                .map(link -> link.attr("href"))
                .filter(linkUrl -> linkUrl.startsWith("http"))
                .collect(Collectors.toList());
    }
}