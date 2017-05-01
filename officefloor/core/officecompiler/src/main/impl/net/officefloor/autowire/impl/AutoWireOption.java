/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.autowire.impl;

import java.util.function.Supplier;

import net.officefloor.autowire.AutoWire;
import net.officefloor.compile.internal.structure.Node;

/**
 * Option for {@link AutoWire}.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireOption<N extends Node> {

	/**
	 * {@link AutoWire} for the option.
	 */
	private final AutoWire autoWire;

	/**
	 * Factory for the creation of the {@link Node}.
	 */
	private final Supplier<N> factory;

	/**
	 * {@link Node} to use for this option.
	 */
	private N node;

	/**
	 * Instantiate with existing {@link Node}.
	 * 
	 * @param autoWire
	 *            {@link AutoWire} for this option.
	 * @param node
	 *            {@link Node} to use for this option.
	 */
	public AutoWireOption(AutoWire autoWire, N node) {
		this.autoWire = autoWire;
		this.factory = () -> node;
		this.node = node;
	}

	public AutoWireOption(AutoWire autoWire, Supplier<N> factory) {
		this.autoWire = autoWire;
		this.factory = factory;
	}

	public N getNode() {
		if (this.node == null) {
			this.node = this.factory.get();
		}
		return this.node;
	}

}