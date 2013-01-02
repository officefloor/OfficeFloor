/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.servlet.container;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * <p>
 * Confirms results for the {@link HttpServlet}.
 * <p>
 * It provides results of {@link HttpServletRequest} methods on handling a HTTP
 * request.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletConfirmer extends HttpServlet {

	/**
	 * Port to create the {@link Server} on for confirmation.
	 */
	private static final int PORT = 8181;

	/**
	 * {@link RecordedMethodInvocation} instances.
	 */
	private final List<RecordedMethodInvocation> invocations = new LinkedList<RecordedMethodInvocation>();

	/**
	 * Records and plays back the methods for confirming the results.
	 */
	private final InvocationHandler recorder = new InvocationHandler() {
		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			Object returnValue;
			synchronized (ServletConfirmer.this) {

				// Record the invocation
				ServletConfirmer.this.invocations
						.add(new RecordedMethodInvocation(method,
								(args == null ? new Object[0] : args)));

				// Obtain Proxy return and reset
				returnValue = ServletConfirmer.this.proxyReturn;
				ServletConfirmer.this.proxyReturn = null;
			}
			return returnValue;
		}
	};

	/**
	 * Proxy for recording action on the {@link HttpServletRequest}.
	 */
	private final HttpServletRequest proxy = (HttpServletRequest) Proxy
			.newProxyInstance(this.getClass().getClassLoader(),
					new Class[] { HttpServletRequest.class }, this.recorder);

	/**
	 * Context path for confirmation.
	 */
	private String contextPath = "/";

	/**
	 * Specifies the Context Path.
	 * 
	 * @param contextPath
	 *            Context Path.
	 */
	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	/**
	 * Servlet path for confirmation.
	 */
	private String servletPath = "/*";

	/**
	 * Specifies the Servlet Path.
	 * 
	 * @param servletPath
	 *            Servlet Path.
	 */
	public void setServletPath(String servletPath) {
		this.servletPath = servletPath;
	}

	/**
	 * Proxy return.
	 */
	private Object proxyReturn = null;

	/**
	 * Result of last recorded {@link Method}.
	 */
	private Object lastResult;

	/**
	 * Specifies the return the proxy (mainly for primitive return types).
	 * 
	 * @param proxyReturn
	 *            Proxy return value.
	 */
	public synchronized void setProxyReturn(Object proxyReturn) {
		this.proxyReturn = proxyReturn;
	}

	/**
	 * Obtains the {@link HttpServletRequest} to record method to return value.
	 * 
	 * @return {@link HttpServletRequest} recorder.
	 */
	public HttpServletRequest getHttpServletRequestRecorder() {
		return this.proxy;
	}

	/**
	 * Confirms the result of the last {@link HttpServletRequest} method
	 * recorded.
	 * 
	 * @param uri
	 *            URI for the request.
	 * @param headerNameValues
	 *            Header name value pairs.
	 * @return Result of the last {@link HttpServletRequest} method recorded.
	 * @throws Exception
	 *             If fails to confirm.
	 */
	public Object confirm(String uri, String... headerNameValues)
			throws Exception {

		// Start the HTTP container for the HTTP Servlet
		Server server = new Server(PORT);
		ServletContextHandler context = new ServletContextHandler();
		context.setContextPath(this.contextPath);
		context.addServlet(new ServletHolder(this), this.servletPath);
		server.setHandler(context);
		try {
			server.start();

			// Send request to the server
			HttpClient client = new DefaultHttpClient();
			try {
				uri = (uri == null ? "" : uri);
				uri = (uri.startsWith("/") ? uri : "/" + uri);
				HttpPost request = new HttpPost("http://localhost:" + PORT
						+ uri);
				for (int i = 0; i < headerNameValues.length; i += 2) {
					String name = headerNameValues[i];
					String value = headerNameValues[i + 1];
					request.addHeader(name, value);
				}
				HttpResponse response = client.execute(request);
				TestCase.assertEquals("Expecting response to be successful",
						200, response.getStatusLine().getStatusCode());
			} finally {
				client.getConnectionManager().shutdown();
			}

			// Return the last result
			synchronized (this) {
				return this.lastResult;
			}

		} finally {
			// Ensure stop the server
			server.stop();
			server.destroy();
		}
	}

	/*
	 * ===================== HttpServlet ===============================
	 */

	@Override
	@SuppressWarnings("unchecked")
	protected synchronized void service(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		try {

			// Ensure method recorded
			TestCase.assertTrue("No method recorded",
					this.invocations.size() > 0);

			// Iterate over the methods for invocation
			for (RecordedMethodInvocation invocation : this.invocations) {

				// Obtain the parameter types
				Class<?>[] parameterTypes = new Class[invocation.arguments.length];
				for (int i = 0; i < parameterTypes.length; i++) {
					parameterTypes[i] = invocation.arguments[i].getClass();
				}

				// Obtain the method
				Method method = req.getClass().getMethod(
						invocation.method.getName(), parameterTypes);

				// Invoke the method to obtain as last result
				Object result = method.invoke(req, invocation.arguments);

				// Take copy of enumeration (stop concurrent access)
				if (result instanceof Enumeration) {
					Enumeration<Object> enumeration = (Enumeration<Object>) result;
					Vector<Object> vector = new Vector<Object>();
					while (enumeration.hasMoreElements()) {
						vector.add(enumeration.nextElement());
					}
					result = vector.elements();
				}

				// Provide the result
				this.lastResult = result;
			}
		} catch (Exception ex) {
			throw new ServletException(ex);
		}
	}

	/**
	 * Recorded {@link Method} invocation.
	 */
	private static class RecordedMethodInvocation {

		/**
		 * {@link Method} to invoke.
		 */
		public final Method method;

		/**
		 * Arguments for the {@link Method}.
		 */
		public Object[] arguments;

		/**
		 * Initiate.
		 * 
		 * @param method
		 *            {@link Method} to invoke.
		 * @param arguments
		 *            Arguments for the {@link Method}.
		 */
		public RecordedMethodInvocation(Method method, Object[] arguments) {
			this.method = method;
			this.arguments = arguments;
		}
	}

}