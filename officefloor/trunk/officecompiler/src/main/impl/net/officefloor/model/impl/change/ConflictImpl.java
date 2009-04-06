/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.model.impl.change;

import net.officefloor.model.change.Conflict;

/**
 * {@link Conflict} implementation.
 * 
 * @author Daniel
 */
public class ConflictImpl implements Conflict {

	/**
	 * Description of the {@link Conflict}.
	 */
	private final String conflictDescription;

	/**
	 * Initiate.
	 * 
	 * @param conflictDescription
	 *            Description of the {@link Conflict}.
	 */
	public ConflictImpl(String conflictDescription) {
		this.conflictDescription = conflictDescription;
	}

	/*
	 * ====================== Conflict ==================================
	 */

	@Override
	public String getConflictDescription() {
		return this.conflictDescription;
	}

}