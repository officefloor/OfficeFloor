package net.officefloor.tutorial.springwebfluxhttpserver;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import net.officefloor.activity.impl.procedure.ClassProcedureSource;
import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.plugin.managedobject.singleton.Singleton;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.spring.SpringSupplierSource;
import net.officefloor.tutorial.springwebfluxhttpserver.migrated.MigratedController;
import net.officefloor.tutorial.springwebfluxhttpserver.migrated.MigratedRestController;
import net.officefloor.tutorial.springwebfluxhttpserver.migrated.SendResponse;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.template.build.WebTemplateArchitect;
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
			WebTemplateArchitect templates = context.getWebTemplater();

			// Configure in Spring
			office.addSupplier("Spring", SpringSupplierSource.class.getName())
					.addProperty(SpringSupplierSource.CONFIGURATION_CLASS_NAME, Application.class.getName());

			// Add sender
			OfficeSection sender = office.addOfficeSection("SENDER", ClassSectionSource.class.getName(),
					SendResponse.class.getName());

			// Add the controller procedures
			addSpringControllerProcedure("GET", "/migrated/rest", MigratedRestController.class, "get", sender,
					procedures, web, office);
			addSpringControllerProcedure("GET", "/migrated/path/{param}", MigratedRestController.class, "path", sender,
					procedures, web, office);
			addSpringControllerProcedure("PUT", "/migrated/update", MigratedRestController.class, "post", sender,
					procedures, web, office);

			// Add the template
			templates.addTemplate(false, "/migrated/html", "migrated/simple.woof.html")
					.setLogicClass(MigratedController.class.getName());

			// Dependency
			Singleton.load(office, new SpringDependency());
		});
		server = compile.open();
	}

	private static void addSpringControllerProcedure(String httpMethod, String path, Class<?> migratedClass,
			String methodName, OfficeSection sender, ProcedureArchitect<OfficeSection> procedures, WebArchitect web,
			OfficeArchitect office) {
		OfficeSection controller = procedures.addProcedure("_" + methodName, migratedClass.getName(),
				ClassProcedureSource.SOURCE_NAME, methodName, true, null);
		office.link(web.getHttpInput(false, httpMethod, path).getInput(),
				controller.getOfficeSectionInput(ProcedureArchitect.INPUT_NAME));
		office.link(controller.getOfficeSectionOutput(ProcedureArchitect.NEXT_OUTPUT_NAME),
				sender.getOfficeSectionInput("send"));
	}

	@AfterClass
	public static void stopServer() throws Exception {
		server.close();
	}

	@Test
	public void get() {
		MockWoofResponse response = server.send(MockWoofServer.mockRequest("/migrated/rest"));
		response.assertJson(200, new ResponseModel("GET Spring Dependency"));
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

	@Test
	public void html() {
		MockWoofResponse response = server.send(MockWoofServer.mockRequest("/migrated/html?name=Daniel"));
		response.assertResponse(303, "");
		String location = response.getHeader("location").getValue();
		response = server.send(MockWoofServer.mockRequest(location).cookies(response));
		response.assertResponse(200, "<html><body><p >Hello Daniel</p></body></html>");
	}

}