package net.officefloor.tutorial.jaxrshttpserver;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import net.officefloor.activity.impl.procedure.ClassProcedureSource;
import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.plugin.managedobject.singleton.Singleton;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.servlet.supply.ServletWoofExtensionService;
import net.officefloor.tutorial.jaxrshttpserver.migrated.MigratedResource;
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

			// Configure Servlet
			new ServletWoofExtensionService().extend(context);

			// Add the JAX-RS procedures
			addJaxRsProcedure("GET", "/migrated", MigratedResource.class, "get", procedures, web, office);
			addJaxRsProcedure("GET", "/migrated/path/{param}", MigratedResource.class, "path", procedures, web, office);
			addJaxRsProcedure("PUT", "/migrated/update", MigratedResource.class, "post", procedures, web, office);

			// Dependency
			Singleton.load(office, new JaxRsDependency());
		});
		server = compile.open();
	}

	private static void addJaxRsProcedure(String httpMethod, String path, Class<?> migratedClass, String methodName,
			ProcedureArchitect<OfficeSection> procedures, WebArchitect web, OfficeArchitect office) {
		OfficeSection controller = procedures.addProcedure("_" + methodName, migratedClass.getName(),
				ClassProcedureSource.SOURCE_NAME, methodName, false, null);
		office.link(web.getHttpInput(false, httpMethod, path).getInput(),
				controller.getOfficeSectionInput(ProcedureArchitect.INPUT_NAME));
	}

	@AfterClass
	public static void stopServer() throws Exception {
		server.close();
	}

	@Test
	public void get() {
		MockWoofResponse response = server.send(MockWoofServer.mockRequest("/migrated"));
		response.assertResponse(200, "GET OfficeFloor Dependency");
	}

	@Test
	public void pathParam() {
		MockWoofResponse response = server.send(MockWoofServer.mockRequest("/migrated/path/parameter"));
		response.assertJson(200, new ResponseModel("parameter"));
	}

	@Test
	public void put() {
		MockWoofResponse response = server
				.send(MockWoofServer.mockJsonRequest(HttpMethod.PUT, "/migrated/update", new RequestModel("INPUT")));
		response.assertJson(200, new ResponseModel("INPUT"));
	}

}