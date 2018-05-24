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
package net.officefloor.plugin.bayeux.adapt;

import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.SecurityPolicy;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.bayeux.server.ServerMessage;
import org.cometd.bayeux.server.ServerSession;

/**
 * Adapter for the {@link SecurityPolicy}.
 * 
 * @author Daniel Sagenschneider
 */
public class SecurityPolicyAdapter implements SecurityPolicy {

	/*
	 * ====================== Security Policy =====================
	 */

	@Override
	public boolean canHandshake(BayeuxServer server, ServerSession session,
			ServerMessage message) {
		return true;
	}

	@Override
	public boolean canCreate(BayeuxServer server, ServerSession session,
			String channelId, ServerMessage message) {
		return true;
	}

	@Override
	public boolean canSubscribe(BayeuxServer server, ServerSession session,
			ServerChannel channel, ServerMessage message) {
		return true;
	}

	@Override
	public boolean canPublish(BayeuxServer server, ServerSession session,
			ServerChannel channel, ServerMessage message) {
		return true;
	}

}