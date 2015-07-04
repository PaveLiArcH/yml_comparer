package yml_comparer;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.*;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 *
 * @author paveliarch
 */
public class YmlParser implements Runnable {

    protected String path;
    protected SAXParser parser;
    protected YmlHandler handler;
    protected ConcurrentMap<String, OfferHash> map;
    protected long mask;

    YmlParser(String path, SAXParserFactory parserFactory, ConcurrentMap<String, OfferHash> map, long mask) {
        this.path = convertToFileURL(path);
        this.map = map;
        this.mask = mask;
        try {
            parser = parserFactory.newSAXParser();
            handler = new YmlHandler(map, mask);
        } catch (ParserConfigurationException | SAXException ex) {
            Logger.getLogger(YmlParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected static String convertToFileURL(String filename) {
        String path = new File(filename).getAbsolutePath();
        if (File.separatorChar != '/') {
            path = path.replace(File.separatorChar, '/');
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return "file:" + path;
    }

    @Override
    public void run() {
        try {
            XMLReader _reader = parser.getXMLReader();
            _reader.setContentHandler(handler);
            _reader.setErrorHandler(handler);
            _reader.parse(path);
        } catch (IOException | SAXException ex) {
            Logger.getLogger(YmlParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
