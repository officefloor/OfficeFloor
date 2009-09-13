/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.server.http.file;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * {@link HttpFileDescriber} that maps the file extension to the description
 * details.
 *
 * @author Daniel Sagenschneider
 */
public class FileExtensionHttpFileDescriber implements HttpFileDescriber {

	/**
	 * Prefix on {@link Properties} name to obtain the
	 * <code>Content-Encoding</code> details.
	 */
	public static final String CONTENT_ENCODING_PREFIX = "content.encoding.";

	/**
	 * Prefix on {@link Properties} name to obtain the <code>Content-Type</code>
	 * details.
	 */
	public static final String CONTENT_TYPE_PREFIX = "content.type.";

	/**
	 * Mapping of file extension to its {@link DescriptionStruct}.
	 */
	private final Map<String, DescriptionStruct> descriptions = new HashMap<String, DescriptionStruct>();

	/**
	 * Loads the default file extension descriptions.
	 */
	public void loadDefaultDescriptions() {

		// Text related extension
		this.mapDescription("html", null, "text/html");
		this.mapDescription("htm", null, "text/html");
		this.mapDescription("js", null, "text/javascript");
		this.mapDescription("css", null, "text/css");
		this.mapDescription("xml", null, "text/xml");
		this.mapDescription("txt", null, "text/plain");

		// Image related extensions
		this.mapDescription("gif", null, "image/gif");
		this.mapDescription("png", null, "image/x-png");
		this.mapDescription("jpg", null, "image/jpeg");
		this.mapDescription("jpeg", null, "image/jpeg");
		this.mapDescription("jpe", null, "image/jpeg");

		// Audio related extensions
		this.mapDescription("wav", null, "audio/x-wav");
		this.mapDescription("mpa", null, "audio/x-mpeg");
		this.mapDescription("abs", null, "audio/x-mpeg");
		this.mapDescription("mpega", null, "audio/x-mpeg");
		this.mapDescription("mp2a", null, "audio/x-mpeg2");
		this.mapDescription("mpa2", null, "audio/x-mpeg2");

		// Video related extensions
		this.mapDescription("mpeg", null, "video/mpeg");
		this.mapDescription("mpg", null, "video/mpeg");
		this.mapDescription("mpe", null, "video/mpeg");
		this.mapDescription("mpv2", null, "video/mpeg2");
		this.mapDescription("mp2v", null, "video/mpeg2");
		this.mapDescription("qt", null, "video/quicktime");
		this.mapDescription("mov", null, "video/quicktime");
		this.mapDescription("avi", null, "video/x-msvideo");

		// Application related extensions
		this.mapDescription("ps", null, "application/postscript");
		this.mapDescription("pdf", null, "application/pdf");
		this.mapDescription("odt", null,
				"application/vnd.oasis.opendocument.text");
		this.mapDescription("odg", null,
				"application/vnd.oasis.opendocument.graphics");
		this.mapDescription("odp", null,
				"application/vnd.oasis.opendocument.presentation");
		this.mapDescription("ods", null,
				"application/vnd.oasis.opendocument.spreadsheet");
		this.mapDescription("odc", null,
				"application/vnd.oasis.opendocument.chart");
		this.mapDescription("doc", null, "application/msword");
		this.mapDescription("ppt", null, "application/mspowerpoint");
		this.mapDescription("xls", null, "application/x-msexcel");

		// Archive related extensions
		this.mapDescription("tar", null, "application/x-tar");
		this.mapDescription("zip", null, "application/zip");
		this.mapDescription("jar", null, "application/octet-stream");
		this.mapDescription("exe", null, "application/octet-stream");
	}

	/**
	 * Loads the descriptions from the {@link Properties}.
	 *
	 * @param properties
	 *            {@link Properties}.
	 */
	public void loadDescriptions(Properties properties) {

		// Iterate over the properties, mapping in values
		for (String name : properties.stringPropertyNames()) {

			// Determine if content-encoding
			if (name.startsWith(CONTENT_ENCODING_PREFIX)) {
				// Map the content-encoding
				String fileExtension = name.substring(CONTENT_ENCODING_PREFIX
						.length());
				String contentEncoding = properties.getProperty(name);
				this.mapContentEncoding(fileExtension, contentEncoding);
			}

			// Determine if content-type
			if (name.startsWith(CONTENT_TYPE_PREFIX)) {
				// Map the content-type
				String fileExtension = name.substring(CONTENT_TYPE_PREFIX
						.length());
				String contentType = properties.getProperty(name);
				this.mapContentType(fileExtension, contentType);
			}
		}
	}

	/**
	 * Maps the <code>Content-Encoding</code> for the file extension.
	 *
	 * @param fileExtension
	 *            File extension.
	 * @param contentEncoding
	 *            <code>Content-Encoding</code>.
	 */
	public void mapContentEncoding(String fileExtension, String contentEncoding) {
		this.mapDescription(fileExtension, contentEncoding, null);
	}

	/**
	 * Maps the <code>Content-Type</code> for the file extension.
	 *
	 * @param fileExtension
	 *            File extension.
	 * @param contentType
	 *            <code>Content-Encoding</code>.
	 */
	public void mapContentType(String fileExtension, String contentType) {
		this.mapDescription(fileExtension, null, contentType);
	}

	/**
	 * Maps the description for the extension.
	 *
	 * @param fileExtension
	 *            File extension.
	 * @param contentEncoding
	 *            Overriding <code>Content-Encoding</code>.
	 * @param contentType
	 *            Overriding <code>Content-Type</code>.
	 */
	private void mapDescription(String fileExtension, String contentEncoding,
			String contentType) {

		// Always find by lower case file extension
		fileExtension = fileExtension.toLowerCase();

		// Obtain the details
		DescriptionStruct details = this.descriptions.get(fileExtension);

		// Override the details
		this.descriptions.put(fileExtension, new DescriptionStruct(details,
				contentEncoding, contentType));
	}

	/*
	 * ==================== HttpFileDescriber ===========================
	 */

	@Override
	public void describe(HttpFileDescription description) {

		// Obtain the file extension
		String fileExtension = description.getExtension();
		if (fileExtension == null) {
			return; // no extension, no description
		}

		// Obtain the description details for the file extension
		DescriptionStruct details = this.descriptions.get(fileExtension
				.toLowerCase());
		if (details == null) {
			return; // no description
		}

		// Load the descriptions
		if (details.contentEncoding != null) {
			description.setContentEncoding(details.contentEncoding);
		}
		if (details.contentType != null) {
			description.setContentType(details.contentType);
		}
	}

	/**
	 * Contains the description of a file extension.
	 */
	private static class DescriptionStruct {

		/**
		 * <code>Content-Encoding</code>.
		 */
		public final String contentEncoding;

		/**
		 * <code>Content-Type</code>.
		 */
		public final String contentType;

		/**
		 * Initiate.
		 *
		 * @param prototype
		 *            Prototype {@link DescriptionStruct} to base description.
		 *            May be <code>null</code>.
		 * @param contentEncoding
		 *            Overriding <code>Content-Encoding</code>.
		 *            <code>null</code> to not override.
		 * @param contentType
		 */
		public DescriptionStruct(DescriptionStruct prototype,
				String contentEncoding, String contentType) {
			if (prototype == null) {
				// No prototype, so use input values
				this.contentEncoding = contentEncoding;
				this.contentType = contentType;
			} else {
				// Have prototype, so only override if have value
				this.contentEncoding = (contentEncoding == null ? prototype.contentEncoding
						: contentEncoding);
				this.contentType = (contentType == null ? prototype.contentType
						: contentType);
			}
		}
	}

}