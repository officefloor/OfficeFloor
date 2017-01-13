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
package net.officefloor.frame.api.team;

import net.officefloor.frame.internal.structure.TeamManagement;

/**
 * <p>
 * Identifier for a {@link Team}.
 * <p>
 * There will only be one {@link TeamIdentifier} instance associated per
 * {@link Team} instance so that they may be compared with direct object
 * reference equality (==), which makes <code>null</code> checks easier.
 * 
 * @author Daniel Sagenschneider
 * 
 * @see TeamManagement
 */
public interface TeamIdentifier {
}