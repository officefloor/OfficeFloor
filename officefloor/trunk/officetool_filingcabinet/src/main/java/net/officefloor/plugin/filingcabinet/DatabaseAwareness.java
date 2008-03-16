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
package net.officefloor.plugin.filingcabinet;

import java.sql.Types;

/**
 * Provides additional awareness of a database.
 * 
 * @author Daniel
 */
public interface DatabaseAwareness {

	/**
	 * Obtains the java {@link Class} for the input {@link Types}.
	 * 
	 * @param sqlType
	 *            Type from {@link Types}.
	 * @return Java {@link Class} for the input sqlType.
	 * @throws If
	 *             fails to determine the {@link Class} for the {@link Types}.
	 */
	Class<?> getJavaType(int sqlType) throws Exception;
}
