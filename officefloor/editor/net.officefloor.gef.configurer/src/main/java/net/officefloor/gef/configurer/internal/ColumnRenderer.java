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