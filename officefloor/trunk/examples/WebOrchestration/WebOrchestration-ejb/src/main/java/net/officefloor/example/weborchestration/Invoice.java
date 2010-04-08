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

import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import net.officefloor.example.weborchestration.Accounts.InvoiceSeed;

/**
 * Invoice.
 * 
 * @author Daniel Sagenschneider
 */
@Entity
public class Invoice {

	/**
	 * {@link Invoice} identifier.
	 */
	@Id
	@GeneratedValue
	private Long invoiceId;

	/**
	 * {@link Customer} requesting this {@link Invoice}.
	 */
	@ManyToOne
	private Customer customer;

	/**
	 * {@link Account} being invoiced.
	 */
	@ManyToOne
	private Account account;

	/**
	 * {@link InvoiceLineItem} instances.
	 */
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private List<InvoiceLineItem> lineItems;

	/**
	 * Default constructor for {@link Entity}.
	 */
	public Invoice() {
	}

	/**
	 * Initiate.
	 * 
	 * @param seed
	 *            {@link InvoiceSeed}.
	 */
	public Invoice(InvoiceSeed seed) {
		this.customer = seed.customer;
		this.account = seed.account;
	}

	/**
	 * Obtains the {@link Invoice} identifier.
	 * 
	 * @return {@link Invoice} identifier.
	 */
	public Long getInvoiceId() {
		return this.invoiceId;
	}

	/**
	 * Obtains the {@link Customer}.
	 * 
	 * @return {@link Customer}.
	 */
	public Customer getCustomer() {
		return this.customer;
	}

	/**
	 * Obtains the {@link Account}.
	 * 
	 * @return {@link Account}.
	 */
	public Account getAccount() {
		return this.account;
	}

	/**
	 * Obtains the {@link InvoiceLineItem} instances.
	 * 
	 * @return {@link InvoiceLineItem} instances.
	 */
	public List<InvoiceLineItem> getInvoiceLineItems() {

		// Ensure have line items list
		if (this.lineItems == null) {
			this.lineItems = new LinkedList<InvoiceLineItem>();
		}

		// Return the line items
		return this.lineItems;
	}

}