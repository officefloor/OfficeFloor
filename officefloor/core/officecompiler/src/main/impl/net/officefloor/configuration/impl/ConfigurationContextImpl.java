/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.configuration.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Properties;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.configuration.ConfigurationError;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.frame.api.source.SourceProperties;

/**
 * Abstract {@link ConfigurationContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class ConfigurationContextImpl implements ConfigurationContext {

	/**
	 * Default {@link Charset} name.
	 */
	private static final String DEFAULT_CHARSET_NAME = Charset.defaultCharset().name();

	/**
	 * Function interface to obtain the {@link InputStream} for the
	 * {@link ConfigurationItem}.
	 */
	public static interface ConfigurationSource {

		/**
		 * Implement to obtain the configuration.
		 * 
		 * @param location Location of the {@link ConfigurationItem}.
		 * @return {@link Reader} to the {@link ConfigurationItem}.
		 * @throws IOException If fails to obtain the {@link ConfigurationItem}.
		 */
		InputStream getConfigurationInputStream(String location) throws IOException;
	}

	/**
	 * {@link ConfigurationSource}.
	 */
	private final ConfigurationSource configurationSource;

	/**
	 * {@link SourceProperties}.
	 */
	private final SourceProperties properties;

	/**
	 * Instantiate.
	 * 
	 * @param configurationSource {@link ConfigurationSource}.
	 * @param properties          {@link SourceProperties}.
	 */
	public ConfigurationContextImpl(ConfigurationSource configurationSource, SourceProperties properties) {
		this.configurationSource = configurationSource;
		this.properties = properties;
	}

	/**
	 * Obtains the {@link ConfigurationSource}.
	 * 
	 * @return {@link ConfigurationSource}.
	 */
	protected ConfigurationSource getConfigurationSource() {
		return this.configurationSource;
	}

	/**
	 * <p>
	 * Creates the {@link ConfigurationItem}.
	 * <p>
	 * Provided to enable overriding the creation of the {@link ConfigurationItem}.
	 * 
	 * @param location         Location of the {@link ConfigurationItem}.
	 * @param rawConfiguration Raw configuration read from
	 *                         {@link ConfigurationSource}.
	 * @param configuration    Configuration with {@link Property} replacement.
	 * @param charset          Output {@link Charset}.
	 * @return {@link ConfigurationItem}.
	 */
	protected ConfigurationItem createConfigurationItem(String location, byte[] rawConfiguration, String configuration,
			Charset charset) {
		return new ConfigurationItemImpl(configuration, charset);
	}

	/*
	 * ======================== ConfigurationContext =======================
	 */

	@Override
	public ConfigurationItem getConfigurationItem(String location, PropertyList properties) {

		// Obtain the configuration item
		ConfigurationItem item = this.getOptionalConfigurationItem(location, properties);
		if (item == null) {
			throw new ConfigurationError(location);
		}

		// Return the item
		return item;
	}

	@Override
	public ConfigurationItem getOptionalConfigurationItem(String location, PropertyList properties) {
		try {

			// Obtain the configuration input stream
			InputStream inputStream = this.getConfigurationSource().getConfigurationInputStream(location);
			if (inputStream == null) {
				return null;
			}

			// Obtain the properties
			Properties tags = new Properties();
			if (this.properties != null) {
				String[] propertyNames = this.properties.getPropertyNames();
				for (String propertyName : propertyNames) {
					String propertyValue = this.properties.getProperty(propertyName);
					tags.setProperty(propertyName, propertyValue);
				}
			}

			// Override the properties
			if (properties != null) {
				for (Property property : properties) {
					tags.setProperty(property.getName(), property.getValue());
				}
			}

			// Obtain the charset
			String charsetName = tags.getProperty(PROPERTY_CONFIGURATION_INPUT_CHARSET);
			if (CompileUtil.isBlank(charsetName)) {
				charsetName = DEFAULT_CHARSET_NAME;
			}
			Charset charset = Charset.forName(charsetName);

			// Obtain the content (keeping track of raw configuration)
			ByteArrayOutputStream rawBuffer = new ByteArrayOutputStream();
			StringWriter buffer = new StringWriter();
			for (int byteValue = inputStream.read(); byteValue >= 0; byteValue = inputStream.read()) {
				rawBuffer.write(byteValue);
			}
			buffer.flush();
			byte[] rawContent = rawBuffer.toByteArray();

			// Obtain the content (in appropriate charset)
			String content = new String(rawContent, charset);

			// Obtain the tag prefix/suffix
			String tagPrefix = tags.getProperty(PROPERTY_CONFIGURATION_INPUT_TAG_PREFIX);
			if (CompileUtil.isBlank(tagPrefix)) {
				tagPrefix = DEFAULT_TAG_PREFIX;
			}
			String tagSuffix = tags.getProperty(PROPERTY_CONFIGURATION_INPUT_TAG_SUFFIX);
			if (CompileUtil.isBlank(tagSuffix)) {
				tagSuffix = DEFAULT_TAG_SUFFIX;
			}

			// Tag replace the content
			for (String propertyName : tags.stringPropertyNames()) {
				String propertyValue = tags.getProperty(propertyName);

				// Tag replace content
				content = content.replace(tagPrefix + propertyName + tagSuffix, propertyValue);
			}

			// Determine if tag was not replaced
			int startIndex = content.indexOf(tagPrefix);
			int endIndex = content.indexOf(tagSuffix, startIndex + tagPrefix.length());
			if ((startIndex >= 0) && (startIndex < endIndex)) {
				// Non configured tag
				String tagName = content.substring(startIndex + tagPrefix.length(), endIndex);
				throw new ConfigurationError(location, tagName);
			}

			// Obtain the output charset
			String outputCharsetName = tags.getProperty(PROPERTY_CONFIGURATION_OUTPUT_CHARSET);
			if (CompileUtil.isBlank(outputCharsetName)) {
				outputCharsetName = charsetName;
			}
			Charset outputCharset = Charset.forName(outputCharsetName);

			// Return the configuration item
			return this.createConfigurationItem(location, rawContent, content, outputCharset);

		} catch (Exception ex) {
			// Propagate the failure
			throw new ConfigurationError(location, ex);
		}
	}

	/**
	 * {@link ConfigurationItem} implementation.
	 */
	private static class ConfigurationItemImpl implements ConfigurationItem {

		/**
		 * Content.
		 */
		private final String content;

		/**
		 * {@link InputStream} {@link Charset}.
		 */
		private final Charset inputStreamCharset;

		/**
		 * Instantiate.
		 * 
		 * @param content            Content.
		 * @param inputStreamCharset {@link InputStream} {@link Charset}.
		 */
		private ConfigurationItemImpl(String content, Charset inputStreamCharset) {
			this.content = content;
			this.inputStreamCharset = inputStreamCharset;
		}

		/*
		 * =================== ConfigurationItem ============================
		 */

		@Override
		public Reader getReader() {
			return new StringReader(this.content);
		}

		@Override
		public InputStream getInputStream() throws ConfigurationError {
			return new ByteArrayInputStream(this.content.getBytes(this.inputStreamCharset));
		}
	}

}
