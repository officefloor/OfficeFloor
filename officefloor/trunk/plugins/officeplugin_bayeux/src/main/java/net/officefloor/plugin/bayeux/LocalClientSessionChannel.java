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

import java.util.List;
import java.util.Set;

import org.cometd.bayeux.ChannelId;
import org.cometd.bayeux.client.ClientSession;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.cometd.bayeux.server.LocalSession;

/**
 * {@link ClientSessionChannel} local to the server for a {@link LocalSession}.
 * 
 * @author Daniel Sagenschneider
 */
public class LocalClientSessionChannel implements ClientSessionChannel {

	/*
	 * ===================== ClientSessionChannel ========================
	 */

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
	public void addListener(ClientSessionChannelListener listener) {
		// TODO implement ClientSessionChannel.addListener
		throw new UnsupportedOperationException(
				"TODO implement ClientSessionChannel.addListener");
	}

	@Override
	public void removeListener(ClientSessionChannelListener listener) {
		// TODO implement ClientSessionChannel.removeListener
		throw new UnsupportedOperationException(
				"TODO implement ClientSessionChannel.removeListener");
	}

	@Override
	public List<ClientSessionChannelListener> getListeners() {
		// TODO implement ClientSessionChannel.getListeners
		throw new UnsupportedOperationException(
				"TODO implement ClientSessionChannel.getListeners");
	}

	@Override
	public ClientSession getSession() {
		// TODO implement ClientSessionChannel.getSession
		throw new UnsupportedOperationException(
				"TODO implement ClientSessionChannel.getSession");
	}

	@Override
	public void publish(Object data) {
		// TODO implement ClientSessionChannel.publish
		throw new UnsupportedOperationException(
				"TODO implement ClientSessionChannel.publish");
	}

	@Override
	public void publish(Object data, MessageListener listener) {
		// TODO implement ClientSessionChannel.publish
		throw new UnsupportedOperationException(
				"TODO implement ClientSessionChannel.publish");
	}

	@Override
	public void subscribe(MessageListener listener) {
		// TODO implement ClientSessionChannel.subscribe
		throw new UnsupportedOperationException(
				"TODO implement ClientSessionChannel.subscribe");
	}

	@Override
	public void unsubscribe(MessageListener listener) {
		// TODO implement ClientSessionChannel.unsubscribe
		throw new UnsupportedOperationException(
				"TODO implement ClientSessionChannel.unsubscribe");
	}

	@Override
	public void unsubscribe() {
		// TODO implement ClientSessionChannel.unsubscribe
		throw new UnsupportedOperationException(
				"TODO implement ClientSessionChannel.unsubscribe");
	}

	@Override
	public List<MessageListener> getSubscribers() {
		// TODO implement ClientSessionChannel.getSubscribers
		throw new UnsupportedOperationException(
				"TODO implement ClientSessionChannel.getSubscribers");
	}

	@Override
	public boolean release() {
		// TODO implement ClientSessionChannel.release
		throw new UnsupportedOperationException(
				"TODO implement ClientSessionChannel.release");
	}

	@Override
	public boolean isReleased() {
		// TODO implement ClientSessionChannel.isReleased
		throw new UnsupportedOperationException(
				"TODO implement ClientSessionChannel.isReleased");
	}

}
