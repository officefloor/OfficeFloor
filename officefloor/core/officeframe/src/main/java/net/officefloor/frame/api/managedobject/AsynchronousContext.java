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

package net.officefloor.frame.api.managedobject;

/**
 * Context to be notified about asynchronous operations by the
 * {@link AsynchronousManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AsynchronousContext {

	/**
	 * Undertakes an {@link AsynchronousOperation}.
	 *
	 * @param <T>
	 *            Possible exception type from {@link AsynchronousOperation}.
	 * @param operation
	 *            Optional operation to be undertaken, once the
	 *            {@link AsynchronousManagedObject} is registered as started an
	 *            asynchronous operation. May be <code>null</code>.
	 */
	<T extends Throwable> void start(AsynchronousOperation<T> operation);

	/**
	 * Indicates that the {@link AsynchronousManagedObject} has completed and is
	 * ready for another operation.
	 * 
	 * @param <T>
	 *            Possible exception type from {@link AsynchronousOperation}.
	 * @param operation
	 *            Optional operation to be undertaken, once the
	 *            {@link AsynchronousManagedObject} is unregistered from undertaking
	 *            an asynchronous operation. May be <code>null</code>.
	 */
	<T extends Throwable> void complete(AsynchronousOperation<T> operation);

}
