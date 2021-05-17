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

package net.officefloor.vertx.sqlclient;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.vertx.sqlclient.Pool;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.vertx.OfficeFloorVertx;

/**
 * Tests the {@link VertxSqlPoolManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
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

			// Ensure retrieve the message
			RetrieveDataSection.message = null;
			CompileOfficeFloor.invokeProcess(officeFloor, "source.retrieve", null);
			assertEquals("TEST", RetrieveDataSection.message, "Incorrect message");
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