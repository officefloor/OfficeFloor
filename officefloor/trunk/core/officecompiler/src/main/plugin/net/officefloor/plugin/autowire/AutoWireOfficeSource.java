/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
import net.officefloor.compile.spi.section.source.SectionSource;

/**
 * {@link OfficeSource} implementation that auto-wires the configuration based
 * on type.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireOfficeSource extends AbstractOfficeSource {

	/**
	 * {@link Section} instances.
	 */
	private final List<Section> sections = new LinkedList<Section>();

	/**
	 * Adds an {@link OfficeSection}.
	 * 
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param sectionSourceClassName
	 *            {@link SectionSource} class name.
	 * @param sectionLocation
	 *            {@link OfficeSection} location.
	 * @return {@link PropertyList} to configure properties for the
	 *         {@link OfficeSection}.
	 */
	public PropertyList addSection(String sectionName,
			String sectionSourceClassName, String sectionLocation) {

		// Create the properties
		PropertyList properties = OfficeFloorCompiler.newPropertyList();

		// Create and add the section
		Section section = new Section(sectionName, sectionSourceClassName,
				sectionLocation, properties);
		this.sections.add(section);

		// Return the properties for the section
		return properties;
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
		Map<Class<?>, List<OfficeSectionInput>> inputs = new HashMap<Class<?>, List<OfficeSectionInput>>();
		Map<Class<?>, List<OfficeSectionOutput>> outputs = new HashMap<Class<?>, List<OfficeSectionOutput>>();
		Map<Class<?>, OfficeObject> objects = new HashMap<Class<?>, OfficeObject>();
		for (Section section : this.sections) {

			// Add the section
			OfficeSection officeSection = architect.addOfficeSection(
					section.name, section.sourceClassName, section.location,
					section.properties);

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
			for (OfficeSectionInput input : officeSection
					.getOfficeSectionInputs()) {
				String parameterType = input.getParameterType();
				if ((parameterType != null) && (parameterType.length() > 0)) {
					Class<?> parameterClass = classLoader
							.loadClass(parameterType);
					List<OfficeSectionInput> list = inputs.get(parameterClass);
					if (list == null) {
						list = new LinkedList<OfficeSectionInput>();
						inputs.put(parameterClass, list);
					}
					list.add(input);
				}
			}

			// Register the outputs
			for (OfficeSectionOutput output : officeSection
					.getOfficeSectionOutputs()) {
				String argumentType = output.getArgumentType();
				if ((argumentType != null) && (argumentType.length() > 0)) {
					Class<?> argumentClass = classLoader
							.loadClass(argumentType);
					List<OfficeSectionOutput> list = outputs.get(argumentClass);
					if (list == null) {
						list = new LinkedList<OfficeSectionOutput>();
						outputs.put(argumentClass, list);
					}
					list.add(output);
				}
			}
		}

		// Link outputs to inputs
		for (Class<?> outputType : outputs.keySet()) {
			List<OfficeSectionOutput> outputList = outputs.get(outputType);
			for (OfficeSectionOutput output : outputList) {

				// Ignore escalations
				if (output.isEscalationOnly()) {
					continue;
				}

				// Obtain the input to handle the output
				List<OfficeSectionInput> inputList = inputs.get(outputType);
				if (inputList != null) {

					// Ensure not ambiguous link on type
					if (inputList.size() > 1) {
						throw new AmbiguousException(
								"More than one input for output type "
										+ outputType.getName());
					}

					// Link output to input
					OfficeSectionInput input = inputList.get(0);
					architect.link(output, input);
				}
			}
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
	 * Section.
	 */
	private static class Section {

		/**
		 * Name of section.
		 */
		private final String name;

		/**
		 * {@link SectionSource} class name.
		 */
		private final String sourceClassName;

		/**
		 * Location of section.
		 */
		private final String location;

		/**
		 * Properties for the section.
		 */
		private final PropertyList properties;

		/**
		 * Initiate.
		 * 
		 * @param name
		 *            Name of section.
		 * @param sourceClassName
		 *            {@link SectionSource} class name.
		 * @param location
		 *            Location of section.
		 * @param properties
		 *            Properties for the section.
		 */
		public Section(String name, String sourceClassName, String location,
				PropertyList properties) {
			this.name = name;
			this.sourceClassName = sourceClassName;
			this.location = location;
			this.properties = properties;
		}
	}

}