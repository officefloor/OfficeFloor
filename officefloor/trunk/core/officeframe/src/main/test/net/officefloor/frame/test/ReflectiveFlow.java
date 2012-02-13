/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.frame.test;

import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.internal.structure.JobSequence;

/**
 * Reflective flow to be used as a parameter.
 * 
 * @author Daniel Sagenschneider
 */
public interface ReflectiveFlow {

	/**
	 * Invokes the flow.
	 * 
	 * @param parameter
	 *            Parameter to the flow.
	 * @return {@link FlowFuture} for the invoked {@link JobSequence}.
	 */
	FlowFuture doFlow(Object parameter);

}
