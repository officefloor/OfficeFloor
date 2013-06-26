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
package net.officefloor.autowire;

import net.officefloor.frame.internal.structure.JobSequence;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.Team;

/**
 * <p>
 * Provides wiring of a {@link ManagedObjectSource}.
 * <p>
 * This is to assist auto-wiring for {@link ManagedObjectSource} instances that
 * have {@link JobSequence}, {@link Team} or {@link Object} dependencies.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectSourceWirer {

	/**
	 * Wires the {@link ManagedObjectSource}.
	 * 
	 * @param context
	 *            {@link ManagedObjectSourceWirerContext}.
	 */
	void wire(ManagedObjectSourceWirerContext context);

}