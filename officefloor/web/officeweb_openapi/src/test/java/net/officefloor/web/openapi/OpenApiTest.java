package net.officefloor.web.openapi;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.parameters.CookieParameter;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import net.officefloor.compile.spi.office.ExecutionManagedFunction;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.section.clazz.Next;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.HttpCookieParameter;
import net.officefloor.web.HttpHeaderParameter;
import net.officefloor.web.HttpObject;
import net.officefloor.web.HttpParameters;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.HttpResponse;
import net.officefloor.web.ObjectResponse;
import net.officefloor.web.build.HttpInput;
import net.officefloor.web.build.HttpObjectParser;
import net.officefloor.web.build.HttpObjectParserFactory;
import net.officefloor.web.build.HttpObjectResponder;
import net.officefloor.web.build.HttpObjectResponderFactory;
import net.officefloor.web.compile.CompileWebExtension;
import net.officefloor.woof.compile.CompileWoof;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Test generating OpenAPI specification.
 * 
 * @author Daniel Sagenschneider
 */
public class OpenApiTest extends OfficeFrameTestCase {

	/**
	 * Ensure able to obtain swagger specification.
	 */
	public void testAllMethods() {
		this.doOpenApiTest((context) -> {
			for (HttpMethod httpMethod : HttpMethod.values()) {
				context.link(false, httpMethod.name(), "/methods/all", NoOpService.class);
			}
		});
	}

	public static class NoOpService {
		public void service() {
			// no operation
		}
	}

	/**
	 * Ensure able to provide path description.
	 */
	public void testPathDescription() {
		this.doOpenApiTest((context) -> {
			HttpInput input = context.link(false, "/path", NoOpService.class);
			// input.setDocumentation("TEST DESCRIPTION");
		});
	}

	/**
	 * Ensure can provide {@link PathParameter}.
	 */
	public void testPathParameter() {
		this.doOpenApiTest((context) -> context.link(false, "/path/{parameter}", PathParameterService.class));
	}

	public static class PathParameterService {
		public void service(@HttpPathParameter("parameter") String parameter) {
			// no operation
		}
	}

	/**
	 * Ensure can provide {@link QueryParameter}.
	 */
	public void testQueryParameter() {
		this.doOpenApiTest((context) -> context.link(false, "/path", QueryParameterService.class));
	}

	public static class QueryParameterService {
		public void service(@HttpQueryParameter("parameter") String parameter) {
			// no operation
		}
	}

	/**
	 * Ensure can provide {@link HeaderParameter}.
	 */
	public void testHeaderParameter() {
		this.doOpenApiTest((context) -> context.link(false, "/path", HeaderParameterService.class));
	}

	public static class HeaderParameterService {
		public void service(@HttpHeaderParameter("parameter") String parameter) {
			// no operation
		}
	}

	/**
	 * Ensure can provide {@link CookieParameter}.
	 */
	public void testCookieParameter() {
		this.doOpenApiTest((context) -> context.link(false, "/path", CookieParameterService.class));
	}

	public static class CookieParameterService {
		public void service(@HttpCookieParameter("parameter") String parameter) {
			// no operation
		}
	}

	/**
	 * Ensure can annotate {@link Parameter}.
	 */
	public void testAnnotatedParameter() {
		this.doOpenApiTest((context) -> context.link(false, "/path", AnnotatedParameterService.class));
	}

	public static class AnnotatedParameterService {
		public void service(
				@io.swagger.v3.oas.annotations.Parameter(description = "DESCRIPTION", required = true, example = "EXAMPLE") @HttpQueryParameter("parameter") String parameter) {
			// no operation
		}
	}

	/**
	 * Ensure can describe {@link HttpParameters} object.
	 */
	public void testHttpParameters() {
		this.doOpenApiTest((context) -> context.link(false, "/path/{one}", ParametersService.class));
	}

	@HttpParameters
	public static class Parameters implements Serializable {
		private static final long serialVersionUID = 1L;

		public void setOne(String one) {
			// no operation
		}

		public void setTwo(String two) {
			// no operation
		}
	}

	public static class ParametersService {
		public void service(Parameters parameters) {
			// no operation
		}
	}

	/**
	 * Ensure can provide {@link RequestBody}.
	 */
	public void testRequestBody() {
		this.doOpenApiTest((context) -> context.link(false, "/path", RequestBodyService.class));
	}

	@HttpObject
	public static class Request {
		public String getMessage() {
			return "MOCK";
		}
	}

	public static class RequestBodyService {
		public void service(Request request) {
			// no operation
		}
	}

	/**
	 * Ensure can provide complex {@link RequestBody}.
	 */
	public void testRequestComplexBody() {
		this.doOpenApiTest((context) -> context.link(false, "/path", RequestComplexBodyService.class));
	}

	@HttpObject
	public static class ComplexRequest {
		public RequestChild getChild() {
			return new RequestChild();
		}
	}

	public static class RequestChild {
		public String getMessage() {
			return "MOCK";
		}
	}

	public static class RequestComplexBodyService {
		public void service(ComplexRequest request) {
			// no operation
		}
	}

	/**
	 * Ensure handle alternate {@link RequestBody} format.
	 */
	public void testRequestBodyAlternateContentType() {
		this.doOpenApiTest((context) -> {
			context.link(false, "/path", RequestBodyService.class);
			context.getWebArchitect().addHttpObjectParser(new MockHttpObjectParserFactory<>());
		});
	}

	private static class MockHttpObjectParserFactory<P> implements HttpObjectParserFactory, HttpObjectParser<P> {

		@Override
		public String getContentType() {
			return "mock/test";
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> HttpObjectParser<T> createHttpObjectParser(Class<T> objectClass) throws Exception {
			return (HttpObjectParser<T>) this;
		}

		@Override
		public Class<P> getObjectType() {
			fail("Should not be required");
			return null;
		}

		@Override
		public P parse(ServerHttpConnection connection) throws HttpException {
			fail("Should not require to parse");
			return null;
		}
	}

	/**
	 * Ensure can provide {@link ApiResponse}.
	 */
	public void testResponse() {
		this.doOpenApiTest((context) -> context.link(false, "/path", ResponseService.class));
	}

	public static class Response {
		public String getResult() {
			return "MOCK";
		}
	}

	public static class ResponseService {
		public void service(ObjectResponse<Response> responder) {
			// no operation
		}
	}

	/**
	 * Ensure can provide complex {@link ApiResponse}.
	 */
	public void testResponseComplex() {
		this.doOpenApiTest((context) -> context.link(false, "/path", ResponseComplexService.class));
	}

	public static class ComplexResponse {
		public ResponseChild getChild() {
			return new ResponseChild();
		}
	}

	public static class ResponseChild {
		public String getResult() {
			return "MOCK";
		}
	}

	public static class ResponseComplexService {
		public void service(ObjectResponse<ComplexResponse> responder) {
			// no operation
		}
	}

	/**
	 * Ensure can provide alternate status {@link ApiResponse}.
	 */
	public void testResponseAlternateStatus() {
		this.doOpenApiTest((context) -> context.link(false, "/path", AlternateStatusResponseService.class));
	}

	public static class AlternateStatusResponseService {
		public void service(@HttpResponse(status = 418) ObjectResponse<Response> responder) {
			// no operation
		}
	}

	/**
	 * Ensure handle alternate {@link ApiResponse} format.
	 */
	public void testResponseAlternateContentType() {
		this.doOpenApiTest((context) -> {
			context.link(false, "/path", ResponseService.class);
			context.getWebArchitect().addHttpObjectResponder(new MockHttpObjectResponderFactory<>());
		});
	}

	public static class MockHttpObjectResponderFactory<P>
			implements HttpObjectResponderFactory, HttpObjectResponder<P> {

		@Override
		public String getContentType() {
			return "mock/test";
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> HttpObjectResponder<T> createHttpObjectResponder(Class<T> objectType) {
			return (HttpObjectResponder<T>) this;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <E extends Throwable> HttpObjectResponder<E> createHttpEscalationResponder(Class<E> escalationType) {
			return (HttpObjectResponder<E>) this;
		}

		@Override
		public Class<P> getObjectType() {
			fail("Should not be required");
			return null;
		}

		@Override
		public void send(P object, ServerHttpConnection connection) throws IOException {
			fail("Should no require to send");
		}
	}

	/**
	 * Ensure that can explore graph of {@link ExecutionManagedFunction} instances.
	 */
	public void testExploreGraph() {
		this.doOpenApiTest((context) -> context.link(false, "/path", ExploreGraphService.class));
	}

	@FlowInterface
	public static interface Flows {
		void flow();
	}

	@FlowInterface
	public static interface FurtherFlows {
		void furtherFlow();
	}

	public static class ExploreGraphService {

		@Next("next")
		public void service(Flows flows) throws IOException {
			// no operation
		}

		public void flow(FurtherFlows flows, @HttpQueryParameter("flow") String parameter) {
			// no operation
		}

		public void furtherFlow(@HttpQueryParameter("furtherFlow") String parameter) {
			// no operation
		}

		@Next("furtherNext")
		public void next(@HttpQueryParameter("next") String parameter) {
			// no operation
		}

		public void furtherNext(@HttpQueryParameter("furtherNext") String parameter) {
			// no operation
		}

		public void handleIoException(@net.officefloor.plugin.section.clazz.Parameter IOException exception,
				@HttpQueryParameter("exception") String parameter) throws SQLException {
			// no operation
		}

		public void handleSqlException(@net.officefloor.plugin.section.clazz.Parameter SQLException exception,
				@HttpQueryParameter("furtherException") String parameter) {
			// no operation
		}
	}

	/**
	 * Ensure handles recursive graph of {@link ExecutionManagedFunction} instances.
	 */
	public void testExploreRecursiveGraph() {
		this.doOpenApiTest((context) -> context.link(false, "/path", ExploreRecursiveGraphService.class));
	}

	@FlowInterface
	public static interface RecursiveFlows {
		void service();
	}

	public static class ExploreRecursiveGraphService {

		@Next("recursive")
		public void service() {
			// no operation
		}

		@Next("next")
		public void recursive(Flows flows, @net.officefloor.plugin.section.clazz.Parameter SQLException exception)
				throws IOException {
			// no operation
		}

		public void flow(RecursiveFlows flows, @HttpQueryParameter("flow") String parameter) {
			// no operation
		}

		@Next("service")
		public void next(@HttpQueryParameter("next") String parameter) {
			// no operation
		}

		public void handle(@net.officefloor.plugin.section.clazz.Parameter IOException exception,
				@HttpQueryParameter("exception") String parameter) throws SQLException {
			// no operation
		}
	}

	/**
	 * Ensure explore {@link Escalation} instances for failed responses.
	 */
	public void testHandleEscalations() {
		fail("TODO implement exploring escalation handling for failed responses");
	}

	/**
	 * Ensure can handle <code>BASIC</code> security.
	 */
	public void testBasicSecurity() {
		fail("TODO handle basic security");
	}

	/**
	 * Ensure can handle <code>Bearer</code> security.
	 */
	public void testBearerSecurity() {
		fail("TODO handle bearer security");
	}

	/**
	 * Undertakes the OpenAPI test.
	 * 
	 * @param extension {@link CompileWebExtension}.
	 */
	private void doOpenApiTest(CompileWebExtension extension) {
		try {
			CompileWoof compiler = new CompileWoof();
			compiler.web(extension);
			try (MockWoofServer server = compiler.open()) {

				// Obtain the expected specification
				String testName = this.getName();
				String expectedFileName = testName.substring("test".length()) + ".json";
				String expectedContent = this.getFileContents(this.findFile(this.getClass(), expectedFileName));

				// Translate to YAML and JSON (round trip for better comparison)
				OpenAPI expectedApi = Json.mapper().readValue(expectedContent, OpenAPI.class);

				// Ensure correct JSON
				MockWoofResponse response = server.send(MockHttpServer.mockRequest("/openapi.json"));
				assertEquals("Should find OpenAPI JSON", 200, response.getStatus().getStatusCode());
				String expectedJson = Json.pretty(expectedApi);
				String actualJson = response.getEntity(null);
				this.printMessage("EXPECTED:\n" + expectedJson);
				this.printMessage("JSON:\n" + actualJson);
				assertEquals("Incorrect JSON", expectedJson, actualJson);

				// Ensure correct YAML
				response = server.send(MockHttpServer.mockRequest("/openapi.yaml"));
				assertEquals("Should find OpenAPI YAML", 200, response.getStatus().getStatusCode());
				String expectedYaml = Yaml.pretty(expectedApi);
				String actualYaml = response.getEntity(null);
				this.printMessage("YAML:\n" + actualYaml);
				assertEquals("Incorrect YAML", expectedYaml, actualYaml);
			}
		} catch (Exception ex) {
			throw fail(ex);
		}
	}

}