/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.eclipse.configurer.example;

import net.officefloor.compile.properties.PropertyList;

/**
 * Example model to be configured.
 * 
 * @author Daniel Sagenschneider
 */
public class ExampleModel {

	/**
	 * Text.
	 */
	public String text;

	/**
	 * Class name.
	 */
	public String className;

	/**
	 * Resource name.
	 */
	public String resourceName;

	/**
	 * Choice value.
	 */
	public String choiceValue;

	/**
	 * {@link PropertyList}.
	 */
	public PropertyList properties;

	/**
	 * Flag.
	 */
	public boolean flag;
}