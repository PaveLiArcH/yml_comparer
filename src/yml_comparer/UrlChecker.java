package yml_comparer;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author paveliarch
 */
public class UrlChecker implements Runnable {

    public static final int CONNECT_TIMEOUT = 2000;
    public static final int READ_TIMEOUT = 2000;
    public static final long QUEUE_TIMEOUT = 1000;

    protected BlockingQueue<OfferPicture> queue;
    protected ConcurrentMap<String, OfferHash> map;
    protected AtomicBoolean stop;

    public UrlChecker(BlockingQueue<OfferPicture> queue, ConcurrentMap<String, OfferHash> map, AtomicBoolean stop) {
        this.queue = queue;
        this.map = map;
        this.stop = stop;
    }

    public static boolean checkIfURLExists(String url) {
        HttpURLConnection _connection;
        try {
            _connection = (HttpURLConnection) new URL(url).openConnection();
            _connection.setRequestMethod("HEAD");
            _connection.setConnectTimeout(CONNECT_TIMEOUT);
            _connection.setReadTimeout(READ_TIMEOUT);
            return (_connection.getResponseCode() == HttpURLConnection.HTTP_OK);
        } catch (IOException ex) {
            Logger.getLogger(UrlChecker.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public void run() {
        while (!stop.get()) {
            try {
                OfferPicture _picture = queue.poll(QUEUE_TIMEOUT, TimeUnit.MILLISECONDS);
                if (!checkIfURLExists(_picture.url)) {
                    OfferHash _hash = new OfferHash();
                    _hash.flags = OfferHash.OFFER_PICTURE_PROBLEM;
                    map.merge(_picture.id, _hash, (OfferHash t, OfferHash u) -> {
                        u.flags |= t.flags;
                        return u;
                    });
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(UrlChecker.class.getName()).log(Level.SEVERE, null, ex);
                break;
            }
        }
    }
}
