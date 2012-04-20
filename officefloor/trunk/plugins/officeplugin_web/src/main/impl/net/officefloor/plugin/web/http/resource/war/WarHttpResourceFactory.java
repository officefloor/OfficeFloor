/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
package net.officefloor.plugin.web.http.resource.war;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.officefloor.plugin.web.http.resource.AbstractHttpFileDescription;
import net.officefloor.plugin.web.http.resource.AbstractHttpResourceFactory;
import net.officefloor.plugin.web.http.resource.HttpFile;
import net.officefloor.plugin.web.http.resource.HttpFileDescription;
import net.officefloor.plugin.web.http.resource.HttpResource;
import net.officefloor.plugin.web.http.resource.HttpResourceUtil;
import net.officefloor.plugin.web.http.resource.InvalidHttpRequestUriException;

/**
 * <p>
 * Locates a {@link HttpFile} from a WAR directory or archive.
 * <p>
 * As per Servlet standards it will not provide access to files within the
 * <code>WEB-INF</code> directory.
 * 
 * @author Daniel Sagenschneider
 */
public class WarHttpResourceFactory extends AbstractHttpResourceFactory {

	/**
	 * {@link Logger}.
	 */
	private static Logger LOGGER = Logger
			.getLogger(WarHttpResourceFactory.class.getName());

	/**
	 * WAR directory.
	 */
	private final File warDir;

	/**
	 * Initiate.
	 * 
	 * @param warDir
	 *            WAR directory.
	 */
	public WarHttpResourceFactory(File warDir) {
		this.warDir = warDir;
	}

	/*
	 * =================== HttpResourceFactory =======================
	 */

	@Override
	public HttpResource createHttpResource(String requestUriPath)
			throws IOException, InvalidHttpRequestUriException {

		// Transform to canonical path
		String canonicalPath = HttpResourceUtil
				.transformToCanonicalPath(requestUriPath);

		// Obtain the resource
		File resource = new File(this.warDir, canonicalPath);

		// Describe the resource
		HttpFileDescriptionImpl description = new HttpFileDescriptionImpl(
				canonicalPath, resource);
		this.describeFile(description);

		// Return the file
		// TODO return the file
		return null;
	}

	/**
	 * {@link HttpFileDescription} implementation.
	 */
	private static class HttpFileDescriptionImpl extends
			AbstractHttpFileDescription {

		/**
		 * {@link File}.
		 */
		private final File file;

		/**
		 * Initiate.
		 * 
		 * @param resourcePath
		 *            {@link HttpResource} path.
		 * @param file
		 *            {@link File}.
		 */
		public HttpFileDescriptionImpl(String resourcePath, File file) {
			super(resourcePath);
			this.file = file;
		}

		/*
		 * ================== HttpFileDescription ============================
		 */

		@Override
		public ByteBuffer getContents() {

			// Obtain the input stream
			InputStream content = null;
			try {
				content = new FileInputStream(this.file);
			} catch (IOException ex) {
				if (LOGGER.isLoggable(Level.FINE)) {
					LOGGER.log(Level.FINE, "Failed to read content from file "
							+ this.file.getAbsolutePath(), ex);
				}
			}

			// Obtain resource contents for file
			return getHttpResourceContents(content, this.resourcePath);
		}
	}

}