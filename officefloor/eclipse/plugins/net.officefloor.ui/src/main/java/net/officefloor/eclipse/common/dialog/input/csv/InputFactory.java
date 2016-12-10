/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.eclipse.common.dialog.input.csv;

import net.officefloor.eclipse.common.dialog.input.Input;

import org.eclipse.swt.widgets.Control;

/**
 * Factory for the creation of an {@link Input}.
 * 
 * @author Daniel Sagenschneider
 */
public interface InputFactory<C extends Control> {

	/**
	 * Creates a new {@link Input}.
	 * 
	 * @return New {@link Input}.
	 */
	Input<C> createInput();

}