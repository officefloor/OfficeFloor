package net.officefloor.spring.webmvc.procedure;

import java.lang.reflect.Method;
import java.util.function.Consumer;

import org.springframework.stereotype.Controller;

import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.tutorial.springapp.InjectController;
import net.officefloor.tutorial.springapp.SimpleController;
import net.officefloor.woof.compile.CompileWoof;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Ensure can invoke Spring {@link Controller}.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringControllerProcedureTest extends OfficeFrameTestCase {

	/**
	 * {@link SimpleController}.
	 */
	public void testSimple() {
		this.doControllerTest("GET", "/", SimpleController.class, "simple",
				(server) -> server.send(MockHttpServer.mockRequest()).assertResponse(200, "Simple Spring"));
	}

	/**
	 * {@link InjectController#inject()}.
	 */
	public void testInject() {
		this.doControllerTest("GET", "/", InjectController.class, "inject",
				(server) -> server.send(MockHttpServer.mockRequest()).assertResponse(200, "Inject Dependency"));
	}

	/**
	 * {@link InjectController#status()}.
	 */
	public void testStatus() {
		this.doControllerTest("GET", "/", InjectController.class, "status",
				(server) -> server.send(MockHttpServer.mockRequest()).assertResponse(201, "Status"));
	}

	/**
	 * {@link InjectController#pathParam(String)}.
	 */
	public void testPathParam() {
		this.doControllerTest("GET", "/path/{param}", InjectController.class, "pathParam", (server) -> server
				.send(MockHttpServer.mockRequest("/path/value")).assertResponse(200, "Parameter value"));
	}

	/**
	 * {@link InjectController#requestParam(String)}.
	 */
	public void testQueryParam() {
		this.doControllerTest("GET", "/", InjectController.class, "requestParam", (server) -> server
				.send(MockHttpServer.mockRequest("/?param=value")).assertResponse(200, "Parameter value"));
	}

	/**
	 * {@link InjectController#header(String)}.
	 */
	public void testHeader() {
		this.doControllerTest("GET", "/", InjectController.class, "header", (server) -> server
				.send(MockHttpServer.mockRequest("/").header("header", "value")).assertResponse(200, "Header value"));
	}

	/**
	 * {@link InjectController#post(String)}.
	 */
	public void testPost() {
		this.doControllerTest("GET", "/", InjectController.class, "post", (server) -> server
				.send(MockHttpServer.mockRequest("/").entity("value")).assertResponse(200, "Body value"));
	}

	/**
	 * Undertakes test.
	 * 
	 * @param httpMethodName       HTTP method name.
	 * @param path                 Path.
	 * @param controllerClass      {@link Controller} {@link Class}.
	 * @param controllerMethodName Name of {@link Controller} {@link Method}.
	 * @param validator            Validator.
	 */
	private void doControllerTest(String httpMethodName, String path, Class<?> controllerClass,
			String controllerMethodName, Consumer<MockWoofServer> validator) {
		CompileWoof compiler = new CompileWoof(true);
		compiler.woof((context) -> {
			OfficeSection controller = context.getProcedureArchitect().addProcedure("Controller",
					controllerClass.getName(), SpringControllerProcedureSource.SOURCE_NAME, controllerMethodName, false,
					null);
			context.getOfficeArchitect().link(
					context.getWebArchitect().getHttpInput(false, httpMethodName, path).getInput(),
					controller.getOfficeSectionInput(ProcedureArchitect.INPUT_NAME));
		});
		try (MockWoofServer server = compiler.open()) {
			validator.accept(server);
		} catch (Exception ex) {
			throw fail(ex);
		}
	}

}