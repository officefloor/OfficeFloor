/*-
 * #%L
 * Activity
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
