package net.officefloor.server.aws.sam;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Base64;

import org.junit.jupiter.api.Test;

/**
 * Tests the {@link Base64Buffer}.
 * 
 * @author Daniel Sagenschneider
 */
public class Base64BufferTest {

	/**
	 * Ensure can round trip the Base64.
	 */
	@Test
	public void roundTrip() throws IOException {

		// Ensure can output all byte values
		byte[] bytes = new byte[Math.abs(Byte.MIN_VALUE) + Byte.MAX_VALUE];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) (i - Byte.MIN_VALUE);
		}

		// Write out the data
		Base64Buffer buffer = new Base64Buffer();
		buffer.getOutputStream().write(bytes);

		// Obtain the base 64 text
		String base64Text = buffer.getBase64Text();

		// Translate back bytes
		byte[] roundTrip = Base64.getDecoder().decode(base64Text);

		// Ensure same bytes
		assertEquals(bytes.length, roundTrip.length, "Incorrect number of bytes");
		for (int i = 0; i < bytes.length; i++) {
			assertEquals(bytes[i], roundTrip[i], "Incorrect byte " + i);
		}
	}

}