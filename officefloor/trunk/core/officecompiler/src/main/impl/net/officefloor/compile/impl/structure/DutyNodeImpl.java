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
import net.officefloor.compile.internal.structure.Node;
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
	 * ========================== Node =============================
	 */

	@Override
	public String getNodeName() {
		// TODO implement Node.getNodeName
		throw new UnsupportedOperationException(
				"TODO implement Node.getNodeName");

	}

	@Override
	public String getNodeType() {
		// TODO implement Node.getNodeType
		throw new UnsupportedOperationException(
				"TODO implement Node.getNodeType");

	}

	@Override
	public String getLocation() {
		// TODO implement Node.getLocation
		throw new UnsupportedOperationException(
				"TODO implement Node.getLocation");

	}

	@Override
	public Node getParentNode() {
		// TODO implement Node.getParentNode
		throw new UnsupportedOperationException(
				"TODO implement Node.getParentNode");

	}

	@Override
	public boolean isInitialised() {
		// TODO implement Node.isInitialised
		throw new UnsupportedOperationException(
				"TODO implement Node.isInitialised");

	}

	@Override
	public void initialise() {
		// TODO implement DutyNode.initialise
		throw new UnsupportedOperationException(
				"TODO implement DutyNode.initialise");

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
		taskBuilder.linkPreTaskAdministration(
				this.administrator.getOfficeAdministratorName(), this.dutyName);
	}

	@Override
	public void buildPostTaskAdministration(WorkBuilder<?> workBuilder,
			TaskBuilder<?, ?, ?> taskBuilder) {

		// Link the post task duty
		taskBuilder.linkPostTaskAdministration(
				this.administrator.getOfficeAdministratorName(), this.dutyName);
	}

}