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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;

/**
 * Buffer to capture the output data as a Base64 string.
 * 
 * @author Daniel Sagenschneider
 */
public class Base64Buffer {

	/**
	 * Captures the Base64 text.
	 */
	private final StringBuilder buffer = new StringBuilder();

	/**
	 * {@link OutputStream}.
	 */
	private final OutputStream outputStream;

	/**
	 * Instantiate.
	 */
	public Base64Buffer() {

		// Create the output stream
		this.outputStream = Base64.getEncoder().wrap(new OutputStream() {

			@Override
			public void write(int b) throws IOException {

				// Each byte as a character
				Base64Buffer.this.buffer.append((char) b);
			}
		});
	}

	/**
	 * Obtains the {@link OutputStream} to write data.
	 * 
	 * @return {@link OutputStream} to write data.
	 */
	public OutputStream getOutputStream() {
		return this.outputStream;
	}

	/**
	 * Obtains the Base64 text.
	 * 
	 * @return Base64 text.
	 * @throws IOException If fails to obtain Base64.
	 */
	public String getBase64Text() throws IOException {
		this.outputStream.close();
		return this.buffer.toString();
	}

}
