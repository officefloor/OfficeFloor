/*-
 * #%L
 * r2dbc
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

package net.officefloor.r2dbc;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;

import org.junit.jupiter.api.Test;

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
	@Test
	public void specification() {
		ManagedObjectLoaderUtil.validateSpecification(R2dbcManagedObjectSource.class,
				ConnectionFactoryOptions.DRIVER.name(), "Driver", ConnectionFactoryOptions.PROTOCOL.name(), "Protocol",
				ConnectionFactoryOptions.USER.name(), "User", ConnectionFactoryOptions.PASSWORD.name(), "Password");
	}

	/**
	 * Ensure correct type.
	 */
	@Test
	public void type() {
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
	@Test
	public void retrieveData() throws Throwable {
		this.doRetrieveDataTest(false);
	}

	/**
	 * Ensure can retrieve data with pooling.
	 */
	@Test
	public void retrieveDataPooled() throws Throwable {
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
			assertEquals("TEST", RetrieveDataSection.message, "Incorrect message");
			assertTrue(Proxy.isProxyClass(RetrieveDataSection.connection.getClass()), "Should be provided a proxy");
			assertSame(RetrieveDataSection.connection, RetrieveDataSection.nextConnection,
					"Should get same connection for next");

			if (isPool) {
				// Validate pooled
				assertNotNull(pool, "Should have pool");
				assertFalse(pool.isDisposed(), "OfficeFloor open, so pool should be active");
				assertValidConnection("Should keep pooled connection open", true, RetrieveDataSection.connection);

			} else {
				// Validate creating new connection each time
				assertNull(pool, "Should not have pool");
				assertValidConnection("Should close connection", false, RetrieveDataSection.connection);
			}
		}

		// Ensure pool is closed
		if (isPool) {
			assertTrue(pool.isDisposed(), "OfficeFloor closed, so pool should be disposed");
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
			assertSame(connection, source.getConnection().block(TIMEOUT), "Should be same connection");

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
			assertEquals(activeCount, pool.getMetrics().get().acquiredSize(), message);
		}
	}

	private static void assertValidConnection(String message, boolean isValid, Connection connection) {
		assertEquals(Boolean.valueOf(isValid), Mono.from(connection.validate(ValidationDepth.REMOTE)).block(TIMEOUT),
				message);
	}

}
