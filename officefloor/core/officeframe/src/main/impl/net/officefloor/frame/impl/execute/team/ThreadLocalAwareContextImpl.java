/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
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
