package yml_comparer;

import java.util.zip.Adler32;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 *
 * @author paveliarch
 */
public class ChecksumProvider implements Checksum {

    private final CRC32 firstChecksum;
    private final Adler32 secondChecksum;

    public ChecksumProvider() {
        firstChecksum = new CRC32();
        secondChecksum = new Adler32();
    }

    @Override
    public void update(int i) {
        firstChecksum.update(i);
        secondChecksum.update(i);
    }

    @Override
    public void update(byte[] bytes, int offset, int length) {
        firstChecksum.update(bytes, offset, length);
        secondChecksum.update(bytes, offset, length);
    }

    @Override
    public long getValue() {
        long result = firstChecksum.getValue() << 32;
        result |= secondChecksum.getValue();
        return result;
    }

    @Override
    public void reset() {
        firstChecksum.reset();
        secondChecksum.reset();
    }

}
