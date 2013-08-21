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

import java.util.HashMap;
import java.util.Map;

import org.cometd.bayeux.ChannelId;
import org.cometd.bayeux.Message;
import org.cometd.bayeux.server.ServerMessage.Mutable;

/**
 * {@link Message} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class MessageImpl extends HashMap<String, Object> implements Mutable {

	@Override
	public org.cometd.bayeux.server.ServerMessage.Mutable getAssociated() {
		// TODO implement ServerMessage.getAssociated
		throw new UnsupportedOperationException(
				"TODO implement ServerMessage.getAssociated");
	}

	@Override
	public boolean isLazy() {
		// TODO implement ServerMessage.isLazy
		throw new UnsupportedOperationException(
				"TODO implement ServerMessage.isLazy");
	}

	@Override
	public Map<String, Object> getAdvice() {
		// TODO implement Message.getAdvice
		throw new UnsupportedOperationException(
				"TODO implement Message.getAdvice");
	}

	@Override
	public String getChannel() {
		// TODO implement Message.getChannel
		throw new UnsupportedOperationException(
				"TODO implement Message.getChannel");
	}

	@Override
	public ChannelId getChannelId() {
		// TODO implement Message.getChannelId
		throw new UnsupportedOperationException(
				"TODO implement Message.getChannelId");
	}

	@Override
	public String getClientId() {
		// TODO implement Message.getClientId
		throw new UnsupportedOperationException(
				"TODO implement Message.getClientId");
	}

	@Override
	public Object getData() {
		// TODO implement Message.getData
		throw new UnsupportedOperationException(
				"TODO implement Message.getData");
	}

	@Override
	public boolean isMeta() {
		// TODO implement Message.isMeta
		throw new UnsupportedOperationException("TODO implement Message.isMeta");
	}

	@Override
	public boolean isPublishReply() {
		// TODO implement Message.isPublishReply
		throw new UnsupportedOperationException(
				"TODO implement Message.isPublishReply");
	}

	@Override
	public boolean isSuccessful() {
		// TODO implement Message.isSuccessful
		throw new UnsupportedOperationException(
				"TODO implement Message.isSuccessful");
	}

	@Override
	public Map<String, Object> getDataAsMap() {
		// TODO implement Message.getDataAsMap
		throw new UnsupportedOperationException(
				"TODO implement Message.getDataAsMap");
	}

	@Override
	public Map<String, Object> getExt() {
		// TODO implement Message.getExt
		throw new UnsupportedOperationException("TODO implement Message.getExt");
	}

	@Override
	public String getId() {
		// TODO implement Message.getId
		throw new UnsupportedOperationException("TODO implement Message.getId");
	}

	@Override
	public String getJSON() {
		// TODO implement Message.getJSON
		throw new UnsupportedOperationException(
				"TODO implement Message.getJSON");
	}

	@Override
	public Map<String, Object> getAdvice(boolean create) {
		// TODO implement Mutable.getAdvice
		throw new UnsupportedOperationException(
				"TODO implement Mutable.getAdvice");
	}

	@Override
	public Map<String, Object> getDataAsMap(boolean create) {
		// TODO implement Mutable.getDataAsMap
		throw new UnsupportedOperationException(
				"TODO implement Mutable.getDataAsMap");
	}

	@Override
	public Map<String, Object> getExt(boolean create) {
		// TODO implement Mutable.getExt
		throw new UnsupportedOperationException("TODO implement Mutable.getExt");
	}

	@Override
	public void setChannel(String channel) {
		// TODO implement Mutable.setChannel
		throw new UnsupportedOperationException(
				"TODO implement Mutable.setChannel");
	}

	@Override
	public void setClientId(String clientId) {
		// TODO implement Mutable.setClientId
		throw new UnsupportedOperationException(
				"TODO implement Mutable.setClientId");
	}

	@Override
	public void setData(Object data) {
		// TODO implement Mutable.setData
		throw new UnsupportedOperationException(
				"TODO implement Mutable.setData");
	}

	@Override
	public void setId(String id) {
		// TODO implement Mutable.setId
		throw new UnsupportedOperationException("TODO implement Mutable.setId");
	}

	@Override
	public void setSuccessful(boolean successful) {
		// TODO implement Mutable.setSuccessful
		throw new UnsupportedOperationException(
				"TODO implement Mutable.setSuccessful");
	}

	@Override
	public void setAssociated(
			org.cometd.bayeux.server.ServerMessage.Mutable message) {
		// TODO implement Mutable.setAssociated
		throw new UnsupportedOperationException(
				"TODO implement Mutable.setAssociated");
	}

	@Override
	public void setLazy(boolean lazy) {
		// TODO implement Mutable.setLazy
		throw new UnsupportedOperationException(
				"TODO implement Mutable.setLazy");
	}

}