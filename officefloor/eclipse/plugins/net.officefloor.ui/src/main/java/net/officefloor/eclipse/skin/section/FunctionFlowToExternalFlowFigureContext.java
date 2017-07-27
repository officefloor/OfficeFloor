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
package net.officefloor.eclipse.skin.section;

import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.model.section.FunctionFlowToExternalFlowModel;

/**
 * Context to decorate the {@link FunctionFlowToExternalFlowModel} connection.
 * 
 * @author Daniel Sagenschneider
 */
public interface FunctionFlowToExternalFlowFigureContext {

	/**
	 * Indicates whether spawns a {@link ThreadState}.
	 * 
	 * @return <code>true</code> to indicate {@link Flow} spawns in a
	 *         {@link ThreadState}.
	 */
	boolean isSpawnThreadState();

}