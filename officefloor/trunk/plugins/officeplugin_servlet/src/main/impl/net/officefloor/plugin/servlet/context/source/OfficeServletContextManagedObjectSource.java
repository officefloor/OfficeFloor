/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
package net.officefloor.plugin.servlet.context.source;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.servlet.context.OfficeServletContext;
import net.officefloor.plugin.servlet.context.OfficeServletContextImpl;
import net.officefloor.plugin.servlet.filter.configuration.FilterMapping;
import net.officefloor.plugin.servlet.log.Logger;
import net.officefloor.plugin.servlet.log.StdoutLogger;
import net.officefloor.plugin.servlet.mapping.MappingType;
import net.officefloor.plugin.servlet.resource.FileSystemResourceLocator;
import net.officefloor.plugin.servlet.resource.ResourceLocator;

/**
 * {@link ManagedObjectSource} for the {@link OfficeServletContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeServletContextManagedObjectSource extends
		AbstractManagedObjectSource<None, None> implements ManagedObject {

	/**
	 * Property name to specify the server name.
	 */
	public static final String PROPERTY_SERVER_NAME = "server.name";

	/**
	 * Property name to specify the server port.
	 */
	public static final String PROPERTY_SERVER_PORT = "server.port";

	/**
	 * Property name to specify the {@link ServletContext} name.
	 */
	public static final String PROPERTY_SERVLET_CONTEXT_NAME = "servlet.context.name";

	/**
	 * Property prefix to an init parameter.
	 */
	public static final String PROPERTY_PREFIX_INIT_PARAMETER = "init.parameter.";

	/**
	 * Property prefix to file extension to MIME type mapping.
	 */
	public static final String PROPERTY_PREFIX_FILE_EXTENSION_TO_MIME_TYPE = "file.ext.to.mime.type.";

	/**
	 * Property name to specify the context path.
	 */
	public static final String PROPERTY_CONTEXT_PATH = "context.path";

	/**
	 * Property name to specify the resource path root.
	 */
	public static final String PROPERTY_RESOURCE_PATH_ROOT = "resource.path.root";

	/**
	 * Prefix on property name to obtain the {@link Filter} name and its
	 * corresponding {@link Filter} {@link Class} name.
	 */
	public static final String PROPERTY_FILTER_INSTANCE_NAME_PREFIX = "filter.instance.name.";

	/**
	 * Prefix on property name to obtain Init Parameter name and its
	 * corresponding value.
	 */
	public static final String PROPERTY_FILTER_INSTANCE_INIT_PREFIX = "filter.instance.init.";

	/**
	 * Prefix on property name to obtain the {@link FilterMapping} index and its
	 * corresponding {@link Filter} name.
	 */
	public static final String PROPERTY_FILTER_MAPPING_INDEX_PREFIX = "filter.mapping.index.";

	/**
	 * Prefix on property name to obtain the {@link FilterMapping} URL pattern.
	 */
	public static final String PROPERTY_FILTER_MAPPING_URL_PREFIX = "filter.mapping.url.";

	/**
	 * Prefix on property name to obtain the {@link FilterMapping}
	 * {@link Servlet} name.
	 */
	public static final String PROPERTY_FILTER_MAPPING_SERVLET_PREFIX = "filter.mapping.servlet.";

	/**
	 * Prefix on property name to obtain the {@link FilterMapping}
	 * {@link MappingType}.
	 */
	public static final String PROPERTY_FILTER_MAPPING_TYPE_PREFIX = "filter.mapping.type.";

	/**
	 * {@link OfficeServletContext}.
	 */
	private OfficeServletContext officeServletContext;

	/**
	 * Extracts the mapping from the {@link ManagedObjectSourceContext}.
	 * 
	 * @param prefix
	 *            Prefix of properties with remaining being the key.
	 * @param context
	 *            {@link ManagedObjectSourceContext}.
	 * @param mappings
	 *            Mappings to be loaded..
	 * @return Mapping.
	 */
	public Map<String, String> loadMappings(String prefix,
			ManagedObjectSourceContext<None> context,
			Map<String, String> mappings) {

		// Load the mappings
		Properties properties = context.getProperties();
		for (String name : properties.stringPropertyNames()) {
			if (name.startsWith(prefix)) {
				// Obtain key of property
				String key = name.substring(prefix.length());

				// Add the mapping
				mappings.put(key, properties.getProperty(name));
			}
		}

		// Return the mappings
		return mappings;
	}

	/*
	 * ===================== ManagedObjectSource =======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_SERVER_NAME, "Server Name");
		context.addProperty(PROPERTY_SERVLET_CONTEXT_NAME,
				"Servlet Context Name");
		context.addProperty(PROPERTY_CONTEXT_PATH, "Context Path");
		context.addProperty(PROPERTY_RESOURCE_PATH_ROOT, "Resource Path Root");
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context)
			throws Exception {
		ManagedObjectSourceContext<None> mosContext = context
				.getManagedObjectSourceContext();

		// Obtain configuration for the servlet context
		String serverName = mosContext.getProperty(PROPERTY_SERVER_NAME);
		int serverPort = Integer.parseInt(mosContext.getProperty(
				PROPERTY_SERVER_PORT, "80"));
		String servletContextName = mosContext
				.getProperty(PROPERTY_SERVLET_CONTEXT_NAME);
		String contextPath = mosContext.getProperty(PROPERTY_CONTEXT_PATH);

		// Create the init parameters
		Map<String, String> initParameters = this.loadMappings(
				PROPERTY_PREFIX_INIT_PARAMETER, mosContext,
				new HashMap<String, String>());

		// Create the file extension to MIME type mappings (including defaults)
		Map<String, String> fileExtensionToMimeType = new HashMap<String, String>();
		fileExtensionToMimeType.put("css", "text/css");
		fileExtensionToMimeType.put("gif", "image/gif");
		fileExtensionToMimeType.put("htm", "text/html");
		fileExtensionToMimeType.put("html", "text/html");
		fileExtensionToMimeType.put("ico", "image/x-icon");
		fileExtensionToMimeType.put("jpeg", "image/jpeg");
		fileExtensionToMimeType.put("jpg", "image/jpeg");
		fileExtensionToMimeType.put("js", "application/x-javascript");
		fileExtensionToMimeType.put("log", "text/plain");
		fileExtensionToMimeType.put("pdf", "application/pdf");
		fileExtensionToMimeType.put("png", "image/png");
		fileExtensionToMimeType.put("txt", "text/plain");
		fileExtensionToMimeType.put("xml", "text/xml");
		this.loadMappings(PROPERTY_PREFIX_FILE_EXTENSION_TO_MIME_TYPE,
				mosContext, fileExtensionToMimeType);

		// Create the resource locator
		File resourcePathRoot = new File(mosContext
				.getProperty(PROPERTY_RESOURCE_PATH_ROOT));
		if (!resourcePathRoot.isDirectory()) {
			throw new FileNotFoundException(
					"Resource path root is not a directory: "
							+ resourcePathRoot.getPath());
		}
		ResourceLocator resourceLocator = new FileSystemResourceLocator(
				resourcePathRoot);

		// Create the logger
		Logger logger = new StdoutLogger();

		// Obtain the filter configurations
		Properties properties = mosContext.getProperties();
		ClassLoader classLoader = mosContext.getClassLoader();

		// Create the office servlet context instance
		this.officeServletContext = new OfficeServletContextImpl(serverName,
				serverPort, servletContextName, contextPath, initParameters,
				fileExtensionToMimeType, resourceLocator, logger, properties,
				classLoader);

		// Specify the meta-data
		context.setManagedObjectClass(this.getClass());
		context.setObjectClass(OfficeServletContext.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return this;
	}

	/*
	 * ======================== ManagedObject =============================
	 */

	@Override
	public Object getObject() throws Throwable {
		return this.officeServletContext;
	}

}