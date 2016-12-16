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
package net.officefloor.frame.spi.administration.source;

import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;

/**
 * Meta-data for the {@link Duty} of the {@link AdministratorSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministratorDutyMetaData<A extends Enum<A>, F extends Enum<F>> {

	/**
	 * Obtains a unique name to identify this {@link Duty} for the
	 * {@link Administrator}.
	 * 
	 * @return Name to uniquely identify this {@link Duty}.
	 */
	String getDutyName();

	/**
	 * Obtains the {@link Enum} key identifying this {@link Duty}. If
	 * <code>null</code> then {@link Duty} will be referenced by this instance's
	 * index in the array returned from {@link AdministratorSourceMetaData}.
	 * 
	 * @return {@link Enum} key identifying the {@link Duty} or
	 *         <code>null</code> indicating identified by an index.
	 */
	A getKey();

	/**
	 * Obtains the list of {@link AdministratorDutyFlowMetaData} instances
	 * should this {@link Duty} require instigating a {@link Flow}.
	 * 
	 * @return Meta-data of {@link Flow} instances instigated by this
	 *         {@link Duty}.
	 */
	AdministratorDutyFlowMetaData<F>[] getFlowMetaData();

}