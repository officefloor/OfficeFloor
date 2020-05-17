/*-
 * #%L
 * Servlet
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

package net.officefloor.servlet;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.Request;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
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

	/**
	 * Ensure correct value.
	 */
	public void testDirectTomcatForValidateServlet() throws Exception {
		this.doDirectTomcatTest(ValidateServlet.class, this::doValidateServletRequest);
	}

	/**
	 * Ensure OfficeFloor correctly embeds Tomcat.
	 */
	public void testOfficeFloorEmbedTomcatForValidateServlet() throws Exception {
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

	/**
	 * Ensure correct value.
	 */
	public void testDirectTomcatForSendAbsoluteRedirect() throws Exception {
		this.doDirectTomcatTest(SendRedirectServlet.class, () -> this.doSendRedirectRequest("/redirect", "/redirect"));
	}

	/**
	 * Ensure OfficeFloor correctly embeds Tomcat.
	 */
	public void testOfficeFloorEmbedTomcatForSendAbsoluteRedirect() throws Exception {
		this.doOfficeFloorEmbedTomcatTest(SendRedirectServlet.class,
				() -> this.doSendRedirectRequest("/redirect", "redirect"));
	}

	/**
	 * Ensure correct value.
	 */
	public void testDirectTomcatForSendRelativeRedirect() throws Exception {
		this.doDirectTomcatTest(SendRedirectServlet.class, () -> this.doSendRedirectRequest("redirect", "redirect"));
	}

	/**
	 * Ensure OfficeFloor correctly embeds Tomcat.
	 */
	public void testOfficeFloorEmbedTomcatForSendRelativeRedirect() throws Exception {
		this.doOfficeFloorEmbedTomcatTest(SendRedirectServlet.class,
				() -> this.doSendRedirectRequest("redirect", "/relative/redirect"));
	}

	private void doSendRedirectRequest(String redirectPath, String expectedLocation) throws IOException {
		try (CloseableHttpClient client = HttpClientBuilder.create().disableRedirectHandling().build()) {
			// Undertake request with redirect path
			SendRedirectServlet.redirectPath = redirectPath;
			HttpResponse response = client.execute(new HttpGet("http://localhost:7878/relative"));
			assertEquals("Should be redirect", 302, response.getStatusLine().getStatusCode());
			assertEquals("Incorrect redirect location", expectedLocation,
					response.getFirstHeader("location").getValue());
		}
	}

	public static class SendRedirectServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;

		private static volatile String redirectPath;

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.sendRedirect(redirectPath);
		}
	}

	/**
	 * Ensure correct value.
	 */
	public void testDirectTomcatForSendError() throws Exception {
		this.doDirectTomcatTest(SendErrorServlet.class, () -> this.doSendErrorRequest(null));
	}

	/**
	 * Ensure OfficeFloor correctly embeds Tomcat.
	 */
	public void testOfficeFloorEmbedTomcatForSendError() throws Exception {
		this.doOfficeFloorEmbedTomcatTest(SendErrorServlet.class, () -> this.doSendErrorRequest("ignored"));
	}

	/**
	 * Ensure correct value.
	 */
	public void testDirectTomcatForSendErrorMessage() throws Exception {
		this.doDirectTomcatTest(SendErrorServlet.class, () -> this.doSendErrorRequest("ignored"));
	}

	/**
	 * Ensure OfficeFloor correctly embeds Tomcat.
	 */
	public void testOfficeFloorEmbedTomcatForSendErrorMessage() throws Exception {
		this.doOfficeFloorEmbedTomcatTest(SendErrorServlet.class, () -> this.doSendErrorRequest("ignored"));
	}

	private void doSendErrorRequest(String message) throws IOException {
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			// Undertake request with message
			SendErrorServlet.message = message;
			HttpResponse response = client.execute(new HttpGet("http://localhost:7878/error"));
			assertEquals("Should be error", 478, response.getStatusLine().getStatusCode());
		}
	}

	public static class SendErrorServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;

		private static volatile String message;

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			if (message == null) {
				resp.sendError(478);
			} else {
				resp.sendError(478, message);
			}
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
		Tomcat.addServlet(context, "servlet", servletClass.getName());
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