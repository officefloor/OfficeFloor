/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.server.http;

import net.officefloor.frame.api.escalate.Escalation;

/**
 * Context for the {@link HttpEscalationHandler}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpEscalationContext {

	/**
	 * Obtains the {@link Escalation}.
	 * 
	 * @return {@link Escalation}.
	 */
	Throwable getEscalation();

	/**
	 * Indicates whether the stack trace should be included.
	 * 
	 * @return <code>true</code> to include the stack trace.
	 */
	boolean isIncludeStacktrace();

	/**
	 * Obtains the {@link ServerHttpConnection} to write the {@link Escalation}.
	 * 
	 * @return {@link ServerHttpConnection}.
	 */
	ServerHttpConnection getServerHttpConnection();

}