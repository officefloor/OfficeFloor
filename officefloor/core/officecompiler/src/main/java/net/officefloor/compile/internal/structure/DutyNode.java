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

import net.officefloor.compile.spi.office.OfficeDuty;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.function.ManagedFunction;

/**
 * {@link OfficeDuty} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface DutyNode extends Node, OfficeDuty {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Duty";

	/**
	 * Initialises the {@link DutyNode}.
	 */
	void initialise();

	/**
	 * Builds the pre {@link ManagedFunction} administration.
	 * 
	 * @param workBuilder
	 *            {@link WorkBuilder}.
	 * @param taskBuilder
	 *            {@link ManagedFunctionBuilder}.
	 */
	void buildPreTaskAdministration(WorkBuilder<?> workBuilder,
			ManagedFunctionBuilder<?, ?, ?> taskBuilder);

	/**
	 * Builds the post {@link ManagedFunction} administration.
	 * 
	 * @param workBuilder
	 *            {@link WorkBuilder}.
	 * @param taskBuilder
	 *            {@link ManagedFunctionBuilder}.
	 */
	void buildPostTaskAdministration(WorkBuilder<?> workBuilder,
			ManagedFunctionBuilder<?, ?, ?> taskBuilder);

}