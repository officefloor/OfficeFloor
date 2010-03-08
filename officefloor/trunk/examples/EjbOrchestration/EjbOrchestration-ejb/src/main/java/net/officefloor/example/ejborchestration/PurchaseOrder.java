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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

/**
 * Purchase Order.
 * 
 * @author Daniel Sagenschneider
 */
@Entity
public class PurchaseOrder {

	/**
	 * Identifier for this {@link PurchaseOrder}.
	 */
	@Id
	@GeneratedValue
	private Long purchaseOrderId;

	/**
	 * {@link Customer}.
	 */
	@ManyToOne
	private Customer customer;

	/**
	 * {@link PurchaseOrderLineItem} instances for this {@link PurchaseOrder}.
	 */
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private List<PurchaseOrderLineItem> lineItems;

	/**
	 * Default constructor for {@link Entity}.
	 */
	public PurchaseOrder() {
	}

	/**
	 * Initiate.
	 * 
	 * @param customer
	 *            {@link Customer}.
	 */
	public PurchaseOrder(Customer customer) {
		this.customer = customer;
	}

	/**
	 * Obtains the identifier for this {@link PurchaseOrder}.
	 * 
	 * @return {@link PurchaseOrder} Id.
	 */
	public Long getPurchaseOrderId() {
		return this.purchaseOrderId;
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
	 * Obtains the {@link PurchaseOrderLineItem} instances.
	 * 
	 * @return {@link PurchaseOrderLineItem} instances.
	 */
	public List<PurchaseOrderLineItem> getPurchaseOrderLineItems() {

		// Lazy create to allow adding while create
		if (this.lineItems == null) {
			this.lineItems = new LinkedList<PurchaseOrderLineItem>();
		}

		// Return the line items
		return this.lineItems;
	}

}