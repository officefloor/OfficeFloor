/*-
 * #%L
 * [bundle] OfficeFloor Editor
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
