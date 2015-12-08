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
import net.officefloor.plugin.bayeux.transport.ConnectResult;
import net.officefloor.plugin.bayeux.transport.DisconnectResult;
import net.officefloor.plugin.bayeux.transport.HandshakeResult;
import net.officefloor.plugin.bayeux.transport.PublishResult;
import net.officefloor.plugin.bayeux.transport.SubscribeResult;
import net.officefloor.plugin.bayeux.transport.TransportBayeuxServer;
import net.officefloor.plugin.bayeux.transport.TransportMessage;
import net.officefloor.plugin.bayeux.transport.TransportMessage.TransportMutable;
import net.officefloor.plugin.bayeux.transport.TransportResult;
import net.officefloor.plugin.bayeux.transport.UnsubscribeResult;

import org.cometd.bayeux.Channel;
import org.cometd.bayeux.Message;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.bayeux.server.ServerMessage;
import org.cometd.bayeux.server.ServerSession;
import org.junit.Ignore;

/**
 * Tests the {@link BayeuxServer} for {@link Message} communication.
 * 
 * @author Daniel Sagenschneider
 */
@Ignore("TODO provide implementation of tests")
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
		MockTransportCallback<HandshakeResult> callback = new MockTransportCallback<HandshakeResult>();
		this.server.handshake(request, callback);

		// Ensure enough information to send response
		TransportMessage response = callback.getSuccessfulMessage();
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
		MockTransportCallback<HandshakeResult> callback = new MockTransportCallback<HandshakeResult>();
		this.server.handshake(message, callback);

		// Ensure enough information to send response
		TransportMessage response = callback.getUnsuccessfulMessage();
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
	 * Ensure able to connect.
	 */
	public void testConnect() {

		// Handshake
		this.doHandshake();

		// Create the connect request
		TransportMutable request = this.server.createMessage();
		request.setChannel(Channel.META_CONNECT);
		request.setClientId(CLIENT_ID);
		request.setConnectionType("long-polling");

		// Undertake connect
		MockTransportCallback<ConnectResult> callback = new MockTransportCallback<ConnectResult>();
		this.server.connect(request, null);

		// Ensure enough information to send the response
		TransportMessage response = callback.getSuccessfulMessage();
		MessageValidateUtil.assertMessage(response, Message.CHANNEL_FIELD,
				Channel.META_CONNECT, Message.SUCCESSFUL_FIELD, "true",
				Message.ERROR_FIELD, "", Message.CLIENT_ID_FIELD, CLIENT_ID,
				Message.TIMESTAMP_FIELD, "12:00:00 1970", Message.ADVICE_FIELD,
				"{" + Message.RECONNECT_FIELD + "="
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
		MockTransportCallback<DisconnectResult> callback = new MockTransportCallback<DisconnectResult>();
		this.server.disconnect(request, null);

		// Ensure enough information to send response
		TransportMessage response = callback.getSuccessfulMessage();
		MessageValidateUtil.assertMessage(response, Message.CHANNEL_FIELD,
				Channel.META_DISCONNECT, Message.CLIENT_ID_FIELD, CLIENT_ID,
				Message.SUCCESSFUL_FIELD, String.valueOf(true));
	}

	/**
	 * Ensure able to subscribe successfully.
	 */
	public void testSuccessfulSubscribe() {

		// Handshake
		this.doHandshake();

		// Create the subscribe request
		TransportMutable request = this.server.createMessage();
		request.setChannel(Channel.META_SUBSCRIBE);
		request.setClientId(CLIENT_ID);
		request.setSubscription("/foo/**");

		// Undertake the subscription
		MockTransportCallback<SubscribeResult> callback = new MockTransportCallback<SubscribeResult>();
		this.server.subscribe(request, callback);

		// Ensure enough information to send response
		TransportMessage response = callback.getSuccessfulMessage();
		MessageValidateUtil.assertMessage(response, Message.CHANNEL_FIELD,
				Channel.META_SUBSCRIBE, Message.CLIENT_ID_FIELD, CLIENT_ID,
				Message.SUBSCRIPTION_FIELD, "/foo/**",
				Message.SUCCESSFUL_FIELD, "true", Message.ERROR_FIELD, "");
	}

	/**
	 * Ensure able to not subscribe.
	 */
	public void testUnsuccessfulSubscribe() {

		// Disallow subscriptions
		BayeuxServer bayeuxServer = this.server.getBayeuxServer();
		bayeuxServer.setSecurityPolicy(new SecurityPolicyAdapter() {
			@Override
			public boolean canSubscribe(BayeuxServer server,
					ServerSession session, ServerChannel channel,
					ServerMessage message) {
				return false;
			}
		});

		// Handshake
		this.doHandshake();

		// Create the subscribe request
		TransportMutable request = this.server.createMessage();
		request.setChannel(Channel.META_SUBSCRIBE);
		request.setClientId(CLIENT_ID);
		request.setSubscription("/bar/baz");

		// Undertake the subscribe
		MockTransportCallback<SubscribeResult> callback = new MockTransportCallback<SubscribeResult>();
		this.server.subscribe(request, callback);

		// Ensure enough information to send response
		TransportMessage response = callback.getUnsuccessfulMessage();
		MessageValidateUtil.assertMessage(response, Message.CHANNEL_FIELD,
				Channel.META_SUBSCRIBE, Message.CLIENT_ID_FIELD, CLIENT_ID,
				Message.SUBSCRIPTION_FIELD, "/bar/baz",
				Message.SUCCESSFUL_FIELD, "false", Message.ERROR_FIELD,
				"403:/bar/baz:Permission Denied");
	}

	/**
	 * Ensure able to unsubscribe.
	 */
	public void testUnsubscribe() {

		// Subscribe
		this.doHandshake();
		this.doSubscribe("/foo/**");

		// Create the unsubscribe request
		TransportMutable request = this.server.createMessage();
		request.setChannel(Channel.META_UNSUBSCRIBE);
		request.setClientId(CLIENT_ID);
		request.setSubscription("/foo/**");

		// Undertake the unsubscribe
		MockTransportCallback<UnsubscribeResult> callback = new MockTransportCallback<UnsubscribeResult>();
		this.server.unsubscribe(request, callback);

		// Ensure enough information to send response
		TransportMessage response = callback.getSuccessfulMessage();
		MessageValidateUtil.assertMessage(response, Message.CHANNEL_FIELD,
				Channel.META_UNSUBSCRIBE, Message.CLIENT_ID_FIELD, CLIENT_ID,
				Message.SUBSCRIPTION_FIELD, "/foo/**",
				Message.SUCCESSFUL_FIELD, "true", Message.ERROR_FIELD, "");
	}

	/**
	 * Ensure able to publish.
	 */
	public void testPublish() {

		// Handshake
		this.doHandshake();

		// Create the publish request
		TransportMutable request = this.server.createMessage();
		request.setChannel("/some/channel");
		request.setClientId(CLIENT_ID);
		request.setData("some application string or JSON encoded object");
		request.setId("some unique message id");

		// Undertake the publish
		MockTransportCallback<PublishResult> callback = new MockTransportCallback<PublishResult>();
		this.server.publish(request, callback);

		// Ensure enough information to send response
		TransportMessage response = callback.getSuccessfulMessage();
		MessageValidateUtil.assertMessage(response, Message.CHANNEL_FIELD,
				"/some/channel", Message.SUCCESSFUL_FIELD, "true",
				Message.ID_FIELD, "some unique message id");
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

		// Undertake handshake (with generic callback implementation)
		MockTransportCallback<TransportResult> callback = new MockTransportCallback<TransportResult>();
		this.server.handshake(request, callback);

		// Ensure successful
		assertNotNull("Handshake should be successful",
				callback.getSuccessfulResult());
	}

	/**
	 * Undertakes subscribe with the server.
	 * 
	 * @param path
	 *            Path to subscribe.
	 */
	private void doSubscribe(String path) {

		// Create the subscribe request
		TransportMutable request = this.server.createMessage();
		request.setChannel(Channel.META_SUBSCRIBE);
		request.setClientId(CLIENT_ID);
		request.setSubscription(path);

		// Undertake subscribe (with generic callback implementation)
		MockTransportCallback<TransportResult> callback = new MockTransportCallback<TransportResult>();
		this.server.subscribe(request, callback);

		// Ensure successful
		assertNotNull("Subscribe to path " + path + " should be successful",
				callback.getSuccessfulResult());
	}

}