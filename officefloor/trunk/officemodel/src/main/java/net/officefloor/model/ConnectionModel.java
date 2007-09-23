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
package net.officefloor.model;

/**
 * Interface to aid manipulation of connections/associations.
 * 
 * @author Daniel
 */
public interface ConnectionModel {

	/**
	 * Indicates if this connection is removable.
	 * 
	 * @return True if may remove the connection.
	 */
	boolean isRemovable();

	/**
	 * Connects the source and target.
	 */
	void connect();

	/**
	 * Removes the connection.
	 */
	void remove();
}
