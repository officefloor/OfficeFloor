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

import net.officefloor.frame.api.build.AdministratorBuilder;
import net.officefloor.frame.api.build.DutyBuilder;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.model.office.AdministratorModel;
import net.officefloor.model.office.DutyFlowModel;
import net.officefloor.model.office.DutyModel;
import net.officefloor.model.office.FlowItemModel;
import net.officefloor.model.office.PropertyModel;
import net.officefloor.util.OFCU;

/**
 * {@link AdministratorModel} entry for the {@link Office}.
 * 
 * @author Daniel
 */
public class AdministratorEntry<A extends Enum<A>> extends
		AbstractEntry<AdministratorBuilder<A>, AdministratorModel> {

	/**
	 * Loads the {@link AdministratorEntry}.
	 * 
	 * @param configuration
	 *            {@link AdministratorModel}.
	 * @param officeEntry
	 *            {@link OfficeEntry}.
	 * @param context
	 *            {@link OfficeFloorCompilerContext}.
	 * @return {@link AdministratorEntry}.
	 * @throws Exception
	 *             If fails.
	 */
	@SuppressWarnings("unchecked")
	public static AdministratorEntry loadAdministrator(
			AdministratorModel configuration, OfficeEntry officeEntry,
			OfficeFloorCompilerContext context) throws Exception {

		// Obtain the duty key class name
		String dutyKeyClassName = configuration.getDutyKeyClass();
		Class dutyKeyClass = context.getLoaderContext().obtainClass(
				dutyKeyClassName);

		// Obtain the administrator source class
		Class administratorSourceClass = context.getLoaderContext()
				.obtainClass(configuration.getSource());

		// Create the builder
		AdministratorBuilder<?> builder = context.getBuilderFactory()
				.createAdministratorBuilder(administratorSourceClass);

		// Create the administrator entry
		AdministratorEntry entry = new AdministratorEntry(
				configuration.getId(), builder, configuration, officeEntry,
				dutyKeyClass);

		// Return entry
		return entry;
	}

	/**
	 * {@link OfficeEntry} containing this {@link AdministratorEntry}.
	 */
	private final OfficeEntry officeEntry;

	/**
	 * {@link Enum} specifying the keys of the {@link Duty} instances.
	 */
	private final Class<A> dutyKeyClass;

	/**
	 * Initiate.
	 * 
	 * @param id
	 *            {@link AdministratorModel} Id.
	 * @param builder
	 *            {@link AdministratorBuilder}.
	 * @param model
	 *            {@link AdministratorModel}.
	 * @param officeEntry
	 *            {@link OfficeEntry}.
	 */
	private AdministratorEntry(String id, AdministratorBuilder<A> builder,
			AdministratorModel model, OfficeEntry officeEntry,
			Class<A> dutyKeyClass) {
		super(id, builder, model);
		this.officeEntry = officeEntry;
		this.dutyKeyClass = dutyKeyClass;
	}

	/**
	 * Obtains the {@link Class} specifying the {@link Duty} key instances.
	 * 
	 * @return {@link Duty} key instances.
	 */
	public Class<A> getDutyKeys() {
		return this.dutyKeyClass;
	}

	/**
	 * Builds the {@link Administrator}.
	 * 
	 * @param context
	 *            {@link LoaderContext}.
	 * @throws Exception
	 *             If fails to build.
	 */
	@SuppressWarnings("unchecked")
	public void build(LoaderContext context) throws Exception {

		// Load administrator details
		for (PropertyModel property : this.getModel().getProperties()) {
			this.getBuilder().addProperty(property.getName(),
					property.getValue());
		}

		// Link in the team
		String teamName = OFCU.get(
				this.getModel().getTeam(),
				"Must link in team for administrator " + this.getId()
						+ " of office " + this.officeEntry.getId()).getTeam()
				.getName();
		this.getBuilder().setTeam(teamName);

		// Load the duties
		for (A dutyKey : this.dutyKeyClass.getEnumConstants()) {

			// Find the corresponding duty
			DutyModel duty = null;
			for (DutyModel dutyModel : this.getModel().getDuties()) {
				if (dutyKey.name().equals(dutyModel.getKey())) {
					duty = dutyModel;
				}
			}

			// Obtain duty builder based on whether have flow keys
			String flowKeysClassName = duty.getFlowKeys();
			DutyBuilder dutyBuilder;
			if (flowKeysClassName != null) {
				// Create the duty builder
				Class flowKeysClass = context.obtainClass(flowKeysClassName);
				dutyBuilder = this.getBuilder().registerDutyBuilder(dutyKey,
						flowKeysClass);

				// Link in the flows
				for (DutyFlowModel dutyFlow : duty.getFlows()) {
					Enum flowKey = (Enum) OFCU.getEnum(flowKeysClass, duty
							.getKey());
					FlowItemModel flowItem = dutyFlow.getFlowItem()
							.getFlowItem();
					dutyBuilder.linkFlow(flowKey, flowItem.getWorkName(),
							flowItem.getTaskName());
				}

			} else {
				// Create the duty builder
				dutyBuilder = this.getBuilder().registerDutyBuilder(dutyKey);

				// Link in the flows
				for (DutyFlowModel dutyFlow : duty.getFlows()) {
					int index = Integer.parseInt(dutyFlow.getKey());
					FlowItemModel flowItem = dutyFlow.getFlowItem()
							.getFlowItem();
					dutyBuilder.linkFlow(index, flowItem.getWorkName(),
							flowItem.getTaskName());
				}
			}
		}

		// Register the administrator
		this.officeEntry.getBuilder().addAdministrator(this.getModel().getId(),
				this.getBuilder());
	}
}
