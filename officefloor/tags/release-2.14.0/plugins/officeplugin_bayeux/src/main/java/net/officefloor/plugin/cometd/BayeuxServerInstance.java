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
package net.officefloor.plugin.cometd;

import java.util.List;
import java.util.Set;

import org.cometd.bayeux.Transport;
import org.cometd.bayeux.server.BayeuxContext;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ConfigurableServerChannel.Initializer;
import org.cometd.bayeux.server.LocalSession;
import org.cometd.bayeux.server.SecurityPolicy;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.bayeux.server.ServerMessage.Mutable;
import org.cometd.bayeux.server.ServerSession;

/**
 * {@link BayeuxServer} instance for a particular request.
 * 
 * @author Daniel Sagenschneider
 */
public class BayeuxServerInstance implements BayeuxServer {

	/**
	 * {@link BayeuxServerImpl}.
	 */
	private final BayeuxServerImpl server;

	/**
	 * {@link ServerSession} for this instance.
	 */
	private final ServerSession session;

	/**
	 * Initiate.
	 * 
	 * @param server
	 *            {@link BayeuxServerImpl}.
	 * @param session
	 *            {@link ServerSession} for this instance.
	 */
	public BayeuxServerInstance(BayeuxServerImpl server, ServerSession session) {
		this.server = server;
		this.session = session;
	}

	/*
	 * =================== BayeuxServer =====================
	 */

	// --------------- Instance Use Specific ----------------

	@Override
	public Transport getCurrentTransport() {
		// TODO implement BayeuxServer.getCurrentTransport
		throw new UnsupportedOperationException(
				"TODO implement BayeuxServer.getCurrentTransport");
	}

	@Override
	public BayeuxContext getContext() {
		// TODO implement BayeuxServer.getContext
		throw new UnsupportedOperationException(
				"TODO implement BayeuxServer.getContext");
	}

	// ---------------- Server Use Specific -----------------

	@Override
	public boolean createIfAbsent(String channelId, Initializer... initializers) {
		return this.server.createIfAbsent(channelId, initializers);
	}

	@Override
	public ServerChannel getChannel(String channelId) {
		return this.server.getChannel(channelId);
	}

	@Override
	public List<ServerChannel> getChannels() {
		// TODO implement BayeuxServer.getChannels
		throw new UnsupportedOperationException(
				"TODO implement BayeuxServer.getChannels");
	}

	@Override
	public ServerSession getSession(String clientId) {
		return this.server.getSession(clientId);
	}

	@Override
	public List<ServerSession> getSessions() {
		// TODO implement BayeuxServer.getSessions
		throw new UnsupportedOperationException(
				"TODO implement BayeuxServer.getSessions");
	}

	@Override
	public LocalSession newLocalSession(String idHint) {
		// TODO implement BayeuxServer.newLocalSession
		throw new UnsupportedOperationException(
				"TODO implement BayeuxServer.newLocalSession");
	}

	@Override
	public Mutable newMessage() {
		return new MessageImpl();
	}

	// ----------- Server Configuration Specific ------------

	@Override
	public void addListener(BayeuxServerListener listener) {
		this.server.addListener(listener);
	}

	@Override
	public void removeListener(BayeuxServerListener listener) {
		// TODO implement BayeuxServer.removeListener
		throw new UnsupportedOperationException(
				"TODO implement BayeuxServer.removeListener");
	}

	@Override
	public Set<String> getKnownTransportNames() {
		// TODO implement Bayeux.getKnownTransportNames
		throw new UnsupportedOperationException(
				"TODO implement Bayeux.getKnownTransportNames");
	}

	@Override
	public Transport getTransport(String transport) {
		// TODO implement Bayeux.getTransport
		throw new UnsupportedOperationException(
				"TODO implement Bayeux.getTransport");
	}

	@Override
	public List<String> getAllowedTransports() {
		// TODO implement Bayeux.getAllowedTransports
		throw new UnsupportedOperationException(
				"TODO implement Bayeux.getAllowedTransports");
	}

	@Override
	public Object getOption(String qualifiedName) {
		// TODO implement Bayeux.getOption
		throw new UnsupportedOperationException(
				"TODO implement Bayeux.getOption");
	}

	@Override
	public void setOption(String qualifiedName, Object value) {
		// TODO implement Bayeux.setOption
		throw new UnsupportedOperationException(
				"TODO implement Bayeux.setOption");
	}

	@Override
	public Set<String> getOptionNames() {
		// TODO implement Bayeux.getOptionNames
		throw new UnsupportedOperationException(
				"TODO implement Bayeux.getOptionNames");
	}

	@Override
	public void addExtension(Extension extension) {
		this.server.addExtension(extension);
	}

	@Override
	public void removeExtension(Extension extension) {
		// TODO implement BayeuxServer.removeExtension
		throw new UnsupportedOperationException(
				"TODO implement BayeuxServer.removeExtension");
	}

	@Override
	public List<Extension> getExtensions() {
		// TODO implement BayeuxServer.getExtensions
		throw new UnsupportedOperationException(
				"TODO implement BayeuxServer.getExtensions");
	}

	@Override
	public SecurityPolicy getSecurityPolicy() {
		// TODO implement BayeuxServer.getSecurityPolicy
		throw new UnsupportedOperationException(
				"TODO implement BayeuxServer.getSecurityPolicy");
	}

	@Override
	public void setSecurityPolicy(SecurityPolicy securityPolicy) {
		this.server.setSecurityPolicy(securityPolicy);
	}

}