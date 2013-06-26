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
package net.officefloor.compile.test.administrator;

import net.officefloor.compile.administrator.DutyFlowType;
import net.officefloor.compile.administrator.DutyType;
import net.officefloor.frame.internal.structure.JobSequence;

/**
 * Builder of the {@link DutyType}.
 * 
 * @author Daniel Sagenschneider
 */
public interface DutyTypeBuilder<F extends Enum<F>> {

	/**
	 * Adds a {@link DutyFlowType}.
	 * 
	 * @param flowName
	 *            Name of the {@link JobSequence}.
	 * @param argumentType
	 *            Argument type.
	 * @param index
	 *            Index of the {@link JobSequence}.
	 * @param flowKey
	 *            Key of the {@link JobSequence}.
	 */
	void addFlow(String flowName, Class<?> argumentType, int index, F flowKey);

}