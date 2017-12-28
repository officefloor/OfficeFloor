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
package net.officefloor.web.resource.impl;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import net.officefloor.web.resource.HttpResource;
import net.officefloor.web.resource.build.HttpFileDescriber;
import net.officefloor.web.resource.build.HttpFileDescription;

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
	 * Prefix on {@link Properties} name to obtain the {@link Charset}.
	 */
	public static final String CHARSET_PREFIX = "charset.";

	/**
	 * Mapping of file extension to its {@link DescriptionStruct}.
	 */
	private final Map<String, DescriptionStruct> descriptions = new HashMap<String, DescriptionStruct>();

	/**
	 * Loads the default file extension descriptions.
	 */
	public void loadDefaultDescriptions() {

		// Obtain the default charset for text files
		Charset charset = Charset.defaultCharset();

		// Text related extension
		this.mapContentType("html", "text/html", charset);
		this.mapContentType("htm", "text/html", charset);
		this.mapContentType("css", "text/css", charset);
		this.mapContentType("xml", "text/xml", charset);
		this.mapContentType("txt", "text/plain", charset);

		// Image related extensions
		this.mapContentType("gif", "image/gif", null);
		this.mapContentType("png", "image/x-png", null);
		this.mapContentType("jpg", "image/jpeg", null);
		this.mapContentType("jpeg", "image/jpeg", null);
		this.mapContentType("jpe", "image/jpeg", null);

		// Audio related extensions
		this.mapContentType("wav", "audio/x-wav", null);
		this.mapContentType("mpa", "audio/x-mpeg", null);
		this.mapContentType("abs", "audio/x-mpeg", null);
		this.mapContentType("mpega", "audio/x-mpeg", null);
		this.mapContentType("mp2a", "audio/x-mpeg2", null);
		this.mapContentType("mpa2", "audio/x-mpeg2", null);

		// Video related extensions
		this.mapContentType("mpeg", "video/mpeg", null);
		this.mapContentType("mpg", "video/mpeg", null);
		this.mapContentType("mpe", "video/mpeg", null);
		this.mapContentType("mpv2", "video/mpeg2", null);
		this.mapContentType("mp2v", "video/mpeg2", null);
		this.mapContentType("qt", "video/quicktime", null);
		this.mapContentType("mov", "video/quicktime", null);
		this.mapContentType("avi", "video/x-msvideo", null);

		// Application related extensions
		this.mapContentType("ps", "application/postscript", null);
		this.mapContentType("js", "application/javascript", charset);
		this.mapContentType("pdf", "application/pdf", null);
		this.mapContentType("odt", "application/vnd.oasis.opendocument.text",
				null);
		this.mapContentType("odg",
				"application/vnd.oasis.opendocument.graphics", null);
		this.mapContentType("odp",
				"application/vnd.oasis.opendocument.presentation", null);
		this.mapContentType("ods",
				"application/vnd.oasis.opendocument.spreadsheet", null);
		this.mapContentType("odc", "application/vnd.oasis.opendocument.chart",
				null);
		this.mapContentType("doc", "application/msword", null);
		this.mapContentType("ppt", "application/mspowerpoint", null);
		this.mapContentType("xls", "application/x-msexcel", null);

		// Archive related extensions
		this.mapContentType("tar", "application/x-tar", null);
		this.mapContentType("zip", "application/zip", null);
		this.mapContentType("jar", "application/octet-stream", null);
		this.mapContentType("exe", "application/octet-stream", null);
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

				// Obtain the charset
				String charsetName = properties.getProperty(CHARSET_PREFIX
						+ fileExtension);
				Charset charset = (charsetName == null ? null : Charset
						.forName(charsetName));

				// Map the content-type
				this.mapContentType(fileExtension, contentType, charset);
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
		this.mapDescription(fileExtension, contentEncoding, null, null);
	}

	/**
	 * Maps the <code>Content-Type</code> for the file extension.
	 * 
	 * @param fileExtension
	 *            File extension.
	 * @param contentType
	 *            <code>Content-Encoding</code>.
	 * @param charset
	 *            {@link Charset}.
	 */
	public void mapContentType(String fileExtension, String contentType,
			Charset charset) {
		this.mapDescription(fileExtension, null, contentType, charset);
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
	 * @param charset
	 *            Overriding {@link Charset}.
	 */
	private void mapDescription(String fileExtension, String contentEncoding,
			String contentType, Charset charset) {

		// Always find by lower case file extension
		fileExtension = fileExtension.toLowerCase();

		// Obtain the details
		DescriptionStruct details = this.descriptions.get(fileExtension);

		// Override the details
		this.descriptions.put(fileExtension, new DescriptionStruct(details,
				contentEncoding, contentType, charset));
	}

	/*
	 * ==================== HttpFileDescriber ===========================
	 */

	@Override
	public boolean describe(HttpFileDescription description) {

		// Obtain the file extension
		String fileExtension = null;
		HttpResource resource = description.getResource();
		if (resource == null) {
			return false; // no resource, no description
		}
		String path = resource.getPath();
		if (path == null) {
			return false; // no path, no description
		}
		int extensionSplit = path.lastIndexOf('.');
		int nameSplit = path.lastIndexOf('/');
		if ((extensionSplit >= 0) && (extensionSplit > nameSplit)) {
			// Have extension so obtain it (+1 to skip '.')
			fileExtension = path.substring(extensionSplit + 1);
		}
		if (fileExtension == null) {
			return false; // no extension, no description
		}

		// Obtain the description details for the file extension
		DescriptionStruct details = this.descriptions.get(fileExtension
				.toLowerCase());
		if (details == null) {
			return false; // no description
		}

		// Load the descriptions
		if (details.contentEncoding != null) {
			description.setContentEncoding(details.contentEncoding);
		}
		if (details.contentType != null) {
			description.setContentType(details.contentType, details.charset);
		}

		// Description provided
		return true;
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
		 * {@link Charset}.
		 */
		public final Charset charset;

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
		 *            Overriding <code>Content-Type</code>. <code>null</code> to
		 *            not override.
		 * @param charset
		 *            Overriding {@link Charset} should the
		 *            <code>Content-Type</code> be overridden.
		 */
		public DescriptionStruct(DescriptionStruct prototype,
				String contentEncoding, String contentType, Charset charset) {
			if (prototype == null) {
				// No prototype, so use input values
				this.contentEncoding = contentEncoding;
				this.contentType = contentType;
				this.charset = charset;

			} else {
				// Have prototype, so only override if have value
				this.contentEncoding = (contentEncoding == null ? prototype.contentEncoding
						: contentEncoding);

				// Override if content-type is provided
				if (contentType == null) {
					this.contentType = null;
					this.charset = null;
				} else {
					this.contentType = contentType;
					this.charset = charset;
				}
			}
		}
	}

}