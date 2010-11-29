package net.officefloor.example.weborchestration.deliver;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.example.weborchestration.Product;

/**
 * Delivered {@link Product} instances.
 * 
 * @author daniel
 */
public class DeliveredProducts {

	/**
	 * {@link ProductDelivery} instances by their key.
	 */
	private final Map<String, ProductDelivery> deliveries = new HashMap<String, ProductDelivery>();

	/**
	 * Loads a {@link ProductDelivery}.
	 * 
	 * @param key
	 *            Key of the {@link ProductDelivery}.
	 * @param delivery
	 *            {@link ProductDelivery}.
	 */
	public void setProducts(String key, ProductDelivery delivery) {
		delivery.setRowIndex(key);
		this.deliveries.put(key, delivery);
	}

	/**
	 * Obtains the {@link ProductDelivery} instances.
	 * 
	 * @return {@link ProductDelivery} instances.
	 */
	public Map<String, ProductDelivery> getDeliveries() {
		return this.deliveries;
	}

}