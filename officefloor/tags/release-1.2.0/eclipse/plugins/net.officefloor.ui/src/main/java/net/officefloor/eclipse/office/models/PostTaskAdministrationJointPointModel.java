/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

package net.officefloor.eclipse.office.models;

import net.officefloor.model.office.OfficeTaskModel;

/**
 * Post {@link AbstractTaskAdministrationJoinPointModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class PostTaskAdministrationJointPointModel extends
		AbstractTaskAdministrationJoinPointModel {

	/**
	 * Initiate.
	 * 
	 * @param task
	 *            {@link OfficeTaskModel}.
	 */
	public PostTaskAdministrationJointPointModel(OfficeTaskModel task) {
		super(task, false);
	}

}