/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
package net.officefloor.example.weborchestration;

import java.util.List;

import javax.ejb.Local;
import javax.naming.NamingException;

/**
 * {@link Local} interface for {@link TestReset}.
 * 
 * @author Daniel Sagenschneider
 */
@Local
public interface TestResetLocal {

	/**
	 * Resets for next test.
	 */
	void reset();

	/**
	 * Sets up the {@link Customer}.
	 * 
	 * @return {@link Customer}.
	 * @throws NamingException
	 *             If fails to setup {@link Customer}.
	 * @throws CustomerExistsException
	 *             If {@link Customer} already setup.
	 */
	Customer setupCustomer() throws NamingException, CustomerExistsException;

	/**
	 * Sets up the {@link Product} instances.
	 * 
	 * @return {@link Product} instances.
	 * @throws NamingException
	 *             If fails to setup {@link Product} instances.
	 */
	List<Product> setupProducts() throws NamingException;

}