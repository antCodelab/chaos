package club.gclmit.chaos.core.io;

import club.gclmit.chaos.core.exception.ExceptionUtils;
import club.gclmit.chaos.core.lang.text.Charsets;
import com.google.common.io.CharStreams;
import org.springframework.lang.Nullable;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;

/**
 * <p>
 * IO 流工具类
 * </p>
 *
 * @author gclm
 */
public class IOUtils extends org.springframework.util.StreamUtils {

    /**
     * closeQuietly
     *
     * @param closeable 自动关闭
     */
    public static void closeQuietly(@Nullable Closeable closeable) {
        if (closeable == null) {
            return;
        }
        if (closeable instanceof Flushable) {
            try {
                ((Flushable) closeable).flush();
            } catch (IOException ignored) {
                // ignore
            }
        }
        try {
            closeable.close();
        } catch (IOException ignored) {
            // ignore
        }
    }

    /**
     * InputStream to String utf-8
     *
     * @param input the <code>InputStream</code> to read from
     * @return the requested String
     */
    public static String copy(InputStream input) {
        return copy(input, Charsets.CHARSET_UTF_8);
    }

    /**
     * InputStream to String
     *
     * @param input   输入流
     * @param charset 编码格式
     * @return 字符串
     */
    public static String copy(@Nullable InputStream input, Charset charset) {
        try {
            return copyToString(input, charset);
        } catch (IOException e) {
            throw ExceptionUtils.unchecked(e);
        } finally {
            closeQuietly(input);
        }
    }

    /**
     * Reader to String
     *
     * @param input Reader
     * @return 字符串
     * @throws IOException IO异常
     */
    public static String copy(Reader input) throws IOException {
        return CharStreams.toString(input);
    }

    /**
     * Writes chars from a <code>String</code> to bytes on an
     * <code>OutputStream</code> using the specified character encoding.
     * <p>
     * This method uses {@link String#getBytes(String)}.
     * </p>
     *
     * @param data     the <code>String</code> to write, null ignored
     * @param output   the <code>OutputStream</code> to write to
     * @param encoding the encoding to use, null means platform default
     * @throws NullPointerException if output is null
     * @throws IOException          if an I/O error occurs
     */
    public static void write(@Nullable final String data, final OutputStream output, final Charset encoding) throws IOException {
        if (data != null) {
            output.write(data.getBytes(encoding));
        }
    }

    /**
     * 获得一个Reader
     *
     * @param in      输入流
     * @param charset 字符集
     * @return BufferedReader对象
     */
    public static BufferedReader getReader(InputStream in, Charset charset) {
        if (null == in) {
            return null;
        }

        InputStreamReader reader;
        if (null == charset) {
            reader = new InputStreamReader(in);
        } else {
            reader = new InputStreamReader(in, charset);
        }

        return new BufferedReader(reader);
    }

    /**
     * 从流中读取内容
     *
     * @param in         输入流
     * @param charset    编码格式
     * @return 内容
     */
    public static List<String> readLines(InputStream in, Charset charset) {
        BufferedReader reader = getReader(in, charset);
        try {
            return CharStreams.readLines(reader);
        } catch (IOException e) {
            throw ExceptionUtils.unchecked(e);
        }
    }
}
