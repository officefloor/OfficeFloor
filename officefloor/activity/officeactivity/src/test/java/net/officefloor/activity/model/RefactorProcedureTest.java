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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import net.officefloor.activity.procedure.ProcedureType;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.model.change.Change;

/**
 * Tests refactoring the {@link ActivityProcedureModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorProcedureTest extends AbstractActivityChangesTestCase {

	/**
	 * {@link ActivityProcedureModel}.
	 */
	private ActivityProcedureModel procedure;

	/**
	 * {@link ActivityProcedureOutputModel} name mapping.
	 */
	private Map<String, String> procedureOutputNameMapping = new HashMap<String, String>();;

	/**
	 * Initiate.
	 */
	public RefactorProcedureTest() {
		super(true);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.procedure = this.model.getActivityProcedures().get(0);
	}

	/**
	 * Ensure handle no change.
	 */
	public void testNoChange() {
		
		// Create the procedure type
		ProcedureType procedureType = this.constructProcedureType("procedure", String.class, (context) -> {
			context.addFlowType("OUTPUT_A", Integer.class);
			context.addFlowType("OUTPUT_B", null);
			context.addFlowType("OUTPUT_C", null);
			context.addEscalationType(IOException.class);
			context.addObjectType("IGNORE_OBJECT", DataSource.class, null);
			context.addVariableType("IGNORE_VARIABLE", Boolean.class);
			context.setNextArgumentType(Byte.class);
		});

		// Create the properties
		PropertyList properties = OfficeFloorCompiler.newPropertyList();
		properties.addProperty("name.one").setValue("value.one");
		properties.addProperty("name.two").setValue("value.two");

		// Keep procedure output names
		this.procedureOutputNameMapping.put("OUTPUT_A", "OUTPUT_A");
		this.procedureOutputNameMapping.put("OUTPUT_B", "OUTPUT_B");
		this.procedureOutputNameMapping.put("OUTPUT_C", "OUTPUT_C");

		// Refactor the procedure with same details
		Change<ActivityProcedureModel> change = this.operations.refactorProcedure(this.procedure, "PROCEDURE",
				"resource", "Class", "method", properties, procedureType, this.procedureOutputNameMapping);

		// Validate change
		this.assertChange(change, null, "Refactor Procedure", true);
	}

	/**
	 * Ensure handle change to all details.
	 */
	public void testChange() {

		// Create the procedure type
		ProcedureType procedureType = this.constructProcedureType("method_change", Character.class, (context) -> {
			context.addFlowType("OUTPUT_A", Integer.class);
			context.addFlowType("OUTPUT_B", String.class);
			context.addFlowType("OUTPUT_C", null);
			context.addEscalationType(IOException.class);
			context.addObjectType("IGNORE_OBJECT", DataSource.class, null);
			context.addVariableType("IGNORE_VARIABLE", Boolean.class);
			context.setNextArgumentType(Short.class);
		});

		// Create the properties
		PropertyList properties = OfficeFloorCompiler.newPropertyList();
		properties.addProperty("name.1").setValue("value.one");
		properties.addProperty("name.two").setValue("value.2");

		// Change procedure output names around
		this.procedureOutputNameMapping.put("OUTPUT_B", "OUTPUT_A");
		this.procedureOutputNameMapping.put("OUTPUT_C", "OUTPUT_B");
		this.procedureOutputNameMapping.put("OUTPUT_A", "OUTPUT_C");

		// Refactor the procedure with same details
		Change<ActivityProcedureModel> change = this.operations.refactorProcedure(this.procedure, "CHANGE",
				"resource_change", "JavaScript", "function", properties, procedureType,
				this.procedureOutputNameMapping);

		// Validate change
		this.assertChange(change, null, "Refactor Procedure", true);
	}

	/**
	 * Ensure handle remove {@link PropertyModel} and
	 * {@link ActivityProcedureOutputModel} instances.
	 */
	public void testRemoveDetails() {

		// Create the procedure type
		ProcedureType procedureType = this.constructProcedureType("procedure", null, null);

		// Refactor the procedure removing details
		Change<ActivityProcedureModel> change = this.operations.refactorProcedure(this.procedure, "PROCEDURE",
				"resource", "Class", "method", null, procedureType, null);

		// Validate change
		this.assertChange(change, null, "Refactor Procedure", true);
	}

	/**
	 * Ensure handle adding {@link PropertyModel}, {@link ActivitySectionInputModel}
	 * and {@link ActivitySectionOutputModel} instances.
	 */
	public void testAddDetails() {

		// Create the procedure type
		ProcedureType procedureType = this.constructProcedureType("method", Character.class, (context) -> {
			context.addFlowType("OUTPUT_A", Integer.class);
			context.addFlowType("OUTPUT_B", String.class);
			context.addFlowType("OUTPUT_C", null);
			context.addEscalationType(IOException.class);
			context.addObjectType("IGNORE_OBJECT", DataSource.class, null);
			context.addVariableType("IGNORE_VARIABLE", Boolean.class);
			context.setNextArgumentType(Long.class);
		});

		// Create the properties
		PropertyList properties = OfficeFloorCompiler.newPropertyList();
		properties.addProperty("name.one").setValue("value.one");
		properties.addProperty("name.two").setValue("value.two");

		// Refactor the procedure adding details
		Change<ActivityProcedureModel> change = this.operations.refactorProcedure(this.procedure, "ADD", "resource",
				"Class", "method", properties, procedureType, this.procedureOutputNameMapping);

		// Validate change
		this.assertChange(change, null, "Refactor Procedure", true);
	}

}
