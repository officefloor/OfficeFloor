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

import java.util.LinkedList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import net.officefloor.example.ejborchestration.Accounts.AccountSeed;

/**
 * Account.
 * 
 * @author Daniel Sagenschneider
 */
@Entity
public class Account {

	/**
	 * {@link Account} identifier.
	 */
	@Id
	@GeneratedValue
	private Long accountId;

	/**
	 * {@link Customer} instances for this {@link Account}.
	 */
	@OneToMany
	private List<Customer> customers;

	/**
	 * Balance for this {@link Account}.
	 */
	private double balance;

	/**
	 * Default constructor for {@link Entity}.
	 */
	public Account() {
	}

	/**
	 * Allow initiating from {@link Accounts}.
	 * 
	 * @param seed
	 *            {@link AccountSeed}.
	 */
	public Account(AccountSeed seed) {
		this.customers = new LinkedList<Customer>();
		this.customers.add(seed.customer);
	}

	/**
	 * Obtains the {@link Account} identifier.
	 * 
	 * @return {@link Account} identifier.
	 */
	public Long getAccountId() {
		return this.accountId;
	}

	/**
	 * Obtains the {@link Customer} instances for this {@link Account}.
	 * 
	 * @return {@link Customer} instances for this {@link Account}.
	 */
	public List<Customer> getCustomers() {
		return this.customers;
	}

	/**
	 * Obtains the balance for this {@link Account}.
	 * 
	 * @return Balance for this {@link Account}.
	 */
	public double getBalance() {
		return this.balance;
	}

	/**
	 * Specifies the balance for this {@link Account}.
	 * 
	 * @param balance
	 *            Balance.
	 */
	void setBalance(double balance) {
		this.balance = balance;
	}

}