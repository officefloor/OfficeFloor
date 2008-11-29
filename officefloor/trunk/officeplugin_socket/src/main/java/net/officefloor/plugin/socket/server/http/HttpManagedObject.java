/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.plugin.socket.server.http;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import net.officefloor.frame.api.execute.EscalationHandler;
import net.officefloor.frame.spi.managedobject.AsynchronousListener;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.plugin.socket.server.http.api.HttpRequest;
import net.officefloor.plugin.socket.server.http.api.HttpResponse;
import net.officefloor.plugin.socket.server.http.api.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.parse.HttpRequestParser;

/**
 * {@link ManagedObject} for the {@link ServerHttpConnection}.
 * 
 * @author Daniel
 */
public class HttpManagedObject implements ServerHttpConnection,
		AsynchronousManagedObject, EscalationHandler {

	/**
	 * {@link HttpConnectionHandler}.
	 */
	private HttpConnectionHandler connectionHandler;

	/**
	 * {@link HttpRequest}.
	 */
	private final HttpRequest request;

	/**
	 * {@link HttpResponse}.
	 */
	private final HttpResponseImpl response;

	/**
	 * Initiate.
	 * 
	 * @param connectionHandler
	 *            {@link HttpConnectionHandler} that has just received a HTTP
	 *            request.
	 */
	public HttpManagedObject(HttpConnectionHandler connectionHandler) {
		this.connectionHandler = connectionHandler;

		// Create the request
		this.request = new HttpRequestImpl(this.connectionHandler
				.getHttpRequestParser());

		// Create the response to fill out
		this.response = new HttpResponseImpl(connectionHandler, this.request);
	}

	/*
	 * =============== ServerHttpConnection =================================
	 */

	/**
	 * {@link AsynchronousListener}.
	 */
	private AsynchronousListener asynchronousListener;

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.spi.managedobject.AsynchronousManagedObject#
	 * registerAsynchronousCompletionListener
	 * (net.officefloor.frame.spi.managedobject.AsynchronousListener)
	 */
	@Override
	public void registerAsynchronousCompletionListener(
			AsynchronousListener listener) {
		this.asynchronousListener = listener;

		// TODO use the asynchronous listener
		System.err.println("TODO [" + this.getClass().getSimpleName()
				+ "] use the asynchronous listener "
				+ this.asynchronousListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.ManagedObject#getObject()
	 */
	@Override
	public Object getObject() throws Exception {
		return this;
	}

	/*
	 * ================== ServerHttpConnection =========================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.plugin.socket.server.http.api.ServerHttpConnection#
	 * getHttpRequest()
	 */
	public synchronized HttpRequest getHttpRequest() {
		return this.request;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.plugin.socket.server.http.api.ServerHttpConnection#
	 * getHttpResponse()
	 */
	@Override
	public HttpResponse getHttpResponse() {
		return this.response;
	}

	/*
	 * ================== EscalationHandler =============================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.api.execute.EscalationHandler#handleEscalation(
	 * java.lang.Throwable)
	 */
	@Override
	public void handleEscalation(Throwable escalation) throws Throwable {
		// Indicate failure to handle request
		this.response.sendFailure(escalation);
	}

	/**
	 * {@link HttpRequest} implementation.
	 */
	private class HttpRequestImpl implements HttpRequest {

		/**
		 * Method.
		 */
		private final String method;

		/**
		 * Path.
		 */
		private final String path;

		/**
		 * Version.
		 */
		private final String version;

		/**
		 * Headers.
		 */
		private final Map<String, String> headers;

		/**
		 * Body.
		 */
		private final byte[] body;

		/**
		 * Initiate from {@link HttpRequestParser}.
		 * 
		 * @param parser
		 *            {@link HttpRequestParser}.
		 */
		public HttpRequestImpl(HttpRequestParser parser) {
			this.method = parser.getMethod();
			this.path = parser.getPath();
			this.version = parser.getVersion();
			this.headers = parser.getHeaders();
			this.body = parser.getBody();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * net.officefloor.plugin.socket.server.http.api.HttpRequest#getMethod()
		 */
		@Override
		public String getMethod() {
			return this.method;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * net.officefloor.plugin.socket.server.http.api.HttpRequest#getPath()
		 */
		@Override
		public String getPath() {
			return this.path;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * net.officefloor.plugin.socket.server.http.api.HttpRequest#getVersion
		 * ()
		 */
		@Override
		public String getVersion() {
			return this.version;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * net.officefloor.plugin.socket.server.http.api.HttpRequest#getHeaderNames
		 * ()
		 */
		@Override
		public Set<String> getHeaderNames() {
			return this.headers.keySet();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * net.officefloor.plugin.socket.server.http.api.HttpRequest#getHeader
		 * (java.lang.String)
		 */
		@Override
		public String getHeader(String name) {
			return this.headers.get(name.toUpperCase());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * net.officefloor.plugin.socket.server.http.api.HttpRequest#getBody()
		 */
		@Override
		public InputStream getBody() {
			return new ByteArrayInputStream(this.body);
		}

	}

}
