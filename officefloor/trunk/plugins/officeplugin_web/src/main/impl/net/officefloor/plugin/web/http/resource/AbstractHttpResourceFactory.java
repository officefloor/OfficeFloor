/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.web.http.resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract {@link HttpResourceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractHttpResourceFactory implements
		HttpResourceFactory {

	/**
	 * {@link Logger}.
	 */
	private static Logger LOGGER = Logger
			.getLogger(AbstractHttpResourceFactory.class.getName());

	/**
	 * Empty {@link ByteBuffer}.
	 */
	private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.wrap(new byte[0])
			.asReadOnlyBuffer();

	/**
	 * Obtains the contents of the {@link HttpResource} for the
	 * {@link InputStream}.
	 * 
	 * @param content
	 *            {@link InputStream} containing contents of
	 *            {@link HttpResource}.
	 * @param path
	 *            Path for logging potential issue in obtaining content.
	 * @return {@link InputStream} for the {@link HttpResource}. May be
	 *         <code>null</code>.
	 */
	public static ByteBuffer getHttpResourceContents(InputStream content,
			String path) {

		// Ensure have content
		if (content == null) {
			// Can not locate the file, return no content
			return EMPTY_BUFFER;
		}

		try {
			// Obtain the contents
			ByteArrayOutputStream data = new ByteArrayOutputStream();
			for (int value = content.read(); value != -1; value = content
					.read()) {
				data.write(value);
			}
			content.close();

			// Return the contents of the file
			return ByteBuffer.wrap(data.toByteArray()).asReadOnlyBuffer();

		} catch (IOException ex) {
			// Indicate unable to obtain contents
			if (LOGGER.isLoggable(Level.FINE)) {
				LOGGER.log(Level.FINE, "Failed reading resource contents for "
						+ path, ex);
			}

			// Failed obtaining contents, return no content
			return EMPTY_BUFFER;
		}
	}

	/**
	 * Listing of {@link HttpFileDescriber} instances to describe the created
	 * {@link HttpFile} instances.
	 */
	private final List<HttpFileDescriber> describers = new LinkedList<HttpFileDescriber>();

	/**
	 * <p>
	 * Describes the {@link HttpFile}.
	 * <p>
	 * Only registered {@link HttpFileDescriber} instances are used therefore it
	 * is possible that the {@link HttpFile} may not be able to be described.
	 * 
	 * @param fileDescription
	 *            {@link HttpFileDescription}.
	 */
	protected void describeFile(HttpFileDescription fileDescription) {

		// Describe the file (as best as possible)
		DESCRIBED: for (HttpFileDescriber describer : this.describers) {
			if (describer.describe(fileDescription)) {
				break DESCRIBED; // have description
			}
		}
	}

	/*
	 * ==================== HttpResourceFactory ===================
	 */

	@Override
	public void addHttpFileDescriber(HttpFileDescriber httpFileDescriber) {
		this.describers.add(httpFileDescriber);
	}

}