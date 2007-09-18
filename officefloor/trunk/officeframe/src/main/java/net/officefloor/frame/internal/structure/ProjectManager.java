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
package net.officefloor.frame.internal.structure;

/**
 * Manages the {@link net.officefloor.frame.internal.structure.AssetManager}
 * instances within the Office.
 * 
 * @author Daniel
 */
public interface ProjectManager {

	/**
	 * Starts this {@link ProjectManager} managing the
	 * {@link net.officefloor.frame.api.execute.Work} within the Office.
	 * 
	 * @param interval
	 *            Interval in milliseconds between each check of the Office.
	 *            Setting this high reduces overhead of managing the office,
	 *            however setting lower increases responsiveness of the Office.
	 */
	void startManaging(long interval);

	/**
	 * <p>
	 * Does the managing the {@link net.officefloor.frame.api.execute.Work}
	 * within the Office.
	 * <p>
	 * Provided to allow manual invocation of managing the Office.
	 */
	void manage();

	/**
	 * Stops this {@link ProjectManager} managing the
	 * {@link net.officefloor.frame.api.execute.Work} within the Office.
	 */
	void stopManaging();
}
