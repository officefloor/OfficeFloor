/*-
 * #%L
 * Web on OfficeFloor
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

package net.officefloor.woof;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import net.officefloor.compile.impl.ApplicationOfficeFloorSource;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.properties.Property;
import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.SourceContext;
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
	public static final String DEFAULT_WOOF_PATH = "application.woof";

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
		 * Avoid loading external configuration. This is typically for testing to ensure
		 * external configurations do not cause false positive test failures.
		 */
		void notLoadExternal();

		/**
		 * Flags not to load the additional profiles.
		 */
		void notLoadAdditionalProfiles();

		/**
		 * Flags not to load the override {@link Properties} configuration.
		 */
		void notLoadOverrideProperties();

		/**
		 * Flags not to load the {@link WoofExtensionService} instances.
		 */
		void notLoadWoofExtensions();

		/**
		 * Adds a profile.
		 * 
		 * @param profile Profile.
		 */
		void addProfile(String profile);

		/**
		 * Adds an override {@link Property}.
		 * 
		 * @param name  Name of {@link Property}.
		 * @param value Value of {@link Property}.
		 */
		void addOverrideProperty(String name, String value);

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
			this.notLoadOverrideProperties();
			this.notLoadAdditionalProfiles();
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
		public void notLoadExternal() {
			this.config.isLoadExternal = false;
		}

		@Override
		public void notLoadAdditionalProfiles() {
			this.config.isLoadAdditionalProfiles = false;
		}

		@Override
		public void notLoadOverrideProperties() {
			this.config.isLoadOverrideProperties = false;
		}

		@Override
		public void notLoadWoofExtensions() {
			this.config.isLoadWoofExtensions = false;
		}

		@Override
		public void addProfile(String profile) {
			this.config.profiles.add(profile);
		}

		@Override
		public void addOverrideProperty(String name, String value) {
			this.config.overrideProperties.setProperty(name, value);
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
		 * Indicates to load external {@link Property} and profiles.
		 */
		private boolean isLoadExternal = true;

		/**
		 * Indicates to load the additional profiles.
		 */
		private boolean isLoadAdditionalProfiles = true;

		/**
		 * Indicates to load the {@link Properties} configuration.
		 */
		private boolean isLoadOverrideProperties = true;

		/**
		 * Indicates to load the {@link WoofExtensionService} instances.
		 */
		private boolean isLoadWoofExtensions = true;

		/**
		 * Additional profiles.
		 */
		private final List<String> profiles = new LinkedList<>();

		/**
		 * Override {@link Properties}.
		 */
		private final Properties overrideProperties = new Properties();

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
		 * Obtains the additional profiles.
		 * 
		 * @param context {@link SourceContext}.
		 * @return Additional profiles.
		 */
		public String[] getAdditionalProfiles(SourceContext context) {

			// Functionality to load profiles
			List<String> profiles = new LinkedList<>();
			Consumer<String> profileLoader = (propertyValue) -> {
				if (!CompileUtil.isBlank(propertyValue)) {
					for (String profile : propertyValue.split(",")) {
						if (!CompileUtil.isBlank(profile)) {
							String rawProfile = profile.trim();
							if (!profiles.contains(rawProfile)) {
								profiles.add(rawProfile);
							}
						}
					}
				}
			};
			Consumer<Function<String, String>> sourceLoader = (profileGet) -> {

				// Load the office named profiles
				String propertyValue = profileGet.apply(this.officeName);
				profileLoader.accept(propertyValue);

				// Load the office alias named profiles
				String[] aliases = officeToAliasNames.get(this.officeName);
				if (aliases != null) {
					for (String alias : aliases) {
						propertyValue = profileGet.apply(alias);
						profileLoader.accept(propertyValue);
					}
				}
			};

			// Various profile sources
			final String SUFFIX = ".profiles";
			Function<String, String> contextualSource = (name) -> String.join(",", this.profiles);
			Function<String, String> contextSource = (name) -> context.getProperty(name + SUFFIX, null);
			Function<String, String> systemSource = (name) -> System.getProperty(name + SUFFIX);
			Function<String, String> environmentSource = (name) -> System
					.getenv(OfficeFloor.class.getSimpleName().toUpperCase() + "." + name + SUFFIX);

			// Load profiles
			sourceLoader.accept(contextualSource);
			sourceLoader.accept(contextSource);
			if (this.isLoadExternal) {
				sourceLoader.accept(systemSource);
				sourceLoader.accept(environmentSource);
			}

			// Return the profiles
			return profiles.toArray(new String[profiles.size()]);
		}

		/**
		 * Obtains the override {@link Properties}.
		 * 
		 * @param sourceContext        {@link SourceContext}.
		 * @param configurationContext {@link ConfigurationContext}.
		 * @return Override {@link Properties}.
		 */
		public Properties getOverrideProperties(SourceContext sourceContext,
				ConfigurationContext configurationContext) {

			// Functionality to load properties
			Properties overrideProperties = new Properties();
			Consumer<Function<String, Properties>> sourceLoader = (propertiesGet) -> {

				// Load the office alias named properties
				String[] aliases = officeToAliasNames.get(this.officeName);
				if (aliases != null) {
					for (String alias : aliases) {
						Properties properties = propertiesGet.apply(alias);
						overrideProperties.putAll(properties);
					}
				}

				// Load the office properties (takes precedence)
				Properties properties = propertiesGet.apply(this.officeName);
				overrideProperties.putAll(properties);
			};
			BiFunction<String, Boolean, Properties> configLoader = (fileName, isRequired) -> {
				Properties properties = new Properties();
				ConfigurationItem item = isRequired ? configurationContext.getConfigurationItem(fileName, null)
						: configurationContext.getOptionalConfigurationItem(fileName, null);
				if (item != null) {
					try {
						properties.load(item.getInputStream());
					} catch (IOException ex) {
						throw new PropertiesLoadException("Failed to load properties file " + fileName, ex);
					}
				}
				return properties;
			};
			File userConfigDir = new File(System.getProperty("user.home"), ".config/officefloor");
			Function<String, Properties> userFileLoader = (fileName) -> {
				Properties properties = new Properties();
				File userFile = new File(userConfigDir, fileName);
				if (userFile.exists()) {
					try (InputStream input = new FileInputStream(userFile)) {
						properties.load(input);
					} catch (IOException ex) {
						throw new PropertiesLoadException(
								"Failed to load properties file " + userFile.getAbsolutePath(), ex);
					}
				}
				return properties;
			};
			BiFunction<Properties, String, Properties> propertiesFilter = (properties, name) -> {
				String prefix = name + ".";
				Properties filteredProperties = new Properties();
				for (String propertyName : properties.stringPropertyNames()) {
					if (propertyName.startsWith(prefix)) {
						String overrideName = propertyName.substring(prefix.length());
						String value = properties.getProperty(propertyName);
						overrideProperties.setProperty(overrideName, value);
					}
				}
				return filteredProperties;
			};
			BiFunction<String, Function<String, Properties>, Properties> profilesLoader = (name, fileLoader) -> {
				Properties properties = new Properties();

				// Load non-profile as least precedent
				final String EXTENSION = ".properties";
				properties.putAll(fileLoader.apply(name + EXTENSION));

				// Profiles take precedence
				for (String profile : this.getAdditionalProfiles(sourceContext)) {
					properties.putAll(fileLoader.apply(name + "-" + profile + EXTENSION));
				}
				return properties;
			};

			// Various property sources
			Function<String, Properties> classPathSource = (name) -> {
				return profilesLoader.apply(name, (fileName) -> configLoader.apply(fileName, false));
			};
			Function<String, Properties> contextSource = (name) -> this.overrideProperties;
			Function<String, Properties> environmentSource = (name) -> {
				Properties properties = new Properties();
				properties.putAll(System.getenv());
				return propertiesFilter.apply(properties, OfficeFloor.class.getSimpleName().toUpperCase() + "." + name);
			};
			Function<String, Properties> userSource = (name) -> {
				return profilesLoader.apply(name, userFileLoader);
			};
			Function<String, Properties> systemSource = (name) -> propertiesFilter.apply(System.getProperties(), name);
			Function<String, Properties> commandLineSource = (name) -> {
				return propertiesFilter.apply(sourceContext.getProperties(), name);
			};

			// Determine if specific path
			if (!CompileUtil.isBlank(this.propertiesPath)) {
				return configLoader.apply(this.propertiesPath, true);
			}

			// Load properties (order to provide last higher precedence)
			sourceLoader.accept(classPathSource);
			sourceLoader.accept(contextSource);
			if (this.isLoadExternal) {
				sourceLoader.accept(environmentSource);
				sourceLoader.accept(userSource);
				sourceLoader.accept(systemSource);
				sourceLoader.accept(commandLineSource);
			}

			// Return the override properties
			return overrideProperties;
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
		 * Indicates to load the additional profiles.
		 * 
		 * @return <code>true</code> to load.
		 */
		public boolean isLoadAdditionalProfiles() {
			return this.isLoadAdditionalProfiles;
		}

		/**
		 * Indicates to load the override {@link Properties} configuration.
		 * 
		 * @return <code>true</code> to load.
		 */
		public boolean isLoadOverrideProperties() {
			return this.isLoadOverrideProperties;
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

	/**
	 * {@link Properties} load {@link Escalation}.
	 */
	private static class PropertiesLoadException extends RuntimeException {

		/**
		 * Default serial version.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Instantiate.
		 * 
		 * @param message Message.
		 * @param cause   Cause.
		 */
		private PropertiesLoadException(String message, Throwable cause) {
			super(message, cause);
		}
	}

}
