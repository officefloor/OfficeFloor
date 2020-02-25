/*-
 * #%L
 * Web on OfficeFloor
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
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

package net.officefloor.woof;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.officefloor.compile.impl.ApplicationOfficeFloorSource;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.server.http.HttpServer;
import net.officefloor.woof.model.objects.WoofObjectsModel;
import net.officefloor.woof.model.resources.WoofResourcesModel;
import net.officefloor.woof.model.teams.WoofTeamsModel;
import net.officefloor.woof.model.woof.WoofModel;

/**
 * Settings for the {@link WoofLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofLoaderSettings {

	/**
	 * Default path for WoOF configuration file.
	 */
	public static final String DEFAULT_WOOF_PATH = "application.properties";

	/**
	 * {@link Office} name to alias configuration name.
	 */
	private static final Map<String, String[]> officeToAliasNames = new HashMap<>();

	static {
		officeToAliasNames.put(ApplicationOfficeFloorSource.OFFICE_NAME, new String[] { "application" });
	}

	/**
	 * {@link ThreadLocal} for the {@link Office} name to
	 * {@link WoofLoaderConfiguration}.
	 */
	private static final ThreadLocal<Map<String, WoofLoaderConfiguration>> configuration = new ThreadLocal<>();

	/**
	 * Obtains the {@link WoofLoaderConfiguration} for an {@link Office}.
	 * 
	 * @param officeName Name of {@link Office}.
	 * @return {@link WoofLoaderConfiguration} for the {@link Office}.
	 */
	public static WoofLoaderConfiguration getWoofLoaderConfiguration(String officeName) {

		// Determine if override configuration
		Map<String, WoofLoaderConfiguration> offices = configuration.get();
		if (offices != null) {
			WoofLoaderConfiguration config = offices.get(officeName);
			if (config != null) {
				return config;
			}
		}

		// As here, provide default configuration
		return new WoofLoaderConfiguration(officeName);
	}

	/**
	 * Undertakes a contextual load.
	 *
	 * @param <R>      Return type from {@link WoofLoaderRunnable}.
	 * @param <E>      Possible {@link Throwable} from {@link WoofLoaderRunnable}.
	 * @param runnable {@link WoofLoaderRunnable} to configure the contextual load.
	 * @return Returned object from {@link WoofLoaderRunnable}.
	 * @throws E Potential failure.
	 */
	public static <R, E extends Throwable> R contextualLoad(WoofLoaderRunnable<R, E> runnable) throws E {

		// Lazy create the woof loader configuration
		Map<String, WoofLoaderConfiguration> offices = configuration.get();
		boolean isCleanUp = false;
		if (offices == null) {
			offices = new HashMap<>();
			configuration.set(offices);

			// Clean up once leave this contextual load
			isCleanUp = true;
		}

		try {

			// Create context for runnable
			String defaultOfficeName = ApplicationOfficeFloorSource.OFFICE_NAME;
			WoofLoaderConfiguration defaultOfficeConfig = offices.get(defaultOfficeName);
			if (defaultOfficeConfig == null) {
				defaultOfficeConfig = new WoofLoaderConfiguration(defaultOfficeName);
				offices.put(defaultOfficeName, defaultOfficeConfig);
			}
			WoofLoaderRunnableContext context = new WoofLoaderRunnableContextImpl(defaultOfficeConfig, offices);

			// Undertake runnable
			return runnable.run(context);

		} finally {
			// Clean up if created
			if (isCleanUp) {
				configuration.remove();
			}
		}
	}

	/**
	 * Runs within a context.
	 */
	public static interface WoofLoaderRunnable<R, E extends Throwable> {

		/**
		 * Runs.
		 * 
		 * @param context {@link WoofLoaderRunnableContext}.
		 * @return Allows for return an object.
		 * @throws E Potential failure.
		 */
		R run(WoofLoaderRunnableContext context) throws E;
	}

	/**
	 * {@link WoofLoaderRunnable} context. Will be
	 * {@link WoofLoaderConfigurerContext} for the default {@link Office}.
	 */
	public static interface WoofLoaderRunnableContext extends WoofLoaderConfigurerContext {

		/**
		 * Configures the particular {@link Office}.
		 * 
		 * @param officeName Name of {@link Office}.
		 * @param configurer {@link WoofLoaderConfigurer} for the {@link Office}.
		 */
		void configure(String officeName, WoofLoaderConfigurer configurer);
	}

	/**
	 * Configurer for an {@link Office}.
	 */
	public static interface WoofLoaderConfigurer {

		/**
		 * Configures the {@link Office}.
		 * 
		 * @param context {@link WoofLoaderConfigurerContext}.
		 * @throws Exception If fails to configure.
		 */
		void configure(WoofLoaderConfigurerContext context);
	}

	/**
	 * {@link WoofLoaderConfigurer} context.
	 */
	public static interface WoofLoaderConfigurerContext {

		/**
		 * Flags to not load any configuration.
		 */
		void notLoad();

		/**
		 * Flags to not load the {@link HttpServer}.
		 */
		void notLoadHttpServer();

		/**
		 * Flags not to load the {@link WoofTeamsModel} configuration.
		 */
		void notLoadTeams();

		/**
		 * Flags not to load the {@link WoofModel} configuration.
		 */
		void notLoadWoof();

		/**
		 * Flags not to load the {@link WoofObjectsModel} configuration.
		 */
		void notLoadObjects();

		/**
		 * Flags not to load the {@link WoofResourcesModel} configuration.
		 */
		void notLoadResources();

		/**
		 * Flags not to load the {@link Properties} configuration.
		 */
		void notLoadProperties();

		/**
		 * Flags not to load the {@link WoofExtensionService} instances.
		 */
		void notLoadWoofExtensions();

		/**
		 * Adds {@link WoofExtensionService}.
		 * 
		 * @param extension {@link WoofExtensionService}.
		 */
		void extend(WoofExtensionService extension);

		/**
		 * <p>
		 * Specifies the resource path to the WoOF configuration file.
		 * <p>
		 * This is useful for testing to specify different configuration files for
		 * different tests.
		 * 
		 * @param path Resource path to to the WoOF configuration file.
		 */
		void setWoofPath(String path);

		/**
		 * <p>
		 * Specifies the resource path to the objects configuration file.
		 * <p>
		 * This is useful for testing to specify different configuration files for
		 * different tests.
		 * 
		 * @param path Resource path to to the objects configuration file.
		 */
		void setObjectsPath(String path);

		/**
		 * <p>
		 * Specifies the resource path to the teams configuration file.
		 * <p>
		 * This is useful for testing to specify different configuration files for
		 * different tests.
		 * 
		 * @param path Resource path to to the teams configuration file.
		 */
		void setTeamsPath(String path);

		/**
		 * <p>
		 * Specifies the resource path to the resources configuration file.
		 * <p>
		 * This is useful for testing to specify different configuration files for
		 * different tests.
		 * 
		 * @param path Resource path to to the resources configuration file.
		 */
		void setResourcesPath(String path);

		/**
		 * <p>
		 * Specifies the resource path to the {@link Properties} configuration file.
		 * <p>
		 * This is useful for testing to specify different configuration files for
		 * different tests.
		 * 
		 * @param path Resource path to to the {@link Properties} configuration file.
		 */
		void setPropertiesPath(String path);
	}

	/**
	 * {@link WoofLoaderRunnableContext} implementation.
	 */
	private static class WoofLoaderRunnableContextImpl implements WoofLoaderRunnableContext {

		/**
		 * {@link WoofLoaderConfiguration}.
		 */
		private final WoofLoaderConfiguration config;

		/**
		 * {@link WoofLoaderConfiguration} for the {@link Office} instances.
		 */
		private final Map<String, WoofLoaderConfiguration> offices;

		/**
		 * Instantiate.
		 * 
		 * @param config  {@link WoofLoaderConfiguration}.
		 * @param offices {@link WoofLoaderConfiguration} for the {@link Office}
		 *                instances.
		 */
		private WoofLoaderRunnableContextImpl(WoofLoaderConfiguration config,
				Map<String, WoofLoaderConfiguration> offices) {
			this.config = config;
			this.offices = offices;
		}

		/*
		 * ================ WoofLoaderRunnableContext ================
		 */

		@Override
		public void configure(String officeName, WoofLoaderConfigurer configurer) {

			// Lazy obtain the configuration
			WoofLoaderConfiguration config = this.offices.get(officeName);
			if (config == null) {
				config = new WoofLoaderConfiguration(officeName);
				this.offices.put(officeName, config);
			}

			// Undertake configuration
			configurer.configure(new WoofLoaderRunnableContextImpl(config, this.offices));
		}

		/*
		 * ================ WoofLoaderConfigurerContext ================
		 */

		@Override
		public void notLoad() {
			this.notLoadWoof();
			this.notLoadTeams();
			this.notLoadResources();
			this.notLoadObjects();
			this.notLoadHttpServer();
			this.notLoadWoofExtensions();
		}

		@Override
		public void notLoadWoof() {
			this.config.isLoadWoof = false;
		}

		@Override
		public void notLoadTeams() {
			this.config.isLoadTeams = false;
		}

		@Override
		public void notLoadResources() {
			this.config.isLoadResources = false;
		}

		@Override
		public void notLoadObjects() {
			this.config.isLoadObjects = false;
		}

		@Override
		public void notLoadHttpServer() {
			this.config.isLoadHttpServer = false;
		}

		@Override
		public void notLoadProperties() {
			this.config.isLoadProperties = false;
		}

		@Override
		public void notLoadWoofExtensions() {
			this.config.isLoadWoofExtensions = false;
		}

		@Override
		public void extend(WoofExtensionService extension) {
			this.config.contextualExtensions.add(extension);
		}

		@Override
		public void setWoofPath(String path) {
			this.config.woofPath = path;
		}

		@Override
		public void setObjectsPath(String path) {
			this.config.objectsPath = path;
		}

		@Override
		public void setTeamsPath(String path) {
			this.config.teamsPath = path;
		}

		@Override
		public void setResourcesPath(String path) {
			this.config.resourcesPath = path;
		}

		@Override
		public void setPropertiesPath(String path) {
			this.config.propertiesPath = path;
		}
	}

	/**
	 * Configuration for the {@link WoofLoader}.
	 */
	public static class WoofLoaderConfiguration {

		/**
		 * Name of the {@link Office}.
		 */
		private final String officeName;

		/**
		 * Resource paths to the WoOF configuration file.
		 */
		private String woofPath = null;

		/**
		 * Resource path to the objects configuration file.
		 */
		private String objectsPath = null;

		/**
		 * Resource path to the teams configuration file.
		 */
		private String teamsPath = null;

		/**
		 * Resource path to the resources configuration file.
		 */
		private String resourcesPath = null;

		/**
		 * Resource path to the {@link Properties} configuration file.
		 */
		private String propertiesPath = null;

		/**
		 * Indicates to load the {@link HttpServer}.
		 */
		private boolean isLoadHttpServer = true;

		/**
		 * Indicates to load the {@link WoofModel} configuration.
		 */
		private boolean isLoadWoof = true;

		/**
		 * Indicates to load the {@link WoofObjectsModel} configuration.
		 */
		private boolean isLoadObjects = true;

		/**
		 * Indicates to load the {@link WoofTeamsModel} configuration.
		 */
		private boolean isLoadTeams = true;

		/**
		 * Indicates to load the {@link WoofResourcesModel} configuration.
		 */
		private boolean isLoadResources = true;

		/**
		 * Indicates to load the {@link Properties} configuration.
		 */
		private boolean isLoadProperties = true;

		/**
		 * Indicates to load the {@link WoofExtensionService} instances.
		 */
		private boolean isLoadWoofExtensions = true;

		/**
		 * Contextually added {@link WoofExtensionService} instances.
		 */
		private final List<WoofExtensionService> contextualExtensions = new LinkedList<>();

		/**
		 * Instantiate.
		 * 
		 * @param officeName Name of the {@link Office}.
		 */
		private WoofLoaderConfiguration(String officeName) {
			this.officeName = officeName;
		}

		/**
		 * Obtains the {@link ConfigurationItem} to the WooF configuration file.
		 * 
		 * @param context {@link ConfigurationContext}.
		 * @return {@link ConfigurationItem} to the application WooF configuration file.
		 *         May be <code>null</code> if not found.
		 */
		public ConfigurationItem getWoofConfiguration(ConfigurationContext context) {
			return this.getConfigurationItem(this.woofPath, ".woof", context);
		}

		/**
		 * Obtains the {@link ConfigurationItem} to the objects configuration file.
		 * 
		 * @param context {@link ConfigurationContext}.
		 * @return {@link ConfigurationItem} to the objects configuration file. May be
		 *         <code>null</code> if not found.
		 */
		public ConfigurationItem getObjectsConfiguration(ConfigurationContext context) {
			return this.getConfigurationItem(this.objectsPath, ".objects", context);
		}

		/**
		 * Obtains the {@link ConfigurationItem} to the teams configuration file.
		 * 
		 * @param context {@link ConfigurationContext}.
		 * @return {@link ConfigurationItem} to the teams configuration file. May be
		 *         <code>null</code> if not found.
		 */
		public ConfigurationItem getTeamsConfiguration(ConfigurationContext context) {
			return this.getConfigurationItem(this.teamsPath, ".teams", context);
		}

		/**
		 * Obtains the {@link ConfigurationItem} to the resources configuration file.
		 * 
		 * @param context {@link ConfigurationContext}.
		 * @return {@link ConfigurationItem} to the resources configuration file.
		 */
		public ConfigurationItem getResourcesConfiguration(ConfigurationContext context) {
			return this.getConfigurationItem(this.resourcesPath, ".resources", context);
		}

		/**
		 * Obtains the {@link ConfigurationItem} to the {@link Properties} configuration
		 * file.
		 * 
		 * @param profile Profile name. May be <code>null</code> for default
		 *                {@link Properties}.
		 * @param context {@link ConfigurationContext}.
		 * @return {@link ConfigurationItem} to the {@link Properties} configuration
		 *         file.
		 */
		public ConfigurationItem getPropertiesConfiguration(String profile, ConfigurationContext context) {
			return this.getConfigurationItem(this.propertiesPath,
					(profile == null ? "" : "." + profile) + ".properties", context);
		}

		/**
		 * Indicates if contextual load.
		 * 
		 * @return <code>true</code> if contextual load.
		 */
		public boolean isContextualLoad() {
			return configuration.get() != null;
		}

		/**
		 * Indicates to load the {@link HttpServer}.
		 * 
		 * @return <code>true</code> to load.
		 */
		public boolean isLoadHttpServer() {
			return this.isLoadHttpServer;
		}

		/**
		 * Indicates to load {@link WoofModel} configuration.
		 * 
		 * @return <code>true</code> to load.
		 */
		public boolean isLoadWoof() {
			return this.isLoadWoof;
		}

		/**
		 * Indicates to load the {@link WoofObjectsModel} configuration.
		 * 
		 * @return <code>true</code> to load.
		 */
		public boolean isLoadObjects() {
			return this.isLoadObjects;
		}

		/**
		 * Indicates to load the {@link WoofTeamsModel} configuration.
		 * 
		 * @return <code>true</code> to load.
		 */
		public boolean isLoadTeams() {
			return this.isLoadTeams;
		}

		/**
		 * Indicates to load the {@link WoofResourcesModel} configuration.
		 * 
		 * @return <code>true</code> to load.
		 */
		public boolean isLoadResources() {
			return this.isLoadResources;
		}

		/**
		 * Indicates to load the {@link Properties} configuration.
		 * 
		 * @return <code>true</code> to load.
		 */
		public boolean isLoadProperties() {
			return this.isLoadProperties;
		}

		/**
		 * Indicates to load the {@link WoofExtensionService} instances.
		 * 
		 * @return <code>true</code> to load.
		 */
		public boolean isLoadWoofExtensions() {
			return this.isLoadWoofExtensions;
		}

		/**
		 * Contextually added {@link WoofExtensionService} instances.
		 * 
		 * @return <code>true</code> to load.
		 */
		public WoofExtensionService[] getContextualWoofExtensionServices() {
			return this.contextualExtensions.toArray(new WoofExtensionService[this.contextualExtensions.size()]);
		}

		/**
		 * Determines if a WoOF application.
		 * 
		 * @param context {@link ConfigurationContext}.
		 * @return <code>true</code> if a WoOF application.
		 * @throws IOException If fails to check if WoOF application.
		 */
		public boolean isWoofApplication(ConfigurationContext context) throws IOException {

			// Determine if configuring extensions
			if (contextualExtensions.size() > 0) {
				return true; // configures WoOF
			}

			// No extension, so determine if configuration available
			return isApplicationWoofAvailable(context);
		}

		/**
		 * Determines if the WoOF configuration available.
		 * 
		 * @param context {@link ConfigurationContext}.
		 * @return <code>true</code> if WoOF configuration available.
		 * @throws IOException If fails to check if WoOF application available.
		 */
		public boolean isApplicationWoofAvailable(ConfigurationContext context) throws IOException {

			// Obtain configuration file
			ConfigurationItem config = this.getWoofConfiguration(context);
			if (config != null) {
				config.getInputStream().close();
			}

			// WoOF application if configuration file
			return config != null;
		}

		/**
		 * Obtains the {@link ConfigurationItem}.
		 * 
		 * @param path      Specific path to use. May be <code>null</code> to search via
		 *                  {@link Office} details.
		 * @param extension Extension for the {@link ConfigurationItem}.
		 * @param context   {@link ConfigurationContext}.
		 * @return {@link ConfigurationItem} or <code>null</code> if not found.
		 */
		private ConfigurationItem getConfigurationItem(String path, String extension, ConfigurationContext context) {

			// Determine if specific path
			if (!CompileUtil.isBlank(path)) {
				return context.getOptionalConfigurationItem(path, null);
			}

			// Attempt to find via Office name
			path = this.officeName + extension;
			ConfigurationItem configurationItem = context.getOptionalConfigurationItem(path, null);
			if (configurationItem != null) {
				return configurationItem; // found by Office name
			}

			// Determine if alias name for Office
			String[] aliasNames = officeToAliasNames.get(this.officeName);
			if (aliasNames != null) {
				for (String aliasName : aliasNames) {
					path = aliasName + extension;
					configurationItem = context.getOptionalConfigurationItem(path, null);
					if (configurationItem != null) {
						return configurationItem; // found by alias name
					}
				}
			}

			// As here, no configuration found
			return null;
		}
	}

}