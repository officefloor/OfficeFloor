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