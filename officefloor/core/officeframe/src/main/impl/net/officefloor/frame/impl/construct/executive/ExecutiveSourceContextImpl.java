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

package net.officefloor.frame.impl.construct.executive;

import java.util.concurrent.ThreadFactory;

import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.source.ExecutiveSourceContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.impl.execute.execution.ThreadFactoryManufacturer;
import net.officefloor.frame.impl.execute.executive.DefaultExecutive;

/**
 * {@link ExecutiveSourceContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ExecutiveSourceContextImpl extends SourceContextImpl implements ExecutiveSourceContext {

	/**
	 * Name for the {@link Executive}.
	 */
	public static final String EXECUTIVE_NAME = "Executive";

	/**
	 * {@link ThreadFactoryManufacturer}.
	 */
	private final ThreadFactoryManufacturer threadFactoryManufacturer;

	/**
	 * Default {@link Executive}.
	 */
	private final Executive defaultExecutive;

	/**
	 * Instantiate.
	 * 
	 * @param isLoadingType             Indicates if loading type.
	 * @param sourceContext             {@link SourceContext}.
	 * @param sourceProperties          {@link SourceProperties}.
	 * @param threadFactoryManufacturer {@link ThreadFactoryManufacturer}.
	 */
	public ExecutiveSourceContextImpl(boolean isLoadingType, SourceContext sourceContext,
			SourceProperties sourceProperties, ThreadFactoryManufacturer threadFactoryManufacturer) {
		super(EXECUTIVE_NAME, isLoadingType, new String[0], sourceContext, sourceProperties);
		this.threadFactoryManufacturer = threadFactoryManufacturer;

		// Create the default executive
		this.defaultExecutive = new DefaultExecutive(this.threadFactoryManufacturer);
	}

	/*
	 * ================ ExecutiveSourceContext ===========================
	 */

	@Override
	public ThreadFactory createThreadFactory(String executionStrategyName, Executive executive) {

		// Ensure have executive
		if (executive == null) {
			executive = this.defaultExecutive;
		}

		// Return the thread factory
		return this.threadFactoryManufacturer.manufactureThreadFactory(executionStrategyName, executive);
	}

}
