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

package net.officefloor.frame.api.executive;

import net.officefloor.frame.internal.structure.ProcessState;

/**
 * <p>
 * Optional interface for {@link Executive} to implement to indicate it supports
 * background scheduling of {@link Runnable} instances.
 * <p>
 * Some {@link Executive} implementations can only have {@link ProcessState}
 * scoped {@link Thread} instances that disallow long running background
 * {@link Thread}. However, long running background {@link Thread} allows more
 * efficiency in running scheduled {@link Runnable} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface BackgroundScheduler {

	/**
	 * Schedules the {@link Runnable} to be executed after the delay.
	 * 
	 * @param delay    Delay in milliseconds.
	 * @param runnable {@link Runnable}.
	 */
	void schedule(long delay, Runnable runnable);

}
