/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

package net.officefloor.plugin.autowire;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.OfficeSubSection;
import net.officefloor.compile.spi.office.OfficeTask;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.office.source.impl.AbstractOfficeSource;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;

/**
 * {@link OfficeSource} implementation that auto-wires the configuration based
 * on type.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireOfficeSource extends AbstractOfficeSource {

	/**
	 * {@link OfficeFloorCompiler}.
	 */
	private final OfficeFloorCompiler compiler;

	/**
	 * {@link AutoWireSection} instances.
	 */
	private final List<AutoWireSection> sections = new LinkedList<AutoWireSection>();

	/**
	 * {@link Link} instances.
	 */
	private final List<Link> links = new LinkedList<Link>();

	/**
	 * Initiate.
	 * 
	 * @param compiler
	 *            {@link OfficeFloorCompiler}.
	 */
	public AutoWireOfficeSource(OfficeFloorCompiler compiler) {
		this.compiler = compiler;
	}

	/**
	 * Default constructor.
	 */
	public AutoWireOfficeSource() {
		this(OfficeFloorCompiler.newOfficeFloorCompiler());
	}

	/**
	 * Adds an {@link OfficeSection}.
	 * 
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param sectionSourceClass
	 *            {@link SectionSource} class.
	 * @param sectionLocation
	 *            {@link OfficeSection} location.
	 * @return {@link AutoWireSection} to configure properties and link flows.
	 */
	public <S extends SectionSource> AutoWireSection addSection(
			String sectionName, Class<S> sectionSourceClass,
			String sectionLocation) {

		// Create the properties
		PropertyList properties = this.compiler.createPropertyList();

		// Create and add the section
		AutoWireSection section = new AutoWireSection(sectionName,
				sectionSourceClass, sectionLocation, properties);
		this.sections.add(section);

		// Return the section
		return section;
	}

	/**
	 * Links the source {@link SectionOutput} to a target {@link SectionInput}.
	 * 
	 * @param sourceSection
	 *            Source section.
	 * @param sourceOutputName
	 *            Name of the source {@link SectionOutput}.
	 * @param targetSection
	 *            Target section.
	 * @param targetInputName
	 *            Name of the target {@link SectionInput}.
	 */
	public void link(AutoWireSection sourceSection, String sourceOutputName,
			AutoWireSection targetSection, String targetInputName) {
		this.links.add(new Link(sourceSection, sourceOutputName, targetSection,
				targetInputName));
	}

	/**
	 * <p>
	 * Determines if the {@link AutoWireSection} output is configured for
	 * linking.
	 * <p>
	 * This aids configuration by allowing to know if {@link SectionOutput}
	 * flows have been configured (linked).
	 * 
	 * @param section
	 *            {@link AutoWireSection}.
	 * @param sectionOutputName
	 *            {@link SectionOutput} name.
	 * @return <code>true</code> if configured for linking, otherwise
	 *         <code>false</code>.
	 */
	public boolean isLinked(AutoWireSection section, String sectionOutputName) {

		// Determine if linked
		for (Link link : this.links) {
			if ((link.sourceSection.getSectionName().equals(section
					.getSectionName()))
					&& (link.sourceOutputName.equals(sectionOutputName))) {
				// Matching section output so configured for linking
				return true;
			}
		}

		// As here, not linked
		return false;
	}

	/*
	 * ===================== OfficeSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	public void sourceOffice(OfficeArchitect architect,
			OfficeSourceContext context) throws Exception {

		// Obtain the class loader
		ClassLoader classLoader = context.getClassLoader();

		// Add the team
		OfficeTeam team = architect.addOfficeTeam("team");

		// Load the sections
		Map<String, Map<String, OfficeSectionInput>> inputs = new HashMap<String, Map<String, OfficeSectionInput>>();
		Map<String, Map<String, OfficeSectionOutput>> outputs = new HashMap<String, Map<String, OfficeSectionOutput>>();
		Map<Class<?>, OfficeObject> objects = new HashMap<Class<?>, OfficeObject>();
		for (AutoWireSection section : this.sections) {

			// Obtain the section name
			String sectionName = section.getSectionName();

			// Add the section
			OfficeSection officeSection = architect.addOfficeSection(
					sectionName, section.getSectionSourceClass().getName(),
					section.getSectionLocation(),
					section.getSectionProperties());

			// Link section tasks to team
			for (OfficeTask task : officeSection.getOfficeTasks()) {
				architect.link(task.getTeamResponsible(), team);
			}

			// Link sub section tasks to team
			for (OfficeSubSection subSection : officeSection
					.getOfficeSubSections()) {
				this.linkTasksToTeams(subSection, team, architect);
			}

			// Link the objects
			for (OfficeSectionObject object : officeSection
					.getOfficeSectionObjects()) {
				String objectType = object.getObjectType();
				Class<?> objectClass = classLoader.loadClass(objectType);
				OfficeObject officeObject = objects.get(objectClass);
				if (officeObject == null) {
					officeObject = architect.addOfficeObject(objectType,
							objectType);
					objects.put(objectClass, officeObject);
				}
				architect.link(object, officeObject);
			}

			// Register the inputs
			Map<String, OfficeSectionInput> sectionInputs = new HashMap<String, OfficeSectionInput>();
			inputs.put(sectionName, sectionInputs);
			for (OfficeSectionInput input : officeSection
					.getOfficeSectionInputs()) {
				String inputName = input.getOfficeSectionInputName();
				sectionInputs.put(inputName, input);
			}

			// Register the outputs
			Map<String, OfficeSectionOutput> sectionOutputs = new HashMap<String, OfficeSectionOutput>();
			outputs.put(sectionName, sectionOutputs);
			for (OfficeSectionOutput output : officeSection
					.getOfficeSectionOutputs()) {
				String outputName = output.getOfficeSectionOutputName();
				sectionOutputs.put(outputName, output);
			}
		}

		// Link outputs to inputs
		for (Link link : this.links) {

			// Obtain the link details
			String sourceSectionName = link.sourceSection.getSectionName();
			String sourceOutputName = link.sourceOutputName;
			String outputName = sourceSectionName + ":" + sourceOutputName;
			String targetSectionName = link.targetSection.getSectionName();
			String targetInputName = link.targetInputName;
			String inputName = targetSectionName + ":" + targetInputName;

			// Obtain the output
			OfficeSectionOutput sectionOutput = null;
			Map<String, OfficeSectionOutput> sectionOutputs = outputs
					.get(sourceSectionName);
			if (sectionOutputs != null) {
				sectionOutput = sectionOutputs.get(sourceOutputName);
			}
			if (sectionOutput == null) {
				architect.addIssue("Unknown section output '" + outputName
						+ "' to link to section input '" + inputName + "'",
						AssetType.TASK, outputName);
				continue; // no output so can not link
			}

			// Obtain the input
			OfficeSectionInput sectionInput = null;
			Map<String, OfficeSectionInput> sectionInputs = inputs
					.get(targetSectionName);
			if (sectionInputs != null) {
				sectionInput = sectionInputs.get(targetInputName);
			}
			if (sectionInput == null) {
				architect.addIssue("Unknown section input '" + inputName
						+ "' for linking section output '" + outputName + "'",
						AssetType.TASK, inputName);
				continue; // no input so can not link
			}

			// Link the output to the input
			architect.link(sectionOutput, sectionInput);
		}
	}

	/**
	 * Links the {@link OfficeTask} instances of the {@link OfficeSubSection} to
	 * the {@link OfficeTeam}.
	 * 
	 * @param subSection
	 *            {@link OfficeSubSection}.
	 * @param team
	 *            {@link OfficeTeam}.
	 * @param architect
	 *            {@link OfficeArchitect}.
	 */
	private void linkTasksToTeams(OfficeSubSection subSection, OfficeTeam team,
			OfficeArchitect architect) {

		// Link section tasks to team
		for (OfficeTask task : subSection.getOfficeTasks()) {
			architect.link(task.getTeamResponsible(), team);
		}

		// Recursively link the sub sections
		for (OfficeSubSection subSubSection : subSection.getOfficeSubSections()) {
			this.linkTasksToTeams(subSubSection, team, architect);
		}
	}

	/**
	 * Link of {@link SectionOutput} to {@link SectionInput}.
	 */
	private static class Link {

		/**
		 * Source {@link AutoWireSection}.
		 */
		private final AutoWireSection sourceSection;

		/**
		 * Source {@link SectionOutput} name.
		 */
		private final String sourceOutputName;

		/**
		 * Target {@link AutoWireSection}.
		 */
		private final AutoWireSection targetSection;

		/**
		 * Target {@link SectionInput} name.
		 */
		private final String targetInputName;

		/**
		 * Initiate.
		 * 
		 * @param sourceSection
		 *            Source {@link AutoWireSection}.
		 * @param sourceOutputName
		 *            Source {@link SectionOutput} name.
		 * @param targetSection
		 *            Target {@link AutoWireSection}.
		 * @param targetInputName
		 *            Target {@link SectionInput} name.
		 */
		public Link(AutoWireSection sourceSection, String sourceOutputName,
				AutoWireSection targetSection, String targetInputName) {
			this.sourceSection = sourceSection;
			this.sourceOutputName = sourceOutputName;
			this.targetSection = targetSection;
			this.targetInputName = targetInputName;
		}
	}

}