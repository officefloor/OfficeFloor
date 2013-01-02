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
package net.officefloor.eclipse.common.editpolicies.layout;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;

/**
 * Request to delete a model from the office.
 * 
 * @author Daniel Sagenschneider
 */
public class DeleteRequest extends Request {

	/**
	 * {@link EditPart} instance to delete.
	 */
	private final EditPart[] editPartsToDelete;

	/**
	 * Deletes the sender of this request.
	 * 
	 * @param editPartsToDelete
	 *            {@link EditPart} instance to delete.
	 */
	public DeleteRequest(EditPart[] editPartsToDelete) {
		super(RequestConstants.REQ_DELETE_DEPENDANT);
		this.editPartsToDelete = editPartsToDelete;
	}

	/**
	 * Obtains the {@link EditPart} instances to delete.
	 * 
	 * @return {@link EditPart} instances to delete.
	 */
	public EditPart[] getEditPartsToDelete() {
		return this.editPartsToDelete;
	}
}