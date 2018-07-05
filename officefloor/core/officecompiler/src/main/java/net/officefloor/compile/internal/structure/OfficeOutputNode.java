/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.internal.structure;

import net.officefloor.compile.office.OfficeOutputType;
import net.officefloor.compile.spi.office.OfficeOutput;

/**
 * {@link OfficeOutput} node.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeOutputNode extends LinkFlowNode, OfficeOutput {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Office Output";

	/**
	 * Initialises the {@link OfficeOutputNode}.
	 * 
	 * @param argumentType
	 *            Argument type from this {@link OfficeOutput}.
	 */
	void initialise(String argumentType);

	/**
	 * Loads the {@link OfficeOutputType}.
	 * 
	 * @param compileContext
	 *            {@link CompileContext}.
	 * @return {@link OfficeOutputType} or <code>null</code> if can not
	 *         determine.
	 */
	OfficeOutputType loadOfficeOutputType(CompileContext compileContext);

}