/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.servlet.context.OfficeServletContext;
import net.officefloor.plugin.servlet.context.OfficeServletContextImpl;
import net.officefloor.plugin.servlet.filter.configuration.FilterMapping;
import net.officefloor.plugin.servlet.host.ServletServer;
import net.officefloor.plugin.servlet.mapping.MappingType;

/**
 * {@link ManagedObjectSource} for the {@link OfficeServletContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeServletContextManagedObjectSource
		extends
		AbstractManagedObjectSource<OfficeServletContextManagedObjectSource.DependencyKeys, None>
		implements
		CoordinatingManagedObject<OfficeServletContextManagedObjectSource.DependencyKeys> {

	/**
	 * Dependency keys for the {@link OfficeServletContextManagedObjectSource}.
	 */
	public static enum DependencyKeys {
		SERVLET_SERVER
	}

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
	private OfficeServletContextImpl officeServletContext;

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
		context.addProperty(PROPERTY_SERVLET_CONTEXT_NAME,
				"Servlet Context Name");
	}

	@Override
	protected void loadMetaData(MetaDataContext<DependencyKeys, None> context)
			throws Exception {
		ManagedObjectSourceContext<None> mosContext = context
				.getManagedObjectSourceContext();

		// Obtain configuration for the servlet context
		String servletContextName = mosContext
				.getProperty(PROPERTY_SERVLET_CONTEXT_NAME);

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

		// Obtain the filter configurations
		Properties properties = mosContext.getProperties();
		ClassLoader classLoader = mosContext.getClassLoader();

		// Create the office servlet context instance
		this.officeServletContext = new OfficeServletContextImpl(
				servletContextName, initParameters, fileExtensionToMimeType,
				properties, classLoader);

		// Specify the meta-data
		context.setObjectClass(OfficeServletContext.class);
		context.setManagedObjectClass(this.getClass());
		context.addDependency(DependencyKeys.SERVLET_SERVER,
				ServletServer.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return this;
	}

	/*
	 * ======================== ManagedObject =============================
	 */

	@Override
	public void loadObjects(ObjectRegistry<DependencyKeys> registry)
			throws Throwable {
		// Obtain the servlet server
		ServletServer servletServer = (ServletServer) registry
				.getObject(DependencyKeys.SERVLET_SERVER);

		// Ensure office servlet context is initialised
		this.officeServletContext.init(servletServer);
	}

	@Override
	public Object getObject() throws Throwable {
		return this.officeServletContext;
	}

}