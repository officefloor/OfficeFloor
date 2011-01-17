/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

import net.officefloor.example.weborchestration.Accounts.QuoteSeed;

/**
 * Quote.
 * 
 * @author Daniel Sagenschneider
 */
@Entity
public class Quote {

	/**
	 * {@link Quote} identifier.
	 */
	@Id
	@GeneratedValue
	private Long quoteId;

	/**
	 * {@link Customer}.
	 */
	@ManyToOne
	private Customer customer;

	/**
	 * {@link QuoteItem} instances.
	 */
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private List<QuoteItem> quoteItems;

	/**
	 * Default constructor for {@link Entity}.
	 */
	public Quote() {
	}

	/**
	 * Initiate.
	 * 
	 * @param seed
	 *            {@link QuoteSeed}.
	 */
	public Quote(QuoteSeed seed) {
		this.customer = seed.customer;
	}

	/**
	 * Obtains the {@link Long}.
	 * 
	 * @return {@link Long}.
	 */
	public Long getQuoteId() {
		return this.quoteId;
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
	 * Obtains the {@link List<QuoteItem>}.
	 * 
	 * @return {@link List<QuoteItem>}.
	 */
	public List<QuoteItem> getQuoteItems() {

		// Lazy create the items
		if (this.quoteItems == null) {
			this.quoteItems = new LinkedList<QuoteItem>();
		}

		// Return the items
		return this.quoteItems;
	}

}