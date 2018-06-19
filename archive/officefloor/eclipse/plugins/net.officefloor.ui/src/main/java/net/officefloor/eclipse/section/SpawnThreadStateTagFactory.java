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
package net.officefloor.eclipse.section;

import org.eclipse.gef.requests.CreationFactory;

import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Implementation of the {@link CreationFactory} that returns whether to spawn a
 * {@link ThreadState}.
 * 
 * @author Daniel Sagenschneider
 */
public class SpawnThreadStateTagFactory implements CreationFactory {

	/**
	 * Indicates whether to spawn a {@link ThreadState}.
	 */
	private final boolean isSpawnThreadState;

	/**
	 * Initiate.
	 * 
	 * @param isSpawnThreadState
	 *            Indicates whether to spawn a {@link ThreadState}.
	 */
	public SpawnThreadStateTagFactory(boolean isSpawnThreadState) {
		this.isSpawnThreadState = isSpawnThreadState;
	}

	/*
	 * ==================== CreationFactory ===========================
	 */

	@Override
	public Object getNewObject() {
		return Boolean.valueOf(this.isSpawnThreadState);
	}

	@Override
	public Object getObjectType() {
		return Boolean.class;
	}

}