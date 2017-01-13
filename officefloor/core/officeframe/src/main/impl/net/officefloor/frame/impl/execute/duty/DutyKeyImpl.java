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
package net.officefloor.frame.impl.execute.duty;

import net.officefloor.frame.api.administration.Duty;
import net.officefloor.frame.api.administration.DutyKey;

/**
 * @author Daniel Sagenschneider
 * 
 */
public class DutyKeyImpl<A extends Enum<A>> implements DutyKey<A> {

	/**
	 * Key identifying the {@link AdministrationDuty}.
	 */
	private final A key;

	/**
	 * Index identifying the {@link AdministrationDuty}.
	 */
	private final int index;

	/**
	 * Initiate.
	 * 
	 * @param key
	 *            Key identifying the {@link AdministrationDuty}.
	 */
	public DutyKeyImpl(A key) {
		this.key = key;
		this.index = key.ordinal();
	}

	/**
	 * Initiate.
	 * 
	 * @param index
	 *            Index identifying the {@link AdministrationDuty}.
	 */
	public DutyKeyImpl(int index) {
		this.key = null;
		this.index = index;
	}

	/*
	 * ====================== DutyKey ================================
	 */

	@Override
	public int getIndex() {
		return this.index;
	}

	@Override
	public A getKey() {
		return this.key;
	}

}