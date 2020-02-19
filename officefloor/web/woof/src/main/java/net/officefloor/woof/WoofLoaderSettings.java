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
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

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
	 * Default resource path to the application WoOF configuration file.
	 */
	public static final String APPLICATION_WOOF_DEFAULT_PATH = "application.woof";

	/**
	 * Default resource path to the application objects configuration file.
	 */
	public static final String APPLICATION_OBJECTS_DEFAULT_PATH = "application.objects";

	/**
	 * Default resource path to the application teams configuration file.
	 */
	public static final String APPLICATION_TEAMS_DEFAULT_PATH = "application.teams";

	/**
	 * Default resource path to the application resources configuration file.
	 */
	public static final String APPLICATION_RESOURCES_DEFAULT_PATH = "application.resources";

	/**
	 * Configuration for the {@link WoofLoader}.
	 */
	public static class WoofLoaderConfiguration {

		/**
		 * Indicates if contextual load.
		 */
		private boolean isContextualLoad = false;

		/**
		 * Resource path to the application WoOF configuration file.
		 */
		private String applicationWoofPath = APPLICATION_WOOF_DEFAULT_PATH;

		/**
		 * Resource path to the application objects configuration file.
		 */
		private String applicationObjectsPath = APPLICATION_OBJECTS_DEFAULT_PATH;

		/**
		 * Resource path to the application teams configuration file.
		 */
		private String applicationTeamsPath = APPLICATION_TEAMS_DEFAULT_PATH;

		/**
		 * Resource path to the application resources configuration file.
		 */
		private String applicationResourcesPath = APPLICATION_RESOURCES_DEFAULT_PATH;

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
		 * Indicates to load the {@link WoofExtensionService} instances.
		 */
		private boolean isLoadWoofExtensions = true;

		/**
		 * Contextually added {@link WoofExtensionService} instances.
		 */
		private final List<WoofExtensionService> contextualExtensions = new LinkedList<>();

		/**
		 * Obtains the resource path to the application WooF configuration file.
		 * 
		 * @return Resource path to the application WooF configuration file.
		 */
		public String getApplicationWoofPath() {
			return this.applicationWoofPath;
		}

		/**
		 * Obtains the resource path to the application objects configuration file.
		 * 
		 * @return Resource path to the application objects configuration file.
		 */
		public String getApplicationObjectsPath() {
			return this.applicationObjectsPath;
		}

		/**
		 * Obtains the resource path to the application teams configuration file.
		 * 
		 * @return Resource path to the application teams configuration file.
		 */
		public String getApplicationTeamsPath() {
			return this.applicationTeamsPath;
		}

		/**
		 * Obtains the resource path to the application resources configuration file.
		 * 
		 * @return Resource path to the application resources configuration file.
		 */
		public String getApplicationResourcesPath() {
			return this.applicationResourcesPath;
		}

		/**
		 * Indicates if contextual load.
		 * 
		 * @return <code>true</code> if contextual load.
		 */
		public boolean isContextualLoad() {
			return this.isContextualLoad;
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
		 * @param context {@link SourceContext}.
		 * @return <code>true</code> if a WoOF application.
		 * @throws IOException If fails to check if WoOF application.
		 */
		public boolean isWoofApplication(SourceContext context) throws IOException {

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
		 * @param context {@link SourceContext}.
		 * @return <code>true</code> if WoOF configuration available.
		 * @throws IOException If fails to check if WoOF application available.
		 */
		public boolean isApplicationWoofAvailable(SourceContext context) throws IOException {

			// Obtain configuration file
			InputStream config = context.getOptionalResource(this.applicationWoofPath);
			if (config != null) {
				config.close();
			}

			// WoOF application if configuration file
			return config != null;
		}
	}

	/**
	 * {@link ThreadLocal} for the {@link WoofLoaderConfiguration}.
	 */
	private static final ThreadLocal<WoofLoaderConfiguration> configuration = new ThreadLocal<>();

	/**
	 * Obtains the {@link WoofLoaderConfiguration}.
	 */
	public static WoofLoaderConfiguration getWoofLoaderConfiguration() {
		WoofLoaderConfiguration config = configuration.get();
		if (config == null) {
			config = new WoofLoaderConfiguration();
		}
		return config;
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
	 * {@link WoofLoaderRunnable} context.
	 */
	public static interface WoofLoaderRunnableContext {

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
		 * Specifies the resource path to the application WoOF configuration file.
		 * <p>
		 * This is useful for testing to specify different configuration files for
		 * different tests.
		 * 
		 * @param path Resource path to to the application WoOF configuration file.
		 */
		void setApplicationWoofPath(String path);

		/**
		 * <p>
		 * Specifies the resource path to the application objects configuration file.
		 * <p>
		 * This is useful for testing to specify different configuration files for
		 * different tests.
		 * 
		 * @param path Resource path to to the application objects configuration file.
		 */
		void setApplicationObjectsPath(String path);

		/**
		 * <p>
		 * Specifies the resource path to the application teams configuration file.
		 * <p>
		 * This is useful for testing to specify different configuration files for
		 * different tests.
		 * 
		 * @param path Resource path to to the application teams configuration file.
		 */
		void setApplicationTeamsPath(String path);

		/**
		 * <p>
		 * Specifies the resource path to the application resources configuration file.
		 * <p>
		 * This is useful for testing to specify different configuration files for
		 * different tests.
		 * 
		 * @param path Resource path to to the application resources configuration file.
		 */
		void setApplicationResourcesPath(String path);
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
		WoofLoaderConfiguration config = configuration.get();
		boolean isCleanUp = false;
		if (config == null) {
			// Flag is contextual load and clean up once loaded
			config = new WoofLoaderConfiguration();
			config.isContextualLoad = true;
			configuration.set(config);
			isCleanUp = true;
		}

		try {

			// Undertake runnable
			final WoofLoaderConfiguration finalConfig = config;
			return runnable.run(new WoofLoaderRunnableContext() {

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
					finalConfig.isLoadWoof = false;
				}

				@Override
				public void notLoadTeams() {
					finalConfig.isLoadTeams = false;
				}

				@Override
				public void notLoadResources() {
					finalConfig.isLoadResources = false;
				}

				@Override
				public void notLoadObjects() {
					finalConfig.isLoadObjects = false;
				}

				@Override
				public void notLoadHttpServer() {
					finalConfig.isLoadHttpServer = false;
				}

				@Override
				public void notLoadWoofExtensions() {
					finalConfig.isLoadWoofExtensions = false;
				}

				@Override
				public void extend(WoofExtensionService extension) {
					finalConfig.contextualExtensions.add(extension);
				}

				@Override
				public void setApplicationWoofPath(String path) {
					finalConfig.applicationWoofPath = path;
				}

				@Override
				public void setApplicationObjectsPath(String path) {
					finalConfig.applicationObjectsPath = path;
				}

				@Override
				public void setApplicationTeamsPath(String path) {
					finalConfig.applicationTeamsPath = path;
				}

				@Override
				public void setApplicationResourcesPath(String path) {
					finalConfig.applicationResourcesPath = path;
				}
			});

		} finally {
			// Clean up if created
			if (isCleanUp) {
				configuration.remove();
			}
		}
	}

}