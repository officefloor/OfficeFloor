/*-
 * #%L
 * AWS SAM HTTP Server
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
