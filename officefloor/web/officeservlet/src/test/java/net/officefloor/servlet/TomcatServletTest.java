package net.officefloor.servlet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.AbstractProtocol;
import org.apache.coyote.OutputBuffer;
import org.apache.coyote.Processor;
import org.apache.coyote.Request;
import org.apache.coyote.Response;
import org.apache.coyote.UpgradeProtocol;
import org.apache.coyote.UpgradeToken;
import org.apache.coyote.http11.Constants;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SocketEvent;
import org.apache.tomcat.util.net.SocketProcessorBase;
import org.apache.tomcat.util.net.SocketWrapperBase;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Ensure can provide {@link OfficeFloor} {@link Connector}.
 * 
 * @author Daniel Sagenschneider
 */
public class TomcatServletTest extends OfficeFrameTestCase {

	/**
	 * Ensure can override the {@link Connector}.
	 */
	public void testTomcatViaOfficeFloor() throws Exception {

		// Create OfficeFloor connector
		Connector connector = new Connector(OfficeFloorProtocol.class.getName());
		connector.setPort(1);

		// Start with connector
		Tomcat tomcat = new Tomcat();
		tomcat.setConnector(connector);
		Context context = tomcat.addContext("/", "/tmp");
		Tomcat.addServlet(context, "TEST", new MockHttpServlet());
		context.addServletMappingDecoded("/", "TEST");
		tomcat.start();

		// Obtain OfficeFloor protocol to input request
		OfficeFloorProtocol<?> protocol = (OfficeFloorProtocol<?>) connector.getProtocolHandler();

		// Create the request
		Request request = new Request();
		request.requestURI().setString("/");
		request.decodedURI().setString("/");
		request.method().setString("GET");

		// Create the response
		Response response = new Response();
		request.setResponse(response);
		response.setOutputBuffer(new OfficeFloorOutputBuffer());

		// Service request
		protocol.getAdapter().service(request, response);

		// Ensure successful
		assertEquals("Should be successful: " + response.getMessage(), 200, response.getStatus());
	}

	private static class OfficeFloorEndPoint<S, U> extends AbstractEndpoint<S, U> {

		private final Log log = LogFactory.getLog(this.getClass());

		@Override
		protected void createSSLContext(SSLHostConfig sslHostConfig) throws Exception {
		}

		@Override
		protected InetSocketAddress getLocalAddress() throws IOException {
			throw new UnsupportedOperationException("Should not require Local Address");
		}

		@Override
		public boolean isAlpnSupported() {
			return false;
		}

		@Override
		protected boolean getDeferAccept() {
			return false;
		}

		@Override
		protected SocketProcessorBase<S> createSocketProcessor(SocketWrapperBase<S> socketWrapper, SocketEvent event) {
			return null;
		}

		@Override
		public void bind() throws Exception {
		}

		@Override
		public void unbind() throws Exception {
		}

		@Override
		public void startInternal() throws Exception {
		}

		@Override
		public void stopInternal() throws Exception {
		}

		@Override
		protected Log getLog() {
			return log;
		}

		@Override
		protected void doCloseServerSocket() throws IOException {
		}

		@Override
		protected U serverSocketAccept() throws Exception {
			return null;
		}

		@Override
		protected boolean setSocketOptions(U socket) {
			return true;
		}

		@Override
		protected void destroySocket(U socket) {
		}
	}

	public static class OfficeFloorProtocol<S> extends AbstractProtocol<S> {

		private final Log log = LogFactory.getLog(this.getClass());

		public OfficeFloorProtocol() {
			super(new OfficeFloorEndPoint<>());
			setConnectionTimeout(Constants.DEFAULT_CONNECTION_TIMEOUT);
			ConnectionHandler<S> cHandler = new ConnectionHandler<>(this);
			setHandler(cHandler);
			getEndpoint().setHandler(cHandler);
		}

		@Override
		public void addSslHostConfig(SSLHostConfig sslHostConfig) {
		}

		@Override
		public SSLHostConfig[] findSslHostConfigs() {
			return new SSLHostConfig[0];
		}

		@Override
		public void addUpgradeProtocol(UpgradeProtocol upgradeProtocol) {
		}

		@Override
		public UpgradeProtocol[] findUpgradeProtocols() {
			return new UpgradeProtocol[0];
		}

		@Override
		protected Log getLog() {
			return log;
		}

		@Override
		protected String getNamePrefix() {
			return "OfficeFloor";
		}

		@Override
		protected String getProtocolName() {
			return "OfficeFloor";
		}

		@Override
		protected UpgradeProtocol getNegotiatedProtocol(String name) {
			return null;
		}

		@Override
		protected UpgradeProtocol getUpgradeProtocol(String name) {
			return null;
		}

		@Override
		protected Processor createProcessor() {
			return null;
		}

		@Override
		protected Processor createUpgradeProcessor(SocketWrapperBase<?> socket, UpgradeToken upgradeToken) {
			return null;
		}
	}

	private static class OfficeFloorOutputBuffer implements OutputBuffer {

		private final Log log = LogFactory.getLog(this.getClass());

		private long bytesWritten = 0;

		@Override
		public int doWrite(ByteBuffer chunk) throws IOException {
			int size = chunk.remaining();
			byte[] buffer = new byte[size];
			chunk.get(buffer);
			String value = new String(buffer);
			this.log.info("RESPONSE: " + value);
			this.bytesWritten += size;
			return size;
		}

		@Override
		public long getBytesWritten() {
			return this.bytesWritten;
		}
	}

	private class MockHttpServlet extends HttpServlet {

		private static final long serialVersionUID = 1L;

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.getWriter().write("Hello World");
		}
	}

}