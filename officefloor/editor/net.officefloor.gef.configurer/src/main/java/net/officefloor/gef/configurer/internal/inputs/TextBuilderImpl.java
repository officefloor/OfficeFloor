/*-
 * #%L
 * OfficeFloor Configurer
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

package net.officefloor.gef.configurer.internal.inputs;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import net.officefloor.gef.configurer.TextBuilder;
import net.officefloor.gef.configurer.internal.AbstractBuilder;
import net.officefloor.gef.configurer.internal.ValueInput;
import net.officefloor.gef.configurer.internal.ValueInputContext;

/**
 * {@link TextBuilder} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class TextBuilderImpl<M> extends AbstractBuilder<M, String, ValueInput, TextBuilder<M>>
		implements TextBuilder<M> {

	/**
	 * Indicates if text may contain multiple lines.
	 */
	private boolean isMultiLine = false;

	/**
	 * Instantiate.
	 * 
	 * @param label Label.
	 */
	public TextBuilderImpl(String label) {
		super(label);
	}

	/*
	 * ============== TextBuilder ================
	 */

	@Override
	public TextBuilder<M> multiline(boolean isMultiline) {
		this.isMultiLine = isMultiline;
		return this;
	}

	/*
	 * ============= AbstractBuilder ==============
	 */

	@Override
	protected ValueInput createInput(ValueInputContext<M, String> context) {

		// Obtain the value
		Property<String> value = context.getInputValue();

		// Determine if can edit the value
		if (this.isEditable()) {

			// Provide editable text box
			if (this.isMultiLine) {
				TextArea text = new TextArea();
				text.textProperty().bindBidirectional(value);
				text.getStyleClass().add("configurer-input-text");
				return () -> text;
			} else {
				TextField text = new TextField();
				text.textProperty().bindBidirectional(value);
				text.getStyleClass().add("configurer-input-text");
				return () -> text;
			}

		} else {

			// Provide label (can not edit)
			Label label = new Label();
			label.textProperty().bind(value);
			label.getStyleClass().add("configurer-input-label");
			return () -> label;
		}
	}

	@Override
	protected Property<String> createCellProperty() {
		return new SimpleStringProperty();
	}

	@Override
	protected <R> void configureTableColumn(TableView<R> table, TableColumn<R, String> column,
			Callback<Integer, ObservableValue<String>> callback) {
		column.getStyleClass().add("configurer-input-column-" + this.getLabel().replace(' ', '-').replace('\t', '-'));
		column.setCellFactory((col) -> new EditingCell<>(column));
	}

	/**
	 * Editing {@link TableCell} with focus awareness.
	 */
	private static class EditingCell<R> extends TableCell<R, String> {

		/**
		 * {@link TableColumn}.
		 */
		private final TableColumn<R, String> column;

		/**
		 * {@link TextField} for editing.
		 */
		private TextField textField = null;

		/**
		 * Instantiate.
		 * 
		 * @param column {@link TableColumn}.
		 */
		public EditingCell(TableColumn<R, String> column) {
			this.column = column;
		}

		/**
		 * Creates the {@link TextField}.
		 */
		private void createTextField() {
			if (this.textField == null) {

				// Lazy create the text field
				this.textField = new TextField();
				this.textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);

				// Commit on lost focus
				this.textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
					if (!newValue) {
						this.commitEdit(this.textField.getText());
					}
				});

				// Commit on press enter
				this.textField.setOnAction((event) -> {
					this.commitEdit(this.textField.getText());
				});

				// Cancel edit on escape
				this.textField.setOnKeyPressed((event) -> {
					switch (event.getCode()) {
					case ESCAPE:
						// Cancel the editing
						this.cancelEdit();
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

		/**
		 * Keeps this {@link TableCell} selected after edits.
		 */
		private void keepSelected() {
			TableView<R> table = this.column.getTableView();
			if (table != null) {
				int row = this.getIndex();

				// Keep focus on table and selected cell
				this.getTableView().requestFocus();
				table.getSelectionModel().select(row, this.column);
			}
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

			// Place focus on text box
			this.textField.requestFocus();
			this.textField.selectAll();
		}

		@Override
		public void cancelEdit() {
			super.cancelEdit();

			// Clear editing
			this.setGraphic(null);

			// Keep selection
			this.keepSelected();
		}

		@Override
		protected void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);

			// Handle add row
			if (ListBuilderImpl.isUpdateItemAddRow(this)) {
				return;
			}

			// Provide value for display
			this.setGraphic(null);
			this.setText(item);
		}

		@Override
		public void commitEdit(String newValue) {
			super.commitEdit(newValue);

			// Keep selection
			this.keepSelected();
		}
	}

}
