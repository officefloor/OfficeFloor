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
package net.officefloor.eclipse.bootstrap;

import java.util.Map;

import net.officefloor.eclipse.OfficeFloorPlugin;

/**
 * <p>
 * Implementations are run in their own {@link ClassLoader} instances.
 * <p>
 * It is therefore required that the implementations are not invoked except by
 * the {@link Bootstrap}. This includes all references to the implementation
 * that may cause it to be loaded in the {@link ClassLoader} of the
 * {@link OfficeFloorPlugin}.
 * 
 * @author Daniel Sagenschneider
 */
public interface Bootable {

	/**
	 * Method name for booting.
	 */
	static final String BOOT_METHOD_NAME = "boot";
	
	/**
	 * Boots the implementation.
	 * 
	 * @param arguments
	 *            Arguments to the implementation to specialise.
	 * @throws Throwable
	 *             If fails.
	 */
	void boot(Map<String, String> arguments) throws Throwable;

}
