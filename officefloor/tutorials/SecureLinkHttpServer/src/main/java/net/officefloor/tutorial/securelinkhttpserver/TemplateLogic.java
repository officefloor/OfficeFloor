/*-
 * #%L
 * Secure link HTTP Server Tutorial
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

package net.officefloor.tutorial.securelinkhttpserver;

import java.io.Serializable;

import lombok.Data;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.HttpParameters;

/**
 * Logic for the template.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class TemplateLogic {

	@Data
	@HttpParameters
	public static class LoginParameters implements Serializable {
		private static final long serialVersionUID = 1L;

		private String username;

		private String password;
	}

	public void login(LoginParameters credentials, ServerHttpConnection connection) {

		// Confirm a secure connection (not needed but included for tutorial)
		if (!connection.isSecure()) {
			throw new IllegalStateException();
		}

		// Logic for login
	}

}
// END SNIPPET: tutorial
