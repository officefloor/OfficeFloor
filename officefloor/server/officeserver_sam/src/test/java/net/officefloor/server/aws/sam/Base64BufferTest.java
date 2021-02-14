/*-
 * #%L
 * AWS SAM HTTP Server
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
