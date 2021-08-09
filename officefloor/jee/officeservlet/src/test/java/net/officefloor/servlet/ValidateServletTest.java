/*-
 * #%L
 * Servlet
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

package net.officefloor.servlet;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

import javax.servlet.AsyncContext;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.Request;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.servlet.procedure.ServletProcedureSource;
import net.officefloor.servlet.supply.ServletSupplierSource;
import net.officefloor.woof.WoofContext;
import net.officefloor.woof.WoofExtensionService;
import net.officefloor.woof.WoofExtensionServiceFactory;

/**
 * Ensure integration of {@link OfficeFloor} to {@link Tomcat} {@link Request}
 * is valid.
 * 
 * @author Daniel Sagenschneider
 */
public class ValidateServletTest extends OfficeFrameTestCase
		implements WoofExtensionServiceFactory, WoofExtensionService {

	/**
	 * Web directory.
	 */
	private static final String WEB_DIR = new File("src/test/webapp").getAbsolutePath();

	/**
	 * Specifies the {@link Servlet} to load.
	 */
	private static final ThreadLocal<Class<? extends Servlet>> woofLoadServlet = new ThreadLocal<>();

	/*
	 * ------------------- Validate Functionality ---------------------
	 */

	public void test_Tomcat_ValidateServlet() throws Exception {
		this.doDirectTomcatTest(ValidateServlet.class, this::doValidateServletRequest);
	}

	public void test_OfficeFloor_ValidateServlet() throws Exception {
		this.doOfficeFloorEmbedTomcatTest(ValidateServlet.class, this::doValidateServletRequest);
	}

	private void doValidateServletRequest() throws IOException {
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			HttpPost post = new HttpPost("http://localhost:7878/path?query=QUERY&param=ANOTHER");
			post.addHeader("header", "HEADER");
			post.addHeader("cookie", "COOKIE=INPUT");
			StringEntity entity = new StringEntity("entity");
			entity.setContentType("text/plain");
			post.setEntity(entity);
			HttpResponse response = client.execute(post);
			assertEquals("Should be successful", 201, response.getStatusLine().getStatusCode());
			assertNull("Should not include reset header", response.getFirstHeader(ValidateServlet.RESET_HEADER_NAME));
			assertEquals("Response Cookie", "COOKIE=TEST", response.getFirstHeader("set-cookie").getValue());
			assertEquals("Response data header", "Thu, 01 Jan 1970 00:00:00 GMT",
					response.getFirstHeader("date-header").getValue());
			assertEquals("Response header", "RESPONSE_HEADER", response.getFirstHeader("header").getValue());
			assertEquals("Response int header", "2", response.getFirstHeader("int-header").getValue());
			assertEquals("Response Content Type", "text/plain;charset=UTF8",
					response.getFirstHeader("content-type").getValue());
			assertEquals("Response Content Length", String.valueOf("Successful".length()),
					response.getFirstHeader("content-length").getValue());
			assertEquals("Response Entity", "Successful", EntityUtils.toString(response.getEntity()));
		}
	}

	public static class ValidateServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;

		private static final String RESET_HEADER_NAME = "reset-header";

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

			// Validate data
			assertEquals("ContentLength", 6, req.getContentLength());
			assertEquals("ContentLength", 6, req.getContentLengthLong());
			assertEquals("ContentType", "text/plain", req.getContentType());
			assertEquals("ContextPath", "", req.getContextPath());
			assertEquals("Cookie", "COOKIE", req.getCookies()[0].getName());
			assertEquals("Header", "HEADER", req.getHeader("header"));
			assertEquals("Method", "POST", req.getMethod());
			assertEquals("Parameter query", "QUERY", req.getParameter("query"));
			assertEquals("Parameter param", "ANOTHER", req.getParameter("param"));
			assertEquals("Protocol", "HTTP/1.1", req.getProtocol());
			assertEquals("QueryString", "query=QUERY&param=ANOTHER", req.getQueryString());
			assertEquals("RequestURI", "/path", req.getRequestURI());
			assertEquals("Scheme", "http", req.getScheme());

			// Validate request entity
			StringWriter requestEntity = new StringWriter();
			Reader requestEntityReader = req.getReader();
			for (int character = requestEntityReader.read(); character != -1; character = requestEntityReader.read()) {
				requestEntity.write(character);
			}
			assertEquals("Request entity", "entity", requestEntity.toString());

			// Validate functionality
			assertEquals("encodeRedirectURL", "/path", resp.encodeRedirectURL("/path"));
			assertEquals("encodeURL", "/path", resp.encodeURL("/path"));
			assertEquals("Incorrect buffer size", 8192, resp.getBufferSize());
			assertEquals("Incorrect character encoding", "ISO-8859-1", resp.getCharacterEncoding());
			assertFalse("Should not be committed", resp.isCommitted());

			// Ensure reset
			resp.addHeader(RESET_HEADER_NAME, "Should not be sent");
			resp.getOutputStream().write("Should not be sent".getBytes());
			resp.reset();

			// Write response (for validation)
			resp.setStatus(201);
			resp.addCookie(new Cookie("COOKIE", "TEST"));
			resp.addDateHeader("date-header", 0);
			resp.addHeader("header", "RESPONSE_HEADER");
			assertTrue("Should have response header", resp.containsHeader("header"));
			assertEquals("Incorrect response header", "RESPONSE_HEADER", resp.getHeader("header"));
			resp.addIntHeader("int-header", 2);
			resp.setContentType("text/plain");
			resp.setCharacterEncoding("UTF8");

			// Ensure reset buffer
			resp.getWriter().write("Should not be sent");
			resp.resetBuffer();

			// Write actual response
			resp.getWriter().write("Successful");
		}
	}

	/*
	 * ------------------- Throw Exception ---------------------
	 */

	public void test_Tomcat_ThrowException() throws Exception {
		this.doDirectTomcatTest(ThrowExceptionServlet.class,
				() -> this.doExceptionRequest(false, RuntimeException.class, "TEST"));
	}

	public void test_OfficeFloor_ThrowException() throws Exception {
		this.doOfficeFloorEmbedTomcatTest(ThrowExceptionServlet.class,
				() -> this.doExceptionRequest(true, RuntimeException.class, "TEST"));
	}

	private void doExceptionRequest(boolean isOfficeFloor, Class<? extends Throwable> exceptionType, String message)
			throws IOException {
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			HttpResponse response = client.execute(new HttpPost("http://localhost:7878/path"));
			String entity = EntityUtils.toString(response.getEntity());
			assertEquals("Should report error status: " + entity, 500, response.getStatusLine().getStatusCode());
			if (isOfficeFloor) {
				assertEquals("Invalid cause", "{\"error\":\"" + message + "\"}", entity);
			} else {
				assertTrue("Invalid Tomcat cause: " + entity,
						entity.contains(exceptionType.getName() + ": " + message));
			}
		}
	}

	public static class ThrowExceptionServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			throw new RuntimeException("TEST");
		}
	}

	/*
	 * ------------------- Send Redirect ---------------------
	 */

	public void test_Tomcat_SendRedirect() throws Exception {
		this.doDirectTomcatTest(SendRedirectServlet.class, () -> this.doSendRedirectRequest("redirect"));
	}

	public void test_OfficeFloor_SendRedirect() throws Exception {
		this.doOfficeFloorEmbedTomcatTest(SendRedirectServlet.class, () -> this.doSendRedirectRequest("redirect"));
	}

	private void doSendRedirectRequest(String location) throws IOException {
		try (CloseableHttpClient client = HttpClientBuilder.create().disableRedirectHandling().build()) {
			HttpResponse response = client.execute(new HttpPost("http://localhost:7878/path"));
			assertEquals("Should be redirect: " + EntityUtils.toString(response.getEntity()), 302,
					response.getStatusLine().getStatusCode());
			assertEquals("Incorrect redirect location", location, response.getFirstHeader("location").getValue());
		}
	}

	public static class SendRedirectServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.sendRedirect("redirect");
		}
	}

	/*
	 * ------------------- Send Error ---------------------
	 */

	public void test_Tomcat_SendError() throws Exception {
		this.doDirectTomcatTest(SendErrorServlet.class, () -> this.doSendErrorRequest(false, 478, "TEST"));
	}

	public void test_OfficeFloor_SendError() throws Exception {
		this.doOfficeFloorEmbedTomcatTest(SendErrorServlet.class, () -> this.doSendErrorRequest(true, 478, "TEST"));
	}

	private void doSendErrorRequest(boolean isOfficeFloor, int statusCode, String message) throws IOException {
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			HttpResponse response = client.execute(new HttpPost("http://localhost:7878/path"));
			String entity = EntityUtils.toString(response.getEntity());
			assertEquals("Should be error: " + entity, statusCode, response.getStatusLine().getStatusCode());
			if (isOfficeFloor) {
				assertEquals("Incorrect message", "{\"error\":\"" + message + "\"}", entity);
			} else {
				assertTrue("Incorrect message: " + entity, entity.contains("p><b>Message</b> " + message + "</p>"));
			}
		}
	}

	public static class SendErrorServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.sendError(478, "TEST");
		}
	}

	/*
	 * -------------- ServletSupplierSource Send Error ----------------
	 */

	public void test_Tomcat_ServletSupplierSource_SendError() throws Exception {
		this.doDirectTomcatTest(ServletSupplierSourceSendErrorServlet.class,
				() -> this.doExceptionRequest(false, Exception.class, "TEST"));
	}

	public void test_OfficeFloor_ServletSupplierSource_SendError() throws Exception {
		this.doOfficeFloorEmbedTomcatTest(ServletSupplierSourceSendErrorServlet.class,
				() -> this.doExceptionRequest(true, Exception.class, "TEST"));
	}

	public static class ServletSupplierSourceSendErrorServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			ServletSupplierSource.sendError(new Exception("TEST"), req, resp);
		}
	}

	/*
	 * ------------------- Async success ---------------------
	 */

	public void test_Tomcat_Async() throws Exception {
		this.doDirectTomcatTest(AsyncServlet.class, () -> this.doAsyncRequest());
	}

	public void test_OfficeFloor_Async() throws Exception {
		this.doOfficeFloorEmbedTomcatTest(AsyncServlet.class, () -> this.doAsyncRequest());
	}

	private void doAsyncRequest() throws IOException {
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			HttpResponse response = client.execute(new HttpPost("http://localhost:7878/path"));
			String entity = EntityUtils.toString(response.getEntity());
			assertEquals("Should be successful: " + entity, 201, response.getStatusLine().getStatusCode());
			assertEquals("Incorrect entity", "successful", entity);
		}
	}

	public static class AsyncServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			AsyncContext async = req.startAsync();
			async.start(() -> {
				HttpServletResponse response = (HttpServletResponse) async.getResponse();
				try {
					response.setStatus(201);
					response.getWriter().write("successful");
					async.complete();
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			});
		}
	}

	/*
	 * ------------------- Async Send Redirect ---------------------
	 */

	public void test_Tomcat_Async_SendRedirect() throws Exception {
		this.doDirectTomcatTest(AsyncSendRedirectServlet.class, () -> this.doSendRedirectRequest("redirect"));
	}

	public void test_OfficeFloor_Async_SendRedirect() throws Exception {
		this.doOfficeFloorEmbedTomcatTest(AsyncSendRedirectServlet.class, () -> this.doSendRedirectRequest("redirect"));
	}

	public static class AsyncSendRedirectServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			AsyncContext async = req.startAsync();
			async.start(() -> {
				HttpServletResponse response = (HttpServletResponse) async.getResponse();
				try {
					response.sendRedirect("redirect");
					async.complete();
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			});
		}
	}

	/*
	 * ------------------- Async Send Error ---------------------
	 */

	public void test_Tomcat_Async_SendError() throws Exception {
		this.doDirectTomcatTest(AsyncSendErrorServlet.class, () -> this.doSendErrorRequest(false, 478, "TEST"));
	}

	public void test_OfficeFloor_Async_SendError() throws Exception {
		this.doOfficeFloorEmbedTomcatTest(AsyncSendErrorServlet.class,
				() -> this.doSendErrorRequest(true, 478, "TEST"));
	}

	public static class AsyncSendErrorServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			AsyncContext async = req.startAsync();
			async.start(() -> {
				HttpServletResponse response = (HttpServletResponse) async.getResponse();
				try {
					response.sendError(478, "TEST");
					async.complete();
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			});
		}
	}

	/*
	 * ------------------- Async Throw Exception ---------------------
	 */

	public void test_OfficeFloor_Async_ThrowException() throws Exception {
		this.doOfficeFloorEmbedTomcatTest(AsyncThrowExceptionServlet.class,
				() -> this.doExceptionRequest(true, RuntimeException.class, "TEST"));
	}

	public static class AsyncThrowExceptionServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			AsyncContext async = req.startAsync();
			async.start(() -> {
				throw new RuntimeException("TEST");
			});
		}
	}

	@FunctionalInterface
	private static interface Validator {
		void validate() throws IOException;
	}

	/**
	 * Undertake direct Tomcat test.
	 */
	private void doDirectTomcatTest(Class<? extends Servlet> servletClass, Validator validator) throws Exception {
		Tomcat tomcat = new Tomcat();
		Connector connector = new Connector();
		connector.setPort(7878);
		tomcat.setConnector(connector);
		Context context = tomcat.addContext("", WEB_DIR);
		Wrapper wrapper = Tomcat.addServlet(context, "servlet", servletClass.getName());
		wrapper.setAsyncSupported(true);
		context.addServletMappingDecoded("/*", "servlet");
		try {
			tomcat.start();
			validator.validate();
		} finally {
			tomcat.stop();
			tomcat.destroy();
		}
	}

	/**
	 * Undertake OfficeFloor embedding Tomcat test.
	 */
	private void doOfficeFloorEmbedTomcatTest(Class<? extends Servlet> servletClass, Validator validator)
			throws Exception {
		woofLoadServlet.set(servletClass);
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		try (OfficeFloor officeFloor = compiler.compile("OfficeFloor")) {
			officeFloor.openOfficeFloor();
			validator.validate();
		} finally {
			woofLoadServlet.remove();
		}
	}

	/*
	 * ===================== WoofExtensionServiceFactory ========================
	 */

	@Override
	public WoofExtensionService createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * =========================== WoofExtension =================================
	 */

	@Override
	public void extend(WoofContext context) throws Exception {
		Class<? extends Servlet> servletClass = woofLoadServlet.get();
		if (servletClass != null) {
			OfficeSection servlet = context.getProcedureArchitect().addProcedure("Servlet", servletClass.getName(),
					ServletProcedureSource.SOURCE_NAME, servletClass.getSimpleName(), false, null);
			context.getOfficeArchitect().link(context.getWebArchitect().getHttpInput(false, "POST", "/path").getInput(),
					servlet.getOfficeSectionInput(ProcedureArchitect.INPUT_NAME));
		}
	}

}
