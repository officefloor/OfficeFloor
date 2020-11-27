/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.impl.execute.executive;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.TeamOversight;
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
	 * Default {@link ExecutionStrategy} name.
	 */
	public static final String EXECUTION_STRATEGY_NAME = "default";

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
				threadFactoryManufacturer.manufactureThreadFactory(this.getExecutionStrategyName(), this) };
	}

	/**
	 * Obtains the {@link ExecutionStrategy} instances by name.
	 * 
	 * @return {@link ExecutionStrategy} instances by name.
	 */
	public Map<String, ThreadFactory[]> getExecutionStrategyMap() {
		Map<String, ThreadFactory[]> executionStrategies = new HashMap<>();
		executionStrategies.put(this.getExecutionStrategyName(), this.threadFactories);
		return executionStrategies;
	}

	/**
	 * Obtains the {@link TeamOversight} instances by name.
	 * 
	 * @return {@link TeamOversight} instances by name.
	 */
	public Map<String, TeamOversight> getTeamOversightMap() {
		return Collections.emptyMap();
	}

	/*
	 * ================= ExecutiveSource =================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	public Executive createExecutive(ExecutiveSourceContext context) throws Exception {
		int availableProcessors = Runtime.getRuntime().availableProcessors();
		this.threadFactories = new ThreadFactory[availableProcessors];
		for (int i = 0; i < availableProcessors; i++) {
			this.threadFactories[i] = context.createThreadFactory(this.getExecutionStrategyName() + "-" + i, this);
		}
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
		return EXECUTION_STRATEGY_NAME;
	}

	@Override
	public ThreadFactory[] getThreadFactories() {
		return this.threadFactories;
	}

	@Override
	public void startManaging() throws Exception {
		// TODO Implement Executive.startManaging
		throw new IllegalStateException("TODO implement Executive.startManaging");
	}

	@Override
	public void stopManaging() throws Exception {
		// TODO Implement Executive.stopManaging
		throw new IllegalStateException("TODO implement Executive.stopManaging");
	}

}
