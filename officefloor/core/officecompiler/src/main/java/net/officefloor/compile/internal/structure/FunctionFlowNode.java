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

import net.officefloor.compile.spi.section.FunctionFlow;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * {@link FunctionFlow} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface FunctionFlowNode extends LinkFlowNode, FunctionFlow {

	/**
	 * {@link Node} type.
	 */
	String TYPE = "Function Flow";

	/**
	 * Initialises the {@link FunctionFlowNode}.
	 */
	void initialise();

	/**
	 * Indicates whether to spawn a {@link ThreadState} for this
	 * {@link FunctionFlow}.
	 * 
	 * @return <code>true</code> to spawn a {@link ThreadState} for this
	 *         {@link FunctionFlow}.
	 */
	boolean isSpawnThreadState();

	/**
	 * Specifies whether to spawn a {@link ThreadState} for this
	 * {@link FunctionFlow}.
	 * 
	 * @param isSpawnThreadState
	 *            <code>true</code> to spawn a {@link ThreadState} for this
	 *            {@link FunctionFlow}.
	 */
	void setSpawnThreadState(boolean isSpawnThreadState);

}