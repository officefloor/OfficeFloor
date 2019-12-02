/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
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
package net.officefloor.activity;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.activity.model.ActivityInputModel;
import net.officefloor.activity.model.ActivityModel;
import net.officefloor.activity.model.ActivityProcedureModel;
import net.officefloor.activity.model.ActivityRepositoryImpl;
import net.officefloor.activity.procedure.ProcedureLoader;
import net.officefloor.activity.procedure.ProcedureType;
import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;

/**
 * {@link ActivityLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ActivityLoaderImpl implements ActivityLoader {

	@Override
	public void loadActivityConfiguration(ActivityContext context) throws Exception {

		// Load the model
		ActivityModel activityModel = new ActivityModel();
		new ActivityRepositoryImpl(new ModelRepositoryImpl()).retrieveActivity(activityModel,
				context.getConfiguration());

		// Obtain the various helpers
		SectionDesigner designer = context.getSectionDesigner();
		ProcedureArchitect<SubSection> procedureArchitect = context.getProcedureArchitect();
		ProcedureLoader procedureLoader = context.getProcedureLoader();

		// Load inputs
		Map<String, SectionInput> inputs = new HashMap<>();
		for (ActivityInputModel inputModel : activityModel.getActivityInputs()) {
			String inputName = inputModel.getActivityInputName();
			SectionInput input = designer.addSectionInput(inputName, inputModel.getArgumentType());
			inputs.put(inputName, input);
		}

		// Load the procedures and their types
		Map<String, SubSection> procedures = new HashMap<>();
		Map<String, ProcedureType> procedureTypes = new HashMap<>();
		for (ActivityProcedureModel procedureModel : activityModel.getActivityProcedures()) {
			String procedureName = procedureModel.getActivityProcedureName();
//			SubSection procedure = procedureArchitect.addProcedure(procedureName, resource, sourceName, procedureName,
//					isNext, properties);
//			ProcedureType procedureType = procedureLoader.loadProcedureType(resource, serviceName, procedureName,
//					properties);
		}
	}

}