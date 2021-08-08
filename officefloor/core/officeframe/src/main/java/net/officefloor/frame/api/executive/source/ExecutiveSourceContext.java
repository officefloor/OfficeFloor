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

package net.officefloor.frame.api.executive.source;

import java.util.concurrent.ThreadFactory;

import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.source.SourceContext;

/**
 * Context for the {@link ExecutiveSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ExecutiveSourceContext extends SourceContext {

	/**
	 * Creates the underlying {@link ThreadFactory} that should be used for
	 * {@link ExecutionStrategy} instances.
	 * 
	 * @param executionStrategyName Name of the {@link ExecutionStrategy} to
	 *                              associate {@link Thread} names to the
	 *                              {@link ExecutionStrategy}.
	 * @param executive             {@link Executive}.
	 * @return {@link ThreadFactory} to use for {@link ExecutionStrategy} instances.
	 */
	ThreadFactory createThreadFactory(String executionStrategyName, Executive executive);

}
