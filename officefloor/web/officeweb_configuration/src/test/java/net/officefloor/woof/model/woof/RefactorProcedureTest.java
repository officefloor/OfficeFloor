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
package net.officefloor.woof.model.woof;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import net.officefloor.activity.procedure.ProcedureType;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.model.change.Change;

/**
 * Tests refactoring the {@link WoofProcedureModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorProcedureTest extends AbstractWoofChangesTestCase {

	/**
	 * {@link WoofProcedureModel}.
	 */
	private WoofProcedureModel procedure;

	/**
	 * {@link WoofProcedureOutputModel} name mapping.
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
		this.procedure = this.model.getWoofProcedures().get(0);
	}

	/**
	 * Ensure handle no change.
	 */
	public void testNoChange() {

		// Create the procedure type
		ProcedureType procedureType = this.constructProcedureType("procedure", String.class, (context) -> {
			context.addFlow("OUTPUT_A", Integer.class);
			context.addFlow("OUTPUT_B", String.class);
			context.addFlow("OUTPUT_C", null);
			context.addFlow("OUTPUT_D", null);
			context.addFlow("OUTPUT_E", null);
			context.addFlow("OUTPUT_F", null);
			context.addEscalation(IOException.class);
			context.addObject("IGNORE_OBJECT", DataSource.class, null);
			context.addVariable("IGNORE_VARIABLE", Boolean.class);
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
		this.procedureOutputNameMapping.put("OUTPUT_D", "OUTPUT_D");
		this.procedureOutputNameMapping.put("OUTPUT_E", "OUTPUT_E");
		this.procedureOutputNameMapping.put("OUTPUT_F", "OUTPUT_F");

		// Refactor the procedure with same details
		Change<WoofProcedureModel> change = this.operations.refactorProcedure(this.procedure, "PROCEDURE", "resource",
				"Class", "method", properties, procedureType, this.procedureOutputNameMapping);

		// Validate change
		this.assertChange(change, null, "Refactor Procedure", true);
	}

	/**
	 * Ensure handle change to all details.
	 */
	public void testChange() {

		// Create the procedure type
		ProcedureType procedureType = this.constructProcedureType("method_change", Character.class, (context) -> {
			context.addFlow("OUTPUT_A", Integer.class);
			context.addFlow("OUTPUT_B", String.class);
			context.addFlow("OUTPUT_C", null);
			context.addFlow("OUTPUT_D", null);
			context.addFlow("OUTPUT_E", null);
			context.addFlow("OUTPUT_F", null);
			context.addEscalation(IOException.class);
			context.addObject("IGNORE_OBJECT", DataSource.class, null);
			context.addVariable("IGNORE_VARIABLE", Boolean.class);
			context.setNextArgumentType(Short.class);
		});

		// Create the properties
		PropertyList properties = OfficeFloorCompiler.newPropertyList();
		properties.addProperty("name.1").setValue("value.one");
		properties.addProperty("name.two").setValue("value.2");

		// Change procedure output names around
		this.procedureOutputNameMapping.put("OUTPUT_B", "OUTPUT_A");
		this.procedureOutputNameMapping.put("OUTPUT_C", "OUTPUT_B");
		this.procedureOutputNameMapping.put("OUTPUT_D", "OUTPUT_C");
		this.procedureOutputNameMapping.put("OUTPUT_E", "OUTPUT_D");
		this.procedureOutputNameMapping.put("OUTPUT_F", "OUTPUT_E");
		this.procedureOutputNameMapping.put("OUTPUT_A", "OUTPUT_F");

		// Refactor the procedure with same details
		Change<WoofProcedureModel> change = this.operations.refactorProcedure(this.procedure, "CHANGE",
				"resource_change", "JavaScript", "function", properties, procedureType,
				this.procedureOutputNameMapping);

		// Validate change
		this.assertChange(change, null, "Refactor Procedure", true);
	}

	/**
	 * Ensure handle remove {@link PropertyModel} and
	 * {@link WoofProcedureOutputModel} instances.
	 */
	public void testRemoveDetails() {

		// Create the procedure type
		ProcedureType procedureType = this.constructProcedureType("procedure", null, null);

		// Refactor the procedure removing details
		Change<WoofProcedureModel> change = this.operations.refactorProcedure(this.procedure, "PROCEDURE", "resource",
				"Class", "method", null, procedureType, null);

		// Validate change
		this.assertChange(change, null, "Refactor Procedure", true);
	}

	/**
	 * Ensure handle adding {@link PropertyModel}, {@link WoofSectionInputModel} and
	 * {@link WoofSectionOutputModel} instances.
	 */
	public void testAddDetails() {

		// Create the procedure type
		ProcedureType procedureType = this.constructProcedureType("method", Character.class, (context) -> {
			context.addFlow("OUTPUT_A", Integer.class);
			context.addFlow("OUTPUT_B", String.class);
			context.addFlow("OUTPUT_C", null);
			context.addEscalation(IOException.class);
			context.addObject("IGNORE_OBJECT", DataSource.class, null);
			context.addVariable("IGNORE_VARIABLE", Boolean.class);
			context.setNextArgumentType(Long.class);
		});

		// Create the properties
		PropertyList properties = OfficeFloorCompiler.newPropertyList();
		properties.addProperty("name.one").setValue("value.one");
		properties.addProperty("name.two").setValue("value.two");

		// Refactor the procedure adding details
		Change<WoofProcedureModel> change = this.operations.refactorProcedure(this.procedure, "ADD", "resource",
				"Class", "method", properties, procedureType, this.procedureOutputNameMapping);

		// Validate change
		this.assertChange(change, null, "Refactor Procedure", true);
	}

}