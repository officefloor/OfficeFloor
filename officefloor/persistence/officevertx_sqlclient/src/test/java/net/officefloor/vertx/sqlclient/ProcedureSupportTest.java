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

import org.junit.jupiter.api.Test;

import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import net.officefloor.activity.impl.procedure.ClassProcedureSource;
import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.activity.procedure.build.ProcedureEmployer;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.test.UsesDockerTest;

/**
 * Ensure can support returning {@link Future} from {@link Procedure}.
 * 
 * @author Daniel Sagenschneider
 */
@UsesDockerTest
public class ProcedureSupportTest extends AbstractDatabaseTestCase {

	/**
	 * Ensure can handle {@link Future} return.
	 */
	@Test
	public void futureReturn() throws Throwable {

		// Setup the database
		this.setupDatabase();

		// Ensure can retrieve data
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.office((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();
			this.configureSqlPoolSource(office, new VertxSqlPoolManagedObjectSource());

			// Configure the procedures
			ProcedureArchitect<OfficeSection> procedures = ProcedureEmployer.employProcedureArchitect(office,
					context.getOfficeSourceContext());
			OfficeSection getMessage = procedures.addProcedure("getMessage", Procedures.class.getName(),
					ClassProcedureSource.SOURCE_NAME, "getMessage", true, null);
			OfficeSection next = procedures.addProcedure("next", Procedures.class.getName(),
					ClassProcedureSource.SOURCE_NAME, "next", false, null);
			office.link(getMessage.getOfficeSectionOutput(ProcedureArchitect.NEXT_OUTPUT_NAME),
					next.getOfficeSectionInput(ProcedureArchitect.INPUT_NAME));
		});
		try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {

			// Undertake procedures
			Procedures.message = null;
			CompileOfficeFloor.invokeProcess(officeFloor, "getMessage.procedure", null);
			assertEquals("TEST", Procedures.message, "Incorrect message");
		}
	}

	public static class Procedures {

		private static String message = null;

		public Future<String> getMessage(Pool pool) {
			return pool.withConnection((connection) -> connection.query("SELECT message FROM test").execute()
					.map((rowSet) -> rowSet.iterator().next().getString("message")));
		}

		public void next(@Parameter String msg) {
			message = msg;
		}
	}

}
