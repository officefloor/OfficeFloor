/*-
 * #%L
 * [bundle] OfficeFloor Configurer
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

package net.officefloor.gef.configurer.internal;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

/**
 * Renderer for a {@link TableView} {@link TableColumn}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ColumnRenderer<I, V> {

	/**
	 * Creates the {@link TableColumn}.
	 *
	 * @param <R>
	 *            Row object type.
	 * @param table
	 *            {@link TableView} to contain the {@link TableColumn}.
	 * @param callback
	 *            {@link Callback}.
	 * @return {@link TableColumn}.
	 */
	<R> TableColumn<R, V> createTableColumn(TableView<R> table, Callback<Integer, ObservableValue<V>> callback);

	/**
	 * Indicates if the column is editable.
	 * 
	 * @return <code>true</code> if column is editable.
	 */
	boolean isEditable();

	/**
	 * Creates the {@link ValueRenderer} for the cell.
	 * 
	 * @param context
	 *            {@link ValueRendererContext}.
	 * @return {@link ValueRenderer}.
	 */
	CellRenderer<I, V> createCellRenderer(ValueRendererContext<I> context);

}
