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
package net.officefloor.frame.impl.construct.function;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.Duty;
import net.officefloor.frame.internal.configuration.ManagedFunctionDutyConfiguration;
import net.officefloor.frame.internal.structure.AdministratorScope;

/**
 * {@link ManagedFunctionDutyConfiguration} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionDutyConfigurationImpl<A extends Enum<A>> implements
		ManagedFunctionDutyConfiguration<A> {

	/**
	 * Name of the {@link Administration} within the {@link AdministratorScope}.
	 */
	private final String scopeAdministratorName;

	/**
	 * Name identifying the {@link AdministrationDuty} of the {@link Administration}.
	 */
	private final String dutyName;

	/**
	 * Key identifying the {@link AdministrationDuty} of the {@link Administration}.
	 */
	private final A dutyKey;

	/**
	 * Initiate.
	 * 
	 * @param scopeAdministratorName
	 *            Name of the {@link Administration} within the
	 *            {@link AdministratorScope}.
	 * @param dutyName
	 *            Name identifying {@link AdministrationDuty} of the {@link Administration}.
	 */
	public ManagedFunctionDutyConfigurationImpl(String scopeAdministratorName,
			String dutyName) {
		this.scopeAdministratorName = scopeAdministratorName;
		this.dutyName = dutyName;
		this.dutyKey = null;
	}

	/**
	 * Initiate.
	 * 
	 * @param scopeAdministratorName
	 *            Name of the {@link Administration} within the
	 *            {@link AdministratorScope}.
	 * @param dutyKey
	 *            Key identifying the {@link AdministrationDuty} of the {@link Administration}.
	 */
	public ManagedFunctionDutyConfigurationImpl(String scopeAdministratorName, A dutyKey) {
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