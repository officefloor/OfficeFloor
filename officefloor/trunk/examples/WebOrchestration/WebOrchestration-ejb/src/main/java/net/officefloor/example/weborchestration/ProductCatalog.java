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
package net.officefloor.example.weborchestration;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * Product catalog {@link Stateless} bean.
 * 
 * @author Daniel Sagenschneider
 */
@Stateless
public class ProductCatalog implements ProductCatalogLocal {

	/**
	 * Seed to create a {@link Product}.
	 */
	public static class ProductSeed {

		/**
		 * Name.
		 */
		public final String name;

		/**
		 * Price.
		 */
		public final double price;

		/**
		 * Initiate.
		 * 
		 * @param name
		 *            Name.
		 * @param price
		 *            Price.
		 */
		private ProductSeed(String name, double price) {
			this.name = name;
			this.price = price;
		}
	}

	/**
	 * {@link EntityManager}.
	 */
	@PersistenceContext(unitName = "product-unit")
	private EntityManager entityManager;

	/*
	 * ==================== ProductCatalogLocal ==================
	 */

	@Override
	public Product createProduct(String name, double price) {
		// Create and return the Product
		Product product = new Product(new ProductSeed(name, price));
		this.entityManager.persist(product);
		return product;
	}

	@Override
	public Product retrieveProduct(Long productId) {
		return this.entityManager.find(Product.class, productId);
	}

	@Override
	public List<Product> retrieveProductsByName(String productName) {
		Query query = this.entityManager
				.createQuery("SELECT p FROM Product p WHERE p.name = :name");
		query.setParameter("name", productName);
		return this.retrieveProducts(query);
	}

	@Override
	public List<Product> retrieveProductList() {
		Query query = this.entityManager.createQuery("SELECT p FROM Product p");
		return this.retrieveProducts(query);
	}

	/**
	 * Retrieves the {@link Product} instances for the {@link Query}.
	 * 
	 * @param query
	 *            {@link Query}.
	 * @return {@link Product} instances for the {@link Query}.
	 */
	@SuppressWarnings("unchecked")
	private List<Product> retrieveProducts(Query query) {
		return (List<Product>) query.getResultList();
	}

}