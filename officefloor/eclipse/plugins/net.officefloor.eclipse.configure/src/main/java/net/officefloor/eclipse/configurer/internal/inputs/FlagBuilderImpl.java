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
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.util.Callback;
import net.officefloor.eclipse.configurer.FlagBuilder;
import net.officefloor.eclipse.configurer.TextBuilder;
import net.officefloor.eclipse.configurer.internal.AbstractBuilder;

/**
 * {@link TextBuilder} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class FlagBuilderImpl<M> extends AbstractBuilder<M, Boolean, FlagBuilder<M>> implements FlagBuilder<M> {

	/**
	 * Instantiate.
	 * 
	 * @param label
	 *            Label.
	 */
	public FlagBuilderImpl(String label) {
		super(label);
	}

	/*
	 * ============= AbstractBuilder ==============
	 */

	@Override
	protected Node createInput(Property<Boolean> value) {
		CheckBox checkBox = new CheckBox();
		if (this.isEditable()) {
			checkBox.selectedProperty().bindBidirectional(value);
		} else {
			checkBox.setDisable(true);
			checkBox.selectedProperty().bind(value);
		}
		checkBox.getStyleClass().add("configurer-input-checkbox");
		return checkBox;
	}

	@Override
	protected Property<Boolean> createCellProperty() {
		return new SimpleBooleanProperty();
	}

	@Override
	protected <R> void configureTableColumn(TableColumn<R, Boolean> column,
			Callback<Integer, ObservableValue<Boolean>> callback) {
		column.setCellFactory(CheckBoxTableCell.forTableColumn(callback));
	}

}