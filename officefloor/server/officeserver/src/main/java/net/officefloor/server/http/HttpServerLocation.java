/*-
 * #%L
 * HTTP Server
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

package net.officefloor.server.http;

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.source.SourceContext;

/**
 * <p>
 * Location of the HTTP server.
 * <p>
 * Note the {@link Property} configuration is provided here to aid identifying
 * how to configure common settings for the {@link HttpServer}. Configuration is
 * expected to follow:
 * <ol>
 * <li>checking the {@link SourceContext}</li>
 * <li>checking {@link System#getProperty(String)}</li>
 * <li>using defaults</li>
 * </ol>
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpServerLocation {

	/**
	 * Name of {@link Property} for the domain.
	 */
	static final String PROPERTY_DOMAIN = "server.domain";

	/**
	 * Name of {@link Property} for the HTTP port.
	 */
	static final String PROPERTY_HTTP_PORT = "http.port";

	/**
	 * Name of {@link Property} for the HTTPS port.
	 */
	static final String PROPERTY_HTTPS_PORT = "https.port";

	/**
	 * Name of {@link Property} for the cluster host name.
	 */
	static final String PROPERTY_CLUSTER_HOST_NAME = "cluster.host";

	/**
	 * Name of {@link Property} for the cluster HTTP port.
	 */
	static final String PROPERTY_CLUSTER_HTTP_PORT = "cluster.http.port";

	/**
	 * Name of {@link Property} for the cluster HTTPS port.
	 */
	static final String PROPERTY_CLUSTER_HTTPS_PORT = "cluster.https.port";

	/**
	 * <p>
	 * Obtains the domain for the server.
	 * <p>
	 * This is as the client will see the server.
	 * 
	 * @return Domain for the server.
	 */
	String getDomain();

	/**
	 * <p>
	 * Obtains the HTTP port.
	 * <p>
	 * This is as the client will see the server.
	 * 
	 * @return HTTP port.
	 */
	int getHttpPort();

	/**
	 * <p>
	 * Obtains the HTTPS port.
	 * <p>
	 * This is as the client will see the server.
	 * 
	 * @return HTTPS port.
	 */
	int getHttpsPort();

	/**
	 * Obtains the name of the host for the server within the cluster. This name
	 * should be understood by all nodes within the cluster.
	 * 
	 * @return Name of the host within the cluster.
	 */
	String getClusterHostName();

	/**
	 * The cluster may be behind a load balancer and the server may be running
	 * on a different port than expected by the client.
	 * 
	 * @return Actual port on the cluster host the server is running on.
	 */
	int getClusterHttpPort();

	/**
	 * The cluster may be behind a load balancer and the server may be running
	 * on a different port than expected by the client.
	 * 
	 * @return Actual secure port on the cluster host the server is running on.
	 */
	int getClusterHttpsPort();

	/**
	 * Creates the client URL to call the {@link HttpServer}.
	 * 
	 * @param isSecure
	 *            If secure URL.
	 * @param path
	 *            Path including query string and fragment.
	 * @return URL for the client to call on the {@link HttpServer}.
	 */
	String createClientUrl(boolean isSecure, String path);

}
