/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.compile.test.administrator;

import net.officefloor.compile.administrator.DutyFlowType;
import net.officefloor.compile.administrator.DutyType;
import net.officefloor.frame.internal.structure.Flow;

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
	 *            Name of the {@link Flow}.
	 * @param argumentType
	 *            Argument type.
	 * @param index
	 *            Index of the {@link Flow}.
	 * @param flowKey
	 *            Key of the {@link Flow}.
	 */
	void addFlow(String flowName, Class<?> argumentType, int index, F flowKey);

}