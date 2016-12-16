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
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.TeamIdentifier;

/**
 * {@link JobNodeActivateSet} that allows activating the {@link JobNode}
 * instances.
 * 
 * @author Daniel Sagenschneider
 */
@Deprecated // using job loop
public interface JobNodeActivatableSet extends JobNodeActivateSet {

	/**
	 * Activates the {@link JobNode} instances within this
	 * {@link JobNodeActivateSet}.
	 * 
	 * @param currentTeam
	 *            {@link TeamIdentifier} of the current {@link Team} activating
	 *            the {@link JobNode} instances.
	 */
	void activateJobNodes(TeamIdentifier currentTeam);

}