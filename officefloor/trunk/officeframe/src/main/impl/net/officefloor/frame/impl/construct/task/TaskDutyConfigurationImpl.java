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
package net.officefloor.frame.impl.construct.task;

import net.officefloor.frame.internal.configuration.TaskDutyConfiguration;
import net.officefloor.frame.internal.structure.AdministratorScope;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;

/**
 * {@link TaskDutyConfiguration} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class TaskDutyConfigurationImpl<A extends Enum<A>> implements
		TaskDutyConfiguration<A> {

	/**
	 * Name of the {@link Administrator} within the {@link AdministratorScope}.
	 */
	private final String scopeAdministratorName;

	/**
	 * Name identifying the {@link Duty} of the {@link Administrator}.
	 */
	private final String dutyName;

	/**
	 * Key identifying the {@link Duty} of the {@link Administrator}.
	 */
	private final A dutyKey;

	/**
	 * Initiate.
	 * 
	 * @param scopeAdministratorName
	 *            Name of the {@link Administrator} within the
	 *            {@link AdministratorScope}.
	 * @param dutyKey
	 *            Name identifying {@link Duty} of the {@link Administrator}.
	 */
	public TaskDutyConfigurationImpl(String scopeAdministratorName,
			String dutyName) {
		this.scopeAdministratorName = scopeAdministratorName;
		this.dutyName = dutyName;
		this.dutyKey = null;
	}

	/**
	 * Initiate.
	 * 
	 * @param scopeAdministratorName
	 *            Name of the {@link Administrator} within the
	 *            {@link AdministratorScope}.
	 * @param dutyKey
	 *            Key identifying the {@link Duty} of the {@link Administrator}.
	 */
	public TaskDutyConfigurationImpl(String scopeAdministratorName, A dutyKey) {
		this.scopeAdministratorName = scopeAdministratorName;
		this.dutyName = null;
		this.dutyKey = dutyKey;
	}

	/*
	 * ==================== TaskDutyConfiguration =========================
	 */

	@Override
	public String getScopeAdministratorName() {
		return this.scopeAdministratorName;
	}

	@Override
	public String getDutyName() {
		return this.dutyName;
	}

	@Override
	public A getDutyKey() {
		return this.dutyKey;
	}

}