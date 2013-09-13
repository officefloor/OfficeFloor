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
package net.officefloor.plugin.bayeux;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.bayeux.adapt.SecurityPolicyAdapter;
import net.officefloor.plugin.bayeux.transport.TransportBayeuxServer;
import net.officefloor.plugin.bayeux.transport.TransportMessage;
import net.officefloor.plugin.bayeux.transport.TransportMessage.TransportMutable;
import net.officefloor.plugin.bayeux.transport.disconnect.Disconnect;
import net.officefloor.plugin.bayeux.transport.handshake.SuccessfulHandshake;
import net.officefloor.plugin.bayeux.transport.handshake.UnsuccessfulHandshake;

import org.cometd.bayeux.Channel;
import org.cometd.bayeux.Message;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ServerMessage;
import org.cometd.bayeux.server.ServerSession;
import org.junit.Ignore;

/**
 * Tests the {@link BayeuxServer} for {@link Message} communication.
 * 
 * @author Daniel Sagenschneider
 */
@Ignore("TODO implement tests and functionality")
public class BayeuxServerMessageTest extends OfficeFrameTestCase {

	/**
	 * Client / {@link ServerSession} identifier.
	 */
	private static final String CLIENT_ID = "CLIENT_ID";

	/**
	 * {@link TransportBayeuxServer}.
	 */
	private final TransportBayeuxServer server = new BayeuxServerImpl(
			new SessionIdentifierGenerator() {
				@Override
				public String newSessionId() {
					return CLIENT_ID;
				}
			});

	/**
	 * Ensure can handshake with {@link BayeuxServer}.
	 */
	public void testSuccessfulHandshake() {

		// Create the request to handshake
		TransportMutable request = this.server.createMessage();
		request.setChannel(Channel.META_HANDSHAKE);
		request.setVersion("1.0");
		request.setSupportedConnectionTypes("long-polling", "callback-polling",
				"iframe");

		// Undertake handshake
		MockHandshakeCallback callback = new MockHandshakeCallback();
		this.server.handshake(request, callback);
		SuccessfulHandshake result = callback.getSuccessfulHandshake();

		// Ensure enough information to send response
		TransportMessage response = result.getResponse();
		MessageValidateUtil.assertMessage(response, Message.CHANNEL_FIELD,
				Channel.META_HANDSHAKE, Message.VERSION_FIELD, "1.0",
				Message.SUPPORTED_CONNECTION_TYPES_FIELD,
				"[long-polling,callback-polling]", Message.CLIENT_ID_FIELD,
				CLIENT_ID, Message.SUCCESSFUL_FIELD, String.valueOf(true),
				TransportMessage.AUTH_SUCCESSFUL_FIELD, String.valueOf(true),
				Message.ADVICE_FIELD, "{" + Message.RECONNECT_FIELD + "="
						+ Message.RECONNECT_RETRY_VALUE + "}");
	}

	/**
	 * Ensure unsuccessful handshake if not authenticated.
	 */
	public void testUnsuccessfulHandshake() {

		// Disallow handshake
		BayeuxServer bayeuxServer = this.server.getBayeuxServer();
		bayeuxServer.setSecurityPolicy(new SecurityPolicyAdapter() {
			@Override
			public boolean canHandshake(BayeuxServer server,
					ServerSession session, ServerMessage message) {
				return false;
			}
		});

		// Create the message
		TransportMutable message = this.server.createMessage();
		message.setChannel(Channel.META_HANDSHAKE);
		message.setVersion("1.0");
		message.setSupportedConnectionTypes("long-polling", "callback-polling",
				"iframe");

		// Undertake handshake
		MockHandshakeCallback callback = new MockHandshakeCallback();
		this.server.handshake(message, callback);
		UnsuccessfulHandshake result = callback.getUnsuccessfulHandshake();

		// Ensure enough information to send response
		TransportMessage response = result.getResponse();
		MessageValidateUtil.assertMessage(response, Message.CHANNEL_FIELD,
				Channel.META_HANDSHAKE, Message.VERSION_FIELD, "1.0",
				Message.SUPPORTED_CONNECTION_TYPES_FIELD,
				"[long-polling,callback-polling]", Message.CLIENT_ID_FIELD,
				CLIENT_ID, Message.SUCCESSFUL_FIELD, String.valueOf(false),
				Message.ERROR_FIELD, "Authentication failed",
				Message.ADVICE_FIELD, "{" + Message.RECONNECT_FIELD + "="
						+ Message.RECONNECT_NONE_VALUE + "}");
	}

	/**
	 * Ensure able to disconnect.
	 */
	public void testDisconnect() {

		// Handshake
		this.doHandshake();

		// Create the disconnect request
		TransportMutable request = this.server.createMessage();
		request.setChannel(Channel.META_DISCONNECT);
		request.setClientId(CLIENT_ID);

		// Undertake disconnect
		MockDisconnectCallback callback = new MockDisconnectCallback();
		this.server.disconnect(request, null);
		Disconnect result = callback.getDisconnect();

		// Ensure enough information to send response
		TransportMessage response = result.getResponse();
		MessageValidateUtil.assertMessage(response, Message.CHANNEL_FIELD,
				Channel.META_DISCONNECT, Message.CLIENT_ID_FIELD, CLIENT_ID,
				Message.SUCCESSFUL_FIELD, String.valueOf(true));
	}

	/**
	 * Undertakes handshake with the server.
	 */
	private void doHandshake() {

		// Create the handshake request
		TransportMutable request = this.server.createMessage();
		request.setChannel(Channel.META_HANDSHAKE);
		request.setVersion("1.0");
		request.setSupportedConnectionTypes("long-polling", "callback-polling",
				"iframe");

		// Undertake handshake
		MockHandshakeCallback callback = new MockHandshakeCallback();
		this.server.handshake(request, callback);

		// Ensure successful
		assertNotNull("Handshake should be successful",
				callback.getSuccessfulHandshake());
	}

}