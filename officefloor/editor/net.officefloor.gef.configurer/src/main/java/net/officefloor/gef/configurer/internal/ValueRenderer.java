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

import javafx.scene.Node;
import net.officefloor.gef.configurer.Builder;

/**
 * <p>
 * Renders the values.
 * <p>
 * Implementations must provide new instances of the {@link Node}, as there may
 * be different layouts requiring multiple {@link Node} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface ValueRenderer<M, I extends ValueInput> {

	/**
	 * Creates a new input {@link ValueInput}. {@link ValueInput} responsible for
	 * capturing the configuration via the UI.
	 * 
	 * @return New input {@link ValueInput}.
	 */
	I createInput();

	/**
	 * Obtains the label text.
	 * 
	 * @param valueInput {@link ValueInput}.
	 * @return Label text.
	 */
	String getLabel(I valueInput);

	/**
	 * Creates a new label {@link Node}.
	 * 
	 * @param labelText  Label text.
	 * @param valueInput {@link ValueInput}.
	 * @return New label {@link Node}.
	 */
	Node createLabel(String labelText, I valueInput);

	/**
	 * Creates a new error feedback {@link Node}.
	 * 
	 * @param valueInput {@link ValueInput}.
	 * @return Error feedback {@link Node}.
	 */
	Node createErrorFeedback(I valueInput);

	/**
	 * Triggers to reload the value if matches the {@link Builder}.
	 * 
	 * @param builder {@link Builder} identifying the value to be reloaded from the
	 *                model.
	 * @return <code>true</code> if the {@link Builder}.
	 */
	boolean reloadIf(Builder<?, ?, ?> builder);

	/**
	 * Obtains the error with value.
	 * 
	 * @param valueInput {@link ValueInput}.
	 * @return Error with value. <code>null</code> to indicate no error.
	 * 
	 * @see MessageOnlyException
	 */
	Throwable getError(I valueInput);

}
