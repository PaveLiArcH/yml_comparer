package yml_comparer;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author paveliarch
 */
public class YmlHandler extends DefaultHandler {

    private boolean isOffer;
    private final long mask;
    private ByteBuffer buffer;
    private String offerId;
    private Stack<StringBuilder> data;
    private Stack<TreeSet<ElementHash>> hash;
    private TreeMap<String, String> attributes;
    private Stack<ChecksumProvider> checksum;
    protected ConcurrentMap<String, OfferHash> map;

    public HashMap<String, BiConsumer<String, Attributes>> preListeners;
    public HashMap<String, BiConsumer<String, String>> postListeners;

    public YmlHandler(ConcurrentMap<String, OfferHash> map, long mask) {
        this.map = map;
        this.mask = mask;
        preListeners = new HashMap<>();
        postListeners = new HashMap<>();
    }

    @Override
    public void startDocument() throws SAXException {
        buffer = ByteBuffer.allocate(Long.BYTES);
        isOffer = false;
        offerId = null;
        data = new Stack<>();
        hash = new Stack<>();
        checksum = new Stack<>();
        attributes = new TreeMap<>();
    }

    @Override
    public void startElement(String namespaceURI, String localName,
            String qName, Attributes atts) throws SAXException {
        switch (localName) {
            case "offer":
                offerId = atts.getValue("id");
                if (offerId != null) {
                    isOffer = true;
                }
                break;
            default:
                break;
        }
        if (isOffer) {
            if (preListeners.containsKey(qName)) {
                preListeners.get(qName).accept(offerId, atts);
            }
            hash.push(new TreeSet<>());
            checksum.push(new ChecksumProvider());
            data.push(new StringBuilder());
            attributes.clear();
            for (int i = 0; i < atts.getLength(); i++) {
                attributes.put(atts.getQName(i), atts.getValue(i));
            }
            byte[] _bytes;
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                _bytes = entry.getKey().getBytes();
                checksum.peek().update(_bytes, 0, _bytes.length);
                _bytes = entry.getValue().getBytes();
                checksum.peek().update(_bytes, 0, _bytes.length);
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (isOffer) {
            data.peek().append(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (isOffer) {
            String _data = data.pop().toString().trim();
            byte[] _bytes = _data.getBytes();
            ChecksumProvider _checksum = checksum.pop();
            _checksum.update(_bytes, 0, _bytes.length);
            TreeSet<ElementHash> _hash = hash.pop();
            for (ElementHash _elementHash : _hash) {
                _bytes = _elementHash.element.getBytes();
                _checksum.update(_bytes, 0, _bytes.length);
                buffer.putLong(0, _elementHash.hash);
                _bytes = buffer.array();
                _checksum.update(_bytes, 0, _bytes.length);
            }
            if (!"offer".equals(localName)) {
                ElementHash _currentHash = new ElementHash(localName, _checksum.getValue());
                hash.peek().add(_currentHash);
            } else {
                OfferHash _offerHash = new OfferHash();
                _offerHash.hash = _checksum.getValue();
                _offerHash.flags = mask;
                map.merge(offerId, _offerHash, (OfferHash t, OfferHash u) -> {
                    u.flags |= t.flags;
                    if (u.hash == null) {
                        u.hash = t.hash;
                    }
                    boolean possiblyModified = (u.flags & OfferHash.OFFER_WAS_IN_BOTH) == OfferHash.OFFER_WAS_IN_BOTH;
                    if (possiblyModified && !Objects.equals(t.hash, u.hash)) {
                        u.flags |= OfferHash.OFFER_WAS_MODIFIED;
                    }
                    return u;
                });
                isOffer = false;
            }
            if (postListeners.containsKey(qName)) {
                postListeners.get(qName).accept(offerId, _data);
            }
        }
    }

    @Override
    public void endDocument() throws SAXException {
    }
}
