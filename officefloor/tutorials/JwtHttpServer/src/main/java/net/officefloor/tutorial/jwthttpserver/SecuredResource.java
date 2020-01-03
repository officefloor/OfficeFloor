/*-
 * #%L
 * JWT Tutorial
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

package net.officefloor.tutorial.jwthttpserver;

import java.io.IOException;

import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.security.HttpAccess;

/**
 * Secured resource.
 * 
 * @author Daniel Sagenschneider
 */
public class SecuredResource {

	@HttpAccess(ifRole = "tutorial")
	public void secure(ServerHttpConnection connection) throws IOException {
		connection.getResponse().getEntityWriter().write("Hello JWT secured World");
	}

}
