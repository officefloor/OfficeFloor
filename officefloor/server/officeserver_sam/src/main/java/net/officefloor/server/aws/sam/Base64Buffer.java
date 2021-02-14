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
