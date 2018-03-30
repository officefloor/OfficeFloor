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
import javafx.scene.control.Label;
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

		// Determine if can edit the value
		if (this.isEditable()) {

			// Provide editable text box
			TextField text = new TextField(value.getValue());
			text.textProperty().bindBidirectional(value);
			text.getStyleClass().add("configurer-input-text");
			return text;

		} else {

			// Provide label (can not edit)
			Label label = new Label(value.getValue());
			label.textProperty().bind(value);
			label.getStyleClass().add("configurer-input-label");
			return label;
		}
	}

	@Override
	protected Property<String> createCellProperty() {
		return new SimpleStringProperty();
	}

	@Override
	protected <R> void configureTableColumn(TableColumn<R, String> column,
			Callback<Integer, ObservableValue<String>> callback) {
		column.getStyleClass().add("configurer-input-column-" + this.getLabel().replace(' ', '-').replace('\t', '-'));
		column.setCellFactory((col) -> new EditingCell<>());
	}

	/**
	 * Editing {@link TableCell} with focus awareness.
	 */
	private static class EditingCell<R> extends TableCell<R, String> {

		/**
		 * {@link TextField} for editing.
		 */
		private TextField textField = null;

		/**
		 * Creates the {@link TextField}.
		 */
		private void createTextField() {
			if (this.textField == null) {

				// Lazy create the text field
				this.textField = new TextField();
				this.textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);

				// Commit on lost focus
				this.textField.focusedProperty().addListener((event, oldValue, newValue) -> {
					if (!newValue) {
						this.commitEdit(this.textField.getText());
					}
				});

				// Commit on press enter
				this.textField.setOnAction((event) -> {
					this.commitEdit(this.textField.getText());

					// Keep focus on the cell
					this.requestFocus();
				});

				// Cancel edit on escape
				this.textField.setOnKeyPressed((event) -> {
					switch (event.getCode()) {
					case ESCAPE:
						// Cancel the editing
						this.cancelEdit();
						this.setGraphic(null);
						break;
					default:
						// Allow typing
						break;
					}
				});
			}
		}

		/**
		 * Obtain the value.
		 * 
		 * @return Value.
		 */
		private String getString() {
			return this.getItem() == null ? "" : this.getItem().toString();
		}

		/*
		 * ============ TableCell ================
		 */

		@Override
		public void startEdit() {
			super.startEdit();

			// Create the text field
			this.createTextField();
			this.textField.setText(this.getString());
			this.setGraphic(this.textField);

			// Take focus and have all content selected
			this.textField.requestFocus();
			this.textField.selectAll();
		}

		@Override
		public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);

			// Handle stop editing
			if (!this.isEditing()) {
				// Update the text for cell with new value
				this.setText(this.getString());
				this.setGraphic(null);
			}
		}
	}

}