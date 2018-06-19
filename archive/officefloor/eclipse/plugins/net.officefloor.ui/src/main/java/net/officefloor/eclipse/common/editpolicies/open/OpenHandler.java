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
package net.officefloor.eclipse.common.editpolicies.open;

import net.officefloor.model.Model;

import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;

/**
 * {@link Command} factory for an open {@link Request} (
 * {@link RequestConstants#REQ_OPEN}).
 * 
 * @author Daniel Sagenschneider
 */
public interface OpenHandler<M extends Model> {

	/**
	 * Triggers handle the open.
	 * 
	 * @param context
	 *            {@link OpenHandlerContext} for the open.
	 */
	void doOpen(OpenHandlerContext<M> context);

}