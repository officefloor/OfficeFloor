/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.api.build;

import net.officefloor.frame.spi.administration.source.AdministratorSource;

/**
 * Enables building a
 * {@link net.officefloor.frame.spi.administration.Administrator}.
 * 
 * @author Daniel
 */
public interface AdministratorBuilder<A extends Enum<A>> {

	/**
	 * Specifies the {@link Class} of the
	 * {@link net.officefloor.frame.spi.administration.source.AdministratorSource}.
	 * 
	 * @param <S>
	 *            {@link AdministratorSource} type.
	 * @param administratorSourceClass
	 *            {@link Class} of the {@link AdministratorSource}.
	 * @throws BuildException
	 *             Indicate failure in building.
	 */
	<S extends AdministratorSource> void setAdministratorSourceClass(
			Class<S> administratorSourceClass) throws BuildException;

	/**
	 * Specifies the scope of the
	 * {@link net.officefloor.frame.spi.administration.Administrator}. Default
	 * scope is {@link OfficeScope#WORK}.
	 * 
	 * @param scope
	 *            Scope for the
	 *            {@link net.officefloor.frame.spi.administration.Administrator}.
	 */
	void setAdministratorScope(OfficeScope scope);

	/**
	 * Specifies a property for the {@link AdministratorSource}.
	 * 
	 * @param name
	 *            Name of property.
	 * @param value
	 *            Value of property.
	 * @throws BuildException
	 *             Indicate failure in building.
	 */
	void addProperty(String name, String value) throws BuildException;

	/**
	 * Creates a {@link DutyBuilder} for a
	 * {@link net.officefloor.frame.spi.administration.Duty} of the
	 * {@link net.officefloor.frame.spi.administration.Administrator}.
	 * 
	 * @param <F>
	 *            {@link Enum} listing
	 *            {@link net.officefloor.frame.internal.structure.Flow}
	 *            instances to be instigated by the
	 *            {@link net.officefloor.frame.spi.administration.Duty}.
	 * @param dutyKey
	 *            Key identifying the
	 *            {@link net.officefloor.frame.spi.administration.Duty}.
	 * @param flowListingEnum
	 *            {@link Enum} {@link Class} listing
	 *            {@link net.officefloor.frame.internal.structure.Flow}
	 *            instances to be instigated by the
	 *            {@link net.officefloor.frame.spi.administration.Duty}.
	 * @return {@link DutyBuilder} for the specified duty.
	 * @throws BuildException
	 *             Build failure.
	 */
	<F extends Enum<F>> DutyBuilder<F> registerDutyBuilder(A dutyKey,
			Class<F> flowListingEnum) throws BuildException;

	/**
	 * Creates a {@link DutyBuilder} for a
	 * {@link net.officefloor.frame.spi.administration.Duty} of the
	 * {@link net.officefloor.frame.spi.administration.Administrator}.
	 * 
	 * @param dutyKey
	 *            Key identifying the
	 *            {@link net.officefloor.frame.spi.administration.Duty}.
	 * @return {@link DutyBuilder} for the specified duty.
	 * @throws BuildException
	 *             Build failure.
	 */
	DutyBuilder<Indexed> registerDutyBuilder(A dutyKey) throws BuildException;

}
