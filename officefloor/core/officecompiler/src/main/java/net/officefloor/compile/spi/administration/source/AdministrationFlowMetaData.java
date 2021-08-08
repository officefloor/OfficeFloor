/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.spi.administration.source;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.internal.structure.AdministrationMetaData;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Describes a {@link Flow} required by the {@link Administration}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationFlowMetaData<F extends Enum<F>> {

	/**
	 * Obtains the {@link Enum} key identifying this {@link Flow}. If
	 * <code>null</code> then {@link Flow} will be referenced by this instance's
	 * index in the array returned from {@link AdministrationMetaData}.
	 * 
	 * @return {@link Enum} key identifying the {@link Flow} or
	 *         <code>null</code> indicating identified by an index.
	 */
	F getKey();

	/**
	 * <p>
	 * Obtains the {@link Class} of the argument that is passed to the
	 * {@link Flow}.
	 * <p>
	 * This may be <code>null</code> to indicate no argument is passed.
	 * 
	 * @return Type of the argument that is passed to the {@link Flow}.
	 */
	Class<?> getArgumentType();

	/**
	 * Provides a descriptive name for this {@link Flow}. This is useful to
	 * better describe the {@link Flow}.
	 * 
	 * @return Descriptive name for this {@link Flow}.
	 */
	String getLabel();

}
