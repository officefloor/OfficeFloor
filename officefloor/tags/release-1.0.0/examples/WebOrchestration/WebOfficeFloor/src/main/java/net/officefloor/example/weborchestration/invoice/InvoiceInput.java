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

package net.officefloor.example.weborchestration.invoice;

import net.officefloor.example.weborchestration.Invoice;
import net.officefloor.example.weborchestration.Quote;

/**
 * Input for {@link Invoice}.
 * 
 * @author daniel
 */
public class InvoiceInput {

	/**
	 * {@link Quote} Id.
	 */
	private String quoteId;

	/**
	 * Specifies the {@link Quote} Id.
	 * 
	 * @param quoteId
	 *            {@link Quote} Id.
	 */
	public void setQuoteId(String quoteId) {
		this.quoteId = quoteId;
	}

	/**
	 * Obtains the {@link Quote} Id.
	 * 
	 * @return {@link Quote} Id.
	 */
	public String getQuoteId() {
		return this.quoteId;
	}

}