package yml_comparer;

/**
 *
 * @author paveliarch
 */
public class OfferHash {

    public static final int OFFER_WAS_MODIFIED = 1;
    public static final int OFFER_WAS_IN_OLD = 2;
    public static final int OFFER_WAS_IN_NEW = 4;
    public static final int OFFER_WAS_IN_BOTH = OFFER_WAS_IN_OLD | OFFER_WAS_IN_NEW;
    public static final int OFFER_PICTURE_PROBLEM = 8;

    Long hash;
    long flags = 0;

    public String formatResult(String id) {
        if (flags == OFFER_WAS_IN_BOTH) {
            return null;
        }
        StringBuilder _result = new StringBuilder();
        _result.append(id);
        if ((flags & OFFER_WAS_IN_BOTH) != OFFER_WAS_IN_BOTH) {
            if ((flags & OFFER_WAS_IN_OLD) == OFFER_WAS_IN_OLD) {
                _result.append('r');
            } else {
                _result.append('n');
            }
        } else {
            if ((flags & OFFER_WAS_MODIFIED) == OFFER_WAS_MODIFIED) {
                _result.append('m');
            }
        }
        if ((flags & OFFER_PICTURE_PROBLEM) == OFFER_PICTURE_PROBLEM) {
            _result.append('p');
        }
        return _result.toString();
    }
}
