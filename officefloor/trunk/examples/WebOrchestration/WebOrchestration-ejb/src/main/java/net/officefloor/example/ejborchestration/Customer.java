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
package net.officefloor.example.ejborchestration;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import net.officefloor.example.ejborchestration.Sales.CustomerSeed;

/**
 * Customer.
 * 
 * @author Daniel Sagenschneider
 */
@Entity
public class Customer {

	/**
	 * {@link Customer} Id.
	 */
	@Id
	@GeneratedValue
	private Long customerId;

	/**
	 * Email address.
	 */
	private String email;

	/**
	 * Name.
	 */
	private String name;

	/**
	 * Default constructor for {@link Entity}.
	 */
	public Customer() {
	}

	/**
	 * Allow {@link Sales} to create a {@link Customer}.
	 * 
	 * @param seed
	 *            {@link CustomerSeed}.
	 */
	public Customer(CustomerSeed seed) {
		this.email = seed.email;
		this.name = seed.name;
	}

	/**
	 * Obtains the {@link Customer} Id.
	 * 
	 * @return {@link Customer} Id.
	 */
	public Long getCustomerId() {
		return this.customerId;
	}

	/**
	 * <p>
	 * Obtains the email.
	 * <p>
	 * As the email identifies the {@link Customer}, it can not be changed.
	 * 
	 * @return Email.
	 */
	public String getEmail() {
		return this.email;
	}

	/**
	 * Obtains the name.
	 * 
	 * @return Name.
	 */
	public String getName() {
		return this.name;
	}

}