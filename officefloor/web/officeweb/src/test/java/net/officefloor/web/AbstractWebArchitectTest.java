/*-
 * #%L
 * Web Plug-in
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

package net.officefloor.web;

import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.HttpCookie;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import net.officefloor.compile.impl.structure.OfficeNodeImpl;
import net.officefloor.compile.spi.office.ExecutionManagedFunction;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.managedobject.singleton.Singleton;
import net.officefloor.plugin.section.clazz.Next;
import net.officefloor.server.http.EntityUtil;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.build.HttpInput;
import net.officefloor.web.build.HttpInputExplorerContext;
import net.officefloor.web.build.HttpObjectParser;
import net.officefloor.web.build.HttpObjectParserFactory;
import net.officefloor.web.build.HttpObjectParserServiceFactory;
import net.officefloor.web.build.HttpObjectResponder;
import net.officefloor.web.build.HttpObjectResponderFactory;
import net.officefloor.web.build.HttpObjectResponderServiceFactory;
import net.officefloor.web.build.HttpPathFactory;
import net.officefloor.web.build.HttpUrlContinuation;
import net.officefloor.web.build.HttpValueLocation;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.build.WebInterceptServiceFactory;
import net.officefloor.web.compile.WebCompileOfficeFloor;
import net.officefloor.web.session.HttpSession;

/**
 * Abstract tests for the {@link WebArchitect}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractWebArchitectTest extends OfficeFrameTestCase {

	/**
	 * Obtains the context path to use in testing.
	 * 
	 * @return Context path to use in testing. May be <code>null</code>.
	 */
	protected abstract String getContextPath();

	/**
	 * Context path to use for testing.
	 */
	private final String contextPath = this.getContextPath();

	/**
	 * {@link WebCompileOfficeFloor}.
	 */
	private final WebCompileOfficeFloor compile = new WebCompileOfficeFloor(this.contextPath);

	/**
	 * {@link MockHttpServer}.
	 */
	private MockHttpServer server;

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	@Override
	protected void setUp() throws Exception {
		this.compile.officeFloor((context) -> {
			this.server = MockHttpServer.configureMockHttpServer(context.getDeployedOffice()
					.getDeployedOfficeInput(WebArchitect.HANDLER_SECTION_NAME, WebArchitect.HANDLER_INPUT_NAME));
		});
	}

	@Override
	protected void tearDown() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Ensure able to GET root.
	 */
	public void testGetRoot() throws Exception {
		MockHttpResponse response = this.service("GET", "/", MockSection.class, this.mockRequest("/"));
		response.assertResponse(200, "TEST");
	}

	public static class MockSection {
		public void service(ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("TEST");
		}
	}

	/**
	 * Ensure able to GET secure root.
	 */
	public void testSecureGetRoot() throws Exception {
		this.secureService("GET", "/", MockSection.class, "/", "TEST");
	}

	/**
	 * Ensure able to obtain the path.
	 */
	public void testParamGetRoot() throws Exception {
		MockHttpResponse response = this.service("GET", "{path}", MockParamSection.class, this.mockRequest("/"));
		response.assertResponse(200, "/");
	}

	public static class MockParamSection {
		public void service(ServerHttpConnection connection, @HttpPathParameter("path") String parameter)
				throws IOException {
			connection.getResponse().getEntityWriter().write(parameter);
		}
	}

	/**
	 * Ensure able to GET secure param root.
	 */
	public void testSecureParamGetRoot() throws Exception {
		this.secureService("GET", "{path}", MockParamSection.class, "/", "/");
	}

	/**
	 * Ensure sends 404 if param but not match on method.
	 */
	public void testParamNotMatchMethod() throws Exception {
		MockHttpResponse response = this.service("GET", "{path}", MockParamSection.class,
				this.mockRequest("/").method(HttpMethod.POST).header("accept", "text/html"));
		response.assertResponse(404, "No resource found for " + this.contextUrl("", "/"));
	}

	/**
	 * Ensure sends 404 if resource not found.
	 */
	public void testResourceNotFound() throws Exception {
		this.compile.web(null);
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest().header("accept", "text/html"));
		response.assertResponse(404, "No resource found for /");
	}

	/**
	 * Ensure able to POST root.
	 */
	public void testPostRoot() throws Exception {
		MockHttpResponse response = this.service("POST", "/", MockSection.class,
				this.mockRequest("/").method(HttpMethod.POST));
		response.assertResponse(200, "TEST");
	}

	/**
	 * Ensure able to POST secure root.
	 */
	public void testSecurePostRoot() throws Exception {
		this.secureService("POST", "/", MockSection.class, "/", "TEST");
	}

	/**
	 * Ensure appropriately indicates {@link HttpMethod} not allowed.
	 */
	public void testPostNotAllowed() throws Exception {
		MockHttpResponse response = this.service("GET", "/", MockSection.class,
				this.mockRequest("/").method(HttpMethod.POST));
		response.assertResponse(405, "", "Allow", "GET, HEAD, OPTIONS");
	}

	/**
	 * Ensure able to obtain resource at path.
	 */
	public void testPath() throws Exception {
		MockHttpResponse response = this.service("GET", "/path/to/resource", MockSection.class,
				this.mockRequest("/path/to/resource"));
		response.assertResponse(200, "TEST");
	}

	/**
	 * Ensure redirect on secure path.
	 */
	public void testSecurePath() throws Exception {
		this.secureService("GET", "/path/to/resource", MockSection.class, "/path/to/resource", "TEST");
	}

	/**
	 * Ensure able to provide parameter via path.
	 */
	public void testPathParameter() throws Exception {
		MockHttpResponse response = this.service("GET", "/path/{param}", MockPathParameter.class,
				this.mockRequest("/path/value"));
		response.assertResponse(200, "Parameter=value");
	}

	@Data
	@HttpParameters
	public static class PathParameter implements Serializable {
		private static final long serialVersionUID = 1L;

		@HttpPathParameter("") // default to field property name
		protected String param;
	}

	public static class MockPathParameter {
		public void service(PathParameter param, ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("Parameter=" + param.param);
		}
	}

	/**
	 * Ensure able to provide value via path.
	 */
	public void testPathValue() throws Exception {
		MockHttpResponse response = this.service("GET", "/path/{param}/{two}", MockPathValue.class,
				this.mockRequest("/path/value/2"));
		response.assertResponse(200, "Value=value");
	}

	public static class MockPathValue {
		public void service(@HttpPathParameter("param") String param, ServerHttpConnection connection)
				throws IOException {
			connection.getResponse().getEntityWriter().write("Value=" + param);
		}
	}

	/**
	 * Ensure able to provide value via secure path.
	 */
	public void testSecurePathValue() throws Exception {
		this.secureService("GET", "/path/{param}/{two}", MockPathValue.class, "/path/value/2", "Value=value");
	}

	/**
	 * Ensure can have multiple parameters on the path.
	 */
	public void testPathMultipleParameters() throws Exception {
		MockHttpResponse response = this.service("GET", "/path/with/first-{param}/and/{second}/param",
				MockMultipleParameters.class, this.mockRequest("/path/with/first-one/and/two/param"));
		response.assertResponse(200, "One=one and Two=two");
	}

	@HttpParameters
	public static class MultipleParameters extends PathParameter {
		private static final long serialVersionUID = 1L;

		private String second;

		// No path annotation, so will take from anywhere
		public void setSecond(String second) {
			this.second = second;
		}
	}

	public static class MockMultipleParameters {
		public void service(ServerHttpConnection connection, MultipleParameters params) throws IOException {
			connection.getResponse().getEntityWriter().write("One=" + params.param + " and Two=" + params.second);
		}
	}

	/**
	 * Ensure can have multiple values on the path.
	 */
	public void testPathMultipleValues() throws Exception {
		MockHttpResponse response = this.service("GET", "/path/with/first-{param}/and/{second}/param",
				MockMultipleValues.class, this.mockRequest("/path/with/first-one/and/two/param"));
		assertEquals("Incorrect status", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect response", "One=one and Two=two", response.getEntity(null));
	}

	public static class MockMultipleValues {
		public void service(@HttpPathParameter("second") String second, ServerHttpConnection connection,
				@HttpPathParameter("param") String param) throws IOException {
			connection.getResponse().getEntityWriter().write("One=" + param + " and Two=" + second);
		}
	}

	/**
	 * Ensure able to provide parameter via query string.
	 */
	public void testQueryParameter() throws Exception {
		MockHttpResponse response = this.service("GET", "/path", MockQueryParameter.class,
				this.mockRequest("/path?param=value"));
		response.assertResponse(200, "Parameter=value");
	}

	@HttpParameters
	public static class QueryParameter implements Serializable {
		private static final long serialVersionUID = 1L;

		protected String param;

		@HttpQueryParameter("") // default to method property name
		public void setParam(String param) {
			this.param = param;
		}
	}

	public static class MockQueryParameter {
		public void service(QueryParameter param, ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("Parameter=" + param.param);
		}
	}

	/**
	 * Ensure able to provide value via query string.
	 */
	public void testQueryValue() throws Exception {
		MockHttpResponse response = this.service("GET", "/path", MockQueryValue.class,
				this.mockRequest("/path?param=value"));
		response.assertResponse(200, "Value=value");
	}

	public static class MockQueryValue {
		public void service(@HttpQueryParameter("param") String param, ServerHttpConnection connection)
				throws IOException {
			connection.getResponse().getEntityWriter().write("Value=" + param);
		}
	}

	/**
	 * Ensure able to provide value via {@link HttpHeader}.
	 */
	public void testHeaderValue() throws Exception {
		MockHttpResponse response = this.service("GET", "/path", MockHeaderValue.class,
				this.mockRequest("/path").header("x-test", "value"));
		response.assertResponse(200, "Value=value");
	}

	public static class MockHeaderValue {
		public void service(@HttpHeaderParameter("x-test") String param, ServerHttpConnection connection)
				throws IOException {
			connection.getResponse().getEntityWriter().write("Value=" + param);
		}
	}

	/**
	 * Ensure able to provide value via {@link HttpCookie}.
	 */
	public void testCookieValue() throws Exception {
		MockHttpResponse response = this.service("GET", "/path", MockCookieValue.class,
				this.mockRequest("/path").header("cookie", new HttpCookie("param", "value").toString()));
		response.assertResponse(200, "Value=value");
	}

	public static class MockCookieValue {
		public void service(@HttpCookieParameter("param") String param, ServerHttpConnection connection)
				throws IOException {
			connection.getResponse().getEntityWriter().write("Value=" + param);
		}
	}

	/**
	 * Ensure able to provide form parameter.
	 */
	public void testFormParameter() throws Exception {
		MockHttpResponse response = this.service("POST", "/path", MockFormParameter.class,
				this.mockRequest("/path").method(HttpMethod.POST)
						.header("Content-Type", "application/x-www-form-urlencoded").entity("param=value"));
		response.assertResponse(200, "Parameter=value");
	}

	@HttpParameters
	public static class FormParameter implements Serializable {
		private static final long serialVersionUID = 1L;

		protected String param;

		public void setParam(String param) {
			this.param = param;
		}
	}

	public static class MockFormParameter {
		public void service(FormParameter param, ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("Parameter=" + param.param);
		}
	}

	/**
	 * Ensure able to provide form value.
	 */
	public void testFormValue() throws Exception {
		MockHttpResponse response = this.service("POST", "/path", MockFormValue.class,
				this.mockRequest("/path").method(HttpMethod.POST)
						.header("Content-Type", "application/x-www-form-urlencoded").entity("param=value"));
		response.assertResponse(200, "Parameter=value");
	}

	public static class MockFormValue {
		public void service(@HttpContentParameter("param") String param, ServerHttpConnection connection)
				throws IOException {
			connection.getResponse().getEntityWriter().write("Parameter=" + param);
		}
	}

	/**
	 * Ensure able to construct static path for {@link HttpInput}.
	 */
	public void testConstructStaticPath() throws Exception {

		// Configure the server
		Closure<HttpInputPath> input = new Closure<>();
		this.compile.web((context) -> {
			input.value = context.link(false, "/static/path", MockSection.class).getPath();
		});
		this.compile.compileAndOpenOfficeFloor();

		// Ensure construct static path
		assertEquals("Incorrect path with no values", this.contextUrl("", "/static/path"),
				input.value.createHttpPathFactory(null).createApplicationClientPath(null));
		assertEquals("Incorrect path ignoring values", this.contextUrl("", "/static/path"),
				input.value.createHttpPathFactory(PathValues.class).createApplicationClientPath(new PathValues()));

		// Ensure indicate if match on paths
		assertFalse("Should not match root", input.value.isMatchPath(this.contextUrl("", "/"), -1));
		assertFalse("Should not match partial path", input.value.isMatchPath(this.contextUrl("", "/static"), -1));
		assertTrue("Should match same static path", input.value.isMatchPath(this.contextUrl("", "/static/path"), -1));
		assertTrue("Should ignore terminating character on static path",
				input.value.isMatchPath(this.contextUrl("", "/static/path"), '/'));
		assertFalse("Should not match with longer path",
				input.value.isMatchPath(this.contextUrl("", "/static/path/extra"), -1));
	}

	public static class PathValues {
		public String getParam() {
			return "value";
		}
	}

	/**
	 * Ensure able to construct dynamic path for {@link HttpInput}.
	 */
	public void testConstructDynamicPath() throws Exception {

		// Configure the server
		Closure<HttpInputPath> input = new Closure<>();
		this.compile.web((context) -> {
			input.value = context.link(false, "/dynamic/{param}", MockSection.class).getPath();
		});
		this.compile.compileAndOpenOfficeFloor();

		// Ensure construct dynamic path
		HttpPathFactory<PathValues> pathFactory = input.value.createHttpPathFactory(PathValues.class);
		String path = pathFactory.createApplicationClientPath(new PathValues());
		assertEquals("Incorrect path ignoring values", this.contextUrl("", "/dynamic/value"), path);

		// Ensure not able to construct path missing values
		try {
			input.value.createHttpPathFactory(Exception.class);
			fail("Should not be successful");
		} catch (HttpException ex) {
			assertEquals("Incorrect cause",
					"For path '/dynamic/{param}', no property 'param' on object " + Exception.class.getName(),
					ex.getEntity());
		}

		// Ensure indicate if match on paths
		assertFalse("Should not match root", input.value.isMatchPath(this.contextUrl("", "/"), -1));
		assertFalse("Should not match partial path", input.value.isMatchPath(this.contextUrl("", "/dynamic"), -1));
		assertTrue("Should match same dynamic path",
				input.value.isMatchPath(this.contextUrl("", "/dynamic/value"), -1));
		assertTrue("Should match same dynamic path (without terminating character)",
				input.value.isMatchPath(this.contextUrl("", "/dynamic/value"), '+'));
		assertTrue("Should match same dynamic path (consuming rest of path)",
				input.value.isMatchPath(this.contextUrl("", "/dynamic/value+link"), -1));
		assertFalse("Should not match with longer path",
				input.value.isMatchPath(this.contextUrl("", "/dynamic/value+link"), '+'));
	}

	/**
	 * Ensure able to provide HTTP argument.
	 */
	public void testHttpArgument() throws Exception {

		// Configure the server
		this.compile.web((context) -> {
			context.getWebArchitect().addHttpArgument("param", HttpValueLocation.QUERY);
			context.link(false, "GET", "/", HttpArgumentSection.class);
		});
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Send the request
		MockHttpResponse response = this.server.send(this.mockRequest("/?param=value"));
		response.assertResponse(200, "Argument=value");
	}

	public static class HttpArgumentSection {
		public void service(String argument, ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("Argument=" + argument);
		}
	}

	/**
	 * Ensure able to parse out the HTTP object.
	 */
	public void testHttpObject() throws Exception {

		// Configure the server
		this.compile.web((context) -> {
			context.getWebArchitect().addHttpObjectParser(new ObjectValueFactory());
			context.link(false, "POST", "/path", MockObjectValue.class);
		});
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Send the request
		MockHttpResponse response = this.server.send(this.mockRequest("/path").method(HttpMethod.POST)
				.header("Content-Type", "application/mock").entity("value"));
		response.assertResponse(200, "Value=value");
	}

	@HttpObject
	public static class ObjectValue {
		private final String content;

		public ObjectValue(String content) {
			this.content = content;
		}
	}

	public static class ObjectValueFactory implements HttpObjectParserFactory, HttpObjectParser<ObjectValue> {

		/*
		 * =================== HttpObjectParserFactory =====================
		 */

		@Override
		public String getContentType() {
			return "application/mock";
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> HttpObjectParser<T> createHttpObjectParser(Class<T> objectType) {
			return (HttpObjectParser<T>) this;
		}

		/*
		 * ==================== HttpObjectParser ===========================
		 */

		@Override
		public Class<ObjectValue> getObjectType() {
			return ObjectValue.class;
		}

		@Override
		public ObjectValue parse(ServerHttpConnection connection) throws HttpException {
			String content = EntityUtil.toString(connection.getRequest(), null);
			return new ObjectValue(content);
		}
	}

	public static class MockObjectValue {
		public void service(ObjectValue object, ServerHttpConnection connection) throws Exception {
			connection.getResponse().getEntityWriter().write("Value=" + object.content);
		}
	}

	/**
	 * Ensure able to register default {@link HttpObjectParserServiceFactory}.
	 */
	public void testDefaultHttpObjectParser() throws Exception {

		// Configure the server
		this.compile.web((context) -> {
			context.getWebArchitect().setDefaultHttpObjectParser(new ObjectValueServiceFactory());
			context.link(false, "POST", "/path", MockObjectValue.class);
		});
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Send the request
		MockHttpResponse response = this.server.send(this.mockRequest("/path").method(HttpMethod.POST)
				.header("Content-Type", "application/mock").entity("value"));
		response.assertResponse(200, "Value=value");
	}

	public static class ObjectValueServiceFactory implements HttpObjectParserServiceFactory {

		@Override
		public HttpObjectParserFactory createService(ServiceContext context) throws Throwable {
			return new ObjectValueFactory();
		}
	}

	/**
	 * Code generators are likely to create objects with different
	 * {@link Annotation} values. Therefore, rather than force the code generators
	 * to include {@link HttpObject}, another {@link Annotation} can be used to
	 * alias the {@link HttpObject}.
	 */
	public void testHttpObjectAlias() throws Exception {

		// Configure the server
		this.compile.web((context) -> {
			WebArchitect web = context.getWebArchitect();
			web.addHttpObjectParser(new ObjectAliasFactory());
			web.addHttpObjectAnnotationAlias(MockAlias.class, "application/alias");
			context.link(false, "POST", "/path", MockObjectAlias.class);
		});
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Send the request
		MockHttpResponse response = this.server.send(this.mockRequest("/path").method(HttpMethod.POST)
				.header("Content-Type", "application/alias").entity("value"));
		response.assertResponse(200, "Value=value");
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public static @interface MockAlias {
	}

	@MockAlias
	public static class ObjectAlias {
		private final String content;

		public ObjectAlias(String content) {
			this.content = content;
		}
	}

	public static class ObjectAliasFactory implements HttpObjectParserFactory, HttpObjectParser<ObjectAlias> {

		/*
		 * =================== HttpObjectParserFactory =====================
		 */

		@Override
		public String getContentType() {
			return "application/alias";
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> HttpObjectParser<T> createHttpObjectParser(Class<T> objectType) {
			return (HttpObjectParser<T>) this;
		}

		/*
		 * ==================== HttpObjectParser ===========================
		 */

		@Override
		public Class<ObjectAlias> getObjectType() {
			return ObjectAlias.class;
		}

		@Override
		public ObjectAlias parse(ServerHttpConnection connection) throws HttpException {
			String content = EntityUtil.toString(connection.getRequest(), null);
			return new ObjectAlias(content);
		}
	}

	public static class MockObjectAlias {
		public void service(ObjectAlias object, ServerHttpConnection connection) throws Exception {
			connection.getResponse().getEntityWriter().write("Value=" + object.content);
		}
	}

	/**
	 * Ensure can register {@link HttpObjectParserFactory}.
	 */
	public void testRegisteredHttpObject() throws Exception {

		// Configure the server
		this.compile.web((context) -> {
			context.getWebArchitect();
			context.link(false, "POST", "/path", MockRegisteredObject.class);
		});
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Send the request
		MockHttpResponse response = this.server.send(this.mockRequest("/path").method(HttpMethod.POST)
				.header("Content-Type", "registered/object").entity("value"));
		response.assertResponse(200, "Value=value");
	}

	@HttpObject
	public static class RegisteredObject {
		private final String content;

		public RegisteredObject(String content) {
			this.content = content;
		}
	}

	public static class MockRegisteredObject {
		public void service(RegisteredObject object, ServerHttpConnection connection) throws Exception {
			connection.getResponse().getEntityWriter().write("Value=" + object.content);
		}
	}

	/**
	 * Ensure can send object.
	 */
	public void testResponseObject() throws Exception {

		// Configure the server
		this.compile.web((context) -> {
			context.link(false, "GET", "/path/{param}", MockObjectSection.class);
			context.getWebArchitect().addHttpObjectResponder(new MockObjectResponderFactory());
		});
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Send request
		MockHttpResponse response = this.server
				.send(this.mockRequest("/path/value").header("accept", "application/mock"));
		response.assertResponse(200, "{value=\"OBJECT value\"}", "content-type", "application/mock");
	}

	public static class MockObjectSection {
		public void service(PathParameter param, ObjectResponse<String> response) {
			response.send("OBJECT " + param.param);
		}
	}

	public static class MockObjectResponderFactory implements HttpObjectResponderFactory {

		@Override
		public String getContentType() {
			return "application/mock";
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> HttpObjectResponder<T> createHttpObjectResponder(Class<T> objectType) {
			return (HttpObjectResponder<T>) new StringObjectResponder();
		}

		@Override
		@SuppressWarnings("unchecked")
		public <E extends Throwable> HttpObjectResponder<E> createHttpEscalationResponder(Class<E> escalationType) {
			return (HttpObjectResponder<E>) new EscalationObjectResponder();
		}
	}

	public static class StringObjectResponder implements HttpObjectResponder<String> {

		@Override
		public String getContentType() {
			return "application/mock";
		}

		@Override
		public Class<String> getObjectType() {
			return String.class;
		}

		@Override
		public void send(String object, ServerHttpConnection connection) throws IOException {
			HttpResponse response = connection.getResponse();
			response.setContentType(this.getContentType(), null);
			response.getEntityWriter().write("{value=\"" + object + "\"}");
		}
	}

	public static class EscalationObjectResponder implements HttpObjectResponder<Throwable> {

		@Override
		public String getContentType() {
			return "application/mock";
		}

		@Override
		public Class<Throwable> getObjectType() {
			return Throwable.class;
		}

		@Override
		public void send(Throwable object, ServerHttpConnection connection) throws IOException {
			HttpResponse response = connection.getResponse();
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			response.setContentType(this.getContentType(), null);
			response.getEntityWriter().write("{error: \"" + object.getMessage() + "\"}");
		}
	}

	/**
	 * Ensure can different status for {@link ObjectResponse}.
	 */
	public void testStatusForObjectResponse() throws Exception {

		// Configure the server
		this.compile.web((context) -> {
			context.link(false, "GET", "/path/{param}", StatusForObjectResponseSection.class);
			context.getWebArchitect().addHttpObjectResponder(new MockObjectResponderFactory());
		});
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Send request
		MockHttpResponse response = this.server
				.send(this.mockRequest("/path/create").header("accept", "application/mock"));
		response.assertResponse(201, "{value=\"OBJECT create\"}", "content-type", "application/mock");
	}

	public static class StatusForObjectResponseSection {
		public void service(PathParameter param,
				@net.officefloor.web.HttpResponse(status = 201) ObjectResponse<String> response) {
			response.send("OBJECT " + param.param);
		}
	}

	/**
	 * Ensure can send escalation.
	 */
	public void testResponseObjectEscalation() throws Exception {

		// Configure the server
		this.compile.web((context) -> {
			context.link(false, "GET", "/path", MockEscalate.class);
			context.getWebArchitect().addHttpObjectResponder(new MockObjectResponderFactory());
		});
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Send request
		MockHttpResponse response = this.server.send(this.mockRequest("/path").header("accept", "application/mock"));
		response.assertResponse(500, "{error: \"TEST ESCALATION\"}", "content-type", "application/mock");
	}

	public static class MockEscalate {
		public void service() throws Exception {
			throw new Exception("TEST ESCALATION");
		}
	}

	/**
	 * Ensure can register default {@link MockRegisterObjectResponderService}.
	 */
	public void testDefaultObjectResponder() throws Exception {

		// Flag to not register
		MockHttpObjectResponderServiceFactory.isInclude = false;
		try {

			// Configure the server
			this.compile.web((context) -> {
				context.link(false, "GET", "/path/{param}", MockObjectSection.class);
				context.getWebArchitect().setDefaultHttpObjectResponder(new MockObjectResponderServiceFactory());
			});
			this.officeFloor = this.compile.compileAndOpenOfficeFloor();

			// Send request
			MockHttpResponse response = this.server
					.send(this.mockRequest("/path/value").header("accept", "application/mock"));
			response.assertResponse(200, "{value=\"OBJECT value\"}", "content-type", "application/mock");

		} finally {
			MockHttpObjectResponderServiceFactory.isInclude = true;
		}
	}

	public static class MockObjectResponderServiceFactory implements HttpObjectResponderServiceFactory {

		@Override
		public HttpObjectResponderFactory createService(ServiceContext context) throws Throwable {
			return new MockObjectResponderFactory();
		}
	}

	/**
	 * Ensure can register {@link HttpObjectResponderFactory} instances as services.
	 */
	public void testRegisterObjectResponderService() throws Exception {

		// Configure the server
		this.compile.web((context) -> {
			context.link(false, "/path", MockRegisterObjectResponderService.class);
		});
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Send request
		MockHttpResponse response = this.server.send(this.mockRequest("/path").header("accept", "registered/response"));
		response.assertResponse(200, "{ registered: REGISTERED }", "content-type", "registered/response");
	}

	public static class MockRegisterObjectResponderService {
		public void service(ObjectResponse<RegisteredResponse> response) {
			response.send(new RegisteredResponse("REGISTERED"));
		}
	}

	public static class RegisteredResponse {
		private String content;

		public RegisteredResponse(String content) {
			this.content = content;
		}

		public String getContent() {
			return this.content;
		}
	}

	/**
	 * Ensure can register {@link HttpObjectResponderFactory} for handling
	 * exception.
	 */
	public void testRegisterEscalationResponderService() throws Exception {

		// Configure the server
		this.compile.web((context) -> {
			context.link(false, "/path", MockRegisterEscalationResponderService.class);
		});
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Send request
		MockHttpResponse response = this.server.send(this.mockRequest("/path").header("accept", "registered/response"));
		response.assertResponse(500, "{ registeredError: REGISTERED }", "content-type", "registered/response");
	}

	public static class MockRegisterEscalationResponderService {
		public void service() throws Exception {
			throw new Exception("REGISTERED");
		}
	}

	/**
	 * Ensure can store state within application.
	 */
	public void testApplicationState() throws Exception {

		// Configure the server
		this.compile.web((context) -> {
			OfficeSection servicer = context.addSection("SECTION", MockApplication.class);
			OfficeArchitect office = context.getOfficeArchitect();
			WebArchitect web = context.getWebArchitect();
			office.link(web.getHttpInput(false, "POST", "/path").getInput(), servicer.getOfficeSectionInput("post"));
			office.link(web.getHttpInput(false, "/path").getInput(), servicer.getOfficeSectionInput("get"));
		});
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Send request to store state in application state
		MockHttpResponse response = this.server.send(this.mockRequest("/path?param=value").method(HttpMethod.POST));
		response.assertResponse(200, "post=value");

		// Obtain value from application state
		response = this.server.send(this.mockRequest("/path"));
		response.assertResponse(200, "get=value");
	}

	@HttpApplicationStateful
	public static class ApplicationObject {
		private String value = null;
	}

	public static class MockApplication {
		public void post(QueryParameter parameter, ServerHttpConnection connection, ApplicationObject object)
				throws IOException {
			object.value = parameter.param;
			connection.getResponse().getEntityWriter().write("post=" + object.value);
		}

		public void get(ServerHttpConnection connection, ApplicationObject object) throws IOException {
			connection.getResponse().getEntityWriter().write("get=" + object.value);
		}
	}

	/**
	 * Ensure can store state within {@link HttpSession}.
	 */
	public void testSession() throws Exception {

		// Configure the server
		this.compile.web((context) -> {
			OfficeSection servicer = context.addSection("SECTION", MockSession.class);
			OfficeArchitect office = context.getOfficeArchitect();
			WebArchitect web = context.getWebArchitect();
			office.link(web.getHttpInput(false, "POST", "/path").getInput(), servicer.getOfficeSectionInput("post"));
			office.link(web.getHttpInput(false, "/path").getInput(), servicer.getOfficeSectionInput("get"));
		});
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Send request to store state in session
		MockHttpResponse response = this.server.send(this.mockRequest("/path?param=value").method(HttpMethod.POST));
		response.assertResponse(200, "post=value");

		// Obtain value from session
		response = this.server.send(this.mockRequest("/path").cookies(response));
		response.assertResponse(200, "get=value");
	}

	@HttpSessionStateful
	public static class SessionObject implements Serializable {
		private static final long serialVersionUID = 1L;

		private String value = null;
	}

	public static class MockSession {
		public void post(ServerHttpConnection connection, SessionObject object, QueryParameter parameter)
				throws IOException {
			object.value = parameter.param;
			connection.getResponse().getEntityWriter().write("post=" + object.value);
		}

		public void get(ServerHttpConnection connection, SessionObject object) throws IOException {
			connection.getResponse().getEntityWriter().write("get=" + object.value);
		}
	}

	/**
	 * Ensure can redirect (remembering original request).
	 */
	public void testRedirect() throws Exception {

		// Configure the server
		this.compile.web((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();
			WebArchitect web = context.getWebArchitect();

			// Provide continuation
			HttpUrlContinuation continuation = context.link(false, "/path", MockQueryParameter.class);

			// Configure to redirect to continuation
			OfficeSection section = context.addSection("REDIRECT", MockRedirect.class);
			office.link(web.getHttpInput(false, "POST", "/redirect").getInput(),
					section.getOfficeSectionInput("service"));
			office.link(section.getOfficeSectionOutput("redirect"), continuation.getRedirect(null));
		});
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Obtain the expected serialisable identifier
		int nextSerialisableIdentifier = new SerialisedRequestState(null).identifier + 1;

		// Send request that provides redirect
		MockHttpResponse response = this.server.send(this.mockRequest("/redirect?param=value").method(HttpMethod.POST));
		response.assertResponse(303, "", "location", this.contextUrl("", "/path"));
		response.assertCookie(MockHttpServer.mockResponseCookie("ofr", String.valueOf(nextSerialisableIdentifier))
				.setPath(this.contextUrl("", "/path")).setHttpOnly(true));

		// Ensure can redirect
		MockHttpRequestBuilder redirectRequest = this.mockRequest("/path").cookies(response);
		response = this.server.send(redirectRequest);
		response.assertResponse(200, "Parameter=value");
	}

	/**
	 * Ensure can redirect (remembering original request).
	 */
	public void testRedirectToSecurePath() throws Exception {

		// Configure the server
		this.compile.web((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();
			WebArchitect web = context.getWebArchitect();

			// Provide continuation
			HttpUrlContinuation continuation = context.link(true, "/path", MockQueryParameter.class);

			// Configure to redirect to continuation
			OfficeSection section = context.addSection("REDIRECT", MockRedirect.class);
			office.link(web.getHttpInput(false, "POST", "/redirect").getInput(),
					section.getOfficeSectionInput("service"));
			office.link(section.getOfficeSectionOutput("redirect"), continuation.getRedirect(null));
		});
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Obtain the expected serialisable identifier
		int nextSerialisableIdentifier = new SerialisedRequestState(null).identifier + 1;

		// Send request that provides redirect (to secure URL)
		MockHttpResponse response = this.server.send(this.mockRequest("/redirect?param=value").method(HttpMethod.POST));
		response.assertResponse(303, "", "location", this.contextUrl("https://mock.officefloor.net", "/path"));
		response.assertCookie(MockHttpServer.mockResponseCookie("ofr", String.valueOf(nextSerialisableIdentifier))
				.setPath(this.contextUrl("", "/path")).setSecure(true).setHttpOnly(true));

		// Ensure can redirect
		MockHttpRequestBuilder redirectRequest = this.mockRequest("/path").secure(true).cookies(response);
		response = this.server.send(redirectRequest);
		response.assertResponse(200, "Parameter=value");
	}

	public static class MockRedirect implements MockPathParameters {
		@Next("redirect")
		public MockPathParameters service() {
			return this;
		}

		@Override
		public String getParam() {
			return "value";
		}
	}

	public static interface MockPathParameters {
		String getParam();
	}

	/**
	 * Ensure can redirect to a path containing parameters.
	 */
	public void testRedirectWithPathParameters() throws Exception {

		// Configure the server
		this.compile.web((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();

			// Provide continuation
			HttpUrlContinuation continuation = context.link(false, "/path/{param}", MockQueryParameter.class);

			// Configure to redirect to continuation
			OfficeSection section = context.addSection("REDIRECT", MockRedirect.class);
			office.link(context.getWebArchitect().getHttpInput(false, "POST", "/redirect").getInput(),
					section.getOfficeSectionInput("service"));
			office.link(section.getOfficeSectionOutput("redirect"),
					continuation.getRedirect(MockPathParameters.class.getName()));
		});
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Send request that provides redirect
		MockHttpResponse response = this.server.send(this.mockRequest("/redirect?param=value").method(HttpMethod.POST));
		response.assertResponse(303, "", "location", this.contextUrl("", "/path/value"));

		// Ensure can redirect
		response = this.server.send(this.mockRequest("/path/value").cookies(response));
		response.assertResponse(200, "Parameter=value");
	}

	/**
	 * Ensure can re-route.
	 */
	public void testReroute() throws Exception {

		// Configure the server
		this.compile.web((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();
			WebArchitect web = context.getWebArchitect();

			// Configure section and its state
			OfficeSection reroute = context.addSection("REROUTE", RerouteSection.class);
			context.addManagedObject("REROUTE_STATE", RerouteState.class, ManagedObjectScope.PROCESS);

			// Configure reroute
			web.reroute(reroute.getOfficeSectionOutput("routeAgain"));

			// Provide HTTP input to service
			office.link(web.getHttpInput(false, "/reroute").getInput(), reroute.getOfficeSectionInput("reroute"));
		});
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Send request that should re-route multiple times
		MockHttpResponse response = this.server.send(this.mockRequest("/reroute"));
		response.assertResponse(200, "10");
	}

	public static class RerouteState {
		private int iteration = 0;
	}

	@FlowInterface
	public static interface RerouteFlows {
		void routeAgain();
	}

	public static class RerouteSection {
		public void reroute(RerouteState state, RerouteFlows flows, ServerHttpConnection connection)
				throws IOException {
			state.iteration++;
			if (state.iteration < 10) {
				flows.routeAgain();
			} else {
				connection.getResponse().getEntityWriter().write(String.valueOf(state.iteration));
			}
		}
	}

	/**
	 * Ensure can intercept before servicing.
	 */
	public void testIntercept() throws Exception {

		// Configure the server
		this.compile.web((context) -> {
			// Configure handling
			context.link(false, "/path", MockSection.class);

			// Configure intercepting
			OfficeSection intercept = context.addSection("INTERCEPT", MockIntercept.class);
			WebArchitect web = context.getWebArchitect();
			web.intercept(intercept.getOfficeSectionInput("intercept"), intercept.getOfficeSectionOutput("service"));
		});
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Send request and confirm it is intercepted
		MockHttpResponse response = this.server.send(this.mockRequest("/path"));
		response.assertResponse(200, "intercepted TEST");
	}

	public static class MockIntercept {
		@Next("service")
		public void intercept(ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("intercepted ");
		}
	}

	/**
	 * Ensure an load intercept via service.
	 */
	public void testInterceptService() throws Exception {
		this.doInterceptService(MockIntercept.class, "intercepted TEST");
	}

	/**
	 * Ensure issue if multiple inputs for intercepter.
	 */
	public void testInterceptService_multipleInputs() throws Exception {
		MockCompilerIssues issues = new MockCompilerIssues(this);
		issues.recordCaptureIssues(false);
		issues.recordCaptureIssues(false);
		issues.recordCaptureIssues(false);
		issues.recordIssue("OFFICE", OfficeNodeImpl.class, "Web intercept " + MockMultipleInputIntercept.class.getName()
				+ " must only have one input (inputs: interceptOne interceptTwo)");
		issues.recordCaptureIssues(false);
		issues.recordCaptureIssues(false);
		this.compile.getOfficeFloorCompiler().setCompilerIssues(issues);
		this.replayMockObjects();
		this.doInterceptService(MockMultipleInputIntercept.class, null);
		this.verifyMockObjects();
	}

	public static class MockMultipleInputIntercept {
		@Next("output")
		public void interceptOne() {
			// Testing error
		}

		@Next("output")
		public void interceptTwo() {
			// Testing error
		}
	}

	/**
	 * Ensure issue if multiple outputs for intercepter.
	 */
	public void testInterceptService_multipleOutputs() throws Exception {
		MockCompilerIssues issues = new MockCompilerIssues(this);
		issues.recordCaptureIssues(false);
		issues.recordCaptureIssues(false);
		issues.recordCaptureIssues(false);
		issues.recordIssue("OFFICE", OfficeNodeImpl.class,
				"Web intercept " + MockMultipleOutputIntercept.class.getName()
						+ " must only have one output (outputs: outputOne outputTwo)");
		issues.recordCaptureIssues(false);
		issues.recordCaptureIssues(false);
		this.compile.getOfficeFloorCompiler().setCompilerIssues(issues);
		this.replayMockObjects();
		this.doInterceptService(MockMultipleOutputIntercept.class, null);
		this.verifyMockObjects();
	}

	@FlowInterface
	public static interface MockMultipleOutputFlows {
		void outputOne();

		void outputTwo();
	}

	public static class MockMultipleOutputIntercept {
		public void intercept(MockMultipleOutputFlows flows) {
			// Testing error
		}
	}

	/**
	 * Undertakes {@link WebInterceptServiceFactory} test.
	 * 
	 * @param interceptClass Intercept {@link Class}.
	 * @param expectedEntity Expected entity.
	 */
	private void doInterceptService(Class<?> interceptClass, String expectedEntity) throws Exception {

		// Configure the intercept
		MockWebInterceptServiceFactory.interceptor = interceptClass;
		try {

			// Configure the server
			// (will register interceptor via service)
			this.compile.web((context) -> {
				context.link(false, "/path", MockSection.class);
			});
			this.officeFloor = this.compile.compileAndOpenOfficeFloor();

			// Send request and confirm it is intercepted
			if (expectedEntity != null) {
				MockHttpResponse response = this.server.send(this.mockRequest("/path"));
				response.assertResponse(200, expectedEntity);
			}

		} finally {
			// Clear interceptor
			MockWebInterceptServiceFactory.interceptor = null;
		}
	}

	/**
	 * Ensure can chain servicer.
	 */
	public void testChainedServicer() throws Exception {

		// Configure the server
		this.compile.web((context) -> {
			OfficeSection section = context.addSection("SECTION", MockChainedServicer.class);
			WebArchitect web = context.getWebArchitect();
			web.chainServicer(section.getOfficeSectionInput("input"), null);
		});
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Send request to be serviced by chained servicer
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest());
		assertEquals("Should be serviced", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect response", "chained", response.getEntity(null));
	}

	public static class MockChainedServicer {
		public void input(ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("chained");
		}
	}

	/**
	 * Ensure can service by appropriate chained servicer.
	 */
	public void testSecondChainedServicer() throws Exception {

		// Configure the server
		this.compile.web((context) -> {
			OfficeSection pass = context.addSection("PASS", MockPassThroughChainedServicer.class);
			OfficeSection section = context.addSection("SECTION", MockChainedServicer.class);
			WebArchitect web = context.getWebArchitect();
			web.chainServicer(pass.getOfficeSectionInput("pass"), pass.getOfficeSectionOutput("chain"));
			web.chainServicer(section.getOfficeSectionInput("input"), null);
		});
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Send request to be serviced by chained servicer
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest());
		assertEquals("Should be serviced", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect response", "pass - chained", response.getEntity(null));
	}

	public static class MockPassThroughChainedServicer {
		@Next("chain")
		public void pass(ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("pass - ");
		}
	}

	/**
	 * Ensure can explore handling of {@link HttpInput}.
	 */
	public void testHttpInputExplorer() throws Exception {

		// Capture the inputs that can be explored
		Map<String, HttpInputExplorerContext> inputs = new HashMap<>();

		// Capture the factories
		Closure<HttpObjectParserFactory[]> parserFactories = new Closure<>();
		Closure<HttpObjectResponderFactory[]> responderFactories = new Closure<>();

		// Configure the server
		this.compile.web((context) -> {

			// Provide some inputs to explore
			context.link(false, "/one", HttpInputExploreOneSection.class)
					.setDocumentation("HTTP Continuation description");
			context.link(true, "POST", "/two", HttpInputExploreTwoSection.class)
					.setDocumentation("HTTP Input description");

			// Capture exploring
			context.getWebArchitect().addHttpInputExplorer((explore) -> {
				inputs.put(explore.getRoutePath(), explore);
				parserFactories.value = explore.getHttpObjectParserFactories();
				responderFactories.value = explore.getHttpObjectResponderFactories();
			});

			// Provide dependencies
			Singleton.load(context.getOfficeArchitect(), "TEST");
			Singleton.load(context.getOfficeArchitect(), Integer.valueOf(1));
		});
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Ensure each input is able to be explored
		this.assertExploredHttpInput(inputs.get("/one"), false, HttpMethod.GET, "/one", "GET_/one.service",
				String.class, "HTTP Continuation description");
		this.assertExploredHttpInput(inputs.get("/two"), true, HttpMethod.POST, "/two", "POST_/two.service",
				Integer.class, "HTTP Input description");
		assertEquals("Should only be two inputs", 2, inputs.size());

		// Ensure correct factories
		assertEquals("Incorrect number of parser factories", 1, parserFactories.value.length);
		assertEquals("Incorrect parser factory", "registered/object", parserFactories.value[0].getContentType());
		assertEquals("Incorrect number of responder factories", 1, responderFactories.value.length);
		assertEquals("Incorrect responder factory", "registered/response",
				responderFactories.value[0].getContentType());
	}

	private void assertExploredHttpInput(HttpInputExplorerContext context, boolean isSecure, HttpMethod httpMethod,
			String routePath, String functionName, Class<?> objectType, String documentation) {
		String suffix = " (" + routePath + ")";
		assertNotNull("No input explored" + suffix, context);
		assertEquals("Incorrect secure" + suffix, isSecure, context.isSecure());
		assertEquals("Incorrect HTTP method" + suffix, httpMethod, context.getHttpMethod());
		assertEquals("Incorrect context path", this.contextPath, context.getContextPath());
		assertEquals("Incorrect route path", routePath, context.getRoutePath());
		assertEquals("Incorrect application path" + suffix, this.contextUrl("", routePath),
				context.getApplicationPath());
		ExecutionManagedFunction function = context.getInitialManagedFunction();
		assertEquals("Incorrect function name" + suffix, functionName, function.getManagedFunctionName());
		assertEquals("Incorrect object type" + suffix, objectType,
				function.getManagedFunctionType().getObjectTypes()[1].getObjectType());
		assertEquals("Incorrect documentation" + suffix, documentation, context.getDocumentation());
	}

	public static class HttpInputExploreOneSection {
		public void service(String value) {
			// test method
		}
	}

	public static class HttpInputExploreTwoSection {
		public void service(Integer value) {
			// test method
		}
	}

	/**
	 * Ensure can explore with {@link HttpPathParameter}.
	 */
	public void testHttpInputWithPathParameter() throws Exception {

		// Capture the input
		Closure<HttpInputExplorerContext> explorer = new Closure<>();

		// Configure the server
		this.compile.web((context) -> {

			// Provide some inputs to explore
			context.link(false, "/path/{parameter}", HttpInputWithPathParameterSection.class);

			// Capture exploring
			context.getWebArchitect().addHttpInputExplorer((explore) -> {
				explorer.value = explore;
			});
		});
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Ensure correct method
		assertExploredHttpInput(explorer.value, false, HttpMethod.GET, "/path/{parameter}",
				"GET_/path/{parameter}.service", String.class, null);
	}

	public static class HttpInputWithPathParameterSection {
		public void service(@HttpPathParameter("parameter") String parameter) {
			// no operation
		}
	}

	/**
	 * Adds the context path to the path.
	 * 
	 * @param server Server details (e.g. http://officefloor.net:80 ).
	 * @param path   Path.
	 * @return URL with the context path.
	 */
	private String contextUrl(String server, String path) {
		if (this.contextPath != null) {
			path = this.contextPath + path;
		}
		return server + path;
	}

	/**
	 * Creates a {@link MockHttpRequestBuilder} for the path (including context
	 * path).
	 * 
	 * @param path Path for the {@link MockHttpRequestBuilder}.
	 * @return {@link MockHttpRequestBuilder}.
	 */
	private MockHttpRequestBuilder mockRequest(String path) {
		if (this.contextPath != null) {
			path = this.contextPath + path;
		}
		return MockHttpServer.mockRequest(path);
	}

	/**
	 * Services the {@link MockHttpRequestBuilder}.
	 * 
	 * @param httpMethodName  Name of the {@link HttpMethod}.
	 * @param applicationPath Application path.
	 * @param servicer        {@link Class} of the servicer.
	 * @param request         {@link MockHttpRequestBuilder}.
	 * @return {@link MockHttpResponse}.
	 */
	private MockHttpResponse service(String httpMethodName, String applicationPath, Class<?> servicer,
			MockHttpRequestBuilder request) throws Exception {
		return this.service(false, httpMethodName, applicationPath, servicer, request);
	}

	/**
	 * Services the {@link MockHttpRequestBuilder}.
	 * 
	 * @param isSecure        Indicates if route is secure.
	 * @param httpMethodName  Name of the {@link HttpMethod}.
	 * @param applicationPath Application path.
	 * @param servicer        {@link Class} of the servicer.
	 * @param request         {@link MockHttpRequestBuilder}.
	 * @return {@link MockHttpResponse}.
	 */
	private MockHttpResponse service(boolean isSecure, String httpMethodName, String applicationPath, Class<?> servicer,
			MockHttpRequestBuilder request) throws Exception {
		this.compile.web((context) -> context.link(isSecure, httpMethodName, applicationPath, servicer));
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();
		return this.server.send(request);
	}

	/**
	 * Validates the redirect for requiring a secure connection.
	 * 
	 * @param httpMethodName  Name of the {@link HttpMethod}.
	 * @param applicationPath Application path. May contain parameters.
	 * @param servicer        {@link Class} of the servicer.
	 * @param requestPath     Path for the {@link MockHttpRequestBuilder}.
	 * @param expectedEntity  Expected secure entity.
	 */
	private void secureService(String httpMethodName, String applicationPath, Class<?> servicer, String requestPath,
			String expectedEntity) throws Exception {
		HttpMethod httpMethod = HttpMethod.getHttpMethod(httpMethodName);
		MockHttpResponse response = this.service(true, httpMethodName, applicationPath, servicer,
				this.mockRequest(requestPath).method(httpMethod));
		assertEquals("Incorrect status", 307, response.getStatus().getStatusCode());
		response.assertHeader("location", this.contextUrl("https://mock.officefloor.net", requestPath));

		// Ensure able to GET over secure connection
		response = this.server.send(this.mockRequest(requestPath).method(httpMethod).secure(true));
		assertEquals("Incorrect status", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect response", expectedEntity, response.getEntity(null));
	}

}
