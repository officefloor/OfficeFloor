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
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.spi.office.OfficeDuty;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
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
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Initialised state.
	 */
	private InitialisedState state;

	/**
	 * Initialised state.
	 */
	private static class InitialisedState {
	}

	/**
	 * Initiate.
	 * 
	 * @param dutyName
	 *            Name of this {@link OfficeDuty}.
	 * @param administrator
	 *            {@link AdministratorNode} containing this {@link DutyNode}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public DutyNodeImpl(String dutyName, AdministratorNode administrator,
			NodeContext context) {
		this.dutyName = dutyName;
		this.administrator = administrator;
		this.context = context;
	}

	/*
	 * ========================== Node =============================
	 */

	@Override
	public String getNodeName() {
		return this.dutyName;
	}

	@Override
	public String getNodeType() {
		return TYPE;
	}

	@Override
	public String getLocation() {
		return null;
	}

	@Override
	public Node getParentNode() {
		return this.administrator;
	}

	@Override
	public boolean isInitialised() {
		return (this.state != null);
	}

	@Override
	public void initialise() {
		this.state = NodeUtil.initialise(this, this.context, this.state,
				() -> new InitialisedState());
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
			ManagedFunctionBuilder<?, ?, ?> taskBuilder) {

		// Link the pre task duty
		taskBuilder.linkPreTaskAdministration(
				this.administrator.getOfficeAdministratorName(), this.dutyName);
	}

	@Override
	public void buildPostTaskAdministration(WorkBuilder<?> workBuilder,
			ManagedFunctionBuilder<?, ?, ?> taskBuilder) {

		// Link the post task duty
		taskBuilder.linkPostTaskAdministration(
				this.administrator.getOfficeAdministratorName(), this.dutyName);
	}

}