import org.slf4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScanManager {

    private final Logger log = Slf4jLogger.getLogger();

    private Set<String> crossDepthUrlUniqueness;
    private List<Set<String>> depthToUrls;
    private CountDownLatch latch;

    private int maxUrlsPerPage;
    private int maxDepth;
    private boolean unique;

    public ScanManager(String baseUrl, int maxUrlsPerPage, int maxDepth, boolean unique) {
        this.maxUrlsPerPage = maxUrlsPerPage;
        this.maxDepth = maxDepth;
        this.unique = unique;

        if (unique) {
            crossDepthUrlUniqueness = new HashSet<>();
        }
        depthToUrls = new ArrayList<>();
        depthToUrls.add(new HashSet<>());
        depthToUrls.get(0).add(baseUrl);
    }

    int getMaxUrlsPerPage() {
        return maxUrlsPerPage;
    }

    /**
     * Add a new URL to the URLs list per depth
     * non-unique URLs will not be added if the unique property is set to true
     *
     * @param url - URL represeted by a String
     * @param depth - the depth to be assosiated with this url
     * @return true if the new URL was added, otherwise false
     */
    synchronized boolean addUrlToDepth(String url, int depth) {
        if (unique && crossDepthUrlUniqueness.contains(url)) {
            return false;
        }
        if (depthToUrls.size() == depth) {
            depthToUrls.add(depth, new HashSet<>());
        }
        depthToUrls.get(depth).add(url);
        crossDepthUrlUniqueness.add(url);
        return true;
    }

    void scan() {
        ExecutorService pool = Executors.newFixedThreadPool(maxUrlsPerPage);
        File filesDir = initFilesDirectory();
        for (int depth = 0; depth <= maxDepth; depth++) {
            if (depthToUrls.size() == depth) {
                log.info("No URLs found for depth " + depth);
                break;
            }
            Set<String> urlsByDepth = depthToUrls.get(depth);
            saveUrls(pool, filesDir, depth, urlsByDepth);
            scanForUrls(pool, depth, urlsByDepth);
        }
        pool.shutdownNow();
    }

    private File initFilesDirectory() {
        File filesDir = new File(System.getProperty("user.dir"), "files");
        if (filesDir.mkdir()) {
            log.info("Created new dir " + filesDir.getAbsolutePath());
        }
        return filesDir;
    }

    private void saveUrls(ExecutorService pool, File fileDir, int depth, Set<String> urlsByDepth) {
        List<UrlSaver> urlSavers = new ArrayList<>();
        for (String url : urlsByDepth) {
            urlSavers.add(new UrlSaver(this, fileDir, url, depth));
        }
        latch = new CountDownLatch(urlSavers.size());
        urlSavers.forEach(pool::execute);
        awaitForLatch();
    }

    private void scanForUrls(ExecutorService pool, int depth, Set<String> urlsByDepth) {
        List<UrlScanner> urlScanners = new ArrayList<>();
        for (String url : urlsByDepth) {
            urlScanners.add(new UrlScanner(this, url, depth));
        }
        latch = new CountDownLatch(urlScanners.size());
        urlScanners.forEach(pool::execute);
        awaitForLatch();
    }

    private void awaitForLatch() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for Thread", e);
            throw new RuntimeException("Unexpected interruption", e);
        }
    }

    void workFinished() {
        latch.countDown();
    }
}