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
import java.util.Map;
import java.util.Set;

import org.cometd.bayeux.client.ClientSession;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.cometd.bayeux.server.LocalSession;

/**
 * {@link ClientSession} local to the server for {@link LocalSession}.
 * 
 * @author Daniel Sagenschneider
 */
public class LocalClientSession implements ClientSession {

	/*
	 * ======================= ClientSession ========================
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
		// TODO implement ClientSession.addExtension
		throw new UnsupportedOperationException(
				"TODO implement ClientSession.addExtension");
	}

	@Override
	public void removeExtension(Extension extension) {
		// TODO implement ClientSession.removeExtension
		throw new UnsupportedOperationException(
				"TODO implement ClientSession.removeExtension");
	}

	@Override
	public List<Extension> getExtensions() {
		// TODO implement ClientSession.getExtensions
		throw new UnsupportedOperationException(
				"TODO implement ClientSession.getExtensions");
	}

	@Override
	public void handshake() {
		// TODO implement ClientSession.handshake
		throw new UnsupportedOperationException(
				"TODO implement ClientSession.handshake");
	}

	@Override
	public void handshake(Map<String, Object> template) {
		// TODO implement ClientSession.handshake
		throw new UnsupportedOperationException(
				"TODO implement ClientSession.handshake");
	}

	@Override
	public ClientSessionChannel getChannel(String channelName) {
		// TODO implement ClientSession.getChannel
		throw new UnsupportedOperationException(
				"TODO implement ClientSession.getChannel");
	}

}