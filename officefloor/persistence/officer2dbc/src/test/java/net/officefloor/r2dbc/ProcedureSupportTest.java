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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.h2.engine.Procedure;
import org.junit.jupiter.api.Test;

import net.officefloor.activity.impl.procedure.ClassProcedureSource;
import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.activity.procedure.build.ProcedureEmployer;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.plugin.section.clazz.Parameter;
import reactor.core.publisher.Mono;

/**
 * Ensure can support returning {@link Mono} from {@link Procedure}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcedureSupportTest extends AbstractDatabaseTestCase {

	/**
	 * Ensure can handle {@link Mono} return.
	 */
	@Test
	public void monoReturn() throws Throwable {

		// Setup the database
		this.setupDatabase();

		// Ensure can retrieve data
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.office((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();
			this.configureR2dbcSource(office, new R2dbcManagedObjectSource(), false);

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

		public Mono<String> getMessage(R2dbcSource source) {
			return source.getConnection()
					.flatMap(c -> Mono.from(c.createStatement("SELECT message FROM test").execute()))
					.map(r -> r.map((row, metaData) -> row.get("message", String.class)))
					.flatMap(msg -> Mono.from(msg));
		}

		public void next(@Parameter String msg) {
			message = msg;
		}
	}

}
