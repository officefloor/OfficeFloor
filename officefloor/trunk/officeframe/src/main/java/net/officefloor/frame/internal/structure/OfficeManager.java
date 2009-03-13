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

import net.officefloor.frame.api.manage.Office;

/**
 * Manages the {@link AssetManager} instances within an {@link Office}.
 * 
 * @author Daniel
 */
public interface OfficeManager {

	/**
	 * Starts this {@link OfficeManager} managing the {@link AssetManager}
	 * instances within the {@link Office}.
	 * 
	 * @param interval
	 *            Interval in milliseconds between each check of the
	 *            {@link Office}. Setting this high reduces overhead of managing
	 *            the {@link Office}, however setting lower increases
	 *            responsiveness of the {@link Office}.
	 */
	void startManaging(long interval);

	/**
	 * <p>
	 * Does the managing the {@link AssetManager} instances within the
	 * {@link Office}.
	 * <p>
	 * Provided to allow manual invocation of managing the {@link Office}.
	 */
	void manage();

	/**
	 * Stops this {@link OfficeManager} managing the {@link AssetManager}
	 * instances within the {@link Office}.
	 */
	void stopManaging();

}