import org.slf4j.Logger;

public class Main {

    private static Logger log = Slf4jLogger.getLogger();

    public static void main(String[] args) {
        if (args.length != 4) {
            log.error("Usage: scanManager <base_url> <max_urls_per_page> <depth> <uniqueness>");
            return;
        }
        String baseUrl = args[0];
        int maxUrlsPerPage = Integer.valueOf(args[1]);
        int maxDepth = Integer.valueOf(args[2]);
        boolean unique = "true".equals(args[3].toLowerCase());

        log.info("Scanning URL " + baseUrl + " with max depth of " + maxDepth);
        ScanManager scanManager = new ScanManager(baseUrl, maxUrlsPerPage, maxDepth, unique);
        scanManager.scan();
        log.info("Scan complete");
    }
}
