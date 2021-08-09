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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;

import net.officefloor.compile.impl.properties.PropertyListSourceProperties;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.configuration.ConfigurationError;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationContext;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.configuration.impl.ConfigurationContextImpl.ConfigurationSource;

/**
 * Abstract {@link WritableConfigurationContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class AbstractWritableConfigurationContext extends ConfigurationContextImpl
		implements WritableConfigurationContext {

	/**
	 * Function interface to write the {@link InputStream} for the
	 * {@link ConfigurationItem}.
	 */
	public static interface ConfigurationWriter {

		/**
		 * Implement to write the configuration.
		 * 
		 * @param location
		 *            Location of the {@link ConfigurationItem}.
		 * @param isCreate
		 *            Indicates if creating the {@link ConfigurationItem}.
		 * @param configuration
		 *            Content for the {@link ConfigurationItem}.
		 * @throws IOException
		 *             If fails to write the {@link ConfigurationItem}.
		 */
		void writeConfiguration(String location, boolean isCreate, InputStream configuration) throws IOException;
	}

	/**
	 * Function interface to remove the {@link ConfigurationItem}.
	 */
	public static interface ConfigurationRemover {

		/**
		 * Implement to remove the configuration.
		 * 
		 * @param location
		 *            Location of the {@link ConfigurationItem}.
		 * @throws IOException
		 *             If fails to remove the {@link ConfigurationItem}.
		 */
		void removeConfiguration(String location) throws IOException;
	}

	/**
	 * {@link ConfigurationSource}.
	 */
	private ConfigurationSource configurationSource;

	/**
	 * {@link ConfigurationWriter}.
	 */
	private ConfigurationWriter configurationWriter;

	/**
	 * {@link ConfigurationRemover}.
	 */
	private ConfigurationRemover configurationRemover;

	/**
	 * Instantiate.
	 * 
	 * @param configurationSource
	 *            {@link ConfigurationSource}.
	 * @param configurationWriter
	 *            {@link ConfigurationWriter}.
	 * @param configurationRemover
	 *            {@link ConfigurationRemover}.
	 * @param properties
	 *            {@link PropertyList}.
	 */
	public AbstractWritableConfigurationContext(ConfigurationSource configurationSource,
			ConfigurationWriter configurationWriter, ConfigurationRemover configurationRemover,
			PropertyList properties) {
		super(configurationSource, new PropertyListSourceProperties(properties));
		this.init(configurationSource, configurationWriter, configurationRemover);
	}

	/**
	 * Default construction. Must invoke <code>init()</code> method to use.
	 */
	public AbstractWritableConfigurationContext() {
		super(null, null);
	}

	/**
	 * Instantiate.
	 * 
	 * @param configurationSource
	 *            {@link ConfigurationSource}.
	 * @param configurationWriter
	 *            {@link ConfigurationWriter}.
	 * @param configurationRemover
	 *            {@link ConfigurationRemover}.
	 */
	protected void init(ConfigurationSource configurationSource, ConfigurationWriter configurationWriter,
			ConfigurationRemover configurationRemover) {
		this.configurationSource = configurationSource;
		this.configurationWriter = configurationWriter;
		this.configurationRemover = configurationRemover;
	}

	/*
	 * ======================== ConfigurationContext =========================
	 */

	@Override
	protected ConfigurationSource getConfigurationSource() {
		return this.configurationSource;
	}

	@Override
	protected ConfigurationItem createConfigurationItem(String location, byte[] rawConfiguration, String configuration,
			Charset charset) {

		// Always provide writable instance
		ConfigurationItem item = super.createConfigurationItem(location, rawConfiguration, configuration, charset);
		return new WritableConfigurationItemImpl(location, item);
	}

	/*
	 * ====================== WritableConfigurationContext ====================
	 */

	@Override
	public WritableConfigurationItem getWritableConfigurationItem(String location) throws IOException {
		return new WritableConfigurationItemImpl(location, null);
	}

	@Override
	public WritableConfigurationItem createConfigurationItem(String location, InputStream configuration)
			throws IOException {

		// Write the configuration
		this.configurationWriter.writeConfiguration(location, true, configuration);

		// Create the configuration item
		return (WritableConfigurationItem) this.getConfigurationItem(location, null);
	}

	@Override
	public void deleteConfigurationItem(String location) throws IOException {
		this.configurationRemover.removeConfiguration(location);
	}

	/**
	 * {@link WritableConfigurationItem} implementation.
	 */
	private class WritableConfigurationItemImpl implements WritableConfigurationItem {

		/**
		 * Location.
		 */
		private final String location;

		/**
		 * {@link ConfigurationItem}.
		 */
		private ConfigurationItem configurationItem;

		/**
		 * Instantiate.
		 * 
		 * @param location
		 *            Location.
		 * @param configurationItem
		 *            {@link ConfigurationItem}.
		 */
		private WritableConfigurationItemImpl(String location, ConfigurationItem configurationItem) {
			this.location = location;
			this.configurationItem = configurationItem;
		}

		/*
		 * ==================== WritableConfigurationItem ====================
		 */

		@Override
		public Reader getReader() throws ConfigurationError {
			if (this.configurationItem == null) {
				return AbstractWritableConfigurationContext.this.getConfigurationItem(this.location, null).getReader();
			} else {
				return this.configurationItem.getReader();
			}
		}

		@Override
		public InputStream getInputStream() throws ConfigurationError {
			if (this.configurationItem == null) {
				return AbstractWritableConfigurationContext.this.getConfigurationItem(this.location, null)
						.getInputStream();
			} else {
				return this.configurationItem.getInputStream();
			}
		}

		@Override
		public InputStream getRawConfiguration() throws IOException {
			return AbstractWritableConfigurationContext.this.configurationSource
					.getConfigurationInputStream(this.location);
		}

		@Override
		public void setConfiguration(InputStream configuration) throws IOException {

			// Configuration changing, so must clear cached data
			this.configurationItem = null;

			// Change configuration
			AbstractWritableConfigurationContext.this.configurationWriter.writeConfiguration(this.location, false,
					configuration);
		}

		@Override
		public WritableConfigurationContext getContext() {
			return AbstractWritableConfigurationContext.this;
		}
	}

}
