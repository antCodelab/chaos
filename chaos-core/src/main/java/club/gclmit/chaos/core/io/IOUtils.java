package club.gclmit.chaos.core.io;

import club.gclmit.chaos.core.exception.ChaosCoreException;
import club.gclmit.chaos.core.util.CharsetUtils;
import club.gclmit.chaos.core.util.StringUtils;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.Collection;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  mark commons io 修改的IO工具类
 * </p>
 *
 * @author: gclm
 * @date: 2020/4/15 5:01 下午
 * @version: V1.0
 * @since 1.8
 */
public class IOUtils {

	/**
	 * Represents the end-of-file (or stream).
	 * @since 2.5 (made public)
	 */
	public static final int EOF = -1;

	/**
	 * The Unix directory separator character.
	 */
	public static final char DIR_SEPARATOR_UNIX = '/';
	/**
	 * The Windows directory separator character.
	 */
	public static final char DIR_SEPARATOR_WINDOWS = '\\';
	/**
	 * The system directory separator character.
	 */
	public static final char DIR_SEPARATOR = File.separatorChar;
	/**
	 * The Unix line separator string.
	 */
	public static final String LINE_SEPARATOR_UNIX = "\n";
	/**
	 * The Windows line separator string.
	 */
	public static final String LINE_SEPARATOR_WINDOWS = "\r\n";
	/**
	 * The system line separator string.
	 */
	public static final String LINE_SEPARATOR;

	static {
		try (final StringBuilderWriter buf = new StringBuilderWriter(4);
			 final PrintWriter out = new PrintWriter(buf)) {
			out.println();
			LINE_SEPARATOR = buf.toString();
		}
	}

	/**
	 * The default buffer size ({@value}) to use for
	 * {@link #copyLarge(InputStream, OutputStream)}
	 * and
	 * {@link #copyLarge(Reader, Writer)}
	 */
	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

	/**
	 * The default buffer size to use for the skip() methods.
	 */
	private static final int SKIP_BUFFER_SIZE = 2048;

	// Allocated in the relevant skip method if necessary.
	/*
	 * These buffers are static and are shared between threads.
	 * This is possible because the buffers are write-only - the contents are never read.
	 *
	 * N.B. there is no need to synchronize when creating these because:
	 * - we don't care if the buffer is created multiple times (the data is ignored)
	 * - we always use the same size buffer, so if it it is recreated it will still be OK
	 * (if the buffer size were variable, we would need to synch. to ensure some other thread
	 * did not create a smaller one)
	 */
	private static char[] SKIP_CHAR_BUFFER;
	private static byte[] SKIP_BYTE_BUFFER;

	/**
	 * Instances should NOT be constructed in standard programming.
	 */
	public IOUtils() {
		super();
	}

	// Is
	//-----------------------------------------------------------------------
	/**
	 *  判断 InputStream 是否为空
	 *
	 * @author gclm
	 * @param: in
	 * @date 2020/5/2 2:11 下午
	 * @return: boolean
	 */
	public static boolean isEmpty(InputStream in) {
		try {
			return in == null || in.available() == 0;
		} catch (IOException e) {
			return in == null;
		}
	}

	/**
	 *  判断 InputStream 是否为空
	 *
	 * @author gclm
	 * @param: in
	 * @date 2020/5/2 2:12 下午
	 * @return: boolean
	 */
	public static boolean isNotEmpty(InputStream in) {
		return !isEmpty(in);
	}


	//-----------------------------------------------------------------------

	/**
	 * Closes a URLConnection.
	 *
	 * @param conn the connection to close.
	 * @since 2.4
	 */
	public static void close(final URLConnection conn) {
		if (conn instanceof HttpURLConnection) {
			((HttpURLConnection) conn).disconnect();
		}
	}

	/**
	 * 关闭<br>
	 * 关闭失败不会抛出异常
	 *
	 * @param closeable 被关闭的对象
	 */
	public static void close(AutoCloseable closeable) {
		if (null != closeable) {
			try {
				closeable.close();
			} catch (Exception e) {
				// 静默关闭
			}
		}
	}

	/**
	 * 尝试关闭指定对象<br>
	 * 判断对象如果实现了{@link AutoCloseable}，则调用之
	 *
	 * @param obj 可关闭对象
	 * @since 4.3.2
	 */
	public static void closeIfPosible(Object obj) {
		if (obj instanceof AutoCloseable) {
			close((AutoCloseable) obj);
		}
	}

	/**
	 * Returns the given reader if it is a {@link BufferedReader}, otherwise creates a BufferedReader from the given
	 * reader.
	 *
	 * @param reader the reader to wrap or return (not null)
	 * @return the given reader or a new {@link BufferedReader} for the given reader
	 * @throws NullPointerException if the input parameter is null
	 * @see #buffer(Reader)
	 * @since 2.2
	 */
	public static BufferedReader toBufferedReader(final Reader reader) {
		return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
	}

	/**
	 * Returns the given reader if it is a {@link BufferedReader}, otherwise creates a BufferedReader from the given
	 * reader.
	 *
	 * @param reader the reader to wrap or return (not null)
	 * @param size the buffer size, if a new BufferedReader is created.
	 * @return the given reader or a new {@link BufferedReader} for the given reader
	 * @throws NullPointerException if the input parameter is null
	 * @see #buffer(Reader)
	 * @since 2.5
	 */
	public static BufferedReader toBufferedReader(final Reader reader, final int size) {
		return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader, size);
	}

	/**
	 * Returns the given reader if it is already a {@link BufferedReader}, otherwise creates a BufferedReader from
	 * the given reader.
	 *
	 * @param reader the reader to wrap or return (not null)
	 * @return the given reader or a new {@link BufferedReader} for the given reader
	 * @throws NullPointerException if the input parameter is null
	 * @since 2.5
	 */
	public static BufferedReader buffer(final Reader reader) {
		return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
	}

	/**
	 * Returns the given reader if it is already a {@link BufferedReader}, otherwise creates a BufferedReader from the
	 * given reader.
	 *
	 * @param reader the reader to wrap or return (not null)
	 * @param size the buffer size, if a new BufferedReader is created.
	 * @return the given reader or a new {@link BufferedReader} for the given reader
	 * @throws NullPointerException if the input parameter is null
	 * @since 2.5
	 */
	public static BufferedReader buffer(final Reader reader, final int size) {
		return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader, size);
	}

	/**
	 * Returns the given Writer if it is already a {@link BufferedWriter}, otherwise creates a BufferedWriter from the
	 * given Writer.
	 *
	 * @param writer the Writer to wrap or return (not null)
	 * @return the given Writer or a new {@link BufferedWriter} for the given Writer
	 * @throws NullPointerException if the input parameter is null
	 * @since 2.5
	 */
	public static BufferedWriter buffer(final Writer writer) {
		return writer instanceof BufferedWriter ? (BufferedWriter) writer : new BufferedWriter(writer);
	}

	/**
	 * Returns the given Writer if it is already a {@link BufferedWriter}, otherwise creates a BufferedWriter from the
	 * given Writer.
	 *
	 * @param writer the Writer to wrap or return (not null)
	 * @param size the buffer size, if a new BufferedWriter is created.
	 * @return the given Writer or a new {@link BufferedWriter} for the given Writer
	 * @throws NullPointerException if the input parameter is null
	 * @since 2.5
	 */
	public static BufferedWriter buffer(final Writer writer, final int size) {
		return writer instanceof BufferedWriter ? (BufferedWriter) writer : new BufferedWriter(writer, size);
	}

	/**
	 * Returns the given OutputStream if it is already a {@link BufferedOutputStream}, otherwise creates a
	 * BufferedOutputStream from the given OutputStream.
	 *
	 * @param outputStream the OutputStream to wrap or return (not null)
	 * @return the given OutputStream or a new {@link BufferedOutputStream} for the given OutputStream
	 * @throws NullPointerException if the input parameter is null
	 * @since 2.5
	 */
	public static BufferedOutputStream buffer(final OutputStream outputStream) {
		// reject null early on rather than waiting for IO operation to fail
		if (outputStream == null) { // not checked by BufferedOutputStream
			throw new NullPointerException();
		}
		return outputStream instanceof BufferedOutputStream ?
				(BufferedOutputStream) outputStream : new BufferedOutputStream(outputStream);
	}

	/**
	 * Returns the given OutputStream if it is already a {@link BufferedOutputStream}, otherwise creates a
	 * BufferedOutputStream from the given OutputStream.
	 *
	 * @param outputStream the OutputStream to wrap or return (not null)
	 * @param size the buffer size, if a new BufferedOutputStream is created.
	 * @return the given OutputStream or a new {@link BufferedOutputStream} for the given OutputStream
	 * @throws NullPointerException if the input parameter is null
	 * @since 2.5
	 */
	public static BufferedOutputStream buffer(final OutputStream outputStream, final int size) {
		// reject null early on rather than waiting for IO operation to fail
		if (outputStream == null) { // not checked by BufferedOutputStream
			throw new NullPointerException();
		}
		return outputStream instanceof BufferedOutputStream ?
				(BufferedOutputStream) outputStream : new BufferedOutputStream(outputStream, size);
	}

	/**
	 * Returns the given InputStream if it is already a {@link BufferedInputStream}, otherwise creates a
	 * BufferedInputStream from the given InputStream.
	 *
	 * @param inputStream the InputStream to wrap or return (not null)
	 * @return the given InputStream or a new {@link BufferedInputStream} for the given InputStream
	 * @throws NullPointerException if the input parameter is null
	 * @since 2.5
	 */
	public static BufferedInputStream buffer(final InputStream inputStream) {
		// reject null early on rather than waiting for IO operation to fail
		if (inputStream == null) { // not checked by BufferedInputStream
			throw new NullPointerException();
		}
		return inputStream instanceof BufferedInputStream ?
				(BufferedInputStream) inputStream : new BufferedInputStream(inputStream);
	}

	/**
	 * Returns the given InputStream if it is already a {@link BufferedInputStream}, otherwise creates a
	 * BufferedInputStream from the given InputStream.
	 *
	 * @param inputStream the InputStream to wrap or return (not null)
	 * @param size the buffer size, if a new BufferedInputStream is created.
	 * @return the given InputStream or a new {@link BufferedInputStream} for the given InputStream
	 * @throws NullPointerException if the input parameter is null
	 * @since 2.5
	 */
	public static BufferedInputStream buffer(final InputStream inputStream, final int size) {
		// reject null early on rather than waiting for IO operation to fail
		if (inputStream == null) { // not checked by BufferedInputStream
			throw new NullPointerException();
		}
		return inputStream instanceof BufferedInputStream ?
				(BufferedInputStream) inputStream : new BufferedInputStream(inputStream, size);
	}

	// read toByteArray
	//-----------------------------------------------------------------------

	/**
	 * Gets the contents of an <code>InputStream</code> as a <code>byte[]</code>.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedInputStream</code>.
	 *
	 * @param input the <code>InputStream</code> to read from
	 * @return the requested byte array
	 * @throws NullPointerException if the input is null
	 * @throws IOException          if an I/O error occurs
	 */
	public static byte[] toByteArray(final InputStream input) throws IOException {
		try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			copy(input, output);
			return output.toByteArray();
		}
	}

	/**
	 * Gets contents of an <code>InputStream</code> as a <code>byte[]</code>.
	 * Use this method instead of <code>toByteArray(InputStream)</code>
	 * when <code>InputStream</code> size is known.
	 * <b>NOTE:</b> the method checks that the length can safely be cast to an int without truncation
	 * before using {@link IOUtils#toByteArray(java.io.InputStream, int)} to read into the byte array.
	 * (Arrays can have no more than Integer.MAX_VALUE entries anyway)
	 *
	 * @param input the <code>InputStream</code> to read from
	 * @param size the size of <code>InputStream</code>
	 * @return the requested byte array
	 * @throws IOException              if an I/O error occurs or <code>InputStream</code> size differ from parameter
	 * size
	 * @throws IllegalArgumentException if size is less than zero or size is greater than Integer.MAX_VALUE
	 * @see IOUtils#toByteArray(java.io.InputStream, int)
	 * @since 2.1
	 */
	public static byte[] toByteArray(final InputStream input, final long size) throws IOException {

		if (size > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Size cannot be greater than Integer max value: " + size);
		}

		return toByteArray(input, (int) size);
	}

	/**
	 * Gets the contents of an <code>InputStream</code> as a <code>byte[]</code>.
	 * Use this method instead of <code>toByteArray(InputStream)</code>
	 * when <code>InputStream</code> size is known
	 *
	 * @param input the <code>InputStream</code> to read from
	 * @param size the size of <code>InputStream</code>
	 * @return the requested byte array
	 * @throws IOException              if an I/O error occurs or <code>InputStream</code> size differ from parameter
	 * size
	 * @throws IllegalArgumentException if size is less than zero
	 * @since 2.1
	 */
	public static byte[] toByteArray(final InputStream input, final int size) throws IOException {

		if (size < 0) {
			throw new IllegalArgumentException("Size must be equal or greater than zero: " + size);
		}

		if (size == 0) {
			return new byte[0];
		}

		final byte[] data = new byte[size];
		int offset = 0;
		int read;

		while (offset < size && (read = input.read(data, offset, size - offset)) != EOF) {
			offset += read;
		}

		if (offset != size) {
			throw new IOException("Unexpected read size. current: " + offset + ", expected: " + size);
		}

		return data;
	}

	/**
	 * Gets the contents of a <code>Reader</code> as a <code>byte[]</code>
	 * using the specified character encoding.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedReader</code>.
	 *
	 * @param input the <code>Reader</code> to read from
	 * @param encoding the encoding to use, null means platform default
	 * @return the requested byte array
	 * @throws NullPointerException if the input is null
	 * @throws IOException          if an I/O error occurs
	 * @since 2.3
	 */
	public static byte[] toByteArray(final Reader input, final Charset encoding) throws IOException {
		try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			copy(input, output, encoding);
			return output.toByteArray();
		}
	}

	/**
	 * Gets the contents of a <code>Reader</code> as a <code>byte[]</code>
	 * using the specified character encoding.
	 * <p>
	 * Character encoding names can be found at
	 * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedReader</code>.
	 *
	 * @param input the <code>Reader</code> to read from
	 * @param encoding the encoding to use, null means platform default
	 * @return the requested byte array
	 * @throws NullPointerException                         if the input is null
	 * @throws IOException                                  if an I/O error occurs
	 * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
	 *                                                      .UnsupportedEncodingException} in version 2.2 if the
	 *                                                      encoding is not supported.
	 * @since 1.1
	 */
	public static byte[] toByteArray(final Reader input, final String encoding) throws IOException {
		return toByteArray(input, CharsetUtils.toCharset(encoding));
	}

	/**
	 * Gets the contents of a <code>URI</code> as a <code>byte[]</code>.
	 *
	 * @param uri the <code>URI</code> to read
	 * @return the requested byte array
	 * @throws NullPointerException if the uri is null
	 * @throws IOException          if an I/O exception occurs
	 * @since 2.4
	 */
	public static byte[] toByteArray(final URI uri) throws IOException {
		return IOUtils.toByteArray(uri.toURL());
	}

	/**
	 * Gets the contents of a <code>URL</code> as a <code>byte[]</code>.
	 *
	 * @param url the <code>URL</code> to read
	 * @return the requested byte array
	 * @throws NullPointerException if the input is null
	 * @throws IOException          if an I/O exception occurs
	 * @since 2.4
	 */
	public static byte[] toByteArray(final URL url) throws IOException {
		final URLConnection conn = url.openConnection();
		try {
			return IOUtils.toByteArray(conn);
		} finally {
			close(conn);
		}
	}

	/**
	 * Gets the contents of a <code>URLConnection</code> as a <code>byte[]</code>.
	 *
	 * @param urlConn the <code>URLConnection</code> to read
	 * @return the requested byte array
	 * @throws NullPointerException if the urlConn is null
	 * @throws IOException          if an I/O exception occurs
	 * @since 2.4
	 */
	public static byte[] toByteArray(final URLConnection urlConn) throws IOException {
		try (InputStream inputStream = urlConn.getInputStream()) {
			return IOUtils.toByteArray(inputStream);
		}
	}

	// read char[]
	//-----------------------------------------------------------------------
	
	/**
	 * Gets the contents of an <code>InputStream</code> as a character array
	 * using the specified character encoding.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedInputStream</code>.
	 *
	 * @param is the <code>InputStream</code> to read from
	 * @param encoding the encoding to use, null means platform default
	 * @return the requested character array
	 * @throws NullPointerException if the input is null
	 * @throws IOException          if an I/O error occurs
	 * @since 2.3
	 */
	public static char[] toCharArray(final InputStream is, final Charset encoding)
			throws IOException {
		final CharArrayWriter output = new CharArrayWriter();
		copy(is, output, encoding);
		return output.toCharArray();
	}

	/**
	 * Gets the contents of an <code>InputStream</code> as a character array
	 * using the specified character encoding.
	 * <p>
	 * Character encoding names can be found at
	 * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedInputStream</code>.
	 *
	 * @param is the <code>InputStream</code> to read from
	 * @param encoding the encoding to use, null means platform default
	 * @return the requested character array
	 * @throws NullPointerException                         if the input is null
	 * @throws IOException                                  if an I/O error occurs
	 * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
	 *                                                      .UnsupportedEncodingException} in version 2.2 if the
	 *                                                      encoding is not supported.
	 * @since 1.1
	 */
	public static char[] toCharArray(final InputStream is, final String encoding) throws IOException {
		return toCharArray(is, CharsetUtils.toCharset(encoding));
	}

	/**
	 * Gets the contents of a <code>Reader</code> as a character array.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedReader</code>.
	 *
	 * @param input the <code>Reader</code> to read from
	 * @return the requested character array
	 * @throws NullPointerException if the input is null
	 * @throws IOException          if an I/O error occurs
	 * @since 1.1
	 */
	public static char[] toCharArray(final Reader input) throws IOException {
		final CharArrayWriter sw = new CharArrayWriter();
		copy(input, sw);
		return sw.toCharArray();
	}

	// resources
	//-----------------------------------------------------------------------

	/**
	 * Gets the contents of a classpath resource as a String using the
	 * specified character encoding.
	 *
	 * <p>
	 * It is expected the given <code>name</code> to be absolute. The
	 * behavior is not well-defined otherwise.
	 * </p>
	 *
	 * @param name     name of the desired resource
	 * @param encoding the encoding to use, null means platform default
	 * @return the requested String
	 * @throws IOException if an I/O error occurs
	 *
	 * @since 2.6
	 */
	public static String resourceToString(final String name, final Charset encoding) throws IOException {
		return resourceToString(name, encoding, null);
	}

	/**
	 * Gets the contents of a classpath resource as a String using the
	 * specified character encoding.
	 *
	 * <p>
	 * It is expected the given <code>name</code> to be absolute. The
	 * behavior is not well-defined otherwise.
	 * </p>
	 *
	 * @param name     name of the desired resource
	 * @param encoding the encoding to use, null means platform default
	 * @param classLoader the class loader that the resolution of the resource is delegated to
	 * @return the requested String
	 * @throws IOException if an I/O error occurs
	 *
	 * @since 2.6
	 */
	public static String resourceToString(final String name, final Charset encoding, final ClassLoader classLoader) throws IOException {
		return StringUtils.toString(resourceToURL(name, classLoader), encoding);
	}

	/**
	 * Gets the contents of a classpath resource as a byte array.
	 *
	 * <p>
	 * It is expected the given <code>name</code> to be absolute. The
	 * behavior is not well-defined otherwise.
	 * </p>
	 *
	 * @param name name of the desired resource
	 * @return the requested byte array
	 * @throws IOException if an I/O error occurs
	 *
	 * @since 2.6
	 */
	public static byte[] resourceToByteArray(final String name) throws IOException {
		return resourceToByteArray(name, null);
	}

	/**
	 * Gets the contents of a classpath resource as a byte array.
	 *
	 * <p>
	 * It is expected the given <code>name</code> to be absolute. The
	 * behavior is not well-defined otherwise.
	 * </p>
	 *
	 * @param name name of the desired resource
	 * @param classLoader the class loader that the resolution of the resource is delegated to
	 * @return the requested byte array
	 * @throws IOException if an I/O error occurs
	 *
	 * @since 2.6
	 */
	public static byte[] resourceToByteArray(final String name, final ClassLoader classLoader) throws IOException {
		return toByteArray(resourceToURL(name, classLoader));
	}

	/**
	 * Gets a URL pointing to the given classpath resource.
	 *
	 * <p>
	 * It is expected the given <code>name</code> to be absolute. The
	 * behavior is not well-defined otherwise.
	 * </p>
	 *
	 * @param name name of the desired resource
	 * @return the requested URL
	 * @throws IOException if an I/O error occurs
	 *
	 * @since 2.6
	 */
	public static URL resourceToURL(final String name) throws IOException {
		return resourceToURL(name, null);
	}

	/**
	 * Gets a URL pointing to the given classpath resource.
	 *
	 * <p>
	 * It is expected the given <code>name</code> to be absolute. The
	 * behavior is not well-defined otherwise.
	 * </p>
	 *
	 * @param name        name of the desired resource
	 * @param classLoader the class loader that the resolution of the resource is delegated to
	 * @return the requested URL
	 * @throws IOException if an I/O error occurs
	 *
	 * @since 2.6
	 */
	public static URL resourceToURL(final String name, final ClassLoader classLoader) throws IOException {
		// What about the thread context class loader?
		// What about the system class loader?
		final URL resource = classLoader == null ? IOUtils.class.getResource(name) : classLoader.getResource(name);

		if (resource == null) {
			throw new IOException("Resource not found: " + name);
		}

		return resource;
	}

	// readLines
	//-----------------------------------------------------------------------

	/**
	 * Gets the contents of an <code>InputStream</code> as a list of Strings,
	 * one entry per line, using the specified character encoding.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedInputStream</code>.
	 *
	 * @param input the <code>InputStream</code> to read from, not null
	 * @param encoding the encoding to use, null means platform default
	 * @return the list of Strings, never null
	 * @throws NullPointerException if the input is null
	 * @throws IOException          if an I/O error occurs
	 * @since 2.3
	 */
	public static List<String> readLines(final InputStream input, final Charset encoding){
		final InputStreamReader reader = new InputStreamReader(input, CharsetUtils.toCharset(encoding));
		return readLines(reader);
	}

	/**
	 * Gets the contents of an <code>InputStream</code> as a list of Strings,
	 * one entry per line, using the specified character encoding.
	 * <p>
	 * Character encoding names can be found at
	 * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedInputStream</code>.
	 *
	 * @param input the <code>InputStream</code> to read from, not null
	 * @param encoding the encoding to use, null means platform default
	 * @return the list of Strings, never null
	 * @throws NullPointerException                         if the input is null
	 * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
	 *                                                      .UnsupportedEncodingException} in version 2.2 if the
	 *                                                      encoding is not supported.
	 * @since 1.1
	 */
	public static List<String> readLines(final InputStream input, final String encoding) {
		return readLines(input, CharsetUtils.toCharset(encoding));
	}

	/**
	 * Gets the contents of a <code>Reader</code> as a list of Strings,
	 * one entry per line.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedReader</code>.
	 *
	 * @param input the <code>Reader</code> to read from, not null
	 * @return the list of Strings, never null
	 * @since 1.1
	 */
	public static List<String> readLines(final Reader input){
		final BufferedReader reader = toBufferedReader(input);
		final List<String> list = new ArrayList<>();
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				list.add(line);
				line = reader.readLine();
			}
			return list;
		} catch (IOException e) {
			throw new ChaosCoreException("readLines 发生异常",e);
		}
	}

	//-----------------------------------------------------------------------

	/**
	 * Converts the specified CharSequence to an input stream, encoded as bytes
	 * using the specified character encoding.
	 *
	 * @param input the CharSequence to convert
	 * @param encoding the encoding to use, null means platform default
	 * @return an input stream
	 * @since 2.3
	 */
	public static InputStream toInputStream(final CharSequence input, final Charset encoding) {
		return toInputStream(input.toString(), encoding);
	}

	/**
	 * Converts the specified CharSequence to an input stream, encoded as bytes
	 * using the specified character encoding.
	 * <p>
	 * Character encoding names can be found at
	 * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
	 *
	 * @param input the CharSequence to convert
	 * @param encoding the encoding to use, null means platform default
	 * @return an input stream
	 * @throws IOException                                  if the encoding is invalid
	 * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
	 *                                                      .UnsupportedEncodingException} in version 2.2 if the
	 *                                                      encoding is not supported.
	 * @since 2.0
	 */
	public static InputStream toInputStream(final CharSequence input, final String encoding) throws IOException {
		return toInputStream(input, CharsetUtils.toCharset(encoding));
	}

	//-----------------------------------------------------------------------

	/**
	 * Converts the specified string to an input stream, encoded as bytes
	 * using the specified character encoding.
	 *
	 * @param input the string to convert
	 * @param encoding the encoding to use, null means platform default
	 * @return an input stream
	 * @since 2.3
	 */
	public static InputStream toInputStream(final String input, final Charset encoding) {
		return new ByteArrayInputStream(input.getBytes(CharsetUtils.toCharset(encoding)));
	}

	/**
	 * Converts the specified string to an input stream, encoded as bytes
	 * using the specified character encoding.
	 * <p>
	 * Character encoding names can be found at
	 * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
	 *
	 * @param input the string to convert
	 * @param encoding the encoding to use, null means platform default
	 * @return an input stream
	 * @throws IOException                                  if the encoding is invalid
	 * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
	 *                                                      .UnsupportedEncodingException} in version 2.2 if the
	 *                                                      encoding is not supported.
	 * @since 1.1
	 */
	public static InputStream toInputStream(final String input, final String encoding) throws IOException {
		final byte[] bytes = input.getBytes(CharsetUtils.toCharset(encoding));
		return new ByteArrayInputStream(bytes);
	}

	// write byte[]
	//-----------------------------------------------------------------------

	/**
	 * Writes bytes from a <code>byte[]</code> to an <code>OutputStream</code>.
	 *
	 * @param data the byte array to write, do not modify during output,
	 * null ignored
	 * @param output the <code>OutputStream</code> to write to
	 * @throws NullPointerException if output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 1.1
	 */
	public static void write(final byte[] data, final OutputStream output)
			throws IOException {
		if (data != null) {
			output.write(data);
		}
	}

	/**
	 * Writes bytes from a <code>byte[]</code> to an <code>OutputStream</code> using chunked writes.
	 * This is intended for writing very large byte arrays which might otherwise cause excessive
	 * memory usage if the native code has to allocate a copy.
	 *
	 * @param data the byte array to write, do not modify during output,
	 * null ignored
	 * @param output the <code>OutputStream</code> to write to
	 * @throws NullPointerException if output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 2.5
	 */
	public static void writeChunked(final byte[] data, final OutputStream output)
			throws IOException {
		if (data != null) {
			int bytes = data.length;
			int offset = 0;
			while (bytes > 0) {
				final int chunk = Math.min(bytes, DEFAULT_BUFFER_SIZE);
				output.write(data, offset, chunk);
				bytes -= chunk;
				offset += chunk;
			}
		}
	}

	/**
	 * Writes bytes from a <code>byte[]</code> to chars on a <code>Writer</code>
	 * using the specified character encoding.
	 * <p>
	 * This method uses {@link String#String(byte[], String)}.
	 *
	 * @param data the byte array to write, do not modify during output,
	 * null ignored
	 * @param output the <code>Writer</code> to write to
	 * @param encoding the encoding to use, null means platform default
	 * @throws NullPointerException if output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 2.3
	 */
	public static void write(final byte[] data, final Writer output, final Charset encoding) throws IOException {
		if (data != null) {
			output.write(new String(data, CharsetUtils.toCharset(encoding)));
		}
	}

	/**
	 * Writes bytes from a <code>byte[]</code> to chars on a <code>Writer</code>
	 * using the specified character encoding.
	 * <p>
	 * Character encoding names can be found at
	 * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
	 * <p>
	 * This method uses {@link String#String(byte[], String)}.
	 *
	 * @param data the byte array to write, do not modify during output,
	 * null ignored
	 * @param output the <code>Writer</code> to write to
	 * @param encoding the encoding to use, null means platform default
	 * @throws NullPointerException                         if output is null
	 * @throws IOException                                  if an I/O error occurs
	 * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
	 *                                                      .UnsupportedEncodingException} in version 2.2 if the
	 *                                                      encoding is not supported.
	 * @since 1.1
	 */
	public static void write(final byte[] data, final Writer output, final String encoding) throws IOException {
		write(data, output, CharsetUtils.toCharset(encoding));
	}

	// write char[]
	//-----------------------------------------------------------------------

	/**
	 * Writes chars from a <code>char[]</code> to a <code>Writer</code>
	 *
	 * @param data the char array to write, do not modify during output,
	 * null ignored
	 * @param output the <code>Writer</code> to write to
	 * @throws NullPointerException if output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 1.1
	 */
	public static void write(final char[] data, final Writer output) throws IOException {
		if (data != null) {
			output.write(data);
		}
	}

	/**
	 * Writes chars from a <code>char[]</code> to a <code>Writer</code> using chunked writes.
	 * This is intended for writing very large byte arrays which might otherwise cause excessive
	 * memory usage if the native code has to allocate a copy.
	 *
	 * @param data the char array to write, do not modify during output,
	 * null ignored
	 * @param output the <code>Writer</code> to write to
	 * @throws NullPointerException if output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 2.5
	 */
	public static void writeChunked(final char[] data, final Writer output) throws IOException {
		if (data != null) {
			int bytes = data.length;
			int offset = 0;
			while (bytes > 0) {
				final int chunk = Math.min(bytes, DEFAULT_BUFFER_SIZE);
				output.write(data, offset, chunk);
				bytes -= chunk;
				offset += chunk;
			}
		}
	}

	/**
	 * Writes chars from a <code>char[]</code> to bytes on an
	 * <code>OutputStream</code> using the specified character encoding.
	 * <p>
	 * This method uses {@link String#String(char[])} and
	 * {@link String#getBytes(String)}.
	 *
	 * @param data the char array to write, do not modify during output,
	 * null ignored
	 * @param output the <code>OutputStream</code> to write to
	 * @param encoding the encoding to use, null means platform default
	 * @throws NullPointerException if output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 2.3
	 */
	public static void write(final char[] data, final OutputStream output, final Charset encoding) throws IOException {
		if (data != null) {
			output.write(new String(data).getBytes(CharsetUtils.toCharset(encoding)));
		}
	}

	/**
	 * Writes chars from a <code>char[]</code> to bytes on an
	 * <code>OutputStream</code> using the specified character encoding.
	 * <p>
	 * Character encoding names can be found at
	 * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
	 * <p>
	 * This method uses {@link String#String(char[])} and
	 * {@link String#getBytes(String)}.
	 *
	 * @param data the char array to write, do not modify during output,
	 * null ignored
	 * @param output the <code>OutputStream</code> to write to
	 * @param encoding the encoding to use, null means platform default
	 * @throws NullPointerException                         if output is null
	 * @throws IOException                                  if an I/O error occurs
	 * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
	 * .UnsupportedEncodingException} in version 2.2 if the encoding is not supported.
	 * @since 1.1
	 */
	public static void write(final char[] data, final OutputStream output, final String encoding)
			throws IOException {
		write(data, output, CharsetUtils.toCharset(encoding));
	}

	// write CharSequence
	//-----------------------------------------------------------------------

	/**
	 * Writes chars from a <code>CharSequence</code> to a <code>Writer</code>.
	 *
	 * @param data the <code>CharSequence</code> to write, null ignored
	 * @param output the <code>Writer</code> to write to
	 * @throws NullPointerException if output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 2.0
	 */
	public static void write(final CharSequence data, final Writer output) throws IOException {
		if (data != null) {
			write(data.toString(), output);
		}
	}

	/**
	 * Writes chars from a <code>CharSequence</code> to bytes on an
	 * <code>OutputStream</code> using the specified character encoding.
	 * <p>
	 * This method uses {@link String#getBytes(String)}.
	 *
	 * @param data the <code>CharSequence</code> to write, null ignored
	 * @param output the <code>OutputStream</code> to write to
	 * @param encoding the encoding to use, null means platform default
	 * @throws NullPointerException if output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 2.3
	 */
	public static void write(final CharSequence data, final OutputStream output, final Charset encoding)
			throws IOException {
		if (data != null) {
			write(data.toString(), output, encoding);
		}
	}

	/**
	 * Writes chars from a <code>CharSequence</code> to bytes on an
	 * <code>OutputStream</code> using the specified character encoding.
	 * <p>
	 * Character encoding names can be found at
	 * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
	 * <p>
	 * This method uses {@link String#getBytes(String)}.
	 *
	 * @param data the <code>CharSequence</code> to write, null ignored
	 * @param output the <code>OutputStream</code> to write to
	 * @param encoding the encoding to use, null means platform default
	 * @throws NullPointerException        if output is null
	 * @throws IOException                 if an I/O error occurs
	 * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
	 * .UnsupportedEncodingException} in version 2.2 if the encoding is not supported.
	 * @since 2.0
	 */
	public static void write(final CharSequence data, final OutputStream output, final String encoding)
			throws IOException {
		write(data, output, CharsetUtils.toCharset(encoding));
	}

	// write String
	//-----------------------------------------------------------------------

	/**
	 * Writes chars from a <code>String</code> to a <code>Writer</code>.
	 *
	 * @param data the <code>String</code> to write, null ignored
	 * @param output the <code>Writer</code> to write to
	 * @throws NullPointerException if output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 1.1
	 */
	public static void write(final String data, final Writer output) throws IOException {
		if (data != null) {
			output.write(data);
		}
	}

	/**
	 * Writes chars from a <code>String</code> to bytes on an
	 * <code>OutputStream</code> using the specified character encoding.
	 * <p>
	 * This method uses {@link String#getBytes(String)}.
	 *
	 * @param data the <code>String</code> to write, null ignored
	 * @param output the <code>OutputStream</code> to write to
	 * @param encoding the encoding to use, null means platform default
	 * @throws NullPointerException if output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 2.3
	 */
	public static void write(final String data, final OutputStream output, final Charset encoding) throws IOException {
		if (data != null) {
			output.write(data.getBytes(CharsetUtils.toCharset(encoding)));
		}
	}

	/**
	 * Writes chars from a <code>String</code> to bytes on an
	 * <code>OutputStream</code> using the specified character encoding.
	 * <p>
	 * Character encoding names can be found at
	 * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
	 * <p>
	 * This method uses {@link String#getBytes(String)}.
	 *
	 * @param data the <code>String</code> to write, null ignored
	 * @param output the <code>OutputStream</code> to write to
	 * @param encoding the encoding to use, null means platform default
	 * @throws NullPointerException        if output is null
	 * @throws IOException                 if an I/O error occurs
	 * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
	 * .UnsupportedEncodingException} in version 2.2 if the encoding is not supported.
	 * @since 1.1
	 */
	public static void write(final String data, final OutputStream output, final String encoding)
			throws IOException {
		write(data, output, CharsetUtils.toCharset(encoding));
	}

	// writeLines
	//-----------------------------------------------------------------------

	/**
	 * Writes the <code>toString()</code> value of each item in a collection to
	 * an <code>OutputStream</code> line by line, using the specified character
	 * encoding and the specified line ending.
	 *
	 * @param lines the lines to write, null entries produce blank lines
	 * @param lineEnding the line separator to use, null is system default
	 * @param output the <code>OutputStream</code> to write to, not null, not closed
	 * @param encoding the encoding to use, null means platform default
	 * @throws NullPointerException if the output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 2.3
	 */
	public static void writeLines(final Collection<?> lines, String lineEnding, final OutputStream output,
								  final Charset encoding) throws IOException {
		if (lines == null) {
			return;
		}
		if (lineEnding == null) {
			lineEnding = LINE_SEPARATOR;
		}
		final Charset cs = CharsetUtils.toCharset(encoding);
		for (final Object line : lines) {
			if (line != null) {
				output.write(line.toString().getBytes(cs));
			}
			output.write(lineEnding.getBytes(cs));
		}
	}

	/**
	 * Writes the <code>toString()</code> value of each item in a collection to
	 * an <code>OutputStream</code> line by line, using the specified character
	 * encoding and the specified line ending.
	 * <p>
	 * Character encoding names can be found at
	 * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
	 *
	 * @param lines the lines to write, null entries produce blank lines
	 * @param lineEnding the line separator to use, null is system default
	 * @param output the <code>OutputStream</code> to write to, not null, not closed
	 * @param encoding the encoding to use, null means platform default
	 * @throws NullPointerException                         if the output is null
	 * @throws IOException                                  if an I/O error occurs
	 * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
	 *                                                      .UnsupportedEncodingException} in version 2.2 if the
	 *                                                      encoding is not supported.
	 * @since 1.1
	 */
	public static void writeLines(final Collection<?> lines, final String lineEnding,
								  final OutputStream output, final String encoding) throws IOException {
		writeLines(lines, lineEnding, output, CharsetUtils.toCharset(encoding));
	}

	/**
	 * Writes the <code>toString()</code> value of each item in a collection to
	 * a <code>Writer</code> line by line, using the specified line ending.
	 *
	 * @param lines the lines to write, null entries produce blank lines
	 * @param lineEnding the line separator to use, null is system default
	 * @param writer the <code>Writer</code> to write to, not null, not closed
	 * @throws NullPointerException if the input is null
	 * @throws IOException          if an I/O error occurs
	 * @since 1.1
	 */
	public static void writeLines(final Collection<?> lines, String lineEnding,
								  final Writer writer) throws IOException {
		if (lines == null) {
			return;
		}
		if (lineEnding == null) {
			lineEnding = LINE_SEPARATOR;
		}
		for (final Object line : lines) {
			if (line != null) {
				writer.write(line.toString());
			}
			writer.write(lineEnding);
		}
	}

	// copy from InputStream
	//-----------------------------------------------------------------------

	/**
	 * Copies bytes from an <code>InputStream</code> to an
	 * <code>OutputStream</code>.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedInputStream</code>.
	 * <p>
	 * Large streams (over 2GB) will return a bytes copied value of
	 * <code>-1</code> after the copy has completed since the correct
	 * number of bytes cannot be returned as an int. For large streams
	 * use the <code>copyLarge(InputStream, OutputStream)</code> method.
	 *
	 * @param input the <code>InputStream</code> to read from
	 * @param output the <code>OutputStream</code> to write to
	 * @return the number of bytes copied, or -1 if &gt; Integer.MAX_VALUE
	 * @throws NullPointerException if the input or output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 1.1
	 */
	public static int copy(final InputStream input, final OutputStream output) throws IOException {
		final long count = copyLarge(input, output);
		if (count > Integer.MAX_VALUE) {
			return -1;
		}
		return (int) count;
	}

	/**
	 * Copies bytes from an <code>InputStream</code> to an <code>OutputStream</code> using an internal buffer of the
	 * given size.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a <code>BufferedInputStream</code>.
	 * <p>
	 *
	 * @param input the <code>InputStream</code> to read from
	 * @param output the <code>OutputStream</code> to write to
	 * @param bufferSize the bufferSize used to copy from the input to the output
	 * @return the number of bytes copied
	 * @throws NullPointerException if the input or output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 2.5
	 */
	public static long copy(final InputStream input, final OutputStream output, final int bufferSize)
			throws IOException {
		return copyLarge(input, output, new byte[bufferSize]);
	}

	/**
	 * Copies bytes from a large (over 2GB) <code>InputStream</code> to an
	 * <code>OutputStream</code>.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedInputStream</code>.
	 * <p>
	 * The buffer size is given by {@link #DEFAULT_BUFFER_SIZE}.
	 *
	 * @param input the <code>InputStream</code> to read from
	 * @param output the <code>OutputStream</code> to write to
	 * @return the number of bytes copied
	 * @throws NullPointerException if the input or output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 1.3
	 */
	public static long copyLarge(final InputStream input, final OutputStream output)
			throws IOException {
		return copy(input, output, DEFAULT_BUFFER_SIZE);
	}

	/**
	 * Copies bytes from a large (over 2GB) <code>InputStream</code> to an
	 * <code>OutputStream</code>.
	 * <p>
	 * This method uses the provided buffer, so there is no need to use a
	 * <code>BufferedInputStream</code>.
	 * <p>
	 *
	 * @param input the <code>InputStream</code> to read from
	 * @param output the <code>OutputStream</code> to write to
	 * @param buffer the buffer to use for the copy
	 * @return the number of bytes copied
	 * @throws NullPointerException if the input or output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 2.2
	 */
	public static long copyLarge(final InputStream input, final OutputStream output, final byte[] buffer)
			throws IOException {
		long count = 0;
		int n;
		while (EOF != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}

	/**
	 * Copies some or all bytes from a large (over 2GB) <code>InputStream</code> to an
	 * <code>OutputStream</code>, optionally skipping input bytes.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedInputStream</code>.
	 * </p>
	 * <p>
	 * Note that the implementation uses {@link #skip(InputStream, long)}.
	 * This means that the method may be considerably less efficient than using the actual skip implementation,
	 * this is done to guarantee that the correct number of characters are skipped.
	 * </p>
	 * The buffer size is given by {@link #DEFAULT_BUFFER_SIZE}.
	 *
	 * @param input the <code>InputStream</code> to read from
	 * @param output the <code>OutputStream</code> to write to
	 * @param inputOffset : number of bytes to skip from input before copying
	 * -ve values are ignored
	 * @param length : number of bytes to copy. -ve means all
	 * @return the number of bytes copied
	 * @throws NullPointerException if the input or output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 2.2
	 */
	public static long copyLarge(final InputStream input, final OutputStream output, final long inputOffset,
								 final long length) throws IOException {
		return copyLarge(input, output, inputOffset, length, new byte[DEFAULT_BUFFER_SIZE]);
	}

	/**
	 * Copies some or all bytes from a large (over 2GB) <code>InputStream</code> to an
	 * <code>OutputStream</code>, optionally skipping input bytes.
	 * <p>
	 * This method uses the provided buffer, so there is no need to use a
	 * <code>BufferedInputStream</code>.
	 * </p>
	 * <p>
	 * Note that the implementation uses {@link #skip(InputStream, long)}.
	 * This means that the method may be considerably less efficient than using the actual skip implementation,
	 * this is done to guarantee that the correct number of characters are skipped.
	 * </p>
	 *
	 * @param input the <code>InputStream</code> to read from
	 * @param output the <code>OutputStream</code> to write to
	 * @param inputOffset : number of bytes to skip from input before copying
	 * -ve values are ignored
	 * @param length : number of bytes to copy. -ve means all
	 * @param buffer the buffer to use for the copy
	 * @return the number of bytes copied
	 * @throws NullPointerException if the input or output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 2.2
	 */
	public static long copyLarge(final InputStream input, final OutputStream output,
								 final long inputOffset, final long length, final byte[] buffer) throws IOException {
		if (inputOffset > 0) {
			skipFully(input, inputOffset);
		}
		if (length == 0) {
			return 0;
		}
		final int bufferLength = buffer.length;
		int bytesToRead = bufferLength;
		if (length > 0 && length < bufferLength) {
			bytesToRead = (int) length;
		}
		int read;
		long totalRead = 0;
		while (bytesToRead > 0 && EOF != (read = input.read(buffer, 0, bytesToRead))) {
			output.write(buffer, 0, read);
			totalRead += read;
			if (length > 0) { // only adjust length if not reading to the end
				// Note the cast must work because buffer.length is an integer
				bytesToRead = (int) Math.min(length - totalRead, bufferLength);
			}
		}
		return totalRead;
	}

	/**
	 * Copies bytes from an <code>InputStream</code> to chars on a
	 * <code>Writer</code> using the specified character encoding.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedInputStream</code>.
	 * <p>
	 * This method uses {@link InputStreamReader}.
	 *
	 * @param input the <code>InputStream</code> to read from
	 * @param output the <code>Writer</code> to write to
	 * @param inputEncoding the encoding to use for the input stream, null means platform default
	 * @throws NullPointerException if the input or output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 2.3
	 */
	public static void copy(final InputStream input, final Writer output, final Charset inputEncoding)
			throws IOException {
		final InputStreamReader in = new InputStreamReader(input, CharsetUtils.toCharset(inputEncoding));
		copy(in, output);
	}

	/**
	 * Copies bytes from an <code>InputStream</code> to chars on a
	 * <code>Writer</code> using the specified character encoding.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedInputStream</code>.
	 * <p>
	 * Character encoding names can be found at
	 * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
	 * <p>
	 * This method uses {@link InputStreamReader}.
	 *
	 * @param input the <code>InputStream</code> to read from
	 * @param output the <code>Writer</code> to write to
	 * @param inputEncoding the encoding to use for the InputStream, null means platform default
	 * @throws NullPointerException                         if the input or output is null
	 * @throws IOException                                  if an I/O error occurs
	 * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
	 *                                                      .UnsupportedEncodingException} in version 2.2 if the
	 *                                                      encoding is not supported.
	 * @since 1.1
	 */
	public static void copy(final InputStream input, final Writer output, final String inputEncoding)
			throws IOException {
		copy(input, output, CharsetUtils.toCharset(inputEncoding));
	}

	// copy from Reader
	//-----------------------------------------------------------------------

	/**
	 * Copies chars from a <code>Reader</code> to a <code>Writer</code>.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedReader</code>.
	 * <p>
	 * Large streams (over 2GB) will return a chars copied value of
	 * <code>-1</code> after the copy has completed since the correct
	 * number of chars cannot be returned as an int. For large streams
	 * use the <code>copyLarge(Reader, Writer)</code> method.
	 *
	 * @param input the <code>Reader</code> to read from
	 * @param output the <code>Writer</code> to write to
	 * @return the number of characters copied, or -1 if &gt; Integer.MAX_VALUE
	 * @throws NullPointerException if the input or output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 1.1
	 */
	public static int copy(final Reader input, final Writer output) throws IOException {
		final long count = copyLarge(input, output);
		if (count > Integer.MAX_VALUE) {
			return -1;
		}
		return (int) count;
	}

	/**
	 * Copies chars from a large (over 2GB) <code>Reader</code> to a <code>Writer</code>.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedReader</code>.
	 * <p>
	 * The buffer size is given by {@link #DEFAULT_BUFFER_SIZE}.
	 *
	 * @param input the <code>Reader</code> to read from
	 * @param output the <code>Writer</code> to write to
	 * @return the number of characters copied
	 * @throws NullPointerException if the input or output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 1.3
	 */
	public static long copyLarge(final Reader input, final Writer output) throws IOException {
		return copyLarge(input, output, new char[DEFAULT_BUFFER_SIZE]);
	}

	/**
	 * Copies chars from a large (over 2GB) <code>Reader</code> to a <code>Writer</code>.
	 * <p>
	 * This method uses the provided buffer, so there is no need to use a
	 * <code>BufferedReader</code>.
	 * <p>
	 *
	 * @param input the <code>Reader</code> to read from
	 * @param output the <code>Writer</code> to write to
	 * @param buffer the buffer to be used for the copy
	 * @return the number of characters copied
	 * @throws NullPointerException if the input or output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 2.2
	 */
	public static long copyLarge(final Reader input, final Writer output, final char[] buffer) throws IOException {
		long count = 0;
		int n;
		while (EOF != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}

	/**
	 * Copies some or all chars from a large (over 2GB) <code>InputStream</code> to an
	 * <code>OutputStream</code>, optionally skipping input chars.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedReader</code>.
	 * <p>
	 * The buffer size is given by {@link #DEFAULT_BUFFER_SIZE}.
	 *
	 * @param input the <code>Reader</code> to read from
	 * @param output the <code>Writer</code> to write to
	 * @param inputOffset : number of chars to skip from input before copying
	 * -ve values are ignored
	 * @param length : number of chars to copy. -ve means all
	 * @return the number of chars copied
	 * @throws NullPointerException if the input or output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 2.2
	 */
	public static long copyLarge(final Reader input, final Writer output, final long inputOffset, final long length)
			throws IOException {
		return copyLarge(input, output, inputOffset, length, new char[DEFAULT_BUFFER_SIZE]);
	}

	/**
	 * Copies some or all chars from a large (over 2GB) <code>InputStream</code> to an
	 * <code>OutputStream</code>, optionally skipping input chars.
	 * <p>
	 * This method uses the provided buffer, so there is no need to use a
	 * <code>BufferedReader</code>.
	 * <p>
	 *
	 * @param input the <code>Reader</code> to read from
	 * @param output the <code>Writer</code> to write to
	 * @param inputOffset : number of chars to skip from input before copying
	 * -ve values are ignored
	 * @param length : number of chars to copy. -ve means all
	 * @param buffer the buffer to be used for the copy
	 * @return the number of chars copied
	 * @throws NullPointerException if the input or output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 2.2
	 */
	public static long copyLarge(final Reader input, final Writer output, final long inputOffset, final long length,
								 final char[] buffer)
			throws IOException {
		if (inputOffset > 0) {
			skipFully(input, inputOffset);
		}
		if (length == 0) {
			return 0;
		}
		int bytesToRead = buffer.length;
		if (length > 0 && length < buffer.length) {
			bytesToRead = (int) length;
		}
		int read;
		long totalRead = 0;
		while (bytesToRead > 0 && EOF != (read = input.read(buffer, 0, bytesToRead))) {
			output.write(buffer, 0, read);
			totalRead += read;
			if (length > 0) { // only adjust length if not reading to the end
				// Note the cast must work because buffer.length is an integer
				bytesToRead = (int) Math.min(length - totalRead, buffer.length);
			}
		}
		return totalRead;
	}

	/**
	 * Copies chars from a <code>Reader</code> to bytes on an
	 * <code>OutputStream</code> using the specified character encoding, and
	 * calling flush.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedReader</code>.
	 * </p>
	 * <p>
	 * Due to the implementation of OutputStreamWriter, this method performs a
	 * flush.
	 * </p>
	 * <p>
	 * This method uses {@link OutputStreamWriter}.
	 * </p>
	 *
	 * @param input the <code>Reader</code> to read from
	 * @param output the <code>OutputStream</code> to write to
	 * @param outputEncoding the encoding to use for the OutputStream, null means platform default
	 * @throws NullPointerException if the input or output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 2.3
	 */
	public static void copy(final Reader input, final OutputStream output, final Charset outputEncoding)
			throws IOException {
		final OutputStreamWriter out = new OutputStreamWriter(output, CharsetUtils.toCharset(outputEncoding));
		copy(input, out);
		// XXX Unless anyone is planning on rewriting OutputStreamWriter,
		// we have to flush here.
		out.flush();
	}

	/**
	 * Copies chars from a <code>Reader</code> to bytes on an
	 * <code>OutputStream</code> using the specified character encoding, and
	 * calling flush.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedReader</code>.
	 * <p>
	 * Character encoding names can be found at
	 * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
	 * <p>
	 * Due to the implementation of OutputStreamWriter, this method performs a
	 * flush.
	 * <p>
	 * This method uses {@link OutputStreamWriter}.
	 *
	 * @param input the <code>Reader</code> to read from
	 * @param output the <code>OutputStream</code> to write to
	 * @param outputEncoding the encoding to use for the OutputStream, null means platform default
	 * @throws NullPointerException                         if the input or output is null
	 * @throws IOException                                  if an I/O error occurs
	 * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
	 *                                                      .UnsupportedEncodingException} in version 2.2 if the
	 *                                                      encoding is not supported.
	 * @since 1.1
	 */
	public static void copy(final Reader input, final OutputStream output, final String outputEncoding)
			throws IOException {
		copy(input, output, CharsetUtils.toCharset(outputEncoding));
	}

	// content equals
	//-----------------------------------------------------------------------

	/**
	 * Compares the contents of two Streams to determine if they are equal or
	 * not.
	 * <p>
	 * This method buffers the input internally using
	 * <code>BufferedInputStream</code> if they are not already buffered.
	 *
	 * @param input1 the first stream
	 * @param input2 the second stream
	 * @return true if the content of the streams are equal or they both don't
	 * exist, false otherwise
	 * @throws NullPointerException if either input is null
	 * @throws IOException          if an I/O error occurs
	 */
	public static boolean contentEquals(InputStream input1, InputStream input2)
			throws IOException {
		if (input1 == input2) {
			return true;
		}
		if (!(input1 instanceof BufferedInputStream)) {
			input1 = new BufferedInputStream(input1);
		}
		if (!(input2 instanceof BufferedInputStream)) {
			input2 = new BufferedInputStream(input2);
		}

		int ch = input1.read();
		while (EOF != ch) {
			final int ch2 = input2.read();
			if (ch != ch2) {
				return false;
			}
			ch = input1.read();
		}

		final int ch2 = input2.read();
		return ch2 == EOF;
	}

	/**
	 * Compares the contents of two Readers to determine if they are equal or
	 * not.
	 * <p>
	 * This method buffers the input internally using
	 * <code>BufferedReader</code> if they are not already buffered.
	 *
	 * @param input1 the first reader
	 * @param input2 the second reader
	 * @return true if the content of the readers are equal or they both don't
	 * exist, false otherwise
	 * @throws NullPointerException if either input is null
	 * @throws IOException          if an I/O error occurs
	 * @since 1.1
	 */
	public static boolean contentEquals(Reader input1, Reader input2)
			throws IOException {
		if (input1 == input2) {
			return true;
		}

		input1 = toBufferedReader(input1);
		input2 = toBufferedReader(input2);

		int ch = input1.read();
		while (EOF != ch) {
			final int ch2 = input2.read();
			if (ch != ch2) {
				return false;
			}
			ch = input1.read();
		}

		final int ch2 = input2.read();
		return ch2 == EOF;
	}

	/**
	 * Compares the contents of two Readers to determine if they are equal or
	 * not, ignoring EOL characters.
	 * <p>
	 * This method buffers the input internally using
	 * <code>BufferedReader</code> if they are not already buffered.
	 *
	 * @param input1 the first reader
	 * @param input2 the second reader
	 * @return true if the content of the readers are equal (ignoring EOL differences),  false otherwise
	 * @throws NullPointerException if either input is null
	 * @throws IOException          if an I/O error occurs
	 * @since 2.2
	 */
	public static boolean contentEqualsIgnoreEOL(final Reader input1, final Reader input2)
			throws IOException {
		if (input1 == input2) {
			return true;
		}
		final BufferedReader br1 = toBufferedReader(input1);
		final BufferedReader br2 = toBufferedReader(input2);

		String line1 = br1.readLine();
		String line2 = br2.readLine();
		while (line1 != null && line2 != null && line1.equals(line2)) {
			line1 = br1.readLine();
			line2 = br2.readLine();
		}
		return line1 == null ? line2 == null ? true : false : line1.equals(line2);
	}

	/**
	 * Skips bytes from an input byte stream.
	 * This implementation guarantees that it will read as many bytes
	 * as possible before giving up; this may not always be the case for
	 * skip() implementations in subclasses of {@link InputStream}.
	 * <p>
	 * Note that the implementation uses {@link InputStream#read(byte[], int, int)} rather
	 * than delegating to {@link InputStream#skip(long)}.
	 * This means that the method may be considerably less efficient than using the actual skip implementation,
	 * this is done to guarantee that the correct number of bytes are skipped.
	 * </p>
	 *
	 * @param input byte stream to skip
	 * @param toSkip number of bytes to skip.
	 * @return number of bytes actually skipped.
	 * @throws IOException              if there is a problem reading the file
	 * @throws IllegalArgumentException if toSkip is negative
	 * @see InputStream#skip(long)
	 * @see <a href="https://issues.apache.org/jira/browse/IO-203">IO-203 - Add skipFully() method for InputStreams</a>
	 * @since 2.0
	 */
	public static long skip(final InputStream input, final long toSkip) throws IOException {
		if (toSkip < 0) {
			throw new IllegalArgumentException("Skip count must be non-negative, actual: " + toSkip);
		}
		/*
		 * N.B. no need to synchronize this because: - we don't care if the buffer is created multiple times (the data
		 * is ignored) - we always use the same size buffer, so if it it is recreated it will still be OK (if the buffer
		 * size were variable, we would need to synch. to ensure some other thread did not create a smaller one)
		 */
		if (SKIP_BYTE_BUFFER == null) {
			SKIP_BYTE_BUFFER = new byte[SKIP_BUFFER_SIZE];
		}
		long remain = toSkip;
		while (remain > 0) {
			// See https://issues.apache.org/jira/browse/IO-203 for why we use read() rather than delegating to skip()
			final long n = input.read(SKIP_BYTE_BUFFER, 0, (int) Math.min(remain, SKIP_BUFFER_SIZE));
			if (n < 0) { // EOF
				break;
			}
			remain -= n;
		}
		return toSkip - remain;
	}

	/**
	 * Skips bytes from a ReadableByteChannel.
	 * This implementation guarantees that it will read as many bytes
	 * as possible before giving up.
	 *
	 * @param input ReadableByteChannel to skip
	 * @param toSkip number of bytes to skip.
	 * @return number of bytes actually skipped.
	 * @throws IOException              if there is a problem reading the ReadableByteChannel
	 * @throws IllegalArgumentException if toSkip is negative
	 * @since 2.5
	 */
	public static long skip(final ReadableByteChannel input, final long toSkip) throws IOException {
		if (toSkip < 0) {
			throw new IllegalArgumentException("Skip count must be non-negative, actual: " + toSkip);
		}
		final ByteBuffer skipByteBuffer = ByteBuffer.allocate((int) Math.min(toSkip, SKIP_BUFFER_SIZE));
		long remain = toSkip;
		while (remain > 0) {
			skipByteBuffer.position(0);
			skipByteBuffer.limit((int) Math.min(remain, SKIP_BUFFER_SIZE));
			final int n = input.read(skipByteBuffer);
			if (n == EOF) {
				break;
			}
			remain -= n;
		}
		return toSkip - remain;
	}

	/**
	 * Skips characters from an input character stream.
	 * This implementation guarantees that it will read as many characters
	 * as possible before giving up; this may not always be the case for
	 * skip() implementations in subclasses of {@link Reader}.
	 * <p>
	 * Note that the implementation uses {@link Reader#read(char[], int, int)} rather
	 * than delegating to {@link Reader#skip(long)}.
	 * This means that the method may be considerably less efficient than using the actual skip implementation,
	 * this is done to guarantee that the correct number of characters are skipped.
	 * </p>
	 *
	 * @param input character stream to skip
	 * @param toSkip number of characters to skip.
	 * @return number of characters actually skipped.
	 * @throws IOException              if there is a problem reading the file
	 * @throws IllegalArgumentException if toSkip is negative
	 * @see Reader#skip(long)
	 * @see <a href="https://issues.apache.org/jira/browse/IO-203">IO-203 - Add skipFully() method for InputStreams</a>
	 * @since 2.0
	 */
	public static long skip(final Reader input, final long toSkip) throws IOException {
		if (toSkip < 0) {
			throw new IllegalArgumentException("Skip count must be non-negative, actual: " + toSkip);
		}
		/*
		 * N.B. no need to synchronize this because: - we don't care if the buffer is created multiple times (the data
		 * is ignored) - we always use the same size buffer, so if it it is recreated it will still be OK (if the buffer
		 * size were variable, we would need to synch. to ensure some other thread did not create a smaller one)
		 */
		if (SKIP_CHAR_BUFFER == null) {
			SKIP_CHAR_BUFFER = new char[SKIP_BUFFER_SIZE];
		}
		long remain = toSkip;
		while (remain > 0) {
			// See https://issues.apache.org/jira/browse/IO-203 for why we use read() rather than delegating to skip()
			final long n = input.read(SKIP_CHAR_BUFFER, 0, (int) Math.min(remain, SKIP_BUFFER_SIZE));
			if (n < 0) { // EOF
				break;
			}
			remain -= n;
		}
		return toSkip - remain;
	}

	/**
	 * Skips the requested number of bytes or fail if there are not enough left.
	 * <p>
	 * This allows for the possibility that {@link InputStream#skip(long)} may
	 * not skip as many bytes as requested (most likely because of reaching EOF).
	 * <p>
	 * Note that the implementation uses {@link #skip(InputStream, long)}.
	 * This means that the method may be considerably less efficient than using the actual skip implementation,
	 * this is done to guarantee that the correct number of characters are skipped.
	 * </p>
	 *
	 * @param input stream to skip
	 * @param toSkip the number of bytes to skip
	 * @throws IOException              if there is a problem reading the file
	 * @throws IllegalArgumentException if toSkip is negative
	 * @throws EOFException             if the number of bytes skipped was incorrect
	 * @see InputStream#skip(long)
	 * @since 2.0
	 */
	public static void skipFully(final InputStream input, final long toSkip) throws IOException {
		if (toSkip < 0) {
			throw new IllegalArgumentException("Bytes to skip must not be negative: " + toSkip);
		}
		final long skipped = skip(input, toSkip);
		if (skipped != toSkip) {
			throw new EOFException("Bytes to skip: " + toSkip + " actual: " + skipped);
		}
	}

	/**
	 * Skips the requested number of bytes or fail if there are not enough left.
	 *
	 * @param input ReadableByteChannel to skip
	 * @param toSkip the number of bytes to skip
	 * @throws IOException              if there is a problem reading the ReadableByteChannel
	 * @throws IllegalArgumentException if toSkip is negative
	 * @throws EOFException             if the number of bytes skipped was incorrect
	 * @since 2.5
	 */
	public static void skipFully(final ReadableByteChannel input, final long toSkip) throws IOException {
		if (toSkip < 0) {
			throw new IllegalArgumentException("Bytes to skip must not be negative: " + toSkip);
		}
		final long skipped = skip(input, toSkip);
		if (skipped != toSkip) {
			throw new EOFException("Bytes to skip: " + toSkip + " actual: " + skipped);
		}
	}

	/**
	 * Skips the requested number of characters or fail if there are not enough left.
	 * <p>
	 * This allows for the possibility that {@link Reader#skip(long)} may
	 * not skip as many characters as requested (most likely because of reaching EOF).
	 * <p>
	 * Note that the implementation uses {@link #skip(Reader, long)}.
	 * This means that the method may be considerably less efficient than using the actual skip implementation,
	 * this is done to guarantee that the correct number of characters are skipped.
	 * </p>
	 *
	 * @param input stream to skip
	 * @param toSkip the number of characters to skip
	 * @throws IOException              if there is a problem reading the file
	 * @throws IllegalArgumentException if toSkip is negative
	 * @throws EOFException             if the number of characters skipped was incorrect
	 * @see Reader#skip(long)
	 * @since 2.0
	 */
	public static void skipFully(final Reader input, final long toSkip) throws IOException {
		final long skipped = skip(input, toSkip);
		if (skipped != toSkip) {
			throw new EOFException("Chars to skip: " + toSkip + " actual: " + skipped);
		}
	}


	/**
	 * Reads characters from an input character stream.
	 * This implementation guarantees that it will read as many characters
	 * as possible before giving up; this may not always be the case for
	 * subclasses of {@link Reader}.
	 *
	 * @param input where to read input from
	 * @param buffer destination
	 * @param offset initial offset into buffer
	 * @param length length to read, must be &gt;= 0
	 * @return actual length read; may be less than requested if EOF was reached
	 * @throws IOException if a read error occurs
	 * @since 2.2
	 */
	public static int read(final Reader input, final char[] buffer, final int offset, final int length)
			throws IOException {
		if (length < 0) {
			throw new IllegalArgumentException("Length must not be negative: " + length);
		}
		int remaining = length;
		while (remaining > 0) {
			final int location = length - remaining;
			final int count = input.read(buffer, offset + location, remaining);
			if (EOF == count) { // EOF
				break;
			}
			remaining -= count;
		}
		return length - remaining;
	}

	/**
	 * Reads characters from an input character stream.
	 * This implementation guarantees that it will read as many characters
	 * as possible before giving up; this may not always be the case for
	 * subclasses of {@link Reader}.
	 *
	 * @param input where to read input from
	 * @param buffer destination
	 * @return actual length read; may be less than requested if EOF was reached
	 * @throws IOException if a read error occurs
	 * @since 2.2
	 */
	public static int read(final Reader input, final char[] buffer) throws IOException {
		return read(input, buffer, 0, buffer.length);
	}

	/**
	 * Reads bytes from an input stream.
	 * This implementation guarantees that it will read as many bytes
	 * as possible before giving up; this may not always be the case for
	 * subclasses of {@link InputStream}.
	 *
	 * @param input where to read input from
	 * @param buffer destination
	 * @param offset initial offset into buffer
	 * @param length length to read, must be &gt;= 0
	 * @return actual length read; may be less than requested if EOF was reached
	 * @throws IOException if a read error occurs
	 * @since 2.2
	 */
	public static int read(final InputStream input, final byte[] buffer, final int offset, final int length)
			throws IOException {
		if (length < 0) {
			throw new IllegalArgumentException("Length must not be negative: " + length);
		}
		int remaining = length;
		while (remaining > 0) {
			final int location = length - remaining;
			final int count = input.read(buffer, offset + location, remaining);
			if (EOF == count) { // EOF
				break;
			}
			remaining -= count;
		}
		return length - remaining;
	}

	// -------------------------------------------------------------------------------------- read start

	/**
	 * 从流中读取内容
	 *
	 * @param in          输入流
	 * @param charsetName 字符集
	 * @return 内容
	 * @throws IOException IO异常
	 */
	public static String read(InputStream in, String charsetName) throws IOException {
		FastByteArrayOutputStream out = read(in);
		return StringUtils.isBlank(charsetName) ? out.toString() : out.toString(charsetName);
	}

	/**
	 * 从流中读取内容，读取完毕后并不关闭流
	 *
	 * @param in      输入流，读取完毕后并不关闭流
	 * @param charset 字符集
	 * @return 内容
	 * @throws IOException IO异常
	 */
	public static String read(InputStream in, Charset charset) throws IOException {
		FastByteArrayOutputStream out = read(in);
		return null == charset ? out.toString() : out.toString(charset);
	}

	/**
	 * 从流中读取内容，读到输出流中，读取完毕后并不关闭流
	 *
	 * @param in 输入流
	 * @return 输出流
	 * @throws IOException IO异常
	 */
	public static FastByteArrayOutputStream read(InputStream in) throws IOException {
		final FastByteArrayOutputStream out = new FastByteArrayOutputStream();
		copy(in, out);
		return out;
	}

	/**
	 * 从Reader中读取String，读取完毕后并不关闭Reader
	 *
	 * @param reader Reader
	 * @return String
	 */
	public static String read(Reader reader){
		final StringBuilder builder = StringUtils.builder();
		final CharBuffer buffer = CharBuffer.allocate(DEFAULT_BUFFER_SIZE);
		try {
			while (-1 != reader.read(buffer)) {
				builder.append(buffer.flip().toString());
			}
		} catch (IOException e) {
			throw new ChaosCoreException("从Reader中读取String发生异常",e);
		}
		return builder.toString();
	}

	/**
	 * Reads bytes from an input stream.
	 * This implementation guarantees that it will read as many bytes
	 * as possible before giving up; this may not always be the case for
	 * subclasses of {@link InputStream}.
	 *
	 * @param input where to read input from
	 * @param buffer destination
	 * @return actual length read; may be less than requested if EOF was reached
	 * @throws IOException if a read error occurs
	 * @since 2.2
	 */
	public static int read(final InputStream input, final byte[] buffer) throws IOException {
		return read(input, buffer, 0, buffer.length);
	}

	/**
	 * Reads bytes from a ReadableByteChannel.
	 * <p>
	 * This implementation guarantees that it will read as many bytes
	 * as possible before giving up; this may not always be the case for
	 * subclasses of {@link ReadableByteChannel}.
	 *
	 * @param input the byte channel to read
	 * @param buffer byte buffer destination
	 * @return the actual length read; may be less than requested if EOF was reached
	 * @throws IOException if a read error occurs
	 * @since 2.5
	 */
	public static int read(final ReadableByteChannel input, final ByteBuffer buffer) throws IOException {
		final int length = buffer.remaining();
		while (buffer.remaining() > 0) {
			final int count = input.read(buffer);
			if (EOF == count) { // EOF
				break;
			}
		}
		return length - buffer.remaining();
	}

	/**
	 * Reads the requested number of characters or fail if there are not enough left.
	 * <p>
	 * This allows for the possibility that {@link Reader#read(char[], int, int)} may
	 * not read as many characters as requested (most likely because of reaching EOF).
	 *
	 * @param input where to read input from
	 * @param buffer destination
	 * @param offset initial offset into buffer
	 * @param length length to read, must be &gt;= 0
	 * @throws IOException              if there is a problem reading the file
	 * @throws IllegalArgumentException if length is negative
	 * @throws EOFException             if the number of characters read was incorrect
	 * @since 2.2
	 */
	public static void readFully(final Reader input, final char[] buffer, final int offset, final int length)
			throws IOException {
		final int actual = read(input, buffer, offset, length);
		if (actual != length) {
			throw new EOFException("Length to read: " + length + " actual: " + actual);
		}
	}

	/**
	 * Reads the requested number of characters or fail if there are not enough left.
	 * <p>
	 * This allows for the possibility that {@link Reader#read(char[], int, int)} may
	 * not read as many characters as requested (most likely because of reaching EOF).
	 *
	 * @param input where to read input from
	 * @param buffer destination
	 * @throws IOException              if there is a problem reading the file
	 * @throws IllegalArgumentException if length is negative
	 * @throws EOFException             if the number of characters read was incorrect
	 * @since 2.2
	 */
	public static void readFully(final Reader input, final char[] buffer) throws IOException {
		readFully(input, buffer, 0, buffer.length);
	}

	/**
	 * Reads the requested number of bytes or fail if there are not enough left.
	 * <p>
	 * This allows for the possibility that {@link InputStream#read(byte[], int, int)} may
	 * not read as many bytes as requested (most likely because of reaching EOF).
	 *
	 * @param input where to read input from
	 * @param buffer destination
	 * @param offset initial offset into buffer
	 * @param length length to read, must be &gt;= 0
	 * @throws IOException              if there is a problem reading the file
	 * @throws IllegalArgumentException if length is negative
	 * @throws EOFException             if the number of bytes read was incorrect
	 * @since 2.2
	 */
	public static void readFully(final InputStream input, final byte[] buffer, final int offset, final int length)
			throws IOException {
		final int actual = read(input, buffer, offset, length);
		if (actual != length) {
			throw new EOFException("Length to read: " + length + " actual: " + actual);
		}
	}

	/**
	 * Reads the requested number of bytes or fail if there are not enough left.
	 * <p>
	 * This allows for the possibility that {@link InputStream#read(byte[], int, int)} may
	 * not read as many bytes as requested (most likely because of reaching EOF).
	 *
	 * @param input where to read input from
	 * @param buffer destination
	 * @throws IOException              if there is a problem reading the file
	 * @throws IllegalArgumentException if length is negative
	 * @throws EOFException             if the number of bytes read was incorrect
	 * @since 2.2
	 */
	public static void readFully(final InputStream input, final byte[] buffer) throws IOException {
		readFully(input, buffer, 0, buffer.length);
	}

	/**
	 * Reads the requested number of bytes or fail if there are not enough left.
	 * <p>
	 * This allows for the possibility that {@link InputStream#read(byte[], int, int)} may
	 * not read as many bytes as requested (most likely because of reaching EOF).
	 *
	 * @param input where to read input from
	 * @param length length to read, must be &gt;= 0
	 * @return the bytes read from input
	 * @throws IOException              if there is a problem reading the file
	 * @throws IllegalArgumentException if length is negative
	 * @throws EOFException             if the number of bytes read was incorrect
	 * @since 2.5
	 */
	public static byte[] readFully(final InputStream input, final int length) throws IOException {
		final byte[] buffer = new byte[length];
		readFully(input, buffer, 0, buffer.length);
		return buffer;
	}

	/**
	 * Reads the requested number of bytes or fail if there are not enough left.
	 * <p>
	 * This allows for the possibility that {@link ReadableByteChannel#read(ByteBuffer)} may
	 * not read as many bytes as requested (most likely because of reaching EOF).
	 *
	 * @param input the byte channel to read
	 * @param buffer byte buffer destination
	 * @throws IOException  if there is a problem reading the file
	 * @throws EOFException if the number of bytes read was incorrect
	 * @since 2.5
	 */
	public static void readFully(final ReadableByteChannel input, final ByteBuffer buffer) throws IOException {
		final int expected = buffer.remaining();
		final int actual = read(input, buffer);
		if (actual != expected) {
			throw new EOFException("Length to read: " + expected + " actual: " + actual);
		}
	}

}
