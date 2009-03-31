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
package net.officefloor.compile.change;

/**
 * A distinct change in a type to a use of the type as an instance.
 * 
 * @author Daniel
 */
public interface Change {

	/**
	 * Target instance of the type which requires a {@link Change} to adhere to
	 * the type.
	 * 
	 * @return Target instance of the type.
	 */
	Object getTarget();

	/**
	 * Obtains a description of the {@link Change}.
	 * 
	 * @return Description of the {@link Change}.
	 */
	String getDescription();

	/**
	 * Applies this {@link Change} to the type instance.
	 */
	void apply();

	/**
	 * <p>
	 * Reverts this {@link Change} to the type instance (after being applied).
	 * <p>
	 * This enables do/undo functionality.
	 */
	void revert();

}