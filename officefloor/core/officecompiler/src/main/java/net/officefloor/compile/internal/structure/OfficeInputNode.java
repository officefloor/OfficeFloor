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
package net.officefloor.compile.internal.structure;

import net.officefloor.compile.office.OfficeInputType;
import net.officefloor.compile.spi.office.OfficeInput;
import net.officefloor.compile.type.TypeContext;

/**
 * {@link OfficeInput} node.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeInputNode extends LinkFlowNode, LinkSynchronousNode, OfficeInput {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Office Input";

	/**
	 * Initialises the {@link OfficeInputNode}.
	 * 
	 * @param parameterType
	 *            Parameter type of {@link OfficeInput}.
	 */
	void initialise(String parameterType);

	/**
	 * Obtains the {@link OfficeInputType}.
	 * 
	 * @param typeContext
	 *            {@link TypeContext}.
	 * @return {@link OfficeInputType} or <code>null</code> if can not
	 *         determine.
	 */
	OfficeInputType loadOfficeInputType(TypeContext typeContext);

}