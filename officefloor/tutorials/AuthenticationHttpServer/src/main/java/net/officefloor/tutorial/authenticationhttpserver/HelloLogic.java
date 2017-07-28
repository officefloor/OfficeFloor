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
package net.officefloor.tutorial.authenticationhttpserver;

import lombok.Data;
import net.officefloor.plugin.section.clazz.NextFunction;
import net.officefloor.plugin.web.http.security.HttpAuthentication;
import net.officefloor.plugin.web.http.security.HttpCredentials;
import net.officefloor.plugin.web.http.security.HttpSecurity;

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

	public TemplateData getTemplateData(HttpSecurity security) {
		String username = security.getRemoteUser();
		return new TemplateData(username);
	}

	@NextFunction("LoggedOut")
	public void logout(HttpAuthentication<HttpSecurity, HttpCredentials> authentication) {
		authentication.logout(null);
	}

}
// END SNIPPET: tutorial