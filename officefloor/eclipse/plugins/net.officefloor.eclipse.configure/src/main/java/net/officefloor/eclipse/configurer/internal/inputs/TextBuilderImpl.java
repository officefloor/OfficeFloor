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

import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import net.officefloor.eclipse.configurer.TextBuilder;
import net.officefloor.eclipse.configurer.internal.AbstractBuilder;

/**
 * {@link TextBuilder} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class TextBuilderImpl<M> extends AbstractBuilder<M, String, TextBuilder<M>> implements TextBuilder<M> {

	/**
	 * Instantiate.
	 * 
	 * @param label
	 *            Label.
	 */
	public TextBuilderImpl(String label) {
		super(label);
	}

	/*
	 * ============= AbstractBuilder ==============
	 */

	@Override
	protected Node createInput(Property<String> value) {
		TextField text = new TextField(value.getValue());
		text.textProperty().bindBidirectional(value);
		text.getStyleClass().add("configurer-input-text");
		return text;
	}

	@Override
	protected Property<String> createCellProperty() {
		return new SimpleStringProperty();
	}

	@Override
	protected <R> void configureTableColumn(TableColumn<R, String> column,
			Callback<Integer, ObservableValue<String>> callback) {
		column.setCellFactory((col) -> new EditingCell<>());
	}

	/**
	 * Editing {@link TableCell} with focus awareness.
	 */
	private static class EditingCell<R> extends TableCell<R, String> {

		/**
		 * {@link TextField} for editing.
		 */
		private TextField textField;

		/*
		 * ============ TableCell ================
		 */

		@Override
		public void startEdit() {
			if (!this.isEmpty()) {
				super.startEdit();
				this.createTextField();
				this.setText(null);
				this.setGraphic(this.textField);
				this.textField.selectAll();
			}
		}

		@Override
		public void cancelEdit() {
			super.cancelEdit();
			this.setText(this.getString());
			this.setGraphic(null);
		}

		@Override
		public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);
			if (empty) {
				this.setText(null);
				this.setGraphic(null);
			} else {
				if (this.isEditing()) {
					if (this.textField != null) {
						this.textField.setText(getString());
					}
					this.setText(null);
					this.setGraphic(this.textField);
				} else {
					this.setText(this.getString());
					this.setGraphic(null);
				}
			}
		}

		private void createTextField() {
			this.textField = new TextField(getString());
			this.textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			this.textField.focusedProperty().addListener((event, oldValue, newValue) -> {
				if (!newValue) {
					commitEdit(this.textField.getText());
				}
			});
		}

		private String getString() {
			return this.getItem() == null ? "" : this.getItem().toString();
		}
	}

}