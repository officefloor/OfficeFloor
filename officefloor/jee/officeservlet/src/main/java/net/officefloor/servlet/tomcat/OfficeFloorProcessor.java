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

import java.io.IOException;
import java.util.Enumeration;
import java.util.concurrent.Executor;

import javax.servlet.RequestDispatcher;

import org.apache.catalina.connector.CoyoteAdapter;
import org.apache.coyote.AbstractProcessor;
import org.apache.coyote.ActionCode;
import org.apache.coyote.ActionHookLoader;
import org.apache.coyote.ContinueResponseTiming;
import org.apache.coyote.Processor;
import org.apache.coyote.Request;
import org.apache.coyote.Response;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.http.MimeHeaders;
import org.apache.tomcat.util.net.AbstractEndpoint.Handler.SocketState;
import org.apache.tomcat.util.net.SocketWrapperBase;

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.AsynchronousFlowCompletion;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpResponseHeaders;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.servlet.inject.InjectContext;

/**
 * {@link OfficeFloor} {@link Processor}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorProcessor extends AbstractProcessor {

	/**
	 * {@link Log}.
	 */
	private static final Log log = LogFactory.getLog(OfficeFloorProcessor.class);

	/**
	 * {@link ServerHttpConnection}.
	 */
	private final ServerHttpConnection connection;

	/**
	 * {@link AsynchronousFlow}.
	 */
	private final AsynchronousFlow asynchronousFlow;

	/**
	 * {@link AsynchronousFlowCompletion}.
	 */
	private final AsynchronousFlowCompletion asynchronousFlowCompletion;

	/**
	 * Instantiate.
	 * 
	 * @param protocol                   {@link OfficeFloorProtocol}.
	 * @param request                    {@link Request}.
	 * @param response                   {@link Response}.
	 * @param connection                 {@link ServerHttpConnection}.
	 * @param executor                   {@link Executor}.
	 * @param asynchronousFlow           {@link AsynchronousFlow}.
	 * @param asynchronousFlowCompletion {@link AsynchronousFlowCompletion}.
	 */
	public OfficeFloorProcessor(OfficeFloorProtocol protocol, Request request, Response response,
			ServerHttpConnection connection, Executor executor, AsynchronousFlow asynchronousFlow,
			AsynchronousFlowCompletion asynchronousFlowCompletion) {
		super(protocol.getAdapter(), request, response);
		this.connection = connection;
		this.asynchronousFlow = asynchronousFlow;
		this.asynchronousFlowCompletion = asynchronousFlowCompletion;
		this.setSocketWrapper(protocol.getOfficeFloorEndPoint().getOfficeFloorSocketWrapper());

		// Ensure complete async context
		ActionHookLoader.loadActionHook((actionCode, param) -> {

			// Determine actions to intercept
			switch (actionCode) {
			case ASYNC_RUN:

				// Execute the runnable
				Runnable runnable = (Runnable) param;
				InjectContext injectContext = (InjectContext) request
						.getAttribute(InjectContext.REQUEST_ATTRIBUTE_NAME);
				injectContext.synchroniseForAnotherThread();
				executor.execute(() -> {
					injectContext.activate();
					runnable.run();
				});
				return; // executed, so no further handling

			case ACK:
			case REQ_HOST_ADDR_ATTRIBUTE:
			case CLIENT_FLUSH:
				return; // no socket

			default:
				// carry on to action
				break;
			}

			// Undertake action
			this.action(actionCode, param);

			// Handle additional action
			switch (actionCode) {

			case DISPATCH_EXECUTE:
				// Determine if error in asynchronous runnable
				if ((!response.isCommitted()) && (response.isError())) {
					// Send failed response
					this.forceSendResponse();
					return; // complete
				}
				break;

			case ASYNC_COMPLETE:
				// Async complete so finish response
				this.forceSendResponse();
				break;

			default:
				// no additional action
				break;
			}
		}, request, response);
	}

	/**
	 * Force send response.
	 */
	private void forceSendResponse() {

		// Ensure can write completion
		org.apache.catalina.connector.Response httpResponse = (org.apache.catalina.connector.Response) this.response
				.getNote(CoyoteAdapter.ADAPTER_NOTES);
		httpResponse.setSuspended(false);

		// Flush data
		try {
			if (!httpResponse.isCommitted()) {
				httpResponse.flushBuffer();
			}
		} catch (IOException ex) {
			this.sendFailure(ex);
		}

		// Close
		this.action(ActionCode.CLOSE, null);
	}

	/**
	 * Sends the possible failure.
	 * 
	 * @param failure Optional failure to send.
	 * @return <code>true</code> if sent failure.
	 */
	private boolean sendFailure(Throwable failure) {

		// Obtain the possible exception
		if (failure == null) {
			failure = this.response.getErrorException();
		}
		if (failure == null) {
			org.apache.catalina.connector.Request httpRequest = (org.apache.catalina.connector.Request) this.request
					.getNote(CoyoteAdapter.ADAPTER_NOTES);
			failure = (Throwable) httpRequest.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
		}
		if ((failure == null) && (this.response.isError())) {
			int statusCode = this.response.getStatus();
			String message = this.response.getMessage();
			if ((message != null) && (message.length() > 0)) {
				failure = new HttpException(statusCode, message);
			}
		}
		if (failure == null) {
			return false; // no error
		}

		// Propagate failure
		Throwable finalFailure = failure;
		this.asynchronousFlow.complete(() -> {
			throw finalFailure;
		});

		// Failure sent
		return true;
	}

	/*
	 * ====================== Processor ========================
	 */

	@Override
	protected Log getLog() {
		return log;
	}

	@Override
	protected int available(boolean doRead) {
		try {
			return this.connection.getRequest().getEntity().available();
		} catch (IOException ex) {
			return 0;
		}
	}

	@Override
	protected void prepareResponse() throws IOException {

		// Copy details to response starting with status
		HttpResponse httpResponse = this.connection.getResponse();
		httpResponse.setStatus(HttpStatus.getHttpStatus(this.response.getStatus()));

		// Load the possible content type
		String contentType = this.response.getContentType();
		if (contentType != null) {
			httpResponse.setContentType(contentType, null);
		}

		// Load headers
		HttpResponseHeaders httpHeaders = httpResponse.getHeaders();
		MimeHeaders headers = this.response.getMimeHeaders();
		Enumeration<String> headerNames = headers.names();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			Enumeration<String> values = headers.values(headerName);
			while (values.hasMoreElements()) {
				String headerValue = values.nextElement();
				httpHeaders.addHeader(headerName, headerValue);
			}
		}

		// Entity already written
	}

	@Override
	protected void finishResponse() throws IOException {

		// Determine if send failure
		if (this.sendFailure(null)) {
			return;
		}

		// Successful
		this.asynchronousFlow.complete(this.asynchronousFlowCompletion);
	}

	@Override
	protected void setSwallowResponse() {
		// Allow swallowing response
	}

	/*
	 * ================== Processor (unused) ====================
	 */

	@Override
	protected void ack() {
		throw OfficeFloorSocketWrapper.noSocket();
	}

	@Override
	protected void ack(ContinueResponseTiming arg0) {
		throw OfficeFloorSocketWrapper.noSocket();
	}

	@Override
	public void pause() {
		throw OfficeFloorSocketWrapper.noSocket();
	}

	@Override
	protected void flush() throws IOException {
		throw OfficeFloorSocketWrapper.noSocket();
	}

	@Override
	protected void setRequestBody(ByteChunk body) {
		throw OfficeFloorSocketWrapper.noSocket();
	}

	@Override
	protected void disableSwallowRequest() {
		throw OfficeFloorSocketWrapper.noSocket();
	}

	@Override
	protected boolean isRequestBodyFullyRead() {
		throw OfficeFloorSocketWrapper.noSocket();
	}

	@Override
	protected void registerReadInterest() {
		throw OfficeFloorSocketWrapper.noSocket();
	}

	@Override
	protected boolean isReadyForWrite() {
		throw OfficeFloorSocketWrapper.noSocket();
	}

	@Override
	protected boolean isTrailerFieldsReady() {
		throw OfficeFloorSocketWrapper.noSocket();
	}

	@Override
	protected boolean flushBufferedWrite() throws IOException {
		throw OfficeFloorSocketWrapper.noSocket();
	}

	@Override
	protected SocketState dispatchEndRequest() throws IOException {
		throw OfficeFloorSocketWrapper.noSocket();
	}

	@Override
	protected SocketState service(SocketWrapperBase<?> socketWrapper) throws IOException {
		throw OfficeFloorSocketWrapper.noSocket();
	}

}
