/*-
 * #%L
 * Vertx SQL Client
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

package net.officefloor.vertx.sqlclient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.SqlConnection;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.test.UsesDockerTest;
import net.officefloor.vertx.OfficeFloorVertx;
import net.officefloor.vertx.OfficeFloorVertxException;

/**
 * Tests the {@link VertxSqlPoolManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
@UsesDockerTest
public class VertxSqlPoolManagedObjectSourceTest extends AbstractDatabaseTestCase {

	/**
	 * Ensure correct specification.
	 */
	@Test
	public void specification() {
		ManagedObjectLoaderUtil.validateSpecification(VertxSqlPoolManagedObjectSource.class);
	}

	/**
	 * Ensure correct type.
	 */
	@Test
	public void type() {
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(Pool.class);
		ManagedObjectLoaderUtil.validateManagedObjectType(type, VertxSqlPoolManagedObjectSource.class);
	}

	/**
	 * Ensure can retrieve data.
	 */
	@Test
	public void retrieveData() throws Throwable {

		// Create to extract information to test
		VertxSqlPoolManagedObjectSource poolMos = new VertxSqlPoolManagedObjectSource();

		// Setup data
		this.setupDatabase();

		// Ensure can retrieve data
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.office((context) -> {
			context.addSection("source", RetrieveDataSection.class);
			this.configureSqlPoolSource(context.getOfficeArchitect(), poolMos);
		});
		try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {

			// Ensure pool is active
			SqlConnection connection = OfficeFloorVertx.block(poolMos.getPool().getConnection());
			assertNotNull(connection, "should have connection");
			OfficeFloorVertx.block(connection.close());

			// Ensure retrieve the message
			RetrieveDataSection.message = null;
			CompileOfficeFloor.invokeProcess(officeFloor, "source.retrieve", null);
			assertEquals("TEST", RetrieveDataSection.message, "Incorrect message");
		}

		// Ensure pool is closed
		try {
			OfficeFloorVertx.block(poolMos.getPool().getConnection());
			fail("Should not successfully obtain connection as pool should be closed");
		} catch (OfficeFloorVertxException ex) {
			assertEquals("Pool closed", ex.getCause().getMessage(), "Should have closed pool");
		}
	}

	public static class RetrieveDataSection {

		private static String message = null;

		public void retrieve(Pool pool) throws Exception {
			message = OfficeFloorVertx
					.block(pool.withConnection((connection) -> connection.query("SELECT message FROM test").execute()
							.map((rowSet) -> rowSet.iterator().next().getString("message"))));
		}
	}

}
