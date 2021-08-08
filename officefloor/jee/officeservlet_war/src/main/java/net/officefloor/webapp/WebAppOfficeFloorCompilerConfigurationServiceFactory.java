/*-
 * #%L
 * OfficeFloor integration of WAR
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

package net.officefloor.webapp;

import java.io.File;
import java.net.URL;

import net.officefloor.compile.OfficeFloorCompilerConfigurer;
import net.officefloor.compile.OfficeFloorCompilerConfigurerContext;
import net.officefloor.compile.OfficeFloorCompilerConfigurerServiceFactory;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.servlet.archive.ArchiveAwareClassLoaderFactory;

/**
 * Loads the {@link ClassLoader} for WAR.
 * 
 * @author Daniel Sagenschneider
 */
public class WebAppOfficeFloorCompilerConfigurationServiceFactory
		implements OfficeFloorCompilerConfigurerServiceFactory {

	/*
	 * ========== OfficeFloorCompilerConfigurationServiceFactory ==========
	 */

	@Override
	public OfficeFloorCompilerConfigurer createService(ServiceContext context) throws Throwable {

		// Obtain the location of the web app (WAR)
		String webAppPath = context.getProperty(OfficeFloorWar.PROPERTY_WAR_PATH);

		// Create and return the configuration
		ClassLoader classLoader = context.getClassLoader();
		return new WebAppOfficeFlooorConfigurationService(webAppPath, classLoader);
	}

	/**
	 * {@link OfficeFloorCompilerConfigurer} for the web app (WAR).
	 */
	private static class WebAppOfficeFlooorConfigurationService implements OfficeFloorCompilerConfigurer {

		/**
		 * Path to the web app (WAR).
		 */
		private final String webAppPath;

		/**
		 * {@link ClassLoader}.
		 */
		private final ClassLoader classLoader;

		/**
		 * Instantiate.
		 * 
		 * @param webAppPath  Path to the web app (WAR).
		 * @param classLoader {@link ClassLoader}.
		 */
		private WebAppOfficeFlooorConfigurationService(String webAppPath, ClassLoader classLoader) {
			this.webAppPath = webAppPath;
			this.classLoader = classLoader;
		}

		/*
		 * ============== OfficeFloorCompilerConfigurationService =============
		 */

		@Override
		public void configureOfficeFloorCompiler(OfficeFloorCompilerConfigurerContext context) throws Exception {

			// Create class loader for the web application (WAR)
			URL webAppUrl = new File(this.webAppPath).toURI().toURL();
			ClassLoader compileClassLoader = new ArchiveAwareClassLoaderFactory(this.classLoader)
					.createClassLoader(webAppUrl, "WEB-INF/classes/", "WEB-INF/lib/");

			// Configure the class loader
			context.setClassLoader(compileClassLoader);
		}
	}

}
