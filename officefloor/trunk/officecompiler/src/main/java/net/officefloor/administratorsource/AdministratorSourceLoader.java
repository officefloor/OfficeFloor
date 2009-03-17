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
package net.officefloor.administratorsource;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.impl.construct.administrator.AdministratorSourceContextImpl;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.source.AdministratorDutyMetaData;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.administration.source.AdministratorSourceMetaData;
import net.officefloor.model.office.AdministratorModel;
import net.officefloor.model.office.DutyFlowModel;
import net.officefloor.model.office.DutyModel;
import net.officefloor.model.office.PropertyModel;

/**
 * Loads the {@link AdministratorModel}.
 * 
 * @author Daniel
 */
public class AdministratorSourceLoader {

	/**
	 * Loads the {@link AdministratorModel}.
	 * 
	 * @param administratorId
	 *            Id of the {@link Administrator}.
	 * @param administratorSource
	 *            {@link AdministratorSource}.
	 * @param properties
	 *            {@link Properties}.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @return {@link AdministratorModel}.
	 * @throws Throwable
	 *             If fails.
	 */
	public <I, A extends Enum<A>> AdministratorModel loadAdministratorSource(
			String administratorId,
			AdministratorSource<I, A> administratorSource,
			Properties properties, ClassLoader classLoader) throws Throwable {

		// Initialise the administrator source
		administratorSource
				.init(new AdministratorSourceContextImpl(properties));

		// Create the listing of properties
		List<PropertyModel> propertyModels = new LinkedList<PropertyModel>();
		for (String name : properties.stringPropertyNames()) {
			String value = properties.getProperty(name);
			propertyModels.add(new PropertyModel(name, value));
		}

		// Obtain the meta-data
		AdministratorSourceMetaData<I, A> metaData = administratorSource
				.getMetaData();

		// Obtain the duty keys
		Class<A> dutyKeysClass = metaData.getAministratorDutyKeys();

		// Create the listing of duty models
		A[] dutyKeys = dutyKeysClass.getEnumConstants();
		DutyModel[] dutyModels = new DutyModel[dutyKeys.length];
		for (int i = 0; i < dutyModels.length; i++) {

			// Obtain the meta-data for the duty
			A dutyKey = dutyKeys[i];
			AdministratorDutyMetaData<?> dutyMetaData = metaData
					.getAdministratorDutyMetaData(dutyKey);

			// Create the listing of duty flows
			Class<?> flowKeys = dutyMetaData.getFlowKeys();
			String flowKeysClassName = None.class.getName();
			DutyFlowModel[] dutyFlows = null;
			if (flowKeys != null) {
				flowKeysClassName = flowKeys.getName();
				Object[] flowKeyObjects = flowKeys.getEnumConstants();
				dutyFlows = new DutyFlowModel[flowKeyObjects.length];
				for (int j = 0; j < flowKeyObjects.length; j++) {
					Enum<?> flowKey = (Enum<?>) flowKeyObjects[j];
					dutyFlows[j] = new DutyFlowModel(flowKey.name(), null);
				}
			}

			// Create the duty model
			dutyModels[i] = new DutyModel(dutyKey.name(), flowKeysClassName,
					dutyFlows, null, null);
		}

		// Create the Administrator
		AdministratorModel administratorModel = new AdministratorModel(
				administratorId, administratorSource.getClass().getName(),
				dutyKeysClass.getName(), null, propertyModels
						.toArray(new PropertyModel[0]), null, dutyModels);

		// Return the Administrator
		return administratorModel;
	}

}