package net.officefloor.pay.paypal;

import com.paypal.core.PayPalEnvironment;

/**
 * Repository for PayPal configuration.
 * 
 * @author Daniel Sagenschneider
 */
public interface PayPalConfigurationRepository {

	/**
	 * Creates the {@link PayPalEnvironment}.
	 * 
	 * @return {@link PayPalEnvironment}.
	 */
	PayPalEnvironment createPayPalEnvironment();

}