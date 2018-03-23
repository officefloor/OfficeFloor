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
package net.officefloor.eclipse.editor;

import java.util.List;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.Pane;
import net.officefloor.eclipse.editor.models.AdaptedConnector;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;

/**
 * Adapted {@link Model}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedChild<M extends Model> extends AdaptedModel<M> {

	/**
	 * Obtains the {@link ReadOnlyStringProperty} for the label.
	 * 
	 * @return {@link StringProperty} for the label. May be <code>null</code> if no
	 *         label.
	 */
	ReadOnlyStringProperty getLabel();

	/**
	 * Obtains the {@link StringProperty} to edit the label.
	 * 
	 * @return {@link StringProperty} to edit the label. May be <code>null</code> if
	 *         label not editable.
	 */
	StringProperty getEditLabel();

	/**
	 * Obtains the {@link ChildrenGroup} instances.
	 * 
	 * @return {@link ChildrenGroup} instances.
	 */
	List<ChildrenGroup<M, ?>> getChildrenGroups();

	/**
	 * Obtains the {@link AdaptedConnector} instances.
	 * 
	 * @return {@link AdaptedConnector} instances.
	 */
	List<AdaptedConnector<M>> getAdaptedConnectors();

	/**
	 * Obtains the {@link AdaptedConnector}.
	 * 
	 * @param connectionClass
	 *            {@link ConnectionModel} {@link Class}.
	 * @return {@link AdaptedConnector}.
	 */
	AdaptedConnector<M> getAdaptedConnector(Class<? extends ConnectionModel> connectionClass);

	/**
	 * Obtains the {@link AdaptedConnection} instances of this {@link AdaptedChild}
	 * and all its {@link AdaptedChild} instances.
	 * 
	 * @return {@link AdaptedConnection} instances.
	 */
	List<AdaptedConnection<?>> getConnections();

	/**
	 * Indicates if this {@link AdaptedChild} can connect to the target
	 * {@link AdaptedChild}.
	 * 
	 * @param target
	 *            Target {@link AdaptedChild},
	 * @return <code>true</code> if can be connected.
	 */
	<T extends Model> boolean canConnect(AdaptedChild<T> target);

	/**
	 * Creates the {@link ConnectionModel} within the {@link Model} structure.
	 * 
	 * @param target
	 *            Target {@link AdaptedChild}.
	 */
	<T extends Model> void createConnection(AdaptedChild<T> target);

	/**
	 * Creates the visual {@link Pane}.
	 * 
	 * @param context
	 *            {@link AdaptedModelVisualFactoryContext}.
	 * @return Visual {@link Pane}.
	 */
	Pane createVisual(AdaptedModelVisualFactoryContext context);

}