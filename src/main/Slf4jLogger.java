import org.slf4j.LoggerFactory;

class Slf4jLogger {

    private static org.slf4j.Logger log;

    static org.slf4j.Logger getLogger() {
        if (log == null) {
            log = LoggerFactory.getLogger("");
        }
        return log;
    }

}
