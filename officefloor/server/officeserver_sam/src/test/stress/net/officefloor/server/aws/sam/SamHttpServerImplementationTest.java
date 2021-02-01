package net.officefloor.server.aws.sam;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.servlet.ServletHolder;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import net.officefloor.server.http.HttpServerImplementation;
import net.officefloor.server.servlet.test.AbstractServletHttpServerImplementationTest;

/**
 * Tests the {@link SamHttpServerImplementation}.
 * 
 * @author Daniel Sagenschneider
 */
public class SamHttpServerImplementationTest extends AbstractServletHttpServerImplementationTest {

	/*
	 * =================== HttpServerImplementationTest =====================
	 */

	@Override
	protected int getRequestCount() {
		return 100000;
	}

	@Override
	protected Class<? extends HttpServerImplementation> getHttpServerImplementationClass() {
		return SamHttpServerImplementation.class;
	}

	@Override
	protected void configureServer(ServerContext context) throws Exception {

		// Create SAM (which should load OfficeFloor)
		OfficeFloorSam sam = new OfficeFloorSam();

		// Provide wrapping of SAM for testing
		context.getHandler().addServlet(new ServletHolder(new SamServlet(sam)), "/*");
	}

	@Override
	protected String getServerNameSuffix() {
		return "SAM";
	}

	/**
	 * {@link HttpServlet} wrapper for SAM.
	 */
	private static class SamServlet extends HttpServlet {

		/**
		 * Required serial version.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * {@link OfficeFloorSam} to wrap.
		 */
		private final OfficeFloorSam sam;

		/**
		 * Instantiate.
		 * 
		 * @param sam {@link OfficeFloorSam}.
		 */
		private SamServlet(OfficeFloorSam sam) {
			this.sam = sam;
		}

		@Override
		protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

			// Translate request into SAM request
			APIGatewayProxyRequestEvent samReq = new APIGatewayProxyRequestEvent();
			samReq.setHttpMethod(req.getMethod());
			String queryString = req.getQueryString();
			samReq.setPath(req.getRequestURI() + (queryString != null ? "?" + queryString : ""));
			Map<String, List<String>> headers = new HashMap<>();
			Enumeration<String> headerNames = req.getHeaderNames();
			while (headerNames.hasMoreElements()) {
				String headerName = headerNames.nextElement();
				List<String> values = new LinkedList<>();
				headers.put(headerName, values);
				Enumeration<String> headerValues = req.getHeaders(headerName);
				while (headerValues.hasMoreElements()) {
					String headerValue = headerValues.nextElement();
					values.add(headerValue);
				}
			}
			samReq.setMultiValueHeaders(headers);

			// Translate the request entity
			Base64Buffer buffer = new Base64Buffer();
			try (OutputStream outputStream = buffer.getOutputStream()) {
				try (InputStream input = req.getInputStream()) {
					for (int datum = input.read(); datum != -1; datum = input.read()) {
						outputStream.write(datum);
					}
				}
				samReq.setIsBase64Encoded(true);
				samReq.setBody(buffer.getBase64Text());
			}

			// Service the request
			APIGatewayProxyResponseEvent samResp = this.sam.handleRequest(samReq, null);

			// Translate SAM response onto response
			resp.setStatus(samResp.getStatusCode());
			samResp.getHeaders().forEach((name, value) -> resp.addHeader(name, value));

			// Translate the response entity
			InputStream responseEntity = new ByteArrayInputStream(
					samResp.getIsBase64Encoded() ? Base64.getDecoder().decode(samResp.getBody())
							: samResp.getBody().getBytes(Charset.forName("UTF-8")));
			try (OutputStream outputStream = resp.getOutputStream()) {
				for (int datum = responseEntity.read(); datum != -1; datum = responseEntity.read()) {
					outputStream.write(datum);
				}
			}
		}
	}

}