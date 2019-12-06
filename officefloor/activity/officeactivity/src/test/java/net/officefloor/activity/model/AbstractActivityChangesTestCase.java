/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.activity.model;

import junit.framework.TestCase;
import net.officefloor.activity.ActivityTestTrait;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.configuration.impl.configuration.MemoryConfigurationContext;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.test.changes.AbstractChangesTestCase;

/**
 * Abstract {@link ActivityChanges} {@link TestCase}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractActivityChangesTestCase extends AbstractChangesTestCase<ActivityModel, ActivityChanges>
		implements ActivityTestTrait {

	/**
	 * Initiate.
	 */
	public AbstractActivityChangesTestCase() {
	}

	/**
	 * Initiate.
	 * 
	 * @param isSpecificSetupFilePerTest Flags if there is a specific setup file per
	 *                                   test.
	 */
	public AbstractActivityChangesTestCase(boolean isSpecificSetupFilePerTest) {
		super(isSpecificSetupFilePerTest);
	}

	/*
	 * =================== AbstractOperationsTestCase ========================
	 */

	@Override
	protected String getModelFileExtension() {
		return ".activity.xml";
	}

	@Override
	protected ActivityModel retrieveModel(ConfigurationItem configurationItem) throws Exception {
		ActivityModel activity = new ActivityModel();
		new ActivityRepositoryImpl(new ModelRepositoryImpl()).retrieveActivity(activity, configurationItem);
		return activity;
	}

	@Override
	protected ActivityChanges createModelOperations(ActivityModel model) {
		return new ActivityChangesImpl(model);
	}

	@Override
	protected void assertModels(ActivityModel expected, ActivityModel actual) throws Exception {

		// Determine if output XML of actual
		if (this.isPrintMessages()) {

			// Provide details of the model compare
			this.printMessage("=============== MODEL COMPARE ================");

			// Provide details of expected model
			this.printMessage("------------------ EXPECTED ------------------");
			WritableConfigurationItem expectedConfig = MemoryConfigurationContext
					.createWritableConfigurationItem("location");
			new ActivityRepositoryImpl(new ModelRepositoryImpl()).storeActivity(expected, expectedConfig);
			this.printMessage(expectedConfig.getReader());

			// Provide details of actual model
			this.printMessage("------------------- ACTUAL -------------------");
			WritableConfigurationItem actualConfig = MemoryConfigurationContext
					.createWritableConfigurationItem("location");
			new ActivityRepositoryImpl(new ModelRepositoryImpl()).storeActivity(actual, actualConfig);
			this.printMessage(actualConfig.getReader());
			this.printMessage("================ END COMPARE =================");
		}

		// Under take the compare
		super.assertModels(expected, actual);
	}

}