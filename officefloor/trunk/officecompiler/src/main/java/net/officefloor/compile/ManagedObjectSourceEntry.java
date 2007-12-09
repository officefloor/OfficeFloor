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

import net.officefloor.LoaderContext;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.model.officefloor.ManagedObjectSourceModel;
import net.officefloor.model.officefloor.PropertyModel;
import net.officefloor.util.OFCU;

/**
 * {@link net.officefloor.frame.spi.managedobject.source.ManagedObjectSource}
 * entry for the {@link net.officefloor.frame.api.manage.OfficeFloor}.
 * 
 * @author Daniel
 */
public class ManagedObjectSourceEntry extends
		AbstractEntry<ManagedObjectBuilder<?>, ManagedObjectSourceModel> {

	/**
	 * Loads the {@link ManagedObjectSourceEntry}.
	 * 
	 * @param configuration
	 *            {@link ManagedObjectSourceModel}.
	 * @param officeFloorEntry
	 *            {@link OfficeFloorEntry} containing this
	 *            {@link ManagedObjectSourceEntry}.
	 * @param context
	 *            {@link OfficeFloorCompilerContext}.
	 * @return {@link ManagedObjectSourceEntry}.
	 */
	public static ManagedObjectSourceEntry loadManagedObjectSource(
			ManagedObjectSourceModel configuration,
			OfficeFloorEntry officeFloorEntry,
			OfficeFloorCompilerContext context) {

		// Create the builder
		ManagedObjectBuilder<?> builder = context.getBuilderFactory()
				.createManagedObjectBuilder();

		// Create the entry
		ManagedObjectSourceEntry mosEntry = new ManagedObjectSourceEntry(
				configuration.getId(), builder, configuration, officeFloorEntry);

		// Register the managed object source
		context.getManagedObjectSourceRegistry()
				.put(mosEntry.getId(), mosEntry);

		// Return the entry
		return mosEntry;
	}

	/**
	 * {@link OfficeFloorEntry} containing this {@link ManagedObjectSourceEntry}.
	 */
	private final OfficeFloorEntry officeFloorEntry;

	/**
	 * Initiate.
	 * 
	 * @param id
	 *            Id of the
	 *            {@link net.officefloor.frame.spi.managedobject.source.ManagedObjectSource}.
	 * @param builder
	 *            {@link ManagedObjectBuilder}.
	 * @param model
	 *            {@link ManagedObjectSourceModel}.
	 */
	public ManagedObjectSourceEntry(String id, ManagedObjectBuilder<?> builder,
			ManagedObjectSourceModel model, OfficeFloorEntry officeFloorEntry) {
		super(id, builder, model);
		this.officeFloorEntry = officeFloorEntry;
	}

	/**
	 * Obtains the {@link OfficeFloorEntry}.
	 * 
	 * @return {@link OfficeFloorEntry}.
	 */
	public OfficeFloorEntry getOfficeFloorEntry() {
		return this.officeFloorEntry;
	}

	/**
	 * Builds the
	 * {@link net.officefloor.frame.spi.managedobject.source.ManagedObjectSource}.
	 */
	@SuppressWarnings("unchecked")
	public void build(LoaderContext builderUtil) throws Exception {

		// Configure attributes
		this.getBuilder().setManagedObjectSourceClass(
				(Class<ManagedObjectSource>) builderUtil.obtainClass(this
						.getModel().getSource()));
		this.getBuilder().setManagingOffice(
				OFCU.get(this.getModel().getManagingOffice(),
						"No managing office for managed object source ${0}",
						this.getModel().getId()).getManagingOfficeName());

		// Configure properties
		for (PropertyModel property : this.getModel().getProperties()) {
			this.getBuilder().addProperty(property.getName(),
					property.getValue());
		}

		// Register managed object source with the office floor
		this.officeFloorEntry.getBuilder().addManagedObject(this.getId(),
				this.getBuilder());
	}
}
