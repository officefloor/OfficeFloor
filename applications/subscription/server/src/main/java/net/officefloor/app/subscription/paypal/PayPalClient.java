package net.officefloor.app.subscription.paypal;

import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;

/**
 * PayPal client.
 * 
 * @author Daniel Sagenschneider
 */
public class PayPalClient {

	private final PayPalEnvironment sandboxEnvironment = new PayPalEnvironment.Sandbox("YOUR APPLICATION CLIENT ID",
			"YOUR APPLICATION CLIENT SECRET");

	private final PayPalEnvironment liveEnvironment = new PayPalEnvironment.Live("YOUR APPLICATION CLIENT ID",
			"YOUR APPLICATION CLIENT SECRET");

	private final PayPalHttpClient client = new PayPalHttpClient(this.sandboxEnvironment);

	public PayPalHttpClient getClient() {
		return this.client;
	}

}