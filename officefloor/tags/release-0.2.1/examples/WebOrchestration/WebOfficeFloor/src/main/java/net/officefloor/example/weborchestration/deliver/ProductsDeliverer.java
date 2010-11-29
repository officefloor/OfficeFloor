package net.officefloor.example.weborchestration.deliver;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.example.weborchestration.Product;
import net.officefloor.example.weborchestration.ProductCatalogLocal;
import net.officefloor.example.weborchestration.ProductWarehouseLocal;
import net.officefloor.example.weborchestration.WebUtil;
import net.officefloor.plugin.work.clazz.FlowInterface;
import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Handles delivery of {@link Product} instances.
 * 
 * @author daniel
 */
public class ProductsDeliverer {

	/**
	 * Obtains the {@link ProductDelivery} instances.
	 * 
	 * @return {@link ProductDelivery} instances.
	 */
	public ProductDelivery[] obtainProducts() {

		// Obtain the products in the catalogue
		ProductCatalogLocal catalog = WebUtil
				.lookupService(ProductCatalogLocal.class);
		List<Product> products = catalog.retrieveProductList();

		// Create the listing of product deliveries
		List<ProductDelivery> deliveries = new LinkedList<ProductDelivery>();
		for (int i = 0; i < products.size(); i++) {
			deliveries.add(new ProductDelivery(i, products.get(i)));
		}

		// Return the product deliveries
		return deliveries.toArray(new ProductDelivery[0]);
	}

	/**
	 * Flows for displaying the {@link ProductCatalogLocal}.
	 */
	@FlowInterface
	public static interface DisplayCatalogFlows {

		/**
		 * Displays the head.
		 */
		void displayHead();

		/**
		 * Displays a {@link ProductDelivery}.
		 * 
		 * @param delivery
		 *            {@link ProductDelivery}.
		 */
		void displayProductDelivery(ProductDelivery delivery);

		/**
		 * Displays the tail.
		 */
		void displayTail();
	}

	/**
	 * Displays the {@link ProductCatalogLocal}.
	 * 
	 * @param deliveries
	 *            {@link ProductDelivery} instances.
	 * @param flows
	 *            {@link DisplayCatalogFlows}.
	 */
	public void displayProductCatalog(ProductDelivery[] deliveries,
			DisplayCatalogFlows flows) {

		// Sort the deliveries
		Arrays.sort(deliveries, new Comparator<ProductDelivery>() {
			@Override
			public int compare(ProductDelivery a, ProductDelivery b) {
				return a.getRowIndex() - b.getRowIndex();
			}
		});

		// Display the head
		flows.displayHead();

		// Display the products
		for (ProductDelivery delivery : deliveries) {
			flows.displayProductDelivery(delivery);
		}

		// Display the tail
		flows.displayTail();
	}

	/**
	 * Flows for delivery.
	 */
	@FlowInterface
	public static interface DeliverFlows {

		/**
		 * Invalid delivery.
		 * 
		 * @param deliveries
		 *            {@link ProductDelivery} instances.
		 */
		void invalidDelivery(ProductDelivery[] deliveries);

		/**
		 * Delivery was successful.
		 */
		void deliverySuccessful();
	}

	/**
	 * Delivers the {@link Product} instances.
	 * 
	 * @param deliveredProducts
	 *            {@link DeliveredProducts}.
	 * @param flows
	 *            {@link DeliverFlows}.
	 */
	public void deliverProducts(DeliveredProducts deliveredProducts,
			DeliverFlows flows) {

		// Obtain the Product Catalog
		ProductCatalogLocal catalog = WebUtil
				.lookupService(ProductCatalogLocal.class);

		// Obtain the deliveries
		ProductDelivery[] deliveries = deliveredProducts.getDeliveries()
				.values().toArray(new ProductDelivery[0]);

		// Process the products
		boolean isError = false;
		Map<ProductDelivery, Integer> quantities = new HashMap<ProductDelivery, Integer>();
		for (ProductDelivery delivery : deliveries) {

			// Determine if quantity (handling valid value)
			int quantity;
			try {
				String quantityText = delivery.getQuantity();
				quantity = (WebUtil.isBlank(quantityText) ? 0 : Integer
						.parseInt(quantityText.trim()));
				if (quantity < 0) {
					isError = true;
					delivery.setError("Must be a positive integer value");
				}
			} catch (NumberFormatException ex) {
				isError = true;
				delivery.setError("Must be an integer value");
				continue; // do not process further
			}

			// Record the quantities
			quantities.put(delivery, new Integer(quantity));
		}
		if (isError) {
			flows.invalidDelivery(deliveries);
		}

		// Obtain the warehouse to deliver the products
		ProductWarehouseLocal warehouse = WebUtil
				.lookupService(ProductWarehouseLocal.class);

		// Deliver the products
		for (ProductDelivery delivery : deliveries) {

			// Obtain the Product
			Product product = catalog.retrieveProduct(delivery.getProductId());

			// Obtain the quantity delivered
			int quantity = quantities.get(delivery).intValue();

			// Deliver the product
			warehouse.productDelivered(product, quantity);
		}

		// Successful
		flows.deliverySuccessful();
	}

}