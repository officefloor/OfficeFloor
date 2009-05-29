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
package net.officefloor.eclipse.common.editpolicies.connection;

import org.eclipse.gef.requests.CreateConnectionRequest;

import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;
import net.officefloor.model.change.Change;

/**
 * Factory to create a {@link Change} to add a {@link ConnectionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ConnectionChangeFactory<S, T> {

	/**
	 * Creates a {@link Change} to add a {@link ConnectionModel} between the
	 * source and target.
	 * 
	 * @param source
	 *            Source {@link Model} of the {@link ConnectionModel}.
	 * @param target
	 *            Target {@link Model} of the {@link ConnectionModel}.
	 * @param request
	 *            {@link CreateConnectionRequest}.
	 * @return {@link Change} to add a {@link ConnectionModel} between the
	 *         source and target.
	 */
	Change<?> createChange(S source, T target, CreateConnectionRequest request);

}