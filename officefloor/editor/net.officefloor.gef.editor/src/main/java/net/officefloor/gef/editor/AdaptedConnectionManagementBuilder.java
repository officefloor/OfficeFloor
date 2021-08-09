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

import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;

/**
 * Builds management of the adapted {@link ConnectionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedConnectionManagementBuilder<R extends Model, O, S extends Model, C extends ConnectionModel, T extends Model> {

	/**
	 * Provides means to create the {@link ConnectionModel}.
	 * 
	 * @param createConnection
	 *            {@link ConnectionFactory}.
	 * @return <code>this</code>.
	 */
	AdaptedConnectionManagementBuilder<R, O, S, C, T> create(ConnectionFactory<R, O, S, C, T> createConnection);

	/**
	 * Provides means to delete the {@link ConnectionModel}.
	 * 
	 * @param removeConnection
	 *            {@link ConnectionRemover}.
	 * @return <code>this</code>.
	 */
	AdaptedConnectionManagementBuilder<R, O, S, C, T> delete(ConnectionRemover<R, O, C> removeConnection);

	/**
	 * {@link Function} interface to create a {@link ConnectionModel}.
	 */
	public static interface ConnectionFactory<R extends Model, O, S extends Model, C extends ConnectionModel, T extends Model> {

		/**
		 * Adds a {@link ConnectionModel}.
		 *
		 * @param source
		 *            Source {@link Model}.
		 * @param target
		 *            Target {@link Model}.
		 * @param context
		 *            {@link ModelActionContext}.
		 * @throws Exception
		 *             If failure in adding the {@link ConnectionModel}.
		 */
		void addConnection(S source, T target, ModelActionContext<R, O, C> context) throws Exception;
	}

	/**
	 * {@link Function} interface to remove a {@link ConnectionModel}.
	 */
	public static interface ConnectionRemover<R extends Model, O, C extends ConnectionModel> {

		/**
		 * Removes the {@link ConnectionModel}.
		 * 
		 * @param context
		 *            {@link ModelActionContext}.
		 * @throws Exception
		 *             If failure in removing the {@link ConnectionModel}.
		 */
		void removeConnection(ModelActionContext<R, O, C> context) throws Exception;
	}

}
