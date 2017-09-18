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
package net.officefloor.tutorial.securepagehttpserver;

import java.io.Serializable;

import lombok.Data;
import net.officefloor.plugin.web.http.application.HttpParameters;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * Logic for the card template.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class CardLogic {

	@Data
	@HttpParameters
	public static class CardDetails implements Serializable {

		private String number;

		private String month;

		private String year;

		private String csc;
	}

	public CardDetails getTemplateData(CardDetails cardDetails,
			ServerHttpConnection connection) {

		// Confirm a secure connection (not needed but included for tutorial)
		if (!connection.isSecure()) {
			throw new IllegalStateException();
		}

		// Return the card details for rendering
		return cardDetails;
	}

	public void save(CardDetails cardDetails, ServerHttpConnection connection) {

		// Confirm a secure connection (not needed but included for tutorial)
		if (!connection.isSecure()) {
			throw new IllegalStateException();
		}

		// Logic for processing the card details
	}

}
// END SNIPPET: tutorial