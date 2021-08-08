/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.model;

import java.beans.PropertyChangeListener;

/**
 * Contract for top level functionality for all model elements.
 * 
 * @author Daniel Sagenschneider
 */
public interface Model {

	/**
	 * Obtains the X co-ordinate for the model.
	 * 
	 * @return X co-ordinate for the model.
	 */
	int getX();

	/**
	 * Specifies the X co-ordinate for the model.
	 * 
	 * @param x
	 *            X co-ordinate for the model.
	 */
	void setX(int x);

	/**
	 * Obtains the Y co-ordinate for the model.
	 * 
	 * @return Y co-ordinate for the model.
	 */
	int getY();

	/**
	 * Specifies the Y co-ordinate for the model.
	 * 
	 * @param y
	 *            Y co-ordinate for the model.
	 */
	void setY(int y);

	/**
	 * Adds a {@link PropertyChangeListener} to this model element.
	 * 
	 * @param listener
	 *            {@link PropertyChangeListener} to this model element.
	 */
	void addPropertyChangeListener(PropertyChangeListener listener);

	/**
	 * Removes a {@link PropertyChangeListener} from this model element.
	 * 
	 * @param listener
	 *            {@link PropertyChangeListener}.
	 */
	void removePropertyChangeListener(PropertyChangeListener listener);

}
