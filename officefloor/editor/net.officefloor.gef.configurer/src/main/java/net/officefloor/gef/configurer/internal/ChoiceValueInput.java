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

import java.util.function.Supplier;

import javafx.beans.property.ReadOnlyProperty;

/**
 * {@link ValueInput} allowing for rendering choice of following
 * {@link ValueInput} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface ChoiceValueInput<M> extends ValueInput {

	/**
	 * Obtains the array of {@link ValueRendererFactory} instances.
	 * 
	 * @return Array of {@link ValueRendererFactory} instances.
	 */
	Supplier<ValueRendererFactory<M, ? extends ValueInput>[]>[] getChoiceValueRendererFactories();

	/**
	 * Obtains the index into choice {@link ValueRenderer} listing to display.
	 * 
	 * @return Index into choice {@link ValueRenderer} listing to display.
	 */
	ReadOnlyProperty<Integer> getChoiceIndex();

}
