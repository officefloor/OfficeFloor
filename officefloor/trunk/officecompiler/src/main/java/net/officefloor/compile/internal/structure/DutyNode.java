/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.compile.internal.structure;

import net.officefloor.compile.spi.office.OfficeDuty;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.execute.Task;

/**
 * {@link OfficeDuty} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface DutyNode extends OfficeDuty {

	/**
	 * Builds the pre {@link Task} administration.
	 * 
	 * @param workBuilder
	 *            {@link WorkBuilder}.
	 * @param taskBuilder
	 *            {@link TaskBuilder}.
	 */
	void buildPreTaskAdministration(WorkBuilder<?> workBuilder,
			TaskBuilder<?, ?, ?> taskBuilder);

	/**
	 * Builds the post {@link Task} administration.
	 * 
	 * @param workBuilder
	 *            {@link WorkBuilder}.
	 * @param taskBuilder
	 *            {@link TaskBuilder}.
	 */
	void buildPostTaskAdministration(WorkBuilder<?> workBuilder,
			TaskBuilder<?, ?, ?> taskBuilder);

}