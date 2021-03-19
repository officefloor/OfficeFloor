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

package net.officefloor.servlet.tomcat;

import java.nio.charset.Charset;

import org.apache.catalina.Container;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Service;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.connector.CoyoteAdapter;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;

/**
 * Delegate {@link Connector} to provide to {@link CoyoteAdapter} to service via
 * {@link Container}.
 * 
 * @author Daniel Sagenschneider
 */
public class ContainerAdapter extends CoyoteAdapter {

	/**
	 * Instantiate.
	 * 
	 * @param wrapper   {@link Wrapper}.
	 * @param connector {@link Connector}.
	 */
	public ContainerAdapter(Wrapper wrapper, Connector connector) {
		super(new ContainerConnector(wrapper, connector));
	}

	/**
	 * {@link Container} {@link Connector}.
	 */
	private static class ContainerConnector extends Connector {

		/**
		 * {@link Wrapper}.
		 */
		private final Wrapper wrapper;

		/**
		 * Delegate {@link Connector}.
		 */
		private final Connector delegate;

		/**
		 * Instantiate.
		 * 
		 * @param wrapper  {@link Wrapper}.
		 * @param delegate {@link Connector}.
		 */
		public ContainerConnector(Wrapper wrapper, Connector delegate) {
			super(OfficeFloorProtocol.class.getName());
			this.wrapper = wrapper;
			this.delegate = delegate;

			// Override domain (as getter final)
			this.setDomain(this.delegate.getDomain());
		}

		/*
		 * ====================== Connector =======================
		 */

		@Override
		public Request createRequest() {

			// Create the request
			Request request = this.delegate.createRequest();

			// Specify the wrapper to directly invoke
			request.getMappingData().wrapper = this.wrapper;

			// Return the request
			return request;
		}

		/*
		 * ================= Connector (delegate) =================
		 */

		@Override
		public Response createResponse() {
			return this.delegate.createResponse();
		}

		@Override
		public Charset getURICharset() {
			return this.delegate.getURICharset();
		}

		@Override
		public boolean getXpoweredBy() {
			return this.delegate.getXpoweredBy();
		}

		@Override
		public LifecycleState getState() {
			return this.delegate.getState();
		}

		@Override
		public String getScheme() {
			return this.delegate.getScheme();
		}

		@Override
		public boolean getSecure() {
			return this.delegate.getSecure();
		}

		@Override
		public Service getService() {
			return this.delegate.getService();
		}

		@Override
		public String getProxyName() {
			return this.delegate.getProxyName();
		}

		@Override
		public int getProxyPort() {
			return this.delegate.getProxyPort();
		}

		@Override
		public boolean getAllowTrace() {
			return this.delegate.getAllowTrace();
		}

		@Override
		public boolean getUseIPVHosts() {
			return this.delegate.getUseIPVHosts();
		}
	}

}
