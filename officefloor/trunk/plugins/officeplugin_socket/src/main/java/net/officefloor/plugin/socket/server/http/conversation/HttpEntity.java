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
package net.officefloor.plugin.socket.server.http.conversation;

import java.io.Serializable;

import net.officefloor.plugin.stream.ServerInputStream;
import net.officefloor.plugin.stream.impl.NotAllDataAvailableException;

/**
 * HTTP entity.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpEntity {

	/**
	 * Obtains the {@link ServerInputStream} to the entity content.
	 * 
	 * @return {@link ServerInputStream} to the entity content.
	 */
	ServerInputStream getInputStream();

	/**
	 * <p>
	 * Exports the current state of the entity.
	 * <p>
	 * Note that only non-consumed content will be available in the current
	 * state.
	 * 
	 * @return Momento of the current state of the entity.
	 * @throws NotAllDataAvailableException
	 *             Should the data for the entity not be fully received from the
	 *             client.
	 */
	Serializable exportState() throws NotAllDataAvailableException;

}