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
