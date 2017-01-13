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
package net.officefloor.compile.spi.administration.source;

import net.officefloor.frame.api.administration.Administration;

/**
 * <p>
 * Source to obtain a particular type of {@link Administration}.
 * <p>
 * Implemented by the {@link Administration} provider.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministratorSource<E, D extends Enum<D>> {

	/**
	 * <p>
	 * Obtains the specification for this.
	 * <p>
	 * This will be called before any other methods, therefore this method must
	 * be able to return the specification immediately after a default
	 * constructor instantiation.
	 * 
	 * @return Specification of this.
	 */
	AdministratorSourceSpecification getSpecification();

	/**
	 * Called only once after the {@link AdministratorSource} is instantiated.
	 * 
	 * @param context
	 *            {@link AdministratorSourceContext} to initialise this instance
	 *            of the {@link AdministratorSource}.
	 * @throws Exception
	 *             Should the {@link AdministratorSource} fail to configure
	 *             itself from the input properties.
	 */
	void init(AdministratorSourceContext context) throws Exception;

	/**
	 * <p>
	 * Obtains the meta-data to describe this.
	 * <p>
	 * This is called after the {@link #init(AdministratorSourceContext)} method
	 * and therefore may use the configuration.
	 * <p>
	 * This should always return non-null. If there is a problem due to
	 * incorrect configuration, the {@link #init(AdministratorSourceContext)}
	 * should indicate this via an exception.
	 * 
	 * @return Meta-data to describe this.
	 */
	AdministratorSourceMetaData<E, D> getMetaData();

	/**
	 * <p>
	 * Creates a new {@link Administration}.
	 * 
	 * @return New {@link Administration}.
	 * @throws Throwable
	 *             If fails to create the {@link Administration}.
	 */
	Administration<E, D> createAdministrator() throws Throwable;

}