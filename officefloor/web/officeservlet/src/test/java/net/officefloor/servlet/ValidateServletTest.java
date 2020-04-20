package net.officefloor.servlet;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
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
	 * Determines if load {@link ValidateServlet}.
	 */
	private static final ThreadLocal<Boolean> woofLoadServlet = new ThreadLocal<>();

	/**
	 * Ensure correct value.
	 */
	public void testDirectTomcat() throws Exception {
		Tomcat tomcat = new Tomcat();
		Connector connector = new Connector();
		connector.setPort(7878);
		tomcat.setConnector(connector);
		Context context = tomcat.addContext("", WEB_DIR);
		Tomcat.addServlet(context, "servlet", new ValidateServlet());
		context.addServletMappingDecoded("/*", "servlet");
		try {
			tomcat.start();
			this.doRequest();
		} finally {
			tomcat.stop();
			tomcat.destroy();
		}
	}

	/**
	 * Ensure OfficeFloor correctly integrates to Tomcat.
	 */
	public void testOfficeFloorToTomcat() throws Exception {
		woofLoadServlet.set(true);
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		try (OfficeFloor officeFloor = compiler.compile("OfficeFloor")) {
			officeFloor.openOfficeFloor();
			this.doRequest();
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
		if (woofLoadServlet.get() != null) {
			OfficeSection servlet = context.getProcedureArchitect().addProcedure("Servlet",
					ValidateServlet.class.getName(), ServletProcedureSource.SOURCE_NAME,
					ValidateServlet.class.getSimpleName(), false, null);
			context.getOfficeArchitect().link(context.getWebArchitect().getHttpInput(false, "POST", "/path").getInput(),
					servlet.getOfficeSectionInput(ProcedureArchitect.INPUT_NAME));
		}
	}

	private void doRequest() throws IOException {
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			HttpPost post = new HttpPost("http://localhost:7878/path?query=QUERY&param=ANOTHER");
			post.addHeader("header", "HEADER");
			StringEntity entity = new StringEntity("entity");
			entity.setContentType("text/plain");
			post.setEntity(entity);
			HttpResponse response = client.execute(post);
			assertEquals("Should be successful", 201, response.getStatusLine().getStatusCode());
			assertEquals("Response Header", "RESPONSE_HEADER", response.getFirstHeader("header").getValue());
			assertEquals("Response Content Type", "text/plain;charset=UTF8",
					response.getFirstHeader("content-type").getValue());
			assertEquals("Response Content Length", String.valueOf("Successful".length()),
					response.getFirstHeader("content-length").getValue());
			assertEquals("Response Entity", "Successful", EntityUtils.toString(response.getEntity()));
		}
	}

	public static class ValidateServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

			// Validate data
			assertEquals("ContentLength", 6, req.getContentLength());
			assertEquals("ContentLength", 6, req.getContentLengthLong());
			assertEquals("ContentType", "text/plain", req.getContentType());
			assertEquals("ContextPath", "", req.getContextPath());
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

			// Write response (for validation)
			resp.setStatus(201);
			resp.addHeader("header", "RESPONSE_HEADER");
			resp.setContentType("text/plain");
			resp.setCharacterEncoding("UTF8");
			resp.getWriter().write("Successful");
		}
	}

}