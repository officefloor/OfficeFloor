/*-
 * #%L
 * [bundle] OfficeFloor Configurer
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
