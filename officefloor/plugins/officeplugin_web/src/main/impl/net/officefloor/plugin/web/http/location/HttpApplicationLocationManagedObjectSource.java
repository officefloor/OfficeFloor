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
package net.officefloor.plugin.web.http.location;

import java.net.InetAddress;
import java.net.UnknownHostException;

import net.officefloor.compile.impl.properties.PropertiesUtil;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * {@link ManagedObjectSource} for the {@link HttpApplicationLocation}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpApplicationLocationManagedObjectSource
		extends AbstractManagedObjectSource<HttpApplicationLocationManagedObjectSource.Dependencies, None> {

	/**
	 * Copies the {@link Property} instances.
	 * 
	 * @param source
	 *            {@link SourceProperties}.
	 * @param target
	 *            {@link PropertyConfigurable}.
	 */
	public static void copyProperties(SourceProperties source, PropertyConfigurable target) {
		PropertiesUtil.copyProperties(source, target, PROPERTY_DOMAIN, PROPERTY_HTTP_PORT, PROPERTY_HTTPS_PORT,
				PROPERTY_CONTEXT_PATH, PROPERTY_CLUSTER_HOST, PROPERTY_CLUSTER_HTTP_PORT, PROPERTY_CLUSTER_HTTPS_PORT);
	}

	/**
	 * <p>
	 * Obtains the default host name.
	 * <p>
	 * Configuration may override this.
	 * 
	 * @return Default Host name.
	 */
	public static String getDefaultHostName() {
		try {
			return InetAddress.getLocalHost().getCanonicalHostName();
		} catch (UnknownHostException ex) {
			return "localhost";
		}
	}

	/**
	 * Dependency keys.
	 */
	public static enum Dependencies {
		SERVER_HTTP_CONNECTION
	}

	/**
	 * Name of property to obtain the domain.
	 */
	public static final String PROPERTY_DOMAIN = "http.host";

	/**
	 * Name of property to obtain the HTTP port.
	 */
	public static final String PROPERTY_HTTP_PORT = "http.port";

	/**
	 * Default HTTP port.
	 */
	public static final int DEFAULT_HTTP_PORT = 7878;

	/**
	 * Name of property to obtain the HTTPS port.
	 */
	public static final String PROPERTY_HTTPS_PORT = "https.port";

	/**
	 * Default HTTPS port.
	 */
	public static final int DEFAULT_HTTPS_PORT = 7979;

	/**
	 * Name of property to obtain the HTTP context path.
	 */
	public static final String PROPERTY_CONTEXT_PATH = "http.context.path";

	/**
	 * Name of property to obtain the fully qualified host name within the
	 * cluster.
	 */
	public static final String PROPERTY_CLUSTER_HOST = "cluster.http.host";

	/**
	 * Name of property to obtain the cluster HTTP port.
	 */
	public static final String PROPERTY_CLUSTER_HTTP_PORT = "cluster.http.port";

	/**
	 * Name of property to obtain the cluster HTTPS port.
	 */
	public static final String PROPERTY_CLUSTER_HTTPS_PORT = "cluster.https.port";

	/**
	 * Domain.
	 */
	private String domain;

	/**
	 * HTTP port.
	 */
	private int httpPort;

	/**
	 * HTTPS port.
	 */
	private int httpsPort;

	/**
	 * Context path.
	 */
	private String contextPath;

	/**
	 * Cluster host name.
	 */
	private String clusterHostName;

	/**
	 * Cluster HTTP port.
	 */
	private int clusterHttpPort;

	/**
	 * Cluster HTTPS port.
	 */
	private int clusterHttpsPort;

	/*
	 * ====================== ManagedObjectSource ======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// All properties optional with defaults
	}

	@Override
	protected void loadMetaData(MetaDataContext<Dependencies, None> context) throws Exception {
		ManagedObjectSourceContext<None> mosContext = context.getManagedObjectSourceContext();

		// Obtain the configuration
		this.domain = mosContext.getProperty(PROPERTY_DOMAIN, null);
		this.httpPort = Integer.parseInt(mosContext.getProperty(PROPERTY_HTTP_PORT, String.valueOf(DEFAULT_HTTP_PORT)));
		this.httpsPort = Integer
				.parseInt(mosContext.getProperty(PROPERTY_HTTPS_PORT, String.valueOf(DEFAULT_HTTPS_PORT)));
		this.contextPath = mosContext.getProperty(PROPERTY_CONTEXT_PATH, null);
		this.clusterHostName = mosContext.getProperty(PROPERTY_CLUSTER_HOST, null);
		this.clusterHttpPort = Integer
				.parseInt(mosContext.getProperty(PROPERTY_CLUSTER_HTTP_PORT, String.valueOf(this.httpPort)));
		this.clusterHttpsPort = Integer
				.parseInt(mosContext.getProperty(PROPERTY_CLUSTER_HTTPS_PORT, String.valueOf(this.httpsPort)));

		// Ensure have cluster host
		if (this.clusterHostName == null) {
			this.clusterHostName = getDefaultHostName();
		}

		// Ensure have domain
		if (this.domain == null) {
			this.domain = this.clusterHostName;
		}

		// Ensure have canonical context path
		if (this.contextPath != null) {

			// Ensure have leading slash for context path
			this.contextPath = this.contextPath.trim();
			if (!(this.contextPath.startsWith("/"))) {
				this.contextPath = "/" + this.contextPath;
			}

			// Transform to canonical path
			this.contextPath = HttpApplicationLocationMangedObject.transformToCanonicalPath(this.contextPath);

			// No context if root path
			if ("/".equals(this.contextPath)) {
				this.contextPath = null;
			}
		}

		// Provide the meta-data
		context.setObjectClass(HttpApplicationLocation.class);
		context.setManagedObjectClass(HttpApplicationLocationMangedObject.class);
		context.addDependency(Dependencies.SERVER_HTTP_CONNECTION, ServerHttpConnection.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new HttpApplicationLocationMangedObject(this.domain, this.httpPort, this.httpsPort, this.contextPath,
				this.clusterHostName, this.clusterHttpPort, this.clusterHttpsPort);
	}

}