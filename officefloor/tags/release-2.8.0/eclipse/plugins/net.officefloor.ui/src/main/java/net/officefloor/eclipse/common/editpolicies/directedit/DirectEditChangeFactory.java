/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.eclipse.common.editpolicies.directedit;

import net.officefloor.eclipse.common.editor.AbstractOfficeFloorEditor;
import net.officefloor.eclipse.desk.DeskEditor;
import net.officefloor.model.Model;
import net.officefloor.model.change.Change;
import net.officefloor.model.desk.DeskChanges;

/**
 * Creates a {@link Change} to directly edit the name.
 * 
 * @author Daniel Sagenschneider
 */
public interface DirectEditChangeFactory<C, M> {

	/**
	 * Creates the {@link Change} directly edit the {@link Model}.
	 * 
	 * @param changes
	 *            Changes for the {@link Model} that is obtained from the
	 *            {@link AbstractOfficeFloorEditor}. For example
	 *            {@link DeskChanges} for the {@link DeskEditor}.
	 * @param target
	 *            Target {@link Model} that is directly edited.
	 * @param newValue
	 *            New value for the {@link Model}.
	 * @return {@link Change} to apply the new value onto the {@link Model}.
	 */
	Change<M> createChange(C changes, M target, String newValue);

}