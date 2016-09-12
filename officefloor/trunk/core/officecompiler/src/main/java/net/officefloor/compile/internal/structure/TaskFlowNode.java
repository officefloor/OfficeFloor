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

import net.officefloor.compile.spi.section.TaskFlow;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;

/**
 * {@link TaskFlow} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface TaskFlowNode extends LinkFlowNode, TaskFlow {

	/**
	 * {@link Node} type.
	 */
	String TYPE = "Task Flow";

	/**
	 * Obtains the {@link FlowInstigationStrategyEnum} for the {@link TaskFlow}.
	 * 
	 * @return {@link FlowInstigationStrategyEnum} for this {@link TaskFlow}.
	 */
	FlowInstigationStrategyEnum getFlowInstigationStrategy();

	/**
	 * Specifies the {@link FlowInstigationStrategyEnum} for this
	 * {@link TaskFlow}.
	 * 
	 * @param instigationStrategy
	 *            {@link FlowInstigationStrategyEnum} for this {@link TaskFlow}.
	 */
	void setFlowInstigationStrategy(
			FlowInstigationStrategyEnum instigationStrategy);
}