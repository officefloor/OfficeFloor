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
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.BuilderFactory;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.repository.ConfigurationItem;
import net.officefloor.repository.ModelRepository;

/**
 * Compiles the {@link net.officefloor.model.officefloor.OfficeFloorModel} into
 * a {@link net.officefloor.frame.api.manage.OfficeFloor}.
 * 
 * @author Daniel
 */
public class OfficeFloorCompiler {

	/**
	 * {@link ModelRepository}.
	 */
	private final ModelRepository repository;

	/**
	 * Default constructor.
	 */
	public OfficeFloorCompiler() {
		this(new ModelRepository());
	}

	/**
	 * Initiate.
	 * 
	 * @param repository
	 *            {@link ModelRepository}.
	 */
	public OfficeFloorCompiler(ModelRepository repository) {
		this.repository = repository;
	}

	/**
	 * Compiles the {@link OfficeFloor} from the
	 * {@link net.officefloor.model.officefloor.OfficeFloorModel}.
	 * 
	 * @param configuration
	 *            Configuration containing the
	 *            {@link net.officefloor.model.officefloor.OfficeFloorModel}.
	 * @param builderFactory
	 *            {@link BuilderFactory} to create the builders to build the
	 *            {@link OfficeFloor}.
	 * @param builderContext
	 *            {@link LoaderContext} to use in building and compiling the
	 *            {@link net.officefloor.model.officefloor.OfficeFloorModel}.
	 * @return Compiled {@link OfficeFloor}.
	 */
	public OfficeFloor compileOfficeFloor(ConfigurationItem configuration,
			BuilderFactory builderFactory, LoaderContext builderContext)
			throws Exception {

		// Create the compiler context
		OfficeFloorCompilerContext context = new OfficeFloorCompilerContext(
				configuration.getContext(), this.repository, builderFactory,
				builderContext);

		// Load the office floor
		OfficeFloorEntry officeFloorEntry = OfficeFloorEntry.loadOfficeFloor(
				configuration, context);

		// TODO remove
		System.out.println("[" + this.getClass().getName() + " (todo remove):");
		System.out.println("Offices");
		for (String officeId : context.getOfficeRegistry().keySet()) {
			System.out.println("   " + officeId);
		}
		System.out.println("Work");
		for (String workId : context.getWorkRegistry().keySet()) {
			System.out.print("   " + workId + " [");
			for (String taskId : context.getWorkRegistry().get(workId)
					.getTaskRegistry().keySet()) {
				System.out.print(" " + taskId);
			}
			System.out.println(" ]");
		}
		System.out.println("Managed Objects");
		for (String moId : context.getManagedObjectSourceRegistry().keySet()) {
			System.out.println("   " + moId);
		}
		System.out.println("Teams");
		for (String teamId : context.getTeamRegistry().keySet()) {
			System.out.println("   " + teamId);
		}
		System.out.println(":" + this.getClass().getName() + "]");

		// Build the office floor
		for (WorkEntry<?> workEntry : context.getWorkRegistry().values()) {
			workEntry.build();
		}
		for (OfficeEntry officeEntry : context.getOfficeRegistry().values()) {
			officeEntry.build();
		}
		for (ManagedObjectSourceEntry mosEntry : context
				.getManagedObjectSourceRegistry().values()) {
			mosEntry.build(builderContext);
		}
		for (TeamEntry teamEntry : context.getTeamRegistry().values()) {
			teamEntry.build(builderContext);
		}

		// Return the created Office Floor
		return OfficeFrame.getInstance().registerOfficeFloor(
				configuration.getId(), officeFloorEntry.getBuilder());
	}

}
