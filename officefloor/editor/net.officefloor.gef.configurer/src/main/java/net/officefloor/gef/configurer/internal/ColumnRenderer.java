/*-
 * #%L
 * [bundle] OfficeFloor Configurer
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
