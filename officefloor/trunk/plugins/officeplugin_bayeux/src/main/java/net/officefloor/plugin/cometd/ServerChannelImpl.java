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

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.cometd.bayeux.ChannelId;
import org.cometd.bayeux.Session;
import org.cometd.bayeux.server.Authorizer;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.bayeux.server.ServerMessage.Mutable;
import org.cometd.bayeux.server.ServerSession;

/**
 * {@link ServerChannel} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ServerChannelImpl implements ServerChannel {

	/**
	 * Indicates of this {@link ServerChannel} is persistent.
	 */
	private volatile boolean isPersistent = false;

	/**
	 * Subscribed {@link ServerSession} instances to this {@link ServerChannel}.
	 */
	private final List<ServerSession> subscriptions = new LinkedList<ServerSession>();

	/*
	 * ==================== ServerChannel ======================
	 */

	@Override
	public void addListener(ServerChannelListener listener) {
		// TODO implement ConfigurableServerChannel.addListener
		throw new UnsupportedOperationException(
				"TODO implement ConfigurableServerChannel.addListener");
	}

	@Override
	public void removeListener(ServerChannelListener listener) {
		// TODO implement ConfigurableServerChannel.removeListener
		throw new UnsupportedOperationException(
				"TODO implement ConfigurableServerChannel.removeListener");
	}

	@Override
	public List<ServerChannelListener> getListeners() {
		// TODO implement ConfigurableServerChannel.getListeners
		throw new UnsupportedOperationException(
				"TODO implement ConfigurableServerChannel.getListeners");
	}

	@Override
	public boolean isLazy() {
		// TODO implement ConfigurableServerChannel.isLazy
		throw new UnsupportedOperationException(
				"TODO implement ConfigurableServerChannel.isLazy");
	}

	@Override
	public void setLazy(boolean lazy) {
		// TODO implement ConfigurableServerChannel.setLazy
		throw new UnsupportedOperationException(
				"TODO implement ConfigurableServerChannel.setLazy");
	}

	@Override
	public long getLazyTimeout() {
		// TODO implement ConfigurableServerChannel.getLazyTimeout
		throw new UnsupportedOperationException(
				"TODO implement ConfigurableServerChannel.getLazyTimeout");
	}

	@Override
	public void setLazyTimeout(long lazyTimeout) {
		// TODO implement ConfigurableServerChannel.setLazyTimeout
		throw new UnsupportedOperationException(
				"TODO implement ConfigurableServerChannel.setLazyTimeout");
	}

	@Override
	public boolean isPersistent() {
		// TODO implement ConfigurableServerChannel.isPersistent
		throw new UnsupportedOperationException(
				"TODO implement ConfigurableServerChannel.isPersistent");
	}

	@Override
	public void setPersistent(boolean persistent) {
		this.isPersistent = persistent;
	}

	@Override
	public void addAuthorizer(Authorizer authorizer) {
		// TODO implement ConfigurableServerChannel.addAuthorizer
		throw new UnsupportedOperationException(
				"TODO implement ConfigurableServerChannel.addAuthorizer");
	}

	@Override
	public void removeAuthorizer(Authorizer authorizer) {
		// TODO implement ConfigurableServerChannel.removeAuthorizer
		throw new UnsupportedOperationException(
				"TODO implement ConfigurableServerChannel.removeAuthorizer");
	}

	@Override
	public List<Authorizer> getAuthorizers() {
		// TODO implement ConfigurableServerChannel.getAuthorizers
		throw new UnsupportedOperationException(
				"TODO implement ConfigurableServerChannel.getAuthorizers");
	}

	@Override
	public String getId() {
		// TODO implement Channel.getId
		throw new UnsupportedOperationException("TODO implement Channel.getId");
	}

	@Override
	public ChannelId getChannelId() {
		// TODO implement Channel.getChannelId
		throw new UnsupportedOperationException(
				"TODO implement Channel.getChannelId");
	}

	@Override
	public boolean isMeta() {
		// TODO implement Channel.isMeta
		throw new UnsupportedOperationException("TODO implement Channel.isMeta");
	}

	@Override
	public boolean isService() {
		// TODO implement Channel.isService
		throw new UnsupportedOperationException(
				"TODO implement Channel.isService");
	}

	@Override
	public boolean isBroadcast() {
		// TODO implement Channel.isBroadcast
		throw new UnsupportedOperationException(
				"TODO implement Channel.isBroadcast");
	}

	@Override
	public boolean isWild() {
		// TODO implement Channel.isWild
		throw new UnsupportedOperationException("TODO implement Channel.isWild");
	}

	@Override
	public boolean isDeepWild() {
		// TODO implement Channel.isDeepWild
		throw new UnsupportedOperationException(
				"TODO implement Channel.isDeepWild");
	}

	@Override
	public void setAttribute(String name, Object value) {
		// TODO implement Channel.setAttribute
		throw new UnsupportedOperationException(
				"TODO implement Channel.setAttribute");
	}

	@Override
	public Object getAttribute(String name) {
		// TODO implement Channel.getAttribute
		throw new UnsupportedOperationException(
				"TODO implement Channel.getAttribute");
	}

	@Override
	public Set<String> getAttributeNames() {
		// TODO implement Channel.getAttributeNames
		throw new UnsupportedOperationException(
				"TODO implement Channel.getAttributeNames");
	}

	@Override
	public Object removeAttribute(String name) {
		// TODO implement Channel.removeAttribute
		throw new UnsupportedOperationException(
				"TODO implement Channel.removeAttribute");
	}

	@Override
	public Set<ServerSession> getSubscribers() {
		// TODO implement ServerChannel.getSubscribers
		throw new UnsupportedOperationException(
				"TODO implement ServerChannel.getSubscribers");
	}

	@Override
	public boolean subscribe(ServerSession session) {
		
		// Subscribe
		this.subscriptions.add(session);
		
		// Successfully subscribed
		return true;
	}

	@Override
	public boolean unsubscribe(ServerSession session) {
		// TODO implement ServerChannel.unsubscribe
		throw new UnsupportedOperationException(
				"TODO implement ServerChannel.unsubscribe");
	}

	@Override
	public void publish(Session from, Mutable message) {
		
		// Publish the message to the subscribers
		for (ServerSession session : this.subscriptions) {
			session.deliver(from, message);
		}
	}

	@Override
	public void publish(Session from, Object data, String id) {
		// TODO implement ServerChannel.publish
		throw new UnsupportedOperationException(
				"TODO implement ServerChannel.publish");
	}

	@Override
	public void remove() {
		// TODO implement ServerChannel.remove
		throw new UnsupportedOperationException(
				"TODO implement ServerChannel.remove");
	}

}