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

import net.officefloor.plugin.cometd.publish.TransportLine;
import net.officefloor.plugin.cometd.publish.TransportServerSession;

import org.cometd.bayeux.Session;
import org.cometd.bayeux.server.LocalSession;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.bayeux.server.ServerMessage.Mutable;
import org.cometd.bayeux.server.ServerSession;

/**
 * {@link ServerSession} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ServerSessionImpl implements ServerSession, TransportServerSession {

	/**
	 * {@link TransportLine} instances.
	 */
	private final List<TransportLine> lines = new LinkedList<TransportLine>();

	/*
	 * ================== TransportServerSession ================
	 */

	@Override
	public void registerTransportLine(TransportLine line) {
		this.lines.add(line);
	}

	/*
	 * ===================== ServerSession ======================
	 */

	@Override
	public String getId() {
		// TODO implement Session.getId
		throw new UnsupportedOperationException("TODO implement Session.getId");
	}

	@Override
	public boolean isConnected() {
		// TODO implement Session.isConnected
		throw new UnsupportedOperationException(
				"TODO implement Session.isConnected");
	}

	@Override
	public boolean isHandshook() {
		// TODO implement Session.isHandshook
		throw new UnsupportedOperationException(
				"TODO implement Session.isHandshook");
	}

	@Override
	public void disconnect() {
		// TODO implement Session.disconnect
		throw new UnsupportedOperationException(
				"TODO implement Session.disconnect");
	}

	@Override
	public void setAttribute(String name, Object value) {
		// TODO implement Session.setAttribute
		throw new UnsupportedOperationException(
				"TODO implement Session.setAttribute");
	}

	@Override
	public Object getAttribute(String name) {
		// TODO implement Session.getAttribute
		throw new UnsupportedOperationException(
				"TODO implement Session.getAttribute");
	}

	@Override
	public Set<String> getAttributeNames() {
		// TODO implement Session.getAttributeNames
		throw new UnsupportedOperationException(
				"TODO implement Session.getAttributeNames");
	}

	@Override
	public Object removeAttribute(String name) {
		// TODO implement Session.removeAttribute
		throw new UnsupportedOperationException(
				"TODO implement Session.removeAttribute");
	}

	@Override
	public void batch(Runnable batch) {
		// TODO implement Session.batch
		throw new UnsupportedOperationException("TODO implement Session.batch");
	}

	@Override
	public void startBatch() {
		// TODO implement Session.startBatch
		throw new UnsupportedOperationException(
				"TODO implement Session.startBatch");
	}

	@Override
	public boolean endBatch() {
		// TODO implement Session.endBatch
		throw new UnsupportedOperationException(
				"TODO implement Session.endBatch");
	}

	@Override
	public void addExtension(Extension extension) {
		// TODO implement ServerSession.addExtension
		throw new UnsupportedOperationException(
				"TODO implement ServerSession.addExtension");
	}

	@Override
	public void removeExtension(Extension extension) {
		// TODO implement ServerSession.removeExtension
		throw new UnsupportedOperationException(
				"TODO implement ServerSession.removeExtension");
	}

	@Override
	public List<Extension> getExtensions() {
		// TODO implement ServerSession.getExtensions
		throw new UnsupportedOperationException(
				"TODO implement ServerSession.getExtensions");
	}

	@Override
	public void addListener(ServerSessionListener listener) {
		// TODO implement ServerSession.addListener
		throw new UnsupportedOperationException(
				"TODO implement ServerSession.addListener");
	}

	@Override
	public void removeListener(ServerSessionListener listener) {
		// TODO implement ServerSession.removeListener
		throw new UnsupportedOperationException(
				"TODO implement ServerSession.removeListener");
	}

	@Override
	public boolean isLocalSession() {
		// TODO implement ServerSession.isLocalSession
		throw new UnsupportedOperationException(
				"TODO implement ServerSession.isLocalSession");
	}

	@Override
	public LocalSession getLocalSession() {
		// TODO implement ServerSession.getLocalSession
		throw new UnsupportedOperationException(
				"TODO implement ServerSession.getLocalSession");
	}

	@Override
	public void deliver(Session from, Mutable message) {

		// Deliver to transport line
		TransportLine line = this.lines.remove(0);
		line.respond(message);
	}

	@Override
	public void deliver(Session from, String channel, Object data, String id) {
		// TODO implement ServerSession.deliver
		throw new UnsupportedOperationException(
				"TODO implement ServerSession.deliver");
	}

	@Override
	public Set<ServerChannel> getSubscriptions() {
		// TODO implement ServerSession.getSubscriptions
		throw new UnsupportedOperationException(
				"TODO implement ServerSession.getSubscriptions");
	}

	@Override
	public String getUserAgent() {
		// TODO implement ServerSession.getUserAgent
		throw new UnsupportedOperationException(
				"TODO implement ServerSession.getUserAgent");
	}

	@Override
	public long getInterval() {
		// TODO implement ServerSession.getInterval
		throw new UnsupportedOperationException(
				"TODO implement ServerSession.getInterval");
	}

	@Override
	public void setInterval(long interval) {
		// TODO implement ServerSession.setInterval
		throw new UnsupportedOperationException(
				"TODO implement ServerSession.setInterval");
	}

	@Override
	public long getTimeout() {
		// TODO implement ServerSession.getTimeout
		throw new UnsupportedOperationException(
				"TODO implement ServerSession.getTimeout");
	}

	@Override
	public void setTimeout(long timeout) {
		// TODO implement ServerSession.setTimeout
		throw new UnsupportedOperationException(
				"TODO implement ServerSession.setTimeout");
	}

}