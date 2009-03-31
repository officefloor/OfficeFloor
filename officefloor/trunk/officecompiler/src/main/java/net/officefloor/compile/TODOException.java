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
package net.officefloor.compile;

import net.officefloor.compile.issues.CompilerIssues;

/**
 * TODO exception (that is focus to be removed in light of using
 * {@link CompilerIssues}).
 * 
 * @author Daniel
 */
public class TODOException extends RuntimeException {

	/**
	 * Initiate.
	 */
	public TODOException() {
		super();
	}

	/**
	 * Initiate.
	 * 
	 * @param message
	 *            Message.
	 */
	public TODOException(String message) {
		super(message);
	}

}
