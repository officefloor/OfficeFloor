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

import java.util.List;

import javax.ejb.Local;

/**
 * {@link Local} interface for {@link Accounts}.
 * 
 * @author Daniel Sagenschneider
 */
@Local
public interface AccountsLocal {

	/**
	 * Retrieves the {@link Account} for the {@link Customer}.
	 * 
	 * @param customer
	 *            {@link Customer}.
	 * @return {@link Account} for the {@link Customer}.
	 */
	Account retrieveAccount(Customer customer);

	/**
	 * Creates a {@link Quote} for the {@link Customer} for the
	 * {@link InvoiceableItem} instances.
	 * 
	 * @param customer
	 *            {@link Customer}.
	 * @param items
	 *            Listing of {@link InvoiceableItem} to provide a {@link Quote}.
	 * @return {@link Quote}.
	 */
	Quote createQuote(Customer customer, List<? extends InvoiceableItem> items);

	/**
	 * Retrieves the {@link Quote}.
	 * 
	 * @param quoteId
	 *            {@link Quote} identifier.
	 * @return {@link Quote} or <code>null</code> if no {@link Quote} by
	 *         identifier.
	 */
	Quote retrieveQuote(Long quoteId);

	/**
	 * Creates the {@link Invoice} for the {@link Quote}.
	 * 
	 * @param quote
	 *            {@link Quote}.
	 * @return {@link Invoice}.
	 */
	Invoice createInvoice(Quote quote);

	/**
	 * Retrieves the {@link Invoice}.
	 * 
	 * @param invoiceId
	 *            {@link Invoice} identifier.
	 * @return {@link Invoice} or <code>null</code> if no {@link Invoice} by
	 *         identifier.
	 */
	Invoice retrieveInvoice(Long invoiceId);

	/**
	 * Creates the particular {@link Account} with the dollars.
	 * 
	 * @param account
	 *            {@link Account}.
	 * @param dollars
	 *            Dollars to credit the {@link Account}.
	 */
	void creditAccount(Account account, double dollars);

}