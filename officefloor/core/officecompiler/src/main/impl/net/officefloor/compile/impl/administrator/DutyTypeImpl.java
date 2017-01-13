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
package net.officefloor.compile.impl.administrator;

import net.officefloor.compile.administrator.DutyFlowType;
import net.officefloor.compile.administrator.DutyType;
import net.officefloor.frame.api.administration.Duty;
import net.officefloor.frame.api.build.None;

/**
 * {@link DutyType} implementation.
 * 
 * TODO look to provide flow keys rather than None.
 * 
 * @author Daniel Sagenschneider
 */
public class DutyTypeImpl<A extends Enum<A>> implements DutyType<A, None> {

	/**
	 * Name of the {@link Duty}.
	 */
	private final String dutyName;

	/**
	 * {@link Duty} key.
	 */
	private final A dutyKey;

	/**
	 * Initiate.
	 * 
	 * @param dutyName
	 *            Name of the {@link Duty}.
	 * @param dutyKey
	 *            {@link Duty} key.
	 */
	public DutyTypeImpl(String dutyName, A dutyKey) {
		this.dutyName = dutyName;
		this.dutyKey = dutyKey;
	}

	/*
	 * ======================== DutyType ===================================
	 */

	@Override
	public String getDutyName() {
		return this.dutyName;
	}

	@Override
	public A getDutyKey() {
		return this.dutyKey;
	}

	@Override
	public Class<None> getFlowKeyClass() {
		return None.class;
	}

	@Override
	@SuppressWarnings("unchecked")
	public DutyFlowType<None>[] getFlowTypes() {
		return new DutyFlowType[0];
	}

}