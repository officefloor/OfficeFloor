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
package net.officefloor.compile.internal.structure;

/**
 * Auto-wiring of a source {@link Node} to target {@link Node}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AutoWireLink<N extends Node> {

	/**
	 * Obtains the source {@link Node}.
	 * 
	 * @return Source {@link Node}.
	 */
	N getSourceNode();

	/**
	 * Obtains the matching source {@link AutoWire}.
	 * 
	 * @return Matching source {@link AutoWire}.
	 */
	AutoWire getSourceAutoWire();

	/**
	 * Obtains the target {@link Node}.
	 * 
	 * @return Target {@link Node}.
	 */
	N getTargetNode();

	/**
	 * Obtains the matching target {@link AutoWire}.
	 * 
	 * @return Matching target {@link AutoWire}.
	 */
	AutoWire getTargetAutoWire();

}