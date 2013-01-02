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

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.source.SourceProperties;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.location.HttpApplicationLocationManagedObjectSource.Dependencies;

/**
 * Tests the {@link HttpApplicationLocationManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpApplicationLocationManagedObjectSourceTest extends
		OfficeFrameTestCase {

	/**
	 * {@link ServerHttpConnection}.
	 */
	private final ServerHttpConnection connection = this
			.createMock(ServerHttpConnection.class);

	/**
	 * Host name.
	 */
	private String hostName;

	@Override
	protected void setUp() throws Exception {
		// Obtain the host name
		this.hostName = InetAddress.getLocalHost().getCanonicalHostName();
	}

	/**
	 * Ensure all properties are copied.
	 */
	public void testCopyProperties() {

		final SourceProperties source = this.createMock(SourceProperties.class);
		final PropertyConfigurable target = this
				.createMock(PropertyConfigurable.class);

		// Record copying the properties
		this.recordCopyProperty(source, target, "http.host",
				"test.officefloor.net");
		this.recordCopyProperty(source, target, "http.port", "7878");
		this.recordCopyProperty(source, target, "https.port", "7979");
		this.recordCopyProperty(source, target, "http.context.path", "/context");
		this.recordCopyProperty(source, target, "cluster.http.host",
				"node.officefloor.net");
		this.recordCopyProperty(source, target, "cluster.http.port", "2323");
		this.recordCopyProperty(source, target, "cluster.https.port", "2424");

		// Copy the properties
		this.replayMockObjects();
		HttpApplicationLocationManagedObjectSource.copyProperties(source,
				target);
		this.verifyMockObjects();
	}

	/**
	 * Records copying the {@link Property}.
	 * 
	 * @param source
	 *            {@link SourceProperties}.
	 * @param target
	 *            {@link PropertyConfigurable}.
	 * @param name
	 *            Name of {@link Property}.
	 * @param value
	 *            Value of {@link Property}.
	 */
	private void recordCopyProperty(SourceProperties source,
			PropertyConfigurable target, String name, String value) {
		this.recordReturn(source, source.getProperty(name, null), value);
		target.addProperty(name, value);
	}

	/**
	 * Validate specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil
				.validateSpecification(HttpApplicationLocationManagedObjectSource.class);
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() {

		// Create expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil
				.createManagedObjectTypeBuilder();
		type.setObjectClass(HttpApplicationLocation.class);
		type.addDependency(Dependencies.SERVER_HTTP_CONNECTION,
				ServerHttpConnection.class, null);

		// Validate type
		ManagedObjectLoaderUtil.validateManagedObjectType(type,
				HttpApplicationLocationManagedObjectSource.class);
	}

	/**
	 * Ensure can load the {@link HttpApplicationLocation} from defaults.
	 */
	public void testLoadFromDefaults() {

		// Load with defaults
		HttpApplicationLocation location = this.loadHttpApplicationLocation();

		// Validate location
		assertLocation(this.hostName, 7878, 7979, null, this.hostName, 7878,
				7979, location);
	}

	/**
	 * Ensure can load the {@link HttpApplicationLocation} configured as a
	 * single instance (not within a cluster).
	 */
	public void testLoadForSingleInstance() {

		// Load for single instance configuration
		HttpApplicationLocation location = this.loadHttpApplicationLocation(
				"http.host", "application.officefloor.net", "http.port",
				"8888", "https.port", "8989", "http.context.path", "/test");

		// Validate location
		assertLocation("application.officefloor.net", 8888, 8989, "/test",
				this.hostName, 8888, 8989, location);
	}

	/**
	 * Ensure can load the {@link HttpApplicationLocation} configured within a
	 * cluster.
	 */
	public void testLoadWithinCluster() {

		// Load for single instance configuration
		HttpApplicationLocation location = this.loadHttpApplicationLocation(
				"http.host", "application.officefloor.net", "http.port",
				"8888", "https.port", "8989", "http.context.path", "/test",
				"cluster.http.host", "node.officefloor.net",
				"cluster.http.port", "2222", "cluster.https.port", "2323");

		// Validate location
		assertLocation("application.officefloor.net", 8888, 8989, "/test",
				"node.officefloor.net", 2222, 2323, location);
	}

	/**
	 * Ensure always use canonical context path.
	 */
	public void testCanonicalContextPath() {

		// Ensure prepend leading slash (/)
		assertEquals(
				"/context",
				this.loadHttpApplicationLocation("http.context.path", "context")
						.getContextPath());

		// Ensure transform to canonical path
		assertEquals(
				"/context",
				this.loadHttpApplicationLocation("http.context.path",
						"//./something/../context/").getContextPath());

		// Ensure root context path results in no path
		assertNull(this.loadHttpApplicationLocation("http.context.path", " / ")
				.getContextPath());
	}

	/**
	 * Loads the {@link HttpApplicationLocation}.
	 * 
	 * @param parameterNameValues
	 *            Parameter name value pairs.
	 * @return Loaded {@link HttpApplicationLocation}.
	 */
	private HttpApplicationLocation loadHttpApplicationLocation(
			String... parameterNameValues) {
		try {

			// Load the managed object source
			ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
			for (int i = 0; i < parameterNameValues.length; i += 2) {
				String name = parameterNameValues[i];
				String value = parameterNameValues[i + 1];
				loader.addProperty(name, value);
			}
			HttpApplicationLocationManagedObjectSource source = loader
					.loadManagedObjectSource(HttpApplicationLocationManagedObjectSource.class);

			// Source the managed object
			ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
			user.mapDependency(Dependencies.SERVER_HTTP_CONNECTION,
					this.connection);
			ManagedObject managedObject = user.sourceManagedObject(source);
			assertTrue("Should be co-ordinating",
					(managedObject instanceof CoordinatingManagedObject));

			// Obtain the object
			Object object = managedObject.getObject();
			assertTrue("Should be HttpApplicationLocation",
					(object instanceof HttpApplicationLocation));

			// Return the HTTP application location
			return (HttpApplicationLocation) object;

		} catch (Throwable ex) {
			throw fail(ex);
		}
	}

	/**
	 * Asserts the {@link HttpApplicationLocation} is configured correctly.
	 * 
	 * @param domain
	 *            Expected domain.
	 * @param httpPort
	 *            Expected HTTP port.
	 * @param httpsPort
	 *            Expected HTTPS port.
	 * @param contextPath
	 *            Expected context path.
	 * @param hostName
	 *            Expected host name.
	 * @param clusterHttpPort
	 *            Expected cluster HTTP port.
	 * @param clusterHttpsPort
	 *            Expected cluster HTTPS port.
	 * @param location
	 *            {@link HttpApplicationLocation} to test.
	 */
	private static void assertLocation(String domain, int httpPort,
			int httpsPort, String contextPath, String hostName,
			int clusterHttpPort, int clusterHttpsPort,
			HttpApplicationLocation location) {

		// Validate location
		assertEquals("Incorrect domain", domain, location.getDomain());
		assertEquals("Incorrect HTTP port", httpPort, location.getHttpPort());
		assertEquals("Incorrect HTTPS port", httpsPort, location.getHttpsPort());
		if (contextPath == null) {
			assertNull("Should not have context path",
					location.getContextPath());
		} else {
			assertEquals("Incorrect context path", contextPath,
					location.getContextPath());
		}
		assertEquals("Incorrect cluster host", hostName,
				location.getClusterHostName());
		assertEquals("Incorrect cluster HTTP port", clusterHttpPort,
				location.getClusterHttpPort());
		assertEquals("Incorrect cluster HTTPS port", clusterHttpsPort,
				location.getClusterHttpsPort());
	}

}