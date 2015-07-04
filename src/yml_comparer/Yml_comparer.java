package yml_comparer;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.xml.parsers.SAXParserFactory;

/**
 *
 * @author paveliarch
 */
public class Yml_comparer {
    
    public static final int YML_THREADS = 2;
    public static final int URL_THREADS = 16;

    /**
     * @param args the command line arguments
     * @throws java.lang.InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        if (args.length < 2) {
            usage();
        } else {
            System.out.println("comparing " + args[0] + " to " + args[1]);
            ConcurrentSkipListMap<String, OfferHash> _map = new ConcurrentSkipListMap<>();
            LinkedBlockingQueue<OfferPicture> _queue = new LinkedBlockingQueue<>();
            AtomicBoolean _stop = new AtomicBoolean(false);
            SAXParserFactory _spf = SAXParserFactory.newInstance();
            _spf.setNamespaceAware(true);
            _spf.setValidating(true);
            YmlParser _oldParser = new YmlParser(args[0], _spf, _map, OfferHash.OFFER_WAS_IN_OLD);
            YmlParser _newParser = new YmlParser(args[1], _spf, _map, OfferHash.OFFER_WAS_IN_NEW);
            _newParser.handler.postListeners.put("picture", (String id, String url) -> {
                _queue.add(new OfferPicture(id, url));
            });
            ExecutorService _ymlExecutors = Executors.newFixedThreadPool(YML_THREADS);
            System.out.println("yml traverse starting");
            _ymlExecutors.submit(_oldParser);
            _ymlExecutors.submit(_newParser);
            ExecutorService _urlExecutors = Executors.newFixedThreadPool(URL_THREADS);
            for(int i = 0; i < 10; i++) {
                _urlExecutors.submit(new UrlChecker(_queue, _map, _stop));
            }
            _ymlExecutors.shutdown();
            if (_ymlExecutors.awaitTermination(1l, TimeUnit.DAYS)) {
                System.out.println("yml traverse finished");
            } else {
                System.out.println("yml traverse terminated");
            }
            _stop.set(false);
            _urlExecutors.shutdown();
            _urlExecutors.awaitTermination(1l, TimeUnit.DAYS);
            System.out.println("picture check finished");
            _map.entrySet().stream().forEach((entry) -> {
                String _result = entry.getValue().formatResult(entry.getKey());
                if (_result!=null) {
                    System.out.println(_result);
                }
            });
        }
    }

    /**
     * prints usage info and exits
     */
    private static void usage() {
        System.err.println("Usage: yml_comparer <old_file.yml> <new_file.yml>");
        System.exit(1);
    }
}
