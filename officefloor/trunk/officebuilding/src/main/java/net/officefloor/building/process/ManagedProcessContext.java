/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.building.process;

/**
 * Context for the {@link ManagedProcess}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedProcessContext {

	/**
	 * <p>
	 * Obtains the command arguments for the {@link ManagedProcess}.
	 * <p>
	 * This is equivalent to the command line arguments for the
	 * <code>public static void main(String[] args)</code> method for running a
	 * java program.
	 * 
	 * @return Command arguments.
	 */
	String[] getCommandArguments();

	/**
	 * Flags whether to continue processing.
	 * 
	 * @return <code>true</code> for the {@link ManagedProcess} to continue
	 *         processing. <code>false</code> to gracefully stop.
	 */
	boolean continueProcessing();

}