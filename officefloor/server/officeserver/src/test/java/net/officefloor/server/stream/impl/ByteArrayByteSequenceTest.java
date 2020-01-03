package net.officefloor.server.stream.impl;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * Tests the {@link ByteArrayByteSequence}.
 * 
 * @author Daniel Sagenschneider
 */
public class ByteArrayByteSequenceTest extends OfficeFrameTestCase {

	/**
	 * Expected string.
	 */
	private final String TEST = "TEST";

	/**
	 * Input bytes.
	 */
	private final byte[] bytes = TEST.getBytes(ServerHttpConnection.HTTP_CHARSET);

	/**
	 * {@link ByteArrayByteSequence} to test.
	 */
	private final ByteSequence sequence = new ByteArrayByteSequence(this.bytes);

	/**
	 * Ensure correct length.
	 */
	public void testCorrectLength() {
		assertEquals("Incorrect length", this.bytes.length, this.sequence.length());
	}

	/**
	 * Ensure can read contents.
	 */
	public void testReadContents() {
		byte[] content = new byte[this.sequence.length()];
		for (int i = 0; i < this.sequence.length(); i++) {
			assertEquals("Incorrect byte " + i, this.bytes[i], this.sequence.byteAt(i));
			content[i] = this.sequence.byteAt(i);
		}
		assertEquals("Incorrect reconstructed value", TEST, new String(content, ServerHttpConnection.HTTP_CHARSET));
	}
}
