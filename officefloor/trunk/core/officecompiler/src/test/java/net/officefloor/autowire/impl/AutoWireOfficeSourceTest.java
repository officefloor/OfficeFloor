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
package net.officefloor.autowire.impl;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Array;
import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;
import javax.transaction.xa.XAResource;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireGovernance;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.autowire.AutoWireSectionFactory;
import net.officefloor.autowire.AutoWireSectionTransformer;
import net.officefloor.autowire.AutoWireSectionTransformerContext;
import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.OfficeSectionInputType;
import net.officefloor.compile.section.OfficeSectionManagedObjectType;
import net.officefloor.compile.section.OfficeSectionObjectType;
import net.officefloor.compile.section.OfficeSectionOutputType;
import net.officefloor.compile.section.OfficeSectionType;
import net.officefloor.compile.section.OfficeSubSectionType;
import net.officefloor.compile.section.OfficeTaskType;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeEscalation;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.office.OfficeOutput;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionManagedObject;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.OfficeStart;
import net.officefloor.compile.spi.office.OfficeSubSection;
import net.officefloor.compile.spi.office.OfficeSectionTask;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.office.TaskTeam;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.TaskObject;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.test.section.SectionLoaderUtil;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.escalate.FailedToSourceManagedObjectEscalation;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.TeamSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.managedobject.clazz.Dependency;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.ManagedObject;
import net.officefloor.plugin.section.clazz.Property;
import net.officefloor.plugin.section.clazz.TypeQualifier;
import net.officefloor.plugin.section.work.WorkSectionSource;
import net.officefloor.plugin.work.clazz.Qualifier;

import org.easymock.AbstractMatcher;

/**
 * Tests the {@link AutoWireOfficeSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireOfficeSourceTest extends OfficeFrameTestCase {

	/**
	 * Mock {@link OfficeArchitect}.
	 */
	private final OfficeArchitect architect = this
			.createMock(OfficeArchitect.class);

	/**
	 * Mock {@link OfficeSourceContext}.
	 */
	private final OfficeSourceContext context = this
			.createMock(OfficeSourceContext.class);

	/**
	 * Ensure single section.
	 */
	public void testSingleSection() throws Exception {

		final String SECTION = "Section";

		// Create and configure the source
		AutoWireOfficeSource source = new AutoWireOfficeSource();
		this.addSection(source, SECTION, "name", "value");

		// Record creating the section
		this.recordOfficeSection(SECTION, "name", "value");
		this.recordSectionObjects(SECTION);
		this.recordSectionInputs(SECTION);
		this.recordSectionTasks(SECTION);
		this.recordSubSections(SECTION);
		this.recordSectionOutputs(SECTION);

		// Test
		this.replayMockObjects();

		// Source the Office
		source.sourceOffice(this.architect, context);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can override the {@link AutoWireSection}.
	 */
	public void testOverrideSection() throws Exception {

		final String SECTION = "Section";
		final PropertyList properties = this.createMock(PropertyList.class);
		final String SECTION_LOCATION = "SectionLocation";
		final OfficeSectionType officeSectionType = this
				.createMock(OfficeSectionType.class);
		final OfficeSection officeSection = this
				.createMock(OfficeSection.class);

		// Factory to override section
		final AutoWireSection overridden = this
				.createMock(AutoWireSection.class);
		final AutoWireSection[] seedSection = new AutoWireSection[1];
		final AutoWireSectionFactory<AutoWireSection> factory = new AutoWireSectionFactory<AutoWireSection>() {
			@Override
			public AutoWireSection createAutoWireSection(AutoWireSection seed) {
				seedSection[0] = seed;
				return overridden;
			}
		};

		// Record creating the overridden section
		this.recordReturn(overridden, overridden.getProperties(), properties);
		this.recordReturn(overridden, overridden.getSectionName(), SECTION);
		this.recordReturn(overridden, overridden.getSectionSourceClassName(),
				SectionSource.class.getName());
		this.recordReturn(overridden, overridden.getSectionLocation(),
				SECTION_LOCATION);
		this.recordReturn(
				this.architect,
				this.architect.addOfficeSection(SECTION,
						SectionSource.class.getName(), SECTION_LOCATION),
				officeSection);
		this.recordReturn(overridden, overridden.getProperties(), properties);
		properties.configureProperties(officeSection);
		this.recordReturn(this.context, this.context.loadOfficeSectionType(
				SECTION, SectionSource.class.getName(), SECTION_LOCATION,
				properties), officeSectionType);
		this.recordReturn(officeSectionType,
				officeSectionType.getOfficeSectionObjectTypes(),
				new OfficeSectionObjectType[0]);
		this.recordReturn(officeSectionType,
				officeSectionType.getOfficeSectionInputTypes(),
				new OfficeSectionInputType[0]);
		this.recordReturn(officeSectionType,
				officeSectionType.getOfficeTaskTypes(), new OfficeTaskType[0]);
		this.recordReturn(officeSectionType,
				officeSectionType.getOfficeSubSectionTypes(),
				new OfficeSubSectionType[0]);
		this.recordReturn(overridden, overridden.getSectionName(), SECTION);
		this.recordReturn(officeSectionType,
				officeSectionType.getOfficeSectionOutputTypes(),
				new OfficeSectionOutputType[0]);

		// Test
		this.replayMockObjects();

		// Create and configure the source
		AutoWireOfficeSource source = new AutoWireOfficeSource();
		this.addSection(source, SECTION, factory);

		// Source the Office
		source.sourceOffice(this.architect, context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can transform the {@link AutoWireSection}.
	 */
	public void testTransformSection() throws Exception {

		final String SECTION = "Section";
		final PropertyList properties = this.createMock(PropertyList.class);
		final String SECTION_LOCATION = "SectionLocation";
		final OfficeSectionType officeSectionType = this
				.createMock(OfficeSectionType.class);
		final OfficeSection officeSection = this
				.createMock(OfficeSection.class);

		// Transformers
		final AutoWireSectionTransformer transformerOne = this
				.createMock(AutoWireSectionTransformer.class);
		final AutoWireSectionTransformer transformerTwo = this
				.createMock(AutoWireSectionTransformer.class);

		// Transformed sections
		final AutoWireSection firstTransformed = this
				.createMock(AutoWireSection.class);
		final AutoWireSection secondTransformed = this
				.createMock(AutoWireSection.class);

		// Create and configure the source
		AutoWireOfficeSource source = new AutoWireOfficeSource();
		final AutoWireSection original = this.addSection(source, SECTION);

		// Record transforming the section
		AbstractMatcher matcher = new AbstractMatcher() {

			private boolean isOriginal = true;

			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				AutoWireSectionTransformerContext context = (AutoWireSectionTransformerContext) actual[0];
				if (this.isOriginal) {
					assertSame("Should be original", original,
							context.getSection());
					this.isOriginal = false;
				} else {
					assertSame("Should be first transformed", firstTransformed,
							context.getSection());
				}
				return true;
			}
		};
		this.recordReturn(transformerOne,
				transformerOne.transformAutoWireSection(null),
				firstTransformed, matcher);
		this.recordReturn(transformerTwo,
				transformerTwo.transformAutoWireSection(null),
				secondTransformed, matcher);

		// Record creating the transformed section
		this.recordReturn(secondTransformed,
				secondTransformed.getSectionName(), SECTION);
		this.recordReturn(secondTransformed,
				secondTransformed.getSectionSourceClassName(),
				SectionSource.class.getName());
		this.recordReturn(secondTransformed,
				secondTransformed.getSectionLocation(), SECTION_LOCATION);
		this.recordReturn(secondTransformed, secondTransformed.getProperties(),
				properties);
		this.recordReturn(
				this.architect,
				this.architect.addOfficeSection(SECTION,
						SectionSource.class.getName(), SECTION_LOCATION),
				officeSection);
		properties.configureProperties(officeSection);
		this.recordReturn(this.context, this.context.loadOfficeSectionType(
				SECTION, SectionSource.class.getName(), SECTION_LOCATION,
				properties), officeSectionType);
		this.recordReturn(officeSectionType,
				officeSectionType.getOfficeSectionObjectTypes(),
				new OfficeSectionObjectType[0]);
		this.recordReturn(officeSectionType,
				officeSectionType.getOfficeSectionInputTypes(),
				new OfficeSectionInputType[0]);
		this.recordReturn(officeSectionType,
				officeSectionType.getOfficeTaskTypes(), new OfficeTaskType[0]);
		this.recordReturn(officeSectionType,
				officeSectionType.getOfficeSubSectionTypes(),
				new OfficeSubSectionType[0]);
		this.recordReturn(officeSectionType,
				officeSectionType.getOfficeSectionOutputTypes(),
				new OfficeSectionOutputType[0]);

		// Test
		this.replayMockObjects();

		// Add the section transformers
		source.addSectionTransformer(transformerOne);
		source.addSectionTransformer(transformerTwo);

		// Source the Office
		source.sourceOffice(this.architect, context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure sub sections.
	 */
	public void testSubSections() throws Exception {

		final String SECTION = "Section";

		// Create and configure the source
		AutoWireOfficeSource source = new AutoWireOfficeSource();
		this.addSection(source, SECTION, "name", "value");

		// Record creating the section
		this.recordOfficeSection(SECTION, "name", "value");
		this.recordSectionObjects(SECTION);
		this.recordSectionInputs(SECTION);
		this.recordSectionTasks(SECTION);
		this.recordSubSections(SECTION, "SubSection");
		this.recordSubSections("SubSection", "SubSubSectionOne",
				"SubSubSectionTwo");
		this.recordSubSections("SubSubSectionOne");
		this.recordSubSections("SubSubSectionTwo");
		this.recordSectionOutputs(SECTION);

		// Test
		this.replayMockObjects();
		source.sourceOffice(this.architect, context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure link flow between sections.
	 */
	public void testLinkFlow() throws Exception {

		final String ONE = "One";
		final String ONE_OUTPUT = "output";
		final String TWO = "Two";
		final String TWO_INPUT = "input";

		// Create and configure the source
		AutoWireOfficeSource source = new AutoWireOfficeSource();
		AutoWireSection one = this.addSection(source, ONE);
		AutoWireSection two = this.addSection(source, TWO);
		source.link(one, ONE_OUTPUT, two, TWO_INPUT);

		// Record creating the section
		this.recordOfficeSection(ONE);
		this.recordSectionObjects(ONE);
		this.recordSectionInputs(ONE);
		this.recordSectionTasks(ONE);
		this.recordSubSections(ONE);
		this.recordOfficeSection(TWO);
		this.recordSectionObjects(TWO);
		this.recordSectionInputs(TWO, TWO_INPUT);
		this.recordSectionTasks(TWO);
		this.recordSubSections(TWO);
		this.recordSectionOutputs(ONE, ONE_OUTPUT);
		this.recordSectionOutputs(TWO);
		OfficeSectionOutput output = this.outputs.get(ONE).get(ONE_OUTPUT);
		OfficeSectionInput input = this.inputs.get(TWO).get(TWO_INPUT);
		this.architect.link(output, input);

		// Test
		this.replayMockObjects();
		source.sourceOffice(this.architect, context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if flow is not linked.
	 */
	public void testFlowNotLinked() throws Exception {

		final String SECTION = "Section";
		final String SECTION_OUTPUT = "output";

		// Create and configure the source
		AutoWireOfficeSource source = new AutoWireOfficeSource();
		this.addSection(source, SECTION);

		// Record creating the section
		this.recordOfficeSection(SECTION);
		this.recordSectionObjects(SECTION);
		this.recordSectionInputs(SECTION);
		this.recordSectionTasks(SECTION);
		this.recordSubSections(SECTION);
		OfficeSectionOutputType outputType = this.recordSectionUnlinkedOutput(
				SECTION, SECTION_OUTPUT);
		this.recordReturn(outputType, outputType.isEscalationOnly(), false);
		this.architect
				.addIssue("Section output 'Section:output' is not linked");

		// Test
		this.replayMockObjects();
		source.sourceOffice(this.architect, context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure no issue if escalation flow not linked.
	 */
	public void testEscalationFlowNotLinked() throws Exception {

		final String SECTION = "Section";
		final String SECTION_OUTPUT = "output";

		// Create and configure the source
		AutoWireOfficeSource source = new AutoWireOfficeSource();
		this.addSection(source, SECTION);

		// Record creating the section
		this.recordOfficeSection(SECTION);
		this.recordSectionObjects(SECTION);
		this.recordSectionInputs(SECTION);
		this.recordSectionTasks(SECTION);
		this.recordSubSections(SECTION);
		OfficeSectionOutputType outputType = this.recordSectionUnlinkedOutput(
				SECTION, SECTION_OUTPUT);
		this.recordReturn(outputType, outputType.isEscalationOnly(), true);

		// Test
		this.replayMockObjects();
		source.sourceOffice(this.architect, context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can inherit link flows.
	 */
	public void testInheritLinkFlows() throws Exception {

		final String PARENT = "Parent";
		final String TEMPLATE_OUTPUT = "output";
		final String CHILD = "Child";
		final String TARGET = "Target";
		final String TARGET_INPUT = "input";

		// Create and configure the source
		AutoWireOfficeSource source = new AutoWireOfficeSource();
		AutoWireSection parent = this.addSection(source, PARENT);
		AutoWireSection child = this.addSection(source, CHILD);
		AutoWireSection target = this.addSection(source, TARGET);

		// Link parent to target
		source.link(parent, TEMPLATE_OUTPUT, target, TARGET_INPUT);

		// Child to inherit parent link configuration
		child.setSuperSection(parent);

		// Record creating the section
		this.recordOfficeSection(PARENT);
		this.recordSectionObjects(PARENT);
		this.recordSectionInputs(PARENT);
		this.recordSectionTasks(PARENT);
		this.recordSubSections(PARENT);
		this.recordOfficeSection(CHILD);
		this.recordSectionObjects(CHILD);
		this.recordSectionInputs(CHILD);
		this.recordSectionTasks(CHILD);
		this.recordSubSections(CHILD);
		this.recordOfficeSection(TARGET);
		this.recordSectionObjects(TARGET);
		this.recordSectionInputs(TARGET, TARGET_INPUT);
		this.recordSectionTasks(TARGET);
		this.recordSubSections(TARGET);
		this.recordSectionOutputs(PARENT, TEMPLATE_OUTPUT);
		OfficeSectionOutput parentOutput = this.outputs.get(PARENT).get(
				TEMPLATE_OUTPUT);
		OfficeSectionInput input = this.inputs.get(TARGET).get(TARGET_INPUT);
		this.recordSectionOutputs(CHILD, TEMPLATE_OUTPUT);
		this.architect.link(parentOutput, input);
		OfficeSectionOutput childOutput = this.outputs.get(CHILD).get(
				TEMPLATE_OUTPUT);
		this.recordSectionOutputs(TARGET);
		this.architect.link(childOutput, input);

		// Test
		this.replayMockObjects();
		source.sourceOffice(this.architect, context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if section has cyclic inheritance hierarchy.
	 */
	public void testCyclicSectionInheritanceHierarchy() throws Exception {

		final String ONE = "One";
		final String TWO = "Two";
		final String OUTPUT = "output";

		// Create and configure the source with cyclic inheritance
		AutoWireOfficeSource source = new AutoWireOfficeSource();
		AutoWireSection one = this.addSection(source, ONE);
		AutoWireSection two = this.addSection(source, TWO);
		one.setSuperSection(two);
		two.setSuperSection(one);

		// Record creating the section
		this.recordOfficeSection(ONE);
		this.recordSectionObjects(ONE);
		this.recordSectionInputs(ONE);
		this.recordSectionTasks(ONE);
		this.recordSubSections(ONE);
		this.recordOfficeSection(TWO);
		this.recordSectionObjects(TWO);
		this.recordSectionInputs(TWO);
		this.recordSectionTasks(TWO);
		this.recordSubSections(TWO);
		OfficeSectionOutputType outputType = this.recordSectionUnlinkedOutput(
				ONE, OUTPUT);
		this.recordReturn(outputType, outputType.isEscalationOnly(), true);
		this.recordSectionOutputs(TWO);
		this.architect
				.addIssue("Cyclic section inheritance hierarchy ( One : Two : One : ... )");

		// Test
		this.replayMockObjects();
		source.sourceOffice(this.architect, context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to indicate if a flow is linked.
	 */
	public void testIsFlowLinked() throws Exception {

		final String ONE = "One";
		final String ONE_OUTPUT = "output";
		final String TWO = "Two";
		final String TWO_INPUT = "input";

		// Create source with linked flow
		AutoWireOfficeSource source = new AutoWireOfficeSource();
		AutoWireSection one = this.addSection(source, ONE);
		AutoWireSection two = this.addSection(source, TWO);
		source.link(one, ONE_OUTPUT, two, TWO_INPUT);

		// Ensure indicate if linked
		assertTrue("Should indicate linked", source.isLinked(one, ONE_OUTPUT));

		// Provide indications that not linked
		assertFalse("Output not linked", source.isLinked(one, "NotLinked"));
		assertFalse("Section without output not linked",
				source.isLinked(two, "output"));
	}

	/**
	 * Ensure able to obtain {@link AutoWireSection} after adding it.
	 */
	public void testGetSection() throws Exception {

		// Create source
		AutoWireOfficeSource source = new AutoWireOfficeSource();

		// Ensure no section before added
		assertNull("Should not obtain section before added",
				source.getSection("SECTION"));

		// Add the section
		AutoWireSection section = source.addSection("SECTION",
				ClassSectionSource.class.getName(), Object.class.getName());

		// Ensure able to obtain section
		assertSame("Should be able to obtain section by name", section,
				source.getSection("SECTION"));
	}

	/**
	 * Ensure issue if unknown output.
	 */
	public void testUnknownOutput() throws Exception {

		final String ONE = "One";
		final String ONE_OUTPUT = "output";
		final String TWO = "Two";
		final String TWO_INPUT = "input";

		// Create and configure the source
		AutoWireOfficeSource source = new AutoWireOfficeSource();
		AutoWireSection one = this.addSection(source, ONE);
		AutoWireSection two = this.addSection(source, TWO);
		source.link(one, ONE_OUTPUT, two, TWO_INPUT);

		// Record creating the section
		this.recordOfficeSection(ONE);
		this.recordSectionObjects(ONE);
		this.recordSectionInputs(ONE);
		this.recordSectionTasks(ONE);
		this.recordSubSections(ONE);
		this.recordOfficeSection(TWO);
		this.recordSectionObjects(TWO);
		this.recordSectionInputs(TWO, TWO_INPUT);
		this.recordSectionTasks(TWO);
		this.recordSubSections(TWO);
		this.recordSectionOutputs(ONE);
		this.recordSectionOutputs(TWO);
		this.architect
				.addIssue("Unknown section output 'One:output' to link to section input 'Two:input'");

		// Test
		this.replayMockObjects();
		source.sourceOffice(this.architect, context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if unknown input.
	 */
	public void testUnknownInput() throws Exception {

		final String ONE = "One";
		final String ONE_OUTPUT = "output";
		final String TWO = "Two";
		final String TWO_INPUT = "input";

		// Create and configure the source
		AutoWireOfficeSource source = new AutoWireOfficeSource();
		AutoWireSection one = this.addSection(source, ONE);
		AutoWireSection two = this.addSection(source, TWO);
		source.link(one, ONE_OUTPUT, two, TWO_INPUT);

		// Record creating the section
		this.recordOfficeSection(ONE);
		this.recordSectionObjects(ONE);
		this.recordSectionInputs(ONE);
		this.recordSectionTasks(ONE);
		this.recordSubSections(ONE);
		this.recordOfficeSection(TWO);
		this.recordSectionObjects(TWO);
		this.recordSectionInputs(TWO);
		this.recordSectionTasks(TWO);
		this.recordSubSections(TWO);
		this.recordSectionUnlinkedOutput(ONE, ONE_OUTPUT);
		this.recordSectionOutputs(TWO);
		this.architect
				.addIssue("Unknown section input 'Two:input' for linking section output 'One:output'");

		// Test
		this.replayMockObjects();
		source.sourceOffice(this.architect, this.context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to link {@link Escalation} to {@link OfficeSectionInput}.
	 */
	public void testLinkEscalationToSectionInput() throws Exception {

		final PropertyList propertyList = this.createMock(PropertyList.class);
		final net.officefloor.compile.properties.Property property = this
				.createMock(net.officefloor.compile.properties.Property.class);

		// Create and configure the source
		AutoWireOfficeSource source = new AutoWireOfficeSource();
		AutoWireSection section = this.addSection(source, "SECTION");
		source.linkEscalation(Exception.class, section, "INPUT");
		source.linkEscalation(FailedToSourceManagedObjectEscalation.class,
				section, "INPUT");

		// Record linking escalation
		this.recordOfficeSection("SECTION");
		this.recordSectionObjects("SECTION");
		this.recordSectionInputs("SECTION", "INPUT");
		this.recordSectionTasks("SECTION");
		this.recordSubSections("SECTION");
		this.recordSectionOutputs("SECTION");
		this.recordReturn(this.context, this.context.createPropertyList(),
				propertyList);
		this.recordReturn(propertyList, propertyList
				.addProperty(WorkSectionSource.PROPERTY_PARAMETER_PREFIX
						+ "Handle"), property);
		property.setValue("1");
		this.recordEscalation(Exception.class, "SECTION", "INPUT");
		this.recordReturn(
				propertyList,
				propertyList
						.addProperty(AutoWireEscalationCauseRouteWorkSource.PROPERTY_PREFIX_ESCALATION_TYPE
								+ "0"), property);
		property.setValue(Exception.class.getName());

		// Record manually handling failed to source
		this.recordEscalation(FailedToSourceManagedObjectEscalation.class,
				"SECTION", "INPUT");
		this.recordReturn(
				propertyList,
				propertyList
						.addProperty(AutoWireEscalationCauseRouteWorkSource.PROPERTY_PREFIX_ESCALATION_TYPE
								+ "1"), property);
		property.setValue(FailedToSourceManagedObjectEscalation.class.getName());

		// Test
		this.replayMockObjects();
		source.sourceOffice(this.architect, this.context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to add {@link OfficeStart} linked to
	 * {@link OfficeSectionInput}.
	 */
	public void testAddStartupFlow() throws Exception {

		// Create and configure the source
		AutoWireOfficeSource source = new AutoWireOfficeSource();
		AutoWireSection section = this.addSection(source, "SECTION");
		source.addStartupFlow(section, "INPUT");

		// Record linking start-up flow
		this.recordOfficeSection("SECTION");
		this.recordSectionObjects("SECTION");
		this.recordSectionInputs("SECTION", "INPUT");
		this.recordSectionTasks("SECTION");
		this.recordSubSections("SECTION");
		this.recordSectionOutputs("SECTION");
		this.recordStartupFlow("SECTION", "INPUT");

		// Test
		this.replayMockObjects();
		source.sourceOffice(this.architect, this.context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure dependency.
	 */
	public void testOfficeObject() throws Exception {

		final String SECTION = "Section";

		// Create and configure the source
		AutoWireOfficeSource source = new AutoWireOfficeSource();
		this.addSection(source, SECTION);
		source.addAvailableOfficeObject(new AutoWire(Connection.class));

		// Record creating the section
		this.recordOfficeSection(SECTION);
		this.recordSectionObjects(SECTION, new ExpectedAutoWire(
				Connection.class));
		this.recordSectionInputs(SECTION);
		this.recordSectionTasks(SECTION);
		this.recordSubSections(SECTION);
		this.recordSectionOutputs(SECTION);

		// Test
		this.replayMockObjects();
		source.sourceOffice(this.architect, this.context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure dependency with qualified type.
	 */
	public void testOfficeObjectWithQualifiedType() throws Exception {

		final String SECTION = "Section";

		final ExpectedAutoWire qualifiedConnection = new ExpectedAutoWire(
				"QUALIFIED", "QUALIFIED", Connection.class);
		final ExpectedAutoWire unqualifiedConnection = new ExpectedAutoWire(
				Connection.class);

		final ExpectedAutoWire qualifiedString = new ExpectedAutoWire(
				"QUALIFIED", "QUALIFIED", String.class);

		// Create and configure the source
		AutoWireOfficeSource source = new AutoWireOfficeSource();
		this.addSection(source, SECTION);

		// Add the available auto-wiring
		source.addAvailableOfficeObject(qualifiedConnection.requestedAutoWire);
		source.addAvailableOfficeObject(unqualifiedConnection.requestedAutoWire);
		source.addAvailableOfficeObject(qualifiedString.requestedAutoWire);

		// Record creating the section
		this.recordOfficeSection(SECTION);

		// Record combinations of same qualifiers and types
		this.recordSectionObjects(SECTION, qualifiedConnection,
				unqualifiedConnection, qualifiedString);

		this.recordSectionInputs(SECTION);
		this.recordSectionTasks(SECTION);
		this.recordSubSections(SECTION);
		this.recordSectionOutputs(SECTION);

		// Test
		this.replayMockObjects();
		source.sourceOffice(this.architect, this.context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to have qualified dependency fall back to using unqualified
	 * dependency (should no matching qualified dependency be available).
	 */
	public void testFallBackToUnqualifiedOfficeObject() throws Exception {

		final String SECTION = "Section";

		// Create and configure the source
		AutoWireOfficeSource source = new AutoWireOfficeSource();
		this.addSection(source, SECTION);

		// Add the unqualified available auto-wiring
		source.addAvailableOfficeObject(new AutoWire(Connection.class));

		// Record creating the section
		this.recordOfficeSection(SECTION);

		// Record fall back to unqualified type
		this.recordSectionObjects(SECTION, new ExpectedAutoWire("QUALIFIED",
				null, Connection.class));

		this.recordSectionInputs(SECTION);
		this.recordSectionTasks(SECTION);
		this.recordSubSections(SECTION);
		this.recordSectionOutputs(SECTION);

		// Test
		this.replayMockObjects();
		source.sourceOffice(this.architect, this.context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no matching dependency available.
	 */
	public void testNoAvailableAutoWireOfficeObject() throws Exception {

		final String SECTION = "SECTION";
		final OfficeSectionObjectType objectType = this
				.createMock(OfficeSectionObjectType.class);

		// Create and configure the source
		AutoWireOfficeSource source = new AutoWireOfficeSource();
		this.addSection(source, SECTION);

		// Provide auto-wiring that will not match
		source.addAvailableOfficeObject(new AutoWire(String.class));
		source.addAvailableOfficeObject(new AutoWire("QUALIFIED",
				Connection.class.getName()));

		// Record creating the section
		this.recordOfficeSection(SECTION);

		// Record office section object that not matches available auto-wiring
		OfficeSectionType sectionType = this.sectionTypes.get(SECTION);
		this.recordReturn(sectionType,
				sectionType.getOfficeSectionObjectTypes(),
				new OfficeSectionObjectType[] { objectType });
		this.recordReturn(objectType, objectType.getObjectType(),
				Connection.class.getName());
		this.recordReturn(objectType, objectType.getTypeQualifier(), null);
		this.recordReturn(objectType, objectType.getOfficeSectionObjectName(),
				"OBJECT");
		this.architect
				.addIssue("No available auto-wiring for Section Object OBJECT (qualifier=null, type="
						+ Connection.class.getName()
						+ ") from OfficeSection SECTION");

		this.recordSectionInputs(SECTION);
		this.recordSectionTasks(SECTION);
		this.recordSubSections(SECTION);
		this.recordSectionOutputs(SECTION);

		// Test
		this.replayMockObjects();
		source.sourceOffice(this.architect, this.context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure no used {@link OfficeObject} instances.
	 */
	public void testNoUsedOfficeObjects() throws Exception {

		final String SECTION = "Section";

		// Create and configure the source
		AutoWireOfficeSource source = new AutoWireOfficeSource();
		this.addSection(source, SECTION);

		// Add various available auto-wiring that will not be used
		source.addAvailableOfficeObject(new AutoWire(Connection.class));
		source.addAvailableOfficeObject(new AutoWire("QUALIFIED",
				Connection.class.getName()));
		source.addAvailableOfficeObject(new AutoWire(String.class));
		source.addAvailableOfficeObject(new AutoWire(Integer.class));
		source.addAvailableOfficeObject(new AutoWire(DataSource.class));

		// Record creating the section
		this.recordOfficeSection(SECTION);
		this.recordSectionObjects(SECTION);
		this.recordSectionInputs(SECTION);
		this.recordSectionTasks(SECTION);
		this.recordSubSections(SECTION);
		this.recordSectionOutputs(SECTION);

		// Test
		this.replayMockObjects();
		source.sourceOffice(this.architect, this.context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can provide {@link Governance} over {@link OfficeObject}.
	 */
	public void testOfficeObjectGovernance() throws Exception {

		final String SECTION = "Section";
		final String GOVERNANCE = "GOVERNANCE";

		// Create and configure the source
		AutoWireOfficeSource source = new AutoWireOfficeSource();
		this.addSection(source, SECTION);
		this.addGovernance(source, GOVERNANCE, "PROPERTY_NAME",
				"PROPERTY_VALUE");
		source.addAvailableOfficeObject(new AutoWire(Connection.class),
				XAResource.class);

		// Record governance over office object
		this.recordOfficeSection(SECTION);
		this.recordSectionObjects(SECTION, new ExpectedAutoWire(
				Connection.class));
		this.recordSectionInputs(SECTION);
		this.recordSectionTasks(SECTION);
		this.recordSubSections(SECTION);
		this.recordSectionOutputs(SECTION);
		OfficeGovernance governance = this.recordGovernance(GOVERNANCE,
				XAResource.class);
		OfficeTeam team = this.recordTeam();
		this.architect.link(governance, team);
		this.recordGovernManagedObject(GOVERNANCE, SECTION, null, false);
		this.recordGovernOfficeObject(GOVERNANCE,
				new AutoWire(Connection.class));
		this.recordGovernSubSections(SECTION);

		// Test
		this.replayMockObjects();
		source.sourceOffice(this.architect, this.context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure does not provide {@link Governance} over an unused
	 * {@link OfficeObject}.
	 */
	public void testUnusedOfficeObjectGovernance() throws Exception {

		final String SECTION = "Section";
		final String GOVERNANCE = "GOVERNANCE";

		// Create and configure the source
		AutoWireOfficeSource source = new AutoWireOfficeSource();
		this.addSection(source, SECTION);
		this.addGovernance(source, GOVERNANCE, "PROPERTY_NAME",
				"PROPERTY_VALUE");

		// Used OfficeObject
		source.addAvailableOfficeObject(new AutoWire(Connection.class),
				XAResource.class);

		// Unused OfficeObject (should not be governed)
		source.addAvailableOfficeObject(new AutoWire(DataSource.class),
				XAResource.class);

		// Record governance over the used OfficeObject
		this.recordOfficeSection(SECTION);
		this.recordSectionObjects(SECTION, new ExpectedAutoWire(
				Connection.class));
		this.recordSectionInputs(SECTION);
		this.recordSectionTasks(SECTION);
		this.recordSubSections(SECTION);
		this.recordSectionOutputs(SECTION);
		OfficeGovernance governance = this.recordGovernance(GOVERNANCE,
				XAResource.class);
		OfficeTeam team = this.recordTeam();
		this.architect.link(governance, team);
		this.recordGovernOfficeObject(GOVERNANCE,
				new AutoWire(Connection.class));
		this.recordGovernManagedObject(GOVERNANCE, SECTION, null, false);
		this.recordGovernSubSections(SECTION);

		// Test
		this.replayMockObjects();
		source.sourceOffice(this.architect, this.context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can provide {@link Governance} over {@link ManagedObject}.
	 */
	public void testManagedObjectGovernance() throws Exception {

		final String SECTION = "SECTION";
		final String SUB_SECTION = "SUB_SECTION";
		final String SUB_SUB_SECTION = "SUB_SUB_SECTION";
		final String GOVERNANCE = "GOVERNANCE";

		// Create and configure the source
		AutoWireOfficeSource source = new AutoWireOfficeSource();
		this.addSection(source, SECTION);
		this.addGovernance(source, GOVERNANCE, "PROPERTY_NAME",
				"PROPERTY_VALUE");

		// Record governance over office object
		this.recordOfficeSection(SECTION);
		this.recordSectionObjects(SECTION);
		this.recordSectionInputs(SECTION);
		this.recordSectionTasks(SECTION);
		this.recordSubSections(SECTION, SUB_SECTION);
		this.recordSubSections(SUB_SECTION, SUB_SUB_SECTION);
		this.recordSubSections(SUB_SUB_SECTION);
		this.recordSectionOutputs(SECTION);
		OfficeGovernance governance = this.recordGovernance(GOVERNANCE,
				XAResource.class);
		OfficeTeam team = this.recordTeam();
		this.architect.link(governance, team);
		this.recordGovernManagedObject(GOVERNANCE, SECTION, XAResource.class,
				true);
		this.recordGovernSubSections(SECTION, SUB_SECTION);
		this.recordGovernManagedObject(GOVERNANCE, SUB_SECTION, Map.class,
				false);
		this.recordGovernSubSections(SUB_SECTION, SUB_SUB_SECTION);
		this.recordGovernManagedObject(GOVERNANCE, SUB_SUB_SECTION,
				XAResource.class, true);
		this.recordGovernSubSections(SUB_SUB_SECTION);

		// Test
		this.replayMockObjects();
		source.sourceOffice(this.architect, this.context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if unknown {@link AutoWireSection} for {@link Governance}.
	 */
	public void testUnknownSectionGovernance() throws Exception {

		final String SECTION = "SECTION";
		final String UNKNOWN_SECTION = "UNKNOWN";
		final String GOVERNANCE = "GOVERNANCE";

		final AutoWireSection unknownSection = this
				.createMock(AutoWireSection.class);

		// Create and configure the source
		AutoWireOfficeSource source = new AutoWireOfficeSource();
		this.addSection(source, SECTION);
		AutoWireGovernance governance = this.addGovernance(source, GOVERNANCE);
		governance.governSection(unknownSection);

		// Record governance over office object
		this.recordOfficeSection(SECTION);
		this.recordSectionObjects(SECTION);
		this.recordSectionInputs(SECTION);
		this.recordSectionTasks(SECTION);
		this.recordSubSections(SECTION);
		this.recordSectionOutputs(SECTION);
		OfficeGovernance officeGovernance = this.recordGovernance(GOVERNANCE,
				XAResource.class);
		OfficeTeam team = this.recordTeam();
		this.architect.link(officeGovernance, team);
		this.recordReturn(unknownSection, unknownSection.getSectionName(),
				UNKNOWN_SECTION);
		this.architect.addIssue("Unknown section '" + UNKNOWN_SECTION
				+ "' to be governed by governance " + GOVERNANCE);
		this.recordGovernManagedObject(GOVERNANCE, SECTION, null, false);
		this.recordGovernSubSections(SECTION);

		// Test
		this.replayMockObjects();
		source.sourceOffice(this.architect, this.context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can provide {@link Governance} over {@link AutoWireSection}.
	 */
	public void testSectionGovernance() throws Exception {

		final String SECTION = "Section";
		final String GOVERNANCE = "GOVERNANCE";

		// Create and configure the source
		AutoWireOfficeSource source = new AutoWireOfficeSource();
		AutoWireSection section = this.addSection(source, SECTION);
		AutoWireGovernance governance = this.addGovernance(source, GOVERNANCE,
				"PROPERTY_NAME", "PROPERTY_VALUE");
		governance.governSection(section);

		// Record governance over office object
		this.recordOfficeSection(SECTION);
		this.recordSectionObjects(SECTION);
		this.recordSectionInputs(SECTION);
		this.recordSectionTasks(SECTION);
		this.recordSubSections(SECTION);
		this.recordSectionOutputs(SECTION);
		OfficeGovernance officeGovernance = this.recordGovernance(GOVERNANCE,
				XAResource.class);
		OfficeTeam team = this.recordTeam();
		this.architect.link(officeGovernance, team);
		this.recordGovernSections(GOVERNANCE, SECTION);
		this.recordGovernManagedObject(GOVERNANCE, SECTION, null, false);
		this.recordGovernSubSections(SECTION);

		// Test
		this.replayMockObjects();
		source.sourceOffice(this.architect, this.context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can assign specific {@link Team} for {@link Governance} by
	 * extension interface.
	 */
	public void testGovernanceTeamByExtensionInterface() throws Exception {

		final String GOVERNANCE = "GOVERNANCE";

		// Create and configure the source
		AutoWireOfficeSource source = new AutoWireOfficeSource();
		source.addAvailableOfficeTeam(new AutoWire(XAResource.class));
		this.addGovernance(source, GOVERNANCE, "PROPERTY_NAME",
				"PROPERTY_VALUE");

		// Record governance over office object
		OfficeGovernance governance = this.recordGovernance(GOVERNANCE,
				XAResource.class);
		OfficeTeam team = this.recordTeam(new AutoWire(XAResource.class));
		this.architect.link(governance, team);
		this.recordGovernSections(GOVERNANCE);

		// Test
		this.replayMockObjects();
		source.sourceOffice(this.architect, this.context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can assign specific {@link Team} for {@link Governance} by
	 * extension interface ignoring the qualification.
	 */
	public void testGovernanceTeamByExtensionInterfaceIgnoringQualification()
			throws Exception {

		final String GOVERNANCE = "GOVERNANCE";

		// Create and configure the source
		AutoWireOfficeSource source = new AutoWireOfficeSource();
		source.addAvailableOfficeTeam(new AutoWire("QUALIFIED",
				XAResource.class.getName()));
		source.addAvailableOfficeTeam(new AutoWire(XAResource.class));
		this.addGovernance(source, GOVERNANCE, "PROPERTY_NAME",
				"PROPERTY_VALUE");

		// Record governance over office object (not loading unused teams)
		OfficeGovernance governance = this.recordGovernance(GOVERNANCE,
				XAResource.class);
		OfficeTeam team = this.recordTeam(new AutoWire(XAResource.class));
		this.architect.link(governance, team);
		this.recordGovernSections(GOVERNANCE);

		// Test
		this.replayMockObjects();
		source.sourceOffice(this.architect, this.context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can assign default {@link Team} for {@link Governance}.
	 */
	public void testGovernanceTeamByDefaultTeam() throws Exception {

		final String GOVERNANCE = "GOVERNANCE";

		// Create and configure the source
		AutoWireOfficeSource source = new AutoWireOfficeSource();
		this.addGovernance(source, GOVERNANCE);

		// Record governance over office object
		OfficeGovernance governance = this.recordGovernance(GOVERNANCE,
				XAResource.class);
		OfficeTeam team = this.recordTeam();
		this.architect.link(governance, team);
		this.recordGovernSections(GOVERNANCE);

		// Test
		this.replayMockObjects();
		source.sourceOffice(this.architect, this.context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can assign a {@link Team} for {@link TaskObject}.
	 */
	public void testAssignTeamForTaskObject() throws Exception {
		this.doAssignTeamTest(new AutoWire(Connection.class), true,
				MockTeamTaskObjectSection.class,
				new AutoWire(Connection.class), new AutoWire(String.class));
	}

	/**
	 * Ensure unused {@link Team} not loaded for {@link TaskObject}.
	 */
	public void testUnusedTeamForTaskObject() throws Exception {
		this.doAssignTeamTest(new AutoWire(UnusedTeam.class), false,
				MockTeamTaskObjectSection.class,
				new AutoWire(Connection.class), new AutoWire(String.class));
	}

	/**
	 * Interface to identify unused {@link Team} in testing. This should not
	 * match any of the dependencies and therefore cause the {@link TeamSource}
	 * not to be loaded.
	 */
	private static interface UnusedTeam {
	}

	/**
	 * Mock {@link Team} section class with direct dependency.
	 */
	public static class MockTeamTaskObjectSection {

		public void taskNotAssign(String value) {
		}

		public void taskAssign(Connection connection) {
		}
	}

	/**
	 * Ensure can assign a {@link Team} for qualified {@link TaskObject}.
	 */
	public void testAssignTeamForQualifiedTaskObject() throws Exception {
		this.doAssignTeamTest(new AutoWire(MockQualifier.class, Integer.class),
				true, MockTeamQualifiedTaskObjectSection.class, new AutoWire(
						Integer.class), new AutoWire(MockQualifier.class,
						Integer.class));
	}

	/**
	 * Ensure used {@link Team} not loaded for qualified {@link Task}
	 * dependency.
	 */
	public void testUnusedTeamForQualifiedTaskObject() throws Exception {
		this.doAssignTeamTest(new AutoWire(UnusedTeam.class), false,
				MockTeamQualifiedTaskObjectSection.class, new AutoWire(
						Integer.class), new AutoWire(MockQualifier.class,
						Integer.class));
	}

	/**
	 * Mock {@link Team} section class with qualified {@link Dependency}.
	 */
	public static class MockTeamQualifiedTaskObjectSection {

		public void taskAssign(@MockQualifier Integer object) {
		}

		public void taskNotAssign(Integer object) {
		}
	}

	/**
	 * Ensure can assign a {@link Team} for {@link ManagedObject}.
	 */
	public void testAssignTeamForManagedObject() throws Exception {
		this.doAssignTeamTest(new AutoWire(MockManagedObject.class), true,
				MockTeamManagedObjectSection.class, new AutoWire(
						MockManagedObject.class));
	}

	/**
	 * Ensure not load unused {@link Team} for {@link ManagedObject}.
	 */
	public void testUnusedTeamForManagedObject() throws Exception {
		this.doAssignTeamTest(new AutoWire(UnusedTeam.class), false,
				MockTeamManagedObjectSection.class, new AutoWire(
						MockManagedObject.class));
	}

	/**
	 * Mock dependency for {@link MockTeamDependencySection}.
	 */
	public static class MockManagedObject {
	}

	/**
	 * Mock {@link Team} section class with qualified {@link ManagedObject}.
	 */
	public static class MockTeamManagedObjectSection {

		@ManagedObject(source = ClassManagedObjectSource.class, properties = { @Property(name = ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, valueClass = MockManagedObject.class) })
		MockManagedObject managedObject;

		/**
		 * {@link Task} depends on {@link #managedObject}.
		 */
		public void taskAssign() {
		}
	}

	/**
	 * Ensure can assign a {@link Team} for qualified {@link ManagedObject}.
	 */
	public void testAssignTeamForQualifiedManagedObject() throws Exception {
		this.doAssignTeamTest(new AutoWire(MockQualifier.class, Integer.class),
				true, MockTeamQualifiedManagedObjectSection.class,
				new AutoWire(Integer.class));
	}

	/**
	 * Ensure not load unused {@link Team} for qualified {@link ManagedObject}.
	 */
	public void testUnusedTeamForQualifiedManagedObject() throws Exception {
		this.doAssignTeamTest(new AutoWire(UnusedTeam.class), false,
				MockTeamQualifiedManagedObjectSection.class, new AutoWire(
						Integer.class));
	}

	/**
	 * Mock qualifier.
	 */
	@Qualifier
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface MockQualifier {
	}

	/**
	 * Mock qualified dependency for {@link MockTeamQualifiedDependencySection}.
	 */
	public static class MockQualifiedManagedObject {
	}

	/**
	 * Mock {@link Team} section class with qualified {@link ManagedObject}.
	 */
	public static class MockTeamQualifiedManagedObjectSection {

		@ManagedObject(source = ClassManagedObjectSource.class, qualifiers = { @TypeQualifier(qualifier = MockQualifier.class, type = Integer.class) }, properties = { @Property(name = ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, valueClass = MockQualifiedManagedObject.class) })
		MockQualifiedManagedObject managedObject;

		/**
		 * {@link Task} depends on {@link #managedObject}.
		 */
		public void taskAssign() {
		}
	}

	/**
	 * Ensure can assign a {@link Team} for {@link Task} having dependency on a
	 * {@link ManagedObject} that depends on the matching {@link AutoWire}.
	 */
	public void testAssignTeamForManagedObjectDependency() throws Exception {
		this.doAssignTeamTest(new AutoWire(Connection.class), true,
				MockTeamManagedObjectDependencySection.class, new AutoWire(
						Connection.class));
	}

	/**
	 * Ensure not load unused {@link Team} for {@link Task} having dependency on
	 * a {@link ManagedObject} that depends on the matching {@link AutoWire}.
	 */
	public void testUnusedTeamForManagedObjectDependency() throws Exception {
		this.doAssignTeamTest(new AutoWire(UnusedTeam.class), false,
				MockTeamManagedObjectDependencySection.class, new AutoWire(
						Connection.class));
	}

	/**
	 * Mock {@link Team} {@link ManagedObject} that has dependency.
	 */
	public static class MockTeamDependencyManagedObject {

		@Dependency
		Connection connection;
	}

	/**
	 * Mock {@link Team} section class with dependency via {@link ManagedObject}
	 * dependency.
	 */
	public static class MockTeamManagedObjectDependencySection {

		@ManagedObject(source = ClassManagedObjectSource.class, properties = { @Property(name = ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, valueClass = MockTeamDependencyManagedObject.class) })
		MockTeamDependencyManagedObject managedObject;

		/**
		 * {@link Task} depends on {@link #managedObject} which depends on
		 * {@link Connection}.
		 */
		public void taskAssign() {
		}
	}

	/**
	 * Ensure can assign a {@link Team} for {@link Task} having dependency on a
	 * {@link ManagedObject} that depends on the matching qualified
	 * {@link AutoWire}.
	 */
	public void testAssignTeamForQualifiedManagedObjectDependency()
			throws Exception {
		this.doAssignTeamTest(new AutoWire(MockQualifier.class,
				Connection.class), true,
				MockTeamQualifiedManagedObjectDependencySection.class,
				new AutoWire(MockQualifier.class, Connection.class));
	}

	/**
	 * Ensure not load unused {@link Team} for {@link Task} having dependency on
	 * a {@link ManagedObject} that depends on the matching qualified
	 * {@link AutoWire}.
	 */
	public void testUnusedTeamForQualifiedManagedObjectDependency()
			throws Exception {
		this.doAssignTeamTest(new AutoWire(MockQualifier.class,
				Connection.class), true,
				MockTeamQualifiedManagedObjectDependencySection.class,
				new AutoWire(MockQualifier.class, Connection.class));
	}

	/**
	 * Mock {@link Team} {@link ManagedObject} that has qualified dependency.
	 */
	public static class MockTeamQualifiedDependencyManagedObject {

		@MockQualifier
		@Dependency
		Connection connection;
	}

	/**
	 * Mock {@link Team} section class with qualified dependency via
	 * {@link ManagedObject} dependency.
	 */
	public static class MockTeamQualifiedManagedObjectDependencySection {

		@ManagedObject(source = ClassManagedObjectSource.class, properties = { @Property(name = ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, valueClass = MockTeamQualifiedDependencyManagedObject.class) })
		MockTeamDependencyManagedObject managedObject;

		/**
		 * {@link Task} depends on {@link #managedObject} which depends on
		 * {@link Connection}.
		 */
		public void taskAssign() {
		}
	}

	/**
	 * Does the assign {@link Team} test.
	 * 
	 * @param teamAutoWire
	 *            {@link Team} {@link AutoWire}.
	 * @param isUseTeam
	 *            Indicates if {@link Team} is to be used.
	 * @param sectionClass
	 *            Section class.
	 * @param objectAutoWiring
	 *            Object {@link AutoWire} instances.
	 */
	private void doAssignTeamTest(final AutoWire teamAutoWire,
			final boolean isUseTeam, Class<?> sectionClass,
			AutoWire... objectAutoWiring) throws Exception {

		// Create and configure the source
		AutoWireOfficeSource source = new AutoWireOfficeSource();
		this.addSection(source, sectionClass);

		// Add available auto-wire team
		source.addAvailableOfficeTeam(teamAutoWire);

		// Add the available auto-wire objects
		for (AutoWire autoWire : objectAutoWiring) {
			source.addAvailableOfficeObject(autoWire);
		}

		// Record creating the section
		this.recordOfficeSection(sectionClass, new TeamAssigner() {

			private OfficeTeam usedTeam = null;

			private OfficeTeam defaultTeam = null;

			/**
			 * Indicates if using the {@link OfficeTeam}.
			 * 
			 * @param taskName
			 *            Name of the {@link OfficeSectionTask}.
			 * @return <code>true</code> to use the specific {@link OfficeTeam}.
			 *         <code>false</code> for default {@link OfficeTeam}.
			 */
			private boolean isUseAddedTeam(String taskName) {
				return (isUseTeam && ("taskAssign".equals(taskName)));
			}

			@Override
			public void recordAssignTeam(String taskName, OfficeSectionTask task,
					OfficeTaskType taskType, OfficeTaskType actualTaskType) {

				// Record obtaining the object dependencies (use actual
				// dependencies)
				AutoWireOfficeSourceTest.this.recordReturn(taskType,
						taskType.getObjectDependencies(),
						actualTaskType.getObjectDependencies());

				// Obtain team responsible
				OfficeTeam assignedTeam = null;

				// Determine if using specific assigned team
				if (this.isUseAddedTeam(taskName)) {
					// Only obtain the used team once if using
					if (this.usedTeam == null) {
						this.usedTeam = AutoWireOfficeSourceTest.this
								.recordTeam(teamAutoWire);
					}
					assignedTeam = this.usedTeam;
				}

				// Using default team
				if (assignedTeam == null) {
					// Only create default team once
					if (this.defaultTeam == null) {
						this.defaultTeam = AutoWireOfficeSourceTest.this
								.recordTeam();
					}
					assignedTeam = this.defaultTeam;
				}

				// Assign the team
				TaskTeam responsibleTeam = AutoWireOfficeSourceTest.this
						.createMock(TaskTeam.class);
				AutoWireOfficeSourceTest.this.recordReturn(task,
						task.getTeamResponsible(), responsibleTeam);
				AutoWireOfficeSourceTest.this.architect.link(responsibleTeam,
						assignedTeam);
			}
		});

		// Test
		this.replayMockObjects();
		source.sourceOffice(this.architect, this.context);
		this.verifyMockObjects();
	}

	/**
	 * {@link PropertyList} instances by section name.
	 */
	private final Map<String, PropertyList> sectionProperties = new HashMap<String, PropertyList>();

	/**
	 * {@link OfficeSectionType} instances by name.
	 */
	private final Map<String, OfficeSectionType> sectionTypes = new HashMap<String, OfficeSectionType>();

	/**
	 * {@link OfficeSection} instances by name.
	 */
	private final Map<String, OfficeSection> sections = new HashMap<String, OfficeSection>();

	/**
	 * {@link OfficeSectionInputType} instances by {@link OfficeSection} name
	 * and input type.
	 */
	private final Map<String, Map<String, OfficeSectionInputType>> inputTypes = new HashMap<String, Map<String, OfficeSectionInputType>>();

	/**
	 * {@link OfficeSectionInput} instances by {@link OfficeSection} name and
	 * input type.
	 */
	private final Map<String, Map<String, OfficeSectionInput>> inputs = new HashMap<String, Map<String, OfficeSectionInput>>();

	/**
	 * {@link OfficeSectionOutputType} instances by {@link OfficeSection} name
	 * and output name.
	 */
	private final Map<String, Map<String, OfficeSectionOutputType>> outputTypes = new HashMap<String, Map<String, OfficeSectionOutputType>>();

	/**
	 * {@link OfficeSectionOutput} instances by {@link OfficeSection} name and
	 * output name.
	 */
	private final Map<String, Map<String, OfficeSectionOutput>> outputs = new HashMap<String, Map<String, OfficeSectionOutput>>();

	/**
	 * {@link OfficeSectionObjectType} instances by {@link OfficeSection} name
	 * and object type.
	 */
	private final Map<String, Map<String, OfficeSectionObjectType>> objectTypes = new HashMap<String, Map<String, OfficeSectionObjectType>>();

	/**
	 * {@link OfficeSectionObject} instances by {@link OfficeSection} name and
	 * object type.
	 */
	private final Map<String, Map<String, OfficeSectionObject>> objects = new HashMap<String, Map<String, OfficeSectionObject>>();

	/**
	 * {@link OfficeObject} instances by type.
	 */
	private final Map<String, OfficeObject> dependencies = new HashMap<String, OfficeObject>();

	/**
	 * {@link OfficeSubSectionType} instances by name.
	 */
	private final Map<String, OfficeSubSectionType> subSectionTypes = new HashMap<String, OfficeSubSectionType>();

	/**
	 * {@link OfficeSubSection} instances by name.
	 */
	private final Map<String, OfficeSubSection> subSections = new HashMap<String, OfficeSubSection>();

	/**
	 * {@link OfficeTaskType} instances by {@link OfficeSection} name and
	 * {@link Task} name.
	 */
	private final Map<String, Map<String, OfficeTaskType>> taskTypes = new HashMap<String, Map<String, OfficeTaskType>>();

	/**
	 * {@link OfficeSectionTask} instances by {@link OfficeSection} name and
	 * {@link Task} name.
	 */
	private final Map<String, Map<String, OfficeSectionTask>> tasks = new HashMap<String, Map<String, OfficeSectionTask>>();

	/**
	 * {@link OfficeGovernance} instances by name.
	 */
	private final Map<String, OfficeGovernance> governances = new HashMap<String, OfficeGovernance>();

	/**
	 * {@link PropertyList} instances by {@link Governance} name.
	 */
	private final Map<String, PropertyList> governanceProperties = new HashMap<String, PropertyList>();

	/**
	 * Mock default {@link OfficeTeam}.
	 */
	private OfficeTeam team = null;

	/**
	 * Records the {@link OfficeTeam}.
	 * 
	 * @return {@link OfficeTeam}.
	 */
	private OfficeTeam recordTeam() {

		// Only load the default team once
		if (this.team == null) {
			this.team = this.createMock(OfficeTeam.class);
			this.recordReturn(this.architect,
					this.architect.addOfficeTeam("team"), this.team);
		}

		// Return the team
		return this.team;
	}

	/**
	 * Records the {@link OfficeTeam} for a object.
	 * 
	 * @param autoWire
	 *            {@link AutoWire}.
	 * @return {@link OfficeTeam}.
	 */
	private OfficeTeam recordTeam(AutoWire autoWire) {
		OfficeTeam officeTeam = this.createMock(OfficeTeam.class);
		this.recordReturn(this.architect,
				this.architect.addOfficeTeam(autoWire.getQualifiedType()),
				officeTeam);
		return officeTeam;
	}

	/**
	 * Adds an {@link OfficeSection}.
	 * 
	 * @param source
	 *            {@link AutoWireOfficeSource}.
	 * @param sectionClass
	 *            {@link ClassSectionSource} class.
	 * @return {@link AutoWireSection}.
	 */
	private AutoWireSection addSection(AutoWireOfficeSource source,
			Class<?> sectionClass) {

		final String SECTION_NAME = sectionClass.getSimpleName();

		// Add the section
		AutoWireSection section = source.addSection(SECTION_NAME,
				ClassSectionSource.class.getName(), sectionClass.getName());

		// Register the properties
		PropertyList properties = section.getProperties();
		this.sectionProperties.put(SECTION_NAME, properties);

		// Return the section
		return section;
	}

	/**
	 * Records the {@link OfficeSection} for the {@link ClassSectionSource}
	 * class.
	 * 
	 * @param sectionClass
	 *            {@link ClassSectionSource} class.
	 * @param assigner
	 *            {@link TeamAssigner}.
	 * @return {@link OfficeSection}.
	 */
	private OfficeSection recordOfficeSection(Class<?> sectionClass,
			TeamAssigner assigner) {

		final String SECTION_NAME = sectionClass.getSimpleName();

		// Obtain the properties
		PropertyList properties = this.sectionProperties.get(SECTION_NAME);
		assertNotNull("Section " + SECTION_NAME + " should be added",
				properties);

		// Obtain the actual section type for configuring recording
		OfficeSectionType actualType = SectionLoaderUtil.loadOfficeSectionType(
				SECTION_NAME, ClassSectionSource.class, sectionClass.getName());

		// Record adding the office section
		OfficeSection officeSection = this.createMock(OfficeSection.class);
		this.recordReturn(this.architect, this.architect.addOfficeSection(
				SECTION_NAME, ClassSectionSource.class.getName(),
				sectionClass.getName()), officeSection);
		this.sections.put(SECTION_NAME, officeSection);

		// Record obtaining the office section type
		OfficeSectionType officeSectionType = this
				.createMock(OfficeSectionType.class);
		this.recordReturn(this.context, this.context.loadOfficeSectionType(
				SECTION_NAME, ClassSectionSource.class.getName(),
				sectionClass.getName(), properties), officeSectionType);
		this.sectionTypes.put(SECTION_NAME, officeSectionType);

		// Record obtaining the office objects
		OfficeSectionObjectType[] actualObjectTypes = actualType
				.getOfficeSectionObjectTypes();
		String[] objectNames = (Arrays.asList(actualObjectTypes)).stream()
				.map(t -> t.getOfficeSectionObjectName())
				.toArray(String[]::new);
		OfficeSectionObjectType[] objectTypes = this.createSectionItems(
				SECTION_NAME, OfficeSectionObjectType.class, this.objectTypes,
				objectNames);
		this.recordReturn(officeSectionType,
				officeSectionType.getOfficeSectionObjectTypes(), objectTypes);
		for (int i = 0; i < objectTypes.length; i++) {
			OfficeSectionObjectType actualObjectType = actualObjectTypes[i];
			String objectName = objectNames[i];
			String objectTypeName = actualObjectType.getObjectType();
			String typeQualifier = actualObjectType.getTypeQualifier();
			OfficeSectionObjectType objectType = objectTypes[i];

			// Record adding the office object information
			this.recordReturn(objectType, objectType.getObjectType(),
					objectTypeName);
			this.recordReturn(objectType, objectType.getTypeQualifier(),
					typeQualifier);
			this.recordReturn(objectType,
					objectType.getOfficeSectionObjectName(), objectName);

			// Record obtaining the office section object
			OfficeSectionObject sectionObject = this
					.createMock(OfficeSectionObject.class);
			this.recordReturn(officeSection,
					officeSection.getOfficeSectionObject(objectName),
					sectionObject);

			// Record obtaining the office object
			OfficeObject officeObject = this.createMock(OfficeObject.class);
			this.recordReturn(this.architect, this.architect.addOfficeObject(
					new AutoWire(typeQualifier, objectTypeName)
							.getQualifiedType(), objectTypeName), officeObject);
			if (typeQualifier != null) {
				officeObject.setTypeQualifier(typeQualifier);
			}

			// Record the link
			this.architect.link(sectionObject, officeObject);
		}

		// Record obtaining the office inputs
		OfficeSectionInputType[] actualInputTypes = actualType
				.getOfficeSectionInputTypes();
		String[] inputNames = (Arrays.asList(actualInputTypes)).stream()
				.map(t -> t.getOfficeSectionInputName()).toArray(String[]::new);
		OfficeSectionInputType[] inputTypes = this.createSectionItems(
				SECTION_NAME, OfficeSectionInputType.class, this.inputTypes,
				inputNames);
		OfficeSectionInput[] inputs = this.createSectionItems(SECTION_NAME,
				OfficeSectionInput.class, this.inputs, inputNames);
		this.recordReturn(officeSectionType,
				officeSectionType.getOfficeSectionInputTypes(), inputTypes);
		for (int i = 0; i < inputTypes.length; i++) {
			String inputName = inputNames[i];
			OfficeSectionInputType inputType = inputTypes[i];
			OfficeSectionInput input = inputs[i];
			this.recordReturn(inputType, inputType.getOfficeSectionInputName(),
					inputName);
			this.recordReturn(officeSection,
					officeSection.getOfficeSectionInput(inputName), input);
		}

		// Record obtaining the task types
		OfficeTaskType[] actualTaskTypes = actualType.getOfficeTaskTypes();
		String[] taskNames = (Arrays.asList(actualTaskTypes)).stream()
				.map(t -> t.getOfficeTaskName()).toArray(String[]::new);
		OfficeTaskType[] taskTypes = this.createSectionItems(SECTION_NAME,
				OfficeTaskType.class, this.taskTypes, taskNames);
		OfficeSectionTask[] tasks = this.createSectionItems(SECTION_NAME,
				OfficeSectionTask.class, this.tasks, taskNames);
		this.recordReturn(officeSectionType,
				officeSectionType.getOfficeTaskTypes(), taskTypes);
		for (int i = 0; i < taskTypes.length; i++) {
			String taskName = taskNames[i];
			OfficeTaskType taskType = taskTypes[i];
			OfficeSectionTask task = tasks[i];
			OfficeTaskType actualTaskType = actualTaskTypes[i];
			this.recordReturn(taskType, taskType.getOfficeTaskName(), taskName);
			this.recordReturn(officeSection,
					officeSection.getOfficeSectionTask(taskName), task);

			// Record assigning the team
			assigner.recordAssignTeam(taskName, task, taskType, actualTaskType);
		}

		// No sub sections
		this.recordReturn(officeSectionType,
				officeSectionType.getOfficeSubSectionTypes(),
				new OfficeSubSectionType[0]);

		// Record obtaining the office outputs
		OfficeSectionOutputType[] actualOutputTypes = actualType
				.getOfficeSectionOutputTypes();
		String[] outputNames = (Arrays.asList(actualOutputTypes)).stream()
				.map(t -> t.getOfficeSectionOutputName())
				.toArray(String[]::new);
		OfficeSectionOutputType[] outputTypes = this.createSectionItems(
				SECTION_NAME, OfficeSectionOutputType.class, this.outputTypes,
				outputNames);
		this.recordReturn(officeSectionType,
				officeSectionType.getOfficeSectionOutputTypes(), outputTypes);

		// Record the office section outputs linked to office outputs
		for (int i = 0; i < outputTypes.length; i++) {
			String outputName = outputNames[i];
			OfficeSectionOutputType outputType = outputTypes[i];
			OfficeSectionOutputType actualOutputType = actualOutputTypes[i];

			// Record obtaining the office section output
			OfficeSectionOutput sectionOutput = this
					.createMock(OfficeSectionOutput.class);
			this.recordReturn(officeSection,
					officeSection.getOfficeSectionOutput(outputName),
					sectionOutput);

			// Record adding the office output
			String outputTypeName = actualOutputType.getArgumentType();
			this.recordReturn(outputType, outputType.getArgumentType(),
					outputTypeName);
			OfficeOutput officeOutput = this.createMock(OfficeOutput.class);
			this.recordReturn(this.architect,
					this.architect.addOfficeOutput(outputName, outputTypeName),
					officeOutput);

			// Record the link
			this.architect.link(sectionOutput, officeOutput);
		}

		// Return the office section
		return officeSection;
	}

	/**
	 * Records assigning the {@link Team} for the {@link OfficeSectionTask}.
	 */
	private static interface TeamAssigner {

		/**
		 * Records assigning the {@link Team} for the {@link OfficeSectionTask}.
		 * 
		 * @param taskName
		 *            Name of the {@link OfficeSectionTask}.
		 * @param task
		 *            {@link OfficeSectionTask}.
		 * @param taskType
		 *            {@link OfficeTaskType}.
		 * @param actualTaskType
		 *            Actual {@link OfficeTaskType}.
		 */
		void recordAssignTeam(String taskName, OfficeSectionTask task,
				OfficeTaskType taskType, OfficeTaskType actualTaskType);
	}

	/**
	 * Adds an {@link OfficeSection}.
	 * 
	 * @param source
	 *            {@link AutoWireOfficeSource}.
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param factory
	 *            {@link AutoWireSectionFactory}.
	 * @param propertyNameValues
	 *            Property name value pairs.
	 * @return {@link AutoWireSection}.
	 */
	private <A extends AutoWireSection> AutoWireSection addSection(
			AutoWireOfficeSource source, String sectionName,
			AutoWireSectionFactory<A> factory, String... propertyNameValues) {

		// Add the section
		AutoWireSection section;
		final String sectionLocation = sectionName + "Location";
		if (factory == null) {
			section = source.addSection(sectionName,
					SectionSource.class.getName(), sectionLocation);
		} else {
			section = source.addSection(sectionName,
					SectionSource.class.getName(), sectionLocation, factory);
		}

		// Load the properties
		PropertyList properties = section.getProperties();
		for (int i = 0; i < propertyNameValues.length; i += 2) {
			String name = propertyNameValues[i];
			String value = propertyNameValues[i + 1];
			properties.addProperty(name).setValue(value);
		}
		this.sectionProperties.put(sectionName, properties);

		// Return the section
		return section;
	}

	/**
	 * Adds an {@link OfficeSection}.
	 * 
	 * @param source
	 *            {@link AutoWireOfficeSource}.
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param propertyNameValues
	 *            Property name value pairs.
	 * @return {@link AutoWireSection}.
	 */
	private AutoWireSection addSection(AutoWireOfficeSource source,
			String sectionName, String... propertyNameValues) {
		return this.addSection(source, sectionName, null, propertyNameValues);
	}

	/**
	 * Records adding an {@link OfficeSection}.
	 * 
	 * @param sectionName
	 *            Name.
	 * @param subSectionNames
	 *            Sub section names.
	 * @param nameValuePairs
	 *            Name value pairs for configuring the {@link SectionSource}.
	 * @return {@link OfficeSection}.
	 */
	private void recordOfficeSection(String sectionName,
			String... nameValuePairs) {
		assertNull("Already section by name " + sectionName,
				this.sections.get(sectionName));

		// Obtain the properties
		PropertyList properties = this.sectionProperties.get(sectionName);
		assertNotNull("Section " + sectionName + " should be added", properties);

		// Use name to derive a dummy section location
		final String sectionLocation = sectionName + "Location";

		// Record obtaining the section type
		OfficeSectionType sectionType = this
				.createMock(OfficeSectionType.class);
		this.recordReturn(this.context, this.context.loadOfficeSectionType(
				sectionName, SectionSource.class.getName(), sectionLocation,
				properties), sectionType);
		this.sectionTypes.put(sectionName, sectionType);

		// Record creating the section
		OfficeSection section = this.createMock(OfficeSection.class);
		this.recordReturn(this.architect, this.architect.addOfficeSection(
				sectionName, SectionSource.class.getName(), sectionLocation),
				section);
		for (int i = 0; i < nameValuePairs.length; i += 2) {
			section.addProperty(nameValuePairs[i], nameValuePairs[i + 1]);
		}
		this.sections.put(sectionName, section);
	}

	/**
	 * Obtains the {@link OfficeSection}.
	 * 
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @return {@link OfficeSection}.
	 */
	private OfficeSection getOfficeSection(String sectionName) {
		OfficeSection section = this.sections.get(sectionName);
		assertNotNull("Unknown section " + sectionName, section);
		return section;
	}

	/**
	 * Obtains the {@link OfficeSectionType}.
	 * 
	 * @param sectionName
	 *            Name of the {@link OfficeSectionType}.
	 * @return {@link OfficeSectionType}.
	 */
	private OfficeSectionType getOfficeSectionType(String sectionName) {
		OfficeSectionType sectionType = this.sectionTypes.get(sectionName);
		assertNotNull("Unknown section type " + sectionName, sectionType);
		return sectionType;
	}

	/**
	 * Obtains the {@link OfficeSubSection}.
	 * 
	 * @param sectionName
	 *            Name of the {@link OfficeSubSection}.
	 * @return {@link OfficeSubSection}.
	 */
	private OfficeSubSection getOfficeSubSection(String sectionName) {

		// Obtain the office sub section
		OfficeSubSection section = this.sections.get(sectionName);
		if (section == null) {

			// Obtain as sub section
			section = this.subSections.get(sectionName);
			if (section == null) {

				// Unknown section
				fail("Unknown section " + sectionName);
			}
		}

		// Return the office sub section
		return section;
	}

	/**
	 * Obtains the {@link OfficeSubSectionType}.
	 * 
	 * @param sectionName
	 *            Name of the {@link OfficeSubSection}.
	 * @return {@link OfficeSubSectionType}.
	 */
	private OfficeSubSectionType getOfficeSubSectionType(String sectionName) {

		// Obtain the office sub section type
		OfficeSubSectionType sectionType = this.sectionTypes.get(sectionName);
		if (sectionType == null) {

			// Obtain as sub section type
			sectionType = this.subSectionTypes.get(sectionName);
			if (sectionType == null) {

				// Unknown section
				fail("Unknown section " + sectionName);
			}
		}

		// Return the office sub section type
		return sectionType;
	}

	/**
	 * Adds an {@link AutoWireGovernance}.
	 * 
	 * @param source
	 *            {@link AutoWireOfficeSource}.
	 * @param governanceName
	 *            Name of the {@link Governance}.
	 * @param propertyNameValues
	 *            Property name value pairs.
	 * @return {@link AutoWireGovernances}.
	 */
	private AutoWireGovernance addGovernance(AutoWireOfficeSource source,
			String governanceName, String... propertyNameValues) {

		// Add the governance
		AutoWireGovernance governance = source.addGovernance(governanceName,
				GovernanceSource.class.getName());

		// Load the properties
		for (int i = 0; i < propertyNameValues.length; i += 2) {
			String name = propertyNameValues[i];
			String value = propertyNameValues[i + 1];
			governance.addProperty(name, value);
		}

		// Register properties for later recording
		this.governanceProperties.put(governanceName,
				governance.getProperties());

		// Return the governance
		return governance;
	}

	/**
	 * Records adding the {@link Governance}.
	 * 
	 * @param governanceName
	 *            Name of the {@link Governance}.
	 * @param extensionInterface
	 *            Extension interface.
	 */
	private OfficeGovernance recordGovernance(String governanceName,
			Class<?> extensionInterface) {
		assertNull("Already Governance by name " + governanceName,
				this.governances.get(governanceName));

		// Record creating the Governance
		OfficeGovernance governance = this.createMock(OfficeGovernance.class);
		this.recordReturn(this.architect, this.architect.addOfficeGovernance(
				governanceName, GovernanceSource.class.getName()), governance);

		// Load the properties
		PropertyList properties = this.governanceProperties.get(governanceName);
		if (properties != null) {
			for (net.officefloor.compile.properties.Property property : properties) {
				governance.addProperty(property.getName(), property.getValue());
			}
		}

		// Register the governance
		this.governances.put(governanceName, governance);

		// Load the governance type
		GovernanceType<?, ?> governanceType = this
				.createMock(GovernanceType.class);
		this.recordReturn(
				this.context,
				this.context.loadGovernanceType(
						GovernanceSource.class.getName(), properties),
				governanceType);
		this.recordReturn(governanceType,
				governanceType.getExtensionInterface(), extensionInterface);

		// Return the governance
		return governance;
	}

	/**
	 * Records {@link Governance} over the sections.
	 * 
	 * @param governanceName
	 *            Name of {@link Governance}.
	 * @param sectionNames
	 *            Names of the {@link AutoWireSection} instances to provide
	 *            {@link Governance}.
	 */
	private void recordGovernSections(String governanceName,
			String... sectionNames) {

		// Obtain the governance
		OfficeGovernance officeGovernance = this.governances
				.get(governanceName);
		assertNotNull("Unknown governance " + governanceName);

		// Record governing the sections
		for (String sectionName : sectionNames) {

			// Obtain the section
			OfficeSubSection section = this.getOfficeSubSection(sectionName);

			// Govern the section
			section.addGovernance(officeGovernance);
		}
	}

	/**
	 * Records {@link Governance} for the {@link ManagedObject}.
	 * 
	 * @param governanceName
	 *            Name of the {@link Governance}.
	 * @param sectionName
	 *            {@link OfficeSubSection} name to contain a
	 *            {@link ManagedObject} for {@link Governance}.
	 * @param extensionInterface
	 *            Extension interface of the {@link ManagedObject} for the
	 *            section. <code>null</code> indicates no {@link ManagedObject}
	 *            for section.
	 * @param isGovern
	 *            Identifies if the {@link OfficeSectionManagedObject} should be
	 *            under {@link Governance}.
	 */
	private void recordGovernManagedObject(String governanceName,
			String sectionName, Class<?> extensionInterface, boolean isGovern) {

		// Obtain the section and its type
		OfficeSubSection section = this.getOfficeSubSection(sectionName);
		OfficeSubSectionType sectionType = this
				.getOfficeSubSectionType(sectionName);

		// Record section managed object
		if (extensionInterface == null) {
			// No managed object for section
			this.recordReturn(sectionType,
					sectionType.getOfficeSectionManagedObjectTypes(),
					new OfficeSectionManagedObjectType[0]);

		} else {
			// Managed object for section
			final String MO_NAME = "MO_NAME";
			OfficeSectionManagedObjectType moType = this
					.createMock(OfficeSectionManagedObjectType.class);
			this.recordReturn(sectionType,
					sectionType.getOfficeSectionManagedObjectTypes(),
					new OfficeSectionManagedObjectType[] { moType });
			this.recordReturn(moType, moType.getSupportedExtensionInterfaces(),
					new Class<?>[] { extensionInterface });

			// Provide governance if expected to be under governance
			OfficeGovernance governance = this.governances.get(governanceName);
			assertNotNull("Unknown governance " + governanceName);
			if (isGovern) {

				// Record obtaining the section managed object
				this.recordReturn(moType,
						moType.getOfficeSectionManagedObjectName(), MO_NAME);
				OfficeSectionManagedObject mo = this
						.createMock(OfficeSectionManagedObject.class);
				this.recordReturn(section,
						section.getOfficeSectionManagedObject(MO_NAME), mo);

				// Govern the managed object
				governance.governManagedObject(mo);
			}
		}
	}

	/**
	 * Records {@link Governance} for the {@link OfficeObject}.
	 * 
	 * @param governanceName
	 *            Name of the {@link Governance}.
	 * @param autoWire
	 *            {@link OfficeObject} {@link AutoWire} to be provided
	 *            {@link Governance}.
	 */
	private void recordGovernOfficeObject(String governanceName,
			AutoWire... autoWiring) {
		OfficeGovernance governance = this.governances.get(governanceName);
		for (AutoWire autoWire : autoWiring) {
			OfficeObject officeObject = this.dependencies.get(autoWire
					.getQualifiedType());

			// Record governing the Office Object
			governance.governManagedObject(officeObject);
		}
	}

	/**
	 * Records {@link Governance} for the {@link SubSection}.
	 * 
	 * @param sectionName
	 *            {@link OfficeSection} name.
	 * @param subSectionNames
	 *            {@link SubSection} names.
	 */
	private void recordGovernSubSections(String sectionName,
			String... subSectionNames) {
		OfficeSubSection section = this.getOfficeSubSection(sectionName);
		OfficeSubSectionType sectionType = this
				.getOfficeSubSectionType(sectionName);
		OfficeSubSectionType[] subSectionTypes = new OfficeSubSectionType[subSectionNames.length];
		for (int i = 0; i < subSectionTypes.length; i++) {
			subSectionTypes[i] = this.createMock(OfficeSubSectionType.class);
			this.subSectionTypes.put(subSectionNames[i], subSectionTypes[i]);
		}
		this.recordReturn(sectionType, sectionType.getOfficeSubSectionTypes(),
				subSectionTypes);
		for (int i = 0; i < subSectionTypes.length; i++) {
			String subSectionName = subSectionNames[i];
			this.recordReturn(sectionType, sectionType.getOfficeSectionName(),
					subSectionName);
			OfficeSubSection subSection = this
					.getOfficeSubSection(subSectionName);
			this.recordReturn(section,
					section.getOfficeSubSection(subSectionName), subSection);
		}
	}

	/**
	 * Records the {@link OfficeSectionInput} instances.
	 * 
	 * @param sectionName
	 *            Name of {@link OfficeSection}.
	 * @param inputNames
	 *            Names of the inputs.
	 */
	private void recordSectionInputs(String sectionName, String... inputNames) {

		// Obtain the section and its type
		OfficeSection section = this.getOfficeSection(sectionName);
		OfficeSectionType sectionType = this.getOfficeSectionType(sectionName);

		// Record adding the section inputs
		OfficeSectionInput[] inputs = this.createSectionItems(sectionName,
				OfficeSectionInput.class, this.inputs, inputNames);
		OfficeSectionInputType[] inputTypes = this.createSectionItems(
				sectionName, OfficeSectionInputType.class, this.inputTypes,
				inputNames);
		this.recordReturn(sectionType,
				sectionType.getOfficeSectionInputTypes(), inputTypes);
		for (int i = 0; i < inputNames.length; i++) {
			this.recordReturn(inputTypes[i],
					inputTypes[i].getOfficeSectionInputName(), inputNames[i]);
			this.recordReturn(section,
					section.getOfficeSectionInput(inputNames[i]), inputs[i]);
		}
	}

	/**
	 * Records the {@link OfficeSectionTask} instances.
	 * 
	 * @param sectionName
	 *            Name of {@link OfficeSection}.
	 * @param taskNames
	 *            Names of the tasks.
	 */
	private void recordSectionTasks(String sectionName) {
		OfficeSectionType sectionType = this.getOfficeSectionType(sectionName);
		this.recordReturn(sectionType, sectionType.getOfficeTaskTypes(),
				new OfficeTaskType[0]);
	}

	/**
	 * Records the {@link OfficeSectionOutput} instances.
	 * 
	 * @param sectionName
	 *            Name of {@link OfficeSection}.
	 * @param outputNames
	 *            Names of the outputs.
	 */
	private void recordSectionOutputs(String sectionName, String... outputNames) {

		// Obtain the section and its type
		OfficeSection section = this.getOfficeSection(sectionName);
		OfficeSectionType sectionType = this.getOfficeSectionType(sectionName);

		// Record adding the section outputs
		OfficeSectionOutput[] outputs = this.createSectionItems(sectionName,
				OfficeSectionOutput.class, this.outputs, outputNames);
		OfficeSectionOutputType[] outputTypes = this.createSectionItems(
				sectionName, OfficeSectionOutputType.class, this.outputTypes,
				outputNames);
		this.recordReturn(sectionType,
				sectionType.getOfficeSectionOutputTypes(), outputTypes);
		for (int i = 0; i < outputNames.length; i++) {
			this.recordReturn(outputTypes[i],
					outputTypes[i].getOfficeSectionOutputName(), outputNames[i]);
			this.recordReturn(section,
					section.getOfficeSectionOutput(outputNames[i]), outputs[i]);
		}
	}

	/**
	 * Records unlinked {@link OfficeSectionOutput}.
	 * 
	 * @param sectionName
	 *            Name of {@link OfficeSection}.
	 * @param outputName
	 *            Name of {@link OfficeSectionOutput}.
	 * @return {@link OfficeSectionOutputType}.
	 */
	private OfficeSectionOutputType recordSectionUnlinkedOutput(
			String sectionName, String outputName) {
		OfficeSectionType sectionType = this.getOfficeSectionType(sectionName);
		OfficeSectionOutputType outputType = this
				.createMock(OfficeSectionOutputType.class);
		this.recordReturn(sectionType,
				sectionType.getOfficeSectionOutputTypes(),
				new OfficeSectionOutputType[] { outputType });
		this.recordReturn(outputType, outputType.getOfficeSectionOutputName(),
				outputName);
		return outputType;
	}

	/**
	 * Records the {@link OfficeSectionObject} instances.
	 * 
	 * @param sectionName
	 *            Name of {@link OfficeSection}.
	 * @param autoWiring
	 *            {@link ExpectedAutoWire} instances.
	 */
	private void recordSectionObjects(String sectionName,
			ExpectedAutoWire... autoWiring) {

		// Create names of the objects
		String[] objectNames = new String[autoWiring.length];
		for (int i = 0; i < autoWiring.length; i++) {
			objectNames[i] = autoWiring[i].requestedAutoWire.getQualifiedType();
		}
		OfficeSectionObject[] objects = this.createSectionItems(sectionName,
				OfficeSectionObject.class, this.objects, objectNames);
		OfficeSectionObjectType[] objectTypes = this.createSectionItems(
				sectionName, OfficeSectionObjectType.class, this.objectTypes,
				objectNames);

		// Obtain the section and its type
		OfficeSection section = this.getOfficeSection(sectionName);
		OfficeSectionType sectionType = this.getOfficeSectionType(sectionName);

		// Record obtain the object types
		this.recordReturn(sectionType,
				sectionType.getOfficeSectionObjectTypes(), objectTypes);

		// Record obtaining the objects
		for (int i = 0; i < autoWiring.length; i++) {
			ExpectedAutoWire autoWire = autoWiring[i];
			OfficeSectionObject object = objects[i];
			OfficeSectionObjectType objectType = objectTypes[i];
			String objectName = objectNames[i];

			// Record obtain the object type information
			String usedType = autoWire.usedAutoWire.getType();
			this.recordReturn(objectType, objectType.getObjectType(), usedType);
			String usedQualifier = autoWire.usedAutoWire.getQualifier();
			this.recordReturn(objectType, objectType.getTypeQualifier(),
					usedQualifier);
			this.recordReturn(objectType,
					objectType.getOfficeSectionObjectName(), objectName);

			// Record obtaining the object
			this.recordReturn(section,
					section.getOfficeSectionObject(objectName), object);

			// Lazy add the dependency
			String usedName = autoWire.usedAutoWire.getQualifiedType();
			OfficeObject dependency = this.dependencies.get(usedName);
			if (dependency == null) {
				// Add the dependency from the office
				dependency = this.createMock(OfficeObject.class);
				this.dependencies.put(usedName, dependency);
				this.recordReturn(this.architect,
						this.architect.addOfficeObject(usedName, usedType),
						dependency);
				if (usedQualifier != null) {
					dependency.setTypeQualifier(usedQualifier);
				}
			}

			// Link the object to dependency
			this.architect.link(object, dependency);
		}
	}

	/**
	 * Records the {@link OfficeSubSection} instances.
	 * 
	 * @param sectionName
	 *            Name of the {@link OfficeSection} or {@link OfficeSubSection}.
	 * @param subSectionNames
	 *            Names of the {@link OfficeSubSection}.
	 */
	private void recordSubSections(String sectionName,
			String... subSectionNames) {

		// Obtain the section and its type
		OfficeSubSection section = this.getOfficeSubSection(sectionName);
		OfficeSubSectionType sectionType = this
				.getOfficeSubSectionType(sectionName);

		// Record obtaining the sub section types
		OfficeSubSectionType[] subSectionTypes = new OfficeSubSectionType[subSectionNames.length];
		for (int i = 0; i < subSectionNames.length; i++) {
			String subSectionName = subSectionNames[i];

			// Create the sub section type
			OfficeSubSectionType subSectionType = this.subSectionTypes
					.get(subSectionName);
			if (subSectionType == null) {
				subSectionType = this.createMock(OfficeSubSectionType.class);
				this.subSectionTypes.put(subSectionName, subSectionType);
			}
			subSectionTypes[i] = subSectionType;
		}
		this.recordReturn(sectionType, sectionType.getOfficeSubSectionTypes(),
				subSectionTypes);

		// Record obtaining the sub sections
		for (int i = 0; i < subSectionNames.length; i++) {
			String subSectionName = subSectionNames[i];
			OfficeSubSectionType subSectionType = subSectionTypes[i];

			// Record obtaining sub section name
			this.recordReturn(subSectionType,
					subSectionType.getOfficeSectionName(), subSectionName);

			// Create the sub section
			assertNull("Sub section already registered " + subSectionName,
					this.subSections.get(subSectionName));
			OfficeSubSection subSection = this
					.createMock(OfficeSubSection.class);
			this.subSections.put(subSectionName, subSection);

			// Record obtaining the sub section
			this.recordReturn(section,
					section.getOfficeSubSection(subSectionName), subSection);

			// Record no tasks for section
			this.recordReturn(subSectionType,
					subSectionType.getOfficeTaskTypes(), new OfficeTaskType[0]);
		}
	}

	/**
	 * Records handling of the {@link Escalation}.
	 * 
	 * @param escalationType
	 *            Type of {@link Escalation}.
	 * @param sectionName
	 *            {@link AutoWireSection} name.
	 * @param inputName
	 *            Name of the {@link OfficeSectionInput}.
	 */
	private void recordEscalation(Class<? extends Throwable> escalationType,
			String sectionName, String inputName) {
		OfficeEscalation escalation = this.createMock(OfficeEscalation.class);
		this.recordReturn(this.architect,
				this.architect.addOfficeEscalation(escalationType.getName()),
				escalation);
		OfficeSectionInput sectionInput = this.inputs.get(sectionName).get(
				inputName);
		this.architect.link(escalation, sectionInput);
	}

	/**
	 * Records adding an {@link OfficeStart}.
	 * 
	 * @param sectionName
	 *            {@link AutoWireSection} name.
	 * @param inputName
	 *            Name of the {@link OfficeSectionInput}.
	 */
	private void recordStartupFlow(String sectionName, String inputName) {
		OfficeStart start = this.createMock(OfficeStart.class);
		this.recordReturn(this.architect,
				this.architect.addOfficeStart(sectionName + "-" + inputName),
				start);
		OfficeSectionInput sectionInput = this.inputs.get(sectionName).get(
				inputName);
		this.architect.link(start, sectionInput);
	}

	/**
	 * Records the {@link OfficeSectionInput} instances.
	 * 
	 * @param sectionName
	 *            Name of {@link OfficeSection}.
	 * @param itemType
	 *            Item type.
	 * @param items
	 *            Existing items.
	 * @param itemNames
	 *            Names of the items.
	 */
	@SuppressWarnings("unchecked")
	private <T> T[] createSectionItems(String sectionName, Class<T> itemType,
			Map<String, Map<String, T>> items, String... itemNames) {
		assertNull("Already obtained  " + itemType.getSimpleName()
				+ " for section " + sectionName, items.get(sectionName));

		// Create and register the items
		Map<String, T> entries = new HashMap<String, T>();
		items.put(sectionName, entries);
		List<T> list = new LinkedList<T>();
		for (String itemName : itemNames) {
			T item = this.createMock(itemType);
			entries.put(itemName, item);
			list.add(item);
		}

		// Return the listing of items
		return list.toArray((T[]) Array.newInstance(itemType, list.size()));
	}

	/**
	 * Expected {@link AutoWire}.
	 */
	private static class ExpectedAutoWire {

		/**
		 * Requested {@link AutoWire}.
		 */
		public final AutoWire requestedAutoWire;

		/**
		 * Used {@link AutoWire}.
		 */
		public final AutoWire usedAutoWire;

		/**
		 * Initiate.
		 * 
		 * @param requestedQualifier
		 *            Requested Qualifier.
		 * @param usedQualifier
		 *            Used Qualifier.
		 * @param type
		 *            Type.
		 */
		public ExpectedAutoWire(String requestedQualifier,
				String usedQualifier, Class<?> type) {
			this.requestedAutoWire = new AutoWire(requestedQualifier,
					type.getName());
			this.usedAutoWire = new AutoWire(usedQualifier, type.getName());
		}

		/**
		 * Initiate.
		 * 
		 * @param type
		 *            Type.
		 */
		public ExpectedAutoWire(Class<?> type) {
			this(null, null, type);
		}
	}

}