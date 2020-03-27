package net.officefloor.webapp;

import java.io.File;
import java.net.URL;

import net.officefloor.compile.OfficeFloorCompilerConfigurer;
import net.officefloor.compile.OfficeFloorCompilerConfigurerContext;
import net.officefloor.compile.OfficeFloorCompilerConfigurerServiceFactory;
import net.officefloor.compile.impl.ApplicationOfficeFloorSource;
import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.servlet.supply.ServletWoofExtensionService;
import net.officefloor.web.war.WarAwareClassLoaderFactory;

/**
 * @author Daniel Sagenschneider
 */
public class WebAppOfficeFloorCompilerConfigurationServiceFactory
		implements OfficeFloorCompilerConfigurerServiceFactory {

	/**
	 * {@link Property} name for the web application (WAR) path.
	 */
	public static final String PROPERTY_WEB_APP_PATH = ApplicationOfficeFloorSource.OFFICE_NAME + "."
			+ ServletWoofExtensionService.PROPERTY_WEB_APP_PATH;

	/*
	 * ========== OfficeFloorCompilerConfigurationServiceFactory ==========
	 */

	@Override
	public OfficeFloorCompilerConfigurer createService(ServiceContext context) throws Throwable {

		// Obtain the location of the web app (WAR)
		String webAppPath = context.getProperty(PROPERTY_WEB_APP_PATH);

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