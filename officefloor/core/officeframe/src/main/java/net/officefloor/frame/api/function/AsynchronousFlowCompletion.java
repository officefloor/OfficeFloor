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

package net.officefloor.frame.api.function;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * <p>
 * Allows {@link ThreadState} safe logic to run on the completion of the
 * {@link AsynchronousFlow}.
 * <p>
 * As the {@link AsynchronousFlow} is very likely to use other {@link Thread}
 * instances (and likely call the completion of {@link AsynchronousFlow} on
 * another {@link Thread}), this allows {@link ThreadState} logic to synchronise
 * the results back into the {@link ManagedFunction} and its dependent
 * {@link ManagedObject} instances.
 * 
 * @author Daniel Sagenschneider
 */
@FunctionalInterface
public interface AsynchronousFlowCompletion {

	/**
	 * Contains the {@link ThreadState} safe logic.
	 * 
	 * @throws Throwable Indicate a failure in the {@link AsynchronousFlow}.
	 */
	void run() throws Throwable;

}
