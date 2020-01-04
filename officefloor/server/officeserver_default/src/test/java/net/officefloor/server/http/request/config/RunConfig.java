/*-
 * #%L
 * Default OfficeFloor HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.server.http.request.config;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.server.http.request.HttpRequestTest;

/**
 * Configuration of a {@link HttpRequestTest}.
 * 
 * @author Daniel Sagenschneider
 */
public class RunConfig {

	/**
	 * {@link CommunicationConfig} instances.
	 */
	public final List<CommunicationConfig> communications = new LinkedList<CommunicationConfig>();

	public void addCommunication(CommunicationConfig communication) {
		this.communications.add(communication);
	}
}
