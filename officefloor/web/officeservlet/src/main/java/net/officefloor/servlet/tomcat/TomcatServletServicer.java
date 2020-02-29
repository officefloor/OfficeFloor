package net.officefloor.servlet.tomcat;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.servlet.Servlet;

import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.coyote.OutputBuffer;
import org.apache.coyote.Request;
import org.apache.coyote.Response;

import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.servlet.ServletServicer;

/**
 * {@link Tomcat} {@link ServletServicer}.
 * 
 * @author Daniel Sagenschneider
 */
public class TomcatServletServicer implements ServletServicer {

	/**
	 * Operation to run.
	 */
	@FunctionalInterface
	public static interface Operation<R, T extends Throwable> {

		/**
		 * Logic of operation.
		 * 
		 * @return Result.
		 * @throws T Possible failure.
		 */
		R run() throws T;
	}

	/**
	 * Indicates if within Maven <code>war</code> project.
	 */
	private static final ThreadLocal<Boolean> isWithinMavenWarProject = new ThreadLocal<>();

	/**
	 * Runs the {@link Operation} assuming within Maven <code>war</code> project.
	 * 
	 * @param <R>       Return type.
	 * @param <T>       Possible exception type.
	 * @param operation {@link Operation}.
	 * @return Result.
	 * @throws T Possible failure.
	 */
	public static <R, T extends Throwable> R runInMavenWarProject(Operation<R, T> operation) throws T {
		Boolean original = isWithinMavenWarProject.get();
		try {
			// Flag within war project
			isWithinMavenWarProject.set(Boolean.TRUE);

			// Undertake operation
			return operation.run();

		} finally {
			// Determine if clear (as specified)
			if (original == null) {
				isWithinMavenWarProject.remove();
			}
		}
	}

	/**
	 * {@link Tomcat} for embedded {@link Servlet} container.
	 */
	private final Tomcat tomcat;

	/**
	 * {@link Connector}.
	 */
	private final Connector connector;

	/**
	 * {@link Context}.
	 */
	private final Context context;

	/**
	 * {@link OfficeFloorProtocol}.
	 */
	private OfficeFloorProtocol<?> protocol;

	/**
	 * Instantiate.
	 * 
	 * @param contextPath Context path.
	 * @throws IOException If fails to setup container.
	 */
	public TomcatServletServicer(String contextPath) throws IOException {

		// Create OfficeFloor connector
		this.connector = new Connector(OfficeFloorProtocol.class.getName());
		this.connector.setPort(1);

		// Setup tomcat
		this.tomcat = new Tomcat();
		this.tomcat.setConnector(this.connector);

		// Configure webapp directory
		String username = System.getProperty("user.name");
		Path tempWebApp = Files.createTempDirectory(username + "_webapp");
		String tempWebAppPath = tempWebApp.toAbsolutePath().toString();

		// Create the context
		String contextName = ((contextPath == null) || (contextPath.equals("/"))) ? "" : contextPath;
		this.context = this.tomcat.addWebapp(contextName, tempWebAppPath);

		// Determine if load for running in Maven war project
		if (isWithinMavenWarProject.get() != null) {
			File additionWebInfClasses = new File("target/test-classes");
			WebResourceRoot resources = new StandardRoot(this.context);
			resources.addPreResources(
					new DirResourceSet(resources, "/WEB-INF/classes", additionWebInfClasses.getAbsolutePath(), "/"));
			resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
					new File("target/classes").getAbsolutePath(), "/"));
			this.context.setResources(resources);
		}
	}

	/**
	 * Adds a {@link Servlet}.
	 * 
	 * @param path    Path for the {@link Servlet}.
	 * @param name
	 * @param servlet
	 * @return
	 */
	public Wrapper addServlet(String path, String name, Servlet servlet) {
		Wrapper wrapper = Tomcat.addServlet(this.context, name, servlet);
		this.context.addServletMappingDecoded(path, name);
		return wrapper;
	}

	/**
	 * Starts the {@link Servlet} container.
	 * 
	 * @throws Exception If fails to start.
	 */
	public void start() throws Exception {

		// Start
		this.tomcat.start();

		// Obtain OfficeFloor protocol to input request
		this.protocol = (OfficeFloorProtocol<?>) connector.getProtocolHandler();
	}

	/**
	 * Stops the {@link Servlet} container.
	 * 
	 * @throws Exception If fails to stop.
	 */
	public void stop() throws Exception {
		this.tomcat.stop();
	}

	/*
	 * ===================== ServletServicer ========================
	 */

	@Override
	public void service(ServerHttpConnection connection) throws Exception {

		// Create the request
		Request request = new Request();
		HttpRequest httpRequest = connection.getRequest();
		request.requestURI().setString(httpRequest.getUri());
		request.decodedURI().setString(httpRequest.getUri());
		request.method().setString(httpRequest.getMethod().getName());

		// Create the response
		Response response = new Response();
		HttpResponse httpResponse = connection.getResponse();
		request.setResponse(response);
		response.setOutputBuffer(new OfficeFloorOutputBuffer(httpResponse));

		// Service request
		this.protocol.getAdapter().service(request, response);
	}

	/**
	 * {@link OutputBuffer} for {@link ServerHttpConnection}.
	 */
	private static class OfficeFloorOutputBuffer implements OutputBuffer {

		/**
		 * {@link HttpResponse}.
		 */
		private final HttpResponse httpResponse;

		/**
		 * Bytes written.
		 */
		private long bytesWritten = 0;

		/**
		 * Instantiate.
		 * 
		 * @param httpResponse {@link HttpResponse}.
		 */
		private OfficeFloorOutputBuffer(HttpResponse httpResponse) {
			this.httpResponse = httpResponse;
		}

		/*
		 * ================= OutputBuffer ======================
		 */

		@Override
		public int doWrite(ByteBuffer chunk) throws IOException {
			int size = chunk.remaining();
			this.httpResponse.getEntity().write(chunk);
			this.bytesWritten += size;
			return size;
		}

		@Override
		public long getBytesWritten() {
			return this.bytesWritten;
		}
	}

}