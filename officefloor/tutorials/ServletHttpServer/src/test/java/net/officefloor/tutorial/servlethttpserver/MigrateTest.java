package net.officefloor.tutorial.servlethttpserver;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import net.officefloor.activity.impl.procedure.ClassProcedureSource;
import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.plugin.managedobject.singleton.Singleton;
import net.officefloor.tutorial.servlethttpserver.migrated.MigratedFilter;
import net.officefloor.tutorial.servlethttpserver.migrated.MigratedServlet;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.woof.compile.CompileWoof;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Tests the migrated code.
 * 
 * @author Daniel Sagenschneider
 */
public class MigrateTest {

	private static MockWoofServer server;

	@BeforeClass
	public static void startServer() throws Exception {
		CompileWoof compile = new CompileWoof();
		compile.woof((context) -> {
			ProcedureArchitect<OfficeSection> procedures = context.getProcedureArchitect();
			WebArchitect web = context.getWebArchitect();
			OfficeArchitect office = context.getOfficeArchitect();

			// Add migrated filter
			OfficeSection filter = procedures.addProcedure("filter", MigratedFilter.class.getName(),
					ClassProcedureSource.SOURCE_NAME, "doFilter", false, null);

			// Add migrated servlet
			OfficeSection servlet = procedures.addProcedure("servlet", MigratedServlet.class.getName(),
					ClassProcedureSource.SOURCE_NAME, "doGet", false, null);

			// Link
			office.link(web.getHttpInput(false, "/migrated").getInput(),
					filter.getOfficeSectionInput(ProcedureArchitect.INPUT_NAME));
			office.link(filter.getOfficeSectionOutput("doNext"),
					servlet.getOfficeSectionInput(ProcedureArchitect.INPUT_NAME));

			// Dependency
			Singleton.load(office, new InjectedDependency());
		});
		server = compile.open();
	}

	@AfterClass
	public static void stopServer() throws Exception {
		server.close();
	}

	@Test
	public void filterResponse() {
		MockWoofResponse response = server.send(MockWoofServer.mockRequest("/migrated?filter=true"));
		response.assertResponse(200, "FILTER WITH DEPENDENCY");
	}

	@Test
	public void servletResponse() {
		MockWoofResponse response = server.send(MockWoofServer.mockRequest("/migrated"));
		response.assertResponse(200, "SERVLET WITH DEPENDENCY");
	}

}