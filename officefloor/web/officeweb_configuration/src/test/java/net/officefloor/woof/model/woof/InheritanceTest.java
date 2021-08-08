/*-
 * #%L
 * Web configuration
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

package net.officefloor.woof.model.woof;

import java.io.IOException;
import java.io.StringReader;
import java.util.Set;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.model.change.Change;
import net.officefloor.web.template.build.WebTemplate;
import net.officefloor.web.template.build.WebTemplateArchitectEmployer;
import net.officefloor.web.template.type.WebTemplateLoader;
import net.officefloor.web.template.type.WebTemplateLoaderUtil;
import net.officefloor.web.template.type.WebTemplateType;

/**
 * Tests inheritance of {@link WoofTemplateModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class InheritanceTest extends AbstractWoofChangesTestCase {

	/**
	 * Grand parent.
	 */
	private WoofTemplateModel grandParent;

	/**
	 * Parent.
	 */
	private WoofTemplateModel parent;

	/**
	 * Template.
	 */
	private WoofTemplateModel template;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Obtain the grand parent
		this.grandParent = this.model.getWoofTemplates().get(0);
		assertEquals("Incorrect parent template", "/grandparent", this.grandParent.getApplicationPath());

		// Obtain the parent
		this.parent = this.model.getWoofTemplates().get(1);
		assertEquals("Incorrect parent template", "/parent", this.parent.getApplicationPath());

		// Obtain the template
		this.template = this.model.getWoofTemplates().get(2);
		assertEquals("Incorrect orphan template", "/template", this.template.getApplicationPath());

	}

	/**
	 * Ensure appropriate listing of inheritable {@link WoofTemplateOutputModel}
	 * names.
	 */
	public void testInheritableOutputs() {
		assertOutputs(this.operations.getInheritableOutputNames(this.grandParent));
		assertOutputs(this.operations.getInheritableOutputNames(this.parent), "OUTPUT_GRAND_PARENT_A",
				"OUTPUT_GRAND_PARENT_B", "OUTPUT_GRAND_PARENT_C", "OUTPUT_GRAND_PARENT_D", "OUTPUT_GRAND_PARENT_E",
				"OUTPUT_GRAND_PARENT_F", "OUTPUT_PARENT_A", "OUTPUT_PARENT_B", "OUTPUT_PARENT_C", "OUTPUT_PARENT_D",
				"OUTPUT_PARENT_E", "OUTPUT_PARENT_F");
		assertOutputs(this.operations.getInheritableOutputNames(this.template));
	}

	/**
	 * Ensure appropriate type based on inheritance.
	 */
	public void testInheritableType() throws Exception {

		// Ensure can load type
		WebTemplateLoader loader = WebTemplateArchitectEmployer
				.employWebTemplateLoader(OfficeFloorCompiler.newOfficeFloorCompiler(null));

		// Load configuration
		WebTemplate template = loader.addTemplate(false, "/template", new StringReader("test"));

		// Load the super templates
		this.operations.loadSuperTemplates(template, this.parent, loader);

		// Create the expected template type
		SectionDesigner expected = WebTemplateLoaderUtil.createSectionDesigner();
		expected.addSectionOutput("grandParentLink", null, false);
		expected.addSectionOutput(IOException.class.getName(), IOException.class.getName(), true);

		// Load the type and ensure as expected
		WebTemplateType actual = loader.loadWebTemplateType(template);
		WebTemplateLoaderUtil.validateWebTemplateType(expected, actual);
	}

	/**
	 * Ensure appropriate type based on inheritance.
	 */
	public void testDeepInheritableType() throws Exception {

		// Connect template to parent
		this.operations.linkTemplateToSuperTemplate(this.template, this.parent).apply();

		// Ensure can load type
		WebTemplateLoader loader = WebTemplateArchitectEmployer
				.employWebTemplateLoader(OfficeFloorCompiler.newOfficeFloorCompiler(null));

		// Load configuration
		WebTemplate template = loader.addTemplate(false, "/template", new StringReader("<!-- {:child} -->test"));

		// Load the super templates
		this.operations.loadSuperTemplates(template, this.template, loader);

		// Create the expected template type
		SectionDesigner expected = WebTemplateLoaderUtil.createSectionDesigner();
		expected.addSectionOutput("grandParentLink", null, false);
		expected.addSectionOutput("parentLink", null, false);
		expected.addSectionOutput(IOException.class.getName(), IOException.class.getName(), true);

		// Load the type and ensure as expected
		WebTemplateType actual = loader.loadWebTemplateType(template);
		WebTemplateLoaderUtil.validateWebTemplateType(expected, actual);
	}

	/**
	 * Asserts the inheritable output names
	 * 
	 * @param actual
	 *            Actual names.
	 * @param expected
	 *            Expected names.
	 */
	private static void assertOutputs(Set<String> actual, String... expected) {
		assertEquals("Inocrrect number of outputs", expected.length, actual.size());
		for (String expectedName : expected) {
			assertTrue("Should contain output " + expectedName, actual.contains(expectedName));
		}
	}

	/**
	 * Ensure able to link {@link WoofTemplateModel} with super
	 * {@link WoofTemplateModel}.
	 */
	public void testLinkSuperTemplate() {

		// Link super template
		Change<WoofTemplateToSuperWoofTemplateModel> change = this.operations.linkTemplateToSuperTemplate(this.template,
				this.parent);

		// Validate change
		this.assertChange(change, null, "Link Template to Super Template", true);
	}

	/**
	 * Ensure able to link {@link WoofTemplateToSuperWoofTemplateModel}.
	 */
	public void testRemoveSuperTemplate() {

		// Link super template
		Change<WoofTemplateToSuperWoofTemplateModel> change = this.operations
				.removeTemplateToSuperTemplate(this.parent.getSuperWoofTemplate());

		// Validate change
		this.assertChange(change, null, "Remove Template to Super Template", true);
	}

	/**
	 * Ensure able to change super {@link WoofTemplateModel}.
	 */
	public void testChangeSuperTemplate() {

		// Link super template
		Change<WoofTemplateToSuperWoofTemplateModel> change = this.operations.linkTemplateToSuperTemplate(this.parent,
				this.template);

		// Validate change
		this.assertChange(change, null, "Link Template to Super Template", true);
	}

	/**
	 * Ensure able to change super {@link WoofTemplateModel} application path
	 * causing a name change and therefore child {@link WoofTemplateModel} to
	 * updates its reference to parent {@link WoofTemplateModel}.
	 */
	public void testChangeSuperTemplateApplicationPath() {

		// Test
		this.replayMockObjects();

		// Change template application path
		Change<WoofTemplateModel> change = this.operations.changeApplicationPath(this.grandParent, "/change",
				this.getWoofTemplateChangeContext());

		// Validate the change
		this.assertChange(change, this.grandParent, "Change Template Application Path", true);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to change super {@link WoofTemplateModel} application path
	 * causing a name change and therefore child {@link WoofTemplateModel} to
	 * updates its reference to parent {@link WoofTemplateModel}.
	 */
	public void testRefactorSuperTemplateApplicationPath() {

		// Create the type
		WebTemplateType type = this.constructWebTemplateType((context) -> {
		});

		// Test
		this.replayMockObjects();

		// Change template application path
		Change<WoofTemplateModel> change = this.operations.refactorTemplate(this.grandParent, "/change",
				"example/Change.ofp", null, type, null, null, null, null, false, null, null, null, null, null,
				this.getWoofTemplateChangeContext());

		// Validate the change
		this.assertChange(change, this.grandParent, "Refactor Template", true);

		// Verify
		this.verifyMockObjects();
	}

}
