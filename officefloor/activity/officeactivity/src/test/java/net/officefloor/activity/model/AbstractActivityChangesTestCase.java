/*-
 * #%L
 * Activity
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.activity.model;

import junit.framework.TestCase;
import net.officefloor.activity.ActivityTestTrait;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
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
	protected void storeModel(ActivityModel model, WritableConfigurationItem configurationItem) throws Exception {
		new ActivityRepositoryImpl(new ModelRepositoryImpl()).storeActivity(model, configurationItem);
	}

}
