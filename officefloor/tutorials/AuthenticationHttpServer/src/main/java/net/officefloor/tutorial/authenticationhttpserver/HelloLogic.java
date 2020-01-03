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

import lombok.Data;
import net.officefloor.plugin.section.clazz.Next;
import net.officefloor.web.security.HttpAccess;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;

/**
 * Logic for <code>hello</code> page.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class HelloLogic {

	@Data
	public static class TemplateData {

		private final String username;

	}

	@HttpAccess
	public TemplateData getTemplateData(HttpAccessControl accessControl) {
		String username = accessControl.getPrincipal().getName();
		return new TemplateData(username);
	}

	@Next("LoggedOut")
	public void logout(HttpAuthentication<?> authentication) {
		authentication.logout(null);
	}

}
// END SNIPPET: tutorial
