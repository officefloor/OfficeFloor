/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.server.http;

import java.io.IOException;

import net.officefloor.frame.api.escalate.Escalation;

/**
 * Enables sending an appropriate response for an {@link Escalation}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpEscalationHandler {

	/**
	 * Handles the {@link Escalation}.
	 * 
	 * @param context
	 *            {@link HttpEscalationContext}.
	 * @return <code>true</code> if handled {@link Escalation} into the
	 *         {@link HttpResponse}. <code>false</code> if not able to handle
	 *         the particular {@link Escalation}.
	 * @throws IOException
	 *             If fails to write the {@link Escalation}.
	 */
	boolean handle(HttpEscalationContext context) throws IOException;

}