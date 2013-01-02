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
package net.officefloor.compile.impl.structure;

import net.officefloor.compile.internal.structure.AdministratorNode;
import net.officefloor.compile.internal.structure.DutyNode;
import net.officefloor.compile.spi.office.OfficeDuty;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.WorkBuilder;

/**
 * {@link DutyNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class DutyNodeImpl implements DutyNode {

	/**
	 * Name of this {@link OfficeDuty}.
	 */
	private final String dutyName;

	/**
	 * {@link AdministratorNode} containing this {@link DutyNode}.
	 */
	private final AdministratorNode administrator;

	/**
	 * Initiate.
	 * 
	 * @param dutyName
	 *            Name of this {@link OfficeDuty}.
	 * @param administrator
	 *            {@link AdministratorNode} containing this {@link DutyNode}.
	 */
	public DutyNodeImpl(String dutyName, AdministratorNode administrator) {
		this.dutyName = dutyName;
		this.administrator = administrator;
	}

	/*
	 * ======================= OfficeDuty ==============================
	 */

	@Override
	public String getOfficeDutyName() {
		return this.dutyName;
	}

	/*
	 * ===================== DutyNode ===================================
	 */

	@Override
	public void buildPreTaskAdministration(WorkBuilder<?> workBuilder,
			TaskBuilder<?, ?, ?> taskBuilder) {

		// Link the pre task duty
		taskBuilder.linkPreTaskAdministration(this.administrator
				.getOfficeAdministratorName(), this.dutyName);
	}

	@Override
	public void buildPostTaskAdministration(WorkBuilder<?> workBuilder,
			TaskBuilder<?, ?, ?> taskBuilder) {

		// Link the post task duty
		taskBuilder.linkPostTaskAdministration(this.administrator
				.getOfficeAdministratorName(), this.dutyName);
	}

}