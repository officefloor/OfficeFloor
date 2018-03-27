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
package net.officefloor.eclipse.configurer;

import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableStringValue;

/**
 * Builder of item configuration.
 * 
 * @author Daniel Sagenschneider
 */
public interface ItemBuilder<M> {

	/**
	 * Adds text property to be configured.
	 * 
	 * @param label
	 *            Label.
	 * @param textLoader
	 *            {@link ValueLoader}.
	 * @return {@link TextBuilder}.
	 */
	TextBuilder<M> text(String label, ValueLoader<M, String> textLoader);

	/**
	 * Adds a {@link Class} property to be configured.
	 * 
	 * @param label
	 *            Label.
	 * @param classLoader
	 *            {@link ValueLoader}.
	 * @return {@link ClassBuilder}.
	 */
	ClassBuilder<M> clazz(String label, ValueLoader<M, String> classLoader);

	ObservableStringValue resource();

	ObservableBooleanValue flag();

	/**
	 * Validates the item.
	 * 
	 * @param validator
	 *            {@link ValueValidator}.
	 */
	void validate(ValueValidator<M> validator);

}