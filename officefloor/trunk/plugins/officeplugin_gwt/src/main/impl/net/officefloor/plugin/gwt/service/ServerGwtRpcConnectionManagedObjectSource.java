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
package net.officefloor.plugin.gwt.service;

import java.io.IOException;
import java.io.InputStream;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.protocol.HttpStatus;

import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.SerializationPolicy;

/**
 * {@link ManagedObjectSource} for the {@link ServerGwtRpcConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServerGwtRpcConnectionManagedObjectSource
		extends
		AbstractManagedObjectSource<ServerGwtRpcConnectionManagedObjectSource.Dependencies, None> {

	/**
	 * Dependency keys for {@link ServerGwtRpcConnectionManagedObjectSource}.
	 */
	public static enum Dependencies {
		SERVER_HTTP_CONNECTION
	}

	/*
	 * ===================== ManagedObjectSource ==============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	protected void loadMetaData(MetaDataContext<Dependencies, None> context)
			throws Exception {

		// Specify meta-data
		context.setObjectClass(ServerGwtRpcConnection.class);
		context.setManagedObjectClass(ServerGwtRpcConnectionManagedObject.class);
		context.addDependency(Dependencies.SERVER_HTTP_CONNECTION,
				ServerHttpConnection.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new ServerGwtRpcConnectionManagedObject<Object>();
	}

	/**
	 * {@link ManagedObject} for the {@link ServerGwtRpcConnection}.
	 */
	private static class ServerGwtRpcConnectionManagedObject<T> implements
			CoordinatingManagedObject<Dependencies>, ServerGwtRpcConnection<T> {

		/**
		 * {@link ServerHttpConnection}.
		 */
		private ServerHttpConnection connection;

		/**
		 * {@link RPCRequest}.
		 */
		private RPCRequest request = null;

		/**
		 * Indicates if response already sent.
		 */
		private boolean isResponseSent = false;

		/**
		 * Required return type.
		 */
		private Class<?> requiredReturnType = null;

		/**
		 * Ensure able to send a response.
		 */
		private void ensureCanSendResponse() {
			// Ensure response not already sent
			if (this.isResponseSent) {
				throw new ServerGwtRpcConnectionException(
						"GWT RPC response already provided");
			}
		}

		/**
		 * Sends the response.
		 * 
		 * @param payload
		 *            Payload of the response.
		 */
		private void sendResponse(String payload) {

			// Flag response sent
			this.isResponseSent = true;

			// Obtain the response
			HttpResponse response = this.connection.getHttpResponse();

			// Send the RPC response
			try {
				response.getEntity().write(payload.getBytes());
				response.send();
			} catch (IOException ex) {
				// Should be very rare that not send GWT RPC response
				this.sendFailure(ex);
			}
		}

		/**
		 * Attempts to send a failure.
		 * 
		 * @param cause
		 *            Cause.
		 */
		private void sendFailure(Throwable cause) {

			// Flag response sent
			this.isResponseSent = true;

			// Flag failure on server
			this.connection.getHttpResponse().setStatus(
					HttpStatus.SC_INTERNAL_SERVER_ERROR);

			// Best attempts on sending failure, now notify of failure
			throw ServerGwtRpcConnectionException.newException(cause);
		}

		/*
		 * =================== ManagedObject ==========================
		 */

		@Override
		public void loadObjects(ObjectRegistry<Dependencies> registry)
				throws Throwable {
			// Obtain the Server HTTP Connection
			this.connection = (ServerHttpConnection) registry
					.getObject(Dependencies.SERVER_HTTP_CONNECTION);
		}

		@Override
		public Object getObject() throws Throwable {
			return this;
		}

		/*
		 * ======================= ServerGwtRpcConnection ====================
		 */

		@Override
		public synchronized void setReturnType(Class<?> returnType) {

			// Determine if already requiring a certain return type
			if (this.requiredReturnType != null) {
				// Ensure only specialising the required return type
				if (!(this.requiredReturnType.isAssignableFrom(returnType))) {
					// Not specialising
					throw new ServerGwtRpcConnectionException(
							returnType.getName()
									+ " can not be set as GWT RPC return type, as it does not specialise already specified return type "
									+ this.requiredReturnType.getName());
				}
			}

			// Specify the required return type
			this.requiredReturnType = returnType;
		}

		@Override
		public HttpRequest getHttpRequest() {
			return this.connection.getHttpRequest();
		}

		@Override
		public synchronized RPCRequest getRpcRequest() {

			// Lazy load the RPC request
			if (this.request == null) {

				// Obtain the request
				HttpRequest request = this.getHttpRequest();

				// Obtain the payload
				StringBuilder payload = new StringBuilder();
				try {
					InputStream body = request.getEntity();
					for (int value = body.read(); value != -1; value = body
							.read()) {
						payload.append((char) value);
					}
				} catch (IOException ex) {
					// Flag failed to read request.
					// This should typically not occur.
					this.sendFailure(ex);
					return null; // not occurs as sendFailure propagates failure
				}

				// Decode the GWT request
				this.request = RPC.decodeRequest(payload.toString());
			}

			// Return the RPC request
			return this.request;
		}

		@Override
		public synchronized void onSuccess(T result) {
			try {

				// Ensure can send a response
				this.ensureCanSendResponse();

				// Ensure appropriate return type
				if ((this.requiredReturnType != null) && (result != null)) {
					Class<?> resultType = result.getClass();
					if (!(this.requiredReturnType.isAssignableFrom(resultType))) {
						throw new ServerGwtRpcConnectionException(
								"Return value of type "
										+ resultType.getName()
										+ " is not assignable to required return type "
										+ this.requiredReturnType.getName()
										+ " for GWT RPC");
					}
				}

				// Obtain the request
				RPCRequest rpc = this.getRpcRequest();

				// Obtain the response payload
				String payload;
				try {
					payload = RPC.encodeResponseForSuccess(rpc.getMethod(),
							result, rpc.getSerializationPolicy(),
							rpc.getFlags());
				} catch (Exception ex) {
					// Indicate failure encoding response
					this.onFailure(ex);
					return;
				}

				// Send response
				this.sendResponse(payload);

			} catch (Exception ex) {
				// Ensure always propagate appropriately (unless serious)
				throw ServerGwtRpcConnectionException.newException(ex);
			}
		}

		@Override
		public synchronized void onFailure(Throwable caught) {
			try {

				// Ensure can send a response
				this.ensureCanSendResponse();

				// Attempt to obtain RPC request
				RPCRequest rpc = null;
				try {
					rpc = this.getRpcRequest();
				} catch (Throwable ex) {
					// send failure without specific RPC request details
				}

				// Obtain RPC request details
				SerializationPolicy serializationPolicy;
				int flags;
				if (rpc != null) {
					// Use details from request
					serializationPolicy = rpc.getSerializationPolicy();
					flags = rpc.getFlags();
				} else {
					// Use default details
					serializationPolicy = RPC.getDefaultSerializationPolicy();
					flags = 0;
				}

				// Obtain the response payload
				String payload;
				try {
					payload = RPC.encodeResponseForFailure(null, caught,
							serializationPolicy, flags);
				} catch (Exception ex) {
					// Indicate failure encoding response
					this.sendFailure(ex);
					return;
				}

				// Send response
				this.sendResponse(payload);

			} catch (Exception ex) {
				// Ensure always propagate appropriately (unless serious)
				throw ServerGwtRpcConnectionException.newException(ex);
			}
		}
	}

}