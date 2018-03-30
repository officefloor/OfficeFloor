/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.configurer.internal.inputs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import net.officefloor.eclipse.configurer.FlagBuilder;
import net.officefloor.eclipse.configurer.ListBuilder;
import net.officefloor.eclipse.configurer.TextBuilder;
import net.officefloor.eclipse.configurer.internal.AbstractBuilder;
import net.officefloor.eclipse.configurer.internal.CellRenderer;
import net.officefloor.eclipse.configurer.internal.ColumnRenderer;
import net.officefloor.eclipse.configurer.internal.ValueRendererContext;

/**
 * {@link ListBuilder} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ListBuilderImpl<M, I> extends AbstractBuilder<M, List<I>, ListBuilder<M, I>> implements ListBuilder<M, I> {

	/**
	 * {@link ColumnRenderer} instances.
	 */
	private final List<ColumnRenderer<I, ?>> renderers = new ArrayList<>();

	/**
	 * Items.
	 */
	private final SimpleListProperty<Row> rows = new SimpleListProperty<>();

	/**
	 * Instantiate.
	 * 
	 * @param label
	 *            Label.
	 */
	public ListBuilderImpl(String label) {
		super(label);
	}

	/**
	 * Convenience method to register the {@link ColumnRenderer}.
	 * 
	 * @param builder
	 *            Builder.
	 * @return Input builder.
	 */
	private <V, B extends ColumnRenderer<I, V>> B registerBuilder(B builder) {
		this.renderers.add(builder);
		return builder;
	}

	/*
	 * ============== AbstractBuilder ===============
	 */

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Node createInput(Property<List<I>> value) {

		// Create the table
		TableView<Row> table = new TableView<>(this.rows);
		table.setEditable(true);
		table.columnResizePolicyProperty().set(TableView.CONSTRAINED_RESIZE_POLICY);

		// Load the columns to the table
		TableColumn<Row, ?>[] columns = new TableColumn[this.renderers.size()];
		for (int i = 0; i < columns.length; i++) {
			ColumnRenderer<I, ?> columnRenderer = this.renderers.get(i);

			// Add the column
			int columnIndex = i;
			TableColumn<Row, ?> column = columnRenderer.createTableColumn((index) -> {
				Row row = table.getItems().get(index);
				return (Property) row.cells[columnIndex].getValue();
			});
			columns[i] = column;

			// Load the cell factory
			column.setCellValueFactory((row) -> {
				Property cellValue = row.getValue().cells[columnIndex].getValue();
				return cellValue;
			});
			column.setOnEditCommit((event) -> {
				Row row = table.getItems().get(event.getTablePosition().getRow());
				Property cellValue = row.cells[columnIndex].getValue();
				cellValue.setValue(event.getNewValue());
			});
		}
		table.getColumns().setAll(columns);

		// Load the rows
		List<I> items = value.getValue();
		List<Row> rows = new ArrayList<>(items.size());
		for (I item : items) {
			rows.add(new Row(item));
		}
		table.setItems(FXCollections.observableArrayList(rows));

		// Return the table
		return table;
	}

	/*
	 * =============== ListBuilder ==================
	 */

	@Override
	public TextBuilder<I> text(String label) {
		return this.registerBuilder(new TextBuilderImpl<>(label));
	}

	@Override
	public FlagBuilder<I> flag(String label) {
		return this.registerBuilder(new FlagBuilderImpl<>(label));
	}

	@Override
	public ListBuilder<M, I> addItem(Supplier<I> itemFactory) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ListBuilder<M, I> deleteItem() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Row of the {@link TableView}.
	 */
	private class Row implements ValueRendererContext<I> {

		/**
		 * Item.
		 */
		private final I item;

		/**
		 * {@link CellRenderer} instances for the {@link TableCell} instances.
		 */
		private final CellRenderer<I, ?>[] cells;

		/**
		 * Instantiate.
		 * 
		 * @param item
		 *            Item for the {@link Row}.
		 */
		@SuppressWarnings("unchecked")
		public Row(I item) {
			this.item = item;

			// Create the properties for the row
			this.cells = new CellRenderer[ListBuilderImpl.this.renderers.size()];
			for (int i = 0; i < this.cells.length; i++) {
				this.cells[i] = ListBuilderImpl.this.renderers.get(i).createCellRenderer(this);
			}
		}

		/*
		 * ============== ValueRendererContext ===============
		 */

		@Override
		public I getModel() {
			return this.item;
		}

		@Override
		public void refreshError() {
			// TODO Auto-generated method stub

		}
	}

}