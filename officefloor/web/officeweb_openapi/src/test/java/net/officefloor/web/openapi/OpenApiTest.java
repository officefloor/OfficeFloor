/*-
 * #%L
 * OpenAPI
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

package net.officefloor.web.openapi;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.parameters.CookieParameter;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import net.officefloor.compile.spi.office.ExecutionManagedFunction;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.clazz.Qualified;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
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
import net.officefloor.web.build.HttpObjectParser;
import net.officefloor.web.build.HttpObjectParserFactory;
import net.officefloor.web.build.HttpObjectResponder;
import net.officefloor.web.build.HttpObjectResponderFactory;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.compile.CompileWebExtension;
import net.officefloor.web.jwt.JwtHttpSecuritySource;
import net.officefloor.web.openapi.operation.OpenApiOperationBuilder;
import net.officefloor.web.openapi.operation.OpenApiOperationContext;
import net.officefloor.web.openapi.operation.OpenApiOperationFunctionContext;
import net.officefloor.web.security.HttpAccess;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.build.HttpSecurityArchitect;
import net.officefloor.web.security.build.HttpSecurityBuilder;
import net.officefloor.web.security.scheme.BasicHttpSecuritySource;
import net.officefloor.web.security.store.MockCredentialStoreManagedObjectSource;
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
	 * {@link CompileWoof}.
	 */
	private final CompileWoof compiler = new CompileWoof(true);

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
	public void testDescription() {
		this.doOpenApiTest((context) -> {
			context.link(false, "/path", NoOpService.class).setDocumentation("TEST DESCRIPTION");
		});
	}

	/**
	 * Ensure able to extract path summary by newline.
	 */
	public void testSummaryByNewLine() {
		this.doOpenApiTest((context) -> {
			context.link(false, "/path", NoOpService.class).setDocumentation("TEST\n Not summary. DESCRIPTION");
		});
	}

	/**
	 * Ensure able to extract path summary by period.
	 */
	public void testSummaryByPeriod() {
		this.doOpenApiTest((context) -> {
			context.link(false, "/path", NoOpService.class).setDocumentation("TEST. Not summary\n DESCRIPTION");
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
	 * Ensure can configure {@link Tag}.
	 */
	public void testTag() {
		this.doOpenApiTest((context) -> context.link(false, "/path", TagService.class));
	}

	public static class TagService {
		@Tag(name = "NAME", description = "DESCRIPTION")
		public void service() {
			// no operation
		}
	}

	/**
	 * Ensure can configure {@link Tags}.
	 */
	public void testTags() {
		this.doOpenApiTest((context) -> context.link(false, "/path", TagsService.class));
	}

	public static class TagsService {
		@Tag(name = "ONE", description = "First")
		@Tag(name = "TWO")
		@Tag(name = "THREE")
		@Tag(name = "THREE", description = "Third")
		@Tag(name = "THREE")
		public void service() {
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
	 * Ensure can provide array for {@link ApiResponse}.
	 */
	public void testResponseArray() {
		this.doOpenApiTest((context) -> context.link(false, "/path", ResponseArrayService.class));
	}

	public static class ResponseArrayService {
		public void service(ObjectResponse<Response[]> responder) {
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
		this.doOpenApiTest((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();
			OfficeSection section = context.addSection("SECTION", HandleEscalationsSection.class);
			office.link(context.getWebArchitect().getHttpInput(false, "/path").getInput(),
					section.getOfficeSectionInput("service"));
			for (Class<?> escalationClass : new Class<?>[] { IOException.class, SQLException.class,
					RuntimeException.class, Error.class, Throwable.class }) {
				office.link(office.addOfficeEscalation(escalationClass.getName()),
						section.getOfficeSectionInput(escalationClass.getSimpleName()));
			}
		});
	}

	public static class HandleEscalationsSection {
		public void service() throws SQLException {
			// no operation
		}

		// Should not be included as non-thrown checked exception
		public void IOException(@HttpQueryParameter("IOException") String parameter) {
			// no operation
		}

		public void SQLException(@HttpQueryParameter("SQLException") String parameter) {
			// no operation
		}

		public void RuntimeException(@HttpQueryParameter("RuntimeException") String parameter) {
			// no operation
		}

		public void Error(@HttpQueryParameter("Error") String parameter) {
			// no operation
		}

		public void Throwable(@HttpQueryParameter("Throwable") String parameter) {
			// no operation
		}
	}

	/**
	 * Ensure can handle <code>BASIC</code> and <code>Bearer</code> security.
	 */
	public void testSecurity() {
		this.compiler.woof((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();
			HttpSecurityArchitect security = context.getHttpSecurityArchitect();

			// Setup BASIC
			HttpSecurityBuilder basic = security.addHttpSecurity("BASIC", new BasicHttpSecuritySource());
			basic.addProperty(BasicHttpSecuritySource.PROPERTY_REALM, "test");
			basic.addContentType("application/*");
			office.addOfficeManagedObjectSource("CREDENTIALS", MockCredentialStoreManagedObjectSource.class.getName())
					.addOfficeManagedObject("CREDENTIALS", ManagedObjectScope.THREAD);

			// Setup JWT
			HttpSecurityBuilder jwt = security.addHttpSecurity("JWT", new JwtHttpSecuritySource<>());
			jwt.addProperty(JwtHttpSecuritySource.PROPERTY_CLAIMS_CLASS, Claims.class.getName());
			OfficeSectionInput handleJwt = office
					.addOfficeSection("HANDLE", ClassSectionSource.class.getName(), HandleJwt.class.getName())
					.getOfficeSectionInput("handle");
			for (JwtHttpSecuritySource.Flows flow : JwtHttpSecuritySource.Flows.values()) {
				office.link(jwt.getOutput(flow.name()), handleJwt);
			}
		});
		this.doOpenApiTest((context) -> {
			// Configure different paths (with different security)
			OfficeArchitect office = context.getOfficeArchitect();
			WebArchitect web = context.getWebArchitect();
			OfficeSection section = context.addSection("SECTION", SecuritySection.class);
			for (String service : new String[] { "basic", "jwt", "both", "claims", "httpAccess", "insecure" }) {
				office.link(web.getHttpInput(false, "/" + service).getInput(), section.getOfficeSectionInput(service));
			}
		});
	}

	public static class Claims {
	}

	public static class HandleJwt {
		public void handle() {
			// no operation
		}
	}

	public static class SecuritySection {
		@HttpAccess(withHttpSecurity = "BASIC")
		public void basic() {
			// no operation
		}

		@HttpAccess(withHttpSecurity = "JWT")
		public void jwt() {
			// no operation
		}

		@HttpAccess
		public void both() {
			// no operation
		}

		public void claims(Claims claims) {
			// no operation
		}

		public void httpAccess(@Qualified("BASIC") HttpAccessControl accessControl) {
			// no operation
		}

		public void insecure() {
			// no operation
		}
	}

	/**
	 * Ensure able to extend {@link Operation}.
	 */
	public void testExtendOperation() throws Exception {
		try {
			MockOpenApiOperationExtension.operationBuilder = new MockOperationBuilder();
			this.doOpenApiTest((context) -> {
				context.link(false, "/path", NoOpService.class);
			});
		} finally {
			MockOpenApiOperationExtension.operationBuilder = null;
		}
	}

	private static class MockOperationBuilder implements OpenApiOperationBuilder {

		@Override
		public void buildInManagedFunction(OpenApiOperationFunctionContext context) throws Exception {
			context.getOrAddSecurityRequirement("EXTEND");
		}

		@Override
		public void buildComplete(OpenApiOperationContext context) throws Exception {
			Map<String, SecurityScheme> securities = new HashMap<>();
			securities.put("EXTEND", new SecurityScheme().type(Type.HTTP).scheme("extend"));
			context.getComponents().setSecuritySchemes(securities);
		}
	}

	/**
	 * Undertakes the OpenAPI test.
	 * 
	 * @param extension {@link CompileWebExtension}.
	 */
	private void doOpenApiTest(CompileWebExtension extension) {
		try {
			this.compiler.web(extension);
			try (MockWoofServer server = this.compiler.open()) {

				// Obtain the expected specification
				String testName = this.getName();
				String expectedFileName = testName.substring("test".length()) + ".json";
				String expectedContent = this.getFileContents(this.findFile(this.getClass(), expectedFileName));

				// Translate to YAML and JSON (round trip for better comparison)
				Json.mapper().enable(JsonParser.Feature.ALLOW_COMMENTS);
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
