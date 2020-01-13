package net.officefloor.r2dbc;

import org.h2.engine.Procedure;

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
	public void testMonoReturn() throws Throwable {

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
			assertEquals("Incorrect message", "TEST", Procedures.message);
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