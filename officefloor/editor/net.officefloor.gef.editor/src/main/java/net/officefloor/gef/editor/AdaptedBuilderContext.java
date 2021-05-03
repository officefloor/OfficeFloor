/*-
 * #%L
 * [bundle] OfficeFloor Editor
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
