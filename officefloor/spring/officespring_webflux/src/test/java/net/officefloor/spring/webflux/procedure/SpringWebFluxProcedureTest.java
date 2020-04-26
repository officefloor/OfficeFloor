package net.officefloor.spring.webflux.procedure;

import java.lang.reflect.Method;
import java.util.function.Consumer;

import org.springframework.stereotype.Controller;

import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.tutorial.springfluxapp.FluxController;
import net.officefloor.woof.compile.CompileWoof;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Tests the {@link SpringWebFluxProcedureSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringWebFluxProcedureTest extends OfficeFrameTestCase {

	/**
	 * {@link FluxController#inject()}.
	 */
	public void testInject() {
		this.doControllerTest("GET", "/", FluxController.class, "inject",
				(server) -> server.send(MockHttpServer.mockRequest()).assertResponse(200, "Inject Dependency"));
	}

	/**
	 * {@link FluxController#status()}.
	 */
	public void testStatus() {
		this.doControllerTest("GET", "/", FluxController.class, "status",
				(server) -> server.send(MockHttpServer.mockRequest()).assertResponse(201, "Status"));
	}

	/**
	 * {@link FluxController#pathParam(String)}.
	 */
	public void testPathParam() {
		this.doControllerTest("GET", "/{param}", FluxController.class, "pathParam",
				(server) -> server.send(MockHttpServer.mockRequest("/value")).assertResponse(200, "Parameter value"));
	}

	/**
	 * {@link FluxController#requestParam(String)}.
	 */
	public void testQueryParam() {
		this.doControllerTest("GET", "/", FluxController.class, "requestParam", (server) -> server
				.send(MockHttpServer.mockRequest("/?param=value")).assertResponse(200, "Parameter value"));
	}

	/**
	 * {@link FluxController#header(String)}.
	 */
	public void testHeader() {
		this.doControllerTest("GET", "/", FluxController.class, "header", (server) -> server
				.send(MockHttpServer.mockRequest("/").header("header", "value")).assertResponse(200, "Header value"));
	}

	/**
	 * {@link FluxController#post(String)}.
	 */
	public void testPost() {
		this.doControllerTest("GET", "/", FluxController.class, "post", (server) -> server
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
					controllerClass.getName(), SpringWebFluxProcedureSource.SOURCE_NAME, controllerMethodName, false,
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