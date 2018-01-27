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
package net.officefloor.web.spi.security;

import java.io.Serializable;

import net.officefloor.frame.api.escalate.Escalation;

/**
 * Listens for change in access control (or {@link Escalation} in failing to
 * authenticate).
 * 
 * @author Daniel Sagenschneider
 */
public interface AccessControlListener<AC extends Serializable> {

	/**
	 * Notified of a change to access control.
	 * 
	 * @param accessControl
	 *            Access control. May be <code>null</code> if
	 *            <ul>
	 *            <li>logging out</li>
	 *            <li>failure in authenticating</li>
	 *            </ul>
	 * @param escalation
	 *            Possible {@link Escalation}. Will be <code>null</code> if
	 *            successfully obtain access control or logout.
	 */
	void accessControlChange(AC accessControl, Throwable escalation);

}