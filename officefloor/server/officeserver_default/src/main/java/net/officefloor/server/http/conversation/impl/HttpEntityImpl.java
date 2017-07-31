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
package net.officefloor.server.http.conversation.impl;

import java.io.Serializable;

import net.officefloor.server.http.conversation.HttpEntity;
import net.officefloor.server.stream.ServerInputStream;
import net.officefloor.server.stream.impl.NotAllDataAvailableException;
import net.officefloor.server.stream.impl.ServerInputStreamImpl;

/**
 * {@link HttpEntity} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpEntityImpl implements HttpEntity {

	/**
	 * {@link ServerInputStream} to the entity content
	 */
	private final ServerInputStreamImpl content;

	/**
	 * Initiate.
	 * 
	 * @param content
	 *            {@link ServerInputStream} to the entity content.
	 */
	public HttpEntityImpl(ServerInputStreamImpl content) {
		this.content = content;
	}

	/*
	 * =============== HttpEntity ========================
	 */

	@Override
	public ServerInputStream getInputStream() {
		return this.content;
	}

	@Override
	public Serializable exportState() throws NotAllDataAvailableException {
		return this.content.exportState();
	}

}