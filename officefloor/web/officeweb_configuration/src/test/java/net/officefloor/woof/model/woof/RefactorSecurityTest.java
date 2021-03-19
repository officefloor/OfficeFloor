/*-
 * #%L
 * Web configuration
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

package net.officefloor.woof.model.woof;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.model.change.Change;
import net.officefloor.web.security.HttpCredentials;
import net.officefloor.web.security.type.HttpSecurityType;

/**
 * Tests refactoring the {@link WoofSecurityModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorSecurityTest extends AbstractWoofChangesTestCase {

	/**
	 * {@link WoofSecurityModel}.
	 */
	private WoofSecurityModel security;

	/**
	 * {@link WoofSecurityOutputModel} name mapping.
	 */
	private Map<String, String> securityOutputNameMapping = new HashMap<String, String>();;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.security = this.model.getWoofSecurities().get(0);
	}

	/**
	 * Initiate.
	 */
	public RefactorSecurityTest() {
		super(true);
	}

	/**
	 * Ensure handle no change.
	 */
	public void testNoChange() {

		// Create the security type
		HttpSecurityType<?, ?, ?, ?, ?> securityType = this.constructHttpSecurityType(HttpCredentials.class,
				(context) -> {
					context.addFlow("OUTPUT_A", Integer.class, 0, null);
					context.addFlow("OUTPUT_B", String.class, 1, null);
					context.addFlow("OUTPUT_C", null, 2, null);
					context.addFlow("OUTPUT_D", null, 3, null);
					context.addFlow("OUTPUT_E", null, 4, null);
					context.addFlow("OUTPUT_F", null, 5, null);
					context.addDependency("IGNORE_OBJECT", DataSource.class, null, 0, null);
				});

		// Create the properties
		PropertyList properties = OfficeFloorCompiler.newPropertyList();
		properties.addProperty("name.one").setValue("value.one");
		properties.addProperty("name.two").setValue("value.two");

		// Keep access output names
		this.securityOutputNameMapping.put("OUTPUT_A", "OUTPUT_A");
		this.securityOutputNameMapping.put("OUTPUT_B", "OUTPUT_B");
		this.securityOutputNameMapping.put("OUTPUT_C", "OUTPUT_C");
		this.securityOutputNameMapping.put("OUTPUT_D", "OUTPUT_D");
		this.securityOutputNameMapping.put("OUTPUT_E", "OUTPUT_E");
		this.securityOutputNameMapping.put("OUTPUT_F", "OUTPUT_F");

		// Refactor the access with same details
		Change<WoofSecurityModel> change = this.operations.refactorSecurity(this.security, "SECURITY",
				"net.example.HttpSecuritySource", 4000, properties,
				new String[] { "application/json", "application/xml" }, securityType, this.securityOutputNameMapping);

		// Validate change
		this.assertChange(change, null, "Refactor Security", true);
	}

	/**
	 * Ensure handle change to all details.
	 */
	public void testChange() {

		// Create the security type
		HttpSecurityType<?, ?, ?, ?, ?> securityType = this.constructHttpSecurityType(String.class, (context) -> {
			context.addFlow("OUTPUT_A", Integer.class, 0, null);
			context.addFlow("OUTPUT_B", String.class, 1, null);
			context.addFlow("OUTPUT_C", null, 2, null);
			context.addFlow("OUTPUT_D", null, 3, null);
			context.addFlow("OUTPUT_E", null, 4, null);
			context.addFlow("OUTPUT_F", null, 5, null);
			context.addDependency("IGNORE_OBJECT", DataSource.class, null, 0, null);
		});

		// Create the properties
		PropertyList properties = OfficeFloorCompiler.newPropertyList();
		properties.addProperty("name.1").setValue("value.one");
		properties.addProperty("name.two").setValue("value.2");

		// Keep section output names
		this.securityOutputNameMapping.put("OUTPUT_B", "OUTPUT_A");
		this.securityOutputNameMapping.put("OUTPUT_C", "OUTPUT_B");
		this.securityOutputNameMapping.put("OUTPUT_D", "OUTPUT_C");
		this.securityOutputNameMapping.put("OUTPUT_E", "OUTPUT_D");
		this.securityOutputNameMapping.put("OUTPUT_F", "OUTPUT_E");
		this.securityOutputNameMapping.put("OUTPUT_A", "OUTPUT_F");

		// Refactor the section with same details
		Change<WoofSecurityModel> change = this.operations.refactorSecurity(this.security, "CHANGE",
				"net.change.ChangeSecuritySource", 5000, properties,
				new String[] { "application/json", "text/html", "confirm/change" }, securityType,
				this.securityOutputNameMapping);

		// Validate change
		this.assertChange(change, null, "Refactor Security", true);
	}

	/**
	 * Ensure handle remove {@link PropertyModel}, {@link WoofSecurityModel} and
	 * {@link WoofAccessOutputModel} instances.
	 */
	public void testRemoveDetails() {

		// Create the security type
		HttpSecurityType<?, ?, ?, ?, ?> securityType = this.constructHttpSecurityType(HttpCredentials.class, null);

		// Refactor the access removing details
		Change<WoofSecurityModel> change = this.operations.refactorSecurity(this.security, "SECURITY",
				"net.example.RemoveSecuritySource", 10, null, null, securityType, null);

		// Validate change
		this.assertChange(change, null, "Refactor Security", true);
	}

	/**
	 * Ensure handle adding {@link PropertyModel}, {@link WoofSecurityModel} and
	 * {@link WoofAccessOutputModel} instances.
	 */
	public void testAddDetails() {

		// Create the security type
		HttpSecurityType<?, ?, ?, ?, ?> securityType = this.constructHttpSecurityType(String.class, (context) -> {
			context.addFlow("OUTPUT_A", Integer.class, 0, null);
			context.addFlow("OUTPUT_B", String.class, 1, null);
			context.addFlow("OUTPUT_C", null, 2, null);
			context.addDependency("IGNORE_OBJECT", DataSource.class, null, 0, null);
		});

		// Create the properties
		PropertyList properties = OfficeFloorCompiler.newPropertyList();
		properties.addProperty("name.one").setValue("value.one");
		properties.addProperty("name.two").setValue("value.two");

		// Refactor the access with added details
		Change<WoofSecurityModel> change = this.operations.refactorSecurity(this.security, "SECURITY",
				"net.example.AddSecuritySource", 5000, properties,
				new String[] { "application/json", "application/xml" }, securityType, this.securityOutputNameMapping);

		// Validate change
		this.assertChange(change, null, "Refactor Security", true);
	}

}
