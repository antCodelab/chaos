package club.gclmit.chaos.core.io;


import org.springframework.lang.Nullable;
import java.io.Writer;

/**
 * FastStringWriter，更改于 jdk CharArrayWriter
 *
 * <p>
 * 1. 去掉了锁
 * 2. 初始容量由 32 改为 64
 * 3. null 直接返回，不写入
 * </p>
 *
 * @author L.cm
 */
public class FastStringWriter extends Writer {

    /**
     * The buffer where data is stored.
     */
    private char[] buf;
    /**
     * The number of chars in the buffer.
     */
    private int count;

    /**
     * Creates a new CharArrayWriter.
     */
    public FastStringWriter() {
        this(64);
    }

    /**
     * Creates a new CharArrayWriter with the specified initial size.
     *
     * @param initialSize an int specifying the initial buffer size.
     * @throws IllegalArgumentException if initialSize is negative
     */
    public FastStringWriter(int initialSize) {
        if (initialSize < 0) {
            throw new IllegalArgumentException("Negative initial size: " + initialSize);
        }
        this.buf = new char[initialSize];
        this.count = 0;
    }

    @Override
    public void write(int c) {
        int newCount = count + 1;
        ensureCapacityInternal(newCount);
        buf[count] = (char) c;
        count = newCount;
    }

    @Override
    public void write(char[] c, int off, int len) {
        if ((off < 0) || (off > c.length) || (len < 0) ||
                ((off + len) > c.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        int newCount = count + len;
        ensureCapacityInternal(newCount);
        System.arraycopy(c, off, buf, count, len);
        count = newCount;
    }

    @Override
    public void write(@Nullable String str) {
        if (str == null) {
            return;
        }
        write(str, 0, str.length());
    }

    @Override
    public void write(@Nullable String str, int off, int len) {
        if (str == null) {
            return;
        }
        int newCount = count + len;
        ensureCapacityInternal(newCount);
        str.getChars(off, off + len, buf, count);
        count = newCount;
    }

    private void write(CharSequence s, int start, int end) {
        int len = end - start;
        ensureCapacityInternal(count + len);
        for (int i = start, j = count; i < end; i++, j++) {
            buf[j] = s.charAt(i);
        }
        count += len;
    }

    @Override
    public FastStringWriter append(@Nullable CharSequence csq) {
        if (csq == null) {
            return this;
        }
        int length = csq.length();
        if (csq instanceof String) {
            write((String) csq, 0, length);
        } else {
            write(csq, 0, csq.length());
        }
        return this;
    }

    @Override
    public FastStringWriter append(@Nullable CharSequence csq, int start, int end) {
        if (csq == null) {
            return this;
        }
        if (csq instanceof String) {
            write((String) csq, start, end);
        } else {
            write(csq, start, end);
        }
        return this;
    }

    @Override
    public FastStringWriter append(char c) {
        write(c);
        return this;
    }

    @Override
    public String toString() {
        return new String(buf, 0, count);
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }

    private void ensureCapacityInternal(int minimumCapacity) {
        if (minimumCapacity > buf.length) {
            expandCapacity(minimumCapacity);
        }
    }

    /**
     * 扩容
     *
     * @param minimumCapacity 最小容量
     */
    private void expandCapacity(int minimumCapacity) {
        int newCapacity = Math.max(buf.length << 1, minimumCapacity);
        char[] newBuff = new char[newCapacity];
        if (count > 0) {
            System.arraycopy(buf, 0, newBuff, 0, count);
        }
        buf = newBuff;
    }
}
