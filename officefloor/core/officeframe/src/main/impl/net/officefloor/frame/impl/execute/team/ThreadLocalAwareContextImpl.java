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
package net.officefloor.frame.impl.execute.team;

import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.ThreadLocalAwareContext;
import net.officefloor.frame.internal.structure.ThreadLocalAwareExecutor;

/**
 * {@link ThreadLocalAwareContext} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class ThreadLocalAwareContextImpl implements ThreadLocalAwareContext {

	/**
	 * {@link ThreadLocalAwareExecutor}.
	 */
	private final ThreadLocalAwareExecutor executor;

	/**
	 * Instantiate.
	 * 
	 * @param executor
	 *            {@link ThreadLocalAwareExecutor}.
	 */
	public ThreadLocalAwareContextImpl(ThreadLocalAwareExecutor executor) {
		this.executor = executor;
	}

	/*
	 * ================= ThreadLocalAwareContext =======================
	 */

	@Override
	public void execute(Job job) {
		this.executor.execute(job);
	}

}