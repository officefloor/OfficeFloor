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

import java.util.Queue;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.bayeux.publish.TransportLine;
import net.officefloor.plugin.bayeux.publish.TransportServerSession;

import org.cometd.bayeux.ChannelId;
import org.cometd.bayeux.Message;
import org.cometd.bayeux.server.Authorizer;
import org.cometd.bayeux.server.Authorizer.Operation;
import org.cometd.bayeux.server.Authorizer.Result;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.BayeuxServer.ChannelListener;
import org.cometd.bayeux.server.BayeuxServer.Extension;
import org.cometd.bayeux.server.BayeuxServer.SessionListener;
import org.cometd.bayeux.server.BayeuxServer.SubscriptionListener;
import org.cometd.bayeux.server.ConfigurableServerChannel;
import org.cometd.bayeux.server.ConfigurableServerChannel.Initializer;
import org.cometd.bayeux.server.SecurityPolicy;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.bayeux.server.ServerChannel.MessageListener;
import org.cometd.bayeux.server.ServerMessage;
import org.cometd.bayeux.server.ServerMessage.Mutable;
import org.cometd.bayeux.server.ServerSession;
import org.cometd.bayeux.server.ServerSession.DeQueueListener;
import org.cometd.bayeux.server.ServerSession.MaxQueueListener;
import org.cometd.bayeux.server.ServerSession.RemoveListener;
import org.easymock.AbstractMatcher;

/**
 * Tests the {@link BayeuxServerImpl}.
 * 
 * @author Daniel Sagenschneider
 */
public class BayeuxServerTest extends OfficeFrameTestCase {

	/**
	 * {@link BayeuxServer}.
	 */
	private BayeuxServer instance;

	/**
	 * {@link ServerChannel}.
	 */
	private ServerChannel channel;

	/**
	 * {@link ServerSession}.
	 */
	private ServerSession session;

	/**
	 * Ensure can subscribe to published event.
	 */
	public void testSubscribeToPublishedEvent() {

		final ChannelId channelId = new ChannelId("/test");

		// Record security policy (handshake to create session)
		final ServerMessage handshakeMessage = this
				.createMock(ServerMessage.class);
		final SecurityPolicy securityPolicy = this
				.createMock(SecurityPolicy.class);
		this.recordReturn(securityPolicy, securityPolicy.canHandshake(
				this.instance, this.session, handshakeMessage), true,
				new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {

						// Obtain the bayeux server
						BayeuxServer server = (BayeuxServer) actual[0];
						assertNotNull("Should have bayeux server", server);

						// Validate the new session
						ServerSession session = (ServerSession) actual[1];
						assertEquals(
								"Incorrect sesssion (before creation so not yet obtained)",
								"TEST", session.getId());

						// Validate message
						assertMessage(handshakeMessage, 2, actual);
						return true;
					}
				});

		// Record the adding of the session
		final SessionListener serverSessionListener = this
				.createMock(SessionListener.class);
		serverSessionListener.sessionAdded(this.session);
		this.control(serverSessionListener).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {

				// Validate the new session
				ServerSession session = (ServerSession) actual[0];
				assertEquals(
						"Incorrect sesssion (before creation so not yet obtained)",
						"TEST", session.getId());
				return true;
			}
		});

		// Record security policy (create channel)
		this.recordReturn(securityPolicy,
				securityPolicy.canCreate(null, null, "/test", null), true,
				new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						BayeuxServerTest.this.assertNewBayeuxServer(0, actual);

						// Validate remaining parameters
						assertNull("Should not have session", actual[1]);
						assertEquals("Incorrect channel ID", "/test", actual[2]);
						assertNull("Should be no message", actual[3]);
						return true;
					}
				});

		// Record configuring and adding new channel
		final ChannelListener channelListener = this
				.createMock(ChannelListener.class);
		AbstractMatcher channelMatcher = new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				// Channel not yet obtained, so check has correct id
				ServerChannel channel = (ServerChannel) actual[0];
				assertEquals("Incorrect channel", "/test", channel.getId());
				return true;
			}
		};
		channelListener.configureChannel(this.channel);
		this.control(channelListener).setMatcher(channelMatcher);
		channelListener.channelAdded(this.channel);
		this.control(channelListener).setMatcher(channelMatcher);

		// Record security policy (subscribe to channel)
		this.recordReturn(securityPolicy, securityPolicy.canSubscribe(
				this.instance, this.session, this.channel, null), true,
				new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						BayeuxServerTest.this.assertNewBayeuxServer(0, actual);
						BayeuxServerTest.this.assertServerSession(1, actual);
						BayeuxServerTest.this.assertServerChannel(2, actual);
						assertNull("Should not have subscribe message",
								actual[3]);
						return true;
					}
				});

		// Record authorizing subscription to channel
		Authorizer authorizer = this.createMock(Authorizer.class);
		this.recordReturn(authorizer, authorizer.authorize(Operation.SUBSCRIBE,
				channelId, this.session, null), Result.grant(),
				new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {

						// Ensure operation the same
						boolean isMatch = (expected[0].equals(actual[0]));

						// Ensure channel ID matches
						isMatch &= (expected[1].equals(actual[1]));

						// Validate session
						BayeuxServerTest.this.assertServerSession(2, actual);

						// Validate the message
						isMatch &= (expected[3] == null ? (actual[3] == null)
								: (expected[3].equals(actual[3])));

						// Indicate if match
						return isMatch;
					}
				});

		// Record server subscription
		final SubscriptionListener serverSubscriptionListener = this
				.createMock(SubscriptionListener.class);
		serverSubscriptionListener.subscribed(this.session, this.channel);
		this.control(serverSubscriptionListener).setMatcher(
				new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						BayeuxServerTest.this.assertServerSession(0, actual);
						BayeuxServerTest.this.assertServerChannel(1, actual);
						return true;
					}
				});

		// Record channel subscription
		org.cometd.bayeux.server.ServerChannel.SubscriptionListener channelSubscriptionListener = this
				.createMock(org.cometd.bayeux.server.ServerChannel.SubscriptionListener.class);
		channelSubscriptionListener.subscribed(this.session, this.channel);
		this.control(channelSubscriptionListener).setMatcher(
				new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						BayeuxServerTest.this.assertServerSession(0, actual);
						BayeuxServerTest.this.assertServerChannel(1, actual);
						return true;
					}
				});

		// Record security policy (publish)
		final Mutable publishMessage = this.createMock(Mutable.class);
		this.recordReturn(securityPolicy, securityPolicy.canPublish(
				this.instance, this.session, this.channel, publishMessage),
				true, new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						BayeuxServerTest.this.assertNewBayeuxServer(0, actual);
						BayeuxServerTest.this.assertServerSession(1, actual);
						BayeuxServerTest.this.assertServerChannel(2, actual);
						assertMessage(publishMessage, 3, actual);
						return true;
					}
				});

		// Record authorizing publishing to channel
		this.recordReturn(authorizer, authorizer.authorize(Operation.PUBLISH,
				channelId, this.session, publishMessage), Result.grant());

		// Record server extension receiving message
		Extension serverExtension = this.createMock(Extension.class);
		this.recordReturn(serverExtension,
				serverExtension.rcv(this.session, publishMessage), true,
				new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						BayeuxServerTest.this.assertServerSession(0, actual);
						assertMessage(publishMessage, 1, actual);
						return true;
					}
				});

		// Record session extension receiving message
		org.cometd.bayeux.server.ServerSession.Extension sessionExtension = this
				.createMock(org.cometd.bayeux.server.ServerSession.Extension.class);
		this.recordReturn(sessionExtension,
				sessionExtension.rcv(this.session, publishMessage), true,
				new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						BayeuxServerTest.this.assertServerSession(0, actual);
						assertMessage(publishMessage, 1, actual);
						return true;
					}
				});

		// Record the channel message listener
		MessageListener channelMessageListener = this
				.createMock(MessageListener.class);
		this.recordReturn(channelMessageListener, channelMessageListener
				.onMessage(this.session, this.channel, publishMessage), true,
				new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						BayeuxServerTest.this.assertServerSession(0, actual);
						BayeuxServerTest.this.assertServerChannel(1, actual);
						assertMessage(publishMessage, 2, actual);
						return true;
					}
				});

		// Record server extension sending message
		this.recordReturn(serverExtension, serverExtension.send(this.session,
				this.session, publishMessage), true, new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				BayeuxServerTest.this.assertServerSession(0, actual);
				BayeuxServerTest.this.assertServerSession(1, actual);
				assertMessage(publishMessage, 2, actual);
				return true;
			}
		});

		// Record session extension sending message
		this.recordReturn(sessionExtension,
				sessionExtension.send(this.session, publishMessage),
				publishMessage, new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						BayeuxServerTest.this.assertServerSession(0, actual);
						assertMessage(publishMessage, 1, actual);
						return true;
					}
				});

		// Record dequeuing message
		DeQueueListener deQueueListener = this
				.createMock(DeQueueListener.class);
		deQueueListener.deQueue(this.session, null);
		this.control(deQueueListener).setMatcher(new AbstractMatcher() {
			@Override
			@SuppressWarnings("unchecked")
			public boolean matches(Object[] expected, Object[] actual) {
				BayeuxServerTest.this.assertServerSession(0, actual);
				Queue<ServerMessage> queue = (Queue<ServerMessage>) actual[1];
				assertEquals("Should only be the one message", 1, queue.size());
				assertSame("Incorrect message in queue", publishMessage,
						queue.peek());
				return true;
			}
		});

		// Record allowing a further message
		MaxQueueListener maxQueueListener = this
				.createMock(MaxQueueListener.class);
		this.recordReturn(maxQueueListener, maxQueueListener.queueMaxed(
				this.session, this.session, publishMessage), true,
				new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						BayeuxServerTest.this.assertServerSession(0, actual);
						BayeuxServerTest.this.assertServerSession(1, actual);
						assertMessage(publishMessage, 2, actual);
						return true;
					}
				});

		// Record session listening on message
		org.cometd.bayeux.server.ServerSession.MessageListener sessionMessageListener = this
				.createMock(org.cometd.bayeux.server.ServerSession.MessageListener.class);
		this.recordReturn(sessionMessageListener, sessionMessageListener
				.onMessage(this.session, this.session, publishMessage), true,
				new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						BayeuxServerTest.this.assertServerSession(0, actual);
						BayeuxServerTest.this.assertServerSession(1, actual);
						assertMessage(publishMessage, 2, actual);
						return true;
					}
				});

		// Record the subscribed response
		final TransportLine line = this.createMock(TransportLine.class);
		line.respond(publishMessage);
		this.control(line).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				assertMessage(publishMessage, 0, actual);
				return true;
			}
		});

		// Record unsubscribe from channel as disconnecting the session
		serverSubscriptionListener.unsubscribed(this.session, this.channel);
		this.control(serverSubscriptionListener).setMatcher(
				new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						BayeuxServerTest.this.assertServerSession(0, actual);
						BayeuxServerTest.this.assertServerChannel(1, actual);
						return true;
					}
				});

		// Record channel unsubscription as disconnecting the session
		channelSubscriptionListener.unsubscribed(this.session, this.channel);
		this.control(channelSubscriptionListener).setMatcher(
				new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						BayeuxServerTest.this.assertServerSession(0, actual);
						BayeuxServerTest.this.assertServerChannel(1, actual);
						return true;
					}
				});

		// Record removing session as disconnected
		RemoveListener removeListener = this.createMock(RemoveListener.class);
		removeListener.removed(this.session, false);
		this.control(removeListener).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				BayeuxServerTest.this.assertServerSession(0, actual);
				assertFalse("Should be due to last subscription removed",
						((Boolean) actual[1]).booleanValue());
				return true;
			}
		});

		// Test
		this.replayMockObjects();

		// Create the server
		BayeuxServerImpl server = new BayeuxServerImpl();

		// Setup server listeners
		BayeuxServer serverSetup = new BayeuxServerInstance(server, null);
		serverSetup.addListener(channelListener);
		serverSetup.addListener(serverSessionListener);
		serverSetup.addListener(serverSubscriptionListener);
		serverSetup.addExtension(serverExtension);
		serverSetup.setSecurityPolicy(securityPolicy);

		// Add the transport server session (triggers listeners)
		TransportServerSession transport = server.addSession("TEST",
				handshakeMessage);

		// Register line with session
		transport.registerTransportLine(line);

		// Create the server instance
		this.instance = server.createBayeuxServerInstance(transport);

		// Create the channel
		boolean isInitialized = this.instance.createIfAbsent("/test",
				new Initializer() {
					@Override
					public void configureChannel(
							ConfigurableServerChannel channel) {
						channel.setPersistent(true);
					}
				});
		assertTrue("Should be initialised", isInitialized);

		// Obtain the channel
		this.channel = this.instance.getChannel("/test");

		// Configure the channel
		this.channel.addAuthorizer(authorizer);
		this.channel.addListener(channelSubscriptionListener);
		this.channel.addListener(channelMessageListener);

		// Obtain the server session
		this.session = this.instance.getSession("TEST");

		// Configure the session
		this.session.addListener(deQueueListener);
		this.session.addListener(maxQueueListener);
		this.session.addListener(sessionMessageListener);
		this.session.addListener(removeListener);
		this.session.addExtension(sessionExtension);

		// Subscribe to channel
		boolean isSubscribed = this.channel.subscribe(this.session);
		assertTrue("Should be subscribed successfully", isSubscribed);

		// Publish a message
		this.channel.publish(this.session, publishMessage);

		// Disconnect session (trigger unsubscribe and remove session)
		this.session.disconnect();

		// Verify functionality
		this.verifyMockObjects();
	}

	/**
	 * Asserts the {@link BayeuxServer}.
	 * 
	 * @param index
	 *            Index of the {@link BayeuxServer}.
	 * @param actual
	 *            Objects.
	 */
	private void assertBayeuxServer(int index, Object[] actual) {
		BayeuxServer server = (BayeuxServer) actual[index];
		assertSame("Incorrect bayeux server", this.instance, server);
	}

	/**
	 * Asserts a {@link BayeuxServer} is provided but different instance.
	 * 
	 * @param index
	 *            Index of the {@link BayeuxServer}.
	 * @param actual
	 *            Objects.
	 */
	private void assertNewBayeuxServer(int index, Object[] actual) {
		BayeuxServer server = (BayeuxServer) actual[index];
		assertNotNull("Should have server", server);
		assertNotSame("Should be difference server",
				BayeuxServerTest.this.instance, server);
	}

	/**
	 * Asserts the {@link ServerSession}.
	 * 
	 * @param index
	 *            Index of the {@link ServerSession}.
	 * @param actual
	 *            Objects.
	 */
	private void assertServerSession(int index, Object[] actual) {
		ServerSession session = (ServerSession) actual[index];
		assertSame("Incorrect session", this.session, session);
		assertEquals("Incorrect session ID", "TEST", session.getId());
	}

	/**
	 * Asserts the {@link ServerChannel}.
	 * 
	 * @param index
	 *            Index of the {@link ServerChannel}.
	 * @param actual
	 *            Objects.
	 */
	private void assertServerChannel(int index, Object[] actual) {
		ServerChannel channel = (ServerChannel) actual[index];
		assertSame("Incorrect channel", this.channel, channel);
		assertEquals("Incorrect channel ID", "/test", channel.getId());
	}

	/**
	 * Asserts the {@link Message}.
	 * 
	 * @param expectedMessage
	 *            Expected {@link ServerMessage}.
	 * @param index
	 *            Index of the {@link Message}.
	 * @param actual
	 *            Objects.
	 */
	public static void assertMessage(ServerMessage expectedMessage, int index,
			Object[] actual) {
		Message message = (Message) actual[index];
		assertSame("Incorrect message", expectedMessage, message);
	}

}