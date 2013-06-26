/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.model.woof;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.model.change.Change;
import net.officefloor.plugin.web.http.security.HttpCredentials;
import net.officefloor.plugin.web.http.security.HttpSecuritySectionSource;
import net.officefloor.plugin.web.http.security.type.HttpSecurityType;

/**
 * Tests refactoring the {@link WoofAccessModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorAccessTest extends AbstractWoofChangesTestCase {

	/**
	 * {@link WoofAccessModel}.
	 */
	private WoofAccessModel access;

	/**
	 * {@link WoofAccessOutputModel} name mapping.
	 */
	private Map<String, String> accessOutputNameMapping = new HashMap<String, String>();;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.access = this.model.getWoofAccess();
	}

	/**
	 * Initiate.
	 */
	public RefactorAccessTest() {
		super(true);
	}

	/**
	 * Ensure handle no change.
	 */
	public void testNoChange() {

		// Create the security type
		HttpSecurityType<?, ?, ?, ?> securityType = this
				.constructHttpSecurityType(HttpCredentials.class,
						new HttpSecurityTypeConstructor() {
							@Override
							public void construct(
									HttpSecurityTypeContext context) {
								context.addFlow("OUTPUT_A", Integer.class, null);
								context.addFlow("OUTPUT_B", String.class, null);
								context.addFlow("OUTPUT_C", null, null);
								context.addDependency("IGNORE_OBJECT",
										DataSource.class, null, null);
							}
						});

		// Create the properties
		PropertyList properties = OfficeFloorCompiler.newPropertyList();
		properties.addProperty("name.one").setValue("value.one");
		properties.addProperty("name.two").setValue("value.two");

		// Keep access output names
		this.accessOutputNameMapping.put("OUTPUT_A", "OUTPUT_A");
		this.accessOutputNameMapping.put("OUTPUT_B", "OUTPUT_B");
		this.accessOutputNameMapping.put("OUTPUT_C", "OUTPUT_C");
		this.accessOutputNameMapping.put(
				HttpSecuritySectionSource.OUTPUT_FAILURE,
				HttpSecuritySectionSource.OUTPUT_FAILURE);

		// Refactor the access with same details
		Change<WoofAccessModel> change = this.operations.refactorAccess(
				this.access, "net.example.HttpSecuritySource", 4000,
				properties, securityType, this.accessOutputNameMapping);

		// Validate change
		this.assertChange(change, null, "Refactor Access", true);
	}

	/**
	 * Ensure handle change to all details.
	 */
	public void testChange() {

		// Create the security type
		HttpSecurityType<?, ?, ?, ?> securityType = this
				.constructHttpSecurityType(String.class,
						new HttpSecurityTypeConstructor() {
							@Override
							public void construct(
									HttpSecurityTypeContext context) {
								context.addFlow("OUTPUT_A", Integer.class, null);
								context.addFlow("OUTPUT_B", String.class, null);
								context.addFlow("OUTPUT_C", null, null);
								context.addDependency("IGNORE_OBJECT",
										DataSource.class, null, null);
							}
						});

		// Create the properties
		PropertyList properties = OfficeFloorCompiler.newPropertyList();
		properties.addProperty("name.1").setValue("value.one");
		properties.addProperty("name.two").setValue("value.2");

		// Keep section output names
		this.accessOutputNameMapping.put("OUTPUT_B", "OUTPUT_A");
		this.accessOutputNameMapping.put("OUTPUT_C", "OUTPUT_B");
		this.accessOutputNameMapping.put("OUTPUT_A", "OUTPUT_C");

		// Refactor the section with same details
		Change<WoofAccessModel> change = this.operations.refactorAccess(
				this.access, "net.change.ChangeSecuritySource", 5000,
				properties, securityType, this.accessOutputNameMapping);

		// Validate change
		this.assertChange(change, null, "Refactor Access", true);
	}

	/**
	 * Ensure handle remove {@link PropertyModel}, {@link WoofAccessInputModel}
	 * and {@link WoofAccessOutputModel} instances.
	 */
	public void testRemoveDetails() {

		// Create the security type
		HttpSecurityType<?, ?, ?, ?> securityType = this
				.constructHttpSecurityType(HttpCredentials.class,
						new HttpSecurityTypeConstructor() {
							@Override
							public void construct(
									HttpSecurityTypeContext context) {
								// No flows
							}
						});

		// Refactor the access removing details
		Change<WoofAccessModel> change = this.operations.refactorAccess(
				this.access, "net.example.RemoveSecuritySource", 4000, null,
				securityType, null);

		// Validate change
		this.assertChange(change, null, "Refactor Access", true);
	}

	/**
	 * Ensure handle adding {@link PropertyModel}, {@link WoofAccessInputModel}
	 * and {@link WoofAccessOutputModel} instances.
	 */
	public void testAddDetails() {

		// Create the security type
		HttpSecurityType<?, ?, ?, ?> securityType = this
				.constructHttpSecurityType(String.class,
						new HttpSecurityTypeConstructor() {
							@Override
							public void construct(
									HttpSecurityTypeContext context) {
								context.addFlow("OUTPUT_A", Integer.class, null);
								context.addFlow("OUTPUT_B", String.class, null);
								context.addFlow("OUTPUT_C", null, null);
								context.addDependency("IGNORE_OBJECT",
										DataSource.class, null, null);
							}
						});

		// Create the properties
		PropertyList properties = OfficeFloorCompiler.newPropertyList();
		properties.addProperty("name.one").setValue("value.one");
		properties.addProperty("name.two").setValue("value.two");

		// Refactor the access with added details
		Change<WoofAccessModel> change = this.operations.refactorAccess(
				this.access, "net.example.AddSecuritySource", 5000, properties,
				securityType, this.accessOutputNameMapping);

		// Validate change
		this.assertChange(change, null, "Refactor Access", true);
	}

}