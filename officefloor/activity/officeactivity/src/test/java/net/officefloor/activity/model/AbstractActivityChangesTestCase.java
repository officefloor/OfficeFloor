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