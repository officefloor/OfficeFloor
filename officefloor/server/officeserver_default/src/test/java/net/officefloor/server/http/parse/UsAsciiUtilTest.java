package net.officefloor.server.http.parse;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.UsAsciiUtil;

/**
 * Tests the {@link UsAsciiUtil}.
 *
 * @author Daniel Sagenschneider
 */
public class UsAsciiUtilTest extends OfficeFrameTestCase {

	/**
	 * Validate the conversion to US-ASCII.
	 */
	public void testUsAsciiUtil() throws Exception {

		// US-ASCII values
		final byte a = 97;
		final byte z = 122;
		final byte A = 65;
		final byte Z = 90;
		final byte _0 = 48;
		final byte _9 = 57;
		final byte colon = 58;
		final byte CR = 13;
		final byte LF = 10;

		// String
		final String text = "azAZ09:\r\n";

		byte[] ascii = UsAsciiUtil.convertToUsAscii(text);

		// Validate conversion to US-ASCII
		assertEquals(a, ascii[0]);
		assertEquals(z, ascii[1]);
		assertEquals(A, ascii[2]);
		assertEquals(Z, ascii[3]);
		assertEquals(_0, ascii[4]);
		assertEquals(_9, ascii[5]);
		assertEquals(colon, ascii[6]);
		assertEquals(CR, ascii[7]);
		assertEquals(LF, ascii[8]);

		// Validate conversion back to String
		assertEquals(text, UsAsciiUtil.convertToString(ascii));

		// Ensure the asserts are correct
		UsAsciiUtil.assertEquals(text, ascii);
		UsAsciiUtil.assertEquals("Text", text, ascii);

		// Validate character conversion
		assertEquals(a, UsAsciiUtil.convertToUsAscii('a'));
	}

}