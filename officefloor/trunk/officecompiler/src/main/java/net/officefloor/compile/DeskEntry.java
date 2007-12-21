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

import net.officefloor.desk.DeskLoader;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.DeskWorkModel;
import net.officefloor.repository.ConfigurationItem;

/**
 * {@link net.officefloor.model.desk.DeskModel} entry for the
 * {@link net.officefloor.frame.api.manage.Office}.
 * 
 * @author Daniel
 */
public class DeskEntry extends AbstractEntry<Object, DeskModel> {

	/**
	 * Loads the {@link DeskEntry}.
	 * 
	 * @param deskId
	 *            Id of the {@link DeskModel}.
	 * @param deskName
	 *            Name of the {@link DeskModel}.
	 * @param configurationItem
	 *            {@link ConfigurationItem}.
	 * @param roomEntry
	 *            {@link RoomEntry} containing this {@link DeskEntry}.
	 * @param context
	 *            {@link OfficeFloorCompilerContext}.
	 * @return Loaded {@link DeskEntry}.
	 * @throws Exception
	 *             If fails.
	 */
	public static DeskEntry loadDesk(String deskId, String deskName,
			ConfigurationItem configurationItem, RoomEntry roomEntry,
			OfficeFloorCompilerContext context) throws Exception {

		// Load the desk model
		DeskModel deskModel = new DeskLoader(context.getLoaderContext(),
				context.getModelRepository())
				.loadDeskAndSynchronise(configurationItem);

		// Create the desk entry
		DeskEntry deskEntry = new DeskEntry(deskId, deskName, deskModel,
				roomEntry);

		// Load the work
		for (DeskWorkModel deskWork : deskModel.getWorks()) {
			WorkEntry.loadWork(deskWork, deskEntry, context);
		}

		// Return the desk entry
		return deskEntry;
	}

	/**
	 * Name of the {@link DeskModel}.
	 */
	private final String deskName;

	/**
	 * Parent {@link RoomEntry}.
	 */
	private final RoomEntry parentRoom;

	/**
	 * Initiate.
	 * 
	 * @param deskId
	 *            Id of the {@link DeskModel}.
	 * @param Name
	 *            of the {@link DeskModel}.
	 * @param desk
	 *            {@link DeskModel}.
	 * @param parentRoom
	 *            Parent {@link RoomEntry}.
	 */
	public DeskEntry(String deskId, String deskName, DeskModel desk,
			RoomEntry parentRoom) {
		super(deskId, null, desk);
		this.deskName = deskName;
		this.parentRoom = parentRoom;
	}

	/**
	 * Obtains the name of the {@link DeskModel}.
	 * 
	 * @return Name of the {@link DeskModel}.
	 */
	public String getDeskName() {
		return this.deskName;
	}

	/**
	 * Obtains the parent {@link RoomEntry} for this {@link DeskEntry}.
	 * 
	 * @return Parent {@link RoomEntry} for this {@link DeskEntry}.
	 */
	public RoomEntry getParentRoom() {
		return this.parentRoom;
	}

}
