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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Version;

import net.officefloor.example.weborchestration.Sales.ShoppingCartSeed;

/**
 * Shopping Cart.
 * 
 * @author Daniel Sagenschneider
 */
@Entity
public class ShoppingCart {

	/**
	 * Identifier for this {@link ShoppingCart}.
	 */
	@Id
	@GeneratedValue
	private Long shoppingCartId;

	/**
	 * {@link Customer}.
	 */
	@ManyToOne
	private Customer customer;

	/**
	 * {@link ShoppingCartItem} instances for this {@link ShoppingCart}.
	 */
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private List<ShoppingCartItem> items;

	/**
	 * {@link Version}.
	 */
	@Version
	@SuppressWarnings("unused")
	private Long version;

	/**
	 * Default constructor for {@link Entity}.
	 */
	public ShoppingCart() {
	}

	/**
	 * Initiate.
	 * 
	 * @param seed
	 *            {@link ShoppingCartSeed}.
	 */
	public ShoppingCart(ShoppingCartSeed seed) {
		this.customer = seed.customer;
	}

	/**
	 * Obtains the identifier for this {@link ShoppingCart}.
	 * 
	 * @return {@link ShoppingCart} Id.
	 */
	public Long getShoppingCartId() {
		return this.shoppingCartId;
	}

	/**
	 * Obtains the {@link Customer}.
	 * 
	 * @return {@link Customer}.
	 */
	public Customer getCustomer() {
		return this.customer;
	}

	/**
	 * Obtains the {@link ShoppingCartItem} instances.
	 * 
	 * @return {@link ShoppingCartItem} instances.
	 */
	public List<ShoppingCartItem> getShoppingCartItems() {
		return this.items;
	}

}