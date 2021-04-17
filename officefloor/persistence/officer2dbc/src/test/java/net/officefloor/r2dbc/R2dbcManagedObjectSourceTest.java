/*-
 * #%L
 * r2dbc
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

package net.officefloor.r2dbc;

import java.lang.reflect.Proxy;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.ValidationDepth;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.plugin.section.clazz.Next;
import reactor.core.publisher.Mono;

/**
 * Tests the {@link R2dbcManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class R2dbcManagedObjectSourceTest extends AbstractDatabaseTestCase {

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil.validateSpecification(R2dbcManagedObjectSource.class,
				ConnectionFactoryOptions.DRIVER.name(), "Driver", ConnectionFactoryOptions.PROTOCOL.name(), "Protocol",
				ConnectionFactoryOptions.USER.name(), "User", ConnectionFactoryOptions.PASSWORD.name(), "Password");
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() {
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(R2dbcSource.class);
		ManagedObjectLoaderUtil.validateManagedObjectType(type, R2dbcManagedObjectSource.class,
				ConnectionFactoryOptions.DRIVER.name(), "h2", ConnectionFactoryOptions.PROTOCOL.name(), "mem",
				ConnectionFactoryOptions.DATABASE.name(), "test", ConnectionFactoryOptions.USER.name(), "SA",
				ConnectionFactoryOptions.PASSWORD.name(), "Password");
	}

	/**
	 * Ensure can retrieve data.
	 */
	public void testRetrieveData() throws Throwable {
		this.doRetrieveDataTest(false);
	}

	/**
	 * Ensure can retrieve data with pooling.
	 */
	public void testRetrieveDataPooled() throws Throwable {
		this.doRetrieveDataTest(true);
	}

	/**
	 * Undertakes the retrieve data test.
	 */
	private void doRetrieveDataTest(boolean isPool) throws Throwable {

		// Create to extract information to test
		R2dbcManagedObjectSource r2dbcMos = new R2dbcManagedObjectSource();

		// Setup data
		this.setupDatabase();

		// Ensure can retrieve data
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.office((context) -> {
			context.addSection("source", RetrieveDataSection.class);
			this.configureR2dbcSource(context.getOfficeArchitect(), r2dbcMos, isPool);
		});
		ConnectionPool pool;
		try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {

			// Capture the pool
			pool = r2dbcMos.getConnectionPool();

			// Ensure retrieve the message
			RetrieveDataSection.pool = pool;
			RetrieveDataSection.message = null;
			RetrieveDataSection.connection = null;
			CompileOfficeFloor.invokeProcess(officeFloor, "source.retrieve", null);
			assertEquals("Incorrect message", "TEST", RetrieveDataSection.message);
			assertTrue("Should be provided a proxy", Proxy.isProxyClass(RetrieveDataSection.connection.getClass()));
			assertSame("Should get same connection for next", RetrieveDataSection.connection,
					RetrieveDataSection.nextConnection);

			if (isPool) {
				// Validate pooled
				assertNotNull("Should have pool", pool);
				assertFalse("OfficeFloor open, so pool should be active", pool.isDisposed());
				assertValidConnection("Should keep pooled connection open", true, RetrieveDataSection.connection);

			} else {
				// Validate creating new connection each time
				assertNull("Should not have pool", pool);
				assertValidConnection("Should close connection", false, RetrieveDataSection.connection);
			}
		}

		// Ensure pool is closed
		if (isPool) {
			assertTrue("OfficeFloor closed, so pool should be disposed", pool.isDisposed());
		}

		// Connection should now be closed (regardless of pooling)
		assertValidConnection("Close all connections", false, RetrieveDataSection.connection);
	}

	public static class RetrieveDataSection {

		private static ConnectionPool pool;

		private static Connection connection = null;

		private static String message = null;

		private static Connection nextConnection = null;

		@Next("next")
		public void retrieve(R2dbcSource source) {
			assertActiveConnections("Should only initiate when obtain connection", 0, pool);

			// Obtain the connection
			connection = source.getConnection().block(TIMEOUT);
			assertSame("Should be same connection", connection, source.getConnection().block(TIMEOUT));

			// Ensure can not close connection
			source.getConnection().flatMap(c -> Mono.from(c.close())).block(TIMEOUT);
			assertValidConnection("Should not close connection", true, connection);

			// Obtain the message
			message = source.getConnection()
					.flatMap(c -> Mono.from(c.createStatement("SELECT message FROM test").execute()))
					.map(r -> r.map((row, metaData) -> row.get("message", String.class))).flatMap(msg -> Mono.from(msg))
					.block(TIMEOUT);

			// Should be single connection
			assertActiveConnections("Same connection re-used", 1, pool);
		}

		public void next(R2dbcSource source) {
			nextConnection = source.getConnection().block(TIMEOUT);
			assertActiveConnections("Should re-use connection", 1, pool);
		}
	}

	private static void assertActiveConnections(String message, int activeCount, ConnectionPool pool) {
		if (pool != null) {
			assertEquals(message, activeCount, pool.getMetrics().get().acquiredSize());
		}
	}

	private static void assertValidConnection(String message, boolean isValid, Connection connection) {
		assertEquals(message, Boolean.valueOf(isValid),
				Mono.from(connection.validate(ValidationDepth.REMOTE)).block(TIMEOUT));
	}

}
