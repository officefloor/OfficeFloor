/*-
 * #%L
 * Spring Web Flux Integration
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

package net.officefloor.spring.webflux.procedure;

import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Method;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
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
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.tutorial.springfluxapp.FluxController;
import net.officefloor.woof.compile.CompileWoof;
import net.officefloor.woof.mock.MockWoofServer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Tests the {@link SpringWebFluxProcedureSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringWebFluxProcedureTest {

	/**
	 * Validates non {@link Controller} listing {@link Procedure} instances.
	 */
	@Test
	public void nonControllerProcedures() {
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
	@Test
	public void controllerProcedures() {
		ProcedureLoaderUtil.validateProcedures(SpringController.class,
				ProcedureLoaderUtil.procedure("service", SpringWebFluxProcedureSource.class));
	}

	@Controller
	public static class SpringController {
		public Mono<String> ignored() {
			return Mono.just("ignored");
		}

		@PostMapping("/service")
		public Mono<String> service() {
			return Mono.just("included");
		}
	}

	/**
	 * Validates {@link RestController} listing {@link Procedure} instances.
	 */
	@Test
	public void restControllerProcedures() {
		ProcedureLoaderUtil.validateProcedures(SpringRestController.class,
				ProcedureLoaderUtil.procedure("post", SpringWebFluxProcedureSource.class),
				ProcedureLoaderUtil.procedure("service", SpringWebFluxProcedureSource.class));
	}

	@RestController
	@RequestMapping("/prefix")
	public static class SpringRestController {
		public Mono<String> ignored() {
			return Mono.just("ignored");
		}

		@GetMapping("/service")
		public Mono<String> service() {
			return Mono.just("included");
		}

		@RequestMapping(method = RequestMethod.PUT)
		public Flux<String> post() {
			return Flux.just("post");
		}

		// Web MVC should be ignored

		@GetMapping("/string")
		public String ignoredString() {
			return "IGNORED";
		}

		@PostMapping("/void")
		public void ignoredVoidMvc() {
			// ignored
		}
	}

	/**
	 * {@link FluxController#inject()}.
	 */
	@Test
	public void inject() {
		this.doControllerTest("GET", "/", FluxController.class, "inject",
				(server) -> server.send(MockHttpServer.mockRequest()).assertResponse(200, "Inject Dependency"));
	}

	/**
	 * {@link FluxController#status()}.
	 */
	@Test
	public void status() {
		this.doControllerTest("GET", "/", FluxController.class, "status",
				(server) -> server.send(MockHttpServer.mockRequest()).assertResponse(201, "Status"));
	}

	/**
	 * {@link FluxController#pathParam(String)}.
	 */
	@Test
	public void pathParam() {
		this.doControllerTest("GET", "/{param}", FluxController.class, "pathParam",
				(server) -> server.send(MockHttpServer.mockRequest("/value")).assertResponse(200, "Parameter value"));
	}

	/**
	 * {@link FluxController#requestParam(String)}.
	 */
	@Test
	public void queryParam() {
		this.doControllerTest("GET", "/", FluxController.class, "requestParam", (server) -> server
				.send(MockHttpServer.mockRequest("/?param=value")).assertResponse(200, "Parameter value"));
	}

	/**
	 * {@link FluxController#header(String)}.
	 */
	@Test
	public void header() {
		this.doControllerTest("GET", "/", FluxController.class, "header", (server) -> server
				.send(MockHttpServer.mockRequest("/").header("header", "value")).assertResponse(200, "Header value"));
	}

	/**
	 * {@link FluxController#post(String)}.
	 */
	@Test
	public void post() {
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
			fail(ex);
		}
	}

}
