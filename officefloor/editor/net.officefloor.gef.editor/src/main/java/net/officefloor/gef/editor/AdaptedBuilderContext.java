/*-
 * #%L
 * [bundle] OfficeFloor Editor
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

package net.officefloor.gef.editor;

import java.util.function.Function;

import net.officefloor.model.Model;

/**
 * Provides means to build the adapted model.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedBuilderContext {

	/**
	 * Specifies the root {@link Model}.
	 * 
	 * @param <R>
	 *            Root {@link Model} type.
	 * @param <O>
	 *            Operations type.
	 * @param rootModelClass
	 *            {@link Class} of the root {@link Model}.
	 * @param createOperations
	 *            {@link Function} to create the operations object to wrap the root
	 *            {@link Model}.
	 * @return {@link AdaptedRootBuilder}.
	 */
	<R extends Model, O> AdaptedRootBuilder<R, O> root(Class<R> rootModelClass, Function<R, O> createOperations);

}
