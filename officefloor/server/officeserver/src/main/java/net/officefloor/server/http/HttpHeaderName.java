package net.officefloor.server.http;

import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBufferPool;

/**
 * Means to provide common {@link HttpHeader} names in already encoded HTTP
 * bytes for faster writing.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpHeaderName {

	/**
	 * Name.
	 */
	private final String name;

	/**
	 * Pre-encoded bytes of name ready for HTTP output.
	 */
	private final byte[] encodedName;

	/**
	 * Instantiate.
	 * 
	 * @param name {@link HttpHeaderName}.
	 */
	public HttpHeaderName(String name) {
		this(name, false);
	}

	/**
	 * Instantiate.
	 * 
	 * @param name           {@link HttpHeaderName}.
	 * @param isMaintainCase Whether to maintain {@link HttpHeaderName} case.
	 */
	public HttpHeaderName(String name, boolean isMaintainCase) {
		this.name = isMaintainCase ? name : name.toLowerCase(); // case insensitive
		this.encodedName = this.name.getBytes(ServerHttpConnection.HTTP_CHARSET);
	}

	/**
	 * Obtains the name.
	 * 
	 * @return Name.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Writes this {@link HttpHeaderName} to the {@link StreamBuffer}.
	 * 
	 * @param <B>        Buffer type.
	 * @param head       Head {@link StreamBuffer} of linked list of
	 *                   {@link StreamBuffer} instances.
	 * @param bufferPool {@link StreamBufferPool}.
	 */
	public <B> void write(StreamBuffer<B> head, StreamBufferPool<B> bufferPool) {
		StreamBuffer.write(this.encodedName, head, bufferPool);
	}

}