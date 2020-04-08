/*-
 * #%L
 * OfficeFloor integration of WAR
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

package net.officefloor.webapp;

import java.io.File;
import java.net.URL;

import net.officefloor.compile.OfficeFloorCompilerConfigurer;
import net.officefloor.compile.OfficeFloorCompilerConfigurerContext;
import net.officefloor.compile.OfficeFloorCompilerConfigurerServiceFactory;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.web.war.WarAwareClassLoaderFactory;

/**
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
		 * Path to the web app (WAR). May be <code>null</code>.
		 */
		private final String webAppPath;

		/**
		 * {@link ClassLoader}.
		 */
		private final ClassLoader classLoader;

		/**
		 * Instantiate.
		 * 
		 * @param webAppPath  Path to the web app (WAR). May be <code>null</code>.
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
			ClassLoader compileClassLoader = new WarAwareClassLoaderFactory(this.classLoader)
					.createClassLoader(new URL[] { webAppUrl });

			// Configure the class loader
			context.setClassLoader(compileClassLoader);
		}
	}

}
