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
package net.officefloor.plugin.web.http.route;

import java.io.IOException;
import java.io.Serializable;

import org.easymock.AbstractMatcher;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.web.http.application.HttpRequestState;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * Tests the {@link HttpUrlContinuation}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpUrlContinuationTest extends OfficeFrameTestCase {

	/**
	 * Records saving the request.
	 * 
	 * @param attributeKey
	 *            Attribute Key to save request within {@link HttpSession}.
	 * @param connection
	 *            Mock {@link ServerHttpConnection}.
	 * @param requestState
	 *            Mock {@link HttpRequestState}.
	 * @param session
	 *            Mock {@link HttpSession}.
	 * @param test
	 *            {@link OfficeFrameTestCase}.
	 */
	public static void recordSaveRequest(String attributeKey, ServerHttpConnection connection,
			HttpRequestState requestState, HttpSession session, OfficeFrameTestCase test) throws IOException {

		final Serializable connectionMomento = test.createMock(Serializable.class);
		final Serializable requestStateMomento = test.createMock(Serializable.class);

		// Record obtaining state
		test.recordReturn(connection, connection.exportState(), connectionMomento);
		test.recordReturn(requestState, requestState.exportState(), requestStateMomento);

		// Record storing within the session
		session.setAttribute(attributeKey, null);
		test.control(session).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				assertEquals("Incorrect attribute key", expected[0], actual[0]);
				assertNotNull("Should have request momento", actual[1]);
				return true;
			}
		});
	}

	/**
	 * Records reinstating the request.
	 * 
	 * @param isReinstate
	 *            Indicates whether request is available in {@link HttpSession}
	 *            to reinstate.
	 * @param attributeKey
	 *            Attribute key within {@link HttpSession} containing the
	 *            request state.
	 * @param connection
	 *            Mock {@link ServerHttpConnection}.
	 * @param requestState
	 *            Mock {@link HttpRequestState}.
	 * @param session
	 *            Mock {@link HttpSession}.
	 * @param test
	 *            {@link OfficeFrameTestCase}.
	 */
	public static void recordReinstateRequest(boolean isReinstate, String attributeKey, ServerHttpConnection connection,
			HttpRequestState requestState, HttpSession session, OfficeFrameTestCase test) throws IOException {

		// Determine if reinstate
		if (!isReinstate) {
			// Request momento not available
			test.recordReturn(session, session.getAttribute(attributeKey), null);
			return;
		}

		// Record reinstating the request
		final Serializable connectionMomento = test.createMock(Serializable.class);
		final Serializable requestStateMomento = test.createMock(Serializable.class);

		// Record obtaining the request momento
		Serializable requestMomento = createRequestStateMomento(connectionMomento, requestStateMomento);
		test.recordReturn(session, session.getAttribute(attributeKey), requestMomento);

		// Load the state
		connection.importState(connectionMomento);
		requestState.importState(requestStateMomento);
	}

	/**
	 * Creates the request state momento.
	 * 
	 * @param connectionMomento
	 *            {@link ServerHttpConnection} momento.
	 * @param requestStateMomento
	 *            {@link HttpRequestState} momento.
	 * @return Request state momento.
	 */
	public static Serializable createRequestStateMomento(Serializable connectionMomento,
			Serializable requestStateMomento) throws IOException {
		return new RequestStateMomentoExtractor(connectionMomento, requestStateMomento).extractRedirectStateMomento();
	}

	/**
	 * Enables obtaining the redirect state momento.
	 */
	private static class RequestStateMomentoExtractor extends OfficeFrameTestCase {

		/**
		 * {@link ServerHttpConnection} momento.
		 */
		private final Serializable connectionMomento;

		/**
		 * {@link HttpRequestState} momento.
		 */
		private final Serializable requestStateMomento;

		/**
		 * Initiate.
		 * 
		 * @param connectionMomento
		 *            {@link ServerHttpConnection} momento.
		 * @param requestStateMomento
		 *            {@link HttpRequestState} momento.
		 */
		public RequestStateMomentoExtractor(Serializable connectionMomento, Serializable requestStateMomento) {
			this.connectionMomento = connectionMomento;
			this.requestStateMomento = requestStateMomento;
		}

		/**
		 * Extracts the redirect state momento.
		 * 
		 * @return Redirect state momento.
		 */
		public Serializable extractRedirectStateMomento() throws IOException {

			final String KEY = "KEY";

			final ServerHttpConnection connection = this.createMock(ServerHttpConnection.class);
			final HttpRequestState requestState = this.createMock(HttpRequestState.class);
			final HttpSession session = this.createMock(HttpSession.class);

			// Record saving request
			this.recordReturn(connection, connection.exportState(), this.connectionMomento);
			this.recordReturn(requestState, requestState.exportState(), this.requestStateMomento);

			// Record and capture request state momento
			final Serializable[] redirectStateMomento = new Serializable[1];
			session.setAttribute(KEY, null);
			this.control(session).setMatcher(new AbstractMatcher() {
				@Override
				public boolean matches(Object[] expected, Object[] actual) {
					// Capture the redirect state momento
					redirectStateMomento[0] = (Serializable) actual[1];
					return true;
				}
			});

			// Run redirect to extract the redirect state momento
			this.replayMockObjects();
			HttpUrlContinuation.saveRequest(KEY, connection, requestState, session);
			this.verifyMockObjects();

			// Return the extracted redirect state momento
			return redirectStateMomento[0];
		}
	}

	/**
	 * Ensure can create an instance.
	 */
	public void testInstance() {

		// Ensure correct instance values
		HttpUrlContinuation continuation = new HttpUrlContinuation("FUNCTION", Boolean.TRUE);
		assertEquals("Incorrect function", "FUNCTION", continuation.getFunctionName());
		assertTrue("Should be secure", continuation.isSecure().booleanValue());

		// Ensure secure may be null
		assertNull("May be null secure value", new HttpUrlContinuation("FUNCTION", null).isSecure());
	}

	/**
	 * Ensure can save request.
	 */
	public void testSaveRequest() throws Exception {

		final ServerHttpConnection connection = this.createMock(ServerHttpConnection.class);
		final HttpRequestState requestState = this.createMock(HttpRequestState.class);
		final HttpSession session = this.createMock(HttpSession.class);

		// Record saving request
		recordSaveRequest("KEY", connection, requestState, session, this);

		// Test
		this.replayMockObjects();
		HttpUrlContinuation.saveRequest("KEY", connection, requestState, session);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can reinstate the request.
	 */
	public void testReinstateRequest() throws Exception {

		final ServerHttpConnection connection = this.createMock(ServerHttpConnection.class);
		final HttpRequestState requestState = this.createMock(HttpRequestState.class);
		final HttpSession session = this.createMock(HttpSession.class);

		// Record reinstating the request
		recordReinstateRequest(true, "KEY", connection, requestState, session, this);

		// Test
		this.replayMockObjects();
		boolean isReinstated = HttpUrlContinuation.reinstateRequest("KEY", connection, requestState, session);
		assertTrue("Request should be reinstated", isReinstated);
		this.verifyMockObjects();
	}

	/**
	 * Ensure not reinstate the request if no state found.
	 */
	public void testNotReinstateRequest() throws Exception {

		final ServerHttpConnection connection = this.createMock(ServerHttpConnection.class);
		final HttpRequestState requestState = this.createMock(HttpRequestState.class);
		final HttpSession session = this.createMock(HttpSession.class);

		// Record not reinstating the request
		recordReinstateRequest(false, "KEY", connection, requestState, session, this);

		// Test
		this.replayMockObjects();
		boolean isReinstated = HttpUrlContinuation.reinstateRequest("KEY", connection, requestState, session);
		assertFalse("Request should not be reinstated", isReinstated);
		this.verifyMockObjects();
	}

}