/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.tutorial.paypalhttpserver;

import com.paypal.core.PayPalEnvironment;

import net.officefloor.pay.paypal.PayPalConfigurationRepository;

/**
 * In memory {@link PayPalConfigurationRepository}.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class InMemoryPayPalConfigurationRepository implements PayPalConfigurationRepository {

	private static volatile PayPalEnvironment environmet = null;

	public void loadEnvironment(String clientId, String clientSecret) {
		environmet = new PayPalEnvironment.Sandbox(clientId, clientSecret);
	}

	/*
	 * ================= PayPalConfigurationRepository ==================
	 */

	@Override
	public PayPalEnvironment createPayPalEnvironment() {
		return environmet;
	}
}
// END SNIPPET: tutorial