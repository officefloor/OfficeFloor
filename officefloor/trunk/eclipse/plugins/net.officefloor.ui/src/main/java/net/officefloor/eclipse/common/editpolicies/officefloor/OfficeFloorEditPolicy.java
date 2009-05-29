/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.eclipse.common.editpolicies.officefloor;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.model.Model;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.AbstractEditPolicy;

/**
 * {@link EditPolicy} for the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorEditPolicy extends AbstractEditPolicy {

	/**
	 * {@link DoubleClickCommandFactory} by {@link Model} type it handles.
	 */
	private final Map<Class<?>, DoubleClickCommandFactory<?>> factories = new HashMap<Class<?>, DoubleClickCommandFactory<?>>();

	/**
	 * Adds a {@link DoubleClickCommandFactory}.
	 * 
	 * @param modelType
	 *            Type of the {@link Model}.
	 * @param factory
	 *            {@link DoubleClickCommandFactory}.
	 */
	public <M> void addDoubleClick(Class<M> modelType,
			DoubleClickCommandFactory<M> factory) {
		this.factories.put(modelType, factory);
	}

	/*
	 * ==================== EditPolicy ===========================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public Command getCommand(Request request) {
		// Obtain request type
		Object type = request.getType();

		// Handle request by delegating back to Edit Part
		if (REQ_OPEN.equals(type)) {

			// Double Click on Edit Part
			EditPart editPart = this.getHost();
			Object model = editPart.getModel();
			Class<?> modelType = model.getClass();

			// Obtain the factory for the model
			DoubleClickCommandFactory factory = this.factories.get(modelType);
			if (factory == null) {
				return null; // not handle double click on model
			}

			// Return command from the factory
			return factory.createCommand(model, editPart);
		}

		// Not to be handled
		return null;
	}

}