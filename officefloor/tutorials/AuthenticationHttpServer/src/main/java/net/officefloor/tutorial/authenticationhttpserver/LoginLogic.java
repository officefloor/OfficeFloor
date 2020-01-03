/*-
 * #%L
 * Authentication HTTP Server Tutorial
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

package net.officefloor.tutorial.authenticationhttpserver;

import java.io.Serializable;

import lombok.Data;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.web.HttpParameters;
import net.officefloor.web.security.HttpCredentials;
import net.officefloor.web.security.scheme.HttpCredentialsImpl;

/**
 * Logic for <code>login</code> page.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class LoginLogic {

	@Data
	@HttpParameters
	public static class Form implements Serializable {
		private static final long serialVersionUID = 1L;

		private String username;

		private String password;
	}

	@FlowInterface
	public static interface Flows {

		void authenticate(HttpCredentials credentials);
	}

	public void login(Form form, Flows flows) {
		flows.authenticate(new HttpCredentialsImpl(form.getUsername(), form.getPassword()));
	}

}
// END SNIPPET: tutorial
