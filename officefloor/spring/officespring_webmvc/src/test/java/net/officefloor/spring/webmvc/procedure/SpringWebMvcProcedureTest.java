/*-
 * #%L
 * Spring Web MVC Integration
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

package net.officefloor.spring.webmvc.procedure;

import java.lang.reflect.Method;
import java.util.function.Consumer;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.ProcedureLoaderUtil;
import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.tutorial.springapp.InjectController;
import net.officefloor.tutorial.springapp.SimpleController;
import net.officefloor.woof.compile.CompileWoof;
import net.officefloor.woof.mock.MockWoofServer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Ensure can invoke Spring {@link Controller}.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringWebMvcProcedureTest extends OfficeFrameTestCase {

	/**
	 * Validates non {@link Controller} listing {@link Procedure} instances.
	 */
	public void testNonControllerProcedures() {
		// Load default class methods
		ProcedureLoaderUtil.validateProcedures(NonController.class, ProcedureLoaderUtil.procedure("method"),
				ProcedureLoaderUtil.procedure("service"));
	}

	public static class NonController {
		public void method() {
			// ignored
		}

		@GetMapping("/ignored")
		public void service() {
			// no controller, so ignored
		}
	}

	/**
	 * Validates {@link Controller} listing {@link Procedure} instances.
	 */
	public void testControllerProcedures() {
		ProcedureLoaderUtil.validateProcedures(SpringController.class,
				ProcedureLoaderUtil.procedure("service", SpringWebMvcProcedureSource.class));
	}

	@Controller
	public static class SpringController {
		public void ignored() {
			// ignored
		}

		@PostMapping("/service")
		public void service() {
			// included
		}
	}

	/**
	 * Validates {@link RestController} listing {@link Procedure} instances.
	 */
	public void testRestControllerProcedures() {
		ProcedureLoaderUtil.validateProcedures(SpringRestController.class,
				ProcedureLoaderUtil.procedure("post", SpringWebMvcProcedureSource.class),
				ProcedureLoaderUtil.procedure("service", SpringWebMvcProcedureSource.class));
	}

	@RestController
	@RequestMapping("/prefix")
	public static class SpringRestController {
		public void ignored() {
			// ignored
		}

		@GetMapping("/service")
		public String service() {
			return "included";
		}

		@RequestMapping(method = RequestMethod.PUT)
		public String post() {
			return "post";
		}

		// Web Flux should be ignored

		@GetMapping("/mono")
		public Mono<String> ignoredMono() {
			return Mono.just("IGNORED");
		}

		@GetMapping("/flux")
		public Flux<String> ignoredFlux() {
			return Flux.just("IGNORED");
		}
	}

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
		this.doControllerTest("GET", "/{param}", InjectController.class, "pathParam",
				(server) -> server.send(MockHttpServer.mockRequest("/value")).assertResponse(200, "Parameter value"));
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
					controllerClass.getName(), SpringWebMvcProcedureSource.SOURCE_NAME, controllerMethodName, false,
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
