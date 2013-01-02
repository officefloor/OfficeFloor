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
package net.officefloor.plugin.web.http.security.scheme;

import java.io.IOException;
import java.util.Map;

import net.officefloor.frame.api.build.None;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.security.HttpSecurity;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * {@link HttpSecuritySource} providing no security.
 * 
 * @author Daniel Sagenschneider
 */
public class NoneHttpSecuritySource implements HttpSecuritySource<None> {

	/*
	 * =================== HttpSecuritySource ============================
	 */

	@Override
	public void init(HttpSecuritySourceContext<None> context) throws Exception {
		// Nothing to initialise as no security
	}

	@Override
	public String getAuthenticationScheme() {
		return "None";
	}

	@Override
	public HttpSecurity authenticate(String parameters,
			ServerHttpConnection connection, HttpSession session,
			Map<None, Object> dependencies) throws IOException,
			AuthenticationException {
		// No security so never authenticates
		return null;
	}

	@Override
	public void loadUnauthorised(ServerHttpConnection connection,
			HttpSession session, Map<None, Object> dependencies)
			throws AuthenticationException {
		// No security therefore no challenge to issue
	}

}