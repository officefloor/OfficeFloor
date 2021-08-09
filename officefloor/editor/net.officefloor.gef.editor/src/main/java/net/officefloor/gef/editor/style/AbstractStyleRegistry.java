/*-
 * #%L
 * [bundle] OfficeFloor Editor
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
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

package net.officefloor.gef.editor.style;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Abstract {@link StyleRegistry}.
 * 
 * @author Daniel Sagenschneider
 */
public class AbstractStyleRegistry implements StyleRegistry {

	/**
	 * {@link URL} protocol.
	 */
	public static final String PROTOCOL = "officefloorstyle";

	/**
	 * <p>
	 * Mapping of {@link URL} to style sheet content {@link Property}.
	 * <p>
	 * Allow {@link Property} instances to be GC'ed once no longer used by editor.
	 */
	private static Map<String, ReadOnlyProperty<String>> urlPathToStyleContent = new HashMap<>();

	/**
	 * Next instance index.
	 */
	private static AtomicInteger nextInstanceIndex = new AtomicInteger(1);

	/**
	 * Creates the {@link URLStreamHandler}.
	 * 
	 * @param url
	 *            {@link URL} to open.
	 * @return {@link URLStreamHandler}.
	 * @throws IOException
	 *             If fails to open the {@link URLConnection}.
	 */
	public static URLConnection openConnection(URL url) throws IOException {
		OfficeFloorUrlConnection connection = new OfficeFloorUrlConnection(url);
		connection.connect();
		return connection;
	}

	/**
	 * {@link URLConnection} implementation to retrieve style sheet.
	 */
	private static class OfficeFloorUrlConnection extends URLConnection {

		/**
		 * Stylesheet content.
		 */
		private ReadOnlyProperty<String> stylesheetContent;

		/**
		 * Instantiate.
		 * 
		 * @param url
		 *            {@link URL}.
		 */
		protected OfficeFloorUrlConnection(URL url) {
			super(url);
		}

		/*
		 * ============= URLConnection ======================
		 */

		@Override
		public void connect() throws IOException {
			String urlPath = this.getURL().getPath();
			this.stylesheetContent = urlPathToStyleContent.get(urlPath);
			if (this.stylesheetContent == null) {
				throw new IOException("URL " + this.getURL() + " has no style sheet registered");
			}
		}

		@Override
		public InputStream getInputStream() throws IOException {

			// Obtain the content
			byte[] data = new byte[0];
			String content = this.stylesheetContent.getValue();
			if (content != null) {
				data = content.getBytes();
			}

			// Return input stream to content
			return new ByteArrayInputStream(data);
		}
	}

	/**
	 * Index for current instance.
	 */
	private final int instanceIndex;

	/**
	 * Instantiate.
	 */
	public AbstractStyleRegistry() {
		this.instanceIndex = nextInstanceIndex.getAndIncrement();
	}

	/**
	 * Obtains the style sheet content.
	 * 
	 * @param url
	 *            {@link URL}.
	 * @return {@link ReadOnlyProperty} to the content of the style sheet for the
	 *         {@link URL}. May be <code>null</code> if editor no longer active.
	 */
	public ReadOnlyProperty<String> getStylesheetContent(String url) {
		return null;
	}

	/**
	 * Obtains the {@link URL} for the configuration path.
	 * 
	 * @param configurationPath
	 *            Configuration path.
	 * @param version
	 *            Version of the content for the {@link URL}.
	 * @return {@link URL} for the configuration path.
	 */
	protected URL getUrl(String configurationPath, int version) {
		String url = PROTOCOL + "://in.memory.host/" + String.valueOf(this.instanceIndex) + "/" + configurationPath
				+ "?version=" + version;
		try {
			return new URL(url);
		} catch (MalformedURLException ex) {
			throw new IllegalStateException("Failed to create URL for " + url, ex);
		}
	}

	/**
	 * =============== StyleRegistry =====================
	 */

	@Override
	public ReadOnlyProperty<URL> registerStyle(String configurationPath, ReadOnlyProperty<String> stylesheetContent) {

		// Determine the URL
		int[] version = new int[] { 0 };
		URL url = this.getUrl(configurationPath, version[0]++);

		// Use path to find regardless of version (but trigger new URL for styling)
		String urlPath = url.getPath();

		// Load the mapping
		ReadOnlyProperty<String> stylesheetContentProperty = urlPathToStyleContent.get(urlPath);
		if (stylesheetContentProperty != null) {
			throw new IllegalStateException("Stylesheet already registered for URL " + url);
		}

		// Register the style property
		urlPathToStyleContent.put(urlPath, stylesheetContent);

		// Create property for URL
		Property<URL> urlProperty = new SimpleObjectProperty<>(url);

		// Trigger reload of URL on change to style content
		stylesheetContent.addListener((change) -> {
			urlProperty.setValue(this.getUrl(configurationPath, version[0]++));
		});

		// Return the URL property
		return urlProperty;
	}

}
