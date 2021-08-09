/*-
 * #%L
 * [bundle] OfficeFloor Editor
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

package net.officefloor.gef.editor;

import java.util.List;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import net.officefloor.model.Model;

/**
 * Adapted {@link Model}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedChild<M extends Model> extends AdaptedConnectable<M> {

	/**
	 * Obtains the {@link ReadOnlyStringProperty} for the label.
	 * 
	 * @return {@link StringProperty} for the label. May be <code>null</code> if no
	 *         label.
	 */
	ReadOnlyProperty<String> getLabel();

	/**
	 * Obtains the {@link StringProperty} to edit the label.
	 * 
	 * @return {@link StringProperty} to edit the label. May be <code>null</code> if
	 *         label not editable.
	 */
	Property<String> getEditLabel();

	/**
	 * Obtains the {@link ChildrenGroup} instances.
	 * 
	 * @return {@link ChildrenGroup} instances.
	 */
	List<ChildrenGroup<M, ?>> getChildrenGroups();

	/**
	 * Creates the visual {@link Node}.
	 * 
	 * @param context {@link AdaptedChildVisualFactoryContext}.
	 * @return Visual {@link Node}.
	 */
	Node createVisual(AdaptedChildVisualFactoryContext<M> context);

}
