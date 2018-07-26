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
package net.officefloor.frame.impl.execute.executive;

import java.util.concurrent.ThreadFactory;

import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.source.ExecutiveSource;
import net.officefloor.frame.api.executive.source.ExecutiveSourceContext;
import net.officefloor.frame.api.executive.source.impl.AbstractExecutiveSource;
import net.officefloor.frame.impl.execute.execution.ThreadFactoryManufacturer;

/**
 * Default {@link Executive}.
 * 
 * @author Daniel Sagenschneider
 */
public class DefaultExecutive extends AbstractExecutiveSource implements Executive, ExecutionStrategy {

	/**
	 * {@link ThreadFactory} instances.
	 */
	private ThreadFactory[] threadFactories;

	/**
	 * Default construct to be used as {@link ExecutiveSource}.
	 */
	public DefaultExecutive() {
	}

	/**
	 * Instantiate to use as {@link Executive}.
	 * 
	 * @param threadFactoryManufacturer {@link ThreadFactoryManufacturer}.
	 */
	public DefaultExecutive(ThreadFactoryManufacturer threadFactoryManufacturer) {
		this.threadFactories = new ThreadFactory[] {
				threadFactoryManufacturer.manufactureThreadFactory(this.getExecutionStrategyName()) };
	}

	/*
	 * ================= ExecutiveSource =================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	public Executive createExecutive(ExecutiveSourceContext context) throws Exception {
		this.threadFactories = new ThreadFactory[] { context.createThreadFactory(this.getExecutionStrategyName()) };
		return this;
	}

	/*
	 * =================== Executive =====================
	 */

	@Override
	public ExecutionStrategy[] getExcutionStrategies() {
		return new ExecutionStrategy[] { this };
	}

	/*
	 * ================ ExecutionStrategy ================
	 */

	@Override
	public String getExecutionStrategyName() {
		return "default";
	}

	@Override
	public ThreadFactory[] getThreadFactories() {
		return this.threadFactories;
	}

}